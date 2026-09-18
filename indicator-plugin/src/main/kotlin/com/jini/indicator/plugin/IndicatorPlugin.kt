package com.jini.indicator.plugin

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class IndicatorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.logger.info("[IndicatorPlugin] apply() called on ${project.name}")

        val androidComponents = project.extensions
            .findByType(AndroidComponentsExtension::class.java)
        if (androidComponents == null) {
            project.logger.info("[IndicatorPlugin] AndroidComponentsExtension NOT found")
            return
        }
        project.logger.info("[IndicatorPlugin] AndroidComponentsExtension found, registering instrumentation")

        androidComponents.onVariants { variant ->
            variant.instrumentation.transformClassesWith(
                IndicatorClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) {}
            variant.instrumentation.setAsmFramesComputationMode(
                FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
            )
        }

        project.afterEvaluate {
            generateProguardRules(project)
        }
    }

    private fun generateProguardRules(project: Project) {
        val (includes, excludes) = CompileTimeYamlReader.readScopes(project)
        val rulesContent = ProguardRuleGenerator.generate(includes, excludes)

        val rulesFile = File(
            project.layout.buildDirectory.asFile.get(),
            "generated/indicator/indicator_rules.pro"
        )
        rulesFile.parentFile.mkdirs()
        rulesFile.writeText(rulesContent)

        project.extensions.findByType(BaseExtension::class.java)?.let { android ->
            android.defaultConfig.proguardFile(rulesFile)
        }
    }
}
