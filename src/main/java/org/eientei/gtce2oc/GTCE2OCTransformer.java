package org.eientei.gtce2oc;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

public class GTCE2OCTransformer implements IClassTransformer {
    public static final String OC_BASE_PACKAGE = "li.cil.oc";
    public static final String OC_TILEENTITY_POWER_ACCEPTOR = "li/cil/oc/common/tileentity/traits/PowerAcceptor";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (name.startsWith(OC_BASE_PACKAGE)) {
            return dispatch(name, basicClass);
        }
        return basicClass;
    }

    private byte[] dispatch(String name, byte[] basicClass) {
        ClassReader classReader = new ClassReader(basicClass);
        String[] interfaces = classReader.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].equals(OC_TILEENTITY_POWER_ACCEPTOR)) {
                return handleTileEntityPowerAcceptor(name, classReader);
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

    @SuppressWarnings("StringConcatenationInLoop")
    private byte[] handleTileEntityPowerAcceptor(String name, ClassReader classReader) {
        GTCE2OC.logger.info("instrumenting TileEntity: {}", name);
        ClassNode cn = new ClassNode();
        classReader.accept(cn, 0);
        cn.interfaces.add("gregtech/api/capability/IEnergyContainer");

        final String[] implementedMethods = new String[]{
                "hasCapability",
                "getCapability",
        };

        for (MethodNode mn : cn.methods) {
            for (String implementedMethod : implementedMethods) {
                if (implementedMethod.equals(mn.name)) {
                    mn.name = "_" + mn.name;
                    break;
                }
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);

        implementIEnergyContainer(cn, cw);
        overrideCapabilities(cn, cw);

        return cw.toByteArray();
    }

    private void overrideCapabilities(ClassNode cn, ClassWriter cw) {
        AnnotationVisitor av0;
        MethodVisitor mv;
        {
            mv = cw.visitMethod(ACC_PUBLIC, "hasCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z", "(Lnet/minecraftforge/common/capabilities/Capability<*>;Lnet/minecraft/util/EnumFacing;)Z", null);
            {
                av0 = mv.visitParameterAnnotation(1, "Ljavax/annotation/Nullable;", true);
                av0.visitEnd();
            }
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/TileEntityStatic", "hasCapability", "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z", false);
            Label l0 = new Label();
            mv.visitJumpInsn(IFNE, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            if (hasMethod(cn, "_hasCapability")) {
                mv.visitMethodInsn(INVOKEVIRTUAL, cn.name, "_hasCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z", false);
            } else {
                mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/tileentity/TileEntity", "hasCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z", false);
            }
            Label l1 = new Label();
            mv.visitJumpInsn(IFEQ, l1);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(ICONST_1);
            Label l2 = new Label();
            mv.visitJumpInsn(GOTO, l2);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitInsn(ICONST_0);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
            mv.visitInsn(IRETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;", "<T:Ljava/lang/Object;>(Lnet/minecraftforge/common/capabilities/Capability<TT;>;Lnet/minecraft/util/EnumFacing;)TT;", null);
            {
                av0 = mv.visitParameterAnnotation(1, "Ljavax/annotation/Nullable;", true);
                av0.visitEnd();
            }
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/TileEntityStatic", "getCapability", "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;", false);
            mv.visitVarInsn(ASTORE, 3);
            mv.visitVarInsn(ALOAD, 3);
            Label l0 = new Label();
            mv.visitJumpInsn(IFNONNULL, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 2);
            if (hasMethod(cn, "_getCapability")) {
                mv.visitMethodInsn(INVOKEVIRTUAL, cn.name, "_getCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;", false);
            } else {
                mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/tileentity/TileEntity", "getCapability", "(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;", false);
            }
            Label l1 = new Label();
            mv.visitJumpInsn(GOTO, l1);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"java/lang/Object"}, 0, null);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {"java/lang/Object"});
            mv.visitInsn(ARETURN);
            mv.visitMaxs(3, 4);
            mv.visitEnd();
        }
    }

    private void implementIEnergyContainer(ClassNode cn, ClassWriter cw) {
        MethodVisitor mv;
        {
            mv = cw.visitMethod(ACC_PUBLIC, "acceptEnergyFromNetwork", "(Lnet/minecraft/util/EnumFacing;JJ)J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(LLOAD, 2);
            mv.visitVarInsn(LLOAD, 4);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "acceptEnergyFromNetwork", "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;JJ)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(6, 6);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "inputsEnergy", "(Lnet/minecraft/util/EnumFacing;)Z", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "inputsEnergy", "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)Z", false);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "outputsEnergy", "(Lnet/minecraft/util/EnumFacing;)Z", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "outputsEnergy", "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)Z", false);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "addEnergy", "(J)J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(LLOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "addEnergy", "(Lnet/minecraft/tileentity/TileEntity;J)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "canUse", "(J)Z", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(LLOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "canUse", "(Lnet/minecraft/tileentity/TileEntity;J)Z", false);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getEnergyCanBeInserted", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "getEnergyCanBeInserted", "(Lnet/minecraft/tileentity/TileEntity;)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getEnergyStored", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "getEnergyStored", "(Lnet/minecraft/tileentity/TileEntity;)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getEnergyCapacity", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "getEnergyCapacity", "(Lnet/minecraft/tileentity/TileEntity;)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getEnergyStoredActual", "()Ljava/math/BigInteger;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "getEnergyStoredActual", "(Lnet/minecraft/tileentity/TileEntity;)Ljava/math/BigInteger;", false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getEnergyCapacityActual", "()Ljava/math/BigInteger;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "getEnergyCapacityActual", "(Lnet/minecraft/tileentity/TileEntity;)Ljava/math/BigInteger;", false);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getOutputAmperage", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "getOutputAmperage", "(Lnet/minecraft/tileentity/TileEntity;)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getOutputVoltage", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "getOutputVoltage", "(Lnet/minecraft/tileentity/TileEntity;)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getInputAmperage", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "getInputAmperage", "(Lnet/minecraft/tileentity/TileEntity;)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "getInputVoltage", "()J", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "getInputVoltage", "(Lnet/minecraft/tileentity/TileEntity;)J", false);
            mv.visitInsn(LRETURN);
            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "isSummationOverflowSafe", "()Z", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "isSummationOverflowSafe", "(Lnet/minecraft/tileentity/TileEntity;)Z", false);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC, "isOneProbeHidden", "()Z", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESTATIC, "org/eientei/gtce2oc/impl/EnergyContainerStatic", "isOneProbeHidden", "(Lnet/minecraft/tileentity/TileEntity;)Z", false);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
    }
}
