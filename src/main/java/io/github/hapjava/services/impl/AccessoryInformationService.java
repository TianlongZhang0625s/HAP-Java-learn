package io.github.hapjava.services.impl;

import io.github.hapjava.accessories.HomekitAccessory;
import io.github.hapjava.accessories.optionalcharacteristic.AccessoryWithAccessoryFlags;
import io.github.hapjava.accessories.optionalcharacteristic.AccessoryWithHardwareRevision;
import io.github.hapjava.characteristics.impl.accessoryinformation.AccessoryFlagsCharacteristic;
import io.github.hapjava.characteristics.impl.accessoryinformation.FirmwareRevisionCharacteristic;
import io.github.hapjava.characteristics.impl.accessoryinformation.HardwareRevisionCharacteristic;
import io.github.hapjava.characteristics.impl.accessoryinformation.IdentifyCharacteristic;
import io.github.hapjava.characteristics.impl.accessoryinformation.ManufacturerCharacteristic;
import io.github.hapjava.characteristics.impl.accessoryinformation.ModelCharacteristic;
import io.github.hapjava.characteristics.impl.accessoryinformation.SerialNumberCharacteristic;
import io.github.hapjava.characteristics.impl.common.NameCharacteristic;

/**
 * 接入设备信息服务
 */
public class AccessoryInformationService extends AbstractServiceImpl {

  /**
   * 两种初始化方式，可以分别利用对象传入和单独传入accessory对象实现初始化，也可根据需要
   * 自定义初始化方式
   */
  public AccessoryInformationService(
          // 为什么初始化identity，manufacturer等，这是因为HAP文档中关于AccessoryInformationService
          // 中包含了这些charecterisitics字段，所以，我们需要参考HAP文档来明确初始化哪些变量
          // 需要注意的是这些是标记为必选的字段，所以，这些是一定要实现的， 也可理解为功能实现的最小子集
      IdentifyCharacteristic identify,
      ManufacturerCharacteristic manufacturer,
      ModelCharacteristic model,
      NameCharacteristic name,
      SerialNumberCharacteristic serialNumber,
      FirmwareRevisionCharacteristic firmwareRevision) {
    // 来源于HAP文档中AccessoryInformationService服务的uuid，全局唯一
    super("0000003E-0000-1000-8000-0026BB765291");
    // 调用父类的addCharecteristics方法完成上述成员的初始化
    addCharacteristic(identify);
    addCharacteristic(manufacturer);
    addCharacteristic(model);
    addCharacteristic(name);
    addCharacteristic(serialNumber);
    addCharacteristic(firmwareRevision);
  }

  public AccessoryInformationService(HomekitAccessory accessory) {
    this(
        new IdentifyCharacteristic(
            value -> {
              if (value) {
                accessory.identify();
              }
            }),
        new ManufacturerCharacteristic(accessory::getManufacturer),
        new ModelCharacteristic(accessory::getModel),
        new NameCharacteristic(accessory::getName),
        new SerialNumberCharacteristic(accessory::getSerialNumber),
        new FirmwareRevisionCharacteristic(accessory::getFirmwareRevision));

    /** 判断是否为需要增加optionCharecterisitics, 所以这里增加了AccessoryWithHardwareRevision
     * 和AccessoryWithAccessoryFlags两个可选的Characteristics*/
    if (accessory instanceof AccessoryWithHardwareRevision) {
      addOptionalCharacteristic(
          new HardwareRevisionCharacteristic(
              ((AccessoryWithHardwareRevision) accessory)::getHardwareRevision));
    }
    if (accessory instanceof AccessoryWithAccessoryFlags) {
      addOptionalCharacteristic(
          new AccessoryFlagsCharacteristic(
              ((AccessoryWithAccessoryFlags) accessory)::getAccessoryFlags,
              ((AccessoryWithAccessoryFlags) accessory)::subscribeAccessoryFlags,
              ((AccessoryWithAccessoryFlags) accessory)::unsubscribeAccessoryFlags));
    }
  }

  // 增加额外可选optional的Characteristics，类型为硬件版本
  public void addOptionalCharacteristic(HardwareRevisionCharacteristic hardwareRevision) {
    addCharacteristic(hardwareRevision);
  }

  // 增加额外可选optional的Characteristics，类型为接入特征flag
  public void addOptionalCharacteristic(AccessoryFlagsCharacteristic accessoryFlagsCharacteristic) {
    addCharacteristic(accessoryFlagsCharacteristic);
  }
}
