# 실행 계획: 학원 시간표 API

## 관련 이슈

- 미정

## 목표

프론트 시간표 UI 구조에 맞춰 학원이 강의실과 수업을 `요일 + 강의실 + 시간` 기준으로 관리할 수 있는 백엔드 API를 구현한다.

## 작업 범위

포함하는 작업:

- 강의실 관리 API
- 수업 시간표 API
- 수강 학생 추가/삭제 API
- 시간 겹침, 학원 소속, 권한 검증
- 관련 테스트와 문서 갱신

포함하지 않는 작업:

- 프론트 연동
- 출석, 숙제, 청구서 도메인 구현
- 반복 일정, 드래그앤드롭, 외부 캘린더 연동

## 관련 문서

- `AGENTS.md`
- `docs/WORKFLOW.md`
- `docs/DESIGN.md`
- `docs/SECURITY.md`
- `docs/product-specs/domain-roadmap.md`
- `docs/generated/db-schema.md`

## 구현 순서

1. 시간표 도메인 Entity, enum, Repository를 추가한다.
2. 강의실/수업/수강 학생 DTO와 Service를 구현한다.
3. `/api/academies/me/...` Controller를 추가하고 ACADEMY 권한을 적용한다.
4. 시간 검증, 겹침 검증, 학원 소속 검증 테스트를 추가한다.
5. 도메인/DB/설계 문서를 갱신한다.
6. `./gradlew test`로 검증한다.

## 검증 방법

- [x] `./gradlew test`
- [x] API 권한/소속 검증 테스트
- [x] 시간 겹침 검증 테스트

## 위험 요소

- 학원 소속 검증 누락 시 다른 학원 시간표 접근이 가능해질 수 있다.
- 시간 겹침 조건이 틀리면 같은 강의실에 중복 수업이 생성될 수 있다.

## 롤백 방법

이번 브랜치에서 추가한 schedule 패키지, 관련 ErrorCode, 문서 변경, 테스트 변경을 되돌린다.

## 완료 기준

- [x] 작업 범위가 완료되었다.
- [x] 테스트를 완료했다.
- [x] 필요한 문서를 갱신했다.
- [x] 남은 후속 조치가 있으면 기록했다.

## 검증 결과

- `./gradlew test` 통과

## 후속 조치

- 프론트 PR #16의 mock state를 이번 API 계약에 연결한다.
