# EngTest 앱 소스 코드 최적화 및 리팩토링 검토

## 1. 데이터베이스 및 리소스 최적화 (SQLite / Room)

### 1.1 적용한 개선

- **인덱스 추가**
  - `Word`: `difficulty`, `word` 컬럼 인덱스 → 난이도별 조회·필터, `countByWord` 및 검색 성능 향상
  - `TestResult`: `testDateMillis` 인덱스 → `getResultsBetween` 기간 조회 성능 향상
  - 스키마 변경에 맞춰 **Migration 2→3** 추가, 기존 DB에도 인덱스 생성

- **반복 쿼리 제거 (N+1 방지)**
  - 기록 상세에서 `details` 파싱 후 단어별로 `getWordById` 호출하던 부분 제거
  - `WordDao.getWordsByIds(ids)` 추가 후, ID 목록 기준 **일괄 조회**로 변경

- **Cursor/연결 관리**
  - Room 사용으로 **raw SQLite Cursor 직접 사용 없음** → 연결/리소스는 Room이 관리
  - DAO는 suspend/Flow만 사용하므로 메모리 누수 가능성 낮음

### 1.2 Firebase

- 현재 프로젝트에는 **Firebase 미사용** (로컬 Room만 사용). 추후 Firestore/Realtime DB 도입 시:
  - 필요한 필드만 조회 (전체 문서 로드 지양)
  - 로컬 캐시/오프라인 우선 전략 검토

---

## 2. 코드 구조 및 가독성 (MVVM)

### 2.1 적용한 개선

- **공통 유틸 추출**
  - `TestResultDetailsParser`: 테스트 결과 `details` 문자열 파싱 로직을 **한 곳**에서 관리
  - `WordManageViewModel`, `RecordsViewModel`의 중복 파싱 제거

- **비즈니스 로직 위치**
  - 화면별 ViewModel이 도메인 로직(필터·정렬·집계) 담당
  - UI는 Compose에서 StateFlow 수집·바인딩만 수행 → 역할 분리 유지

### 2.2 추가 권장 사항 (선택)

- **Repository 도입**: DAO를 직접 쓰는 대신 `WordRepository`, `TestResultRepository`로 한 번 감싸면 테스트·교체가 쉬움
- **날짜 포맷**: `SimpleDateFormat` 사용처가 여러 곳이면 `DateFormatterUtil` 등으로 모아서 재사용
- **Idiomatic Kotlin**: `apply`/`also`/`let`으로 null 체크·빌더 패턴 정리 가능 (기존 코드도 무리 없이 읽을 수 있는 수준)

---

## 3. 메모리 및 성능

### 3.1 검토 결과

- **Context 참조**
  - ViewModel은 `EngTestApplication`만 보유 → **Application Context**로 메모리 누수 위험 낮음
  - `WordAssetLoader` 등은 Context를 인자로만 받고 저장하지 않음

- **리스트 (Compose)**
  - RecyclerView 미사용, **LazyColumn** 사용
  - `items(..., key = { it.word.id })` 로 **key** 지정되어 있어 스크롤·갱신 시 효율적
  - Compose에서는 DiffUtil 대신 **key** 기반으로 동일 효과 유지

### 3.2 유지할 점

- 단어/기록 목록은 Flow + stateIn으로 **필요 시에만** 갱신되도록 구성됨

---

## 4. 비동기 처리 (Coroutine / Dispatcher)

### 4.1 적용한 개선

- **DB/파일 I/O에 Dispatchers.IO 명시**
  - `WordTestViewModel` init: 단어 로드 시 `withContext(Dispatchers.IO)`
  - `WordTestViewModel.saveResultAndShowResultScreen`: 결과 저장 시 `withContext(Dispatchers.IO)`
  - `WordManageViewModel`: `loadInitialWords`, `deleteWord`, `updateWord`, `addWordIfNew` 내 DB 호출을 `withContext(Dispatchers.IO)` 로 래핑
  - `RecordsViewModel.setSelectedResult`: 상세 단어 목록 로드 시 `withContext(Dispatchers.IO)`

- **의도**
  - Room suspend는 main-safe하지만, **무거운 작업은 IO 디스패처에서 실행**한다는 것을 코드에 명시
  - 메인 스레드 블로킹 가능성 감소, 유지보수 시 의도 파악 용이

### 4.2 기존 양호 사항

- `WordAssetLoader`는 이미 `withContext(Dispatchers.IO)` 사용
- `EngTestApplication.loadInitialWordsIfNeeded`는 `Dispatchers.IO` 스코프에서 실행

---

## 5. 변경 파일 요약

| 구분 | 파일 | 내용 |
|------|------|------|
| DB | `Word.kt` | `difficulty`, `word` 인덱스 |
| DB | `TestResult.kt` | `testDateMillis` 인덱스 |
| DB | `AppDatabase.kt` | `MIGRATION_2_3`, version 3 |
| DB | `EngTestApplication.kt` | `MIGRATION_2_3` 등록 |
| DAO | `WordDao.kt` | `getWordsByIds(ids)` 추가 |
| 유틸 | `TestResultDetailsParser.kt` | 신규 – details 파싱 공통화 |
| VM | `WordManageViewModel.kt` | 파서 사용, IO 디스패처, 공통 파싱 제거 |
| VM | `RecordsViewModel.kt` | 파서 + getWordsByIds, IO 디스패처 |
| VM | `WordTestViewModel.kt` | 단어 로드/결과 저장 시 IO 디스패처 |

---

## 6. 테스트 권장

- **DB 마이그레이션**: 기기/에뮬레이터에서 기존 DB(version 2)로 앱 실행 후, 업데이트하여 Migration 2→3 적용 여부 확인
- **기록 상세**: 테스트 결과 선택 → 상세 화면에서 단어 목록이 이전과 동일하게 로드되는지 확인
- **단어 관리**: 삭제·편집·추가 후 목록/통계가 정상 반영되는지 확인
