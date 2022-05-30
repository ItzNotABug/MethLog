package com.lazygeniouz.methlog.transform;

import com.android.annotations.NonNull;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.lazygeniouz.methlog.plugin.bytecode.DebugWeaver;
import com.lazygeniouz.methlog.plugin.helper.PluginHelper;
import com.lazygeniouz.methlog.transform.asm.BaseWeaver;
import com.lazygeniouz.methlog.transform.asm.ClassLoaderHelper;
import com.lazygeniouz.methlog.transform.concurrent.Worker;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MethLogTransform extends Transform {

    private static final Set<QualifiedContent.Scope> SCOPES = new HashSet<>();

    static {
        SCOPES.add(QualifiedContent.Scope.PROJECT);
        SCOPES.add(QualifiedContent.Scope.SUB_PROJECTS);
        SCOPES.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES);
    }

    private final Project project;
    private boolean injectByteCode = true;
    private final PluginHelper pluginHelper;
    private final Worker worker = Worker.get();
    private final BaseWeaver bytecodeWeaver = new DebugWeaver();

    public MethLogTransform(Project project) {
        this.project = project;
        this.pluginHelper = new PluginHelper(project);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        return SCOPES;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public void transform(@NonNull TransformInvocation invocation) throws IOException {
        String variantName = invocation.getContext().getVariantName();
        boolean isDebuggableBuild = pluginHelper.checkIfBuildDebuggable(variantName);

        if (!isDebuggableBuild) {
            pluginHelper.log(String.format("Skipping non-debuggable build type `%s`.", variantName));
        } else if (!pluginHelper.isMethLogEnabled()) {
            pluginHelper.log("MethLog is disabled.");
        }

        if (!isDebuggableBuild) injectByteCode = false;
        else injectByteCode = pluginHelper.isMethLogEnabled();

        boolean isIncremental = invocation.isIncremental();
        Collection<TransformInput> inputs = invocation.getInputs();
        TransformOutputProvider outputProvider = invocation.getOutputProvider();
        Collection<TransformInput> referencedInputs = invocation.getReferencedInputs();

        if (!isIncremental) outputProvider.deleteAll();

        URLClassLoader urlClassLoader = ClassLoaderHelper.getClassLoader(inputs, referencedInputs, project);
        this.bytecodeWeaver.setClassLoader(urlClassLoader);
        boolean flagForCleanDexBuilderFolder = false;
        for (TransformInput input : inputs) {
            for (JarInput jarInput : input.getJarInputs()) {
                Status status = jarInput.getStatus();
                File destination = outputProvider.getContentLocation(
                        jarInput.getFile().getAbsolutePath(),
                        jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR
                );
                if (isIncremental && injectByteCode) {
                    switch (status) {
                        case NOTCHANGED:
                            break;
                        case ADDED:
                        case CHANGED:
                            transformJar(jarInput.getFile(), destination);
                            break;
                        case REMOVED:
                            if (destination.exists()) {
                                FileUtils.forceDelete(destination);
                            }
                            break;
                    }
                } else {
                    // Forgive me!, Some project will store 3rd-party aar
                    // for several copies in dexbuilder folder,unknown issue.
                    if (inDuplicatedClassSafeMode() && !isIncremental && !flagForCleanDexBuilderFolder) {
                        cleanDexBuilderFolder(destination);
                        flagForCleanDexBuilderFolder = true;
                    }
                    transformJar(jarInput.getFile(), destination);
                }
            }

            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dest = outputProvider.getContentLocation(directoryInput.getName(),
                        directoryInput.getContentTypes(), directoryInput.getScopes(),
                        Format.DIRECTORY);
                FileUtils.forceMkdir(dest);
                if (isIncremental && injectByteCode) {
                    String srcDirPath = directoryInput.getFile().getAbsolutePath();
                    String destDirPath = dest.getAbsolutePath();
                    Map<File, Status> fileStatusMap = directoryInput.getChangedFiles();
                    for (Map.Entry<File, Status> changedFile : fileStatusMap.entrySet()) {
                        Status status = changedFile.getValue();
                        File inputFile = changedFile.getKey();
                        String destFilePath = inputFile.getAbsolutePath().replace(srcDirPath, destDirPath);
                        File destFile = new File(destFilePath);
                        switch (status) {
                            case NOTCHANGED:
                                break;
                            case REMOVED:
                                if (destFile.exists()) {
                                    //noinspection ResultOfMethodCallIgnored
                                    destFile.delete();
                                }
                                break;
                            case ADDED:
                            case CHANGED:
                                try {
                                    FileUtils.touch(destFile);
                                } catch (IOException e) {
                                    //maybe mkdirs fail for some strange reason, try again.
                                    FileUtils.forceMkdirParent(destFile);
                                }
                                transformSingleFile(inputFile, destFile, srcDirPath);
                                break;
                        }
                    }
                } else {
                    transformDir(directoryInput.getFile(), dest);
                }
            }
        }

        worker.await();
    }

    private void transformSingleFile(
            final File inputFile, final File outputFile,
            final String srcBaseDir) {
        worker.submit(() -> {
            bytecodeWeaver.weaveSingleClassToFile(inputFile, outputFile, srcBaseDir);
            return null;
        });
    }

    private void transformDir(final File inputDir, final File outputDir) throws IOException {
        if (!injectByteCode) {
            FileUtils.copyDirectory(inputDir, outputDir);
            return;
        }
        final String inputDirPath = inputDir.getAbsolutePath();
        final String outputDirPath = outputDir.getAbsolutePath();
        if (inputDir.isDirectory()) {
            for (final File file : com.android.utils.FileUtils.getAllFiles(inputDir)) {
                worker.submit(() -> {
                    String filePath = file.getAbsolutePath();
                    File outputFile = new File(filePath.replace(inputDirPath, outputDirPath));
                    bytecodeWeaver.weaveSingleClassToFile(file, outputFile, inputDirPath);
                    return null;
                });
            }
        }
    }

    private void transformJar(final File srcJar, final File destJar) {
        worker.submit(() -> {
            if (!injectByteCode) {
                FileUtils.copyFile(srcJar, destJar);
                return null;
            }
            bytecodeWeaver.weaveJar(srcJar, destJar);
            return null;
        });
    }

    private void cleanDexBuilderFolder(File dest) {
        worker.submit(() -> {
            try {
                String dexBuilderDir = replaceLastPart(dest.getAbsolutePath(), getName());
                // intermediates/transforms/dexBuilder/debug
                File file = new File(dexBuilderDir).getParentFile();
                pluginHelper.log("Clean dexBuilder folder = " + file.getAbsolutePath());
                if (file.exists() && file.isDirectory()) {
                    com.android.utils.FileUtils.deleteDirectoryContents(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    private String replaceLastPart(String originString, String replacement) {
        int start = originString.lastIndexOf(replacement);
        return originString.substring(0, start) + "dexBuilder" +
                originString.substring(start + replacement.length());
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    protected boolean inDuplicatedClassSafeMode() {
        return false;
    }
}
