package net.sistr.flexibleguns.wip.ecs.component.item.gun

data class AmmoLoadable(val maxAmmo: Int, var ammo: Int = 0, var prevAmmo: Int = 0)
