package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
			if((mob==invoker())&&(mob.location()!=null))
			{
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Exit e=mob.location().getExitInDir(d);
					if((e!=null)&&(e.hasADoor())&&(e.hasALock())&&(e.isLocked()))
					{
						Vector chcommands=new Vector();
						chcommands.addElement(Directions.getDirectionName(d));
						getSpell().invoke(invoker(),chcommands,null,true,0);
					}
				}
				for(int i=0;i<mob.location().numItems();i++)
				{
					Item I=mob.location().fetchItem(i);
					if((I!=null)&&(I instanceof Container)&&(I.container()==null))
					{
						Container C=(Container)I;
						if(C.hasALid()&&C.hasALock()&&C.isLocked())
						{
							Vector chcommands=new Vector();
							chcommands.addElement(C.name());
							getSpell().invoke(invoker(),chcommands,C,true,0);
						}
					}
				}
			}
			for(int i=0;i<mob.inventorySize();i++)
			{
				Item I=mob.fetchInventory(i);
				if((I!=null)&&(I instanceof Container)&&(I.container()==null))
				{
					Container C=(Container)I;
					if(C.hasALid()&&C.hasALock()&&C.isLocked())
					{
						Vector chcommands=new Vector();
						chcommands.addElement(C.name());
						getSpell().invoke(mob,chcommands,C,true,0);
					}
				}
			}
		}
	}
	protected int canAffectCode(){return 0;}


}
