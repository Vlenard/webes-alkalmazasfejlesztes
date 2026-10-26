# Architecture & How It Works

## Overview
The **JobBoard** project is organized around domain modules (Domain-Driven Design / Modular Feature Pattern) rather than technical layer grouping.

## Technical Stack
- **Framework / Bundler**: [Vite](https://vite.dev/) + [React](https://react.dev/) + [TypeScript](https://www.typescriptlang.org/)
- **Routing**: [React Router](https://reactrouter.com/) (BrowserRouter with nested routes)
- **Styling & UI Components**: [Tailwind CSS v4](https://tailwindcss.com/) + [daisyUI v5](https://daisyui.com/)

## Folder Breakdown

### 1. `src/app/`
Contains the application's foundational logic, global providers, and top-level route registry.
- `routes/AppRoutes.tsx`: Defines the application's routing map, linking route paths to modular domain views wrapped in shared layouts.

### 2. `src/modules/`
Features and domain logic are grouped by domain:
- `modules/home/`: Landing page and presentation-focused components for the root view.
- `modules/jobs/`:
  - `types/`: Domain-specific TypeScript models (e.g., `Job` interface).
  - `components/`: UI components belonging to the jobs domain (e.g., `JobCard`).
  - `pages/`: Page-level components (e.g., `JobsPage`).

### 3. `src/shared/`
Reusable logic and UI elements that cross domain boundaries:
- `layouts/MainLayout.tsx`: The primary app shell containing the navigation bar (`navbar`), content container (`<Outlet />`), and footer (`footer`).
- `components/NotFoundPage.tsx`: Fallback 404 page.

### 4. `docs/` & `AGENTS.md`
Documentation files kept up-to-date in English for AI agents and human developers:
- `AGENTS.md`: Rules of engagement, module boundaries, guidelines.
- `docs/status.md`: Project status, milestone tracking, and task backlog.
- `docs/architecture.md`: In-depth breakdown of architectural design choices and workflow.

## Build and Run
- Start dev server: `npm run dev`
- Type-check and production build: `npm run build`

## Skill System (.agents/skills)
The repository now features a project-level skill system located in `.agents/skills/`.
Skills follow the Antigravity specification with frontmatter headers (`name`, `description`) enabling automatic progressive discovery by AI agents:
- `vercel-react-best-practices`: Enforces modern Vercel & React engineering standards, component colocation, accessibility guidelines, rendering optimizations, and data synchronization patterns.
- `daisyui`: Official daisyUI component-library skill containing complete references for components, colors, configuration, and best practices.

## Data Layer & Backend Service Integration
Data fetching is structured using the Service & Custom Hook pattern, directly connected to the **`JobBoardServer`** Spring Boot backend:
- **`jobService.ts`**: Communicates with the Spring Boot backend (`http://localhost:8080/api` or `/api` via Vite dev proxy) for:
  - `GET /api/jobs?search=...&badge=...`: Dynamic list fetching with search and technology badge filters.
  - `GET /api/jobs/:id`: Fetching details for a specific job listing with 404 handling.
  - `POST /api/applications`: Submitting candidate applications to the backend.
  - Native `AbortSignal` cancellation support for quick query updates.
- **`useJobs.ts` & `useJobDetail.ts`**: Custom hooks orchestrating state lifecycles (`isLoading`, `error`, `data`) and cancellation.
- **Components (`JobsPage`, `JobCard`, `JobDetailPage`, `ApplicationForm`)**: Fully decoupled from the transport layer, rendering data smoothly from the live backend.
