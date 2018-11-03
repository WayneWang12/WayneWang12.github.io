package io.github.waynewang12

import java.math.{MathContext, RoundingMode}

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.{ActorMaterializer, FlowShape, Materializer, scaladsl}
import akka.stream.scaladsl.{Balance, Flow, GraphDSL, Keep, Merge, Sink, Source}
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.Await

class ReactiveStreamDemo extends FreeSpec with Matchers {


  // #pull-pattern-classes
  case class Job(id: Long, input: Int, replyTo: ActorRef)

  case class JobResult(id: Long, report: BigDecimal)

  case class WorkRequest(worker: ActorRef, items: Int)

  // #pull-pattern-classes

  "异步流式处理" - {
    "场景一：速率可控的生产者" in {

      // #pull-pattern-worker
      class Worker(manager: ActorRef) extends Actor {
        private val mc = new MathContext(100, RoundingMode.HALF_EVEN)
        private val plus = BigDecimal(1, mc)
        private val minus = BigDecimal(-1, mc)

        private var requested = 0

        def request(): Unit =
          if (requested < 5) {
            manager ! WorkRequest(self, 10)
            requested += 10
          }

        request()

        def receive: Receive = {
          case Job(id, data, replyTo) ⇒
            requested -= 1
            request()
            val sign = if ((data & 1) == 1) plus else minus
            val result = sign / data
            replyTo ! JobResult(id, result)
        }
      }
      // #pull-pattern-worker

      // #pull-pattern-manager
      class Manager(startTime: Long) extends Actor {

        private val workStream: Iterator[Job] =
          Iterator range(1, 1000000) map (x ⇒ Job(x, x, self))

        private val mc = new MathContext(10000, RoundingMode.HALF_EVEN)
        private var approximation = BigDecimal(0, mc)

        private var outstandingWork = 0

        (1 to 8) foreach (_ ⇒ context.actorOf(Props(new Worker(self))))

        def receive: Receive = {
          case WorkRequest(worker, items) ⇒
            workStream.take(items).foreach { job ⇒
              worker ! job
              outstandingWork += 1
            }
          case JobResult(id, result) ⇒
            approximation = approximation + result
            outstandingWork -= 1
            if (outstandingWork == 0 && workStream.isEmpty) {
              println(s"final result: $approximation, time spent with actor is ${System.currentTimeMillis() - startTime}ms")
              context.system.terminate()
            }
        }
      }
      // #pull-pattern-manager

      // #start-manager
      import scala.concurrent.duration._

      val actorSystem = ActorSystem("test")

      actorSystem.actorOf(Props(new Manager(System.currentTimeMillis())))

      Await.result(actorSystem.whenTerminated, 1.minute)
      // #start-manager
    }

    "akka stream实现一版" in {

      // #pull-pattern-stream
      implicit val actorSystem: ActorSystem = ActorSystem("test")
      implicit val materializer: Materializer = ActorMaterializer()

      val mc = new MathContext(100, RoundingMode.HALF_EVEN)
      val plus = BigDecimal(1, mc)
      val minus = BigDecimal(-1, mc)

      val producer: Source[Int, NotUsed] = Source.fromIterator(() => Iterator.range(1, 1000000))

      val consumers: Flow[Int, BigDecimal, NotUsed] = Flow.fromGraph(GraphDSL.create() { implicit builder =>
        import GraphDSL.Implicits._
        val balancer = builder.add(Balance[Int](8))
        val worker = Flow[Int].map { data =>
          val sign = if ((data & 1) == 1) plus else minus
          val result = sign / data
          result
        }

        val merge = builder.add(Merge[BigDecimal](8))
        val workers = List.fill(8)(worker)

        workers.foreach(f =>
          balancer ~> f ~> merge
        )
        FlowShape(balancer.in, merge.out)
      })

      val resultAggregator = Sink.fold[BigDecimal, BigDecimal](BigDecimal(0, mc))(_ + _)

      val runnableGraph = producer.via(consumers).toMat(resultAggregator)(Keep.right)

      val start = System.currentTimeMillis()
      val futureResult = runnableGraph.run()

      import scala.concurrent.duration._

      val approximation = Await.result(futureResult, 1.minute)

      println(s"final result: $approximation, time spent with stream ${System.currentTimeMillis() - start}ms")
      actorSystem.terminate()
      // #pull-pattern-stream

    }
  }

}
