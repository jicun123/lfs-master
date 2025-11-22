package cn.lxinet.lfs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;

/**
 * 线程池配置
 *
 * @author zcx
 * @date 2023/11/20
 */
@EnableAsync
@Configuration
public class ThreadPoolConfig {
	@Bean(name = "transcodeTaskExecutor")
	public TaskExecutor transcodeTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 设置核心线程数
		executor.setCorePoolSize(4);
		// 设置最大线程数，只有队列容量满了之后才会根据这个值创建新线程
		executor.setMaxPoolSize(8);
		// 设置队列容量
		executor.setQueueCapacity(100);
		// 设置线程活跃时间（秒）
		executor.setKeepAliveSeconds(60);
		// 设置默认线程名称
		executor.setThreadNamePrefix("transcode-thread-");
		// 设置拒绝策略
		executor.setRejectedExecutionHandler(rejectedExecutionHandler());
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(60);
		executor.initialize();
		return executor;
	}

	/**
	 * 这个策略就是忽略缓冲队列限制，继续往里边塞，否则可能消息丢失
	 */
	public RejectedExecutionHandler rejectedExecutionHandler() {
		return (r, executor) -> {
			try {
				executor.getQueue().put(r);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
	}
}
