package io.github.hapjava.characteristics.impl.base;

import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.characteristics.EventableCharacteristic;
import io.github.hapjava.characteristics.HomekitCharacteristicChangeCallback;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 此为Characteristic的基本功能的实现，这种实现方式可类似参考AccessoryInformationService
 * 的实现。
 * 此基类定义了Characteristic抽象的共有的行为，继承后的子类可添加和复写父类的方法，实现子类特有
 * 的行为。
 *
 * 对于基本类型的characteristic，其有boolean，enum，float，integer和静态String 5 种类型的实现
 *
 * @author Andy Lintner
 */
public abstract class BaseCharacteristic<T> implements Characteristic, EventableCharacteristic {

  private final Logger logger = LoggerFactory.getLogger(BaseCharacteristic.class);

  private final String type;
  private final String shortType;
  private final String format;
  private final String description;
  private final boolean isReadable;
  private final boolean isWritable;
  private final Optional<Consumer<HomekitCharacteristicChangeCallback>> subscriber;
  private final Optional<Runnable> unsubscriber;

  /**
   * 默认constructor
   *
   * @param type 唯一识别Characteristic的 id. 苹果定义了一个Characteristic的集合，但是用户也可自己定义。
   *             ﻿例如：public.hap.characteristic.administrator-only-access
   * @param format 一个字符串，标识fo此类型的format为可识别的类型，具体可参考HAP文档，例如uint32，uint8等.
   * @param isWritable 是否可写
   * @param isReadable 是否可读，与上面的isWritable可理解为HAP文档中所指明的权限
   * @param description characteristic 的描述
   * @param subscriber 监听事件，变化上报
   * @param unsubscriber 取消变上化报
   */
  public BaseCharacteristic(
      String type,
      String format,
      String description,
      // 是否可读和可写
      boolean isReadable,
      boolean isWritable,
      // 对于事件型的characteristic监听
      Optional<Consumer<HomekitCharacteristicChangeCallback>> subscriber,
      Optional<Runnable> unsubscriber) {
    // 这里可以确定，type，format和description是必须有的
    if (type == null || format == null || description == null) {
      throw new NullPointerException();
    }

    this.type = type;
    // shotType则为一个用$1替换后的短的tpye
    this.shortType = this.type.replaceAll("^0*([0-9a-fA-F]+)-0000-1000-8000-0026BB765291$", "$1");
    this.format = format;
    this.description = description;
    this.isReadable = isReadable;
    this.isWritable = isWritable;
    this.subscriber = subscriber;
    this.unsubscriber = unsubscriber;
  }

  @Override
  /** {@inheritDoc} */
  public final CompletableFuture<JsonObject> toJson(int iid) {
    return makeBuilder(iid).thenApply(builder -> builder.build());
  }

  /**
   * 返回基于HAP协议定义的JSON格式序列化的JSON对象
   *
   * @param instanceId 接入设备的iid
   * @return 返回基于HAP协议定义的JSON格式序列化的JSON对象，这里使用的是JUC包中的Future模式实现的
   */
  protected CompletableFuture<JsonObjectBuilder> makeBuilder(int instanceId) {
    CompletableFuture<T> futureValue = getValue();

    if (futureValue == null) {
      futureValue = CompletableFuture.completedFuture(getDefault());
    }

    return futureValue
        .exceptionally(
            t -> {
              logger.warn("Could not retrieve value " + this.getClass().getName(), t);
              return null;
            })
        .thenApply(
            value -> {
              JsonArrayBuilder perms = Json.createArrayBuilder();
              if (isReadable) {
                perms.add("pr");
              }
              if (isWritable) {
                perms.add("pw");
              }
              if (subscriber.isPresent()) {
                perms.add("ev");
              }
              JsonObjectBuilder builder =
                  Json.createObjectBuilder()
                      .add("iid", instanceId)
                      .add("type", shortType)
                      .add("perms", perms.build())
                      .add("format", format)
                      .add("ev", false)
                      .add("description", description);
              if (isReadable) setJsonValue(builder, value);
              return builder;
            });
  }

  /** {@inheritDoc} */
  @Override
  public final void setValue(JsonValue jsonValue) {
    try {
      setValue(convert(jsonValue));
    } catch (Exception e) {
      logger.warn("Error while setting JSON value", e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void supplyValue(JsonObjectBuilder builder) {
    try {
      setJsonValue(builder, getValue().get());
    } catch (InterruptedException | ExecutionException e) {
      logger.warn("Error retrieving value", e);
      setJsonValue(builder, getDefault());
    }
  }

  /** {@inheritDoc} */
  @Override
  public void subscribe(HomekitCharacteristicChangeCallback callback) {
    subscriber.get().accept(callback);
  }

  /** {@inheritDoc} */
  @Override
  public void unsubscribe() {
    unsubscriber.get().run();
  }

  /**
   * JSON 格式的数据转换为Java Object
   *
   */
  protected abstract T convert(JsonValue jsonValue);

  /**
   * 使用连接设备设置的value对设备characteristic的值进行更新，设置的时候与权限
   * 相关，是否是可写的（isWritable）
   */
  protected abstract void setValue(T value) throws Exception;

  /**
   * 返回当前characteristic的值
   *
   */
  protected abstract CompletableFuture<T> getValue();

  /**
   * 获取characteristic发送到连接客户端的m默认值
   *
   */
  protected abstract T getDefault();

  /**
   * 向序列化的JSON格式数据写入key，value：--> characteristic
   *
   */
  protected void setJsonValue(JsonObjectBuilder builder, T value) {
    // I don't like this - there should really be a way to construct a disconnected JSONValue...
    if (value instanceof Boolean) {
      builder.add("value", (Boolean) value);
    } else if (value instanceof Double) {
      builder.add("value", (Double) value);
    } else if (value instanceof Integer) {
      builder.add("value", (Integer) value);
    } else if (value instanceof Long) {
      builder.add("value", (Long) value);
    } else if (value instanceof BigInteger) {
      builder.add("value", (BigInteger) value);
    } else if (value instanceof BigDecimal) {
      builder.add("value", (BigDecimal) value);
    } else if (value == null) {
      builder.addNull("value");
    } else {
      builder.add("value", value.toString());
    }
  }
}
