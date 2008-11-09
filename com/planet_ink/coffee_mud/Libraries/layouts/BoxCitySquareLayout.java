package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.Enumeration;
import java.util.Vector;

import com.planet_ink.coffee_mud.Libraries.layouts.AbstractLayout.LayoutNode;
import com.planet_ink.coffee_mud.core.Directions;
public class BoxCitySquareLayout extends BoxCityLayout 
{
	public String name() { return "BOXCITYSQUARE";}
	public void drawABox(LayoutSet laySet, int width, int height)
	{
		super.drawABox(laySet, width, height);
		int x = 0 + ((width - 0)/2);
		int y = -(0 + ((height - 0)/2));
		LayoutNode center = laySet.getNode(x,y);
		if(center == null)
		{
			center = new LayoutNode(x,y);
			laySet.use(center,"square");
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
			n = laySet.getNode(sq);
			if(n==null)
			{
				n = new LayoutNode(sq);
				laySet.use(n,"square");
			}
			else
				n.reType("square");
			lastNode.crossLink(n);
			lastNode = n;
		}
		LayoutNode n2;
		n = super.getNextNode(laySet, center, Directions.NORTH);
		if(n != null) center.crossLink(n);
		n2 = super.getNextNode(laySet, n, Directions.NORTH);
		if(n2 != null) n.crossLink(n2);
		n = super.getNextNode(laySet, center, Directions.SOUTH);
		if(n != null) center.crossLink(n);
		n2 = super.getNextNode(laySet, n, Directions.SOUTH);
		if(n2 != null) n.crossLink(n2);
		n = super.getNextNode(laySet, center, Directions.EAST);
		if(n != null) center.crossLink(n);
		n2 = super.getNextNode(laySet, n, Directions.EAST);
		if(n2 != null) n.crossLink(n2);
		n = super.getNextNode(laySet, center, Directions.WEST);
		if(n != null) center.crossLink(n);
		n2 = super.getNextNode(laySet, n, Directions.WEST);
		if(n2 != null) n.crossLink(n2);
		for(long[] sq : square) {
			n = laySet.getNode(sq);
			for(int d=0;d<4;d++) {
				n2 = AbstractLayout.getNextNode(laySet, n, d);
				if(n2!=null) {
					if(n.getLink(d)==n2) continue;
					if(!n2.isStreetLike()) continue;
					Enumeration<LayoutNode> nodes=n2.links.elements();
					LayoutNode p_1=(LayoutNode)nodes.nextElement();
					LayoutNode p_2=(LayoutNode)nodes.nextElement();
					n2.deLink();
					p_1.crossLink(p_2);
					laySet.unUse(n2);
				}
				n2 = AbstractLayout.makeNextNode(n, d);
				n.crossLink(n2);
				laySet.use(n2, "leaf");
			}
		}
	}
}
