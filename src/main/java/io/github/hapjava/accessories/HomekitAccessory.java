package io.github.hapjava.accessories;

import io.github.hapjava.services.Service;
import io.github.hapjava.services.impl.AccessoryInformationService;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * 苹果待接入设备的主接口,其他所有的具体的accessory连接，并通过接口定义了最为基本的行为
 * 也就是定义了相关的方法。
 * 同时，在其HomeKitAccessory的具体类型的实例化对象中也必须包含方法执行所必须的字段，并同时
 * 实现这些方法
 *
 * @author Andy Lintner
 */
public interface HomekitAccessory {

  /**
   * 返回唯一识别码，此识别码用于IOS设备配对。若使用桥接模式的话
   * 进入到桥接器的子设备的话，子设备的ID可能多余1个，此时ID可看作是
   * 桥接器的ID
   *
   * @return the unique identifier 单个设备接入，则是单个设备的ID，桥接的话，一般为桥接器的ID
   */
  int getId();

  /**
   * 返回一个表示名称，String类型，用于在IOS设备上展现使用
   *
   * @return the label 也就是需要展现的名字.
   */
  CompletableFuture<String> getName();

  /**
   * 功能为识别设备的名称或者类型，即便是在不配对的情况下也可以实现获取设备的名称或者类型如
   * in light
   */
  void identify();

  /**
   * IOS设备需要展示的设备序列号
   *
   * @return the serial number, or null.
   */
  CompletableFuture<String> getSerialNumber();

  /**
   * model -->用于在IOS设备上展现的model内容
   *
   * @return the model name, or null.
   */
  CompletableFuture<String> getModel();

  /**
   * 制造商
   * @return the manufacturer, or null.
   */
  CompletableFuture<String> getManufacturer();

  /**
   * 固件版本
   * @return the firmware revision, or null.
   */
  CompletableFuture<String> getFirmwareRevision();

  /**
   * 设备所支持的服务的集合，实现基于HomeKit协议操作设备等主要还是基于服务，除集合里面提供的服务之外
   * 设备必须包含这些必选的信息服务。

   * @return 设备所支持的服务的集合.
   */
  default Collection<Service> getServices() {
    return Collections.singleton(new AccessoryInformationService(this));
  };

  /**
   *获取主服务，这里对应苹果homekit文档，若有疑问请参考第一个文档的相关概念的介绍
   *
   * @return primary service
   */
  default Service getPrimaryService() {
    return getServices().iterator().next();
  };
}
