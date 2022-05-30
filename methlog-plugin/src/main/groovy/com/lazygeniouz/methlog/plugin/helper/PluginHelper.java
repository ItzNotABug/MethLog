package com.lazygeniouz.methlog.plugin.helper;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.LibraryPlugin;
import com.lazygeniouz.methlog.plugin.MethLogExtension;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.util.ArrayList;
import java.util.List;

public class PluginHelper {

    private final Project project;
    private static List<String> debuggableVariants;

    public PluginHelper(Project project) {
        this.project = project;
        checkAndroidOnly();
    }

    /**
     * Get Android Extension as BaseExtension.
     *
     * @return BaseExtension
     */
    public BaseExtension getBaseExtension() {
        return (BaseExtension) getAndroidProperty();
    }

    /**
     * Get Android Extension as AppExtension.
     *
     * @return AppExtension
     */
    public AppExtension getAppExtension() {
        return (AppExtension) getAndroidProperty();
    }

    /**
     * Get Android Extension as LibraryExtension.
     *
     * @return LibraryExtension
     */
    public LibraryExtension getLibraryExtension() {
        return (LibraryExtension) getAndroidProperty();
    }

    /**
     * Create the MethLog plugin extension.
     */
    public void createMethLogPlugin() {
        project.getExtensions().create("methLog", MethLogExtension.class);
    }

    /**
     * MethLog can be added as an annotation.
     * It would be ignored & just won't be process if the plugin is disabled.
     */
    public void addAnnotationsLibrary() {
        DependencyHandler handler = project.getDependencies();
        handler.add("implementation", "com.lazygeniouz.methlog:methlog:1.3.2");
    }

    /**
     * Check if a given variant is debuggable.
     *
     * @param variant Name of the current build.
     * @return True if debuggable, False otherwise.
     */
    public boolean checkIfBuildDebuggable(String variant) {
        return debuggableVariants.contains(variant.toLowerCase());
    }

    /**
     * Check if the plugin is enabled via the extension block
     */
    public boolean isMethLogEnabled() {
        return getMethLogExtension().getEnabled();
    }

    /**
     * Update the variants list after an evaluation.
     */
    public void toggleAfterEvaluate() {
        listDebuggableBuildVariants();
    }

    // Plain old logging.
    public void log(String msg) {
        String message = String.format("MethLog: %s", msg);
        project.getLogger().lifecycle(message);
    }

    private void checkAndroidOnly() {
        boolean isAndroid = hasPlugin(AppPlugin.class) || hasPlugin(LibraryPlugin.class);
        if (!isAndroid) {
            throw new GradleException("'com.android.application' or 'com.android.library' plugin required.");
        }
    }

    private void listDebuggableBuildVariants() {
        if (debuggableVariants != null && !debuggableVariants.isEmpty()) {
            debuggableVariants.clear();
        }

        debuggableVariants = new ArrayList<>();
        boolean isApp = hasPlugin(AppPlugin.class);
        boolean isLibrary = hasPlugin(LibraryPlugin.class);

        if (isApp) {
            AppExtension appExtension = getAppExtension();
            appExtension.getBuildTypes().forEach(variant -> {
                boolean isDebuggable = variant.isDebuggable();
                if (isDebuggable) debuggableVariants.add(variant.getName().toLowerCase());
            });
        } else if (isLibrary) {
            LibraryExtension libraryExtension = getLibraryExtension();
            libraryExtension.getBuildTypes().forEach(variant -> {
                boolean isDebuggable = variant.isDebuggable();
                if (isDebuggable) debuggableVariants.add(variant.getName().toLowerCase());
            });
        }
    }

    private MethLogExtension getMethLogExtension() {
        return (MethLogExtension) project.getExtensions().getByName("methLog");
    }

    private boolean hasPlugin(Class<? extends Plugin<?>> plugin) {
        return project.getPlugins().hasPlugin(plugin);
    }

    private Object getAndroidProperty() {
        return project.getProperties().get("android");
    }
}
