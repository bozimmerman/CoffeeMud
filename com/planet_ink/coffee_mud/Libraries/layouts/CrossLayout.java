package com.planet_ink.coffee_mud.Libraries.layouts;
import java.util.*;
import com.planet_ink.coffee_mud.core.Directions;

public class CrossLayout extends AbstractLayout 
{
	public String name() { return "CROSS";}

	public void addRoom(LayoutSet d, LayoutNode n2, int dir)
	{
		if(d.spaceAvailable())
		{
			LayoutNode nn = getNextNode(d,n2,dir);
			if(nn == null)
			{
				nn = makeNextNode(n2,dir);
				d.use(nn,"leaf");
			}
			n2.crossLink(nn);
		}
	}
	
	public Vector<LayoutNode> generate(int num, int dir) {
		Vector<LayoutNode> set = new Vector<LayoutNode>();
		int diameter = (num / 3 / 2) + 1;
		LayoutSet d = new LayoutSet(set,num);
		LayoutNode n = new LayoutNode(new long[]{0,0});
		for(int x=0;x<diameter;x++)
		{
			d.use(n,"street");
			n.flagRun("N,S");
			LayoutNode nn = getNextNode(d, n, Directions.NORTH);
			if(nn==null) nn=makeNextNode(n, Directions.NORTH);
			n.crossLink(nn);
			n=nn;
		}
		n.flagRun("N,S");
		d.use(n,"street");
		n = new LayoutNode(new long[]{-(diameter/2),-(diameter/2)});
		for(int x=0;x<diameter;x++)
		{
			d.use(n,"street");
			n.flagRun("e,w");
			LayoutNode nn = getNextNode(d, n, Directions.EAST);
			if(nn==null) nn = makeNextNode(n, Directions.EAST);
			n.crossLink(nn);
			n=nn;
		}
		d.use(n,"street");
		n.flagRun("E,W");
		@SuppressWarnings("unchecked")
		Vector<LayoutNode> corridors = (Vector<LayoutNode>)set.clone();
		int x = 0;
		for(LayoutNode n2 : corridors)
		{
			if(x<diameter)
			{
				addRoom(d,n2,Directions.EAST);
				addRoom(d,n2,Directions.WEST);
			} 
			else
			{
				addRoom(d,n2,Directions.NORTH);
				addRoom(d,n2,Directions.SOUTH);
			}
			x++;
		}
		fillInFlags(d);
		return set;
	}

}
