package com.gre.maker.generator.file;

import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class FileGenerator {

    public static void doGenerate(Object dataModel) throws TemplateException, IOException {
        String projectPath = System.getProperty("user.dir");
        File parentFile = new File(projectPath).getParentFile();
        String inputPath = parentFile.getAbsolutePath() + File.separator + "code-generator-demo-projects" + File.separator + "acm-template";
        String outputPath = projectPath;
        StaticFileGenerator.copyFilesByHutool(inputPath, outputPath);


        String dynamicInputPath = projectPath + File.separator + "src/main/resources/template/MainTemplate.java.ftl";
        String dynamicOutputPath = projectPath + File.separator + "acm-template/src/com/gre/acm/MainTemplate.java";

        DynamicFileGenerator.doGenerate(dynamicInputPath, dynamicOutputPath, dataModel);
    }
}
