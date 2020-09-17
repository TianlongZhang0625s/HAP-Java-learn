package io.github.hapjava.characteristics.impl.base;

import io.github.hapjava.characteristics.ExceptionalConsumer;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.json.JsonNumber;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

/**
 * characteristic 为boolean类型的处理
 *
 * @author Andy Lintner
 */
public abstract class BooleanCharacteristic extends BaseCharacteristic<Boolean> {

  private final Optional<Supplier<CompletableFuture<Boolean>>> getter;
  private final Optional<ExceptionalConsumer<Boolean>> setter;

  /**
   * 默认 constructor
   *
   * @param type 可参考BaseCharecteristic的说明
   * @param description 可参考BaseCharecteristic的说明
   * @param getter 获取设备的值
   * @param setter 设置设备的值
   * @param subscriber 可参考BaseCharecteristic的说明
   * @param unsubscriber 可参考BaseCharecteristic的说明
   */
  public BooleanCharacteristic(
      String type,
      String description,
      // 需要了解，Optional，Supplier以及CompltableFuture的用法和实现
      Optional<Supplier<CompletableFuture<Boolean>>> getter,
      Optional<ExceptionalConsumer<Boolean>> setter,
      Optional<Consumer<HomekitCharacteristicChangeCallback>> subscriber,
      Optional<Runnable> unsubscriber) {
//    初始化父类BaseCharecteristic
    super(
        type,
        "bool",
        description,
        getter.isPresent(),
        setter.isPresent(),
        subscriber,
        unsubscriber);
    this.getter = getter;
    this.setter = setter;
  }

  /** {@inheritDoc}
   * Boolean类型对可BaseCharecteristic的convert的实现，可转换为boolean类型的对象
   * */
  @Override
  protected Boolean convert(JsonValue jsonValue) {
    if (jsonValue.getValueType().equals(ValueType.NUMBER)) {
      return ((JsonNumber) jsonValue).intValue() > 0;
    }
    return jsonValue.equals(JsonValue.TRUE);
  }

  // 获取连接设备的Characteristic的boolean值
  @Override
  protected CompletableFuture<Boolean> getValue() {
    return getter.isPresent() ? getter.map(booleanGetter -> booleanGetter.get()).get() : null;
  }

  // 设置值，从连接客户端设置，发送到设备，可参考BaseCharecteristic的说明
  @Override
  protected void setValue(Boolean value) throws Exception {
    if (setter.isPresent()) setter.get().accept(value);
  }

  // 默认值，default为false
  /** {@inheritDoc} */
  @Override
  protected Boolean getDefault() {
    return false;
  }
}
