package org.eientei.gtce2oc.impl;

import gregtech.api.capability.GregtechCapabilities;
import li.cil.oc.api.network.Connector;
import li.cil.oc.common.tileentity.PowerConverter;
import li.cil.oc.common.tileentity.traits.PowerAcceptor;
import li.cil.oc.integration.util.Power;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import org.eientei.gtce2oc.MachineConfig;
import scala.Option;

import javax.annotation.Nullable;
import java.math.BigInteger;

import static org.eientei.gtce2oc.GTCE2OC.*;

public class GenericHandler  {
    private static ThreadLocal<Connector[]> connectorset = ThreadLocal.withInitial(() -> new Connector[EnumFacing.values().length]);

    public static MachineConfig getConfig(PowerAcceptor that) {
        if (that instanceof PowerConverter) {
            return CONFIG.get(POWER_CONVERTER);
        }
        return CONFIG.get(OTHERS);
    }

    public static long addEnergy(PowerAcceptor that, long energy) {
        return changeEnergy(that, energy);
    }

    public static long removeEnergy(PowerAcceptor that, long energy) {
        return changeEnergy(that, -energy) * -1;
    }

    public static long acceptEnergyFromNetwork(PowerAcceptor that, EnumFacing inputSide, long voltage, long amperage) {
        if (!inputsEnergy(that, inputSide)) {
            return 0;
        }

        if (voltage > getInputVoltage(that)) {
            EnergyContainerUpdater.blowup((TileEntity) that, voltage);
            return Math.min(amperage, getInputAmperage(that));
        }

        long insertable = Math.min(getEnergyCanBeInserted(that) / voltage, amperage);
        addEnergy(that, insertable * voltage);
        return insertable;
    }

    public static boolean inputsEnergy(PowerAcceptor that, EnumFacing side) {
        if (that.canConnectPower(side)) {
            for (EnumFacing input : getConfig(that).getInputs()) {
                if (input == side || side == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean outputsEnergy(PowerAcceptor that, EnumFacing side) {
        if (that.canConnectPower(side)) {
            for (EnumFacing input : getConfig(that).getOutputs()) {
                if (input == side || side == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static long changeEnergy(PowerAcceptor that, long energy) {
        if (energy == 0) {
            return 0;
        }
        double unused = Power.fromEU(energy);

        EnumFacing[] sides = energy < 0 ? getConfig(that).getOutputs() : getConfig(that).getInputs();
        for (Connector conn : connectors(that, sides)) {
            if (conn == null) {
                break;
            }
            unused = conn.changeBuffer(unused);
        }

        return (long) (energy - Power.toEU(unused));
    }

    public static boolean canUse(PowerAcceptor that, long energy) {
        return getEnergyStored(that) >= energy;
    }

    public static long getEnergyCanBeInserted(PowerAcceptor that) {
        return getEnergyCapacity(that) - getEnergyStored(that);
    }

    public static Connector[] connectors(PowerAcceptor that, EnumFacing[] sides) {
        Connector[] set = connectorset.get();
        for (int i = 0; i < EnumFacing.values().length; i++) {
            set[i] = null;
        }

        int ptr = 0;
        loop: for (EnumFacing side : sides) {
            if (that.canConnectPower(side)) {
                Option<Connector> connopt = that.connector(side);
                if(connopt.isEmpty()) {
                    continue;
                }
                Connector conn = connopt.get();
                for (int i = 0; i < ptr; i++) {
                    if (set[i].equals(conn)) {
                        continue loop;
                    }
                }
                set[ptr++] = conn;
            }
        }
        return set;
    }

    public static long getEnergyStored(PowerAcceptor that) {
        double amount = 0;
        for (Connector conn : connectors(that, getConfig(that).getInputs())) {
            if (conn == null) {
                break;
            }
            amount += conn.localBuffer();
        }
        return (long) Power.toEU(amount);
    }

    public static long getEnergyCapacity(PowerAcceptor that) {
        double amount = 0;
        for (Connector conn : connectors(that, getConfig(that).getInputs())) {
            if (conn == null) {
                break;
            }
            amount += conn.localBufferSize();
        }
        return (long) Power.toEU(amount);
    }

    public static BigInteger getEnergyStoredActual(PowerAcceptor that) {
        return BigInteger.valueOf(getEnergyStored(that));
    }

    public static BigInteger getEnergyCapacityActual(PowerAcceptor that) {
        return BigInteger.valueOf(getEnergyCapacity(that));
    }

    public static long getOutputAmperage(PowerAcceptor that) {
        return getConfig(that).getOutputAmperage();
    }

    public static long getOutputVoltage(PowerAcceptor that) {
        return getConfig(that).getOutputVoltage();
    }

    public static long getInputAmperage(PowerAcceptor that) {
        return getConfig(that).getInputAmperage();
    }

    public static long getInputVoltage(PowerAcceptor that) {
        return getConfig(that).getInputVoltage();
    }

    public static boolean isSummationOverflowSafe(PowerAcceptor that) {
        return true;
    }

    public static boolean isOneProbeHidden(PowerAcceptor that) {
        return false;
    }

    public static void update(PowerAcceptor that) {
        if (((TileEntity)that).getWorld().isRemote) {
            return;
        }

        long stored = getEnergyStored(that);
        if (stored == 0) {
            return;
        }
        long outputVoltage = getConfig(that).getOutputVoltage();
        long outputAmperage = getConfig(that).getOutputAmperage();

        long amperes = Math.min(outputAmperage, stored / outputVoltage);

        if (amperes == 0) {
            return;
        }

        int sides = 0;
        int outputs = 0;

        for (EnumFacing side : getConfig(that).getOutputs()) {
            if (that.canConnectPower(side)) {
                sides |= 1<<side.ordinal();
                outputs++;
            }
        }

        long accepted = 0;
        for (EnumFacing side : getConfig(that).getOutputs()) {
            if ((sides & (1 << side.ordinal())) != 0) {
                long suggested = (amperes - accepted) / outputs;
                if (suggested == 0) {
                    suggested = 1;
                }

                accepted += EnergyContainerUpdater.suggest((TileEntity) that, side, outputVoltage, suggested);
                if (accepted >= amperes) {
                    break;
                }
            }
        }
        addEnergy(that, -accepted * outputVoltage);
    }

    public static boolean hasCapability(PowerAcceptor that, @Nullable Capability<?> capability, @Nullable EnumFacing side) {
        return (inputsEnergy(that, side) || outputsEnergy(that, side)) && capability != null && capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getCapability(PowerAcceptor that, @Nullable Capability<T> capability, @Nullable EnumFacing side) {
        if ((inputsEnergy(that, side) || outputsEnergy(that, side)) && capability != null && capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER) {
            return capability.cast((T) that);
        }
        return null;
    }
}