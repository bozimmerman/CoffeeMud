package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.Vector;
import com.planet_ink.coffee_mud.core.Directions;

public class MazeLayout extends AbstractLayout 
{
	public String name() { return "MAZE";}
	
	public void fillMaze(LayoutSet d, LayoutNode p, int width, int height)
	{
		Vector<Integer> dirs = new Vector<Integer>();
		for(int i=0;i<Directions.NUM_DIRECTIONS();i++)
			dirs.add(Integer.valueOf(i));
		Vector<Integer> rdirs = new Vector<Integer>();
		while(dirs.size()>0)
		{
			int x = r.nextInt(dirs.size());
			Integer dir = dirs.elementAt(x);
			dirs.removeElementAt(x);
			rdirs.addElement(dir);
		}
		for(int r=0;r<rdirs.size();r++)
		{
			Integer dir = rdirs.elementAt(r);
			LayoutNode p2 = super.makeNextNode(p, dir.intValue());
			if((!d.isUsed(p2.coord))
			&&(p2.coord[0]>=0)
			&&(p2.coord[1]<=0)
			&&(p2.coord[0]<width)
			&&(p2.coord[1]>-height))
			{
				d.use(p2,"interior");
				p.crossLink(p2);
				fillMaze(d,p2,width,height);
			}
		}
		
	}
	
	public Vector<LayoutNode> generate(int num, int dir) 
	{
		Vector<LayoutNode> set = new Vector<LayoutNode>();
		int diameter = (int)Math.round(Math.sqrt((double)num));
		int plusX = (diff(diameter,diameter,num) > diff(diameter+1,diameter,num)) ? 1 : 0;
		
		LayoutSet d = new LayoutSet(set,num);
		LayoutNode n = new LayoutNode(new long[]{diameter/2,0});
		d.use(n,"interior");
		n.flag("gate");
		n.flagGateExit("s");
		fillMaze(d,n,diameter+plusX,diameter);
		fillInFlags(d);
		return set;
	}

}
