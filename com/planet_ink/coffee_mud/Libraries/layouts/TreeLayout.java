package com.planet_ink.coffee_mud.Libraries.layouts;
import java.util.*;

import com.planet_ink.coffee_mud.Libraries.layouts.AbstractLayout.LayoutNode;
import com.planet_ink.coffee_mud.core.Directions;

public class TreeLayout extends AbstractLayout
{
	public String name(){return "TREE";}
	int originalDirection=Directions.NORTH;
	
	private class TreeStem
	{
		public LayoutNode currNode = null;
		private int dir = Directions.NORTH;
		private LayoutSet d = null;
		public TreeStem(long[] coord, int dir, LayoutSet d)
		{
			this.dir = dir;
			this.d = d;
			currNode = new LayoutNode(coord);
		}
		private long[] getCoord(long[] curr, int dir) { return makeNextCoord(curr,dir);}
		
		private int[] getTurns(int dir) 
		{ 
			switch(dir)
			{
			case Directions.NORTH:
			case Directions.SOUTH:
				if(originalDirection==Directions.EAST) return new int[]{Directions.EAST};
				if(originalDirection==Directions.WEST) return new int[]{Directions.WEST};
				return new int[]{Directions.WEST,Directions.EAST};
			case Directions.EAST:
			case Directions.WEST:
				if(originalDirection==Directions.NORTH) return new int[]{Directions.NORTH};
				if(originalDirection==Directions.SOUTH) return new int[]{Directions.SOUTH};
				return new int[]{Directions.NORTH,Directions.SOUTH};
			}
			return null;
		}
		
		public TreeStem nextNode() 
		{
			long[] nextCoord = getCoord(currNode.coord,dir);
			TreeStem stem = new TreeStem(nextCoord,dir,d);
			if(!d.use(stem.currNode,"street")) return null;
			currNode.crossLink(stem.currNode);
			patchRun(currNode,stem.currNode);
			return stem;
		}

		private void patchRun(LayoutNode from, LayoutNode to) {
			to.flagRun(AbstractLayout.getRunDirection(getDirection(from,to)));
		}
		
		public TreeStem firstBranch() {
			int[] turns = getTurns(dir);
			if((turns == null)||(turns.length<1)) return null;
			long[] nextCoord = getCoord(currNode.coord,turns[0]);
			TreeStem newStem =  new TreeStem(nextCoord,turns[0],d);
			if(!d.use(newStem.currNode,"street")) return null;
			currNode.flag("corner");
			currNode.crossLink(newStem.currNode);
			patchRun(currNode,newStem.currNode);
			return newStem;
		}
		public TreeStem secondBranch() {
			int[] turns = getTurns(dir);
			if((turns == null)||(turns.length<2)) return null;
			long[] nextCoord = getCoord(currNode.coord,turns[1]);
			TreeStem newStem =  new TreeStem(nextCoord,turns[1],d);
			if(!d.use(newStem.currNode,"street")) return null;
			currNode.crossLink(newStem.currNode);
			patchRun(currNode,newStem.currNode);
			return newStem;
		}
	}
	
	public Vector<AbstractLayout.LayoutNode> generate(int num, int dir) {
		Vector<AbstractLayout.LayoutNode> set = new Vector<AbstractLayout.LayoutNode>();
		Vector<TreeStem> progress = new Vector<TreeStem>();
		
		long[] rootCoord = new long[]{0,0};
		LayoutSet lSet = new LayoutSet(set,num);
		originalDirection=dir;
		TreeStem root = new TreeStem(rootCoord, dir, lSet);
		progress.add(root);
		lSet.use(root.currNode,"street");
		root.currNode.flag("gate");
		root.currNode.flagGateExit(Directions.getDirectionChar(dir));
		root.currNode.flagRun(AbstractLayout.getRunDirection(dir));
		
		while(lSet.spaceAvailable()) {
			Vector<TreeStem> newOnes = new Vector<TreeStem>();
			for(Iterator<TreeStem> i =  progress.iterator(); i.hasNext() && lSet.spaceAvailable(); )
			{
				TreeStem stem = i.next();
				TreeStem branch = stem.nextNode();
				if(branch != null) newOnes.add(branch);
				branch = stem.firstBranch();
				if(branch != null) newOnes.add(branch);
				branch = stem.secondBranch();
				if(branch != null) newOnes.add(branch);
			}
			progress = new Vector<TreeStem>();
			while(newOnes.size()> 0)
			{
				TreeStem b =  newOnes.elementAt(r.nextInt(newOnes.size()));
				progress.add(b);
				newOnes.remove(b);
			}
		}
		clipLongStreets(lSet);
		fillInFlags(lSet);
		return set;
	}
}
