package com.xormedia.mylib;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.AsyncTask;

public class MyRunLastTask {
  // private static Logger Log = Logger.getLogger(MyRunLastTask.class);
  private ExecutorService executorService = null;
  private ArrayList<_Task> TaskQueue = new ArrayList<_Task>();
  private MyRunLastTask.InThread inThread = null;

  private static class _Task {
    MyRunLastTask.Task task = null;
    MyRunLastTask.ToDo Todo = null;
  }

  @Override
  protected void finalize() throws Throwable {
    TaskQueue.clear();
    super.finalize();
  }

  private class TaskInThread extends AsyncTask<_Task, Integer, _Task> {

    @Override
    protected MyRunLastTask._Task doInBackground(_Task... params) {
      _Task _task = params[0];
      _task.task = inThread.doInBackground(_task.task);
      return _task;
    }

    @Override
    protected void onPostExecute(MyRunLastTask._Task result) {
      if (result != null) {
        synchronized (TaskQueue) {
          if (result.task.isStop == false && result.Todo != null) {
            result.Todo.afterTodo(result.task);
          }
          TaskQueue.remove(result);
        }
      }
    }
  }

  public static class Task {
    public boolean isStop = false;
  }

  public static interface ToDo {
    /**
     * 执行任务前所要初始化过程
     * 
     * @param task
     */
    public void beforeTodo(MyRunLastTask.Task task);

    /**
     * 执行任务后所要处理了任务执行结果
     * 
     * @param task
     */
    public void afterTodo(MyRunLastTask.Task task);
  }

  public static interface InThread {
    /**
     * 任务在线程中的执行过程函数
     * 
     * @param task
     *          执行的任务
     * @return
     */
    public MyRunLastTask.Task doInBackground(MyRunLastTask.Task task);
  }

  /**
   * 始終執行最後一個任務，停止之前未完成的任务。
   * 
   * @param nThreads
   *          使用到的线程数量
   * @param _inThread
   *          任务的执行过程。
   */
  public MyRunLastTask(int nThreads, InThread _inThread) {
    if (nThreads <= 0) {
      nThreads = 1;
    }
    if (_inThread != null) {
      executorService = Executors.newFixedThreadPool(nThreads);
      inThread = _inThread;
    }
  }

  /**
   * 添加任务
   * 
   * @param task
   *          添加任务
   * @param _todo
   *          任务在执行前和执行后所做的事情
   */
  public void addTask(MyRunLastTask.Task task, ToDo _todo) {
    if (task != null) {
      _Task _task = new _Task();
      _task.task = task;
      _task.Todo = _todo;
      synchronized (TaskQueue) {
        for (int i = 0; i < TaskQueue.size(); i++) {
          TaskQueue.get(i).task.isStop = true;
        }
        if (_todo != null) {
          _todo.beforeTodo(task);
        }
        TaskQueue.add(_task);
        new TaskInThread().executeOnExecutor(executorService, _task);
      }
    }
  }

}
