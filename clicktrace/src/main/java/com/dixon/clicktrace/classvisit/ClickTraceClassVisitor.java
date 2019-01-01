package com.dixon.clicktrace.classvisit;

import com.dixon.clicktrace.config.Log;

import org.apache.http.util.TextUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Created by dixon.xu on 2018/12/18.
 */

public class ClickTraceClassVisitor extends ClassVisitor implements Opcodes {

    private MethodVisitor onCreateMethod; //onCreate
    private String superPath; //BaseActivity Parent
    private String basePath; //BaseActivity

    public ClickTraceClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.superPath = superName;
        this.basePath = name;
        if (cv != null) {
            cv.visit(version, access, name, signature, superName, interfaces);
        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ("onCreate".equals(name) && access == Opcodes.ACC_PROTECTED) {
            //这里 cv是ClassWriter 它是ClassVisitor的实现类 它的visitMethod会生成一个MethodWriter对象 即这里返回的实际是MethodWriter对象
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            onCreateMethod = new ClickTraceMethodVisitor(mv, basePath);
            return onCreateMethod;
        }
        if (cv != null) {
            //返回原始方法
            return cv.visitMethod(access, name, desc, signature, exceptions);
        }
        return null;
    }

    @Override
    public void visitEnd() {
        if (onCreateMethod == null && !TextUtils.isEmpty(superPath)) {
            //如果没有则创建onCreate()方法 暂时屏蔽
//            createOnCreateIfNotFind();
            Log.e("please check BaseActivity onCreate()");
        }
        super.visitEnd();
    }

    private void createOnCreateIfNotFind() {
        MethodVisitor onCreate = cv.visitMethod(Opcodes.ACC_PROTECTED, "onCreate", "(Landroid.os.Bundle;)V", null, null);
        onCreate.visitCode();
        ClickTraceMethodVisitor.visitListener(onCreate, basePath);
        onCreate.visitVarInsn(Opcodes.ALOAD, 0);
        onCreate.visitVarInsn(Opcodes.ALOAD, 1);
        onCreate.visitMethodInsn(Opcodes.INVOKESPECIAL, superPath, "onCreate", "(Landroid.os.Bundle;)V");
        onCreate.visitInsn(Opcodes.RETURN);
        onCreate.visitMaxs(2, 2);
        onCreate.visitEnd();
    }
}
