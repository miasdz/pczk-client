# ParamCenter

Spring实现基于ZooKeeper参数中心系统客户端(pczk-client)

博客地址：https://www.cnblogs.com/superstudy/p/9696631.html

## 特性
> * 多系统多模式参数存储
> * 安全验证
> * 参数实时更新
> * 低侵入
> * 高可用

## 适用范围
> 适用spring框架开发的系统

## 使用步骤
> **1.** 从github检出代码，放置到系统内，或者通过maven打包，导入项目依赖包<br/>
> **2.** 在spring配置文件中增加`PczkPropertyPlaceholderConfiguer`配置，详见/src/test/resources/applicationContext.xml<br/>
> **3.** pckz提供了类似于spring中`${}`的功能，配置方式为`zk{}`，配置支持三种配置方法<ul><li>字符窜 `zk{string}`</li><li>JSON对象 `zk{{map}.key}`</li><li>JSON数组 `zk{list[index]}`</li></ul>其中上述配置中name，server和list均为ZooKeeper节点<br/>
> **4.** 通过上述配置方式即可实现基于ZooKeeper的参数配置中心，可参考测试案例【test路径下资源】

## 实时维护
> 基于ZooKeeper的Watcher机制，在ZooKeeper服务端更新参数内容，通过Watcher机制通知到应用，并做相应的维护操作。详见`/src/main/java/itwatertop/core/beans/factory/config/ZookeeperDataLoader.java `以及 `/src/main/java/itwatertop/core/beans/factory/config/PczkBeanDefinitionVisitor.java`。
其中技术实现采用了SpEL表达式用于对Bean属性的访问以及setter方法的调用，因此其属性设置受限于SpEL，不支持Set的属性设置
