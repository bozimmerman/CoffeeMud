package com.planet_ink.coffee_mud.Abilities.Spells;
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
public class Spell_WallOfFire extends Spell
{
	public String ID() { return "Spell_WallOfFire"; }
	public String name(){return "Wall of Fire";}
	public String displayText(){return "(Wall of Fire)";}
	public int maxRange(){return adjustedMaxInvokerRange(10);}
	public int minRange(){return 1;}
	public int abstractQuality(){ return Ability.QUALITY_OK_SELF;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_HEATING|Ability.FLAG_FIREBASED;}

	protected Item theWall=null;
	protected String deathNotice="";

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID==Tickable.TICKID_MOB)
		{
			if((invoker!=null)
			   &&(theWall!=null)
			   &&(invoker.location()!=null))
			{
				Room room=invoker.location();
				if(!invoker.location().isContent(theWall))
					unInvoke();
				else
				for(int m=0;m<room.numInhabitants();m++)
				{
					MOB mob=room.fetchInhabitant(m);
					if((mob!=null)
					&&(mob!=invoker)
					&&(mob.isInCombat())
					&&(mob.getVictim()==invoker)
					&&(mob.rangeToTarget()==1))
					{
						int damage = CMLib.dice().roll((int)Math.round((invoker.envStats().level()+super.getXLEVELLevel(invoker())+(2.0*super.getX1Level(invoker())))/4.0),6,1);
						CMLib.combat().postDamage(invoker,mob,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_FIRE,Weapon.TYPE_BURNING,"The wall of fire flares and <DAMAGE> <T-NAME>!");
					}
				}
			}
		}
		return super.tick(ticking,tickID);
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
				mob.tell("There is already a wall of fire here.");
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


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			CMMsg msg = CMClass.getMsg(mob, target, this,verbalCastCode(mob,target,auto),(auto?"A blazing wall of fire appears!":"^S<S-NAME> conjur(s) up a blazing wall of fire!^?")+CMProps.msp("fireball.wav",10));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item I=CMClass.getItem("GenItem");
				I.setName("a wall of fire");
				I.setDisplayText("a blazing wall of fire is burning here");
				I.setDescription("The flames are high and hot.");
				I.setMaterial(RawMaterial.RESOURCE_NOTHING);
				I.baseEnvStats().setDisposition(I.baseEnvStats().disposition()|EnvStats.IS_LIGHTSOURCE);
				CMLib.flags().setGettable(I,false);
				I.recoverEnvStats();
				mob.location().addItem(I);
				theWall=I;
				deathNotice="The wall of fire flickers...";
				beneficialAffect(mob,I,asLevel,20);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s), but the magic fizzles.");

		// return whether it worked
		return success;
	}
}
