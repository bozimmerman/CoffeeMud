package com.planet_ink.coffee_mud.Libraries.layouts;
import java.util.*;

import com.planet_ink.coffee_mud.core.CMStrings;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.*;
import com.planet_ink.coffee_mud.core.Directions;

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
/**
 * Abstract area layout pattern
 * node tags:
 * nodetype: surround, leaf, offleaf, street, square, interior
 * nodeexits: n,s,e,w, n,s, e,w, n,e,w, etc
 * nodeflags: corner, gate, intersection, tee
 * NODEGATEEXIT: (for gate, offleaf, square): n s e w etc
 * noderun: (for surround, street): n,s e,w
 *
 * @author Bo Zimmerman
 */
public abstract class AbstractLayout implements LayoutManager
{
	Random r = new Random();

	public int diff(int width, int height, int num) {
		final int x = width * height;
		return (x<num) ? (num - x) : (x - num);
	}

	@Override
	public abstract String name();

	@Override
	public abstract List<LayoutNode> generate(int num, int dir);

	public static int getDirection(LayoutNode from, LayoutNode to)
	{
		if(to.coord()[1]<from.coord()[1])
			return Directions.NORTH;
		if(to.coord()[1]>from.coord()[1])
			return Directions.SOUTH;
		if(to.coord()[0]<from.coord()[0])
			return Directions.WEST;
		if(to.coord()[0]>from.coord()[0])
			return Directions.EAST;
		return -1;
	}

	public static LayoutRuns getRunDirection(int dirCode)
	{
		switch(dirCode)
		{
		case Directions.NORTH:
		case Directions.SOUTH:
			return LayoutRuns.ns;
		case Directions.EAST:
		case Directions.WEST:
			return LayoutRuns.ew;
		}
		return LayoutRuns.ns;
	}

}
