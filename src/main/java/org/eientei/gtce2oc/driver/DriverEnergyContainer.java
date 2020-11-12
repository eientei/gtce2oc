package org.eientei.gtce2oc.driver;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import li.cil.oc.integration.ManagedTileEntityEnvironment;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DriverEnergyContainer extends DriverSidedTileEntity {
    @Override
    public Class<?> getTileEntityClass() {
        return IEnergyContainer.class;
    }

    @Override
    public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IEnergyContainer) {
            return true;
        }
        if (tileEntity instanceof MetaTileEntityHolder) {
            return tileEntity.hasCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side);
        }
        return false;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IEnergyContainer) {
            return new Environment((IEnergyContainer) tileEntity);
        }
        if (tileEntity instanceof MetaTileEntityHolder) {
            return new Environment(
                    tileEntity.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side),
                    (MetaTileEntityHolder) tileEntity
            );
        }
        return null;
    }

    public static final class Environment extends ManagedTileEntityEnvironment<IEnergyContainer> implements NamedBlock {
        private String preferredName = "gtce_energyContainer";

        public Environment(IEnergyContainer tileEntity) {
            super(tileEntity, "gtce_energyContainer");
        }

        public Environment(IEnergyContainer tileEntity, MetaTileEntityHolder holder) {
            this(tileEntity);
            preferredName = holder.getMetaTileEntity().metaTileEntityId.getPath();
        }

        @Callback(doc = "function():number --  Returns the amount of electricity contained in this Block, in EU units!")
        public Object[] getEnergyStored(final Context context, final Arguments args) {
            return new Object[]{tileEntity.getEnergyStored()};
        }

        @Callback(doc = "function():number --  "
                + "Returns the amount of electricity containable in this Block, in EU units!")
        public Object[] getEnergyCapacity(final Context context, final Arguments args) {
            return new Object[]{tileEntity.getEnergyCapacity()};
        }

        @Callback(doc = "function():number --  Gets the Output in EU/p.")
        public Object[] getOutputVoltage(final Context context, final Arguments args) {
            return new Object[]{tileEntity.getOutputVoltage()};
        }

        @Callback(doc = "function():number -- Gets the amount of Energy Packets per tick.")
        public Object[] getOutputAmperage(final Context context, final Arguments args) {
            return new Object[]{tileEntity.getOutputAmperage()};
        }

        @Callback(doc = "function():number -- Gets the maximum Input in EU/p.")
        public Object[] getInputVoltage(final Context context, final Arguments args) {
            return new Object[]{tileEntity.getInputVoltage()};
        }

        @Callback(doc = "function():number -- Gets the amount of Energy Packets per tick.")
        public Object[] getInputAmperage(final Context context, final Arguments args) {
            return new Object[]{tileEntity.getInputAmperage()};
        }

        @Override
        public String preferredName() {
            return preferredName;
        }

        @Override
        public int priority() {
            return 0;
        }
    }
}
