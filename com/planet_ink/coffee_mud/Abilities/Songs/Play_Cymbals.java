package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2000-2014 Bo Zimmerman

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
public class Play_Cymbals extends Play_Instrument
{
	public String ID() { return "Play_Cymbals"; }
	public String name(){ return "Cymbals";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_CYMBALS;}
	public String mimicSpell(){return "Spell_Knock";}
	private static Ability theSpell=null;
	protected Ability getSpell()
	{
		if(theSpell!=null) return theSpell;
		if(mimicSpell().length()==0) return null;
		theSpell=CMClass.getAbility(mimicSpell());
		return theSpell;
	}

	protected void inpersistantAffect(MOB mob)
	{
		if(getSpell()!=null)
		{
			Room R=mob.location();
			if(R!=null)
			{
				List<Physical> knockables=new LinkedList<Physical>();
				int dirCode=-1;
				if(mob==invoker())
				{
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					{
						Exit e=R.getExitInDir(d);
						if((e!=null)&&(e.hasADoor())&&(e.hasALock())&&(e.isLocked()))
						{
							knockables.add(e);
							dirCode=d;
						}
					}
					for(int i=0;i<R.numItems();i++)
					{
						Item I=R.getItem(i);
						if((I!=null)&&(I instanceof Container)&&(I.container()==null))
						{
							Container C=(Container)I;
							if(C.hasALid()&&C.hasALock()&&C.isLocked())
								knockables.add(C);
						}
					}
				}
				for(int i=0;i<mob.numItems();i++)
				{
					Item I=mob.getItem(i);
					if((I!=null)&&(I instanceof Container)&&(I.container()==null))
					{
						Container C=(Container)I;
						if(C.hasALid()&&C.hasALock()&&C.isLocked())
							knockables.add(C);
					}
				}
				for(Physical P : knockables)
				{
					int levelDiff=P.phyStats().level()-(mob.phyStats().level()+(2*super.getXLEVELLevel(mob)));
					if(levelDiff<0) levelDiff=0;
					if(proficiencyCheck(mob,-(levelDiff*25),false))
					{
						CMMsg msg=CMClass.getMsg(mob,P,this,CMMsg.MSG_CAST_VERBAL_SPELL,P.name()+" begin(s) to glow!");
						if(R.okMessage(mob,msg))
						{
							R.send(mob,msg);
							msg=CMClass.getMsg(mob,P,null,CMMsg.MSG_UNLOCK,null);
							CMLib.utensils().roomAffectFully(msg,R,dirCode);
							msg=CMClass.getMsg(mob,P,null,CMMsg.MSG_OPEN,"<T-NAME> opens.");
							CMLib.utensils().roomAffectFully(msg,R,dirCode);
						}
					}
				}
			}
		}
	}
	protected int canAffectCode(){return 0;}
}
