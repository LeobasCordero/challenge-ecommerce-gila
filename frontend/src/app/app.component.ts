import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterModule, Router } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

import { AuthStateService } from './services/auth-state.service';
import { CartStateService } from './services/cart-state.service';
import { TranslationService } from './services/translation.service';
import { APP_ROUTES } from './utils/constants';
import { ChatbotComponent } from './components/chatbot/chatbot.component';
import { TranslatePipe } from './pipes/translate.pipe';

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
    MatMenuModule,
    ChatbotComponent,
    TranslatePipe
  ],
  templateUrl: './app.component.html'
})
export class AppComponent {
  public readonly authState = inject(AuthStateService);
  public readonly cartState = inject(CartStateService);
  public readonly ts = inject(TranslationService);
  private readonly router = inject(Router);

  public currentLang(): string {
    return this.ts.currentLang();
  }

  public switchLanguage(lang: string): void {
    this.ts.setLanguage(lang as 'en' | 'es');
  }

  public logout(): void {
    this.authState.logout();
    this.cartState.clearCart();
    this.router.navigate([APP_ROUTES.LOGIN]);
  }

  public isOnLoginPage(): boolean {
    if (typeof window !== 'undefined') {
      const path = window.location.pathname;
      return path.endsWith('/login') || path.endsWith('/login/');
    }
    return false;
  }
}
