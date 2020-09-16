package io.github.hapjava.characteristics;

/**
 * 带有事件监听类型的 Characteristic，可订阅事件和解除事件订阅，可以理解为此类型的Characteristic
 * 可能会有状态的变化。若状态发生变化，则可以理解为产生了一个事件，若理解为某个具体的accessory订阅了
 * 此事件，在状态发生变化时，则订阅者则会根据事件变更自己的状态或者行为。
 * 例如，温度计感受到了变化了的温度，则会产生一个变化事件，我们的ios Client订阅了这个事件，则变化的
 * 温度则会上报到我们的ios
 *
 * @author Andy Lintner
 */
public interface EventableCharacteristic extends Characteristic {

  /**
   * 开始监听带有状态或行为改变的事件的Characteristic，订阅者有反应的前提是订阅了此事件。
   *
   * @param callback 回调方式，将改变的值进行发布。其实就是值变化了，通知订阅者作出相应的改变。
   */
  void subscribe(HomekitCharacteristicChangeCallback callback);

  /** 注销对此事件的监听 */
  void unsubscribe();
}
