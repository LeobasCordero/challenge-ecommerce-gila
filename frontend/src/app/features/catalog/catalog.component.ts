import { Component, OnInit, inject, signal, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ProductsService } from '../../core/api/api/products.service';
import { CartService as CartApiService } from '../../core/api/api/cart.service';
import { OrdersService } from '../../core/api/api/orders.service';
import { ProductDto } from '../../core/api/model/productDto';
import { CartItemDto } from '../../core/api/model/cartItemDto';
import { CartStateService } from '../../core/state/cart-state.service';
import { AuthStateService } from '../../core/state/auth-state.service';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatSnackBarModule
  ],
  templateUrl: './catalog.component.html'
})
export class CatalogComponent implements OnInit {
  private readonly productsService = inject(ProductsService);
  private readonly cartApiService = inject(CartApiService);
  private readonly ordersService = inject(OrdersService);
  public readonly cartState = inject(CartStateService);
  public readonly authState = inject(AuthStateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly platformId = inject(PLATFORM_ID);

  public searchQuery = '';
  public selectedCategory = '';
  public readonly products = signal<Array<ProductDto>>([]);
  public readonly categories = signal<Array<string>>([]);
  public readonly isLoading = signal<boolean>(false);
  public readonly isCartOpen = signal<boolean>(false);
  public readonly isAddingToCart = signal<boolean>(false);
  public readonly isUpdatingCart = signal<boolean>(false);
  public readonly isCheckingOut = signal<boolean>(false);

  public ngOnInit(): void {
    // Only execute search/query operations in browser environment (not SSR)
    if (isPlatformBrowser(this.platformId)) {
      this.route.queryParams.subscribe(params => {
        if (params['cartOpen'] === 'true') {
          this.isCartOpen.set(true);
        }
      });

      this.loadProducts();
      this.loadCategories();
    }
  }

  /** Load paginated products from backend query endpoint. */
  public loadProducts(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    this.isLoading.set(true);
    this.productsService.getProducts(this.searchQuery, this.selectedCategory).subscribe({
      next: (data) => {
        this.products.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.snackBar.open('Error loading products. Please try again.', 'Close', { duration: 3000 });
        console.error('Error fetching products:', err);
      }
    });
  }

  /** Load unique category list from backend product catalog. */
  public loadCategories(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    this.productsService.getProducts().subscribe({
      next: (data) => {
        const uniqueCategories = Array.from(new Set(data.map(p => p.category).filter(Boolean) as string[]));
        this.categories.set(uniqueCategories);
      }
    });
  }

  public onSearchChange(): void {
    this.loadProducts();
  }

  public onFilterChange(): void {
    this.loadProducts();
  }

  /** Add item to cart via Redis-backed backend API. */
  public addToCart(product: ProductDto): void {
    if (!this.authState.isAuthenticated()) {
      this.snackBar.open('Please login to add items to your cart.', 'Login', { duration: 3000 })
        .onAction().subscribe(() => this.router.navigate(['/login']));
      return;
    }

    this.isAddingToCart.set(true);
    const existingItem = this.cartState.cart()?.items?.find(i => i.product?.id === product.id);
    const newQty = (existingItem?.quantity ?? 0) + 1;

    if (!product.id) {
      this.isAddingToCart.set(false);
      this.snackBar.open('Cannot add item: product ID is missing.', 'Close', { duration: 3000 });
      return;
    }

    this.cartApiService.updateCartItem({ productId: product.id, quantity: newQty }).subscribe({
      next: (cart) => {
        this.cartState.setCart(cart);
        this.isAddingToCart.set(false);
        this.snackBar.open(`${product.name} added to cart!`, 'View Cart', { duration: 2500 })
          .onAction().subscribe(() => this.isCartOpen.set(true));
      },
      error: (err) => {
        this.isAddingToCart.set(false);
        const msg = err?.error?.detail ?? 'Could not add item. Please try again.';
        this.snackBar.open(msg, 'Close', { duration: 3000 });
      }
    });
  }

  /** Change the quantity of a cart item up or down by a delta. */
  public changeQuantity(item: CartItemDto, delta: number): void {
    const newQty = (item.quantity ?? 0) + delta;
    if (!item.product?.id) return;

    this.isUpdatingCart.set(true);
    this.cartApiService.updateCartItem({ productId: item.product.id, quantity: newQty }).subscribe({
      next: (cart) => {
        this.cartState.setCart(cart);
        this.isUpdatingCart.set(false);
      },
      error: (err) => {
        this.isUpdatingCart.set(false);
        const msg = err?.error?.detail ?? 'Could not update cart.';
        this.snackBar.open(msg, 'Close', { duration: 3000 });
      }
    });
  }

  /** Remove a specific item from the cart. */
  public removeFromCart(item: CartItemDto): void {
    if (!item.product?.id) return;

    this.isUpdatingCart.set(true);
    this.cartApiService.removeCartItem(item.product.id).subscribe({
      next: (cart) => {
        this.cartState.setCart(cart);
        this.isUpdatingCart.set(false);
      },
      error: () => {
        this.isUpdatingCart.set(false);
        this.snackBar.open('Could not remove item.', 'Close', { duration: 3000 });
      }
    });
  }

  /** Submit checkout order and redirect to success page on completion. */
  public checkout(): void {
    this.isCheckingOut.set(true);
    this.ordersService.checkout().subscribe({
      next: (order) => {
        this.cartState.clearCart();
        this.isCheckingOut.set(false);
        this.isCartOpen.set(false);
        this.router.navigate(['/checkout/success'], { state: { order } });
      },
      error: (err) => {
        this.isCheckingOut.set(false);
        const msg = err?.error?.detail ?? err?.error?.message ?? 'Checkout failed. Please try again.';
        this.snackBar.open(msg, 'Close', { duration: 5000 });
      }
    });
  }

  public closeCart(): void {
    this.isCartOpen.set(false);
  }
}
