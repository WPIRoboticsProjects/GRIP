package edu.wpi.grip.core.util;

/**
 * A daemon thread is a minor thread that does not prevent the JVM from exiting. This is a
 * convenience class to make it easier to use daemon threads in a program. The following code blocks
 * are effectively equivalent:
 * <pre><code>
 *   Thread thread = new Thread(...);
 *   thread.setDaemon(true);
 * </code></pre>
 * <pre><code>
 *   Thread thread = new DaemonThread(...);
 * </code></pre>
 *
 * @see Thread#setDaemon(boolean) Thread.setDaemon
 */
public class DaemonThread extends Thread {

  public DaemonThread() {
    super();
    setDaemon(true);
  }

  public DaemonThread(Runnable target) {
    super(target);
    setDaemon(true);
  }

  public DaemonThread(ThreadGroup group, Runnable target) {
    super(group, target);
    setDaemon(true);
  }

  public DaemonThread(String name) {
    super(name);
    setDaemon(true);
  }

  public DaemonThread(ThreadGroup group, String name) {
    super(group, name);
    setDaemon(true);
  }

  public DaemonThread(Runnable target, String name) {
    super(target, name);
    setDaemon(true);
  }

  public DaemonThread(ThreadGroup group, Runnable target, String name) {
    super(group, target, name);
    setDaemon(true);
  }

  public DaemonThread(ThreadGroup group, Runnable target, String name, long stackSize) {
    super(group, target, name, stackSize);
    setDaemon(true);
  }

}
