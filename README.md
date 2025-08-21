# 🧩 LogMate API Server
![License](https://img.shields.io/badge/license-Apache--2.0-green.svg)
![Java](https://img.shields.io/badge/java-17%2B-blue.svg)
![Build](https://img.shields.io/badge/build-Gradle-success.svg)
> 사용자 인증, 팀/대시보드 관리, 외부 연동을 제공하는 중앙 API 서버

**LogMate API Server**는 LogMate 시스템의 **중앙 관리 컴포넌트**로, 사용자 인증·인가, 팀 및 대시보드 관리, 외부 연동(Webhook) 기능을 제공하는 핵심 백엔드 서버입니다.
**MySQL**을 통해 사용자 및 팀/대시보드 데이터를 저장 · 조회 · 수정하고, Frontend와 **REST API**로 연결되어 사용자 요청을 처리합니다. 
Agent(Tail Mate)가 API 서버로부터 설정(Configuration)과 인증 정보를 Pulling하여 동작하도록 지원합니다.

---
## 📝 Technology Stack

| Category            | Technology                               |
|---------------------|------------------------------------------|
| Language            | Java 17                                  |
| Framework           | Spring Boot 3.3.13                       |
| Databases           | MySQL                                    |
| Authentication      | JWT, Spring Security                     |
| Development Tools   | Lombok, Querydsl                         |
| API Documentation   | (도입 예정: Swagger UI )                  |
| Deployment          | AWS EC2 수동 배포  (CICD 도입 예정)       | 


---
## 🔑 Key Features

### 1. 사용자 인증 및 접근 관리
- 이메일/비밀번호 기반 회원가입 및 로그인 (JWT 토큰 반환)
- 로그아웃(토큰 무효화), 회원 탈퇴 처리
- 사용자 정보 조회/수정 (마이페이지 기능)

### 2. 팀 & 워크스페이스 관리
- 팀 생성, 수정, 초대 URL 발급
- 팀 멤버 권한 수정 및 접근 제어
- 사용자가 속한 팀 목록 조회

### 3. 대시보드 관리
- 특정 팀 내 대시보드 생성/수정/조회
- 로그 경로, 전송 주소 등 설정값 관리

### 4. 알림(예정) & 외부 연동
- 사용자별 알림 목록 조회, 읽음 상태 업데이트, 페이지네이션 (예정)
- Webhook URL 등록 (예: Slack, Discord)
- 이벤트 발생 시 외부 알림 전송 및 실패 재시도 로직

### 5. Agent 설정 관리 (Pulling 방식)
- Agent가 API 서버로부터 설정(Configuration)을 **Pulling** 방식으로 요청해 수신
- 웹 UI에서 대시보드/파싱 룰/필터링 룰을 수정하면, Agent가 API 서버에서 최신 설정을 주기적으로 가져옴
- Agent는 별도의 Push 없이 최소한의 정보(Agent ID)만으로 동작 가능
- 보안 검증된 요청에 대해서만 설정값 반환

---
## 📂 패키지 구조
```
/src/java/com/logmate/
│
├── agentConfig/                # Agent 설정 관리 (Pulling 방식 지원)
│   ├── controller/             # Agent 설정 API
│   ├── dto/                    # 요청/응답 DTO
│   ├── model/                  # JPA 엔티티
│   ├── repository/             # DB 연동
│   └── service/                # 비즈니스 로직
│
├── auth/                       # 인증/인가
│   ├── dto/                    # 인증 관련 DTO
│   ├── util/                   # JWT 유틸
│   └── JwtAuthenticationFilter # JWT 인증 필터
│
├── config/                     # 전역 보안/설정
│   └── SecurityConfig          # Spring Security 설정
│
├── dashboard/                  # 대시보드 관리
│   ├── controller/             # 대시보드 API
│   ├── dto/                    # 요청/응답 DTO
│   ├── model/                  # 엔티티
│   ├── repository/             # DB 연동
│   └── service/                # 비즈니스 로직
│
├── global/                     # 전역 공통 모듈
│   ├── BaseErrorResponse       # 에러 응답 포맷
│   ├── BaseResponse            # 공통 응답 포맷
│   └── GlobalExceptionHandler  # 예외 처리 핸들러
│
├── team/                       # 팀 관리
│   ├── controller/             # 팀 API
│   ├── dto/                    # 팀 관련 DTO
│   ├── model/                  # Team, TeamMember 엔티티
│   ├── repository/             # Team, TeamMember Repository
│   └── service/                # 팀 비즈니스 로직
│
├── user/                       # 사용자 관리 (마이페이지 등)
│   ├── controller/             # 사용자 API
│   ├── dto/                    # 사용자 관련 DTO
│   ├── model/                  # 엔티티
│   ├── repository/             # DB 연동
│   └── service/                # 비즈니스 로직
│
└── webhook/                    # 외부 알림(Webhook) 관리
    ├── controller/             # Webhook API
    ├── dto/                    # 요청/응답 DTO
    ├── model/                  # 엔티티
    ├── repository/             # DB 연동
    └── service/                # Webhook 처리 로직
```
## 📦 API 명세서
[명세서 노션 링크](https://rural-column-222.notion.site/API-255d02fced5a802582f3c7e557485ce2?source=copy_link)
---
## 📄 오픈소스 라이선스

본 프로젝트는 아래의 오픈소스 라이브러리를 사용합니다:

- **Spring Boot / Spring Security** - Apache License 2.0
- **Hibernate(JPA)** - LGPL
- **Lombok** - MIT License
- **MySQL Connector/J** - GPL v2 (FOSS License Exception)
- **SnakeYAML** - Apache License 2.0
- **SLF4J Simple** - MIT License

각 라이브러리는 해당 라이선스에 따라 사용됩니다.

---

## 📄 라이선스
본 프로젝트는 **Apache License 2.0** 에 따라 라이선스가 부여됩니다.
자세한 내용은 [LICENSE](./LICENSE) 파일을 참조하세요.

---

## 🙏 기여 가이드
- PR 생성은 [pull_request_template.md](.github/pull_request_template.md) 문서를 참고해 주세요.
- Issue 생성은 [issue_report.md](.github/ISSUE_TEMPLATE/issue_report.md) 문서를 참고해 주세요.
- API 구조와 기능 정의는 추후 [Wiki]에서 확인하실 수 있습니다.

---

## 📲 연락처
email: jjjjyy020704@naver.com

---

