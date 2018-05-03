package com.planet_ink.coffee_mud.Libraries.layouts;

import java.awt.List;
import java.util.ArrayList;
import java.util.Vector;
import com.planet_ink.coffee_mud.core.Directions;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutRuns;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutTypes;

/*
   Copyright 2013-2018 Bo Zimmerman

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

public class ApartmentLayout extends AbstractLayout
{
	@Override
	public String name()
	{
		return "APARTMENT";
	}

	public void setRunFromDirection(LayoutNode node, int dir)
	{
		switch(dir)
		{
		case Directions.NORTH:
		case Directions.SOUTH:
			node.flagRun(LayoutRuns.ns);
			break;
		case Directions.EAST:
		case Directions.WEST:
			node.flagRun(LayoutRuns.ew);
			break;
		}
	}

	@Override
	public java.util.List<LayoutNode> generate(int num, int dir)
	{
		final Vector<LayoutNode> set = new Vector<LayoutNode>();
		int hallwayLength=num/3;
		int numHallways=1;
		while(hallwayLength > 7)
		{
			hallwayLength = hallwayLength / 2;
			numHallways *= 2;
		}

		final LayoutSet lSet = new LayoutSet(set,num);
		LayoutNode n = null;
		int hallwayDirection=dir;
		int sidewayDirection=dir;
		final int fullNumHallway = numHallways;
		switch(dir)
		{
		case Directions.NORTH:
			n = new DefaultLayoutNode(new long[] { 0, hallwayLength });
			sidewayDirection = Directions.EAST;
			break;
		case Directions.SOUTH:
			n = new DefaultLayoutNode(new long[] { fullNumHallway, 0 });
			sidewayDirection = Directions.WEST;
			break;
		case Directions.EAST:
			n = new DefaultLayoutNode(new long[] { 0, 0 });
			sidewayDirection = Directions.SOUTH;
			break;
		case Directions.WEST:
			n = new DefaultLayoutNode(new long[] { fullNumHallway, hallwayLength });
			sidewayDirection = Directions.NORTH;
			break;
		case Directions.NORTHEAST:
			n = new DefaultLayoutNode(new long[] { 0, hallwayLength });
			sidewayDirection = Directions.EAST;
			hallwayDirection = Directions.NORTH;
			break;
		case Directions.NORTHWEST:
			n = new DefaultLayoutNode(new long[] { fullNumHallway, hallwayLength });
			sidewayDirection = Directions.WEST;
			hallwayDirection = Directions.NORTH;
			break;
		case Directions.SOUTHEAST:
			n = new DefaultLayoutNode(new long[] { 0, 0 });
			sidewayDirection = Directions.SOUTH;
			hallwayDirection = Directions.EAST;
			break;
		case Directions.SOUTHWEST:
			n = new DefaultLayoutNode(new long[] { fullNumHallway, 0 });
			sidewayDirection = Directions.WEST;
			hallwayDirection = Directions.SOUTH;
			break;
		}
		if(n!=null)
		{
			final java.util.List<LayoutNode> hallways=new ArrayList<LayoutNode>();
			hallways.add(n);
			//n.flagGateExit(dir);
			lSet.use(n,LayoutTypes.street);
			n.flag(LayoutFlags.gate);
			n.flag(LayoutFlags.tee);
			setRunFromDirection(n,sidewayDirection);
			for(int h=1;h<numHallways;h++)
			{
				LayoutNode prevNode=n;
				LayoutNode nextNode=lSet.makeNextNode(prevNode, sidewayDirection);
				lSet.use(nextNode,LayoutTypes.street);
				nextNode.crossLink(prevNode);
				setRunFromDirection(nextNode,sidewayDirection);

				prevNode=nextNode;
				nextNode=lSet.makeNextNode(prevNode, sidewayDirection);
				lSet.use(nextNode,LayoutTypes.street);
				nextNode.crossLink(prevNode);
				setRunFromDirection(nextNode,sidewayDirection);

				prevNode=nextNode;
				nextNode=lSet.makeNextNode(prevNode, sidewayDirection);
				lSet.use(nextNode,LayoutTypes.street);
				nextNode.crossLink(prevNode);
				setRunFromDirection(nextNode,sidewayDirection);
				nextNode.flag(LayoutFlags.tee);

				hallways.add(nextNode);
				n=nextNode;
			}
			for(final LayoutNode hallwayNode : hallways)
			{
				LayoutNode prevNode=hallwayNode;
				for(int h=0;h<hallwayLength;h++)
				{
					LayoutNode nextNode=lSet.makeNextNode(prevNode, hallwayDirection);
					lSet.use(nextNode,LayoutTypes.street);
					nextNode.crossLink(prevNode);
					setRunFromDirection(nextNode,hallwayDirection);

					prevNode=nextNode; // this should stick

					nextNode=lSet.makeNextNode(prevNode, sidewayDirection);
					lSet.use(nextNode,LayoutTypes.leaf);
					nextNode.crossLink(prevNode);

					nextNode=lSet.makeNextNode(prevNode, Directions.getOpDirectionCode(sidewayDirection));
					lSet.use(nextNode,LayoutTypes.leaf);
					nextNode.crossLink(prevNode);
				}
			}
			lSet.fillInFlags();
		}
		return set;
	}

}
