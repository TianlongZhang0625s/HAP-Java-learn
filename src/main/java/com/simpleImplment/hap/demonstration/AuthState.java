package com.simpleImplment.hap.demonstration;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 此类实现了序列化接口，这意味这需要经过网络将这个序列化对象进行传输
 * 对此，考虑是否可以通过其他的序列化协议将这个对象进行序列化
 *
 */
class AuthState implements Serializable {
    private static final long serialVersionUID = 1L;
    // 包含了用户验证及配对需要的PIN，mac，salt和privateKey
    String PIN;
    final String mac;
    final BigInteger salt;
    final byte[] privateKey;
    final ConcurrentMap<String, byte[]> userKeyMap = new ConcurrentHashMap<>();

    public AuthState(String _PIN, String _mac, BigInteger _salt, byte[] _privateKey) {
        PIN = _PIN;
        salt = _salt;
        privateKey = _privateKey;
        mac = _mac;
    }
}
