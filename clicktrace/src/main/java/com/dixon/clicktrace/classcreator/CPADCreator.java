package com.dixon.clicktrace.classcreator;

import com.dixon.clicktrace.config.Log;
import com.dixon.clicktrace.config.Params;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by dixon on 2018/12/30.
 * <p>
 * ClickPluginAccessibilityDelegate类的Creator,具体可以参考build/clicktracke/auto目录
 */

public class CPADCreator extends ClassCreator {

    public static final String className = "ClickPluginAccessibilityDelegate";
    private static final String superName = "android/view/View$AccessibilityDelegate";
    private static final String packagePath = Params.AUTO_CLASS_PATH;

    private String mCallPath, mCallMethod;

    public CPADCreator(String classesPath, String mCallPath, String mCallMethod) {
        super(className, packagePath, classesPath);
        this.mCallPath = mCallPath;
        this.mCallMethod = mCallMethod;
    }

    @Override
    protected void classCreate() {
        classCreate(Opcodes.ACC_PUBLIC,
                null,
                superName,
                null);
    }

    @Override
    protected void constructCreate() {
        defaultConstructCreate(superName);
    }

    @Override
    protected void fieldCreate() {

    }

    @Override
    protected void methodAdd() {
        addMethod(new SendAccessibilityEventMC());
    }

    public class SendAccessibilityEventMC extends MethodCreator {

        public static final String methodName = "sendAccessibilityEvent";
        public static final String desc = "(Landroid/view/View;I)V";

        @Override
        protected void methodCreate() {

            MethodVisitor send = mClassWriter.visitMethod(Opcodes.ACC_PUBLIC,
                    methodName,
                    desc,
                    null,
                    null);
            send.visitCode();
            send.visitVarInsn(Opcodes.ALOAD, 0);
            send.visitVarInsn(Opcodes.ALOAD, 1);
            send.visitVarInsn(Opcodes.ILOAD, 2);
            send.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    superName,
                    methodName,
                    desc);

            //方法内调用目标对象
            send.visitVarInsn(Opcodes.ALOAD, 1);
            send.visitVarInsn(Opcodes.ILOAD, 2);
            send.visitVarInsn(Opcodes.ALOAD, 1);
            send.visitMethodInsn(Opcodes.INVOKESTATIC,
                    packagePath + ClickViewHelperCreator.className,
                    ClickViewHelperCreator.GetViewPathMC.methodName,
                    ClickViewHelperCreator.GetViewPathMC.desc);
            send.visitVarInsn(Opcodes.ALOAD, 1);
            send.visitMethodInsn(Opcodes.INVOKESTATIC,
                    packagePath + ClickViewHelperCreator.className,
                    ClickViewHelperCreator.GetContentMC.methodName,
                    ClickViewHelperCreator.GetContentMC.desc);
            send.visitVarInsn(Opcodes.ALOAD, 1);
            send.visitMethodInsn(Opcodes.INVOKESTATIC,
                    packagePath + ClickViewHelperCreator.className,
                    ClickViewHelperCreator.GetPositionMC.methodName,
                    ClickViewHelperCreator.GetPositionMC.desc);
            //下面这行代码 为要调用的方法，请酌情修改
            Log.o("Trace", mCallPath + " " + mCallMethod);
            send.visitMethodInsn(Opcodes.INVOKESTATIC,
                    mCallPath,
                    mCallMethod,
                    "(Landroid/view/View;ILjava/lang/String;Ljava/lang/String;I)V");

            send.visitInsn(Opcodes.RETURN);
            send.visitMaxs(3, 3);
            send.visitEnd();
        }
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