
Better later than never. 一年一度的中间件性能大赛虽然比往年来的都迟了一些、奖金少了一些、决赛名额砍了一些，但是始终还是来了。虽然奖励大幅度下降，但是参加比赛获得的隐形福利仍然是不少的。在这里你可以见识到各路大牛，各种实现思路，并且真实模拟难得一见的双十一的洪峰场景。进入复赛前二十的选手还可以获得阿里中间件招聘优先推荐的名额；进入复赛前五名，还能领取阿里免费三日游的资格，可以参观阿里园区，可以在亲橙里的盒马鲜生吃上各种龙虾（波士顿龙虾、小龙虾、大螃蟹），还可以见到多隆、索尼等神级阿里大牛，甚至还有可能会获得他们的指导，以及一笔奖金。今年还有一个特别的地方是，中间件的内部赛和外部赛是同时举行的，所以在比赛的过程中你也能和阿里内部的选手同台PK，所以更能向阿里的大佬们展现你自己的能力了。

我去年参加比赛有幸进入了最终决赛，认识了蛮多很厉害的伙伴。而且其中很多人都借比赛机会进入了阿里。尤其是岛风同学，不仅年纪轻轻地就拿到了P6的职级，今年更是贡献了众多的初赛代码，从选手变成了出题人，因为比赛而实现了自己职业生涯的华丽转身，真的是颇为励志。所以，各位对阿里有兴趣的同学，还犹豫什么呢，赶紧来参加比赛，用代码证明你自己配得上阿里吧！

参赛地址在这里(https://tianchi.aliyun.com/markets/tianchi/aliware2019?spm=5176.12281905.5490641.1.358b5699BGxiZq). 推荐人请填Wayne Wang。谢谢各位。

同样，为了借比赛机会骗粉，我会写一篇简短的赛题解析来帮助大家更快地进入比赛状态。毕竟，评测6月10号就开放了，接下来的一周把代码写好，就比其他人多了更多的评测机会了。希望大家看完以后踊跃参赛，并且关注本号，谢谢。

### 初赛环境搭建

比赛的git地址在这里(https://code.aliyun.com/middlewarerace2019/adaptive-loadbalance). 实际上这个loadbalance已经实现了一个随机路由的demo算法。而要将demo跑起来，我们需要三个项目。可以找一个目录，依次执行如下命令：

```
git clone https://code.aliyun.com/middlewarerace2019/dubbo-internal.git
mvn clean install -Dmaven.test.skip=true

git clone https://code.aliyun.com/middlewarerace2019/adaptive-loadbalance.git
mvn clean install -Dmaven.test.skip=true

git https://code.aliyun.com/middlewarerace2019/internal-service.git
mvn clean install -Dmaven.test.skip=true
```

要注意顺序。官方repo的readme里面没有提需要install`adaptive-loadbalance`项目，而在本地做开发的时候，`internal-servce`是依赖前者的，所以要跑起来`internal-service`，我们要先install `adaptive-loadbalance`。而readme里面提要install`internal-service`，可是在本地开发的时候，我们不需要做这个事情。你可以在IDE里面打开`internal-service`，并运行`MyProvider`和`MyConsumer`。其中`MyProvider`可以运行三次，只需要给其加上`-Dquota`的flag而已。入下图：
![IDE配置](assets/images/internal-service-ide.jpg)

三个Provier的`quota`的Flag分别设置为small、medium、large，另外，还要做的事情是将如下配置写入自己的hosts文件里面

```
127.0.0.1 provider-small
127.0.0.1 provider-medium
127.0.0.1 provider-large
```
这样，我们的开发环境就搭建完毕。此时还有一点比较麻烦的是，修改了`adaptive-loadbalance`之后，需要重新install才能跑service。因为开发变动的次数可能会很多，所以我的做法是将`internal-service`的子项目代码复制到`adaptive-loadbalance`的项目里面，然后修改`pom.xml`文件，使得其可以直接执行`loadbalance`里面的代码。`pom`要改的地方其实不多，把service里面的几个子项目的`pom.xml`文件里面的parent改成`adaptive-loadbalance`，然后在父项目的`pom`文件里面，将三个service模块加上去就好了。具体内容不贴这里了，相信各位精通maven的读者不需要我这种只用SBT来构建的人来指导如何使用maven对吧。

### 题目解析

细致的题目内容我就不多说了，比赛地址上都有。而一言以蔽之，就是我们要利用Dubbo的扩展机制，实现一个`loadbalance`接口。Consumer可以借助这个接口来将外部请求智能负载到后端的不同的Dubbo服务器上。可以用如下图形表示赛题架构：
![架构图](assets/images/service-architect.png)

官方提供一个评测的脚本`wrk.lua`。我们将`Provider`和`Consumer`都启动以后，可以执行如下命令来进行测试：
```
wrk -t4 -c1024 -d60s -T5 --script=./wrk.lua --latency http://localhost:8087/invoke
```

最终会按照两种排序方式来进行排序：
1. 有效请求数；
2. QPS。

有效请求数高的排名在前；有效请求数一样的话，QPS高的在前。

实际上，今年的初赛和去年的初赛很像，去年初赛其实也有要考察负载均衡实现的意思。只不过去年因为死抠网络协议细节得到的收益远大于负载均衡的收益，所以选手的心思主要放在如何最小化网络开销上了。而今年的题目则将这些细节隐藏，由dubbo专注处理。所以我们要做的事情，就是如何实现一个智能、高效的负载均衡算法，来让后面的`Provider`资源能力得到最大化的利用。

### 代码解析

举办方使用Dubbo的SPI机制来给我们开放接口。SPI(Service Provider Interface)是JDK内置的一种动态加载扩展点的实现。只要在ClassPath的META-INF/services目录下放置一个与接口同名的文本文件，文件的内容为接口的实现类，Dubbo就可以自动加载这些实现类来执行。目前，官方demo中的实现接口分别有：

#### Provider端

1. `org.apache.dubbo.remoting.transport.RequestLimiter`
2. `org.apache.dubbo.rpc.Filter`
3. `org.apache.dubbo.rpc.service.CallbackService`

#### Consumer端

1. `org.apache.dubbo.rpc.cluster.LoadBalance`
2. `org.apache.dubbo.rpc.Filter`
3. `org.apache.dubbo.rpc.listener.CallbackListener`

这上面，`Filter`类是过滤器，Consumer和Provider端都可以用，我们可以通过实现该类，在调用请求的前后做一些特定操作。例如，Demo中代码如下： 

``` 
@Activate(group = Constants.PROVIDER)
public class TestServerFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try{
            //todo 可以在这里做一下统计或者其他相关的逻辑
            Result result = invoker.invoke(invocation);
            //todo 这里也可以
            return result;
        }catch (Exception e){
            throw e;
        }

    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        return result;
    }

}
```

`CallbackService`和`CallbackListener`是一对。在Gateway连接上Provider的时候，会注册一个`CallbackListener`上去。此时`Provider`可以通过这个`CallbackListener`发送信息回给Gateway。我们可以通过这个机制，将`Provder`的一些状态信息发送给`Gateway`，使其能利用它们来实现自己的负载均衡机制。

`RequestLimiter`没啥用，个人推荐不要用。限流的操作交给`Gateway`来处理即可。

`LoadBalance`是我们的重头戏。我们需要围绕这个类来实现我们的负载均衡机制。它只有一个方法：
```
    /**
     * select one invoker in list.
     *
     * @param invokers   invokers.
     * @param url        refer url
     * @param invocation invocation.
     * @return selected invoker.
     */
    @Adaptive("loadbalance")
    <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException;
```

然后Demo实现了一个随机路由的方式：
```
public class UserLoadBalance implements LoadBalance {

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }
}
```

实际上，这个方法能达到的QPS已经蛮高了。在我本地测试有15k左右的QPS。而我们争取要做到的是，能比随机路由的算法做得更高更好。我们要利用前面提到的各个接口的使用机制，记录各个Provider的服务状况，并且依据这些信息，来选择到合适的Invoker,将调用交给其执行。并且争取达成最大量的正确调用数。这个才是决定比赛胜负的王道。

### 一点小提示

不知道大家看到这里有没有发现，想将Consumer的请求通过队列收集起来，然后通过消费队列来做负载均衡是个很困难的事情。因为`select`的时候直接就要选定`invoker`，并且将调用交给`invoker`来执行。这个过程中，没有办法构建统一的协调者，来整流请求和统一负载。如果`select`是异步的方法，假设有多个请求同时打过来，则当时的invoker的状态是一致的，所以这些请求有可能选定相同的Invoker打过去，然后造成了Provider的过载。如果我们获得了后端Provider的最大的处理能力，假设我们想要在Provider都被打满的时候暂时停止向他们发送请求，因为没有队列，那我们此时能如何去做呢？在`select`方法里面执行`Thread.sleep`方法？

而如果能用上队列的话，那上面的问题都不是问题，都能想办法解决。

那么，如何使用队列呢？

我们知道，Dubbo 3.0以来，有一个最大的趋势是异步化改造。也就是，将原来的需要阻塞调用`get`方法的`Future<Result>`类型，改为Java 8引入的`CompletableFuture<Result>`类型。这个不仅仅是接口上要做改变，在Dubbo的后端实现上也需要做对应的改进。目前看来本次比赛使用的Dubbo版本应该是未发布的Dubbo 3.0的一个抢先体验版。所以我们的调用结果`Result`可以是`AsyncRpcResult`。而只要能异步了，我们能操作的空间就开阔多了。

这里就要提到我们常用的`Future/Promise`模式。

`Future`是一个只读句柄(不是Java里面的Future)，`Promise`是对应的单次写入句柄。`Promise`中含有一个`Future`，在使用的过程中，我们可以将这个`Future`交给下游，然后由其他线程负责写入`Promise`，以完成`Future`。这样就直接将执行和结果分离了。下游获得了一个只读结果，并且可以在结果完成的时候，通过回调执行其他事务；而负责完成结果的任务，则可以在任何地方执行，只要它能有`Promise`这个句柄即可。

至于如何应用在本次比赛中，大家可以各显神通了。

### 结语

很高兴阿里能一直坚持举办这种工程类的比赛，这种比赛其实在市面上很稀缺，所以做基础组件的朋友们很容易就可以在这里遇到朋友。目前看来，初赛难度其实比去年低多了，评测直接提交Demo也可以有成绩。而拔高可能就需要更多算法上、细节实现上的思考。不管如何，无论是学生、还是工作中的程序员，都可以来这里感受一下阿里程序员可能会遇到的场景。而不同的体验，可以为我们打开不同的世界。这也是一种别样的收获。

记得，推荐人填Wayne Wang，说不定进复赛的概率就能高30%哟~
