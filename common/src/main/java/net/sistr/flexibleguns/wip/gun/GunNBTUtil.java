package net.sistr.flexibleguns.wip.gun;

import net.minecraft.nbt.NbtCompound;

//ゲッターセッターのみ
//マイナスがあり得ない値は& ~0を付けている
public class GunNBTUtil {
    private static final String HOLDING = "Holding";
    private static final String SHOOTING = "Shooting";
    private static final String RELOAD_TIME = "ReloadTime";
    private static final String SHOOT_DELAY = "ShootDelay";
    private static final String BURST_DELAY = "BurstDelay";
    private static final String BURST_COUNT = "BurstCount";
    private static final String LOADED_BULLETS = "LoadedBullets";
    private static final String ZOOM = "Zoom";
    private static final String CHAMBER_STATE = "ChamberState";
    private static final String CHAMBER_TIME = "ChamberTime";
    private static final String SHOOT_RECORD = "ShootRecord";

    public static void setHolding(NbtCompound gun, boolean holding) {
        gun.putBoolean(HOLDING, holding);
    }

    public static boolean isHolding(NbtCompound gun) {
        return gun.getBoolean(HOLDING);
    }

    public static void setShooting(NbtCompound gun, boolean shooting) {
        gun.putBoolean(SHOOTING, shooting);
    }

    public static boolean isShooting(NbtCompound gun) {
        return gun.getBoolean(SHOOTING);
    }

    public static void setReloadTime(NbtCompound gun, int time) {
        gun.putShort(RELOAD_TIME, (short) time);
    }

    public static int getReloadTime(NbtCompound gun) {
        return gun.getShort(RELOAD_TIME);
    }

    public static void setShootDelay(NbtCompound gun, float delay) {
        gun.putFloat(SHOOT_DELAY, delay);
    }

    public static float getShootDelay(NbtCompound gun) {
        return gun.getFloat(SHOOT_DELAY);
    }

    public static void setBurstDelay(NbtCompound gun, float delay) {
        gun.putFloat(BURST_DELAY, delay);
    }

    public static float getBurstDelay(NbtCompound gun) {
        return gun.getFloat(BURST_DELAY);
    }

    public static void setBurstCount(NbtCompound gun, int burstAmount) {
        gun.putByte(BURST_COUNT, (byte) burstAmount);
    }

    public static int getBurstCount(NbtCompound gun) {
        return gun.getByte(BURST_COUNT) & ~0;
    }

    public static void setLoadedBullets(NbtCompound gun, int amount) {
        gun.putShort(LOADED_BULLETS, (short) amount);
    }

    public static int getLoadedBullets(NbtCompound gun) {
        return gun.getShort(LOADED_BULLETS) & ~0;
    }

    public static void setZoom(NbtCompound gun, boolean zoom) {
        gun.putBoolean(ZOOM, zoom);
    }

    public static boolean getZoom(NbtCompound gun) {
        return gun.getBoolean(ZOOM);
    }

    //0開閉、1装填
    //薬室が開放状態か否か、薬室に弾が入っているか
    public static void setChamberState(NbtCompound gun, byte state) {
        gun.putByte(CHAMBER_STATE, state);
    }

    public static void setChamberState(NbtCompound gun, int flag, boolean bool) {
        byte state = getChamberState(gun);
        if (bool) {
            setChamberState(gun, (byte)(state | 1 << flag));
        } else {
            setChamberState(gun, (byte)(state & ~(1 << flag)));
        }
    }

    public static byte getChamberState(NbtCompound gun) {
        return gun.getByte(CHAMBER_STATE);
    }

    public static boolean getChamberState(NbtCompound gun, int flag) {
        return (getChamberState(gun) & (1 << flag)) != 0;
    }

    public static void setChamberTime(NbtCompound gun, int time) {
        gun.putShort(CHAMBER_TIME, (short) time);
    }

    public static int getChamberTime(NbtCompound gun) {
        return gun.getShort(CHAMBER_TIME);
    }

    public static void setShootRecord(NbtCompound gun, int record) {
        gun.putInt(SHOOT_RECORD, record);
    }

    public static int getShootRecord(NbtCompound gun) {
        return gun.getInt(SHOOT_RECORD);
    }

}
