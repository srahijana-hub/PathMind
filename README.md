# Study Agent

AI study planning assistant.

This project is intentionally independent from the larger interview project in the parent directory. It can be moved to another folder or initialized as its own Git repository later.

## Current Stage

- Spring Boot backend skeleton
- Study plan creation API
- Local demo plan generation

## Run

Requires JDK 17 and Maven.

```bash
mvn spring-boot:run
```

## API

```http
POST /api/study/plans
```

Example request:

```json
{
  "subject": "Data Structures",
  "examDate": "2026-06-20",
  "currentLevel": "Basic understanding, weak at trees and graphs",
  "dailyMinutes": 90,
  "targetScore": 85
}
```
