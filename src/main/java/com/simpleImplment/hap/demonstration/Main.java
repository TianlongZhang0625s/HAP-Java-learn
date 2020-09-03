package com.simpleImplment.hap.demonstration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import io.github.hapjava.server.impl.HomekitRoot;
import io.github.hapjava.server.impl.HomekitServer;


/**
 * 一个简易的HAP-Java实现，这个可以帮助我们理解HAP协议具体是如何执行的
 * 持久性实现非常原始，除了为了方便测试，不应使用，因此应用程序启动之间的信
 * 息将保持与iOS设备的配对信息。
 *
 * 将持久化数据写入文件身份验证-state.bin在当前工作目录中。
 */

public class Main {

    /* 指定端口*/
    private static final int PORT = 9123;

    public static void main(String[] args) {
        try {
            String useredparh = "/Users/tianlongzhang/localgitrepo/HAP-Java/src/main/java/com/simpleImplment/hap/demonstration/";
            File authFile = new File(useredparh+"auth-state.bin");
            /**
             * 上述文件读取就不说了，下面进入MockAuthInfo 理解下要干什么
             */
            MockAuthInfo mockAuth;

            // 两种创建方式，auth-state.bin（模拟本地持久化存储的方式）或者是默认创建
            if (authFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(authFile);
                // 按照字节流解析auth-state.bin文件
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                try {
                    System.out.println("Using persisted auth");

                    // 生成authStates
                    AuthState authState = (AuthState) objectInputStream.readObject();
                    mockAuth = new MockAuthInfo(authState);
                } finally {
                    objectInputStream.close();
                }
            } else {
                mockAuth = new MockAuthInfo();
            }

            // 创建指定端口的HomeKitServer实例
            HomekitServer homekit = new HomekitServer(PORT);

            // 创建桥接，这个可参考Homekit协议浅析中的相关定义概念并理解，然后参考
            // 在这里有两种创建accessory的方式，一种方式为创建一对一类型的accessory，
            // 需要对io.github.hapjava.accessories包下面对应accessory类型进行具体实现
            // 实现方式如下：
            // HomekitRoot singleAccessory =
            // homekit.createStandaloneAccessory(mockAuth,accessoryImplement);
            // 另一种为桥接类型，如下：
            HomekitRoot bridge = homekit.createBridge(mockAuth, "Test Bridge", "TestBridge, Inc.", "G6", "111abe234", "1.1", "1.2");

            // 在这里构建了state，其实就是AuthState类型的对象state传入Consumer<AuthState> callback

            mockAuth.onChange(state -> {
                try {
                    System.out.println("State has changed! Writing");
                    // 写到authFile 里面，这里只是写，同时也没有提供AuthState的set方法
                    FileOutputStream fileOutputStream = new FileOutputStream(authFile);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                    objectOutputStream.writeObject(state);
                    objectOutputStream.flush();
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            bridge.addAccessory(new MockSwitch());
            bridge.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
