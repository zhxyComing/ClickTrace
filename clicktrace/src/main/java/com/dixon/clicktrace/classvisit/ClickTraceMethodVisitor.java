package com.dixon.clicktrace.classvisit;

import com.dixon.clicktrace.classcreator.ClickSetCreator;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by dixon.xu on 2018/12/18.
 */

public class ClickTraceMethodVisitor extends MethodVisitor {

    private String basePath; //如com/app/dixon/testclicktrace/BaseActivity

    public ClickTraceMethodVisitor(MethodVisitor mv, String basePath) {
        super(Opcodes.ASM5, mv);
        this.basePath = basePath;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitCode() {
        //此方法在访问方法的头部时被访问到，仅被访问一次
        //此处可插入新的指令
        visitListener(mv, basePath);
        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        //此方法可以获取方法中每一条指令的操作类型，被访问多次
        //如应在方法结尾处添加新指令，则应判断：
        //if (opcode == Opcodes.RETURN) {
        super.visitInsn(opcode);
    }

    public static void visitListener(MethodVisitor mv, String basePath) {

        //原则 先写参数 再写方法

        //this.getWindow().getDecorView().getRootView().getViewTreeObserver().addOnGlobalLayoutListener(new ClickSetListener(this));

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                basePath,
                "getWindow",
                "()Landroid/view/Window;");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "android/view/Window",
                "getDecorView",
                "()Landroid/view/View;");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "android/view/View",
                "getRootView",
                "()Landroid/view/View;");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "android/view/View",
                "getViewTreeObserver",
                "()Landroid/view/ViewTreeObserver;");
        //创建clickSetListener对象
        mv.visitTypeInsn(Opcodes.NEW, ClickSetCreator.packagePath + ClickSetCreator.className); //?
        mv.visitInsn(Opcodes.DUP);
        //创建this
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        //至此完成new ClickDelegateSetListener(this)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                ClickSetCreator.packagePath + ClickSetCreator.className,//?
                "<init>",
                "(Landroid/app/Activity;)V");
        //addOnGlobalLayoutListener(new ClickDelegateSetListener(this));
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "android/view/ViewTreeObserver",
                "addOnGlobalLayoutListener",
                "(Landroid/view/ViewTreeObserver$OnGlobalLayoutListener;)V");
    }
}
