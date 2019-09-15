package org.eientei.gtce2oc.driver;

import net.minecraft.item.ItemStack;

public class ChancedOutput {
    private final ItemStack item;
    private final int chance;
    private final int boost;

    public ChancedOutput(ItemStack item, int chance, int boost) {
        this.item = item;
        this.chance = chance;
        this.boost = boost;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getChance() {
        return chance;
    }

    public int getBoost() {
        return boost;
    }
}
