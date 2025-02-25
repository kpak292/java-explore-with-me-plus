# java-explore-with-me

TEAM 1 PROJECT

## Main DB structure

```mermaid
erDiagram
    categories {
        bigint id PK
        varchar(50) name
    }

    users {
        bigint id PK
        varchar(250) name
        varchar(254) email
    }

    locations {
        bigint id PK
        double lat
        double lon
    }

    events {
        bigint id PK
        varchar(2000) annotation
        bigint category_id FK
        bigint confirmed_requests
        timestamp created_on
        varchar(7000) description
        timestamp event_date
        bigint user_id FK
        bigint location_id FK
        boolean paid
        integer participant_limit
        timestamp published_on
        boolean request_moderation
        varchar(100) state
        varchar(120) title
        bigint views
    }

    users ||--o{ events: user_id
    categories ||--o{ events: category_id
    locations ||--o{ events: location_id
```

## Stats DB structure

```mermaid
erDiagram
    test {
        bigint id PK
        varchar(100) app
        varchar(100) uri
        varchar(100) ip
        timestamp created_at
    }
```

Development agreements:

1) Mappers generated by MapStruct
2) Link ONE Service to Many Repositories
3) All Common contants located in stats-service/stats-common/src/main/java/ru/practicum/Contants.java
4) Stats DTO located in stats-service/stats-common/src/main/java/ru/practicum
5) Tests should be implemented JsonTest, ControllerTest, ServiceTest, IntegrationTest (On Queries)