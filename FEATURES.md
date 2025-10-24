# 🧪 Rick & Morty App — Feature Implementation & Testing Overview

This document explains **how each functional requirement** is implemented in the project and **which tests** validate them.

---

## 1️⃣ List all episodes from the API

### 🧩 Implemented by
- `data/paging/EpisodesRemoteMediator.kt` + `EpisodesRemoteMediatorFactory.kt` — drives paging & network → DB sync.
- `data/repository/EpisodesRepositoryImpl.kt` — exposes `getEpisodesStream()` (Pager over Room + RemoteMediator).
- `presentation/compose/episodes/EpisodesScreen.kt` — collects the `PagingData` and renders the list.
- `data/remote/RnMApiService.kt` — service interface, wrapped by `RetrofitRnMApiRemoteDataSource`.

### 🧪 Tested by
- **Integration-style:** `test/.../data/paging/EpisodesRemoteMediatorTest.kt` — refresh/append, DB writes, and end-of-pagination.
- **Repository unit:** `test/.../data/repository/EpisodesRepositoryImplTest.kt` — `getEpisodesStream` smoke test and mapping.
- **Remote unit:** `test/.../data/remote/RnMApiRemoteDataSourceTest.kt` — success and server error cases.

---

## 2️⃣ Display episode name, air date (dd/MM/yyyy), and code

### 🧩 Implemented by
- `presentation/compose/episodes/EpisodeListItem.kt` — renders each row content.
- `presentation/utils/ComposeFormatDateTime.kt` — provides date formatting for the UI.
- `data/mapper/...` — maps DTO/Entity dates to domain/UI models.

### 🧪 Tested by
- **UI:** `androidTest/.../ui/EpisodesScreenTest.kt` — verifies fields and formatted dates.

---

## 3️⃣ Show text when user reaches the end of the list

### 🧩 Implemented by
- `EpisodesScreen.kt` — observes the `LoadState` from `LazyPagingItems` and shows an **“End of list”** footer when `endOfPaginationReached`.

### 🧪 Tested by
- **UI:** `androidTest/.../ui/EpisodesScreenTest.kt` — scrolls to end and asserts the end-of-list text.

---

## 4️⃣ Tap an episode → list all character IDs

### 🧩 Implemented by
- Navigation: `presentation/navigation/AppNavHost.kt` and `Screen.kt`.
- `presentation/compose/episodes/EpisodeDetailScreen.kt` — displays tapped episode’s character IDs.

### 🧪 Tested by
- **UI:** `androidTest/.../ui/EpisodeDetailsScreenTest.kt` — Checks the screen.

---

## 5️⃣ Tap a character ID → show full character details

### 🧩 Implemented by
- `presentation/viewmodel/CharacterViewModel.kt` — loads character via `CharacterRepository`.
- `presentation/compose/characters/CharacterDetailScreen.kt` — shows image and all fields.
- Domain model: `domain/model/CharacterRnM.kt` (+ `Origin`).
- Data layer: `data/repository/CharacterRepositoryImpl.kt` + mappers.

### 🧪 Tested by
- **UI:** `androidTest/.../ui/CharacterDetailScreenTest.kt` — verifies fields rendered.
- **Repository:** `test/.../data/repository/CharacterRepositoryImplTest.kt`.
- **Mapper:** `test/.../data/mapper/MapperTest.kt`.

---

## 6️⃣ Export character details to a local file

### 🧩 Implemented by
- Pure use case: `domain/usecase/BuildCharacterExportText.kt` — creates export text.
- `CharacterViewModel.exportCharacterInFile()` — builds text and emits `CharacterUiEvent.RequestExport(text, suggestedFileName)`.
- UI layer handles `RequestExport` event and writes via Android’s **SAF / DocumentProvider**.

### 🧪 Tested by
- **Unit:** `test/.../domain/usecase/BuildCharacterExportTextTest.kt` — verifies string output.
- **UI:** `androidTest/.../ui/CharacterDetailScreenTest.kt` — ensures export flow triggers and file event emitted.

---

## 7️⃣ Refresh list content in the background

### 🧩 Implemented by
- **WorkManager:** `infrastructure/work/EpisodesSyncWorker.kt` + `EpisodesSyncScheduler.kt`.
- **Hilt bindings:** `di/NetworkModule.kt`, `di/RemoteDataSourceModule.kt`, `di/RepositoryModule.kt`, etc.

### 🧪 Tested by
- **Worker:** `test/.../infrqstructure/work/EpisodesSyncWorkerTest.kt` — Robolectric test to ensure `EpisodesRepositoryImpl.syncEpisodes()` is invoked.

---

## 8️⃣ Persistence of API data

### 🧩 Implemented by
- **Room Database:**
    - `data/local/AppDatabase.kt`
    - `EpisodeDao.kt`, `EpisodeEntity.kt`, `EpisodeRemoteKey.kt`, `LastRefreshEntity.kt`
- Written and updated by `EpisodesRemoteMediator` and `EpisodesRepositoryImpl.syncEpisodes()`.

### 🧪 Tested by
- **DAO instrumentation:** `androidTest/.../data/local/EpisodeDaoTest.kt`.
- **Repository:** `test/.../data/repository/EpisodesRepositoryImplTest.kt` — happy/error paths.
- **Mediator:** `test/.../data/paging/EpisodesRemoteMediatorTest.kt` — verifies DB writes & key generation.

---

## 9️⃣ Pull-to-refresh (with persistence)

### 🧩 Implemented by
- `EpisodesScreen.kt` — integrates Compose’s `pullRefresh` to trigger `LAUNCH_INITIAL_REFRESH` on the Pager.

### 🧪 Tested by
- **UI:** `androidTest/.../ui/EpisodesScreenTest.kt` — swipe down → verifies refresh behavior.

---

## 🔟 Timestamp of last refresh

### 🧩 Implemented by
- Database: `LastRefreshEntity` + `LastRefreshDao`.
- Repository: `EpisodesRepositoryImpl.lastRefreshFlow` (`Flow<Long?>`).
- UI: `EpisodesScreen.kt` renders timestamp via `presentation/utils/ComposeFormatDateTime.kt`.

### 🧪 Tested by
- **Repository:** `test/.../data/repository/EpisodesRepositoryImplTest.kt` — ensures `lastDao.upsert` and flow wiring.
- **UI:** `androidTest/.../ui/EpisodesScreenTest.kt` — asserts timestamp is shown.

---

## 1️⃣1️⃣ Unit & UI Test Suite Overview

### 🧪 Unit Tests
| Category   | Test Class                        | Purpose                                                 |
|------------|-----------------------------------|---------------------------------------------------------|
| Remote     | `RnMApiRemoteDataSourceTest.kt`   | Retrofit + MockWebServer (success & 500)                |
| Repository | `EpisodesRepositoryImplTest.kt`   | Sync loop, paging snapshot                              |
| Repository | `CharacterRepositoryImplTest.kt`  | Character fetching logic                                |
| Mapping    | `MapperTest.kt`                   | DTO ↔ Entity ↔ Domain conversions                       |
| Paging     | `EpisodesRemoteMediatorTest.kt`   | In-memory Room + fake remote                            |
| Domain     | `BuildCharacterExportTextTest.kt` | Verifies export string format                           |
| Utilities  | `MainDispatcherRule.kt`           | Provides coroutine dispatcher for tests                 |
| Work       | `EpisodesSyncWorkerTest.kt`       | Verify that the worker calls the repo to syncEpisodes() |

---

### 📱 UI / Instrumentation Tests
| Category | Test Class | Purpose |
|-----------|-------------|----------|
| UI | `EpisodesScreenTest.kt` | Paging, refresh, end-of-list |
| UI | `EpisodeDetailsScreenTest.kt` | Navigation from list → details |
| UI | `CharacterDetailScreenTest.kt` | Full character view + export |
| DAO | `EpisodeDaoTest.kt` | Room integration test |
| DI / Hilt | `HiltTestRunner.kt` | Hilt setup for instrumented tests |
| Fakes / DI | `FakeEpisodesRepository.kt`, `FakeRepoEntryPoint.kt`, `TestEpisodesModule.kt` | Provide fake dependencies for controlled testing |

