# JobBoardServer

Spring Boot backend szerver a **JobBoard** webes alkalmazáshoz.
A szerver **Hexagonális architektúrát** (Ports and Adapters) követ, és a fájlok **Domain-Driven Design (DDD)** elv szerint vannak modulokba szervezve.

## Technológiai verem
- **Java**: 21+
- **Spring Boot**: 3.3.4
- **Spring Data JPA & Hibernate**
- **Adatbázis**: PostgreSQL (fejlesztés / éles), H2 (in-memory teszt környezet)
- **Dokumentáció**: Springdoc OpenAPI / Swagger UI
- **Build eszköz**: Apache Maven + Maven Wrapper (`./mvnw`)

## Futtatás és tesztelés

### Előfeltételek
- PostgreSQL fut a helyi gépen (Homebrew: `brew services start postgresql@16`)
- `jobboard` adatbázis létrehozva (Homebrew: `createdb jobboard`)

### Tesztek futtatása
A teljes tesztkészlet (28 automatizált teszt: domain unit tesztek, service tesztek, WebMvc szelettesztek, JPA tesztek, integrációs tesztek) futtatása:
```bash
./mvnw clean test
```

### Alkalmazás indítása
```bash
./mvnw spring-boot:run
```
Az alkalmazás a `http://localhost:8080` címen indul el.
Az induláskor a rendszer automatikusan betölti a frontend által használt 4 kezdő álláshirdetést, amennyiben az adatbázis még üres.

### Swagger / OpenAPI felület
Az interaktív API dokumentáció és tesztelő felület elérhető a böngészőből:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## Fő API Végpontok (illeszkedve a frontendhez)

| Metódus | Végpont | Leírás |
|---|---|---|
| `GET` | `/api/jobs` | Állások listázása (opcionális `search` és `badge` szűrőkkel) |
| `GET` | `/api/jobs/{id}` | Egy adott állás részletei |
| `POST` | `/api/jobs` | Új álláshirdetés feladása |
| `POST` | `/api/applications` | Jelentkezés leadása (a frontend `jobService.submitApplication` formátumában) |
| `POST` | `/api/jobs/{jobId}/applications` | Jelentkezés leadása közvetlenül egy állás URL-je alatt |
| `GET` | `/api/jobs/{jobId}/applications` | Egy adott álláshoz beérkezett jelentkezések |
| `GET` | `/api/applications` | Összes leadott jelentkezés listája |

---

## Architektúra és AI felkészítés
- `AGENTS.md`: Szabályok és konvenciók fejlesztők és AI ágensek számára.
- `docs/architecture.md`: Részletes hexagonális architektúra leírás, adatbázis séma és szekvenciadiagramok.
- `docs/status.md`: Projekt státusz és ütemterv.
- `.agents/skills/`: Újrafelhasználható AI képességek (`spring-hexagonal-architecture`, `spring-boot-best-practices`).
