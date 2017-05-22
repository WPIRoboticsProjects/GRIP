package edu.wpi.grip.core;

import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.MockInputSocket;
import edu.wpi.grip.core.sockets.MockOutputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static edu.wpi.grip.core.AbstractPipelineEntry.idPool;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AbstractPipelineEntryTest {

  private static class TestEntryImpl extends AbstractPipelineEntry {

    TestEntryImpl() {
      super(makeId(TestEntryImpl.class));
    }

    @Override
    public List<InputSocket> getInputSockets() {
      return ImmutableList.of();
    }

    @Override
    public List<OutputSocket> getOutputSockets() {
      return ImmutableList.of();
    }

    @Override
    protected void cleanUp() {
      // NOP
    }

  }

  @Test
  public void testMakeId() {
    TestEntryImpl t = new TestEntryImpl();
    String id = t.getId();
    assertTrue("ID was not added to the pool", idPool.checkId(TestEntryImpl.class, id));
    t.setRemoved();
    assertFalse("ID was not removed from the pool", idPool.checkId(TestEntryImpl.class, id));
  }

  @Test(expected = IllegalStateException.class)
  public void testSetId() {
    TestEntryImpl t = new TestEntryImpl();
    t.setId("foo");
    fail("setId() should fail if called outside deserialization");
  }

  @Test
  public void testSetRemoved() {
    AtomicBoolean didCleanUp = new AtomicBoolean(false);
    TestEntryImpl t = new TestEntryImpl() {
      @Override
      protected void cleanUp() {
        didCleanUp.set(true);
      }
    };
    t.setRemoved();
    assertTrue("cleanUp was not called", didCleanUp.get());
    assertTrue("Entry was not removed", t.removed());
  }

  @Test
  public void testGetSocketById() {
    final InputSocket in = new MockInputSocket("foo");
    final OutputSocket out = new MockOutputSocket("bar");
    TestEntryImpl t = new TestEntryImpl() {
      @Override
      public List<InputSocket> getInputSockets() {
        return ImmutableList.of(in);
      }

      @Override
      public List<OutputSocket> getOutputSockets() {
        return ImmutableList.of(out);
      }
    };
    assertEquals("Did not get correct socket", in, t.getSocketByUid(in.getUid()));
    assertEquals("Did not get correct socket", out, t.getSocketByUid(out.getUid()));
  }

}
