package org.eientei.gtce2oc;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@Name(GTCE2OC.MODID)
@MCVersion(GTCE2OC.MCVERSION)
@SortingIndex(2002)
@DependsOn({"OpenComputers", "gregtech"})
@TransformerExclusions("org.eientei.gtce2oc")
public class GTCE2OC implements IFMLLoadingPlugin {
    public static final String MODID = "@MODID@";
    public static final String MCVERSION = "@MCVERSION@";

    public static final Logger logger = LogManager.getLogger(MODID);

    public static final String POWER_CONVERTER = "PowerConverter";
    public static final String POWER_DISTRIBUTOR = "PowerDistributor";
    public static final String OTHERS = "Other";

    public static final Map<String,MachineConfig> CONFIG = new HashMap<>();

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "org.eientei.gtce2oc.GTCE2OCTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return "org.eientei.gtce2oc.GTCE2OCModContainer";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
