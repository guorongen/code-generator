package com.gre.maker.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.gre.maker.generator.file.FileGenerator;
import com.gre.maker.model.DataModel;
import freemarker.template.TemplateException;
import lombok.Data;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "generate", mixinStandardHelpOptions = true)
@Data
public class GenerateCommand implements Callable {

    @CommandLine.Option(names = {"-l", "--loop"}, description = "是否循环", arity = "0..1", interactive = true, echo = true)
    private boolean loop;

    @CommandLine.Option(names = {"-a", "--author"}, description = "作者名称", arity = "0..1", interactive = true, echo = true)
    private String author;

    @CommandLine.Option(names = {"-o", "--outputText"}, description = "输出文本", arity = "0..1", interactive = true, echo = true)
    private String outputText;

    @Override
    public Integer call() throws TemplateException, IOException {
        DataModel dataModel = new DataModel();
        BeanUtil.copyProperties(this, dataModel);
        System.out.println(dataModel);
        FileGenerator.doGenerate(dataModel);
        return 0;
    }
}
