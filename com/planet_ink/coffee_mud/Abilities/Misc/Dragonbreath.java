package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import java.util.*;

public class Dragonbreath extends StdAbility
{
	public String ID() { return "Dragonbreath"; }
	public String name(){ return "Dragonbreath";}
	public int quality(){return Ability.MALICIOUS;}
	public int maxRange(){return 10;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public boolean putInCommandlist(){return false;}
	private static final String[] triggerStrings = {"DRAGONBREATH"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Dragonbreath();}
	public int classificationCode(){return Ability.SKILL;}
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

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		Hashtable h=properTargets(mob,givenTarget,auto);
		if(h==null)
		{
			mob.tell("There doesn't appear to be anyone here worth breathing on.");
			return false;
		}
		char colorc='f';
		if((text().length()==0)
		&&(mob.charStats().getMyRace().racialCategory().equals("Dragon")))
		{
			int color=-1;
			for(int i=0;i<DragonColors.length;i++)
				if(EnglishParser.containsString(mob.Name(),DragonColors[i][0]))
				{ color=i; break;}
			if(color<0)
			for(int i=0;i<DragonColors.length;i++)
				if(EnglishParser.containsString(mob.displayText(),DragonColors[i][0]))
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
			int x=Dice.roll(1,5,-1);
			colorc=("rlcag").substring(x,x+1).charAt(0);
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

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
				castPhrase="<S-NAME> shoot(s) numerous bursts of lightning from <S-HIS-HER> mouth!"+CommonStrings.msp("lightning.wav",40);
				WeaponType=Weapon.TYPE_STRIKING;
				break;
		case 'c':
				puffPhrase="<S-NAME> puff(s) cold air from <S-HIS-HER> mouth.";
				autoPhrase="A blast of frozen air erupts!";
				stuffWord="cold";
				castPhrase="<S-NAME> blast(s) a frozen cone of frost from <S-HIS-HER> mouth!"+CommonStrings.msp("spelldam1.wav",40);
				WeaponType=Weapon.TYPE_FROSTING;
				break;
		case 'a':
				puffPhrase="<S-NAME> dribble(s) acid harmlessly from <S-HIS-HER> mouth.";
				autoPhrase="A spray of acid erupts!";
				stuffWord="acid";
				castPhrase="<S-NAME> spray(s) acid from <S-HIS-HER> mouth!"+CommonStrings.msp("water.wav",40);
				WeaponType=Weapon.TYPE_MELTING;
				break;
		case 'g':
				puffPhrase="<S-NAME> puff(s) gas harmlessly from <S-HIS-HER> mouth.";
				autoPhrase="A cloud of deadly gas descends!";
				stuffWord="gas";
				castPhrase="<S-NAME> blow(s) deadly gas from <S-HIS-HER> mouth!";
				WeaponType=Weapon.TYPE_GASSING;
				break;

		}

		if(success)
		{

			if(text().length()==0)
				setMiscText("");
			if(mob.location().show(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,auto?autoPhrase:castPhrase))
			for(Enumeration f=h.elements();f.hasMoreElements();)
			{
				MOB target=(MOB)f.nextElement();

				// it worked, so build a copy of this ability,
				// and add it to the affects list of the
				// affected MOB.  Then tell everyone else
				// what happened.
				FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSK_MALICIOUS_MOVE|strikeType|(auto?CMMsg.MASK_GENERAL:0),null);
				if(mob.location().okMessage(mob,msg))
				{
					mob.location().send(mob,msg);
					invoker=mob;

					int damage = 0;
					int maxDie =  mob.envStats().level();
					if (maxDie > 10)
						maxDie = 10;
					damage += Dice.roll(maxDie,6,1);
					if(msg.value()>0)
						damage = (int)Math.round(Util.div(damage,2.0));
					MUDFight.postDamage(mob,target,this,damage,CMMsg.MASK_GENERAL|CMMsg.MASK_SOUND|strikeType,WeaponType,"^FThe "+stuffWord+" <DAMAGE> <T-NAME>!^?");
				}
			}
		}
		else
			return maliciousFizzle(mob,null,puffPhrase);


		// return whether it worked
		return success;
	}
}