# Ringdu Plans

이 문서는 Ringdu 백엔드의 현재 개발 상태와 다음 우선순위를 관리한다. 실제 구현 상태는 현재 코드 기준으로 판단한다.

## Done

- Health Check API: `/api/health`
- Common Foundation
- `ApiResponse`
- `ErrorCode`
- `BusinessException`
- `GlobalExceptionHandler`
- `BaseEntity`
- `Role` enum
- `User` entity와 `UserRepository`
- 일반 회원가입 API: `/api/auth/signup`
- 일반 회원가입 권한 제한: `TEACHER`, `PARENT`, `STUDENT` 허용, `ADMIN`, `OWNER`, `DESK` 차단
- BCrypt 기반 비밀번호 암호화

## In Progress

- Login/JWT 인증 흐름
- Access Token body 반환
- Refresh Token `HttpOnly` Cookie 반환
- Refresh Token hash 저장 및 rotation
- `/api/auth/me`
- `/api/auth/logout`

현재 작업 트리에는 위 인증 기능 코드와 테스트가 존재한다. Redis 기반 저장소 전환은 아직 예정이다.

## Planned

1. Redis 기반 Refresh Token Rotation으로 저장소 전환
2. ADMIN이 OWNER 계정을 생성하거나 권한을 부여하는 API
3. `Academy` 도메인
4. `AcademyMember` 도메인
5. Teacher/Parent/Student 연결 구조
6. Class 관리
7. Schedule 관리
8. Attendance 관리
9. Homework 관리
10. Invoice/Payment 관리
11. Notice 관리
12. Community/홍보 기능

## MVP 제외

- 실제 PG 결제 연동
- 카카오 알림톡
- 푸시 알림
- 실시간 채팅
- AI 기능
- 고도화 통계
- QR/NFC/자동 출결 기기 연동
- 실제 KAKAO/GOOGLE/NAVER OAuth 연동
- 복잡한 멀티테넌트 과금 구조
