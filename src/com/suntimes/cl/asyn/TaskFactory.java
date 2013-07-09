package com.suntimes.cl.asyn;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import android.os.Handler;
import android.os.Looper;

import com.suntimes.cl.util.CLLog;

public final class TaskFactory{
	public static final String TAG = "TaskFactory";
	public static final int THREAD_POOL_SIZE = 20;
	public static final Handler uiHandler = new Handler(Looper.getMainLooper());
	private static TaskFactory instance;
	private static ExecutorService cachedThreadPool;
	/**线程安全*/
	private static final Hashtable<BgTask, Future<?>> BG_TASK_TABLE = new Hashtable<BgTask, Future<?>>();
	
	private TaskFactory(){
		cachedThreadPool = Executors.newCachedThreadPool(new ThreadFactory() {
			
			@Override
			public Thread newThread(final Runnable r) {
				Thread t = new Thread(r);
				t.setName(TAG + ":" + BG_TASK_TABLE.size());
				t.setDaemon(true);
				return t;
			}
		});
	}
	
	public static final TaskFactory getInstance() {
		if (instance == null) {
			synchronized (TaskFactory.class) {
				if (instance == null) {
					instance = new TaskFactory();
				}
			}
		}
		return instance;
	}
	
	public static final void destory(){
		synchronized(TaskFactory.class){
			cleanTaskPool();
			cachedThreadPool.shutdownNow();
			instance = null;
			cachedThreadPool = null;
		}
	}
	
	public void run(BgTask bgTask){
		run(bgTask, 0, true);
	}

	public void run(BgTask bgTask, int timeout){
		run(bgTask, timeout, true);
	}

	/**
	 * 执行后台任务
	 * @param bgTask
	 * @param timeout 毫秒
	 * @param mayInterruptIfRunning 是否打断已存在的任务(true 打断存在的任务并运行新任务，false 不打断已存在的任务，不运行新的任务)
	 */
	public void run(final BgTask bgTask, int timeout, boolean mayInterruptIfRunning){
		if(Looper.myLooper() != Looper.getMainLooper()){
			throw new RuntimeException("must be called in UI thread!");
		}
		if(bgTask == null){
			return;
		}
		
		if(mayInterruptIfRunning){
			cancel(bgTask);
		}

		Future<?> exists = getFuture(bgTask);
		if(exists == null){
			final Future<?> future = cachedThreadPool.submit(bgTask);
			BG_TASK_TABLE.put(bgTask,future);
			if(timeout > 0){
				uiHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						cancel(bgTask);
					}
				}, timeout);
			}
			CLLog.d(TAG, "added task="+bgTask);
		}else{
			CLLog.d(TAG, "discard task="+bgTask);
			
		}
	}

	public boolean cancel(BgTask bgTask){
		boolean result = false;
		Future<?> f = getFuture(bgTask);
		if(f != null){
			result = f.cancel(true);
			CLLog.d(TAG, "cancel task: "+bgTask+"; result="+result);
		}
		return result;
	}
	
	/**
	 * 获取正在运行的BgTask的Future
	 * @param bgTask 
	 * @return 提交后的Future
	 */
	private Future<?> getFuture(BgTask bgTask){
		LinkedList<BgTask> beRemoved = new LinkedList<BgTask>();
		Set<BgTask> keySet = BG_TASK_TABLE.keySet();
		for(BgTask key : keySet){
			Future<?> future = BG_TASK_TABLE.get(key);
			if(future.isDone()){
				beRemoved.add(key);
			}
		}
		for(BgTask task : beRemoved){
			BG_TASK_TABLE.remove(task);
		}
		return BG_TASK_TABLE.get(bgTask);
	}
	
	private static void cleanTaskPool(){
		if(BG_TASK_TABLE == null){
			return;
		}
		Set<BgTask> keySet = BG_TASK_TABLE.keySet();
		for(BgTask key : keySet){
			Future<?> future = BG_TASK_TABLE.get(key);
			future.cancel(true);
		}
		BG_TASK_TABLE.clear();
	}
	
	public static final void markUIRunning(){
		if(Looper.myLooper() != Looper.getMainLooper()){
			throw new RuntimeException("must run on UI thread!");
		}
	}
	
	public static final void markBgRunning(){
		if(Looper.myLooper() == Looper.getMainLooper()){
			throw new RuntimeException("must run on Background thread!");
		}
	}
	
//	public static void printTaskPool(String prefix){
//		Set<BgTask> keySet = BG_TASK_TABLE.keySet();
//		StringBuffer strb = new StringBuffer();
//		for(BgTask key : keySet){
//			strb.append(" ");
//			strb.append(BG_TASK_TABLE.get(key).isDone());
//		}
//		Log.e(TAG, prefix+": pool : "+strb);
//	}
	
	public static class TaskBuilder{
		public static BgTask buildBgTask(Runnable runnable){
			return new BgTask(System.currentTimeMillis(), runnable);
		}
		public static BgTask buildBgTask(long taskId, Runnable runnable){
			return new BgTask(taskId,runnable);
		}
	}
	
	public static class BgTask implements Runnable{
		private long id;
		private Runnable runnable;
		
		public BgTask(long id,Runnable runnable){
			if(id <= 0){
				throw new IllegalArgumentException("task id must be > 0");
			}
			if(runnable == null){
				throw new IllegalArgumentException("runnable can't be null");
			}
			this.id = id;
			this.runnable = runnable;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (id ^ (id >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BgTask other = (BgTask) obj;
			if (id != other.id)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "BgTask [id=" + id + "]";
		}

		@Override
		public void run() {
//			new Handler(Looper.myLooper()).post(runnable);
			try{
				runnable.run();
			}catch(Throwable e){
				e.printStackTrace();
			}
		}
	}
}
