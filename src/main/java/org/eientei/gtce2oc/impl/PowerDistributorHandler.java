package org.eientei.gtce2oc.impl;

import gregtech.api.capability.GregtechCapabilities;
import li.cil.oc.common.tileentity.traits.PowerBalancer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.math.BigInteger;

import static org.eientei.gtce2oc.GTCE2OC.*;

public class PowerDistributorHandler {
    public static long acceptEnergyFromNetwork(PowerBalancer that, EnumFacing inputSide, long voltage, long amperage) {
        if (!inputsEnergy(that, inputSide)) {
            return 0;
        }

        if (voltage > getInputVoltage(that)) {
            EnergyContainerUpdater.blowup((TileEntity) that, voltage);
            return Math.min(amperage, getInputAmperage(that));
        }

        if (amperage == 0) {
            return 0;
        }

        int sides = 0;
        int outputs = 0;
        for (EnumFacing side : CONFIG.get(POWER_DISTRIBUTOR).getOutputs()) {
            if (that.canConnect(side)) {
                sides |= 1<<side.ordinal();
                outputs++;
            }
        }

        long accepted = 0;
        for (EnumFacing side : CONFIG.get(POWER_DISTRIBUTOR).getOutputs()) {
            if ((sides & (1 << side.ordinal())) != 0) {
                long suggested = (amperage - accepted) / outputs;
                if (suggested == 0) {
                    suggested = 1;
                }

                accepted += EnergyContainerUpdater.suggest((TileEntity) that, side, voltage, suggested);
                if (accepted >= amperage) {
                    break;
                }
            }
        }

        return accepted;
    }

    public static boolean inputsEnergy(PowerBalancer that, EnumFacing side) {
        if (that.canConnect(side)) {
            for (EnumFacing input : CONFIG.get(POWER_DISTRIBUTOR).getInputs()) {
                if (input == side || side == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean outputsEnergy(PowerBalancer that, EnumFacing side) {
        if (that.canConnect(side)) {
            for (EnumFacing input : CONFIG.get(POWER_DISTRIBUTOR).getOutputs()) {
                if (input == side || side == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static long addEnergy(PowerBalancer that, long energy) {
        return 0;
    }

    public static boolean canUse(PowerBalancer that, long energy) {
        return false;
    }

    public static long getEnergyCanBeInserted(PowerBalancer that) {
        return Integer.MAX_VALUE;
    }

    public static long getEnergyStored(PowerBalancer that) {
        return 0;
    }

    public static long getEnergyCapacity(PowerBalancer that) {
        return Integer.MAX_VALUE;
    }

    public static BigInteger getEnergyStoredActual(PowerBalancer that) {
        return BigInteger.valueOf(getEnergyStored(that));
    }

    public static BigInteger getEnergyCapacityActual(PowerBalancer that) {
        return BigInteger.valueOf(getEnergyCapacity(that));
    }

    public static long getOutputAmperage(PowerBalancer that) {
        return CONFIG.get(POWER_DISTRIBUTOR).getOutputAmperage();
    }

    public static long getOutputVoltage(PowerBalancer that) {
        return CONFIG.get(POWER_DISTRIBUTOR).getOutputVoltage();
    }

    public static long getInputAmperage(PowerBalancer that) {
        return CONFIG.get(POWER_DISTRIBUTOR).getInputAmperage();
    }

    public static long getInputVoltage(PowerBalancer that) {
        return CONFIG.get(POWER_DISTRIBUTOR).getInputVoltage();
    }

    public static boolean isSummationOverflowSafe(PowerBalancer that) {
        return true;
    }

    public static boolean isOneProbeHidden(PowerBalancer that) {
        return true;
    }


    public static void update(PowerBalancer that) {

    }

    public static boolean hasCapability(PowerBalancer that, @Nullable Capability<?> capability, @Nullable EnumFacing side) {
        return (inputsEnergy(that, side) || outputsEnergy(that, side)) && capability != null && capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getCapability(PowerBalancer that, @Nullable Capability<T> capability, @Nullable EnumFacing side) {
        if ((inputsEnergy(that, side) || outputsEnergy(that, side)) && capability != null && capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return capability.cast((T) that);
        }
        return null;
    }
}