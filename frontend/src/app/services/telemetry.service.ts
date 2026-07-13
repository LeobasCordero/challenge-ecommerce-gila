import { Injectable, inject } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthStateService } from './auth-state.service';

/**
 * Service capturing user interaction telemetry and application exceptions,
 * storing them locally in browser localStorage.
 */
@Injectable({
  providedIn: 'root'
})
export class TelemetryService {
  private readonly router = inject(Router);
  private readonly authState = inject(AuthStateService);
  private readonly storageKey = 'gila_user_telemetry';

  constructor() {
    this.initRouteTracking();
  }

  /**
   * Capture route changes automatically.
   */
  private initRouteTracking(): void {
    if (this.router && this.router.events) {
      this.router.events
        .pipe(filter(event => event instanceof NavigationEnd))
        .subscribe((event: any) => {
          this.logEvent('NAVIGATE', { url: event.urlAfterRedirects || event.url });
        });
    }
  }

  /**
   * Log telemetry interaction events.
   * @param action category descriptor (NAVIGATE, CLICK, ERROR, etc.)
   * @param metadata key-value information payload
   */
  public logEvent(action: string, metadata: any = {}): void {
    if (typeof window === 'undefined' || !window.localStorage) {
      return;
    }

    const logs = this.getLogs();
    const newLog = {
      id: crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2, 9),
      timestamp: new Date().toISOString(),
      username: this.authState.username() || 'GUEST',
      action,
      metadata
    };

    logs.push(newLog);
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(logs));
    } catch {
      // Handle quota limits by keeping the last 100 entries
      if (logs.length > 100) {
        localStorage.setItem(this.storageKey, JSON.stringify(logs.slice(-100)));
      }
    }
  }

  /**
   * Retrieve all parsed telemetry logs from localStorage.
   * @returns array of telemetry actions
   */
  public getLogs(): any[] {
    if (typeof window === 'undefined' || !window.localStorage) {
      return [];
    }
    const data = localStorage.getItem(this.storageKey);
    if (!data) {
      return [];
    }
    try {
      return JSON.parse(data);
    } catch {
      return [];
    }
  }

  /**
   * Purge all telemetry storage.
   */
  public clearLogs(): void {
    if (typeof window === 'undefined' || !window.localStorage) {
      return;
    }
    localStorage.removeItem(this.storageKey);
  }
}
