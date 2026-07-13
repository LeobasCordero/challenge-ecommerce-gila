import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { AdminComponent } from '../../../app/pages/admin/admin.component';
import { AuthStateService } from '../../../app/services/auth-state.service';
import { ProductsService } from '../../../app/core/api/api/products.service';
import { OrdersService } from '../../../app/core/api/api/orders.service';
import { AuditLogsService } from '../../../app/core/api/api/auditLogs.service';
import { TelemetryService } from '../../../app/services/telemetry.service';

describe('AdminComponent', () => {
  let mockAuthState: jasmine.SpyObj<AuthStateService>;
  let mockProductsService: jasmine.SpyObj<ProductsService>;
  let mockOrdersService: jasmine.SpyObj<OrdersService>;
  let mockAuditLogsService: jasmine.SpyObj<AuditLogsService>;
  let mockTelemetryService: jasmine.SpyObj<TelemetryService>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;

  beforeEach(async () => {
    mockAuthState = jasmine.createSpyObj('AuthStateService', ['isAdmin']);
    mockAuthState.isAdmin.and.returnValue(true);

    mockProductsService = jasmine.createSpyObj('ProductsService', ['getProducts', 'createProduct', 'updateProduct', 'deleteProduct']);
    mockProductsService.getProducts.and.returnValue(of([]) as any);

    mockOrdersService = jasmine.createSpyObj('OrdersService', ['clearOrders']);
    mockOrdersService.clearOrders.and.returnValue(of(null) as any);

    mockAuditLogsService = jasmine.createSpyObj('AuditLogsService', ['getAuditLogs']);
    mockAuditLogsService.getAuditLogs.and.returnValue(of([]) as any);

    mockTelemetryService = jasmine.createSpyObj('TelemetryService', ['getLogs', 'clearLogs', 'logEvent']);
    mockTelemetryService.getLogs.and.returnValue([]);

    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        ReactiveFormsModule,
        AdminComponent
      ],
      providers: [
        FormBuilder,
        { provide: AuthStateService, useValue: mockAuthState },
        { provide: ProductsService, useValue: mockProductsService },
        { provide: OrdersService, useValue: mockOrdersService },
        { provide: AuditLogsService, useValue: mockAuditLogsService },
        { provide: TelemetryService, useValue: mockTelemetryService },
        { provide: MatSnackBar, useValue: mockSnackBar }
      ]
    }).compileComponents();
  });

  it('should create the admin component', () => {
    const fixture = TestBed.createComponent(AdminComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
