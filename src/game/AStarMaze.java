package game;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.io.*;

public class AStarMaze extends JFrame implements Runnable, MouseListener, MouseMotionListener, KeyListener {

	// member data
	private boolean isInitialised = false;
	private BufferStrategy strategy;
	private Graphics offscreenBuffer;
	
	private boolean map[][] = new boolean[40][40];
	private boolean isGameRunning = false;
	
	private BadGuy badguy;
	private Player player;
	
	private String imageFilePath = System.getProperty("user.dir") + File.separator + "img" + File.separator;
	private String saveFilePath = System.getProperty("user.dir") + File.separator + "save" + File.separator;
	
	// constructor
	public AStarMaze () {
		
		// Display the window, centred on the screen
		Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int x = screensize.width/2 - 400;
		int y = screensize.height/2 - 400;
		setBounds(x, y, 800, 800);
		setVisible(true);
		this.setTitle("A* Pathfinding Demo");
		
		// load raster graphics and instantiate game objects
		ImageIcon icon = new ImageIcon(imageFilePath + "badguy.png");
		Image img = icon.getImage();
		badguy = new BadGuy(img);
		icon = new ImageIcon(imageFilePath + "player.png");
		img = icon.getImage();
		player = new Player(img);
		
		// create and start our animation thread
		Thread t = new Thread(this);
		t.start();
		
		// initialise double-buffering
		createBufferStrategy(2);
		strategy = getBufferStrategy();
		offscreenBuffer = strategy.getDrawGraphics();
		
		// register the Jframe itself to receive mouse and keyboard events
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		
		// initialise the map state
		for (x = 0; x < 40; x++) {
			for (y = 0; y < 40; y++) {
				map[x][y] = false;
			}
		}
		
		isInitialised = true;
	}
	
	// thread's entry point
	public void run() {
		long loops=0;
		while (true) {
			// 1: sleep for 1/8 sec
			try {
				Thread.sleep(125);
			} catch (InterruptedException e) { }
			
			// 2: animate game objects
			if (isGameRunning) {
				loops++;
				player.move(map); // player moves every frame
				if (loops % 2 == 0) { // badguy moves once every 2 frames
					badguy.move(map, player.x, player.y);
				}
			}
			
			// 3: force an application repaint
			this.repaint();
			
		}
	}
	
	private void loadMaze() {
		// Because I changed saveMaze I had to change this as well.
		String filename = saveFilePath + "maze.txt";
		String[] rawinput = new String[40];
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			for (int i = 0; i < 40; i++) {
				rawinput[i] = reader.readLine();
			}
			reader.close();
		} 
		catch (IOException e) {}
		
		// This next part is to combine the array of Strings into a single String.
		StringBuilder sb = new StringBuilder();
		for (String line : rawinput) {
			sb.append(line);
		}
		String savedData = sb.toString();
		
		if (savedData != null) {
			for (int y = 0; y < 40; y++) {
			for (int x = 0; x < 40; x++) {
				map[x][y] = (savedData.charAt(y * 40 + x) == 'M');
			}
		}
		}
	}
	
	private void saveMaze() {
		// pack maze into a string
		String outputtext = "";
		// This way, the save file will be human-readable and easily editable (if wanted).
		for (int y = 0; y < 40; y++) {
			for (int x = 0; x < 40; x++) {
				if (map[x][y])
					outputtext += "M"; // Maze walls.
				else
					outputtext += "."; // Empty cells.
			}
			outputtext += System.getProperty("line.separator");
		}
		
		try {
			String filename = saveFilePath + "maze.txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			writer.write(outputtext);
			writer.close();
		}
		catch (IOException e) {}		
	}
	
	// mouse events which must be implemented for MouseListener
	public void mousePressed(MouseEvent e) {
		if (!isGameRunning) {
			int x = e.getX();
			int y = e.getY();
			if (isStartClicked(x, y)) {
				isGameRunning = true;
				badguy.reCalcPath(map, player.x, player.y);
				return;
			}
			if (isLoadClicked(x, y)) {
				loadMaze();
				return;
			}
			if (isSaveClicked(x, y)) {
				saveMaze();
				return;
			}
		}
		
		// determine which cell of the gameState array was clicked on
		int x = e.getX() / 20;
		int y = e.getY() / 20;
		// toggle the state of the cell
		map[x][y] = !map[x][y];
		// throw an extra repaint, to get immediate visual feedback
		this.repaint();
		// store mouse position so that each tiny drag doesn't toggle the cell
		// (see mouseDragged method below)
		prevx = x;
		prevy = y;
	}
	
	private boolean isStartClicked(int x, int y) {
		return x >= 15 && x <= 85 && y >= 40 && y <= 70;
	}
	
	private boolean isLoadClicked(int x, int y) {
		return x >= 315 && x <= 385 && y >= 40 && y <= 70;
	}
	
	private boolean isSaveClicked(int x, int y) {
		return x >= 415 && x <= 485 && y >= 40 && y <= 70;
	}
	
	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mouseClicked(MouseEvent e) {}
	
	// mouse events which must be implemented for MouseMotionListener
	public void mouseMoved(MouseEvent e) {}

	// mouse position on previous mouseDragged event
	// must be member variables for lifetime reasons
	int prevx = -1, prevy = -1; 
	public void mouseDragged(MouseEvent e) {
		// determine which cell of the gameState array was clicked on
		// and make sure it has changed since the last mouseDragged event
		int x = e.getX() / 20;
		int y = e.getY() / 20;
		if (x != prevx || y != prevy) {
			// toggle the state of the cell
			map[x][y] = !map[x][y];
			// throw an extra repaint, to get immediate visual feedback
			this.repaint();
			// store mouse position so that each tiny drag doesn't toggle the cell
			prevx = x;
			prevy = y;
		}
	}

	// Keyboard events
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			player.setXSpeed(-1);
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			 player.setXSpeed(1);
		} else if (e.getKeyCode() == KeyEvent.VK_UP) {
			 player.setYSpeed(-1);
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			player.setYSpeed(1);
		}
		badguy.reCalcPath(map, player.x, player.y);
	}
	
	public void keyReleased(KeyEvent e) { 
		if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
			player.setXSpeed(0);
		} else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
			player.setYSpeed(0);
		}
	}
	
	public void keyTyped(KeyEvent e) { }
	
	// application's paint method
	public void paint(Graphics g) {
		if (!isInitialised)
			return;
		
		g = offscreenBuffer; // draw to offscreen buffer
	
		// clear the canvas with a big black rectangle
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 800, 800);
		
		// redraw the map
		g.setColor(Color.WHITE);
		for (int x = 0; x < 40; x++) {
			for (int y = 0; y < 40; y++) {
				if (map[x][y]) {
					g.fillRect(x*20, y*20, 20, 20);
				}
			}
		}		
		// redraw the player and badguy
		// paint the game objects
		player.paint(g);
		badguy.paint(g);
		
		if (!isGameRunning) {
		// game is not running.
		// draw a 'start button' as a rectangle with text on top
		// also draw 'load' and 'save' buttons
		g.setColor(Color.GREEN);
		g.fillRect(15, 40, 70, 30);
		g.fillRect(315, 40, 70, 30);
		g.fillRect(415, 40, 70, 30);
		g.setFont(new Font("Times", Font.PLAIN, 24));
		g.setColor(Color.BLACK);
		g.drawString("Start", 22, 62);
		g.drawString("Load", 322, 62);
		g.drawString("Save", 422, 62);
		}
		
		// flip the buffers
		strategy.show();
	}
	
	// application entry point
	public static void main(String[] args) {
		AStarMaze w = new AStarMaze();
	}

}
