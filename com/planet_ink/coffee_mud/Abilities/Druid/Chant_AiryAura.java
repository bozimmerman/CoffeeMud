package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2018-2020 Bo Zimmerman

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
public class Chant_AiryAura extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_AiryAura";
	}

	private final static String	localizedName	= CMLib.lang().L("Airy Aura");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return L("(Airy Aura)");
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	protected Set<MOB>			druidsGroup		= null;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WATERCONTROL;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		final boolean inv = mob==invoker();
		super.unInvoke();
		if(canBeUninvoked())
		{
			if(inv)
				mob.tell(L("Your airy aura disappears"));
			else
				mob.tell(L("You no longer feel the airy aura."));
		}
	}

	@Override
	public CMObject copyOf()
	{
		if(druidsGroup==null)
			return super.copyOf();
		final Chant_AiryAura P=(Chant_AiryAura)super.copyOf();
		if(P==null)
			return super.copyOf();
		P.druidsGroup=new HashSet<MOB>(druidsGroup);
		return P;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(!(affected instanceof MOB))
			return false;
		final MOB druidMob=(invoker == null) ? (MOB)affected : invoker;
		if(druidMob==null)
			return false;
		final Set<MOB> paladinsGroup=this.druidsGroup;
		if((paladinsGroup!=null)&&(affected == druidMob))
		{
			final List<MOB> addHere=new LinkedList<MOB>();
			final List<MOB>	removeFromGroup	= new LinkedList<MOB>();
			synchronized(paladinsGroup)
			{
				if(druidMob.fetchEffect(ID())==null)
				{
					paladinsGroup.clear();
					removeFromGroup.addAll(paladinsGroup);
				}
				else
				{
					for(final MOB M : paladinsGroup)
					{
						if(M.location()!=druidMob.location())
							removeFromGroup.add(M);
					}
					paladinsGroup.clear();
					druidMob.getGroupMembers(paladinsGroup);
					for(final MOB M : paladinsGroup)
					{
						if((M!=druidMob)
						&&(M.location()==druidMob.location())
						&&(M.fetchEffect(ID())==null))
							addHere.add(M);
					}
				}
			}
			for(final MOB M : addHere)
			{
				final Ability A=(Ability)this.copyOf();
				A.startTickDown(druidMob, M, super.tickDown);
				M.tell(L("You feel surrounded by an airy aura."));
			}
			for(final MOB M : removeFromGroup)
			{
				final Ability A=M.fetchEffect(ID());
				if(A!=null)
					A.unInvoke();
			}
		}
		return true;
	}

	protected int[]	lastSet	= null;
	protected int[]	newSet	= null;

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		final int[] breatheables=affectableStats.getBreathables();
		if(breatheables.length==0)
			return;
		if((lastSet!=breatheables)||(newSet==null))
		{
			newSet=Arrays.copyOf(affectableStats.getBreathables(),affectableStats.getBreathables().length+2);
			newSet[newSet.length-1]=RawMaterial.RESOURCE_SALTWATER;
			newSet[newSet.length-2]=RawMaterial.RESOURCE_FRESHWATER;
			Arrays.sort(newSet);
			lastSet=breatheables;
		}
		affectableStats.setBreathables(newSet);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already exuding an airy aura."));
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) to <T-NAMESELF>.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> give(s) off an airy aura!"));
				final Chant_AiryAura A=(Chant_AiryAura)beneficialAffect(mob,target,asLevel,0);
				if(A!=null)
				{
					A.druidsGroup = new HashSet<MOB>();
					A.tick(mob, Tickable.TICKID_MOB);
				}
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) to <T-NAMESELF>, but nothing happens."));

		return success;
	}
}
