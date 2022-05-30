package com.lazygeniouz.methlog.plugin;

import com.android.annotations.NonNull;
import com.android.build.gradle.BaseExtension;
import com.lazygeniouz.methlog.plugin.helper.PluginHelper;
import com.lazygeniouz.methlog.transform.MethLogTransform;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.Collections;

public class MethLogPlugin implements Plugin<Project> {

    @Override
    public void apply(@NonNull Project project) {
        PluginHelper pluginHelper = new PluginHelper(project);

        pluginHelper.createMethLogPlugin();
        pluginHelper.addAnnotationsLibrary();

        /*
         * This quickly updates the build variants so
         * we can decide whether to use transformation or not.
         * However, this does not work for Extension value updates.
         *
         * The extension value is UPDATED but is completely ignored.
         * This happens if there is NO change in Code or any other Gradle scripts.
         *
         * A clean or rebuild is required to toggle the enabling / disabling of the plugin.
         */
        project.afterEvaluate(__ -> pluginHelper.toggleAfterEvaluate());

        BaseExtension baseExtension = pluginHelper.getBaseExtension();
        baseExtension.registerTransform(new MethLogTransform(project), Collections.EMPTY_LIST);
    }
}