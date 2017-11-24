package com.github.hippo.test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.hippo.bean.HippoRequest;
import com.github.hippo.callback.CallType;
import com.github.hippo.callback.ICallBack;
import com.github.hippo.chain.ChainThreadLocal;
import com.github.hippo.enums.HippoRequestEnum;
import com.github.hippo.hystrix.HippoCommand;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class HippoHystrixCommandTest {

  public static void main(String[] args)
      throws InstantiationException, IllegalAccessException, InterruptedException {

    Observable.interval(5, TimeUnit.SECONDS).window(3, TimeUnit.SECONDS)
        .subscribe(new Observer<Observable<Long>>() {
          @Override
          public void onCompleted() {
            System.out.println("------>onCompleted()");
          }

          @Override
          public void onError(Throwable e) {
            System.out.println("------>onError()" + e);
          }

          @Override
          public void onNext(Observable<Long> integerObservable) {
            System.out.println("------->onNext()");
            integerObservable.subscribe(new Action1<Long>() {
              @Override
              public void call(Long integer) {
                System.out.println("------>call():" + integer);
              }
            });
          }
        });

    Thread.sleep(10000 * 10000);
    //
    // HippoHystrixCommandTest hippoHystrixCommandTest = new HippoHystrixCommandTest();
    // HippoCommand hippoCommand = hippoHystrixCommandTest.builderHippoCommand();
    // try {
    //
    // System.out.println(hippoCommand.execute());
    // } catch (Exception e) {
    // // TODO: handle exception
    // }
    //
    // System.out.println(hippoCommand.execute());
  }

  private HippoRequest buildHippoRequest() {
    HippoRequest request = new HippoRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setChainId(ChainThreadLocal.INSTANCE.getChainId());
    request.setChainOrder(ChainThreadLocal.INSTANCE.getChainOrder());
    request.setRequestType(HippoRequestEnum.RPC.getType());
    request.setClassName("com.holyshared.issue.service.PublishService");
    request.setMethodName("isPublished");
    request.setCallType(CallType.ONEWAY);
    request.setiCallBack(new ICallBack() {
      @Override
      public void onSuccess(Object result) {
        System.out.println("调用成功");

      }

      @Override
      public void onFailure(Throwable e) {
        System.out.println("调用失败");

      }
    });
    Class<?>[] classes = new Class<?>[1];
    Object[] objects = new Object[1];
    classes[0] = String.class;
    objects[0] = "http://www.baidu.com";
    request.setParameterTypes(classes);
    request.setParameters(objects);
    String serviceName = "holyshared.issue.service";
    request.setServiceName(serviceName);


    ChainThreadLocal.INSTANCE.clearTL();
    return request;

  }

  private HippoCommand builderHippoCommand() throws InstantiationException, IllegalAccessException {
    return new HippoCommand(buildHippoRequest(), 3000, 1, true, 10, Void.class, false);
  }


}
