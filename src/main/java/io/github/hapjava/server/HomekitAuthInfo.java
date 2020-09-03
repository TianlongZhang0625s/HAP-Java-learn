package io.github.hapjava.server;

import java.math.BigInteger;



/**
 * HomeKitAuthInfo 暴露了集中通用的验证行为的方法，作为一个暴露的接口，
 * 其通过暴露公用方法实现对公共行为操作的定义，这里面的内容必须自己实现，因为这里需要
 * 设备配对以及连接真实物理设备和身份验证等。
 */
public interface HomekitAuthInfo {

  /**
   * 获取 PIN码，用于IOS和设备之间配对用的，对于PIN来说，数字不能是连续的，不应该有重复的模式。
   *
   * @return PIN形式为：###-##-###
   */
  String getPin();

  /**
   * 这里需要的是设备的Mac地址，这里的Mac地址不一定是真是网络硬件接口的mac地址，
   * 可以通过HomeKitServer的generateMac方法获取。
   *
   * @return 全局唯一的Mac地址
   */
  String getMac();

  /**
   * 获取生成的salt类似于噪声，在密码进行hash时候加入，hash后的整体（salt+key）发送去客户端
   * 这个方法用于获取salt，salt生成可参考HomekitServer的generateSalt
   *
   * @return the Salt.
   */
  BigInteger getSalt();

  /**
   * 获取私钥，用于配对和消息加密时候使用，同样可参考HomekitServer的generateKey()
   *
   * @return the private key.
   */
  byte[] getPrivateKey();

  /**
   * 创建用户，此方法在配对过程中使用，这里将用户和公钥实现为可存储的持久化的内容，
   * 主要用于重置或者是信息丢失时。
   *
   * @param username the iOS device's username.
   * @param publicKey the iOS device's public key.
   */
  void createUser(String username, byte[] publicKey);

  /**
   * 用于删除/接触已有的配对，执行后在调用getuserpublicKey时，unsername将置为null
   *
   * @param username the username to delete from the persistent store.
   */
  void removeUser(String username);

  /**
   * 适用于配对后的设备重连情况，获取的公钥将和设备配对验证中的签名进行对比，用来验证设备，
   * 其实就是根据用户名获取已经存储的公钥
   *
   * @param username the username of the iOS device to retrieve the public key for.
   * @return the previously stored public key.
   */
  byte[] getUserPublicKey(String username);

  /**
   * 用于验证用户是否已经创建，是否早已和接入设备进行了配对。
   *
   * @return whether a user has been created and stored
   */
  default boolean hasUser() {
    return false;
  };
}
