# 🧪 Rick and Morty App — MVVM Clean Architecture (Kotlin)

>A modern Android application built using MVVM Clean Architecture, Jetpack libraries, and Hilt dependency injection.
It consumes the Rick and Morty API
to display characters and episodes using offline caching and pagination.

# 🧩 Architecture Overview

>The app follows Clean Architecture and the MVVM (Model–View–ViewModel) pattern with a clear separation of concerns between layers.

```text
┌─────────────────────────────────────────────────────────────────────┐
│                         Presentation Layer (UI)                     │
│  ├── Activities / Compose Screens                                   │
│  ├── ViewModels (State management & user interaction)               │
│  └── Hilt injection for dependencies                                │
├─────────────────────────────────────────────────────────────────────┤
│                         Domain Layer                                │
│  ├── Entities (pure Kotlin models)                                  │
│  ├── Repository interfaces                                          │
│  └── UseCases (business logic)                                      │
├─────────────────────────────────────────────────────────────────────┤
│                         Data Layer                                  │
│  ├── Repository implementations                                     │
│  ├── Local sources (Room database)                                  │
│  ├── Remote sources (Retrofit API calls)                            │
│  └── Paging & synchronization                                       │
└─────────────────────────────────────────────────────────────────────┘
```

Each layer depends only on the layer below it and communicates via interfaces, ensuring testability and maintainability.

# 🧱 Tech Stack
- Layer	Technology
- UI / Presentation	Jetpack Compose (or XML), ViewModel, LiveData / Flow
- Domain	Kotlin coroutines, UseCases, Repository interfaces
- Data	Room (offline caching), Retrofit (network), Paging 3 (infinite scrolling)
- DI	Hilt
- Networking	OkHttp, Retrofit, Gson
- Other	Coroutines, Flow, Result wrappers, SafeCall utilities


# 🧠 Key Components
- ViewModel (Presentation Logic)

Each screen (e.g., Episodes, Characters) has its own ViewModel that:
Exposes UI state via StateFlow or LiveData.
Invokes repository methods to fetch or update data.
Handles loading and error states.

- Repository Layer

Repositories bridge Domain and Data layers.
They expose abstracted methods like getEpisodesStream() or getCharacter(id) without UI knowing where data comes from (network or database).

- Remote Layer

All API calls are centralized in a Retrofit interface: RnMApiService

- Local Layer (Room)

Room provides offline caching and serves as a single source of truth for UI.

- Paging 3 + RemoteMediator

Paging ensures seamless loading of data from both network and local cache.
EpisodesRemoteMediator coordinates between network and database:

- Dependency Injection (Hilt)

All major components are injected via Hilt:
NetworkModule — provides Retrofit and OkHttp
RemoteDataSourceModule — binds interface to Retrofit implementation
AppModule — provides Room database, DAOs, and repositories

# 📂 Project Structure
```text
com.anael.rickandmorty
│
├── data
│   ├── local/          # Room entities, DAOs, converters
│   ├── remote/         # Retrofit API + data source abstraction
│   ├── paging/         # Paging 3 + RemoteMediator
│   ├── mapper/         # DTO ↔ Entity ↔ Domain mapping
│   ├── repository/     # Repository implementations
│   └── utils/          # Helpers (safeCall, etc.)
│
├── domain
│   ├── model/          # Domain models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Optional business logic
│
├── di/                 # Hilt modules
│
├── infrastructure/
│   └── work/           # Background sync with WorkManager
│
└── presentation/
├── viewmodel/      # ViewModels
├── compose/        # UI components (or XML)
├── navigation/     # NavHost setup
└── activities/     # Main entry points
```