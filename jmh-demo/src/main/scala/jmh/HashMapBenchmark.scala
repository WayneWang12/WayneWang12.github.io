package jmh

import java.util.concurrent.TimeUnit

import com.koloboke.collect.impl.hash.MutableLHashParallelKVLongLongMapGO
import com.madhukaraphatak.sizeof.SizeEstimator
import gnu.trove.map.hash.TLongLongHashMap
import org.openjdk.jmh.annotations._

import scala.util.Random

object HashMapBenchmark {
  final val OperationsPerInvocation = 64000000 * 2
}

trait LongLongOp {
  def put(key: Long, value: Long): Long

  def get(key: Long): Long
}

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class HashMapBenchmark {

  import HashMapBenchmark._

  val random = new Random(42)
  val MapSize = 64000000

  val testSet: List[(Long, Long)] = List.range(0, MapSize).map { _ =>
    val key = random.nextLong()
    val value = random.nextLong()
    key -> value
  }

  def testSetTraverse(hashMap: LongLongOp) = {
    testSet.foreach { case (k, v) =>
      hashMap.put(k, v)
    }
    testSet.foreach { case (k, v) =>
      val readValue = hashMap.get(k)
      assert(readValue == v)
    }
  }

  def printlnObjectSize(message: String, obj: AnyRef) = {
    val size = SizeEstimator.estimate(obj)
    val sizeInGB = size.toDouble / 1024 / 1024 / 1024
    println(s"$message size is ${sizeInGB}GB")
  }

  @Benchmark
  @OperationsPerInvocation(OperationsPerInvocation)
  def testEclipseCollection() = {

    import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap

    val map = new LongLongHashMap(MapSize)
    testSet.foreach { case (k, v) =>
      map.put(k, v)
    }
    testSet.foreach { case (k, v) =>
      val readValue = map.get(k)
      assert(readValue == v)
    }
    printlnObjectSize("Eclipse LongLongHashMap", map)
  }

  @Benchmark
  @OperationsPerInvocation(OperationsPerInvocation)
  def testHppc() = {

    import com.carrotsearch.hppc.LongLongHashMap

    val map = new LongLongHashMap(MapSize, 0.99) with LongLongOp
    testSetTraverse(map)
    printlnObjectSize("hppc LongLongHashMap", map)
  }

  @Benchmark
  @OperationsPerInvocation(OperationsPerInvocation)
  def testKoloboke() = {
    val map = new MutableLHashParallelKVLongLongMapGO() with LongLongOp
    testSetTraverse(map)
    printlnObjectSize("Koloboke", map)
  }

  @Benchmark
  @OperationsPerInvocation(OperationsPerInvocation)
  def testTrove() = {
    val map = new TLongLongHashMap(MapSize, 1.0f) with LongLongOp
    testSetTraverse(map)
    printlnObjectSize("Trove LongLongMap", map)
  }


}
