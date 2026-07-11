import { Injectable, signal, computed } from '@angular/core';
import { CartDto } from '../api/model/cartDto';

@Injectable({
  providedIn: 'root'
})
export class CartStateService {
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

  public setCart(cart: CartDto): void {
    this._cart.set(cart);
  }

  public clearCart(): void {
    this._cart.set(null);
  }
}
