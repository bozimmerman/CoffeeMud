package com.planet_ink.coffee_mud.Abilities.Misc;
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
   Copyright 2000-2018 Mike Rundell

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

public class Dragonbreath extends StdAbility
{
	@Override
	public String ID()
	{
		return "Dragonbreath";
	}

	private final static String	localizedName	= CMLib.lang().L("Dragonbreath");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(10);
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_MOBS;
	}

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	private static final String[]	triggerStrings	= I(new String[] { "DRAGONBREATH" });

	protected boolean lesser = false;
	
	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
		if((affected instanceof MOB)&&(!CMLib.flags().canBreatheThis((MOB)affected, RawMaterial.RESOURCE_DUST)))
			affectableStats.setSensesMask(affectableStats.sensesMask()|PhyStats.CAN_NOT_BREATHE);
	}
	
	@Override
	public void setMiscText(String newMiscText)
	{
		List<String> parms = CMParms.parse(newMiscText.toUpperCase().trim());
		lesser = parms.contains("LESSER");
		super.setMiscText(newMiscText);
	}
	
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_RACIALABILITY;
	}

	protected char getBreathColor(MOB mob)
	{
		char colorc='f';
		if(mob == null)
			mob = invoker();
		if((text().length()==0)
		&&(mob!=null)
		&&(mob.charStats().getMyRace().racialCategory().equals("Dragon")))
		{
			int color=-1;
			for(int i=0;i<DragonColors.length;i++)
			{
				if(CMLib.english().containsString(mob.Name(),DragonColors[i][0]))
				{
					color = i;
					break;
				}
			}
			if(color<0)
			{
				for(int i=0;i<DragonColors.length;i++)
				{
					if(CMLib.english().containsString(mob.displayText(),DragonColors[i][0]))
					{
						color = i;
						break;
					}
				}
			}
			if(color<0)
				colorc='f';
			else
				colorc=DragonColors[color][1].charAt(0);
		}
		else
		if(text().trim().length()>0)
			colorc=text().trim().toLowerCase().charAt(0);
		else
		{
			final int x=CMLib.dice().roll(1,DragonColors.length,-1);
			colorc=DragonColors[x][1].charAt(0);
		}
		return colorc;
	}
	
	@Override
	public long flags()
	{
		switch(getBreathColor(null))
		{
		case 'f': // fire
			return super.flags() | Ability.FLAG_FIREBASED;
		case 'l': // lightning
			return super.flags() | Ability.FLAG_AIRBASED;
		case 'c':// cold
			return super.flags() | Ability.FLAG_WATERBASED;
		case 'a': // acid
			return super.flags() | Ability.FLAG_EARTHBASED;
		case 'o': // ooze
			return super.flags() | Ability.FLAG_EARTHBASED;
		case 's': // slime
			return super.flags() | Ability.FLAG_EARTHBASED;
		case 'g':// gas
			return super.flags() | Ability.FLAG_INTOXICATING;
		case 'u':// undead
			return super.flags() | Ability.FLAG_UNHOLY;
		case 'p': // pebbles
			return super.flags();
		case 'd': // dust
			return super.flags();
		default:
			return super.flags() | Ability.FLAG_FIREBASED;
		}
	}
	
	private final static String[][] DragonColors={
		{"WHITE","c"},
		{"BLACK","a"},
		{"BLUE","l"},
		{"GREEN","g"},
		{"RED","f"},
		{"BRASS","f"},
		{"COPPER","a"},
		{"BRONZE","l"},
		{"SILVER","c"},
		{"GOLD","g"},
	};

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell(L("There doesn't appear to be anyone here worth breathing on."));
			return false;
		}
		if(!CMLib.flags().canBreatheHere(mob,mob.location()))
		{
			mob.tell(L("You can't breathe!"));
			return false;
		}
		if(mob.charStats().getBodyPart(Race.BODY_MOUTH)==0)
		{
			mob.tell(L("You don't have a mouth!"));
			return false;
		}
		this.setInvoker(mob);
		char colorc = getBreathColor(mob);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		final String puffPhrase;
		final String autoPhrase;
		final String stuffWord;
		final String castPhrase;
		final int weaponType;
		final int strikeType;

		switch(colorc)
		{
		default:
		case 'f':
			puffPhrase=L("<S-NAME> puff(s) smoke from <S-HIS-HER> mouth.");
			autoPhrase=L("A blast of flames erupts!");
			stuffWord=L("flames");
			castPhrase=L("<S-NAME> blast(s) flames from <S-HIS-HER> mouth!");
			weaponType=Weapon.TYPE_BURNING;
			strikeType=CMMsg.TYP_FIRE;
			break;
		case 'l':
			puffPhrase=L("<S-NAME> spark(s) a little from <S-HIS-HER> mouth.");
			autoPhrase=L("A blast of lightning bursts erupt!");
			stuffWord=L("bolt");
			castPhrase=L("<S-NAME> shoot(s) numerous bursts of lightning from <S-HIS-HER> mouth!")+CMLib.protocol().msp("lightning.wav",40);
			weaponType=Weapon.TYPE_STRIKING;
			strikeType=CMMsg.TYP_ELECTRIC;
			break;
		case 'c':
			puffPhrase=L("<S-NAME> puff(s) cold air from <S-HIS-HER> mouth.");
			autoPhrase=L("A blast of frozen air erupts!");
			stuffWord=L("cold");
			castPhrase=L("<S-NAME> blast(s) a frozen cone of frost from <S-HIS-HER> mouth!")+CMLib.protocol().msp("spelldam1.wav",40);
			weaponType=Weapon.TYPE_FROSTING;
			strikeType=CMMsg.TYP_COLD;
			break;
		case 'a':
			puffPhrase=L("<S-NAME> dribble(s) acid harmlessly from <S-HIS-HER> mouth.");
			autoPhrase=L("A spray of acid erupts!");
			stuffWord=L("acid");
			castPhrase=L("<S-NAME> spray(s) acid from <S-HIS-HER> mouth!")+CMLib.protocol().msp("water.wav",40);
			weaponType=Weapon.TYPE_MELTING;
			strikeType=CMMsg.TYP_ACID;
			break;
		case 'o':
			puffPhrase=L("<S-NAME> bubbles(s) acidic ooze harmlessly from <S-HIS-HER> mouth.");
			autoPhrase=L("A splurt of acidic ooze erupts!");
			stuffWord=L("ooze");
			castPhrase=L("<S-NAME> belch(es) acidic ooze from <S-HIS-HER> mouth!");
			weaponType=Weapon.TYPE_MELTING;
			strikeType=CMMsg.TYP_ACID;
			break;
		case 's':
			puffPhrase=L("<S-NAME> ooze(s) acidic slime harmlessly from <S-HIS-HER> mouth.");
			autoPhrase=L("A splurt of acidic slime erupts!");
			stuffWord=L("slime");
			castPhrase=L("<S-NAME> sling(s) acidic slime from <S-HIS-HER> mouth!");
			weaponType=Weapon.TYPE_MELTING;
			strikeType=CMMsg.TYP_ACID;
			break;
		case 'g':
			puffPhrase=L("<S-NAME> puff(s) gas harmlessly from <S-HIS-HER> mouth.");
			autoPhrase=L("A cloud of deadly gas descends!");
			stuffWord=L("gas");
			castPhrase=L("<S-NAME> blow(s) deadly gas from <S-HIS-HER> mouth!");
			weaponType=Weapon.TYPE_GASSING;
			strikeType=CMMsg.TYP_GAS;
			if(CMLib.dice().rollPercentage()<50)
				success = false;
			break;
		case 'p':
			puffPhrase=L("<S-NAME> dribble(s) pebbles harmlessly from <S-HIS-HER> mouth.");
			autoPhrase=L("A line of pebbles shoot out!");
			stuffWord=L("pebbles");
			castPhrase=L("<S-NAME> blow(s) a line of pebbles from <S-HIS-HER> mouth!");
			weaponType=Weapon.TYPE_BASHING;
			strikeType=CMMsg.TYP_JUSTICE;
			if(CMLib.dice().rollPercentage()<50)
				success = false;
			break;
		case 'u':
			puffPhrase=L("<S-NAME> cough(s) death harmlessly from <S-HIS-HER> mouth.");
			autoPhrase=L("A death ray shoot out!");
			stuffWord=L("death");
			castPhrase=L("<S-NAME> blow(s) a ray of death from <S-HIS-HER> mouth!");
			weaponType=Weapon.TYPE_BURSTING;
			strikeType=CMMsg.TYP_UNDEAD;
			if(CMLib.dice().rollPercentage()<50)
				success = false;
			break;
		case 'd':
			puffPhrase=L("<S-NAME> puff(s) dust harmlessly from <S-HIS-HER> mouth.");
			autoPhrase=L("A cloud of dust descends!");
			stuffWord=L("dust");
			castPhrase=L("<S-NAME> blow(s) a cloud of dust from <S-HIS-HER> mouth!");
			weaponType=Weapon.TYPE_GASSING;
			strikeType=CMMsg.TYP_GAS;
			if(CMLib.dice().rollPercentage()<50)
				success = false;
			break;
		}
		final Room R=mob.location();
		if((success)&&(R!=null))
		{

			if(text().length()==0)
				setMiscText("");
			if(R.show(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,auto?autoPhrase:castPhrase))
			{
				for (final Object element : h)
				{
					final MOB target=(MOB)element;

					final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|strikeType|(auto?CMMsg.MASK_ALWAYS:0),null);
					if(R.okMessage(mob,msg))
					{
						R.send(mob,msg);
						invoker=mob;

						if(colorc == 'd')
						{
							int ticks = 2 + ((getX1Level(mob)+getXLEVELLevel(mob))/4);
							if(msg.value()<=0)
								maliciousAffect(mob, target, asLevel, ticks, strikeType);
						}
						else
						{
							int damage = 0;
							int levelBy=(mob.phyStats().level()+(2*getXLEVELLevel(mob))+(2*getX1Level(mob)))/4;
							if(levelBy<1)
								levelBy=1;
							if(lesser)
								damage += CMLib.dice().roll(levelBy,4,0);
							else
								damage += CMLib.dice().roll(levelBy,6,levelBy);
							if(msg.value()>0)
								damage = (int)Math.round(CMath.div(damage,2.0));
							CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.MASK_SOUND|strikeType,weaponType,L("^F^<FIGHT^>The @x1 <DAMAGE> <T-NAME>!^</FIGHT^>^?",stuffWord));
						}
					}
				}
			}
		}
		else
			return maliciousFizzle(mob,null,puffPhrase);

		// return whether it worked
		return success;
	}
}
