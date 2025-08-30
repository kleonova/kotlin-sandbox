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

### 🌐 4. Реализация API

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

### 🧪 5. Тестирование

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

### 🧱 6. Архитектура и лучшие практики

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

### 🔄 8. Интеграция с notification-service (на будущее)

- [ ] Подумать о способе уведомления:
    - [ ] REST API вызов
    - [ ] Или очередь (Kafka/RabbitMQ — позже)
- [ ] При регистрации пользователя — отправлять запрос в `notification-service` на `POST /api/v1/emails`
- [ ] Использовать `Ktor Client` для внутренних вызовов

---

## 📚 Дополнительные ресурсы

- [Keycloak + Ktor интеграция](https://ktor.io/docs/jwt.html)
- [Flyway с Kotlin](https://flywaydb.org/documentation/usage/api/)
- [Exposed ORM Guide](https://github.com/JetBrains/Exposed)
- [TestContainers для Kotlin](https://www.testcontainers.org/modules/databases/postgresql/)
- [Ktor + Docker](https://ktor.io/docs/docker.html)

---

## Настройка Keycloak

1. Создать Realm
2. Создать Client

**Основные параметры при создании клиента**

| Параметр                | Описание                                           | Рекомендованное значение                               |
|-------------------------|----------------------------------------------------|--------------------------------------------------------|
| Client ID               | Уникальный идентификатор клиента в рамках Realm    | Любое уникальное имя (напр. my-web-app, mobile-client) |
| Client Protocol         | Используемый протокол аутентификации               | openid-connect (для OAuth2/OIDC)                       |
| Client Authentication   | Требовать ли аутентификацию клиента                | On для серверных приложений, Off для SPA/мобильных     |
| Authorization           | Включить сервис авторизации (UMA)                  | Off (если не используются политики авторизации)        |
| Authentication Flows    | Доступные потоки аутентификации                    | Standard flow + Direct access grants (для API)         |
| PKCE Method             | Метод защиты Code Exchange                         | S256 (для публичных клиентов)                          |
| Root URL	               | Базовый URL клиентского приложения                 | Полный URL (напр. https://app.example.com)             |
| Valid Redirect URIs     | Разрешенные URI для редиректа после аутентификации | Точные URI (напр. https://app.example.com/callback)    |
| Web Origins             | Разрешенные домены для CORS                        | Конкретные домены (напр. https://app.example.com)      |
| Always Display in UI    | Показывать клиента в интерфейсе входа              | Off (если не требуется выбор клиента)                  |
| Consent Required        | Требовать согласие пользователя                    | Off (для доверенных приложений)                        |
| Login Theme             | Тема оформления экрана входа	                      | Пусто (используется тема Realm)                        | 

3. Сохранить *Client Secret*
4. Создать пользователя
