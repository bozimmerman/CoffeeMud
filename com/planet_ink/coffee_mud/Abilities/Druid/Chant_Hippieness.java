package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2000-2013 Bo Zimmerman

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
public class Chant_Hippieness extends Chant
{
	public String ID() { return "Chant_Hippieness"; }
	public String name(){return "Hippieness";}
	public String displayText(){return "(Feeling Groovy)";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_ENDURING;}
	public int abstractQuality(){ return Ability.QUALITY_BENEFICIAL_SELF;}
	protected int canAffectCode(){return CAN_MOBS;}
	protected List<Pair<Clan,Integer>> oldClans=null;

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affectableStats.setStat(CharStats.STAT_WISDOM,affectableStats.getStat(CharStats.STAT_WISDOM)-2);
		if(affectableStats.getStat(CharStats.STAT_WISDOM)<1)
			affectableStats.setStat(CharStats.STAT_WISDOM,1);
		for(Pair<Clan,Integer> p : affected.clans())
			oldClans.add(p);
		affected.setClan("",Integer.MIN_VALUE); // deletes all clans
	}

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if(affected instanceof MOB)
		{
			for(Pair<Clan,Integer> p : ((MOB)affected).clans())
				oldClans.add(p);
			((MOB)affected).setClan("",Integer.MIN_VALUE); // deletes all clans
		}

		if((msg.source()==affected)
		&&(msg.tool() instanceof Ability)
		&&(!msg.tool().ID().equals("FoodPrep"))
		&&(!msg.tool().ID().equals("Cooking"))
		&&(((((Ability)msg.tool()).classificationCode()&Ability.ALL_DOMAINS)==Ability.DOMAIN_CRAFTINGSKILL)
			||((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)))
		{
			msg.source().tell("No, man... work is so bourgeois...");
			return false;
		}
		return super.okMessage(host,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;
			for(Pair<Clan,Integer> p : mob.clans())
				oldClans.add(p);
			mob.setClan("",Integer.MIN_VALUE); // deletes all clans
			
			boolean mouthed=mob.fetchFirstWornItem(Wearable.WORN_MOUTH)!=null;
			Room R=mob.location();
			if((!mouthed)&&(R!=null)&&(R.numItems()>0))
			{
				Item I=R.getRandomItem();
				if((I!=null)&&(I.fitsOn(Wearable.WORN_MOUTH)))
					CMLib.commands().postGet(mob,I.container(),I,false);
			}

			if(mob.numItems()>0)
			{
				Item I=mob.getRandomItem();
				if(mouthed)
				{
					if((I!=null)&&(!I.amWearingAt(Wearable.IN_INVENTORY))&&(!I.amWearingAt(Wearable.WORN_MOUTH)))
						CMLib.commands().postRemove(mob,I,false);
				}
				else
				if((I!=null)&&(I instanceof Light)&&(I.fitsOn(Wearable.WORN_MOUTH)))
				{
					if((I instanceof Container)
					&&(((Container)I).containTypes()==Container.CONTAIN_SMOKEABLES)
					&&(((Container)I).getContents().size()==0))
					{
						Item smoke=CMClass.getItem("GenResource");
						if(smoke!=null)
						{
							smoke.setName("some smoke");
							smoke.setDescription("Looks liefy and green.");
							smoke.setDisplayText("some smoke is sitting here.");
							smoke.setMaterial(RawMaterial.RESOURCE_HEMP);
							smoke.basePhyStats().setWeight(1);
							smoke.setBaseValue(25);
							smoke.recoverPhyStats();
							smoke.text();
							mob.addItem(smoke);
							smoke.setContainer((Container)I);
						}
					}
					mob.doCommand(CMParms.parse("WEAR \""+I.Name()+"\""),Command.METAFLAG_FORCED);
				}
				else
				if((I!=null)&&(!I.amWearingAt(Wearable.IN_INVENTORY))&&(!I.amWearingAt(Wearable.WORN_MOUTH)))
					CMLib.commands().postRemove(mob,I,false);
			}
		}
		return true;
	}


	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
		{
			for(Pair<Clan,Integer> p : oldClans)
				mob.setClan(p.first.clanID(),p.second.intValue());
			mob.tell("You don't feel quite so groovy.");
		}
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
				if(CMLib.flags().isAnimalIntelligence((MOB)target))
					return Ability.QUALITY_INDIFFERENT;
			}
		}
		return super.castingQuality(mob,target);
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(CMLib.flags().isAnimalIntelligence(target))
		{
			mob.tell(target.name()+" is not smart enough to be a hippy.");
			return false;
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,target,this,(target.isMonster()?0:CMMsg.MASK_MALICIOUS)|verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> chant(s) to <T-NAMESELF>!^?");
			CMMsg msg2=CMClass.getMsg(mob,target,this,(target.isMonster()?0:CMMsg.MASK_MALICIOUS)|CMMsg.MSK_CAST_VERBAL|CMMsg.TYP_DISEASE|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					oldClans=new LinkedList<Pair<Clan,Integer>>();
					for(Pair<Clan,Integer> p : target.clans())
						oldClans.add(p);
					target.setClan("",Integer.MIN_VALUE); // deletes all clans
					CMLib.commands().postSay(target,null,"Far out...",false,false);
					maliciousAffect(mob,target,asLevel,0,verbalCastMask(mob,target,auto)|CMMsg.TYP_MIND);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> chant(s) to <T-NAMESELF>, but nothing more happens.");

		// return whether it worked
		return success;
	}
}
