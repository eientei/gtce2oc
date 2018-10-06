package org.eientei.gtce2oc.reference;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.eientei.gtce2oc.impl.TileEntityStatic;

import javax.annotation.Nullable;

public class TileEntityExample extends TileEntity {
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return TileEntityStatic.hasCapability(this, capability, facing) || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        T value = TileEntityStatic.getCapability(this, capability, facing);
        return (value == null) ? super.getCapability(capability, facing) : value;
    }
}
