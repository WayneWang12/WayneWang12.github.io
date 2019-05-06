很多年以来，程序员的工作都充斥着大量的CRUD开发，我们要做的就是根据实体的变化去创建、查询、更新或者删除数据库中的记录而已。这些开发工作并不难，所以做多了以后，业务程序员可能会觉得枯燥、没意思。因为开发工作也许就是调调其他人的接口、提供一下自己的接口，然后顺便更新后面的库而已。

但是实际上，我一直以来的观点是，只要在系统性能指标可以接受的范围内，快速支撑业务往往比精良的高性能系统更重要，除非你的当前CRUD系统无法支撑自己的海量业务和复杂需求。这个时候，就需要架构师来做一下架构升级，要么将后端的数据库引入主从提升性能，要么分库分表，要么引入复杂的分布式数据库，以继续提供ACID的支持。

当然，还有另一种选项，就是CQRS。

### 命令与查询职责分离

CQRS模式的全称是*命令与查询职责分离*(Command & Query Responsibility Segregation)。它的基本思想很简单，将用户的操作都抽象成命令，每次操作就是向对象发送一条命令，之后写入命令产生对应的事件，读取命令则回复当前状态，然后再由事件处理器依据生成的事件来对数据库做出修改，而查询的时候则直接查库就好。如此，我们就将业务的处理过程简化成为如下几点：

1. 抽象出实体并设计操作命令
2. 设计由命令生成的事件
3. 持久化事件, 并更新实体的当前状态，响应命令操作
4. 根据不同事件执行不同的数据库更新操作
5. 响应查询相关的操作

这样，我们就将读写操作分离开来，写入的时候不需要考虑对于数据库的操作，只需要向实体发送命令，让其逐一消费即可；事件的存储则可以在单一文件后顺序写入；产生的事件可以交给数据库慢慢消费，直到完成为止。

然而传统的架构观点则是，CQRS会使得开发变得更复杂，因为本来是直接更新的东西，我们必须通过设计不同的命令和事件来处理；本来是基于单一数据库强一致性的内容，现在存储事件之后就响应了用户，数据库在稍后才会更新；而CQRS在本质上追求的是高可用和最终一致性，它与我们长期以来的ACID直觉相悖。我们将存储简化为了对事件的顺序存储和对数据库的延迟更新，而在部分业务中，这可能是无法接受的。

所以，在采用CQRS架构的时候，我们需要确定好它的界限上下文。在不同的上下文中，根据自己的需求来采用不同的架构。

### 领域驱动设计

CQRS和DDD(Domain-Driven Development, 领域驱动设计)息息相关。首先可以列出部分DDD中的概念:

#### 实体(Entity)

当一个对象由其标识(而不是属性)区分时，这种对象称为实体(Entity)。比如当两个对象的标识不同时，即使两个对象的其他属性全都相同，我们也认为他们是两个完全不同的实体。

#### 值对象(Value Object)

当一个对象用于对事物进行描述而没有唯一标识时，那么它被称作值对象。值对象一般是不可变的数据结构。

#### 聚合与聚合根

Aggregate(聚合）是一组相关对象的集合，作为一个整体被外界访问，聚合根（Aggregate Root）是这个聚合的根节点。聚合由根实体，值对象和实体组成。

#### 领域事件
领域事件是对领域内发生的活动进行的建模。

将以上于CQRS关联，实体就是我们的操作对象，我们可以通过值对象来描述实体的状态，以及其操作命令。由命令产生的事件则可看作为领域事件，我们持久化领域事件，并根据领域事件来对数据库进行操作。实际上，CQRS受到重视与DDD的大力推广联系紧密。人们在微服务的实践过程中遇到很多问题，其中一点就是如何划分微服务的范围。在划分的过程中就有人发现了DDD在这方面的巨大价值。而CQRS模式天然地就适用于这种划分。基于CQRS架构，我们能很快地开发出对于实体的操作逻辑。如果产品经理在设计的时候能够讲清楚实体相关的操作与处理逻辑，业务开发就能轻松地就根据文档所描述的命令和事件开发代码。基于这种特性，在同一领域上下文中，领域专家（产品）和开发就能无障碍地沟通与交流。产品与开发的阶级矛盾斗争也就不会那么激烈。

### 事件溯源

事件溯源(Event Sourcing)其实是CQRS没有Q时候的形态。为什么有CQRS呢？是因为之前的实践中，使用事件溯源模式来更新实体状态的时候，在查询功能上有痛点。比如，每个用户有一个标识，我们通过标识找到了对应的用户实体，然后通过命令更改了用户的所属地区。之后，如果有一项需求是查询地区为上海的用户的数量时，事件溯源模式就有问题了。因为我们只存储了对用户的相关操作，每次获取用户当前状态的时候，我们需要将用户实体相关的事件全部读取出来，然后依次更新用户实体状态，直到达到最新状态；然后再判断用户的地区是否是上海。而对于每一个用户，我们必须都得进行相同的操作，这样才能将所有地区在上海的用户找出来。而这在性能上是无法忍受的，所以我们在事件持久化的基础上，存储了一份用来查询的信息在数据库里，这就是对于事件溯源的扩展。

而事件溯源则可以在无需查询的时候进行应用。比如我说了要讲还没有讲的Raft协议，其基础是可复制日志。我们可以把每条日志记录都看作一个事件，这个事件通过Raft协议在不同的节点上确定了相同的位置。之后每个节点根据此事件来更新当前的状态，使得集群达到了一致性状态。后续，如果集群崩溃重启，我们可以根据日志事件，一件件回溯并更新状态，直到恢复崩溃之前的状态。只要日志在集群中还存在，系统就能恢复。

当然，假设集群运行了很久，事件有几千上万甚至几十万的时候，那么事件溯源的时间就会非常地久。此时为了使得恢复的速度加快，我们可以使用快照机制来加速。实践的方式很简单，可以设置一个指数N，每N个事件之后，我们将实体的当前状态持久化。之后要恢复的话，则只需要获取最近的一个快照状态，然后读取该快照之后发生的所有事件，并应用到实体上，就可以恢复到之前的状态了。

### CQRS架构里的CAP

了解到Raft和事件溯源的关系，我们自然也能猜到，CQRS也可以支持高可用(HA)，也易于扩展。只需要在实现上选择一种模式即可。如果采用主动-被动模式，则集群中只有一个节点可以接受写入命令，其他节点只需要将写入命令转发给主动节点进行处理即可。而读取的时候则根据需求来选择就好。如果追求节点之间实体的强一致性，则读取请求也可以交给主动节点；而如果不追求强一致性，则每个节点都可以接受读请求。这时请求得到的回复可能会与主动节点短暂不一致，但是最终，集群还是会达到一致的状态。

如果采用多主复制的模式，则我们要从以下三种进行选择：
1. 基于共识的复制
2. 带有冲突探测和解决的方案
3. 无冲突的可复制数据类型(CRDT, Conflict-free Replicated Data Types)

这个我们就提一提，不在这里具体展开了。有兴趣的可以去看看《反应式设计模式》第13章，书里面讲得明明白白。

### 框架实践演示

在前面我提过，在微服务改造的时候，其实最重要的并不是微服务基础的那些注册、发现、治理等，这些基本每个微服务框架都会着重解决；而具体的微服务划分、扩展等议题，则很少有框架直接关注。Java世界中常用的支持CQRS的框架有两个，一个是Axon Framework, 一个是Lagom。这两个是我了解过的。其中Axon是纯Java框架，而Lagom则提供了Java和Scala的API。它们两个都可以用来做微服务改造。Axon相对来说可能与传统Spring世界的结合更好，而Lagom则基于Play和Akka来构建，会有更好的性能和抽象模式。这里我们着重用Lagom来做一下演示。

Lagom是瑞典语，其意思为“刚刚好”。Lagom的设计者对于微服务的期待为，Not too big, not too small, Lagom. 因而他们将`PersistentEntity`作为自己的框架的核心。它是对于CQRS的一种快捷实现方式，基于Akka Persistence和Akka Cluster构建。我们通过以下代码来展示。

假设我们要做一个博客的管理系统。首先我们定义好博客实体的状态：

```scala
import play.api.libs.json._

object BlogState {
  val empty = BlogState(None, published = false)

  implicit val postContentFormat = Json.format[PostContent]

  implicit val format: Format[BlogState] = Json.format[BlogState]
}

final case class BlogState(content: Option[PostContent], published: Boolean) {
  def withBody(body: String): BlogState = {
    content match {
      case Some(c) =>
        copy(content = Some(c.copy(body = body)))
      case None =>
        throw new IllegalStateException("Can't set body without content")
    }
  }

  def isEmpty: Boolean = content.isEmpty
}

final case class PostContent(title: String, body: String)
```

其次，我们定义对于博客的相关操作命令：

```scala
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import akka.Done
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer

sealed trait BlogCommand

object BlogCommand {
  import play.api.libs.json._
  import JsonSerializer.emptySingletonFormat

  implicit val postContentFormat = Json.format[PostContent]

  val serializers = Vector(
    JsonSerializer(Json.format[AddPost]),
    JsonSerializer(Json.format[AddPostDone]),
    JsonSerializer(emptySingletonFormat(GetPost)),
    JsonSerializer(Json.format[ChangeBody]),
    JsonSerializer(emptySingletonFormat(Publish)))
}

final case class AddPost(content: PostContent) extends BlogCommand with ReplyType[AddPostDone]

final case class AddPostDone(postId: String)

case object GetPost extends BlogCommand with ReplyType[PostContent]

final case class ChangeBody(body: String) extends BlogCommand with ReplyType[Done]

case object Publish extends BlogCommand with ReplyType[Done]
```

然后，我们定义命令相关的事件（其中`JsonSerializer`的是为了事件序列化而做的声明; NumShard是为了提高写入性能而设置的分片数量）：

```scala
import com.lightbend.lagom.scaladsl.persistence.AggregateEvent
import com.lightbend.lagom.scaladsl.persistence.AggregateEventShards
import com.lightbend.lagom.scaladsl.persistence.AggregateEventTag
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer

object BlogEvent {
  val NumShards = 20
  val Tag = AggregateEventTag.sharded[BlogEvent](NumShards)

  import play.api.libs.json._
  private implicit val postContentFormat = Json.format[PostContent]

  val serializers = Vector(
    JsonSerializer(Json.format[PostAdded]),
    JsonSerializer(Json.format[BodyChanged]),
    JsonSerializer(Json.format[PostPublished]))
}

sealed trait BlogEvent extends AggregateEvent[BlogEvent] {
  override def aggregateTag: AggregateEventShards[BlogEvent] = BlogEvent.Tag
}

final case class PostAdded(postId: String, content: PostContent) extends BlogEvent

final case class BodyChanged(postId: String, body: String) extends BlogEvent

final case class PostPublished(postId: String) extends BlogEvent
```

这之后，就有了实体的状态变更和处理逻辑：

```scala
import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

final class Post extends PersistentEntity {

  override type Command = BlogCommand
  override type Event = BlogEvent
  override type State = BlogState

  override def initialState: BlogState = BlogState.empty

  override def behavior: Behavior = {
    case state if state.isEmpty  => initial
    case state if !state.isEmpty => postAdded
  }

  private val initial: Actions = {
    Actions()
      .onCommand[AddPost, AddPostDone] {
        case (AddPost(content), ctx, state) =>
          if (content.title == null || content.title.equals("")) {
            ctx.invalidCommand("Title must be defined")
            ctx.done
          } else {
            ctx.thenPersist(PostAdded(entityId, content)) { _ =>
              ctx.reply(AddPostDone(entityId))
            }
          }
      }
      .onEvent {
        case (PostAdded(postId, content), state) =>
          BlogState(Some(content), published = false)
      }
  }

  private val postAdded: Actions = {
    Actions()
      .onCommand[ChangeBody, Done] {
        case (ChangeBody(body), ctx, state) =>
          ctx.thenPersist(BodyChanged(entityId, body))(_ => ctx.reply(Done))
      }
      .onEvent {
        case (BodyChanged(_, body), state) =>
          state.withBody(body)
      }
      .onReadOnlyCommand[GetPost.type, PostContent] {
        case (GetPost, ctx, state) =>
          ctx.reply(state.content.get)
      }
  }

}
```

这边我们看到，`Post`实体根据实体状态的不同有两种行为方式：

1. 博文没有创建的时候，行为是`initial`。在此状态下，`BlogState`内部的`content`是`None`，因此它只接受一种命令，`AddPost`，之后生成事件`PostAdded`，并将其持久化。然后根据此事件将`BlogState`置为`         BlogState(Some(content), published = false)`状态

2. 博文已经创建的时候，行为是`postAdded`。在此状态下，实体只接受两种命令，一个是`ChangeBody`，也就是修改博文内容的命令。此命令产生`BodyChanged`事件并将其持久化，之后根据事件将状态置为拥有新的内容的`state.withBody(body)`；另一个命令是`ReadOnly`的。也就是说该命令并不修改状态，所以在服务的任意节点上都能访问。该命令回复博文内容给请求者。

Lagom中对于`PersistentEntity`的实现采用主动-被动模式。所有写入命令放在`onCommand`里，由节点转发给主动节点消费；读取命令则放在`onReadOnlyCommand`里处理，由接受到请求的节点回复。

在Lagom中，通过如下配置对`PersistentEntity`的相关参数进行修改：

```
lagom.persistence {
  # 分片的数量。它应该10倍于节点的最大数量，以达到最好的性能
  max-number-of-shards = 100
  
  # 多少个事件后存储快照。如果不需要快照功能的话，则设置为off
  snapshot-after = 100

  # 一个持久化实体在多久之后可以被GC。设置为0则可以使得实体可以一直停留在内存中
  passivate-after-idle-timeout = 120s

  
  # 指定实体所在的节点角色。如果为空的话则所有节点都会被使用。若设定了，其他节点仍然可以访问实体
  run-entities-on-role = ""

  # 发送命令到得到回复之间默认的超时时间
  ask-timeout = 5s

  # 实体处理的调度器设置
  dispatcher {
    type = Dispatcher
    executor = "thread-pool-executor"
    thread-pool-executor {
      fixed-pool-size = 16
    }
    throughput = 1
  }
}
```

然后对于查询数据库的写入，则通过另外一个事件处理器来处理：

```scala
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.Done
import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.AggregateEventTag
import com.lightbend.lagom.scaladsl.persistence.EventStreamElement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraReadSide
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import scala.concurrent.Promise

class BlogEventProcessor(session: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[BlogEvent] {

  override def aggregateTags: Set[AggregateEventTag[BlogEvent]] =
    BlogEvent.Tag.allTags

  private def createTable(): Future[Done] =
    session.executeCreateTable("CREATE TABLE IF NOT EXISTS blogsummary ( " +
      "id TEXT, title TEXT, PRIMARY KEY (id))")

  private val writeTitlePromise = Promise[PreparedStatement] // initialized in prepare
  private def writeTitle: Future[PreparedStatement] = writeTitlePromise.future

  private def prepareWriteTitle(): Future[Done] = {
    val f = session.prepare("INSERT INTO blogsummary (id, title) VALUES (?, ?)")
    writeTitlePromise.completeWith(f)
    f.map(_ => Done)
  }

  private def processPostAdded(eventElement: EventStreamElement[PostAdded]): Future[List[BoundStatement]] = {
    writeTitle.map { ps =>
      val bindWriteTitle = ps.bind()
      bindWriteTitle.setString("id", eventElement.event.postId)
      bindWriteTitle.setString("title", eventElement.event.content.title)
      List(bindWriteTitle)
    }
  }

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[BlogEvent] = {
    val builder = readSide.builder[BlogEvent]("blogsummaryoffset")
    builder.setGlobalPrepare(() => createTable())
    builder.setPrepare(tag => prepareWriteTitle())
    builder.setEventHandler[PostAdded](processPostAdded)
    builder.build()
  }
}
```

这一个类只要在应用创建的时候，通过`readSide`实例注册上去，就可以监听`BlogEvent`事件，并根据其来更新数据库(我们这里选的后端是Cassandra)。Processor会用一个表专门来记录消费事件的offset。

读取则需要创建专门的`Repository`来进行查询。相关代码在此不赘述。

上述实现的Java版本可以参考 https://github.com/lagom/lagom/tree/master/docs/manual/java/guide/cluster/code/docs/home/persistence 下的`BlogEvent.java`, `BlogCommand.java`, `BlogState.java`, `Post.java`, `CassandraBlogEventProcessor.java`等。

`PersistentEntity`基于Akka Persistence实现。它利用Akka的集群单例机制来实现的主动-被动模式；基于Event Sourcing来处理实体的状态更新和同步；因为它的状态可以在内存中驻留一段时间，所以在简单情况下，我们甚至不需要分布式缓存；所有的操作除了持久化事件的时候，都是异步无阻塞的，而持久化事件的时候也是顺序写入，所以其性能也不会特别差。

还有一个特别重要的点在于，在这种模式下，我们的架构就是Event-Driven的形式。这就意味着，所有的有影响力的操作都会产生领域事件，而这个事件不仅仅可以在服务内部消化，还可以通过Broker或者消息队列提供给其他服务订阅。事件的消费过程又可以和我们之前讲过的回压机制相关联，这样使得系统之间的交互更加平稳。

当然，上述代码也比普通的CRUD复杂很多。所以在引入CQRS架构的时候，必须要衡量其引入价值是否高于其实现成本。只有在实现成本低于性能提升和可扩展收益的时候，才应该要考虑引入这种架构。

### 关于一致性的再次思考

我们前面讲过，CQRS架构在一致性上面是有一些问题的。因为它只是及时存储事件，查询数据库的更新则相对滞后。但是对于单个实体的查询来说，它的问题就是我们所说的主动-被动模式所存在的问题。

举个例子，假设我们实现了一个`Account`的实体，其中包含着账户余额。我发现里面还有两百块，决定把它提出来去买键盘。操作完成之后，在集群上有一个节点可能尚未更新的时候，我的妻子也发起了提取请求，她准备去买化妆品。这个时候，写入请求会被转发到主动节点，由于主动节点上的状态是最新的，所以她的取钱操作会失败，因为余额不足；而这之后她如果再次刷新，要么能看到新的账户事件（我买了个键盘给自己以后跪）、要么仍然看到老的余额却无法取钱。相对来说，这个过程仍然很安全，只是有稍微的体验降低而已。而这种降低，实际上也只会在系统负载特别严重的时候才会发生。而无论什么样的系统，负载严重的时候体验都会降低，所以在一致性问题上相对来说还可以接受。

实际上，最大的问题不在写入，而在做完一个操作之后，实体可能会在查询中暂时不可见。如果我们用实体的标识直接访问实体的话，实体的状态是最新的状态；而如果我们想在最新实体列表中看到我们刚刚创建的实体的时候，可能需要数秒之后才能看到它出现。因为查询数据库的插入操作是滞后的，所以批量查询的操作也是滞后的。

这个问题，就要根据你的业务做出权衡了。如果很多时候创建了实体之后，用户仍然只查看当前实体，而不使用批量查询在一批实体中寻找刚刚创建的实体，那么刚刚说的问题就不成问题；而如果实体创建之后，系统要求必须能出现在批量列表中，那这个问题就很成问题了。当然，目前这样要求苛刻的系统还是蛮少见的。所以我们暂时可以假定CQRS架构，实际上还是蛮使用的。

### 分布式事务

我们讲到分布式事务的时候，遇到的问题就是一个操作如果涉及到多个微服务的调用、并且要求这个操作是原子的，那应该怎么办。传统的解决办法就是分布式事务。但是在CQRS架构下，使用分布式事务的基础没有了。因为写入的是事件，查询数据库的更新则是异步的、滞后的。那么在这种情况下，应该怎么处理呢？

答案太长，我们下一篇再讲。
