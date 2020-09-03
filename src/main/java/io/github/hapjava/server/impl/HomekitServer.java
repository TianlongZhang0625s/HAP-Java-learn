package io.github.hapjava.server.impl;

import io.github.hapjava.accessories.HomekitAccessory;
import io.github.hapjava.server.HomekitAuthInfo;
import io.github.hapjava.server.impl.http.impl.HomekitHttpServer;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.util.concurrent.ExecutionException;


/**
 * 这个类的作用就是创建一个在指定端口的服务器实例监听HomeKit连接，
 * 对于每个唯一的实例和端口，只能添加一个单例的接入accessory root，
 * 特殊情况为bridge桥接的接入，桥接接入则带有子类型的accessory
 * 可参考{@link #createBridge(HomekitAuthInfo, String,
 *  String, String, String, String, String) bridge accessory}
 *
 * 对于HomekitAuthInfo接口必须实现，在前面MockAuthInfo已经提到，因为需要这个实现
 * 类提供PIN，Key，Mac，Salt等。在每次启动应用程序时，须提供上述值，如Mac等非常重要，
 * 否则HomeKit将无法识别接入的设备是否相同，无法确定是否唯一，这里也对应了MockAuthInfo
 * 里面AuthState设置为final，也是为了保证指定的AuthState为唯一的。
 * 对此可在此参考{@link HomekitAuthInfo HomekitAuthInfo}和 MockAuthInfo
 *
 * @author Andy Lintner
 * @modified Tianlong Zhang
 */
public class HomekitServer {

  // HomeKit的HttpServer，设计为final
  private final HomekitHttpServer http;
  // 使用的Java JDK的InetAddress
  private final InetAddress localAddress;

  /**
   * HomeKitServer构建实例对象的构造方法。其中包含了创建线程的数量，服务地址，端口。
   * 其他构造函数将其默认为可用处理器的数量，但是在有许多用户和/或阻塞的accessory的
   * 实现的环境中，则可以增加线程的数量。
   *
   * @param localAddress 须绑定的本地地址
   * @param port 须绑定的本地服务的端口
   * @param nThreads http服务器的线程数量
   * @throws IOException 无法绑定时的异常处理
   */
  public HomekitServer(InetAddress localAddress, int port, int nThreads) throws IOException {
    this.localAddress = localAddress;
    http = new HomekitHttpServer(localAddress, port, nThreads);
  }

  /**
   * 构造方法 （默认当前可环境可使用的线程数量-->Runtime.getRuntime().availableProcessors())）
   *
   * @param localAddress 须绑定的本地地址
   * @param port 须绑定的本地服务的端口
   * @throws IOException 无法绑定时的异常处理
   */
  public HomekitServer(InetAddress localAddress, int port) throws IOException {
    this(localAddress, port, Runtime.getRuntime().availableProcessors());
  }

  /**
   * 构造方法（基于本地地址的，一般为单机的localhost）
   *
   * @param port 须绑定的本地服务的端口
   * @throws IOException 无法绑定时的异常处理
   */
  public HomekitServer(int port) throws IOException {
    this(InetAddress.getLocalHost(), port);
  }

  /**
   * 停止当前的所有的服务，并关闭现有的所有的http连接，同时阻止新的http连接产生。
   * 一般在多线程情况下，我们对stop方法还是很重视的，因为在释放资源的时候最容易出现安全问题，
   * 由于此HomeKitServer的底层实现为Netty的，所以，其实关闭的是netty的EventLoopGroup接口对应的
   * 线程组并释放资源。其实追寻HAP-Java的源码可知，EventLoopGroup接口继承了EventExecutorGroup接口，
   * 而EventExecutorGroup接口则又继承了Java Concurrent包下的ScheduledExecutorService，到这里
   * 就熟悉了，这就是经常见到的ScheduledExecutorService接口，线程池的概念，这些内容就不展开了。
   * 而说回stop方法，其实调用的是底层netty的shutdownGracefully方法，这个方法将会在NettyHomekitHttpService
   * #shutdown()方法中继续分析。
   *
   */
  public void stop() {
    http.stop();
  }

  /**
   * Accessory两种类型之一：普通的单个一对一的accessory，非桥接方式
   *
   * @param authInfo 对应accessory的授权信息，这些值应该持久化到本地，
   *                 并在重新启动应用程序时重新提供
   * @param accessory accessory的对应实现，其实就是io.github.hapjava.accessories
   *                  包下面支持接入的accessory的具体实现
   * @return homekitStandaloneAccessoryServer 新创建的HomekitStandaloneAccessoryServer
   *                  类型的实例，可通过调用start方法执行
   * @throws IOException 多播DNS无法连接网络的异常
   */
  public HomekitStandaloneAccessoryServer createStandaloneAccessory(
      HomekitAuthInfo authInfo, HomekitAccessory accessory)
      throws IOException, ExecutionException, InterruptedException {
    return new HomekitStandaloneAccessoryServer(accessory, http, localAddress, authInfo);
  }

  /**
   * 多次提到，对于桥接设备，则一个接入的桥接accessory持有多个接入子类型的accessories，与多个独立的accessory
   * 相比，桥接的优势在于只需要iOS为桥接器提供一个配对。
   *
   * @param authInfo 对应accessory的授权信息，这些值应该持久化到本地，
   *                 并在重新启动应用程序时重新提供
   * @param label 桥接标识，在配对时使用。
   * @param manufacturer 桥接器的制造商
   * @param model 桥接器的制造商
   * @param serialNumber 桥接器的序列号
   * @param firmwareRevision 桥接器的固件版本
   * @param hardwareRevision 桥接器的硬件版本
   * @return 桥接器实例, 可通过 HomekitRoot的addAccessory和accessories增加一个或者多个
   *                  接入的accessory，通过start方法处理请求。
   * @throws IOException 多播DNS无法连接网络的异常
   */
  public HomekitRoot createBridge(
      HomekitAuthInfo authInfo,
      String label,
      String manufacturer,
      String model,
      String serialNumber,
      String firmwareRevision,
      String hardwareRevision)
      throws IOException {
    HomekitRoot root = new HomekitRoot(label, http, localAddress, authInfo);
    root.addAccessory(
        new HomekitBridge(
            label, serialNumber, model, manufacturer, firmwareRevision, hardwareRevision));
    return root;
  }

  /**
   * 下面为默认状态下，通过HomekitUtils工具类随机生成PIN，Mac，Salt以及密钥
   * 服务器端生成用作配对？
   */

  /**
   * Generates a value to supply in {@link HomekitAuthInfo#getSalt() HomekitAuthInfo.getSalt()}.
   * This is used to salt the pin-code. You don't need to worry about that though - the salting is
   * done on the plaintext pin. (Yes, plaintext passwords are bad. Please don't secure your nuclear
   * storage facility with this implementation)
   *
   * @return the generated salt
   */
  public static BigInteger generateSalt() {
    return HomekitUtils.generateSalt();
  }

  /**
   * Generates a value to supply in {@link HomekitAuthInfo#getPrivateKey()
   * HomekitAuthInfo.getPrivKey()}. This is used as the private key during pairing and connection
   * setup.
   *
   * @return the generated key
   * @throws InvalidAlgorithmParameterException if the JVM does not contain the necessary encryption
   *     algorithms.
   */
  public static byte[] generateKey() throws InvalidAlgorithmParameterException {
    return HomekitUtils.generateKey();
  }

  /**
   * Generates a value to supply in {@link HomekitAuthInfo#getMac() HomekitAuthInfo.getMac()}. This
   * is used as the unique identifier of the accessory during mDNS advertising. It is a valid MAC
   * address generated in the locally administered range so as not to conflict with any commercial
   * devices.
   *
   * @return the generated MAC
   */
  public static String generateMac() {
    return HomekitUtils.generateMac();
  }

  /**
   * Generates a value to supply in {@link HomekitAuthInfo#getPin() HomekitAuthInfo.getPin()}. This
   * is used as the Pin a user enters into their HomeKit device in order to confirm pairing.
   *
   * @return the generated Pin
   */
  public static String generatePin() {
    return HomekitUtils.generatePin();
  }
}
