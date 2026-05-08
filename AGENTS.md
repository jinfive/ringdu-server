
# Ringdu Backend 개발 지침

## 1. 프로젝트 개요

Ringdu는 “Linking Edu”의 의미를 가진 학원 관리 웹/앱 서비스이다.

초기 목표는 소규모 학원이 학생, 시간표, 출석, 숙제, 청구서, 납부 상태를 웹에서 안정적으로 관리할 수 있는 실사용 가능한 학원 관리 시스템을 만드는 것이다.

현재 프로젝트는 Ringdu의 Spring Boot 백엔드 서버이다.

## 2. 기술 스택

- Java 17
- Spring Boot 3.x
- Gradle
- PostgreSQL
- Spring Data JPA
- Spring Security
- Validation
- Lombok
- Docker
- 추후 프론트엔드는 Next.js 기반으로 연동 예정
- 추후 모바일 앱은 iOS/Android 모두 지원할 수 있도록 확장 예정

## 3. 버전 기준

- Java 17 기준으로 개발한다.
- Spring Boot는 3.x 안정 버전을 사용한다.
- Spring Boot 4.x는 사용하지 않는다.
- 의존성 추가 시 Java 17 및 Spring Boot 3.x와 호환되는지 확인한다.

## 4. 기본 패키지 기준

기본 패키지는 반드시 아래로 통일한다.

```text
com.ringdu.server
````

* `com.ringdu.ringduserver` 패키지는 사용하지 않는다.
* `RingduServerApplication` 클래스는 `com.ringdu.server` 패키지 아래에 둔다.
* Controller, Service, Repository, Entity, DTO 등 모든 도메인 패키지는 `com.ringdu.server` 하위에 둔다.

## 5. GitHub 저장소

원격 저장소는 아래를 사용한다.

```text
https://github.com/jinfive/ringdu-server
```

브랜치 전략:

* `main`: 배포 기준 브랜치
* `develop`: 개발 통합 브랜치
* `feat/*`: 기능 개발 브랜치
* `fix/*`: 버그 수정 브랜치
* `refactor/*`: 리팩토링 브랜치
* `docs/*`: 문서 작업 브랜치

작업 규칙:

* `main`에는 직접 커밋하지 않는다.
* 모든 작업은 먼저 GitHub Issue를 생성하고, 해당 이슈 범위의 브랜치에서 진행한다.
* 기능 작업은 `develop`에서 기능 브랜치를 생성해 진행한다.
* PR 대상은 기본적으로 `develop`이다.
* 작업 완료 시 기능 브랜치를 원격에 push하고 PR을 생성한다.
* PR이 merge되면 해당 기능 브랜치는 로컬과 원격에서 삭제한다.
* 커밋 메시지는 Conventional Commit 스타일을 사용한다.

커밋 메시지 예시:

```text
feat: add health check api
feat: add common api response
feat: add user signup api
feat: add attendance management
fix: resolve security config issue
refactor: align package structure
docs: update agents guidelines
test: add user service test
```

## 6. 로컬 PostgreSQL 개발 환경

로컬 개발 DB는 Docker Compose 기반 PostgreSQL을 사용한다.

DB 설정:

```text
DB 이름: ringdu
DB 사용자: ringdu
DB 비밀번호: ringdu1234
포트: 5432
```

주의사항:

* `application.yaml`과 `docker-compose.yml`의 DB 접속 정보는 일치해야 한다.
* PostgreSQL 볼륨이 기존 설정으로 꼬인 경우 `docker compose down -v` 후 다시 생성할 수 있다.
* 운영 환경에서는 DB 비밀번호와 환경변수를 별도로 관리한다.

## 7. 권한 구조

Ringdu의 권한은 아래 6개를 기준으로 설계한다.

```text
ADMIN
OWNER
DESK
TEACHER
PARENT
STUDENT
```

### 7-1. ADMIN

Ringdu 플랫폼 관리자 계정이다.

학원 내부 운영자가 아니라, Ringdu 서비스 전체를 관리하는 운영자 권한이다.

주요 기능:

* 전체 학원 목록 조회
* 학원 계정 승인/비활성화
* 원장 계정 관리
* 서비스 공지 관리
* 커뮤니티/홍보 게시글 관리
* 신고 게시글 관리
* 전체 서비스 운영 현황 조회
* 장애 대응 및 운영 관리
* 추후 요금제/결제/광고 관리

초기 MVP에서는 ADMIN 기능을 최소화한다.

초기 ADMIN MVP 범위:

* 관리자 계정 구분
* 전체 학원 목록 조회
* 학원 상태 관리
* 서비스 운영용 기본 권한 구조 확보

### 7-2. OWNER

학원 원장 계정이다.

학원 전체 운영 권한을 가진다.

주요 기능:

* 학원 정보 관리
* 데스크 직원 등록/관리
* 선생님 등록/관리
* 학생 등록/관리
* 학부모 연결 관리
* 반 생성/수정/삭제
* 시간표 생성/수정/삭제
* 출석 현황 조회/수정
* 숙제 현황 조회
* 청구서 생성/수정/삭제
* 납부 상태 관리
* 공지사항 작성/수정/삭제
* 전체 운영 대시보드 조회

### 7-3. DESK

학원 데스크, 실장, 상담/수납 담당 계정이다.

원장을 보조하여 학원 운영 업무를 처리한다.

주요 기능:

* 학생 등록/수정
* 학부모 정보 등록/수정
* 학생-학부모 연결
* 반 배정 관리
* 시간표 조회 및 일부 수정
* 출석 상태 입력 및 수정
* 결석 사유 메모
* 청구서 생성 보조
* 납부 상태 변경
* 미납자 조회
* 공지사항 작성 보조
* 학부모 문의 대응을 위한 출석/납부 이력 조회

제한:

* 학원 삭제 불가
* 원장 권한 변경 불가
* 전체 시스템 설정 변경 불가

### 7-4. TEACHER

선생님 계정이다.

본인이 담당하는 반과 학생을 중심으로 기능을 사용한다.

주요 기능:

* 담당 반 조회
* 담당 학생 조회
* 본인 시간표 조회
* 담당 반 출석 체크
* 담당 학생 출석 상태 변경
* 지각, 결석, 조퇴 사유 입력
* 숙제 등록
* 숙제 수정
* 숙제 제출 여부 확인
* 학생별 수업 메모 작성
* 공지사항 확인
* 담당 반 공지 작성

제한:

* 청구서 생성 불가
* 납부 상태 변경 불가
* 학원 운영 설정 변경 불가

### 7-5. PARENT

학부모 계정이다.

연결된 자녀의 정보만 조회할 수 있다.

주요 기능:

* 연결된 자녀 목록 조회
* 자녀 시간표 조회
* 자녀 출석 상태 조회
* 자녀 출석 이력 조회
* 지각, 결석, 조퇴 여부 확인
* 결석 사유 확인
* 자녀 숙제 조회
* 자녀 숙제 제출 상태 확인
* 청구서 조회
* 납부 상태 조회
* 공지사항 조회
* 학원 문의 작성

제한:

* 다른 학생 정보 조회 불가
* 다른 학부모 정보 조회 불가
* 출석/숙제/납부 상태 직접 수정 불가

### 7-6. STUDENT

학생 계정이다.

본인의 학습 정보만 조회하고 제출할 수 있다.

주요 기능:

* 본인 시간표 조회
* 본인 숙제 조회
* 숙제 상세 내용 확인
* 숙제 제출
* 본인 숙제 제출 상태 확인
* 본인 출석 상태 조회
* 본인 출석 이력 확인
* 공지사항 조회

제한:

* 청구서와 납부 정보는 조회하지 않는다.
* 다른 학생의 시간표, 숙제, 출석 정보는 조회할 수 없다.

## 8. 초기 MVP 핵심 기능

초기 MVP에서는 아래 기능을 우선 구현한다.

* Health Check API
* 공통 응답 구조
* 공통 예외 처리
* 사용자 인증/인가
* 관리자 계정 구분
* 학원 계정 관리
* 원장 계정 관리
* 데스크 계정 관리
* 선생님 계정 관리
* 학부모 계정 관리
* 학생 계정 관리
* 반 관리
* 시간표 관리
* 학생 본인 시간표 조회
* 학부모 자녀 시간표 조회
* 출석 관리
* 학생 본인 출석 이력 조회
* 학부모 자녀 출석 확인
* 숙제 관리
* 학생 본인 숙제 조회
* 학부모 자녀 숙제 확인
* 청구서 관리
* 납부 상태 관리
* 공지사항 관리

## 9. 개발 우선순위

현재 개발 순서는 아래를 기준으로 한다.

1. Health Check API
2. 공통 응답 구조
3. 공통 예외 처리
4. BaseEntity
5. Role Enum
6. User Entity
7. 회원가입 API
8. 로그인 API
9. Spring Security/JWT
10. Academy Entity
11. 관리자 계정 기본 구조
12. 원장-학원 연결
13. 데스크 계정 등록
14. 선생님 계정 등록
15. 학생/학부모 등록
16. 반 관리
17. 시간표 관리
18. 출석 관리
19. 숙제 관리
20. 청구서/납부 관리
21. 공지사항 관리
22. 커뮤니티/홍보 기능

## 10. 패키지 구조

패키지 구조는 아래 기준을 따른다.

```text
com.ringdu.server
├── global
│   ├── config
│   ├── security
│   ├── exception
│   └── common
├── auth
│   ├── controller
│   ├── service
│   ├── dto
│   └── entity
├── user
│   ├── controller
│   ├── service
│   ├── repository
│   ├── dto
│   └── entity
├── academy
│   ├── controller
│   ├── service
│   ├── repository
│   ├── dto
│   └── entity
├── admin
│   ├── controller
│   ├── service
│   ├── repository
│   ├── dto
│   └── entity
├── desk
├── teacher
├── parent
├── student
├── classroom
├── schedule
├── attendance
│   ├── controller
│   ├── service
│   ├── repository
│   ├── dto
│   └── entity
├── homework
├── invoice
├── payment
├── notice
└── community
```

## 11. 코드 작성 규칙

* Controller, Service, Repository, DTO, Entity 구조를 따른다.
* Entity를 Controller 응답으로 직접 반환하지 않는다.
* 요청 DTO와 응답 DTO를 분리한다.
* Service 계층에 비즈니스 로직을 둔다.
* Controller는 요청 검증과 응답 반환에 집중한다.
* Repository는 데이터 접근만 담당한다.
* Lombok 사용은 가능하지만 과도하게 사용하지 않는다.
* Setter 사용은 지양한다.
* 생성자, 정적 팩토리 메서드, 비즈니스 메서드를 우선 사용한다.
* 모든 API는 RESTful하게 설계한다.
* API 경로는 `/api`로 시작한다.
* URL은 복수형 리소스명을 사용한다.

## 12. 공통 응답 규칙

모든 API 응답은 가능한 한 `ApiResponse` 형식을 따른다.

성공 응답 예시:

```json
{
  "success": true,
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

실패 응답 예시:

```json
{
  "success": false,
  "message": "잘못된 요청입니다.",
  "data": null
}
```

## 13. 예외 처리 규칙

* 전역 예외 처리 `GlobalExceptionHandler`를 사용한다.
* 커스텀 예외를 도메인별로 관리한다.
* 클라이언트에게 일관된 에러 응답을 반환한다.
* 예외 메시지는 명확하게 작성한다.
* 내부 구현 정보, DB 구조, 서버 경로 등은 에러 응답에 노출하지 않는다.

## 14. 인증/인가 규칙

* Spring Security를 사용한다.
* 추후 JWT 기반 인증을 적용할 수 있도록 구조를 잡는다.
* 비밀번호는 반드시 BCrypt로 암호화한다.
* 권한은 `ADMIN`, `OWNER`, `DESK`, `TEACHER`, `PARENT`, `STUDENT` 기준으로 관리한다.
* `/api/health`는 인증 없이 접근 가능해야 한다.
* ADMIN은 Ringdu 플랫폼 전체 관리 권한을 가진다.
* OWNER는 학원 전체 운영 권한을 가진다.
* DESK는 학원 운영 보조 권한을 가진다.
* TEACHER는 담당 반과 담당 학생 중심으로 접근한다.
* PARENT는 연결된 자녀 정보만 접근한다.
* STUDENT는 본인 정보만 접근한다.
* 권한 검증 없이 다른 학원, 다른 학생, 다른 학부모 데이터에 접근할 수 없도록 한다.

## 15. 권한 검증 규칙

* ADMIN은 Ringdu 플랫폼 운영 범위에서 전체 학원 정보를 관리할 수 있다.
* OWNER와 DESK는 소속 학원 내 학생 리소스를 조회/관리할 수 있다.
* TEACHER는 담당 반 또는 담당 학생의 리소스만 조회/관리할 수 있다.
* PARENT는 연결된 자녀 `studentId`에 해당하는 리소스만 조회할 수 있다.
* STUDENT는 본인 `studentId`에 해당하는 리소스만 조회할 수 있다.
* 다른 학원의 학생, 시간표, 출석, 숙제, 청구서 정보에 접근할 수 없도록 한다.

## 16. DB/JPA 규칙

* PostgreSQL 기준으로 설계한다.
* JPA Entity에는 기본 생성자를 `protected`로 둔다.
* 연관관계는 필요한 경우에만 설정한다.
* 무분별한 양방향 연관관계는 피한다.
* N+1 문제가 생기지 않도록 주의한다.
* `createdAt`, `updatedAt` 같은 공통 시간 필드는 `BaseEntity`로 관리한다.
* 실제 운영을 고려하여 `nullable`, `unique`, `length` 제약을 신중히 둔다.
* 초기 개발 단계에서는 `ddl-auto: update`를 사용할 수 있다.
* 운영 단계에서는 `validate` 또는 Flyway 같은 마이그레이션 도구를 사용한다.

## 17. 도메인 설계 기본 방향

* User는 로그인 가능한 계정을 의미한다.
* Role은 User의 권한을 의미한다.
* Academy는 학원을 의미한다.
* ADMIN은 Ringdu 플랫폼 운영자이다.
* OWNER, DESK, TEACHER는 특정 Academy에 소속된다.
* PARENT는 학생과 연결된다.
* STUDENT는 학원과 반에 소속된다.
* 한 학부모는 여러 자녀와 연결될 수 있다.
* 한 학생은 여러 반에 소속될 수 있다.
* 한 선생님은 여러 반을 담당할 수 있다.
* 청구서와 납부 정보는 학생 기준으로 관리한다.
* 납부 정보는 주로 OWNER와 DESK가 관리한다.
* 출석 정보는 학부모와 학생 모두 조회 가능하다.
* 학생은 본인의 시간표, 출석, 숙제를 조회할 수 있다.

## 18. 출석 관리 기준

출석은 학생과 수업/반 기준으로 기록한다.

출석 상태는 아래 Enum 기준으로 설계한다.

```text
AttendanceStatus
- PRESENT: 출석
- ABSENT: 결석
- LATE: 지각
- EARLY_LEAVE: 조퇴
- EXCUSED: 인정 결석
```

출석 도메인 설계 방향:

* 출석은 학생과 수업/반 기준으로 기록한다.
* 출석 날짜를 반드시 저장한다.
* 누가 출석을 체크했는지 기록한다.
* 출석 상태 변경 시 변경자와 변경 시간을 기록할 수 있도록 고려한다.
* 학부모는 본인 자녀의 출석 정보만 조회할 수 있다.
* 학생은 본인의 출석 정보만 조회할 수 있다.
* 선생님은 담당 반 학생의 출석만 관리할 수 있다.
* 데스크와 원장은 학원 전체 출석 현황을 조회할 수 있다.
* 출석 정보는 청구서/납부 정보와 달리 학부모와 학생 모두 조회 가능하다.

초기 MVP 출석 기능:

* 선생님이 담당 반 학생 출석 체크
* 데스크가 출석 상태 수정
* 원장이 전체 출석 현황 확인
* 학부모가 자녀 출석 이력 확인
* 학생이 본인 출석 이력 확인

초기 MVP에서 제외:

* 자동 출결 기기 연동
* QR 출석
* NFC 출석
* 출석 알림톡 자동 발송
* 출석 통계 고도화

## 19. 시간표 관리 기준

시간표 권한 기준:

* ADMIN은 플랫폼 운영 관점에서 학원별 시간표 데이터 상태를 확인할 수 있다.
* OWNER는 전체 시간표를 관리할 수 있다.
* DESK는 전체 시간표를 조회하고 일부 수정할 수 있다.
* TEACHER는 본인 담당 반 시간표를 조회할 수 있다.
* PARENT는 연결된 자녀의 시간표를 조회할 수 있다.
* STUDENT는 본인 시간표만 조회할 수 있다.

예상 API:

```text
GET /api/students/{studentId}/schedules
- 학생 시간표 조회
- PARENT는 연결된 자녀만 조회 가능
- STUDENT는 본인 것만 조회 가능
```

## 20. 숙제 관리 기준

숙제 권한 기준:

* OWNER는 전체 숙제 현황을 조회할 수 있다.
* TEACHER는 담당 반 숙제를 등록, 수정, 조회할 수 있다.
* PARENT는 연결된 자녀의 숙제와 제출 상태를 조회할 수 있다.
* STUDENT는 본인에게 배정된 숙제를 조회하고 제출할 수 있다.

예상 API:

```text
GET /api/students/{studentId}/homeworks
- 학생 숙제 목록 조회
- PARENT는 연결된 자녀만 조회 가능
- STUDENT는 본인 것만 조회 가능

GET /api/homeworks/{homeworkId}
- 숙제 상세 조회

POST /api/homeworks/{homeworkId}/submissions
- 학생 숙제 제출
```

## 21. 출석 관련 예상 API

```text
POST /api/classes/{classId}/attendances
- 반별 출석 체크

GET /api/classes/{classId}/attendances
- 반별 출석 현황 조회

GET /api/students/{studentId}/attendances
- 학생별 출석 이력 조회
- PARENT는 연결된 자녀만 조회 가능
- STUDENT는 본인 것만 조회 가능

PATCH /api/attendances/{attendanceId}
- 출석 상태 수정
```

## 22. 초기 MVP에서 제외할 기능

초기 MVP에서는 아래 기능을 뒤로 미룬다.

* 실제 PG 결제 연동
* 카카오 알림톡
* 푸시 알림
* 실시간 채팅
* AI 기능
* 복잡한 통계
* 모바일 앱
* 광고 결제 시스템
* Spring Boot 4.x 전환
* 복잡한 멀티테넌트 과금 구조
* 고도화된 플랫폼 관리자 기능
* QR 출석
* NFC 출석
* 자동 출결 기기 연동

## 23. 테스트 규칙

* 핵심 Service 로직은 테스트 코드를 작성한다.
* Controller는 필요한 경우 MockMvc 테스트를 작성한다.
* 테스트 메서드명은 의도가 드러나게 작성한다.
* 인증, 권한, 상태 변경, 예외 케이스를 우선 테스트한다.
* 단순 getter/setter 테스트는 작성하지 않는다.

## 24. AI 개발 에이전트 작업 규칙

* AGENTS.md의 내용을 먼저 확인하고 작업한다.
* 한 번에 너무 큰 기능을 만들지 않는다.
* 기능 단위로 작게 나눠 작업한다.
* 작업 전 현재 파일 구조와 기존 코드를 먼저 확인한다.
* 기존 코드 스타일을 유지한다.
* 불필요한 의존성을 추가하지 않는다.
* 임의로 패키지명이나 프로젝트명을 변경하지 않는다.
* 보안 관련 코드는 신중하게 작성한다.
* Entity를 직접 응답으로 노출하지 않는다.
* API 변경이 있으면 README 또는 문서에 반영한다.
* 작업 후 가능하면 `./gradlew test` 또는 `./gradlew bootRun` 가능 여부를 확인한다.
* 변경 사항 요약과 확인 방법을 함께 남긴다.

## 25. 최종 목표

Ringdu의 최종 목표는 다음과 같다.

```text
소규모 학원이 학생, 시간표, 출석, 숙제, 청구서, 납부 상태를 웹에서 안정적으로 관리할 수 있는 실사용 가능한 학원 관리 시스템을 만든다.
```

````

이제 핵심 구조는 이렇게 확정하면 돼.

```
ADMIN   = 링듀 플랫폼 관리자
OWNER   = 학원 원장
DESK    = 학원 데스크/실장
TEACHER = 선생님
PARENT  = 학부모
STUDENT = 학생
````

