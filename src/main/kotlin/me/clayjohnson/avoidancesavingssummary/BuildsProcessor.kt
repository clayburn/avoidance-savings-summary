package me.clayjohnson.avoidancesavingssummary

import com.gradle.develocity.api.BuildsApi
import com.gradle.develocity.api.model.Build
import com.gradle.develocity.api.model.BuildModelName
import com.gradle.develocity.api.model.BuildsQuery
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.function.Consumer

class BuildsProcessor constructor(
    private val buildsApi: BuildsApi,
    private val maxBuildsPerQuery: Int,
    private val maxWaitSeconds: Int,
) {
    private val logger = KotlinLogging.logger {}

    fun process(fromInstant: Instant) : List<Build> {
        val builds = mutableListOf<Build>()
        var fromApplicator = Consumer {
            buildsQuery: BuildsQuery -> buildsQuery.fromInstant(fromInstant.toEpochMilli())
        }

        while (true) {
            val query = BuildsQuery().apply {
                reverse = false
                maxWaitSecs = maxWaitSeconds
                maxBuilds = maxBuildsPerQuery
                models = mutableListOf(BuildModelName.GRADLE_ATTRIBUTES, BuildModelName.GRADLE_BUILD_CACHE_PERFORMANCE)
                query = "buildTool:gradle"
            }
            fromApplicator.accept(query)

            logger.info { "Querying next $maxBuildsPerQuery builds (fromInstant=${query.fromInstant}, fromBuild=${query.fromBuild})" }
            val result = buildsApi.getBuilds(query)
            logger.info { "Received ${result.size} builds" }

            if (result.isEmpty()) {
                break
            }

            builds.addAll(result)
            fromApplicator = Consumer {
                buildsQuery: BuildsQuery -> buildsQuery.fromBuild(result.last().id)
            }
        }

        return builds.toList()
    }
}