package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.Vector;
import com.planet_ink.coffee_mud.core.Directions;
public class BoxCitySquareLayout extends BoxCityLayout 
{
	public String name() { return "BOXCITYSQUARE";}
	public void drawABox(LayoutSet d, int width, int height)
	{
		super.drawABox(d, width, height);
		int x = 0 + ((width - 0)/2);
		int y = -(0 + ((height - 0)/2));
		LayoutNode center = d.getNode(x,y);
		if(center == null)
		{
			center = new LayoutNode(x,y);
			d.use(center,"square");
		}
		Vector<long[]> square = new Vector<long[]>();
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
		for(long[] sq : square)
		{
			n = d.getNode(sq);
			if(n==null)
			{
				n = new LayoutNode(sq);
				d.use(n,"square");
			}
			else
				n.reType("square");
			lastNode.crossLink(n);
			lastNode = n;
		}
		n = super.getNextNode(d, center, Directions.NORTH);
		if(n != null) center.crossLink(n);
		n = super.getNextNode(d, center, Directions.SOUTH);
		if(n != null) center.crossLink(n);
		n = super.getNextNode(d, center, Directions.EAST);
		if(n != null) center.crossLink(n);
		n = super.getNextNode(d, center, Directions.WEST);
		if(n != null) center.crossLink(n);
	}
	
}
