# auth-service

`auth-service` - 

## Назначение

## Зависимости
ktor, exposed, flyway, postgresql, keycloak-admin-client, kotlinx.serialization, logback, koin

## TODO
- Подключить зависимости
- Настроить application.conf (подключения к БД, Keycloak (realm, clientId, secret, URL))
- настроить .env или docker-compose переменные окружения
3. 🗄️ База данных и миграции
- [ ] Подключить Flyway
- [ ] Создать миграции:
- V1__create_users_table.sql
- V2__add_user_profile_fields.sql (если нужно)
- [ ] Настроить Exposed DSL для работы с таблицей users
4. 🔐 Интеграция с Keycloak
- [ ] Настроить Keycloak Realm, Client, Roles
- [ ] Реализовать регистрацию пользователя:
- Создание пользователя через Keycloak Admin API
- Сохранение профиля в PostgreSQL
- [ ] Реализовать вход через Keycloak (OIDC flow)
- [ ] Настроить middleware для проверки JWT токенов
5. 📡 API
- [ ] POST /register — регистрация пользователя
- [ ] GET /profile — получить профиль текущего пользователя
- [ ] PUT /profile — обновить профиль
- [ ] GET /health — проверка состояния сервиса
- [ ] GET /users — (опционально) список пользователей (только для админов)
6. 🧪 Тестирование
- [ ] Юнит-тесты для бизнес-логики (например, сервис регистрации)
- [ ] Интеграционные тесты с тестовой БД
- [ ] Тесты API с использованием Ktor TestApplicationEngine или RestAssured
- [ ] Тесты взаимодействия с Keycloak (можно использовать Testcontainers)
7. 🧩 Логирование и мониторинг
- [ ] Настроить логирование через Logback
- [ ] Добавить метрики (например, через Micrometer)
- [ ] Настроить /health и /metrics endpoints
8. 🐳 Docker
- [ ] Создать Dockerfile для auth-service
- [ ] Убедиться, что сервис корректно работает в docker-compose
9. 📚 Документация
- [ ] Описание API (можно использовать Swagger/OpenAPI)
- [ ] README с инструкциями по запуску
- [ ] Скрипты для локальной настройки Keycloak (CLI или REST)
