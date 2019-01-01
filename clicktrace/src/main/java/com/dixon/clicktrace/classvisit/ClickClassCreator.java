package com.dixon.clicktrace.classvisit;

import com.dixon.clicktrace.classcreator.CPADCreator;
import com.dixon.clicktrace.classcreator.ClickSetCreator;
import com.dixon.clicktrace.classcreator.ClickViewHelperCreator;

import com.dixon.clicktrace.utils.ClickFileUtils;

import org.apache.http.util.TextUtils;

import java.io.File;

/**
 * Created by dixon.xu on 2018/12/26.
 * <p>
 * 1.新生成的类都会放到com.dixon.classtrace目录下,这是插件的默认目录;
 * 2.buildDir:即项目的app/build目录;
 * 3.classesDir:即项目的intermediates/classes目录,一般在buildDir目录下;
 * 4.callPath:开发者回调的类的全路径;
 */

public class ClickClassCreator {

    private String buildDir; // 如/Users/dixon.xu/Dji-New/TestClickTrace/app/build
    private String classesDir; // 如/Users/dixon.xu/Dji-New/TestClickTrace/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes

    //AccessibilityDelegate类回调的对象
    private String callPath, callMethod; ////如com/app/dixon/testclicktrace/trace

    public ClickClassCreator(String buildDir, String classesDir, String callPath, String callMethod) {
        if (TextUtils.isEmpty(callPath) || TextUtils.isEmpty(callMethod)) {
            throw new NullPointerException("tracePath cannot null!");
        }
        if (buildDir == null) {
            throw new NullPointerException("buildPath cannot null!");
        }
        if (TextUtils.isEmpty(classesDir)) {
            throw new NullPointerException("classesPath is error!");
        }
        if (!buildDir.endsWith("/")) {
            buildDir = buildDir + "/";
        }
        if (!classesDir.endsWith("/")) {
            classesDir = classesDir + "/";
        }
        this.buildDir = buildDir;
        this.classesDir = classesDir;
        this.callPath = callPath;
        this.callMethod = callMethod;
    }

    //本质对类的创建顺序没有要求 所以没必要设置拦截器
    public void create() {
        createClickPluginAccessibilityDelegate();
        createClickSetListener();
        createClickViewHelper();
    }

    private void createClickPluginAccessibilityDelegate() {
        File file = new CPADCreator(classesDir, callPath, callMethod).create();
        if (file != null) {
            ClickFileUtils.copyCTAuto(file, buildDir);
        }
    }

    private void createClickSetListener() {
        File file = new ClickSetCreator(classesDir).create();
        if (file != null) {
            ClickFileUtils.copyCTAuto(file, buildDir);
        }
    }

    private void createClickViewHelper() {

        //原则1 参数先行 方法后写 getViewPath((View)var0.getParent())
        //原则2 同级调用 顺序生成 var0.getClass().getSimpleName();
        //原则3 条件先行 判断后行
        File file = new ClickViewHelperCreator(classesDir).create();
        if (file != null) {
            ClickFileUtils.copyCTAuto(file, buildDir);
        }
    }
}
