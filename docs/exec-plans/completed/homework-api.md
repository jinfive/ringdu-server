# 실행 계획: 숙제 API 및 역할별 조회 연결

## 1. 목표

숙제 상태를 `DONE`, `NOT_DONE` 두 가지로 제한하고, 선생님 등록·검사와 학생·학부모·학원 조회 API를 구현한다.

## 2. 배경

프론트 PR #29의 숙제 화면은 mock/localStorage 기반이다. 실제 수업과 학생 관계를 기준으로 저장하고 역할별 권한을 검증하는 백엔드 API가 필요하다.

## 3. 작업 범위

포함되는 작업:

- 숙제와 학생별 숙제 상태 엔티티 및 저장소
- 담당 수업 선생님의 숙제 생성, 조회, 상태 변경, 삭제
- 학생 본인, 연결된 자녀, 학원 소속 학생의 숙제 조회
- 역할 및 소유권 검증 테스트
- 프론트 API 연결을 위한 응답 계약

포함하지 않는 작업:

- 파일 업로드
- 학생 온라인 제출
- 청구, 출석, 상담 기능 변경

## 4. 관련 문서

- `AGENTS.md`
- `ARCHITECTURE.md`
- `docs/SECURITY.md`
- `docs/QUALITY_SCORE.md`
- `docs/product-specs/role-based-ux-plan.md`

## 5. 관련 파일

- `src/main/java/com/ringdu/server/homework/**`
- `src/main/java/com/ringdu/server/global/exception/ErrorCode.java`
- `src/test/java/com/ringdu/server/homework/controller/HomeworkControllerTest.java`
- `docs/generated/db-schema.md`

## 6. 구현 순서

1. 숙제 상태와 엔티티, 저장소를 정의한다.
2. 선생님 담당 수업과 ACTIVE 수강생 검증을 포함한 서비스를 구현한다.
3. 선생님, 학생, 학부모, 학원 컨트롤러를 추가한다.
4. 권한과 상태 정책 통합 테스트를 작성한다.
5. 전체 테스트와 실제 서버 부팅을 검증한다.

## 7. 검증 방법

- `./gradlew test`
- `./gradlew bootRun`
- 담당 외 수업, 수업 외 학생, 연결되지 않은 자녀, 타 학원 학생 접근 테스트
- `DONE`, `NOT_DONE` 외 상태 요청 거부 테스트

## 8. 위험 요소

- 학생 계정은 학원별 `StudentProfile`이 여러 개일 수 있다.
- 학부모 관계는 사용자 관계이므로 자녀의 프로필 소유 userId를 함께 검증해야 한다.
- 삭제된 숙제는 모든 조회에서 제외해야 한다.

## 9. 롤백 방법

숙제 도메인 커밋을 되돌리고 Hibernate가 생성한 `homeworks`, `homework_students` 테이블을 개발 DB에서 제거한다.

## 10. 완료 기준

- [x] 숙제 상태가 `DONE`, `NOT_DONE`만 허용된다.
- [x] 선생님 담당 수업 권한이 검증된다.
- [x] 역할별 조회 범위가 검증된다.
- [x] 전체 테스트와 서버 부팅이 성공한다.
