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

---

## Decisiones de Diseño y Alcance del Proyecto / Project Design Decisions & Scope

### Español
Intente utilizar todas las tecnologias que podrian ayudar a un e-commerce, ademas de aprovechar para agregar analiticas que se utilizan en este tipo de aplicaciones, asimismo aplicar reglas basicas de seguridad tanto en los endpoints como en el flujo de la aplicacion, tambien intente agregar tecnologias que ayuden a la calidad del codigo pero aumentaban mucho el tiempo de dockerizacion. Agregue un posible pipeline para el deploye en produccion, por ahora no funciona, pero se dejan las instrucciones en caso de que se tengan las credenciales necesarias. Decidi agregar tambien un bot que solo conteste preguntas sobre los productos, esto para incluir el uso de IA en la aplicacion, tmb inclui multilenguaje (ingles y español por ahora), el diseño que le di al front fue generico y deje que la IA lo decidiera al no verlo prioritario en esta entrega.
Tambien se incluye el uso de redis para la mejora de la velocidad utilizandolo como cache, y un brocker de apache kafka para que nos ayude con el performance cuando existan muchos productos por procesar.
En el front utilice sass para los estilos y le di estructura de patron 7-1, ademas tanto en front como en back estan utilizando Spec Driven Development y por lo tanto tengo una especificacion de contrato que ayuda a la IA a que su desarrollo tenga fronteras visibles y no vaya a alucinar.
En los pipelines de continuos integration trate de que fueran lo mas optimizados para que fueran lo mas rapido posible, ahorita en local los tiempos tienden a ser entre 90 y 150 segundos.
Una ultima cosa, por ahora solo hay dos usuarios que son de prueba y se agregaron en el login, el customer y el admin, ambos funcionan diferente de tal manera que se note la distincion, un ejemplo es que el admin no puede hacer compras, y el customer no puede agregar productos.

### English
I tried to use all the technologies that could help an e-commerce, in addition to taking the opportunity to add analytics that are typically used in these kinds of applications. Furthermore, I applied basic security rules both on the endpoints and in the application flow. I also tried to add technologies that assist with code quality, but they significantly increased the dockerization time. I added a potential pipeline for production deployment; it doesn't work for now, but instructions are provided in case the necessary credentials are available. I decided to also include a chatbot that only answers product-related questions to incorporate AI usage in the application. Additionally, I included multi-language support (English and Spanish for now). The design given to the frontend was generic, letting the AI decide it, as it was not considered a priority for this delivery.
The project also includes the use of Redis for speed improvement by using it as a cache, and an Apache Kafka broker to assist with performance when there are many products to process.
On the frontend, I used Sass for styling with a 7-1 pattern structure. Additionally, both the frontend and backend utilize Spec-Driven Development; hence, I have a contract specification that helps the AI keep its development within visible boundaries and avoid hallucinations.
I tried to keep the Continuous Integration pipelines as optimized as possible to make them run as fast as possible; currently, local build times tend to range between 90 and 150 seconds.
One last thing: for now, there are only two test users added at login, the customer and the admin. Both function differently to make their distinction clear. For example, the admin cannot make purchases, and the customer cannot add new products.
