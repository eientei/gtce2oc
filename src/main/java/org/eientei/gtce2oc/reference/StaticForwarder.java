package org.eientei.gtce2oc.reference;

import gregtech.api.capability.IEnergyContainer;
import li.cil.oc.common.tileentity.PowerConverter;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import org.eientei.gtce2oc.impl.GenericHandler;

import javax.annotation.Nullable;

public class StaticForwarder extends PowerConverter implements IEnergyContainer, ITickable {

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltge, long amperage) {
        return GenericHandler.acceptEnergyFromNetwork(this, side, voltge, amperage);
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return GenericHandler.inputsEnergy(this, side);
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        return GenericHandler.outputsEnergy(this, side);
    }

    @Override
    public long changeEnergy(long energy) {
        return GenericHandler.changeEnergy(this, energy);
    }

    @Override
    public long addEnergy(long energy) {
        return GenericHandler.addEnergy(this, energy);
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        return GenericHandler.removeEnergy(this, energyToRemove);
    }

    @Override
    public long getEnergyCanBeInserted() {
        return GenericHandler.getEnergyCanBeInserted(this);
    }

    @Override
    public long getEnergyStored() {
        return GenericHandler.getEnergyStored(this);
    }

    @Override
    public long getEnergyCapacity() {
        return GenericHandler.getEnergyCapacity(this);
    }

    @Override
    public long getOutputAmperage() {
        return GenericHandler.getOutputAmperage(this);
    }

    @Override
    public long getOutputVoltage() {
        return GenericHandler.getOutputVoltage(this);
    }

    @Override
    public long getInputAmperage() {
        return GenericHandler.getInputAmperage(this);
    }

    @Override
    public long getInputVoltage() {
        return GenericHandler.getInputVoltage(this);
    }

    @Override
    public boolean isOneProbeHidden() {
        return GenericHandler.isOneProbeHidden(this);
    }

    @Override
    public void update() {
        __gtce2oc__update();
        GenericHandler.update(this);
    }

    @Override
    public boolean hasCapability(@Nullable Capability<?> capability, @Nullable EnumFacing side) {
        if (GenericHandler.hasCapability(this, capability, side)) {
            return true;
        }
        if (__gtce2oc__hasCapability(capability, side)) {
            return true;
        }
        if (super.hasCapability(capability, side)) {
            return true;
        }
        return false;
    }

    @Override
    public <T> T getCapability(@Nullable Capability<T> capability, @Nullable EnumFacing side) {
        if (GenericHandler.hasCapability(this, capability, side)) {
            return GenericHandler.getCapability(this, capability, side);
        }
        if (__gtce2oc__hasCapability(capability, side)) {
            return __gtce2oc__getCapability(capability, side);
        }
        if (super.hasCapability(capability, side)) {
            return super.getCapability(capability, side);
        }
        return null;
    }

    public void __gtce2oc__update() {
    }

    public boolean __gtce2oc__hasCapability(@Nullable Capability<?> capability, @Nullable EnumFacing side) {
        return false;
    }

    public <T> T __gtce2oc__getCapability(@Nullable Capability<T> capability, @Nullable EnumFacing side) {
        return null;
    }
}
