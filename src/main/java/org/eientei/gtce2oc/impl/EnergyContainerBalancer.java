package org.eientei.gtce2oc.impl;

import gregtech.api.capability.IEnergyContainer;
import li.cil.oc.common.tileentity.traits.PowerBalancer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class EnergyContainerBalancer implements IEnergyContainer, EnergyContainerTickable {
    private PowerBalancer powerBalancer;
    private final MachineConfig config;

    public EnergyContainerBalancer(PowerBalancer powerBalancer, MachineConfig config) {
        this.powerBalancer = powerBalancer;
        this.config = config;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing inputSide, long voltage, long amperage) {
        if (!inputsEnergy(inputSide)) {
            return 0;
        }

        if (voltage > getInputVoltage()) {
            EnergyContainerUpdater.blowup((TileEntity) powerBalancer, voltage);
            return Math.min(amperage, getInputAmperage());
        }

        if (amperage == 0) {
            return 0;
        }

        int sides = 0;
        int outputs = 0;
        for (EnumFacing side : config.getOutputs()) {
            if (powerBalancer.canConnect(side)) {
                sides |= 1<<side.ordinal();
                outputs++;
            }
        }

        long accepted = 0;
        for (EnumFacing side : config.getOutputs()) {
            if ((sides & (1 << side.ordinal())) != 0) {
                long suggested = (amperage - accepted) / outputs;
                if (suggested == 0) {
                    suggested = 1;
                }

                accepted += EnergyContainerUpdater.suggest((TileEntity) powerBalancer, side, voltage, suggested);
                if (accepted >= amperage) {
                    break;
                }
            }
        }

        return accepted;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        if (powerBalancer.canConnect(side)) {
            for (EnumFacing input : config.getInputs()) {
                if (input == side || side == null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean outputsEnergy(EnumFacing side) {
        if (powerBalancer.canConnect(side)) {
            for (EnumFacing input : config.getOutputs()) {
                if (input == side || side == null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public long changeEnergy(long l) {
        return 0;
    }

    @Override
    public long addEnergy(long energyToAdd) {
        return 0;
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        return 0;
    }

    @Override
    public long getEnergyCanBeInserted() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getEnergyStored() {
        return 0;
    }

    @Override
    public long getEnergyCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long getOutputAmperage() {
        return config.getOutputAmperage();
    }

    @Override
    public long getOutputVoltage() {
        return config.getOutputVoltage();
    }

    @Override
    public long getInputAmperage() {
        return config.getInputAmperage();
    }

    @Override
    public long getInputVoltage() {
        return config.getInputVoltage();
    }

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }

    @Override
    public void updateEnergyContainer() {

    }
}
