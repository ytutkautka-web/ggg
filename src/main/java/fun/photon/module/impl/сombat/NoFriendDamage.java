package fun.photon.module.impl.сombat;

import fun.photon.Photon;
import fun.photon.friend.FriendManager;
import fun.photon.module.Category;
import fun.photon.module.Module;
import fun.photon.module.ModuleInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@ModuleInfo(
    name = "NoFriendDamage",
    category = Category.COMBAT,
    description = "Убирает урон по друзьям"
)
public class NoFriendDamage extends Module {

    public static boolean shouldCancel(Entity target) {
        try {
            NoFriendDamage m = Photon.getInstance().getModuleManager().getModule(NoFriendDamage.class);
            if (m == null || !m.isEnabled()) return false;
            if (!(target instanceof Player)) return false;
            return FriendManager.get().isFriend(target.getName().getString());
        } catch (Throwable ignored) {
            return false;
        }
    }
}
