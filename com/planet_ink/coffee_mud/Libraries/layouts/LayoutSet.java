package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutRuns;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutTypes;
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
	public boolean use(LayoutNode n, LayoutTypes nodeType) {
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
		n.flag(LayoutFlags.corner);
		for(int y=0;y<height-1;y++)
		{
			use(n,LayoutTypes.surround);
			LayoutNode nn = getNextNode(n, Directions.NORTH);
			if(nn==null) nn=makeNextNode(n, Directions.NORTH);
			n.crossLink(nn);
			nn.flagRun(LayoutRuns.ns);
			n=nn;
		}
		n.flag(LayoutFlags.corner);
		use(n,LayoutTypes.surround);
		n = new DefaultLayoutNode(new long[]{width-1,0});
		n.flag(LayoutFlags.corner);
		for(int y=0;y<height-1;y++)
		{
			use(n,LayoutTypes.surround);
			LayoutNode nn = getNextNode(n, Directions.NORTH);
			if(nn==null) nn=makeNextNode(n, Directions.NORTH);
			n.crossLink(nn);
			nn.flagRun(LayoutRuns.ns);
			n=nn;
		}
		n.flag(LayoutFlags.corner);
		use(n,LayoutTypes.surround);
		n = getNode(new long[]{0,0});
		n.flag(LayoutFlags.corner);
		for(int x=0;x<width-1;x++)
		{
			use(n,LayoutTypes.surround);
			LayoutNode nn = getNextNode(n, Directions.EAST);
			if(nn==null) nn=makeNextNode(n, Directions.EAST);
			n.crossLink(nn);
			nn.flagRun(LayoutRuns.ns);
			n=nn;
		}
		n.flag(LayoutFlags.corner);
		use(n,LayoutTypes.surround);
		n = getNode(new long[]{0,-height+1});
		n.flag(LayoutFlags.corner);
		for(int x=0;x<width-1;x++)
		{
			use(n,LayoutTypes.surround);
			LayoutNode nn = getNextNode(n, Directions.EAST);
			if(nn==null) nn=makeNextNode(n, Directions.EAST);
			n.crossLink(nn);
			nn.flagRun(LayoutRuns.ns);
			n=nn;
		}
		n.flag(LayoutFlags.corner);
		use(n,LayoutTypes.surround);
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
				use(p2,LayoutTypes.interior);
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
							use(p3, LayoutTypes.leaf);
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
			int[] dirs=new int[n.links().size()];
			int x=0;
			for(Integer dirLink : n.links().keySet())
				dirs[x++]=dirLink.intValue();
			n.setExits(dirs);
			if((dirs.length==1)&&(!n.isFlagged(LayoutFlags.gate)))
				n.reType(LayoutTypes.leaf);
		}
		for(Enumeration<LayoutNode> e=set().elements();e.hasMoreElements();)
		{
			LayoutNode n = (LayoutNode)e.nextElement();
			if(n.links().size()==2)
			{
				LayoutFlags flag = null;
				if(n.type()==LayoutTypes.interior)
					for(Integer dirLink : n.links().keySet())
					{
						LayoutNode n2=n.links().get(dirLink);
						if((n2!=null)&&(n2.type()==LayoutTypes.leaf))
							flag=LayoutFlags.offleaf;
					}
				if(flag!=null)
					n.flag(flag);
				else
				{
					Enumeration<Integer> dirs=n.links().keys();
					Integer lN1=dirs.nextElement();
					Integer lN2=dirs.nextElement();
					if(lN1.intValue() != Directions.getOpDirectionCode(lN2.intValue()))
						n.flag(LayoutFlags.corner);
				}
			}
			else
			if((n.links().size()==3)
			&&(((n.type()==LayoutTypes.street)
				||(n.type()!=LayoutTypes.surround))))
			{
				boolean allStreet = true;
				for(Integer dirLink : n.links().keySet())
				{
					LayoutNode n2=n.links().get(dirLink);
					if((n2==null)
					||((n2.type()!=LayoutTypes.street)
						&&(n2.type()!=LayoutTypes.surround)))
							allStreet = false;
				}
				if(allStreet)
					n.flag(LayoutFlags.tee);
			}
			else
			if((n.links().size()==4)
			&&(((n.type()==LayoutTypes.street)
				||(n.type()!=LayoutTypes.surround))))
			{
				boolean allStreet = true;
				for(Integer dirLink : n.links().keySet())
				{
					LayoutNode n2=n.links().get(dirLink);
					if((n2==null)
					||((n2.type()!=LayoutTypes.street)
						&&(n2.type()!=LayoutTypes.surround)))
							allStreet = false;
				}
				if(allStreet)
					n.flag(LayoutFlags.intersection);
			}
		}
	}
}
