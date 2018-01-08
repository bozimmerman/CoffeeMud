package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.*;

import com.planet_ink.coffee_mud.core.Directions;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutRuns;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutTypes;
import com.planet_ink.coffee_mud.Locales.interfaces.*;

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

public class BoxCityLayout extends AbstractLayout
{
	@Override
	public String name()
	{
		return "BOXCITY";
	}

	public void halfLineN(LayoutSet lSet, int startX, int endX, int height, TreeSet<Integer> xposUsed)
	{
		final int x = startX + ((endX - startX)/2);
		if((x-startX)<3)
			return;
		LayoutNode n = lSet.getNode(new long[]{x,0});
		if(n!=null)
		{
			xposUsed.add(Integer.valueOf(x));
			for(int y=0;y<height-1;y++)
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
		}
		halfLineN(lSet,startX,x,height,xposUsed);
		halfLineN(lSet,x,endX,height,xposUsed);
	}

	public void halfLineE(LayoutSet lSet, int startY, int endY, int width, TreeSet<Integer> yposUsed)
	{
		final int y = startY + ((endY - startY)/2);
		if((startY-y)<3)
			return;
		LayoutNode n = lSet.getNode(new long[]{0,y});
		if(n!=null)
		{
			yposUsed.add(Integer.valueOf(y));
			for(int x=0;x<width-1;x++)
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
		}
		halfLineE(lSet,startY,y,width,yposUsed);
		halfLineE(lSet,y,endY,width,yposUsed);
	}

	public boolean fillMaze(LayoutSet lSet, LayoutNode p, int dir)
	{
		LayoutNode n = lSet.getNextNode(p, dir);
		if(n != null)
			return false;
		n = lSet.makeNextNode(p, dir);
		p.crossLink(n);
		lSet.use(n,LayoutTypes.interior);
		return lSet.fillMaze(n);
	}

	protected void drawABox(LayoutSet lSet, int width, int height)
	{
		lSet.drawABox(width, height);
	}

	@Override
	public List<LayoutNode> generate(int num, int dir)
	{
		final Vector<LayoutNode> set = new Vector<LayoutNode>();
		final int diameter = (int)Math.round(Math.sqrt(num));
		final int plusX = (diff(diameter,diameter,num) > diff(diameter+1,diameter,num)) ? 1 : 0;
		final LayoutSet lSet = new LayoutSet(set,num);
		drawABox(lSet,diameter+plusX,diameter);
		final TreeSet<Integer> yposUsed = new TreeSet<Integer>();
		final TreeSet<Integer> xposUsed = new TreeSet<Integer>();
		xposUsed.add(Integer.valueOf(0));
		halfLineN(lSet,0,diameter+plusX,diameter,xposUsed);
		xposUsed.add(Integer.valueOf(diameter+plusX-1));
		yposUsed.add(Integer.valueOf(0));
		halfLineE(lSet,0,-diameter,diameter+plusX,yposUsed);
		yposUsed.add(Integer.valueOf(-diameter+1));

		int x = 0;
		for(final Integer y : yposUsed)
		{
			Integer lastX = null;
			for(final Iterator<Integer> thisXE = xposUsed.iterator(); thisXE.hasNext();)
			{
				Integer thisX = thisXE.next();
				if(lastX != null)
				{
					x=lastX.intValue()+((thisX.intValue() - lastX.intValue()) / 2);
					if(y.intValue() > (-diameter+1))
					{
						if(!fillMaze(lSet, lSet.getNode(x, y.intValue()), Directions.NORTH))
							fillMaze(lSet, lSet.getNode(x+1, y.intValue()), Directions.NORTH);
					}
					if(thisXE.hasNext())
					{
						lastX = thisX;
						thisX = thisXE.next();
						x=lastX.intValue()+((thisX.intValue() - lastX.intValue()) / 2);
						if(y.intValue() < 0)
						{
							if(!fillMaze(lSet, lSet.getNode(x, y.intValue()), Directions.SOUTH))
								fillMaze(lSet, lSet.getNode(x+1, y.intValue()), Directions.SOUTH);
						}
					}
				}
				lastX = thisX;
			}
		}
		lSet.clipLongStreets();
		lSet.fillInFlags();
		LayoutNode n = null;
		int tryDiff=0;
		while((n==null)&&((++tryDiff)<10))
		{
			switch(dir)
			{
			case Directions.NORTH: n=lSet.getNode(new long[]{((diameter+plusX)/2)+tryDiff,0}); break;
			case Directions.SOUTH: n=lSet.getNode(new long[]{((diameter+plusX)/2)+tryDiff,-diameter+1}); break;
			case Directions.EAST: n=lSet.getNode(new long[]{0,((-diameter+1)/2)+tryDiff}); break;
			case Directions.WEST: n=lSet.getNode(new long[]{diameter+plusX-1,((-diameter+1)/2)+tryDiff}); break;
			}
			if((n!=null)&&(n.type()==LayoutTypes.leaf))
				n=null;
			if(n==null)
			{
				if(tryDiff>0)
					tryDiff=-tryDiff;
				else if(tryDiff<0) tryDiff=(-tryDiff)+1;
				else tryDiff++;
			}
		}
		if(n!=null)
		{
			set.remove(n);
			set.insertElementAt(n,0);
		}
		else
			Log.errOut("BoxCityLayout","Not able to find start room for direction: "+dir);
		return set;
	}

}
