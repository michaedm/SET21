# Basic tests for SET21 🧪

This folder holds the **minimum set of tests** for the website as it exists today:
the "Siste nytt" (news) page, plus the `lokallag` and `kurs` API endpoints.

They follow the course lectures (Uke 39 – Enhetstesting, Uke 42 – Integrasjonstesting and Test Doubles):
JUnit, Arrange–Act–Assert, equivalence partitions (normal input, empty, null, one element), and a *Fake* instead of the real database.

---

## The big picture

The website works like a relay race:

```
Supabase (database)  →  Repository  →  Controller (/api/...)  →  script.js  →  the page
```

Each test file checks one or more legs of that race:

| File | Type | What it checks |
|---|---|---|
| `NyhetJsonTests` | Unit test | A news item from the database becomes a correct `Nyhet` object, and back into the JSON field names `script.js` uses (`tittel`, `innhold`, `publisert_dato`, `bilde_url`). |
| `KursJsonTests` | Unit test | Courses: prices (including 0 kr), dates, and optional fields like `sluttdato` being empty. |
| `LokallagJsonTests` | Unit test | Local chapters: names with æ, ø and å survive, and lists with one item. |
| `RepositoryIntegrationTests` | Integration test | Each repository asks the **right table**, with the **right sorting**, sends the **API key**, and turns the answer into Java objects. Also checks what happens with an empty table or an error from Supabase. |
| `ApiEndpointTests` | Integration test | Starts the real web server and calls `/api/nyheter`, `/api/lokallag` and `/api/kurs`, the same way the website does. It checks status codes (200, 404, 500) and the JSON content. |
| `FakeSupabase` | Helper (test double) | A tiny pretend Supabase that runs on your own machine. Tests decide what it answers, and it remembers what was asked. |

**24 tests in total. All of them pass.**

---

## Why a "fake" Supabase? 🤔

The tests **never touch the real database**. `pom.xml` points `SUPABASE_URL` at `http://localhost:18321` while the tests run. That means:

- ✅ Nobody's real news gets read or changed
- ✅ The tests give the same result every time (the lecture calls this *deterministic*)
- ✅ They work without internet
- ✅ You can test situations that are hard to cause on purpose, like "the database is down"

---

## How to run them ▶️

From the `SET21` folder:

```bash
mvn test
```

Or in IntelliJ: right-click the `basictests` folder → **Run 'Tests in basictests'**.

> ⚠️ The tests need `NyhetController.java`, `Nyhet.java` and `NyhetRepository.java`. If they ever go missing locally, get them back with `git restore src/main/java`.

> ℹ️ The code calls `Dotenv.load()`, so a `.env` file must exist in `SET21` even for the tests. The values in it don't matter during testing.

---

## Things the tests found or can't cover 🔍

1. **A new database column would break the API.** If someone adds a column to `publish` (for example `forfatter`), Jackson refuses the unknown field and `/api/nyheter` returns 500. A one-line fix is to add `@JsonIgnoreProperties(ignoreUnknown = true)` to the records.
2. **Errors from Supabase become a generic 500.** That's fine for now, but a clearer error message would help later.
3. **The page itself isn't tested automatically.** The lecture says to cover that with **user testing**. A minimal plan:
   - *Task:* "Find the newest news item and read it." Success: the user finds it without help.
   - *Task:* "Find out which courses are coming up." (when the Kurs page exists)
   - Watch quietly, let the user think aloud, note where they get stuck, and add the findings to the backlog.

---

## What to add next 🚀

- **Login / editor (sprint 2):** when editors can create or change news, test that it saves correctly. The lecture recommends an H2 test database for that.
- **Kurs rules:** when logic is added (for example "course is full", or "end date before start date"), unit test each rule with edge cases.
