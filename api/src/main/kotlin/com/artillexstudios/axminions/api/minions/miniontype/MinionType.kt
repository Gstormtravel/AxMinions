package com.artillexstudios.axminions.api.minions.miniontype

import com.artillexstudios.axapi.config.Config
import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section
import com.artillexstudios.axapi.libs.boostedyaml.dvs.versioning.BasicVersioning
import com.artillexstudios.axapi.libs.boostedyaml.settings.dumper.DumperSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.general.GeneralSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.loader.LoaderSettings
import com.artillexstudios.axapi.libs.boostedyaml.settings.updater.UpdaterSettings
import com.artillexstudios.axapi.utils.ItemBuilder
import com.artillexstudios.axapi.utils.StringUtils
import com.artillexstudios.axminions.api.AxMinionsAPI
import com.artillexstudios.axminions.api.config.Messages
import com.artillexstudios.axminions.api.minions.Minion
import com.artillexstudios.axminions.api.utils.Keys
import com.artillexstudios.axminions.api.utils.fastFor
import java.io.File
import java.io.InputStream
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class MinionType(private val name: String, private val defaults: InputStream, private val autoUpdateConfig: Boolean) {
    val faces = arrayOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
    private lateinit var config: Config

    constructor(name: String, defaults: InputStream) : this(name, defaults, false)

    fun load() {
        if (!autoUpdateConfig) {
            config = Config(
                File(AxMinionsAPI.INSTANCE.getAxMinionsDataFolder(), "/minions/$name.yml"),
                defaults,
                GeneralSettings.builder().setUseDefaults(false).build(),
                LoaderSettings.DEFAULT,
                DumperSettings.DEFAULT,
                UpdaterSettings.DEFAULT
            )
        } else {
            config = Config(
                File(AxMinionsAPI.INSTANCE.getAxMinionsDataFolder(), "/minions/$name.yml"),
                defaults,
                GeneralSettings.builder().setUseDefaults(false).build(),
                LoaderSettings.builder().setAutoUpdate(true).build(),
                DumperSettings.DEFAULT,
                UpdaterSettings.builder().setVersioning(BasicVersioning("config-version")).build()
            )
        }
        AxMinionsAPI.INSTANCE.getDataHandler().insertType(this)
    }

    fun getName(): String {
        return this.name
    }

    open fun onToolDirty(minion: Minion) {

    }

    open fun shouldRun(minion: Minion): Boolean {
        return true
    }

    fun tick(minion: Minion) {
        if (!com.artillexstudios.axminions.api.config.Config.WORK_WHEN_OWNER_OFFLINE() && !minion.isOwnerOnline()) return
        if (!shouldRun(minion)) return

        minion.resetAnimation()
        run(minion)
    }

    @JvmOverloads
    fun getItem(
        level: Int = 1,
        actions: Long = 0,
        charge: Long = 0,
        skills: String = "",
        serializedTool: String = "",
        toolDisplayName: String? = null,
        firstOwner: String = "",
        ownerHistory: String = ""
    ): ItemStack {
        val builder = ItemBuilder.create(
            config.getSection("item"),
            Placeholder.unparsed("level", level.toString()),
            Placeholder.unparsed("actions", actions.toString())
        )
        val item = builder.clonedGet()
        val meta = item.itemMeta!!
        meta.persistentDataContainer.set(Keys.MINION_TYPE, PersistentDataType.STRING, name)
        meta.persistentDataContainer.set(Keys.LEVEL, PersistentDataType.INTEGER, level)
        meta.persistentDataContainer.set(Keys.STATISTICS, PersistentDataType.LONG, actions)
        meta.persistentDataContainer.set(Keys.CHARGE, PersistentDataType.LONG, charge)
        if (skills.isNotEmpty()) {
            meta.persistentDataContainer.set(Keys.SKILLS, PersistentDataType.STRING, skills)
        }
        if (firstOwner.isNotBlank()) {
            meta.persistentDataContainer.set(Keys.FIRST_OWNER, PersistentDataType.STRING, firstOwner)
        }
        if (ownerHistory.isNotBlank()) {
            meta.persistentDataContainer.set(Keys.OWNER_HISTORY, PersistentDataType.STRING, ownerHistory)
        }
        if (serializedTool.isNotEmpty()) {
            meta.persistentDataContainer.set(Keys.TOOL, PersistentDataType.STRING, serializedTool)
            if (!toolDisplayName.isNullOrBlank()) {
                val loreList = meta.lore?.toMutableList() ?: mutableListOf()
                val formattedLine = StringUtils.formatToString(
                    Messages.TOOL_STORED_LORE().replace("<tool>", toolDisplayName)
                )
                loreList.add(formattedLine)
                meta.lore = loreList
            }
        }
        item.itemMeta = meta
        return item
    }

    fun getConfig(): Config {
        return this.config
    }

    fun getString(key: String, level: Int): String {
        return get(key, level, "---", String::class.java)!!
    }

    fun getDouble(key: String, level: Int): Double {
        return get(key, level, 0.0, Double::class.java)!!
    }

    fun getLong(key: String, level: Int): Long {
        return get(key, level, 0, Long::class.java)!!
    }

    fun getSection(key: String, level: Int): Section? {
        return get(key, level, null, Section::class.java)
    }

    private fun <T> get(key: String, level: Int, defaultValue: T?, clazz: Class<T>): T? {
        var n = defaultValue

        config.getSection("upgrades").getRoutesAsStrings(false).forEach {
            if (it.toInt() > level) {
                return n
            }

            if (config.backingDocument.getAsOptional("upgrades.$it.$key", clazz).isEmpty) return@forEach

            n = config.get("upgrades.$it.$key")
        }

        return n
    }

    fun hasReachedMaxLevel(minion: Minion): Boolean {
        return !config.backingDocument.isSection("upgrades.${minion.getLevel() + 1}")
    }

    fun hasChestOnSide(block: Block): Boolean {
        faces.fastFor {
            if (block.getRelative(it).type == Material.CHEST) {
                return true
            }
        }

        return false
    }

    @Volatile
    private var cachedSkillSpec: com.artillexstudios.axminions.api.skills.SkillSystemSpec? = null

    fun getSkillSystemSpec(): com.artillexstudios.axminions.api.skills.SkillSystemSpec {
        val cached = cachedSkillSpec
        if (cached != null) return cached
        val parsed = com.artillexstudios.axminions.api.skills.SkillConfigParser.parse(config)
        cachedSkillSpec = parsed
        return parsed
    }

    fun invalidateSkillCache() {
        cachedSkillSpec = null
    }

    abstract fun run(minion: Minion)
}
