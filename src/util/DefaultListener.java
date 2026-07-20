package util;

import java.awt.event.*;

public interface DefaultListener extends MouseListener,MouseWheelListener,MouseMotionListener,KeyListener,ActionListener
{
	default public void mouseClicked(MouseEvent e) { }
	default public void mousePressed(MouseEvent e) { }
	default public void mouseReleased(MouseEvent e) { }
	default public void mouseEntered(MouseEvent e) { }
	default public void mouseExited(MouseEvent e) { }
	default public void mouseMoved(MouseEvent e) { }
	default public void mouseDragged(MouseEvent e) { }
	default public void mouseWheelMoved(MouseWheelEvent e) { }
	default public void keyTyped(KeyEvent e) { }
	default public void keyPressed(KeyEvent e) { }
	default public void keyReleased(KeyEvent e) { }
	default public void actionPerformed(ActionEvent e) { }
}
