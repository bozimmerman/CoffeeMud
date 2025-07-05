package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import com.planet_ink.coffee_mud.core.Directions;
import com.planet_ink.coffee_mud.core.Log;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutFlags;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutNode;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutRuns;
import com.planet_ink.coffee_mud.Libraries.interfaces.AreaGenerationLibrary.LayoutTypes;

/*
   Copyright 2025-2025 Bo Zimmerman

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

public class SpottedMeshLayout extends MeshLayout
{
	@Override
	public String name()
	{
		return "SPOTTEDMESH";
	}

	@Override
	protected void finishGenerate(final List<LayoutNode> set)
	{
		final Set<LayoutNode> exclude = new HashSet<LayoutNode>();
		for(final LayoutNode n : set)
			if(n.links().size()<4)
				exclude.add(n);
		if(exclude.size()==set.size())
			return;
		int numLeafs = (int)Math.round(Math.ceil(set.size()/7.0));
		int tries = numLeafs * 10;
		final Random r = new Random(System.nanoTime());
		final Map<Integer,Integer> opDirs = new HashMap<Integer,Integer>();
		for(int d=0;d<4;d++)
			opDirs.put(Integer.valueOf(d), Integer.valueOf(Directions.getOpDirectionCode(d)));
		while((numLeafs > 0) && (--tries>0))
		{
			final int which = r.nextInt(set.size());
			final LayoutNode n = set.get(which);
			final int dir = r.nextInt(4);
			if((n.type() == LayoutTypes.interior)
			&&(n.links().size()>3)
			&&(!exclude.contains(n)))
			{
				boolean safe = true;
				for(final Integer odir : opDirs.keySet())
					if(odir.intValue() != dir)
					{
						final Integer opDir = opDirs.get(odir);
						if(n.links().containsKey(odir))
						{
							if(n.links().get(odir).getLink(opDir.intValue())==n)
							{
								if(n.links().get(odir).links().size()==1)
								{
									safe=false;
									break;
								}
							}
						}
					}
				if(!safe)
				{
					exclude.add(n);
					continue;
				}
				for(final Integer odir : opDirs.keySet())
					if(odir.intValue() != dir)
					{
						final Integer opDir = opDirs.get(odir);
						if(n.links().containsKey(odir))
						{
							if(n.links().get(odir).getLink(opDir.intValue())==n)
								n.links().get(odir).delLink(n);
							n.links().remove(odir);
						}
					}
				n.reType(LayoutTypes.leaf);
				n.getLink(dir).flag(LayoutFlags.offleaf);
				exclude.add(n.getLink(dir));
				numLeafs--;
			}
		}
	}
}