package com.thefind.sisyphus;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Eric Gaudet
 */
public abstract class InputYielder
extends Input
{
  private final static String[] EOF_ITEM = new String[0];
  private final BlockingQueue<String[]> item_queue_;

  private Generator gen_;

  public InputYielder(String[] out_cols)
  {
    super(out_cols);
    item_queue_  = new ArrayBlockingQueue(100, true); // fair
  }

  @Override
  public final boolean open()
  {
    gen_ = new Generator();
    boolean ready = beforeLoop();
    if (ready) {
      gen_.start();
    }

    return ready;
  }

  @Override
  public final void ready(List<String> schema_row)
  { super.ready(schema_row); }

  @Override
  public final void close()
  { afterLoop(); }

  @Override
  protected final int readRow(String[] result)
  {
    String[] item = EOF_ITEM;
    try {
      item = item_queue_.take();
    }
    catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }

    if (item == EOF_ITEM) {
      if (gen_.ex_!=null) {
        throw new RuntimeException(gen_.ex_);
      }
      return -1;
    }

    int sz = result.length;
    if (item.length<sz) {
      sz = item.length;
    }
    System.arraycopy(item, 0, result, 0, sz);
    return item.length;
  }

  protected abstract boolean beforeLoop();
  protected abstract void mainLoop() throws Exception;
  protected abstract void afterLoop();

  protected void yield(String[] item)
  {
    try {
      item_queue_.put(item);
    }
    catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  protected final long getInternalHashCode()
  { return hashCode(); }

  @Override
  protected final String toStringWhich()
  { return " ~> " + getSchemaOut(); }

  private class Generator
  extends Thread
  {
    private Exception ex_ = null;

    public Generator()
    {
      super();
      setName(toString());
    }

    @Override
    public void run()
    {
      try {
        mainLoop();
      }
      catch (Exception ex) {
        ex_ = ex;
      }

      try {
        item_queue_.put(EOF_ITEM);
      }
      catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
  }
}

