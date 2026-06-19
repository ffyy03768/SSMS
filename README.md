# SSMS — Student Score Management System

A client/server student score management system built with an **Android client** and a **Spring Boot backend**, with data persisted in an **H2 database**. The client uses Kotlin + MVVM; the backend uses Spring Boot + Spring Data JPA. The system models data as a proper **one-to-many relational schema (Student / Course / Grade)** instead of a flat "one student, one course, one score" table, and supports full CRUD for students, grades and courses, plus major filtering, search, GPA calculation, score distribution and ranking.

## Features

- **Student management** — list view (name, student number, class, GPA, course count); create / edit / delete students.
- **Grade management** — per-student transcript (color-coded by score); add / edit / delete a single grade.
- **Course management** — CRUD for courses; deleting a course that is referenced by existing grades is rejected with a clear message (HTTP 409).
- **Filter & search** — filter students by major from the top bar; search by name or student number; the two can be combined.
- **Statistics & ranking** — overall summary (students / courses / grades / overall average), score distribution (excellent / pass / fail), per-course average scores, and a student ranking ordered by **GPA**.
- **Dashboard** — a server-rendered page at `/dashboard` for a quick visual view of the database.
- **Persistence** — H2 file database on the server + Room local cache on the client (single source of truth); data survives restarts.

## Tech Stack

**Client (Android · `StudentManager/`)**
- Kotlin, MVVM, ViewBinding
- Retrofit 2 + RxJava 2 (networking & thread scheduling)
- Room (local database with one-to-many `@Relation`)
- Material Components, ConstraintLayout
- minSdk 24 / targetSdk 36

**Backend (`crudSpringBoot/`)**
- Kotlin, Spring Boot 3.4.x
- Spring Web (RESTful API)
- Spring Data JPA + Hibernate (ORM)
- Thymeleaf (dashboard page)
- H2 Database (file mode, `jdbc:h2:file:./data/studentdb`)

## Project Structure

```
SSMS/
├── StudentManager/                 # Android client (Android Studio project)
│   └── app/src/main/
│       ├── java/com/example/studentmanagermvcandrxjava/
│       │   ├── mainScreen/         # MainScreenActivity, MainScreenViewModel, StudentAdapter
│       │   ├── detail/             # StudentDetailActivity, StudentDetailViewModel, GradeAdapter
│       │   ├── addEdit/            # AddEditStudentActivity, AddEditStudentViewModel
│       │   ├── course/             # CourseListActivity, CourseViewModel, CourseAdapter
│       │   ├── stats/              # StatsActivity, StatsViewModel, RankAdapter
│       │   ├── model/
│       │   │   ├── MainRepository.kt
│       │   │   ├── api/            # ApiService.kt (Retrofit), Dtos.kt
│       │   │   └── local/          # MyDatabase.kt
│       │   │       └── student/    # StudentEntity, GradeEntity, StudentWithGrades, StudentDao
│       │   └── utils/              # Constance, ApiServiceSingleTon, Extensions, Grades, AppViewModelFactory
│       ├── res/                    # layouts, menus, drawables, values (colors/strings/themes/styles)
│       └── AndroidManifest.xml
└── crudSpringBoot/                 # Spring Boot backend
    └── src/main/
        ├── kotlin/org/example/
        │   ├── student_spring/     # StudentSpringApplication (entry), ServletInitializer
        │   ├── controller/         # StudentController, GradeController, CourseController, DashboardController
        │   ├── service/            # StudentService
        │   ├── repository/         # Repositories.kt (StudentRepository / CourseRepository / GradeRepository)
        │   ├── entity/             # Student, Course, Grade
        │   ├── dto/                # Dtos.kt
        │   └── config/             # DataSeeder (sample data on first run)
        └── resources/
            ├── application.properties
            └── templates/dashboard.html
```

> Note: the entry class `StudentSpringApplication` lives in the `org.example.student_spring` package, while the rest of the code lives directly under `org.example`. The entry class therefore declares `@SpringBootApplication(scanBasePackages = ["org.example"])`, `@EntityScan("org.example.entity")` and `@EnableJpaRepositories("org.example.repository")` so all components, entities and repositories are picked up.

## Data Model

Three tables form a one-to-many schema:

- `Student` 1 — N `Grade`
- `Course` 1 — N `Grade`

`Grade` is the join table carrying `score` and `term`, with foreign keys to `Student` and `Course`, which expresses the many-to-many enrollment relationship between students and courses.

## Getting Started

### 1. Run the backend
Requires JDK 17 or later.

```bash
cd crudSpringBoot

# Option A: command line
./gradlew bootRun          # Windows: gradlew.bat bootRun

# Option B: open the project in IntelliJ IDEA and run the main() in
# src/main/kotlin/org/example/student_spring/StudentSpringApplication.kt
```

When the console prints `Started StudentSpringApplication ...`, the server is up on port **8080**. On first run it creates the tables and seeds sample data automatically.

- Dashboard: http://localhost:8080/dashboard
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/studentdb`, user `sa`)

### 2. Run the client
Open the `StudentManager/` project in Android Studio and wait for the Gradle sync to finish.

- **Emulator**: create an emulator with API level >= 24 (e.g. Pixel 6) and run. The emulator reaches the host machine's backend via `10.0.2.2`, which is already configured in `utils/Constance.kt` (`BASE_URL = "http://10.0.2.2:8080/"`). No change needed.
- **Physical device**: change `BASE_URL` in `utils/Constance.kt` to your computer's LAN IP (e.g. `http://192.168.x.x:8080/`) and make sure the phone and computer are on the same network.

> Start the backend first, then run the client.

## REST API

| Method | Path | Description |
|---|---|---|
| GET | `/students` | List all students (with grades, average, GPA, course count) |
| GET | `/students/{id}` | Get one student |
| POST | `/students` | Create a student |
| PUT | `/students/{id}` | Update a student |
| DELETE | `/students/{id}` | Delete a student (cascades to their grades) |
| POST | `/students/{id}/grades` | Add a grade to a student |
| PUT | `/grades/{id}` | Update a grade |
| DELETE | `/grades/{id}` | Delete a grade |
| GET | `/courses` | List all courses |
| POST | `/courses` | Create a course |
| PUT | `/courses/{id}` | Update a course |
| DELETE | `/courses/{id}` | Delete a course (returns 409 if referenced by grades) |
| GET | `/dashboard` | Visual dashboard (HTML) |

All JSON fields are camelCase.

## GPA Calculation

GPA is derived from the credit-weighted average score:

```
GPA = (credit-weighted average - 50) / 10
credit-weighted average = Σ(score × credit) / Σ(credit)
```

For example, a weighted average of 90 → GPA 4.0, 80 → 3.0, 100 → 5.0. The client and the server use the same formula.

## Troubleshooting

- **Client list is empty / "load failed"** — make sure the backend is running; use `10.0.2.2` on the emulator or your LAN IP on a physical device; cleartext HTTP is enabled in the manifest (`usesCleartextTraffic`).
- **Backend fails to start with a column error (e.g. NULL not allowed for column ...)** — an old database file with the previous schema is present. Delete `crudSpringBoot/data/studentdb.mv.db` and restart.
- **Cannot delete a course** — if the course already has grade records it is rejected with HTTP 409; delete the related grades first.

## License

For coursework / educational use.
