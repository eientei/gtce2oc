package org.eientei.gtce2oc.driver;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import li.cil.oc.common.recipe.Recipes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.GameData;
import scala.Function2;
import scala.Option;
import scala.runtime.AbstractFunction2;
import scala.runtime.BoxedUnit;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

import static org.eientei.gtce2oc.GTCE2OC.LOGGER;

public class RecipeIntegration {
    public static class HandlerWrapper extends AbstractFunction2<ItemStack, Config, BoxedUnit> {
        private Consumer<ParsedRecipe> handler;

        public HandlerWrapper(Consumer<ParsedRecipe> handler) {
            this.handler = handler;
        }

        @Override
        public BoxedUnit apply(ItemStack output, Config recipe) {
            handler.accept(parseRecipe(output, recipe));
            return BoxedUnit.UNIT;
        }
    }

    public static String nameItem(Object o) {
        if (o instanceof ItemStack) {
            return ((ItemStack) o).getDisplayName();
        } else if (o instanceof FluidStack) {
            return ((FluidStack) o).getLocalizedName();
        }
        return "???";
    }

    public static void namedList(StringBuilder p, List list) {
        p.append("[");
        boolean first = true;
        for (Object o : list) {
            if (!first) {
                p.append(", ");
            }
            if (o instanceof ItemStack) {
                p.append(nameItem(o));
            } else if (o instanceof FluidStack) {
                p.append(nameItem(o));
            } else if (o instanceof ChancedOutput) {
                ItemStack item = ((ChancedOutput) o).getItem();
                String chance = Recipe.formatChanceValue(((ChancedOutput) o).getChance());
                String boost = Recipe.formatChanceValue(((ChancedOutput) o).getBoost());
                p.append(String.format("(%s+(%s*T))%% of %s", chance, boost, nameItem(item)));
            }
            first = false;
        }
        p.append("]");
    }

    public static void registerRecipeHandlers() throws IllegalAccessException {
        for (Field field : RecipeMaps.class.getDeclaredFields()) {
            if (!field.getType().equals(RecipeMap.class)) {
                continue;
            }
            if (!field.getName().endsWith("_RECIPES")) {
                continue;
            }

            String rawName = field.getName().replace("_RECIPES", "");
            StringBuilder sb = new StringBuilder();
            sb.append("gtce_");
            int[] codepoints = rawName.codePoints().toArray();
            for (int i = 0; i < codepoints.length; i++) {
                int c = codepoints[i];
                if (c == '_') {
                    continue;
                }

                if (i > 0 && codepoints[i - 1] == '_') {
                    sb.appendCodePoint(c);
                } else {
                    sb.appendCodePoint(Character.toLowerCase(c));
                }
            }

            LOGGER.info("Registering recipe handler {}", sb.toString());
            RecipeMap entry = (RecipeMap) field.get(null);
            Recipes.registerRecipeHandler(sb.toString(), new HandlerWrapper(parsed -> {
                StringBuilder p = new StringBuilder();
                if (!parsed.getInputs().isEmpty()) {
                    if (p.length() > 0) {
                        p.append(", ");
                    }
                    namedList(p, parsed.getInputs());
                    p.append(" inputs");
                }
                if (!parsed.getOutputs().isEmpty()) {
                    if (p.length() > 0) {
                        p.append(", ");
                    }
                    namedList(p, parsed.getOutputs());
                    p.append(" outputs");
                }
                if (!parsed.getInputFluids().isEmpty()) {
                    if (p.length() > 0) {
                        p.append(", ");
                    }
                    namedList(p, parsed.getInputFluids());
                    p.append(" fluid inputs");
                }
                if (!parsed.getOutputFluids().isEmpty()) {
                    if (p.length() > 0) {
                        p.append(", ");
                    }
                    namedList(p, parsed.getOutputFluids());
                    p.append(" fluid outputs");
                }
                if (!parsed.getChancedOutputs().isEmpty()) {
                    if (p.length() > 0) {
                        p.append(", ");
                    }
                    namedList(p, parsed.getChancedOutputs());
                    p.append(" chanced outputs");
                }
                p.append(" requiring ")
                        .append(parsed.getEu()).append(" EUs and taking ")
                        .append(parsed.getDuration()).append(" ticks");

                LOGGER.info("Registering recipe for handler {} with {}", sb.toString(), p.toString());
                RecipeBuilder builder = entry.recipeBuilder()
                        .duration(parsed.getDuration())
                        .EUt(parsed.getEu())
                        .inputs(parsed.getInputs())
                        .fluidInputs(parsed.getInputFluids())
                        .outputs(parsed.getOutputs())
                        .fluidOutputs(parsed.getOutputFluids());
                for (ChancedOutput co : parsed.getChancedOutputs()) {
                    builder = builder.chancedOutput(co.getItem(), co.getChance(), co.getBoost());
                }
                builder.buildAndRegister();
            }));
        }

        Recipes.registerRecipeHandler("shaped", new HandlerWrapper(parsed -> {
            if (parsed.getInputs().size() != 9) {
                throw new Recipes.RecipeException("Invalid recipe input size " + parsed.getInputs().size() + " != 9");
            }
            if (parsed.getOutputs().size() != 1) {
                throw new Recipes.RecipeException("Invalid recipe output size " + parsed.getOutputs().size() + " != 1");
            }

            ItemStack output = parsed.getOutputs().get(0);

            Map<Character, ItemStack> ingredients = new HashMap<>();
            List<Object> args = new ArrayList<>();
            boolean nonempty = false;
            for (int i = 0; i < 3; i++) {
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < 3; k++) {
                    int n = (i * 3) + k;
                    ItemStack value = parsed.getInputs().get(n);
                    if (value == null) {
                        sb.append(' ');
                    } else{
                        char c = Character.toChars('a' + n)[0];
                        sb.append(c);
                        ingredients.put(c, value);
                        nonempty = true;
                    }
                }
                args.add(sb.toString());
            }
            for (Map.Entry<Character, ItemStack> e : ingredients.entrySet()) {
                args.add(e.getKey());
                args.add(e.getValue());
            }
            if (nonempty && output.getCount() > 0) {
                GameData.register_impl(new ShapedOreRecipe(null, output, args.toArray()));
            }
        }));

        Recipes.registerRecipeHandler("shapeless", new HandlerWrapper(parsed -> {
            ItemStack output = parsed.getOutputs().get(0);

            if (!parsed.getInputs().isEmpty() && output.getCount() > 0) {
                GameData.register_impl(new ShapelessOreRecipe(null, output, parsed.getInputs().toArray()));
            }
        }));

        Recipes.registerRecipeHandler("furnace", new HandlerWrapper(parsed -> {
            ItemStack output = parsed.getOutputs().get(0);
            ItemStack input = parsed.getInputs().get(0);

            FurnaceRecipes.instance().addSmelting(input.getItem(), output, 0);
        }));

        Recipes.registerRecipeHandler("gtce_multiple", new HandlerWrapper(null) {
            @Override
            public BoxedUnit apply(ItemStack output, Config recipe) {
                List<? extends Config> alternatives = recipe.getConfigList("alternatives");
                for (Config altconf : alternatives) {
                    String type = "shaped";
                    if (altconf.hasPath("type")) {
                        type = altconf.getString("type");
                    }
                    Option<Function2<ItemStack, Config, BoxedUnit>> func = Recipes.recipeHandlers().get(type);
                    if (func.isDefined()) {
                        LOGGER.info("Registering recipe alternative for handler {} and output {}", type, output.getDisplayName());
                        func.get().apply(output, altconf);
                    }
                }
                return BoxedUnit.UNIT;
            }
        });
    }

    public static void setCount(List<ItemStack> stacks, List<Integer> counts, int i) {
        ItemStack stack = stacks.get(i);
        if (stack == null) {
            return;
        }
        int count = counts.get(i);
        if (count <= 0) {
            return;
        }
        stack.setCount(Math.min(stack.getMaxStackSize(), count));
    }

    public static ParsedRecipe parseRecipe(ItemStack primaryOutput, Config recipe) {
        List<ItemStack> inputs = parseIngredientList(recipe.getValue("input"));
        primaryOutput.setCount(Recipes.tryGetCount(recipe));
        List<Integer> inputCount = recipe.getIntList("count");
        if (inputCount.size() != inputs.size()) {
            throw new Recipes.RecipeException("Mismatched ingredient count: " + inputs.size() + " != " + inputCount.size() + ".");
        }
        for (int i = 0; i < inputs.size(); i++) {
            setCount(inputs, inputCount, i);
        }
        List<ItemStack> outputs = new ArrayList<>();
        if (recipe.hasPath("secondaryOutput")) {
            List<ItemStack> secondaryOutputs = parseIngredientList(recipe.getValue("secondaryOutput"));
            List<Integer> secondaryOutputCount = recipe.getIntList("secondaryOutputCount");
            if (secondaryOutputs.size() != secondaryOutputCount.size()) {
                throw new Recipes.RecipeException("Mismatched secoundary output count: " + secondaryOutputs.size() + " != " + secondaryOutputCount.size() + ".");
            }
            for (int i = 0; i < secondaryOutputs.size(); i++) {
                setCount(secondaryOutputs, secondaryOutputCount, i);
            }
            outputs.addAll(secondaryOutputs);
        }
        outputs.add(0, primaryOutput);

        List<ChancedOutput> chancedOutputs = new ArrayList<>();
        if (recipe.hasPath("chancedOutput")) {
            Object unwrapped = recipe.getValue("chancedOutput").unwrapped();
            if (unwrapped instanceof Collection) {
                for (Object ing : (Collection)unwrapped) {
                    if (!(ing instanceof HashMap)) {
                        throw new Recipes.RecipeException("Invalid chanced output definition" + ing);
                    }
                    Object item = ((HashMap) ing).get("item");
                    List<ItemStack> stacks = transformIngredient(item);
                    if (stacks.isEmpty()) {
                        throw new Recipes.RecipeException("Invalid chanced output definition for item " + ing);
                    }
                    ItemStack chancedStack = stacks.get(0);
                    Object chance = ((HashMap) ing).get("chance");
                    if (!(chance instanceof Number)) {
                        throw new Recipes.RecipeException("Invalid chanced output definition for chance " + ing);
                    }
                    Object boost = ((HashMap) ing).get("boost");
                    if (!(boost instanceof Number)) {
                        throw new Recipes.RecipeException("Invalid chanced output definition for boost " + ing);
                    }
                    chancedOutputs.add(new ChancedOutput(chancedStack, ((Number) chance).intValue(), ((Number) boost).intValue()));
                }
            }
        }

        List<FluidStack> inputFluids = new ArrayList<>();
        if (recipe.hasPath("inputFluid")) {
            Option<FluidStack> stack = Recipes.parseFluidIngredient(recipe.getConfig("inputFluid"));
            if (stack.isDefined()) {
                inputFluids.add(stack.get());
            }
        }

        List<FluidStack> outputFluids = new ArrayList<>();
        if (recipe.hasPath("outputFluid")) {
            Option<FluidStack> stack = Recipes.parseFluidIngredient(recipe.getConfig("outputFluid"));
            if (stack.isDefined()) {
                outputFluids.add(stack.get());
            }
        }

        int eu = recipe.getInt("eu");
        int duration = recipe.getInt("duration");

        return new ParsedRecipe(inputs, inputFluids, outputFluids, outputs, chancedOutputs, eu, duration);
    }

    public static List<ItemStack> transformStack(String ref) {
        Object ing = Recipes.parseIngredient(ref);
        if (ing instanceof ItemStack) {
            return Collections.singletonList(((ItemStack) ing).copy());
        }
        return OreDictionary.getOres(ref);
    }

    public static List<ItemStack> transformIngredient(Object ing) {
        if (ing instanceof String) {
            String ref = ing.toString();
            for (ItemStack o : transformStack(ref)) {
                return Collections.singletonList(o.copy());
            }
        } else if (ing instanceof ItemStack) {
            return Collections.singletonList(((ItemStack) ing).copy());
        } else if (ing instanceof Map) {
            Map m = (Map) ing;
            Object oreDict = m.get("oreDict");
            Object gtid = m.get("gtid");
            if (!(oreDict instanceof String) || !(gtid instanceof String)) {
                throw new Recipes.RecipeException("Invalid ingredient type: " + ing.getClass().getName() + " @ " + ing);
            }
            List<ItemStack> ores = transformStack((String) oreDict);
            for (ItemStack o : ores) {
                if (o.getItem() instanceof MetaItem) {
                    MetaItem.MetaValueItem item = ((MetaItem) o.getItem()).getItem(o);
                    if (gtid.equals(item.unlocalizedName)) {
                        return Collections.singletonList(o.copy());
                    }
                }
            }
        } else if (ing == null) {
            return Collections.emptyList();
        }
        throw new Recipes.RecipeException("Invalid ingredient type: " + ing.getClass().getName() + " @ " + ing);
    }

    private static List<ItemStack> parseIngredientList(ConfigValue input) {
        Object unwrapped = input.unwrapped();
        List<ItemStack> ingredients = new ArrayList<>();
        if (unwrapped instanceof Collection) {
            for (Object ing : (Collection)unwrapped) {
                ingredients.addAll(transformIngredient(ing));
            }
        } else {
            ingredients.addAll(transformIngredient(unwrapped));
        }
        return ingredients;
    }
}
