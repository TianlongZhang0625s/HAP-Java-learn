package io.github.hapjava.server.impl.http;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Map;

/**
 * http 应答，状态码，消息体和版本
 */
public interface HttpResponse {

  int getStatusCode();

  default ByteBuffer getBody() {
    return ByteBuffer.allocate(0);
  }

  /**
   * HttpVersion 主要支持1.1和EVENT_1_0
   * @return 支持通信协议的版本
   */
  default HttpVersion getVersion() {
    return HttpVersion.HTTP_1_1;
  }

  default Map<String, String> getHeaders() {
    return Collections.emptyMap();
  }

  default boolean doUpgrade() {
    return false;
  }

  public enum HttpVersion {
    HTTP_1_1,
    EVENT_1_0
  }
}
