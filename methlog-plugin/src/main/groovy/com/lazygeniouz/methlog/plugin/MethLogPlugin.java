package com.lazygeniouz.methlog.plugin;

import com.android.annotations.NonNull;
import com.android.build.gradle.AppExtension;
import com.lazygeniouz.methlog.transform.MethLogTransform;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.ExtensionContainer;

import java.util.Collections;

public class MethLogPlugin implements Plugin<Project> {

    @Override
    public void apply(@NonNull Project project) {

        /*
         * @MethLog can still be added as an annotation.
         * It would be ignored & just won't be process if the plugin is disabled.
         */
        addAnnotationsLibrary(project);

        checkExtensionAndRun(project, () -> {
            AppExtension appExtension = (AppExtension) project.getProperties().get("android");
            appExtension.registerTransform(new MethLogTransform(project), Collections.EMPTY_LIST);
        });
    }

    /**
     * Check if methLog is enabled via extension function.
     * Initialize the Transformer only if enabled, however the value is updated in the
     * Project.afterEvaluate block.
     *
     * @param project Project instance to access extension & logger.
     * @param block   Runnable to process if methLog is enabled via extension.
     */
    private void checkExtensionAndRun(Project project, Runnable block) {
        ExtensionContainer container = project.getExtensions();
        MethLogExtension extension = container.create("methLog", MethLogExtension.class);

        project.afterEvaluate(__ -> {
            if (extension.getEnabled()) {
                block.run();
            } else {
                String message = "MethLog disabled via extension, skipping transformation...";
                project.getLogger().lifecycle(String.format("\n%s", message));
            }
        });
    }

    /**
     * Add the @MethLog annotation to the project's dependency.
     *
     * @param project Project instance to add dependency.
     */
    private void addAnnotationsLibrary(Project project) {
        DependencyHandler handler = project.getDependencies();
        handler.add("implementation", "com.lazygeniouz.methlog:methlog:1.3.0");
    }
}