package com.swara.app.data.model

data class SurvivalPackCatalog(
    val version: String = "",
    val lastUpdated: String = "",
    val scope: String = "",
    val packs: List<SurvivalPackGuide> = emptyList()
)

data class SurvivalPackGuide(
    val category: EmergencyCategory = EmergencyCategory.OTHER,
    val title: String = "",
    val sourceLabel: String = "",
    val sourceUrls: List<String> = emptyList(),
    val version: String = "",
    val lastUpdated: String = "",
    val scope: String = "",
    val quickHelp: List<String> = emptyList(),
    val detailedSteps: List<String> = emptyList(),
    val doNot: List<String> = emptyList(),
    val kit: List<String> = emptyList(),
    val addedModules: List<GuideModule> = emptyList()
)

data class GuideModule(
    val id: String = "",
    val category: EmergencyCategory = EmergencyCategory.OTHER,
    val title: String = "",
    val summary: String = "",
    val sourceName: String = "",
    val sourceUrl: String = "",
    val version: String = "",
    val quickHelp: List<String> = emptyList(),
    val detailedSteps: List<String> = emptyList(),
    val doNot: List<String> = emptyList(),
    val whenToGetHelp: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

enum class GuideDownloadMode {
    SIMULATED,
    REMOTE
}

data class GuideCatalogItem(
    val id: String = "",
    val title: String = "",
    val category: EmergencyCategory = EmergencyCategory.OTHER,
    val summary: String = "",
    val version: String = "",
    val sourceName: String = "",
    val sourceUrl: String = "",
    val downloadMode: GuideDownloadMode = GuideDownloadMode.SIMULATED,
    val payloadUrl: String? = null,
    val checksum: String? = null,
    val guideModule: GuideModule = GuideModule(),
    val comingSoon: Boolean = false
)
