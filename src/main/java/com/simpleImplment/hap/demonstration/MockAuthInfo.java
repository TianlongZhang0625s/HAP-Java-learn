package com.simpleImplment.hap.demonstration;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.util.function.Consumer;

import io.github.hapjava.server.HomekitAuthInfo;
import io.github.hapjava.server.impl.HomekitServer;

/**
 * @author Andy Lintner
 */


/**
 * 这里其实实现了HomeKit的HomeKitAuthInfo这个接口，这个接口定义了用户验证的公共的行为方式，只是
 * 用于测试，不可以用于实际的产品。毕竟mac地址（设备的网络接口的mac地址是固定），salt和私钥在进程应
 * 用每次启动的时候都会自动生成，这显然和实际是不同的，也同时意味着每次重启应用程序都会重新配对。
 * @modified Tianlong Zhang
 */

public class MockAuthInfo implements HomekitAuthInfo {

    /**
     * 1. AuthState 是需要自定义实现的，authStatus根据需要需提供PIN码，Mac地址以及
     * privateKey
     * 这里设计为final的AuthState，意味着authState的指向不能发生变化，也理解为指针确定分
     * 具体的内容可不可变化还需看具体实现的对象设计为 immutable类型的，没有提供set方法，
     * 同时 authState标识的，对象的指向是不可变化的，后续这个设计特性有用。可参考多线程设计模式
     * 的immutable模式
     * {@link HomekitAuthInfo HomekitAuthInfo}里面的注释
     *
     * 组合方式实现：
     * 可理解为实现HomeKitAutoInfo接口，就是为了提供配对及验证用的信息（来自于服务器还是设备？）
     */
    private final AuthState authState;

    /**
     * 可以看到这里使用了Java 8 中函数式编程的接口，Consumer接口
     * 我的理解是，这个接口可以使用对象把自己的处理逻辑（不需要返回值的处理逻辑）封装起来，
     * 然后可以通过andThen方法实现按照顺序执行Consumer<T>中T类型数据的操作，
     * 较为优雅的实现了对象的处理，
     * 可参考博客：https://blog.csdn.net/fangyang_000/article/details/103664403
     * 实例体会
     */
    Consumer<AuthState> callback;

    // 两种初始化方式
    public MockAuthInfo() throws InvalidAlgorithmParameterException {
        // 除了PIN码之外，还需使用HomeKitServer生成一个Mac，salt和密钥。
        this(new AuthState("031-45-154", HomekitServer.generateMac(), HomekitServer.generateSalt(),
                HomekitServer.generateKey()));
    }

    /**
     * 默认为传入一个authState 对象，使用默认生成时，可参考上面的初始化方法
     *
     * @param _authState 传入的authState，这个
     */

    public MockAuthInfo(AuthState _authState) {
        authState = _authState;
        System.out.println("The PIN for pairing is " + authState.PIN);
    }

    // 实现HomekitAuthInfo接口中的方法，privateKey
    @Override
    public String getPin() {
        return authState.PIN;
    }

    @Override
    public String getMac() {
        return authState.mac;
    }

    @Override
    public BigInteger getSalt() {
        return authState.salt;
    }

    @Override
    public byte[] getPrivateKey() {
        return authState.privateKey;
    }

    // 实现用户创建
    @Override
    public void createUser(String username, byte[] publicKey) {
        // 判断用户是否存在，不存在则创建用户user的KeyMap，key值为用户，value为
        // 公钥
        if (!authState.userKeyMap.containsKey(username)) {
            authState.userKeyMap.putIfAbsent(username, publicKey);
            System.out.println("Added pairing for " + username);
            // 更新操作 （onChange方法是否对callback进行了赋值操作），没有改动则不更新
            notifyChange();
        } else {
            System.out.println("Already have a user for " + username);
        }
    }

    // 下面的方法可参考HomekitAuthInfo接口
    @Override
    public void removeUser(String username) {
        authState.userKeyMap.remove(username);
        System.out.println("Removed pairing for " + username);
        notifyChange();
    }

    @Override
    public byte[] getUserPublicKey(String username) {
        return authState.userKeyMap.get(username);
    }


    // 在这里调用onChange方法的时，就已经实现了对mockAuth里面 Consumer<AuthState> callback
    // 的赋值，所以，callback不为空
    public void onChange(Consumer<AuthState> _callback) {
        callback = _callback;
        notifyChange();
    }


    private void notifyChange() {
        if (callback != null) {
            callback.accept(authState);
        }
    }
}
