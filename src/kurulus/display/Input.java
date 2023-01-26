package kurulus.display;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.util.HashMap;
import java.util.Map;

import kurulus.Vector;

public final class Input
  implements WindowListener, WindowFocusListener, WindowStateListener,
  KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
  static Input init(Window window, Component content) {
    final var input = new Input(new HashMap<>(), new HashMap<>(), new Vector(),
      new Vector(), new Vector(), 0, 0, false, false);

    window.addWindowListener(input);
    window.addWindowFocusListener(input);
    window.addWindowStateListener(input);
    content.addKeyListener(input);
    content.addMouseListener(input);
    content.addMouseMotionListener(input);
    content.addMouseWheelListener(input);

    return input;
  }

  private final Map<Integer, Key> keyboardKeys;
  private final Map<Integer, Key> mouseKeys;

  private Vector  cursorPreviousPosition;
  private Vector  cursorPosition;
  private Vector  cursorMovement;
  private int     wheelPreviousRotation;
  private int     wheelAccumulatedRotation;
  private boolean windowClosing;
  private boolean windowActive;

  private Input(Map<Integer, Key> keyboardKeys, Map<Integer, Key> mouseKeys,
    Vector cursorPreviousPosition, Vector cursorPosition, Vector cursorMovement,
    int wheelPreviousRotation, int wheelAccumulatedRotation,
    boolean windowClosing, boolean windowActive) {
    this.keyboardKeys             = keyboardKeys;
    this.mouseKeys                = mouseKeys;
    this.cursorPreviousPosition   = cursorPreviousPosition;
    this.cursorPosition           = cursorPosition;
    this.cursorMovement           = cursorMovement;
    this.wheelPreviousRotation    = wheelPreviousRotation;
    this.wheelAccumulatedRotation = wheelAccumulatedRotation;
    this.windowClosing            = windowClosing;
    this.windowActive             = windowActive;
  }

  public Key getKeyboardKey(int code) {
    if (!keyboardKeys.containsKey(code)) { keyboardKeys.put(code, new Key()); }
    return keyboardKeys.get(code);
  }

  public Key getMouseKey(int code) {
    if (!mouseKeys.containsKey(code)) { mouseKeys.put(code, new Key()); }
    return mouseKeys.get(code);
  }

  public Vector getCursorPosition() { return cursorPosition; }
  public Vector getCursorMovement() { return cursorMovement; }
  public int getWheelRotation() { return wheelPreviousRotation; }
  public boolean isWindowClosing() { return windowClosing; }
  public boolean isWindowActive() { return windowActive; }

  public void update() {
    for (final var key : keyboardKeys.values()) { key.update(); }
    for (final var key : mouseKeys.values()) { key.update(); }

    cursorMovement         = cursorPosition.sub(cursorPreviousPosition);
    cursorPreviousPosition = cursorPosition;

    wheelPreviousRotation    = wheelAccumulatedRotation;
    wheelAccumulatedRotation = 0;
  }

  @Override public void windowActivated(WindowEvent event) {}
  @Override public void windowClosed(WindowEvent event) {}
  @Override public void windowClosing(WindowEvent event) {
    windowClosing = true;
  }
  @Override public void windowDeactivated(WindowEvent event) {}
  @Override public void windowDeiconified(WindowEvent event) {}
  @Override public void windowIconified(WindowEvent event) {}
  @Override public void windowOpened(WindowEvent event) {}
  @Override public void windowGainedFocus(WindowEvent event) {
    windowActive = true;
  }
  @Override public void windowLostFocus(WindowEvent event) {
    windowActive = false;
  }
  @Override public void windowStateChanged(WindowEvent event) {}

  @Override public void keyPressed(KeyEvent event) {
    if (!keyboardKeys.containsKey(event.getKeyCode())) { return; }
    keyboardKeys.get(event.getKeyCode()).setDown(true);
  }
  @Override public void keyReleased(KeyEvent event) {
    if (!keyboardKeys.containsKey(event.getKeyCode())) { return; }
    keyboardKeys.get(event.getKeyCode()).setDown(false);
  }
  @Override public void keyTyped(KeyEvent event) {}

  @Override public void mouseClicked(MouseEvent event) {}
  @Override public void mouseEntered(MouseEvent event) {}
  @Override public void mouseExited(MouseEvent event) {}
  @Override public void mousePressed(MouseEvent event) {
    if (!mouseKeys.containsKey(event.getButton())) { return; }
    mouseKeys.get(event.getButton()).setDown(true);
  }
  @Override public void mouseReleased(MouseEvent event) {
    if (!mouseKeys.containsKey(event.getButton())) { return; }
    mouseKeys.get(event.getButton()).setDown(false);
  }
  @Override public void mouseDragged(MouseEvent event) {
    cursorPosition = new Vector(event.getX(), event.getY());
  }
  @Override public void mouseMoved(MouseEvent event) {
    cursorPosition = new Vector(event.getX(), event.getY());
  }
  @Override public void mouseWheelMoved(MouseWheelEvent event) {
    wheelAccumulatedRotation += event.getWheelRotation();
  }
}
