package org.eientei.gtce2oc.impl;

import gregtech.api.capability.IEnergyContainer;
import li.cil.oc.api.network.Connector;
import li.cil.oc.common.tileentity.traits.PowerAcceptor;
import li.cil.oc.integration.util.Power;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import scala.Option;

public class EnergyContainerAcceptor implements IEnergyContainer, EnergyContainerTickable {
    private final PowerAcceptor powerAcceptor;
    private final MachineConfig config;
    private final Connector[] connectorset = new Connector[EnumFacing.values().length];

    public EnergyContainerAcceptor(PowerAcceptor powerAcceptor, MachineConfig config) {
        this.powerAcceptor = powerAcceptor;
        this.config = config;
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing inputSide, long voltage, long amperage) {
        if (!inputsEnergy(inputSide)) {
            return 0;
        }

        if (voltage > getInputVoltage()) {
            EnergyContainerUpdater.blowup((TileEntity) powerAcceptor, voltage);
            return Math.min(amperage, getInputAmperage());
        }

        long insertable = Math.min(getEnergyCanBeInserted() / voltage, amperage);
        addEnergy(insertable * voltage);
        return insertable;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        if (powerAcceptor.canConnectPower(side)) {
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
        if (powerAcceptor.canConnectPower(side)) {
            for (EnumFacing input : config.getOutputs()) {
                if (input == side || side == null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public long changeEnergy(long energy) {
        if (energy == 0) {
            return 0;
        }
        double unused = Power.fromEU(energy);

        EnumFacing[] sides = energy < 0 ? config.getOutputs() : config.getInputs();
        for (Connector conn : connectors(sides)) {
            if (conn == null) {
                break;
            }
            unused = conn.changeBuffer(unused);
        }

        return (long) (energy - Power.toEU(unused));
    }

    @Override
    public long addEnergy(long energy) {
        return changeEnergy(energy);
    }

    @Override
    public long removeEnergy(long energyToRemove) {
        return changeEnergy(-energyToRemove) * -1;
    }

    @Override
    public long getEnergyCanBeInserted() {
        return getEnergyCapacity() - getEnergyStored();
    }

    @Override
    public long getEnergyStored() {
        double amount = 0;
        for (Connector conn : connectors(config.getInputs())) {
            if (conn == null) {
                break;
            }
            amount += conn.localBuffer();
        }
        return (long) Power.toEU(amount);
    }

    @Override
    public long getEnergyCapacity() {
        double amount = 0;
        for (Connector conn : connectors(config.getInputs())) {
            if (conn == null) {
                break;
            }
            amount += conn.localBufferSize();
        }
        return (long) Power.toEU(amount);
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
        return false;
    }

    @Override
    public void updateEnergyContainer() {
        if (((TileEntity) powerAcceptor).getWorld().isRemote) {
            return;
        }

        long stored = getEnergyStored();
        if (stored == 0) {
            return;
        }
        long outputVoltage = config.getOutputVoltage();
        long outputAmperage = config.getOutputAmperage();

        long amperes = Math.min(outputAmperage, stored / outputVoltage);

        if (amperes == 0) {
            return;
        }

        int sides = 0;
        int outputs = 0;

        for (EnumFacing side : config.getOutputs()) {
            if (powerAcceptor.canConnectPower(side)) {
                sides |= 1 << side.ordinal();
                outputs++;
            }
        }

        long accepted = 0;
        for (EnumFacing side : config.getOutputs()) {
            if ((sides & (1 << side.ordinal())) != 0) {
                long suggested = (amperes - accepted) / outputs;
                if (suggested == 0) {
                    suggested = 1;
                }

                accepted += EnergyContainerUpdater.suggest((TileEntity) powerAcceptor, side, outputVoltage, suggested);
                if (accepted >= amperes) {
                    break;
                }
            }
        }
        addEnergy(-accepted * outputVoltage);
    }

    public Connector[] connectors(EnumFacing[] sides) {
        for (int i = 0; i < EnumFacing.values().length; i++) {
            connectorset[i] = null;
        }

        int ptr = 0;
        loop: for (EnumFacing side : sides) {
            if (powerAcceptor.canConnectPower(side)) {
                Option<Connector> connopt = powerAcceptor.connector(side);
                if (connopt.isEmpty()) {
                    continue;
                }
                Connector conn = connopt.get();
                for (int i = 0; i < ptr; i++) {
                    if (connectorset[i].equals(conn)) {
                        continue loop;
                    }
                }
                connectorset[ptr++] = conn;
            }
        }
        return connectorset;
    }
}
