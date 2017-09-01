package game;

import java.awt.Graphics;
import java.awt.Image;

public class Player {

	private Image myImage;
	public int x = 0, y = 0;
	private int xSpeed = 0, ySpeed = 0;

	public Player(Image i) {
		myImage = i;
		x = 10;
		y = 35;
	}
	
	public void setXSpeed(int x) {
		xSpeed = x;
	}
	
	public void setYSpeed(int y) {
		ySpeed = y;
	}
	
	public void move(boolean map[][]) {
		int newx = x + xSpeed;
		int newy = y + ySpeed;
		
		// If the order of these conditions is swapped, an exception will
		// still be thrown when it tries to access map[-1] or map[40].
		if (isInBounds(newx, newy) && !map[newx][newy]) {
			x = newx;
			y = newy;
		}
	}
	
	private boolean isInBounds(int newx, int newy) {
		return (newx >= 0 && newx < 40) && (newy >= 0 && newy < 40);
	}
	
	public void paint(Graphics g) {
		g.drawImage(myImage, x * 20, y * 20, null);
	}
	
}
