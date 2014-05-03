package com.planet_ink.coffee_mud.Abilities.Traps;
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
public class Trap_AcidPit extends Trap_RoomPit
{
	@Override public String ID() { return "Trap_AcidPit"; }
	@Override public String name(){ return "acid pit";}
	@Override protected int canAffectCode(){return Ability.CAN_ROOMS;}
	@Override protected int canTargetCode(){return 0;}
	@Override protected int trapLevel(){return 18;}
	@Override public String requiresToSet(){return "";}
	@Override
	public int baseRejuvTime(int level)
	{
		int time=super.baseRejuvTime(level);
		if(time<15) time=15;
		return time;
	}

	@Override
	public void finishSpringing(MOB target)
	{
		if((!invoker().mayIFight(target))||(target.phyStats().weight()<5))
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> float(s) gently into the pit!"));
		else
		{
			target.location().show(target,null,CMMsg.MSG_OK_ACTION,_("<S-NAME> hit(s) the pit floor with a THUMP!"));
			final int damage=CMLib.dice().roll(trapLevel()+abilityCode(),6,1);
			CMLib.combat().postDamage(invoker(),target,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.TYP_ACID,-1,null);
			target.location().showHappens(CMMsg.MSG_OK_VISUAL,"Acid starts pouring into the room!");
		}
		CMLib.commands().postLook(target,true);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_TRAP_RESET)&&(getReset()>0))
		{
			if((sprung)
			&&(affected!=null)
			&&(affected instanceof Room)
			&&(pit!=null)
			&&(pit.size()>1)
			&&(!disabled()))
			{
				final Room R=(Room)pit.firstElement();
				for(int i=0;i<R.numInhabitants();i++)
				{
					final MOB M=R.fetchInhabitant(i);
					if((M!=null)&&(M!=invoker()))
					{
						final int damage=CMLib.dice().roll(trapLevel()+abilityCode(),6,1);
						CMLib.combat().postDamage(invoker(),M,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_ACID,Weapon.TYPE_MELTING,"The acid <DAMAGE> <T-NAME>!");
						if((!M.isInCombat())&&(M.isMonster())&&(M!=invoker)&&(invoker!=null)&&(M.location()==invoker.location())&&(M.location().isInhabitant(invoker))&&(CMLib.flags().canBeSeenBy(invoker,M)))
							CMLib.combat().postAttack(M,invoker,M.fetchWieldedItem());
					}
				}
				return super.tick(ticking,tickID);
			}
			return false;
		}
		return super.tick(ticking,tickID);
	}

}
