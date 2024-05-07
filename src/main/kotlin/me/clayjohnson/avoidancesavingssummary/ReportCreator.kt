package me.clayjohnson.avoidancesavingssummary

import java.nio.file.Path

class CsvCreator(private val csvFile: Path, private val develocityUrl: String) {
    fun createReport(builds: List<BuildInfo>) {
        csvFile.toFile().apply {
            parentFile.mkdirs()
            bufferedWriter().use { writer ->
                writer.write("Build Scan, Build Location, Task Avoidance Savings (Up-to-date) (ms), Task Avoidance Savings (Local Build Cache) (ms), Task Avoidance Savings (Remote Build Cache) (ms), Transform Avoidance Savings (Up-to-date) (ms), Transform Avoidance Savings (Local Build Cache) (ms), Transform Avoidance Savings (Remote Build Cache) (ms)")
                writer.newLine()

                builds.forEach {
                    writer.write("${develocityUrl}/s/${it.id}, ${it.location}, ${it.taskAvoidanceSavings.fromUpToDate}, ${it.taskAvoidanceSavings.fromLocalCache}, ${it.taskAvoidanceSavings.fromRemoteCache}, ${it.transformAvoidanceSavings.fromUpToDate}, ${it.transformAvoidanceSavings.fromLocalCache}, ${it.transformAvoidanceSavings.fromRemoteCache}")
                    writer.newLine()
                }
            }
        }
    }
}