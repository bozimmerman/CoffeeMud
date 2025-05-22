package com.planet_ink.coffee_mud.Libraries.layouts;

import java.util.List;
import java.util.Random;
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
		int numLeafs = (int)Math.round(Math.ceil(set.size()/7.0));
		int tries = numLeafs * 10;
		final Random r = new Random(System.nanoTime());
		while((numLeafs > 0) && (--tries>0))
		{
			final int which = r.nextInt(set.size());
			final LayoutNode n = set.get(which);
			final int dir = r.nextInt(4);
			if((n.type() == LayoutTypes.interior)
			&&(n.getLink(0)!=null)
			&&((n.getLink(0).type() == LayoutTypes.interior)
				||(n.getLink(0).type() == LayoutTypes.surround))
			&&(n.getLink(1)!=null)
			&&((n.getLink(1).type() == LayoutTypes.interior)
				||(n.getLink(1).type() == LayoutTypes.surround))
			&&(n.getLink(2)!=null)
			&&((n.getLink(2).type() == LayoutTypes.interior)
				||(n.getLink(2).type() == LayoutTypes.surround))
			&&(n.getLink(3)!=null)
			&&((n.getLink(3).type() == LayoutTypes.interior)
				||(n.getLink(3).type() == LayoutTypes.surround)))
			{
				for(int i=0;i<4;i++)
					if(i != dir)
					{
						n.links().get(Integer.valueOf(i)).delLink(n);
						n.links().remove(Integer.valueOf(i));
					}
				n.reType(LayoutTypes.leaf);
				n.getLink(dir).flag(LayoutFlags.offleaf);
				numLeafs--;
			}
		}
	}
}