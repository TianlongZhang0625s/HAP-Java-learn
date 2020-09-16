package io.github.hapjava.characteristics;

import java.util.concurrent.CompletableFuture;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * 此可以理解为所有Characteristic的最抽象的描述，所有被抽象的Characteristic，均继承或者实现
 * 这个最高级抽象的接口
 *
 * 需要知道的是，这些Characteristic是可以被远程client修改和查看的（主要是状态）。一般的，如果
 * 实现一个自定义的accessory时，可以先考虑使用标准库（io.github.hapjava.characteristics）
 * 里面的相近的Characteristic的具体类型进行实现，而不是自己直接去基于JSON格式去直接实现，除非
 * 真的没有合适的，可按照现有库的形式，构建自定义的accessory接入的Characteristic。
 *
 * @author Andy Lintner
 */
public interface Characteristic {

  /**
   * 本质上为添加键值对
   * JsonObjectBuilder add(String var1, JsonValue var2); from JsonObjectBuilder source code
   *
   * @param characteristicBuilder 添加键值对的JsonObjectBuilder对象.
   */
  void supplyValue(JsonObjectBuilder characteristicBuilder);

  /**
   * 构建JSON形式描述的Characteristic对象
   *
   * @param iid Characteristic的instance ID，在序列化时须加入此iid
   * @return JSON格式描述Characteristic的完成的JSON object结果.
   */
  CompletableFuture<JsonObject> toJson(int iid);

  /**
   * 由远程客户端调用，调用后将更新Characteristic的值
   *
   * @param jsonValue 要设置的JSON序列化后的值.
   */
  void setValue(JsonValue jsonValue);
}
