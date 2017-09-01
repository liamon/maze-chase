package game;

import java.awt.Point;

public class GameBoardNode implements Comparable<GameBoardNode> {
	
	private int totalCost; // f
	private int costFromStart; // g
	private int heuristicCost; // h
	private Point thisPoint;
	private GameBoardNode parentNode;
	
	public GameBoardNode(Point point, GameBoardNode parent) {
		thisPoint = point;
		parentNode = parent;
	}
	
	public GameBoardNode(Point point) {
		this(point, null);
	}
	
	public GameBoardNode getParent() {
		return parentNode;
	}
	
	public void setParent(GameBoardNode parent) {
		parentNode = parent;
	}
	
	public Point getPoint() {
		return thisPoint;
	}
	
	public int getCostFromStart() {
		return costFromStart;
	}
	
	public int getTotalCost() {
		return totalCost;
	}
	
	public void setCost(int targetX, int targetY) {
		if (parentNode == null) {
			costFromStart = 0;
		} else {
			costFromStart = distanceFromParent(parentNode) + parentNode.getCostFromStart();
		}
		heuristicCost = Math.abs(thisPoint.x - targetX) * 10 +
				Math.abs(thisPoint.y - targetY) * 10;
		totalCost = costFromStart + heuristicCost;
	}

	private int distanceFromParent(GameBoardNode parentNode) {
		int distanceFromParent;
		Point parentPoint = parentNode.getPoint();
		
		if (Math.abs(thisPoint.x - parentPoint.x) == 1 &&
				Math.abs(thisPoint.y - parentPoint.y) == 1) { // diagonal
			distanceFromParent = 14; // Roughly Math.sqrt(2) * 10.
		} else { // orthogonal
			distanceFromParent = 10;
		}
		return distanceFromParent;
	}

	@Override
	public int compareTo(GameBoardNode node) {
		// In the first time round the enhanced for loop in reCalcPath in
		// BadGuy, nodeWithBestCost will be null so need to account for that.
		if (node == null || totalCost < node.getTotalCost()) {
			return -1;
		} else if (totalCost == node.getTotalCost()) {
			return 0;
		} else {
			return 1;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GameBoardNode) {
			// equals only needs to check if the Points are equal.
			return thisPoint.equals(((GameBoardNode) obj).getPoint());
		} else {
			return false;
		}
	}

}
