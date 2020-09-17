package io.github.hapjava.characteristics.impl.battery;

import io.github.hapjava.characteristics.EventableCharacteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import io.github.hapjava.characteristics.impl.base.IntegerCharacteristic;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
/**
 * 可以参考苹果HAP文档中关于battery的service的描述，里面包含了电池电量，电池充电状态
 * 电池低电量提示等，这里的实现和HAP文档基本相似。
 * 不过实现了required 类型的characteristic
 */

/** This characteristic describes the current level of the battery. */
public class BatteryLevelCharacteristic extends IntegerCharacteristic
    implements EventableCharacteristic {

  public BatteryLevelCharacteristic(
      Supplier<CompletableFuture<Integer>> getter,
      Consumer<HomekitCharacteristicChangeCallback> subscriber,
      Runnable unsubscriber) {
    super(
        "00000068-0000-1000-8000-0026BB765291",
        "battery level",
        0,
        100,
        "%",
        Optional.of(getter),
        Optional.empty(),
        Optional.of(subscriber),
        Optional.of(unsubscriber));
  }
}
