# Ringdu DB Schema

이 문서는 현재 코드와 예정 설계를 기준으로 한 DB/저장소 스키마 개요다. 실제 운영 스키마는 migration 도구 도입 후 별도 관리한다.

## 설계 원칙

`User`는 로그인 계정 정보만 관리한다. 학원명, 학원주소, 학원코드, 학원전화번호는 `users`에 넣지 않는다. 학원 정보는 `academies`, 학원 구성원 관계는 `academy_members`, 학생 관계는 `students`, `student_guardians`, `academy_students`로 분리한다.

## Current Tables

### users

현재 `User` Entity 기준.

| 컬럼 | 설명 |
|---|---|
| id | PK |
| email | 로그인 이메일, unique |
| password | BCrypt hash, 소셜 계정은 null 가능 |
| name | 사용자 이름 |
| phone | 전화번호 |
| role | ADMIN, OWNER, DESK, TEACHER, PARENT, STUDENT |
| status | ACTIVE, INACTIVE |
| provider | LOCAL, KAKAO, GOOGLE, NAVER |
| provider_id | 소셜 provider 사용자 ID |
| created_at | BaseEntity |
| updated_at | BaseEntity |

인덱스/제약:

- `uk_users_email`
- `idx_users_provider_provider_id`

### refresh_tokens

현재 `RefreshToken` Entity 기준. Redis 전환 전 상태 저장소다.

| 컬럼 | 설명 |
|---|---|
| id | PK |
| user_id | users FK |
| token_hash | Refresh Token SHA-256 hash, unique |
| expires_at | 만료 시각 |
| revoked | 폐기 여부 |
| rotated_at | rotation 또는 폐기 시각 |
| created_at | BaseEntity |
| updated_at | BaseEntity |

## Planned Tables

### academies

학원 기본 정보. 학원명, 주소, 코드, 전화번호를 관리한다.

### academy_members

학원과 `OWNER`, `DESK`, `TEACHER` 계정의 관계를 관리한다.

### students

학생 프로필을 관리한다. 학생 로그인 계정이 있을 수 있으나 `User`와 학생 프로필은 분리한다.

### student_guardians

학부모와 학생의 관계를 관리한다. 한 학부모는 여러 자녀와 연결될 수 있다.

### academy_students

학원과 학생의 관계를 관리한다. 학생이 여러 학원 또는 반에 속할 가능성을 열어둔다.

### classes

반 정보를 관리한다.

### schedules

반, 선생님, 학생 기준 시간표를 관리한다.

### attendances

학생과 수업/반 기준 출석 기록을 관리한다. 상태 후보는 `PRESENT`, `ABSENT`, `LATE`, `EARLY_LEAVE`, `EXCUSED`다.

### homeworks

숙제 정보와 담당 반/학생 배정을 관리한다.

### invoices

학생 기준 청구서를 관리한다.

### payments

청구서의 납부 상태와 결제/수납 기록을 관리한다.

## Planned Redis Storage

Refresh Token 상태 저장소는 Redis로 전환 예정이다.

- key: refresh token hash 또는 token id 기반 key
- value: user id, token status, issued metadata
- TTL: Refresh Token 만료 시간과 동일
- 원문 Refresh Token 저장 금지
