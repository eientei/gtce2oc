package org.eientei.gtce2oc.tile;

import li.cil.oc.api.API;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.TileEntityEnvironment;
import net.minecraft.nbt.NBTTagCompound;

public abstract class TileEntityOCComponent extends TileEntityEnvironment implements ManagedEnvironment {
    private boolean addedToNetwork;
    private final String name;
    private Visibility visibility;

    public TileEntityOCComponent(String name, Visibility visibility) {
        this.name = name;
        this.visibility = visibility;
        setupNode();
    }

    @Override
    public void load(NBTTagCompound nbt) {
        if(nbt.hasKey("visibility"))
            this.visibility = Visibility.values()[nbt.getInteger("visibility")];

        if(nbt.hasKey("node"))
            node.load(nbt.getCompoundTag("node"));
    }

    @Override
    public void save(NBTTagCompound nbt) {
        setupNode();
        if(node == null)
            return;

        NBTTagCompound nodeTag = new NBTTagCompound();
        node.save(nodeTag);
        nbt.setTag("node", nodeTag);

        nbt.setInteger("visibility", visibility.ordinal());
    }

    private void setupNode(){
        if(this.node() == null || this.node().network() == null)
            this.node = API.network.newNode(this, visibility).withComponent(getComponentName()).withConnector().create();
    }

    @Override
    public boolean canUpdate(){
        return false;
    }

    public void sendComputerSignal(String eventType, String name){
        if(node == null) return;
        node.sendToReachable("computer.signal", eventType.toLowerCase(), name);
    }

    public String getComponentName() { return this.name; }

    @Override
    public void update() {
        if (!addedToNetwork) {
            addedToNetwork = true;
            Network.joinOrCreateNetwork(this);
        }
    }
}
