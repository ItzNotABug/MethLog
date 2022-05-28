package com.lazygeniouz.methlog.plugin;

import com.android.annotations.NonNull;
import com.android.build.gradle.AppExtension;
import com.lazygeniouz.methlog.transform.MethLogTransform;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;

import java.util.Collections;

public class MethLogPlugin implements Plugin<Project> {

    @Override
    public void apply(@NonNull Project project) {
        addAnnotationsLibrary(project);
        AppExtension appExtension = (AppExtension) project.getProperties().get("android");
        appExtension.registerTransform(new MethLogTransform(project), Collections.EMPTY_LIST);
    }

    private void addAnnotationsLibrary(Project project) {
        DependencyHandler handler = project.getDependencies();
        handler.add("implementation", "com.lazygeniouz.methlog:methlog:1.2.8");
    }
}