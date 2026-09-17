package com.jini.indicator.config

import org.yaml.snakeyaml.Yaml

object YamlConfigParser {

    fun parse(context: android.content.Context, fileName: String = "indicator_config.yaml"): IndicatorConfig =
        try {
            context.assets.open(fileName).bufferedReader().use {
                parseFromString(it.readText())
            }
        } catch (e: Exception) {
            IndicatorConfig()
        }

    @Suppress("UNCHECKED_CAST")
    fun parseFromString(yaml: String): IndicatorConfig {
        val root = Yaml().load<Map<String, Any>>(yaml) ?: return IndicatorConfig()
        val ind = root["indicator"] as? Map<String, Any> ?: emptyMap()
        val timeout = (ind["timeout"] as? Number)?.toLong() ?: 30_000L
        val overlayMap = ind["overlay"] as? Map<String, Any> ?: emptyMap()
        val overlay = OverlayConfig(
            blur = overlayMap["blur"] as? Boolean ?: true,
            dimAlpha = (overlayMap["dim_alpha"] as? Number)?.toFloat() ?: 0.6f
        )
        val imageMap = ind["image"] as? Map<String, Any> ?: emptyMap()
        val image = ImageConfig(
            type = when (imageMap["type"] as? String) {
                "lottie" -> ImageType.LOTTIE
                "image" -> ImageType.IMAGE
                else -> ImageType.DEFAULT
            },
            file = imageMap["file"] as? String
        )
        val scopesMap = root["scopes"] as? Map<String, Any> ?: emptyMap()
        val scopes = ScopesConfig(
            include = parseRules(scopesMap["include"]),
            exclude = parseRules(scopesMap["exclude"])
        )
        return IndicatorConfig(timeout, overlay, image, scopes)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseRules(raw: Any?): List<ScopeRule> {
        val list = raw as? List<Map<String, Any>> ?: return emptyList()
        return list.mapNotNull { entry ->
            when {
                entry["package"] != null ->
                    ScopeRule.PackageRule(entry["package"] as String)
                entry["class"] != null -> {
                    val methods = (entry["methods"] as? List<String>) ?: emptyList()
                    ScopeRule.ClassRule(entry["class"] as String, methods)
                }
                else -> null
            }
        }
    }
}
