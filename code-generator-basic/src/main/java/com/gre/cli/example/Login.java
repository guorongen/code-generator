package com.gre.cli.example;

import picocli.CommandLine;

import java.util.concurrent.Callable;

public class Login implements Callable<Integer> {
    @CommandLine.Option(names = { "-u", "--user" }, description = "user name")
    String user;

    @CommandLine.Option(names = { "-p", "--password" }, arity = "0..1", description = "Passphrase", interactive = true, prompt = "请输入密码：")
    String password;

    @CommandLine.Option(names = { "-cp", "--checkPassword" }, description = "check password", interactive = true)
    String checkPassword;

    @Override
    public Integer call() throws Exception {
        System.out.println("password: " + password);
        System.out.println("checkPassword: " + checkPassword);
        return 0;
    }

    public static void main(String[] args) {
        new CommandLine(new Login()).execute("-u", "user123", "-p", "-cp");
    }
}
