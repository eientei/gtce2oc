package org.eientei.gtce2oc;

import li.cil.oc.common.tileentity.PowerConverter;
import li.cil.oc.common.tileentity.traits.PowerAcceptor;
import li.cil.oc.common.tileentity.traits.PowerBalancer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.eientei.gtce2oc.impl.EnergyContainerAcceptor;
import org.eientei.gtce2oc.impl.EnergyContainerBalancer;
import org.eientei.gtce2oc.impl.EnergyContainerTickable;
import org.eientei.gtce2oc.impl.MachineConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GTCE2OCEventHandler {
    public static class EnergyContainerProvider implements ICapabilityProvider {
        private final EnergyContainerTickable impl;

        public EnergyContainerProvider(EnergyContainerTickable impl) {
            this.impl = impl;
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == GTCE2OC.ENERGY_CONTAINER_CAP;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        @Override
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == GTCE2OC.ENERGY_CONTAINER_CAP) {
                return (T) impl;
            }
            return null;
        }
    }

    @SubscribeEvent
    public void attachCapabilityTileEntity(AttachCapabilitiesEvent<TileEntity> event) {
        attachCapability(event);
    }

    @SubscribeEvent
    public void attachCapabilityEntity(AttachCapabilitiesEvent<Entity> event) {
        attachCapability(event);
    }

    private void attachCapability(AttachCapabilitiesEvent<?> event) {
        if (event.getObject() instanceof PowerBalancer) {
            event.addCapability(GTCE2OC.ENERGY_CONTAINER_CAP_PROVIDER, new EnergyContainerProvider(new EnergyContainerBalancer((PowerBalancer) event.getObject(), GTCE2OC.POWER_BALANCER)));
        } else if (event.getObject() instanceof PowerAcceptor) {
            MachineConfig config = GTCE2OC.POWER_ACCEPTOR;
            if (event.getObject() instanceof PowerConverter) {
                config = GTCE2OC.POWER_CONVERTER;
            }
            event.addCapability(GTCE2OC.ENERGY_CONTAINER_CAP_PROVIDER, new EnergyContainerProvider(new EnergyContainerAcceptor((PowerAcceptor) event.getObject(), config)));
        }
    }
}
