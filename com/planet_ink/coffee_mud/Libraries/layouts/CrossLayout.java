package com.planet_ink.coffee_mud.Libraries.layouts;
import java.util.*;

import com.planet_ink.coffee_mud.core.Directions;
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

public class CrossLayout extends AbstractLayout
{
	@Override
	public String name()
	{
		return "CROSS";
	}

	public void addRoom(LayoutSet lSet, LayoutNode n2, int dir)
	{
		if(lSet.spaceAvailable())
		{
			LayoutNode nn = lSet.getNextNode(n2,dir);
			if(nn == null)
			{
				nn = lSet.makeNextNode(n2,dir);
				lSet.use(nn,LayoutTypes.leaf);
				n2.crossLink(nn);
			}
		}
	}

	@Override
	public List<LayoutNode> generate(int num, int dir)
	{
		final Vector<LayoutNode> set = new Vector<LayoutNode>();
		final int diameter = (num / 3 / 2) + 1;
		final LayoutSet lSet = new LayoutSet(set,num);
		LayoutNode n = new DefaultLayoutNode(new long[]{0,0});
		LayoutNode firstNode = n;
		for(int x=0;x<diameter;x++)
		{
			lSet.use(n,LayoutTypes.street);
			n.flagRun(LayoutRuns.ns);
			LayoutNode nn = lSet.getNextNode(n, Directions.NORTH);
			if(nn==null)
				nn=lSet.makeNextNode(n, Directions.NORTH);
			n.crossLink(nn);
			n=nn;
		}
		n.flagRun(LayoutRuns.ns);
		lSet.use(n,LayoutTypes.street);
		if(dir==Directions.SOUTH)
			firstNode=n;
		n = new DefaultLayoutNode(new long[]{-(diameter/2),-(diameter/2)});
		if(dir==Directions.EAST)
			firstNode=n;
		for(int x=0;x<diameter;x++)
		{
			lSet.use(n,LayoutTypes.street);
			n.flagRun(LayoutRuns.ew);
			LayoutNode nn = lSet.getNextNode(n, Directions.EAST);
			if(nn==null)
				nn = lSet.makeNextNode(n, Directions.EAST);
			n.crossLink(nn);
			n=nn;
		}
		lSet.use(n,LayoutTypes.street);
		n.flagRun(LayoutRuns.ew);
		if(dir==Directions.WEST)
			firstNode=n;
		@SuppressWarnings("unchecked")
		final
		Vector<LayoutNode> corridors = (Vector<LayoutNode>)set.clone();
		int x = 0;
		for(final LayoutNode n2 : corridors)
		{
			if(x<diameter)
			{
				addRoom(lSet,n2,Directions.EAST);
				addRoom(lSet,n2,Directions.WEST);
			}
			else
			{
				addRoom(lSet,n2,Directions.NORTH);
				addRoom(lSet,n2,Directions.SOUTH);
			}
			x++;
		}
		lSet.fillInFlags();
		set.remove(firstNode);
		set.insertElementAt(firstNode,0);
		return set;
	}

}
