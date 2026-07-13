import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { OrderDto } from '../../core/api/model/orderDto';
import { TranslationService } from '../../services/translation.service';
import { TranslatePipe } from '../../pipes/translate.pipe';

@Component({
  selector: 'app-checkout-success',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    TranslatePipe
  ],
  templateUrl: './checkout-success.component.html'
})
export class CheckoutSuccessComponent implements OnInit {
  private readonly router = inject(Router);
  public readonly ts = inject(TranslationService);

  public order: OrderDto | null = null;

  constructor() {
    const nav = this.router.getCurrentNavigation();
    this.order = nav?.extras?.state?.['order'] ?? null;
  }

  public ngOnInit(): void {
    // If navigated directly without state, attempt to read from current history state
    if (!this.order && typeof window !== 'undefined') {
      this.order = window.history.state?.order ?? null;
    }
  }
}
