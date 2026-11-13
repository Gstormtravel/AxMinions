package com.artillexstudios.axminions.nms.v26_3

import com.artillexstudios.axminions.api.minions.Minion
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Mob
import org.bukkit.entity.Player
import java.util.UUID

object DamageHandler {

    private val currentMinion = ThreadLocal<Minion?>()
    private val fallbackUUID = UUID.randomUUID()

    fun getUUID(): UUID =
        currentMinion.get()?.getOwnerUUID() ?: fallbackUUID

    fun getMinion(): Minion? =
        currentMinion.get()

    fun damage(source: Minion, entity: Entity) {
        if (!entity.isValid || entity !is LivingEntity) return

        currentMinion.set(source)
        try {
            val owner = source.getOwner()
            val tool = source.getTool()

            var damage = 1.0 // Dano base do punho

            if (tool != null) {
                // 1. Dano base do material
                val defaultModifiers = tool.type.getDefaultAttributeModifiers(EquipmentSlot.HAND).get(Attribute.ATTACK_DAMAGE)
                defaultModifiers?.forEach { modifier -> damage += modifier.amount }

                // 2. Dano customizado
                val meta = tool.itemMeta
                if (meta != null) {
                    val customModifiers = meta.getAttributeModifiers(Attribute.ATTACK_DAMAGE)
                    customModifiers?.forEach { modifier -> damage += modifier.amount }
                }

                // 3. Afiação (Sharpness)
                val sharpnessLevel = tool.getEnchantmentLevel(Enchantment.SHARPNESS)
                if (sharpnessLevel > 0) {
                    damage += 1.0 + (sharpnessLevel - 1) * 0.5
                }

                // 4. Julgamento (Smite)
                val smiteLevel = tool.getEnchantmentLevel(Enchantment.SMITE)
                if (smiteLevel > 0 && isUndead(entity)) {
                    damage += 2.5 * smiteLevel
                }

                // 5. Ruína dos Artrópodes (Bane of Arthropods)
                val baneLevel = tool.getEnchantmentLevel(Enchantment.BANE_OF_ARTHROPODS)
                if (baneLevel > 0 && isArthropod(entity)) {
                    damage += 2.5 * baneLevel
                }

                // 6. Fire Aspect
                val fireAspectLevel = tool.getEnchantmentLevel(Enchantment.FIRE_ASPECT)
                if (fireAspectLevel > 0) {
                    entity.fireTicks = Math.max(entity.fireTicks, 80 * fireAspectLevel)
                }
            }

            // Bypass na Imunidade (I-Frames)
            entity.noDamageTicks = 0

            // Aplica o Dano
            if (owner is Player && owner.isOnline) {
                entity.damage(damage, owner)
            } else {
                entity.damage(damage)
            }

            // Knockback
            if (tool != null) {
                val knockbackLevel = tool.getEnchantmentLevel(Enchantment.KNOCKBACK)
                if (knockbackLevel > 0) {
                    val direction = entity.location.toVector().subtract(source.getLocation().toVector()).normalize()
                    entity.velocity = entity.velocity.add(direction.multiply(knockbackLevel * 0.4))
                }
            }

            // Evitar Aggro
            if (entity is Mob) {
                entity.target = null
            }

        } finally {
            currentMinion.remove()
        }
    }

    // Verifica se o mob é morto-vivo (para o Smite)
    private fun isUndead(entity: Entity): Boolean {
        return when (entity.type) {
            org.bukkit.entity.EntityType.ZOMBIE,
            org.bukkit.entity.EntityType.ZOMBIE_VILLAGER,
            org.bukkit.entity.EntityType.HUSK,
            org.bukkit.entity.EntityType.DROWNED,
            org.bukkit.entity.EntityType.SKELETON,
            org.bukkit.entity.EntityType.STRAY,
            org.bukkit.entity.EntityType.WITHER_SKELETON,
            org.bukkit.entity.EntityType.WITHER,
            org.bukkit.entity.EntityType.PHANTOM,
            org.bukkit.entity.EntityType.ZOMBIFIED_PIGLIN,
            org.bukkit.entity.EntityType.ZOGLIN,
            org.bukkit.entity.EntityType.SKELETON_HORSE,
            org.bukkit.entity.EntityType.ZOMBIE_HORSE -> true
            else -> false
        }
    }

    // Verifica se o mob é um artrópode (para o Bane of Arthropods)
    private fun isArthropod(entity: Entity): Boolean {
        return when (entity.type) {
            org.bukkit.entity.EntityType.SPIDER,
            org.bukkit.entity.EntityType.CAVE_SPIDER,
            org.bukkit.entity.EntityType.SILVERFISH,
            org.bukkit.entity.EntityType.ENDERMITE,
            org.bukkit.entity.EntityType.BEE -> true
            else -> false
        }
    }
}