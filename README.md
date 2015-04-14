# anima

anmia 是基于java游戏服务器框架，它是高性能、高可伸缩、分布式多进程的游戏服务器框架，包括基础开发框和库，可以帮助游戏开发人员省去枯燥的重复劳动和底层逻辑工作，让开发人员只关心具体的游戏逻辑，从而提供开发效率。

# anima-sever 包结构说明
 	backend		提供后端Server服务
 	blacklist 	网络黑名单处理模块
	channel	    后端channel服务，方便推送、广播消息给客户端
	client	    异步client实现，用于后端服务器之间网络通讯
	common     公共库：包括编解码库、线程池、公共模块库、工具类等等
	fronend    提供前端Server服务
	handler	   消息处理器模块
	protocol   包含消息协议定义
	remoting	网络通讯底层模块
	route	前端server 路由模块
	session	Client session 和 Server sesisin
	surrogate	前端代理模块，前端服务器与后端服务器通讯的桥梁
# 代码commit flag说明
	feature -  新功能.
	imp     -  improvment简写，改进现有功能.
	ref     -  regactor简写，代码重构.
	fix     -  修复bug.
	test    -  测试相关.
	review  -  code review后添加的TODO 标记，说明或改动.
	res     -  引入资源.
#　anima 近期开发计划

1. 完善前端服务器（连接服务器）功能，前端服可以为后端提供服务，并且前端提供服务与为后端提供服务之间隔离，客服端不能使用后端的服务。
2.服务器提供接口给应用层，如连接事件，重连事件
3. 改进客户端请求服务器支持无请求体也就是请求的体可以为空；服务器响应客户端可以没有响应体。
4. 性能测试。



 
 
