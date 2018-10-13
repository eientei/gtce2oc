package org.eientei.gtce2oc.driver;

import gregtech.api.items.IDamagableItem;
import li.cil.oc.api.event.RobotUsedToolEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {
    @SubscribeEvent
    public static void onRobotApplyDamageRate(RobotUsedToolEvent.ApplyDamageRate e) {
        if (e.toolBeforeUse.getItem() instanceof IDamagableItem && e.toolAfterUse.getItem() instanceof IDamagableItem) {
            IDamagableItem before = (IDamagableItem) e.toolBeforeUse.getItem();
            IDamagableItem after = (IDamagableItem) e.toolAfterUse.getItem();
            double damage = after.getInternalDamage(e.toolAfterUse) - before.getInternalDamage(e.toolBeforeUse);
            if (damage > 0) {
                double actualDamage = damage * e.getDamageRate();
                int repairedDamage = (int) ((e.agent.player().getRNG().nextDouble() > 0.5) ? damage - Math.floor(actualDamage) : damage - Math.ceil(actualDamage));
                after.doDamageToItem(e.toolAfterUse, -repairedDamage, false);
            }
        }
    }

    public static Double getDurability(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof IDamagableItem) {
            IDamagableItem ditem = (IDamagableItem) item;
            return 1.0 - (double)ditem.getInternalDamage(stack) / (double)ditem.getMaxInternalDamage(stack);
        }
        return Double.NaN;
    }
}
