package com.planet_ink.coffee_mud.Libraries.layouts;
import java.util.*;

import com.planet_ink.coffee_mud.core.Directions;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutTypes;

/*
   Copyright 2008-2018 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

public class TreeLayout extends AbstractLayout
{
	@Override
	public String name()
	{
		return "TREE";
	}

	int originalDirection=Directions.NORTH;

	private class TreeStem
	{
		public LayoutNode currNode = null;
		private int dir = Directions.NORTH;
		private LayoutSet lSet = null;
		public TreeStem(long[] coord, int dir, LayoutSet d)
		{
			this.dir = dir;
			this.lSet = d;
			currNode = new DefaultLayoutNode(coord);
		}

		private long[] getCoord(long[] curr, int dir) { return lSet.makeNextCoord(curr,dir);}

		private int[] getTurns(int dir)
		{
			switch(dir)
			{
			case Directions.NORTH:
			case Directions.SOUTH:
				if (originalDirection == Directions.EAST)
					return new int[] { Directions.EAST };
				if (originalDirection == Directions.WEST)
					return new int[] { Directions.WEST };
				return new int[] { Directions.WEST, Directions.EAST };
			case Directions.EAST:
			case Directions.WEST:
				if (originalDirection == Directions.NORTH)
					return new int[] { Directions.NORTH };
				if (originalDirection == Directions.SOUTH)
					return new int[] { Directions.SOUTH };
				return new int[] { Directions.NORTH, Directions.SOUTH };
			}
			return null;
		}

		public TreeStem nextNode()
		{
			final long[] nextCoord = getCoord(currNode.coord(),dir);
			final TreeStem stem = new TreeStem(nextCoord,dir,lSet);
			if(!lSet.use(stem.currNode,LayoutTypes.street))
				return null;
			currNode.crossLink(stem.currNode);
			patchRun(currNode,stem.currNode);
			return stem;
		}

		private void patchRun(LayoutNode from, LayoutNode to)
		{
			to.flagRun(AbstractLayout.getRunDirection(getDirection(from,to)));
		}

		public TreeStem firstBranch()
		{
			final int[] turns = getTurns(dir);
			if((turns == null)||(turns.length<1))
				return null;
			final long[] nextCoord = getCoord(currNode.coord(),turns[0]);
			final TreeStem newStem =  new TreeStem(nextCoord,turns[0],lSet);
			if(!lSet.use(newStem.currNode,LayoutTypes.street))
				return null;
			currNode.flag(LayoutFlags.corner);
			currNode.crossLink(newStem.currNode);
			patchRun(currNode,newStem.currNode);
			return newStem;
		}

		public TreeStem secondBranch()
		{
			final int[] turns = getTurns(dir);
			if((turns == null)||(turns.length<2))
				return null;
			final long[] nextCoord = getCoord(currNode.coord(),turns[1]);
			final TreeStem newStem =  new TreeStem(nextCoord,turns[1],lSet);
			if(!lSet.use(newStem.currNode,LayoutTypes.street))
				return null;
			currNode.crossLink(newStem.currNode);
			patchRun(currNode,newStem.currNode);
			return newStem;
		}
	}

	@Override
	public List<LayoutNode> generate(int num, int dir)
	{
		final Vector<LayoutNode> set = new Vector<LayoutNode>();
		Vector<TreeStem> progress = new Vector<TreeStem>();

		final long[] rootCoord = new long[]{0,0};
		final LayoutSet lSet = new LayoutSet(set,num);
		originalDirection=dir;
		final TreeStem root = new TreeStem(rootCoord, dir, lSet);
		progress.add(root);
		lSet.use(root.currNode,LayoutTypes.street);
		root.currNode.flag(LayoutFlags.gate);
		//root.currNode.flagGateExit(dir);
		root.currNode.flagRun(AbstractLayout.getRunDirection(dir));

		while(lSet.spaceAvailable())
		{
			final Vector<TreeStem> newOnes = new Vector<TreeStem>();
			for(final Iterator<TreeStem> i =  progress.iterator(); i.hasNext() && lSet.spaceAvailable(); )
			{
				final TreeStem stem = i.next();
				TreeStem branch = stem.nextNode();
				if(branch != null)
					newOnes.add(branch);
				branch = stem.firstBranch();
				if(branch != null)
					newOnes.add(branch);
				branch = stem.secondBranch();
				if(branch != null)
					newOnes.add(branch);
			}
			progress = new Vector<TreeStem>();
			while(newOnes.size()> 0)
			{
				final TreeStem b =  newOnes.elementAt(r.nextInt(newOnes.size()));
				progress.add(b);
				newOnes.remove(b);
			}
		}
		lSet.clipLongStreets();
		lSet.fillInFlags();
		return set;
	}
}
