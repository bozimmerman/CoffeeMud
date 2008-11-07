package com.planet_ink.coffee_mud.Libraries.layouts;
import java.util.*;

import com.planet_ink.coffee_mud.core.Directions;

public class TreeLayout extends AbstractLayout
{
	public String name(){return "BUSH";}
	
	private class TreeStem
	{
		public LayoutNode currNode = null;
		private int[] dir = null;
		private LayoutSet d = null;
		public TreeStem(long[] coord, int[] dir, LayoutSet d)
		{
			this.dir = dir;
			this.d = d;
			currNode = new LayoutNode(coord);
		}
		private long[] getCoord(long[] curr, int[] dir) { return new long[]{curr[0]+dir[0],curr[1]+dir[1]}; }
		private int[][] getTurns(int[] dir) { return (dir[1]==0) ? new int[][]{{0,-1}} : new int[][]{{-1,0},{1,0}}; }
		public TreeStem nextNode() {
			long[] nextCoord = getCoord(currNode.coord,dir);
			TreeStem stem = new TreeStem(nextCoord,dir,d);
			if(!d.use(stem.currNode,"street")) return null;
			currNode.crossLink(stem.currNode);
			patchRun(currNode,stem.currNode);
			return stem;
		}

		private void patchRun(LayoutNode from, LayoutNode to)
		{
			int dirCode = getDirection(from,to);
			switch(dirCode)
			{
			case Directions.NORTH:
			case Directions.SOUTH:
				to.flagRun("n,s"); break;
			case Directions.EAST:
			case Directions.WEST:
				to.flagRun("e,w"); break;
			}
		}
		
		public TreeStem firstBranch() {
			int[][] turns = getTurns(dir);
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
			int[][] turns = getTurns(dir);
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
		LayoutSet d = new LayoutSet(set,num);
		TreeStem root = new TreeStem(rootCoord, new int[]{0,-1}, d);
		progress.add(root);
		d.use(root.currNode,"street");
		root.currNode.flag("gate");
		root.currNode.flagGateExit("s");
		root.currNode.flagRun("n,s");
		
		while(d.spaceAvailable()) {
			Vector<TreeStem> newOnes = new Vector<TreeStem>();
			for(Iterator<TreeStem> i =  progress.iterator(); i.hasNext() && d.spaceAvailable(); )
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
		fillInFlags(d);
		return set;
	}
}
