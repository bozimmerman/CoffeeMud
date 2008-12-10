package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.Vector;
import com.planet_ink.coffee_mud.core.Directions;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutRuns;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutTypes;

public class GridCityLayout extends AbstractLayout 
{
	public String name() { return "GRIDCITY";}
	
	public Vector<LayoutNode> generate(int num, int dir) 
	{
		Vector<LayoutNode> set = new Vector<LayoutNode>();
		int diameter = (int)Math.round(Math.sqrt((double)num));
		int plusX = (diff(diameter,diameter,num) > diff(diameter+1,diameter,num)) ? 1 : 0;
		
		LayoutSet lSet = new LayoutSet(set,num);
		lSet.drawABox(diameter+plusX,diameter);
		int middle=(diameter+plusX)/2;
		LayoutNode firstNode=null;
		for(int x=0;x<diameter+plusX;x+=2)
		{
			LayoutNode n = lSet.getNode(new long[]{x,0});
			if(n!=null)
			{
				if((dir==Directions.NORTH)&&(x>=middle-1)&&(x<=middle+1))
					firstNode=n;
				for(int y=0;y<diameter-1;y++)
				{
					lSet.use(n,LayoutTypes.street);
					n.flagRun(LayoutRuns.ns);
					LayoutNode nn = lSet.getNextNode(n, Directions.NORTH);
					if(nn==null) nn=lSet.makeNextNode(n, Directions.NORTH);
					n.crossLink(nn);
					n=nn;
				}
				lSet.use(n,LayoutTypes.street);
				n.flagRun(LayoutRuns.ns);
				if((dir==Directions.SOUTH)&&(x>=middle-1)&&(x<=middle+1))
					firstNode=n;
			}
		}
		int endX=diameter+plusX-1;
		middle=(-diameter+1)/2;
		for(int y=-2;y>=-diameter+1;y-=2)
		{
			LayoutNode n = lSet.getNode(new long[]{0,y});
			if(n!=null)
			{
				if((dir==Directions.EAST)&&(y>=middle-1)&&(y<=middle+1))
					firstNode=n;
				for(int x=0;x<endX;x++)
				{
					lSet.use(n,LayoutTypes.street);
					n.flagRun(LayoutRuns.ew);
					LayoutNode nn = lSet.getNextNode(n, Directions.EAST);
					if(nn==null) nn=lSet.makeNextNode(n, Directions.EAST);
					n.crossLink(nn);
					n=nn;
				}
				lSet.use(n,LayoutTypes.street);
				n.flagRun(LayoutRuns.ew);
				if((dir==Directions.WEST)&&(y>=middle-1)&&(y<=middle+1))
					firstNode=n;
			}
		}
		boolean north=true;
		for(int y=0;y>=(-diameter)+1;y-=2)
		{
			for(int x=1;x<endX;x++)
			{
				LayoutNode n = lSet.getNode(new long[]{x,y});
				LayoutNode nn = lSet.getNextNode(n, Directions.NORTH);
				if(nn==null)
				{
					if(north)
					{
						if(y>(-diameter)+2)
						{
							nn = lSet.makeNextNode( n, Directions.NORTH);
						}
					}
					if(nn != null)
					{
						n.crossLink(nn);
						lSet.use(nn,LayoutTypes.interior);
					}
					north = !north;
				}
			}
		}
		for(int y=-2;y>=(-diameter)+1;y-=2)
		{
			for(int x=1;x<endX;x++)
			{
				LayoutNode n = lSet.getNode(new long[]{x,y});
				LayoutNode nn = lSet.getNextNode(n, Directions.SOUTH);
				if(nn==null)
				{
					nn = lSet.makeNextNode( n, Directions.SOUTH);
					if(nn != null)
					{
						n.crossLink(nn);
						lSet.use(nn,LayoutTypes.interior);
					}
				}
			}
		}
		lSet.fillInFlags();
		if(firstNode != null)
		{
			set.remove(firstNode);
			set.insertElementAt(firstNode, 0);
		}
		else
			Log.errOut("GridCityLayout","Not able to find start room for direction: "+dir);
		return set;
	}

}
