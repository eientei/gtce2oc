package org.eientei.gtce2oc.reference;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class ASMDump {
    public static void main(String[] args) throws IOException {
        ClassReader cr = new ClassReader(new FileInputStream(args[0]));
        cr.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out)), ClassReader.SKIP_DEBUG);
    }
}
