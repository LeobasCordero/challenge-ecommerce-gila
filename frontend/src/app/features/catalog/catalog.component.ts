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
  template: `
    <div class="catalog-pagecontainer container">
      <!-- Search & Filters section -->
      <section class="filters-section">
        <div class="search-box">
          <mat-form-field appearance="outline" class="search-field">
            <mat-label i18n="@@searchPlaceholder">Search products...</mat-label>
            <input matInput [(ngModel)]="searchQuery" (input)="onSearchChange()" placeholder="Type product name or description...">
            <mat-icon matSuffix>search</mat-icon>
          </mat-form-field>
        </div>

        <div class="category-box">
          <mat-form-field appearance="outline" class="category-field">
            <mat-label i18n="@@categoryLabel">Category</mat-label>
            <mat-select [(ngModel)]="selectedCategory" (selectionChange)="onFilterChange()">
              <mat-option value="" i18n="@@allCategories">All Categories</mat-option>
              <mat-option *ngFor="let cat of categories()" [value]="cat">{{ cat }}</mat-option>
            </mat-select>
          </mat-form-field>
        </div>
      </section>

      <!-- Products catalog section -->
      <section class="catalog-products-section">
        <div class="spinner-container" *ngIf="isLoading()">
          <mat-spinner diameter="50"></mat-spinner>
        </div>

        <div class="empty-state" *ngIf="!isLoading() && products().length === 0">
          <mat-icon class="empty-icon">search_off</mat-icon>
          <p i18n="@@noProductsFound">No products found matching your criteria.</p>
        </div>

        <div class="grid grid-cols-1 grid-cols-2 grid-cols-3 grid-cols-4" *ngIf="!isLoading()">
          <mat-card class="card product-card" *ngFor="let product of products()">
            <div class="product-img-placeholder">
              <mat-icon class="product-icon">image</mat-icon>
            </div>
            
            <mat-card-header class="product-card-header">
              <mat-card-title class="card-title">{{ product.name }}</mat-card-title>
              <mat-card-subtitle class="product-category">{{ product.category }}</mat-card-subtitle>
            </mat-card-header>

            <mat-card-content class="product-card-content">
              <p class="card-description">{{ product.description }}</p>
              <div class="product-specs">
                <span class="stock-info" [class.low-stock]="(product.stock ?? 0) <= 5">
                  <span i18n="@@stockLabel">Stock:</span> {{ product.stock }}
                </span>
              </div>
            </mat-card-content>

            <mat-card-footer class="product-card-footer">
              <span class="product-price">\${{ (product.price ?? 0).toFixed(2) }}</span>
              <button mat-raised-button color="primary" id="add-to-cart-{{ product.id }}"
                class="add-to-cart-btn" (click)="addToCart(product)"
                [disabled]="(product.stock ?? 0) === 0 || isAddingToCart()">
                <mat-icon>add_shopping_cart</mat-icon>
                <span i18n="@@addToCartBtn">Add</span>
              </button>
            </mat-card-footer>
          </mat-card>
        </div>
      </section>

      <!-- Cart Sidebar Drawer -->
      <button class="cart-sidebar-overlay" [class.open]="isCartOpen()" (click)="closeCart()"
        aria-label="Close cart overlay" tabindex="-1"></button>
      <aside class="cart-sidebar" [class.open]="isCartOpen()" aria-label="Shopping cart">
        <div class="cart-sidebar-header">
          <h3 i18n="@@cartTitle">Shopping Cart</h3>
          <button mat-icon-button id="close-cart-btn" (click)="closeCart()" aria-label="Close cart">
            <mat-icon>close</mat-icon>
          </button>
        </div>
        <mat-divider></mat-divider>

        <div class="cart-sidebar-content">
          <!-- Empty state -->
          <div class="cart-empty" *ngIf="cartState.itemCount() === 0">
            <mat-icon class="cart-empty-icon">shopping_cart_off</mat-icon>
            <p i18n="@@cartEmptyMessage">Your cart is empty.</p>
          </div>

          <!-- Cart items list -->
          <div class="cart-items" *ngIf="cartState.itemCount() > 0">
            <div class="cart-item" *ngFor="let item of cartState.cart()?.items ?? []">
              <div class="cart-item-info">
                <span class="cart-item-name">{{ item.product?.name }}</span>
                <span class="cart-item-price">\${{ ((item.product?.price ?? 0) * (item.quantity ?? 0)).toFixed(2) }}</span>
              </div>
              <div class="cart-item-controls">
                <button mat-icon-button class="qty-btn" id="decrease-qty-{{ item.product?.id }}"
                  (click)="changeQuantity(item, -1)"
                  [disabled]="isUpdatingCart()"
                  aria-label="Decrease quantity">
                  <mat-icon>remove</mat-icon>
                </button>
                <span class="cart-item-qty">{{ item.quantity }}</span>
                <button mat-icon-button class="qty-btn" id="increase-qty-{{ item.product?.id }}"
                  (click)="changeQuantity(item, 1)"
                  [disabled]="isUpdatingCart() || (item.quantity ?? 0) >= (item.product?.stock ?? 0)"
                  aria-label="Increase quantity">
                  <mat-icon>add</mat-icon>
                </button>
                <button mat-icon-button class="remove-btn" id="remove-item-{{ item.product?.id }}"
                  (click)="removeFromCart(item)"
                  [disabled]="isUpdatingCart()"
                  aria-label="Remove item">
                  <mat-icon>delete_outline</mat-icon>
                </button>
              </div>
            </div>

            <mat-divider></mat-divider>

            <!-- Cart totals -->
            <div class="cart-totals">
              <span class="cart-total-label" i18n="@@cartTotalLabel">Total</span>
              <span class="cart-total-amount">\${{ cartState.totalPrice().toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- Cart footer with Checkout button -->
        <div class="cart-sidebar-footer">
          <button mat-raised-button color="primary" id="checkout-btn"
            class="checkout-btn" (click)="checkout()"
            [disabled]="cartState.itemCount() === 0 || isCheckingOut() || !authState.isAuthenticated()">
            <mat-spinner *ngIf="isCheckingOut()" diameter="18" class="checkout-spinner"></mat-spinner>
            <mat-icon *ngIf="!isCheckingOut()">shopping_bag</mat-icon>
            <span i18n="@@buyNowBtn">Buy Now</span>
          </button>
          <p *ngIf="!authState.isAuthenticated()" class="login-to-checkout" i18n="@@loginToCheckout">
            <a routerLink="/login">Login</a> to checkout
          </p>
        </div>
      </aside>
    </div>
  `,
  styles: [`
    .catalog-pagecontainer {
      padding-top: 32px;
      padding-bottom: 64px;
      position: relative;
    }
    .filters-section {
      display: flex;
      gap: 16px;
      margin-bottom: 32px;
      
      @media (max-width: 600px) {
        flex-direction: column;
        gap: 8px;
      }
    }
    .search-box {
      flex: 1;
    }
    .search-field, .category-field {
      width: 100%;
    }
    .spinner-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 200px;
    }
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 64px 0;
      color: #64748b;
    }
    .empty-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 16px;
    }
    .product-card {
      display: flex;
      flex-direction: column;
      height: 100%;
      box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
    }
    .product-img-placeholder {
      background-color: #f1f5f9;
      height: 160px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-top-left-radius: 12px;
      border-top-right-radius: 12px;
      color: #94a3b8;
    }
    .product-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
    }
    .product-card-header {
      padding: 16px 16px 8px 16px;
    }
    .product-category {
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: #059669;
      font-weight: 600;
    }
    .product-card-content {
      padding: 0 16px 16px 16px;
      flex-grow: 1;
    }
    .product-specs {
      margin-top: 12px;
      font-size: 13px;
    }
    .stock-info {
      color: #475569;
      font-weight: 500;
    }
    .low-stock {
      color: #ef4444;
      font-weight: 600;
    }
    .product-card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      background-color: #fafafa;
      border-top: 1px solid #f1f5f9;
    }
    .product-price {
      font-size: 18px;
      font-weight: 700;
      color: #0f172a;
    }
    .add-to-cart-btn {
      background-color: #059669 !important;
      color: white !important;
      border-radius: 6px;
    }
    .add-to-cart-btn[disabled] {
      background-color: #cbd5e1 !important;
      color: #64748b !important;
    }
    /* Cart Overlay */
    .cart-sidebar-overlay {
      display: none;
      position: fixed;
      inset: 0;
      background: rgba(0,0,0,0.35);
      z-index: 99;
      opacity: 0;
      transition: opacity 0.3s ease;
      /* button reset */
      border: none;
      padding: 0;
      margin: 0;
      cursor: default;
      border-radius: 0;
    }
    .cart-sidebar-overlay.open {
      display: block;
      opacity: 1;
    }
    /* Cart Sidebar */
    .cart-sidebar {
      position: fixed;
      top: 0;
      right: -380px;
      width: 380px;
      height: 100vh;
      background-color: white;
      box-shadow: -4px 0 24px rgba(0, 0, 0, 0.12);
      z-index: 100;
      transition: right 0.3s ease-in-out;
      display: flex;
      flex-direction: column;

      @media (max-width: 480px) {
        width: 100vw;
        right: -100vw;
      }
    }
    .cart-sidebar.open {
      right: 0;
    }
    .cart-sidebar-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
    }
    .cart-sidebar-header h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 700;
      color: #0f172a;
    }
    .cart-sidebar-content {
      padding: 16px 20px;
      flex-grow: 1;
      overflow-y: auto;
    }
    .cart-empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding-top: 64px;
      color: #94a3b8;
    }
    .cart-empty-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 12px;
    }
    /* Cart items */
    .cart-item {
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding: 12px 0;
      border-bottom: 1px solid #f1f5f9;
    }
    .cart-item-info {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
    }
    .cart-item-name {
      font-size: 14px;
      font-weight: 600;
      color: #0f172a;
      flex: 1;
      padding-right: 8px;
    }
    .cart-item-price {
      font-size: 14px;
      font-weight: 700;
      color: #059669;
      white-space: nowrap;
    }
    .cart-item-controls {
      display: flex;
      align-items: center;
      gap: 4px;
    }
    .qty-btn {
      width: 28px;
      height: 28px;
      line-height: 28px;
    }
    .qty-btn .mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }
    .cart-item-qty {
      font-size: 14px;
      font-weight: 600;
      min-width: 24px;
      text-align: center;
    }
    .remove-btn {
      margin-left: auto;
      color: #ef4444 !important;
    }
    /* Cart totals */
    .cart-totals {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 0 8px 0;
    }
    .cart-total-label {
      font-size: 16px;
      font-weight: 600;
      color: #334155;
    }
    .cart-total-amount {
      font-size: 20px;
      font-weight: 800;
      color: #0f172a;
    }
    /* Cart footer */
    .cart-sidebar-footer {
      padding: 16px 20px;
      border-top: 1px solid #e2e8f0;
    }
    .checkout-btn {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 700;
      background-color: #059669 !important;
      color: white !important;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }
    .checkout-btn[disabled] {
      background-color: #cbd5e1 !important;
      color: #64748b !important;
    }
    .checkout-spinner {
      display: inline-block;
    }
    .login-to-checkout {
      text-align: center;
      margin-top: 8px;
      font-size: 13px;
      color: #64748b;
    }
    .login-to-checkout a {
      color: #059669;
      font-weight: 600;
      text-decoration: none;
    }
  `]
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

    this.cartApiService.updateCartItem({ productId: product.id!, quantity: newQty }).subscribe({
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
