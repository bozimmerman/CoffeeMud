package com.planet_ink.coffee_mud.Libraries.layouts;
import java.util.*;
import java.text.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.*;

import com.planet_ink.coffee_mud.core.Directions;

public class LayoutTester
{
	public static void draw(LayoutManager layout, int size, int dir)
	{
		Vector<LayoutNode> V=layout.generate(size, dir);
		
		System.out.println("Layout "+layout.name()+", size="+V.size()+": "+continuityCheck(V));
		long lowestX=Long.MAX_VALUE;
		long lowestY=Long.MAX_VALUE;
		long highestX=Long.MIN_VALUE;
		long highestY=Long.MIN_VALUE;
		Hashtable<Long,Vector<LayoutNode>> HY = new Hashtable<Long,Vector<LayoutNode>>();
		for(LayoutNode ls : V)
		{
			if(ls.coord()[0]<lowestX) lowestX = ls.coord()[0];
			if(ls.coord()[1]<lowestY) lowestY = ls.coord()[1];
			if(ls.coord()[0]>highestX) highestX = ls.coord()[0];
			if(ls.coord()[1]>highestY) highestY = ls.coord()[1];
			if(!HY.containsKey(Long.valueOf(ls.coord()[1]))) 
				HY.put(Long.valueOf(ls.coord()[1]), new Vector<LayoutNode>());
			HY.get(Long.valueOf(ls.coord()[1])).add(ls);
			
		}
		for(long y=lowestY;y<=highestY;y++)
		{
			Vector<LayoutNode> ys = HY.get(Long.valueOf(y));
			if(ys != null)
			{
				Hashtable<Long,LayoutNode> H = new Hashtable<Long,LayoutNode>();
				for(LayoutNode xs : ys) H.put(Long.valueOf(xs.coord()[0]),xs);
				for(int i=0;i<3;i++)
				{
					for(long x=lowestX;x<=highestX;x++)
						if(H.containsKey(Long.valueOf(x)))
							System.out.print(H.get(Long.valueOf(x)).getColorRepresentation(i));
						else
							System.out.print("   ");
					System.out.println("");
				}
			}
		}
	}
	public static boolean continuityCheck(Vector<LayoutNode> set)
	{
		for(int s=0;s<set.size();s++)
		{
			LayoutNode node = set.elementAt(s);
			for(Enumeration<LayoutNode> e=node.links().elements();e.hasMoreElements();)
				if(!set.contains(e.nextElement()))
					return false;
		}
		return true;
	}
	
	public static void main(String[] args)
	{
		Directions.instance();
		int d=Directions.NORTH;
		{
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
