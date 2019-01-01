package com.dixon.clicktrace;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.dixon.clicktrace.classvisit.*;
import com.dixon.clicktrace.config.Log;
import com.dixon.clicktrace.utils.ClickFileUtils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by dixon.xu on 2018/12/18.
 */

public class ClickTraceTransform extends Transform {

    private Project project;
    private List<String> basepathArray = new ArrayList<>();
    private String tracePath, traceMethod;

    public ClickTraceTransform(Project project) {
        this.project = project;
    }

    @Override
    public String getName() {
        return ClickTraceTransform.class.getSimpleName();
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);

        parseBasepath();
        parseTracepath();

        //这段代码用Groovy就俩行each的事...
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();

        Iterator<TransformInput> iterator = inputs.iterator();
        while (iterator.hasNext()) {
            TransformInput next = iterator.next();
            parseJarInputs(next.getJarInputs(), outputProvider);
            parseDirectoryInputs(next.getDirectoryInputs(), outputProvider);
        }
    }

    private void parseTracepath() {
        com.dixon.clicktrace.config.ClickTraceConfig clickConfig = (com.dixon.clicktrace.config.ClickTraceConfig) project.getExtensions().getByName("clickconfig");
        tracePath = clickConfig.getTracepath().replace(".", "/");
        traceMethod = clickConfig.getTracemethod();
    }

    private void parseBasepath() {

        //等价于Groovy:project['clickconfig'].basepath
        String basePath = ((com.dixon.clicktrace.config.ClickTraceConfig) project.getExtensions().getByName("clickconfig")).getBasepath();
        String[] basepaths = ((com.dixon.clicktrace.config.ClickTraceConfig) project.getExtensions().getByName("clickconfig")).getBasepaths();

        List<String> temp = new ArrayList<>();
        if (basePath != null) {
            temp.add(basePath);
        }
        if (basepaths != null) {
            Collections.addAll(temp, basepaths);
        }

        //转化格式
        for (String path : temp) {
            if (path != null) {
                path = path.replace(".", "/");
                if (!path.endsWith(".class")) {
                    path = path.concat(".class");
                }
            }
            basepathArray.add(path);
        }

        for (String path : basepathArray) {
            Log.o("BasePath", path);
        }
    }

    private void parseJarInputs(Collection<JarInput> jarInputs, TransformOutputProvider outputProvider) {

        Iterator<JarInput> iterator = jarInputs.iterator();
        while (iterator.hasNext()) {
            JarInput next = iterator.next();
            String jarName = next.getName();
            String md5Name = DigestUtils.md5Hex(next.getFile().getAbsolutePath());
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length() - 4);
            }

            File dest = outputProvider.getContentLocation(
                    jarName + md5Name,
                    next.getContentTypes(),
                    next.getScopes(),
                    Format.JAR);

            try {
                FileUtils.copyFile(next.getFile(), dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseDirectoryInputs(Collection<DirectoryInput> directoryInputs, TransformOutputProvider outputProvider) {

        Iterator<DirectoryInput> iterator = directoryInputs.iterator();
        //遍历每一个文件
        if (iterator.hasNext()) {
            DirectoryInput next = iterator.next();
            if (next.getFile().isDirectory()) {
                //遍历class之前生成需要的类
                if (next.getFile().getAbsolutePath().endsWith("classes")) {
                    ClickClassCreator classCreator = new ClickClassCreator(
                            this.project.getBuildDir().getAbsolutePath(),
                            next.getFile().getAbsolutePath(),
                            tracePath,
                            traceMethod);
                    classCreator.create();
                }
                parseClassDir(next.getFile());
            }

            File dest = outputProvider.getContentLocation(
                    next.getName(),
                    next.getContentTypes(),
                    next.getScopes(),
                    Format.DIRECTORY);

            try {
                //注意是copyDirectory不是File
                FileUtils.copyDirectory(next.getFile(), dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseClassDir(File file) {

        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                parseClassDir(f);
            } else {
                parseClass(f);
            }
        }
    }

    //解析每一个文件
    private void parseClass(File file) {

        for (String basePath : basepathArray) {

            if (file.getAbsolutePath().endsWith(basePath)) {
                //获取click事件处理类路径

                String name = file.getName();
                ClassReader classReader = new ClassReader(ClickFileUtils.getBytes(file.getAbsolutePath()));
                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
                ClassVisitor classVisitor = new ClickTraceClassVisitor(classWriter);
                classReader.accept(classVisitor, Opcodes.ASM5);

                byte[] code = classWriter.toByteArray();
                String filepath = file.getParentFile().getAbsolutePath() + File.separator + name;
                try {
                    FileOutputStream fos = new FileOutputStream(filepath);
                    fos.write(code);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ClickFileUtils.copyCTGen(new File(filepath), this.project.getBuildDir().getAbsolutePath());
                //已找到目标文件 此次循环可以退出
                break;
            }
        }
    }
}
