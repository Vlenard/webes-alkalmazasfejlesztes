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
  - `src/modules/auth/` for authentication, session management, and role-based access.
  - `src/shared/` for shared layouts (`MainLayout`) and generic pages (`NotFoundPage`).
- [x] Added `AGENTS.md` and documentation in `docs/`.
- [x] Implemented Auth Module (`src/modules/auth/`):
  - Domain models & DTOs for `UserRole` (`APPLICANT` / `COMPANY_MANAGER`), `AuthUser`, and Auth responses.
  - Token and session persistence service (`authService`) with JWT in localStorage.
  - Global `AuthContext` and `useAuth` hook with session re-validation via `/api/auth/me`.
  - Role-based `ProtectedRoute` supporting route access restriction.
  - Purpose-driven registration page (`RegisterPage`) with role cards (`APPLICANT` vs `COMPANY_MANAGER`) and conditional `companyName` field.
  - Login page (`LoginPage`) with role-aware redirection (`/employer` for company managers, `/jobs` for applicants).
  - Updated `MainLayout` with dynamic user navigation, role badges, and logout flow.
  - Configured protected routing in `AppRoutes` for `/my-applications` and `/employer`.

- [x] Implemented Jobs & Applications Domain Features (Részterv 4):
  - Domain types for `JobStatus` (`ACTIVE` / `CLOSED`), `ApplicationStatus` (`PENDING` / `ACCEPTED` / `REJECTED`), `ApplicantApplicationItem`, `EmployerApplicationItem`, and `CreateJobPayload`.
  - Comprehensive `jobService` with JWT Bearer header integration for all protected endpoints (`/api/jobs/my`, `/api/jobs` creation, `/api/jobs/:id/status` toggle, `/api/applications/my`, `/api/jobs/:id/applications`, `/api/applications/:id/status`, and binary resume file download).
  - Multipart resume upload in `ApplicationForm` with strict `.pdf` and `.docx` file validation.
  - Role-aware and status-aware job details page (`JobDetailPage`) with disabled CTA for closed jobs or company managers, and automatic login redirect for unauthenticated users.
  - Applicant dashboard (`MyApplicationsPage`) displaying application timeline, resume file name, and specific status alerts:
    - `ACCEPTED`: "Továbbjutott! A cég értesíti Önt a továbbiakról."
    - `REJECTED`: "Sajnáljuk, a jelentkezését elutasították."
    - `PENDING`: "Jelentkezése elbírálás alatt áll."
  - Employer dashboard (`EmployerDashboardPage`) with job posting creation, active/closed status toggle, applicant listing, status progression (acceptance/rejection), and binary resume download.

- [x] Implemented User Profile Management (Adatmódosítás):
  - Created `ProfilePage` (`/profile`) protected route allowing authenticated users to review and update personal details.
  - Profile features:
    - User identity badge & summary card with avatar initial and role details.
    - Editable full name (`name`) and e-mail address (`email`).
    - Non-modifiable role (`role`: `APPLICANT` or `COMPANY_MANAGER`) displayed as an informative read-only status card explaining account permissions.
    - Company name (`companyName`) configuration for employers and candidates.
    - Optional password change section with current password verification, new password length validation, and confirmation check.
    - Reset changes functionality restoring unmodified user data.
    - Integrated with daisyUI feedback alerts (`alert-success`, `alert-error`) and button loading state (`loading-spinner`).
  - Updated `authService.updateProfile` and `AuthContext` to persist updated JWT and user context.
  - Updated `MainLayout` with direct navigation to `/profile` in both desktop navbar and mobile sidebar drawer.

## Planned / Next Steps
- [x] Connect jobs list to a dynamic API or state store (connected to Spring Boot JobBoardServer REST API).
- [x] Implement job details view (`/jobs/:id`).
- [x] Add job filtering, search, and category selection.
- [x] Add job posting/creation flow on frontend.
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
- [x] Configured Vite build output (`build.outDir: '../JobBoardServer/src/main/resources/static'`, `emptyOutDir: true`) to enable static site serving directly from Spring Boot.
- [x] Implemented responsive pagination (`Pagination` component using daisyUI 5 `join`, `join-item`, `btn`) on `JobsPage` (`/jobs`):
  - URL query parameter synchronization (`?page=X`, `?size=Y`) via React Router `useSearchParams`.
  - Default 4 jobs per page (2x2 grid) with configurable page sizes (4, 6, 8, 12).
  - Informative counter displaying current slice range and total active jobs.
  - Automatic page reset when searching or resizing pages, with smooth scroll to top.
- [x] Implemented responsive navigation layout in `MainLayout`:
  - Desktop: Full horizontal navbar menu (`menu-horizontal`) with branding, navigation links, and auth controls.
  - Mobile: Sidebar drawer (`drawer`, `drawer-toggle`, `drawer-side`) toggled via hamburger button, with drawer overlay, user profile card, vertical menu links, and auth actions.
  - Automatic closing of drawer on navigation or route changes.
- [x] Implemented pagination on Employer Dashboard (`/employer`):
  - Added daisyUI 5 pagination support (`Pagination` component) for employer's posted jobs with URL query parameter synchronization (`?page=X`, `?size=Y`).
  - Configurable page size (4, 6, 8, 12 per page, default 4).
  - Synchronized status modifications, new job creation, and application inspection across pages.
- [x] Aligned Jelentkezők listája container with the first job card on Employer Dashboard:
  - Added matching top height spacer on desktop so the applicants inspector card top edge aligns with the top edge of the first job card.
  - Optimized sticky behavior on desktop (`lg:sticky lg:top-8`).
