package io.github.hapjava.server.impl.http;

import java.util.function.Consumer;

public interface HomekitClientConnectionFactory {

  /**
   * 其实可以理解为是HomeKit 自己实现的一个独有的Http服务协议
   * 创建基于http服务的 连接，最为重要的就是创建一个连接，重要的
   * 就是分析HomeKitClientConnection ！！！
   * @param outOfBandMessageCallback
   * @return HomekitClientConnection
   */

  HomekitClientConnection createConnection(Consumer<HttpResponse> outOfBandMessageCallback);
}
