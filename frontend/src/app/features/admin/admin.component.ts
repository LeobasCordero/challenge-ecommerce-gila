import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subscription, interval } from 'rxjs';
import { switchMap, takeWhile } from 'rxjs/operators';

import { ProductsService } from '../../core/api/api/products.service';
import { OrdersService } from '../../core/api/api/orders.service';
import { ProductDto } from '../../core/api/model/productDto';
import { ProductImportStatusDto } from '../../core/api/model/productImportStatusDto';
import { AuthStateService } from '../../core/state/auth-state.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatTabsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatCardModule,
    MatProgressBarModule,
    MatDividerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="admin-pagecontainer container">
      <!-- Role security check -->
      <div *ngIf="!authState.isAdmin()" class="unauthorized-container">
        <mat-card class="unauthorized-card">
          <mat-card-header>
            <mat-icon color="warn" class="unauth-icon">block</mat-icon>
            <mat-card-title i18n="@@unauthorizedTitle">Access Denied</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p i18n="@@unauthorizedMsg">You do not have administrative permissions to access this control panel.</p>
          </mat-card-content>
        </mat-card>
      </div>

      <div *ngIf="authState.isAdmin()">
        <h1 i18n="@@adminDashboardTitle">Admin Dashboard</h1>

        <mat-tab-group class="admin-tabs">
          <!-- Products Catalog CRUD Tab -->
          <mat-tab label="Products CRUD" i18n-label="@@tabCrud">
            <div class="tab-content">
              <!-- Edit/Create Product Card -->
              <mat-card class="product-form-card">
                <mat-card-header>
                  <mat-card-title>
                    <span *ngIf="!isEditing()" i18n="@@createProductTitle">Create New Product</span>
                    <span *ngIf="isEditing()" i18n="@@editProductTitle">Edit Product</span>
                  </mat-card-title>
                </mat-card-header>
                <mat-card-content>
                  <form [formGroup]="productForm" (ngSubmit)="saveProduct()" class="product-form">
                    <div class="form-row">
                      <mat-form-field appearance="outline">
                        <mat-label i18n="@@prodName">Name</mat-label>
                        <input matInput formControlName="name" required>
                      </mat-form-field>

                      <mat-form-field appearance="outline">
                        <mat-label i18n="@@prodCategory">Category</mat-label>
                        <input matInput formControlName="category" required>
                      </mat-form-field>
                    </div>

                    <div class="form-row">
                      <mat-form-field appearance="outline">
                        <mat-label i18n="@@prodPrice">Price</mat-label>
                        <input matInput type="number" formControlName="price" required>
                      </mat-form-field>

                      <mat-form-field appearance="outline">
                        <mat-label i18n="@@prodStock">Stock</mat-label>
                        <input matInput type="number" formControlName="stock" required>
                      </mat-form-field>
                    </div>

                    <mat-form-field appearance="outline" class="full-width">
                      <mat-label i18n="@@prodDescription">Description</mat-label>
                      <textarea matInput formControlName="description" rows="3"></textarea>
                    </mat-form-field>

                    <div class="form-actions">
                      <button mat-stroked-button type="button" *ngIf="isEditing()" (click)="cancelEdit()" i18n="@@cancelBtn">Cancel</button>
                      <button mat-raised-button color="primary" class="save-btn" type="submit" [disabled]="productForm.invalid">
                        <span i18n="@@saveBtn">Save Product</span>
                      </button>
                    </div>
                  </form>
                </mat-card-content>
              </mat-card>

              <!-- Products List Table -->
              <div class="table-container">
                <table mat-table [dataSource]="products()" class="mat-elevation-z1 products-table">
                  <ng-container matColumnDef="name">
                    <th mat-header-cell *matHeaderCellDef i18n="@@tableName">Name</th>
                    <td mat-cell *matCellDef="let p">{{ p.name }}</td>
                  </ng-container>

                  <ng-container matColumnDef="category">
                    <th mat-header-cell *matHeaderCellDef i18n="@@tableCategory">Category</th>
                    <td mat-cell *matCellDef="let p">{{ p.category }}</td>
                  </ng-container>

                  <ng-container matColumnDef="price">
                    <th mat-header-cell *matHeaderCellDef i18n="@@tablePrice">Price</th>
                    <td mat-cell *matCellDef="let p">\${{ (p.price ?? 0).toFixed(2) }}</td>
                  </ng-container>

                  <ng-container matColumnDef="stock">
                    <th mat-header-cell *matHeaderCellDef i18n="@@tableStock">Stock</th>
                    <td mat-cell *matCellDef="let p">{{ p.stock }}</td>
                  </ng-container>

                  <ng-container matColumnDef="actions">
                    <th mat-header-cell *matHeaderCellDef i18n="@@tableActions">Actions</th>
                    <td mat-cell *matCellDef="let p">
                      <button mat-icon-button color="primary" (click)="editProduct(p)" [attr.aria-label]="'Edit'">
                        <mat-icon>edit</mat-icon>
                      </button>
                      <button mat-icon-button color="warn" (click)="deleteProduct(p)" [attr.aria-label]="'Delete'">
                        <mat-icon>delete</mat-icon>
                      </button>
                    </td>
                  </ng-container>

                  <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
                  <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
                </table>
              </div>
            </div>
          </mat-tab>

          <!-- CSV Import Tab -->
          <mat-tab label="CSV Bulk Import" i18n-label="@@tabCsv">
            <div class="tab-content">
              <mat-card class="import-card">
                <mat-card-header>
                  <mat-card-title i18n="@@importTitle">Import Products from CSV</mat-card-title>
                </mat-card-header>
                <mat-card-content>
                  <p class="import-instructions" i18n="@@importInstructions">Select a CSV file containing catalog details. Upload will parse and insert records asynchronously.</p>
                  
                  <div class="file-uploader-box">
                    <input type="file" #fileInput (change)="onFileSelected($event)" accept=".csv" class="hidden-file-input">
                    <button mat-raised-button color="accent" class="select-file-btn" (click)="fileInput.click()">
                      <mat-icon>attach_file</mat-icon>
                      <span i18n="@@selectFileBtn">Select CSV File</span>
                    </button>
                    <span class="selected-file-name" *ngIf="selectedFile()">{{ selectedFile()?.name }}</span>
                  </div>

                  <div class="import-actions">
                    <button mat-raised-button color="primary" class="upload-btn" (click)="uploadCsv()" [disabled]="!selectedFile() || isUploading()">
                      <span i18n="@@uploadBtn">Upload and Start Import</span>
                    </button>
                  </div>

                  <!-- Real-time progress display -->
                  <div class="progress-container" *ngIf="isUploading() || importStatus()">
                    <h3 i18n="@@importStatusHeader">Import Task Progress</h3>
                    <div class="status-badge" [ngClass]="(importStatus()?.status || '').toLowerCase()">
                      {{ importStatus()?.status || 'QUEUED' }}
                    </div>
                    
                    <mat-progress-bar mode="determinate" [value]="calcProgress()"></mat-progress-bar>
                    
                    <div class="progress-details" *ngIf="importStatus()">
                      <div><span i18n="@@rowsTotal">Total rows found:</span> {{ importStatus()?.totalRows }}</div>
                      <div><span i18n="@@rowsProcessed">Processed rows:</span> {{ importStatus()?.processedRows }}</div>
                      <div><span i18n="@@rowsErrors">Error count:</span> {{ importStatus()?.errorCount }}</div>
                    </div>

                    <!-- Row-level warning logs -->
                    <div class="warnings-box" *ngIf="importStatus()?.warnings?.length">
                      <h4 i18n="@@warningsTitle">Import Logs & Warnings</h4>
                      <ul>
                        <li *ngFor="let w of importStatus()?.warnings">{{ w }}</li>
                      </ul>
                    </div>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
          </mat-tab>

          <!-- System Reset Tab -->
          <mat-tab label="System Reset" i18n-label="@@tabReset">
            <div class="tab-content">
              <mat-card class="reset-card">
                <mat-card-header>
                  <mat-icon class="reset-icon">warning_amber</mat-icon>
                  <mat-card-title i18n="@@resetTitle">Reset System for UAT</mat-card-title>
                </mat-card-header>
                <mat-card-content>
                  <p class="reset-description" i18n="@@resetDescription">
                    This action permanently deletes all orders and restores every product's stock to its initial value.
                    Use this only to reset the application state for evaluator testing.
                  </p>
                  <mat-divider class="reset-divider"></mat-divider>
                  <div class="reset-actions">
                    <mat-progress-bar *ngIf="isResetting()" mode="indeterminate" class="reset-progress"></mat-progress-bar>
                    <button
                      mat-raised-button
                      id="btn-reset-orders"
                      class="reset-btn"
                      [disabled]="isResetting()"
                      (click)="clearOrders()"
                      i18n="@@resetBtn">
                      <mat-icon>delete_sweep</mat-icon>
                      Reset All Orders &amp; Restore Stock
                    </button>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
          </mat-tab>
        </mat-tab-group>
      </div>
    </div>
  `,
  styles: [`
    .admin-pagecontainer {
      padding-top: 32px;
      padding-bottom: 64px;
    }
    .unauthorized-container {
      display: flex;
      justify-content: center;
      padding: 64px 0;
    }
    .unauthorized-card {
      max-width: 400px;
      padding: 16px;
      text-align: center;
    }
    .unauth-icon {
      font-size: 36px;
      width: 36px;
      height: 36px;
      margin: 0 auto 12px auto;
    }
    .admin-tabs {
      margin-top: 16px;
    }
    .tab-content {
      padding: 24px 0;
      display: flex;
      flex-direction: column;
      gap: 24px;
    }
    .product-form-card {
      padding: 16px;
      border-radius: 8px;
    }
    .product-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-top: 16px;
    }
    .form-row {
      display: flex;
      gap: 16px;
      
      @media (max-width: 600px) {
        flex-direction: column;
        gap: 8px;
      }
    }
    .form-row mat-form-field {
      flex: 1;
    }
    .full-width {
      width: 100%;
    }
    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
    .save-btn {
      background-color: #059669 !important;
      color: white !important;
    }
    .table-container {
      overflow-x: auto;
    }
    .products-table {
      width: 100%;
    }
    .import-card {
      padding: 24px;
      border-radius: 8px;
    }
    .import-instructions {
      color: #64748b;
      margin-bottom: 24px;
    }
    .file-uploader-box {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 24px;
    }
    .hidden-file-input {
      display: none;
    }
    .select-file-btn {
      background-color: transparent !important;
      color: #475569 !important;
      border: 1px solid #cbd5e1 !important;
    }
    .selected-file-name {
      font-size: 14px;
      color: #334155;
      font-weight: 500;
    }
    .import-actions {
      margin-bottom: 32px;
    }
    .upload-btn {
      background-color: #059669 !important;
      color: white !important;
    }
    .upload-btn[disabled] {
      background-color: #cbd5e1 !important;
      color: #64748b !important;
    }
    .progress-container {
      border: 1px solid #e2e8f0;
      border-radius: 8px;
      padding: 16px;
      background-color: #f8fafc;
    }
    .status-badge {
      display: inline-block;
      padding: 4px 12px;
      border-radius: 9999px;
      font-size: 12px;
      font-weight: 700;
      text-transform: uppercase;
      margin-bottom: 16px;
    }
    .status-badge.completed {
      background-color: #d1fae5;
      color: #065f46;
    }
    .status-badge.failed {
      background-color: #fee2e2;
      color: #991b1b;
    }
    .status-badge.processing, .status-badge.queued {
      background-color: #fef3c7;
      color: #92400e;
    }
    .progress-details {
      display: flex;
      gap: 24px;
      margin-top: 16px;
      font-size: 14px;
      color: #475569;
    }
    .warnings-box {
      margin-top: 24px;
      padding: 12px;
      background-color: #fff;
      border: 1px solid #cbd5e1;
      border-radius: 6px;
      max-height: 200px;
      overflow-y: auto;
    }
    .warnings-box h4 {
      margin-bottom: 8px;
      color: #991b1b;
    }
    .warnings-box ul {
      padding-left: 16px;
      font-size: 13px;
      color: #7f1d1d;
    }
    .reset-card {
      padding: 24px;
      border-radius: 8px;
      border: 1px solid #fca5a5;
      background-color: #fff7f7;
    }
    .reset-icon {
      color: #dc2626;
      font-size: 28px;
      width: 28px;
      height: 28px;
      margin-right: 12px;
    }
    .reset-description {
      color: #64748b;
      margin-bottom: 24px;
      line-height: 1.6;
    }
    .reset-divider {
      margin-bottom: 24px;
    }
    .reset-actions {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    .reset-progress {
      width: 100%;
      max-width: 400px;
    }
    .reset-btn {
      background-color: #dc2626 !important;
      color: white !important;
      font-weight: 600;
      letter-spacing: 0.5px;
    }
    .reset-btn[disabled] {
      background-color: #fca5a5 !important;
      color: #fff !important;
    }
  `]
})
export class AdminComponent implements OnInit, OnDestroy {
  public readonly authState = inject(AuthStateService);
  private readonly productsService = inject(ProductsService);
  private readonly ordersService = inject(OrdersService);
  private readonly fb = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  public readonly products = signal<Array<ProductDto>>([]);
  public readonly displayedColumns: string[] = ['name', 'category', 'price', 'stock', 'actions'];
  
  public readonly isEditing = signal<boolean>(false);
  public readonly editingId = signal<string | null>(null);
  
  // File upload fields
  public readonly selectedFile = signal<File | null>(null);
  public readonly isUploading = signal<boolean>(false);
  public readonly isResetting = signal<boolean>(false);
  public readonly importStatus = signal<ProductImportStatusDto | null>(null);

  public productForm: FormGroup = this.fb.group({
    name: ['', [Validators.required]],
    category: ['', [Validators.required]],
    price: [0, [Validators.required, Validators.min(0.01)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    description: ['']
  });

  private pollSubscription?: Subscription;

  public ngOnInit(): void {
    if (this.authState.isAdmin()) {
      this.loadProducts();
    }
  }

  public ngOnDestroy(): void {
    this.stopPolling();
  }

  public loadProducts(): void {
    this.productsService.getProducts().subscribe({
      next: (data) => this.products.set(data),
      error: (err) => console.error('Error fetching admin products:', err)
    });
  }

  public saveProduct(): void {
    if (this.productForm.invalid) {
      return;
    }

    const formValue = this.productForm.value;
    const productDto: ProductDto = {
      name: formValue.name,
      category: formValue.category,
      price: formValue.price,
      stock: formValue.stock,
      description: formValue.description
    };

    if (this.isEditing() && this.editingId()) {
      this.productsService.updateProduct(this.editingId()!, productDto).subscribe({
        next: () => {
          this.snackBar.open('Product updated successfully!', 'Close', { duration: 3000 });
          this.cancelEdit();
          this.loadProducts();
        },
        error: (err) => {
          this.snackBar.open('Error updating product. Check log details.', 'Close', { duration: 3000 });
          console.error(err);
        }
      });
    } else {
      this.productsService.createProduct(productDto).subscribe({
        next: () => {
          this.snackBar.open('Product created successfully!', 'Close', { duration: 3000 });
          this.productForm.reset({ price: 0, stock: 0 });
          this.loadProducts();
        },
        error: (err) => {
          this.snackBar.open('Error creating product. Name must be unique.', 'Close', { duration: 3000 });
          console.error(err);
        }
      });
    }
  }

  public editProduct(product: ProductDto): void {
    this.isEditing.set(true);
    this.editingId.set(product.id || null);
    this.productForm.patchValue({
      name: product.name,
      category: product.category,
      price: product.price,
      stock: product.stock,
      description: product.description
    });
  }

  public cancelEdit(): void {
    this.isEditing.set(false);
    this.editingId.set(null);
    this.productForm.reset({ price: 0, stock: 0 });
  }

  public deleteProduct(product: ProductDto): void {
    if (confirm(`Are you sure you want to delete ${product.name}?`)) {
      this.productsService.deleteProduct(product.id!).subscribe({
        next: () => {
          this.snackBar.open('Product deleted successfully!', 'Close', { duration: 3000 });
          this.loadProducts();
        },
        error: (err) => {
          this.snackBar.open('Error deleting product.', 'Close', { duration: 3000 });
          console.error(err);
        }
      });
    }
  }

  // CSV Import Dashboard Actions
  public onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile.set(file);
      this.importStatus.set(null);
      this.stopPolling();
    }
  }

  public uploadCsv(): void {
    if (!this.selectedFile()) return;

    this.isUploading.set(true);
    this.importStatus.set(null);

    this.productsService.importProducts(this.selectedFile()!).subscribe({
      next: (response) => {
        if (response.taskId) {
          this.snackBar.open('CSV file uploaded. Processing import...', 'Close', { duration: 3000 });
          this.startPolling(response.taskId);
        } else {
          this.isUploading.set(false);
          this.snackBar.open('Upload failed. No taskId returned.', 'Close', { duration: 3000 });
        }
      },
      error: (err) => {
        this.isUploading.set(false);
        this.snackBar.open('Error uploading file. Check format and rules.', 'Close', { duration: 3000 });
        console.error(err);
      }
    });
  }

  public calcProgress(): number {
    const status = this.importStatus();
    if (!status || status.totalRows === 0) return 0;
    return (status.processedRows / status.totalRows) * 100;
  }

  private startPolling(taskId: string): void {
    this.stopPolling();
    
    this.pollSubscription = interval(2000).pipe(
      switchMap(() => this.productsService.getImportStatus(taskId)),
      takeWhile(status => status.status === 'QUEUED' || status.status === 'PROCESSING', true)
    ).subscribe({
      next: (status) => {
        this.importStatus.set(status);
        if (status.status === 'COMPLETED') {
          this.isUploading.set(false);
          this.selectedFile.set(null);
          this.snackBar.open('Bulk import successfully finished!', 'Refresh', { duration: 5000 })
            .onAction().subscribe(() => this.loadProducts());
          this.stopPolling();
        } else if (status.status === 'FAILED') {
          this.isUploading.set(false);
          this.snackBar.open('Import task failed.', 'Close', { duration: 5000 });
          this.stopPolling();
        }
      },
      error: (err) => {
        this.isUploading.set(false);
        this.stopPolling();
        console.error('Import polling error:', err);
      }
    });
  }

  private stopPolling(): void {
    if (this.pollSubscription) {
      this.pollSubscription.unsubscribe();
      this.pollSubscription = undefined;
    }
  }

  /**
   * Confirm and trigger the UAT reset: delete all orders and restore product stock defaults.
   */
  public clearOrders(): void {
    const confirmed = confirm('This will delete ALL orders and restore all product stock. Continue?');
    if (!confirmed) {
      return;
    }
    this.isResetting.set(true);
    this.ordersService.clearOrders().subscribe({
      next: () => {
        this.isResetting.set(false);
        this.snackBar.open('System reset complete. All orders cleared and stock restored.', 'Close', { duration: 5000 });
        this.loadProducts();
      },
      error: (err) => {
        this.isResetting.set(false);
        this.snackBar.open('Reset failed. Check server logs.', 'Close', { duration: 4000 });
        console.error('Reset error:', err);
      }
    });
  }
}
