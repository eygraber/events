package com.staticbloc.events.app;

import android.app.Activity;
import android.widget.Toast;

import com.staticbloc.events.Event;
import com.staticbloc.events.EventHandler;
import com.staticbloc.events.Events;
import com.staticbloc.events.RunType;

/**
 * Created with IntelliJ IDEA.
 * User: eygraber
 * Date: 6/21/2015
 * Time: 2:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class EventHandlerTestActivity extends Activity {
  private Events events = Events.getDefault();

  public static class EventImpl implements Event {}

  private Events.OnEventListener eventListener = new Events.OnEventListener() {
    @Override
    public void onEvent(Event event) {
      Toast.makeText(EventHandlerTestActivity.this, "Got a listener event too", Toast.LENGTH_SHORT).show();
    }
  };

  @Override
  protected void onResume() {
    super.onResume();

    events.subscribe(this);

    events.subscribe(eventListener, RunType.MAIN);

    findViewById(android.R.id.content).postDelayed(new Runnable() {
      @Override
      public void run() {
        events.post(new EventImpl());
      }
    }, 2500);
  }

  @Override
  protected void onPause() {
    super.onPause();

    events.unsubscribe(this);
    events.unsubscribe(eventListener, RunType.MAIN);
  }

  @EventHandler(runType = RunType.MAIN)
  public void test(EventImpl e) {
    Toast.makeText(this, "Got an event", Toast.LENGTH_SHORT).show();
  }
}
