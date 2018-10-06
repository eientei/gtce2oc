package org.eientei.gtce2oc.impl;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.math.BigInteger;

import static org.eientei.gtce2oc.GTCE2OC.logger;

public class EnergyContainerStatic {
    public static long acceptEnergyFromNetwork(TileEntity that, EnumFacing enumFacing, long voltage, long amperage) {
        logger.info("{}.acceptEnergyFromNetwork {} v:{} a:{}", String.valueOf(that), enumFacing.toString(), voltage, amperage);
        return amperage; // black box
    }

    public static boolean inputsEnergy(TileEntity that, EnumFacing enumFacing) {
        logger.info("{}.inputsEnergy {}", String.valueOf(that), enumFacing.toString());
        return true;
    }

    public static boolean outputsEnergy(TileEntity that, EnumFacing side) {
        logger.info("{}.outputsEnergy {}", String.valueOf(that), side.toString());
        return false;
    }

    public static long addEnergy(TileEntity that, long energy) {
        logger.info("{}.addEnergy {}", String.valueOf(that), energy);
        return energy;
    }

    public static boolean canUse(TileEntity that, long energy) {
        logger.info("{}.canUse {}", String.valueOf(that), energy);
        return false;
    }

    public static long getEnergyCanBeInserted(TileEntity that) {
        logger.info("{}.getEnergyCanBeInserted", String.valueOf(that));
        return Long.MAX_VALUE;
    }

    public static long getEnergyStored(TileEntity that) {
        logger.info("{}.getEnergyStored", String.valueOf(that));
        return 0;
    }

    public static long getEnergyCapacity(TileEntity that) {
        logger.info("{}.getEnergyCapacity", String.valueOf(that));
        return Long.MAX_VALUE;
    }

    public static BigInteger getEnergyStoredActual(TileEntity that) {
        logger.info("{}.getEnergyStoredActual", String.valueOf(that));
        return BigInteger.ZERO;
    }

    public static BigInteger getEnergyCapacityActual(TileEntity that) {
        logger.info("{}.getEnergyCapacityActual", String.valueOf(that));
        return BigInteger.valueOf(Long.MAX_VALUE);
    }

    public static long getOutputAmperage(TileEntity that) {
        logger.info("{}.getOutputAmperage", String.valueOf(that));
        return 0;
    }

    public static long getOutputVoltage(TileEntity that) {
        logger.info("{}.getOutputVoltage", String.valueOf(that));
        return 0;
    }

    public static long getInputAmperage(TileEntity that) {
        logger.info("{}.getInputAmperage", String.valueOf(that));
        return 1;
    }

    public static long getInputVoltage(TileEntity that) {
        logger.info("{}.getInputVoltage", String.valueOf(that));
        return 128;
    }

    public static boolean isSummationOverflowSafe(TileEntity that) {
        logger.info("{}.isSummationOverflowSafe", String.valueOf(that));
        return true;
    }

    public static boolean isOneProbeHidden(TileEntity that) {
        logger.info("{}.isOneProbeHidden", String.valueOf(that));
        return false;
    }
}
