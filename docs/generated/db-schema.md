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
- 학생 프로필은 `student_profiles` 테이블로 분리한다.
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

student_profiles
→ 학생 프로필 (비회원 학생 포함)

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
| `academies` | 학원 정보 | 사용 |
| `academy_teacher_invitations` | 학원-선생님 초대장 | 사용 |
| `academy_student_invitations` | 학원-학생 초대장 | 사용 |
| `academy_members` | 학원과 ACADEMY / TEACHER 운영 소속 관계 | 사용 |
| `parent_student_invitations` | 부모-학생 연결 초대장 | 사용 |
| `parent_student_relations` | 부모-학생 연결 관계 | 사용 |
| `student_profiles` | 학생 프로필 정보 | 사용 |
| `academy_students` | 학원과 학생 소속 관계 | 예정 |
| `attendance_records` | 출석 기록 | 예정 |
| `homework_assignments` | 숙제 등록 정보 | 예정 |
| `homework_submissions` | 숙제 제출 정보 | 예정 |
| `billing_invoices` | 청구서 정보 | 예정 |
| `payment_records` | 납부 기록 | 예정 |
| `notifications` | 알림 정보 | 예정 |
| `files` | 업로드 파일 메타데이터 | 예정 |
| `audit_logs` | 사용자 및 관리자 활동 로그 | 예정 |

---

## 6. 테이블 상세

## student_profiles

### 설명

학생 프로필 정보를 저장하는 테이블이다.

학원이 직접 등록한 학생 정보를 포함하며, 로그인 계정이 없는 비회원 학생도 저장 가능하다.  
학생이 계정을 가진 경우 `user_id`를 통해 `users` 테이블과 연결된다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 학생 ID |
| academy_id | bigint | No |  | 등록한 학원 ID |
| user_id | bigint | Yes |  | 연결된 사용자 ID |
| name | varchar | No |  | 학생 이름 |
| birth_date | date | Yes |  | 생년월일 |
| school | varchar | Yes |  | 학교명 |
| grade | varchar | Yes |  | 학년 |
| email | varchar | Yes |  | 학생 이메일 |
| phone | varchar | Yes |  | 학생 전화번호 |
| guardian_phone | varchar | Yes |  | 보호자 전화번호 |
| status | varchar | No | `ACTIVE` | 학생 상태 |
| memo | text | Yes |  | 메모 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 제약조건

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| pk_student_profiles | id | Primary Key | 학생 기본 키 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_student_profiles_academy_id | academy_id | Index | 학원별 학생 조회 |
| idx_student_profiles_academy_status | academy_id, status | Index | 학원별 상태별 학생 조회 |
| idx_student_profiles_email | email | Index | 이메일로 학생 검색 |
| idx_student_profiles_phone | phone | Index | 전화번호로 학생 검색 |
| idx_student_profiles_user_id | user_id | Index | 사용자별 프로필 조회 |

### 비고

- `status` 값은 `ACTIVE`, `INACTIVE`, `GRADUATED`를 사용한다.
- `user_id`는 학생이 직접 가입 후 연결될 때 채워진다.
- `guardian_phone`은 학원 연락용으로만 사용되며, 부모 계정 연결은 `parent_student_relations`를 따른다.

---

## academy_student_invitations

### 설명

학원이 학생(계정 소유자)에게 보내는 초대장 정보를 저장하는 테이블이다.
학생이 수락하면 학원 학생 프로필과 연결된다.

### 컬럼

| 컬럼명 | 타입 | Null 허용 | 기본값 | 설명 |
|---|---|---:|---|---|
| id | bigint | No | auto increment | 초대 ID |
| academy_id | bigint | No |  | 초대 학원 ID |
| student_profile_id | bigint | Yes |  | 연결할 학생 프로필 ID (nullable) |
| receiver_user_id | bigint | No |  | 수신 학생 사용자 ID |
| receiver_email | varchar | Yes |  | 수신 학생 이메일 |
| receiver_phone | varchar | Yes |  | 수신 학생 전화번호 |
| message | varchar | Yes |  | 초대 메시지 |
| status | varchar | No | `PENDING` | 초대 상태 |
| created_by_user_id | bigint | No |  | 초대한 사용자 ID |
| responded_by_user_id | bigint | Yes |  | 응답한 사용자 ID |
| responded_at | timestamp | Yes |  | 응답 일시 |
| created_at | timestamp | No | current timestamp | 생성 일시 |
| updated_at | timestamp | No | current timestamp | 수정 일시 |

### 인덱스

| 이름 | 컬럼 | 유형 | 설명 |
|---|---|---|---|
| idx_academy_student_invitations_academy_id | academy_id | Index | 학원별 초대 조회 |
| idx_academy_student_invitations_receiver_user_id | receiver_user_id | Index | 학생별 초대 조회 |
| idx_academy_student_invitations_status | status | Index | 상태별 초대 조회 |
| idx_academy_student_invitations_academy_receiver_status | academy_id, receiver_user_id, status | Index | 중복 초대 방지 및 상태 확인 |

---

## users
... (기존 내용 유지)
