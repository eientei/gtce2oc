package org.eientei.gtce2oc.reference;

import gregtech.api.capability.IEnergyContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.eientei.gtce2oc.impl.EnergyContainerStatic;

import java.math.BigInteger;

public class EnergyContainerExample extends TileEntity implements IEnergyContainer {
    @Override
    public long acceptEnergyFromNetwork(EnumFacing enumFacing, long volatage, long amperage) {
        return EnergyContainerStatic.acceptEnergyFromNetwork(this, enumFacing, volatage, amperage);
    }

    @Override
    public boolean inputsEnergy(EnumFacing enumFacing) {
        return EnergyContainerStatic.inputsEnergy(this, enumFacing);
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return EnergyContainerStatic.outputsEnergy(this, side);
    }

    @Override
    public long addEnergy(long energy) {
        return EnergyContainerStatic.addEnergy(this, energy);
    }

    @Override
    public boolean canUse(long energy) {
        return EnergyContainerStatic.canUse(this, energy);
    }

    @Override
    public long getEnergyCanBeInserted() {
        return EnergyContainerStatic.getEnergyCanBeInserted(this);
    }

    @Override
    public long getEnergyStored() {
        return EnergyContainerStatic.getEnergyStored(this);
    }

    @Override
    public long getEnergyCapacity() {
        return EnergyContainerStatic.getEnergyCapacity(this);
    }

    @Override
    public BigInteger getEnergyStoredActual() {
        return EnergyContainerStatic.getEnergyStoredActual(this);
    }

    @Override
    public BigInteger getEnergyCapacityActual() {
        return EnergyContainerStatic.getEnergyCapacityActual(this);
    }

    @Override
    public long getOutputAmperage() {
        return EnergyContainerStatic.getOutputAmperage(this);
    }

    @Override
    public long getOutputVoltage() {
        return EnergyContainerStatic.getOutputVoltage(this);
    }

    @Override
    public long getInputAmperage() {
        return EnergyContainerStatic.getInputAmperage(this);
    }

    @Override
    public long getInputVoltage() {
        return EnergyContainerStatic.getInputVoltage(this);
    }

    @Override
    public boolean isSummationOverflowSafe() {
        return EnergyContainerStatic.isSummationOverflowSafe(this);
    }

    @Override
    public boolean isOneProbeHidden() {
        return EnergyContainerStatic.isOneProbeHidden(this);
    }
}
