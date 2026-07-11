import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
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
import { ProductDto } from '../../core/api/model/productDto';
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
              <button mat-raised-button color="primary" class="add-to-cart-btn" (click)="addToCart(product)" [disabled]="(product.stock ?? 0) === 0">
                <mat-icon>add_shopping_cart</mat-icon>
                <span i18n="@@addToCartBtn">Add</span>
              </button>
            </mat-card-footer>
          </mat-card>
        </div>
      </section>

      <!-- Sidebar Cart Summary Panel Placeholder -->
      <div class="cart-sidebar" [class.open]="isCartOpen()">
        <div class="cart-sidebar-header">
          <h3 i18n="@@cartTitle">Shopping Cart</h3>
          <button mat-icon-button (click)="closeCart()">
            <mat-icon>close</mat-icon>
          </button>
        </div>
        <mat-divider></mat-divider>
        
        <div class="cart-sidebar-content">
          <div class="cart-empty" *ngIf="cartState.itemCount() === 0">
            <mat-icon class="cart-empty-icon">shopping_cart_off</mat-icon>
            <p i18n="@@cartEmptyMessage">Your cart is empty.</p>
          </div>
          
          <div class="cart-items" *ngIf="cartState.itemCount() > 0">
            <!-- Items mapped list will appear here in Phase 4 -->
            <div class="cart-summary-totals">
              <span i18n="@@cartTotalLabel">Total Items:</span>
              <strong>{{ cartState.itemCount() }}</strong>
            </div>
          </div>
        </div>
      </div>
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
    .cart-sidebar {
      position: fixed;
      top: 0;
      right: -320px;
      width: 320px;
      height: 100vh;
      background-color: white;
      box-shadow: -4px 0 15px rgba(0, 0, 0, 0.1);
      z-index: 100;
      transition: right 0.3s ease-in-out;
      display: flex;
      flex-direction: column;
    }
    .cart-sidebar.open {
      right: 0;
    }
    .cart-sidebar-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
    }
    .cart-sidebar-content {
      padding: 16px;
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
      font-size: 36px;
      width: 36px;
      height: 36px;
      margin-bottom: 12px;
    }
    .cart-summary-totals {
      display: flex;
      justify-content: space-between;
      margin-top: 24px;
      font-size: 15px;
    }
  `]
})
export class CatalogComponent implements OnInit {
  private readonly productsService = inject(ProductsService);
  public readonly cartState = inject(CartStateService);
  public readonly authState = inject(AuthStateService);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  public readonly searchQuery = signal<string>('');
  public readonly selectedCategory = signal<string>('');
  public readonly products = signal<Array<ProductDto>>([]);
  public readonly categories = signal<Array<string>>([]);
  public readonly isLoading = signal<boolean>(false);
  public readonly isCartOpen = signal<boolean>(false);

  public ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['cartOpen'] === 'true') {
        this.isCartOpen.set(true);
      }
    });

    this.loadProducts();
    this.loadCategories();
  }

  public loadProducts(): void {
    this.isLoading.set(true);
    this.productsService.getProducts(this.searchQuery(), this.selectedCategory()).subscribe({
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

  public loadCategories(): void {
    // Generate unique category items from pre-defined mock or fetch
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

  public addToCart(product: ProductDto): void {
    // In Phase 3, we mock adding to the cart using local storage or temporary cart DTO
    // In Phase 4, we will hook this up with the Redis backed Cart API
    const currentItems = this.cartState.cart()?.items || [];
    const existingIndex = currentItems.findIndex(i => i.product?.id === product.id);
    const updatedItems = [...currentItems];

    if (existingIndex > -1) {
      const quantity = (updatedItems[existingIndex].quantity ?? 0) + 1;
      updatedItems[existingIndex] = {
        ...updatedItems[existingIndex],
        quantity
      };
    } else {
      updatedItems.push({
        product,
        quantity: 1
      });
    }

    const totalPrice = updatedItems.reduce((acc, i) => acc + (i.quantity * (i.product?.price ?? 0)), 0);
    this.cartState.setCart({ items: updatedItems, totalPrice });

    this.snackBar.open(`${product.name} added to cart!`, 'View Cart', { duration: 3000 })
      .onAction().subscribe(() => this.isCartOpen.set(true));
  }

  public closeCart(): void {
    this.isCartOpen.set(false);
  }
}
