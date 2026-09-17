package com.jini.indicator.plugin

import org.gradle.api.Project
import org.yaml.snakeyaml.Yaml
import java.io.File

object CompileTimeYamlReader {

    @Suppress("UNCHECKED_CAST")
    fun readScopes(project: Project): Pair<List<Map<String, Any>>, List<Map<String, Any>>> {
        val yamlFile = File(project.projectDir, "src/main/assets/indicator_config.yaml")
        if (!yamlFile.exists()) return Pair(emptyList(), emptyList())

        val root = Yaml().load<Map<String, Any>>(yamlFile.readText())
            ?: return Pair(emptyList(), emptyList())

        val scopes = root["scopes"] as? Map<String, Any> ?: emptyMap()
        return Pair(
            scopes["include"] as? List<Map<String, Any>> ?: emptyList(),
            scopes["exclude"] as? List<Map<String, Any>> ?: emptyList()
        )
    }
}
