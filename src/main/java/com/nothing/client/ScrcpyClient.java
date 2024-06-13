package com.nothing.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ScrcpyClient {
    private static String resourcePath = System.getProperty("user.dir") + "/src/main/resources/";

    private static final String SERVER_JAR = "/data/local/tmp/scrcpy-server.jar";
    private static final String SERVER_CLASS = "app_process / com.genymobile.scrcpy.Server";
    private static final String SERVER_VERSION = "2.4";
    public static final int SERVER_PORT = 27183;
    private static int MAX_SIZE = 1920;
    private static String LOG_LEVEL = "debug";
    private static Thread scrcpyServerThread = null;

    private static boolean serverStarted = false;
    private static Object waitObj = new Object();

    public static boolean startServer() {
        // 关闭scrcpy服务,防止端口占用
        try {
            stopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //开启线程启动scrcpy服务
        scrcpyServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                startServer(LOG_LEVEL, MAX_SIZE);
            }
        });
        scrcpyServerThread.start();
        synchronized (waitObj) {
            try {
                waitObj.wait();
                Thread.sleep(2 * 1000);
                return serverStarted;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void startServer(String logLevel, int maxSize){
        try {
            ExeResult ret;
            ret = execCmd("adb", "push", resourcePath + "scrcpy-server", SERVER_JAR);
            if (ret.getRet() != 0) {
                throw new RuntimeException("push scrcpy-server failed " + ret.getOut());
            }
            ret = execCmd("adb", "forward", "tcp:" + SERVER_PORT, "localabstract:scrcpy");
            if (ret.getRet() != 0) {
                System.out.println("forward port failed " + ret.getOut());
                throw new RuntimeException("forward port failed " + ret.getOut());
            }
            //服务端启动参数
            Map<String, String> options = Map.of(
                    "tunnel_forward", "true",
                    "log_level", logLevel,
                    "video", "true",
                    "audio", "false",
                    "control", "false",
                    "max_size", String.valueOf(maxSize));
            String optionsStr = String.join(" ", options.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).toArray(String[]::new));
            String command = String.format("adb shell CLASSPATH=%s %s %s %s",
                    SERVER_JAR, SERVER_CLASS, SERVER_VERSION, optionsStr);
            synchronized (waitObj) {
                serverStarted=true;
                waitObj.notifyAll();
            }
            ret = execCmd(true, command.split(" "));
            if (ret.getRet() != 0) {
                throw new RuntimeException("start scrcpy-server failed " + ret.getOut());
            }
        }catch (Exception e){
            e.printStackTrace();
            synchronized (waitObj) {
                serverStarted=false;
                waitObj.notifyAll();
            }
        }
        serverStarted=false;
    }

    /**
     * 停止scrcpy服务
     */
    private static void stopServer() {
        if (scrcpyServerThread != null) {
            scrcpyServerThread.interrupt();
            scrcpyServerThread = null;
        }
        try {
            execCmd("adb", "forward", "--remove", "tcp:" + SERVER_PORT);
            // 查找并杀掉scrcpy-server进程
            ExeResult ret = execCmd("adb", "shell", "ps", "-ef");
            String[] lines = ret.getOut().split("\n");
            for (String line : lines) {
                if (line.contains(SERVER_CLASS)) {
                    String[] parts = line.split("\\s+");
                    execCmd("adb", "shell", "kill", "-9", parts[1]);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行系统命令 返回执行结果
     *
     * @param cmd
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private static ExeResult execCmd(boolean transferToConsole, String... cmd) throws IOException, InterruptedException {
        Process process = null;
        int ret = -1;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            process.getInputStream().transferTo(transferToConsole ? System.out : bos);
            ret = process.waitFor();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        ExeResult exeResult = new ExeResult(ret, transferToConsole ? "执行结果已经输出到控制台" : bos.toString(StandardCharsets.UTF_8));
        System.out.printf("cmd: %s, ret: %s\n", String.join(" ", cmd), exeResult.getRet());
        return exeResult;
    }

    /**
     * 执行命令
     *
     * @param cmd
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private static ExeResult execCmd(String... cmd) throws IOException, InterruptedException {
        return execCmd(false, cmd);
    }
}