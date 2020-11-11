package org.eientei.gtce2oc.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.eientei.gtce2oc.GTCE2OC;
import org.eientei.gtce2oc.tile.TileEntityGTCEBridge;

public class BlockGTCEBridge extends Block implements ITileEntityProvider {
    public static String NAME = "gtce_bridge";
    private static final PropertyDirection FACING = PropertyDirection.create("facing");
    public BlockGTCEBridge() {
        super(Material.IRON);
        this.setRegistryName(NAME);
        this.setTranslationKey(GTCE2OC.MODID+"."+NAME);
        setCreativeTab(GTCE2OC.creativeTab);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityGTCEBridge();
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityGTCEBridge){
            ((TileEntityGTCEBridge) te).setFrontFacing(axis);
            return true;
        }
        return false;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = worldIn.getTileEntity(pos);
        EnumFacing facing = EnumFacing.UP;
        if (te instanceof TileEntityGTCEBridge)
            facing = ((TileEntityGTCEBridge) te).getFrontFacing();
        return super.getActualState(state, worldIn, pos).withProperty(FACING, facing);
    }
}
