# Academy Schedule Design

## 1. 목적

학원 시간표를 `요일 + 강의실 + 시간` 기준으로 안정적으로 관리하기 위한 백엔드 구조를 정의한다.

## 2. 목표

- 요일은 탭/선택값으로 사용한다.
- 선택된 요일의 시간표에서 열은 강의실, 행은 시간이다.
- 수업은 특정 요일, 강의실, 시작/종료 시간에 배치된다.
- 같은 학원, 같은 요일, 같은 강의실에서 시간이 겹치는 활성 수업은 허용하지 않는다.

## 3. 비목표

- 반복 일정 생성
- 드래그앤드롭 편집
- 출석, 숙제, 청구서 구현
- 외부 캘린더 연동

## 4. 제안 구조

```txt
academy_classrooms
→ 학원별 강의실

academy_classes
→ dayOfWeek + classroomId + startTime + endTime 기준 수업

academy_class_students
→ 수업별 수강 학생
```

API는 학원 계정 기준으로 `/api/academies/me/...` 아래에 둔다.

```txt
GET /api/academies/me/classrooms
POST /api/academies/me/classrooms
PUT /api/academies/me/classrooms/{classroomId}
DELETE /api/academies/me/classrooms/{classroomId}

GET /api/academies/me/classes
POST /api/academies/me/classes
GET /api/academies/me/classes/{classId}
PUT /api/academies/me/classes/{classId}
DELETE /api/academies/me/classes/{classId}

POST /api/academies/me/classes/{classId}/students
DELETE /api/academies/me/classes/{classId}/students/{studentProfileId}
```

## 5. 권한과 보안

- 모든 API는 인증이 필요하다.
- 이번 범위에서는 `ACADEMY`만 강의실과 수업을 관리할 수 있다.
- 조회/수정/삭제는 항상 현재 로그인한 ACADEMY의 `academyId`로 소속을 검증한다.
- 다른 학원의 `classroomId`, `classId`, `studentProfileId` 접근은 차단한다.
- 담당 선생님은 해당 학원에 `ACTIVE` 상태로 연결된 `AcademyMember`만 지정할 수 있다.

## 6. 예외 상황

- `startTime >= endTime`이면 실패한다.
- 같은 `academyId + classroomId + dayOfWeek`에서 `기존.startTime < 새.endTime AND 새.startTime < 기존.endTime`이면 실패한다.
- 이미 추가된 학생을 같은 수업에 다시 추가하면 중복 오류를 반환한다.

## 7. 검증 방법

- `./gradlew test`
- ACADEMY 권한 검증
- 학원 소속 검증
- 시간 겹침 검증
- 수강 학생 추가와 상세 조회 검증

## 8. 결정 사항

- 강의실과 수업 삭제는 물리 삭제 대신 `INACTIVE` 비활성화를 기본으로 한다.
- 반복 일정은 도메인을 복잡하게 만들기 때문에 MVP 범위에서 제외한다.
- 출석, 숙제, 청구서는 후속 도메인에서 `academy_classes.id`를 기준으로 연결한다.
