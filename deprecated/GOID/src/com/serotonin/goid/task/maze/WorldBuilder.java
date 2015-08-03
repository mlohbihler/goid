package com.serotonin.goid.task.maze;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

public class WorldBuilder {
	
	private MazeCell[][] maze;
	private int cellSize;
	private int offsetX;
	private int offsetY;
	
	public WorldBuilder(int width, int height) {
		maze = MazeCell.makeMaze(width, height);
		
		cellSize = 40;
		offsetX = -cellSize/2 - ((int)(Math.random()*(width-6))+3)*cellSize;
		offsetY = -cellSize/2 - ((int)(Math.random()*(height-6))+3)*cellSize;
	}
	
	public List<Point> makeMazePoints() {
		List<Point> points = new ArrayList<Point>();
		
		int direction = 0; // 0 = south, 1 = east, 2 = north, 3 = west
		int xPos = -1;
		int yPos = 0;
		do {
			if (canWalk(direction+1, xPos, yPos)) {
				points.add(makeCorner(xPos, yPos, direction, direction+1));
				direction = (direction+1)%4;
				xPos += xChange(direction);
				yPos += yChange(direction);
				continue;
			}
			
			if (canWalk(direction, xPos, yPos)) {
				xPos += xChange(direction);
				yPos += yChange(direction);
				continue;
			}
			
			points.add(makeCorner(xPos, yPos, direction+2, direction+1));
			direction = (direction+3)%4;
			
			if (canWalk(direction, xPos, yPos)) {
				xPos += xChange(direction);
				yPos += yChange(direction);
			}
		} while(xPos != -1 || yPos != 0 || direction != 0); //do until we're at the start again
		
        return points;
    }
	
	public boolean canWalk(int direction, int xPos, int yPos) {
		direction = direction %4;
		return
			(direction == 0 && !getCell(xPos, yPos+1).northWall)
			||
			(direction == 1 && !getCell(xPos+1, yPos).westWall)
			||
			(direction == 2 && !getCell(xPos, yPos).northWall)
			||
			(direction == 3 && !getCell(xPos, yPos).westWall);
	}
	
	public MazeCell getCell(int x, int y) {
		try {
			return maze[y][x];
		} catch (IndexOutOfBoundsException e) {
			MazeCell c = new MazeCell();
			if (x >= maze[0].length || x < 0 || y < 0) {
				c.northWall = false;
			}
			if (y >= maze.length || x < 0 || y < 0) {
				c.westWall = false;
			}
			return c;
		}
	}
	
	private Point makeCorner(int xPos, int yPos, int directionFrom, int directionTurned) {
		int x = cellSize*xPos + offsetX + cellSize/2 + (cellSize/4)*xChange(directionTurned) - (cellSize/4)*xChange(directionFrom);
		int y = cellSize*yPos + offsetY + cellSize/2 + (cellSize/4)*yChange(directionTurned) - (cellSize/4)*yChange(directionFrom);
		return new Point(x,y);
	}

	public static int xChange(int direction) {
		direction = direction %4;
		return (direction % 2)*(-direction+2);
	}
	
	public static int yChange(int direction) {
		direction = direction %4;
		return ((direction+1) % 2)*(-direction+1);
	}
	
	public List<Point> makeMazeCap() {
		List<Point> points = new ArrayList<Point>();
		points.add(new Point(offsetX+cellSize/4,offsetY-cellSize/4));
		points.add(new Point(offsetX+cellSize*3/4,offsetY-cellSize/4));
		points.add(new Point(offsetX+cellSize*3/4,offsetY+cellSize/4));
		points.add(new Point(offsetX+cellSize/4,offsetY+cellSize/4));
		return points;
	}
	
    public List<Shape> getGoalRegions() {
    	ArrayList<Shape> shuffle = new ArrayList<Shape>();
    	shuffle.add(new Rectangle(offsetX, offsetY, cellSize, cellSize));
    	shuffle.add(new Rectangle(cellSize*(maze[0].length-1)+offsetX, offsetY, cellSize, cellSize));
    	shuffle.add(new Rectangle(cellSize*(maze[0].length-1)+offsetX, cellSize*(maze.length-1)+offsetY, cellSize, cellSize));
    	shuffle.add(new Rectangle(offsetX, cellSize*(maze.length-1)+offsetY, cellSize, cellSize));
        
        ArrayList<Shape> regions = new ArrayList<Shape>();
        while (shuffle.size() > 0) {
        	int index = (int) (Math.random()*shuffle.size());
        	regions.add(shuffle.get(index));
        	shuffle.remove(index);
        }
        regions.add(new Rectangle(-cellSize/2, -cellSize/2, cellSize, cellSize));
        return regions;
    }
}
