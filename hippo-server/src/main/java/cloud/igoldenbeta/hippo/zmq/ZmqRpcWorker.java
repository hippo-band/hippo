package cloud.igoldenbeta.hippo.zmq;

import java.util.UUID;

import org.zeromq.ZMQ;

/**
 * worker 处理业务请求
 * 
 * @author sl
 *
 */
public class ZmqRpcWorker implements Runnable {
  @Override
  public void run() {
    ZMQ.Context context = ZMQ.context(1);
    ZMQ.Socket socket = context.socket(ZMQ.REQ);
    socket.setIdentity(UUID.randomUUID().toString().getBytes());
    socket.connect("ipc://back"); // 连接，用于获取要处理的请求，并发送回去处理结果
    socket.send("ready".getBytes()); // 发送ready，表示当前可用
    while (true) {
      ZmqRpcWorkerProcess zmqRpcWorkerProcess = new ZmqRpcWorkerProcess();
      zmqRpcWorkerProcess.process(socket);
    }
  }
}
