import { TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { LoginComponent } from '../../../app/pages/auth/login.component';
import { AuthService } from '../../../app/core/api/api/auth.service';
import { AuthStateService } from '../../../app/services/auth-state.service';

describe('LoginComponent', () => {
  let mockAuthService: jasmine.SpyObj<AuthService>;
  let mockAuthStateService: jasmine.SpyObj<AuthStateService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockAuthService = jasmine.createSpyObj('AuthService', ['login']);
    mockAuthStateService = jasmine.createSpyObj('AuthStateService', ['login']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [
        NoopAnimationsModule,
        ReactiveFormsModule,
        LoginComponent
      ],
      providers: [
        FormBuilder,
        { provide: AuthService, useValue: mockAuthService },
        { provide: AuthStateService, useValue: mockAuthStateService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();
  });

  it('should create the login component', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});
