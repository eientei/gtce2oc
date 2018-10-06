package org.eientei.gtce2oc.impl;

import gregtech.api.capability.GregtechCapabilities;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class TileEntityStatic {
    public static boolean hasCapability(TileEntity that, Capability<?> capability,  @Nullable EnumFacing facing) {
        return capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getCapability(TileEntity that, Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return capability.cast((T) that);
        }
        return null;
    }
}
