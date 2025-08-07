# harbor-connector

`harbor-connector` — это прокси-сервис, выступающий в роли посредника между Docker-клиентом и Harbor.  
Сервис предназначен для тестирования и отладки запросов к API Registry v2, реализованного в Harbor.

## Назначение

Сервис позволяет:

- Перехватывать и анализировать запросы от Docker-клиента к Harbor.
- Логировать и модифицировать запросы/ответы в целях отладки.
- Использоваться как песочница для проверки взаимодействия с Registry API v2.

## Поддерживаемые операции

Сервис прозрачно обрабатывает стандартные операции API Registry v2, включая:
- Запросы к манифестам (`GET /v2/<repo>/manifests/<tag>`)
- Загрузка слоёв (`GET /v2/<repo>/blobs/<digest>`)
- Проверка доступности репозитория (`GET /v2/`)

## Использование

```
docker pull host.docker.internal:9001/docker-hub/nginx
docker pull host.docker.internal:9001/docker-hub/matomo
docker pull host.docker.internal:9001/docker-hub/sonarqube:latest
```

