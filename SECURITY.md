# Ringdu Security

Ringdu 백엔드는 학원, 학생, 학부모, 청구 정보를 다루므로 인증/인가와 데이터 노출 방지를 기본 보안 요구사항으로 둔다.

## 현재 적용된 기준

- 비밀번호는 BCrypt로 암호화한다.
- 일반 회원가입은 `TEACHER`, `PARENT`, `STUDENT`만 허용한다.
- `ADMIN`, `OWNER`, `DESK`는 일반 회원가입으로 생성하지 않는다.
- Access Token은 응답 body로 반환하는 정책을 사용한다.
- Refresh Token은 `HttpOnly` Cookie로 내려주는 정책을 사용한다.
- Refresh Token 원문은 저장하지 않고 SHA-256 hash를 저장한다.
- Refresh Token rotation과 재사용 감지 흐름이 있다.
- JWT secret, token 만료 시간, CORS origin, Cookie secure 설정은 `application.yaml`에서 환경변수로 오버라이드한다.
- Entity를 직접 응답으로 노출하지 않고 DTO를 사용한다.

## 예정 또는 강화 필요

- Refresh Token 저장소를 Redis로 전환한다.
- Redis에는 Refresh Token hash와 TTL만 저장한다.
- 실제 KAKAO, GOOGLE, NAVER OAuth 연동을 구현한다.
- `Academy`, `AcademyMember`, `StudentGuardian`, `AcademyStudent` 기반의 리소스 권한 검증을 구현한다.
- 운영 환경에서 `JWT_SECRET`은 secret manager 또는 환경변수로만 관리한다.

## Token/Cookie 정책

- Access Token: 응답 body 반환, 프론트엔드 JavaScript 메모리 저장 전제
- Refresh Token: `HttpOnly` Cookie 저장
- 운영 HTTPS: `Secure=true`
- Cross-site 요청이 필요한 경우 `SameSite=None; Secure` 조합을 검토한다.
- refresh endpoint는 Cookie 기반 요청이므로 CORS `allowCredentials=true`와 프론트 `credentials: "include"` 또는 `withCredentials: true` 설정이 필요하다.
- Cookie `Path`는 인증 API 범위로 제한하는 것을 우선 검토한다.

## XSS/CSRF 고려

Access Token을 localStorage에 저장하지 않는 이유는 XSS 피해 범위를 줄이기 위해서다. Refresh Token은 JavaScript에서 읽을 수 없도록 `HttpOnly` Cookie에 저장한다.

Refresh Token이 Cookie로 전달되므로 CSRF 위험을 검토해야 한다. SameSite 정책, CORS origin 제한, 상태 변경 API의 인증 헤더 요구 여부를 함께 설계한다.

## 권한 원칙

- `ADMIN`: 플랫폼 운영 범위에서 학원과 원장 계정을 관리한다.
- `OWNER`: 소속 학원 전체를 관리한다.
- `DESK`: 소속 학원의 운영 보조 업무를 처리한다.
- `TEACHER`: 담당 반과 담당 학생 중심으로 접근한다.
- `PARENT`: 연결된 자녀 정보만 조회한다.
- `STUDENT`: 본인 정보만 조회한다.

다른 학원, 다른 학생, 다른 학부모 데이터 접근은 반드시 차단한다.
