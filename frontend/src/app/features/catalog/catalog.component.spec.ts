import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { PLATFORM_ID } from '@angular/core';
import { CatalogComponent } from './catalog.component';
import { ProductsService } from '../../core/api/api/products.service';
import { CartService as CartApiService } from '../../core/api/api/cart.service';
import { OrdersService } from '../../core/api/api/orders.service';
import { CartStateService } from '../../core/state/cart-state.service';
import { AuthStateService } from '../../core/state/auth-state.service';

describe('CatalogComponent', () => {
  let mockProductsService: jasmine.SpyObj<ProductsService>;
  let mockCartApiService: jasmine.SpyObj<CartApiService>;
  let mockOrdersService: jasmine.SpyObj<OrdersService>;
  let mockCartState: jasmine.SpyObj<CartStateService>;
  let mockAuthState: jasmine.SpyObj<AuthStateService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockSnackBar: jasmine.SpyObj<MatSnackBar>;
  let mockActivatedRoute: any;

  beforeEach(async () => {
    mockProductsService = jasmine.createSpyObj('ProductsService', ['getProducts']);
    mockProductsService.getProducts.and.returnValue(of([]) as any);

    mockCartApiService = jasmine.createSpyObj('CartApiService', ['getCart', 'updateCartItem', 'clearCart']);
    mockOrdersService = jasmine.createSpyObj('OrdersService', ['checkout']);

    mockCartState = jasmine.createSpyObj('CartStateService', ['cart', 'itemCount', 'totalPrice', 'loadCart', 'clearCart']);
    mockCartState.itemCount.and.returnValue(0);
    mockCartState.totalPrice.and.returnValue(0);

    mockAuthState = jasmine.createSpyObj('AuthStateService', ['isAuthenticated', 'username', 'role', 'isAdmin']);
    mockAuthState.isAuthenticated.and.returnValue(false);
    mockAuthState.isAdmin.and.returnValue(false);

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockSnackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    mockActivatedRoute = {
      queryParams: of({})
    };

    await TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        CatalogComponent
      ],
      providers: [
        { provide: ProductsService, useValue: mockProductsService },
        { provide: CartApiService, useValue: mockCartApiService },
        { provide: OrdersService, useValue: mockOrdersService },
        { provide: CartStateService, useValue: mockCartState },
        { provide: AuthStateService, useValue: mockAuthState },
        { provide: Router, useValue: mockRouter },
        { provide: MatSnackBar, useValue: mockSnackBar },
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: PLATFORM_ID, useValue: 'browser' }
      ]
    }).compileComponents();
  });

  it('should create the catalog component', () => {
    const fixture = TestBed.createComponent(CatalogComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
