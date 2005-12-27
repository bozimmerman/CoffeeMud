package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;


/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class ResourceOverride extends StdBehavior
{
	public String ID(){return "ResourceOverride";}
	protected int canImproveCode(){return Behavior.CAN_ROOMS;}


	int lastResourceSet=-1;
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Tickable.TICKID_ROOM_BEHAVIOR) return true;
		if(!(ticking instanceof Room)) return true;
		Room R=(Room)ticking;
		if((lastResourceSet<0)
		||(R.myResource()!=lastResourceSet)
		||(R.myResource()<0))
		{
			Vector V=CMParms.parse(getParms());
			if(V.size()==0) return true;
			// first try for a real one
			int code=-1;
			String which=((String)V.elementAt(CMLib.dice().roll(1,V.size(),-1))).toUpperCase().trim();
			if((CMath.s_int(which)>0)||(which.equalsIgnoreCase("0")))
			   code=CMath.s_int(which);

			if(code<0)
			for(int i=0;i<RawMaterial.RESOURCE_DESCS.length;i++)
			{
				if(RawMaterial.RESOURCE_DESCS[i].equalsIgnoreCase(which))
					code=RawMaterial.RESOURCE_DATA[i][0];
			}
			if(code<0)
			for(int i=0;i<RawMaterial.MATERIAL_DESCS.length;i++)
			{
				if(RawMaterial.MATERIAL_DESCS[i].equalsIgnoreCase(which))
					for(int i2=0;i2<RawMaterial.RESOURCE_DESCS.length;i2++)
					{
						if(CMath.isSet((RawMaterial.RESOURCE_DATA[i][0]&RawMaterial.MATERIAL_MASK),i))
						{ code=RawMaterial.RESOURCE_DATA[i][0]; break;}
					}
			}
			if(code<0)
			for(int i=0;i<RawMaterial.RESOURCE_DESCS.length;i++)
			{
				if(RawMaterial.RESOURCE_DESCS[i].startsWith(which))
					code=RawMaterial.RESOURCE_DATA[i][0];
			}
			if(code<0)
			for(int i=0;i<RawMaterial.MATERIAL_DESCS.length;i++)
			{
				if(RawMaterial.MATERIAL_DESCS[i].startsWith(which))
					for(int i2=0;i2<RawMaterial.RESOURCE_DESCS.length;i2++)
					{
						if(CMath.isSet((RawMaterial.RESOURCE_DATA[i][0]&RawMaterial.MATERIAL_MASK),i))
						{ code=RawMaterial.RESOURCE_DATA[i][0]; break;}
					}
			}
			lastResourceSet=code;
			R.setResource(code);
		}
		return true;
	}
}
