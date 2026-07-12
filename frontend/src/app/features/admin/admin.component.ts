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
  templateUrl: './admin.component.html'
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
