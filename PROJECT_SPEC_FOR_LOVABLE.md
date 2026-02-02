# PotenUp Community - Project Specification for Frontend Development

> **⚠️ Lovable AI를 위한 핵심 지침 (Project Rebuild Manifesto)**
>
> 이 프로젝트는 기존 프론트엔드의 **페이지 흐름(Routing Flow)만 유지**하고, 내부 구현과 UI/UX를 **완전히 새롭게 고도화(Re-engineering)**하는 작업입니다.
>
> 1.  **No Legacy Code**: 기존 프로젝트의 스타일링이나 상태 관리 코드를 복사하지 마세요. 오직 "어떤 페이지가 필요한지"만 참고하십시오.
> 2.  **High-End UX**: 단순한 기능 구현을 넘어, **Toss/Vercel 스타일의 높은 사용성**을 목표로 합니다.
>     - 모든 데이터 로딩에 **스켈레톤 UI** 적용.
>     - 버튼 클릭, 좋아요 등의 액션에 **즉각적인 피드백(Optimistic Update, Toast)** 필수.
>     - 모바일 환경에서의 완벽한 반응형 동작.
> 3.  **Modern Stack**: Next.js 14+ (App Router), Tailwind CSS, TanStack Query, Shadcn/UI(권장)를 사용하여 프로덕션 레벨의 코드를 작성하세요.
>
> ---

이 문서는 **Lovable AI**를 사용하여 프론트엔드를 개발하기 위한 상세 백엔드 명세서입니다. 제공된 Swagger(OpenAPI) YAML 파일과 함께 이 문서를 활용하여 프로젝트의 맥락과 비즈니스 로직을 파악하시기 바랍니다.

---

## 1. 프로젝트 개요 (Project Overview)
**PotenUp Community**는 개발자 지망생 및 현업자를 위한 커뮤니티 플랫폼입니다. 주요 기능으로는 지식 공유 게시판, 스터디 모집, 실시간 알림, 활동 통계 대시보드가 포함됩니다.

- **Backend Base URL**: `/api/v1`

- **인증 방식**: JWT (Header: `Authorization: Bearer {token}`) 및 Google OAuth 기반
- **대상 사용자**: 일반 유저(USER), 관리자(ADMIN)

## 2. 주요 도메인 및 API 연동 가이드

### 🔐 인증 및 유저 (Auth & User) - **중요: Cookie 기반 인증**
이 프로젝트는 **HttpOnly Cookie**를 사용하여 JWT Access Token과 Refresh Token을 관리합니다. 프론트엔드에서 토큰을 LocalStorage에 저장할 필요가 없습니다.

- **로그인 (`POST /api/v1/auth/login`)**:
    - **Request**: Google OAuth를 통해 발급받은 `idToken`을 Body에 담아 보냅니다.
    - **Response**: 
        - **Body**: 유저의 Role 정보 (`{ "role": "USER" }`)
        - **Headers**: `Set-Cookie` 헤더를 통해 `Authorization`(Access Token)과 `RefreshToken` 쿠키가 자동으로 설정됩니다.
    - **Frontend Action**: 로그인 요청 시 `credentials: 'include'` 옵션을 사용해야 하며, 이후 모든 API 요청에도 이 옵션을 필수로 포함해야 쿠키가 서버로 전송됩니다.

- **회원 가입**:
    - `POST /api/v1/users/signup`: 회원가입 API.
    - idToken을 우선적으로 login엔드 포인트에 송신후 404 반환시 리다이렉트.
    - idToken값을 유지시켜 추가적인 개인정보를 입력받는 회원가입 과정을 진행해야 함.

- **자동 토큰 갱신 (Silent Refresh)**:
    - 백엔드의 `JwtAuthenticationFilter`가 Access Token 만료 시, 쿠키에 있는 Refresh Token을 확인하여 자동으로 토큰을 재발급(Rotation)하고 `Set-Cookie` 헤더로 갱신해줍니다.
    - 프론트엔드는 별도의 리프레시 로직을 구현할 필요 없이, API 호출 실패 시(401 등) 로그인 페이지로 이동하는 처리만 하면 됩니다.

- **로그아웃 (`DELETE /api/v1/auth/logout`)**:
    - 서버에서 쿠키를 만료시키는 `Set-Cookie` 헤더를 반환합니다.

- **인증 상태 확인 (`GET /api/v1/auth/me`)**:
    - 페이지 새로고침 시 유저의 로그인 상태(유효성)와 Role을 확인하는 API입니다. 앱 초기 로딩 시 이 API를 호출하여 로그인 여부를 판단하세요.

- **기타 API**:
    - `POST /api/v1/users/signup`: 회원가입 API.
    - `GET /api/v1/users/myInfo`: 내 정보 조회.


### 📝 커뮤니티 게시판 (Post)
- **게시글 분류 (Topic Enum)**:
    - `NOTICE`: 공지사항 (주로 관리자 작성)
    - `KNOWLEDGE`: 지식줍줍 (기술 공유, 아티클 등)
    - `EMPLOYMENT_TIP`: 취업팁 (면접 후기, 자소서 팁 등)
    - `SMALL_TALK`: 자유 게시판 (일상, 잡담)

- **게시글 작성 및 마크다운 에디터 (Velog 스타일)**:
    - **UI 요구사항**: Velog와 유사한 화면 구성 (좌측: 마크다운 에디터, 우측: 실시간 프리뷰).
    - **이미지 업로드 로직 (Draft System)**:
        1.  **Draft ID 생성**: 유저가 글쓰기 페이지에 진입하면 프론트엔드에서 `UUID v4`를 생성합니다 (`draftId`).
        2.  **이미지 첨부**: 에디터에 이미지를 붙여넣거나 업로드하면 `POST /api/v1/files/upload`를 호출합니다.
            - `FormData`: `file` (이미지 파일), `draftId` (1번에서 생성한 UUID).
            - **Response**: 업로드된 이미지 URL을 반환.
            - 에디터는 반환된 URL을 `![alt](url)` 형태로 마크다운 본문에 삽입해야 합니다.
        3.  **게시글 등록 (`POST /api/v1/posts`)**:
            - Request Body:
                ```json
                {
                  "draftId": "generated-uuid-v4",
                  "topic": "KNOWLEDGE",
                  "title": "제목",
                  "content": "마크다운 본문...",
                  "highlightType": null
                }
                ```
            - 백엔드는 `draftId`를 이용해 임시 저장된 이미지들을 영구 저장소로 이동시키고 게시글과 연결합니다.

- **주요 기능**:
    - 목록 조회 (`GET /api/v1/posts/summary?topic=...`): 무한 스크롤 또는 페이지네이션으로 구현.

    - 상세 조회 (`GET /api/v1/posts/{id}`): 게시글 본문 및 작성자 정보 포함.
    - 내 활동: 내가 작성한 글(`GET /api/v1/posts/me`), 내가 좋아요한 글(`GET /api/v1/posts/me/liked`) 목록 제공.

### 📚 스터디 모집 (Study)
- **특이사항**: 스터디 개설 시 관리자의 승인이 필요한 프로세스를 가질 수 있습니다.
- **주요 기능**:
    - 스터디 검색 (`GET /api/v1/studies`): 기술 스택이나 모집 상태에 따른 필터링 필요.
    - 상세 보기 (`GET /api/v1/studies/{id}`): 모집 내용, 기간, 인원 확인.
    - 관리자 전용: `PATCH /api/v1/studies/{id}/approve` 및 `reject`를 통한 승인/거절 처리.

### 💬 댓글 및 리액션 (Comment & Reaction)
- **댓글**: 게시글 하단에 계층형 또는 리스트형으로 댓글을 렌더링합니다. (`GET /api/v1/comments/{postId}`)
- **리액션(좋아요)**: 
    - `POST /api/v1/reactions`: 좋아요 추가.
    - `DELETE /api/v1/reactions`: 좋아요 취소.
    - 게시글과 댓글 모두 동일한 리액션 API를 사용하며 `targetType`으로 구분합니다.

### 🔔 알림 (Notification)
- **실시간성**: 유저가 로그인하면 읽지 않은 알림 개수(`GET /api/v1/notifications/unread-count`)를 표시합니다.
- **목록**: 알림 센터에서 내역을 확인하고 전체 읽음(`PATCH /api/v1/notifications/read-all`) 처리가 가능해야 합니다.

### 📊 대시보드 (Dashboard)
- `GET /api/v1/dashboard/overview`: 현재 커뮤니티의 활성 지표(전체 게시글 수, 유저 수 등) 또는 개인의 활동 요약을 시각화합니다.

## 3. 프론트엔드 구현 전략 제안

### UI/UX 디자인 방향
- **스타일**: 깔끔하고 전문적인 느낌의 UI (예: Toss, Vercel 스타일).
- **컴포넌트**: 
    - 카드형 레이아웃 (게시글 목록, 스터디 목록)
    - 상단 탭 네비게이션 (토픽별 게시판 이동)
    - 플로팅 액션 버튼 (글쓰기 버튼)

### 상태 관리 및 데이터 패칭
- **Axios 설정 (필수)**: 
    - 모든 요청에 쿠키가 포함되도록 `withCredentials: true` (또는 `credentials: 'include'`) 옵션을 기본 설정으로 적용해야 합니다.
    - 별도의 `Authorization` 헤더 주입 로직(`Bearer ...`)은 **필요하지 않습니다**. 브라우저가 쿠키를 자동으로 처리합니다.
- **React Query**: 서버 상태 관리, 캐싱, 무한 스크롤 구현에 적극 활용해주세요.

### 보안 및 인증 처리
- **로그인 유지**: 앱 시작 시 `GET /api/v1/auth/me`를 호출하여 세션이 유효한지 확인하고, 유저 정보를 전역 상태(Context 또는 Store)에 저장하세요.
- **Google Login**: 프론트엔드에서 Google OAuth SDK(예: `@react-oauth/google`)를 사용하여 `idToken`을 발급받은 후, 백엔드 로그인 API로 전달하면 됩니다.

---

**Lovable에게 보내는 요청 예시:**
> "첨부한 Swagger 명세서와 PROJECT_SPEC_FOR_LOVABLE.md 파일을 바탕으로 PotenUp 커뮤니티 프론트엔드를 React와 Tailwind CSS로 구축해줘. 특히 게시판의 토픽 필터링 기능과 JWT 기반의 로그인 유지가 핵심이야."

---

## 4. 엔티티별 데이터 구조 및 요청 필드 상세 (Entity Data Structures)
프론트엔드 폼(Form) 구성 및 API 호출 시 Body 데이터 구조를 파악하는 데 참고하세요.

### 👤 회원가입 (`POST /api/v1/users/signup`)
*   **Request Body**:
    ```json
    {
      "idToken": "google_id_token_string",  // Google 로그인 후 받은 토큰
      "trackId": 1,                         // 선택한 트랙 ID
      "name": "홍길동",                      // 실명 (최대 10자)
      "phoneNumber": "01012345678",         // 전화번호
      "provider": "GOOGLE"                  // 가입 제공자
    }
    ```

### 📝 게시글 (`POST /api/v1/posts`, `PATCH /api/v1/posts/{id}`)
*   **Create Request**:
    ```json
    {
      "draftId": "uuid-v4",                 // 필수: 이미지 업로드 세션 ID
      "topic": "NOTICE",                    // NOTICE, KNOWLEDGE, EMPLOYMENT_TIP, SMALL_TALK
      "title": "게시글 제목",
      "content": "마크다운 내용...",
      "highlightType": null                 // 옵션 (ADMIN용)
    }
    ```
*   **Update Request**: `draftId`는 필수이며, 수정할 필드만 보냅니다.

### 📚 스터디 (`POST /api/v1/studies`, `PATCH /api/v1/studies/{id}`)
*   **Create Request**:
    ```json
    {
      "name": "스터디 이름",                // 필수 (2~50자)
      "description": "스터디 설명...",      // 필수 (최대 300자)
      "capacity": 5,                        // 최소 2명
      "budget": "FREE",                     // FREE, PAID 등 (BudgetType)
      "chatUrl": "https://open.kakao...",   // 오픈채팅방 링크 등
      "refUrl": "https://notion...",        // 참고 링크 (Optional)
      "tags": ["Spring", "Kotlin"]          // 최대 5개
    }
    ```
*   **Update Request**: 생성 시와 필드가 거의 동일하며 `scheduleId`가 추가로 요구될 수 있습니다.

### 💬 댓글 (`POST /api/v1/comments`, `PUT /api/v1/comments/{id}`)
*   **Create Request**:
    ```json
    {
      "postId": 10,                         // 게시글 ID
      "parentId": null,                     // 대댓글일 경우 부모 댓글 ID
      "content": "댓글 내용입니다.",
      "mentionUserIds": [2, 5]              // 멘션된 유저 ID 목록 (Optional)
    }
    ```

### 👍 리액션 (좋아요) (`POST/DELETE /api/v1/reactions`)
*   **Request Body**:
    ```json
    {
      "targetType": "POST",                 // "POST" 또는 "COMMENT"
      "targetId": 123,                      // 게시글 ID 또는 댓글 ID
      "reactionType": "LIKE"                // 현재는 "LIKE"만 사용
    }
    ```

### 🛤️ 트랙 (Admin) (`POST /api/v1/admin/tracks`, `PATCH ...`)
*   **Create Request**:
    ```json
    {
      "trackName": "Backend 3기",
      "startDate": "2025-03-01",
      "endDate": "2025-08-31"
    }
    ```

---

## 5. UX/UI 개선 및 품질 기준 (UX/UI Quality Standards)
기존 프로젝트의 UI/UX 단점을 보완하고, 완성도 높은 결과물을 만들기 위한 가이드라인입니다.

### 🎨 디자인 컨셉
- **Clean & Modern**: 복잡한 장식보다는 여백과 타이포그래피를 강조한 깔끔한 스타일 (예: Vercel, Toss, Linear 디자인 언어 참고).
- **반응형 (Responsive)**: 데스크탑뿐만 아니라 모바일에서도 완벽하게 동작하는 Grid/Flex 레이아웃 필수 적용.

### ⚡ 사용자 경험 (UX)
- **로딩 처리**: 데이터 로딩 시 단순 스피너 대신 **스켈레톤 UI (Skeleton UI)**를 적용하여 체감 속도를 높여주세요.
- **에러 핸들링**: API 호출 실패 시 사용자에게 친절한 토스트 메시지(Toast Notification)를 띄우고, 폼 에러는 인풋 필드 하단에 즉시 표시해주세요.
- **직관적인 네비게이션**:
    - GNB(Global Navigation Bar)는 항상 접근 가능하도록 상단 고정.
    - 현재 위치를 알 수 있는 Breadcrumb 또는 탭 활성화 표시.

### 🛠️ 개발 표준
- **컴포넌트 분리**: 페이지 단위로 코드를 뭉치지 말고, `PostCard`, `CommentItem` 등 재사용 가능한 단위로 컴포넌트를 분리해주세요.
- **상수 관리**: API URL, 메뉴 목록, 토픽 Enum 등은 별도의 상수 파일(`constants.ts`)로 분리하여 관리해주세요.

---

## 6. 프론트엔드 라우팅 및 폴더 구조 가이드 (Recommended Route Structure)
Next.js 14+ App Router를 기준으로 한 권장 폴더 구조입니다. URL을 통해 게시판의 토픽 상태를 관리하는 것이 핵심입니다.

### 📂 폴더 구조 (src/app)
- `(public)`
    - `/signin`: 로그인 페이지 (Google OAuth 버튼 포함)
    - `/signup`: 회원가입 페이지 (추가 정보 입력)
- `(authenticated)`: 로그인이 필요한 영역 (Layout에서 AuthGuard 처리)
    - `/(board)`: 메인 피드
        - `/`: 전체 글 목록
        - `/[topic]`: 토픽별 글 목록 (`/knowledge`, `/notice` 등)
    - `/studies`: 스터디 모집 목록 및 상세
    - `/post/write`: 새 글 작성 (마크다운 에디터)
    - `/mypage`: 내 정보 및 활동 내역
    - `/admin`: 관리자 전용 페이지 (트랙 관리, 스터디 승인)

### 🧩 주요 UI 컴포넌트 (Components)
기존 프로젝트의 좋은 패턴을 유지하며 다음 컴포넌트들을 구현해주세요.

- **AppShell (`/components/layout/AppShell`)**: 헤더(GNB)를 포함한 최상위 레이아웃 껍데기.
- **Aside (`/components/layout/Aside`)**: 데스크탑 뷰에서 좌측 사이드바 (프로필 요약, 주요 메뉴).
- **CategoryFilter**: 게시판 상단에서 토픽(공지, 지식, 잡담, 스터디 등)을 탭 형태로 전환하는 컴포넌트.
- **FloatingNav**: 모바일 뷰에서 하단에 고정되는 네비게이션 바.
- **MarkdownEditor**: `create` 모드와 `edit` 모드를 모두 지원하는 Velog 스타일 에디터.

### 📡 API 통신 패턴 (`/lib/api`)
- **Server Component**: `fetch`를 사용하여 쿠키를 자동으로 전달 (`{ cache: 'no-store' }` 등 옵션 활용).
- **Client Component**: `useEffect` 또는 `React Query`를 통해 데이터 패칭. `credentials: 'include'` 옵션 필수.

---

## 7. 핵심 구현 상세 및 지뢰 제거 가이드 (Critical Implementation Details)

### 🔐 7.1. 인증 및 회원가입 워크플로우 (Strict Auth Flow)
- **미가입 유저 처리**: `POST /api/v1/auth/login` 시 **404(Not Found)** 응답을 받으면 미가입 유저입니다.
    - 이때 사용한 `idToken`을 `Session Storage`에 임시 저장하고 `/signup` 페이지로 리다이렉트하세요.
    - 회원가입(`POST /api/v1/users/signup`) 성공 후에는 세션에 저장된 토큰을 반드시 삭제해야 합니다.
- **에러 인터셉터 (Global Error Handler)**:
    - **401(Unauthorized)**: 로그아웃 처리(Auth Store 초기화) 후 로그인 페이지로 강제 이동.
    - **400, 422, 500**: 서버 응답 Body의 에러 메시지를 추출하여 Toast UI로 즉시 노출.

### 🌐 7.2. 배포 환경 및 CORS/Cookie 대응
- **Next.js Rewrites (필수)**: 
    - 백엔드(ngrok)와 프론트엔드 도메인이 다르므로, 브라우저의 서드파티 쿠키 차단 정책을 피하기 위해 **프록시(Proxy)**를 사용합니다.
    - 모든 API 요청은 절대 경로가 아닌 **상대 경로(`/api/v1/...`)**로 보내야 합니다.
    - `next.config.ts` 설정 예시:
      ```typescript
      async rewrites() {
        return [{ source: '/api/:path*', destination: `${process.env.NEXT_PUBLIC_API_URL}/api/:path*` }]
      }
      ```
- **Credentials**: 모든 API 호출(Axios, Fetch, React Query)에 `credentials: 'include'` 설정이 기본으로 적용되어야 합니다.

### 📝 7.3. 마크다운 에디터 및 리소스 관리
- **Draft 시스템**: 게시글 수정(PATCH) 시에도 새로운 `draftId`를 생성하여 이미지 업로드 세션을 관리하는 것을 권장합니다.
- **에디터 이탈 방지**: 사용자가 글쓰기 중 페이지를 벗어나려 할 때, 작성 중인 내용이 있다면 `window.onbeforeunload` 또는 Next.js 라우터 이벤트를 통해 컨펌 창을 띄우세요.

### 📡 7.4. 데이터 페칭 전략 (TanStack Query)
- **목록 조회**: `GET /api/v1/posts/summary` 등은 `useInfiniteQuery`를 사용하여 무한 스크롤로 구현하세요.
- **낙관적 업데이트 (Optimistic Update)**: 좋아요(`Reaction`) 버튼 클릭 시 UI가 즉각 반응하도록 `useMutation`의 `onMutate` 로직을 적용하세요.
- **캐시 무효화**: 댓글 작성, 게시글 수정 등의 액션 발생 시 관련 쿼리 키를 `invalidateQueries` 하여 최신 데이터를 유지하세요.

---

## 9. 주요 페이지별 UI 구성요소 상세 (Page UI Specs)
기존 코드 분석을 통해 도출된, 각 페이지에 **반드시 포함되어야 할 필수 UI 요소**입니다.

### 📝 회원가입 페이지 (`/signup`)
- **컨텍스트**: 구글 로그인을 시도했으나 미가입 유저로 판명되어 리다이렉트된 상황입니다.
- **필수 요소**:
    - **안내 문구**: "구글 계정 인증이 완료되었습니다. 추가 정보를 입력해주세요."
    - **트랙 선택 (Select Box)**: 서버에서 트랙 목록(`GET /api/v1/tracks`)을 받아와 선택할 수 있게 해야 합니다. (예: Backend 3기, Frontend 4기 등)
    - **입력 폼**: 실명, 전화번호 입력 필드.
    - **디자인 팁**: 깔끔한 카드 형태의 레이아웃을 사용하고, 하단에 커뮤니티의 장점(트렌드, Q&A, 커리어)을 아이콘과 함께 나열하여 가입 유도율을 높이세요.

### 👤 마이페이지 (`/mypage`)
- **레이아웃**: 좌측에는 프로필 요약 카드, 우측에는 활동 내역 탭이 위치하는 2단 레이아웃(데스크탑 기준)을 권장합니다.
- **탭 구성 (Tabs)**:
    - **내 게시글 (`/mypage/post`)**: 내가 작성한 글 목록.
    - **내 댓글 (`/mypage/comment`)**: 내가 댓글 단 글 목록.
    - **좋아요 한 글 (`/mypage/like`)**: 내가 리액션(좋아요)을 남긴 글 목록.
- **프로필 카드**:
    - 사용자 이미지, 이름, 소속 트랙 표시.
    - '내 정보 수정' 버튼 (모달 또는 별도 페이지 이동).

### 🏠 메인 피드 및 게시판 (`/(board)`)
- **카테고리 필터**: 상단에 '전체', '공지사항', '지식', '취업팁', '잡담' 탭을 배치하여 URL Query Parameter(`?topic=...`)로 필터링합니다.
- **글쓰기 버튼**: 우측 하단 Floating Action Button (Mobile) 또는 상단 배너 영역 (Desktop)에 배치.
- **사이드바 (Desktop)**:
    - 로그인 상태: 내 프로필 요약, 알림 센터 진입점.
    - 비로그인 상태: 로그인 유도 버튼.



