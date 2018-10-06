package org.eientei.gtce2oc;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.DependsOn;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Name(GTCE2OC.MODID)
@MCVersion(GTCE2OC.MCVERSION)
@DependsOn({"OpenComputers", "gregtech"})
@TransformerExclusions("org.eientei.gtce2oc")
public class GTCE2OC implements IFMLLoadingPlugin {
    public static final String VERSION = "@VERSION@";
    public static final String MODID = "@MODID@";
    public static final String MCVERSION = "@MCVERSION@";

    public static final Logger logger = LogManager.getLogger(MODID);

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "org.eientei.gtce2oc.GTCE2OCTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return null;
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

    public static void main(String[] args) throws IOException {
        ClassReader cr = new ClassReader(new FileInputStream(args[0]));
        cr.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out)), ClassReader.SKIP_DEBUG);
    }
}
