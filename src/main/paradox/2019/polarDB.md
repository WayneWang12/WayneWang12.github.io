---
withComments: true
pageId: polarDB
withAds: true
---

第一届阿里云PolarDB数据库性能大赛"RDP飞起来"队伍攻略总结
---

### 前言

持续好几个月的第一届阿里云PolarDB性能挑战赛终于圆满结束了。我所在的“RDP飞起来”团队获得了初赛第三、复赛第六的名次，最终拿到了赛事的季军。在参加比赛的这段时间内，我学到了很多东西，认识了很多人，也有幸去北京阿里中心转了一圈，聆听各位大佬教导，受益匪浅。为了让其他同样对阿里比赛有兴趣的朋友可以有同样的机会，这里将我这段时间的比赛心得做一个分享，希望能对后续想参加比赛的人有所帮助。

### 提纲

不同于其他选手的分享方式，我将会主要讲述一下自己的比赛历程，并尽量从简单到深入来介绍一下我的解题思路，希望大家能身临其境地感受到我在比赛中的迭代方式，从而有更多体悟。大致提纲如下:

1. 赛题解析
2. 初赛Java版本
3. 初赛、复赛CPP版本
4. Range 实现
5. 总结

### 赛题解析

赛题解析我之前做过一次简单版本的。这里再次重复一下。本次比赛其实是个简化版本的KV。赛事组织者为了简化题目，让更多的人有机会参与进来，并获得成绩，所以将我们常见的KV场景固化为 Key 的大小是8字节，Value 的大小是4KB。然后评测的标准是，用64个线程调用你所开发的程序，每个线程写入100万对键值，然后清理page cache；再用64个线程调用，每个线程随机读取100万键值，之后清理page cache；再用64个线程调用，每个线程按照升序遍历全量键值（也就是6400万）两次，之后清理page cache。这三个阶段完成之后，获得总时间，用时越少越好。

其中，运行环境使用 CGroup 对 C++ 程序做了一个2G的限定，对 Java 程序做了3G的限定，如果超出的话，程序就会被杀掉。也就是说，我们可用的内存有限，并且要在这个内存上完成对大约250G的数据的读写，以及全局遍历。另外还有一个特殊的要求，就是在检测过程中，评测程序会通过`kill -9`命令来杀掉 kv 进程，但是要求这个动作不会影响 kv 的运行。所以我们也必须考虑如何应付突然的程序崩溃。

### 初赛 Java 版本

在比赛最开始的时候，我写了一个[初赛攻略](polarDB.md)，其中宣称，无论百万队列还是千万键值，我都是单文件、顺序写、跳读，一把梭来着。在初赛最开始的时候，我就按照这个思路实现了一个版本。用Java实现起来其实蛮简单的，我将负责读写文件的类命名为`KeyValueLog`，在这个`KeyValueLog`中会创建一个 Key 文件，一个 Value 文件。初赛没有Range阶段的评测，所以构建索引直接使用的`HashMap`。`KeyValueLog`在各个阶段的逻辑如下：

#### 写入

1. Value写入FileChannel中
2. Key写入Key文件映射出来的`MappedByteBuffer`，利用脏页回写的方式来保证`kill -9`不会丢失数据

这个过程中，如果`kill -9`发生在步骤1和2之间，则可以认为该次操作失败。而我们重建索引是由Key文件来进行的，因为Key没有写入，所以最新的FilePosition不在已经写入文件的位置，所以不会有脏数据。重新运行的时候，写入位置仍然会覆盖上次写入Value的值。

如果`kill -9`发生在步骤2之后，则因为Value和Key已经写入，操作系统会自动帮助我们将脏页回写进文件。再次构建索引的时候，Key和Value都存在，所以数据没有丢失。

#### 读取

由于Key和Value分别在两个文件中一一对应地存储，而且他们都是固定大小，所以读取N个8字节的key和N个4KB的Value之后，我们可以确定第N个Key对应第N个Value。而对应的Value的文件位置，则可以通过N * 4096来确认。为了省一些空间，所以我们构建索引的时候，只使用Int类型，Key为Long类型，则一条索引记录12字节就可以解决。我们将索引信息存入`Map[Long, Int]`中，当读取请求到来的时候，我们将Key转化为Long类型，然后在索引中找到其位置N，之后将N乘以4096，再用这个值去Value文件中读取出来Value，将其交回评测程序。

#### 索引构建

在用Java构建索引的时候，最 naive 的想法就是使用`HashMap`。但是这里需要注意，6400万 * 12字节，在`HashMap`中不是等于768MB的内存，而是要到3G~4G。其开销计算可以按照如下公式

    32 * SIZE + 4 * CAPACITY

6400W的Long和Int对，假设装填因子（load_factor)为1.0，则需要768M + 2G + 256M ≈ 3G。所以，我们在选择索引的数据结构的时候，需要使用特殊的类型。我所选择的具体类型可以参见我之前写的[应用JMH测试大型HashMap的性能](https://waynewang12.me/2018/jmh.html)。最终通过JMH进行性能检验之后，选择了[hppc](https://labs.carrotsearch.com/hppc.html)。hppc所需要额外内存大致为 `8 * CAPACITY`, 计算下来为768M + 256M ≈ 1G

#### 单文件版本

我使用了一个`KeyValueLog`来构建了程序。最终我得到了第一次的结果为669秒。但是这个结果明显没有发挥硬件的最大威力。

#### 64文件版本

为了更好利用硬件，我使用多个`KeyValueLog`来完成读写操作。例如，创建64个`KeyValueLog`，每个Log都分配一个id，然后当键值对到来时，使用Key的首字节去余64，之后选择到对应的`KeyValueLog`来执行写入操作。读的时候也一样，先通过Key来做路由操作，然后在进行读操作。做了这个变更之后，线上成绩一下就到了240s。

### 插播一下对于文件 I/O 的介绍

实际上，我们通常进行的文件 I/O 可以分为两种:

1. Buffer I/O
2. Direct I/O

#### Buffer I/O

缓存 I/O 又被称作标准 I/O，大多数文件系统的默认 I/O 操作都是缓存 I/O。在 Linux 的缓存 I/O 机制中，操作系统会将 I/O 的数据缓存在文件系统的页缓存（ page cache ）中，也就是说，数据会先被拷贝到操作系统内核的缓冲区中，然后才会从操作系统内核的缓冲区拷贝到应用程序的地址空间。缓存 I/O 有以下这些优点：

1. 缓存 I/O 使用了操作系统内核缓冲区，在一定程度上分离了应用程序空间和实际的物理设备。
2. 缓存 I/O 可以减少读盘的次数，从而提高性能。

Buffer I/O 中有一类特别的操作叫做内存映射文件，它的不同点在于，中间会减少一层数据从用户地址空间到操作系统地址空间的复制开销。使用`mmap`函数的时候，会在当前进程的用户地址空间中开辟一块内存，这块内存与系统的文件进行映射。对其的读取和写入，会转化为对相应文件的操作。并且，在进程退出的时候，会将变化的内容（脏页）自动回写到对应的文件里面。我们在此次比赛中利用相关特性，来进行对`kill -9`的处理。

#### Direct I/O

凡是通过直接 I/O 方式进行数据传输，数据均直接在用户地址空间的缓冲区和磁盘之间直接进行传输，完全不需要页缓存的支持。操作系统层提供的缓存往往会使应用程序在读写数据的时候获得更好的性能，但是对于某些特殊的应用程序，比如说数据库管理系统这类应用，他们更倾向于选择他们自己的缓存机制，因为数据库管理系统往往比操作系统更了解数据库中存放的数据，数据库管理系统可以提供一种更加有效的缓存机制来提高数据库中数据的存取性能。

从上述描述可以看出，如果我们对自己的数据更加了解，那么使用 Direct I/O 的方式会得到更高的性能，因为我们的数据是直接从用户地址空间复制到磁盘上去的，其中会少一层从用户地址空间到操作系统地址空间的复制。所以我们想着要使用 Direct I/O 的方式来完成我们的应用。

### Java中的 Direct I/O

Java 中目前没有对 Direct I/O的直接支持，其中主要原因可能是因为不是所有操作系统都支持，而 JVM 必须保证跨平台性，所以 Java 中只有 Buffer I/O。但是可以通过自己手撸 JNA 的方式来实现其支持。JNA 是 Java 中一种用来与本地共享库进行互操作的便捷方式，使用它可以直接调用操作系统本地库。这里我们的系统环境是 Linux，而 Linux 中对 Direct I/O 的支持是通过`pread`、`pwrite`，及其标志位`O_DIRECT`来实现的。所以此处我们实现一下对这几个函数的封装。具体代码如下：

```java
/** Simple example of JNA interface mapping and usage. */
public class DirectIOLib {
    
    static {
        try {
            if (!Platform.isLinux()) {
                logger.warn("Not running Linux, jaydio support disabled");
            } else {
                Native.register(Platform.C_LIBRARY_NAME);
            }
        } catch (Throwable e) {
            logger.warn("Unable to register libc at class load time: " + e.getMessage(), e);
        }
    }
    
    static native int posix_memalign(PointerByReference memptr, NativeLong alignment, NativeLong size);
    public static native void free(Pointer ptr);
    private static native int open(String pathname, int flags, int mode);
    public native int close(int fd); // musn't forget to do this
    public static native int ftruncate(int fd, long length);
    private static native NativeLong pwrite(int fd, Pointer buf, NativeLong count, NativeLong offset);
    private static native NativeLong pread(int fd, Pointer buf, NativeLong count, NativeLong offset);
    private static native int getpagesize();
   private static native String strerror(int errnum);
}
```

其中需要的映射函数基本如上。在进行操作的时候，我们可以创建一个 Java nio 中的`DirectByteBuffer`对象，然后将要写入的数据直接写入这个对象中，然后再通过如下方式，将数据写入文件中：

```java
public int pwrite(int fd, ByteBuffer buf, long offset) throws IOException {
    final int start = buf.position();
    assert start == blockStart(start);
    final int toWrite = blockEnd(buf.limit()) - start;

    final long address = ((DirectBuffer) buf).address();
    Pointer pointer = new Pointer(address);

    int n = pwrite(fd, pointer.share(start), new NativeLong(toWrite), new NativeLong(offset)).intValue();
    if (n < 0) {
        throw new IOException("error writing file at offset " + offset + ": " + getLastError());
    }
    return n;
}
```

读取的时候，则通过filePosition将数据读入`DirectByteBuffer`中：

```java
public int pread(int fd, ByteBuffer buf, long offset) throws IOException {
    buf.clear(); // so that we read an entire buffer
    final long address = ((DirectBuffer) buf).address();
    Pointer pointer = new Pointer(address);
    int n = pread(fd, pointer, new NativeLong(buf.capacity()), new NativeLong(offset)).intValue();
    if (n < 0) {
        throw new IOException("error reading file at offset " + offset + ": " + getLastError());
    }
    return n;
}
```

通过这样的操作，能够使得 Java 在比赛中获得较大的性能提升。我在实践中得到的第一阶段写入速度为115s，第二阶段随机读的速度为106秒。

在初赛中期就开始进行 Java 到 C++ 的改造。其中 Java 代码和 C++ 基本上是1比1照搬的。但是结果却大相径庭。其中提升总结如下：

1. Java -> C++: 240s -> 228s
2. 使用 Direct IO: 228 -> 224s
3. 使用 Merge IO: 224 -> 222s

最终，在运气好的情况下，获得了 221.6s的成绩，在初赛排名第三。

### Merge IO

上面提到的 Merge IO 需要讲一下。一般来说，SSD的读写性能在数据量大小为4K以上时才能发挥最大效率。因为其每次读写都是以4KB为单位的。也就是说，如果写入在4K以下，其实所耗费的时间和4K一样，造成了磁盘的IO能力的浪费。

而且实践中，也并非4KB为最佳大小。在本次比赛中，16KB为单位才能发挥傲腾的最大读写速度（这个是我们的测算，实际上，第一名对如何发挥磁盘最大IOPS有更科学的测量和原理说明，大家可以看一下其分享，地址在[POLARDB数据库性能大赛总决赛冠军比赛攻略_Rapids队
](https://tianchi.aliyun.com/notebook-ai/detail?spm=5176.12282027.0.0.1fb51580BQjt8U&postId=45111)）。但是因为有`kill -9`机制的检验，我们没法在内存中直接维护一块16KB的buffer，因为如果这16KB没有写入，则会发生数据丢失。

为了解决这个问题，我们开辟了一块`mmap`来维护这块Buffer。写入的时候，会先把mmap出来的buffer填满，然后再`pwrite`到文件里面去。
```
void putValue(const PolarString &key, const PolarString &value, uint64_t key64) {
    pthread_mutex_lock(&_mutex);
    
    int segmentPosition = currentKeyPosition % SegmentCount;
    memcpy(static_cast<char *>(segmentBuffer) + (segmentPosition * 4096), value.data(), 4096);
    
    if (segmentPosition == SegmentCount - 1) {
        auto valueFilePosition = (currentKeyPosition - segmentPosition) * ValueSize;
        pwrite64(this->valueFd, segmentBuffer, SegmentSize, valueFilePosition + initialOffset);
    }

    auto *key64Buffer = reinterpret_cast<uint64_t *>(keyBuffer);
    key64Buffer[currentKeyPosition] = key64;
    currentKeyPosition++;
    
    pthread_mutex_unlock(&_mutex);
}
```

如上，我们就完成了对Merge IO的改写。这次改动之后，初赛成绩就从224s提升到了222s，然后多提交几次抖了抖，221.6，最终获得了第三名。

### Range 实现

复赛比初赛多了一个环节，就是要64个线程，每个线程按照增序遍历两次全量数据。在这里，我们就必须要充分利用剩余的的 1G 多内存了。我们希望借助这 1G 内存来做缓存，并且做到，读一次全量数据进入这 1G 缓存，然后让所有64个线程都上车一起遍历。如此，就只需要全量读取两次数据，就能满足64 * 2 = 128次全量遍历的需求。那么，具体应该怎么做呢?

#### 1. 选定数据读取方式

事实上，即使是傲腾这样随机读速度达到2G多的给力磁盘，也是顺序读速度远大于随机读速度。所以在全量的情况下，要做到最快的速度，最好就是顺序读所有数据，然后在内存里面进行随机读。因为我们之前按照字节余64的方式将 KV 路由到了对应的文件里面，为了在顺序 range 的过程方便我们访问数据，这里我们将64个文件扩展到512个文件，使得每个文件是512mb，并且每对 KV 都按照 Key 的前9位路由到对应的文件里面。这样，对于文件来说，文件编号大的文件，其中的 Value 对应的 Key 也一定大于编号小的文件。但是在文件内部，kv 则不一定了。但是这个时候因为文件大小只有512mb了，所以我们可以把每个文件的内容都读入缓存，然后在内存里面随机读取。

如此我们定下来我们的读取方式：

1. 顺序读入大片文件进入内存；
2. 内存内随机读文件数据。

#### 2. 选定要竞争的资源和临界区

其实文件的大小我们可以自己选定的。只要在1G以内（索引信息会占大约七八百兆），按照文件数目，可以选取256、512、1024乃至2048。在初期我选的是512个文件，这是因为这样的话，我的缓存就能分为两页，然后我就可以用单个缓存读取线程一直读取文件数据进入内存，当读完一页了以后，就可以将这一页缓存交给访问线程进行访问，而缓存线程可以继续读下一页，并在读完之后，又可以回头将文件读入刚刚那页被访问完成的内存，如此反复交替，则可以使得访问线程跟随缓存线程，顺序遍历完所有的数据。

如此，很明显，在我们这里，内存缓存是稀缺资源。它将被由缓存线程和访问线程，进行竞争。而访问线程因为有 N 个，所以我们必须得设定好条件，让缓存线程和访问线程都能在恰当的时机下访问缓存。为了完成这一目标，我将缓存设置如下：

```
class KeyLogCache {
    public:
        KeyLogCache() : keyLogId(-1), inUsed(0) {
            pthread_mutex_init(&mutex, NULL);
            pthread_cond_init(&condition, NULL);
            posix_memalign(&cache, getpagesize(), PerCacheSize);
        }

        ~KeyLogCache() {
            pthread_mutex_destroy(&mutex);
            pthread_cond_destroy(&condition);
            free(cache);
        }

        int keyLogId;
        int inUsed;
        void *cache;
        pthread_mutex_t mutex;
        pthread_cond_t condition;
}
```
`keyLogId`是当前缓存对应的`KeyValueLog`的id，当缓存线程要往里面读取数据的时候，会先抢到锁，然后将`keyLogId`置成当前要读的文件的ID，然后往`cache`里面写入数据。缓存线程的逻辑代码如下：
```
void rangeCache(KeyValueLog **pKeyValueLogs, int size) {
    auto t = size * 2;  //size在这里等于512， 然后因为range两次，为了简略，我们直接乘以2
    for (int i = 0; i < t; i++) {
        auto keyLogId = i % size;
        auto cacheIndex = i % cacheCount;
        auto cache = caches + cacheIndex;
        pthread_mutex_lock(&(cache->mutex));
        while (cache->inUsed > 0) {
            pthread_cond_wait(&cache->condition, &cache->mutex);
        }
        pread(pKeyValueLogs[keyLogId]->valueFd, cache->cache, PerCacheSize, 0);
        cache->inUsed = 1;
        cache->keyLogId = keyLogId;
        fprintf(stderr, "caching keyLogId %d\n", keyLogId);
        pthread_mutex_unlock(&cache->mutex);
    }
}
```

如果缓存线程在获取到`keyLogCache`之后，发现这个cache正在被使用，则其将会让出锁，并且等待条件变量`condition`来将它唤醒。

访问线程来访问的时候，每当一个访问线程进入了缓存页，它就会将缓存页的引用计数加一，访问完成之后，则退出，并且退出引用。访问线程的访问过程如下：
```
for (int id = 0; id < ShardingSize; id++) {
    auto cache = cacheManager.getKeyLogCache(id);
    while(cache == NULL) {
        cache = cacheManager.getKeyLogCache(id);
        usleep(1);
    }
    keyValueLogs[id]->range(visitor, cache);
    cache->releaseReference();
}
```
先去试图从Manager那里获取Cache，如果没有，则`usleep`让出cpu时间片；后续再试图获取。当获取到了以后，就顺序访问 KV ，完成之后就释放引用。`releaseReference`的代码如下：
```
void releaseReference() {
        pthread_mutex_lock(&mutex);
        inUsed--;
        if( inUsed == 1) {
            inUsed = 0;
            pthread_cond_broadcast(&condition);
        }
        pthread_mutex_unlock(&mutex);
    }
};
```
注意，当`inUsed == 1`的时候，就会将其置零，然后唤醒因为占用而等待的缓存线程，让其继续前进。

这样一波改造之后，我们得出了422s的成绩。这个成绩就算是最后比赛结束的时候，也是在前20的。

#### 多线程读取缓存

初赛的经验告诉了我们，傲腾的盘多线程读取的话，会有更快的速度。那么，几个线程， 每个线程读多大，会更快呢？经过思考以后，我希望能将程序改写成拥有这样的能力：

1. 缓存可以分为 N 页；
2. 每页可以分为 M 段；
3. 缓存线程可以为 L 个；
4. 可以通过配置进行N、M、L的分配，以使它们协同工作。

之后，我在`KeyLogCache`里面添加了如下变量：

```
int keyLogId;          //当前Cache对应的LogId
int inUsed;            //当前正在访问对应缓存的访问线程的数量
int completedPart;     //当前Cache已经读完的部分
int segmentCount;      //当前Cache能分成的段的数目
int partToFill;        //当前需要被填充的段的id；
size_t  perCacheSize;  //每页缓存的大小；
void *cache;           //缓存
pthread_mutex_t mutex; //缓存锁
```

之后，处理逻辑，对于缓存线程，假设其正在读取第 K 页Log, 则其行为可以描述如下：

1. 竞争获取当前第 K % N 页缓存
2. 若缓存id不为K, 且`inUse`不为0，则循环等待；
3. 若缓存id不为K, 且`inUse`为0，则将`keyLogId`置为K,
4. 若缓存id为K, 且`partToFill < segmentCount`为0，则获取`partToFill`放入临时变量`m`，并将`partToFill`加一
5. 从文件中读取第`m`段内容进入内存；
6. 重复4至当前步骤，直到`partToFill == segmentCount`的时候；
7. 令 `K = K + 1`, 重复1至当前，直到 `K` 大于等于当前总文件数目。

如此，我们就完成了对于缓存线程的改造。

这之后，我分别试过了 L、M、N 的不同排列组合，最终发现当 N = 8, M = 4, L = 2的时候效果最好。这之后，我的成绩就到了416s，进入了前十。

#### 最大化IO

为了使得range操作的IO最大化，我使用了一个在 Netty和 Akka 中都比较常见的提升性能操作，绑核。

其实说实话，一个线程如果在做IO的时候，不应该令其完全占用核心，因为这个时候CPU的时间片被IO操作所阻塞，直接被浪费掉了。但是在这次比赛里面，由于CPU有64个核心，CPU资源过剩，所以我决定将两个缓存线程直接绑定到特定核心上进行操作。相关代码很简单，如下列出：

```
cpu_set_t mask;
CPU_ZERO(&mask);
CPU_SET(threadId, &mask);
int rc = pthread_setaffinity_np(pthread_self(), sizeof(mask), &mask);

if (rc != 0) {
    fprintf(stderr, "Error calling pthread_setaffinity_np: %d\n ", rc);
}
```

这个段加上去之后，提交运行，代码就跑到了415s，然后抖一下，最终获得了414.41的成绩。

### 总结

在这次比赛中，我经历了从Java到Cpp，从Buffer I/O 到 Direct I/O, 从单线程Range到多线程M段读取的变迁。整个比赛过程中，对文件I/O有了更加深入的认识，实践了自己之前学习的多线程知识，见识到了诸如傲腾这样的新硬件给软件带来的红利，还认识了很多新朋友和大牛。在此不得不再次感谢阿里云悉心组织了比赛。

另外想对小伙伴们说一件事情，就是参加这样的比赛真的能学到很多东西，而且还有真金实银的奖励。特别推荐大家可以参赛，希望下届比赛开始的时候，能有更多的小伙伴一起，一起感受追求极致性能来编程带来的快感。

