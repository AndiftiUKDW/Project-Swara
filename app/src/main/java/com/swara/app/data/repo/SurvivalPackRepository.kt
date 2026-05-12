package com.swara.app.data.repo

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.swara.app.data.model.EmergencyCategory
import com.swara.app.data.model.GuideCatalogItem
import com.swara.app.data.model.GuideDownloadMode
import com.swara.app.data.model.GuideModule
import com.swara.app.data.model.SurvivalPackCatalog
import com.swara.app.data.model.SurvivalPackGuide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SurvivalPackRepository(
    context: Context,
    private val gson: Gson = Gson()
) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences("swara_guides", Context.MODE_PRIVATE)
    private val moduleListType = object : TypeToken<List<GuideModule>>() {}.type
    private val _installedModules = MutableStateFlow(loadInstalledModules())
    val installedModules: StateFlow<List<GuideModule>> = _installedModules.asStateFlow()

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

    fun marketplaceCatalog(): List<GuideCatalogItem> = dummyMarketplace

    fun installMarketplaceGuide(itemId: String): Result<GuideModule> {
        val item = dummyMarketplace.firstOrNull { it.id == itemId }
            ?: return Result.failure(IllegalArgumentException("Guide not found."))
        if (item.comingSoon) return Result.failure(IllegalStateException("This guide is coming soon."))
        val current = _installedModules.value
        val next = (current.filterNot { it.id == item.guideModule.id } + item.guideModule)
            .sortedWith(compareBy<GuideModule> { it.category.ordinal }.thenBy { it.title })
        saveInstalledModules(next)
        _installedModules.value = next
        return Result.success(item.guideModule)
    }

    fun metadata(): SurvivalPackMetadata {
        return SurvivalPackMetadata(
            version = catalog.version,
            lastUpdated = catalog.lastUpdated,
            scope = catalog.scope
        )
    }

    fun rawCatalogJson(): String {
        return appContext.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
    }

    private fun SurvivalPackGuide.withCatalogMetadata(): SurvivalPackGuide {
        val modules = _installedModules.value.filter { it.category == category }
        return copy(
            version = catalog.version,
            lastUpdated = catalog.lastUpdated,
            scope = catalog.scope,
            title = title.replace("Pack", "Guide"),
            addedModules = modules
        )
    }

    private fun loadInstalledModules(): List<GuideModule> {
        val json = preferences.getString(KEY_MODULES, null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<GuideModule>>(json, moduleListType).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun saveInstalledModules(modules: List<GuideModule>) {
        preferences.edit().putString(KEY_MODULES, gson.toJson(modules)).apply()
    }

    companion object {
        private const val ASSET_NAME = "survival_pack_v0_1.json"
        private const val KEY_MODULES = "installed_guide_modules"
    }
}

data class SurvivalPackMetadata(
    val version: String = "",
    val lastUpdated: String = "",
    val scope: String = ""
)

private val heatExhaustionModule = GuideModule(
    id = "medical-heat-exhaustion",
    category = EmergencyCategory.MEDICAL,
    title = "Heat Exhaustion Guide",
    summary = "Recognize heat exhaustion and cool the person quickly.",
    sourceName = "American Red Cross / CDC NIOSH",
    sourceUrl = "https://www.redcross.org/take-a-class/resources/learn-first-aid/heat-exhaustion",
    version = "0.1",
    quickHelp = listOf(
        "Move the person to a cooler place or shade.",
        "Loosen tight clothing and remove extra layers.",
        "Cool with wet cloths, water, fan, or cool compresses.",
        "Give small sips of water if the person is awake and can swallow."
    ),
    detailedSteps = listOf(
        "Stop activity and move away from heat.",
        "Lay the person down or let them rest in a cool place.",
        "Cool the skin with water, wet cloths, fan, or cool compresses.",
        "Give small sips of water or oral rehydration fluid if awake and not vomiting.",
        "Watch for confusion, fainting, seizure, or worsening symptoms because these can signal heat stroke."
    ),
    doNot = listOf(
        "Do not give drinks if the person is confused, unconscious, or cannot swallow.",
        "Do not ignore confusion, fainting, or hot dry skin.",
        "Do not let the person return to heat or heavy activity immediately."
    ),
    whenToGetHelp = listOf(
        "Call emergency help if symptoms worsen, the person faints, becomes confused, has a seizure, or heat stroke is suspected."
    ),
    tags = listOf("heat", "dehydration", "medical", "exhaustion")
)

private val powerOutageModule = GuideModule(
    id = "general-power-outage",
    category = EmergencyCategory.OTHER,
    title = "Power Outage Guide",
    summary = "Stay safe during electricity outages, food spoilage risk, and generator use.",
    sourceName = "Ready.gov",
    sourceUrl = "https://www.ready.gov/power-outages",
    version = "0.1",
    quickHelp = listOf(
        "Use flashlights instead of candles when possible.",
        "Keep refrigerator and freezer doors closed.",
        "Unplug sensitive appliances to reduce surge damage.",
        "Use generators outdoors only, away from doors, windows, and vents."
    ),
    detailedSteps = listOf(
        "Check whether anyone nearby depends on powered medical equipment.",
        "Keep phones charged and use low-power mode.",
        "Keep refrigerator and freezer closed to slow food spoilage.",
        "Disconnect appliances and electronics to reduce surge damage when power returns.",
        "If using a generator, keep it outside and far from doors, windows, vents, and garages."
    ),
    doNot = listOf(
        "Do not run a generator indoors, in a garage, or near windows.",
        "Do not use a gas stove or oven to heat the home.",
        "Do not touch downed power lines or anything in contact with them.",
        "Do not open refrigerator/freezer repeatedly."
    ),
    whenToGetHelp = listOf(
        "Call emergency help for downed power lines, carbon monoxide symptoms, fire, or urgent medical equipment needs."
    ),
    tags = listOf("power", "outage", "generator", "food", "general")
)

private val dummyMarketplace = listOf(
    GuideCatalogItem(
        id = heatExhaustionModule.id,
        title = heatExhaustionModule.title,
        category = heatExhaustionModule.category,
        summary = heatExhaustionModule.summary,
        version = heatExhaustionModule.version,
        sourceName = heatExhaustionModule.sourceName,
        sourceUrl = heatExhaustionModule.sourceUrl,
        downloadMode = GuideDownloadMode.SIMULATED,
        guideModule = heatExhaustionModule
    ),
    GuideCatalogItem(
        id = powerOutageModule.id,
        title = powerOutageModule.title,
        category = powerOutageModule.category,
        summary = powerOutageModule.summary,
        version = powerOutageModule.version,
        sourceName = powerOutageModule.sourceName,
        sourceUrl = powerOutageModule.sourceUrl,
        downloadMode = GuideDownloadMode.SIMULATED,
        guideModule = powerOutageModule
    ),
    GuideCatalogItem(
        id = "flood-clean-water",
        title = "Clean Water After Flood",
        category = EmergencyCategory.FLOOD,
        summary = "Water treatment and contamination safety after flooding.",
        version = "0.1",
        sourceName = "Ready.gov",
        sourceUrl = "https://www.ready.gov/water",
        comingSoon = true
    ),
    GuideCatalogItem(
        id = "earthquake-aftershock-home",
        title = "Aftershock Home Safety",
        category = EmergencyCategory.EARTHQUAKE,
        summary = "Post-earthquake checks for unstable buildings and aftershocks.",
        version = "0.1",
        sourceName = "Ready.gov",
        sourceUrl = "https://www.ready.gov/earthquakes",
        comingSoon = true
    )
)
