package com.planet_ink.coffee_mud.Abilities.Spells;

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
public class Spell_WallOfForce extends Spell
{
	public String ID() { return "Spell_WallOfForce"; }
	public String name(){return "Wall of Force";}
	public String displayText(){return "(Wall of Force)";}
	public int maxRange(){return 10;}
	public int minRange(){return 1;}
	public int quality(){ return OK_SELF;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.SPELL|Ability.DOMAIN_CONJURATION;}

	private Item theWall=null;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Item)))
			return true;

		MOB mob=msg.source();

		if((invoker!=null)
		&&(mob.isInCombat())
		&&((mob.getVictim()==invoker)||(mob==invoker))
		&&(mob.rangeToTarget()>=1)
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&((msg.targetCode()&CMMsg.MASK_MALICIOUS)>0))
		{
			if(((msg.tool()!=null)
				&&(msg.tool() instanceof Ability))
			||((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
				&&(msg.tool()!=null)
				&&(msg.tool() instanceof Weapon)
				&&(!((Weapon)msg.tool()).amWearingAt(Item.INVENTORY))
				&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)))
			{
				mob.tell("Malice neither escapes nor enters the wall of force.");
				if(mob.isMonster())
					CommonMsgs.remove(mob,(Item)msg.tool(),true);
				return false;
			}
			if((msg.sourceMinor()==CMMsg.TYP_ADVANCE)
			&&((mob==invoker)||(mob.rangeToTarget()==1)))
			{
				if(mob!=invoker)
                {
                    FullMsg msg2=new FullMsg(mob,null,CMMsg.MSG_WEAPONATTACK,"^F^<FIGHT^><S-NAME> attempt(s) to penetrate the wall of force and fail(s).^</FIGHT^>^?");
                    CMColor.fixSourceFightColor(msg2);
					if(mob.location().okMessage(mob,msg2))
                        mob.location().send(mob,msg2);
                }
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
				((Room)theWall.owner()).showHappens(CMMsg.MSG_OK_VISUAL,"The wall of force is gone.");
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
		if((!mob.isInCombat())||(mob.rangeToTarget()<1))
		{
			mob.tell("You really should be in ranged combat to cast this.");
			return false;
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I!=null)&&(I.fetchEffect(ID())!=null))
			{
				mob.tell("There is already a wall of force here.");
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

			FullMsg msg = new FullMsg(mob, target, this,affectType(auto),auto?"An impenetrable wall of force appears!":"^S<S-NAME> conjur(s) up a impenetrable wall of force!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item I=CMClass.getItem("GenItem");
				I.setName("a wall of force");
				I.setDisplayText("an impenetrable wall of force surrounds "+mob.name());
				I.setDescription("It`s tough, that's for sure.");
				I.setMaterial(EnvResource.RESOURCE_NOTHING);
				Sense.setGettable(I,false);
				I.recoverEnvStats();
				mob.location().addItem(I);
				theWall=I;
				beneficialAffect(mob,I,asLevel,10);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s), but the magic fizzles.");

		// return whether it worked
		return success;
	}
}
