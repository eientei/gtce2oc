package org.eientei.gtce2oc;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.eientei.gtce2oc.block.BlockGTCEBridge;
import org.eientei.gtce2oc.tile.TileEntityGTCEBridge;

public class ObjectRegistryHandler {
    @GameRegistry.ObjectHolder(GTCE2OC.MODID + ":" + BlockGTCEBridge.NAME)
    public static BlockGTCEBridge BLOCK_GTCE_BRIDGE;

    public ObjectRegistryHandler() {
        BLOCK_GTCE_BRIDGE = new BlockGTCEBridge();
    }

    @SubscribeEvent
    public void addItems(RegistryEvent.Register<Item> event) {
        ItemBlock itemGTCEBridge = new ItemBlock(BLOCK_GTCE_BRIDGE);
        itemGTCEBridge.setRegistryName(BLOCK_GTCE_BRIDGE.getRegistryName());
        itemGTCEBridge.setTranslationKey(BLOCK_GTCE_BRIDGE.getTranslationKey());
        itemGTCEBridge.setCreativeTab(GTCE2OC.CREATIVE_TABS);
        event.getRegistry()
                .register(new ItemBlock(BLOCK_GTCE_BRIDGE).setRegistryName(BLOCK_GTCE_BRIDGE.getRegistryName()));
    }

    @SubscribeEvent
    public void addBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(BLOCK_GTCE_BRIDGE);
        GameRegistry.registerTileEntity(TileEntityGTCEBridge.class,
                new ResourceLocation(GTCE2OC.MODID, BLOCK_GTCE_BRIDGE.NAME));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRegisterModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BLOCK_GTCE_BRIDGE), 0,
                new ModelResourceLocation(BLOCK_GTCE_BRIDGE.getRegistryName().toString()));
    }
}
