package com.dixon.clicktrace.classcreator;

import com.dixon.clicktrace.config.Params;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by xuzheng on 2018/12/31.
 * <p>
 * ClickViewHelper类的生成,具体可以参考build/clicktracke/auto目录
 */

public class ClickViewHelperCreator extends ClassCreator {

    public static final String className = "ClickViewHelper";
    private static final String packagePath = Params.AUTO_CLASS_PATH;

    public ClickViewHelperCreator(String classesPath) {
        super(className, packagePath, classesPath);
    }

    @Override
    protected void classCreate() {
        classCreate(Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER,
                null, "java/lang/Object", null);
    }

    @Override
    protected void constructCreate() {

    }

    @Override
    protected void fieldCreate() {

    }

    @Override
    protected void methodAdd() {
        addMethod(new GetViewPathMC());
        addMethod(new GetContentMC());
        addMethod(new GetListViewMC());
        addMethod(new GetListViewItemMC());
        addMethod(new GetPositionMC());
    }

    public class GetViewPathMC extends MethodCreator {

        public static final String methodName = "getViewPath";
        public static final String desc = "(Landroid/view/View;)Ljava/lang/String;";

        @Override
        protected void methodCreate() {
            MethodVisitor getViewPath = mClassWriter.visitMethod(
                    Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                    methodName, desc, null, null);
            getViewPath.visitCode();

            getViewPath.visitVarInsn(Opcodes.ALOAD, 0);
            getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "java/lang/Class", "getSimpleName", "()Ljava/lang/String;");
            //加入第一个临时参数 0是参数
            getViewPath.visitVarInsn(Opcodes.ASTORE, 1);
            //第一个判断
            getViewPath.visitVarInsn(Opcodes.ALOAD, 0);
            getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            Label ifNull = new Label();
            getViewPath.visitJumpInsn(Opcodes.IFNULL, ifNull);

            //第二个判断
            getViewPath.visitVarInsn(Opcodes.ALOAD, 0);
            getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            getViewPath.visitTypeInsn(Opcodes.INSTANCEOF, "android/view/View");
            Label ifType = new Label();
            getViewPath.visitJumpInsn(Opcodes.IFEQ, ifType);

            getViewPath.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            getViewPath.visitInsn(Opcodes.DUP);
            getViewPath.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    "java/lang/StringBuilder", "<init>", "()V");
            getViewPath.visitVarInsn(Opcodes.ALOAD, 0);
            getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            getViewPath.visitTypeInsn(Opcodes.CHECKCAST, "android/view/View");
            getViewPath.visitMethodInsn(Opcodes.INVOKESTATIC,
                    packagePath + className, methodName, desc);
            getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            getViewPath.visitLdcInsn("/");
            getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            //调用第一个临时参数
            getViewPath.visitVarInsn(Opcodes.ALOAD, 1);
            getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            getViewPath.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
            //先return 再if
            getViewPath.visitInsn(Opcodes.ARETURN);
            getViewPath.visitLabel(ifNull);
            getViewPath.visitLabel(ifType);
            getViewPath.visitVarInsn(Opcodes.ALOAD, 1);
            getViewPath.visitInsn(Opcodes.ARETURN);

            getViewPath.visitInsn(Opcodes.RETURN);
            getViewPath.visitMaxs(2, 2);
            getViewPath.visitEnd();
        }
    }

    public class GetContentMC extends MethodCreator {

        public static final String methodName = "getContent";
        public static final String desc = "(Landroid/view/View;)Ljava/lang/String;";

        @Override
        protected void methodCreate() {
            MethodVisitor getContent = mClassWriter.visitMethod(
                    Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                    methodName, desc, null, null);
            getContent.visitCode();

            //第一个判断
            getContent.visitVarInsn(Opcodes.ALOAD, 0);
            getContent.visitTypeInsn(Opcodes.INSTANCEOF, "android/widget/TextView");
            Label ifContentType = new Label();
            getContent.visitJumpInsn(Opcodes.IFEQ, ifContentType);

            //!参数先行
            getContent.visitVarInsn(Opcodes.ALOAD, 0);
            getContent.visitTypeInsn(Opcodes.CHECKCAST, "android/widget/TextView");
            getContent.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/widget/TextView", "getText", "()Ljava/lang/CharSequence;");
            getContent.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                    "java/lang/CharSequence", "toString", "()Ljava/lang/String;");

            //先return 再if
            getContent.visitInsn(Opcodes.ARETURN);
            getContent.visitLabel(ifContentType);

            getContent.visitLdcInsn("");
            getContent.visitInsn(Opcodes.ARETURN);
            getContent.visitMaxs(1, 1);
            getContent.visitEnd();
        }
    }

    public class GetListViewMC extends MethodCreator {

        public static final String methodName = "getListView";
        public static final String desc = "(Landroid/view/View;)Landroid/widget/ListView;";

        @Override
        protected void methodCreate() {
            //return view.getParent() != null && view.getParent() instanceof ListView?(ListView)view.getParent():(view.getParent() != null?(view.getParent() instanceof View?getListView((View)view.getParent()):null):null);
            MethodVisitor getListView = mClassWriter.visitMethod(
                    Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                    methodName, desc, null, null);
            getListView.visitCode();

            //第一个判断
            getListView.visitVarInsn(Opcodes.ALOAD, 0);
            getListView.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            Label glvIfNull1 = new Label();
            getListView.visitJumpInsn(Opcodes.IFNULL, glvIfNull1);
            getListView.visitVarInsn(Opcodes.ALOAD, 0);
            getListView.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            getListView.visitTypeInsn(Opcodes.INSTANCEOF, "android/widget/ListView");
            Label glvIfEQ1 = new Label();
            getListView.visitJumpInsn(Opcodes.IFEQ, glvIfEQ1);
            getListView.visitVarInsn(Opcodes.ALOAD, 0);
            getListView.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            getListView.visitTypeInsn(Opcodes.CHECKCAST, "android/widget/ListView");
            getListView.visitInsn(Opcodes.ARETURN);
            getListView.visitLabel(glvIfNull1);
            getListView.visitLabel(glvIfEQ1);

            //第二个判断
            getListView.visitVarInsn(Opcodes.ALOAD, 0);
            getListView.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            Label glvIfNull2 = new Label();
            getListView.visitJumpInsn(Opcodes.IFNULL, glvIfNull2);
            getListView.visitVarInsn(Opcodes.ALOAD, 0);
            getListView.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            getListView.visitTypeInsn(Opcodes.INSTANCEOF, "android/view/View");
            //第三个判断
            Label glvIfEQ2 = new Label();
            getListView.visitJumpInsn(Opcodes.IFEQ, glvIfEQ2);
            getListView.visitVarInsn(Opcodes.ALOAD, 0);
            getListView.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            getListView.visitTypeInsn(Opcodes.CHECKCAST, "android/view/View");
            getListView.visitMethodInsn(Opcodes.INVOKESTATIC,
                    packagePath + className, methodName, desc);
            getListView.visitInsn(Opcodes.ARETURN);
            getListView.visitLabel(glvIfEQ2);
            getListView.visitInsn(Opcodes.ACONST_NULL);
            getListView.visitInsn(Opcodes.ARETURN);
            getListView.visitLabel(glvIfNull2);
            getListView.visitInsn(Opcodes.ACONST_NULL);
            getListView.visitInsn(Opcodes.ARETURN);
            getListView.visitMaxs(1, 1);
            getListView.visitEnd();
        }
    }

    public class GetListViewItemMC extends MethodCreator {

        public static final String methodName = "getListViewItem";
        public static final String desc = "(Landroid/view/View;)Landroid/view/View;";

        @Override
        protected void methodCreate() {
            //return view.getParent() != null && view.getParent() instanceof ListView?view:(view.getParent() != null?(view.getParent() instanceof View?getListViewItem((View)view.getParent()):null):null);
            MethodVisitor getListViewItem = mClassWriter.visitMethod(
                    Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                    methodName, desc, null, null);
            getListViewItem.visitCode();

            //第一个判断
            getListViewItem.visitVarInsn(Opcodes.ALOAD, 0);
            getListViewItem.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            Label glvIfNull3 = new Label();
            getListViewItem.visitJumpInsn(Opcodes.IFNULL, glvIfNull3);
            getListViewItem.visitVarInsn(Opcodes.ALOAD, 0);
            getListViewItem.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            getListViewItem.visitTypeInsn(Opcodes.INSTANCEOF, "android/widget/ListView");
            Label glvIfEQ3 = new Label();
            getListViewItem.visitJumpInsn(Opcodes.IFEQ, glvIfEQ3);
            getListViewItem.visitVarInsn(Opcodes.ALOAD, 0);
            getListViewItem.visitInsn(Opcodes.ARETURN);
            getListViewItem.visitLabel(glvIfNull3);
            getListViewItem.visitLabel(glvIfEQ3);

            //第二个判断
            getListViewItem.visitVarInsn(Opcodes.ALOAD, 0);
            getListViewItem.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            Label glvIfNull4 = new Label();
            getListViewItem.visitJumpInsn(Opcodes.IFNULL, glvIfNull4);
            getListViewItem.visitVarInsn(Opcodes.ALOAD, 0);
            getListViewItem.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            getListViewItem.visitTypeInsn(Opcodes.INSTANCEOF, "android/view/View");
            //第三个判断
            Label glvIfEQ4 = new Label();
            getListViewItem.visitJumpInsn(Opcodes.IFEQ, glvIfEQ4);
            getListViewItem.visitVarInsn(Opcodes.ALOAD, 0);
            getListViewItem.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getParent", "()Landroid/view/ViewParent;");
            getListViewItem.visitTypeInsn(Opcodes.CHECKCAST, "android/view/View");
            getListViewItem.visitMethodInsn(Opcodes.INVOKESTATIC,
                    packagePath + className, methodName, desc);
            getListViewItem.visitInsn(Opcodes.ARETURN);
            getListViewItem.visitLabel(glvIfEQ4);
            getListViewItem.visitInsn(Opcodes.ACONST_NULL);
            getListViewItem.visitInsn(Opcodes.ARETURN);
            getListViewItem.visitLabel(glvIfNull4);
            getListViewItem.visitInsn(Opcodes.ACONST_NULL);
            getListViewItem.visitInsn(Opcodes.ARETURN);
            getListViewItem.visitMaxs(1, 1);
            getListViewItem.visitEnd();
        }
    }

    public class GetPositionMC extends MethodCreator {

        public static final String methodName = "getPosition";
        public static final String desc = "(Landroid/view/View;)I";

        @Override
        protected void methodCreate() {
            //return view.getParent() != null && view.getParent() instanceof ListView?view:(view.getParent() != null?(view.getParent() instanceof View?getListViewItem((View)view.getParent()):null):null);
            MethodVisitor getPosition = mClassWriter.visitMethod(
                    Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                    methodName, desc, null, null);
            getPosition.visitCode();

            //存入listView和item
            getPosition.visitVarInsn(Opcodes.ALOAD, 0);
            getPosition.visitMethodInsn(Opcodes.INVOKESTATIC,
                    packagePath + className, GetListViewMC.methodName, GetListViewMC.desc);
            getPosition.visitVarInsn(Opcodes.ASTORE, 1);
            getPosition.visitVarInsn(Opcodes.ALOAD, 0);
            getPosition.visitMethodInsn(Opcodes.INVOKESTATIC,
                    packagePath + className, GetListViewItemMC.methodName, GetListViewItemMC.desc);
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
            getPosition.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/widget/ListView", "getChildAt", "(I)Landroid/view/View;");
            getPosition.visitVarInsn(Opcodes.ASTORE, 3);

            //listView.getFirstVisiblePosition()
            getPosition.visitVarInsn(Opcodes.ALOAD, 1);
            getPosition.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/widget/ListView", "getFirstVisiblePosition", "()I");

            //childAt0.getMeasuredHeight()
            getPosition.visitVarInsn(Opcodes.ALOAD, 3);
            getPosition.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getMeasuredHeight", "()I");

            //*乘
            getPosition.visitInsn(Opcodes.IMUL);

            //Math.abs(childAt0.getTop())
            getPosition.visitVarInsn(Opcodes.ALOAD, 3);
            getPosition.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getTop", "()I");
            getPosition.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "java/lang/Math", "abs", "(I)I");

            //+加
            getPosition.visitInsn(Opcodes.IADD);

            //item.getTop()
            getPosition.visitVarInsn(Opcodes.ALOAD, 2);
            getPosition.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getTop", "()I");

            //+加
            getPosition.visitInsn(Opcodes.IADD);

            getPosition.visitVarInsn(Opcodes.ISTORE, 4);

            //childAt0.getMeasuredHeight();
            getPosition.visitVarInsn(Opcodes.ALOAD, 3);
            getPosition.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "android/view/View", "getMeasuredHeight", "()I");

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
        }
    }
}
