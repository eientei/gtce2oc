package org.eientei.gtce2oc.impl;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public final class EnergyContainerUpdater {
    private EnergyContainerUpdater() {
    }

    public static void blowup(TileEntity tileEntity, long voltage) {
        BlockPos pos = tileEntity.getPos();
        tileEntity.getWorld().setBlockToAir(pos);
        if (ConfigHolder.doExplosions) {
            tileEntity.getWorld().createExplosion(null,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    GTUtility.getTierByVoltage(voltage),
                    true);
        }
    }

    public static long suggest(TileEntity tile, EnumFacing side, long voltage, long amperage) {
        TileEntity otherEntity = tile.getWorld().getTileEntity(tile.getPos().offset(side));
        EnumFacing otherSide = side.getOpposite();
        if (otherEntity == null
                || !otherEntity.hasCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, otherSide)) {
            return 0;
        }

        IEnergyContainer otherEnergyContainer =
                otherEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, otherSide);
        if (otherEnergyContainer == null || !otherEnergyContainer.inputsEnergy(otherSide)) {
            return 0;
        }

        return otherEnergyContainer.acceptEnergyFromNetwork(otherSide, voltage, amperage);
    }
}
