package com.gre.maker.model;

import lombok.Data;

/**
 * 静态模板配置
 */
@Data
public class DataModel {

    /**
     * 作者注释
     */
    private String author;

    /**
     * 输出信息
     */
    private String outputText;

    /**
     * 是否循环
     */
    private boolean loop;
}
