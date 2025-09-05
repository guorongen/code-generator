package com.gre.generator;

import com.gre.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator {
    public static void main(String[] args) throws TemplateException, IOException {
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("gre");
        mainTemplateConfig.setLoop(false);
        mainTemplateConfig.setOutputText("求和结果：");

        doGenerate(mainTemplateConfig);
    }

    public static void doGenerate(MainTemplateConfig mainTemplateConfig) throws TemplateException, IOException {
        System.out.println(mainTemplateConfig);
        String projectPath = System.getProperty("user.dir");
        File parentFile = new File(projectPath).getParentFile();
        String inputPath = parentFile.getAbsolutePath() + File.separator + "code-generator-demo-projects" + File.separator + "acm-template";
        String outputPath = projectPath;
        StaticGenerator.copyFilesByRecursive(inputPath, outputPath);


        String dynamicInputPath = projectPath + File.separator + "src/main/resources/template/MainTemplate.java.ftl";
        String dynamicOutputPath = projectPath + File.separator + "acm-template/src/com/gre/acm/MainTemplate.java";

        DynamicGenerator.doGenerate(dynamicInputPath, dynamicOutputPath, mainTemplateConfig);
    }
}
