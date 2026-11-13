package com.artillexstudios.axminions.api.config

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section
import com.artillexstudios.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings
import com.artillexstudios.axapi.utils.RotationType
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.minions.Direction
import org.bukkit.Material
import java.io.File
import java.io.InputStream
import java.util.Locale

class Messages(file: File, stream: InputStream) {
    companion object {
        @JvmStatic
        fun PREFIX() = AxMinionsAPI.INSTANCE.getMessages().get<String>("prefix")
        @JvmStatic
        fun MENU_MODE_SET() = AxMinionsAPI.INSTANCE.getMessages().get<String>("menu.mode-set")
        @JvmStatic
        fun MENU_MODE_BEDROCK() = AxMinionsAPI.INSTANCE.getMessages().get<String>("menu.mode-bedrock")
        @JvmStatic
        fun MENU_MODE_OLD_CLIENT() = AxMinionsAPI.INSTANCE.getMessages().get<String>("menu.mode-old-client")
        @JvmStatic
        fun SKILL_UPGRADED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("skills.upgraded")
        @JvmStatic
        fun SKILL_NO_POINTS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("skills.no-points")
        @JvmStatic
        fun SKILL_MAX_TIER() = AxMinionsAPI.INSTANCE.getMessages().get<String>("skills.max-tier")
        @JvmStatic
        fun SKILL_LOCKED_LEVEL() = AxMinionsAPI.INSTANCE.getMessages().get<String>("skills.locked-level")
        @JvmStatic
        fun SKILL_RESPEC_SUCCESS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("skills.respec-success")
        @JvmStatic
        fun SKILL_RESPEC_FAIL_MONEY() = AxMinionsAPI.INSTANCE.getMessages().get<String>("skills.respec-fail-money")
        @JvmStatic
        fun SKILL_NOTHING_TO_RESPEC() = AxMinionsAPI.INSTANCE.getMessages().get<String>("skills.nothing-to-respec")
        @JvmStatic
        fun NO_CONTAINER_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-container")
        @JvmStatic
        fun NO_TOOL_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-tool")
        @JvmStatic
        fun NO_WATER_NEARBY_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-water-nearby")
        @JvmStatic
        fun CONTAINER_FULL_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.container-full")
        @JvmStatic
        fun TIME_DAY() = AxMinionsAPI.INSTANCE.getMessages().get<String>("time.day")
        @JvmStatic
        fun TIME_HOUR() = AxMinionsAPI.INSTANCE.getMessages().get<String>("time.hour")
        @JvmStatic
        fun TIME_MINUTE() = AxMinionsAPI.INSTANCE.getMessages().get<String>("time.minute")
        @JvmStatic
        fun TIME_SECOND() = AxMinionsAPI.INSTANCE.getMessages().get<String>("time.second")
        @JvmStatic
        fun NO_CHARGE_WARNING() = AxMinionsAPI.INSTANCE.getMessages().get<String>("warnings.no-charge")
        @JvmStatic
        fun RELOAD_SUCCESS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("reload")
        @JvmStatic
        fun PLACE_SUCCESS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.success")
        @JvmStatic
        fun PLACE_LIMIT_REACHED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.limit-reached")
        @JvmStatic
        fun PLACE_MINION_AT_LOCATION() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.minion-at-location")
        @JvmStatic
        fun PLACE_MISSING_PERMISSION() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.missing-permission")
        @JvmStatic
        fun ISLAND_LIMIT_REACHED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("place.island-limit-reached")
        @JvmStatic
        fun STATISTICS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("statistics")
        @JvmStatic
        fun LEVEL_COLOR(level: Int = 1) = AxMinionsAPI.INSTANCE.getMessages().get("levels.$level", "<#33FF33>")
        @JvmStatic
        fun UPGRADES_MAX_LEVEL_REACHED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("upgrades.limit-reached")
        @JvmStatic
        fun ROTATION_NAME(direction: Direction) = AxMinionsAPI.INSTANCE.getMessages().get("directions.${direction.name.lowercase(
            Locale.ENGLISH)}", direction.name)
        @JvmStatic
        fun CHARGE() = AxMinionsAPI.INSTANCE.getMessages().get<String>("charge.charge")
        @JvmStatic
        fun CHARGE_FAIL() = AxMinionsAPI.INSTANCE.getMessages().get<String>("charge.not-enough-money")
        @JvmStatic
        fun CHARGE_NOT_ENOUGH_TIME_PASSED() = AxMinionsAPI.INSTANCE.getMessages().get<String>("charge.not-enough-time-passed")
        @JvmStatic
        fun WRONG_TOOL() = AxMinionsAPI.INSTANCE.getMessages().get<String>("tools.wrong-tool")
        @JvmStatic
        fun TOOL_STORED_LORE() = AxMinionsAPI.INSTANCE.getMessages().get("tools.stored-lore", "<gray>Ferramenta guardada: <white><tool>")
        @JvmStatic
        fun PICKUP_WITH_TOOL() = AxMinionsAPI.INSTANCE.getMessages().get("pickup.with-tool", "<green>Minion recolhido com a ferramenta guardada!")
        @JvmStatic
        fun PICKUP_WITHOUT_TOOL() = AxMinionsAPI.INSTANCE.getMessages().get("pickup.without-tool", "<green>Minion recolhido! A ferramenta foi devolvida separadamente.")
        @JvmStatic
        fun TOOL_NAME(material: Material?): String {
            val section = AxMinionsAPI.INSTANCE.getMessages().get<Section?>("tool-names", null)
            if (material == null || material.isAir) {
                return section?.getString("none") ?: "Nenhuma"
            }
            val underscoreKey = material.name.lowercase(Locale.ENGLISH)
            val spaceKey = underscoreKey.replace('_', ' ')
            val translated = section?.getString(spaceKey) ?: section?.getString(underscoreKey)
            if (!translated.isNullOrBlank()) {
                return translated
            }
            return spaceKey.split(" ").joinToString(" ") { word ->
                word.replaceFirstChar { ch -> ch.uppercase(Locale.ENGLISH) }
            }
        }
        @JvmStatic
        fun ERROR_INVENTORY_FULL() = AxMinionsAPI.INSTANCE.getMessages().get<String>("errors.inventory-full")
        @JvmStatic
        fun GIVE_MAX_LEVEL() = AxMinionsAPI.INSTANCE.getMessages().get<String>("errors.give-max-level")
        @JvmStatic
        fun LINK_SUCCESS() = AxMinionsAPI.INSTANCE.getMessages().get<String>("link.success")
        @JvmStatic
        fun LINK_UNLINK() = AxMinionsAPI.INSTANCE.getMessages().get<String>("link.unlink")
        @JvmStatic
        fun LINK_FAIL() = AxMinionsAPI.INSTANCE.getMessages().get<String>("link.fail")
        @JvmStatic
        fun LINK_START() = AxMinionsAPI.INSTANCE.getMessages().get<String>("link.start")
        @JvmStatic
        fun UPGRADE_FAIL() = AxMinionsAPI.INSTANCE.getMessages().get<String>("upgrades.fail")
        @JvmStatic
        fun RESET() = AxMinionsAPI.INSTANCE.getMessages().get<String>("reset")
        @JvmStatic
        fun NOT_ON_ISLAND() = AxMinionsAPI.INSTANCE.getMessages().get<String>("not-on-island")
        @JvmStatic
        fun SLOT_GIVE() = AxMinionsAPI.INSTANCE.getMessages().get<String>("slot-give")
        @JvmStatic
        fun SLOT_RECEIVE() = AxMinionsAPI.INSTANCE.getMessages().get<String>("slot-receive")
        @JvmStatic
        fun LOCATION_FORMAT() = AxMinionsAPI.INSTANCE.getMessages().get<String>("location-format")
    }

    private val config = Config(
        file,
        stream,
        GeneralSettings.builder().setUseDefaults(false).build(),
        LoaderSettings.builder().setAutoUpdate(true).build(),
        DumperSettings.DEFAULT,
        UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
    )

    fun <T> get(route: String?): T {
        return this.config.get(route)
    }

    fun <T> get(route: String?, default: T): T {
        return this.config.get(route, default)
    }

    fun getFormatted(route: String?): String {
        return StringUtils.formatToString(this.config.get(route))
    }

    fun reload() {
        config.reload()
    }
}
