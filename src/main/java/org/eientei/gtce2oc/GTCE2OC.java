package org.eientei.gtce2oc;

import gregtech.api.capability.IEnergyContainer;
import li.cil.oc.api.Driver;
import li.cil.oc.api.IMC;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.server.timings.TimeTracker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eientei.gtce2oc.driver.DriverEnergyContainer;
import org.eientei.gtce2oc.driver.EventHandler;
import org.eientei.gtce2oc.driver.RecipeIntegration;
import org.eientei.gtce2oc.impl.MachineConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;

@Mod(
        modid = GTCE2OC.MODID,
        name = GTCE2OC.MODNAME,
        version = GTCE2OC.MODVERSION,
        dependencies = "required:gregtech;required:opencomputers"
)
public class GTCE2OC {
    public static final String MODID = "@MODID@";
    public static final String MODNAME = "@MODNAME@";
    public static final String MODVERSION = "@MODVERSION@";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static MachineConfig POWER_ACCEPTOR;
    public static MachineConfig POWER_CONVERTER;
    public static MachineConfig POWER_BALANCER;

    @CapabilityInject(IEnergyContainer.class)
    public static Capability<IEnergyContainer> ENERGY_CONTAINER_CAP = null;

    public static ResourceLocation ENERGY_CONTAINER_CAP_PROVIDER = new ResourceLocation(GTCE2OC.MODID, "energy_container");

    public GTCE2OC() {
        MinecraftForge.EVENT_BUS.register(new GTCE2OCEventHandler());
        wrapTileEntityTracker();
    }

    @SuppressWarnings("unchecked")
    private void wrapTileEntityTracker() {
        try {
            Field field = TimeTracker.class.getDeclaredField("TILE_ENTITY_UPDATE");
            field.setAccessible(true);
            FieldUtils.removeFinalModifier(field);
            TimeTracker<TileEntity> tracker = (TimeTracker<TileEntity>) field.get(null);
            field.set(null, new GTCE2OCTimeTracker(tracker));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Mod.EventHandler
    public void load(FMLPreInitializationEvent evt) throws Exception {
        GTCE2OC.LOGGER.info("loading configuration");
        Configuration config = new Configuration(new File(Loader.instance().getConfigDir(), "gtce2oc.cfg"));
        config.load();

        EnumFacing[] vert = new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN};
        EnumFacing[] horiz = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};

        GTCE2OC.POWER_CONVERTER = new MachineConfig(config, "PowerConverter", 128, 1, vert, horiz);
        GTCE2OC.POWER_BALANCER = new MachineConfig(config, "PowerBalancer", 128, 1, vert, horiz);

        MachineConfig others = new MachineConfig();
        others.setInputs(MachineConfig.GET_SIDES(config, "Other", "possible_inputs", EnumFacing.values(), "Sides from which other machines will possibly accept energy. Valid values are: " + Arrays.toString(MachineConfig.VALID_SIDES)));
        others.setOutputs(MachineConfig.GET_SIDES(config, "Other", "outputs", new EnumFacing[0]));
        others.setInputAmperage(config.getInt("input_amperage", "Other", 1, 0, Integer.MAX_VALUE, "Amperage other machines will accept as input in one tick"));
        others.setOutputAmperage(config.getInt("output_amperage", "Other", 1, 0, Integer.MAX_VALUE, "Amperage other machines will emmit if stored enough energy"));
        others.setInputVoltage(config.getInt("input_voltage", "Other", 128, 0, Integer.MAX_VALUE, "Voltage other machines will accept as input without blowing"));
        others.setOutputVoltage(config.getInt("output_voltage", "Other", 128, 0, Integer.MAX_VALUE, "Voltage other machines will emmit if stored enough energy"));
        GTCE2OC.POWER_ACCEPTOR = others;

        config.save();

        RecipeIntegration.registerRecipeHandlers();
        File recipesConfig = new File(new File(Loader.instance().getConfigDir(), "opencomputers"), "gregtechce.recipes");
        if (!recipesConfig.exists()) {
            if (!recipesConfig.getParentFile().exists() && !recipesConfig.getParentFile().mkdirs()) {
                throw new Exception("Could not create missing parent directory");
            }
            GTCE2OC.LOGGER.info("Placing gregtechce.recipes into opencomputers directory");
            InputStream recipes = getClass().getClassLoader().getResourceAsStream("gregtechce.recipes");
            if (recipes != null) {
                FileOutputStream fos = new FileOutputStream(recipesConfig);
                IOUtils.copy(recipes, fos);
                IOUtils.closeQuietly(recipes);
                IOUtils.closeQuietly(fos);
            }
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent evt) {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        IMC.registerToolDurabilityProvider("org.eientei.gtce2oc.driver.EventHandler.getDurability");
        Driver.add(new DriverEnergyContainer());
    }
}
