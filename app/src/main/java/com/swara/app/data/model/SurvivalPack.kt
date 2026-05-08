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
    val kit: List<String> = emptyList()
)
