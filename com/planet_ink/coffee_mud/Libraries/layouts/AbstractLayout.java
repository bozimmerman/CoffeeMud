package com.planet_ink.coffee_mud.Libraries.layouts;
import java.util.*;

import com.planet_ink.coffee_mud.core.Directions;
/**
 * Abstract area layout pattern
 * node tags:
 * nodetype: surround, leaf, offleaf, street, square, interior
 * nodeexits: n,s,e,w, n,s, e,w, n,e,w, etc
 * nodeflags: corner, gate, intersection, tee
 * NODEGATEEXIT: (for gate, offleaf, square): n s e w etc
 * noderun: (for surround, street): n,s e,w
 *  
 * @author Bo Zimmerman
 */
public abstract class AbstractLayout
{
	Random r = new Random();
	public static int getDirection(LayoutNode from, LayoutNode to)
	{
		if(to.coord[1]<from.coord[1]) return Directions.NORTH;
		if(to.coord[1]>from.coord[1]) return Directions.SOUTH;
		if(to.coord[0]<from.coord[0]) return Directions.WEST;
		if(to.coord[0]>from.coord[0]) return Directions.EAST;
		return -1;
	}
	
	public static class LayoutNode 
	{
		public long[] coord;
		public Hashtable<Integer,LayoutNode> links = new Hashtable<Integer,LayoutNode>();
		private Hashtable<String,String> tags = new Hashtable<String,String>();
		public LayoutNode(long[] coord) {
			this.coord = coord;
		}
		public LayoutNode(long x, long y) {
			this.coord = new long[]{x,y};
		}
		public void crossLink(LayoutNode to) {
			links.put(Integer.valueOf(getDirection(this,to)),to);
			to.links.put(Integer.valueOf(getDirection(to,this)),to);
		}
		public String toString() {
			String s= "("+coord[0]+","+coord[1]+") ->";
			for(LayoutNode n : links.values())
				s+= "("+n.coord[0]+","+n.coord[1]+"),  ";
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
	
	public static LayoutNode makeNextNode(LayoutNode n, int dir)
	{
		switch(dir)
		{
		case Directions.NORTH:
			return new LayoutNode(new long[]{n.coord[0],n.coord[1]-1});
		case Directions.SOUTH:
			return new LayoutNode(new long[]{n.coord[0],n.coord[1]+1});
		case Directions.EAST:
			return new LayoutNode(new long[]{n.coord[0]+1,n.coord[1]});
		case Directions.WEST:
			return new LayoutNode(new long[]{n.coord[0]-1,n.coord[1]});
		}
		return null;
	}
	
	public static LayoutNode getNextNode(LayoutSet set, LayoutNode n, int dir)
	{
		LayoutNode next = makeNextNode(n,dir);
		return set.getNode(next.coord);
	}
	
	protected class LayoutSet
	{
		private long total  = 0;
		private final Hashtable<Long, LayoutNode> used = new Hashtable<Long, LayoutNode>();
		private Vector<AbstractLayout.LayoutNode> set = null;
		public LayoutSet(Vector<AbstractLayout.LayoutNode> V, long total)
		{
			this.total = total;
			this.set = V;
		}
		public Long getHashCode(long x, long y){ return (Long.valueOf((x * total)+y));}
		public boolean isUsed(long[] xy) { return isUsed(xy[0],xy[1]); }
		public boolean isUsed(long x, long y) { return used.containsKey(getHashCode(x,y)); }
		public boolean use(LayoutNode n, String nodeType) {
			if(isUsed(n.coord)) return false;
			used.put(getHashCode(n.coord[0],n.coord[1]),n);
			set.add(n);
			if(nodeType != null)
				n.reType(nodeType);
			return true;
		}
		public LayoutNode getNode(long[] xy) { return getNode(xy[0],xy[1]);}
		public LayoutNode getNode(long x, long y) { return used.get(getHashCode(x,y));}
		public boolean spaceAvailable() { return set.size() < total; }
	}

	public void drawABox(LayoutSet d, int width, int height)
	{
		LayoutNode n = new LayoutNode(new long[]{0,0});
		n.flag("corner");
		for(int y=0;y<height-1;y++)
		{
			d.use(n,"surround");
			LayoutNode nn = getNextNode(d, n, Directions.NORTH);
			if(nn==null) nn=makeNextNode(n, Directions.NORTH);
			n.crossLink(nn);
			nn.tags.put("NODERUN","N,S");
			n=nn;
		}
		n.flag("corner");
		d.use(n,"surround");
		n = new LayoutNode(new long[]{width-1,0});
		n.flag("corner");
		for(int y=0;y<height-1;y++)
		{
			d.use(n,"surround");
			LayoutNode nn = getNextNode(d, n, Directions.NORTH);
			if(nn==null) nn=makeNextNode(n, Directions.NORTH);
			n.crossLink(nn);
			nn.tags.put("NODERUN","N,S");
			n=nn;
		}
		n.flag("corner");
		d.use(n,"surround");
		n = d.getNode(new long[]{0,0});
		n.flag("corner");
		for(int x=0;x<width-1;x++)
		{
			d.use(n,"surround");
			LayoutNode nn = getNextNode(d, n, Directions.EAST);
			if(nn==null) nn=makeNextNode(n, Directions.EAST);
			n.crossLink(nn);
			nn.tags.put("NODERUN","E,W");
			n=nn;
		}
		n.flag("corner");
		d.use(n,"surround");
		n = d.getNode(new long[]{0,-height+1});
		n.flag("corner");
		for(int x=0;x<width-1;x++)
		{
			d.use(n,"surround");
			LayoutNode nn = getNextNode(d, n, Directions.EAST);
			if(nn==null) nn=makeNextNode(n, Directions.EAST);
			n.crossLink(nn);
			nn.tags.put("NODERUN","E,W");
			n=nn;
		}
		n.flag("corner");
		d.use(n,"surround");
	}
	
	public int diff(int width, int height, int num) { 
		int x = width * height;
		return (x<num) ? (num - x) : (x - num); 
	}
	
	public void fillMaze(LayoutSet d, LayoutNode p)
	{
		Vector<Integer> dirs = new Vector<Integer>();
		for(int i=0;i<Directions.NUM_DIRECTIONS();i++)
			dirs.add(Integer.valueOf(i));
		Vector<Integer> rdirs = new Vector<Integer>();
		while(dirs.size()>0)
		{
			int x = r.nextInt(dirs.size());
			Integer dir = dirs.elementAt(x);
			dirs.removeElementAt(x);
			rdirs.addElement(dir);
		}
		for(int r=0;r<rdirs.size();r++)
		{
			Integer dir = rdirs.elementAt(r);
			LayoutNode p2 = getNextNode(d, p, dir.intValue());
			if(p2 == null)
			{
				p2 = makeNextNode(p, dir.intValue());
				p.crossLink(p2);
				d.use(p2,"interior");
				fillMaze(d,p2);
			}
		}
		
	}
	
	public void fillInFlags(LayoutSet laySet)
	{
		for(Enumeration<LayoutNode> e=laySet.set.elements();e.hasMoreElements();)
		{
			LayoutNode n = (LayoutNode)e.nextElement();
			StringBuffer exits = new StringBuffer("");
			for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				if(AbstractLayout.getNextNode(laySet, n, d)!=null)
					if(exits.length()>0)
						exits.append(","+Directions.getDirectionChar(d));
					else
						exits.append(Directions.getDirectionChar(d));
			n.setExits(exits.toString());
			if(exits.length()==1)
				n.reType("leaf");
		}
		for(Enumeration<LayoutNode> e=laySet.set.elements();e.hasMoreElements();)
		{
			LayoutNode n = (LayoutNode)e.nextElement();
			if(n.links.size()==2)
			{
				String type = n.type();
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					LayoutNode n2=AbstractLayout.getNextNode(laySet, n, d);
					if(n2.type().equalsIgnoreCase("leaf"))
						type="offleaf";
				}
				if(!type.equalsIgnoreCase("offleaf"))
					n.flag("corner");
				n.reType(type);
			}
			else
			if((n.links.size()==3)
			&&(((n.type().equalsIgnoreCase("street"))
				||(!n.type().equalsIgnoreCase("surround")))))
			{
				boolean allStreet = true;
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					LayoutNode n2=AbstractLayout.getNextNode(laySet, n, d);
					if((!n2.type().equalsIgnoreCase("street"))
					&&(!n2.type().equalsIgnoreCase("surround")))
						allStreet = false;
				}
				if(allStreet)
					n.flag("tee");
			}
			else
			if((n.links.size()==4)
			&&(((n.type().equalsIgnoreCase("street"))
				||(!n.type().equalsIgnoreCase("surround")))))
			{
				boolean allStreet = true;
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					LayoutNode n2=AbstractLayout.getNextNode(laySet, n, d);
					if((!n2.type().equalsIgnoreCase("street"))
					&&(!n2.type().equalsIgnoreCase("surround")))
						allStreet = false;
				}
				if(allStreet)
					n.flag("intersection");
			}
		}
	}
	
	public abstract String name();
	public abstract Vector<LayoutNode> generate(int num, int dir);
}