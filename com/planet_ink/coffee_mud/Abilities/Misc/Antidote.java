package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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

public class Antidote extends StdAbility
{
	public String ID() { return "Antidote"; }
	public String name(){ return "An Antidote";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_ITEMS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int classificationCode(){return Ability.SKILL;}
	private boolean processing=false;

	public Vector returnOffensiveAffects(Environmental fromMe)
	{
		Vector offenders=new Vector();

		for(int a=0;a<fromMe.numEffects();a++)
		{
			Ability A=fromMe.fetchEffect(a);
			if((A!=null)
			&&((A.classificationCode()&ALL_CODES)==Ability.POISON)
			&&((text().length()==0)||(A.name().toUpperCase().indexOf(text().toUpperCase())>=0)||(A.ID().toUpperCase().indexOf(text().toUpperCase())>=0)))
				offenders.addElement(A);
		}
		return offenders;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(affected==null) return;
		if(affected instanceof Item)
		{
			if(!processing)
			{
				Item myItem=(Item)affected;
				if(myItem.owner()==null) return;
				processing=true;
				if(msg.amITarget(myItem))
					switch(msg.sourceMinor())
					{
					case CMMsg.TYP_DRINK:
						if(myItem instanceof Drink)
						{
							invoke(msg.source(),null,msg.source(),true);
							myItem.destroy();
						}
						break;
					case CMMsg.TYP_EAT:
						if(myItem instanceof Food)
						{
							invoke(msg.source(),null,msg.source(),true);
							myItem.destroy();
						}
						break;
					case CMMsg.TYP_WEAR:
						if(myItem.rawProperLocationBitmap()!=Item.HELD)
						{
							invoke(msg.source(),null,msg.source(),true);
							myItem.destroy();
						}
						break;
					}
			}
			processing=false;
		}
		super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		Vector offensiveAffects=returnOffensiveAffects(target);

		if((success)&&(offensiveAffects.size()>0))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_OK_VISUAL,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(int a=offensiveAffects.size()-1;a>=0;a--)
					((Ability)offensiveAffects.elementAt(a)).unInvoke();
				if((!Sense.stillAffectedBy(target,offensiveAffects,false))&&(target.location()!=null))
					target.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> feel(s) better now.");
			}
		}

		// return whether it worked
		return success;
	}
}
