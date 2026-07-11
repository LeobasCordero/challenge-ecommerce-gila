import { Injectable, signal, computed } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthStateService {
  // Signals for state
  private readonly _token = signal<string | null>(this.getLocalStorageItem('jwt_token'));
  private readonly _username = signal<string | null>(this.getLocalStorageItem('username'));
  private readonly _role = signal<string | null>(this.getLocalStorageItem('role'));

  // Public readonly accessors
  public readonly token = this._token.asReadonly();
  public readonly username = this._username.asReadonly();
  public readonly role = this._role.asReadonly();

  // Computed state
  public readonly isAuthenticated = computed(() => !!this._token());
  public readonly isAdmin = computed(() => this._role() === 'ADMIN');
  public readonly isCustomer = computed(() => this._role() === 'CUSTOMER');

  public login(token: string, username: string, role: string): void {
    this.setLocalStorageItem('jwt_token', token);
    this.setLocalStorageItem('username', username);
    this.setLocalStorageItem('role', role);

    this._token.set(token);
    this._username.set(username);
    this._role.set(role);
  }

  public logout(): void {
    this.removeLocalStorageItem('jwt_token');
    this.removeLocalStorageItem('username');
    this.removeLocalStorageItem('role');

    this._token.set(null);
    this._username.set(null);
    this._role.set(null);
  }

  private getLocalStorageItem(key: string): string | null {
    if (typeof window !== 'undefined' && window.localStorage) {
      return localStorage.getItem(key);
    }
    return null;
  }

  private setLocalStorageItem(key: string, value: string): void {
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.setItem(key, value);
    }
  }

  private removeLocalStorageItem(key: string): void {
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.removeItem(key);
    }
  }
}
