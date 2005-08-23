package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/*
   Copyright 2000-2005 Bo Zimmerman

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

public class Druid_GolemForm extends StdAbility
{
	public String ID() { return "Druid_GolemForm"; }
	public String name(){ return "Golem Form";}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"GOLEMFORM"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}

	public int classificationCode(){return Ability.SKILL;}

	public Race newRace=null;
	public String raceName="";
	public int raceLevel=0;

	public String displayText()
	{
		if(newRace==null)
		{
			unInvoke();
			return "";
		}
		return "(in "+raceName+" form)";
	}

	private static String[] shapes={
	"Steel Golem",
	"Quartz Golem",
	"Mithril Golem",
	"Diamond Golem",
	"Adamantite Golem"
	};
	private static String[] races={
	"MetalGolem",
	"StoneGolem",
	"MetaleGolem",
	"StoneGolem",
	"MetalGolem"
	};

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB))
		{
			affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_SPEAK);
			affectableStats.setName(Util.startWithAorAn(raceName.toLowerCase()));
			int oldAdd=affectableStats.weight()-affected.baseEnvStats().weight();
			newRace.setHeightWeight(affectableStats,'M');
			if(oldAdd>0) affectableStats.setWeight(affectableStats.weight()+oldAdd);
			switch(raceLevel)
			{
			case 0:
				affectableStats.setArmor(affectableStats.armor()-10);
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+10);
				affectableStats.setDamage(affectableStats.attackAdjustment()+5);
				affectableStats.setSpeed(affectableStats.speed()/1.5);
				break;
			case 1:
				affectableStats.setArmor(affectableStats.armor()-20);
				affectableStats.setSpeed(affectableStats.speed()/2.0);
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+20);
				affectableStats.setDamage(affectableStats.attackAdjustment()+10);
				break;
			case 2:
				affectableStats.setArmor(affectableStats.armor()-40);
				affectableStats.setSpeed(affectableStats.speed()/2.5);
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+40);
				affectableStats.setDamage(affectableStats.attackAdjustment()+20);
				break;
			case 3:
				affectableStats.setArmor(affectableStats.armor()-60);
				affectableStats.setSpeed(affectableStats.speed()/3.0);
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+80);
				affectableStats.setDamage(affectableStats.attackAdjustment()+40);
				break;
			case 4:
				affectableStats.setArmor(affectableStats.armor()-80);
				affectableStats.setSpeed(affectableStats.speed()/4.0);
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+160);
				affectableStats.setDamage(affectableStats.attackAdjustment()+80);
				break;
			}
		}
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null) affectableStats.setMyRace(newRace);
	}


	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
		switch(raceLevel)
		{
		case 0:
			affectableState.setMovement(affectableState.getMovement()/1);
			break;
		case 1:
			affectableState.setMovement(affectableState.getMovement()/2);
			break;
		case 2:
			affectableState.setMovement(affectableState.getMovement()/4);
			break;
		case 3:
			affectableState.setMovement(affectableState.getMovement()/8);
			break;
		case 4:
			affectableState.setMovement(affectableState.getMovement()/16);
			break;
		}
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob.location()!=null))
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> revert(s) to "+mob.charStats().raceName().toLowerCase()+" form.");
	}

	public void setRaceName(MOB mob)
	{
        int qualClassLevel=CMAble.qualifyingClassLevel(mob,this);
        int classLevel=qualClassLevel-CMAble.qualifyingLevel(mob,this);
        if(qualClassLevel<0) classLevel=30;
		raceName=getRaceName(classLevel);
		newRace=getRace(classLevel);
	}
	public int getRaceLevel(int classLevel)
	{
		if(classLevel<5)
			return 0;
		else
		if(classLevel<10)
			return 1;
		else
		if(classLevel<15)
			return 2;
		else
		if(classLevel<25)
			return 3;
		else
			return 4;
	}
	public Race getRace(int classLevel)
	{
		return CMClass.getRace(races[getRaceLevel(classLevel)]);
	}
	public String getRaceName(int classLevel)
	{
		return shapes[getRaceLevel(classLevel)];
	}

	public static boolean isShapeShifted(MOB mob)
	{
		if(mob==null) return false;
		for(int a=0;a<mob.numAllEffects();a++)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&(A instanceof Druid_GolemForm))
				return true;
		}
		return false;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		for(int a=mob.numEffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&(A instanceof Druid_GolemForm))
			{
				A.unInvoke();
				return true;
			}
		}

        int qualClassLevel=CMAble.qualifyingClassLevel(mob,this);
        int classLevel=qualClassLevel-CMAble.qualifyingLevel(mob,this);
        if(qualClassLevel<0) classLevel=30;
		String choice=Util.combine(commands,0);
		if(choice.trim().length()>0)
		{
			StringBuffer buf=new StringBuffer("Golem Forms:\n\r");
			Vector choices=new Vector();
			for(int i=0;i<classLevel;i++)
			{
				String s=getRaceName(i);
				if(!choices.contains(s))
				{
					choices.addElement(s);
					buf.append(s+"\n\r");
				}
				if(EnglishParser.containsString(s,choice))
				{
					classLevel=i;
					break;
				}
			}
			if(choice.equalsIgnoreCase("list"))
			{
				mob.tell(buf.toString());
				return true;
			}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if((!appropriateToMyFactions(mob))&&(!auto))
		{
			if((Dice.rollPercentage()<50))
			{
				mob.tell("Extreme emotions disrupt your change.");
				return false;
			}
		}

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_OK_ACTION,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				raceName=getRaceName(classLevel);
				newRace=getRace(classLevel);
				raceLevel=getRaceLevel(classLevel);
				beneficialAffect(mob,mob,asLevel,Integer.MAX_VALUE);
				raceName=Util.capitalizeAndLower(Util.startWithAorAn(raceName.toLowerCase()));
				mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> take(s) on "+raceName.toLowerCase()+" form.");
				mob.confirmWearability();
			}
		}
		else
			beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to <S-HIM-HERSELF>, but nothing happens.");


		// return whether it worked
		return success;
	}
}
