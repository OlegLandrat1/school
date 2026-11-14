# School ORM

Учебный backend курсов на Spring Boot 3.5.16, Spring Data JPA и PostgreSQL. Модель покрывает курсы, модули, уроки, задания, решения, записи на курс, квизы, категории, теги и отзывы. REST API пока не реализован, поэтому сценарии вызываются через сервисы.

## Требования

- JDK 21
- Maven 3.9+
- PostgreSQL 14 или новее

## База данных

Выполните в `psql` от имени администратора. Пароль замените на свой.
```sql
CREATE USER orm_user WITH PASSWORD 'change_me';
CREATE DATABASE orm_dev OWNER orm_user;
CREATE DATABASE orm_test OWNER orm_user;
```

- `orm_dev` используется приложением.
- `orm_test` используется только тестами. Тесты создают и удаляют схему в этой базе, поэтому данные `orm_dev` не затрагиваются.

## Переменные окружения

Учётные данные в коде не хранятся. Задайте их перед запуском:

| Переменная | Обязательна | По умолчанию | Назначение |
|---|---|---|---|
| `DB_USER` | да | нет | пользователь PostgreSQL |
| `DB_PASSWORD` | да | нет | пароль пользователя |
| `DB_URL` | нет | `jdbc:postgresql://localhost:5432/orm_dev` | база приложения |
| `DB_TEST_URL` | нет | `jdbc:postgresql://localhost:5432/orm_test` | база тестов |

Linux и macOS:
```bash
export DB_USER=orm_user
export DB_PASSWORD=change_me
```

Windows PowerShell:
```powershell
$env:DB_USER = "orm_user"
$env:DB_PASSWORD = "change_me"
```

Без `DB_USER` и `DB_PASSWORD` приложение не запустится.

## Сборка и запуск

Сборка без тестов:
```bash
mvn -q -DskipTests compile
```

Запуск приложения:
```bash
mvn spring-boot:run
```

Сейчас приложение поднимает контекст Spring и завершается, потому что точки входа (REST или консольного интерфейса) ещё нет. Схема таблиц создаётся автоматически (`ddl-auto: update`). Это временная настройка до подключения Flyway.

## Тесты

Все тесты:
```bash
mvn test
```

Один класс:
```bash
mvn -q test -Dtest=CourseServiceTest
```

Тесты используют профиль `test` (`src/test/resources/application-test.yml`). Схема создаётся на время прогона (`create-drop`), база `orm_test` очищается после него.

## Примеры использования

REST-эндпоинтов нет. Сценарии показаны вызовами сервисов:
```java
Long courseId = courseService.createCourse("Java", List.of("Intro", "Collections"));
Course course = courseService.getCourse(courseId);

enrollmentService.enroll(userId, courseId);
submissionService.submit(userId, assignmentId, "Ответ на задание");
quizService.recordAttempt(userId, quizId, 7);
reviewService.addReview(userId, courseId, 5, "Полезный курс");

courseService.deleteCourse(courseId);
```

## Структура проекта
```text
src/main/java/com/example/school/
├── SchoolApplication.java
├── course/
│   ├── Course.java, Module.java, Lesson.java, Assignment.java
│   ├── Submission.java, Enrollment.java
│   ├── Quiz.java, Question.java, AnswerOption.java, QuizSubmission.java
│   ├── Category.java, Tag.java, CourseReview.java
│   ├── *Repository.java
│   └── CourseService.java, EnrollmentService.java, SubmissionService.java,
│       QuizService.java, ReviewService.java
└── user/
    ├── User.java, Profile.java, Role.java
    └── UserRepository.java, ProfileRepository.java

src/main/resources/application.yml
src/test/resources/application-test.yml
src/test/java/com/example/school/course/   тесты курса, записей, решений, квизов, отзывов
```

## Модель данных

| Сущность | Связи |
|---|---|
| User | 1:1 Profile |
| Profile | 1:1 User (владелец внешнего ключа) |
| Category | 1:N Course, необязательна у курса |
| Tag | M:N Course через `course_tags` |
| Course | N:1 Category, M:N Tag, 1:N Module, 1:N Enrollment, 1:N CourseReview |
| Enrollment | N:1 User, N:1 Course, уникально `(user_id, course_id)` |
| Module | N:1 Course, 1:N Lesson, 1:1 Quiz (необязательно) |
| Lesson | N:1 Module, 1:N Assignment |
| Assignment | N:1 Lesson, 1:N Submission |
| Submission | N:1 User, N:1 Assignment, уникально `(user_id, assignment_id)` |
| Quiz | 1:1 Module (уникально `module_id`), 1:N Question, 1:N QuizSubmission |
| Question | N:1 Quiz, 1:N AnswerOption |
| AnswerOption | N:1 Question |
| QuizSubmission | N:1 User, N:1 Quiz (повторные попытки разрешены) |
| CourseReview | N:1 User, N:1 Course (без ограничения на количество) |

Все связи `@OneToMany` и `@ManyToMany` загружаются лениво (`LAZY`). Связи `@ManyToOne` и `@OneToOne` тоже помечены `LAZY`.

## Правила удаления

Удаление курса (`CourseService.deleteCourse`) каскадно удаляет:

- модули, уроки и задания;
- решения студентов на задания;
- записи на курс (`Enrollment`);
- квиз модуля вместе с вопросами, вариантами ответов и попытками;
- отзывы о курсе;
- строки связи с тегами (`course_tags`).

Пользователи, категории и теги при удалении курса сохраняются.

Категорию или тег, которые используют курсы, удалить нельзя: внешний ключ отклоняет такую операцию.

## Ленивая загрузка

Коллекция `Course.modules` загружается лениво. Вне транзакции обращение к ней без предварительной загрузки вызывает `LazyInitializationException`.

Тест `CourseServiceTest.loadsModulesOutsideTransaction` проверяет это поведение. Тест не помечен `@Transactional`, поэтому сессия закрыта до обращения к модулям.

Исправление в `CourseRepository`:
```java
@EntityGraph(attributePaths = "modules")
Optional&lt;Course&gt; findWithModulesById(Long id);
```

`CourseService.getCourse` использует этот метод, и модули загружаются вместе с курсом одним запросом. Остальные коллекции остаются ленивыми.

Чтобы увидеть красную версию теста, временно замените в `getCourse` вызов на `courses.findById(id)`.
