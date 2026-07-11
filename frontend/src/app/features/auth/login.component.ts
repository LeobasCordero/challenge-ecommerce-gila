import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '../../core/api/api/auth.service';
import { AuthStateService } from '../../core/state/auth-state.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>
            <span i18n="@@loginTitle">Login to Gila Store</span>
          </mat-card-title>
          <mat-card-subtitle>
            <span i18n="@@loginSubtitle">Enter your credentials below</span>
          </mat-card-subtitle>
        </mat-card-header>
        
        <mat-card-content>
          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="login-form">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label i18n="@@usernameLabel">Username</mat-label>
              <input matInput formControlName="username" placeholder="e.g. admin or customer" required>
              <mat-error *ngIf="loginForm.get('username')?.hasError('required')">
                <span i18n="@@usernameRequired">Username is required</span>
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label i18n="@@passwordLabel">Password</mat-label>
              <input matInput [type]="hidePassword() ? 'password' : 'text'" formControlName="password" required>
              <button mat-icon-button matSuffix (click)="hidePassword.set(!hidePassword())" type="button" [attr.aria-label]="'Hide password'" [attr.aria-pressed]="hidePassword()">
                <mat-icon>{{hidePassword() ? 'visibility_off' : 'visibility'}}</mat-icon>
              </button>
              <mat-error *ngIf="loginForm.get('password')?.hasError('required')">
                <span i18n="@@passwordRequired">Password is required</span>
              </mat-error>
            </mat-form-field>

            <div *ngIf="errorMessage()" class="error-message">
              {{ errorMessage() }}
            </div>

            <button mat-raised-button color="primary" class="full-width login-button" type="submit" [disabled]="loginForm.invalid || isLoading()">
              <mat-spinner diameter="20" *ngIf="isLoading()" class="spinner"></mat-spinner>
              <span *ngIf="!isLoading()" i18n="@@loginBtn">Login</span>
            </button>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 100vh;
      background-color: #f8fafc;
    }
    .login-card {
      width: 100%;
      max-width: 400px;
      padding: 24px;
      border-radius: 12px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
    }
    .login-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-top: 16px;
    }
    .full-width {
      width: 100%;
    }
    .login-button {
      background-color: #059669 !important;
      color: white !important;
      padding: 24px 0 !important;
      font-size: 16px;
      font-weight: 500;
      border-radius: 8px;
    }
    .login-button[disabled] {
      background-color: #d1fae5 !important;
      color: #64748b !important;
    }
    .error-message {
      color: #ef4444;
      font-size: 14px;
      margin-bottom: 8px;
      text-align: center;
    }
    .spinner {
      margin: 0 auto;
    }
  `]
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly authState = inject(AuthStateService);
  private readonly router = inject(Router);

  public readonly hidePassword = signal<boolean>(true);
  public readonly isLoading = signal<boolean>(false);
  public readonly errorMessage = signal<string | null>(null);

  public readonly loginForm: FormGroup = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]]
  });

  public onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const { username, password } = this.loginForm.value;

    this.authService.login({ username, password }).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        const role = username.toLowerCase() === 'admin' ? 'ADMIN' : 'CUSTOMER';
        this.authState.login(response.token, username, role);
        this.router.navigate(['/catalog']);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set('Invalid username or password. Please try again.');
        console.error('Login error:', err);
      }
    });
  }
}
