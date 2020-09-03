# Part 1: HAP(HomeKit Accessory Protocol)协议浅析

## 1. HomeKit Accessory Protocol协议简介：

1. 苹果HomeKit Accessory Protocol协议是HomeKit库是用来沟通和控制家庭自动化配件的，这些家庭自动化配件都支持苹果的HomeKit Accessory Protocol。
2. HomeKit应用程序可让 用户发现兼容配件并配置它们。用户可以创建一些action来控制智能配件（例如恒温或者光线强弱），对其进行分组，并且可以通过Siri触发。 
3. HomeKit 对象被存储在用户iOS设备的数据库中，并且通过iCloud还可以同步到其他iOS设备。

## 2. HomeKit Accessory Protocol核心概念理解：

1. ### **什么是控制终端？**

   一般是指运行IOS或者是MacOS的苹果产品，例如iPhone，iPad，Watch等。

2. ### **何为Transports：**

   这里的Transports指的是硬件设备通过HomeKit Accessory Protocol（以下简称HAP）协议接入时所使用的连接方式，目前苹果支持<u>**Bluetooth BLE**</u>和基于<u>**IP方式**</u>实现接入的。

   **<u>*基于Bluetooth BLE的接入方式：*</u>**

   首先，HAP协议规定使用的Bluetooth LE版本至少为2.0以上，同时遵循标准的蓝牙GATT协议，为方便后续理解，蓝牙GATT协议同样需要有几个概念需要理解，对于两种方式的接入理解，后续会单独介绍：

   1. **profile：**profile可以理解为一种规范，一个标准的通信协议，它存在于从机中。蓝牙组织规定了一些标准的profile，例如 HID OVER GATT ，防丢器 ，心率计等。每个profile中会包含多个service，每个service代表从机的一种能力。

   2. **service：**service可以理解为一个服务，在BLE从机中，通过有多个服务，例如电量信息服务、系统信息服务等，每个service中又包含多个characteristic特征值。每个具体的characteristic特征值才是BLE通信的主题。比如当前的电量是80%，所以会通过电量的characteristic特征值存在从机的profile里，这样主机就可以通过这个characteristic来读取80%这个数据。

   3. **characteristic：**characteristic特征值，BLE主从机的通信均是通过characteristic来实现，可以理解为一个标签，通过这个标签可以获取或者写入想要的内容。

   4. **UUID：**统一识别码，我们刚才提到的service和characteristic，都需要一个唯一的uuid来标识

      **整理一下，每个从机都会有一个叫做profile的东西存在，不管是上面的自定义的profile，还是标准的防丢器profile，他们都是由一些列service组成，然后每个service又包含了多个characteristic，主机和从机之间的通信，均是通过characteristic来实现。**

      **BLE接入要求**：必须使用适配于当前HAP-BLE规定中的一种协议，同时还需要确认蓝牙核心版本是否符合要求（如>= 4.2），同时Accessories（下面会介绍）要确定蓝牙是否在5.0版本以上。

   ***<u>基于IP的接入方式：</u>***

   IP我们并不陌生，HAP同时支持IP方式接入，同时支持了IPv4和IPv6两种方式接入，也就意味着我们可以通过基于ip地址定向的方式实现我们设备的互联互通，也可以通过控制终端来对设备进行操作。

3. ### **何为Security？**

   这里的安全其实为苹果HAP端到端的之间会话的相互认证和加密，每次会话都生成一个唯一的key进行标识，这些验证确认认证工作则是通过配对进行处理的。

   ***<u>paring 配对：</u>***在配对时会建立IOS设备和接入设备之间的加密的配对关系，配对的过程则由两步实现：

   1. *<u>配对启动：</u>*配对设置阶段指的是IOS设备和连接设备之间安全的交换对方的公钥，这其中需要用户在IOS设备上设置一个8位数的配对码。
   2. <u>*配对验证：*</u>这个过程将贯穿每一个HAP的会话，确认IOS设备和接入设备之间建立临时私有的连接验证来保证HAP的会话安全。

   **<u>*Session keys：对*</u>**验证期间由临时私有的连接来传输双方加密和认证密钥。

4. ### **何为Attributes？**

HAP协议有两类属性；**HAP用服务（services）**和**特征（charecteristics）**来描述接入设备的能力。在这里，何为accessories？这里是指接入的设备，例如吊扇，比如灯，智能门等。其中特征是被被包含在服务中的，一个服务中可以有多个charecteristics，两者都是来描述设备功能的，但是charecteristics则描述具体具体的特征，而service可视为这些charecteristics的集合，这个集合则描述了这个接入设备的功能。

<u>***Services：***</u>

<u>*Service Naming ：*</u> 服务名对于和用户交互的或者是用户可见的，则服务中必须有服务名，且必须要有Name 特征进行标识且其他的服务则不包含这项服务名特征。对于是否可见，则需要由IOS控制端来决定是否要展现给用户。

<u>*Extending Services：*</u>扩展服务指的是指为了保证原有老设备的兼容性，任何后续版本中特征的增加都必须设置为可选服务，并且后续版本中的服务不可以改变已有历史版本服务的行为。

<u>*Primary Services：*</u>主服务是指需要指明接入设备众多的服务中最核心、最主要的服务作为主服务，主服务要与设备相匹配并且要匹配当前设备所属的品类，一个主服务在一个接入设备中只能有一个主服务。

<u>*Hidden Services：*</u>隐藏服务则对应Service Naming中的内容，即那些不暴露给用户的服务，例如升级服务中仅仅暴露是否需要升级服务，而不会将升级服务暴露，所以升级服务设置为隐藏服务。同时，一个服务中是包涵多个charecteristics的，若这个服务中所有的charecteristics均为隐藏的charecteristics，则这个服务为隐藏服务。

*<u>Linked Services：</u>*链接服务则增强了sevices对设备的描述能力，举个例子：一个service可以链接多个services，但是他自己是不能连接自己的，如果A链接了B，B链接了C，那么在使用services描述设备时，不能够默认为A链接了C，也就是说，A的链接服务只有B；如果A链接了C，那么A的链接服务中就有了B和C。苹果定义的service举例如下：

![image-20200815195653171](/Users/tianlongzhang/Library/Application Support/typora-user-images/image-20200815195653171.png)

<u>***Charecteristics：***</u>

charecteristics为service最直接相关的操作数据或者行为的对象。它是由类型和附加的属性来确定此charecteristic是如何接入的。一个charecteristic可以被复用到多个services中，而charecteristic则有以下几点需要注意：

*<u>权限：</u>*权限有读/写/提醒三种权限，根据不同的需要进行合理的设置。

<u>*格式：*</u>对于属性的描述可以有多种格式的数据来描述，如int，string等。并且数据也有其对应的限制如maxValue，minValue等来限制数据，使得数据的描述有效。

对于charecterisitc类型中的值有什么要求呢？

1. 验证值的有效性：在连接时charecteristic所描述的值一定是charecteristic元数据里面包含的类型，同样的元数据则枚举了HAP所支持的值的类型。
2. 增加授权数据：HAP中默认写权限是需要授权数据的，这在一定程度上保证了控制的安全和数据以及设备的数据安全，其由控制端产生，意在验证控制端是否有权限操作写等操作的权限。此控制端是有接入设备的app提供的，这个app为接入设备对应操作的app，一般由设备接入方提供。每个写入请求的附加授权数据不能是唯一的，因为控制器不会为每个请求构造或接收唯一的授权数据。一般数据持续一段后会进行更改，例如一个月一次或则是用户授权修改。

苹果定义的charecteristic举例如下：

![image-20200815195907506](/Users/tianlongzhang/Library/Application Support/typora-user-images/image-20200815195907506.png)

<u>***附加HAP服务：***</u>

下表表示了苹果HomeKit协议

![image-20200815194940259](/Users/tianlongzhang/Library/Application Support/typora-user-images/image-20200815194940259.png)

5. ### 何为Profile？

profile定义了合适的services和charecteristics来保持控制设备行为的一致性，可理解为一种品类或sku应当尽可能的有一种整体一致性的行为。在苹果HAP中，apple定义的profile有苹果定义的services和charecteristics来组成，一个自定义的profile则有自定义的charecterisitcs，苹果定义的charecteristics和自定义的services组成。可以看出，无论苹果定义的profile还是自定义的profile，都需要有苹果定义的charecteristics。这也就是说，接入设备必须使用苹果定义的charecteristics来暴露附加的功能，如果这个功能在苹果的定义中已有且可用的话。例如，温度计里面定义了一个当前温度的功能，如果苹果定义了这个charecteristics来描述时，我们首选苹果的这个功能，而不是把这个功能自定义为一个新的工能。我们分析一个例子，例如灯需要开和关这样的charecteristics来描述灯的开关状态，苹果对于此类的charecteristics定义的例子如下，那么我们在描述另一个相似设备时候，就需要复用下面的这个charecteristic，而不是自己重新定义一个charecteristic。

![image-20200815200700917](/Users/tianlongzhang/Library/Application Support/typora-user-images/image-20200815200700917.png)

6. ### 何为Roles？

Roles英文单词释义为角色，是的，Roles在这里则规范化的描述了在HAP中所要产生信息交互的角色，这些抉择在苹果HAP中的定义和我们之前的认知有很大的相似，但还是有些不同，具体的定义如下：

**<u>*HAP Client：*</u>**

一般情况下HAP Client泛指所有的控制端，也就是我们常说的controller，其主要功能有两个：一是在HAP服务器上注册和接收HAP服务器上的提示信息；二是在HAP Client端发送请求消息和接收HAP服务器返回的消息。

**<u>*HAP Accessory Server：*</u>**

HAP Accessory Server一般泛指支持或实现了苹果HAP协议的设备，并且暴露了可实现client接入以及控制的集合，一般至少暴露一个HAP接入的对象（HAP accessory object），其表现为和HAP关系配对中的一个终端。HAP Accessory Server需要支持接收控制端的消息并给予返回信息，需要能够发送提醒信息和客户端注册。

*<u>**HAP Accessory Objects：**</u>*

这个说白了就是接入HAP Server的物理设备而对应在HAP服务器上的对象，例如恒温器将暴露其在服务器上的Obhject以用来提供恒温器的用户可寻址功能。

对于服务来说，接入HAP服务器的对象必须包含一个必选的服务，这个协议强制要求的，必须有一个，所以在实现的时候可以结合自己的设备特点或者结合HAP协议里面的Apple-defined Profiles里面的定义内容。

***<u>Bridges桥接器：</u>***

桥接器的作用个人认为是adapter适配器和链接路由的组合，adapter部分能够实现消息数据的转换，本质上说来其是一种特殊的HAP接入服务器，主要功能为接入其他工作方式的物理设备，如基于zigbee，z-wave以及RF等的设备。其必须暴露一个地址寻址工能用来支持终端设备连接到HAP controller。网桥必须确保分配给代表其连接的桥接端点的HAP附件对象的实例ID在服务器/客户端配对的生存期内不会更改。在HAP中规定不能一次性暴露超过150个接入的HAP accessory Object。同时需要注意的是，基于wifi和IP接入的不需要桥接接入。同样的基于蓝牙LE且可控的设备也不需要，像上面提及到的基于zigbee等的设备则需要一定的桥接方式连接进入。一般的HAP接入对像的instance id和接入对象是一一对应的，然而像物理设备为桥接器的话，则指的是桥接器自己。

HAP附属对象中包含的服务必须并置。举个例子，风扇灯，在苹果HAP协议中，风扇功能和灯的功能将暴露一个HAP接入对象的三个服务：风扇服务，灯的服务以及设备信息服务，这三个服务可以并在一个对应的HAP 接入对象里面，相反的像桥接器连接的多个设备，则需要每个设备都暴露一个HAP 接入对象。

