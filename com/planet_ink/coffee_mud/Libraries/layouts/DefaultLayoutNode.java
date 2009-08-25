package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutRuns;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutTags;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutTypes;
import com.planet_ink.coffee_mud.Locales.interfaces.Room;
import com.planet_ink.coffee_mud.core.Directions;

public class DefaultLayoutNode implements LayoutNode
{
	public long[] coord;
	public Room associatedRoom = null;
	public Hashtable<Integer,LayoutNode> links = new Hashtable<Integer,LayoutNode>();
	private Hashtable<LayoutTags,String> tags = new Hashtable<LayoutTags,String>();
	private HashSet<LayoutFlags> flags = new HashSet<LayoutFlags>();
	
	public DefaultLayoutNode(long[] coord) {
		this.coord = coord;
	}
	public DefaultLayoutNode(long x, long y) {
		this.coord = new long[]{x,y};
	}
	public Room room() { return associatedRoom;}
	public void setRoom(Room room){associatedRoom=room;}
	public long[] coord(){ return coord;}
	public Hashtable<LayoutTags,String> tags(){ return tags;}
	public Hashtable<Integer,LayoutNode> links() { return links;}
	public void crossLink(LayoutNode to) {
		links.put(Integer.valueOf(AbstractLayout.getDirection(this,to)),to);
		to.links().put(Integer.valueOf(AbstractLayout.getDirection(to,this)),this);
	}
	public boolean isFlagged(LayoutFlags flag) { return flags.contains(flag);}
	public LayoutRuns getFlagRuns(){ 
		if(tags.containsKey(LayoutTags.NODERUN))
			return LayoutRuns.valueOf(tags.get(LayoutTags.NODERUN));
		return null;
	}
	public void delLink(LayoutNode linkNode) {
		for(Enumeration<Integer> e=links.keys();e.hasMoreElements();)
		{
			Integer key=e.nextElement();
			if(links.get(key)==linkNode)
				links.remove(key);
		}
	}
	public LayoutNode getLink(int d) { return links.get(Integer.valueOf(d));}
	
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
	public void flag(LayoutFlags flag) {
		String s=tags.get(LayoutTags.NODEFLAGS);
		flags.add(flag);
		if(s==null)
			tags.put(LayoutTags.NODEFLAGS,","+flag.toString()+",");
		else
		if(s.indexOf(","+flag.toString()+",")<0)
			tags.put(LayoutTags.NODEFLAGS,s+flag.toString()+",");
	}
	public void flagRun(LayoutRuns run) {
		tags.put(LayoutTags.NODERUN,run.toString());
	}
	public LayoutTypes type(){ return LayoutTypes.valueOf(tags.get(LayoutTags.NODETYPE));}
	public void setExits(int[] dirs) {
		StringBuffer buf=new StringBuffer(",");
		for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
			for(int i=0;i<dirs.length;i++)
				if(dirs[i]==d)
				{
					buf.append(Directions.getDirectionChar(d).toLowerCase())
					   .append(",");
				}
		tags.put(LayoutTags.NODEEXITS,buf.toString());
	}
	public void flagGateExit(int dir) {
		tags.put(LayoutTags.NODEGATEEXIT,Directions.getDirectionChar(dir).toLowerCase());
	}
	public void reType(LayoutTypes type) {
		tags.put(LayoutTags.NODETYPE,type.toString());
	}
	public String getColorRepresentation(int line) {
		
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
