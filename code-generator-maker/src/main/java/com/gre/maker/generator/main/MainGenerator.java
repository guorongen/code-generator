package com.gre.maker.generator.main;

public class MainGenerator extends GenerateTemplate {
    @Override
    protected String buildDist(String outputPath, String sourceCopyDestPath, String jarPath, String shellOutputFilePath) {
        System.out.println("不生成精简版");
        return "";
    }
}
