package com.planet_ink.coffee_mud.Abilities.Thief;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Thief_Kamikaze extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Kamikaze";
	}

	private final static String localizedName = CMLib.lang().L("Kamikaze");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_OTHERS;
	}

	private static final String[] triggerStrings =I(new String[] {"KAMIKAZE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public boolean disregardsArmorCheck(MOB mob)
	{
		return true;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_CRIMINAL;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected instanceof MOB)
		{
			final MOB mob=(MOB)affected;
			for(int i=0;i<mob.numItems();i++)
			{
				final Item I=mob.getItem(i);
				if((I!=null)&&(I.container()==null))
				{
					final Trap T=CMLib.utensils().fetchMyTrap(I);
					if((T!=null)&&(T.isABomb()))
					{
						if(!I.amWearingAt(Wearable.IN_INVENTORY))
							CMLib.commands().postRemove(mob,I,true);
						CMLib.commands().postDrop(mob,I,false,false,false);
						if(I.owner() instanceof Room)
						{
							final Room R=(Room)I.owner();
							for(int i2=0;i2<R.numInhabitants();i2++)
							{
								final MOB M=R.fetchInhabitant(i2);
								if(M!=null)
									T.spring(M);
							}
							T.disable();
							T.unInvoke();
							I.destroy();
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if((canBeUninvoked())&&(!mob.amDead())&&(mob.location()!=null))
		{
			if(mob.amFollowing()!=null)
				CMLib.commands().postFollow(mob,null,false);
			CMLib.commands().postStand(mob,true);
			if((mob.isMonster())&&(!CMLib.flags().isMobile(mob)))
				CMLib.tracking().wanderAway(mob,true,true);
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell(L("You must specify who your kamikaze bomber is, and which direction they should go."));
			return false;
		}
		final String s=commands.get(commands.size()-1);
		commands.remove(commands.size()-1);
		final MOB target=getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if((!target.mayIFight(mob))||(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)<3))
		{
			mob.tell(L("You can't talk @x1 into a kamikaze mission.",target.name(mob)));
			return false;
		}

		if((s.length()==0)||(CMParms.parse(s).size()==0))
		{
			mob.tell(L("Send @x1 which direction?",target.charStats().himher()));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final double goldRequired=(( Math.round( ( 100.0 - ( ( mob.charStats().getStat( CharStats.STAT_CHARISMA ) + ( 2.0 * getXLEVELLevel( mob ) ) ) * 2.0 ) ) ) * target.phyStats().level() ) );
		final String localCurrency=CMLib.beanCounter().getCurrency(target);
		final String costWords=CMLib.beanCounter().nameCurrencyShort(localCurrency,goldRequired);
		if(CMLib.beanCounter().getTotalAbsoluteValue(mob,localCurrency)<goldRequired)
		{
			mob.tell(L("@x1 requires @x2 to do this.",target.charStats().HeShe(),costWords));
			return false;
		}

		Trap bombFound=null;
		for(int i=0;i<target.numItems();i++)
		{
			final Item I=target.getItem(i);
			if((I!=null)&&(I.container()==null))
			{
				final Trap T=CMLib.utensils().fetchMyTrap(I);
				if((T!=null)&&(T.isABomb()))
				{
					bombFound=T;
					break;
				}
			}
		}
		if(bombFound==null)
		{
			mob.tell(L("@x1 must have some bombs for this to work.",target.name(mob)));
			return false;
		}

		final boolean success=proficiencyCheck(mob,0,auto);

		if(!success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,L("^T<S-NAME> attempt(s) to convince <T-NAMESELF> to kamikaze @x1, but no deal is reached.^?",s));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
		}
		else
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_SPEAK,L("^T<S-NAME> pay(s) <T-NAMESELF> to Kamikaze @x1 for @x2.^?",s,costWords));

			CMLib.beanCounter().subtractMoney(mob,localCurrency,goldRequired);
			mob.recoverPhyStats();
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				CMLib.beanCounter().addMoney(target,localCurrency,goldRequired);
				target.recoverPhyStats();
				beneficialAffect(mob,target,asLevel,2);
				bombFound.activateBomb();
				commands=new Vector<String>();
				commands.add("GO");
				commands.add(s);
				target.enqueCommand(commands,MUDCmdProcessor.METAFLAG_FORCED,0);
			}
		}
		return success;
	}

}
