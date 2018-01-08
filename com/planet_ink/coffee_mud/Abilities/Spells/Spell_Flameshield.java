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
   Copyright 2001-2018 Bo Zimmerman

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

public class Spell_Flameshield extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Flameshield";
	}

	private final static String	localizedName	= CMLib.lang().L("Flameshield");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Flameshield)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_BENEFICIAL_OTHERS;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_EVOCATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HEATING | Ability.FLAG_FIREBASED;
	}

	final static String	msgStr			= CMLib.lang().L("The flame shield around <S-NAME> flares and <DAMAGES> <T-NAME>!");
	protected long		oncePerTickTime	= 0;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			if((mob.location()!=null)&&(!mob.amDead()))
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> flame shield vanishes in a puff of smoke."));
	}

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
							CMLib.combat().postDamage(mob,source,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,msgStr);
						}
					}
					oncePerTickTime=mob.lastTickedDateTime();
				}
			}
		}
		return;
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if(affected==null)
			return;
		if(!(affected instanceof MOB))
			return;
		affectableStats.setArmor(affectableStats.armor()-1-getXLEVELLevel(invoker()));
		affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_LIGHTSOURCE);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),L((auto?"^S":"^S<S-NAME> incant(s) and wave(s) <S-HIS-HER> arms.  ")+"A field of flames erupt(s) around <T-NAME>!^?")+CMLib.protocol().msp("fireball.wav",10));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> incant(s) and wave(s) <S-HIS-HER> arms, but only sparks emerge."));

		// return whether it worked
		return success;
	}
}
