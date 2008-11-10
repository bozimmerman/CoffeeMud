package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.core.Directions;

public class LayoutSet 
{
	Random r = new Random();
	
	private long total  = 0;
	private final Hashtable<Long, LayoutNode> used = new Hashtable<Long, LayoutNode>();
	private Vector<LayoutNode> set = null;
	public LayoutSet(Vector<LayoutNode> V, long total)
	{
		this.total = total;
		this.set = V;
	}
	public Vector<LayoutNode> set() { return set;}
	public Long getHashCode(long x, long y){ return (Long.valueOf((x * total)+y));}
	public boolean isUsed(long[] xy) { return isUsed(xy[0],xy[1]); }
	public boolean isUsed(long x, long y) { return used.containsKey(getHashCode(x,y)); }
	public boolean isUsed(LayoutNode n) { return isUsed(n.coord())&&set.contains(n); }
	public void unUse(LayoutNode n) {
		used.remove(getHashCode(n.coord()[0],n.coord()[1]));
		set.remove(n);
	}
	public boolean use(LayoutNode n, String nodeType) {
		if(isUsed(n.coord())) 
			return false;
		used.put(getHashCode(n.coord()[0],n.coord()[1]),n);
		set.add(n);
		if(nodeType != null)
			n.reType(nodeType);
		return true;
	}
	public LayoutNode getNode(long[] xy) { return getNode(xy[0],xy[1]);}
	public LayoutNode getNode(long x, long y) { return used.get(getHashCode(x,y));}
	public boolean spaceAvailable() { return set.size() < total; }
	
	public long[] makeNextCoord(long[] n, int dir)
	{
		switch(dir)
		{
		case Directions.NORTH:
			return new long[]{n[0],n[1]-1};
		case Directions.SOUTH:
			return new long[]{n[0],n[1]+1};
		case Directions.EAST:
			return new long[]{n[0]+1,n[1]};
		case Directions.WEST:
			return new long[]{n[0]-1,n[1]};
		}
		return null;
	}
	
	public LayoutNode makeNextNode(LayoutNode n, int dir)
	{
		long[] l = makeNextCoord(n.coord(),dir);
		if(l!=null) return new DefaultLayoutNode(l);
		return null;
	}
	
	public LayoutNode getNextNode(LayoutNode n, int dir)
	{
		LayoutNode next = makeNextNode(n,dir);
		return getNode(next.coord());
	}
	

	public void drawABox(int width, int height)
	{
		LayoutNode n = new DefaultLayoutNode(new long[]{0,0});
		n.flag("corner");
		for(int y=0;y<height-1;y++)
		{
			use(n,"surround");
			LayoutNode nn = getNextNode(n, Directions.NORTH);
			if(nn==null) nn=makeNextNode(n, Directions.NORTH);
			n.crossLink(nn);
			nn.tags().put("NODERUN","N,S");
			n=nn;
		}
		n.flag("corner");
		use(n,"surround");
		n = new DefaultLayoutNode(new long[]{width-1,0});
		n.flag("corner");
		for(int y=0;y<height-1;y++)
		{
			use(n,"surround");
			LayoutNode nn = getNextNode(n, Directions.NORTH);
			if(nn==null) nn=makeNextNode(n, Directions.NORTH);
			n.crossLink(nn);
			nn.tags().put("NODERUN","N,S");
			n=nn;
		}
		n.flag("corner");
		use(n,"surround");
		n = getNode(new long[]{0,0});
		n.flag("corner");
		for(int x=0;x<width-1;x++)
		{
			use(n,"surround");
			LayoutNode nn = getNextNode(n, Directions.EAST);
			if(nn==null) nn=makeNextNode(n, Directions.EAST);
			n.crossLink(nn);
			nn.tags().put("NODERUN","E,W");
			n=nn;
		}
		n.flag("corner");
		use(n,"surround");
		n = getNode(new long[]{0,-height+1});
		n.flag("corner");
		for(int x=0;x<width-1;x++)
		{
			use(n,"surround");
			LayoutNode nn = getNextNode(n, Directions.EAST);
			if(nn==null) nn=makeNextNode(n, Directions.EAST);
			n.crossLink(nn);
			nn.tags().put("NODERUN","E,W");
			n=nn;
		}
		n.flag("corner");
		use(n,"surround");
	}
	
	public boolean fillMaze(LayoutNode p)
	{
		Vector<Integer> dirs = new Vector<Integer>();
		for(int i=0;i<4;i++)
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
			LayoutNode p2 = getNextNode(p, dir.intValue());
			if(p2 == null)
			{
				p2 = makeNextNode(p, dir.intValue());
				p.crossLink(p2);
				use(p2,"interior");
				fillMaze(p2);
			} 
		}
		return true;
	}
	
	public void clipLongStreets()
	{
		@SuppressWarnings("unchecked")
		Vector<LayoutNode> set2=(Vector<LayoutNode>)set().clone();
		for(Enumeration<LayoutNode> e=set2.elements();e.hasMoreElements();)
		{
			LayoutNode p=e.nextElement();
			if(isUsed(p) && p.isStreetLike())
				for(int d=0;d<4;d++)
					if(p.getLink(d)==null)
					{
						LayoutNode p2 =getNextNode(p, d); 
						if((p2!=null)
						&&(!p.links().containsValue(p2)))
						{
							Enumeration<LayoutNode> nodes=p.links().elements();
							LayoutNode p_1=(LayoutNode)nodes.nextElement();
							LayoutNode p_2=(LayoutNode)nodes.nextElement();
							p.deLink();
							p_1.crossLink(p_2);
							unUse(p);
							LayoutNode p3 = makeNextNode(p2, Directions.getOpDirectionCode(d));
							p2.crossLink(p3);
							use(p3, "leaf");
							break;
						}
					}
		}
	}
	
	public void fillInFlags()
	{
		for(Enumeration<LayoutNode> e=set().elements();e.hasMoreElements();)
		{
			LayoutNode n = (LayoutNode)e.nextElement();
			StringBuffer exits = new StringBuffer("");
			for(int d=0;d<4;d++)
				if(getNextNode(n, d)!=null)
					if(exits.length()>0)
						exits.append(","+Directions.getDirectionChar(d));
					else
						exits.append(Directions.getDirectionChar(d));
			n.setExits(exits.toString());
			if(exits.length()==1)
				n.reType("leaf");
		}
		for(Enumeration<LayoutNode> e=set().elements();e.hasMoreElements();)
		{
			LayoutNode n = (LayoutNode)e.nextElement();
			if(n.links().size()==2)
			{
				String type = n.type();
				for(int d=0;d<4;d++)
				{
					LayoutNode n2=getNextNode(n, d);
					if((n2!=null)&&(n2.type().equalsIgnoreCase("leaf")))
						type="offleaf";
				}
				if(!type.equalsIgnoreCase("offleaf"))
					n.flag("corner");
				n.reType(type);
			}
			else
			if((n.links().size()==3)
			&&(((n.type().equalsIgnoreCase("street"))
				||(!n.type().equalsIgnoreCase("surround")))))
			{
				boolean allStreet = true;
				for(int d=0;d<4;d++)
				{
					LayoutNode n2=getNextNode(n, d);
					if((n2==null)
					||((!n2.type().equalsIgnoreCase("street"))
						&&(!n2.type().equalsIgnoreCase("surround"))))
							allStreet = false;
				}
				if(allStreet)
					n.flag("tee");
			}
			else
			if((n.links().size()==4)
			&&(((n.type().equalsIgnoreCase("street"))
				||(!n.type().equalsIgnoreCase("surround")))))
			{
				boolean allStreet = true;
				for(int d=0;d<4;d++)
				{
					LayoutNode n2=getNextNode(n, d);
					if((n2==null)
					||((!n2.type().equalsIgnoreCase("street"))
						&&(!n2.type().equalsIgnoreCase("surround"))))
							allStreet = false;
				}
				if(allStreet)
					n.flag("intersection");
			}
		}
	}
}
