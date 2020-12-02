package com.tufusi.track.transform.plugin

import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Matcher

class TufusiDataClassModifier {

    /**
     * 排除 不作修改的 .class编译文件
     */
    private static HashSet<String> exclude = new HashSet<>()

    static {
        exclude = new HashSet<>()
        exclude.add('android.support')
        exclude.add('androidx.')
        exclude.add('com.tufusi.track.sdk')
    }

    /**
     * 是否需要编辑
     */
    protected static boolean isShouldModify(String className) {
        Iterable<String> iterator = exclude.iterator()
        while (iterator.hasNext()) {
            String packageName = iterator.next()
            if (className.startsWith(packageName)) {
                return false
            }
        }

        if (className.contains('R$') ||
                className.contains('R2$') ||
                className.contains('R.class') ||
                className.contains('R2.class') ||
                className.contains('BuildConfig.class')) {
            return false
        }

        return true
    }

    static File modifyClassFile(File dir, File classFile, File tempDir) {
        File modifiedFile = null
        try {
            String className = path2ClassName(classFile.absolutePath.replace(dir.absolutePath + File.separator, ""))
            byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
            byte[] modifiedClassBytes = modifyClass(sourceClassBytes)
            if (modifiedClassBytes) {
                modifiedFile = new File(tempDir, className.replace('.', '') + '.class')
                if (modifiedFile.exists()) {
                    modifiedFile.delete()
                }
                modifiedFile.createNewFile()
                new FileOutputStream(modifiedFile).write(modifiedClassBytes)
            }
        } catch (Exception ex) {
            println("modifyClassFile出错")
            ex.printStackTrace()
        }
        return modifiedFile;
    }

    static String path2ClassName(String pathName) {
        pathName.replace(File.separator, ".").replace(".class", "")
    }

    /**
     * 修改 Jar File
     * @param jarFile jar 文件对象
     * @param tempDir 临时路径
     * @param nameHex 64位开启
     * @return
     */
    static File modifyJar(File jarFile, File tempDir, boolean nameHex) {
        /**
         * 读取原 jar
         */
        def file = new JarFile(jarFile, false)

        /**
         * 设置输出到的 jar
         */
        def hexName = ""
        if (nameHex) {
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream
            try {
                inputStream = file.getInputStream(jarEntry)
            } catch (Exception e) {
                return null
            }
            String entryName = jarEntry.getName()
            if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                //ignore
            } else {
                String className
                JarEntry jarEntry2 = new JarEntry(entryName)
                jarOutputStream.putNextEntry(jarEntry2)

                byte[] modifiedClassBytes = null
                byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)
                if (entryName.endsWith(".class")) {
                    className = entryName.replace(Matcher.quoteReplacement(File.separator), ".").replace(".class", "")
                    if (isShouldModify(className)) {
                        modifiedClassBytes = modifyClass(sourceClassBytes)
                    }
                }
                if (modifiedClassBytes == null) {
                    modifiedClassBytes = sourceClassBytes
                }
                jarOutputStream.write(modifiedClassBytes)
                jarOutputStream.closeEntry()
            }
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    private static byte[] modifyClass(byte[] srcClass) throws IOException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        ClassVisitor classVisitor = new TufusiDataClassVisitor(classWriter)
        ClassReader cr = new ClassReader(srcClass)
        cr.accept(classVisitor, ClassReader.SKIP_FRAMES)
        return classWriter.toByteArray()
    }
}