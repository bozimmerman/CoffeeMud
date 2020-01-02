package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Quills extends StdAbility
{

	@Override
	public String ID()
	{
		return "Quills";
	}

	private final static String	localizedName	= CMLib.lang().L("Quills");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "";
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
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_RACIALABILITY;
	}

	@Override
	public boolean isAutoInvoked()
	{
		return true;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return false;
	}

	private static final String[]	triggerStrings	= I(new String[] { "QUILLS" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(1);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if((mob!=null)&&(target!=null))
		{
			if(mob.isInCombat()&&(mob.rangeToTarget()>maxRange()))
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	final static String	msgStr			= CMLib.lang().L("The quills around <S-NAME> <DAMAGES> <T-NAME>!");
	protected long		oncePerTickTime	= 0;

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(affected==null)
			return;
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(msg.target()==null)
			return;
		if(msg.source()==null)
			return;
		final MOB source=msg.source();
		if(source.location()==null)
			return;

		if(msg.amITarget(mob)&&(!msg.amISource(mob)))
		{
			if((CMath.bset(msg.targetMajor(),CMMsg.MASK_HANDS)||(msg.targetMajor(CMMsg.MASK_MOVE)))
				&&(msg.source().rangeToTarget()==0)
				&&(oncePerTickTime!=mob.lastTickedDateTime()))
			{
				if((CMLib.dice().rollPercentage()>(source.charStats().getStat(CharStats.STAT_DEXTERITY)*3)))
				{
					final CMMsg msg2=CMClass.getMsg(mob,source,this,somanticCastCode(mob,source,true),null);
					if(source.location().okMessage(mob,msg2))
					{
						source.location().send(mob,msg2);
						if(invoker==null)
							invoker=source;
						if(msg2.value()<=0)
						{
							final int damage = CMLib.dice().roll(1,(int)Math.round((invoker.phyStats().level()+super.getXLEVELLevel(invoker())+(2.0*super.getX1Level(invoker())))/4.0),1);
							CMLib.combat().postDamage(mob,source,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_WEAPONATTACK,Weapon.TYPE_PIERCING,msgStr);
						}
					}
					oncePerTickTime=mob.lastTickedDateTime();
				}
			}
		}
		return;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		if(!(affected instanceof MOB))
			return;
		affectableStats.setArmor(affectableStats.armor()-1-getXLEVELLevel(invoker()));
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?"":L("^F^<FIGHT^><S-NAME> shoot(s) quills at <T-NAME>.^</FIGHT^>^N"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final int topDamage=(adjustedLevel(mob,asLevel)/4)+1;
				int damage=CMLib.dice().roll(1,topDamage,0);
				if(msg.value()>0)
					damage = (int)Math.round(CMath.div(damage,2.0));
				CMLib.combat().postDamage(mob,target,this,damage,
						CMMsg.MASK_ALWAYS|CMMsg.MASK_SOUND|CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE,Weapon.TYPE_PIERCING,
						L("^F^<FIGHT^><S-NAME> <DAMAGE> <T-NAME> with a shot of quills!^</FIGHT^>^?"));
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> fail(s) to shoot quills at <T-NAME>."));

		// return whether it worked
		return success;
	}
}
