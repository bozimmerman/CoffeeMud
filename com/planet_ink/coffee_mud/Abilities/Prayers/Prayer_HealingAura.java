package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Prayer_HealingAura extends Prayer
{
	public String ID() { return "Prayer_HealingAura"; }
	public String name(){ return "Healing Aura";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	public String displayText(){ return "(Healing Aura)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public boolean  canBeUninvoked(){return false;}
	public boolean  isAutoInvoked(){return true;}
	private int fiveDown=5;
	private int tenDown=10;
	private int twentyDown=20;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if((affected==null)||(!(affected instanceof MOB)))
		   return false;
		if(tickID!=MudHost.TICK_MOB) return true;
		MOB myChar=(MOB)affected;

		if((fiveDown>1)&&(tenDown>1)&&(twentyDown>1)) return true;

		HashSet followers=myChar.getGroupMembers(new HashSet());
		if(myChar.location()!=null)
			for(int i=0;i<myChar.location().numInhabitants();i++)
			{
				MOB M=myChar.location().fetchInhabitant(i);
				if((M!=null)
				&&((M.getVictim()==null)||(!followers.contains(M.getVictim()))))
					followers.add(M);
			}
		if((--fiveDown)<=0)
		{
			fiveDown=5;
			Ability A=CMClass.getAbility("Prayer_CureLight");
			if(A!=null)
			for(Iterator e=followers.iterator();e.hasNext();)
				A.invoke(myChar,((MOB)e.next()),true,0);
		}
		if((--tenDown)<=0)
		{
			tenDown=10;
			Ability A=CMClass.getAbility("Prayer_RemovePoison");
			if(A!=null)
			for(Iterator e=followers.iterator();e.hasNext();)
				A.invoke(myChar,((MOB)e.next()),true,0);
		}
		if((--twentyDown)<=0)
		{
			twentyDown=10;
			Ability A=CMClass.getAbility("Prayer_CureDisease");
			if(A!=null)
			for(Iterator e=followers.iterator();e.hasNext();)
				A.invoke(myChar,((MOB)e.next()),true,0);
		}
		return true;
	}
}
