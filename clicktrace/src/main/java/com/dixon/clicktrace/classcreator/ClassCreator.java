package com.dixon.clicktrace.classcreator;

import com.dixon.clicktrace.config.Log;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dixon on 2018/12/30.
 * <p>
 * 简单类的生成模板
 */

public abstract class ClassCreator {

    protected ClassWriter mClassWriter;
    protected String mClassName;//eg:ClickSetListener
    protected String mPackagePath;//eg:com/dixon/classtrace/
    protected String mClassesPath;//eg:

    protected List<MethodCreator> mMethods;

    public ClassCreator(String className, String packagePath, String classesPath) {
        this.mClassName = className;
        this.mPackagePath = packagePath;
        this.mClassesPath = classesPath;
        mClassWriter = new ClassWriter(0);
        mMethods = new ArrayList<>();
    }

    protected abstract void classCreate();

    protected void classCreate(int access, String signature, String superName, String[] interfaces) {
        mClassWriter.visit(Opcodes.V1_5,
                access,
                mPackagePath + mClassName,
                signature,
                superName,
                interfaces);
        mClassWriter.visitSource(mClassName + ".java", null);
        mClassWriter.visitEnd();
    }

    protected abstract void constructCreate();

    //默认构造函数的创建 仅限于public 空参数空返回值的情况
    protected void defaultConstructCreate(String superName) {
        MethodVisitor construct = mClassWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        construct.visitVarInsn(Opcodes.ALOAD, 0);
        construct.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", "()V", false);
        construct.visitInsn(Opcodes.RETURN);
        construct.visitMaxs(1, 1);
        construct.visitEnd();
    }

    protected abstract void fieldCreate();

    protected void methodCreate() {
        for (MethodCreator method : mMethods) {
            method.methodCreate();
        }
    }

    protected void addMethod(MethodCreator methodCreator) {
        mMethods.add(methodCreator);
    }

    protected abstract void methodAdd();

    public static abstract class MethodCreator {

        protected abstract void methodCreate();
    }

    protected File toClasses() {
        byte[] data = mClassWriter.toByteArray();
        File dir = new File(mClassesPath + mPackagePath);
        boolean mkdirs = false;
        if (!dir.exists()) {
            mkdirs = dir.mkdirs();
            Log.o("ClassDir", mClassesPath);
            if (!mkdirs) {
                return null;
            }
        }
        File file = new File(mClassesPath + mPackagePath + mClassName + ".class");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public File create() {
        this.classCreate(); //创建类Title 子类实现
        this.constructCreate(); //创建构造函数 子类实现
        this.fieldCreate(); //创建成员变量 子类实现
        this.methodAdd(); //添加方法 子类实现
        this.methodCreate(); //创建方法
        return this.toClasses(); //输出到class文件夹下
    }
}

    /*
     * public class ClickPluginAccessibilityDelegate extends AccessibilityDelegate {
     * public ClickPluginAccessibilityDelegate() {
     * }
     * public void sendAccessibilityEvent(View var1, int var2) {
     * super.sendAccessibilityEvent(var1, var2);
     * TraceUtil.testClickTrace(var1, var2);
     * }
     * }
     */