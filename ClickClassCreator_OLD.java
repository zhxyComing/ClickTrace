package com.dixon.clicktrace.classvisit;

import com.dixon.clicktrace.config.Log;
import com.dixon.clicktrace.config.Params;
import com.dixon.clicktrace.utils.ClickFileUtils;

import org.apache.http.util.TextUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by dixon.xu on 2018/12/26.
 * <p>
 * Expired ClickClassCreator
 */

public class ClickClassCreator_OLD {

    private String buildDir; // 如/Users/dixon.xu/Dji-New/TestClickTrace/app/build
    private String classesDir; // 如/Users/dixon.xu/Dji-New/TestClickTrace/app/build/intermediates/javac/debug/compileDebugJavaWithJavac/classes

    //AccessibilityDelegate类回调的对象
    private String targetPath, targetMethod; ////如com/app/dixon/testclicktrace/trace

    public ClickClassCreator_OLD(String buildDir, String classesDir, String targetPath, String targetMethod) {
        if (TextUtils.isEmpty(targetPath) || TextUtils.isEmpty(targetMethod)) {
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
        this.targetPath = targetPath;
        this.targetMethod = targetMethod;
    }

    public void create() {
        createClickPluginAccessibilityDelegate();
        createClickSetListener();
        createClickViewHelper();
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
    private void createClickPluginAccessibilityDelegate() {

        //生成类
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_5,
//                Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                Opcodes.ACC_PUBLIC,
                Params.AUTO_CLASS_PATH + Params.CLICK_ACCESS_DELEGATE,
                null,
                "android/view/View$AccessibilityDelegate",
                null);
        cw.visitSource(Params.CLICK_ACCESS_DELEGATE + ".java", null);
        cw.visitEnd();

        //创建构造函数
        MethodVisitor construct = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        construct.visitVarInsn(Opcodes.ALOAD, 0);
        construct.visitMethodInsn(Opcodes.INVOKESPECIAL, "android/view/View$AccessibilityDelegate", "<init>", "()V", false);
        construct.visitInsn(Opcodes.RETURN);
        construct.visitMaxs(1, 1);
        construct.visitEnd();

        //创建sendAccessibilityEvent方法 ❗注意(Landroid/view/View;I)V 不是(Landroid.view.View;I)V
        MethodVisitor send = cw.visitMethod(Opcodes.ACC_PUBLIC, "sendAccessibilityEvent", "(Landroid/view/View;I)V", null, null);
        send.visitCode();
        send.visitVarInsn(Opcodes.ALOAD, 0);
        send.visitVarInsn(Opcodes.ALOAD, 1);
        send.visitVarInsn(Opcodes.ILOAD, 2);
        send.visitMethodInsn(Opcodes.INVOKESPECIAL, "android/view/View$AccessibilityDelegate", "sendAccessibilityEvent", "(Landroid/view/View;I)V");

        /*Log
        send.visitLdcInsn("testTag");
        send.visitLdcInsn("testMsg");
        send.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I");
        send.visitInsn(Opcodes.POP);
        */

        //方法内调用目标对象
        send.visitVarInsn(Opcodes.ALOAD, 1);
        send.visitVarInsn(Opcodes.ILOAD, 2);
        send.visitVarInsn(Opcodes.ALOAD, 1);
        send.visitMethodInsn(Opcodes.INVOKESTATIC, Params.AUTO_CLASS_PATH + Params.CLICK_VIEW_HELPER, "getViewPath", "(Landroid/view/View;)Ljava/lang/String;");
        send.visitVarInsn(Opcodes.ALOAD, 1);
        send.visitMethodInsn(Opcodes.INVOKESTATIC, Params.AUTO_CLASS_PATH + Params.CLICK_VIEW_HELPER, "getContent", "(Landroid/view/View;)Ljava/lang/String;");
        send.visitVarInsn(Opcodes.ALOAD, 1);
        send.visitMethodInsn(Opcodes.INVOKESTATIC, Params.AUTO_CLASS_PATH + Params.CLICK_VIEW_HELPER, "getPosition", "(Landroid/view/View;)I");
        //下面这行代码 为要调用的方法，请酌情修改
        Log.o("Trace", targetPath + " " + targetMethod);
        send.visitMethodInsn(Opcodes.INVOKESTATIC,
                targetPath,
                targetMethod, "(Landroid/view/View;ILjava/lang/String;Ljava/lang/String;I)V");

        send.visitInsn(Opcodes.RETURN);
        send.visitMaxs(3, 3);
        send.visitEnd();

        byte[] data = cw.toByteArray();

        File dir = new File(classesDir + Params.AUTO_CLASS_PATH);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            Log.o("ClassDir", classesDir);
        }
        File file = new File(classesDir + Params.AUTO_CLASS_PATH + Params.CLICK_ACCESS_DELEGATE + ".class");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ClickFileUtils.copyCTAuto(file, buildDir);
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
    private void createClickSetListener() {

        //创建类
        ClassWriter clickListener = new ClassWriter(0);
        clickListener.visit(Opcodes.V1_5,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                Params.AUTO_CLASS_PATH + Params.CLICK_SET_LISTENER,
                null,
                "java/lang/Object",
                new String[]{"android/view/ViewTreeObserver$OnGlobalLayoutListener"});
        clickListener.visitSource(Params.CLICK_SET_LISTENER + ".java", null);
        clickListener.visitEnd();

        //创建成员变量 activity
        FieldVisitor fv = clickListener.visitField(Opcodes.ACC_PRIVATE, "baseAty", "Landroid/app/Activity;", null, null);
        fv.visitEnd();

        //创建构造函数
        MethodVisitor click_construct = clickListener.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Landroid/app/Activity;)V", null, null);
        click_construct.visitVarInsn(Opcodes.ALOAD, 0);
        click_construct.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        click_construct.visitVarInsn(Opcodes.ALOAD, 0);
        click_construct.visitVarInsn(Opcodes.ALOAD, 1);
        click_construct.visitFieldInsn(Opcodes.PUTFIELD, Params.AUTO_CLASS_PATH + Params.CLICK_SET_LISTENER, "baseAty", "Landroid/app/Activity;");
        click_construct.visitInsn(Opcodes.RETURN);
        click_construct.visitMaxs(2, 2);
        click_construct.visitEnd();

        //重写onGlobalLayout方法
        MethodVisitor layout = clickListener.visitMethod(Opcodes.ACC_PUBLIC, "onGlobalLayout", "()V", null, null);
        layout.visitCode();
        //调用clickPluginTrack
        layout.visitVarInsn(Opcodes.ALOAD, 0);
        layout.visitMethodInsn(Opcodes.INVOKESPECIAL, Params.AUTO_CLASS_PATH + Params.CLICK_SET_LISTENER, Params.CLICK_TRACK, "()V");
        //Log
//        send.visitLdcInsn("testTag");
//        send.visitLdcInsn("testMsg");
//        send.visitMethodInsn(Opcodes.INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I");
//        send.visitInsn(Opcodes.POP);

        layout.visitInsn(Opcodes.RETURN);
        layout.visitMaxs(1, 1);
        layout.visitEnd();

        //创建clickPluginTrack
        //不清楚为什么这里只能private
        MethodVisitor clickTrace = clickListener.visitMethod(Opcodes.ACC_PRIVATE, Params.CLICK_TRACK, "()V", null, null);
        clickTrace.visitCode();
        clickTrace.visitVarInsn(Opcodes.ALOAD, 0);
        clickTrace.visitFieldInsn(Opcodes.GETFIELD, Params.AUTO_CLASS_PATH + Params.CLICK_SET_LISTENER, "baseAty", "Landroid/app/Activity;");
        //android/app/Activity/getWindow 这里写错会报this错误...
        clickTrace.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/app/Activity", "getWindow", "()Landroid/view/Window;");
        clickTrace.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/Window", "getDecorView", "()Landroid/view/View;");
        clickTrace.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getRootView", "()Landroid/view/View;");
        clickTrace.visitTypeInsn(Opcodes.CHECKCAST, "android/view/ViewGroup");
        clickTrace.visitVarInsn(Opcodes.ASTORE, 2);
        clickTrace.visitVarInsn(Opcodes.ALOAD, 0);
        clickTrace.visitVarInsn(Opcodes.ALOAD, 2);
        clickTrace.visitMethodInsn(Opcodes.INVOKESPECIAL, Params.AUTO_CLASS_PATH + Params.CLICK_SET_LISTENER, Params.CLICK_EVENT, "(Landroid/view/ViewGroup;)V");
        clickTrace.visitInsn(Opcodes.RETURN);
        clickTrace.visitMaxs(1, 1);
        clickTrace.visitEnd();

        //创建clickPluginEvent
        MethodVisitor clickEvent = clickListener.visitMethod(Opcodes.ACC_PRIVATE, Params.CLICK_EVENT, "(Landroid/view/ViewGroup;)V", null, null);
        clickEvent.visitCode();

        //For循环头 for(int var2 = 0; var2 < var1.getChildCount(); ++var2) {
        Label forLabel = new Label();
        Label endLabel = new Label();
        clickEvent.visitInsn(Opcodes.ICONST_0);
        clickEvent.visitVarInsn(Opcodes.ISTORE, 2);
        clickEvent.visitLabel(forLabel);
        clickEvent.visitVarInsn(Opcodes.ILOAD, 2);
        clickEvent.visitVarInsn(Opcodes.ALOAD, 1);
        clickEvent.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/ViewGroup", "getChildCount", "()I");
        clickEvent.visitJumpInsn(Opcodes.IF_ICMPGE, endLabel);

        //View var3 = var1.getChildAt(var2);
        clickEvent.visitVarInsn(Opcodes.ALOAD, 1);
        clickEvent.visitVarInsn(Opcodes.ILOAD, 2);
        clickEvent.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/ViewGroup", "getChildAt", "(I)Landroid/view/View;");
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
        clickEvent.visitMethodInsn(Opcodes.INVOKESPECIAL, Params.AUTO_CLASS_PATH + Params.CLICK_SET_LISTENER, Params.CLICK_EVENT, "(Landroid/view/ViewGroup;)V");

        //IF中
        clickEvent.visitJumpInsn(Opcodes.GOTO, iflable);
        //IF尾
        clickEvent.visitLabel(iflable);

        //必须先调用元素
        clickEvent.visitVarInsn(Opcodes.ALOAD, 3);
        clickEvent.visitVarInsn(Opcodes.ALOAD, 3);
        clickEvent.visitTypeInsn(Opcodes.NEW, Params.AUTO_CLASS_PATH + Params.CLICK_ACCESS_DELEGATE);
        clickEvent.visitInsn(Opcodes.DUP);
        clickEvent.visitMethodInsn(Opcodes.INVOKESPECIAL, Params.AUTO_CLASS_PATH + Params.CLICK_ACCESS_DELEGATE, "<init>", "()V"); //注意第二个参数是owner 这里是这个类的初始化
//        clickEvent.visitMethodInsn(Opcodes.INVOKESPECIAL, delegatePath, "<init>", "()V"); //注意第二个参数是owner 这里是这个类的初始化
        clickEvent.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "setAccessibilityDelegate", "(Landroid/view/View$AccessibilityDelegate;)V");

        //For循环尾
        clickEvent.visitIincInsn(2, 1);
        clickEvent.visitJumpInsn(Opcodes.GOTO, forLabel);
        clickEvent.visitLabel(endLabel);

        clickEvent.visitInsn(Opcodes.RETURN);
        clickEvent.visitMaxs(3, 4);
        clickEvent.visitEnd();

        byte[] data = clickListener.toByteArray();
        File file = new File(classesDir + Params.AUTO_CLASS_PATH + Params.CLICK_SET_LISTENER + ".class");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ClickFileUtils.copyCTAuto(file, buildDir);
    }

    private void createClickViewHelper() {

        //原则 参数先行 方法后写 getViewPath((View)var0.getParent())
        //原则 同级调用 顺序生成 var0.getClass().getSimpleName();
        //原则 条件先行 判断后行

        //创建类
        ClassWriter clickListener = new ClassWriter(0);
        clickListener.visit(Opcodes.V1_5,
                Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                Params.AUTO_CLASS_PATH + Params.CLICK_VIEW_HELPER,
                null,
                "java/lang/Object",
                null);
        clickListener.visitSource(Params.CLICK_VIEW_HELPER + ".java", null);
        clickListener.visitEnd();

        /**getViewPath方法*/
        //String var1 = var0.getClass().getSimpleName();
        //return var0.getParent() != null && var0.getParent() instanceof View?getViewPath((View)var0.getParent()) + "/" + var1:var1;
        MethodVisitor getViewPath = clickListener.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "getViewPath", "(Landroid/view/View;)Ljava/lang/String;", null, null);
        getViewPath.visitCode();

        getViewPath.visitVarInsn(Opcodes.ALOAD, 0);
        getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
        getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;");
        //加入第一个临时参数 0是参数
        getViewPath.visitVarInsn(Opcodes.ASTORE, 1);
        //第一个判断
        getViewPath.visitVarInsn(Opcodes.ALOAD, 0);
        getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        Label ifNull = new Label();
        getViewPath.visitJumpInsn(Opcodes.IFNULL, ifNull);

        //第二个判断
        getViewPath.visitVarInsn(Opcodes.ALOAD, 0);
        getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        getViewPath.visitTypeInsn(Opcodes.INSTANCEOF, "android/view/View");
        Label ifType = new Label();
        getViewPath.visitJumpInsn(Opcodes.IFEQ, ifType);

        getViewPath.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
        getViewPath.visitInsn(Opcodes.DUP);
        getViewPath.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
        getViewPath.visitVarInsn(Opcodes.ALOAD, 0);
        getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        getViewPath.visitTypeInsn(Opcodes.CHECKCAST, "android/view/View");
        getViewPath.visitMethodInsn(Opcodes.INVOKESTATIC, Params.AUTO_CLASS_PATH + Params.CLICK_VIEW_HELPER, "getViewPath", "(Landroid/view/View;)Ljava/lang/String;");
        getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        getViewPath.visitLdcInsn("/");
        getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        //调用第一个临时参数
        getViewPath.visitVarInsn(Opcodes.ALOAD, 1);
        getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        //先return 再if
        getViewPath.visitInsn(Opcodes.ARETURN);
        getViewPath.visitLabel(ifNull);
        getViewPath.visitLabel(ifType);
        getViewPath.visitVarInsn(Opcodes.ALOAD, 1);
        getViewPath.visitInsn(Opcodes.ARETURN);

        getViewPath.visitInsn(Opcodes.RETURN);
        getViewPath.visitMaxs(2, 2);
        getViewPath.visitEnd();

        /**getContent方法*/
        //host instanceof TextView ? ((TextView) host).getText().toString() : "This not textView";
        MethodVisitor getContent = clickListener.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "getContent", "(Landroid/view/View;)Ljava/lang/String;", null, null);
        getContent.visitCode();

        //第一个判断
        getContent.visitVarInsn(Opcodes.ALOAD, 0);
        getContent.visitTypeInsn(Opcodes.INSTANCEOF, "android/widget/TextView");
        Label ifContentType = new Label();
        getContent.visitJumpInsn(Opcodes.IFEQ, ifContentType);

        //!参数先行
        getContent.visitVarInsn(Opcodes.ALOAD, 0);
        getContent.visitTypeInsn(Opcodes.CHECKCAST, "android/widget/TextView");
        getContent.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/widget/TextView", "getText", "()Ljava/lang/CharSequence;");
        getContent.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/lang/CharSequence", "toString", "()Ljava/lang/String;");

        //先return 再if
        getContent.visitInsn(Opcodes.ARETURN);
        getContent.visitLabel(ifContentType);

        getContent.visitLdcInsn("");
        getContent.visitInsn(Opcodes.ARETURN);
        getContent.visitMaxs(1, 1);
        getContent.visitEnd();

        /**getListView*/
        //return view.getParent() != null && view.getParent() instanceof ListView?(ListView)view.getParent():(view.getParent() != null?(view.getParent() instanceof View?getListView((View)view.getParent()):null):null);
        MethodVisitor getListView = clickListener.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "getListView", "(Landroid/view/View;)Landroid/widget/ListView;", null, null);
        getListView.visitCode();

        //第一个判断
        getListView.visitVarInsn(Opcodes.ALOAD, 0);
        getListView.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        Label glvIfNull1 = new Label();
        getListView.visitJumpInsn(Opcodes.IFNULL, glvIfNull1);
        getListView.visitVarInsn(Opcodes.ALOAD, 0);
        getListView.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        getListView.visitTypeInsn(Opcodes.INSTANCEOF, "android/widget/ListView");
        Label glvIfEQ1 = new Label();
        getListView.visitJumpInsn(Opcodes.IFEQ, glvIfEQ1);
        getListView.visitVarInsn(Opcodes.ALOAD, 0);
        getListView.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        getListView.visitTypeInsn(Opcodes.CHECKCAST, "android/widget/ListView");
        getListView.visitInsn(Opcodes.ARETURN);
        getListView.visitLabel(glvIfNull1);
        getListView.visitLabel(glvIfEQ1);

        //第二个判断
        getListView.visitVarInsn(Opcodes.ALOAD, 0);
        getListView.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        Label glvIfNull2 = new Label();
        getListView.visitJumpInsn(Opcodes.IFNULL, glvIfNull2);
        getListView.visitVarInsn(Opcodes.ALOAD, 0);
        getListView.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        getListView.visitTypeInsn(Opcodes.INSTANCEOF, "android/view/View");
        //第三个判断
        Label glvIfEQ2 = new Label();
        getListView.visitJumpInsn(Opcodes.IFEQ, glvIfEQ2);
        getListView.visitVarInsn(Opcodes.ALOAD, 0);
        getListView.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        getListView.visitTypeInsn(Opcodes.CHECKCAST, "android/view/View");
        getListView.visitMethodInsn(Opcodes.INVOKESTATIC, Params.AUTO_CLASS_PATH + Params.CLICK_VIEW_HELPER, "getListView", "(Landroid/view/View;)Landroid/widget/ListView;");
        getListView.visitInsn(Opcodes.ARETURN);
        getListView.visitLabel(glvIfEQ2);
        getListView.visitInsn(Opcodes.ACONST_NULL);
        getListView.visitInsn(Opcodes.ARETURN);
        getListView.visitLabel(glvIfNull2);
        getListView.visitInsn(Opcodes.ACONST_NULL);
        getListView.visitInsn(Opcodes.ARETURN);
        getListView.visitMaxs(1, 1);
        getListView.visitEnd();

        /**getListViewItem*/
        //return view.getParent() != null && view.getParent() instanceof ListView?view:(view.getParent() != null?(view.getParent() instanceof View?getListViewItem((View)view.getParent()):null):null);
        MethodVisitor getListViewItem = clickListener.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "getListViewItem", "(Landroid/view/View;)Landroid/view/View;", null, null);
        getListViewItem.visitCode();

        //第一个判断
        getListViewItem.visitVarInsn(Opcodes.ALOAD, 0);
        getListViewItem.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        Label glvIfNull3 = new Label();
        getListViewItem.visitJumpInsn(Opcodes.IFNULL, glvIfNull3);
        getListViewItem.visitVarInsn(Opcodes.ALOAD, 0);
        getListViewItem.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        getListViewItem.visitTypeInsn(Opcodes.INSTANCEOF, "android/widget/ListView");
        Label glvIfEQ3 = new Label();
        getListViewItem.visitJumpInsn(Opcodes.IFEQ, glvIfEQ3);
        getListViewItem.visitVarInsn(Opcodes.ALOAD, 0);
        getListViewItem.visitInsn(Opcodes.ARETURN);
        getListViewItem.visitLabel(glvIfNull3);
        getListViewItem.visitLabel(glvIfEQ3);

        //第二个判断
        getListViewItem.visitVarInsn(Opcodes.ALOAD, 0);
        getListViewItem.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        Label glvIfNull4 = new Label();
        getListViewItem.visitJumpInsn(Opcodes.IFNULL, glvIfNull4);
        getListViewItem.visitVarInsn(Opcodes.ALOAD, 0);
        getListViewItem.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        getListViewItem.visitTypeInsn(Opcodes.INSTANCEOF, "android/view/View");
        //第三个判断
        Label glvIfEQ4 = new Label();
        getListViewItem.visitJumpInsn(Opcodes.IFEQ, glvIfEQ4);
        getListViewItem.visitVarInsn(Opcodes.ALOAD, 0);
        getListViewItem.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getParent", "()Landroid/view/ViewParent;");
        getListViewItem.visitTypeInsn(Opcodes.CHECKCAST, "android/view/View");
        getListViewItem.visitMethodInsn(Opcodes.INVOKESTATIC, Params.AUTO_CLASS_PATH + Params.CLICK_VIEW_HELPER, "getListViewItem", "(Landroid/view/View;)Landroid/view/View;");
        getListViewItem.visitInsn(Opcodes.ARETURN);
        getListViewItem.visitLabel(glvIfEQ4);
        getListViewItem.visitInsn(Opcodes.ACONST_NULL);
        getListViewItem.visitInsn(Opcodes.ARETURN);
        getListViewItem.visitLabel(glvIfNull4);
        getListViewItem.visitInsn(Opcodes.ACONST_NULL);
        getListViewItem.visitInsn(Opcodes.ARETURN);
        getListViewItem.visitMaxs(1, 1);
        getListViewItem.visitEnd();


        /**getPosition*/
        /*
        ListView listView = getListView(view);
        View item = getListViewItem(view);
        if(listView != null && item != null) {
            View childAt0 = listView.getChildAt(0);
            int realTop = listView.getFirstVisiblePosition() * childAt0.getMeasuredHeight() + Math.abs(childAt0.getTop()) + item.getTop();
            int itemHeight = childAt0.getMeasuredHeight();
            return realTop / itemHeight;
        } else {
            return -1;
        }
         */
        //return view.getParent() != null && view.getParent() instanceof ListView?view:(view.getParent() != null?(view.getParent() instanceof View?getListViewItem((View)view.getParent()):null):null);
        MethodVisitor getPosition = clickListener.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "getPosition", "(Landroid/view/View;)I", null, null);
        getPosition.visitCode();

        //存入listView和item
        getPosition.visitVarInsn(Opcodes.ALOAD, 0);
        getPosition.visitMethodInsn(Opcodes.INVOKESTATIC, Params.AUTO_CLASS_PATH + Params.CLICK_VIEW_HELPER, "getListView", "(Landroid/view/View;)Landroid/widget/ListView;");
        getPosition.visitVarInsn(Opcodes.ASTORE, 1);
        getPosition.visitVarInsn(Opcodes.ALOAD, 0);
        getPosition.visitMethodInsn(Opcodes.INVOKESTATIC, Params.AUTO_CLASS_PATH + Params.CLICK_VIEW_HELPER, "getListViewItem", "(Landroid/view/View;)Landroid/view/View;");
        getPosition.visitVarInsn(Opcodes.ASTORE, 2);

        getPosition.visitVarInsn(Opcodes.ALOAD, 1);
        Label gpIfNull1 = new Label();
        getPosition.visitJumpInsn(Opcodes.IFNULL, gpIfNull1);
        getPosition.visitVarInsn(Opcodes.ALOAD, 2);
        Label gpIfNonNull1 = new Label();
        getPosition.visitJumpInsn(Opcodes.IFNULL, gpIfNonNull1);

        //View childAt0 = listView.getChildAt(0);
        getPosition.visitVarInsn(Opcodes.ALOAD, 1);
        getPosition.visitInsn(Opcodes.ICONST_0);
        getPosition.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/widget/ListView", "getChildAt", "(I)Landroid/view/View;");
        getPosition.visitVarInsn(Opcodes.ASTORE, 3);

        //listView.getFirstVisiblePosition()
        getPosition.visitVarInsn(Opcodes.ALOAD, 1);
        getPosition.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/widget/ListView", "getFirstVisiblePosition", "()I");

        //childAt0.getMeasuredHeight()
        getPosition.visitVarInsn(Opcodes.ALOAD, 3);
        getPosition.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getMeasuredHeight", "()I");

        //*乘
        getPosition.visitInsn(Opcodes.IMUL);

        //Math.abs(childAt0.getTop())
        getPosition.visitVarInsn(Opcodes.ALOAD, 3);
        getPosition.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getTop", "()I");
        getPosition.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "abs", "(I)I");

        //+加
        getPosition.visitInsn(Opcodes.IADD);

        //item.getTop()
        getPosition.visitVarInsn(Opcodes.ALOAD, 2);
        getPosition.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getTop", "()I");

        //+加
        getPosition.visitInsn(Opcodes.IADD);

        getPosition.visitVarInsn(Opcodes.ISTORE, 4);

        //childAt0.getMeasuredHeight();
        getPosition.visitVarInsn(Opcodes.ALOAD, 3);
        getPosition.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "android/view/View", "getMeasuredHeight", "()I");

        getPosition.visitVarInsn(Opcodes.ISTORE, 5);

        getPosition.visitVarInsn(Opcodes.ILOAD, 4);
        getPosition.visitVarInsn(Opcodes.ILOAD, 5);
        // / 除
        getPosition.visitInsn(Opcodes.IDIV);

        //return -1
        getPosition.visitInsn(Opcodes.IRETURN);
        getPosition.visitLabel(gpIfNull1);
        getPosition.visitLabel(gpIfNonNull1);

        getPosition.visitInsn(Opcodes.ICONST_M1);
        getPosition.visitInsn(Opcodes.IRETURN);

        getPosition.visitMaxs(2, 6);
        getPosition.visitEnd();

        byte[] data = clickListener.toByteArray();
        File file = new File(classesDir + Params.AUTO_CLASS_PATH + Params.CLICK_VIEW_HELPER + ".class");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ClickFileUtils.copyCTAuto(file, buildDir);
    }
}
