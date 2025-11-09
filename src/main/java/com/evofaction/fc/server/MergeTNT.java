package com.evofaction.fc.server;

import com.evofaction.fc.Config;
import com.evofaction.fc.TNTInterface;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.TntEntity;


public class MergeTNT {
    private static final TntEntity[] prev = { null };

    public static void init() {
        ServerTickEvents.START_WORLD_TICK.register((world) -> {
            if (!Config.MERGE_TNT) return;

            world.entityList.forEach(MergeTNT::tryMerge);
        });
    }

    private static void tryMerge(Entity entity) {
        if (!(entity instanceof TntEntity tntEntity)) return;

        // Last consecutive TNT entity in tick loop wins the merge
        if (prev[0] != null && ((TNTInterface) prev[0])._$canMergeWith(tntEntity)) {
            ((TNTInterface) entity)._$addMergedTNT(((TNTInterface) prev[0]));
            prev[0].discard();
        }

        prev[0] = tntEntity;
    }
}
