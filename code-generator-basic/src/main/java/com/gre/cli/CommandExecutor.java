package com.gre.cli;

import com.gre.cli.command.ConfigCommand;
import com.gre.cli.command.GenerateCommand;
import com.gre.cli.command.ListCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine;

@Command(name = "cg", mixinStandardHelpOptions = true)
public class CommandExecutor implements Runnable {

    private final CommandLine commandLine;

    {
        commandLine = new CommandLine(this)
                .addSubcommand(new GenerateCommand())
                .addSubcommand(new ConfigCommand())
                .addSubcommand(new ListCommand());
    }

    @Override
    public void run() {
        // 不输入子命令时，给出友好提示
        System.out.println("请输入具体命令，或输入 --help 查看命令提示");
    }

    /**
     * 执行命令
     *
     * @param args
     * @return
     */
    public Integer execute(String[] args) {
        return commandLine.execute(args);
    }
}
