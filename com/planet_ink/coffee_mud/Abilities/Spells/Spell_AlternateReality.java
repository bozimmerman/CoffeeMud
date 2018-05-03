package com.planet_ink.coffee_mud.Abilities.Spells;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2003-2018 Bo Zimmerman

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

public class Spell_AlternateReality extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_AlternateReality";
	}

	private final static String localizedName = CMLib.lang().L("Alternate Reality");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Alternate Reality)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ILLUSION;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(L("Your reality returns to normal."));
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return false;
		final MOB mob=(MOB)affected;
		if(!mob.isInCombat())
		{
			unInvoke();
			return false;
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0)
		&&((msg.amISource((MOB)affected)))
		&&(msg.target()!=null)
		&&(invoker()!=null))
		{
			final Set<MOB> H=invoker().getGroupMembers(new HashSet<MOB>());
			if(H.contains(msg.target()))
			{
				msg.source().tell(L("But you are on @x1's side!",invoker().name()));
				if(invoker().getVictim()!=affected)
					((MOB)affected).setVictim(invoker().getVictim());
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=super.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(target.getVictim()!=mob)
		{
			mob.tell(L("But @x1 isn't fighting you!",target.charStats().heshe()));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> incant(s) to <T-NAME>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					success=maliciousAffect(mob,target,asLevel,0,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0))!=null;
					if(success)
					{
						final Room R=target.location();
						R.show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> change(s) sides!"));
						target.makePeace(true);
						if(mob.getVictim()==target)
							mob.setVictim(null);
						final Set<MOB> H=mob.getGroupMembers(new HashSet<MOB>());
						if(!H.contains(mob))
							H.add(mob);
						final Vector<MOB> badGuys=new Vector<MOB>();
						for(int i=0;i<R.numInhabitants();i++)
						{
							final MOB M=R.fetchInhabitant(i);
							if((M!=null)&&(M!=mob)&&(M!=target))
							{
								if(!H.contains(M))
								{
									if(M.getVictim()==mob)
									{
										badGuys.clear();
										badGuys.addElement(M);
										break;
									}
									badGuys.addElement(M);
								}
								else
								if(M.getVictim()==target)
									M.setVictim(null);
							}
						}
						if(badGuys.size()>0)
						{
							target.setVictim(badGuys.elementAt(CMLib.dice().roll(1,badGuys.size(),-1)));
							if(mob.getVictim()==null)
								mob.setVictim(badGuys.elementAt(CMLib.dice().roll(1,badGuys.size(),-1)));
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> incant(s) to <T-NAME>, but fizzle(s) the spell."));

		// return whether it worked
		return success;
	}
}
