package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Ningyu He on 2016/11/29.
 */
public class Proxy {
    private ExecutorService executorService;

    private ServerSocket serverSocket;

    private static int LISTEN_PORT = 1234;//代理监听端口为1234

    public Proxy(int port) {
        executorService = Executors.newCachedThreadPool();//创建线程池的标准用法
        try {
            serverSocket = new ServerSocket(port);//初始化一个监听端口，准备accept来建立一个socket
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void accept() {
        while (true) {
            try {
                executorService.execute(new RequestThread(serverSocket.accept()));//RequestThread继承了Runnable接口，为每一个socket建立一个线程
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Proxy proxy = new Proxy(LISTEN_PORT);//初始化所有变量
        proxy.accept();
    }
}
