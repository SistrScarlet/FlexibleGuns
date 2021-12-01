package net.sistr.flexibleguns.wip.ecs.component.item

import net.minecraft.util.Hand

data class Hold(
    var heldHand: Hand? = null,
    var hold: Boolean = false,
    var prevHold: Boolean = false
)