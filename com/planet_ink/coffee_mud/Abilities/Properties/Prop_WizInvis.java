package com.planet_ink.coffee_mud.Abilities.Properties;
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
public class Prop_WizInvis extends Property
{
	public String ID() { return "Prop_WizInvis"; }
	public String displayText() 
	{
	    if(CMath.bset(abilityCode(),EnvStats.IS_CLOAKED|EnvStats.IS_NOT_SEEN))
		    return "(Wizard Invisibility)";
	    else
	    if(CMath.bset(abilityCode(),EnvStats.IS_NOT_SEEN))
		    return "(WizUndetectable)";
	    else
	    if(CMath.bset(abilityCode(),EnvStats.IS_CLOAKED))
		    return "(Cloaked)";
	    else
	        return "";
	}
	public String name(){ return "Wizard Invisibility";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected boolean disabled=false;
	protected int abilityCode=EnvStats.IS_NOT_SEEN|EnvStats.IS_CLOAKED;
	public int abilityCode(){return abilityCode;}
	public void setAbilityCode(int newCode){abilityCode=newCode;}

	public String accountForYourself()
	{ return "Wizard Invisibile";	}


	public boolean canBeUninvoked(){return true;}
	public boolean isAnAutoEffect(){return false;}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		// when this spell is on a MOBs Affected list,
		// it should consistantly put the mob into
		// a sleeping state, so that nothing they do
		// can get them out of it.
		affectableStats.setDisposition(affectableStats.disposition()|abilityCode);
		if(CMath.bset(abilityCode(),EnvStats.IS_NOT_SEEN))
		{
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_INVISIBLE);
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_HIDDEN);
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SNEAKING);
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_FLYING);
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_CLIMBING);
			affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SWIMMING);
		}
		if((affected instanceof MOB)&&(!CMLib.flags().canBreathe((MOB)affected)))
			affectableStats.setSensesMask(affectableStats.sensesMask()-EnvStats.CAN_NOT_BREATHE);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_HIDDEN);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_DARK);
		affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_SEE_INVISIBLE);
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		affected.curState().setHunger(affected.maxState().maxHunger(affected.baseWeight()));
		affected.curState().setThirst(affected.maxState().maxThirst(affected.baseWeight()));
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		if(affected==null) return;
		Environmental being=affected;

		if(this.canBeUninvoked())
		{
			being.delEffect(this);
			if(being instanceof Room)
				((Room)being).recoverRoomStats();
			else
			if(being instanceof MOB)
			{
				if(((MOB)being).location()!=null)
					((MOB)being).location().recoverRoomStats();
				else
				{
					being.recoverEnvStats();
					((MOB)being).recoverCharStats();
					((MOB)being).recoverMaxState();
				}
			}
			else
				being.recoverEnvStats();
			mob.tell("You begin to fade back into view.");
		}
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS)&&(msg.amITarget(affected))&&(affected!=null)&&(!disabled)))
		{
			if(msg.source()!=msg.target())
			{
				msg.source().tell("Ah, leave "+affected.name()+" alone.");
				if(affected instanceof MOB)
					((MOB)affected).makePeace();
			}
			return false;
		}
		else
		if((affected!=null)&&(affected instanceof MOB))
		{
			if((CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))&&(msg.amISource((MOB)affected)))
				disabled=true;
			else
			if((msg.amISource((MOB)affected))
			&&(CMath.bset(msg.source().getBitmap(),MOB.ATT_SYSOPMSGS))
			&&(msg.source().location()!=null)
			&&(!CMSecurity.isAllowed(msg.source(),msg.source().location(),"SYSMSGS")))
				msg.source().setBitmap(CMath.unsetb(msg.source().getBitmap(),MOB.ATT_SYSOPMSGS));
		}

		return super.okMessage(myHost,msg);
	}
}
