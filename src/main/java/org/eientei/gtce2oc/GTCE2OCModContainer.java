package org.eientei.gtce2oc;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import li.cil.oc.api.Driver;
import li.cil.oc.api.IMC;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import org.apache.commons.io.IOUtils;
import org.eientei.gtce2oc.driver.DriverEnergyContainer;
import org.eientei.gtce2oc.driver.EventHandler;
import org.eientei.gtce2oc.driver.RecipeIntegration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import static org.eientei.gtce2oc.GTCE2OC.*;
import static org.eientei.gtce2oc.MachineConfig.GET_SIDES;
import static org.eientei.gtce2oc.MachineConfig.VALID_SIDES;

@Mod.EventBusSubscriber(modid = "@MODID@")
public class GTCE2OCModContainer extends DummyModContainer {
    private static ModMetadata md;
    static {
        DefaultArtifactVersion openComputers = new DefaultArtifactVersion("OpenComputers", true);
        DefaultArtifactVersion gregtech = new DefaultArtifactVersion("gregtech", true);

        md = new ModMetadata();
        md.authorList.add("@MODAUTHOR@");
        md.modId = "@MODID@";
        md.version = "@MODVERSION@";
        md.name = "@MODNAME@";
        md.url = "@MODURL@";
        md.description = "@MODDESC@";
        md.dependencies.add(openComputers);
        md.dependencies.add(gregtech);
        md.requiredMods.add(openComputers);
        md.requiredMods.add(gregtech);
    }

    public GTCE2OCModContainer() {
        super(md);
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void load(FMLPreInitializationEvent evt) throws Exception {
        logger.info("loading configuration");
        Configuration config = new Configuration(new File(Loader.instance().getConfigDir(), "gtce2oc.cfg"));
        config.load();

        EnumFacing[] vert = new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN};
        EnumFacing[] horiz = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};

        CONFIG.put(POWER_CONVERTER, new MachineConfig(config, POWER_CONVERTER, 128, 1, vert, horiz));
        CONFIG.put(POWER_DISTRIBUTOR, new MachineConfig(config, POWER_DISTRIBUTOR, 128, 1, vert, horiz));

        MachineConfig others = new MachineConfig();
        others.setInputs(GET_SIDES(config, OTHERS, "possible_inputs", EnumFacing.values(), "Sides from which other machines will possibly accept energy. Valid values are: " + Arrays.toString(VALID_SIDES)));
        others.setOutputs(GET_SIDES(config, OTHERS, "outputs", new EnumFacing[0]));
        others.setInputAmperage(config.getInt("input_amperage", OTHERS, 1, 0, Integer.MAX_VALUE, "Amperage other machines will accept as input in one tick"));
        others.setOutputAmperage(config.getInt("output_amperage", OTHERS, 1, 0, Integer.MAX_VALUE, "Amperage other machines will emmit if stored enough energy"));
        others.setInputVoltage(config.getInt("input_voltage", OTHERS, 128, 0, Integer.MAX_VALUE, "Voltage other machines will accept as input without blowing"));
        others.setOutputVoltage(config.getInt("output_voltage", OTHERS, 128, 0, Integer.MAX_VALUE, "Voltage other machines will emmit if stored enough energy"));
        CONFIG.put(OTHERS, others);

        config.save();

        RecipeIntegration.registerRecipeHandlers();
        File recipesConfig = new File(new File(Loader.instance().getConfigDir(), "opencomputers"), "gregtechce.recipes");
        if (!recipesConfig.exists()) {
            if (!recipesConfig.getParentFile().exists() && !recipesConfig.getParentFile().mkdirs()) {
                throw new Exception("Could not create missing parent directory");
            }
            logger.info("Placing gregtechce.recipes into opencomputers directory");
            InputStream recipes = getClass().getClassLoader().getResourceAsStream("gregtechce.recipes");
            FileOutputStream fos = new FileOutputStream(recipesConfig);
            IOUtils.copy(recipes, fos);
            IOUtils.closeQuietly(recipes);
            IOUtils.closeQuietly(fos);
        }
    }

    @Subscribe
    public void init(FMLInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        IMC.registerToolDurabilityProvider("org.eientei.gtce2oc.driver.EventHandler.getDurability");
        Driver.add(new DriverEnergyContainer());
    }
}
