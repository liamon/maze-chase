package game;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.util.LinkedList;
import java.util.Stack;

public class BadGuy {
	
	private Image myImage;
	private int x = 0, y = 0;
	private boolean hasPath = false;
	
	private LinkedList<GameBoardNode> openList = new LinkedList<GameBoardNode>();
	private LinkedList<GameBoardNode> closedList = new LinkedList<GameBoardNode>();
	private Stack<Point> pointsToMoveTo = new Stack<Point>();
	private Point[] neighbors = new Point[8];

	public BadGuy(Image i) {
		myImage = i;
		x = 30;
		y = 10;
	}
	
	public void reCalcPath(boolean map[][], int targx, int targy) {
		hasPath = false;
		while (!pointsToMoveTo.empty()) {
			pointsToMoveTo.pop();
		}
		if (x == targx && y == targy) {
			return; // The stack will be empty so BadGuy will not move.
		}
		// The two lists need to be re-initialized every time. Otherwise, the
		// nodes left in closedList will mean that checkNeighbors will eventually
		// think that all cells are in closedList, so it won't add them to openList.
		openList = new LinkedList<GameBoardNode>();
		closedList = new LinkedList<GameBoardNode>();
		
		int currentX = x, currentY = y;
		GameBoardNode startPosition = new GameBoardNode(new Point(currentX, currentY));
		openList.add(startPosition);
		setNeighbours(currentX, currentY);
		
		for (int i = 0; i < 8; i++) {
			if (map[neighbors[i].x][neighbors[i].y] == false) {
				GameBoardNode currentNode = new GameBoardNode(neighbors[i], startPosition);
				openList.add(currentNode);
				currentNode.setCost(targx, targy);
			}
			// Else do not add walls to list, ignore them.
		}
		openList.remove(startPosition);
		closedList.add(startPosition);
		
		while (checkNeighbors(map, targx, targy));
		fillPointsToMoveTo(startPosition, targx, targy);
		hasPath = true;
	}
	
	private GameBoardNode nodeWithBestCost() {
		GameBoardNode nodeWithBestCost = null;
		for (GameBoardNode node : openList) {
			// Having <= as opposed to < will make nodeWithBestCost be the last
			// node added to openList with the same cost as another node,
			// which policyalmanac.org/games/aStarTutorial.htm says is faster.
			if (node.compareTo(nodeWithBestCost) <= 0) {
				nodeWithBestCost = node;
			}
		}
		return nodeWithBestCost;
	}
	
	private boolean checkNeighbors(boolean map[][], int targx, int targy) {
		GameBoardNode nodeWithBestCost = nodeWithBestCost();
		int currentX = nodeWithBestCost.getPoint().x;
		int currentY = nodeWithBestCost.getPoint().y;
		openList.remove(nodeWithBestCost);
		closedList.add(nodeWithBestCost);
		
		// This if statement must be here otherwise the target
		// node will not get added to the closedList.
		if (currentX == targx && currentY == targy) {
			return false;
		}
		setNeighbours(currentX, currentY);
		
		checkNeighborsLoop:
			for (int i = 0; i < 8; i++) {
				GameBoardNode currentNode = new GameBoardNode(neighbors[i], nodeWithBestCost);
				if (map[neighbors[i].x][neighbors[i].y] == false) {
					for (GameBoardNode node : closedList) {
						if (currentNode.equals(node)) {
							continue checkNeighborsLoop; // Don't add to openList from closedList.
						}
					}
					
					for (GameBoardNode node : openList) {
						if (currentNode.equals(node)) {
							GameBoardNode possibleNewNode =
									new GameBoardNode(node.getPoint(), nodeWithBestCost);
							possibleNewNode.setCost(targx, targy);
							if (possibleNewNode.compareTo(node) < 0) {
								node.setParent(nodeWithBestCost);
								node.setCost(targx, targy);
							} // else don't change the parent node.
							continue checkNeighborsLoop; // Don't add it twice to openList.
						}
					}
					// These two lines should only run if it's not in either list.
					openList.add(currentNode);
					currentNode.setCost(targx, targy);
				}
				// Else do not add walls to list, ignore them.
			}
		return true;
	}
	
	private void fillPointsToMoveTo(GameBoardNode startPosition, int targx, int targy) {
		GameBoardNode endPosition = null;
		// Must say .size() - 1 otherwise the array index is out of bounds.
		for (int i = closedList.size() - 1; i >= 0; i--) {
			// For some reason, the target node is not being added to closedList,
			// so this does not work.
			if (closedList.get(i).getPoint().x == targx &&
					closedList.get(i).getPoint().y == targy) {
				endPosition = closedList.get(i);
			}
		}
		pointsToMoveTo.push(endPosition.getPoint());
		while (endPosition.getParent() != startPosition) {
			endPosition = endPosition.getParent();
			pointsToMoveTo.push(endPosition.getPoint());
		}
	}

	public void move(boolean map[][], int targx, int targy) {
		if (hasPath) {
			if (pointsToMoveTo.empty()) {
				// Don't move, BadGuy is in same spot as Player.
			} else {
				Point nextPoint = pointsToMoveTo.pop();
				x = nextPoint.x;
				y = nextPoint.y;
			}
		}
		else {
			// no path known, so just do a dumb 'run towards' behaviour
			int newx = x, newy = y;
			if (targx < x)
				newx--;
			else if (targx > x)
				newx++;
			if (targy < y)
				newy--;
			else if (targy > y)
				newy++;
			if (!map[newx][newy]) {
				x = newx;
				y = newy;
			}
		}
	}
	
	private void setNeighbours(int x, int y) {
		// Top left, top centre, top right, middle left.
		neighbors[0] = new Point(positiveMod(x - 1, 40), positiveMod(y - 1, 40));
		neighbors[1] = new Point(positiveMod(x, 40), positiveMod(y - 1, 40));
		neighbors[2] = new Point(positiveMod(x + 1, 40), positiveMod(y - 1, 40));
		neighbors[3] = new Point(positiveMod(x - 1, 40), positiveMod(y, 40));
		// Middle right, bottom left, bottom centre, bottom right.
		neighbors[4] = new Point(positiveMod(x + 1, 40), positiveMod(y, 40));
		neighbors[5] = new Point(positiveMod(x - 1, 40), positiveMod(y + 1, 40));
		neighbors[6] = new Point(positiveMod(x, 40), positiveMod(y + 1, 40));
		neighbors[7] = new Point(positiveMod(x + 1, 40), positiveMod(y + 1, 40));
	}
	
	private int positiveMod(int dividend, int modulus) {
		// Based off a solution described in this link: http://stackoverflow.com/a/4412200
		return (dividend % modulus + modulus) % modulus;
	}
	
	public void paint(Graphics g) {
		g.drawImage(myImage, x * 20, y * 20, null);
	}
	
}

