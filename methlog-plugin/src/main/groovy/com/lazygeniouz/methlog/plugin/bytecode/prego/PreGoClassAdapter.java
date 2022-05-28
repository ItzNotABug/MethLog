package com.lazygeniouz.methlog.plugin.bytecode.prego;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public final class PreGoClassAdapter extends ClassVisitor {

    private boolean needParameter = false;
    private final List<String> includes = new ArrayList<>();

    public PreGoClassAdapter(final ClassVisitor cv) {
        super(Opcodes.ASM7, cv);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(
            final int access, final String name,
            final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        PreGoMethodAdapter preGoMethodAdapter = new PreGoMethodAdapter(
                name, mv, false, methodName -> {
            includes.add(methodName);
            needParameter = true;
        });
        return mv == null ? null : preGoMethodAdapter;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public boolean isNeedParameter() {
        return needParameter;
    }

    interface MethodCollector {
        void onIncludeMethod(String methodName);
    }
}