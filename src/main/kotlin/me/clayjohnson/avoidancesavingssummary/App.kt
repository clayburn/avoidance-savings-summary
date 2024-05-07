package me.clayjohnson.avoidancesavingssummary

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.gradle.develocity.api.BuildsApi
import com.gradle.develocity.api.client.ApiClient
import com.jakewharton.picnic.table
import java.nio.file.Paths
import java.time.Instant

fun main(args: Array<String>) = App().main(args)

class App : CliktCommand() {
    private val apiKey by option(envvar = "API_KEY").required()
    private val develocityUrl by option(envvar = "DEVELOCITY_URL").required()
    private val csv by option(envvar = "CSV_FILE")
    private val days by option().int().default(7)

    override fun run() {
        val now = Instant.now()

        val buildsApi = BuildsApi().apply {
            apiClient = ApiClient().apply {
                basePath = develocityUrl
                setBearerToken(apiKey)
            }
        }

        val builds = BuildsProcessor(
            buildsApi = buildsApi,
        ).process(now.daysAgo(days)).map { BuildInfo(it) }

        csv?.let {
            CsvCreator(
                csvFile = Paths.get(it),
                develocityUrl = develocityUrl,
            ).createReport(builds)
        }

        println(table {
            cellStyle { border = true }
            header {
                row("",
                    "Avoidance Savings (Up-to-date) (ms)",
                    "Avoidance Savings (Local Build Cache) (ms)",
                    "Avoidance Savings (Remote Build Cache) (ms)")
            }
            row("Tasks",
                builds.sumOf { it.taskAvoidanceSavings.fromUpToDate ?: 0 },
                builds.sumOf { it.taskAvoidanceSavings.fromLocalCache ?: 0 },
                builds.sumOf { it.taskAvoidanceSavings.fromRemoteCache ?: 0 })
            row("Transforms",
                builds.sumOf { it.transformAvoidanceSavings.fromUpToDate ?: 0 },
                builds.sumOf { it.transformAvoidanceSavings.fromLocalCache ?: 0 },
                builds.sumOf { it.transformAvoidanceSavings.fromRemoteCache ?: 0 })
        })
    }
}

