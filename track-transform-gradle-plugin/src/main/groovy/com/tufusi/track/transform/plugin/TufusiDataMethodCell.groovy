package com.tufusi.track.transform.plugin

/**
 * Created by LeoCheung on 2020/12/2.
 * @description 方法体
 */
class TufusiDataMethodCell {

    /**
     * 原方法名
     */
    String name

    /**
     * 原方法描述
     */
    String desc

    /**
     * 方法所在的接口或类
     */
    String parent

    /**
     * 采集数据的方法名
     */
    String agentName

    /**
     * 采集数据的方法描述
     */
    String agentDesc

    /**
     * 采集数据的方法参数起始索引（0: this, 1+: 普通参数）
     */
    int parameterStart

    /**
     * 采集数据的方法参数的个数
     */
    int parameterCount

    /**
     * 参数类型对应的ASM指令，加载不同类型的参数需要不同的指令
     */
    List<Integer> opcodes

    TufusiDataMethodCell(String name, String desc, String agentName) {
        this.name = name
        this.desc = desc
        this.agentName = agentName
    }

    TufusiDataMethodCell(String name, String desc, String parent, String agentName, String agentDesc,
                         int parameterStart, int parameterCount, List<Integer> opcodes) {
        this.name = name
        this.desc = desc
        this.parent = parent
        this.agentName = agentName
        this.agentDesc = agentDesc
        this.parameterStart = parameterStart
        this.parameterCount = parameterCount
        this.opcodes = opcodes
    }
}
