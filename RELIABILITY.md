# Ringdu Reliability

이 문서는 Ringdu 백엔드의 안정성 기준을 정리한다. 현재 로컬 개발 환경과 운영 예정 환경을 구분한다.

## 현재 로컬 기준

- Spring Boot 3.3.5, Java 17
- PostgreSQL 16 Docker Compose
- 애플리케이션 기본 포트: `8081`
- DB 기본값: `ringdu` / `ringdu` / `ringdu1234`
- JPA `ddl-auto: update`
- Health Check: `GET /api/health`
- Redis 설정 값은 `application.yaml`에 있으나 Docker Compose 서비스와 실제 저장소 전환은 아직 예정

## 운영 예정 기준

운영 환경에서는 아래 구성을 고려한다.

- PostgreSQL: AWS RDS PostgreSQL 또는 동등한 managed PostgreSQL
- Redis: ElastiCache Redis 또는 동등한 managed Redis
- 파일 저장소: S3
- 실행 환경: EC2, ECS, 또는 컨테이너 기반 배포
- 설정: 환경변수와 secret manager 기반 관리
- 마이그레이션: 운영에서는 `ddl-auto: validate`와 Flyway 같은 migration 도구 사용

## 장애 대응 기준

- Health Check는 로드밸런서와 배포 검증에서 사용할 수 있어야 한다.
- 인증, 결제, 출석 같은 핵심 흐름은 오류 로그에 추적 가능한 request context를 남긴다.
- 클라이언트 응답에는 내부 경로, SQL, stack trace를 노출하지 않는다.
- 장애 발생 시 최근 배포, DB 연결, Redis 연결, JWT 설정, CORS/Cookie 설정을 우선 확인한다.

## Refresh Token 안정성

현재 코드에는 Refresh Token hash 저장과 rotation 흐름이 존재한다. 저장소는 JPA Entity 기반이며, Redis 기반 상태 저장소 전환은 예정이다.

Redis 전환 시 고려 사항:

- Refresh Token 원문은 저장하지 않고 hash만 저장한다.
- TTL은 Refresh Token 만료 시간과 일치시킨다.
- rotation 중 재사용이 감지되면 해당 토큰을 거부한다.
- Redis 장애 시 로그인 유지, 재발급 실패, 강제 재로그인 정책을 명확히 정한다.
- Redis 장애가 DB 장애로 전파되지 않도록 인증 실패 응답을 일관되게 처리한다.

## 백업과 복구

- PostgreSQL은 자동 백업과 point-in-time recovery를 고려한다.
- 운영 DB migration은 롤백 계획과 함께 적용한다.
- 학원, 학생, 출석, 청구 데이터는 서비스 핵심 데이터이므로 삭제 API는 soft delete 또는 감사 로그 정책을 검토한다.
