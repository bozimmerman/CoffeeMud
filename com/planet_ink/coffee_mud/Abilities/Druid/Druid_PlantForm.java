package com.planet_ink.coffee_mud.Abilities.Druid;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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

public class Druid_PlantForm extends StdAbility
{
	public String ID() { return "Druid_PlantForm"; }
	public String name(){ return "Plant Form";}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"PLANTFORM"};
	public String[] triggerStrings(){return triggerStrings;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return 0;}

	public int classificationCode(){return Ability.SKILL;}

	public Race newRace=null;
	public String raceName="";

	public String displayText()
	{
		if(newRace==null)
		{
			unInvoke();
			return "";
		}
		return "(in "+newRace.name().toLowerCase()+" form)";
	}

	private static String[] shapes={
	"Flower",
	"Vine",
	"Tumbleweed",
	"Shambler"
	};
	private static String[] races={
	"Flower",
	"Vine",
	"Tumbleweed",
	"Shambler"
	};

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0)
		&&((msg.amITarget(affected))))
		{
			MOB target=(MOB)msg.target();
			if((!target.isInCombat())
			&&(msg.source().isMonster())
			&&(msg.source().getVictim()!=target))
			{
				msg.source().tell("Attack a plant?!");
				if(target.getVictim()==msg.source())
				{
					target.makePeace();
					target.setVictim(null);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if((newRace!=null)&&(affected instanceof MOB))
		{
			affectableStats.setName(Util.startWithAorAn(raceName.toLowerCase()));
			int oldAdd=affectableStats.weight()-affected.baseEnvStats().weight();
			newRace.setHeightWeight(affectableStats,'M');
			if(oldAdd>0) affectableStats.setWeight(affectableStats.weight()+oldAdd);
		}
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(newRace!=null)
	    {
		    int oldCat=affected.baseCharStats().ageCategory();
			affectableStats.setMyRace(newRace);
			if(affected.baseCharStats().getStat(CharStats.AGE)>0)
				affectableStats.setStat(CharStats.AGE,newRace.getAgingChart()[oldCat]);
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
		int classLevel=CMAble.qualifyingClassLevel(mob,this)-CMAble.qualifyingLevel(mob,this);
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
			return 3;
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
			if((A!=null)&&(A instanceof Druid_PlantForm))
				return true;
		}
		return false;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		for(int a=mob.numEffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)&&(A instanceof Druid_PlantForm))
			{
				A.unInvoke();
				return true;
			}
		}

		if((mob.location().domainType()&Room.INDOORS)>0)
		{
			mob.tell("You must be outdoors to take on your plant form.");
			return false;
		}
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT))
		{
			mob.tell("You must be in the wild to take on your plant form.");
			return false;
		}

		int classLevel=CMAble.qualifyingClassLevel(mob,this)-CMAble.qualifyingLevel(mob,this);
		String choice=Util.combine(commands,0);
		if(choice.trim().length()>0)
		{
			StringBuffer buf=new StringBuffer("Plant Forms:\n\r");
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
		raceName=getRaceName(classLevel);
		newRace=getRace(classLevel);

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if((!appropriateToMyAlignment(mob.getAlignment()))&&(!auto))
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
				beneficialAffect(mob,mob,asLevel,Integer.MAX_VALUE);
				raceName=Util.capitalize(Util.startWithAorAn(raceName.toLowerCase()));
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
