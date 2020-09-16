package io.github.hapjava.services.impl;

import io.github.hapjava.characteristics.Characteristic;
import io.github.hapjava.services.Service;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实现了Service基本的功能，也可理解为Service的通用服务的default实现
 *
 * 主要功能为添加和读取charecteristics
 */
abstract class AbstractServiceImpl implements Service {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final String type;
  // 存放charecteristics，此为服务中包含的charecteristics
  private final List<Characteristic> characteristics = new LinkedList<>();

  /** @param type unique UUID; HAP 中定义的服务的的uuid */
  public AbstractServiceImpl(String type) {
    this.type = type;
  }

  /* 使用unmodifiableList 可以提供一个只读的列表，防止修改*/
  @Override
  public List<Characteristic> getCharacteristics() {
    return Collections.unmodifiableList(characteristics);
  }

  @Override
  public String getType() {
    return type;
  }

  public void addCharacteristic(Characteristic characteristic) {
    this.characteristics.add(characteristic);
  }
}
