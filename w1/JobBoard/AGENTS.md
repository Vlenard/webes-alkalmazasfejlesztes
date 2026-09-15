# AGENTS.md - Development & Architecture Guidelines

## Project Overview
This repository contains the **JobBoard** web application, built with React, TypeScript, Vite, Tailwind CSS v4, daisyUI v5, and React Router v7.

## Architecture Guidelines (Domain-Driven Design / Modular Structure)
We organize application code by domain modules under `src/modules/` to ensure high cohesion and low coupling.

### Directory Structure
```
src/
├── app/                  # Application bootstrap, routing configuration, global providers
│   └── routes/           # Central route declarations (AppRoutes.tsx)
├── modules/              # Domain-specific feature modules
│   ├── home/             # Home / landing domain
│   │   └── pages/
│   └── jobs/             # Job board domain
│       ├── components/   # Domain-specific components (e.g., JobCard)
│       ├── types/        # Domain-specific types/interfaces (e.g., Job)
│       ├── services/     # (Optional) Domain API/data fetchers
│       └── pages/        # Route page views for the jobs domain
├── shared/               # Cross-cutting concerns, reusable components, layouts
│   ├── components/       # Generic components used across domains (e.g., NotFoundPage)
│   └── layouts/          # Top-level shell layouts (e.g., MainLayout)
├── App.tsx               # Top-level Router root
├── main.tsx              # React DOM render entry point
└── index.css             # Tailwind v4 & daisyUI imports
```

## Rules for Agents & Developers
1. **Domain Boundary Isolation**:
   - Keep domain business logic, data models, and feature-specific components inside their respective module folder under `src/modules/<domain>/`.
   - Do not import directly from another module's internal components. If a component or helper is needed by multiple domains, move or extract it into `src/shared/`.
2. **Language & Documentation**:
   - Internal project documentation (`docs/`, `AGENTS.md`) MUST be maintained in English.
   - Keep `docs/status.md` and `docs/architecture.md` updated with relevant architectural decisions and progress.
3. **Styling & UI**:
   - Use Tailwind utility classes and daisyUI components.
   - Avoid creating custom CSS files for individual components unless absolutely necessary.
4. **Testing & Verification**:
   - Always verify that `npm run build` succeeds without TypeScript or bundling errors before finishing any task.
