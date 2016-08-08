package cloud.igoldenbeta.hippo.zmq;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMsg;

/**
 * zmq rpc 监听类
 * 
 * @author sl
 *
 */
public class ZmqRpcWorkerListener implements Runnable {
  private ExecutorService executorService = Executors.newFixedThreadPool(10);

  private int zmqPort;

  public ZmqRpcWorkerListener(int port) {
    this.zmqPort = port;
  }

  @Override
  public void run() {
    ZMQ.Context context = ZMQ.context(1);
    ZMQ.Socket fronted = context.socket(ZMQ.ROUTER); // 创建一个router，用于接收client发送过来的请求，以及向client发送处理结果
    ZMQ.Socket backend = context.socket(ZMQ.ROUTER); // 创建一个router，用于向后面的worker发送数据，然后接收处理的结果
    Queue<ZFrame> workers = new LinkedList<>();
    fronted.bind("tcp://*:" + zmqPort); // 监听，等待client的连接
    backend.bind("ipc://back"); // 监听，等待worker连接

    createWorker();
    while (!Thread.currentThread().isInterrupted()) {
      Poller poller = new ZMQ.Poller(2);
      // 创建pollItem
      ZMQ.PollItem fitem = new ZMQ.PollItem(fronted, ZMQ.Poller.POLLIN);
      ZMQ.PollItem bitem = new ZMQ.PollItem(backend, ZMQ.Poller.POLLIN);
      //   Always poll for worker activity on backend
      poller.register(bitem);
      //   Poll front-end only if we have available workers
      if (CollectionUtils.isNotEmpty(workers)) {
        poller.register(fitem);
      }

      poller.poll();
      if (fitem.isReadable()) { // 表示前面有请求发过来了
        ZMsg msg = ZMsg.recvMsg(fitem.getSocket()); // 获取client发送过来的请求，这里router会在实际请求上面套一个连接的标志frame
        ZFrame worker = workers.poll();
        msg.wrap(worker); // 在request前面包装一层，把可以用的worker的标志frame包装上，这样router就会发给相应的worker的连接
        msg.send(backend);
      }
      if (bitem.isReadable()) {
        ZMsg msg = ZMsg.recvMsg(bitem.getSocket());
        ZFrame workerID = msg.unwrap();
        workers.add(workerID);
        ZFrame readyOrAddress = msg.getFirst();
        if ("ready".equals(new String(readyOrAddress.getData()))) {
          msg.destroy();
        } else {
          msg.send(fronted);
        }
      }
    }
    context.close();
  }

  private void createWorker() {
    for (int i = 0; i < 10; i++)
      executorService.execute(new ZmqRpcWorker());
  }


}
