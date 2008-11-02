package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.Vector;
import com.planet_ink.coffee_mud.core.Directions;

public class GridCityLayout extends AbstractLayout 
{
	public String name() { return "GRIDCITY";}
	
	public Vector<LayoutNode> generate(int num) 
	{
		Vector<LayoutNode> set = new Vector<LayoutNode>();
		int diameter = (int)Math.round(Math.sqrt((double)num));
		int plusX = (diff(diameter,diameter,num) > diff(diameter+1,diameter,num)) ? 1 : 0;
		
		LayoutSet d = new LayoutSet(set,num);
		drawABox(d,diameter+plusX,diameter);
		for(int x=0;x<diameter+plusX;x+=2)
		{
			LayoutNode n = d.getNode(new long[]{x,0});
			if(n!=null)
			{
				for(int y=0;y<diameter-1;y++)
				{
					d.use(n,"street");
					n.flagRun("n,s");
					LayoutNode nn = getNextNode(d, n, Directions.NORTH);
					if(nn==null) nn=makeNextNode(n, Directions.NORTH);
					n.crossLink(nn);
					n=nn;
				}
				d.use(n,"street");
				n.flagRun("n,s");
			}
		}
		int endX=diameter+plusX-1;
		for(int y=-2;y>=-diameter+1;y-=2)
		{
			LayoutNode n = d.getNode(new long[]{0,y});
			if(n!=null)
			{
				for(int x=0;x<endX;x++)
				{
					d.use(n,"street");
					n.flagRun("e,w");
					LayoutNode nn = getNextNode(d, n, Directions.EAST);
					if(nn==null) nn=makeNextNode(n, Directions.EAST);
					n.crossLink(nn);
					n=nn;
				}
				d.use(n,"street");
				n.flagRun("e,w");
			}
		}
		boolean north=true;
		for(int y=0;y>=(-diameter)+1;y-=2)
		{
			for(int x=1;x<endX;x++)
			{
				LayoutNode n = d.getNode(new long[]{x,y});
				LayoutNode nn = super.getNextNode(d, n, Directions.NORTH);
				if(nn==null)
				{
					if(north)
					{
						if(y>(-diameter)+2)
						{
							nn = super.makeNextNode( n, Directions.NORTH);
						}
					}
					if(nn != null)
					{
						n.crossLink(nn);
						d.use(nn,"interior");
					}
					north = !north;
				}
			}
		}
		for(int y=-2;y>=(-diameter)+1;y-=2)
		{
			for(int x=1;x<endX;x++)
			{
				LayoutNode n = d.getNode(new long[]{x,y});
				LayoutNode nn = super.getNextNode(d, n, Directions.SOUTH);
				if(nn==null)
				{
					nn = super.makeNextNode( n, Directions.SOUTH);
					if(nn != null)
					{
						n.crossLink(nn);
						d.use(nn,"interior");
					}
				}
			}
		}
		fillInFlags(d);
		return set;
	}

}
