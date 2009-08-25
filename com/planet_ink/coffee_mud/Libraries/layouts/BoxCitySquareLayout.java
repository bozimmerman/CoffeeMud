package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.Enumeration;
import java.util.Vector;

import com.planet_ink.coffee_mud.core.Directions;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutTypes;
public class BoxCitySquareLayout extends BoxCityLayout 
{
	public String name() { return "BOXCITYSQUARE";}
	public void drawABox(LayoutSet lSet, int width, int height)
	{
		lSet.drawABox(width, height);
		int x = 0 + ((width - 0)/2);
		int y = -(0 + ((height - 0)/2));
		LayoutNode center = lSet.getNode(x,y);
		if(center == null)
		{
			center = new DefaultLayoutNode(x,y);
			lSet.use(center,LayoutTypes.square);
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
		if(n != null) center.crossLink(n);
		n2 = lSet.getNextNode( n, Directions.NORTH);
		if((n2 != null)&&(n!=null)) n.crossLink(n2);
		n = lSet.getNextNode( center, Directions.SOUTH);
		if(n != null) center.crossLink(n);
		n2 = lSet.getNextNode( n, Directions.SOUTH);
		if((n2 != null)&&(n!=null)) n.crossLink(n2);
		n = lSet.getNextNode( center, Directions.EAST);
		if(n != null) center.crossLink(n);
		n2 = lSet.getNextNode( n, Directions.EAST);
		if((n2 != null)&&(n!=null)) n.crossLink(n2);
		n = lSet.getNextNode( center, Directions.WEST);
		if(n != null) center.crossLink(n);
		n2 = lSet.getNextNode( n, Directions.WEST);
		if((n2 != null)&&(n!=null)) n.crossLink(n2);
		for(long[] sq : square) {
			n = lSet.getNode(sq);
			for(int d=0;d<4;d++) {
				n2 = lSet.getNextNode( n, d);
				if(n2!=null) {
					if(n.getLink(d)==n2) continue;
					if(!n2.isStreetLike()) continue;
					Enumeration<LayoutNode> nodes=n2.links().elements();
					LayoutNode p_1=(LayoutNode)nodes.nextElement();
					LayoutNode p_2=(LayoutNode)nodes.nextElement();
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
