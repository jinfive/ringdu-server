# Ringdu Workflow

Ringdu 백엔드 작업은 이슈 기반 브랜치 흐름을 따른다.

## Branches

- `main`: 배포 기준 브랜치
- `develop`: 개발 통합 브랜치
- `feat/*`: 기능 개발
- `fix/*`: 버그 수정
- `docs/*`: 문서 작업
- `refactor/*`: 리팩토링

## 작업 순서

1. GitHub Issue를 만든다.
2. `develop`에서 작업 브랜치를 만든다.
3. 기능 단위로 작게 구현한다.
4. 관련 테스트와 문서를 갱신한다.
5. `./gradlew clean test`를 실행한다.
6. Conventional Commit 형식으로 커밋한다.
7. 원격 브랜치에 push하고 `develop` 대상으로 PR을 만든다.
8. PR merge 후 로컬과 원격 기능 브랜치를 삭제한다.
9. 후속 작업은 새 이슈와 새 브랜치에서 진행한다.

## Commit Examples

```text
feat: add user signup api
feat: add jwt login api
fix: resolve security config issue
docs: add project documentation index
test: add auth service test
refactor: align package structure
```

## 작업 전 확인

- 현재 브랜치와 변경 파일을 확인한다.
- 기존 사용자 변경을 되돌리지 않는다.
- Java, Spring Boot, DB 설정 변경은 요청 범위 안에서만 수행한다.
- 보안/인증/DB 변경은 테스트와 문서 갱신을 함께 진행한다.
