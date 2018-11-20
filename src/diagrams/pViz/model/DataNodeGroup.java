package diagrams.pViz.model;

import java.util.ArrayList;
import java.util.List;

import diagrams.pViz.view.VNode;
import javafx.geometry.BoundingBox;
import javafx.scene.layout.AnchorPane;
import model.AttributeMap;


@SuppressWarnings("serial")
public class DataNodeGroup extends DataNode {

//	protected VNode stack;
//	AnchorPane anchorPane;
	private double minX = Double.MAX_VALUE;
	private double maxX = Double.MIN_VALUE;
	private double minY = Double.MAX_VALUE;
	private double maxY = Double.MIN_VALUE;
	private List<DataNode> members = new ArrayList<DataNode>();
//	public List<DataNode> getChildren() { return children ; } 
	public void addMember(DataNode child)	{		members.add(child);	}
	public void clearMembers() 				{		members.clear();	}
	public List<DataNode> getMembers() 		{		return members;	}
//	public AnchorPane getAnchorPane()		{ 		return anchorPane;	}
	public DataNodeGroup(AttributeMap map, Model m)
	{
		super(map,m);
//		anchorPane = new AnchorPane();
//		stack.getChildren().add(anchorPane);
	}	

	public void calcBounds()
	{
		for (DataNode child : members)
		{
			double centerX =child.getDouble("CenterX");
			double centerY =child.getDouble("CenterY");
			double width =child.getDouble("Width");
			double height =child.getDouble("Height");
			double halfwidth = width / 2;
			double halfheight = height / 2;
			if (centerX - halfwidth < minX)  minX = centerX - halfwidth;
			if (centerX + halfwidth > maxX)  maxX = centerX + halfwidth;
			if (centerY - halfheight < minY)  minY = centerY - halfheight;
			if (centerY + halfheight > maxY)  maxY = centerY + halfheight;
		}
		putDouble("X", minX);
		putDouble("Y", minY);
		putDouble("CenterX", (minX + maxX) / 2.0);
		putDouble("CenterY", (minY + maxY) / 2.0);
		putDouble("Width",  maxX - minX);
		putDouble("Height",  maxY - minY);
	}

	public BoundingBox getBounds() {
		double padding = 5;
		return new BoundingBox(minX-padding,minY-padding,0, maxX-minX + 2 * padding, maxY-minY+ 2 * padding, 0);
	}

	static int counter = 4000;
	static String getNextId()	{ return "id" + counter++; }
//	public DataGroup(AttributeMap am, Model m)
//	{
//		super(am,m);
//	}

	public VNode getStack()					{		return stack;	}
	public void setStack(VNode st)			{		 stack = st;	}
	public Model getModel()					{		return model;	}
	public String getShapeType() 			{		return "GroupComponent";	}
	public String getType() 				{		return get("Type");	}
	public String getLabel() 				{		return get("TextLabel");	}

	public String getInfoStr()	{ return "GROUP HTML Template for " + getGraphId() + "\n" + toString();	}
//	@Override public String toString()	{ return getGraphId() + " = " + getName();	}
	//---------------------------------------------------------------------------------------
	public String toGPML()	{ 
		StringBuilder bldr = new StringBuilder();
		buildNodeOpen(bldr);
		buildXRefTag(bldr);
		buildNodeClose(bldr);
		return bldr.toString();
	}
	String elementType = "Group";
	String[]  nodeAttrs = {  "TextLabel", "GroupId", "GraphId", "Style"};
	private void buildNodeOpen(StringBuilder bldr) {
		bldr.append("<Group " + attributeList(nodeAttrs) + " >\n");
	}
	private void buildNodeClose(StringBuilder bldr) {		bldr.append("</Group>\n");	}


}
