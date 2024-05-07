package me.clayjohnson.avoidancesavingssummary

import com.gradle.develocity.api.BuildsApi
import com.gradle.develocity.api.model.Build
import com.gradle.develocity.api.model.BuildModelName
import com.gradle.develocity.api.model.BuildsQuery
import java.time.Instant
import java.util.function.Consumer

class BuildsProcessor(
    private val buildsApi: BuildsApi
) {
    fun process(fromInstant: Instant) : List<Build> {
        val builds = mutableListOf<Build>()
        var fromApplicator = Consumer {
            buildsQuery: BuildsQuery -> buildsQuery.fromInstant(fromInstant.toEpochMilli())
        }

        while (true) {
            val query = BuildsQuery().apply {
                reverse = false
                maxWaitSecs = 20
                models = mutableListOf(BuildModelName.GRADLE_ATTRIBUTES, BuildModelName.GRADLE_BUILD_CACHE_PERFORMANCE)
                query = "buildTool:gradle"
            }
            fromApplicator.accept(query)

            val result = buildsApi.getBuilds(query)

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