# 多人聊天室的实现
## 需求描述
### 目标
实现一个多人聊天室，每个客户端启动后可以在自己的聊天框中发送消息或接收消息。客户端中发送的消息可以被聊天室中的其它客户端接收。客户端输入“quit”时，表示客户端下线，不再发送消息或接收任何消息。
### 功能拆分
- 客户端需要实现发送消息和接收消息的功能
- 服务端需要实现转发消息的功能。
- 客户端输入 quit 后，不再发送和接收消息

## BIO 实现
### BIO 编程模型
![Alt text](https://myblog-1258060977.cos.ap-beijing.myqcloud.com/cnblog/IO%26NIO/io/BIO%E7%BC%96%E7%A8%8B%E6%A8%A1%E5%9E%8B.png)

由于 BIO 是阻塞的，因此服务端在与一个客户端建立连接之后，就没办法继续与其它客户端建立连接并接收其它客户端的消息。因此，上图的编程模型中，将服务端拆分为`Acceptor`和`Handler`是必要的。`Acceptor`负责监听客户端连接，同时为每一个客户端连接，创建一个`Handler`来处理与客户端的通信。这样一来，Client 和 Handler 就是一对一的关系。

### 功能时序图
![Alt text](https://myblog-1258060977.cos.ap-beijing.myqcloud.com/cnblog/IO%26NIO/io/BIO%E5%AE%9E%E7%8E%B0%E6%97%B6%E5%BA%8F%E5%9B%BE.png)

### BIO 编程模型的优化
#### BIO 编程模型的缺点
客户端与服务端的 Handler 是一对一的关系，也就是说，每当一个客户端请求服务端时，服务端就要为新的客户端创建一个线程。客户端请求数很多时，服务端也将创建大量的线程，占用大量的系统资源，极端情况下，可能会导致系统负载过大。
针对以上情况，引入线程池，将线程数量控制起来。
#### 伪异步 IO 编程模型
![Alt text](https://myblog-1258060977.cos.ap-beijing.myqcloud.com/cnblog/IO%26NIO/io/%E4%BC%AA%E5%BC%82%E6%AD%A5IO%E7%BC%96%E7%A8%8B%E6%A8%A1%E5%9E%8B.png)

代码的优化：
```java
// 引入线程池
private ExecutorService executorService;
// 使用线程池
// 为每个客户端连接创建一个 Handler 线程处理
// 1.方法一：来一个创建一个
// new Thread(new ChatHandler(this, socket)).start();
// 2.方法二：线程池
executorService.submit(new ChatHandler(this, socket));
```


