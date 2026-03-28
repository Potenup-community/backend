# potenup-community backend

`potenup-community/backend`는 커뮤니티 플랫폼의 단일 백엔드 애플리케이션입니다.  
인증/유저, 스터디, 게시글/댓글/리액션, 알림, 포인트, 상점, 세션, 프로젝트 갤러리 등 다수 도메인을 하나의 서비스로 제공합니다.

## 1. 기술 스택

- Kotlin, Spring Boot 3.5.x
- Java 21
- Spring Web, Validation, Data JPA, Security, Cache, Mail, AMQP, Actuator
- QueryDSL
- MySQL
- RabbitMQ (비동기 이벤트 처리)
- JWT 인증
- OpenAPI(springdoc)
- Micrometer + Prometheus + OTel bridge

## 2. 도메인 구성

코드베이스는 도메인 중심 패키지 구조를 사용합니다.

- 사용자/인증: `user`, `token`, `session`, `global.jwt`, `global.security`
- 커뮤니티: `study`, `study_schedule`, `post`, `comment`, `reaction`
- 운영/성장: `point`, `notification`, `dashboard`, `attendance`, `track`
- 콘텐츠: `image`, `gallery`, `shop`
- 공통: `common`, `global.config`, `exception`

각 도메인은 대체로 아래 레이어를 유지합니다.

- `presentation` (Controller/API)
- `application` (Service/UseCase)
- `domain` (Entity/VO/규칙)
- `infra` (Repository/외부 연동)

## 3. 보안/인증 설계

- Stateless JWT 인증 (`SecurityConfig`)
- 인증 예외와 인가 예외를 분리한 핸들러 운영
- 공개 엔드포인트와 보호 엔드포인트를 명시적으로 분리
- 로그인 시 Access/Refresh 토큰을 HttpOnly + Secure 쿠키로 발급
- 세션 컨텍스트(디바이스 ID, user-agent, IP) 수집 기반 세션 관리

## 4. 비동기/스케줄링

## 4.1 Outbox Publisher
- `OutboxPublisher`가 publish 후보 이벤트를 주기적으로 읽어 RabbitMQ 발행
- 성공 시 `published` 처리, 실패 시 사유 저장 후 재시도 대상 유지

## 4.2 주요 스케줄러
- 이미지 정리 스케줄러
- 트랙/스터디 일정 초기화 및 상태 전환 스케줄러
- 신규 게시글 알림 스케줄러

## 4.3 메시지 소비
- `ReviewCompletedListener`가 RabbitMQ queue를 소비하여 이력 후처리 수행

## 5. 대표 API 그룹

- 인증/유저
  - `/api/v1/auth/*`, `/api/v1/users/*`, `/api/v1/auth/sessions/*`
- 스터디
  - `/api/v1/studies/*`, `/api/v1/studies/schedules/*`
- 게시글/댓글/리액션
  - `/api/v1/posts/*`, `/api/v1/comments/*`, `/api/v1/reactions/*`
- 포인트/상점
  - `/api/v1/points/*`, `/api/v1/shop/*`, `/api/v1/inventory/*`
- 프로젝트/이미지
  - `/api/v1/projects/*`, `/api/v1/files/upload`
- 알림
  - `/api/v1/notifications/*`

Swagger
- `/swagger-ui.html`
- `/v3/api-docs`

## 6. 실행 방법

## 6.1 사전 준비
- JDK 21
- MySQL
- RabbitMQ
- `.env` 파일(로컬 환경 변수)

## 6.2 주요 환경변수(핵심)

- DB
  - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- JWT
  - `JWT_SECRET_TOKEN`, `ACCESS_TOKEN_EXPIRED`, `REFRESH_TOKEN_EXPIRED`, `REFRESH_TOKEN_HASH`
- Google OAuth
  - `GOOGLE_CLIENT_KEY`
- RabbitMQ
  - `RABBIT_HOST`, `RABBIT_PORT`, `RABBIT_USER`, `RABBIT_PASS`, `RABBIT_VHOST`
- 업로드/정적 파일
  - `LOCAL_UPLOAD_DIR`, `PUBLIC_BASE_PATH`, `BASE_URL`

## 6.3 로컬 실행

```bash
./gradlew clean bootRun
```

## 6.4 테스트

```bash
./gradlew test
```

## 7. 운영 관측

- Actuator: 별도 관리 포트 사용(`management.server.port`)
- Prometheus endpoint 노출
- HTTP 요청 메트릭 히스토그램 수집
- 로그는 운영 목적에 맞게 SQL/바인딩 로그 레벨 조정 가능

## 8. CI

`.github/workflows/backend-ci.yml`

- 트리거: `develop`, `main` push/PR
- 단계: Gradle test 실행
- 테스트 결과를 PR 코멘트/체크로 게시

## 9. 면접 관점에서 강조할 점

- 대규모 도메인을 단일 서비스에서 모듈 경계로 관리한 구조
- JWT + 세션 추적을 병행해 보안과 운영 가시성을 동시에 확보한 점
- Outbox + RabbitMQ 조합으로 트랜잭션과 비동기 발행을 분리한 점
- 스케줄러/이벤트 리스너 기반의 운영 자동화(알림/정리/상태전이)
