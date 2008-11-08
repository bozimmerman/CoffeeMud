package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.Vector;
import com.planet_ink.coffee_mud.core.Directions;

public class GridCityLayout extends AbstractLayout 
{
	public String name() { return "GRIDCITY";}
	
	public Vector<LayoutNode> generate(int num, int dir) 
	{
		Vector<LayoutNode> set = new Vector<LayoutNode>();
		int diameter = (int)Math.round(Math.sqrt((double)num));
		int plusX = (diff(diameter,diameter,num) > diff(diameter+1,diameter,num)) ? 1 : 0;
		
		LayoutSet d = new LayoutSet(set,num);
		drawABox(d,diameter+plusX,diameter);
		int middle=(diameter+plusX)/2;
		LayoutNode firstNode=null;
		for(int x=0;x<diameter+plusX;x+=2)
		{
			LayoutNode n = d.getNode(new long[]{x,0});
			if(n!=null)
			{
				if((dir==Directions.NORTH)&&(x>=middle-1)&&(x<=middle+1))
					firstNode=n;
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
				if((dir==Directions.SOUTH)&&(x>=middle-1)&&(x<=middle+1))
					firstNode=n;
			}
		}
		int endX=diameter+plusX-1;
		middle=(-diameter+1)/2;
		for(int y=-2;y>=-diameter+1;y-=2)
		{
			LayoutNode n = d.getNode(new long[]{0,y});
			if(n!=null)
			{
				if((dir==Directions.EAST)&&(y>=middle-1)&&(y<=middle+1))
					firstNode=n;
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
				if((dir==Directions.WEST)&&(y>=middle-1)&&(y<=middle+1))
					firstNode=n;
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
		if(firstNode != null)
		{
			set.remove(firstNode);
			set.insertElementAt(firstNode, 0);
		}
		return set;
	}

}
