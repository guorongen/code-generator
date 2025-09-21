package com.gre.maker.template;

import com.gre.maker.meta.Meta;
import com.gre.maker.template.enums.FileFilterRangeEnum;
import com.gre.maker.template.enums.FileFilterRuleEnum;
import com.gre.maker.template.model.FileFilterConfig;
import com.gre.maker.template.model.TemplateMakerFileConfig;
import com.gre.maker.template.model.TemplateMakerModelConfig;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TemplateMakerTest {

    @Test
    public void testMakeTemplateBug1() {
        // 1、项目的基本信息
        Meta meta = new Meta();
        meta.setName("acm-template-generator");
        meta.setDescription("ACM 示例模板生成器");

        // 指定原始项目路径
        String projectPath = System.getProperty("user.dir");
        String originProjectPath = new File(projectPath).getParentFile().getAbsolutePath() + File.separator + "code-generator-demo-projects/springboot-init-master";
        String fileInputPath1 = "src/main/java/com/yupi/springbootinit/common";

        // 文件过滤配置
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(fileInputPath1);

        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = Arrays.asList(fileInfoConfig1);

        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        templateMakerFileConfig.setFiles(fileInfoConfigList);

        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();
        // 模型配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("url");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");
        modelInfoConfig1.setReplaceText("jdbc:mysql://localhost:3306/my_db");

        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList = Arrays.asList(modelInfoConfig1);
        templateMakerModelConfig.setModels(modelInfoConfigList);

        long id = TemplateMaker.makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, 1L);
        System.out.println(id);
    }

    @Test
    public void testMakeTemplateBug2() {
        // 1、项目的基本信息
        Meta meta = new Meta();
        meta.setName("acm-template-generator");
        meta.setDescription("ACM 示例模板生成器");

        // 指定原始项目路径
        String projectPath = System.getProperty("user.dir");
        String originProjectPath = new File(projectPath).getParentFile().getAbsolutePath() + File.separator + "code-generator-demo-projects/springboot-init-master";
        String fileInputPath1 = "src/main/java/com/yupi/springbootinit/common";

        // 文件过滤配置
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(fileInputPath1);

        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = Arrays.asList(fileInfoConfig1);

        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        templateMakerFileConfig.setFiles(fileInfoConfigList);

        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();
        // 模型配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("className");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setReplaceText("BaseResponse");

        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList = Arrays.asList(modelInfoConfig1);
        templateMakerModelConfig.setModels(modelInfoConfigList);

        long id = TemplateMaker.makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, 1L);
        System.out.println(id);
    }
}