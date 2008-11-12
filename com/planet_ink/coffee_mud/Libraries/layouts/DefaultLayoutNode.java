package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.Enumeration;
import java.util.Hashtable;

import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.core.Directions;

public class DefaultLayoutNode implements LayoutNode
{
	public long[] coord;
	public Room associatedRoom = null;
	public Hashtable<Integer,LayoutNode> links = new Hashtable<Integer,LayoutNode>();
	private Hashtable<String,String> tags = new Hashtable<String,String>();
	public DefaultLayoutNode(long[] coord) {
		this.coord = coord;
	}
	public DefaultLayoutNode(long x, long y) {
		this.coord = new long[]{x,y};
	}
	public Room room() { return associatedRoom;}
	public void setRoom(Room room){associatedRoom=room;}
	public long[] coord(){ return coord;}
	public Hashtable<String,String> tags(){ return tags;}
	public Hashtable<Integer,LayoutNode> links() { return links;}
	public void crossLink(LayoutNode to) {
		links.put(Integer.valueOf(AbstractLayout.getDirection(this,to)),to);
		to.links().put(Integer.valueOf(AbstractLayout.getDirection(to,this)),this);
	}
	public void delLink(LayoutNode linkNode) {
		for(Enumeration<Integer> e=links.keys();e.hasMoreElements();)
		{
			Integer key=e.nextElement();
			if(links.get(key)==linkNode)
				links.remove(key);
		}
	}
	public LayoutNode getLink(int d) { return links.get(d);}
	
	public boolean isStreetLike() {
		if(links.size()!=2) return false;
		Enumeration<LayoutNode> linksE=links.elements();
		LayoutNode n1=linksE.nextElement();
		LayoutNode n2=linksE.nextElement();
		int d1=AbstractLayout.getDirection(this, n1);
		int d2=AbstractLayout.getDirection(this, n2);
		switch(d1)
		{
		case Directions.NORTH: return d2==Directions.SOUTH;
		case Directions.SOUTH: return d2==Directions.NORTH;
		case Directions.EAST: return d2==Directions.WEST;
		case Directions.WEST: return d2==Directions.EAST;
		}
		return false;
	}
	public void deLink() {
		for(Enumeration<Integer> e=links.keys();e.hasMoreElements();)
		{
			Integer key=e.nextElement();
			LayoutNode linkNode=links.get(key);
			linkNode.delLink(this);
		}
		links.clear();
	}
	public String toString() {
		String s= "("+coord[0]+","+coord[1]+") ->";
		for(LayoutNode n : links.values())
			s+= "("+n.coord()[0]+","+n.coord()[1]+"),  ";
		return s;
	}
	public void flag(String flag) {
		String s=tags.get("NODEFLAGS");
		if(s==null)
			tags.put("NODEFLAGS",","+flag.toLowerCase()+",");
		else
		if(s.indexOf(","+flag.toLowerCase()+",")<0)
			tags.put("NODEFLAGS",s+flag.toLowerCase()+",");
	}
	public void flagRun(String dirs) {
		tags.put("NODERUN",dirs.toLowerCase().trim());
	}
	public String type(){ return tags.get("NODETYPE");}
	public void setExits(String dirs) {
		tags.put("NODEEXITS",dirs.toLowerCase().trim());
	}
	public String getExitsString() {return tags.get("NODEEXITS");}
	public void flagGateExit(String dir) {
		tags.put("NODEGATEEXIT",dir.toLowerCase().trim());
	}
	public void reType(String type) {
		tags.put("NODETYPE",type.toLowerCase().trim());
	}
	public String getRep(int line) {
		
		switch(line)
		{
			case 0:
				if(links.containsKey(Integer.valueOf(Directions.NORTH)))
					return " ^ ";
				return "   ";
			case 1:
			{
				if(links.containsKey(Integer.valueOf(Directions.EAST)))
				{
					if(links.containsKey(Integer.valueOf(Directions.WEST)))
						return "<*>";
					return " *>";
				}
				else
				if(links.containsKey(Integer.valueOf(Directions.WEST)))
					return "<* ";
				return " * ";
			}
			case 2:
				if(links.containsKey(Integer.valueOf(Directions.SOUTH)))
					return " v ";
				return "   ";
			default: return "   ";
		}
	}
}
