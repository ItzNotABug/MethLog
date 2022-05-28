package com.lazygeniouz.methlog.plugin.bytecode;

import com.lazygeniouz.methlog.plugin.bytecode.prego.PreGoClassAdapter;
import com.lazygeniouz.methlog.transform.asm.BaseWeaver;
import com.lazygeniouz.methlog.transform.asm.ExtendClassWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;

public final class DebugWeaver extends BaseWeaver {

    private static final String PLUGIN_LIBRARY = "com.lazygeniouz.methlog";

    @Override
    public byte[] weaveSingleClassToByteArray(InputStream inputStream) throws IOException {
        ClassReader classReader = new ClassReader(inputStream);
        ClassWriter classWriter = new ExtendClassWriter(classLoader, ClassWriter.COMPUTE_MAXS);
        PreGoClassAdapter preGoClassAdapter = new PreGoClassAdapter(classWriter);
        classReader.accept(preGoClassAdapter, ClassReader.EXPAND_FRAMES);
        if (preGoClassAdapter.isNeedParameter()) {
            classWriter = new ExtendClassWriter(classLoader, ClassWriter.COMPUTE_MAXS);

            ClassAdapter classAdapter = new ClassAdapter(classWriter);
            classAdapter.attachIncludeMethodsAndImplMethods(preGoClassAdapter.getIncludes());
            classReader.accept(classAdapter, ClassReader.EXPAND_FRAMES);
        }
        return classWriter.toByteArray();
    }

    @Override
    public boolean isWeavableClass(String fullQualifiedClassName) {
        boolean superResult = super.isWeavableClass(fullQualifiedClassName);
        boolean isByteCodePlugin = fullQualifiedClassName.startsWith(PLUGIN_LIBRARY);
        return superResult && !isByteCodePlugin;
    }
}