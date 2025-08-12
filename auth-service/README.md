# auth-service

`auth-service` - 

## Назначение
- Интегрируется с Keycloak для аутентификации
- Управляет пользователями (профиль, регистрация и т.д.)
- Хранит данные пользователей в PostgreSQL (ksb_auth_db)
- Использует Flyway для миграций
- Предоставляет REST API
- Покрыт тестами
- Поддерживает лучшие практики проектирования

## Зависимости
keycloak-admin-client, kotlinx.serialization

- Ktor Server (core, auth, content-negotiation, etc.)
- Kotlinx Serialization
- Exposed (или другая ORM/библиотека работы с БД)
- HikariCP (пул соединений)
- Flyway
- PostgreSQL JDBC
- Koin (DI)
- Logback
- Test containers (для интеграционных тестов)
- Kotlin Test

## TODO

## 🛠️ 1. Настройка проекта

- [ ] Создать модуль `auth-service` в `kotlin-sandbox`
- [ ] Добавить `build.gradle.kts` с зависимостями:
    
- [ ] Настроить `application.conf` (HOCON) для:
    - Порт (9001)
    - БД (host, port, user, password, db name)
    - Keycloak (auth server URL, realm, client ID)
    - Логирование
- [ ] Настроить `Dockerfile` для `auth-service`
- [ ] Добавить `docker-compose.yml` (уже есть БД, добавить Keycloak и MailHog)

---

## 🔐 2. Интеграция с Keycloak

- [ ] Запустить Keycloak через Docker (в `docker-compose.yml`)
    - [ ] Настроить админ-консоль (порт 8080)
    - [ ] Создать realm: `ksb-realm`
    - [ ] Создать клиент: `auth-service-client` (с типом `confidential`)
    - [ ] Включить `Direct Access Grants` и `Service Accounts`
    - [ ] Настроить `Valid Redirect URIs` и `Web Origins`
- [ ] Настроить аутентификацию JWT в Ktor:
    - [ ] Добавить `install(Authentication)`
    - [ ] Использовать `jwt { }` с верификацией токена от Keycloak
    - [ ] Настроить `verifier` с помощью `jwkProvider` (публичный ключ из `.well-known/openid-configuration`)
- [ ] Реализовать эндпоинт `/auth/token` (опционально, если нужен прокси-доступ)
- [ ] Добавить проверку ролей (например, `@RequiresRole("user")` через Ktor features)

---

## 🗄️ 3. Работа с PostgreSQL и Flyway

- [ ] Настроить подключение к `ksb_auth_db` через Hikari
- [ ] Добавить Flyway:
    - [ ] Создать папку `src/main/resources/db/migration`
    - [ ] Написать `V1__create_users_table.sql`:
      ```sql
      CREATE TABLE users (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          username VARCHAR(255) UNIQUE NOT NULL,
          email VARCHAR(255) UNIQUE NOT NULL,
          first_name VARCHAR(255),
          last_name VARCHAR(255),
          created_at TIMESTAMP DEFAULT NOW(),
          updated_at TIMESTAMP DEFAULT NOW()
      );
      ```
- [ ] Настроить автоматический запуск миграций при старте приложения
- [ ] Создать сущность `User` (data class)
- [ ] Создать DAO/Repository для работы с пользователями (через Exposed или raw JDBC)

---

## 🌐 4. Реализация API

- [ ] Создать роуты:
    - `GET /api/v1/users/me` → возвращает профиль текущего пользователя (из JWT и БД)
    - `POST /api/v1/users/register` → создаёт пользователя в БД (и, опционально, в Keycloak)
    - `PUT /api/v1/users/me` → обновляет профиль
    - `GET /api/v1/health` → проверка работоспособности
- [ ] Валидация входных данных (например, через `kotlinx.validation` или вручную)
- [ ] Обработка ошибок:
    - [ ] Создать `ExceptionHandler` (с помощью `StatusPages`)
    - [ ] Возвращать `400`, `401`, `403`, `404`, `500` с JSON-телом
- [ ] Добавить OpenAPI/Swagger (например, через `ktor-swagger` или `kotlinx-serialization` + ручной JSON)

---

## 🧪 5. Тестирование

- [ ] Unit-тесты:
    - [ ] Сервисы, валидация, мапперы
    - [ ] Использовать `MockK` для моков
- [ ] Интеграционные тесты:
    - [ ] Запуск приложения с реальной БД через `TestContainers`
    - [ ] Тесты для эндпоинтов (с `ktor-server-test-host`)
    - [ ] Проверка:
        - Создание пользователя
        - Получение профиля
        - Валидация JWT
        - Обработка ошибок
- [ ] Тестирование миграций Flyway
- [ ] Покрытие тестами > 70% (настроить JaCoCo)

---

## 🧱 6. Архитектура и лучшие практики

- [ ] Применить принципы Clean Architecture (если уместно):
    - `domain`, `application`, `infrastructure`, `presentation`
- [ ] Использовать Dependency Injection (Koin):
    - [ ] Настроить модули: `databaseModule`, `routingModule`, `authModule`
- [ ] Логирование:
    - [ ] Добавить MDC (для трейсов запросов)
    - [ ] Логировать входящие запросы и ошибки
- [ ] Конфигурация:
    - [ ] Разделить `application.conf` на `dev`, `test`, `prod` (через профили)
- [ ] Безопасность:
    - [ ] CORS
    - [ ] Rate limiting (опционально)
    - [ ] Заголовки безопасности (например, через `ktor-freemarker` или ручные заголовки)

---

## 📦 7. Docker и CI/CD (опционально, но рекомендуется)

- [ ] Настроить `Dockerfile` для `auth-service`
- [ ] Добавить `auth-service` в `docker-compose.yml`
- [ ] Проверить, что сервис стартует и подключается к БД и Keycloak
- [ ] Настроить `.github/workflows` (если используешь GitHub):
    - [ ] Сборка
    - [ ] Тесты
    - [ ] Линтинг (ktlint)
- [ ] Настроить `ktlint` и `detekt` в проекте

---

## 🔄 8. Интеграция с notification-service (на будущее)

- [ ] Подумать о способе уведомления:
    - [ ] REST API вызов
    - [ ] Или очередь (Kafka/RabbitMQ — позже)
- [ ] При регистрации пользователя — отправлять запрос в `notification-service` на `POST /api/v1/emails`
- [ ] Использовать `Ktor Client` для внутренних вызовов

---

## ✅ 9. Проверка и запуск

- [ ] Запустить `docker-compose up` — должны стартовать:
    - `ksb_auth_db`
    - `keycloak`
    - `auth-service`
- [ ] Проверить:
    - [ ] Доступ к Keycloak Admin Console
    - [ ] Миграции Flyway прошли
    - [ ] Эндпоинты работают (через curl или Postman)
    - [ ] JWT токен из Keycloak проходит валидацию
    - [ ] Пользователь сохраняется в БД

---

## 📚 Дополнительные ресурсы

- [Keycloak + Ktor интеграция](https://ktor.io/docs/jwt.html)
- [Flyway с Kotlin](https://flywaydb.org/documentation/usage/api/)
- [Exposed ORM Guide](https://github.com/JetBrains/Exposed)
- [TestContainers для Kotlin](https://www.testcontainers.org/modules/databases/postgresql/)
- [Ktor + Docker](https://ktor.io/docs/docker.html)

---

> ✅ **Совет**: Делай по одному пункту в день. Сначала — база и миграции, потом Keycloak, потом API. Пиши тесты параллельно.

Когда закончишь `auth-service`, перейдём к `notification-service` с MailHog и тестовыми письмами.

Если хочешь — могу помочь с `docker-compose.yml` или первым миграционным скриптом.
