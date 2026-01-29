# Plugin Message Forwarder

**Velocity 插件**，用于无条件转发插件消息 (`PluginMessage`)，面向开发人员。

## 背景

根据 [Velocity 开发文档](https://docs.papermc.io/velocity/dev/plugin-messaging/#case-3-receiving-a-plugin-message-from-a-backend-server)，插件消息转发方向可分为四种：

![Velocity 插件消息转发方向](https://docs.papermc.io/d2/docs/velocity/dev/api/plugin-messaging-0.svg)

在使用 **Client Mod** 与 **Bukkit / 下游服务器** 进行网络通信时：

* 如果子服使用 **Velocity**，Client Mod 可以直接向子服中的插件发送消息并被接收。即图上的方向 1 和 2。
* 但是，如果子服想向 **Client Mod** 发送消息，则必须在 **Velocity 层**进行插件消息转发，否则消息会被静默忽略。即图上的方向 3 和 4。

有时，插件开发者只需要简单地在 **Velocity 层**转发消息，而不想额外编写复杂插件。
本插件正是为了这一场景提供轻量、开箱即用的解决方案。

## 功能

* 自动转发来自后端服务器的插件消息给玩家。
* 支持多通道配置。
* 面向开发人员，无需额外逻辑即可实现消息转发。

## 配置

在 `plugins/plugin-message-forwarder/config.yml` 中配置需要转发的通道，例如：

```yaml
enable: true
channels:
  - foo:bar
```

> 插件会自动在 Velocity 启动时注册这些通道。

## 安装

1. 将 `plugin-message-forwarder-$version.jar` 放入 `plugins/` 目录。
2. 启动或重载 Velocity。
3. 确认 `logs/latest.log` 中插件启用信息。

## 示例

```yml
channels:
  - myplugin:example
  - anotherplugin:chat
```

启动后，**所有后端服务器发往这些通道的消息都会被自动转发给客户端**。



