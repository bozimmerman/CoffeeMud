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
public class Prayer_HolyDay extends Prayer
{
	public String ID() { return "Prayer_HolyDay"; }
	public String name(){ return "Holy Day";}
	public String displayText(){ return "(Holy Day)";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	protected String godName="the gods";

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof Area)))
			return;
		Area A=(Area)affected;

		super.unInvoke();

		if(canBeUninvoked())
		{
			for(Enumeration e=A.getMetroMap();e.hasMoreElements();)
			{
				Room R=(Room)e.nextElement();
				R.showHappens(CMMsg.MSG_OK_VISUAL,"The holy day has ended.");
			}
		}
	}

	public boolean okMessage(Environmental host, CMMsg msg)
	{
		if((msg.tool() instanceof Ability)
		&&(!((Ability)msg.tool()).isAutoInvoked())
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_COMMON_SKILL)
		&&(msg.source()!=invoker()))
		{
			msg.source().tell("You are not allowed to work on the holy day of "+godName+".");
			return false;
		}
		else
		if(((msg.sourceMinor()==CMMsg.TYP_BUY)
			||(msg.sourceMinor()==CMMsg.TYP_BID)
			||(msg.sourceMinor()==CMMsg.TYP_SELL)
			||(msg.sourceMinor()==CMMsg.TYP_WITHDRAW)
			||(msg.sourceMinor()==CMMsg.TYP_BORROW)
			||(msg.sourceMinor()==CMMsg.TYP_DEPOSIT))
		&&(msg.source()!=invoker()))
		{
			msg.source().tell("You are not allowed to work or do commerce on the holy day of "+godName+".");
			return false;
		}
		else
		if(((CMath.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
			||(CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			||(CMath.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)))
		&&(msg.source().getClanID().length()>0))
		{
            LegalBehavior B=null;
			if(msg.source().location()!=null) 
			    B=CMLib.law().getLegalBehavior(msg.source().location());
			if((B!=null)&&(B.controlPoints()>0))
			{
				msg.source().tell("There can be no conquest on the holy day of "+godName+".");
				return false;
			}
		}
		return super.okMessage(host,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected==null)||(!(affected instanceof Area)))
			return super.tick(ticking,tickID);

		if(((Area)affected).getTimeObj().getTimeOfDay()==15)
			unInvoke();

		return super.tick(ticking,tickID);
	}
	
    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
        }
        return super.castingQuality(mob,target);
    }
    
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Area target=mob.location().getArea();
		if(target==null) return false;
		if(target.fetchEffect(ID())!=null)
		{
			target.fetchEffect(ID()).unInvoke();
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for a holy day.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(Enumeration e=target.getMetroMap();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					godName=mob.getWorshipCharID();
					if((godName.length()==0)||(CMLib.map().getDeity(godName)==null))
						godName="the gods";
					R.showHappens(CMMsg.MSG_OK_VISUAL,"A holy day of "+godName+" has begun!");
				}
				beneficialAffect(mob,target,asLevel,CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY));
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for a holy day, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}
