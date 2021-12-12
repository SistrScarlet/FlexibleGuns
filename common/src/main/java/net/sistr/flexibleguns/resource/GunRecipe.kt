package net.sistr.flexibleguns.resource

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.tag.ServerTagManagerHolder
import net.minecraft.tag.Tag
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.sistr.flexibleguns.item.GunItem
import net.sistr.flexibleguns.setup.Registration
import java.util.stream.Collectors

class GunRecipe(
    private val id: Identifier,
    val resultId: Identifier,
    matchPredicates: Collection<MatchPredicateEntry>
) {
    private val matchPredicates = ImmutableList.copyOf(matchPredicates)

    fun getRecipeId(): Identifier {
        return id
    }

    fun match(inventory: Inventory): Boolean {
        val collect = matchPredicates.stream()
            .map { TmpEntry(0, it) }
            .collect(Collectors.toList())
        for (index in 0 until inventory.size()) {
            val stack = inventory.getStack(index)
            collect.forEach {
                if (it.entry.matchPredicate.match(stack)) {
                    it.count += stack.count
                    //全部揃ったらtrue
                    if (collect.stream().allMatch { e -> e.entry.required <= e.count }) {
                        return true
                    }
                    return@forEach
                }
            }
        }
        return false
    }

    //match済み前提のため、アイテムがあろうがなかろうがリザルトを返す
    fun craft(inventory: Inventory): ItemStack {
        val collect = matchPredicates.stream()
            .map { TmpEntry(0, it) }
            .collect(Collectors.toList())
        for (index in 0 until inventory.size()) {
            val stack = inventory.getStack(index)
            collect.forEach {
                if (it.entry.matchPredicate.match(stack)) {
                    //アイテムを必要分だけ減らす
                    val num = Math.min(stack.count, it.entry.required - it.count)
                    stack.decrement(num)
                    it.count += num
                    if (stack.isEmpty) {
                        inventory.removeStack(index)
                    }
                    //全部揃ったらtrue
                    if (collect.stream().allMatch { e -> e.entry.required <= e.count }) {
                        return getResult()
                    }
                    return@forEach
                }
            }
        }

        return getResult()
    }

    fun getMaterialExample(): List<List<ItemStack>> {
        return matchPredicates.stream()
            .map { entry ->
                entry.matchPredicate.getExample()
                    .map { stack ->
                        val oStack = stack.copy()
                        oStack.count = entry.required
                        oStack
                    }
            }
            .collect(Collectors.toList())
    }

    fun getMatchPredicates(): List<MatchPredicateEntry> {
        return matchPredicates
    }

    fun getResult(): ItemStack {
        return Registration.GUN_ITEM.get().createGun(resultId)
    }

    fun write(buf: PacketByteBuf) {
        buf.writeIdentifier(id)
        buf.writeIdentifier(resultId)
        matchPredicates.forEach {
            buf.writeBoolean(true)
            buf.writeByte(it.required)
            it.matchPredicate.write(buf)
        }
        buf.writeBoolean(false)
    }

    data class TmpEntry(var count: Int, val entry: MatchPredicateEntry)

    data class MatchPredicateEntry(val required: Int, val matchPredicate: MatchPredicate)

    interface MatchPredicate {
        fun match(stack: ItemStack): Boolean
        fun getExample(): List<ItemStack>
        fun write(buf: PacketByteBuf)
    }

    class ItemMatchPredicate(private val item: Item) : MatchPredicate {
        override fun match(stack: ItemStack): Boolean {
            return !stack.isEmpty && stack.item == item
        }

        override fun getExample(): List<ItemStack> {
            return ImmutableList.of(ItemStack(item))
        }

        override fun write(buf: PacketByteBuf) {
            buf.writeByte(0)
            buf.writeIdentifier(Registry.ITEM.getId(item))
        }
    }

    class TagMatchPredicate(private val tag: Tag<Item>) : MatchPredicate {
        override fun match(stack: ItemStack): Boolean {
            return !stack.isEmpty && tag.contains(stack.item)
        }

        override fun getExample(): List<ItemStack> {
            return tag.values().map { ItemStack(it) }
        }

        override fun write(buf: PacketByteBuf) {
            buf.writeByte(1)
            buf.writeIdentifier(
                ServerTagManagerHolder.getTagManager().getTagId(
                    Registry.ITEM_KEY, tag
                ) { IllegalStateException("Unknown item tag") }
            )
        }
    }

    class GunMatchPredicate(private val id: Identifier) : MatchPredicate {
        private val gun = Registration.GUN_ITEM.get().createGun(id)

        override fun match(stack: ItemStack): Boolean {
            return !stack.isEmpty && stack.item is GunItem && id == (stack.item as GunItem).getGunId(stack)
        }

        override fun getExample(): List<ItemStack> {
            return ImmutableList.of(gun)
        }

        override fun write(buf: PacketByteBuf) {
            buf.writeByte(2)
            buf.writeIdentifier(id)
        }
    }

    class Builder(val id: Identifier) {
        private val entries = Lists.newArrayList<MatchPredicateEntry>()
        var result: Identifier? = null

        constructor(buf: PacketByteBuf) : this(buf.readIdentifier()) {
            setResult(buf.readIdentifier())
            while (buf.readBoolean()) {
                val count = buf.readByte().toInt()

                val type = buf.readByte().toInt()
                when (type) {
                    0 -> {
                        val id = buf.readIdentifier()
                        addPredicate(count, ItemMatchPredicate(Registry.ITEM.get(id)))
                    }
                    1 -> {
                        val id = buf.readIdentifier()
                        addPredicate(
                            count,
                            TagMatchPredicate(ServerTagManagerHolder.getTagManager().getTag(
                                Registry.ITEM_KEY, id
                            ) { IllegalStateException("Unknown item tag") }
                            )
                        )
                    }
                    2 -> {
                        val id = buf.readIdentifier()
                        addPredicate(count, GunMatchPredicate(id))
                    }
                }

            }
        }

        fun setResult(result: Identifier): Builder {
            this.result = result
            return this
        }

        fun addPredicate(required: Int, gun: Identifier): Builder {
            addPredicate(required, GunMatchPredicate(gun))
            return this
        }

        fun addPredicate(required: Int, tag: Tag<Item>): Builder {
            addPredicate(required, TagMatchPredicate(tag))
            return this
        }

        fun addPredicate(required: Int, item: Item): Builder {
            addPredicate(required, ItemMatchPredicate(item))
            return this
        }

        fun addPredicate(required: Int, matchPredicate: MatchPredicate): Builder {
            addPredicate(MatchPredicateEntry(required, matchPredicate))
            return this
        }

        fun addPredicate(entry: MatchPredicateEntry): Builder {
            entries.add(entry)
            return this
        }

        fun build(): GunRecipe {
            return GunRecipe(id, result!!, entries)
        }

    }

}