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
public class Spell_TimeStop extends Spell
{
	public String ID() { return "Spell_TimeStop"; }
	public String name(){return "Time Stop";}
	public String displayText(){return "(Time is Stopped)";}
	protected int canAffectCode(){return CAN_ROOMS|CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 100;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}

	protected Vector fixed=new Vector();

    public void setMiscText(String newText)
    {
        super.setMiscText(newText);
        fixed=new Vector();
    }
    
	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected!=null)&&(canBeUninvoked()))
		{
			if(affected instanceof Room)
			{
				Room room=(Room)affected;
				room.showHappens(CMMsg.MSG_OK_VISUAL, "Time starts moving again...");
				if(invoker!=null)
				{
					Ability me=invoker.fetchEffect(ID());
					if(me!=null)
						me.unInvoke();
				}
				CMLib.threads().resumeTicking(room,-1);
				for(int i=0;i<fixed.size();i++)
				{
					MOB mob2=(MOB)fixed.elementAt(i);
					CMLib.threads().resumeTicking(mob2,-1);
				}
				fixed=new Vector();
			}
			else
			if(affected instanceof MOB)
			{
				MOB mob=(MOB)affected;
				CMLib.threads().resumeTicking(mob,-1);
				if(mob.location()!=null)
				{
					mob.location().show(mob, null, CMMsg.MSG_OK_VISUAL, "Time starts moving again...");
					Ability me=mob.location().fetchEffect(ID());
					if(me!=null)
						me.unInvoke();
				}
			}
		}
		super.unInvoke();
	}
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected!=null)
		   &&(affected instanceof Room))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_ENTER:
			case CMMsg.TYP_LEAVE:
			case CMMsg.TYP_FLEE:
				if(msg.source()==invoker)
					msg.source().tell("You cannot travel beyond the time stopped area.");
				else
					msg.source().tell("Nothing just happened.  You didn't do that.");
				return false;
			default:
				if((msg.source() == invoker)
				&&(msg.target() != invoker)
				&&(msg.target() instanceof MOB)
			    &&((CMath.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
			    ||(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			    ||(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))))
				{
					if(invoker.getVictim()==null)
						invoker.setVictim((MOB)msg.target());
				}
				else
				if((msg.source()!=invoker)
				   &&(!CMath.bset(msg.sourceCode(),CMMsg.MASK_ALWAYS))
				   &&(!CMath.bset(msg.targetCode(),CMMsg.MASK_ALWAYS)))
				{
					msg.source().tell("Time is stopped. Nothing just happened.  You didn't do that.");
					return false;
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		Environmental target = mob.location();

		if(target.fetchEffect(this.ID())!=null)
		{
		    mob.tell(mob,null,null,"Time has already been stopped here!");
			return false;
		}


		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.

			CMMsg msg = CMClass.getMsg(mob, target, this,verbalCastCode(mob,target,auto),(auto?"T":"^S<S-NAME> speak(s) and gesture(s) and t")+"ime suddenly STOPS!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(msg.value()<=0)
				{
					Room room=mob.location();
					fixed=new Vector();
					HashSet grpMembers = mob.getGroupMembers(new HashSet());
					for(int m=0;m<room.numInhabitants();m++)
					{
						MOB mob2=room.fetchInhabitant(m);
						if((mob2!=mob)&&(mob.mayIFight(mob2)))
						{
							msg=CMClass.getMsg(mob,mob2,this,CMMsg.MASK_MALICIOUS|CMMsg.TYP_MIND,null);
							if(!grpMembers.contains(mob2))
							{
								if(room.okMessage(mob, msg))
								{
									room.send(mob, msg);
									if(msg.value()>0)
										return false;
								}
								else
									return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s) for awhile, but the spell fizzles.");
							}
						}
					}
					CMLib.threads().suspendTicking(room,-1);
					for(int m=0;m<room.numInhabitants();m++)
					{
						MOB mob2=room.fetchInhabitant(m);
						if(mob2!=mob)
						{
							fixed.addElement(mob2);
							CMLib.threads().suspendTicking(mob2,-1);
						}
					}
					beneficialAffect(mob,room,asLevel,2);
				}
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> incant(s) for awhile, but the spell fizzles.");

		// return whether it worked
		return success;
	}
}
