package com.gre.web.job;

import cn.hutool.core.util.StrUtil;
import com.gre.web.manager.CosManager;
import com.gre.web.mapper.GeneratorMapper;
import com.gre.web.model.entity.Generator;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ClearCosJobHandler {

    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratorMapper generatorMapper;

    /**
     * 每天执行
     *
     * @throws Exception
     */
    @XxlJob("clearCosJobHandler")
    public void clearCosJobHandler() throws Exception {
        log.info("clearCosJobHandler start");

        // 1、删除用户上传的模板制作文件
        cosManager.deleteDir("/generator_make_template/");

        // 2、已删除的代码生成器对应的产物包文件
        List<Generator> generatorList = generatorMapper.listDeletedGenerator();
        List<String> keyList = generatorList.stream().map(Generator::getDistPath)
                .filter(StrUtil::isNotBlank)
                .map(disPath -> disPath.substring(1))
                .collect(Collectors.toList());
        cosManager.deleteObjects(keyList);

        log.info("clearCosJobHandler end");
    }
}
