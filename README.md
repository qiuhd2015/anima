# anima

anmia 是基于java游戏服务器框架，它是高性能、高可伸缩、分布式多进程的游戏服务器框架，包括基础开发框和库，可以帮助游戏开发人员省去枯燥的重复劳动和底层逻辑工作，让开发人员只关心具体的游戏逻辑，从而提供开发效率。

# anima-sever 包结构说明

 ---- backend		提供后端Server服务
	
	---- blacklist 	网络黑名单处理模块
	
	---- channel		后端channel服务，方便推送、广播消息给客户端
	
	---- client		异步client实现，用于后端服务器之间网络通讯
	
	---- common       公共库：包括编解码库、线程池、公共模块库、工具类等等
	
	---- fronend      提供前端Server服务
	
	---- handler		消息处理器模块
	
	---- protocol     包含消息协议定义
	
	---- remoting		网络通讯底层模块
	
	---- route		前端server 路由模块
	
	---- session		Client session 和 Server sesisin
	
	---- surrogate	前端代理模块，前端服务器与后端服务器通讯的桥梁

# 代码commit flag说明
feature -  新功能.

imp     -  improvment简写，改进现有功能.

ref     -  regactor简写，代码重构.

fix     -  修复bug.

test    -  测试相关.

review  -  code review后添加的TODO 标记，说明或改动.

res     -  引入资源.
 
 
