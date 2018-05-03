package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.Directions;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
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
public class BoxCitySquareLayout extends BoxCityLayout
{
	@Override
	public String name()
	{
		return "BOXCITYSQUARE";
	}

	@Override
	protected void drawABox(LayoutSet lSet, int width, int height)
	{
		lSet.drawABox(width, height);
		final int x = 0 + ((width - 0)/2);
		final int y = -(0 + ((height - 0)/2));
		LayoutNode center = lSet.getNode(x,y);
		if(center == null)
		{
			center = new DefaultLayoutNode(x,y);
			lSet.use(center,LayoutTypes.square);
		}
		final Vector<long[]> square = new Vector<long[]>();
		square.add(new long[]{x-1,y-1});
		square.add(new long[]{x,y-1});
		square.add(new long[]{x+1,y-1});
		square.add(new long[]{x+1,y});
		square.add(new long[]{x+1,y+1});
		square.add(new long[]{x,y+1});
		square.add(new long[]{x-1,y+1});
		square.add(new long[]{x-1,y});
		square.add(new long[]{x-1,y-1});
		LayoutNode n;
		LayoutNode lastNode = center;
		for(final long[] sq : square)
		{
			n = lSet.getNode(sq);
			if(n==null)
			{
				n = new DefaultLayoutNode(sq);
				lSet.use(n,LayoutTypes.square);
			}
			else
				n.reType(LayoutTypes.square);
			lastNode.crossLink(n);
			lastNode = n;
		}
		LayoutNode n2;
		n = lSet.getNextNode( center, Directions.NORTH);
		if(n != null)
			center.crossLink(n);
		n2 = lSet.getNextNode( n, Directions.NORTH);
		if((n2 != null)&&(n!=null))
			n.crossLink(n2);
		n = lSet.getNextNode( center, Directions.SOUTH);
		if(n != null)
			center.crossLink(n);
		n2 = lSet.getNextNode( n, Directions.SOUTH);
		if((n2 != null)&&(n!=null))
			n.crossLink(n2);
		n = lSet.getNextNode( center, Directions.EAST);
		if(n != null)
			center.crossLink(n);
		n2 = lSet.getNextNode( n, Directions.EAST);
		if((n2 != null)&&(n!=null))
			n.crossLink(n2);
		n = lSet.getNextNode( center, Directions.WEST);
		if(n != null)
			center.crossLink(n);
		n2 = lSet.getNextNode( n, Directions.WEST);
		if((n2 != null)&&(n!=null))
			n.crossLink(n2);
		for(final long[] sq : square)
		{
			n = lSet.getNode(sq);
			for(int d=0;d<4;d++)
			{
				n2 = lSet.getNextNode( n, d);
				if(n2!=null)
				{
					if(n.getLink(d)==n2)
						continue;
					if(!n2.isStreetLike())
						continue;
					final Iterator<LayoutNode> nodes=n2.links().values().iterator();
					final LayoutNode p_1=nodes.next();
					final LayoutNode p_2=nodes.next();
					n2.deLink();
					p_1.crossLink(p_2);
					lSet.unUse(n2);
				}
				n2 = lSet.makeNextNode(n, d);
				n.crossLink(n2);
				lSet.use(n2, LayoutTypes.leaf);
			}
		}
	}
}
