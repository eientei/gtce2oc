package org.eientei.gtce2oc.driver;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ParsedRecipe {
    private List<ItemStack> inputs;
    private final List<FluidStack> inputFluids;
    private final List<FluidStack> outputFluids;
    private final List<ItemStack> outputs;
    private final List<ChancedOutput> chancedOutputs;
    private final int eu;
    private final int duration;

    public ParsedRecipe(List<ItemStack> inputs, List<FluidStack> inputFluids, List<FluidStack> outputFluids,  List<ItemStack> outputs, List<ChancedOutput> chancedOutputs, int eu, int duration) {
        this.inputs = inputs;
        this.inputFluids = inputFluids;
        this.outputFluids = outputFluids;
        this.outputs = outputs;
        this.chancedOutputs = chancedOutputs;
        this.eu = eu;
        this.duration = duration;
    }

    public List<ItemStack> getInputs() {
        return inputs;
    }

    public void setInputs(List<ItemStack> inputs) {
        this.inputs = inputs;
    }
    public List<FluidStack> getInputFluids() {
        return inputFluids;
    }

    public List<FluidStack> getOutputFluids() {
        return outputFluids;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    public List<ChancedOutput> getChancedOutputs() {
        return chancedOutputs;
    }

    public int getEu() {
        return eu;
    }

    public int getDuration() {
        return duration;
    }
}
