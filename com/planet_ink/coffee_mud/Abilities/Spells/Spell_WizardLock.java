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
public class Spell_WizardLock extends Spell
{
	public String ID() { return "Spell_WizardLock"; }
	public String name(){return "Wizard Lock";}
	public String displayText(){return "(Wizard Locked)";}
	protected int canAffectCode(){return CAN_ITEMS|CAN_EXITS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS|Ability.CAN_EXITS;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(affected==null)
			return true;

		if(!super.okMessage(myHost,msg))
			return false;

		MOB mob=msg.source();
		if(((!msg.amITarget(affected))&&(msg.tool()!=affected))
		||(msg.source()==invoker())
		||(CMLib.law().doesHavePriviledgesHere(mob,msg.source().location()))&&(text().toUpperCase().indexOf("MALICIOUS")<0))
			return true;
        
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_OPEN:
			mob.tell(affected.name()+" appears to be magically locked.");
			return false;
		case CMMsg.TYP_UNLOCK:
			mob.tell(affected.name()+" appears to be magically locked.");
			return false;
		case CMMsg.TYP_JUSTICE:
			if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_DELICATE))
				return true;
		case CMMsg.TYP_DELICATE_HANDS_ACT:
			mob.tell(affected.name()+" appears to be magically protected.");
			return false;
		default:
			break;
		}
		return true;
	}

	public void unInvoke()
	{
		if((canBeUninvoked())&&(affected!=null))
		{
			if(affected instanceof Exit)
			{
				Exit exit=(Exit)affected;
				exit.setDoorsNLocks(exit.hasADoor(),!exit.hasADoor(),exit.defaultsClosed(),
									exit.hasALock(),exit.hasALock(),exit.defaultsLocked());
			}
			else
			if(affected instanceof Container)
			{
				Container container=(Container)affected;
				container.setLidsNLocks(container.hasALid(),!container.hasALid(),container.hasALock(),container.hasALock());
			}
		}
		super.unInvoke();
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Wizard Lock what?.");
			return false;
		}
		String targetName=CMParms.combine(commands,0);

		Environmental target=null;
		int dirCode=Directions.getGoodDirectionCode(targetName);
		if(dirCode>=0)
			target=mob.location().getExitInDir(dirCode);
		if(target==null)
			target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null) return false;

		if((!(target instanceof Container))&&(!(target instanceof Exit)))
		{
			mob.tell("You can't lock that.");
			return false;
		}

		if(target instanceof Container)
		{
			Container container=(Container)target;
			if((!container.hasALid())||(!container.hasALock()))
			{
				mob.tell("You can't lock that!");
				return false;
			}
		}
		else
		if(target instanceof Exit)
		{
			Exit exit=(Exit)target;
			if(!exit.hasADoor())
			{
				mob.tell("You can't lock that!");
				return false;
			}
		}

		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target.name()+" is already magically locked!");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> point(s) <S-HIS-HER> finger at <T-NAMESELF>, incanting.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(target instanceof Exit)
				{
					Exit exit=(Exit)target;
					exit.setDoorsNLocks(exit.hasADoor(),false,exit.defaultsClosed(),
										exit.hasALock(),true,exit.defaultsLocked());
					Room R=mob.location();
					Room R2=null;
					for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
						if(R.getExitInDir(d)==target)
						{ R2=R.getRoomInDir(d); break;}
					if((CMLib.law().doesOwnThisProperty(mob,R))
					||((R2!=null)&&(CMLib.law().doesOwnThisProperty(mob,R2))))
					{
						target.addNonUninvokableEffect((Ability)copyOf());
						CMLib.database().DBUpdateExits(R);
					}
					else
						beneficialAffect(mob,target,asLevel,Integer.MAX_VALUE/2);
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> look(s) shut tight!");
				}
				else
				if(target instanceof Container)
				{
					beneficialAffect(mob,target,asLevel,Integer.MAX_VALUE/2);
					Container container=(Container)target;
					container.setLidsNLocks(container.hasALid(),false,container.hasALock(),true);
					mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<T-NAME> look(s) shut tight!");
				}
				Ability lock=target.fetchEffect(ID());
				if(lock != null)
				{
					lock.setMiscText(Integer.toString(mob.envStats().level()));
					if(target instanceof Exit)
					{
						Room R=mob.location();
						if(!CMLib.law().doesHavePriviledgesHere(mob,R))
							for(int a=0;a<R.numEffects();a++)
								if((R.fetchEffect(a) instanceof LandTitle)
								   &&(((LandTitle)R.fetchEffect(a)).landOwner().length()>0))
									lock.setMiscText(lock.text()+" MALICIOUS");
					}
				}
			}

		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> point(s) at <T-NAMESELF>, incanting, but nothing happens.");


		// return whether it worked
		return success;
	}
}
