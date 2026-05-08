# Auth API

## 회원가입

```http
POST /api/auth/signup
```

일반 회원가입은 이메일/비밀번호 기반 계정 생성만 처리한다. 소셜 로그인과 JWT 발급은 아직 구현하지 않는다.

### 요청

```json
{
  "email": "teacher@example.com",
  "password": "password123",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "role": "TEACHER"
}
```

일반 회원가입 가능 권한:

- `TEACHER`
- `PARENT`
- `STUDENT`

일반 회원가입 불가 권한:

- `ADMIN`
- `OWNER`
- `DESK`

### 응답

```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": 1,
    "email": "teacher@example.com",
    "name": "홍길동",
    "role": "TEACHER",
    "provider": "LOCAL"
  }
}
```

일반 회원가입 사용자는 `provider`가 `LOCAL`로 저장되고 `providerId`는 `null`로 저장된다.
