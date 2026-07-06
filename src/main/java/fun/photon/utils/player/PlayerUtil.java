package fun.photon.utils.player;

import fun.photon.utils.IMinecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

public class PlayerUtil implements IMinecraft {

    public static boolean nullCheck() {
        return mc.player == null || mc.level == null;
    }

    public static Block blockRelativeToPlayer(double offsetX, double offsetY, double offsetZ) {
        if (nullCheck()) return null;
        return mc.level.getBlockState(BlockPos.containing(mc.player.getX() + offsetX, mc.player.getY() + offsetY, mc.player.getZ() + offsetZ)).getBlock();
    }

    public static Block block(BlockPos pos) {
        if (nullCheck()) return null;
        return mc.level.getBlockState(pos).getBlock();
    }

    public static Block block(double x, double y, double z) {
        if (nullCheck()) return null;
        return mc.level.getBlockState(BlockPos.containing(x, y, z)).getBlock();
    }

    public static double getEntityHealth(LivingEntity entity) {
        return entity.getHealth() + entity.getAbsorptionAmount();
    }

    public static double getEntityArmor(LivingEntity entity) {
        return entity.getArmorValue();
    }

    public static boolean isBlockSolid(BlockPos pos) {
        if (nullCheck()) return false;
        return mc.level.getBlockState(pos).isSolid();
    }

    public static boolean isBlockAboveHead() {
        if (nullCheck()) return false;
        float width = mc.player.getBbWidth() / 2F;
        AABB aabb = new AABB(
                mc.player.getX() - width, mc.player.getY() + mc.player.getEyeHeight(), mc.player.getZ() - width,
                mc.player.getX() + width, mc.player.getY() + (mc.player.onGround() ? 2.5 : 1.5), mc.player.getZ() + width
        );
        return !mc.level.noCollision(mc.player, aabb);
    }

    public static boolean isBlockUnder() {
        if (nullCheck()) return false;
        for (int offset = 0; offset < 2; offset++) {
            Block block = blockRelativeToPlayer(0, -offset - 0.1, 0);
            if (block != null && !block.defaultBlockState().isAir()) {
                return true;
            }
        }
        return false;
    }
}