package com.cyberark.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
 *  A class that monitors inactivity in an application.
 *
 *  It does this by using a Swing Timer and by listening for specified
 *  AWT events. When an event is received the Timer is restarted.
 *  If no event is received during the specified time interval then the
 *  timer will fire and invoke the specified Action.
 *
 *  When creating the listener the inactivity interval is specified in
 *  minutes. However, once the listener has been created you can reset
 *  this value in milliseconds if you need to.
 *
 *  Some common event masks have be defined with the class:
 *
 *  KEY_EVENTS
 *  MOUSE_EVENTS - which includes mouse motion events
 *  USER_EVENTS - includes KEY_EVENTS and MOUSE_EVENT (this is the default)
 *
 *  The inactivity interval and event mask can be changed at any time,
 *  however, they will not become effective until you stop and start
 *  the listener.
 */
class InactivityListener implements ActionListener, AWTEventListener {
  public final static long KEY_EVENTS = AWTEvent.KEY_EVENT_MASK;

  public final static long MOUSE_EVENTS =
      AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK;

  public final static long USER_EVENTS = KEY_EVENTS + MOUSE_EVENTS;

  private final Window window;
  private Action action;
  private int interval;
  private long eventMask;
  private final Timer timer = new Timer(0, this);
  private static final Logger logger = LogManager.getLogger(InactivityListener.class);

  /*
   *  Use a default inactivity interval of 1 minute and listen for
   *  USER_EVENTS
   */
  public InactivityListener(Window window, Action action) {
    this(window, action, 1);
  }

  /*
   *	Specify the inactivity interval and listen for USER_EVENTS
   */
  public InactivityListener(Window window, Action action, int interval) {
    this(window, action, interval, USER_EVENTS);
  }

  /*
   *  Specify the inactivity interval and the events to listen for
   */
  public InactivityListener(Window window, Action action, int minutes, long eventMask) {
    this.window = window;
    setAction(action);
    setInterval(minutes);
    setEventMask(eventMask);
  }

  /*
   *  The Action to be invoked after the specified inactivity period
   */
  public void setAction(Action action) {
    this.action = action;
  }

  /*
   *  The interval before the Action is invoked specified in minutes
   */
  public void setInterval(int minutes) {
    setIntervalInMillis(minutes * 60000);
  }

  /*
   *  The interval before the Action is invoked specified in milliseconds
   */
  public void setIntervalInMillis(int interval) {
    this.interval = interval;
    timer.setInitialDelay(interval);
  }

  /*
   *	A mask specifying the events to be passed to the AWTEventListener
   */
  public void setEventMask(long eventMask) {
    this.eventMask = eventMask;
  }

  /*
   *  Start listening for events.
   */
  public void start() {
    logger.trace("start::enter");

    timer.setInitialDelay(interval);
    timer.setRepeats(false);
    timer.start();
    Toolkit.getDefaultToolkit().addAWTEventListener(this, eventMask);

    logger.trace("start::exit");
  }

  /*
   *  Stop listening for events
   */
  public void stop() {
    logger.trace("stop::enter");

    logger.debug("remove AWT event listener");
    Toolkit.getDefaultToolkit().removeAWTEventListener(this);

    logger.debug("Stop timer");
    timer.stop();

    logger.trace("stop::exit");
  }

  //  Implement ActionListener for the Timer
  public void actionPerformed(ActionEvent e) {
    logger.trace("actionPerformed::enter {}", e);

    logger.debug("Invoking action: {}", action);
    ActionEvent ae = new ActionEvent(window, ActionEvent.ACTION_PERFORMED, "");
    action.actionPerformed(ae);

    logger.trace("actionPerformed::exit {}", e);
  }

  //  Implement AWTEventListener
  public void eventDispatched(AWTEvent e) {
    if (timer.isRunning()) {
      timer.restart();
    }
  }
}