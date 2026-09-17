# ProcessingIndicatorSDK for AOP — 설계 문서

**작성일**: 2026-09-17  
**작성자**: 팀장 지니  
**버전**: 1.0.0

---

## 1. 프로젝트 개요

### 목표
AOP(Aspect-Oriented Programming)를 활용하여 **기존 소스 코드 수정 없이** `assets/indicator_config.yaml` 설정만으로 네트워크 호출 시 전체화면 로딩 인디케이터를 자동 제어하는 Android 라이브러리 SDK.

### 핵심 원칙
- 앱 코드 무수정 (`build.gradle` 플러그인 추가 + YAML 파일만 허용)
- Kotlin 2.0 / K2 컴파일러 완전 호환
- Android minSdk 24 지원

---

## 2. 요건 정의

| # | 요건 | 결정 사항 |
|---|------|---------|
| 1 | 배포 형태 | 라이브러리 (AAR + Gradle Plugin) |
| 2 | 소스 변경 범위 | `build.gradle` 플러그인 추가만 허용 |
| 3 | 타겟 OS | Android minSdk 24 |
| 4 | 인디케이터 종류 | Lottie + 일반이미지(AnimationDrawable) + 기본 내장 스피너 |
| 5 | 네트워크 감지 대상 | OkHttp/Retrofit + HttpURLConnection |
| 6 | 스코프 설정 방식 | `assets/indicator_config.yaml` (include + exclude, package/class/method 레벨) |
| 7 | UI | 전체화면 블러(API 31+) / 반투명 오버레이(API 24–30) |
| 8 | SDK 초기화 | ContentProvider 자동 초기화 (코드 0줄) |
| 9 | 동시 호출 처리 | 마지막 완료 시 숨김 (AtomicInteger depth counter) |
| 10 | 타임아웃 | YAML 설정값, 기본 30,000ms |
| 11 | AOP 엔진 | 커스텀 Gradle Plugin + ASM 바이트코드 위빙 |
| 12 | ProGuard/R8 | Plugin이 YAML 기반 `-keep` 규칙 자동 생성 |
| 13 | 배포 | GitHub Packages `com.jini:processing-indicator-aop:1.0.0` |
| 14 | 테스트앱 | 동일 저장소 내 `indicator-test-app` 모듈 포함 |

---

## 3. 프로젝트 구조

```
ProcessingIndicatorSDKForAOP/
├── indicator-plugin/              # Gradle Plugin (Kotlin)
│   ├── src/main/kotlin/com/jini/indicator/plugin/
│   │   ├── IndicatorPlugin.kt             # Plugin 진입점, AGP 등록
│   │   ├── IndicatorAsmVisitorFactory.kt  # AGP Instrumentation API 연결
│   │   ├── AsmWeaver.kt                   # OkHttpClient / HttpURLConnection 주입
│   │   ├── YamlConfigReader.kt            # 컴파일타임 YAML 파싱
│   │   └── ProguardRuleGenerator.kt       # YAML 기반 -keep 규칙 자동 생성
│   └── build.gradle.kts
│
├── indicator-core/                # Runtime Library (AAR)
│   ├── src/main/
│   │   ├── AndroidManifest.xml            # ContentProvider 선언
│   │   └── kotlin/com/jini/indicator/
│   │       ├── init/
│   │       │   ├── IndicatorContentProvider.kt
│   │       │   └── IndicatorSDK.kt
│   │       ├── config/
│   │       │   ├── YamlConfigParser.kt
│   │       │   ├── IndicatorConfig.kt     # 데이터 클래스 (Config 전체)
│   │       │   └── ScopeConfig.kt         # 싱글톤, include/exclude 보유
│   │       ├── context/
│   │       │   └── IndicatorContext.kt    # depth counter + timeout
│   │       ├── network/
│   │       │   ├── IndicatorOkHttpInterceptor.kt
│   │       │   └── IndicatorUrlConnectionWrapper.kt
│   │       ├── scope/
│   │       │   ├── ScopeMatcher.kt        # 스택트레이스 → YAML 매칭
│   │       │   └── ScopeRule.kt           # sealed class (Package/Class/Method)
│   │       ├── overlay/
│   │       │   ├── IndicatorOverlayManager.kt
│   │       │   └── IndicatorOverlayView.kt
│   │       └── renderer/
│   │           ├── IndicatorRenderer.kt   # 인터페이스
│   │           ├── DefaultSpinnerRenderer.kt
│   │           ├── ImageRenderer.kt
│   │           └── LottieRenderer.kt
│   ├── consumer-rules.pro
│   └── build.gradle.kts
│
├── indicator-test-app/            # 데모 앱
│   ├── src/main/assets/
│   │   └── indicator_config.yaml
│   └── src/main/java/com/jini/testapp/
│       ├── MainActivity.kt
│       └── scenarios/
│           ├── OkHttpScenario.kt
│           ├── RetrofitScenario.kt
│           ├── HttpUrlScenario.kt
│           ├── ExcludeScenario.kt
│           └── TimeoutScenario.kt
│
└── settings.gradle.kts
```

---

## 4. 아키텍처 설계

### 4-1. 전체 데이터 흐름

```
[컴파일타임]
indicator_config.yaml
  → YamlConfigReader (Plugin)
  → AsmWeaver
      → OkHttpClient.Builder.build() 바이트코드에 IndicatorOkHttpInterceptor 주입
      → URL.openConnection() 호출 지점에 IndicatorUrlConnectionWrapper.wrap() 치환
  → ProguardRuleGenerator → indicator_generated_rules.pro

[런타임]
ContentProvider.onCreate()
  → YamlConfigParser → ScopeConfig 초기화
  → IndicatorOverlayManager 초기화

네트워크 호출 발생
  → IndicatorOkHttpInterceptor 또는 IndicatorUrlConnectionWrapper
  → ScopeMatcher.isInScope() — 스택트레이스 vs YAML 매칭
      → 매칭 O: IndicatorContext.acquire(callId, timeout) → Overlay 표시
      → 매칭 X: 그대로 통과

네트워크 호출 완료 (또는 timeout)
  → IndicatorContext.release(callId)
  → depth == 0이면 Overlay 숨김
```

---

### 4-2. Gradle Plugin (컴파일타임)

**AGP 연결 방식**: AGP 7.0+ `Instrumentation API` (`TransformAction` 대체)

```kotlin
androidComponents.onVariants { variant ->
    variant.instrumentation.transformClassesWith(
        IndicatorAsmVisitorFactory::class.java,
        InstrumentationScope.ALL
    ) { params -> params.configJson.set(yamlAsJson) }

    variant.instrumentation.setAsmFramesComputationMode(
        FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
    )
    variant.proguardFiles.add(generatedKeepRulesFile)
}
```

**ASM 변환 대상**

| 대상 | 변환 방식 | 위치 |
|------|---------|------|
| `okhttp3.OkHttpClient$Builder.build()` | 메서드 내부 주입 — `IndicatorOkHttpInterceptor` 추가 | 메서드 본문 앞 |
| `java.net.URL.openConnection()` | 호출 지점(call-site) 치환 — `IndicatorUrlConnectionWrapper.wrap()` | 전체 클래스 스캔 |

**ProGuard 자동 생성**

```
# YAML include → -keep 자동 생성
-keep class com.example.app.feature.** { *; }
-keep class com.example.app.HomeActivity { *; }
-keepclassmembers class com.example.app.ApiService {
    *** fetchUser(...);
}

# SDK 내부 보호
-keep class com.jini.indicator.** { *; }

# OkHttpClient.build() R8 인라이닝 방지
-keep class okhttp3.OkHttpClient$Builder {
    public okhttp3.OkHttpClient build();
}
```

---

### 4-3. IndicatorContext (Runtime)

```kotlin
object IndicatorContext {
    private val depth = AtomicInteger(0)
    private val timeoutJobs = ConcurrentHashMap<String, Job>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun acquire(callId: String, timeoutMs: Long) {
        if (depth.getAndIncrement() == 0) IndicatorOverlayManager.show()
        timeoutJobs[callId] = scope.launch {
            delay(timeoutMs)
            release(callId)
        }
    }

    fun release(callId: String) {
        timeoutJobs.remove(callId)?.cancel()
        val remaining = depth.decrementAndGet()
        if (remaining <= 0) {
            depth.set(0)
            IndicatorOverlayManager.hide()
        }
    }
}
```

---

### 4-4. YAML 설정 스키마

```yaml
indicator:
  timeout: 30000            # ms (기본값)
  overlay:
    blur: true              # API 31+: RenderEffect / API 24–30: dimming fallback
    dim_alpha: 0.6          # 반투명도 (0.0–1.0)
  image:
    type: lottie            # lottie | image | default
    file: assets/loading.json

scopes:
  include:
    - package: com.example.app.feature
    - class: com.example.app.HomeActivity
    - class: com.example.app.ApiService
      methods:
        - fetchUser
        - submitData
  exclude:
    - class: com.example.app.feature.LoggingService
    - class: com.example.app.AnalyticsService
      methods:
        - trackEvent
```

**매칭 우선순위**: method > class > package  
**exclude 우선**: 동일 레벨에서 exclude가 include보다 우선

---

### 4-5. Overlay UI

| API 레벨 | 블러 방식 |
|---------|---------|
| API 31+ | `RenderEffect.createBlurEffect()` |
| API 24–30 | 반투명 검정 오버레이 (`dim_alpha` 설정값) |

**Renderer 폴백 순서**: YAML 지정 → assets 파일 없으면 → DefaultSpinnerRenderer

---

### 4-6. 엣지케이스 처리

| 상황 | 처리 방법 |
|------|---------|
| OkHttpClient 인스턴스 여러 개 | `build()` 주입 시 중복 인터셉터 체크 |
| OkHttp 패키지 shading | Plugin 설정에서 타겟 클래스명 오버라이드 |
| R8 인라이닝 | `consumer-rules.pro`로 `build()` 메서드 보존 |
| 코루틴 스레드 전환 | `IndicatorUrlConnectionWrapper` thread-safe 구현 |
| Lottie 의존성 없음 | `runCatching`으로 `DefaultSpinnerRenderer` 폴백 |
| 음수 depth | `depth.set(0)` 클램프 |

---

## 5. 테스트 시나리오 (indicator-test-app)

| 시나리오 | 검증 항목 |
|---------|---------|
| 단일 OkHttp 호출 | 인디케이터 표시 → 완료 후 숨김 |
| 동시 3개 OkHttp 호출 | 마지막 완료 후 숨김 |
| Retrofit 호출 | OkHttp 인터셉터 경유 확인 |
| HttpURLConnection 호출 | Wrapper 경유 확인 |
| exclude 클래스 호출 | 인디케이터 미표시 |
| exclude 메서드 호출 | 특정 메서드만 제외 확인 |
| 타임아웃 (30s) | 자동 해제 확인 |
| Lottie 렌더러 | Lottie 애니메이션 표시 |
| Image 렌더러 | AnimationDrawable 표시 |
| Default 렌더러 | 기본 스피너 표시 |
| API 24 에뮬레이터 | dimming fallback 확인 |
| API 31+ 기기 | 블러 효과 확인 |

---

## 6. GitHub Packages 배포

### 아티팩트

| 아티팩트 | 좌표 |
|---------|------|
| Runtime Library | `com.jini:processing-indicator-aop:1.0.0` |
| Gradle Plugin | `com.jini:processing-indicator-aop-plugin:1.0.0` |
| Plugin ID | `com.jini.indicator` |

### 앱 적용 방법

```kotlin
// settings.gradle.kts
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/jini/ProcessingIndicatorSDKForAOP")
        credentials {
            username = providers.gradleProperty("gpr.user").orNull
            password = providers.gradleProperty("gpr.key").orNull
        }
    }
}

// app/build.gradle.kts
plugins {
    id("com.jini.indicator") version "1.0.0"
}
dependencies {
    implementation("com.jini:processing-indicator-aop:1.0.0")
    // 선택: Lottie 사용 시
    implementation("com.airbnb.android:lottie:6.x.x")
}
```

`assets/indicator_config.yaml` 파일 추가 → 완료.

---

## 7. 구현 순서 (고수준)

1. **Gradle Plugin 기반 구축** — AGP 연결, ASM OkHttp 주입, ProGuard 생성
2. **Runtime Core** — ContentProvider 초기화, YAML 파싱, ScopeConfig, IndicatorContext
3. **네트워크 인터셉터** — OkHttp Interceptor, HttpURLConnection Wrapper
4. **Overlay UI** — WindowManager 오버레이, 블러/dimming, Renderer 3종
5. **ScopeMatcher** — 스택트레이스 매칭 로직
6. **HttpURLConnection ASM 주입** — call-site 치환
7. **테스트앱** — 시나리오별 검증
8. **GitHub Packages 배포 설정**
