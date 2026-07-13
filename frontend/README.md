# Frontend Application (Angular Client)

This frontend client is built on **Angular 17** utilizing Standalone components, Signals state management, and Sass 7-1 architectural patterns.

---

## Directory Layout

We strictly classify our files by architectural intent:

```
src/
├── app/
│   ├── components/       # Presentation components (reusable widgets)
│   ├── services/         # Hand-written application services (AuthStateService, CartStateService)
│   ├── models/           # Custom local models/interfaces
│   ├── pipes/            # Custom Angular pipes
│   ├── directives/       # Reusable element directives
│   ├── guards/           # Route guards (admin guard, auth guards)
│   ├── interceptors/     # Http interceptors (authInterceptor)
│   ├── pages/            # Page/Route components (login, catalog, admin, checkout-success)
│   ├── utils/            # Shared constants, enums, and helpers
│   └── core/api/         # Generated OpenAPI client services & models
└── tests/                # Exclusive tests folder mirroring the app/ folder layout
```

---

## Styles & Sass 7-1 Pattern

Styles are extracted globally and mapped into layout and component partials inside `src/styles/`:
- **`src/styles/abstracts/`**: Contains global variables (`_variables.scss`) and typography guidelines.
- **`src/styles/layout/`**: Centralizes application layout patterns like `_app.scss`.
- **`src/styles/components/`**: Standardizes scoped style modules for components like `_admin.scss`, `_login.scss`, `_catalog.scss`, and `_checkout-success.scss`.
- **`src/styles/main.scss`**: Root index importing all Sass modules.

---

## Command Reference

### Development Server
Run the dev server on `http://localhost:4200/`:
```bash
npm start
```

### Production Build
Builds the client with optimization budgets to `/dist/frontend/browser/`:
```bash
npm run build
```
*Note: In the containerized environment, the compiled static assets are served through an Alpine Nginx reverse-proxy on port 80, which is preconfigured to handle Angular client-side routing.*

### Unit Tests (Karma / Jasmine)
Executes all local unit spec files inside the centralized `src/tests/` folder:
```bash
npm run test
```

### Contract Tests (Jest / Pact)
Executes consumer contract tests against mock providers:
```bash
npm run test:pact
```

### Code Quality Linters
Runs ESLint typescript checking and Stylelint SCSS validation rules:
```bash
# TS Linter
npm run lint

# SCSS Linter
npm run stylelint
```
