package org.eientei.gtce2oc.tile;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.pipenet.tile.AttachmentType;
import gregtech.common.pipelike.cable.net.EnergyNet;
import gregtech.common.pipelike.cable.net.WorldENet;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Visibility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.eientei.gtce2oc.driver.values.MachineObject;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TileEntityGTCEBridge extends TileEntityOCComponent implements ICapabilityProvider, IEnergyContainer {
    private long lastUpdate = 0;
    private final List<BlockPos> activeNodes = new ArrayList<>();
    private EnumFacing frontFacing = EnumFacing.UP;
    private IBlockState state;
    private WeakReference<EnergyNet> currentEnergyNet = new WeakReference<>(null);

    public TileEntityGTCEBridge() {
        super("gtce_bridge", Visibility.Network);
    }

    @Callback(doc = "function():table -- Returns machines list.")
    public Object[] getMachines(Context context, Arguments args) {
        return new Object[] {getMachines()};
    }

    @Callback(doc = "function():boolean -- Returns have EnergyNet updated.")
    public Object[] isDirty(Context context, Arguments args) {
        EnergyNet energyNet = getEnergyNet();
        if (energyNet != null) {
            if (lastUpdate != energyNet.getLastUpdate()) {
                return new Object[] {true};
            }
        }
        return new Object[] {false};
    }

    public IBlockState getBlockState() {
        if (this.state == null) {
            this.state = this.world.getBlockState(this.pos);
        }
        return this.state;
    }

    private List<MachineObject> getMachines() {
        updateNodes();
        List<MachineObject> machines = new ArrayList<>();
        for (BlockPos pos : activeNodes) {
            BlockPos.PooledMutableBlockPos blockPos = BlockPos.PooledMutableBlockPos.retain();
            TileEntity tileEntityCable = world.getTileEntity(pos);
            if (!(tileEntityCable instanceof TileEntityCable)) {
                continue;
            }
            for (EnumFacing facing : EnumFacing.VALUES) {
                blockPos.setPos(pos).move(facing);
                if (!world.isBlockLoaded(pos)) {
                    continue; //do not allow cables to load chunks
                }
                TileEntity tileEntity = world.getTileEntity(blockPos);
                if (tileEntity instanceof MetaTileEntityHolder
                        && !((TileEntityCable) tileEntityCable).isConnectionBlocked(AttachmentType.PIPE, facing)) {
                    MachineObject mo = new MachineObject((MetaTileEntityHolder) tileEntity);
                    if (!machines.contains(mo)) {
                        machines.add(mo);
                    }
                }
            }
        }
        return machines;
    }

    private void updateNodes() {
        EnergyNet energyNet = getEnergyNet();
        if (energyNet == null) {
            return;
        }
        if (energyNet.getLastUpdate() == lastUpdate) {
            return;
        }
        lastUpdate = energyNet.getLastUpdate();
        activeNodes.clear();
        energyNet.getAllNodes().forEach((pos, node) -> {
            if (node.isActive) {
                activeNodes.add(pos);
            }
        });
    }

    private EnergyNet getEnergyNet() {
        if (!world.isRemote) {
            TileEntity te = this.world.getTileEntity(new BlockPos.MutableBlockPos(this.pos).move(frontFacing));
            if (te instanceof TileEntityCable) {
                TileEntityCable tileEntityCable = (TileEntityCable) te;
                EnergyNet currentEnergyNet = this.currentEnergyNet.get();
                if (currentEnergyNet != null && currentEnergyNet.isValid()
                        && currentEnergyNet.containsNode(tileEntityCable.getPipePos())) {
                    return currentEnergyNet; //return current net if it is still valid
                }
                WorldENet worldENet =
                        (WorldENet) tileEntityCable.getPipeBlock().getWorldPipeNet(tileEntityCable.getPipeWorld());
                currentEnergyNet = worldENet.getNetFromPos(tileEntityCable.getPipePos());
                if (currentEnergyNet != null) {
                    this.currentEnergyNet = new WeakReference<>(currentEnergyNet);
                }
                return currentEnergyNet;
            }
        }
        return null;
    }

    public EnumFacing getFrontFacing() {
        return frontFacing;
    }

    public void setFrontFacing(EnumFacing frontFacing) {
        if (this.frontFacing == frontFacing) {
            return;
        }
        this.frontFacing = frontFacing;
        markDirty();
        if (world != null) {
            world.notifyBlockUpdate(this.pos, getBlockState(), getBlockState(), 3);
            world.notifyNeighborsOfStateChange(this.pos, this.blockType, true);
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 1, this.getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        final NBTTagCompound data = new NBTTagCompound();
        data.setInteger("FACING", frontFacing.getIndex());
        data.setInteger("x", this.pos.getX());
        data.setInteger("y", this.pos.getY());
        data.setInteger("z", this.pos.getZ());
        return data;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        EnumFacing facing = EnumFacing.values()[tag.getInteger("FACING")];
        if (facing != frontFacing) {
            frontFacing = facing;
            world.notifyBlockUpdate(this.pos, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        frontFacing = EnumFacing.values()[compound.getInteger("FACING")];
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("FACING", frontFacing.getIndex());
        return super.writeToNBT(compound);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER && frontFacing == facing)
                || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER && frontFacing == facing) {
            return (T) this;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public long acceptEnergyFromNetwork(EnumFacing side, long voltage, long amperage) {
        return 0;
    }

    @Override
    public boolean inputsEnergy(EnumFacing side) {
        return false;
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        return 0;
    }

    @Override
    public long getEnergyStored() {
        return 0;
    }

    @Override
    public long getEnergyCapacity() {
        return 0;
    }

    @Override
    public long getInputAmperage() {
        return 0;
    }

    @Override
    public long getInputVoltage() {
        return 0;
    }
}
