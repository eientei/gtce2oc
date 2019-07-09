package org.eientei.gtce2oc;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

import static org.eientei.gtce2oc.GTCE2OC.logger;
import static org.objectweb.asm.Opcodes.*;

public class GTCE2OCTransformer implements IClassTransformer {
    public static final String OC_BASE_PACKAGE = "li.cil.oc";
    public static final String OC_TILEENTITY_POWER_ACCEPTOR_INTERFACE = "li/cil/oc/common/tileentity/traits/PowerAcceptor";
    public static final String OC_TILEENTITY_POWER_BALANCER_INTERFACE = "li/cil/oc/common/tileentity/traits/PowerBalancer";
    public static final String MINECRAFT_ITICKABLE_INTERFACE = "net/minecraft/util/ITickable";
    public static final String GTCE_IENERGY_CONTAINER_INTERFACE = "gregtech/api/capability/IEnergyContainer";

    public static final String IMPL_GENERIC = "org/eientei/gtce2oc/impl/GenericHandler";
    public static final String IMPL_POWER_DISTRIBUTOR = "org/eientei/gtce2oc/impl/PowerDistributorHandler";
    
    public static final String POWER_ACCEPTOR_TYPE = "Lli/cil/oc/common/tileentity/traits/PowerAcceptor;";
    public static final String POWER_BALANCER_TYPE = "Lli/cil/oc/common/tileentity/traits/PowerBalancer;";

    public static final String MINECRAFT_ITICKABLE_UPDATE_METHOD = "update";

    public static final String MANGLE_PREFIX = "__gtce2oc__";

    public static final String[] IMPLEMENTED_INTERFACES = new String[]{
            MINECRAFT_ITICKABLE_INTERFACE,
            GTCE_IENERGY_CONTAINER_INTERFACE,
    };
    public static final String[] IMPLEMENTED_METHODS = new String[]{
            "acceptEnergyFromNetwork",
            "inputsEnergy",
            "outputsEnergy",
            "changeEnergy",
            "addEnergy",
            "removeEnergy",
            "getEnergyStored",
            "getEnergyCapacity",
            "getOutputAmperage",
            "getOutputVoltage",
            "getInputAmperage",
            "getInputVoltage",
            "isOneProbeHidden",
            MINECRAFT_ITICKABLE_UPDATE_METHOD,
            "hasCapability",
            "getCapability",
    };

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (name.startsWith(OC_BASE_PACKAGE)) {
            return dispatch(name, basicClass);
        }
        return basicClass;
    }

    private byte[] dispatch(String name, byte[] basicClass) {
        ClassReader cr = new ClassReader(basicClass);
        String[] interfaces = cr.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].equals(OC_TILEENTITY_POWER_ACCEPTOR_INTERFACE)) {
                logger.info("instrumenting generic TileEntity: {}", name);
                return implementIEnergyContainer(cr, IMPL_GENERIC, POWER_ACCEPTOR_TYPE);
            } else if (interfaces[i].equals(OC_TILEENTITY_POWER_BALANCER_INTERFACE)) {
                logger.info("instrumenting balancer TileEntity: {}", name);
                return implementIEnergyContainer(cr, IMPL_POWER_DISTRIBUTOR, POWER_BALANCER_TYPE);
            }
        }
        return basicClass;
    }

    private boolean hasMethod(ClassNode cn, String method) {
        for (MethodNode mn : cn.methods) {
            if (mn.name.equals(method)) {
                return true;
            }
        }
        return false;
    }

    private byte[] implementIEnergyContainer(ClassReader cr, String handler, String type) {
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        List<String> required = new ArrayList<>();
        for (String iface : IMPLEMENTED_INTERFACES) {
            boolean found = false;
            for (String actual : cn.interfaces) {
                if (actual.equals(iface)) {
                    found = true;
                    break;
                }
            }
            if (!found){
                required.add(iface);
            }
        }

        cn.interfaces.addAll(required);

        for (MethodNode mn : cn.methods) {
            for (String implementedMethod : IMPLEMENTED_METHODS) {
                if (implementedMethod.equals(FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(cn.name, mn.name, mn.desc))) {
                    mn.name = MANGLE_PREFIX + mn.name;
                    break;
                }
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);

        MethodVisitor mv;
        AnnotationVisitor av0;
        {
            mv = cw.visitMethod(ACC_PUBLIC, "acceptEnergyFromNetwork", "(Lnet/minecraft/util/EnumFacing;JJ)J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(LLOAD, 2);
            mv.visitVarInsn(LLOAD, 4);
            mv.visitMethodInsn(INVOKESTATIC, handler, "acceptEnergyFromNetwork", "(" + type + "Lnet/minecraft/util/EnumFacing;JJ)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(6, 6);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "inputsEnergy", "(Lnet/minecraft/util/EnumFacing;)Z", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, handler, "inputsEnergy", "(" + type + "Lnet/minecraft/util/EnumFacing;)Z", false);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "outputsEnergy", "(Lnet/minecraft/util/EnumFacing;)Z", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, handler, "outputsEnergy", "(" + type + "Lnet/minecraft/util/EnumFacing;)Z", false);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "changeEnergy", "(J)J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(LLOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, handler, "changeEnergy", "(" + type + "J)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "addEnergy", "(J)J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(LLOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, handler, "addEnergy", "(" + type + "J)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "removeEnergy", "(J)J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(LLOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, handler, "removeEnergy", "(" + type + "J)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getEnergyStored", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, handler, "getEnergyStored", "(" + type + ")J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getEnergyCapacity", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, handler, "getEnergyCapacity", "(" + type + ")J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getOutputAmperage", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, handler, "getOutputAmperage", "(" + type + ")J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getOutputVoltage", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, handler, "getOutputVoltage", "(" + type + ")J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getInputAmperage", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, handler, "getInputAmperage", "(" + type + ")J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getInputVoltage", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, handler, "getInputVoltage", "(" + type + ")J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "isOneProbeHidden", "()Z", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, handler, "isOneProbeHidden", "(" + type + ")Z", false);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            String updateMethodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(MINECRAFT_ITICKABLE_INTERFACE, MINECRAFT_ITICKABLE_UPDATE_METHOD, "()V");
            mv = cw.visitMethod(ACC_PUBLIC, updateMethodName, "()V", null, null);
            mv.visitCode();
            if (hasMethod(cn, MANGLE_PREFIX + updateMethodName)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, cn.name, MANGLE_PREFIX + updateMethodName, "()V", false);
            }
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, handler, "update", "(" + type + ")V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "hasCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z", "(Lnet/minecraftforge/common/capabilities/Capability<*>;Lnet/minecraft/util/EnumFacing;)Z", null);
            {
                av0 = mv.visitParameterAnnotation(0, "Ljavax/annotation/Nullable;", true);
                av0.visitEnd();
            }
            {
                av0 = mv.visitParameterAnnotation(1, "Ljavax/annotation/Nullable;", true);
                av0.visitEnd();
            }
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, handler, "hasCapability", "(" + type + "Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z", false);
            Label l0 = new Label();
            mv.visitJumpInsn(IFEQ, l0);
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            if (hasMethod(cn, MANGLE_PREFIX + "hasCapability")) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKEVIRTUAL, cn.name, MANGLE_PREFIX + "hasCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z", false);
                Label l1 = new Label();
                mv.visitJumpInsn(IFEQ, l1);
                mv.visitInsn(ICONST_1);
                mv.visitInsn(IRETURN);
                mv.visitLabel(l1);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            }
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESPECIAL, cn.superName, "hasCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z", false);
            Label l2 = new Label();
            mv.visitJumpInsn(IFEQ, l2);
            mv.visitInsn(ICONST_1);
            mv.visitInsn(IRETURN);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;", "<T:Ljava/lang/Object;>(Lnet/minecraftforge/common/capabilities/Capability<TT;>;Lnet/minecraft/util/EnumFacing;)TT;", null);
            {
                av0 = mv.visitParameterAnnotation(0, "Ljavax/annotation/Nullable;", true);
                av0.visitEnd();
            }
            {
                av0 = mv.visitParameterAnnotation(1, "Ljavax/annotation/Nullable;", true);
                av0.visitEnd();
            }
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, handler, "hasCapability", "(" + type + "Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z", false);
            Label l0 = new Label();
            mv.visitJumpInsn(IFEQ, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, handler, "getCapability", "(" + type + "Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;", false);
            mv.visitInsn(ARETURN);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            if (hasMethod(cn, MANGLE_PREFIX + "hasCapability") &&  hasMethod(cn, MANGLE_PREFIX + "getCapability")) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKEVIRTUAL, cn.name, MANGLE_PREFIX + "hasCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z", false);
                Label l1 = new Label();
                mv.visitJumpInsn(IFEQ, l1);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKEVIRTUAL, cn.name, MANGLE_PREFIX + "getCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;", false);
                mv.visitInsn(ARETURN);
                mv.visitLabel(l1);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            }
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESPECIAL, cn.superName, "hasCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z", false);
            Label l2 = new Label();
            mv.visitJumpInsn(IFEQ, l2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESPECIAL, cn.superName, "getCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;", false);
            mv.visitInsn(ARETURN);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }

        return cw.toByteArray();
    }
}