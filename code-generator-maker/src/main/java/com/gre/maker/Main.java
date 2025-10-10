package com.gre.maker;

import com.gre.maker.generator.main.GenerateTemplate;
import com.gre.maker.generator.main.MainGenerator;
import com.gre.maker.generator.main.ZipGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
//        MainGenerator mainGenerator = new MainGenerator();
        GenerateTemplate generateTemplate = new ZipGenerator();
        generateTemplate.doGenerate();
    }
}