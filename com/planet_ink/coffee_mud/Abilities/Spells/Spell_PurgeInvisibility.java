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
   Copyright 2014-2018 Bo Zimmerman

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

public class Spell_PurgeInvisibility extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_PurgeInvisibility";
	}

	private final static String localizedName = CMLib.lang().L("Purge Invisibility");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Invisibility Purge)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS|CAN_ITEMS|CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}
	
	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		final Room room=CMLib.map().roomLocation(affected);
		if(canBeUninvoked() && (room != null))
			room.showHappens(CMMsg.MSG_OK_ACTION, L("The invisibility purge is lifted from @x1.",affected.Name()));
		super.unInvoke();
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(CMath.bset(affectableStats.disposition(),PhyStats.IS_INVISIBLE))
			affectableStats.setDisposition(affectableStats.disposition() & ~PhyStats.IS_INVISIBLE);
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if((!CMLib.flags().isInvisible(mob))
			&&(!CMLib.flags().canSeeInvisible(mob))
			&&(CMLib.flags().isInvisible(target)))
				return Ability.QUALITY_BENEFICIAL_SELF;
		}
		return super.castingQuality(mob,target);
	}

	public static List<Ability> returnOffensiveAffects(Physical fromMe)
	{
		final MOB newMOB=CMClass.getFactoryMOB();
		newMOB.setLocation(CMLib.map().roomLocation(fromMe));
		final List<Ability> offenders=new Vector<Ability>();
		for(int a=0;a<fromMe.numEffects();a++) // personal
		{
			final Ability A=fromMe.fetchEffect(a);
			if((A!=null)&&(A.canBeUninvoked()))
			{
				try
				{
					newMOB.recoverPhyStats();
					A.affectPhyStats(newMOB,newMOB.phyStats());
					if(CMLib.flags().isInvisible(newMOB))
						offenders.add(A);
				}
				catch(final Exception e)
				{
				}
			}
		}
		newMOB.destroy();
		return offenders;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			final Room R=mob.location();
			final CMMsg msg = CMClass.getMsg(mob, null, this, somanticCastCode(mob,null,auto),L((auto?"S":"^S<S-NAME> gesture(s) and a s")+"upressive magic falls over the area.^?"));
			if((R!=null)&&(R.okMessage(mob,msg)))
			{
				R.send(mob,msg);
				for(Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
				{
					final MOB M=m.nextElement();
					if(M!=null)
					{
						List<Ability> list=returnOffensiveAffects(M);
						for(Ability A : list)
						{
							if(A.canBeUninvoked())
								A.unInvoke();
							else
								beneficialAffect(mob,M,asLevel,0);
						}
						for(Enumeration<Item> i=M.items();i.hasMoreElements();)
						{
							final Item I=i.nextElement();
							if(I!=null)
							{
								List<Ability> list2=returnOffensiveAffects(I);
								for(Ability A : list2)
								{
									if(A.canBeUninvoked()
									&&((A.invoker()==null)||(mob.phyStats().level() >= A.adjustedLevel(invoker(), 0))))
										A.unInvoke();
									else
										beneficialAffect(mob,I,asLevel,0);
								}
							}
						}
					}
				}
				for(Enumeration<Item> i=R.items();i.hasMoreElements();)
				{
					final Item I=i.nextElement();
					if(I!=null)
					{
						List<Ability> list=returnOffensiveAffects(I);
						for(Ability A : list)
						{
							if(A.canBeUninvoked()
							&&((A.invoker()==null)||(mob.phyStats().level() >= A.adjustedLevel(invoker(), 0))))
								A.unInvoke();
							else
								beneficialAffect(mob,I,asLevel,0);
						}
					}
				}
				R.recoverRoomStats();
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> gesture(s) for supressing magic, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
