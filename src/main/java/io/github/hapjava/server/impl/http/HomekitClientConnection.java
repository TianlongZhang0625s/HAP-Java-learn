package io.github.hapjava.server.impl.http;

import java.io.IOException;

public interface HomekitClientConnection {

  HttpResponse handleRequest(HttpRequest request) throws IOException;

  //解谜
  byte[] decryptRequest(byte[] ciphertext);

  // 加密
  byte[] encryptResponse(byte[] plaintext) throws IOException;

  void close();

  void outOfBand(HttpResponse message);
}
