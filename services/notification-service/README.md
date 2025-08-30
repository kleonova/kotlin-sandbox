# notification-service

`notification-service` -

## Назначение

## Архитектура

+----------------+     HTTP POST /notify      +---------------------+     SMTP     +----------+
|  auth-service  | -------------------------> | notification-service | -----------> |  MailHog  |
+----------------+                            +---------------------+              +----------+
(Ktor 9001)                                      (Ktor 9002)                     (порт 1025)

## TODO

- Добавить Ktor Mail плагин (ktor-client-java + javax.mail)
- Написать шаблоны писем (HTML + текст)
- Endpoint: POST /send {to, subject, template, context}
- Тестирование