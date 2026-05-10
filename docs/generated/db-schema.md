
# Database Schema

## 1. 목적

이 문서는 Ringdu 프로젝트의 데이터베이스 스키마를 정리하기 위한 문서이다.

Ringdu는 학원, 선생님, 학부모, 학생을 연결하는 학원 관리 웹/앱 서비스이며, 실제 배포와 운영까지 고려한다.  
따라서 테이블, 컬럼, 관계, 제약조건, 인덱스, enum 값을 명확하게 관리해야 한다.

이 문서는 다음 내용을 정리한다.

- 현재 사용 중인 테이블
- 향후 도메인 확장을 고려한 테이블
- 컬럼과 타입
- 테이블 간 관계
- 주요 제약조건
- 인덱스 기준
- enum 값
- DB 변경 시 확인할 기준

이 문서는 실제 코드의 Entity, Repository, 마이그레이션 또는 JPA 생성 결과와 함께 확인한다.  
문서와 코드가 다르면 현재 정상 동작하는 코드를 우선하고, 이후 문서를 갱신한다.

---

## 2. 문서 역할

이 문서는 다음 작업 전에 참고한다.

- 테이블 추가
- 컬럼 추가, 수정, 삭제
- 외래키 관계 변경
- 인덱스 추가 또는 삭제
- unique 제약조건 변경
- enum 값 변경
- API 응답에 필요한 데이터 확인
- 데이터 정합성 검토
- 마이그레이션 작성
- DB 관련 오류 분석
- 권한과 소속 관계 검토
- 학원, 학생, 학부모 관계 변경

---

## 3. 기본 설계 원칙

Ringdu의 데이터베이스 설계는 다음 원칙을 따른다.

- `users` 테이블은 로그인 계정 정보만 담당한다.
- 학원 정보는 `academies` 테이블로 분리한다.
- 학원과 운영 인력의 관계는 `academy_members` 테이블로 분리한다.
- 학생 프로필은 `students` 테이블로 분리한다.
- 학부모와 학생 관계는 `student_guardians` 테이블로 분리한다.
- 학원과 학생 관계는 `academy_students` 테이블로 분리한다.
- Entity를 API 응답으로 직접 노출하지 않는다.
- 비밀번호, 토큰, 시크릿 등 민감정보는 응답에 포함하지 않는다.
- 운영을 고려해 unique, index, foreign key를 신중하게 설계한다.

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
→ 학원과 ACADEMY / TEACHER의 운영 소속 관계

students
→ 학생 프로필

student_guardians
→ 학부모와 학생 관계

academy_students
→ 학원과 학생 관계
```

---

## 4. 작성 기준

각 테이블은 다음 형식으로 작성한다.

```md
## 테이블명

### 설명

테이블의 역할을 설명한다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 기본 키 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|

### 비고

추가로 알아야 할 내용을 적는다.
```

타입은 실제 PostgreSQL 기준으로 작성한다.  
JPA Entity와 실제 DB 컬럼명이 다르면 실제 DB 컬럼명을 우선한다.

---

## 5. 전체 테이블 목록

현재 또는 향후 Ringdu 도메인에서 사용하는 주요 테이블은 다음과 같다.

| 테이블명 | 설명 | 상태 |
|---|---|---|
| `users` | 로그인 계정 정보 | 사용 |
| `academies` | 학원 정보 | 예정 |
| `academy_members` | 학원과 ACADEMY / TEACHER 운영 소속 관계 | 예정 |
| `students` | 학생 프로필 정보 | 예정 |
| `student_guardians` | 학부모와 학생 관계 | 예정 |
| `academy_students` | 학원과 학생 소속 관계 | 예정 |
| `attendance_records` | 출석 기록 | 예정 |
| `homework_assignments` | 숙제 등록 정보 | 예정 |
| `homework_submissions` | 숙제 제출 정보 | 예정 |
| `billing_invoices` | 청구서 정보 | 예정 |
| `payment_records` | 납부 기록 | 예정 |
| `notifications` | 알림 정보 | 예정 |
| `files` | 업로드 파일 메타데이터 | 예정 |
| `audit_logs` | 사용자 및 관리자 활동 로그 | 예정 |

주의:

- 모든 테이블을 처음부터 만들 필요는 없다.
- 기능이 구현될 때 필요한 테이블만 추가한다.
- 아직 구현되지 않은 테이블은 예정 구조로 관리한다.
- 실제 Entity와 불일치하면 이 문서를 갱신한다.

---

## 6. 테이블 상세

## users

### 설명

사용자 로그인 계정 정보를 저장하는 테이블이다.

`users`는 계정 식별, 인증, 기본 사용자 정보만 담당한다.  
학원 정보, 학생 소속 정보, 학부모-학생 관계 정보는 다른 테이블에서 관리한다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 사용자 ID |
| email | varchar | No |  | 로그인 이메일 |
| password | varchar | Yes |  | BCrypt로 암호화된 비밀번호 |
| name | varchar | No |  | 사용자 이름 |
| phone | varchar | Yes |  | 전화번호 |
| role | varchar | No |  | 사용자 권한 |
| status | varchar | No |  | 사용자 상태 |
| provider | varchar | No | `LOCAL` | 가입 제공자 |
| provider_id | varchar | Yes |  | 소셜 로그인 제공자 ID |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_users | id | Primary Key | 사용자 기본 키 |
| uk_users_email | email | Unique | 이메일 중복 방지 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_users_email | email | Index | 로그인 이메일 조회 |
| idx_users_role | role | Index | 권한별 사용자 조회 |
| idx_users_status | status | Index | 사용자 상태별 조회 |
| idx_users_provider_provider_id | provider, provider_id | Index | 소셜 로그인 사용자 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| academy_members | 1:N | 한 사용자는 여러 학원 소속 관계를 가질 수 있다 |
| students | 1:1 또는 1:N | 학생 계정과 학생 프로필이 연결될 수 있다 |
| student_guardians | 1:N | 학부모 사용자는 여러 학생과 연결될 수 있다 |
| audit_logs | 1:N | 한 사용자는 여러 활동 로그와 연결될 수 있다 |
| notifications | 1:N | 한 사용자는 여러 알림을 받을 수 있다 |

### 비고

- 비밀번호는 평문으로 저장하지 않는다.
- 일반 가입 사용자는 `provider = LOCAL`, `provider_id = null`이다.
- 소셜 가입 사용자는 `provider = KAKAO / GOOGLE / NAVER`, `provider_id`를 사용한다.
- 소셜 가입 사용자는 `password`가 null일 수 있다.
- API 응답에 `password`를 포함하지 않는다.
- `ADMIN`은 일반 회원가입으로 생성하지 않는다.
- `ACADEMY`는 일반 회원가입으로 생성하지 않고 ADMIN 전용 API로 생성한다.
- `OWNER`, `DESK`는 MVP 역할에서 제외하며 향후 확장 역할로 검토한다.

---

## academies

### 설명

학원 정보를 저장하는 테이블이다.

학원 이름, 주소, 전화번호, 학원 코드 등 학원 자체의 정보를 관리한다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 학원 ID |
| name | varchar | No |  | 학원명 |
| phone | varchar | Yes |  | 학원 전화번호 |
| address | varchar | Yes |  | 학원 주소 |
| academy_code | varchar | No |  | 학원 식별 코드 |
| status | varchar | No |  | 학원 상태 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_academies | id | Primary Key | 학원 기본 키 |
| uk_academies_academy_code | academy_code | Unique | 학원 코드 중복 방지 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_academies_status | status | Index | 학원 상태별 조회 |
| idx_academies_name | name | Index | 학원명 검색 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| academy_members | 1:N | 한 학원은 여러 운영 인력을 가질 수 있다 |
| academy_students | 1:N | 한 학원은 여러 학생을 가질 수 있다 |
| billing_invoices | 1:N | 한 학원은 여러 청구서를 가질 수 있다 |

### 비고

- 학원 정보는 `users` 테이블에 넣지 않는다.
- 학원 코드는 초대, 연결, 조회 흐름에서 사용할 수 있다.
- 학원 삭제 정책은 실제 운영 정책에 맞게 별도 정의한다.

---

## academy_members

### 설명

학원과 사용자 사이의 소속 관계를 저장하는 테이블이다.

주로 `ACADEMY`, `TEACHER`가 학원과 연결될 때 사용한다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 소속 관계 ID |
| academy_id | bigint | No |  | 학원 ID |
| user_id | bigint | No |  | 사용자 ID |
| academy_role | varchar | No |  | 학원 내 역할 |
| status | varchar | No |  | 소속 상태 |
| joined_at | timestamp | Yes |  | 가입 또는 승인 일시 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_academy_members | id | Primary Key | 소속 관계 기본 키 |
| fk_academy_members_academy_id | academy_id | Foreign Key | academies.id 참조 |
| fk_academy_members_user_id | user_id | Foreign Key | users.id 참조 |
| uk_academy_members_academy_user | academy_id, user_id | Unique | 같은 학원에 같은 사용자가 중복 소속되는 것 방지 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_academy_members_academy_id | academy_id | Index | 학원별 구성원 조회 |
| idx_academy_members_user_id | user_id | Index | 사용자별 소속 학원 조회 |
| idx_academy_members_academy_role | academy_role | Index | 학원 내 역할별 조회 |
| idx_academy_members_status | status | Index | 소속 상태별 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| academies | N:1 | 소속 관계는 특정 학원에 속한다 |
| users | N:1 | 소속 관계는 특정 사용자에 속한다 |

### 비고

- `ACADEMY`, `TEACHER`의 학원 운영 소속 관계를 관리한다.
- 플랫폼 권한인 `ADMIN`은 일반 학원 소속 관계와 분리해서 관리할 수 있다.
- 학원 내 권한과 전역 사용자 권한의 관계를 혼동하지 않는다.

---

## students

### 설명

학생 프로필 정보를 저장하는 테이블이다.

학생이 직접 로그인 계정을 가진 경우 `users`와 연결될 수 있다.  
어린 학생처럼 직접 계정이 없는 경우 정책에 따라 `user_id`를 null로 둘 수 있다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 학생 ID |
| user_id | bigint | Yes |  | 학생 로그인 계정 ID |
| name | varchar | No |  | 학생 이름 |
| birth_date | date | Yes |  | 생년월일 |
| school_name | varchar | Yes |  | 학교명 |
| grade | varchar | Yes |  | 학년 |
| status | varchar | No |  | 학생 상태 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_students | id | Primary Key | 학생 기본 키 |
| fk_students_user_id | user_id | Foreign Key | users.id 참조 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_students_user_id | user_id | Index | 사용자 계정과 연결된 학생 조회 |
| idx_students_name | name | Index | 학생명 검색 |
| idx_students_status | status | Index | 학생 상태별 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| users | N:1 또는 1:1 | 학생 계정과 연결될 수 있다 |
| student_guardians | 1:N | 한 학생은 여러 보호자와 연결될 수 있다 |
| academy_students | 1:N | 한 학생은 여러 학원과 연결될 수 있다 |
| attendance_records | 1:N | 한 학생은 여러 출석 기록을 가질 수 있다 |
| homework_submissions | 1:N | 한 학생은 여러 숙제 제출 기록을 가질 수 있다 |

### 비고

- 학생 프로필은 `users`와 분리한다.
- 학생이 직접 로그인하지 않는 구조도 고려할 수 있다.
- 학부모와 학생 연결은 `student_guardians`에서 관리한다.

---

## student_guardians

### 설명

학부모와 학생의 관계를 저장하는 테이블이다.

학부모가 자녀의 시간표, 출석, 숙제, 청구서, 납부 상태를 확인할 때 이 관계를 기준으로 권한을 판단한다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 관계 ID |
| student_id | bigint | No |  | 학생 ID |
| guardian_user_id | bigint | No |  | 학부모 사용자 ID |
| relationship | varchar | Yes |  | 학생과의 관계 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_student_guardians | id | Primary Key | 관계 기본 키 |
| fk_student_guardians_student_id | student_id | Foreign Key | students.id 참조 |
| fk_student_guardians_guardian_user_id | guardian_user_id | Foreign Key | users.id 참조 |
| uk_student_guardians_student_guardian | student_id, guardian_user_id | Unique | 같은 학생과 학부모 관계 중복 방지 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_student_guardians_student_id | student_id | Index | 학생별 보호자 조회 |
| idx_student_guardians_guardian_user_id | guardian_user_id | Index | 학부모별 자녀 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| students | N:1 | 관계는 특정 학생에 속한다 |
| users | N:1 | 관계는 특정 학부모 사용자에 속한다 |

### 비고

- `guardian_user_id`는 `PARENT` 권한 사용자를 기준으로 한다.
- 학부모는 연결된 자녀의 정보만 조회할 수 있어야 한다.
- 관계 승인 방식은 제품 정책에 따라 별도 설계한다.

---

## academy_students

### 설명

학원과 학생의 소속 관계를 저장하는 테이블이다.

학생이 어느 학원에 등록되어 있는지 관리한다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 학원-학생 관계 ID |
| academy_id | bigint | No |  | 학원 ID |
| student_id | bigint | No |  | 학생 ID |
| status | varchar | No |  | 등록 상태 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_academy_students | id | Primary Key | 관계 기본 키 |
| fk_academy_students_academy_id | academy_id | Foreign Key | academies.id 참조 |
| fk_academy_students_student_id | student_id | Foreign Key | students.id 참조 |
| uk_academy_students_academy_student | academy_id, student_id | Unique | 같은 학원에 같은 학생 중복 등록 방지 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_academy_students_academy_id | academy_id | Index | 학원별 학생 조회 |
| idx_academy_students_student_id | student_id | Index | 학생별 소속 학원 조회 |
| idx_academy_students_status | status | Index | 등록 상태별 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| academies | N:1 | 관계는 특정 학원에 속한다 |
| students | N:1 | 관계는 특정 학생에 속한다 |

### 비고

- 학생이 여러 학원에 다닐 수 있는 구조를 고려한다.
- 학원별 학생 상태 관리는 이 테이블에서 처리한다.

---

## attendance_records

### 설명

학생 출석 기록을 저장하는 테이블이다.

학부모는 연결된 자녀의 출석 상태를 확인할 수 있어야 한다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 출석 기록 ID |
| academy_id | bigint | No |  | 학원 ID |
| student_id | bigint | No |  | 학생 ID |
| class_id | bigint | Yes |  | 수업 ID |
| status | varchar | No |  | 출석 상태 |
| attendance_date | date | No |  | 출석 일자 |
| checked_by_user_id | bigint | Yes |  | 출석 처리 사용자 ID |
| checked_at | timestamp | Yes |  | 출석 처리 일시 |
| memo | text | Yes |  | 메모 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_attendance_records | id | Primary Key | 출석 기록 기본 키 |
| fk_attendance_records_academy_id | academy_id | Foreign Key | academies.id 참조 |
| fk_attendance_records_student_id | student_id | Foreign Key | students.id 참조 |
| fk_attendance_records_checked_by_user_id | checked_by_user_id | Foreign Key | users.id 참조 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_attendance_records_academy_date | academy_id, attendance_date | Index | 학원별 일자 출석 조회 |
| idx_attendance_records_student_date | student_id, attendance_date | Index | 학생별 출석 조회 |
| idx_attendance_records_status | status | Index | 출석 상태별 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| academies | N:1 | 출석 기록은 특정 학원에 속한다 |
| students | N:1 | 출석 기록은 특정 학생에 속한다 |
| users | N:1 | 출석 처리 사용자와 연결될 수 있다 |

### 비고

- 출석 상태는 enum으로 관리한다.
- 학부모는 연결된 자녀의 출석 정보만 조회할 수 있어야 한다.
- 선생님은 담당 수업 또는 담당 학생 기준으로 권한을 검증해야 한다.

---

## homework_assignments

### 설명

선생님이 등록한 숙제 정보를 저장하는 테이블이다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 숙제 ID |
| academy_id | bigint | No |  | 학원 ID |
| teacher_user_id | bigint | No |  | 숙제 등록 선생님 ID |
| title | varchar | No |  | 숙제 제목 |
| content | text | Yes |  | 숙제 내용 |
| due_date | date | Yes |  | 제출 기한 |
| status | varchar | No |  | 숙제 상태 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_homework_assignments | id | Primary Key | 숙제 기본 키 |
| fk_homework_assignments_academy_id | academy_id | Foreign Key | academies.id 참조 |
| fk_homework_assignments_teacher_user_id | teacher_user_id | Foreign Key | users.id 참조 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_homework_assignments_academy_id | academy_id | Index | 학원별 숙제 조회 |
| idx_homework_assignments_teacher_user_id | teacher_user_id | Index | 선생님별 숙제 조회 |
| idx_homework_assignments_due_date | due_date | Index | 마감일 기준 조회 |
| idx_homework_assignments_status | status | Index | 숙제 상태별 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| academies | N:1 | 숙제는 특정 학원에 속한다 |
| users | N:1 | 숙제는 특정 선생님이 등록한다 |
| homework_submissions | 1:N | 하나의 숙제는 여러 제출 기록을 가진다 |

### 비고

- 수업 도메인이 추가되면 수업 ID와 연결할 수 있다.
- 숙제 대상 학생 또는 반 관리 방식은 별도 설계가 필요하다.

---

## homework_submissions

### 설명

학생의 숙제 제출 정보를 저장하는 테이블이다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 숙제 제출 ID |
| homework_assignment_id | bigint | No |  | 숙제 ID |
| student_id | bigint | No |  | 학생 ID |
| content | text | Yes |  | 제출 내용 |
| status | varchar | No |  | 제출 상태 |
| submitted_at | timestamp | Yes |  | 제출 일시 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_homework_submissions | id | Primary Key | 제출 기본 키 |
| fk_homework_submissions_homework_assignment_id | homework_assignment_id | Foreign Key | homework_assignments.id 참조 |
| fk_homework_submissions_student_id | student_id | Foreign Key | students.id 참조 |
| uk_homework_submissions_assignment_student | homework_assignment_id, student_id | Unique | 같은 숙제에 같은 학생의 중복 제출 방지 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_homework_submissions_assignment_id | homework_assignment_id | Index | 숙제별 제출 조회 |
| idx_homework_submissions_student_id | student_id | Index | 학생별 제출 조회 |
| idx_homework_submissions_status | status | Index | 제출 상태별 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| homework_assignments | N:1 | 제출은 특정 숙제에 속한다 |
| students | N:1 | 제출은 특정 학생에 속한다 |

### 비고

- 첨부파일 제출이 필요하면 `files` 테이블과 연결한다.
- 제출 수정 가능 여부는 제품 정책에 따라 별도 정의한다.

---

## billing_invoices

### 설명

학원에서 학생 또는 학부모에게 발행하는 청구서 정보를 저장하는 테이블이다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 청구서 ID |
| academy_id | bigint | No |  | 학원 ID |
| student_id | bigint | No |  | 학생 ID |
| amount | numeric | No |  | 청구 금액 |
| status | varchar | No |  | 청구 상태 |
| issued_at | timestamp | Yes |  | 발행 일시 |
| due_date | date | Yes |  | 납부 기한 |
| memo | text | Yes |  | 메모 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_billing_invoices | id | Primary Key | 청구서 기본 키 |
| fk_billing_invoices_academy_id | academy_id | Foreign Key | academies.id 참조 |
| fk_billing_invoices_student_id | student_id | Foreign Key | students.id 참조 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_billing_invoices_academy_id | academy_id | Index | 학원별 청구서 조회 |
| idx_billing_invoices_student_id | student_id | Index | 학생별 청구서 조회 |
| idx_billing_invoices_status | status | Index | 청구 상태별 조회 |
| idx_billing_invoices_due_date | due_date | Index | 납부 기한 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| academies | N:1 | 청구서는 특정 학원에 속한다 |
| students | N:1 | 청구서는 특정 학생에게 발행된다 |
| payment_records | 1:N | 하나의 청구서에 여러 납부 기록이 연결될 수 있다 |

### 비고

- 결제 연동 전에는 납부 상태 관리 중심으로 설계할 수 있다.
- 실제 PG 연동은 별도 설계 문서에서 다룬다.

---

## payment_records

### 설명

청구서에 대한 납부 기록을 저장하는 테이블이다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 납부 기록 ID |
| billing_invoice_id | bigint | No |  | 청구서 ID |
| amount | numeric | No |  | 납부 금액 |
| method | varchar | Yes |  | 납부 방식 |
| paid_at | timestamp | Yes |  | 납부 일시 |
| status | varchar | No |  | 납부 상태 |
| memo | text | Yes |  | 메모 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_payment_records | id | Primary Key | 납부 기록 기본 키 |
| fk_payment_records_billing_invoice_id | billing_invoice_id | Foreign Key | billing_invoices.id 참조 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_payment_records_invoice_id | billing_invoice_id | Index | 청구서별 납부 기록 조회 |
| idx_payment_records_paid_at | paid_at | Index | 납부 일자 조회 |
| idx_payment_records_status | status | Index | 납부 상태별 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| billing_invoices | N:1 | 납부 기록은 특정 청구서에 속한다 |

### 비고

- 실제 결제 연동 시 PG 거래 ID, 승인 번호, 취소 상태 등 추가 컬럼이 필요할 수 있다.
- 결제 취소와 부분 납부 정책은 별도 설계가 필요하다.

---

## notifications

### 설명

사용자 알림 정보를 저장하는 테이블이다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 알림 ID |
| user_id | bigint | No |  | 수신 사용자 ID |
| type | varchar | No |  | 알림 유형 |
| title | varchar | No |  | 알림 제목 |
| message | text | Yes |  | 알림 내용 |
| read_at | timestamp | Yes |  | 읽은 일시 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_notifications | id | Primary Key | 알림 기본 키 |
| fk_notifications_user_id | user_id | Foreign Key | users.id 참조 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_notifications_user_id | user_id | Index | 사용자별 알림 조회 |
| idx_notifications_read_at | read_at | Index | 읽음 여부 조회 |
| idx_notifications_created_at | created_at | Index | 최신 알림 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| users | N:1 | 알림은 특정 사용자에게 속한다 |

### 비고

- 알림 내용에 민감정보를 과도하게 포함하지 않는다.
- 읽음 상태와 삭제 정책은 제품 요구사항에 맞게 정의한다.

---

## files

### 설명

업로드 파일의 메타데이터를 저장하는 테이블이다.

파일 자체는 로컬 디스크 또는 S3 같은 외부 저장소에 저장하고, DB에는 메타데이터만 저장한다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 파일 ID |
| uploader_id | bigint | Yes |  | 업로드 사용자 ID |
| original_name | varchar | No |  | 원본 파일명 |
| stored_name | varchar | No |  | 저장 파일명 |
| content_type | varchar | Yes |  | MIME 타입 |
| size_bytes | bigint | No |  | 파일 크기 |
| storage_path | varchar | No |  | 저장 위치 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_files | id | Primary Key | 파일 기본 키 |
| fk_files_uploader_id | uploader_id | Foreign Key | users.id 참조 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_files_uploader_id | uploader_id | Index | 사용자별 파일 조회 |
| idx_files_created_at | created_at | Index | 업로드 일시 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| users | N:1 | 파일은 업로드 사용자와 연결될 수 있다 |

### 비고

- 파일 경로를 외부 응답에 그대로 노출하지 않는다.
- 파일 접근 권한을 별도로 확인한다.
- 운영 환경에서는 S3 같은 외부 저장소 사용을 고려한다.
- 업로드 보안 기준은 `docs/SECURITY.md`를 따른다.

---

## audit_logs

### 설명

사용자 또는 관리자 활동 이력을 저장하는 테이블이다.

운영 중 보안 이벤트, 관리자 작업, 주요 상태 변경을 추적하기 위해 사용할 수 있다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 로그 ID |
| user_id | bigint | Yes |  | 작업 사용자 ID |
| action | varchar | No |  | 작업 종류 |
| target_type | varchar | Yes |  | 대상 유형 |
| target_id | varchar | Yes |  | 대상 ID |
| ip_address | varchar | Yes |  | 요청 IP |
| user_agent | varchar | Yes |  | User-Agent |
| created_at | timestamp | No | current timestamp | 생성 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_audit_logs | id | Primary Key | 로그 기본 키 |
| fk_audit_logs_user_id | user_id | Foreign Key | users.id 참조 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_audit_logs_user_id | user_id | Index | 사용자별 로그 조회 |
| idx_audit_logs_action | action | Index | 작업 종류별 조회 |
| idx_audit_logs_created_at | created_at | Index | 기간별 조회 |
| idx_audit_logs_target | target_type, target_id | Index | 대상별 이력 조회 |

### 관계

| 대상 테이블 | 관계 | 설명 |
|---|---|---|
| users | N:1 | 로그는 특정 사용자와 연결될 수 있다 |

### 비고

- 보안 이벤트와 관리자 작업은 기록하는 것을 권장한다.
- 비밀번호, 토큰, 시크릿 등 민감정보는 기록하지 않는다.
- 로그 보관 기간은 운영 정책에 따라 별도 정의한다.

---

## 7. Redis 저장 구조

Refresh Token은 DB 테이블에 원문으로 저장하지 않는다.

Ringdu 인증 구조에서는 Refresh Token 상태 관리를 Redis TTL 기반 key로 처리한다.

기본 원칙:

- Refresh Token 원문 저장 금지
- SHA-256 등으로 해시한 `tokenHash`만 저장
- 활성 Refresh Token key에는 Refresh Token 만료 시간과 동일한 TTL 설정
- 사용된 Refresh Token key에는 남은 만료 시간 또는 설정 TTL 적용
- Refresh Token Rotation 적용
- 재사용 감지를 위해 사용된 토큰 해시를 일정 시간 보관

현재 key 구조:

```txt
refresh:{tokenHash}
used-refresh:{oldTokenHash}
```

현재 저장값:

```txt
userId
```

주의:

- Redis 저장 구조는 관계형 DB 스키마가 아니다.
- `refresh_tokens` 테이블을 별도로 만들지 않는다.
- Access Token은 DB에도 Redis에도 저장하지 않는다.
- Refresh Token 원문, tokenHash, Cookie 원문은 로그에 남기지 않는다.
- 운영 환경에서는 Redis 장애 상황도 고려해야 한다.

---

## 8. Enum 값

프로젝트에서 사용하는 enum 값은 이곳에 정리한다.

실제 값은 코드의 enum 정의를 기준으로 한다.

### Role

| 값 | 설명 |
|---|---|
| ADMIN | Ringdu 플랫폼 관리자 |
| ACADEMY | 학원 |
| TEACHER | 선생 |
| PARENT | 부모 |
| STUDENT | 학생 |

`OWNER`, `DESK`는 MVP enum 기준에서 제외한다. 향후 학원 내부 세부 권한 분리가 필요할 때 확장 역할로 검토한다.

### AuthProvider

| 값 | 설명 |
|---|---|
| LOCAL | 일반 이메일 회원가입 |
| KAKAO | 카카오 소셜 로그인 |
| GOOGLE | 구글 소셜 로그인 |
| NAVER | 네이버 소셜 로그인 |

### UserStatus

| 값 | 설명 |
|---|---|
| ACTIVE | 활성 사용자 |
| INACTIVE | 비활성 사용자 |
| SUSPENDED | 정지된 사용자 |
| DELETED | 삭제된 사용자 |

UserStatus 값은 실제 코드 enum과 반드시 일치해야 한다.  
코드에 정의된 값이 다르면 코드 기준으로 이 문서를 갱신한다.

### AcademyStatus

| 값 | 설명 |
|---|---|
| ACTIVE | 운영 중 |
| INACTIVE | 비활성 |
| SUSPENDED | 정지 |
| CLOSED | 폐원 또는 종료 |

### AcademyMemberStatus

| 값 | 설명 |
|---|---|
| INVITED | 초대됨 |
| ACTIVE | 활성 소속 |
| INACTIVE | 비활성 |
| LEFT | 퇴사 또는 소속 해제 |

### StudentStatus

| 값 | 설명 |
|---|---|
| ACTIVE | 재원 또는 활성 학생 |
| INACTIVE | 비활성 |
| GRADUATED | 졸업 또는 수료 |
| WITHDRAWN | 퇴원 |

### AttendanceStatus

| 값 | 설명 |
|---|---|
| PRESENT | 출석 |
| ABSENT | 결석 |
| LATE | 지각 |
| EXCUSED | 인정 결석 |
| UNKNOWN | 미확인 |

### HomeworkStatus

| 값 | 설명 |
|---|---|
| OPEN | 진행 중 |
| CLOSED | 마감 |
| CANCELED | 취소 |

### HomeworkSubmissionStatus

| 값 | 설명 |
|---|---|
| NOT_SUBMITTED | 미제출 |
| SUBMITTED | 제출 |
| LATE_SUBMITTED | 지각 제출 |
| REVIEWED | 확인 완료 |

### BillingStatus

| 값 | 설명 |
|---|---|
| DRAFT | 작성 중 |
| ISSUED | 청구됨 |
| PAID | 납부 완료 |
| PARTIALLY_PAID | 부분 납부 |
| OVERDUE | 연체 |
| CANCELED | 취소 |

### PaymentStatus

| 값 | 설명 |
|---|---|
| PENDING | 대기 |
| PAID | 납부 완료 |
| CANCELED | 취소 |
| FAILED | 실패 |
| REFUNDED | 환불 |

---

## 9. 관계 요약

Ringdu의 핵심 관계는 다음과 같다.

```txt
users
├── academy_members
├── student_guardians
├── students
├── notifications
└── audit_logs

academies
├── academy_members
├── academy_students
├── attendance_records
├── homework_assignments
└── billing_invoices

students
├── student_guardians
├── academy_students
├── attendance_records
├── homework_submissions
└── billing_invoices
```

관계 상세:

| 기준 테이블 | 대상 테이블 | 관계 | 설명 |
|---|---|---|---|
| users | academy_members | 1:N | 한 사용자는 여러 학원 소속 관계를 가질 수 있다 |
| academies | academy_members | 1:N | 한 학원은 여러 운영 인력을 가질 수 있다 |
| users | students | 1:1 또는 1:N | 학생 로그인 계정과 학생 프로필이 연결될 수 있다 |
| users | student_guardians | 1:N | 학부모 사용자는 여러 학생과 연결될 수 있다 |
| students | student_guardians | 1:N | 한 학생은 여러 보호자와 연결될 수 있다 |
| academies | academy_students | 1:N | 한 학원은 여러 학생과 연결될 수 있다 |
| students | academy_students | 1:N | 한 학생은 여러 학원과 연결될 수 있다 |
| students | attendance_records | 1:N | 한 학생은 여러 출석 기록을 가진다 |
| students | homework_submissions | 1:N | 한 학생은 여러 숙제 제출 기록을 가진다 |
| homework_assignments | homework_submissions | 1:N | 하나의 숙제는 여러 제출 기록을 가진다 |
| students | billing_invoices | 1:N | 한 학생은 여러 청구서를 가질 수 있다 |
| billing_invoices | payment_records | 1:N | 하나의 청구서는 여러 납부 기록을 가질 수 있다 |

---

## 10. 인덱스 설계 기준

인덱스는 조회 성능을 위해 사용하지만, 쓰기 비용도 함께 고려한다.

인덱스가 필요한 경우:

- 로그인 이메일 조회
- 학원별 구성원 조회
- 사용자별 소속 학원 조회
- 학부모별 자녀 조회
- 학생별 출석 조회
- 학원별 출석 조회
- 학생별 숙제 제출 조회
- 청구 상태별 조회
- 납부 상태별 조회
- 최신 알림 조회
- 기간별 로그 조회

인덱스 설계 원칙:

- foreign key 컬럼에는 인덱스를 고려한다.
- 자주 조회되는 상태값에는 인덱스를 고려한다.
- 날짜 범위 조회가 많은 컬럼에는 인덱스를 고려한다.
- 복합 조회 조건은 복합 인덱스를 고려한다.
- 사용하지 않는 인덱스를 과도하게 만들지 않는다.
- 운영 중 쿼리 패턴이 확인되면 인덱스를 재검토한다.

---

## 11. 제약조건 설계 기준

제약조건은 데이터 정합성을 지키기 위해 사용한다.

필수로 고려할 제약조건:

- Primary Key
- Foreign Key
- Unique
- Not Null
- 상태값 enum
- 금액 컬럼의 음수 방지
- 중복 관계 방지

Ringdu에서 특히 중요한 unique 기준:

| 테이블 | 컬럼 | 목적 |
|---|---|---|
| users | email | 이메일 중복 가입 방지 |
| academies | academy_code | 학원 코드 중복 방지 |
| academy_members | academy_id, user_id | 같은 학원에 같은 사용자 중복 소속 방지 |
| student_guardians | student_id, guardian_user_id | 같은 학생과 학부모 관계 중복 방지 |
| academy_students | academy_id, student_id | 같은 학원에 같은 학생 중복 등록 방지 |
| homework_submissions | homework_assignment_id, student_id | 같은 숙제에 같은 학생 중복 제출 방지 |

---

## 12. 마이그레이션 작성 기준

DB 스키마 변경 시 다음 기준을 따른다.

- 변경 전 현재 스키마를 확인한다.
- Entity 변경과 DB 변경이 일치하는지 확인한다.
- 컬럼 추가 시 null 허용 여부와 기본값을 명확히 한다.
- 기존 데이터에 영향을 주는 변경은 별도 검증이 필요하다.
- 컬럼 삭제는 신중하게 진행한다.
- enum 값 변경은 기존 데이터와 코드 영향을 함께 확인한다.
- 외래키 추가 시 참조 무결성을 확인한다.
- 인덱스 추가 시 조회 성능과 쓰기 비용을 함께 고려한다.
- 운영 환경 마이그레이션은 롤백 방법을 고려한다.
- 운영 데이터가 있는 경우 destructive change를 피한다.

운영 데이터에 영향을 줄 수 있는 변경 예시:

```txt
컬럼 삭제
컬럼 타입 변경
not null 제약 추가
unique 제약 추가
외래키 추가
enum 값 제거
대량 데이터 수정
```

---

## 13. DB 변경 체크리스트

DB 변경 전후 다음을 확인한다.

- [ ] 변경 대상 테이블이 명확한가?
- [ ] 변경 이유가 명확한가?
- [ ] 컬럼 타입이 적절한가?
- [ ] null 허용 여부가 명확한가?
- [ ] 기본값이 필요한가?
- [ ] unique 제약조건이 필요한가?
- [ ] 외래키 관계가 필요한가?
- [ ] 인덱스가 필요한가?
- [ ] 기존 데이터와 충돌하지 않는가?
- [ ] API 요청 또는 응답에 영향이 있는가?
- [ ] 프론트엔드 화면에 영향이 있는가?
- [ ] 권한 또는 보안에 영향이 있는가?
- [ ] 테스트 데이터와 운영 데이터 모두 고려했는가?
- [ ] 마이그레이션 실패 시 롤백 가능한가?
- [ ] 관련 문서가 갱신되었는가?

---

## 14. 보안 기준

DB에는 민감정보가 저장될 수 있으므로 다음 기준을 지킨다.

- 비밀번호는 BCrypt로 암호화한다.
- Refresh Token 원문은 DB에 저장하지 않는다.
- Access Token은 DB에 저장하지 않는다.
- JWT secret은 DB나 코드에 저장하지 않는다.
- 개인정보를 불필요하게 많이 저장하지 않는다.
- 파일 저장 경로를 외부 응답에 그대로 노출하지 않는다.
- 로그 테이블에 비밀번호, 토큰, 시크릿을 저장하지 않는다.
- Entity를 API 응답으로 직접 반환하지 않는다.
- 권한 검증 없이 다른 학원의 데이터에 접근할 수 없게 한다.
- 학부모는 연결된 자녀의 데이터만 조회할 수 있게 한다.
- 선생님은 담당 범위의 학생과 수업 데이터만 접근할 수 있게 한다.

---

## 15. 에이전트 작업 지침

AI 에이전트가 DB 관련 작업을 할 때는 다음을 따른다.

- 이 문서를 먼저 확인한다.
- 실제 코드의 Entity, Repository, Migration 또는 JPA 생성 결과와 비교한다.
- DB 스키마와 코드가 다르면 현재 동작하는 코드를 기준으로 판단한다.
- 스키마 변경이 필요한 경우 영향 범위를 확인한다.
- 민감정보 컬럼은 API 응답에 노출하지 않는다.
- 외래키, unique, index 등 제약조건을 함께 검토한다.
- 기존 데이터를 손상시킬 수 있는 변경은 피하거나 롤백 방법을 작성한다.
- 보안, 권한, 데이터 정합성에 영향을 주는 변경은 설계 문서를 함께 확인한다.
- 변경 후 이 문서를 갱신한다.
- 임시로 만든 컬럼이나 테이블은 기술부채로 남긴다.

---

## 16. 관련 문서

DB 스키마를 변경하거나 검토할 때 다음 문서를 함께 확인한다.

| 목적 | 문서 |
|---|---|
| 전체 구조 | `docs/ARCHITECTURE.md` |
| 설계 기준 | `docs/design-docs/index.md` |
| 제품 요구사항 | `docs/product-specs/index.md` |
| 보안 기준 | `docs/SECURITY.md` |
| 안정성 기준 | `docs/RELIABILITY.md` |
| 품질 기준 | `docs/QUALITY_SCORE.md` |
| 기술부채 | `docs/exec-plans/tech-debt-tracker.md` |
| 작업 흐름 | `docs/WORKFLOW.md` |

---

## 17. 핵심 원칙 요약

- DB 스키마는 Ringdu 데이터 구조의 기준이다.
- User는 로그인 계정 정보만 담당한다.
- 학원 정보는 Academy로 분리한다.
- 학원과 운영 인력 관계는 AcademyMember로 분리한다.
- 학생 프로필은 Student로 분리한다.
- 학부모와 학생 관계는 StudentGuardian으로 분리한다.
- 학원과 학생 관계는 AcademyStudent로 분리한다.
- Refresh Token 원문은 DB에 저장하지 않는다.
- 테이블, 컬럼, 관계, 제약조건을 명확히 기록한다.
- 민감정보는 안전하게 저장하고 노출하지 않는다.
- DB 변경은 코드, API, 보안, 안정성에 영향을 줄 수 있다.
- 운영 데이터에 영향을 주는 변경은 신중하게 계획한다.
- 스키마 변경 후 문서를 갱신한다.
