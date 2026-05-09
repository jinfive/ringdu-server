# Ringdu Tech Debt Tracker

현재 코드와 문서 기준으로 남아 있는 기술 부채를 추적한다.

| 항목 | 상태 | 영향 | 처리 방향 |
|---|---|---|---|
| Redis Refresh Token 저장소 미구현 | Open | Refresh Token 상태 저장이 DB Entity에 묶여 있음 | Redis hash + TTL 기반 저장소로 전환 |
| 실제 OAuth 미구현 | Open | `AuthProvider` 확장 값은 있으나 provider 연동 없음 | KAKAO/GOOGLE/NAVER를 별도 이슈로 구현 |
| Academy/AcademyMember 미구현 | Open | OWNER/DESK/TEACHER의 학원 소속 권한 검증 불가 | Academy 도메인부터 구현 |
| Student 관계 도메인 미구현 | Open | PARENT/STUDENT의 자녀/본인 리소스 제한 불가 | Student, StudentGuardian, AcademyStudent 구현 |
| 운영 환경변수 분리 강화 필요 | Open | JWT secret, DB, cookie 설정이 로컬 기본값을 가짐 | 운영 배포 설정과 secret 관리 문서화 |
| 테스트 커버리지 확대 필요 | Open | 인증 이후 도메인 권한 테스트 부족 | Service/Controller 보안 테스트 추가 |
| 문서 링크 정합성 | Open | 문서 구조 변경 시 링크 불일치 가능 | 문서 추가/이동 시 AGENTS.md와 색인 갱신 |

기술 부채는 기능 구현 중 임시 결정이 생길 때마다 이 문서에 추가한다.
