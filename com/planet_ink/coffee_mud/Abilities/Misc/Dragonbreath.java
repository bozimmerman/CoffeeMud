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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2000-2014 Mike Rundell

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

@SuppressWarnings("rawtypes")
public class Dragonbreath extends StdAbility
{
	@Override public String ID() { return "Dragonbreath"; }
	private final static String localizedName = CMLib.lang()._("Dragonbreath");
	@Override public String name() { return localizedName; }
	@Override public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	@Override public int maxRange(){return adjustedMaxInvokerRange(10);}
	@Override protected int canAffectCode(){return 0;}
	@Override protected int canTargetCode(){return Ability.CAN_MOBS;}
	@Override public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings =_i(new String[] {"DRAGONBREATH"});
	@Override public String[] triggerStrings(){return triggerStrings;}
	@Override public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_RACIALABILITY;}
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
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell(_("There doesn't appear to be anyone here worth breathing on."));
			return false;
		}
		if(!CMLib.flags().canBreatheHere(mob,mob.location()))
		{
			mob.tell(_("You can't breathe!"));
			return false;
		}
		if(mob.charStats().getBodyPart(Race.BODY_MOUTH)==0)
		{
			mob.tell(_("You don't have a mouth!"));
			return false;
		}
		char colorc='f';
		if((text().length()==0)
		&&(mob.charStats().getMyRace().racialCategory().equals("Dragon")))
		{
			int color=-1;
			for(int i=0;i<DragonColors.length;i++)
				if(CMLib.english().containsString(mob.Name(),DragonColors[i][0]))
				{ color=i; break;}
			if(color<0)
			for(int i=0;i<DragonColors.length;i++)
				if(CMLib.english().containsString(mob.displayText(),DragonColors[i][0]))
				{ color=i; break;}
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
			final int x=CMLib.dice().roll(1,5,-1);
			colorc=("rlcag").substring(x,x+1).charAt(0);
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		String puffPhrase="<S-NAME> puff(s) smoke from <S-HIS-HER> mouth.";
		String autoPhrase="A blast of flames erupts!";
		String stuffWord="flames";
		String castPhrase="<S-NAME> blast(s) flames from <S-HIS-HER> mouth!";
		int WeaponType=Weapon.TYPE_BURNING;
		int strikeType=CMMsg.TYP_FIRE;

		switch(colorc)
		{
		case 'f':
				break;
		case 'l':
				puffPhrase="<S-NAME> spark(s) a little from <S-HIS-HER> mouth.";
				autoPhrase="A blast of lightning bursts erupt!";
				stuffWord="bolt";
				castPhrase="<S-NAME> shoot(s) numerous bursts of lightning from <S-HIS-HER> mouth!"+CMLib.protocol().msp("lightning.wav",40);
				WeaponType=Weapon.TYPE_STRIKING;
				strikeType=CMMsg.TYP_ELECTRIC;
				break;
		case 'c':
				puffPhrase="<S-NAME> puff(s) cold air from <S-HIS-HER> mouth.";
				autoPhrase="A blast of frozen air erupts!";
				stuffWord="cold";
				castPhrase="<S-NAME> blast(s) a frozen cone of frost from <S-HIS-HER> mouth!"+CMLib.protocol().msp("spelldam1.wav",40);
				WeaponType=Weapon.TYPE_FROSTING;
				strikeType=CMMsg.TYP_COLD;
				break;
		case 'a':
				puffPhrase="<S-NAME> dribble(s) acid harmlessly from <S-HIS-HER> mouth.";
				autoPhrase="A spray of acid erupts!";
				stuffWord="acid";
				castPhrase="<S-NAME> spray(s) acid from <S-HIS-HER> mouth!"+CMLib.protocol().msp("water.wav",40);
				WeaponType=Weapon.TYPE_MELTING;
				strikeType=CMMsg.TYP_ACID;
				break;
		case 'g':
				if(CMLib.dice().rollPercentage()>50)
				{
					puffPhrase="<S-NAME> puff(s) gas harmlessly from <S-HIS-HER> mouth.";
					autoPhrase="A cloud of deadly gas descends!";
					stuffWord="gas";
					castPhrase="<S-NAME> blow(s) deadly gas from <S-HIS-HER> mouth!";
					WeaponType=Weapon.TYPE_GASSING;
					strikeType=CMMsg.TYP_GAS;
				}
				break;

		}
		final Room R=mob.location();
		if((success)&&(R!=null))
		{

			if(text().length()==0)
				setMiscText("");
			if(R.show(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,auto?autoPhrase:castPhrase))
				for (final Object element : h)
				{
					final MOB target=(MOB)element;

					// it worked, so build a copy of this ability,
					// and add it to the affects list of the
					// affected MOB.  Then tell everyone else
					// what happened.
					final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|strikeType|(auto?CMMsg.MASK_ALWAYS:0),null);
					if(R.okMessage(mob,msg))
					{
						R.send(mob,msg);
						invoker=mob;

						int damage = 0;
						int levelBy=(mob.phyStats().level()+(2*getXLEVELLevel(mob)))/4;
						if(levelBy<1) levelBy=1;
						damage += CMLib.dice().roll(levelBy,6,levelBy);
						if(msg.value()>0)
							damage = (int)Math.round(CMath.div(damage,2.0));
						CMLib.combat().postDamage(mob,target,this,damage,CMMsg.MASK_ALWAYS|CMMsg.MASK_SOUND|strikeType,WeaponType,"^F^<FIGHT^>The "+stuffWord+" <DAMAGE> <T-NAME>!^</FIGHT^>^?");
					}
				}
		}
		else
			return maliciousFizzle(mob,null,puffPhrase);


		// return whether it worked
		return success;
	}
}
