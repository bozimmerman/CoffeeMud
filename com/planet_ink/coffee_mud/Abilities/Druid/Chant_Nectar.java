package com.planet_ink.coffee_mud.Abilities.Druid;
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
public class Chant_Nectar extends Chant
{
	public String ID() { return "Chant_Nectar"; }
	public String name(){ return "Nectar";}
	public int classificationCode(){return Ability.ACODE_CHANT|Ability.DOMAIN_PLANTGROWTH;}
	protected int canAffectCode(){return Ability.CAN_ITEMS;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canTargetCode(){return 0;}
	public Vector drank=null;
	protected int lastNum=-1;

	public void unInvoke()
	{
		if((affected==null)
		||(!(affected instanceof Item))
		||(((Item)affected).owner()==null)
		||(!(((Item)affected).owner() instanceof Room)))
			super.unInvoke();
		else
		{
			Item littleSpring=(Item)affected;
			Room SpringLocation=CMLib.map().roomLocation(littleSpring);
			if(canBeUninvoked())
				SpringLocation.showHappens(CMMsg.MSG_OK_VISUAL,littleSpring.name()+" dries up.");
			super.unInvoke();
			if(canBeUninvoked())
			{
				Item spring=littleSpring; // protects against uninvoke loops!
				spring.destroy();
				SpringLocation.recoverRoomStats();
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		if(affected==null) return false;
		if(!(affected instanceof Item)) return false;
		Item littleSpring=(Item)affected;
		Room R=CMLib.map().roomLocation(affected);
		if(R==null) return false;
		if(lastNum!=R.numInhabitants())
		{
			lastNum=R.numInhabitants();
			return true;
		}
		if(lastNum<1) return true;
		MOB M=R.fetchInhabitant(CMLib.dice().roll(1,lastNum,-1));
		if(M==null) return true;
		if(drank==null) drank=new Vector();
		if(drank.contains(M)) return true;
		drank.addElement(M);
		if(CMLib.dice().rollPercentage()>M.charStats().getSave(CharStats.STAT_SAVE_MIND))
		{
			Vector commands=new Vector();
			commands.addElement("DRINK");
			commands.addElement(R.getContextName(littleSpring));
			M.enqueCommand(commands,Command.METAFLAG_FORCED,0);
		}
		return true;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(affected!=null)
		if(msg.amITarget(affected))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_DRINK:
				{
					MOB M=msg.source();
					int hp=CMLib.dice().roll(1,M.charStats().getStat(CharStats.STAT_CONSTITUTION)+super.getX1Level(invoker())+super.getXLEVELLevel(invoker()),0);
					CMLib.combat().postHealing(M,M,this,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,hp,null);
					int mana=CMLib.dice().roll(1,((M.charStats().getStat(CharStats.STAT_WISDOM)+M.charStats().getStat(CharStats.STAT_INTELLIGENCE))/2)+super.getX1Level(invoker())+super.getXLEVELLevel(invoker()),0);
					M.curState().adjMana(mana,M.maxState());
					int move=CMLib.dice().roll(1,((M.charStats().getStat(CharStats.STAT_WISDOM)+M.charStats().getStat(CharStats.STAT_INTELLIGENCE))/2)+super.getX1Level(invoker())+super.getXLEVELLevel(invoker()),0);
					M.curState().adjMovement(move,M.maxState());
				}
				break;
			default:
				break;
			}
		}
		super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!auto)
		{
			if((mob.location().domainType()&Room.INDOORS)>0)
			{
				mob.tell("You must be outdoors for this chant to work.");
				return false;
			}
			if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
			   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT)
			   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)
			   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_AIR)
			   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_WATERSURFACE))
			{
				mob.tell("This magic will not work here.");
				return false;
			}
		}


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		// now see if it worked
		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> chant(s) for nectar.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item newItem=CMClass.getItem("Spring");
				newItem.setName("an enormous flower");
				newItem.setDisplayText("an enormous flower is dripping with nectar");
				newItem.setDescription("The closer you look, the more illusive the flower becomes.  There must be druid magic at work here!");
				Ability A=CMClass.getAbility("Poison_Liquor");
				if(A!=null) newItem.addNonUninvokableEffect(A);

				mob.location().addItem(newItem);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"Suddenly, "+newItem.name()+" starts flowing here.");
				drank=new Vector();
				lastNum=-1;
				beneficialAffect(mob,newItem,asLevel,0);
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) for nectar, but nothing happens.");

		// return whether it worked
		return success;
	}
}
