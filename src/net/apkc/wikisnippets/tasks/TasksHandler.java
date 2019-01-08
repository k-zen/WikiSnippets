package net.apkc.wikisnippets.tasks;

// Util
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Class to handle all tasks in AIME.
 *
 * <p>Definition of a task: A task is an action that will be launched from the
 * EDT to perform some sort of computation/initialization whetter it'll be
 * asynchronous or synchronous, an can or can't return some response/result
 * after completion. The main idea is to encapsulate every single task that AIME
 * performs into a task object that will be launched as a FutureTask using
 * mainly an Executor.</p>
 *
 * <p>There can be task groups and those groups will execute their stack of
 * tasks using a BlockingQueue mostly. A group corresponds to similar tasks, for
 * instance: Server startup tasks, Crawling processes, etc.</p>
 *
 * @author K-Zen
 * @see Singleton Pattern
 */
public class TasksHandler {

    // Instance.
    private static volatile TasksHandler _INSTANCE = new TasksHandler();
    private ExecutorService finiteTaskPool = Executors.newCachedThreadPool();

    private TasksHandler() {
    }

    public static TasksHandler getInstance() {
        return _INSTANCE;
    }

    public void submitInfiniteTask(Runnable task) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(task);
    }

    public Future<Boolean> submitFiniteTask(Runnable task) {
        return finiteTaskPool.submit(task, true);
    }
}
