package com.lazygeniouz.methlog.plugin.bytecode;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public final class ClassAdapter extends ClassVisitor {

    private String className;
    private final List<String> includeMethods = new ArrayList<>();

    ClassAdapter(final ClassVisitor cv) {
        super(Opcodes.ASM7, cv);
    }

    public void attachIncludeMethodsAndImplMethods(List<String> includeMethods) {
        this.includeMethods.addAll(includeMethods);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(
            final int access, final String name,
            final String desc, final String signature, final String[] exceptions
    ) {

        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (includeMethods.contains(name)) {
            return new MethodAdapter(className, name, access, desc, mv);
        }
        return mv;
    }

}