package com.planet_ink.coffee_mud.Abilities.Druid;

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

public class Chant_PlantWall extends Chant
{
	public String ID() { return "Chant_PlantWall"; }
	public String name(){return "Plant Wall";}
	public String displayText(){return "(Plant Wall)";}
	public int maxRange(){return 10;}
	public int minRange(){return 1;}
	public int quality(){ return INDIFFERENT;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}

	private int amountRemaining=0;
	private Item theWall=null;
	private String deathNotice="";

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Item)))
			return true;

		MOB mob=msg.source();

		if((invoker!=null)
		&&(mob.isInCombat())
		&&(mob!=invoker)
		&&(mob.getVictim()==invoker))
		{
			if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
			&&(mob.rangeToTarget()>0)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Weapon)
			&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)
			&&(msg.tool().maxRange()>0))
			{
				mob.location().show(mob,null,CMMsg.MSG_WEAPONATTACK,"^F^<FIGHT^><S-NAME> fire(s) at the plant wall with "+msg.tool().name()+".^</FIGHT^>^?");
				amountRemaining-=mob.envStats().damage();
				if(amountRemaining<0)
				{
					deathNotice="The plant wall is destroyed!";
					((Item)affected).destroy();
				}
				return false;
			}
			else
			if((mob.rangeToTarget()==1)&&(msg.sourceMinor()==CMMsg.TYP_ADVANCE))
			{
				Item w=mob.fetchWieldedItem();
				if(w==null) w=mob.myNaturalWeapon();
				if(w==null) return false;
 				mob.location().show(mob,null,CMMsg.MSG_WEAPONATTACK,"^F<S-NAME> hack(s) at the plant wall with "+w.name()+".^?");
				amountRemaining-=mob.envStats().damage();
				if(amountRemaining<0)
				{
					deathNotice="The plant wall is destroyed!";
					((Item)affected).destroy();
				}
				return false;
			}
			else
			if((mob.rangeToTarget()>0)
			&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(msg.tool().maxRange()>0))
			{
				mob.location().show(mob,null,msg.tool(),CMMsg.MSG_OK_VISUAL,"^F^<FIGHT^>The plant wall absorbs <O-NAME> from <S-NAME>.^</FIGHT^>^?");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		super.unInvoke();
		if(canBeUninvoked())
		{
			if((theWall!=null)
			&&(invoker!=null)
			&&(theWall.owner()!=null)
			&&(theWall.owner() instanceof Room)
			&&(((Room)theWall.owner()).isContent(theWall)))
			{
				((Room)theWall.owner()).show(invoker,null,CMMsg.MSG_OK_VISUAL,deathNotice);
				Item wall=theWall;
				theWall=null;
				wall.destroy();
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==MudHost.TICK_MOB)
		{
			if((invoker!=null)
			   &&(theWall!=null)
			   &&(invoker.location()!=null)
			   &&(!invoker.location().isContent(theWall)))
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(((mob.location().domainType()&Room.INDOORS)>0)&&(!auto))
		{
			mob.tell("You must be outdoors for this chant to work.");
			return false;
		}
		if((!mob.isInCombat())||(mob.rangeToTarget()<1))
		{
			mob.tell("You really should be in ranged combat to use this chant.");
			return false;
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)&&(I.fetchEffect(ID())!=null))
			{
				mob.tell("There is already a plant wall here.");
				return false;
			}
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Environmental target = mob.location();


		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			FullMsg msg = new FullMsg(mob, target, this,affectType(auto),auto?"A plant wall appears!":"^S<S-NAME> chant(s) for a plant wall!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				amountRemaining=mob.baseState().getHitPoints()/6;
				Item I=CMClass.getItem("GenItem");
				I.setName("a plant wall");
				I.setDisplayText("a writhing plant wall has grown here");
				I.setDescription("The wall is thick and stringy.");
				I.setMaterial(EnvResource.RESOURCE_GREENS);
				Sense.setGettable(I,false);
				I.recoverEnvStats();
				mob.location().addItem(I);
				theWall=I;
				deathNotice="The plant wall withers away!";
				beneficialAffect(mob,I,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s), but the magic fizzles.");

		// return whether it worked
		return success;
	}
}
