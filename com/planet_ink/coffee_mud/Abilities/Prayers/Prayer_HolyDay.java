package com.planet_ink.coffee_mud.Abilities.Prayers;

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

public class Prayer_HolyDay extends Prayer
{
	public String ID() { return "Prayer_HolyDay"; }
	public String name(){ return "Holy Day";}
	public String displayText(){ return "(Holy Day)";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	public int quality(){ return MALICIOUS;}
	protected int overrideMana(){return Integer.MAX_VALUE;}
	public long flags(){return Ability.FLAG_HOLY|Ability.FLAG_UNHOLY;}
	private String godName="the gods";

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
		&&((((Ability)msg.tool()).classificationCode()&Ability.ALL_CODES)==Ability.COMMON_SKILL)
		&&(msg.source()!=invoker()))
		{
			msg.source().tell("You are not allowed to work on the holy day of "+godName+".");
			return false;
		}
		else
		if(((msg.sourceMinor()==CMMsg.TYP_BUY)
			||(msg.sourceMinor()==CMMsg.TYP_SELL)
			||(msg.sourceMinor()==CMMsg.TYP_WITHDRAW)
			||(msg.sourceMinor()==CMMsg.TYP_DEPOSIT))
		&&(msg.source()!=invoker()))
		{
			msg.source().tell("You are not allowed to work on the holy day of "+godName+".");
			return false;
		}
		else
		if(((Util.bset(msg.sourceCode(),CMMsg.MASK_MALICIOUS))
			||(Util.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			||(Util.bset(msg.othersCode(),CMMsg.MASK_MALICIOUS)))
		&&(msg.source().getClanID().length()>0))
		{
			Behavior B=null;
			if(msg.source().location()!=null) 
			    B=CoffeeUtensils.getLegalBehavior(msg.source().location());
			if((B!=null)&&(B).modifyBehavior(CoffeeUtensils.getLegalObject(msg.source().location()),msg.source(),new Integer(Law.MOD_CONTROLPOINTS)))
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

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> "+prayWord(mob)+" for a holy day.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				for(Enumeration e=target.getMetroMap();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					godName=mob.getWorshipCharID();
					if((godName.length()==0)||(CMMap.getDeity(godName)==null))
						godName="the gods";
					R.showHappens(CMMsg.MSG_OK_VISUAL,"A holy day of "+godName+" has begun!");
				}
				beneficialAffect(mob,target,asLevel,CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY));
			}
		}
		else
			return beneficialWordsFizzle(mob,target,"<S-NAME> "+prayWord(mob)+" for a holy day, but <S-HIS-HER> plea is not answered.");


		// return whether it worked
		return success;
	}
}
