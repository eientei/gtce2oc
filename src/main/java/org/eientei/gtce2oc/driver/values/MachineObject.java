package org.eientei.gtce2oc.driver.values;

import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.recipes.CountableIngredient;
import gregtech.api.recipes.Recipe;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.machine.Value;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.*;

public class MachineObject implements Value {
    private BlockPos pos;
    private World world;
    private int dim;

    public MachineObject(MetaTileEntityHolder te) {
        pos = te.getPos();
        dim = te.getWorld().provider.getDimension();
        world = te.getWorld();
    }

    public MachineObject() {
    }

    private MetaTileEntityHolder getTile() {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof MetaTileEntityHolder) {
            return (MetaTileEntityHolder) te;
        }
        return null;
    }


    private <T> T getCapability(Capability<T> capability) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof MetaTileEntityHolder) {
            return te.getCapability(capability, null);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MachineObject that = (MachineObject) o;
        return dim == that.dim && Objects.equals(pos, that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, dim);
    }

    @Override
    public Object apply(Context context, Arguments arguments) {
        return null;
    }

    @Override
    public void unapply(Context context, Arguments arguments) {

    }

    @Override
    public Object[] call(Context context, Arguments arguments) {
        return new Object[0];
    }

    @Override
    public void dispose(Context context) {

    }

    @Override
    public void load(NBTTagCompound nbt) {
        dim = nbt.getInteger("dim");
        pos = NBTUtil.getPosFromTag(nbt.getCompoundTag("pos"));
        world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim);
    }

    @Override
    public void save(NBTTagCompound nbt) {
        nbt.setTag("pos", NBTUtil.createPosTag(pos));
        nbt.setInteger("dim", dim);
    }

    @Callback(doc = "function():boolean --  Returns whether machine valid.")
    public Object[] isValid(final Context context, final Arguments args) {
        if (getTile() == null) {
            return new Object[] {false};
        }
        return new Object[] {true};
    }

    @Callback(doc = "function():string --  Returns machine name.")
    public Object[] getMachineName(final Context context, final Arguments args) {
        MetaTileEntityHolder te = getTile();
        if (te != null) {
            return new Object[] {te.getMetaTileEntity().metaTileEntityId.getPath()};
        }
        return new Object[] {null, "invalid machine"};
    }


    @Callback(doc = "function():table --  Returns current recipe.")
    public Object[] getCurrentRecipe(final Context context, final Arguments args) {
        IWorkable aRL = getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE);
        if (aRL instanceof AbstractRecipeLogic) {
            Recipe previousRecipe = ReflectionHelper.getPrivateValue(AbstractRecipeLogic.class,
                    (AbstractRecipeLogic) aRL, "previousRecipe");
            if (previousRecipe != null && aRL.isActive()) {
                HashMap<String, Object> recipe = new HashMap();
                recipe.put("duration", previousRecipe.getDuration());
                recipe.put("EUt", previousRecipe.getEUt());

                List<Map<String, Object>> itemInput = new ArrayList<>();
                List<CountableIngredient> inputs = previousRecipe.getInputs();
                inputs.forEach(iR -> {
                    for (ItemStack itemStack : iR.getIngredient().getMatchingStacks()) {
                        Map<String, Object> input = new HashMap<>();
                        input.put("count", iR.getCount());
                        input.put("name", itemStack.getDisplayName());
                        itemInput.add(input);
                    }
                });
                if (!itemInput.isEmpty()) {
                    recipe.put("itemInputs", itemInput);
                }

                List<Map<String, Object>> fluidInput = new ArrayList<>();
                List<FluidStack> fluidInputs = previousRecipe.getFluidInputs();
                fluidInputs.forEach(iR -> {
                    Map<String, Object> input = new HashMap<>();
                    input.put("count", iR.amount);
                    input.put("name", iR.getFluid().getName());
                    fluidInput.add(input);
                });
                if (!fluidInput.isEmpty()) {
                    recipe.put("fluidInputs", fluidInput);
                }

                List<Map<String, Object>> itemOutput = new ArrayList<>();
                List<ItemStack> outputs = previousRecipe.getOutputs();
                outputs.forEach(iR -> {
                    Map<String, Object> output = new HashMap<>();
                    output.put("count", iR.getCount());
                    output.put("name", iR.getDisplayName());
                    itemOutput.add(output);
                });
                if (!itemOutput.isEmpty()) {
                    recipe.put("itemOutputs", itemOutput);
                }
                List<Map<String, Object>> chancedItemOutput = new ArrayList<>();
                List<Recipe.ChanceEntry> chancedOutputs = previousRecipe.getChancedOutputs();
                chancedOutputs.forEach(iR -> {
                    Map<String, Object> output = new HashMap<>();
                    output.put("chance", iR.getChance());
                    output.put("boostPerTier", iR.getBoostPerTier());
                    output.put("count", iR.getItemStack().getCount());
                    output.put("name", iR.getItemStack().getDisplayName());
                    chancedItemOutput.add(output);
                });
                if (!chancedItemOutput.isEmpty()) {
                    recipe.put("chancedItemOutput", chancedItemOutput);
                }
                List<Map<String, Object>> fluidOutput = new ArrayList<>();
                List<FluidStack> fluidOutputs = previousRecipe.getFluidOutputs();
                fluidOutputs.forEach(iR -> {
                    Map<String, Object> output = new HashMap<>();
                    output.put("count", iR.amount);
                    output.put("name", iR.getFluid().getName());
                    fluidOutput.add(output);
                });
                if (!fluidOutput.isEmpty()) {
                    recipe.put("fluidOutputs", fluidOutput);
                }
                return new Object[] {recipe};
            }
            return new Object[] {null};
        }
        return new Object[] {null, "invalid machine"};
    }

    @Callback(doc = "function():number --  Returns the amount of electricity contained in this Block, in EU units!")
    public Object[] getEnergyStored(final Context context, final Arguments args) {
        IEnergyContainer tileEntity = getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER);
        if (tileEntity == null) {
            return new Object[] {null, "invalid machine"};
        }
        return new Object[] {tileEntity.getEnergyStored()};
    }

    @Callback(doc = "function():number --  Returns the amount of electricity containable in this Block, in EU units!")
    public Object[] getEnergyCapacity(final Context context, final Arguments args) {
        IEnergyContainer tileEntity = getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER);
        if (tileEntity == null) {
            return new Object[] {null, "invalid machine"};
        }
        return new Object[] {tileEntity.getEnergyCapacity()};
    }

    @Callback(doc = "function():number --  Gets the Output in EU/p.")
    public Object[] getOutputVoltage(final Context context, final Arguments args) {
        IEnergyContainer tileEntity = getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER);
        if (tileEntity == null) {
            return new Object[] {null, "invalid machine"};
        }
        return new Object[] {tileEntity.getOutputVoltage()};
    }

    @Callback(doc = "function():number -- Gets the amount of Energy Packets per tick.")
    public Object[] getOutputAmperage(final Context context, final Arguments args) {
        IEnergyContainer tileEntity = getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER);
        if (tileEntity == null) {
            return new Object[] {null, "invalid machine"};
        }
        return new Object[] {tileEntity.getOutputAmperage()};
    }

    @Callback(doc = "function():number -- Gets the maximum Input in EU/p.")
    public Object[] getInputVoltage(final Context context, final Arguments args) {
        IEnergyContainer tileEntity = getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER);
        if (tileEntity == null) {
            return new Object[] {null, "invalid machine"};
        }
        return new Object[] {tileEntity.getInputVoltage()};
    }

    @Callback(doc = "function():number -- Gets the amount of Energy Packets per tick.")
    public Object[] getInputAmperage(final Context context, final Arguments args) {
        IEnergyContainer tileEntity = getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER);
        if (tileEntity == null) {
            return new Object[] {null, "invalid machine"};
        }
        return new Object[] {tileEntity.getInputAmperage()};
    }

    @Callback(doc = "function():number --  Returns the MaxProgress!")
    public Object[] getMaxProgress(final Context context, final Arguments args) {
        IWorkable tileEntity = getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE);
        if (tileEntity == null) {
            return new Object[] {null, "invalid machine"};
        }
        return new Object[] {tileEntity.getMaxProgress()};
    }

    @Callback(doc = "function():number --  Returns the Progress!")
    public Object[] getProgress(final Context context, final Arguments args) {
        IWorkable tileEntity = getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE);
        if (tileEntity == null) {
            return new Object[] {null, "invalid machine"};
        }
        return new Object[] {tileEntity.getProgress()};
    }

    @Callback(doc = "function():boolean --  Returns is active or not.")
    public Object[] isActive(final Context context, final Arguments args) {
        IWorkable tileEntity = getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE);
        if (tileEntity == null) {
            return new Object[] {null, "invalid machine"};
        }
        return new Object[] {tileEntity.isActive()};
    }

    @Callback(doc = "function():boolean --  Returns is working enabled.")
    public Object[] isWorkingEnabled(final Context context, final Arguments args) {
        IWorkable tileEntity = getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE);
        if (tileEntity == null) {
            return new Object[] {null, "invalid machine"};
        }
        return new Object[] {tileEntity.isWorkingEnabled()};
    }

    @Callback(doc = "function(WorkingEnabled:boolean):boolean --  Sets working enabled, return last working enabled.")
    public Object[] setWorkingEnabled(final Context context, final Arguments args) {
        IWorkable tileEntity = getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE);
        if (tileEntity == null) {
            return new Object[] {null, "invalid machine"};
        }
        boolean lastState = tileEntity.isWorkingEnabled();
        tileEntity.setWorkingEnabled(args.checkBoolean(0));
        return new Object[] {lastState};
    }
}
