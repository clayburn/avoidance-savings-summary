package me.clayjohnson.avoidancesavingssummary

import com.gradle.develocity.api.model.Build

enum class BuildLocation {
    LOCAL,
    CI,
    UNKNOWN
}

data class BuildInfo(
    val id: String,
    val location: BuildLocation,
    val taskAvoidanceSavings: AvoidanceSavings,
    val transformAvoidanceSavings: AvoidanceSavings,
) {
    constructor(build: Build) : this(
        id = build.id,
        location = build.location(),
        taskAvoidanceSavings = build.taskAvoidanceSavings(),
        transformAvoidanceSavings = build.transformAvoidanceSavings(),
    )
}

data class AvoidanceSavings(
    val fromUpToDate: Long?,
    val fromLocalCache: Long?,
    val fromRemoteCache: Long?,
)

private fun Build.location(): BuildLocation {
    val tags = this.models?.gradleAttributes?.model?.tags?.mapNotNull { it.uppercase() }
    return when {
        tags?.contains("CI") == true -> BuildLocation.CI
        tags?.contains("LOCAL") == true -> BuildLocation.LOCAL
        else -> BuildLocation.UNKNOWN
        }
}

private fun Build.taskAvoidanceSavings(): AvoidanceSavings {
    val taskAvoidanceSavingsSummary = this.models?.gradleBuildCachePerformance?.model?.taskAvoidanceSavingsSummary
    return AvoidanceSavings(
        fromUpToDate = taskAvoidanceSavingsSummary?.upToDate,
        fromLocalCache = taskAvoidanceSavingsSummary?.localBuildCache,
        fromRemoteCache = taskAvoidanceSavingsSummary?.remoteBuildCache,
    )
}

private fun Build.transformAvoidanceSavings(): AvoidanceSavings {
    val taskAvoidanceSavingsSummary = this.taskAvoidanceSavings()
    val workUnitAvoidanceSavingsSummary = this.models?.gradleBuildCachePerformance?.model?.workUnitAvoidanceSavingsSummary
    return AvoidanceSavings(
        fromUpToDate = workUnitAvoidanceSavingsSummary?.upToDate?.let { workUnitAvoidanceSavings ->
            taskAvoidanceSavingsSummary.fromUpToDate?.let { taskAvoidanceSavings ->
                workUnitAvoidanceSavings - taskAvoidanceSavings
            }
        },
        fromLocalCache = workUnitAvoidanceSavingsSummary?.localBuildCache?.let { workUnitAvoidanceSavings ->
            taskAvoidanceSavingsSummary.fromLocalCache?.let { taskAvoidanceSavings ->
                workUnitAvoidanceSavings - taskAvoidanceSavings
            }
        },
        fromRemoteCache = workUnitAvoidanceSavingsSummary?.remoteBuildCache?.let { workUnitAvoidanceSavings ->
            taskAvoidanceSavingsSummary.fromRemoteCache?.let { taskAvoidanceSavings ->
                workUnitAvoidanceSavings - taskAvoidanceSavings
            }
        },
    )
}
