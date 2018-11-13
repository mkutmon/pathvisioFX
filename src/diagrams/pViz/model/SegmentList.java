package diagrams.pViz.model;

import java.util.ArrayList;
import java.util.List;

import org.pathvisio.core.model.ConnectorRestrictions;

import javafx.geometry.Point2D;

public class SegmentList extends ArrayList<Segment> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//----------------------------------------------------------------------
	public SegmentList(List<Point2D> points)
	{
	  	int len = points.size();
		Point2D mStart = points.get(0);
	  	Point2D mEnd = points.get(len-1);
//	  	if (verbose)	  	System.out.println("length: " + len + " points");
	  	
		double startRelX = 0; // mStart.getRelX();
		double startRelY = 0; //mStart.getRelY();
		double endRelX = 0; //mEnd.getRelX();
		double endRelY = 0; //mEnd.getRelY();
	  	Point2D startPt =  new Point2D(mStart.getX(), mStart.getY());
	  	Point2D endPt =  new Point2D(mEnd.getX(), mEnd.getY());
  	
		int startSide = Segment.getSide(startRelX, startRelY);
		if (startSide < 0)
			startSide = getNearestSide(startPt, endPt);
		int endSide = Segment.getSide(endRelX, endRelY);
		if (endSide < 0)
			endSide = getNearestSide(endPt, startPt);
		
	  	Point2D[] wps = calculateWayPoints(startPt, endPt, startSide, endSide);
	    calculateSegments(startPt, endPt, startSide, endSide, wps);
	}
	
	//----------------------------------------------------------------------
	private int getNearestSide(Point2D startPt, Point2D endPt) {
		double dx = endPt.getX() - startPt.getX();
		double dy= endPt.getY() - startPt.getY();
		if (Math.abs(dy) > Math.abs(dx) ) 
			return (dy > 0) ? ConnectorRestrictions.SIDE_SOUTH : ConnectorRestrictions.SIDE_NORTH;
		return (dx > 0) ? ConnectorRestrictions.SIDE_EAST : ConnectorRestrictions.SIDE_WEST;
	}

		private final static double SEGMENT_OFFSET = 40;


	protected Point2D[] calculateWayPoints(Point2D start, Point2D end, int startSide, int endSide) {
		int nrSegments = getNrSegments( start, end, startSide, endSide);

		//Else, calculate the default waypoints
		Point2D[] waypoints = new Point2D[nrSegments - 2];

		int startAxis = Segment.getSegmentAxis(startSide);
		int startDirection = Segment.getSegmentDirection(startSide);
		int endAxis = Segment.getSegmentAxis(endSide);
		int endDirection = Segment.getSegmentDirection(endSide);


		if(nrSegments - 2 == 1) {
			/*
			 * [S]---
			 * 		|
			 * 		---[S]
			 */
			waypoints[0] = calculateWayPoint(start, end, startAxis, startDirection);
		} else if(nrSegments - 2 == 2) {
			/*
			* [S]---
			* 		| [S]
			* 		|  |
			* 		|---
			*/
			double offset = SEGMENT_OFFSET * endDirection;
			Point2D pt = new Point2D( end.getX() + offset, end.getY() + offset );
			waypoints[0] = calculateWayPoint(start, pt, startAxis, startDirection);

			waypoints[1] = calculateWayPoint(end, waypoints[0], endAxis, endDirection);
		} else if(nrSegments - 2 == 3) {
			/*  -----
			 *  |   |
			 * [S]  | [S]
			 *      |  |
			 *      |---
			 */
			//Start with middle waypoint
			waypoints[1] = Segment.centerPoint(start, end); 
			waypoints[0] = calculateWayPoint(start, waypoints[1], startAxis, startDirection);
			waypoints[2] = calculateWayPoint(end, waypoints[1], endAxis, endDirection);
		}
		 else if(nrSegments - 2 == 4) {			// NEW code UNTESTED
		/*   ---------
		 *   |       |
		 *   ---[S]  | [S]
		 *           |  |
		 *           |---
		 */
				waypoints[2] = Segment.centerPoint(start, end); 
				waypoints[1] = calculateWayPoint(start, waypoints[2], startAxis, startDirection);
				waypoints[0] = calculateWayPoint(start, waypoints[1], startAxis, startDirection);
				waypoints[3] = calculateWayPoint(end, waypoints[1], endAxis, endDirection);
			}
	return waypoints;
	}


	String[] sides = {  "North", "East", "South", "West" };
	protected Point2D calculateWayPoint(Point2D start, Point2D end, int axis, int direction) {
		double x,y = 0;
		if(axis == Segment.AXIS_Y) {
			x = start.getX() + (end.getX() - start.getX()) / 2;
			y = start.getY() + SEGMENT_OFFSET * direction;
		} else {
			x = start.getX() + SEGMENT_OFFSET * direction;
			y = start.getY() + (end.getY() - start.getY()) / 2;
		}
		return new Point2D(x, y);
	}

	protected void calculateSegments(Point2D start, Point2D end, int startSide, int endSide, Point2D[] waypts) {
		int nrSegments = getNrSegments(start, end, startSide, endSide);
		Segment[] segments = new Segment[nrSegments];

		int startAxis = Segment.getSegmentAxis(startSide);
		if(nrSegments == 2) { //No waypoints
			segments[0] = Segment.createStraightSegment(start, end, startAxis);
			segments[1] = Segment.createStraightSegment(segments[0].getEnd(), end, Segment.getOppositeAxis(startAxis));
		} else 
		{
			segments[0] = Segment.createStraightSegment(start, waypts[0], startAxis );
			int axis = 1 - startAxis;
			for(int i = 0; i < waypts.length - 1; i++) {
				segments[i + 1] = Segment.createStraightSegment( segments[i].getEnd(), waypts[i + 1], axis );
				axis = Segment.getOppositeAxis(axis);
		}
		segments[segments.length - 2] = Segment.createStraightSegment( segments[segments.length -3].getEnd(), end, axis);
		segments[segments.length - 1] =Segment. createStraightSegment(
					segments[segments.length - 2].getEnd(), end, Segment.getSegmentAxis(endSide));
		}
		for (Segment s : segments) 
			add(s);
	}
	protected int getNrSegments(Point2D start, Point2D end, int startSide, int endSide) {

		boolean leftToRight = getDirectionX(start, end) > 0;

		Point2D left = leftToRight ? start : end;
		Point2D right = leftToRight ? end : start;
		boolean leftBottom = getDirectionY(left, right) < 0;

		int z = leftBottom ? 0 : 1;
		int x = leftToRight ? startSide : endSide;
		int y = leftToRight ? endSide : startSide;
		return getNrWaypoints(x, y, z) + 2;
	}
	/**
	 * Get the direction of the line on the x axis
	 * @param start The start point of the line
	 * @param end The end point of the line
	 * @return 1 if the direction is positive (from left to right),
	 * -1 if the direction is negative (from right to left)
	 */
	static int getDirectionX(Point2D start, Point2D end) {
		return (int)Math.signum(end.getX() - start.getX());
	}

	/**
	 * Get the direction of the line on the y axis
	 * @param start The start point of the line
	 * @param end The end point of the line
	 * @return 1 if the direction is positive (from top to bottom),
	 * -1 if the direction is negative (from bottom to top)
	 */
	protected int getDirectionY(Point2D start, Point2D end) {
		return (int)Math.signum(end.getY() - start.getY());
	}
	// R=RIGHT, L=LEFT, T=TOP, B=BOTTOM
	// N=NORTH, E=EAST, S=SOUTH, W=WEST
		/* The number of connector for each side and relative position
			RN	RE	RS	RW
	BLN		1	2	1	0
	TLN		1	2	3	2

	BLE		3	1	0	1
	TLE		0	1	2	1

	BLS		3	2	1	2
	TLS		1	2	1	0

	BLW		2	3	2	1
	TLW		2	3	2	1
		There should be some logic behind this, but hey, it's Friday...
		(so we just hard code the array)
		
	BUG:  	There should be some cases where 4 is returned !!  
		 */
		private int[][][] waypointNumbers;

		private int getNrWaypoints(int x, int y, int z) {
			waypointNumbers = new int[][][] {
				{	{ 1, 1 },	{ 2, 2 },	{ 1, 3 },	{ 0, 2 }	},
				{	{ 2, 0 }, 	{ 1, 1 }, 	{ 0, 2 }, 	{ 1, 1 },	},
				{	{ 3, 1 },	{ 2, 2 },	{ 1, 1 },	{ 2, 0 },	},
				{ 	{ 2, 2 },	{ 3, 3 },	{ 2, 2 },	{ 1, 1 },	}
			};
			return waypointNumbers[x][y][z];
		}

}
