 实现了netty发送byte。发送字符串（基于行，基于分隔符）。并写了两个简单的编解码器。netty自带的发送接收protobuf。netty-http,netty-websocket
 
 
 **注意：Server中的Handler，每一个客户端连接上来，就会实例化一个Handler实例。**
