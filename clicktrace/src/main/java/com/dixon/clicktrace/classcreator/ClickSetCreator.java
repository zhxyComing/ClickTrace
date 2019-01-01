package com.dixon.clicktrace.classcreator;

import com.dixon.clicktrace.config.Params;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by dixon on 2018/12/31.
 * <p>
 * ClickSetCreator类的生成,具体可以参考build/clicktracke/auto目录
 */

public class ClickSetCreator extends ClassCreator {

    public static final String className = "ClickSetListener";
    public static final String packagePath = Params.AUTO_CLASS_PATH;

    public ClickSetCreator(String classesPath) {
        super(className, packagePath, classesPath);
    }

    @Override
    protected void classCreate() {
        classCreate(Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                null,
                "java/lang/Object",
                new String[]{"android/view/ViewTreeObserver$OnGlobalLayoutListener"});
    }

    @Override
    protected void constructCreate() {
        MethodVisitor click_construct = mClassWriter.visitMethod(
                Opcodes.ACC_PUBLIC,
                "<init>",
                "(Landroid/app/Activity;)V",
                null,
                null);
        click_construct.visitVarInsn(Opcodes.ALOAD, 0);
        click_construct.visitMethodInsn(Opcodes.INVOKESPECIAL,
                "java/lang/Object",
                "<init>",
                "()V",
                false);
        click_construct.visitVarInsn(Opcodes.ALOAD, 0);
        click_construct.visitVarInsn(Opcodes.ALOAD, 1);
        click_construct.visitFieldInsn(Opcodes.PUTFIELD,
                packagePath + className,
                "baseAty",
                "Landroid/app/Activity;");
        click_construct.visitInsn(Opcodes.RETURN);
        click_construct.visitMaxs(2, 2);
        click_construct.visitEnd();
    }

    @Override
    protected void fieldCreate() {
        //创建成员变量 activity
        FieldVisitor fv = mClassWriter.visitField(Opcodes.ACC_PRIVATE,
                "baseAty",
                "Landroid/app/Activity;",
                null,
                null);
        fv.visitEnd();
    }

    @Override
    protected void methodAdd() {
        addMethod(new OnGlobalLayoutMC());
        addMethod(new ClickPluginTrackMC());
        addMethod(new ClickPluginEventMC());
    }

    private class OnGlobalLayoutMC extends MethodCreator {

        private static final String methodName = "onGlobalLayout";
        private static final String desc = "()V";

        @Override
        protected void methodCreate() {
            MethodVisitor layout = mClassWriter.visitMethod(Opcodes.ACC_PUBLIC,
                    methodName, desc, null, null);
            layout.visitCode();
            //调用clickPluginTrack
            layout.visitVarInsn(Opcodes.ALOAD, 0);
            layout.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    mPackagePath + mClassName,
                    ClickPluginTrackMC.methodName, ClickPluginTrackMC.desc);

            layout.visitInsn(Opcodes.RETURN);
            layout.visitMaxs(1, 1);
            layout.visitEnd();
        }
    }

    private class ClickPluginTrackMC extends MethodCreator {

        private static final String methodName = "clickPluginTrack";
        private static final String desc = "()V";

        @Override
        protected void methodCreate() {
            MethodVisitor clickTrace = mClassWriter.visitMethod(Opcodes.ACC_PRIVATE,
                    methodName, desc, null, null);
            clickTrace.visitCode();
            clickTrace.visitVarInsn(Opcodes.ALOAD, 0);
            clickTrace.visitFieldInsn(Opcodes.GETFIELD,
                    packagePath + className, "baseAty", "Landroid/app/Activity;");
            //android/app/Activity/getWindow 这里写错会报this错误...
            clickTrace.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/app/Activity", "getWindow", "()Landroid/view/Window;");
            clickTrace.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/Window", "getDecorView", "()Landroid/view/View;");
            clickTrace.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getRootView", "()Landroid/view/View;");
            clickTrace.visitTypeInsn(Opcodes.CHECKCAST, "android/view/ViewGroup");
            clickTrace.visitVarInsn(Opcodes.ASTORE, 2);
            clickTrace.visitVarInsn(Opcodes.ALOAD, 0);
            clickTrace.visitVarInsn(Opcodes.ALOAD, 2);
            clickTrace.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    packagePath + className, ClickPluginEventMC.methodName, ClickPluginEventMC.desc);
            clickTrace.visitInsn(Opcodes.RETURN);
            clickTrace.visitMaxs(1, 1);
            clickTrace.visitEnd();
        }

    }

    private class ClickPluginEventMC extends MethodCreator {

        public static final String methodName = "clickPluginEvent";
        public static final String desc = "(Landroid/view/ViewGroup;)V";

        @Override
        protected void methodCreate() {
            MethodVisitor clickEvent = mClassWriter.visitMethod(Opcodes.ACC_PRIVATE,
                    methodName, desc, null, null);
            clickEvent.visitCode();

            //For循环头 for(int var2 = 0; var2 < var1.getChildCount(); ++var2) {
            Label forLabel = new Label();
            Label endLabel = new Label();
            clickEvent.visitInsn(Opcodes.ICONST_0);
            clickEvent.visitVarInsn(Opcodes.ISTORE, 2);
            clickEvent.visitLabel(forLabel);
            clickEvent.visitVarInsn(Opcodes.ILOAD, 2);
            clickEvent.visitVarInsn(Opcodes.ALOAD, 1);
            clickEvent.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/ViewGroup", "getChildCount", "()I");
            clickEvent.visitJumpInsn(Opcodes.IF_ICMPGE, endLabel);

            //View var3 = var1.getChildAt(var2);
            clickEvent.visitVarInsn(Opcodes.ALOAD, 1);
            clickEvent.visitVarInsn(Opcodes.ILOAD, 2);
            clickEvent.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/ViewGroup", "getChildAt", "(I)Landroid/view/View;");
            clickEvent.visitVarInsn(Opcodes.ASTORE, 3);
            clickEvent.visitVarInsn(Opcodes.ALOAD, 3);

            //IF头 if(var3 instanceof ViewGroup) {
            Label iflable = new Label();
            clickEvent.visitTypeInsn(Opcodes.INSTANCEOF, "android/view/ViewGroup");
            clickEvent.visitJumpInsn(Opcodes.IFEQ, iflable);

            //this.clickPluginEvent((ViewGroup)var3);
            clickEvent.visitVarInsn(Opcodes.ALOAD, 0);
            clickEvent.visitVarInsn(Opcodes.ALOAD, 3);
            clickEvent.visitTypeInsn(Opcodes.CHECKCAST, "android/view/ViewGroup");
            clickEvent.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    packagePath + className, methodName, desc);

            //IF中
            clickEvent.visitJumpInsn(Opcodes.GOTO, iflable);
            //IF尾
            clickEvent.visitLabel(iflable);

            //必须先调用元素
            clickEvent.visitVarInsn(Opcodes.ALOAD, 3);
            clickEvent.visitVarInsn(Opcodes.ALOAD, 3);
            clickEvent.visitTypeInsn(Opcodes.NEW, packagePath + CPADCreator.className);
            clickEvent.visitInsn(Opcodes.DUP);
            clickEvent.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    packagePath + CPADCreator.className, "<init>", "()V"); //注意第二个参数是owner 这里是这个类的初始化
            clickEvent.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "setAccessibilityDelegate", "(Landroid/view/View$AccessibilityDelegate;)V");

            //For循环尾
            clickEvent.visitIincInsn(2, 1);
            clickEvent.visitJumpInsn(Opcodes.GOTO, forLabel);
            clickEvent.visitLabel(endLabel);

            clickEvent.visitInsn(Opcodes.RETURN);
            clickEvent.visitMaxs(3, 4);
            clickEvent.visitEnd();
        }

    }
}

    /*
     *public class ClickSetListener implements OnGlobalLayoutListener {
     *private Activity baseAty;
     *
     *public ClickSetListener(Activity var1) {
     *  this.baseAty = var1;
     *}
     *
     *public void onGlobalLayout() {
     *   this.clickPluginTrack();
     *}
     *
     *private void clickPluginTrack() {
     *   ViewGroup var2 = (ViewGroup)this.baseAty.getWindow().getDecorView().getRootView();
     *   this.clickPluginEvent(var2);
     *}
     *
     *private void clickPluginEvent(ViewGroup var1) {
     *   for(int var2 = 0; var2 < var1.getChildCount(); ++var2) {
     *      View var3 = var1.getChildAt(var2);
     *      if(var3 instanceof ViewGroup) {
     *         this.clickPluginEvent((ViewGroup)var3);
     *    }
     *
     *    var3.setAccessibilityDelegate(new ClickPluginAccessibilityDelegate());
     *  }
     *}
     *}
     */