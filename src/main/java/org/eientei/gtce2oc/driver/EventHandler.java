package org.eientei.gtce2oc.driver;

import gregtech.api.items.IToolItem;
import li.cil.oc.api.event.RobotUsedToolEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {
    @SubscribeEvent
    public static void onRobotApplyDamageRate(RobotUsedToolEvent.ApplyDamageRate e) {
        if (e.toolBeforeUse.getItem() instanceof IToolItem && e.toolAfterUse.getItem() instanceof IToolItem) {
            IToolItem  before = (IToolItem) e.toolBeforeUse.getItem();
            IToolItem  after = (IToolItem) e.toolAfterUse.getItem();
            double damage = after.getItemDamage(e.toolAfterUse) - before.getItemDamage(e.toolBeforeUse);
            if (damage > 0) {
                double actualDamage = damage * e.getDamageRate();
                int repairedDamage = (int) ((e.agent.player().getRNG().nextDouble() > 0.5) ? damage - Math.floor(actualDamage) : damage - Math.ceil(actualDamage));
                after.damageItem(e.toolAfterUse, -repairedDamage, false);
            }
        }
    }

    public static Double getDurability(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof IToolItem) {
            IToolItem ditem = (IToolItem) item;
            return 1.0 - (double)ditem.getItemDamage(stack) / (double)ditem.getMaxItemDamage(stack);
        }
        return Double.NaN;
    }
}
