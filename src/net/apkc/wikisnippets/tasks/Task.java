package net.apkc.wikisnippets.tasks;

// Swing
import javax.swing.SwingWorker;

public abstract class Task extends SwingWorker<Object, Object> {

    public abstract void reportProgress(int progress);
}
