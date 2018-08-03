package com.github.hippo.govern.utils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * 服务治理工具类
 *
 * @author sl
 */
public final class ServiceGovernUtil {

    private ServiceGovernUtil() {
    }

    /**
     * 获取服务器可用的端口(不需要用户自己设定端口了)
     *
     * @return 可用端口
     */
    public static int getAvailablePort() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }
}
