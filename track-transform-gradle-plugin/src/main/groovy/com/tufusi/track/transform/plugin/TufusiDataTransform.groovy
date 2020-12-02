package com.tufusi.track.transform.plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import groovy.io.FileType

class TufusiDataTransform extends Transform {

    private Project project
    private TufusiDataExtension extension

    TufusiDataTransform(Project project, TufusiDataExtension extension) {
        this.extension = extension
        this.project = project
    }

    @Override
    String getName() {
        return "tufusiTrackData"
    }

    /**
     * 需要处理的数据类型  有两种枚举类型
     * CLASSES 代表处理的 java 的 class 文件，RESOURCES 代表要处理 java 的资源
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指定 Transform 要操作的内容的范围，官方文档 Scope 有7种类型
     * 1. EXTERNAL_LIBRARIES        只有外部库
     * 2. PROJECT                   只有项目内容
     * 3. PROJECT_LOCAL_DEPS        只有项目的本地依赖(本地jar)
     * 4. PROVIDED_ONLY             只提供本地或远程依赖项
     * 5. SUB_PROJECTS              只有子项目。
     * 6. SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
     * 7. TESTED_CODE               由当前变量(包括依赖项)测试的代码
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * 打印提示信息
     */
    static void printCopyRight() {
        println()
        println("####################################################################")
        println("########                                                    ########")
        println("########                                                    ########")
        println("########          欢迎使用 TufusiASMTrack® 编译插件          ########")
        println("########                                                    ########")
        println("########                                                    ########")
        println("####################################################################")
        println()
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        _transform(transformInvocation.context, transformInvocation.inputs, transformInvocation.outputProvider, transformInvocation.incremental);
    }

    void _transform(Context context, Collection<TransformInput> inputs, TransformOutputProvider outputProvider, boolean isIncremental)
            throws TransformException, InterruptedException, IOException {
        // 如果不是自动增量构建，则删除所有输出目录
        if (!incremental) {
            outputProvider.deleteAll();
        }
        printCopyRight()
        /**
         * Transform 的 inputs 有两种类型，一种是目录，一种是 jar 包，需要分开遍历
         */
        inputs.each { TransformInput input ->
            /* 遍历目录 */
            input.directoryInputs.each { DirectoryInput directoryInput ->
                /** 当前这个Transform输出目录 **/
                File dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                File dir = directoryInput.file

                // 如果输出目录存在
                if (dir) {
                    HashMap<String, File> modifyMap = new HashMap<>()
                    // 遍历以某一扩展名结尾的文件
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                        File classFile ->
                            if (TufusiDataClassModifier.isShouldModify(classFile.name)) {
                                File modifiedFile = null
                                if (extension.enableAppClickTrack) {
                                    println("TufusiDataClassModifier.modifyClassFile")
                                    modifiedFile = TufusiDataClassModifier.modifyClassFile(dir, classFile, context.getTemporaryDir())
                                }
                                if (modifiedFile != null) {
                                    /**key 为包名 + 类名，如：/com/tufusi/autotrack/MainActivity.class*/
                                    String key = classFile.absolutePath.replace(dir.absolutePath, "")
                                    modifyMap.put(key, modified)
                                }
                            }
                    }
                    FileUtils.copyDirectory(directoryInput.file, dest)
                    modifyMap.entrySet().each {
                        Map.Entry<String, File> entry ->
                            File targetFile = new File(dest.absolutePath + entry.getKey())
                            if (targetFile.exists()) {
                                targetFile.delete()
                            }
                            FileUtils.copyFile(entry.getValue(), targetFile)
                            entry.getValue().delete()
                    }
                }
            }

            /* 遍历jar包 */
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.file.name

                /** 截取文件路径的 md5 值重命名输出文件，这里面涉及到重名，可能会覆盖 **/
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
                /** 获取 jar包名字 **/
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }

                /** 获得输出文件*/
                File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                def modifiedJar = null
                if (extension.enableAppClickTrack) {
                    modifiedJar = TufusiDataClassModifier.modifyJar(jarInput.file, context.getTemporaryDir(), true)
                }
                if (modifiedJar == null) {
                    modifiedJar = jarInput.file
                }
                FileUtils.copyFile(modifiedJar, dest)
            }
        }
    }
}