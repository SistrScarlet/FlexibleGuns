package net.sistr.flexibleguns.network

import dev.architectury.networking.NetworkManager
import dev.architectury.platform.Platform
import net.fabricmc.api.EnvType

object Networking {

    fun init() {
        commonInit()
        if (Platform.getEnv() == EnvType.CLIENT) {
            clientInit()
        }
    }

    private fun commonInit() {
        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            InputPacket.ID
        ) { buf, ctx -> InputPacket.receiveC2SPacket(buf, ctx) }
        NetworkManager.registerReceiver(
            NetworkManager.Side.C2S,
            GunCraftPacket.ID
        ) { buf, ctx -> GunCraftPacket.receiveC2SPacket(buf, ctx) }
    }

    private fun clientInit() {
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            CustomEntitySpawnPacket.ID
        ) { buf, ctx -> CustomEntitySpawnPacket.receiveS2CPacket(buf, ctx) }
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            BulletSpawnPacket.ID
        ) { buf, ctx -> BulletSpawnPacket.receiveS2CPacket(buf, ctx) }
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            AmmoPacket.ID
        ) { buf, ctx -> AmmoPacket.receiveS2CPacket(buf, ctx) }
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            BulletHitPacket.ID
        ) { buf, ctx -> BulletHitPacket.receiveS2CPacket(buf, ctx) }
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            GunSyncPacket.ID
        ) { buf, ctx -> GunSyncPacket.receiveS2CPacket(buf, ctx) }
    }

}