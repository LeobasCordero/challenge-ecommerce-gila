import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AppComponent } from '../app/app.component';
import { AuthStateService } from '../app/services/auth-state.service';
import { CartStateService } from '../app/services/cart-state.service';

describe('AppComponent', () => {
  let mockAuthState: Partial<AuthStateService>;
  let mockCartState: Partial<CartStateService>;

  beforeEach(async () => {
    mockAuthState = {
      isAdmin: () => false,
      isAuthenticated: () => false,
      username: () => null,
      role: () => null
    } as any;

    mockCartState = {
      itemCount: () => 0
    } as any;

    await TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        NoopAnimationsModule,
        HttpClientTestingModule,
        AppComponent
      ],
      providers: [
        { provide: AuthStateService, useValue: mockAuthState },
        { provide: CartStateService, useValue: mockCartState }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });
});
