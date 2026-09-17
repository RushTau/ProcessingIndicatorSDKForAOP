package com.jini.indicator.config

data class IndicatorConfig(
    val timeout: Long = 30_000L,
    val overlay: OverlayConfig = OverlayConfig(),
    val image: ImageConfig = ImageConfig(),
    val scopes: ScopesConfig = ScopesConfig()
)

data class OverlayConfig(val blur: Boolean = true, val dimAlpha: Float = 0.6f)

enum class ImageType { DEFAULT, IMAGE, LOTTIE }

data class ImageConfig(val type: ImageType = ImageType.DEFAULT, val file: String? = null)

data class ScopesConfig(
    val include: List<ScopeRule> = emptyList(),
    val exclude: List<ScopeRule> = emptyList()
)

sealed class ScopeRule {
    data class PackageRule(val name: String) : ScopeRule()
    data class ClassRule(val name: String, val methods: List<String> = emptyList()) : ScopeRule()
}
