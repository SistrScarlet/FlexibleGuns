package net.sistr.flexibleguns.item

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.render.model.ModelLoader
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier
import net.minecraft.world.World
import net.sistr.flexibleguns.item.util.CustomTextureItem
import net.sistr.flexibleguns.resource.GunManager
import net.sistr.flexibleguns.resource.GunSetting
import net.sistr.flexibleguns.util.CustomItem
import net.sistr.flexibleguns.util.CustomItemStack
import net.sistr.flexibleguns.util.ItemInstance

//todo リロード音
//todo 減衰
//todo 空気抵抗
//todo 弾の見た目
//todo しゃがみ時の精度上昇効果
//todo 止まっている時の精度上昇効果
//todo 構えとリロード排他設定
class GunItem(settings: Settings) : Item(settings), CustomItem, CustomTextureItem {

    @Environment(EnvType.CLIENT)
    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        val itemIns = ((stack as Any) as CustomItemStack).getItemInstanceFG()
        if (itemIns is GunInstance) {
            val stackNbt = stack.orCreateNbt
            //銃の設定を取得
            val settingId = Identifier(stackNbt.getString("GunSettingId"))
            val setting = GunManager.INSTANCE.getGunSetting(settingId)
            if (setting != null) {
                //lore
                tooltip.add(
                    TranslatableText(
                        "gun." + setting.gunId.namespace + "." +
                                setting.gunId.path.replace("/", ".") + ".lore"
                    )
                )
            }
            if (itemIns.reload != null) {
                tooltip.add(
                    LiteralText(
                        itemIns.reload.ammo.toString() + " / " + itemIns.reload.maxAmmo.toString()
                    )
                )
            }
        }

    }

    override fun getTextureId(stack: ItemStack): ModelIdentifier {
        val nbt = stack.orCreateNbt
        return if (nbt.contains("CustomTextureID")) {
            ModelIdentifier(nbt.getString("CustomTextureID"))
        } else {
            ModelLoader.MISSING_ID
        }
    }

    fun createGun(id: Identifier): ItemStack {
        val stack = ItemStack(this)
        val nbt = stack.orCreateNbt
        nbt.putString("GunSettingId", id.toString())
        init(stack)
        return stack
    }

    fun getGunId(stack: ItemStack): Identifier {
        return Identifier(stack.orCreateNbt.getString("GunSettingId"))
    }

    override fun createItemInstanceFG(stack: ItemStack): ItemInstance {
        //設定が無い場合
        val stackNbt = stack.orCreateNbt
        if (!stackNbt.contains("GunSettingId")) {
            return ItemInstance.EMPTY
        }
        init(stack)
        //銃の設定が無いなら空を返す
        val setting = getGunSetting(stack) ?: return ItemInstance.EMPTY
        val gunDate = stackNbt.getCompound("GunDate")
        return GunInstance(setting, gunDate)
    }

    fun getGunSetting(stack: ItemStack): GunSetting? {
        val stackNbt = stack.orCreateNbt
        if (!stackNbt.contains("GunSettingId")) return null
        val id = Identifier(stackNbt.getString("GunSettingId"))
        return GunManager.INSTANCE.getGunSetting(id)
    }

    fun init(stack: ItemStack) {
        val stackNbt = stack.orCreateNbt

        //銃の設定を取得
        val setting: GunSetting? = getGunSetting(stack)

        //設定が無い場合
        if (setting == null) {
            //テクスチャだけ剥がす
            stackNbt.remove("CustomTextureID")
            //stackNbt.remove("GunSettingId")
            //stackNbt.remove("GunDate")
            //stack.setCustomName(null)
            return
        }

        //テクスチャの設定
        setting.textureId?.let { id ->
            stackNbt.putString("CustomTextureID", "$id#inventory")
        }

        //名前の設定
        val name = TranslatableText(
            "gun." + setting.gunId.namespace + "." + setting.gunId.path.replace("/", ".")
        )
        name.style = name.style.withItalic(false)
        stack.setCustomName(name)
    }

}