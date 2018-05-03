package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.List;
import java.util.Vector;
import com.planet_ink.coffee_mud.core.Directions;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutRuns;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutTypes;

/*
   Copyright 2008-2018 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

public class GridCityLayout extends AbstractLayout
{
	@Override
	public String name()
	{
		return "GRIDCITY";
	}

	@Override
	public List<LayoutNode> generate(int num, int dir)
	{
		final Vector<LayoutNode> set = new Vector<LayoutNode>();
		final int diameter = (int)Math.round(Math.sqrt(num));
		final int plusX = (diff(diameter,diameter,num) > diff(diameter+1,diameter,num)) ? 1 : 0;

		final LayoutSet lSet = new LayoutSet(set,num);
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
					if(nn==null)
						nn=lSet.makeNextNode(n, Directions.NORTH);
					n.crossLink(nn);
					n=nn;
				}
				lSet.use(n,LayoutTypes.street);
				n.flagRun(LayoutRuns.ns);
				if((dir==Directions.SOUTH)&&(x>=middle-1)&&(x<=middle+1))
					firstNode=n;
			}
		}
		final int endX=diameter+plusX-1;
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
					if(nn==null)
						nn=lSet.makeNextNode(n, Directions.EAST);
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
				final LayoutNode n = lSet.getNode(new long[]{x,y});
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
				final LayoutNode n = lSet.getNode(new long[]{x,y});
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
