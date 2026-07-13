import { Component, OnInit, inject, signal, PLATFORM_ID, HostListener } from '@angular/core';
import { isPlatformBrowser, CommonModule, DOCUMENT } from '@angular/common';
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
import { Title, Meta } from '@angular/platform-browser';

import { ProductsService } from '../../core/api/api/products.service';
import { CartService as CartApiService } from '../../core/api/api/cart.service';
import { OrdersService } from '../../core/api/api/orders.service';
import { ProductDto } from '../../core/api/model/productDto';
import { CartItemDto } from '../../core/api/model/cartItemDto';
import { CartStateService } from '../../services/cart-state.service';
import { AuthStateService } from '../../services/auth-state.service';
import { TelemetryService } from '../../services/telemetry.service';
import { ERROR_MESSAGES, SUCCESS_MESSAGES, SNACKBAR_ACTIONS, APP_ROUTES } from '../../utils/constants';

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
  private readonly telemetryService = inject(TelemetryService);
  public readonly cartState = inject(CartStateService);
  public readonly authState = inject(AuthStateService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly titleService = inject(Title);
  private readonly metaService = inject(Meta);
  private readonly document = inject(DOCUMENT);

  public readonly routes = APP_ROUTES;

  public searchQuery = '';
  public selectedCategory = '';
  public readonly products = signal<Array<ProductDto>>([]);
  public readonly categories = signal<Array<string>>([]);
  public readonly isLoading = signal<boolean>(false);
  public readonly isCartOpen = signal<boolean>(false);
  public readonly isAddingToCart = signal<boolean>(false);
  public readonly isUpdatingCart = signal<boolean>(false);
  public readonly isCheckingOut = signal<boolean>(false);

  public currentPage = 0;
  public hasMoreProducts = true;

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
    if (this.isLoading()) return;

    this.isLoading.set(true);
    const pageSize = 12;

    this.productsService.getProducts(this.searchQuery, this.selectedCategory, this.currentPage, pageSize).subscribe({
      next: (data) => {
        if (this.currentPage === 0) {
          this.products.set(data);
        } else {
          this.products.update(current => [...current, ...data]);
        }
        
        this.updateSEOAndMicrodata(this.products());

        if (data.length < pageSize) {
          this.hasMoreProducts = false;
        } else {
          this.currentPage++;
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open(ERROR_MESSAGES.FETCH_PRODUCTS_FAILED, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
      }
    });
  }

  /** Update Title, Meta SEO crawlers information, and dynamic JSON-LD schema microdata. */
  private updateSEOAndMicrodata(productList: ProductDto[]): void {
    if (!isPlatformBrowser(this.platformId)) return;

    // 1. Title & Meta tag optimizations
    this.titleService.setTitle('Gila Store - Product Catalog');
    this.metaService.updateTag({ name: 'description', content: 'Explore our catalog of high-quality products. Check stock, read details, and purchase items directly.' });
    this.metaService.updateTag({ property: 'og:title', content: 'Gila Store - Product Catalog' });
    this.metaService.updateTag({ property: 'og:description', content: 'Explore our catalog of high-quality products. Check stock, read details, and purchase items directly.' });

    // 2. Structured JSON-LD microdata schema block
    const sldElementId = 'gila-jsonld-schema';
    let scriptElement = this.document.getElementById(sldElementId) as HTMLScriptElement;
    if (scriptElement) {
      scriptElement.remove();
    }

    scriptElement = this.document.createElement('script');
    scriptElement.id = sldElementId;
    scriptElement.type = 'application/ld+json';

    const itemListJson = {
      '@context': 'https://schema.org',
      '@type': 'ItemList',
      'numberOfItems': productList.length,
      'itemListElement': productList.map((product, index) => ({
        '@type': 'ListItem',
        'position': index + 1,
        'item': {
          '@type': 'Product',
          'name': product.name,
          'description': product.description,
          'image': 'https://placehold.co/600x400?text=' + encodeURIComponent(product.name),
          'offers': {
            '@type': 'Offer',
            'price': product.price,
            'priceCurrency': 'USD',
            'availability': product.stock > 0 ? 'https://schema.org/InStock' : 'https://schema.org/OutOfStock'
          }
        }
      }))
    };

    scriptElement.text = JSON.stringify(itemListJson);
    this.document.head.appendChild(scriptElement);
  }

  /** Load unique category list from backend product catalog. */
  public loadCategories(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    this.productsService.getProducts(undefined, undefined, 0, 1000).subscribe({
      next: (data) => {
        const uniqueCategories = Array.from(new Set(data.map(p => p.category).filter(Boolean) as string[]));
        this.categories.set(uniqueCategories);
      }
    });
  }

  private resetPaginationAndLoad(): void {
    this.currentPage = 0;
    this.hasMoreProducts = true;
    this.loadProducts();
  }

  public onSearchChange(): void {
    this.telemetryService.logEvent('SEARCH', { query: this.searchQuery });
    this.resetPaginationAndLoad();
  }

  public onFilterChange(): void {
    this.telemetryService.logEvent('FILTER_CATEGORY', { category: this.selectedCategory });
    this.resetPaginationAndLoad();
  }

  @HostListener('window:scroll', [])
  public onWindowScroll(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    if (this.isLoading() || !this.hasMoreProducts) return;

    const threshold = 200; // pixels from the bottom
    const position = window.innerHeight + window.scrollY;
    const height = document.documentElement.scrollHeight;

    if (position >= height - threshold) {
      this.loadProducts();
    }
  }

  /** Add item to cart via Redis-backed backend API. */
  public addToCart(product: ProductDto): void {
    if (!this.authState.isAuthenticated()) {
      this.snackBar.open(ERROR_MESSAGES.LOGIN_REQUIRED, SNACKBAR_ACTIONS.LOGIN, { duration: 3000 })
        .onAction().subscribe(() => this.router.navigate([this.routes.LOGIN]));
      return;
    }

    this.telemetryService.logEvent('ADD_TO_CART', { productId: product.id, productName: product.name });
    this.isAddingToCart.set(true);
    const existingItem = this.cartState.cart()?.items?.find(i => i.product?.id === product.id);
    const newQty = (existingItem?.quantity ?? 0) + 1;

    if (!product.id) {
      this.isAddingToCart.set(false);
      this.snackBar.open(ERROR_MESSAGES.MISSING_PRODUCT_ID, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
      return;
    }

    this.cartApiService.updateCartItem({ productId: product.id, quantity: newQty }).subscribe({
      next: (cart) => {
        this.cartState.setCart(cart);
        this.isAddingToCart.set(false);
        this.snackBar.open(SUCCESS_MESSAGES.ADD_TO_CART(product.name || ''), SNACKBAR_ACTIONS.VIEW_CART, { duration: 2500 })
          .onAction().subscribe(() => this.isCartOpen.set(true));
      },
      error: (err) => {
        this.isAddingToCart.set(false);
        const msg = err?.error?.detail ?? ERROR_MESSAGES.UPDATE_CART_FAILED;
        this.snackBar.open(msg, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
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
        const msg = err?.error?.detail ?? ERROR_MESSAGES.UPDATE_CART_FAILED;
        this.snackBar.open(msg, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
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
        this.snackBar.open(ERROR_MESSAGES.REMOVE_CART_ITEM_FAILED, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
      }
    });
  }

  /** Submit checkout order and redirect to success page on completion. */
  public checkout(): void {
    this.isCheckingOut.set(true);
    this.ordersService.checkout().subscribe({
      next: (order) => {
        this.telemetryService.logEvent('CHECKOUT_SUCCESS', { orderId: order.id, totalPrice: order.totalPrice });
        this.cartState.clearCart();
        this.isCheckingOut.set(false);
        this.isCartOpen.set(false);
        this.router.navigate([this.routes.CHECKOUT_SUCCESS], { state: { order } });
      },
      error: (err) => {
        this.telemetryService.logEvent('CHECKOUT_FAILED', { error: err?.error?.detail || err?.error?.message });
        this.isCheckingOut.set(false);
        const msg = err?.error?.detail ?? err?.error?.message ?? ERROR_MESSAGES.CHECKOUT_FAILED;
        this.snackBar.open(msg, SNACKBAR_ACTIONS.CLOSE, { duration: 5000 });
      }
    });
  }

  public closeCart(): void {
    this.isCartOpen.set(false);
  }

  public isLowStock(product: ProductDto): boolean {
    return (product.stock ?? 0) <= 5;
  }
}
