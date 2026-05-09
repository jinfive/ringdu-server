
# Architecture

## 1. 목적

이 문서는 Ringdu 프로젝트의 전체 구조와 주요 아키텍처 원칙을 설명한다.

Ringdu는 학원, 선생님, 학부모, 학생을 연결하는 학원 관리 웹/앱 서비스다.  
초기에는 웹 MVP를 먼저 개발하지만, 이후 실제 배포와 운영, 모바일 앱 확장까지 고려한다.

에이전트와 개발자는 코드를 수정하기 전에 이 문서를 참고하여 다음 내용을 이해해야 한다.

- 프로젝트의 기본 구조
- 주요 계층의 역할
- 요청과 응답의 흐름
- 도메인 간 책임 분리 기준
- 새 파일을 추가할 위치
- 인증과 권한 처리 기준
- 운영을 고려한 설계 원칙

---

## 2. 기본 프로젝트 구조

Ringdu 백엔드는 Spring Boot 기반으로 구성한다.

기본 패키지는 다음을 사용한다.

```txt
com.ringdu.server
````

주의사항:

* 기본 패키지는 `com.ringdu.server`로 유지한다.
* `com.ringdu.ringduserver` 패키지는 사용하지 않는다.
* Spring Boot 4.x는 사용하지 않는다.
* 현재 프로젝트는 Spring Boot 3.3.5 기준으로 유지한다.
* 프론트엔드는 별도 레포지토리에서 관리한다.

---

## 3. 전체 설계 원칙

Ringdu는 역할과 책임을 기준으로 코드를 분리한다.

기본 흐름은 다음과 같다.

```txt
Client
→ Controller
→ Service
→ Repository
→ Database
```

각 계층의 책임은 명확히 분리한다.

핵심 원칙:

* Controller는 요청과 응답을 담당한다.
* Service는 비즈니스 흐름과 정책을 담당한다.
* Repository는 데이터 접근만 담당한다.
* Entity는 데이터베이스와 연결되는 도메인 모델이다.
* DTO는 외부 요청과 응답을 표현한다.
* 공통 예외, 응답, 설정은 global 영역에서 관리한다.

---

## 4. 권장 패키지 구조

Ringdu 백엔드는 도메인 중심으로 패키지를 나눈다.

예상 구조는 다음과 같다.

```txt
com.ringdu.server
├── auth
│   ├── controller
│   ├── dto
│   ├── service
│   └── repository
├── user
│   ├── entity
│   ├── repository
│   └── dto
├── academy
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
├── student
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
├── attendance
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
├── homework
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
├── billing
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
└── global
    ├── config
    ├── exception
    ├── response
    ├── security
    └── common
```

초기에는 모든 도메인 패키지가 존재하지 않아도 된다.
기능이 추가될 때 해당 도메인의 책임에 맞게 패키지를 생성한다.

---

## 5. Controller 계층

Controller는 외부 HTTP 요청을 받는 계층이다.

역할:

* HTTP 요청을 받는다.
* Request DTO를 검증한다.
* Service를 호출한다.
* Response DTO를 반환한다.
* 인증된 사용자 정보가 필요한 경우 Service에 전달한다.

하지 말아야 할 것:

* 비즈니스 로직을 직접 처리하지 않는다.
* Repository를 직접 호출하지 않는다.
* Entity를 그대로 반환하지 않는다.
* 복잡한 조건 판단을 Controller에 넣지 않는다.
* 권한 정책을 Controller에 몰아넣지 않는다.

예시:

```txt
AuthController
AcademyController
StudentController
AttendanceController
HomeworkController
BillingController
```

---

## 6. Service 계층

Service는 핵심 비즈니스 로직을 처리하는 계층이다.

역할:

* 기능의 주요 흐름을 담당한다.
* 도메인 규칙을 적용한다.
* 필요한 데이터를 조회하거나 저장한다.
* 트랜잭션이 필요한 작업을 관리한다.
* 권한과 소유권 검사가 필요한 경우 이를 수행한다.
* 여러 Repository를 조합해 하나의 기능 흐름을 완성한다.

하지 말아야 할 것:

* HTTP 요청/응답 객체에 직접 의존하지 않는다.
* 화면 표시 방식에 의존하지 않는다.
* 한 Service에 너무 많은 책임을 몰아넣지 않는다.
* 단순 데이터 조회를 넘어선 정책을 Repository에 넘기지 않는다.

예시:

```txt
AuthService
AcademyService
StudentService
AttendanceService
HomeworkService
BillingService
```

---

## 7. Repository 계층

Repository는 데이터 저장소와 통신하는 계층이다.

역할:

* 데이터 조회, 저장, 수정, 삭제를 담당한다.
* Spring Data JPA를 통해 Entity에 접근한다.
* 필요한 조건의 데이터를 조회한다.

하지 말아야 할 것:

* 복잡한 비즈니스 판단을 하지 않는다.
* 권한 정책을 직접 결정하지 않는다.
* API 응답 형식을 만들지 않는다.
* DTO 변환 책임을 갖지 않는다.

예시:

```txt
UserRepository
AcademyRepository
AcademyMemberRepository
StudentRepository
StudentGuardianRepository
AcademyStudentRepository
```

---

## 8. Entity / Domain 계층

Entity는 데이터베이스 테이블과 연결되는 핵심 도메인 객체다.

역할:

* 핵심 데이터 구조를 표현한다.
* 데이터베이스 테이블과 매핑된다.
* 필요한 경우 도메인 규칙을 메서드로 가질 수 있다.

주의할 점:

* API 응답으로 직접 노출하지 않는다.
* 외부 요청 형식에 직접 의존하지 않는다.
* 비밀번호, 토큰, 내부 상태값 등 민감한 필드가 외부로 나가지 않도록 한다.
* Entity에 화면 표시용 필드를 억지로 추가하지 않는다.

---

## 9. DTO 계층

DTO는 외부 입출력 데이터를 표현하는 객체다.

역할:

* API 요청 데이터를 표현한다.
* API 응답 데이터를 표현한다.
* Entity와 외부 응답 형식을 분리한다.
* 필요한 필드만 외부로 전달한다.

원칙:

* 요청 DTO와 응답 DTO를 분리한다.
* 민감정보를 응답 DTO에 포함하지 않는다.
* Entity를 그대로 반환하지 않는다.
* 프론트 화면 구조에 지나치게 종속되지 않는다.

예시:

```txt
SignupRequest
SignupResponse
LoginRequest
LoginResponse
MeResponse
```

---

## 10. Global 영역

`global` 패키지는 여러 도메인에서 공통으로 사용하는 코드를 둔다.

예상 구조:

```txt
global
├── config
├── exception
├── response
├── security
└── common
```

예시:

```txt
ApiResponse
ErrorCode
BusinessException
GlobalExceptionHandler
BaseEntity
SecurityConfig
JwtTokenProvider
JwtAuthenticationFilter
```

주의할 점:

* 무분별하게 `global`에 넣지 않는다.
* 특정 도메인에서만 쓰는 코드는 해당 도메인 안에 둔다.
* 여러 도메인에서 공통으로 사용하는 코드만 `global`에 둔다.

---

## 11. 도메인 분리 원칙

Ringdu는 User에 모든 정보를 몰아넣지 않는다.

User는 로그인 계정 정보만 담당한다.

잘못된 방향:

```txt
users
├── academy_name
├── academy_address
├── academy_phone
└── academy_code
```

올바른 방향:

```txt
users
→ 로그인 계정 정보

academies
→ 학원 정보

academy_members
→ 학원과 OWNER / DESK / TEACHER의 소속 관계

students
→ 학생 프로필

student_guardians
→ 학부모와 학생의 관계

academy_students
→ 학원과 학생의 소속 관계
```

핵심 원칙:

* User에 학원 정보를 넣지 않는다.
* 학원 정보는 Academy로 분리한다.
* 학원 소속 관계는 AcademyMember로 분리한다.
* 학생 프로필은 Student로 분리한다.
* 학부모와 학생 관계는 StudentGuardian으로 분리한다.
* 학원과 학생 관계는 AcademyStudent로 분리한다.

---

## 12. 주요 도메인 책임

### User

로그인 계정 정보를 담당한다.

담당 정보:

```txt
email
password
name
phone
role
status
provider
providerId
```

User는 계정의 식별과 인증에 필요한 정보만 가진다.

---

### Academy

학원 정보를 담당한다.

담당 정보:

```txt
name
phone
address
academyCode
status
```

Academy는 학원 자체의 정보를 표현한다.

---

### AcademyMember

학원과 운영 인력의 소속 관계를 담당한다.

대상 권한:

```txt
OWNER
DESK
TEACHER
```

AcademyMember는 사용자가 어떤 학원에 어떤 역할로 소속되어 있는지 표현한다.

---

### Student

학생 프로필을 담당한다.

Student는 학생의 개인 프로필 정보를 담당한다.
학생이 직접 로그인하는 경우 User와 연결될 수 있다.

---

### StudentGuardian

학부모와 학생의 관계를 담당한다.

예시:

```txt
부
모
보호자
기타
```

학부모가 자녀의 출석, 시간표, 숙제, 청구 정보를 확인할 때 이 관계를 기준으로 권한을 판단한다.

---

### AcademyStudent

학원과 학생의 소속 관계를 담당한다.

학생이 어느 학원에 등록되어 있는지 표현한다.

---

## 13. 권한 구조

Ringdu의 권한은 다음 6개다.

```txt
ADMIN
OWNER
DESK
TEACHER
PARENT
STUDENT
```

### ADMIN

Ringdu 플랫폼 관리자다.

역할:

* 전체 서비스 운영
* 학원 관리
* OWNER 계정 생성 또는 부여
* 운영 정책 관리

일반 회원가입으로 생성하지 않는다.

---

### OWNER

학원 원장이다.

역할:

* 학원 전체 운영
* 선생님 관리
* 데스크 직원 관리
* 학생 관리
* 학원 설정 관리

일반 회원가입으로 생성하지 않는다.
ADMIN이 생성하거나 부여한다.

---

### DESK

학원 데스크, 실장, 상담, 수납 담당자다.

역할:

* 상담 관리
* 수납 관리
* 학생 관리 보조
* 출결 및 학원 운영 업무 보조

일반 회원가입으로 생성하지 않는다.
OWNER가 등록하거나 초대하는 방식으로 생성한다.

---

### TEACHER

선생님이다.

역할:

* 수업 관리
* 출석 관리
* 숙제 관리
* 학생 관리

일반 회원가입이 가능하다.
가입 후 학원 연결 또는 승인 과정을 통해 학원에 소속된다.

---

### PARENT

학부모다.

역할:

* 자녀 시간표 확인
* 자녀 출석 확인
* 자녀 숙제 확인
* 청구서 및 납부 상태 확인

일반 회원가입이 가능하다.
자녀 연결 후 관련 정보를 조회할 수 있다.

---

### STUDENT

학생이다.

역할:

* 본인 시간표 조회
* 본인 출석 조회
* 본인 숙제 조회
* 숙제 제출

일반 회원가입이 가능하다.

---

## 14. 회원가입 권한 원칙

일반 회원가입 가능 권한:

```txt
TEACHER
PARENT
STUDENT
```

일반 회원가입 차단 권한:

```txt
ADMIN
OWNER
DESK
```

원칙:

* 회원가입 화면에 권한 드롭다운을 두지 않는다.
* `/signup/teacher`는 `TEACHER`로 가입한다.
* `/signup/parent`는 `PARENT`로 가입한다.
* `/signup/student`는 `STUDENT`로 가입한다.
* `ADMIN`, `OWNER`, `DESK`는 회원가입 화면에 노출하지 않는다.
* 서버에서도 `ADMIN`, `OWNER`, `DESK` 일반 회원가입을 차단한다.

프론트에서 버튼을 숨기는 것만으로는 보안이 아니다.
반드시 백엔드에서도 권한 검증을 수행한다.

---

## 15. 인증 구조 원칙

Ringdu는 실제 운영을 고려해 JWT 기반 인증 구조를 사용한다.

기본 방향:

```txt
Access Token
→ JWT
→ 응답 body로 반환
→ 프론트 메모리에 저장

Refresh Token
→ HttpOnly Cookie
→ Redis에 tokenHash 저장
→ 원문 저장 금지
→ Rotation 적용
```

원칙:

* Access Token은 DB에 저장하지 않는다.
* Refresh Token 원문은 DB나 Redis에 저장하지 않는다.
* Redis에는 Refresh Token의 해시값만 저장한다.
* Refresh Token은 HttpOnly Cookie로 관리한다.
* 운영 환경에서는 Cookie에 `Secure=true`를 적용한다.
* JWT secret은 코드에 하드코딩하지 않는다.
* 인증 실패와 권한 실패는 명확히 구분한다.

---

## 16. API 응답 구조

Ringdu 백엔드는 공통 응답 형식을 사용한다.

기본 방향:

```txt
성공 응답
→ ApiResponse<T>

실패 응답
→ ErrorCode 기반 예외 응답
```

원칙:

* Controller는 Entity를 직접 반환하지 않는다.
* 응답은 DTO로 변환해서 반환한다.
* 예외는 `BusinessException`과 `ErrorCode`를 중심으로 관리한다.
* 전역 예외 처리는 `GlobalExceptionHandler`에서 담당한다.
* 클라이언트가 이해할 수 있는 일관된 응답 형식을 유지한다.

---

## 17. 예외 처리 구조

예외 처리 흐름은 다음과 같다.

```txt
Service
→ BusinessException 발생
→ GlobalExceptionHandler
→ ErrorCode 기반 응답
→ Client
```

원칙:

* 기능별 예외 메시지를 Controller에서 직접 만들지 않는다.
* 공통 에러 코드는 `ErrorCode`에서 관리한다.
* 비즈니스 예외는 `BusinessException`을 사용한다.
* 예상 가능한 예외는 명확한 ErrorCode로 관리한다.
* 예외 응답에 민감한 내부 정보를 포함하지 않는다.

---

## 18. 트랜잭션 원칙

트랜잭션은 Service 계층에서 관리한다.

원칙:

* 조회 전용 메서드는 `@Transactional(readOnly = true)`를 사용한다.
* 생성, 수정, 삭제 메서드는 `@Transactional`을 사용한다.
* Controller에는 트랜잭션을 걸지 않는다.
* Repository에는 특별한 이유 없이 트랜잭션을 직접 걸지 않는다.
* 하나의 비즈니스 흐름이 여러 저장 작업을 포함하면 Service에서 하나의 트랜잭션으로 묶는다.

예시:

```txt
회원가입
→ 사용자 저장이 필요하므로 @Transactional 사용

내 정보 조회
→ 조회만 하므로 @Transactional(readOnly = true) 사용
```

---

## 19. 보안 원칙

Ringdu는 실제 운영을 고려하므로 보안을 기본 설계에 포함한다.

원칙:

* 비밀번호는 BCrypt로 암호화한다.
* Entity를 직접 응답하지 않는다.
* Refresh Token 원문을 저장하지 않는다.
* JWT secret을 코드에 하드코딩하지 않는다.
* 운영 환경에서는 HTTPS를 사용한다.
* 운영 환경에서는 Refresh Token Cookie에 `Secure=true`를 적용한다.
* 권한 검증은 반드시 서버에서 수행한다.
* 프론트 화면에서 버튼을 숨기는 것은 보안이 아니다.
* 민감정보는 로그에 남기지 않는다.
* 운영 시크릿은 환경변수로 관리한다.

---

## 20. 파일 추가 기준

새 파일을 추가할 때는 책임에 맞는 위치에 둔다.

### 인증 관련

```txt
auth/controller
auth/dto
auth/service
global/security
```

### 사용자 관련

```txt
user/entity
user/repository
user/dto
```

### 학원 관련

```txt
academy/controller
academy/dto
academy/entity
academy/repository
academy/service
```

### 학생 관련

```txt
student/controller
student/dto
student/entity
student/repository
student/service
```

### 출석 관련

```txt
attendance/controller
attendance/dto
attendance/entity
attendance/repository
attendance/service
```

### 숙제 관련

```txt
homework/controller
homework/dto
homework/entity
homework/repository
homework/service
```

### 청구 / 납부 관련

```txt
billing/controller
billing/dto
billing/entity
billing/repository
billing/service
```

### 공통 예외 / 응답

```txt
global/exception
global/response
```

### 설정

```txt
global/config
```

---

## 21. 테스트 원칙

테스트는 기능의 핵심 정책을 검증해야 한다.

기본 위치:

```txt
src/test/java/com/ringdu/server
```

테스트 원칙:

* 성공 케이스를 검증한다.
* 실패 케이스를 검증한다.
* 권한 정책을 검증한다.
* 예외 응답을 검증한다.
* 비밀번호 암호화와 인증 흐름을 검증한다.
* Entity를 직접 노출하지 않는지 확인한다.
* 주요 기능은 테스트 없이 완료된 것으로 판단하지 않는다.

테스트 환경:

* 테스트에서는 H2 profile을 사용할 수 있다.
* 로컬 개발에서는 PostgreSQL을 사용한다.
* 운영 DB와 테스트 DB를 혼동하지 않는다.

---

## 22. 프론트엔드와의 연결 기준

프론트엔드는 별도 레포지토리에서 관리한다.

백엔드는 API 서버 역할을 담당한다.

원칙:

* 백엔드는 프론트 화면 구조에 과하게 종속되지 않는다.
* 프론트는 백엔드 API 계약에 맞춰 요청한다.
* 백엔드는 요청값을 신뢰하지 않고 항상 검증한다.
* 권한 판단은 백엔드에서 수행한다.
* 프론트에서 숨긴 기능도 백엔드에서 반드시 차단한다.

---

## 23. 운영 고려 원칙

Ringdu는 실제 운영을 고려하는 프로젝트다.

운영을 고려해 다음 원칙을 지킨다.

* 운영 시크릿은 코드에 직접 작성하지 않는다.
* DB 접속 정보는 환경변수로 관리한다.
* JWT secret은 환경변수로 관리한다.
* Redis 접속 정보는 환경변수로 관리한다.
* 운영 환경에서는 HTTPS를 사용한다.
* 운영 환경에서는 Cookie Secure 옵션을 사용한다.
* 로그에 비밀번호, 토큰, 개인정보를 남기지 않는다.
* Entity를 직접 응답하지 않는다.
* API 변경 시 프론트 영향도를 고려한다.
* 장애 발생 시 원인을 추적할 수 있도록 예외와 로그 구조를 관리한다.

---

## 24. 변경 시 지켜야 할 원칙

코드를 변경할 때는 다음 원칙을 지킨다.

* Controller에 비즈니스 로직을 넣지 않는다.
* Service에 핵심 흐름을 둔다.
* Repository는 데이터 접근만 담당한다.
* Entity를 API 응답으로 직접 반환하지 않는다.
* 요청 DTO와 응답 DTO를 분리한다.
* User에 학원 정보를 넣지 않는다.
* 학원 정보는 Academy로 분리한다.
* 학원 소속 관계는 AcademyMember로 분리한다.
* 학부모와 학생 관계는 StudentGuardian으로 분리한다.
* 학원과 학생 관계는 AcademyStudent로 분리한다.
* 민감정보를 응답에 포함하지 않는다.
* 운영 시크릿을 코드에 하드코딩하지 않는다.
* 권한 검증은 서버에서 수행한다.
* 테스트 없이 주요 기능을 완료했다고 판단하지 않는다.

---

## 25. 에이전트 작업 기준

에이전트가 코드를 수정할 때는 다음 기준을 따른다.

* 작업 전 현재 패키지 구조를 확인한다.
* 기존 코드 스타일을 우선 따른다.
* 새 파일은 책임에 맞는 패키지에 추가한다.
* 공통 코드와 도메인 코드를 구분한다.
* 임시 해결보다 운영 가능한 구조를 우선한다.
* 불필요한 대규모 리팩토링을 하지 않는다.
* 기능 변경 시 테스트를 함께 고려한다.
* 보안 관련 코드는 특히 신중하게 수정한다.
* 문서와 코드가 충돌하면 코드 기준으로 확인하고 문서를 갱신한다.


