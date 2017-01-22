package com.github.hippo.hystrix;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

/*
 * @author wangjian55
 * 
 */
public class HystrixHelloCommand extends HystrixCommand<String> {

	private final String name;

	public HystrixHelloCommand(String name) {
		// group key
		// commandkey
		// ThreadPoolKey 资源隔离
		super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("myGroup")).
		// 配置依赖超时时间
				andCommandPropertiesDefaults(
						HystrixCommandProperties.Setter().withExecutionIsolationThreadTimeoutInMilliseconds(500)));
		this.name = name;
	}

	@Override
	protected String run() throws Exception {
		// 依赖逻辑封装在 run 里面
		Thread.sleep(10000000L);
		return "hello " + name + " thread " + Thread.currentThread().getName();
	}

	// 重载回调超时策略
	// 熔断策略
	@Override
	protected String getFallback() {
		return "时间延迟 已经被拒绝";
	}

	public static void main(String[] args) throws Exception {
		// 命令封装请求逻辑
		HystrixHelloCommand helloCommand = new HystrixHelloCommand(" hippo 同步调用 ");
		// 同步调用 相当于 helloCommand.queue().execute()
		String result = helloCommand.execute();
		System.out.println("result----" + result);
		// System.out.println("结果是+" + result);
		// helloCommand = new HystrixHelloCommand(" hippo 异步调用 ");
		// Future<String> future = helloCommand.queue();
		// // 超时报警 回调
		// result = future.get(1000, TimeUnit.MILLISECONDS);
		// System.out.println("result:" + result);
		// System.out.println("mainthread +" +
		// Thread.currentThread().getName());

	}
}
