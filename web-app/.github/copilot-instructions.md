# Copilot Instructions for patient-management/web-app

## Project Overview
- This is an Angular web application (see `angular.json`, `src/`, and `main.ts`).
- Tailwind CSS is used for styling (see `tailwind.config.js`, `styles.scss`).
- The project structure follows standard Angular conventions, with feature modules and components under `src/app/`.

## Key Workflows
- **Start Dev Server:** `ng serve` (or `npm start`)
- **Build:** `ng build`
- **Unit Tests:** `ng test`
- **E2E Tests:** `ng e2e` (framework not included by default)
- **Tailwind CSS:** Configured via `tailwind.config.js` and used in global and component styles.

## Patterns & Conventions
- **Component Generation:** Use Angular CLI (`ng generate component <name>`) for new components/services.
- **Styling:** Use Tailwind utility classes in templates. Custom styles go in `styles.scss` or component styles.
- **Dark Mode:** Controlled via `class` or `[data-pc-theme="dark"]` (see `tailwind.config.js`).
- **Assets:** Images and static files are in the `images/` and `public/` directories.
- **TypeScript:** All business logic and components are written in TypeScript.

## Integration Points
- **No backend code** is present in this repo; this is a frontend-only project.
- **External dependencies** are managed via `package.json` (Angular, Tailwind, etc.).

## Examples
- To add a new feature, create a module/component in `src/app/` and register it in the appropriate module.
- To customize Tailwind, edit `tailwind.config.js` and restart the dev server.

## References
- See `README.md` for more CLI commands and details.
- See `angular.json` for build and asset configuration.

---
If you are unsure about a workflow or pattern, prefer Angular CLI conventions and check the `README.md` for project-specific details.
