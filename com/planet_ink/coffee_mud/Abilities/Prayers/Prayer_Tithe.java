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
public class Prayer_Tithe extends Prayer
{
	public String ID() { return "Prayer_Tithe"; }
	public String name(){ return "Tithe";}
	public String displayText(){ return "(Tithe)";}
	public int quality(){ return MALICIOUS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your need to tithe fades.");
	}

	public void affectCharStats(MOB affected, CharStats affectableStats)
	{
		super.affectCharStats(affected,affectableStats);
		if(affected instanceof ShopKeeper)
			affectableStats.setStat(CharStats.CHARISMA,affectableStats.getStat(CharStats.CHARISMA)+2);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_GET)
		&&(msg.source()==affected)
		&&(msg.target() instanceof Coins))
		{
			int num=((Coins)msg.target()).numberOfCoins();
			((Coins)msg.target()).setNumberOfCoins(num-(num/10));
			if(invoker()!=msg.source())
			{
				invoker().tell(msg.source().name()+" tithes.");
				invoker().setMoney(invoker().getMoney()+(num/10));
			}
		}
		if((msg.sourceMinor()==CMMsg.TYP_BUY)
		&&(msg.amITarget(affected))
		&&(msg.tool()!=null))
		{
			ShopKeeper SK=CoffeeUtensils.getShopKeeper((MOB)affected);
			if(SK.doIHaveThisInStock(msg.tool().Name()+"$",msg.source()))
			{
				int[] val=SK.yourValue(msg.source(),msg.tool(),true);
				if((val[0]>0)&&(val[0]<=MoneyUtils.totalMoney(msg.source())))
					if(invoker()!=msg.target())
					{
						invoker().tell(msg.source().name()+" tithes.");
						invoker().setMoney(invoker().getMoney()+(val[0]/10));
					}
			}
		}
		super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"<T-NAME> become(s) filled with a need to tithe!":"^S<S-NAME> "+prayWord(mob)+" for <T-YOUPOSS> need to tithe!^?");
			FullMsg msg3=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg3)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg3);
				if((msg.value()<=0)&&(msg3.value()<=0))
					maliciousAffect(mob,target,asLevel,0,-1);
			}
		}
		else
			return maliciousFizzle(mob,null,"<S-NAME> "+prayWord(mob)+" for <T-YOUPOSS> tithing need but there is no answer.");

		// return whether it worked
		return success;
	}
}
