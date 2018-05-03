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
   Copyright 2002-2018 Bo Zimmerman

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
public class Spell_Tourettes extends Spell implements DiseaseAffect
{

	@Override
	public String ID()
	{
		return "Spell_Tourettes";
	}

	private final static String	localizedName	= CMLib.lang().L("Tourettes");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Tourettes)");

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
		return Ability.ACODE_SPELL | Ability.DOMAIN_ENCHANTMENT;
	}

	@Override
	public int difficultyLevel()
	{
		return 5;
	}

	@Override
	public boolean isMalicious()
	{
		return true;
	}

	int	plagueDown	= 4;

	@Override
	public int spreadBitmap()
	{
		return DiseaseAffect.SPREAD_PROXIMITY;
	}

	@Override
	public String getHealthConditionDesc()
	{
		return "Mental compulsion disorder: Tourettes Syndrome.";
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			if((mob.location()!=null)&&(!mob.amDead()))
			{
				spreadImmunity(mob);
				mob.tell(L("You feel more polite."));
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!(affected instanceof MOB))
			return super.tick(ticking,tickID);

		if(!super.tick(ticking,tickID))
			return false;
		if((--plagueDown)<=0)
		{
			final MOB mob=(MOB)affected;
			plagueDown=4;
			if(invoker==null)
				invoker=mob;

			final MOB target=mob.location().fetchRandomInhabitant();
			if((target!=null)
			&&(!mob.amDead())
			&&(target.charStats().getStat(CharStats.STAT_INTELLIGENCE)>5)
			&&(CMLib.flags().canSpeak(mob))
			&&(CMLib.flags().canBeHeardSpeakingBy(mob,target))
			&&(CMLib.flags().canBeSeenBy(target,mob)))
			{
				String say="Penis wrinkle!";
				switch(CMLib.dice().roll(1,30,0))
				{
				case 1:
					say = "You are a very bad " + target.charStats().displayClassName() + "!";
					break;
				case 2:
					say = "I think all " + target.charStats().raceName() + "s are stupid!";
					break;
				case 3:
					say = "Damn flark!";
					break;
				case 4:
					say = "Squeegee!";
					break;
				case 5:
					say = "Ding dong!";
					break;
				case 6:
					say = "Goober!";
					break;
				case 7:
					say = "Noodle" + ((target.charStats().getStat(CharStats.STAT_GENDER) == 'M') ? "boy" : "girl") + "!";
					break;
				case 8:
					say = "Groin scratcher!";
					break;
				case 9:
					say = "Geek!";
					break;
				case 10:
					say = "Dork!";
					break;
				case 11:
					say = "Orc kisser!";
					break;
				case 12:
					say = "Jerk!";
					break;
				case 13:
					say = "Tuddleworm!";
					break;
				case 14:
					say = "Poopie diaper!";
					break;
				case 15:
					say = "Panty stain!";
					break;
				case 16:
					say = "Blah blah blah blah blah!";
					break;
				case 17:
					say = "Hairpit sniffer!";
					break;
				case 18:
					say = "Gluteous maximus cavity!";
					break;
				case 19:
					say = "Uncle copulator!";
					break;
				case 20:
					say = "Toe jam eater!";
					break;
				case 21:
					say = "Partial excrement!";
					break;
				case 22:
					say = "Female dog!";
					break;
				case 23:
					say = "Illegitimate offspring!";
					break;
				case 24:
					say = "You are overweight!";
					break;
				case 25:
					say = "You smell funny!";
					break;
				case 26:
					say = "You aren't very smart!";
					break;
				case 27:
					say = "You.. you.. ah nevermind.";
					break;
				case 28:
					say = "Yokle!";
					break;
				case 29:
					say = "Ugly head!";
					break;
				case 30:
					say = "Goop" + ((target.charStats().getStat(CharStats.STAT_GENDER) == 'M') ? "boy" : "girl") + "!";
					break;
				}
				CMLib.commands().postSay(mob,target,L(say),false,false);
				if((target!=invoker)&&(target!=mob)&&(target.fetchEffect(ID())==null))
				{
					if(CMLib.dice().rollPercentage()>target.charStats().getSave(CharStats.STAT_SAVE_DISEASE))
					{
						final Room room=target.location();
						final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_CAST_VERBAL_SPELL,L("<T-NAME> get(s) an odd look on <T-HIS-HER> face."));
						if((room!=null)&&(room.okMessage(mob, msg)))
						{
							room.send(mob,msg);
							target.tell(L("You feel different somehow..."));
							maliciousAffect(invoker,target,0,0,-1);
						}
					}
					else
						spreadImmunity(target);
				}
			}
		}
		return true;
	}

	@Override
	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected==null)
			return;
		affectableStats.setStat(CharStats.STAT_CHARISMA,2);
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
			invoker=mob;
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,auto?"":L("^S<S-NAME> incant(s) rudely to <T-NAMESELF>.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_DISEASE|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					invoker=mob;
					maliciousAffect(mob,target,asLevel,0,-1);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> feel(s) different somehow..."));
				}
				else
					spreadImmunity(target);
			}
		}
		else
			return maliciousFizzle(mob,target,L("<S-NAME> incant(s) rudely to <T-NAMESELF>, but the spell fades."));
		// return whether it worked
		return success;
	}
}
