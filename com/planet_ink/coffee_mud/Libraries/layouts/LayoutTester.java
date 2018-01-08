package com.planet_ink.coffee_mud.Libraries.layouts;
import java.util.*;
import java.text.*;

import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.*;
import com.planet_ink.coffee_mud.core.Directions;

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

public class LayoutTester
{
	
	private final static String[] DEFAULT_DIRECTIONS_LIST_COMPASS	= "North,South,East,West,Up,Down,There,Northeast,Northwest,Southeast,Southwest".split(",");
	
	public static void draw(LayoutManager layout, int size, int dir)
	{
		final List<LayoutNode> V=layout.generate(size, dir);
		final LayoutNode firstNode = (V.size()==0) ? null : V.get(0);

		System.out.println("Layout "+layout.name()+", size="+V.size()+", dir="+DEFAULT_DIRECTIONS_LIST_COMPASS[dir]+": "+continuityCheck(V));
		long lowestX=Long.MAX_VALUE;
		long lowestY=Long.MAX_VALUE;
		long highestX=Long.MIN_VALUE;
		long highestY=Long.MIN_VALUE;
		final Hashtable<Long,Vector<LayoutNode>> HY = new Hashtable<Long,Vector<LayoutNode>>();
		for(final LayoutNode ls : V)
		{
			if(ls.coord()[0]<lowestX)
				lowestX = ls.coord()[0];
			if(ls.coord()[1]<lowestY)
				lowestY = ls.coord()[1];
			if(ls.coord()[0]>highestX)
				highestX = ls.coord()[0];
			if(ls.coord()[1]>highestY)
				highestY = ls.coord()[1];
			if(!HY.containsKey(Long.valueOf(ls.coord()[1])))
				HY.put(Long.valueOf(ls.coord()[1]), new Vector<LayoutNode>());
			HY.get(Long.valueOf(ls.coord()[1])).add(ls);

		}
		for(long y=lowestY;y<=highestY;y++)
		{
			final Vector<LayoutNode> ys = HY.get(Long.valueOf(y));
			if(ys != null)
			{
				final Hashtable<Long,LayoutNode> H = new Hashtable<Long,LayoutNode>();
				for(final LayoutNode xs : ys) 
					H.put(Long.valueOf(xs.coord()[0]),xs);
				for(int i=0;i<3;i++)
				{
					for(long x=lowestX;x<=highestX;x++)
					{
						if(H.containsKey(Long.valueOf(x)))
						{
							LayoutNode n = H.get(Long.valueOf(x));
							System.out.print(n.getColorRepresentation((firstNode==n ? 'O':'*'),i));
						}
						else
							System.out.print("   ");
					}
					System.out.println("");
				}
			}
		}
	}

	public static boolean continuityCheck(List<LayoutNode> set)
	{
		for(int s=0;s<set.size();s++)
		{
			final LayoutNode node = set.get(s);
			for(final Iterator<LayoutNode> e=node.links().values().iterator();e.hasNext();)
			{
				if(!set.contains(e.next()))
					return false;
			}
		}
		return true;
	}

	public static void main(String[] args)
	{
		Directions.instance();
		final int d=Directions.NORTH;
		{
			draw(new ApartmentLayout(), 10, d);
			draw(new ApartmentLayout(), 25, d);
			draw(new ApartmentLayout(), 50, d);
			draw(new ApartmentLayout(), 100, d);
			draw(new BoxCityLayout(),25, d);
			draw(new BoxCityLayout(), 50, d);
			draw(new BoxCityLayout(), 100, d);
			draw(new BoxCitySquareLayout(), 25, d);
			draw(new BoxCitySquareLayout(), 50, d);
			draw(new BoxCitySquareLayout(), 100, d);
			draw(new CrossLayout(), 25, d);
			draw(new CrossLayout(), 50, d);
			draw(new CrossLayout(), 100, d);
			draw(new GridCityLayout(), 25, d);
			draw(new GridCityLayout(), 50, d);
			draw(new GridCityLayout(), 100, d);
			draw(new MazeLayout(), 25, d);
			draw(new MazeLayout(), 50, d);
			draw(new MazeLayout(), 100, d);
			draw(new TreeLayout(), 25, d);
			draw(new TreeLayout(), 50, d);
			draw(new TreeLayout(), 100, d);
		}
	}
}
