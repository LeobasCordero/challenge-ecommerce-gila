import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterModule, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

import { AuthStateService } from './core/state/auth-state.service';
import { CartStateService } from './core/state/cart-state.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule
  ],
  template: `
    <header class="header">
      <div class="container header-wrapper">
        <div class="header-logo" routerLink="/">
          <mat-icon class="logo-icon">shopping_bag</mat-icon>
          <span>Gila Store</span>
        </div>
        
        <nav class="header-nav">
          <a class="header-link" routerLink="/catalog" routerLinkActive="active" i18n="@@navCatalog">Catalog</a>
          <a class="header-link" *ngIf="authState.isAdmin()" routerLink="/admin" routerLinkActive="active" i18n="@@navAdmin">Admin</a>
          
          <div class="nav-divider"></div>
          
          <!-- i18n Language Selector -->
          <button mat-button [matMenuTriggerFor]="langMenu" class="lang-selector-btn">
            <mat-icon>language</mat-icon>
            <span class="lang-text">{{ currentLang().toUpperCase() }}</span>
          </button>
          <mat-menu #langMenu="matMenu" xPosition="before">
            <button mat-menu-item (click)="switchLanguage('en')">English</button>
            <button mat-menu-item (click)="switchLanguage('es')">Español</button>
          </mat-menu>

          <a class="header-link cart-btn" routerLink="/catalog" [queryParams]="{ cartOpen: true }">
            <mat-icon>shopping_cart</mat-icon>
            <span class="cart-badge" *ngIf="cartState.itemCount() > 0">{{ cartState.itemCount() }}</span>
          </a>

          <!-- User Session Management -->
          <ng-container *ngIf="authState.isAuthenticated(); else guestView">
            <span class="user-greeting">Hi, {{ authState.username() }}</span>
            <button mat-icon-button color="warn" (click)="logout()" [attr.aria-label]="'Logout'">
              <mat-icon>logout</mat-icon>
            </button>
          </ng-container>
          <ng-template #guestView>
            <button mat-stroked-button color="primary" class="login-btn-header" routerLink="/login" i18n="@@navLogin">Login</button>
          </ng-template>
        </nav>
      </div>
    </header>

    <main class="main-content">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .logo-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
      color: #059669;
    }
    .header-logo {
      cursor: pointer;
    }
    .nav-divider {
      width: 1px;
      height: 24px;
      background-color: #e2e8f0;
      margin: 0 8px;
    }
    .lang-selector-btn {
      color: #64748b !important;
      display: flex;
      align-items: center;
      gap: 4px;
    }
    .lang-text {
      font-weight: 600;
    }
    .cart-btn {
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .cart-badge {
      position: absolute;
      top: -4px;
      right: -4px;
      background-color: #059669;
      color: white;
      font-size: 10px;
      font-weight: 700;
      width: 18px;
      height: 18px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 2px solid white;
    }
    .user-greeting {
      font-size: 14px;
      font-weight: 500;
      color: #334155;
    }
    .login-btn-header {
      border-color: #059669 !important;
      color: #059669 !important;
    }
    .main-content {
      min-height: calc(100vh - 73px);
      background-color: #f8fafc;
    }
  `]
})
export class AppComponent {
  public readonly authState = inject(AuthStateService);
  public readonly cartState = inject(CartStateService);
  private readonly router = inject(Router);

  public currentLang(): string {
    if (typeof window !== 'undefined') {
      const path = window.location.pathname;
      if (path.startsWith('/es/')) return 'es';
    }
    return 'en';
  }

  public switchLanguage(lang: string): void {
    if (typeof window !== 'undefined') {
      const currentLoc = this.currentLang();
      if (currentLoc === lang) return;

      // Redirect browser to localized path configurations (e.g. /es/ or /en/)
      const path = window.location.pathname;
      let newPath = '';
      if (lang === 'es') {
        newPath = '/es' + path;
      } else {
        newPath = path.replace(/^\/es/, '');
      }
      
      window.location.href = window.location.origin + newPath;
    }
  }

  public logout(): void {
    this.authState.logout();
    this.cartState.clearCart();
    this.router.navigate(['/login']);
  }
}
