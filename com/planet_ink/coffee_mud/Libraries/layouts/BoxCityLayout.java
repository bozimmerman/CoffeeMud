package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.*;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import com.planet_ink.coffee_mud.core.Directions;

public class BoxCityLayout extends AbstractLayout 
{
	public String name() { return "BOXCITY";}
	
	public void halfLineN(LayoutSet d, int startX, int endX, int height, TreeSet<Integer> xposUsed)
	{
		int x = startX + ((endX - startX)/2);
		if((x-startX)<3) return;
		LayoutNode n = d.getNode(new long[]{x,0});
		if(n!=null)
		{
			xposUsed.add(Integer.valueOf(x));
			for(int y=0;y<height-1;y++)
			{
				d.use(n,"street");
				n.flagRun("n,s");
				LayoutNode nn = getNextNode(d, n, Directions.NORTH);
				if(nn==null) nn=makeNextNode(n, Directions.NORTH);
				n.crossLink(nn);
				n=nn;
			}
			d.use(n,"street");
			n.flagRun("n,s");
		}
		halfLineN(d,startX,x,height,xposUsed);
		halfLineN(d,x,endX,height,xposUsed);
	}
	
	public void halfLineE(LayoutSet d, int startY, int endY, int width, TreeSet<Integer> yposUsed)
	{
		int y = startY + ((endY - startY)/2);
		if((startY-y)<3) return;
		LayoutNode n = d.getNode(new long[]{0,y});
		if(n!=null)
		{
			yposUsed.add(Integer.valueOf(y));
			for(int x=0;x<width-1;x++)
			{
				d.use(n,"street");
				n.flagRun("n,s");
				LayoutNode nn = getNextNode(d, n, Directions.EAST);
				if(nn==null) nn=makeNextNode(n, Directions.EAST);
				n.crossLink(nn);
				n=nn;
			}
			d.use(n,"street");
			n.flagRun("n,s");
		}
		halfLineE(d,startY,y,width,yposUsed);
		halfLineE(d,y,endY,width,yposUsed);
	}
	
	public void fillMaze(LayoutSet d, LayoutNode p, int dir)
	{
		LayoutNode n = super.getNextNode(d, p, dir);
		if(n != null) return;
		n = super.makeNextNode(p, dir);
		p.crossLink(n);
		d.use(n,"interior");
		super.fillMaze(d, n);
	}
	
	public Vector<LayoutNode> generate(int num, int dir) 
	{
		Vector<LayoutNode> set = new Vector<LayoutNode>();
		int diameter = (int)Math.round(Math.sqrt((double)num));
		int plusX = (diff(diameter,diameter,num) > diff(diameter+1,diameter,num)) ? 1 : 0;
		
		LayoutSet d = new LayoutSet(set,num);
		drawABox(d,diameter+plusX,diameter);
		TreeSet<Integer> yposUsed = new TreeSet<Integer>();
		TreeSet<Integer> xposUsed = new TreeSet<Integer>();
		xposUsed.add(Integer.valueOf(0));
		halfLineN(d,0,diameter+plusX,diameter,xposUsed);
		xposUsed.add(Integer.valueOf(diameter+plusX-1));
		yposUsed.add(Integer.valueOf(0));
		halfLineE(d,0,-diameter,diameter+plusX,yposUsed);
		yposUsed.add(Integer.valueOf(-diameter+1));
		
		int x = 0;
		for(Integer y : yposUsed)
		{
			Integer lastX = null;
			for(Iterator<Integer> thisXE = xposUsed.iterator(); thisXE.hasNext();)
			{
				Integer thisX = thisXE.next();
				if(lastX != null)
				{
					x=lastX.intValue()+((thisX.intValue() - lastX.intValue()) / 2);
					if(y.intValue() > (-diameter+1))
						this.fillMaze(d, d.getNode(x, y.intValue()), Directions.NORTH);
					if(thisXE.hasNext())
					{
						lastX = thisX;
						thisX = thisXE.next();
						x=lastX.intValue()+((thisX.intValue() - lastX.intValue()) / 2);
						if(y.intValue() < 0)
							this.fillMaze(d, d.getNode(x, y.intValue()), Directions.SOUTH);
					}
				}
				lastX = thisX;
			}
		}
		fillInFlags(d);
		return set;
	}

}
