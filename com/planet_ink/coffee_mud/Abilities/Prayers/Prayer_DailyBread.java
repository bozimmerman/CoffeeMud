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
public class Prayer_DailyBread extends Prayer
{
	public String ID() { return "Prayer_DailyBread"; }
	public String name(){ return "Daily Bread";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;}
	public int abstractQuality(){ return Ability.QUALITY_OK_OTHERS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	protected int overrideMana(){return 100;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		int levelDiff=target.envStats().level()-(mob.envStats().level()+(2*getXLEVELLevel(mob)));
		if(levelDiff<0) levelDiff=0;
		boolean success=proficiencyCheck(mob,-(levelDiff*25),auto);
		Item Bread=null;
		Item BreadContainer=null;
		for(int i=0;i<target.inventorySize();i++)
		{
			Item I=target.fetchInventory(i);
			if((I!=null)&&(I instanceof Food))
			{
				if(I.container()!=null)
				{
					Bread=I;
					BreadContainer=I.container();
				}
				else
				{
					Bread=I;
					BreadContainer=null;
					break;
				}
			}
		}
		if((Bread!=null)&&(BreadContainer!=null))
			CMLib.commands().postGet(target,BreadContainer,Bread,false);
		if(Bread==null)
		{
			ShopKeeper SK=CMLib.coffeeShops().getShopKeeper(target);
			if(SK!=null)
			{
				Vector inv=SK.getShop().getStoreInventory();
				for(int i=0;i<inv.size();i++)
				{
					Item I=(Item)inv.elementAt(i);
					if((I!=null)&&(I instanceof Food))
					{
						Bread=(Item)I.copyOf();
						target.addInventory(Bread);
						break;
					}
				}
			}
		}
		if((success)&&(Bread!=null))
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),"^S<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to provide <S-HIS-HER> daily bread!^?");
			CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					msg=CMClass.getMsg(target,mob,Bread,CMMsg.MSG_GIVE,"<S-NAME> gladly donate(s) <O-NAME> to <T-NAMESELF>.");
					if(mob.location().okMessage(mob,msg))
						mob.location().send(mob,msg);
				}
			}
		}
		else
			maliciousFizzle(mob,target,auto?"":"<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to provide <S-HIS-HER> daily bread, but nothing happens.");


		// return whether it worked
		return success;
	}
}
