package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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

@SuppressWarnings("unchecked")
public class Prayer_CreateIdol extends Prayer
{
	public String ID() { return "Prayer_CreateIdol"; }
	public String name(){ return "Create Idol";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CURSING;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public boolean bubbleAffect(){return true;}

	public void affectEnvStats(Environmental aff, EnvStats affectableStats)
	{
		super.affectEnvStats(aff,affectableStats);
		if((affected instanceof Item)&&(((Item)affected).container()==null))
		{
			int xlvl=super.getXLEVELLevel(invoker());
			affectableStats.setArmor(affectableStats.armor()+(20+(4*xlvl)));
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()-10-(2*xlvl));
		}
	}

	public void affectCharStats(MOB aff, CharStats affectableStats)
	{
		super.affectCharStats(aff,affectableStats);
		if((affected instanceof Item)&&(((Item)affected).container()==null))
		{
			if(affectableStats.getStat(CharStats.STAT_STRENGTH)>3) affectableStats.setStat(CharStats.STAT_STRENGTH,3);
			if(affectableStats.getStat(CharStats.STAT_DEXTERITY)>2) affectableStats.setStat(CharStats.STAT_DEXTERITY,2);
			if(affectableStats.getStat(CharStats.STAT_CONSTITUTION)>1) affectableStats.setStat(CharStats.STAT_CONSTITUTION,1);
		}
	}

	public void affectCharState(MOB aff, CharState affectableState)
	{
		super.affectCharState(aff,affectableState);
		if((affected instanceof Item)&&(((Item)affected).container()==null))
		{
			aff.curState().setFatigue(CharState.FATIGUED_MILLIS+10);
			affectableState.setMovement(20);
		}
	}


	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.target() instanceof MOB)
		&&(affected instanceof Item)
		&&(msg.tool()==affected)
		&&(!((MOB)msg.target()).willFollowOrdersOf(msg.source())))
		{
			msg.source().tell(msg.target().name()+" won`t accept "+msg.tool().name()+".");
			return false;
		}
		return super.okMessage(host,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((mob.getWorshipCharID().length()==0)||(CMLib.map().getDeity(mob.getWorshipCharID())==null))
		{
			mob.tell("You must worship a god to use this prayer.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		int material=-1;
		Room R=mob.location();
		if(R!=null)
		{
			if(((R.myResource()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_ROCK)
				||((R.myResource()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL)
				||((R.myResource()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL))
				material=R.myResource();
			else
			{
				Vector V=R.resourceChoices();
				if((V!=null)&&(V.size()>0))
				for(int v=0;v<V.size()*10;v++)
				{
					int rsc=((Integer)V.elementAt(CMLib.dice().roll(1,V.size(),-1))).intValue();
					if(((rsc&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_ROCK)
						||((rsc&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL)
						||((rsc&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL))
					{
						material=rsc;
						break;
					}
				}
			}
		}

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if((success)&&(material>0))
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for an idol.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item newItem=CMClass.getBasicItem("GenItem");
				newItem.setBaseValue(1);
				String name=CMLib.english().startWithAorAn(RawMaterial.CODES.NAME(material).toLowerCase()+" idol of "+mob.getWorshipCharID());
				newItem.setName(name);
				newItem.setDisplayText(name+" sits here.");
				newItem.baseEnvStats().setDisposition(EnvStats.IS_EVIL);
				newItem.baseEnvStats().setWeight(10);
				newItem.setMaterial(material);
				newItem.recoverEnvStats();
				CMLib.flags().setRemovable(newItem,false);
				CMLib.flags().setDroppable(newItem,false);
				newItem.addNonUninvokableEffect((Ability)copyOf());
				mob.location().addItemRefuse(newItem,CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_RESOURCE));
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" grows out of the ground.");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for an idol, but there is no answer.");

		// return whether it worked
		return success;
	}
}
