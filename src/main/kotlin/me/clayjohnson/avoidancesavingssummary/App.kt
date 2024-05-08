package me.clayjohnson.avoidancesavingssummary

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.gradle.develocity.api.BuildsApi
import com.gradle.develocity.api.client.ApiClient
import com.jakewharton.picnic.table
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Paths
import java.time.Instant

fun main(args: Array<String>) = App().main(args)

class App : CliktCommand() {
    private val apiKey by option(envvar = "API_KEY").required()
    private val develocityUrl by option(envvar = "DEVELOCITY_URL").required()
    private val csv by option(envvar = "CSV_FILE")
    private val days by option().int().default(7)
    private val minutes by option().int()
    private val maxBuildsPerQuery by option().int().default(100)
    private val maxWaitSeconds by option().int().default(3)

    override fun run() {
        val now = Instant.now()

        val buildsApi = BuildsApi().apply {
            apiClient = ApiClient().apply {
                basePath = develocityUrl
                setBearerToken(apiKey)
            }
        }

        val startInstant = if (minutes != null) now.minutesAgo(minutes!!) else now.daysAgo(days)

        val builds = BuildsProcessor(
            buildsApi = buildsApi,
            maxBuildsPerQuery = maxBuildsPerQuery,
            maxWaitSeconds = maxWaitSeconds,
        ).process(startInstant).map { BuildInfo(it) }

        csv?.let {
            CsvCreator(
                csvFile = Paths.get(it),
                develocityUrl = develocityUrl,
            ).createReport(builds)
        }

        val localBuilds = builds.filter { it.location == BuildLocation.LOCAL }
        val ciBuilds = builds.filter { it.location == BuildLocation.CI }

        println("# Local builds (${localBuilds.size})")
        printAvoidanceTable(localBuilds)

        println("")

        println("# CI builds (${ciBuilds.size})")
        printAvoidanceTable(ciBuilds)

        println("")

        println("${builds.count { it.location == BuildLocation.UNKNOWN }} of unknown location")
    }

    private fun printAvoidanceTable(builds: List<BuildInfo>) {
        println(table {
            cellStyle { border = true }
            header {
                row("",
                    "Avoidance Savings\nUp-to-date (ms)",
                    "Avoidance Savings\nLocal Build Cache (ms)",
                    "Avoidance Savings\nRemote Build Cache (ms)")
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

