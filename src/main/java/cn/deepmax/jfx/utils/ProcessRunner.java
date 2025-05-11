package cn.deepmax.jfx.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessRunner {

    public static void run(String... args) {
        if (args == null || args.length == 0) {
            System.out.println("No input");
            return;
        }
        // 构建命令和参数列表
        List<String> command = new ArrayList<>(Arrays.asList(args));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        // 将标准错误流合并到标准输出流
        processBuilder.redirectErrorStream(true);

        try {
            // 启动进程
            Process process = processBuilder.start();

            // 读取并打印程序输出
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            // 等待程序结束并获取退出码
            int exitCode = process.waitFor();
            System.out.println("\n程序退出码: " + exitCode);
            if (exitCode != 0)
                throw new RuntimeException("Gcc exit code " + exitCode + "Error Info:\n" + sb.toString());
        } catch (IOException e) {
            System.err.println("执行命令时发生IO异常: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.err.println("进程被中断: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
