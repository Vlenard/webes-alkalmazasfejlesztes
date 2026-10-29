# Elemi Komponensek Kiszervezése – Műszaki Specifikáció (`Final/JobBoard`)

## Általános Szabályok Minden Agent Számára
1. **Munkakönyvtár**: `/Users/vlenard/code/webes-alkalmazasfejlesztes/Final/JobBoard`
2. **1 Worker = Legfeljebb 1 Komponens vagy 1 Oldal**: Minden `frontend-workers` agent kizárólag a neki kijelölt **1 darab `.tsx` fájlt** hozhatja létre vagy módosíthatja!
3. **AI-karakterisztikus ikonok szigorú tilalma (`AGENTS.md`)**: Tilos bármilyen AI-hoz köthető emoji vagy szimbólum használata! Csak tiszta szöveg és minimalista funkcionális SVG ikonok használhatók.
4. **Token jelentési kötelezettség**: Minden `workers` csoportba tartozó agent köteles a jelentésébe beleírni a hátralévő tokenjeit (`Remaining tokens: <becsült érték / %>`). Ha egy agentnek kevés tokenje marad, új agent váltja fel.
5. **Reszponzivitás és Szimmetria**: Minden komponensnek hibátlanul kell skálázódnia desktop (`1280x800`) és mobil (`390x844`) nézetben, vízszintes görgetés (`scrollWidth > clientWidth`) nélkül.

---

## 1. Fázis: Új Elemi és Újrafelhasználható Komponensek (9 db)

### 1.1. `src/shared/components/SkillBadge.tsx` (`fe-worker-1`)
- **Cél**: Elemi skill badge komponens, amely egységesíti a skill címkék megjelenítését a teljes alkalmazásban.
- **Props**:
  ```tsx
  export interface SkillBadgeProps {
    skill: string
    variant?: 'neutral' | 'matched' | 'missing' | 'outline' | 'suggestion'
    size?: 'xs' | 'sm' | 'md'
    onRemove?: (skill: string) => void
    onClick?: (skill: string) => void
    disabled?: boolean
    className?: string
  }
  ```
- **Megjelenítés**:
  - Ha `variant === 'suggestion'` vagy `onClick` meg van adva (és nincs `onRemove`): `<button type="button" onClick={() => onClick?.(skill)} disabled={disabled} className="badge badge-outline badge-sm gap-1 py-2.5 px-2.5 hover:badge-neutral transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed">+ {skill}</button>`.
  - Egyébként `<span>`:
    - `variant === 'matched'`: `badge badge-success badge-sm py-2 px-3 font-medium`
    - `variant === 'missing'` vagy `'outline'`: `badge badge-outline badge-sm py-2 px-3`
    - `variant === 'neutral'` (alapértelmezett): ha `onRemove` van: `badge badge-neutral gap-1.5 py-3 px-3 text-sm font-medium`; ha nincs `onRemove`: `badge badge-neutral badge-sm py-2 px-2.5 font-medium` (vagy `size === 'xs'` esetén `badge badge-neutral text-xs py-2 px-2.5`).
  - Ha `onRemove` meg van adva és `!disabled`, rendereljen egy `<button type="button" onClick={() => onRemove(skill)} className="btn btn-ghost btn-xs btn-circle h-4 w-4 min-h-0 p-0 text-neutral-content/80 hover:text-neutral-content hover:bg-neutral-content/20" aria-label={`${skill} eltávolítása`}>` gombot tiszta `×` SVG ikonnal.
- Mind named export (`export const SkillBadge`), mind `export default SkillBadge` legyen elérhető.

### 1.2. `src/shared/components/SkillBadgeList.tsx` (`fe-worker-2`)
- **Cél**: Skill-lista megjelenítése `SkillBadge` elemekkel, opcionális `matchedSkills` kiemeléssel, törléssel és üres állapot szöveggel.
- **Props**:
  ```tsx
  export interface SkillBadgeListProps {
    skills?: string[]
    matchedSkills?: string[]
    variant?: 'neutral' | 'outline' | 'match-highlight'
    size?: 'xs' | 'sm' | 'md'
    emptyText?: string
    onRemoveSkill?: (skill: string) => void
    disabled?: boolean
    className?: string
  }
  ```
- **Működés**:
  - Ha `!skills || skills.length === 0`: ha van `emptyText`, renderelje `<p className="text-xs text-base-content/50 italic">{emptyText}</p>`, különben `null`.
  - Ha `variant === 'match-highlight'`, építsen case-insensitive `Set`-et a `matchedSkills` elemekből: ha a skill benne van, `SkillBadge` `variant="matched"`, különben `variant="missing"`.
  - Egyébként adja át a `variant` (`'neutral'` vagy `'outline'`), `size`, `onRemove={onRemoveSkill}`, `disabled` propokat a `SkillBadge`-nek egy `flex flex-wrap items-center gap-1.5` konténerben.
- Mind named export (`export const SkillBadgeList`), mind `export default SkillBadgeList`.

### 1.3. `src/shared/components/StatusBadge.tsx` (`fe-worker-3`)
- **Cél**: Álláshirdetés (`ACTIVE`, `CLOSED`) és jelentkezés (`PENDING`, `ACCEPTED`, `REJECTED`) státusz badge elemi komponens.
- **Props**:
  ```tsx
  export interface StatusBadgeProps {
    status: 'ACTIVE' | 'CLOSED' | 'PENDING' | 'ACCEPTED' | 'REJECTED'
    perspective?: 'applicant' | 'employer'
    closedDetailText?: boolean
    className?: string
  }
  ```
- **Megjelenítés**:
  - `ACTIVE`: `<span className="badge badge-success text-white py-2 px-4 h-auto font-medium">Aktív</span>`
  - `CLOSED`: ha `closedDetailText`: `<span className="badge badge-neutral py-2 px-4 h-auto font-medium">Lezárva (Jelentkezőknek rejtett)</span>`, különben `<span className="badge badge-error text-white py-2 px-4 h-auto font-medium">Lezárva</span>`
  - `ACCEPTED`: `<span className="badge badge-success text-white py-2 px-4 h-auto font-semibold">{perspective === 'applicant' ? 'Továbbjutott' : 'Elfogadva'}</span>`
  - `REJECTED`: `<span className="badge badge-error text-white py-2 px-4 h-auto font-semibold">Elutasítva</span>`
  - `PENDING`: `<span className="badge badge-warning py-2 px-4 h-auto font-semibold">{perspective === 'applicant' ? 'Elbírálás alatt' : 'Elbírálásra vár'}</span>`
- Mind named export (`export const StatusBadge`), mind `export default StatusBadge`.

### 1.4. `src/modules/jobs/components/MatchScoreBadge.tsx` (`fe-worker-4`)
- **Cél**: Százalékos illeszkedési mutató badge (`{score}% illeszkedés`) színkódolással.
- **Props**:
  ```tsx
  export interface MatchScoreBadgeProps {
    score?: number | null
    showZero?: boolean
    className?: string
  }
  ```
- **Működés**:
  - Ha `typeof score !== 'number'` vagy (`Math.round(score) <= 0 && !showZero`), adjon vissza `null`-t.
  - Kerekített `rounded = Math.round(score)`:
    - `rounded >= 70`: `badge badge-success text-white py-2 px-3 h-auto font-semibold`
    - `rounded >= 30`: `badge badge-warning py-2 px-3 h-auto font-semibold`
    - `rounded < 30`: `badge badge-ghost py-2 px-3 h-auto font-semibold`
  - Szöveg: `{rounded}% illeszkedés`.
- Mind named export (`export const MatchScoreBadge`), mind `export default MatchScoreBadge`.

### 1.5. `src/modules/jobs/components/JobSearchBar.tsx` (`fe-worker-5`)
- **Cél**: Keresősáv komponens az állások szűréséhez (kiszervezve a `JobsPage.tsx`-ből).
- **Props**:
  ```tsx
  export interface JobSearchBarProps {
    searchTerm: string
    onSearchChange: (value: string) => void
    placeholder?: string
  }
  ```
- **Megjelenítés**:
  - Pontosan a `JobsPage.tsx` sticky keresősáv szerkezete (`sticky top-20 z-40 bg-base-100 p-4 rounded-box shadow-md border border-base-300`), nagyító SVG ikonnal, `input input-bordered w-full` mezővel és feltételes „Törlés” gombbal (`btn btn-ghost py-2 px-4`).
- Mind named export (`export const JobSearchBar`), mind `export default JobSearchBar`.

### 1.6. `src/modules/auth/components/UserSummaryCard.tsx` (`fe-worker-6`)
- **Cél**: Felhasználói profil összefoglaló kártya (kiszervezve a `ProfilePage.tsx`-ből), amely `SkillBadgeList`-et használ a rögzített készségek megjelenítéséhez.
- **Props**:
  ```tsx
  import type { AuthUser } from '../types/auth'

  export interface UserSummaryCardProps {
    user: AuthUser | null
  }
  ```
- **Megjelenítés**:
  - Pontosan a `ProfilePage.tsx` felső kártyájának elrendezése (`card bg-base-100 border border-base-300 shadow-sm p-6`), monogram avatarral, névvel, szerepkör badge-dzsel (`Céges kezelő` / `Álláskereső`), cégnévvel, e-maillel, `#id`-val, és `!isCompanyManager` esetén a `SkillBadgeList` komponenssel (`emptyText="Még nincsenek készségek rögzítve. Az alábbi űrlapon adhatod meg őket az ajánlórendszer működéséhez."`).
- Mind named export (`export const UserSummaryCard`), mind `export default UserSummaryCard`.

### 1.7. `src/modules/jobs/components/EmployerJobCard.tsx` (`fe-worker-7`)
- **Cél**: A munkaadó egyetlen saját álláshirdetés-kártyája az `EmployerDashboardPage.tsx` bal oldali listájában (`StatusBadge` és `SkillBadgeList` használatával).
- **Props**:
  ```tsx
  import type { Job } from '../types/job'

  export interface EmployerJobCardProps {
    job: Job
    isSelected: boolean
    isUpdatingStatus: boolean
    onToggleStatus: (job: Job) => void
    onEditSkills: (job: Job) => void
    onSelectJob: (job: Job) => void
  }
  ```
- **Megjelenítés**:
  - Megőrzi az `EmployerDashboardPage.tsx` kártya pontos osztályait, méreteit és gombsorát (`StatusBadge status={job.status ?? 'ACTIVE'} closedDetailText`, `SkillBadgeList skills={job.skills} variant="outline"`, „Hirdetés lezárása/újranyitása”, „Skillek szerkesztése”, „Jelentkezők megtekintése”).
- Mind named export (`export const EmployerJobCard`), mind `export default EmployerJobCard`.

### 1.8. `src/modules/jobs/components/EmployerApplicantsPanel.tsx` (`fe-worker-8`)
- **Cél**: A kiválasztott álláshirdetésre jelentkezők jobb oldali panelje az `EmployerDashboardPage.tsx`-ből (`StatusBadge` használatával).
- **Props**:
  ```tsx
  import type { EmployerApplicationItem, Job } from '../types/job'

  export interface EmployerApplicantsPanelProps {
    selectedJob: Job | null
    applications: EmployerApplicationItem[]
    isLoading: boolean
    error: string | null
    actionLoadingAppId: number | null
    downloadingAppId: number | null
    onUpdateStatus: (applicationId: number, status: 'ACCEPTED' | 'REJECTED') => void
    onDownloadResume: (app: EmployerApplicationItem) => void
  }
  ```
- **Megjelenítés**:
  - **KRITIKUS SZIMMETRIA SZABÁLY**: A külső konténer tartalmazza a `<div className="hidden lg:block h-8" aria-hidden="true" />` távtartót és a `<div className="card bg-base-100 border border-base-200 shadow-sm">` kártyát, így desktopon a bal oldali első hirdetéskártyával pixelpontosan egy vonalban (`topEdgeDifference = 0px`) marad!
  - Minden jelentkező kártyán `StatusBadge status={app.status} perspective="employer"` jeleníti meg a státuszt.
- Mind named export (`export const EmployerApplicantsPanel`), mind `export default EmployerApplicantsPanel`.

### 1.9. `src/modules/jobs/components/CreateJobModal.tsx` (`fe-worker-9`)
- **Cél**: Új álláshirdetés létrehozása felugró ablak kiszervezése az `EmployerDashboardPage.tsx`-ből (`SkillBadgeInput` használatával).
- **Props**:
  ```tsx
  export interface CreateJobModalProps {
    isOpen: boolean
    defaultCompany?: string
    onClose: () => void
    onCreated: () => Promise<void> | void
  }
  ```
- **Működés**:
  - Ha `!isOpen`, `null`-t ad vissza.
  - Saját belső form state-et (`createForm`, `requirementsInput`, `isCreating`, `createError`) kezel, szinkronizálva a `defaultCompany` értéket.
  - Beküldéskor meghívja a `jobService.createJob(payload)`-ot, majd az `await onCreated()` és `onClose()` függvényeket.
  - Megőrzi a `modal modal-open` és `modal-box w-11/12 max-w-2xl max-h-[90vh] overflow-y-auto bg-base-100 p-4 sm:p-6` struktúrát.
- Mind named export (`export const CreateJobModal`), mind `export default CreateJobModal`.

---

## 2. Fázis: Meglévő Komponensek és Oldalak Refaktorálása az Elemi Komponensek Használatára

1. **`src/shared/components/SkillBadgeInput.tsx`** (`fe-worker-10`):
   - A felvett skillek és a `suggestedSkills` kirajzolásához a `SkillBadge` komponenst használja (`<SkillBadge skill={skill} variant="neutral" onRemove={handleRemoveSkill} disabled={disabled} />` és `<SkillBadge skill={suggested} variant="suggestion" onClick={addSkill} disabled={disabled} />`).
   - Megőrzi a `whitespace-normal break-words` osztályokat a `label` és `helperText` elemeken!
2. **`src/modules/jobs/components/JobCard.tsx`** (`fe-worker-11`):
   - Az illeszkedési jelvényhez a `MatchScoreBadge` (`<MatchScoreBadge score={job.matchScore} showZero={isApplicant} />`), a skillekhez a `SkillBadgeList` (`<SkillBadgeList skills={job.skills} matchedSkills={job.matchedSkills} variant="match-highlight" className="my-1" />`) komponenst használja.
3. **`src/modules/jobs/components/RecommendedJobsSection.tsx`** (`fe-worker-12`):
   - A jelentkező saját skilljeinek megjelenítéséhez a `SkillBadgeList` (`<SkillBadgeList skills={userSkills} variant="neutral" />`) komponenst használja.
4. **`src/modules/auth/pages/ProfilePage.tsx`** (`fe-worker-13`):
   - A felső összefoglaló kártya helyett a `<UserSummaryCard user={user} />` komponenst használja.
5. **`src/modules/jobs/pages/JobsPage.tsx`** (`fe-worker-14`):
   - A keresősáv helyett a `<JobSearchBar searchTerm={searchTerm} onSearchChange={handleSearchChange} />` komponenst használja.
6. **`src/modules/jobs/pages/EmployerDashboardPage.tsx`** (`fe-worker-15`):
   - A kártyalistában az `<EmployerJobCard ... />`, a jobb oldali sávban az `<EmployerApplicantsPanel ... />`, az új hirdetés feladásához pedig a `<CreateJobModal ... />` komponenst használja.
7. **`src/modules/jobs/pages/JobDetailPage.tsx`** (`fe-worker-16`):
   - A fejlécben használja a `StatusBadge` és `MatchScoreBadge` komponenseket, valamint a leírás/követelmények alatt megjeleníti az álláshoz szükséges skilleket a `SkillBadgeList` (`variant="match-highlight"`) komponenssel!
