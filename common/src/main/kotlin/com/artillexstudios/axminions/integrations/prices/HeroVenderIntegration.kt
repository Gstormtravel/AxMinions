package com.artillexstudios.axminions.integrations.prices

import com.artillexstudios.axminions.AxMinionsPlugin
import com.artillexstudios.axminions.api.integrations.types.PricesIntegration
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Method

class HeroVenderIntegration : PricesIntegration {
    private var heroVenderInstance: Any? = null
    private var shopController: Any? = null
    private var userController: Any? = null

    private var getShopsMethod: Method? = null
    private var getShopsForUserMethod: Method? = null
    private var getOrCreateUserMethod: Method? = null
    private var getShopPermissionMethod: Method? = null
    private var getShopItemMethod: Method? = null
    private var getShopItemPriceMethod: Method? = null

    override fun register() {
        try {
            val hvClass = Class.forName("com.github.heroslender.herovender.HeroVender")
            val getInstanceMethod = hvClass.getMethod("getInstance")
            val hv = getInstanceMethod.invoke(null)
            heroVenderInstance = hv

            val getShopControllerMethod = hvClass.getMethod("getShopController")
            val sc = getShopControllerMethod.invoke(hv)
            shopController = sc

            val getUserControllerMethod = hvClass.getMethod("getUserController")
            val uc = getUserControllerMethod.invoke(hv)
            userController = uc

            val scClass = sc.javaClass
            val ucClass = uc.javaClass
            val userClass = Class.forName("com.github.heroslender.herovender.data.User")
            val shopClass = Class.forName("com.github.heroslender.herovender.data.Shop")
            val shopItemClass = Class.forName("com.github.heroslender.herovender.data.ShopItem")

            getShopsMethod = scClass.getMethod("getShops")
            getShopsForUserMethod = scClass.getMethod("getShops", userClass)
            getOrCreateUserMethod = ucClass.getMethod("getOrCreate", org.bukkit.entity.Player::class.java)
            getShopPermissionMethod = shopClass.getMethod("getPermission")
            getShopItemMethod = shopClass.getMethod("getShopItem", ItemStack::class.java)
            getShopItemPriceMethod = shopItemClass.getMethod("getPrice")
        } catch (t: Throwable) {
            AxMinionsPlugin.INSTANCE.logger.warning("[AxMinions] Falha ao inicializar HeroVender: ${t.message}")
            heroVenderInstance = null
        }
    }

    override fun getPrice(itemStack: ItemStack): Double {
        return getPrice(itemStack, null)
    }

    override fun getPrice(itemStack: ItemStack, player: OfflinePlayer?): Double {
        if (itemStack.type.isAir || itemStack.amount <= 0) return 0.0
        val sc = shopController ?: return 0.0
        val getShopItem = getShopItemMethod ?: return 0.0
        val getShopItemPrice = getShopItemPriceMethod ?: return 0.0

        try {
            val candidateShops: List<*> = resolveCandidateShops(sc, player)
            if (candidateShops.isEmpty()) return 0.0

            val singleItem = if (itemStack.amount == 1) itemStack else itemStack.clone().apply { amount = 1 }

            for (shop in candidateShops) {
                if (shop == null) continue
                val shopItem = getShopItem.invoke(shop, singleItem) ?: getShopItem.invoke(shop, itemStack) ?: continue
                val unitPrice = (getShopItemPrice.invoke(shopItem) as? Number)?.toDouble() ?: 0.0
                if (unitPrice > 0.0) {
                    return unitPrice * itemStack.amount
                }
            }
        } catch (t: Throwable) {
            if (com.artillexstudios.axminions.api.config.Config.DEBUG()) {
                Bukkit.getLogger().warning("[AxMinions] Erro ao consultar preco no HeroVender: ${t.message}")
            }
        }
        return 0.0
    }

    private fun resolveCandidateShops(sc: Any, player: OfflinePlayer?): List<*> {
        val onlinePlayer = player?.player
        if (onlinePlayer != null) {
            val uc = userController
            val getOrCreate = getOrCreateUserMethod
            val getShopsForUser = getShopsForUserMethod
            if (uc != null && getOrCreate != null && getShopsForUser != null) {
                val user = getOrCreate.invoke(uc, onlinePlayer)
                if (user != null) {
                    val userShops = getShopsForUser.invoke(sc, user)
                    if (userShops is Array<*>) {
                        return userShops.toList()
                    }
                    if (userShops is Collection<*>) {
                        return userShops.toList()
                    }
                }
            }
        }

        val allShops = (getShopsMethod?.invoke(sc) as? Collection<*>)?.toList() ?: return emptyList<Any?>()
        val getPerm = getShopPermissionMethod ?: return allShops

        return if (onlinePlayer != null) {
            allShops.filter { shop ->
                if (shop == null) return@filter false
                val perm = getPerm.invoke(shop) as? String
                perm.isNullOrBlank() || onlinePlayer.hasPermission(perm)
            }
        } else {
            val publicShops = allShops.filter { shop ->
                if (shop == null) return@filter false
                val perm = getPerm.invoke(shop) as? String
                perm.isNullOrBlank()
            }
            publicShops.ifEmpty { allShops }
        }
    }
}
