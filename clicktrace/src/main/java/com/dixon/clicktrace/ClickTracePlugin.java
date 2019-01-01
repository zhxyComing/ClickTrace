package com.dixon.clicktrace;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Created by dixon.xu on 2018/12/18.
 * <p>
 * Groovy代码更简洁 这里选用Java代码 介于ide对于Groovy太不友好了 经常出现莫名其妙的问题
 */

public class ClickTracePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.getExtensions().create("clickconfig", com.dixon.clicktrace.config.ClickTraceConfig.class);
        registerTransform(project);
    }

    public static void registerTransform(Project project) {

        AppExtension android = project.getExtensions().findByType(AppExtension.class);
        android.registerTransform(new ClickTraceTransform(project));
    }
}
