package com.artillexstudios.axminions.integrations.protection

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import java.lang.reflect.Method

object EliteMobsProtection {
    private var initAttempted = false
    private var isEliteMobMethod: Method? = null
    private var isEliteEntityMethod: Method? = null
    private var getEliteMobEntityMethod: Method? = null

    @Synchronized
    private fun init() {
        if (initAttempted) return
        initAttempted = true
        if (Bukkit.getPluginManager().getPlugin("EliteMobs") == null) return

        try {
            val trackerClass = Class.forName("com.magmaguy.elitemobs.entitytracker.EntityTracker")
            isEliteMobMethod = trackerClass.getMethod("isEliteMob", Entity::class.java)
            getEliteMobEntityMethod = try {
                trackerClass.getMethod("getEliteMobEntity", Entity::class.java)
            } catch (_: Throwable) {
                null
            }
        } catch (_: Throwable) {
        }

        try {
            val taggerClass = Class.forName("com.magmaguy.elitemobs.tagger.PersistentTagger")
            isEliteEntityMethod = taggerClass.getMethod("isEliteEntity", Entity::class.java)
        } catch (_: Throwable) {
        }
    }

    fun isEliteEntity(entity: Entity): Boolean {
        init()

        try {
            if (isEliteMobMethod?.invoke(null, entity) == true) return true
            if (getEliteMobEntityMethod?.invoke(null, entity) != null) return true
            if (isEliteEntityMethod?.invoke(null, entity) == true) return true
        } catch (_: Throwable) {
        }

        if (entity.hasMetadata("EliteMobs") ||
            entity.hasMetadata("EliteMob") ||
            entity.hasMetadata("SuperMob") ||
            entity.hasMetadata("EliteBoss")
        ) {
            return true
        }

        for (key in entity.persistentDataContainer.keys) {
            if (key.namespace.equals("elitemobs", ignoreCase = true)) {
                return true
            }
        }

        return false
    }
}
