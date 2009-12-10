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
public class Prayer_Tithe extends Prayer
{
	public String ID() { return "Prayer_Tithe"; }
	public String name(){ return "Tithe";}
	public String displayText(){ return "(Tithe)";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	public int abstractQuality(){ return Ability.QUALITY_MALICIOUS;}
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
			affectableStats.setStat(CharStats.STAT_CHARISMA,affectableStats.getStat(CharStats.STAT_CHARISMA)+2);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((msg.sourceMinor()==CMMsg.TYP_GET)
		&&(msg.source()==affected)
		&&(msg.target() instanceof Coins))
		{
			long num=((Coins)msg.target()).getNumberOfCoins();
			((Coins)msg.target()).setNumberOfCoins(num-(num/10));
			if((invoker()!=msg.source())&&((num/10)>0))
			{
				invoker().tell(msg.source(),null,null,"<S-NAME> tithes.");
			    String currency=((Coins)msg.target()).getCurrency();
				CMLib.beanCounter().addMoney(invoker(),currency,CMath.mul(((Coins)msg.target()).getDenomination(),(num/10)));
			}
		}
		if((msg.sourceMinor()==CMMsg.TYP_BUY)
		&&(msg.amITarget(affected))
		&&(msg.tool()!=null))
		{
			ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(affected);
			if(SK.getShop().doIHaveThisInStock("$"+msg.tool().Name()+"$",msg.source()))
			{
			    ShopKeeper.ShopPrice price=CMLib.coffeeShops().sellingPrice((MOB)affected,msg.source(),msg.tool(),SK,true);
				if((price.absoluteGoldPrice>0.0)&&(price.absoluteGoldPrice<=CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(msg.source(),invoker())))
					if(invoker()!=msg.target())
					{
						invoker().tell(msg.source(),null,null,"<S-NAME> tithes.");
						CMLib.beanCounter().addMoney(invoker(),CMath.div(price.absoluteGoldPrice,10.0));
					}
			}
		}
		super.executeMsg(myHost,msg);
	}

    public int castingQuality(MOB mob, Environmental target)
    {
        if(mob!=null)
        {
            if(mob.isInCombat())
                return Ability.QUALITY_INDIFFERENT;
            if(target instanceof MOB)
            {
            }
        }
        return super.castingQuality(mob,target);
    }

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"<T-NAME> become(s) filled with a need to tithe!":"^S<S-NAME> "+prayWord(mob)+" for <T-YOUPOSS> need to tithe!^?");
			CMMsg msg3=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
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
