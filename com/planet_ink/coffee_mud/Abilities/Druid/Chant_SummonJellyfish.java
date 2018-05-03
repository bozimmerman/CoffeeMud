package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2016-2018 Bo Zimmerman

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

public class Chant_SummonJellyfish extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_SummonJellyfish";
	}

	private final static String	localizedName	= CMLib.lang().L("Summon Jellyfish");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Harrassed by Jellyfish)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(3);
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_ANIMALAFFINITY;
	}

	protected MOB theJellyfish = null;
	protected MOB[] stingList = new MOB[3];
	
	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_MOB)
		&&(affected instanceof Room))
		{
			final Room R=(Room)affected;
			final MOB invoker=(invoker()!=null) ? invoker() : theJellyfish;
			if((theJellyfish == null) || (theJellyfish.location()!=affected)||(theJellyfish.amDead()))
				unInvoke();
			else
			if((invoker==null)||(!invoker.isInCombat()))
				unInvoke();
			else
			{
				Set<MOB> followers = invoker.getGroupMembers(new HashSet<MOB>());
				for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
				{
					MOB M=m.nextElement();
					if((!followers.contains(M))
					&&(invoker.mayIFight(M))
					&&(!CMParms.contains(stingList, M)))
					{
						final int damage= (M.phyStats().level()/10) + super.getXLEVELLevel(invoker);
						CMLib.combat().postDamage(invoker,M,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_PARALYZE,-1,L("<T-NAME> <T-IS-ARE> stung by a jellyfish!"));
						CMLib.combat().postRevengeAttack(M, invoker);
						for(int i=0;i<stingList.length-1;i++)
							stingList[i+1]=stingList[i];
						stingList[0]=M;
						break;
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof Room))
			return;
		final Room R=(Room)affected;

		super.unInvoke();
		if((canBeUninvoked() || this.unInvoked)&&(theJellyfish!=null))
		{
			R.show(theJellyfish,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> swim away!"));
			theJellyfish.destroy();
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room target=(givenTarget instanceof Room) ? (Room)givenTarget
				: (givenTarget instanceof MOB) ? ((MOB)givenTarget).location()
				: mob.location();
				
		if(target == null)
			return false;

		if(target.fetchEffect(ID())!=null)
		{
			mob.tell(mob,null,null,L("<S-NAME> already <S-HAS-HAVE> a swarm of jellyfish here."));
			return false;
		}
		
		if(!mob.isInCombat())
		{
			mob.tell(L("Only the rage of combat can summon jellyfish!"));
			return false;
		}

		Room R=mob.location();
		if(R==null)
			return false;
		if(!CMLib.flags().isUnderWateryRoom(R))
		{
			mob.tell(L("You can only summon jellyfish underwater."));
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),
					auto?L("An angry swarm of jellyfish arrive!"):
						L("^S<S-NAME> chants to the waters until an angry swarm of jellyfish arrive!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
				Chant_SummonJellyfish C=(Chant_SummonJellyfish)target.fetchEffect(ID());
				if(C!=null)
				{
					C.theJellyfish = CMClass.getFactoryMOB();
					C.theJellyfish.setName(L("a swarm of jellyfish"));
					C.theJellyfish.setDisplayText(L("A swarm of jellyfish dart all around you!"));
					C.theJellyfish.setDescription(L("They appear magically endowed.  Perhaps they are summoned?"));
					Ability P=CMClass.getAbility("Prop_SafePet");
					if(P!=null)
					{
						P.setMiscText("MSG=\""+L("Zoom! Nope, they are just too quick for you.")+"\"");
						C.theJellyfish.addNonUninvokableEffect(P);
					}
					C.theJellyfish.setLocation(target);
					target.addInhabitant(C.theJellyfish);
				}
			}
		}
		else
			return maliciousFizzle(mob,null,L("<S-NAME> chants(s) to the waters, but the magic fizzles."));

		// return whether it worked
		return success;
	}
}
