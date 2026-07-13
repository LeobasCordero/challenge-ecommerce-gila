import { ApplicationConfig, importProvidersFrom, ErrorHandler } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { authInterceptor } from './interceptors/auth.interceptor';
import { BASE_PATH } from './core/api/variables';
import { ApiModule } from './core/api/api.module';
import { Configuration } from './core/api/configuration';
import { TelemetryErrorHandler } from './core/telemetry-error-handler';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(
      withInterceptors([authInterceptor])
    ),
    {
      provide: ErrorHandler,
      useClass: TelemetryErrorHandler
    },
    {
      provide: BASE_PATH,
      useValue: window.location.origin
    },
    importProvidersFrom(
      ApiModule.forRoot(() => new Configuration({
        basePath: window.location.origin
      }))
    )
  ]
};
