package com.swara.app.data.repo

import android.content.Context
import com.google.gson.Gson
import com.swara.app.data.model.EmergencyCategory
import com.swara.app.data.model.SurvivalPackCatalog
import com.swara.app.data.model.SurvivalPackGuide

class SurvivalPackRepository(
    context: Context,
    private val gson: Gson = Gson()
) {
    private val appContext = context.applicationContext

    private val catalog: SurvivalPackCatalog by lazy {
        appContext.assets.open(ASSET_NAME).bufferedReader().use { reader ->
            gson.fromJson(reader, SurvivalPackCatalog::class.java) ?: SurvivalPackCatalog()
        }
    }

    fun findFor(category: EmergencyCategory): SurvivalPackGuide? {
        val guide = catalog.packs.firstOrNull { it.category == category }
            ?: catalog.packs.firstOrNull { it.category == EmergencyCategory.OTHER }
        return guide?.withCatalogMetadata()
    }

    fun allPacks(): List<SurvivalPackGuide> {
        return catalog.packs
            .map { it.withCatalogMetadata() }
    }

    fun metadata(): SurvivalPackMetadata {
        return SurvivalPackMetadata(
            version = catalog.version,
            lastUpdated = catalog.lastUpdated,
            scope = catalog.scope
        )
    }

    private fun SurvivalPackGuide.withCatalogMetadata(): SurvivalPackGuide {
        return copy(
            version = catalog.version,
            lastUpdated = catalog.lastUpdated,
            scope = catalog.scope
        )
    }

    companion object {
        private const val ASSET_NAME = "survival_pack_v0_1.json"
    }
}

data class SurvivalPackMetadata(
    val version: String = "",
    val lastUpdated: String = "",
    val scope: String = ""
)
