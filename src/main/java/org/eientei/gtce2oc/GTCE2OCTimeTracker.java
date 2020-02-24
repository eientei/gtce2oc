package org.eientei.gtce2oc;

import com.google.common.collect.ImmutableList;
import gregtech.api.capability.IEnergyContainer;
import li.cil.oc.common.tileentity.traits.PowerAcceptor;
import li.cil.oc.common.tileentity.traits.PowerBalancer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.server.timings.ForgeTimings;
import net.minecraftforge.server.timings.TimeTracker;
import org.eientei.gtce2oc.impl.EnergyContainerTickable;

public class GTCE2OCTimeTracker extends TimeTracker<TileEntity> {
    private final TimeTracker<TileEntity> tracker;

    public GTCE2OCTimeTracker(TimeTracker<TileEntity> tracker) {
        this.tracker = tracker;
    }

    @Override
    public ImmutableList<ForgeTimings<TileEntity>> getTimingData() {
        return tracker.getTimingData();
    }

    @Override
    public void reset() {
        tracker.reset();
    }

    @Override
    public void trackEnd(TileEntity tracking) {
        if ((tracking instanceof PowerAcceptor || tracking instanceof PowerBalancer) && tracking.hasCapability(GTCE2OC.ENERGY_CONTAINER_CAP, null)) {
            IEnergyContainer capability = tracking.getCapability(GTCE2OC.ENERGY_CONTAINER_CAP, null);
            if (capability instanceof EnergyContainerTickable) {
                ((EnergyContainerTickable) capability).updateEnergyContainer();
            }
        }
        tracker.trackEnd(tracking);
    }

    @Override
    public void enable(int duration) {
        tracker.enable(duration);
    }

    @Override
    public void trackStart(TileEntity toTrack) {
        tracker.trackStart(toTrack);
    }
}
