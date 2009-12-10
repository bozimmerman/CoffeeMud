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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Wimpy extends StdBehavior
{
	public String ID(){return "Wimpy";}
	protected int tickWait=0;
	protected int tickDown=0;
	protected boolean veryWimpy=false;


	public boolean grantsAggressivenessTo(MOB M)
	{
		return false;
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		tickWait=CMParms.getParmInt(newParms,"delay",0);
		tickDown=tickWait;
		veryWimpy=CMParms.getParmInt(newParms,"very",0)==1;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		if(tickID!=Tickable.TICKID_MOB) return true;
		if(((--tickDown)<0)&&(ticking instanceof MOB))
		{
			tickDown=tickWait;
			MOB monster=(MOB)ticking;
			if(monster.location()!=null)
			for(int m=0;m<monster.location().numInhabitants();m++)
			{
				MOB M=monster.location().fetchInhabitant(m);
				if((M!=null)&&(M!=monster)&&(CMLib.masking().maskCheck(getParms(),M,false)))
				{
					if(M.getVictim()==monster)
					{
						CMLib.commands().postFlee(monster,"");
						return true;
					}
					else
					if((veryWimpy)&&(!monster.isInCombat()))
					{
						Room oldRoom=monster.location();
						Vector V=CMLib.flags().flaggedBehaviors(monster,Behavior.FLAG_MOBILITY);
						Behavior B=null;
						for(int b=0;b<V.size();b++)
						{
							B=(Behavior)V.elementAt(b);
							int tries=0;
							while(((++tries)<100)&&(oldRoom==monster.location()))
								B.tick(monster,Tickable.TICKID_MOB);
							if(oldRoom!=monster.location())
								return true;
						}
						if(oldRoom==monster)
							CMLib.tracking().beMobile(monster,false,false,false,false,null,null);
					}
				}
			}
		}
		return true;
	}
}
