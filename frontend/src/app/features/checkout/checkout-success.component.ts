import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { OrderDto } from '../../core/api/model/orderDto';

@Component({
  selector: 'app-checkout-success',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule
  ],
  template: `
    <div class="success-page container">
      <div class="success-card">
        <!-- Success header -->
        <div class="success-header">
          <div class="success-icon-wrapper">
            <mat-icon class="success-icon">check_circle</mat-icon>
          </div>
          <h1 i18n="@@checkoutSuccessTitle">Order Confirmed!</h1>
          <p class="success-subtitle" i18n="@@checkoutSuccessSubtitle">
            Thank you for your purchase. Your payment was processed successfully.
          </p>
        </div>

        <!-- Order details -->
        <div class="order-details" *ngIf="order">
          <div class="order-id-row">
            <span class="order-label" i18n="@@orderIdLabel">Order ID:</span>
            <span class="order-id" id="order-id">{{ order.id }}</span>
          </div>
          <div class="order-status-row">
            <span class="order-label" i18n="@@orderStatusLabel">Status:</span>
            <span class="order-status-badge">{{ order.status }}</span>
          </div>

          <mat-divider class="divider"></mat-divider>

          <!-- Items list -->
          <h3 class="items-title" i18n="@@orderItemsTitle">Items Purchased</h3>
          <div class="order-items">
            <div class="order-item" *ngFor="let item of order.items ?? []" id="order-item-{{ item.product?.id }}">
              <div class="order-item-info">
                <span class="order-item-name">{{ item.product?.name }}</span>
                <span class="order-item-qty" i18n="@@orderItemQtyLabel">× {{ item.quantity }}</span>
              </div>
              <span class="order-item-price">\${{ ((item.priceAtPurchase ?? 0) * (item.quantity ?? 0)).toFixed(2) }}</span>
            </div>
          </div>

          <mat-divider class="divider"></mat-divider>

          <!-- Total -->
          <div class="order-total-row">
            <span class="order-total-label" i18n="@@orderTotalLabel">Total Paid</span>
            <span class="order-total-amount" id="order-total">\${{ (order.totalPrice ?? 0).toFixed(2) }}</span>
          </div>
        </div>

        <!-- No order data fallback -->
        <div class="no-order" *ngIf="!order">
          <mat-icon class="no-order-icon">receipt_long</mat-icon>
          <p i18n="@@noOrderData">No order details available.</p>
        </div>

        <!-- Navigation actions -->
        <div class="success-actions">
          <button mat-raised-button color="primary" id="back-to-catalog-btn"
            class="back-btn" routerLink="/catalog">
            <mat-icon>storefront</mat-icon>
            <span i18n="@@continueShopping">Continue Shopping</span>
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .success-page {
      padding: 48px 16px;
      min-height: calc(100vh - 73px);
      display: flex;
      align-items: flex-start;
      justify-content: center;
    }
    .success-card {
      background: white;
      border-radius: 16px;
      box-shadow: 0 4px 24px rgba(0,0,0,0.08);
      padding: 48px 40px;
      max-width: 540px;
      width: 100%;

      @media (max-width: 600px) {
        padding: 32px 20px;
      }
    }
    .success-header {
      text-align: center;
      margin-bottom: 32px;
    }
    .success-icon-wrapper {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background-color: #dcfce7;
      margin-bottom: 20px;
    }
    .success-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #059669;
    }
    .success-header h1 {
      margin: 0 0 12px 0;
      font-size: 28px;
      font-weight: 800;
      color: #0f172a;
    }
    .success-subtitle {
      font-size: 15px;
      color: #64748b;
      margin: 0;
    }
    .order-details {
      margin-top: 8px;
    }
    .order-id-row, .order-status-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
    }
    .order-label {
      font-size: 14px;
      font-weight: 600;
      color: #64748b;
    }
    .order-id {
      font-size: 12px;
      color: #334155;
      font-family: monospace;
      background: #f1f5f9;
      padding: 4px 8px;
      border-radius: 4px;
      word-break: break-all;
    }
    .order-status-badge {
      font-size: 13px;
      font-weight: 700;
      color: #059669;
      background: #dcfce7;
      padding: 4px 12px;
      border-radius: 20px;
    }
    .divider {
      margin: 20px 0;
    }
    .items-title {
      font-size: 15px;
      font-weight: 700;
      color: #334155;
      margin: 0 0 12px 0;
    }
    .order-items {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .order-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
    }
    .order-item-info {
      display: flex;
      flex-direction: column;
    }
    .order-item-name {
      font-size: 14px;
      font-weight: 600;
      color: #0f172a;
    }
    .order-item-qty {
      font-size: 13px;
      color: #94a3b8;
    }
    .order-item-price {
      font-size: 15px;
      font-weight: 700;
      color: #334155;
    }
    .order-total-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .order-total-label {
      font-size: 17px;
      font-weight: 700;
      color: #0f172a;
    }
    .order-total-amount {
      font-size: 26px;
      font-weight: 800;
      color: #059669;
    }
    .no-order {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 32px 0;
      color: #94a3b8;
    }
    .no-order-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 12px;
    }
    .success-actions {
      margin-top: 32px;
      display: flex;
      justify-content: center;
    }
    .back-btn {
      background-color: #059669 !important;
      color: white !important;
      height: 48px;
      font-size: 16px;
      font-weight: 700;
      padding: 0 32px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      gap: 8px;
    }
  `]
})
export class CheckoutSuccessComponent implements OnInit {
  private readonly router = inject(Router);

  public order: OrderDto | null = null;

  public ngOnInit(): void {
    const nav = this.router.getCurrentNavigation();
    this.order = nav?.extras?.state?.['order'] ?? null;

    // If navigated directly without state, attempt to read from current history state
    if (!this.order && typeof window !== 'undefined') {
      this.order = window.history.state?.order ?? null;
    }
  }
}
