# Project Status & Changelog

## Current Status
- **Stack**: React 19, TypeScript, Vite 8, React Router 7, Tailwind CSS 4, daisyUI 5.
- **Architecture**: React Domain-Driven Design (DDD) / Modular Feature Structure.
- **Health**: Builds cleanly with `npm run build`.

## Implemented Features & Milestones
- [x] Initial React + TypeScript setup via Vite in `w1/JobBoard`.
- [x] Boilerplate cleanup (removed default Vite sample assets & styles).
- [x] React Router 7 SPA integration with shared layout and nested routes.
- [x] Tailwind CSS v4 and daisyUI v5 integration and theming.
- [x] Refactored to React Domain-Driven Modular Architecture:
  - `src/app/routes/` for centralized route definitions.
  - `src/modules/jobs/` for job board features, types, and components.
  - `src/modules/home/` for home/landing page.
  - `src/shared/` for shared layouts (`MainLayout`) and generic pages (`NotFoundPage`).
- [x] Added `AGENTS.md` and documentation in `docs/`.

## Planned / Next Steps
- [x] Connect jobs list to a dynamic API or state store (connected to Spring Boot JobBoardServer REST API).
- [x] Implement job details view (`/jobs/:id`).
- [x] Add job filtering, search, and category selection.
- [ ] Add job posting/creation flow on frontend.
- [x] Connected application submission to Spring Boot backend (POST `/api/applications`).
- [x] Preset data service layer implemented with simulated network latency and AbortController.
- [x] Custom hook (`useJobs`) for state lifecycle management (loading, error, data).
- [x] Interactive filtering by search keyword and technology tags.
- [x] Configured custom daisyUI `lofi` theme with default light mode in `src/index.css`.
- [x] Implemented dedicated Job Detail page (`/jobs/:id`) with `useJobDetail` custom hook.
- [x] Connected `JobCard` "Részletek" button to dynamic route `/jobs/:id`.
- [x] Created `ApplicationForm` component supporting Name, Email, and CV file upload (placeholder).
- [x] Integrated `jobService.submitApplication` with simulated API latency and in-memory storage.
- [x] Embedded application modal on `JobDetailPage` triggered by "Jelentkezés" CTA buttons.
- [x] Made top navigation bar (`MainLayout`) sticky (`sticky top-0 z-50`).
- [x] Made jobs search bar sticky (`sticky top-20 z-40`) on `JobsPage`.
- [x] Removed tech-specific badge filters from `JobsPage` to support generic/multidisciplinary job listings.
