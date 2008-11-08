package com.planet_ink.coffee_mud.Libraries.layouts;
import java.util.*;
import java.text.*;

import com.planet_ink.coffee_mud.core.Directions;

public class LayoutTester
{
	public static void draw(AbstractLayout layout, int size, int dir)
	{
		Vector<AbstractLayout.LayoutNode> V=layout.generate(size, dir);
		
		System.out.println("Layout "+layout.name()+", size="+V.size());
		long lowestX=Long.MAX_VALUE;
		long lowestY=Long.MAX_VALUE;
		long highestX=Long.MIN_VALUE;
		long highestY=Long.MIN_VALUE;
		Hashtable<Long,Vector<AbstractLayout.LayoutNode>> HY = new Hashtable<Long,Vector<AbstractLayout.LayoutNode>>();
		for(AbstractLayout.LayoutNode ls : V)
		{
			if(ls.coord[0]<lowestX) lowestX = ls.coord[0];
			if(ls.coord[1]<lowestY) lowestY = ls.coord[1];
			if(ls.coord[0]>highestX) highestX = ls.coord[0];
			if(ls.coord[1]>highestY) highestY = ls.coord[1];
			if(!HY.containsKey(ls.coord[1])) 
				HY.put(ls.coord[1], new Vector<AbstractLayout.LayoutNode>());
			HY.get(ls.coord[1]).add(ls);
			
		}
		for(long y=lowestY;y<=highestY;y++)
		{
			Vector<AbstractLayout.LayoutNode> ys = HY.get(Long.valueOf(y));
			if(ys != null)
			{
				Hashtable<Long,AbstractLayout.LayoutNode> H = new Hashtable<Long,AbstractLayout.LayoutNode>();
				for(AbstractLayout.LayoutNode xs : ys) H.put(Long.valueOf(xs.coord[0]),xs);
				for(int i=0;i<3;i++)
				{
					for(long x=lowestX;x<=highestX;x++)
						if(H.containsKey(Long.valueOf(x)))
							System.out.print(H.get(Long.valueOf(x)).getRep(i));
						else
							System.out.print("   ");
					System.out.println("");
				}
			}
		}
	}
	
	public static void main(String[] args)
	{
		Directions.instance();
		draw(new BoxCityLayout(),25, Directions.NORTH);
		draw(new BoxCityLayout(), 50, Directions.NORTH);
		draw(new BoxCitySquareLayout(), 25, Directions.NORTH);
		draw(new BoxCitySquareLayout(), 50, Directions.NORTH);
		draw(new CrossLayout(), 25, Directions.NORTH);
		draw(new CrossLayout(), 50, Directions.NORTH);
		draw(new GridCityLayout(), 25, Directions.NORTH);
		draw(new GridCityLayout(), 50, Directions.NORTH);
		draw(new MazeLayout(), 25, Directions.NORTH);
		draw(new MazeLayout(), 50, Directions.NORTH);
		draw(new TreeLayout(), 25, Directions.NORTH);
		draw(new TreeLayout(), 50, Directions.NORTH);
	}
}
