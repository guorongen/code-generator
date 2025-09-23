package com.gre.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.gre.maker.meta.Meta;
import com.gre.maker.meta.enums.FileGenerateTypeEnum;
import com.gre.maker.meta.enums.FileTypeEnum;
import com.gre.maker.template.enums.FileFilterRangeEnum;
import com.gre.maker.template.enums.FileFilterRuleEnum;
import com.gre.maker.template.model.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模板制作工具
 */
public class TemplateMaker {

    /**
     * 制作模板
     *
     * @param templateMakerConfig
     * @return
     */
    public static long makeTemplate(TemplateMakerConfig templateMakerConfig) {
        Long id = templateMakerConfig.getId();
        Meta meta = templateMakerConfig.getMeta();
        String originProjectPath = templateMakerConfig.getOriginProjectPath();
        TemplateMakerFileConfig templateMakerFileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig templateMakerModelConfig = templateMakerConfig.getModelConfig();
        TemplateMakerOutputConfig outputConfig = templateMakerConfig.getOutputConfig();

        return makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, outputConfig, id);
    }

    /**
     * 制作模板
     *
     * @param newMeta
     * @param originProjectPath
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param templateMakerOutputConfig
     * @param id
     * @return
     */
    public static long makeTemplate(Meta newMeta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, TemplateMakerOutputConfig templateMakerOutputConfig, Long id) {
        // 没有id 则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }

        // 复制目录
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + id;
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath, templatePath, true);
        }

        // 一、输入信息
        // 处理模型信息
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = getModelInfoList(templateMakerModelConfig);


        // 2、输入文件信息
        // 要挖坑的项目根目录
        File tempFile = new File(templatePath);
        templatePath = tempFile.getAbsolutePath();
        String sourceRootPath = FileUtil.loopFiles(new File(templatePath), 1, null)
                .stream()
                .filter(File::isDirectory)
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getAbsolutePath();

        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");
        // 制作文件模板
        List<Meta.FileConfig.FileInfo> newFileInfoList = makeFileTemplate(templateMakerFileConfig, templateMakerModelConfig, sourceRootPath);

        // 三、生成配置文件
        String metaOutputPath = templatePath + File.separator + "meta.json";

        // 已有meta文件，不是第一次制作，则在meta基础上进行修改
        if (FileUtil.exist(metaOutputPath)) {
            newMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            // 1、追加配置
            List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
            fileInfoList.addAll(newFileInfoList);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = newMeta.getModelConfig().getModels();
            modelInfoList.addAll(newModelInfoList);

            // 配置去重
            newMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));
            newMeta.getModelConfig().setModels(distinctModels(modelInfoList));

        } else {
            // 1、构造配置参数对象

            Meta.FileConfig fileConfig = new Meta.FileConfig();
            newMeta.setFileConfig(fileConfig);
            fileConfig.setSourceRootPath(sourceRootPath);
            List<Meta.FileConfig.FileInfo> fileInfoList = new ArrayList<>();
            fileConfig.setFiles(fileInfoList);
            fileInfoList.addAll(newFileInfoList);

            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            newMeta.setModelConfig(modelConfig);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = new ArrayList<>();
            modelConfig.setModels(modelInfoList);
            modelInfoList.addAll(newModelInfoList);
        }

        // 额外的输出配置
        if (templateMakerOutputConfig != null) {
            // 文件外层和分组去重
            if (templateMakerOutputConfig.isRemoveGroupFilesFromRoot()) {
                List<Meta.FileConfig.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
                newMeta.getFileConfig().setFiles(TemplateMakerUtils.removeGroupFilesFromRoot(fileInfoList));
            }
        }

        // 2、输出元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);
        return id;
    }

    /**
     * 获取模型配置
     *
     * @param templateMakerModelConfig
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> getModelInfoList(TemplateMakerModelConfig templateMakerModelConfig) {
        // 本次新增的模型列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();
        // 非空校验
        if (templateMakerModelConfig == null) {
            return newModelInfoList;
        }

        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();
        if (CollUtil.isEmpty(models)) {
            return newModelInfoList;
        }

        // 处理模型信息
        // 转换为配置文件接受的modelInfo对象
        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream()
                .map(modelInfoConfig -> {
                    Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
                    BeanUtil.copyProperties(modelInfoConfig, modelInfo);
                    return modelInfo;
                }).collect(Collectors.toList());

        // 如果是模型组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelGroupConfig, groupModelInfo);

            // 模型全部放到一个分组内
            groupModelInfo.setModels(inputModelInfoList);
            newModelInfoList.add(groupModelInfo);
        } else {
            // 不分组，添加所有的模型信息到列表
            newModelInfoList.addAll(inputModelInfoList);
        }
        return newModelInfoList;
    }

    /**
     * 生成多个文件
     *
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param sourceRootPath
     * @return
     */
    private static List<Meta.FileConfig.FileInfo> makeFileTemplate(TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath) {
        // 遍历输入文件
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();

        // 非空校验
        if (templateMakerFileConfig == null) {
            return newFileInfoList;
        }
        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = templateMakerFileConfig.getFiles();
        if (CollUtil.isEmpty(fileInfoConfigList)) {
            return newFileInfoList;
        }

        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList) {
            String inputFilePath = fileInfoConfig.getPath();
            String inputFileAbsolutePath = sourceRootPath + "/" + inputFilePath;

            // 传入绝对路径
            // 得到过滤后的文件列表
            List<File> fileList = FileFilter.doFilter(inputFileAbsolutePath, fileInfoConfig.getFilterConfigList());

            // 不处理已经生成的 FTL 模板文件
            fileList = fileList.stream()
                    .filter(file -> !file.getAbsolutePath().endsWith(".ftl"))
                    .collect(Collectors.toList());
            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig, sourceRootPath, file, fileInfoConfig);
                newFileInfoList.add(fileInfo);
            }
        }

        // 如果是文件组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if (fileGroupConfig != null) {
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();
            String type = fileGroupConfig.getType();

            Meta.FileConfig.FileInfo groupFileInfo = new Meta.FileConfig.FileInfo();
            groupFileInfo.setCondition(condition);
            groupFileInfo.setGroupKey(groupKey);
            groupFileInfo.setGroupName(groupName);
            groupFileInfo.setType(type);
            // 文件全部放到一个分组内
            groupFileInfo.setFiles(newFileInfoList);

            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(groupFileInfo);
        }
        return newFileInfoList;
    }

    /**
     * 制作模板文件
     *
     * @param templateMakerModelConfig
     * @param sourceRootPath
     * @param inputFile
     * @param fileInfoConfig
     * @return
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath, File inputFile, TemplateMakerFileConfig.FileInfoConfig fileInfoConfig) {
        String fileInputAbsolutePath = inputFile.getAbsolutePath();
        fileInputAbsolutePath = fileInputAbsolutePath.replaceAll("\\\\", "/");
        // 要挖坑的文件（一定要是相对路径）
        String fileInputPath = fileInputAbsolutePath.replace(sourceRootPath + "/", "");
        String fileOutputPath = fileInputPath + ".ftl";

        // 二、使用字符串替换，生成模板文件
        String fileOutputAbsolutePath = fileInputAbsolutePath + ".ftl";

        String fileContent;
        // 如果已有模板文件，表示不是第一次制作，则在原有模板的基础上再挖坑
        boolean hasTemplateFile = FileUtil.exist(fileOutputAbsolutePath);
        if (hasTemplateFile) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        }

        // 支持多个模型：对于同一个文件的内容，遍历模型进行多轮替换
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();

        String newFileContent = fileContent;
        String replacement;
        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : templateMakerModelConfig.getModels()) {
            String fieldName = modelInfoConfig.getFieldName();
            // 不是分组
            if (modelGroupConfig == null) {
                replacement = String.format("${%s}", fieldName);
            } else {
                String groupKey = modelGroupConfig.getGroupKey();
                replacement = String.format("${%s.%s}", groupKey, fieldName);
            }
            newFileContent = StrUtil.replace(newFileContent, modelInfoConfig.getReplaceText(), replacement);
        }

        // 文件配置信息
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileOutputPath);
        fileInfo.setOutputPath(fileInputPath);
        fileInfo.setCondition(fileInfoConfig.getCondition());
        fileInfo.setType(FileTypeEnum.FILE.getValue());
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        // 是否更改了文件内容
        boolean contentEquals = newFileContent.equals(fileContent);
        // 之前不存在模板文件，并且这次替换没有修改文件的内容，才是静态生成
        // 和原文件内容一致，表示不用生成模板，静态生成
        if (!hasTemplateFile) {
            if (contentEquals) {
                // 输出路径 = 输入路径
                fileInfo.setInputPath(fileInputPath);
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            } else {
                FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
            }
        } else if (!contentEquals) {
            // 有模板文件，并且增加了新坑，生成/更新模板文件
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
        }

        return fileInfo;
    }

    /**
     * 文件去重
     *
     * @param fileInfoList
     * @return
     */
    private static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList) {

        // 1、将所有文件配置（fileInfo）分为有分组的和无分组的
        // 先处理有分组的文件 以组为单位划分
        // {"groupKey": "a", files: [1, 2]}, {"groupKey": "a", files: [2, 3]}, {"groupKey": "b", files: [4, 5]}
        // {"groupKey": "a", files: [[1, 2], [2, 3]]}, {"groupKey": "b", files: [[4, 5]]}
        Map<String, List<Meta.FileConfig.FileInfo>> groupKeyFileInfoListMap = fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.FileConfig.FileInfo::getGroupKey)
                );
        // 2、对于有分组的文件配置，如果有相同的分组，同分组内的文件进行合并（merge），不同分组可同时保留
        // 同组内配置合并
        // {"groupKey": "a", files: [1, 2], [2, 3]]}
        // {"groupKey": "a", files: [1, 2, 2, 3]}
        // {"groupKey": "a", files: [1, 2, 3]}
        Map<String, Meta.FileConfig.FileInfo> groupKeyMergedFileInfoMap = new HashMap<>();

        for (Map.Entry<String, List<Meta.FileConfig.FileInfo>> entry : groupKeyFileInfoListMap.entrySet()) {
            List<Meta.FileConfig.FileInfo> tempFileInfoList = entry.getValue();
            // [1, 2, 2, 3]
            List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>(tempFileInfoList.stream()
                    .flatMap(fileInfo -> fileInfo.getFiles().stream())
                    .collect(
                            Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                    ).values());

            // 使用新的group配置
            Meta.FileConfig.FileInfo newFileInfo = CollUtil.getLast(tempFileInfoList);
            newFileInfo.setFiles(newFileInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedFileInfoMap.put(groupKey, newFileInfo);
        }

        // 3、创建新的文件配置列表（结果列表），先将合并后的分组添加到结果列表
        ArrayList<Meta.FileConfig.FileInfo> resultList = new ArrayList<>(groupKeyMergedFileInfoMap.values());

        // 4、再将无分组的文件配置列表添加到结果列表
        resultList.addAll(new ArrayList<>(fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (e, r) -> r)
                ).values()));
        return resultList;
    }

    /**
     * 模型去重
     *
     * @param modelInfoList
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
        // 1、将所有模型配置（modelInfo）分为有分组的和无分组的
        // 先处理有分组的模型 以组为单位划分
        // {"groupKey": "a", models: [1, 2]}, {"groupKey": "a", models: [2, 3]}, {"groupKey": "b", models: [4, 5]}
        // {"groupKey": "a", models: [[1, 2], [2, 3]]}, {"groupKey": "b", models: [[4, 5]]}
        Map<String, List<Meta.ModelConfig.ModelInfo>> groupKeymodelInfoListMap = modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isNotBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.ModelConfig.ModelInfo::getGroupKey)
                );
        // 2、对于有分组的模型配置，如果有相同的分组，同分组内的模型进行合并（merge），不同分组可同时保留
        // 同组内配置合并
        // {"groupKey": "a", models: [1, 2], [2, 3]]}
        // {"groupKey": "a", models: [1, 2, 2, 3]}
        // {"groupKey": "a", models: [1, 2, 3]}
        Map<String, Meta.ModelConfig.ModelInfo> groupKeyMergedmodelInfoMap = new HashMap<>();

        for (Map.Entry<String, List<Meta.ModelConfig.ModelInfo>> entry : groupKeymodelInfoListMap.entrySet()) {
            List<Meta.ModelConfig.ModelInfo> tempmodelInfoList = entry.getValue();
            // [1, 2, 2, 3]
            List<Meta.ModelConfig.ModelInfo> newmodelInfoList = new ArrayList<>(tempmodelInfoList.stream()
                    .flatMap(modelInfo -> modelInfo.getModels().stream())
                    .collect(
                            Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                    ).values());

            // 使用新的group配置
            Meta.ModelConfig.ModelInfo newmodelInfo = CollUtil.getLast(tempmodelInfoList);
            newmodelInfo.setModels(newmodelInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedmodelInfoMap.put(groupKey, newmodelInfo);
        }

        // 3、创建新的模型配置列表（结果列表），先将合并后的分组添加到结果列表
        ArrayList<Meta.ModelConfig.ModelInfo> resultList = new ArrayList<>(groupKeyMergedmodelInfoMap.values());

        // 4、再将无分组的模型配置列表添加到结果列表
        resultList.addAll(new ArrayList<>(modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (e, r) -> r)
                ).values()));
        return resultList;
    }
}
