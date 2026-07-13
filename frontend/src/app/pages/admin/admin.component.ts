import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormGroupDirective } from '@angular/forms';
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
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subscription, interval } from 'rxjs';
import { switchMap, takeWhile } from 'rxjs/operators';

import { ProductsService } from '../../core/api/api/products.service';
import { OrdersService } from '../../core/api/api/orders.service';
import { AuditLogsService } from '../../core/api/api/auditLogs.service';
import { ProductDto } from '../../core/api/model/productDto';
import { ProductImportStatusDto } from '../../core/api/model/productImportStatusDto';
import { AuditLogDto } from '../../core/api/model/auditLogDto';
import { AuthStateService } from '../../services/auth-state.service';
import { TelemetryService } from '../../services/telemetry.service';
import { TranslationService } from '../../services/translation.service';
import { ERROR_MESSAGES, SUCCESS_MESSAGES, MODAL_CONFIRMATIONS, SNACKBAR_ACTIONS } from '../../utils/constants';
import { ImportStatus } from '../../utils/enums';
import { TranslatePipe } from '../../pipes/translate.pipe';

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
    MatSnackBarModule,
    MatProgressSpinnerModule,
    TranslatePipe
  ],
  templateUrl: './admin.component.html'
})
export class AdminComponent implements OnInit, OnDestroy {
  public readonly ts = inject(TranslationService);
  public readonly authState = inject(AuthStateService);
  private readonly productsService = inject(ProductsService);
  private readonly ordersService = inject(OrdersService);
  private readonly auditLogsService = inject(AuditLogsService);
  private readonly telemetryService = inject(TelemetryService);
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

  // DB Audit Logs
  public readonly auditLogs = signal<Array<AuditLogDto>>([]);
  public readonly auditPage = signal<number>(0);
  public readonly auditPageSize = signal<number>(20);
  public readonly isAuditLoading = signal<boolean>(false);
  public readonly auditColumns: string[] = ['timestamp', 'username', 'actionType', 'status', 'details'];

  // Client Telemetry Logs
  public readonly telemetryLogs = signal<Array<any>>([]);
  public readonly telemetryColumns: string[] = ['timestamp', 'username', 'action', 'metadata'];

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
      next: (data) => this.products.set(data)
    });
  }

  public saveProduct(formDir?: FormGroupDirective): void {
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
          this.snackBar.open(SUCCESS_MESSAGES.PRODUCT_UPDATED, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
          this.cancelEdit();
          if (formDir) {
            formDir.resetForm({ price: 0, stock: 0 });
          } else {
            this.productForm.reset({ price: 0, stock: 0 });
          }
          this.loadProducts();
        },
        error: () => {
          this.snackBar.open(ERROR_MESSAGES.UPDATE_PRODUCT_FAILED, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
        }
      });
    } else {
      this.productsService.createProduct(productDto).subscribe({
        next: () => {
          this.snackBar.open(SUCCESS_MESSAGES.PRODUCT_CREATED, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
          if (formDir) {
            formDir.resetForm({ price: 0, stock: 0 });
          } else {
            this.productForm.reset({ price: 0, stock: 0 });
          }
          this.loadProducts();
        },
        error: () => {
          this.snackBar.open(ERROR_MESSAGES.CREATE_PRODUCT_FAILED, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
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
    if (confirm(MODAL_CONFIRMATIONS.DELETE_PRODUCT(product.name || ''))) {
      this.productsService.deleteProduct(product.id!).subscribe({
        next: () => {
          this.snackBar.open(SUCCESS_MESSAGES.PRODUCT_DELETED, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
          this.loadProducts();
        },
        error: () => {
          this.snackBar.open(ERROR_MESSAGES.DELETE_PRODUCT_FAILED, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
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
          this.snackBar.open(SUCCESS_MESSAGES.CSV_UPLOADED, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
          this.startPolling(response.taskId);
        } else {
          this.isUploading.set(false);
          this.snackBar.open(ERROR_MESSAGES.UPLOAD_CSV_FAILED_NO_TASK_ID, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
        }
      },
      error: () => {
        this.isUploading.set(false);
        this.snackBar.open(ERROR_MESSAGES.UPLOAD_CSV_FAILED, SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
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
      takeWhile(status => status.status === ImportStatus.QUEUED || status.status === ImportStatus.PROCESSING, true)
    ).subscribe({
      next: (status) => {
        this.importStatus.set(status);
        if (status.status === ImportStatus.COMPLETED) {
          this.isUploading.set(false);
          this.selectedFile.set(null);
          this.snackBar.open(SUCCESS_MESSAGES.IMPORT_COMPLETED, SNACKBAR_ACTIONS.REFRESH, { duration: 5000 })
            .onAction().subscribe(() => this.loadProducts());
          this.stopPolling();
        } else if (status.status === ImportStatus.FAILED) {
          this.isUploading.set(false);
          this.snackBar.open(ERROR_MESSAGES.IMPORT_FAILED, SNACKBAR_ACTIONS.CLOSE, { duration: 5000 });
          this.stopPolling();
        }
      },
      error: () => {
        this.isUploading.set(false);
        this.stopPolling();
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
    const confirmed = confirm(MODAL_CONFIRMATIONS.SYSTEM_RESET);
    if (!confirmed) {
      return;
    }
    this.isResetting.set(true);
    this.ordersService.clearOrders().subscribe({
      next: () => {
        this.isResetting.set(false);
        this.snackBar.open(SUCCESS_MESSAGES.SYSTEM_RESET_COMPLETE, SNACKBAR_ACTIONS.CLOSE, { duration: 5000 });
        this.loadProducts();
      },
      error: () => {
        this.isResetting.set(false);
        this.snackBar.open(ERROR_MESSAGES.RESET_FAILED, SNACKBAR_ACTIONS.CLOSE, { duration: 4000 });
      }
    });
  }

  // Audit Logs and Telemetry methods
  public loadAuditLogs(): void {
    this.isAuditLoading.set(true);
    this.auditLogsService.getAuditLogs(this.auditPage(), this.auditPageSize()).subscribe({
      next: (data) => {
        this.auditLogs.set(data);
        this.isAuditLoading.set(false);
      },
      error: () => {
        this.isAuditLoading.set(false);
        this.snackBar.open('Failed to load database audit logs', SNACKBAR_ACTIONS.CLOSE, { duration: 3000 });
      }
    });
  }

  public loadTelemetryLogs(): void {
    const allLogs = this.telemetryService.getLogs();
    // Sort descending by timestamp
    allLogs.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
    this.telemetryLogs.set(allLogs);
  }

  public nextAuditPage(): void {
    this.auditPage.update(p => p + 1);
    this.loadAuditLogs();
  }

  public prevAuditPage(): void {
    if (this.auditPage() > 0) {
      this.auditPage.update(p => p - 1);
      this.loadAuditLogs();
    }
  }

  public clearTelemetryLogs(): void {
    if (confirm('Are you sure you want to clear all telemetry logs from local storage?')) {
      this.telemetryService.clearLogs();
      this.loadTelemetryLogs();
    }
  }

  public onTabChange(event: any): void {
    const tabIndex = event.index;
    if (tabIndex === 3) {
      this.loadAuditLogs();
    } else if (tabIndex === 4) {
      this.loadTelemetryLogs();
    }
  }

  public formatJson(obj: any): string {
    if (!obj) return '';
    return typeof obj === 'string' ? obj : JSON.stringify(obj);
  }
}
