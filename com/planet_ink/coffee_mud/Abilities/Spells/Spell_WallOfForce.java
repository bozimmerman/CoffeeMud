package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2000-2014 Bo Zimmerman

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
public class Spell_WallOfForce extends Spell
{
	@Override public String ID() { return "Spell_WallOfForce"; }
	@Override public String name(){return "Wall of Force";}
	@Override public String displayText(){return "(Wall of Force)";}
	@Override public int maxRange(){return adjustedMaxInvokerRange(10);}
	@Override public int minRange(){return 1;}
	@Override public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	@Override public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	@Override protected int canAffectCode(){return CAN_ITEMS;}
	@Override protected int canTargetCode(){return 0;}
	@Override public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}

	protected Item theWall=null;

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof Item)))
			return true;

		final MOB mob=msg.source();

		if((invoker!=null)
		&&(mob.isInCombat())
		&&((mob.getVictim()==invoker)||(mob==invoker))
		&&(mob.rangeToTarget()>=1)
		&&(msg.target()!=null)
		&&(msg.target() instanceof MOB)
		&&((msg.targetMajor()&CMMsg.MASK_MALICIOUS)>0))
		{
			if(((msg.tool()!=null)
				&&(msg.tool() instanceof Ability))
			||((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)
				&&(msg.tool()!=null)
				&&(msg.tool() instanceof Weapon)
				&&(!((Weapon)msg.tool()).amWearingAt(Wearable.IN_INVENTORY))
				&&(((Weapon)msg.tool()).weaponClassification()==Weapon.CLASS_RANGED)))
			{
				mob.tell(_("Malice neither escapes nor enters the wall of force."));
				if(mob.isMonster())
					CMLib.commands().postRemove(mob,(Item)msg.tool(),true);
				return false;
			}
			if((msg.sourceMinor()==CMMsg.TYP_ADVANCE)
			&&((mob==invoker)||(mob.rangeToTarget()==1)))
			{
				if(mob!=invoker)
				{
					final CMMsg msg2=CMClass.getMsg(mob,null,CMMsg.MSG_WEAPONATTACK,_("^F^<FIGHT^><S-NAME> attempt(s) to penetrate the wall of force and fail(s).^</FIGHT^>^?"));
					CMLib.color().fixSourceFightColor(msg2);
					if(mob.location().okMessage(mob,msg2))
						mob.location().send(mob,msg2);
				}
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
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
				((Room)theWall.owner()).showHappens(CMMsg.MSG_OK_VISUAL,_("The wall of force is gone."));
				final Item wall=theWall;
				theWall=null;
				wall.destroy();
			}
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((invoker!=null)
			   &&(theWall!=null)
			   &&(invoker.location()!=null)
			   &&(!invoker.location().isContent(theWall)))
				unInvoke();
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((!mob.isInCombat())||(mob.rangeToTarget()<1))
		{
			mob.tell(_("You really should be in ranged combat to cast this."));
			return false;
		}
		for(int i=0;i<mob.location().numItems();i++)
		{
			final Item I=mob.location().getItem(i);
			if((I!=null)&&(I.fetchEffect(ID())!=null))
			{
				mob.tell(_("There is already a wall of force here."));
				return false;
			}
		}

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Physical target = mob.location();


		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			final CMMsg msg = CMClass.getMsg(mob, target, this,verbalCastCode(mob,target,auto),auto?"An impenetrable wall of force appears!":"^S<S-NAME> conjur(s) up a impenetrable wall of force!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				final Item I=CMClass.getItem("GenItem");
				I.setName("a wall of force");
				I.setDisplayText("an impenetrable wall of force surrounds "+mob.name());
				I.setDescription("It`s tough, that's for sure.");
				I.setMaterial(RawMaterial.RESOURCE_NOTHING);
				CMLib.flags().setGettable(I,false);
				I.recoverPhyStats();
				mob.location().addItem(I);
				theWall=I;
				beneficialAffect(mob,I,asLevel,10);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,_("<S-NAME> incant(s), but the magic fizzles."));

		// return whether it worked
		return success;
	}
}
