import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterModule, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

import { AuthStateService } from './services/auth-state.service';
import { CartStateService } from './services/cart-state.service';
import { APP_ROUTES } from './utils/constants';

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
  templateUrl: './app.component.html'
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
    this.router.navigate([APP_ROUTES.LOGIN]);
  }
}
