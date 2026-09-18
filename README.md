# ProcessingIndicatorSDK for AOP

![minSdk](https://img.shields.io/badge/minSdk-24-brightgreen)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF?logo=kotlin)
![AGP](https://img.shields.io/badge/AGP-8.0%2B-02569B?logo=android)
![License](https://img.shields.io/badge/license-MIT-blue)

기존 소스 수정 없이 YAML 설정만으로 네트워크 호출 시 로딩 인디케이터를 자동 제어하는 Android SDK입니다.

---

## Quick Start

### 1. `settings.gradle.kts` — 플러그인 빌드 포함

```kotlin
pluginManagement {
    includeBuild("indicator-plugin")
    repositories {
        google(); mavenCentral(); gradlePluginPortal()
    }
}
```

### 2. `app/build.gradle.kts` — 플러그인 및 의존성 추가

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.jini.indicator")
}

dependencies {
    implementation("com.jini:processing-indicator-aop:1.0.0")
}
```

### 3. `src/main/assets/indicator_config.yaml` — 설정 파일 생성

```yaml
indicator:
  timeout: 10000
  overlay:
    blur: false
    dim_alpha: 0.5
  image:
    type: default  # default | lottie
scopes:
  include:
    - package: com.example.app.network
```

이것으로 끝입니다. 앱을 빌드하면 `indicator_config.yaml`에 매칭되는 네트워크 호출 전후로 인디케이터가 자동 표시됩니다.

---

## YAML 설정 레퍼런스

### `indicator` 블록

| 키 | 타입 | 기본값 | 설명 |
|----|------|--------|------|
| `timeout` | Long (ms) | `30000` | 인디케이터 자동 해제 타임아웃 |
| `overlay.blur` | Boolean | `true` | 배경 블러 처리 (API 31+ 전용) |
| `overlay.dim_alpha` | Float (0.0–1.0) | `0.6` | 배경 딤 투명도 |
| `image.type` | String | `default` | 인디케이터 타입: `default` \| `image` \| `lottie` |
| `image.file` | String | `null` | `image`/`lottie` 타입일 때 assets 경로 |

### `scopes` 블록

```yaml
scopes:
  include:
    - package: com.example.app.network      # 패키지 내 모든 클래스
    - class: com.example.app.ApiService     # 특정 클래스 전체 메서드
    - class: com.example.app.UserRepository
      methods:                              # 특정 메서드만 지정
        - fetchUser
        - updateProfile
  exclude:
    - class: com.example.app.LoggingService # 제외할 클래스
```

| 키 | 설명 |
|----|------|
| `include[].package` | 해당 패키지 하위 모든 클래스에 인디케이터 적용 |
| `include[].class` | 특정 클래스 전체(또는 `methods` 지정 시 해당 메서드만) 적용 |
| `include[].methods` | 클래스 내 적용 메서드 목록 (생략 시 전체) |
| `exclude[].class` | 인디케이터 제외 클래스 |

**전체 설정 예시:**

```yaml
indicator:
  timeout: 15000
  overlay:
    blur: true
    dim_alpha: 0.7
  image:
    type: lottie
    file: assets/loading.json
scopes:
  include:
    - package: com.example.feature
    - class: com.example.HomeActivity
    - class: com.example.ApiService
      methods:
        - fetchUser
  exclude:
    - class: com.example.LoggingService
```

---

## 동작 원리

### 컴파일 타임 — ASM 바이트코드 주입

`com.jini.indicator` Gradle 플러그인이 빌드 시 AGP Transform API를 통해 바이트코드를 분석합니다.

- **OkHttp**: `OkHttpClient.Builder.addInterceptor()`에 `IndicatorOkHttpInterceptor` 자동 주입
- **HttpURLConnection**: `openConnection()` 호출부를 `IndicatorUrlConnectionWrapper`로 래핑

`indicator_config.yaml`의 `scopes` 규칙에 매칭되는 클래스·메서드에만 주입이 적용됩니다.

### 런타임 — 자동 초기화 및 In-App Overlay

| 컴포넌트 | 역할 |
|----------|------|
| `IndicatorContentProvider` | `Application.onCreate()` 이전에 SDK 자동 초기화 (매니페스트 등록 불필요) |
| `ActivityLifecycleCallbacks` | 현재 포그라운드 Activity를 추적하여 Overlay 표시 기준 결정 |
| In-App Overlay | Window에 직접 뷰를 추가하는 방식 (SYSTEM_ALERT_WINDOW 권한 불필요) |

### 동시 호출 추적 — AtomicInteger Depth Counter

```
네트워크 호출 시작  →  depth.incrementAndGet()  →  depth > 0 이면 인디케이터 표시
네트워크 호출 완료  →  depth.decrementAndGet()  →  depth == 0 이면 인디케이터 숨김
```

여러 네트워크 요청이 동시에 발생해도 마지막 요청이 완료될 때까지 인디케이터가 유지됩니다.

---

## 요구사항

| 항목 | 최소 버전 |
|------|----------|
| Android minSdk | 24 (Android 7.0) |
| Kotlin | 2.0+ |
| Android Gradle Plugin | 8.0+ |
| Java | 17 |

---

## 알려진 제한사항

| 이슈 | 설명 |
|------|------|
| `scopes.exclude` 미구현 | exclude 규칙이 현재 동작하지 않습니다 ([#1](../../issues/1)) |
| `overlay.blur`는 API 31+ 전용 | Android 11(API 31) 미만에서는 blur가 무시되고 dim만 적용됩니다 ([#4](../../issues/4)) |

---

## 라이선스

```
MIT License

Copyright (c) 2026 Jini

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
