package com.serotonin.goid.task.maze;

import java.awt.Point;
import java.util.ArrayList;

public class MazeCell {
	
	private enum State {
		INTERNAL, EDGE, UNTOUCHED
	}
	private State state = State.UNTOUCHED;
	
	public boolean northWall = true;
	public boolean westWall = true;
	
	public static MazeCell[][] makeMaze(int width, int height) {
		//Init array
		MazeCell[][] m = new MazeCell[height][width];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				m[y][x] = new MazeCell();
			}
		}
		
		//Connect cells
		ArrayList<Point> frontiers = new ArrayList<Point>();
		
		Point p = new Point((int)(Math.random()*width), (int)(Math.random()*height));
		MazeCell c = m[p.y][p.x];
		c.state = State.INTERNAL;
		for (Point n : getNeighborsWithType(m, p.x, p.y, State.UNTOUCHED)) {
			m[n.y][n.x].state = State.EDGE;
			frontiers.add(new Point(n.x, n.y));
		}
		
		while (!frontiers.isEmpty()) {
			p = frontiers.get((int)(Math.random()*frontiers.size()));
			c = m[p.y][p.x];
			
			ArrayList<Point> internalNeighbors = getNeighborsWithType(m, p.x, p.y, State.INTERNAL);
			Point randomInternalNeighbor = internalNeighbors.get((int)(Math.random()*internalNeighbors.size()));
			
			if (p.y < randomInternalNeighbor.y) {
				m[randomInternalNeighbor.y][randomInternalNeighbor.x].northWall = false;
			} else if (p.y > randomInternalNeighbor.y) {
				m[p.y][p.x].northWall = false;
			} else if (p.x < randomInternalNeighbor.x) {
				m[randomInternalNeighbor.y][randomInternalNeighbor.x].westWall = false;
			} else {
				m[p.y][p.x].westWall = false;
			}
			
			c.state = State.INTERNAL;
			for (Point n : getNeighborsWithType(m, p.x, p.y, State.UNTOUCHED)) {
				m[n.y][n.x].state = State.EDGE;
				frontiers.add(new Point(n.x, n.y));
			}
			
			frontiers.remove(p);
		}
		m[0][0].northWall = false;
		
		return m;
	}
	
	private static ArrayList<Point> getNeighborsWithType(MazeCell[][] m, int x, int y, State desiredType) {
		ArrayList<Point> n = new ArrayList<Point>();
		for (int i=0; i < 4; i++) {
			try {
				MazeCell c = m[y + WorldBuilder.yChange(i)][x + WorldBuilder.xChange(i)];
				if (c.state == desiredType) { n.add(new Point(x + WorldBuilder.xChange(i), y + WorldBuilder.yChange(i))); }
			} catch (IndexOutOfBoundsException e) {}
		}
		return n;
	}
}
