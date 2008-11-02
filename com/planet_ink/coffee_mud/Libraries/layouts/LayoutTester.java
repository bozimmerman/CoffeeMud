package com.planet_ink.coffee_mud.Libraries.layouts;
import java.util.*;
import java.text.*;

public class LayoutTester
{
	public static void draw(Vector<AbstractLayout.LayoutNode> V)
	{
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
		System.out.println("Size="+V.size());
		//for(AbstractPattern.LayoutNode n : V) System.out.println(n.toString());
	}
	
	public static void main(String[] args)
	{
		draw(new BoxCitySquareLayout().generate(10));
		draw(new BoxCitySquareLayout().generate(20));
		draw(new BoxCitySquareLayout().generate(50));
		draw(new BoxCitySquareLayout().generate(70));
		draw(new BoxCitySquareLayout().generate(80));
		draw(new BoxCitySquareLayout().generate(100));
	}
}
