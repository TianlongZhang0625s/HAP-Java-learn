package io.github.hapjava.server.impl.http;

/**
 * Http请求，url/消息体，Http方法
 */
public interface HttpRequest {

  String getUri();

  byte[] getBody();

  // 枚举类，get，post，put类型的http方法
  HttpMethod getMethod();
}
