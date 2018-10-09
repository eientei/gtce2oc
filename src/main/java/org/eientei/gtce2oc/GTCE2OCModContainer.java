package org.eientei.gtce2oc;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.util.Arrays;

import static org.eientei.gtce2oc.GTCE2OC.*;
import static org.eientei.gtce2oc.MachineConfig.GET_SIDES;
import static org.eientei.gtce2oc.MachineConfig.VALID_SIDES;

@Mod.EventBusSubscriber(modid = "@MODID@")
public class GTCE2OCModContainer extends DummyModContainer {
    private static ModMetadata md;
    static {
        md = new ModMetadata();
        md.authorList.add("@MODAUTHOR@");
        md.modId = "@MODID@";
        md.version = "@MODVERSION@";
        md.name = "@MODNAME@";
        md.url = "@MODURL@";
        md.description = "@MODDESC@";
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
    public void preInit(FMLPreInitializationEvent evt) {
        Configuration config = new Configuration(new File(Loader.instance().getConfigDir(), "gtce2oc.cfg"));
        config.load();

        EnumFacing[] vert = new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN};
        EnumFacing[] horiz = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};

        CONFIG.put(POWER_CONVERTER, new MachineConfig(config, POWER_CONVERTER, 128, 1, vert, horiz));
        CONFIG.put(POWER_DISTRIBUTOR, new MachineConfig(config, POWER_DISTRIBUTOR, 128, 1, vert, horiz));

        MachineConfig others = new MachineConfig();
        others.setInputs(GET_SIDES(config, OTHERS, "possible_inputs", EnumFacing.values(), "Sides from which other machines will possibly accept energy. Valid values are: " + Arrays.toString(VALID_SIDES)));
        others.setOutputs(GET_SIDES(config, OTHERS, "outputs", new EnumFacing[0]));
        others.setInputAmperage(config.getInt("input_amperage", OTHERS, 1, 0, Integer.MAX_VALUE, "Amperage other machines will accept as input without blowing"));
        others.setOutputAmperage(config.getInt("output_amperage", OTHERS, 1, 0, Integer.MAX_VALUE, "Amperage other machines will emmit if stored enough energy"));
        others.setInputVoltage(config.getInt("input_voltage", OTHERS, 128, 0, Integer.MAX_VALUE, "Voltage other machines will accept as input without blowing"));
        others.setOutputVoltage(config.getInt("output_voltage", OTHERS, 128, 0, Integer.MAX_VALUE, "Voltage other machines will emmit if stored enough energy"));
        CONFIG.put(OTHERS, others);

        config.save();
    }
}
