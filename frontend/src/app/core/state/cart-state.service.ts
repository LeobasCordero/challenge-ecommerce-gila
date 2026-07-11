import { Injectable, signal, computed, inject, effect } from '@angular/core';
import { CartDto } from '../api/model/cartDto';
import { CartService as CartApiService } from '../api/api/cart.service';
import { AuthStateService } from './auth-state.service';

@Injectable({
  providedIn: 'root'
})
export class CartStateService {
  private readonly cartApi = inject(CartApiService);
  private readonly authState = inject(AuthStateService);

  private readonly _cart = signal<CartDto | null>(null);

  public readonly cart = this._cart.asReadonly();

  public readonly itemCount = computed(() => {
    const currentCart = this._cart();
    if (!currentCart || !currentCart.items) {
      return 0;
    }
    return currentCart.items.reduce((acc, item) => acc + (item.quantity ?? 0), 0);
  });

  public readonly totalPrice = computed(() => {
    const currentCart = this._cart();
    return currentCart?.totalPrice ?? 0;
  });

  constructor() {
    // Automatically fetch cart from Redis whenever the user authenticates.
    effect(() => {
      if (this.authState.isAuthenticated()) {
        this.loadCartFromBackend();
      } else {
        this._cart.set(null);
      }
    });
  }

  /** Fetch the current cart from the Redis-backed backend API. */
  public loadCartFromBackend(): void {
    this.cartApi.getCart().subscribe({
      next: (cart) => this._cart.set(cart),
      error: () => this._cart.set(null)
    });
  }

  public setCart(cart: CartDto): void {
    this._cart.set(cart);
  }

  public clearCart(): void {
    this._cart.set(null);
  }
}
