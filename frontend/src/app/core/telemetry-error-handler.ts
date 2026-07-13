import { ErrorHandler, Injectable, Injector, inject } from '@angular/core';
import { TelemetryService } from '../services/telemetry.service';

/**
 * Custom ErrorHandler catching application crashes and registering them as telemetry events.
 */
@Injectable()
export class TelemetryErrorHandler implements ErrorHandler {
  private readonly injector = inject(Injector);

  /**
   * Capture and intercept exceptions.
   * @param error raw runtime error instance
   */
  handleError(error: any): void {
    console.error(error);

    try {
      const telemetry = this.injector.get(TelemetryService);
      telemetry.logEvent('ERROR', {
        message: error.message || error.toString(),
        stack: error.stack || ''
      });
    } catch {
      // Silence recovery failures during bootstrapping
    }
  }
}
