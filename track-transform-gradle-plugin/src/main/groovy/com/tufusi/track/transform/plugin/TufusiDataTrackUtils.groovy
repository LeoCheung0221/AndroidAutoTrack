package com.tufusi.track.transform.plugin

import org.objectweb.asm.Opcodes

/**
 * Created by LeoCheung on 2020/12/2.
 * @description 实现操作码接口
 */
class TufusiDataTrackUtils implements Opcodes {

    /**
     * 是否是合成操作符
     * @param access 安全访问标记
     * @return
     */
    static boolean isSynthetic(int access) {
        return (access & ACC_SYNTHETIC) != 0
    }

    static boolean isPublic(int access) {
        return (access & ACC_PUBLIC) != 0
    }

    static boolean isPrivate(int access) {
        return (access & ACC_PRIVATE) != 0
    }

    static boolean isStatic(int access) {
        return (access & ACC_STATIC) != 0
    }
}
