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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
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
public class Prayer_Divorce extends Prayer
{
	public String ID() { return "Prayer_Divorce"; }
	public String name(){ return "Divorce";}
	public int classificationCode(){return Ability.ACODE_PRAYER|Ability.DOMAIN_CORRUPTION;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public int abstractQuality(){return Ability.QUALITY_OK_OTHERS;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=getTarget(mob,commands,givenTarget);
		if(target==null) return false;
		if(!target.isMarriedToLiege())
		{
			mob.tell(target.name()+" is not married!");
			return false;
		}
		if(target.fetchWornItem("wedding band")!=null)
		{
			mob.tell(target.name()+" must remove the wedding band first.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> divorce(s) <T-NAMESELF> from "+target.getLiegeID()+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				if((!target.isMonster())&&(target.soulMate()==null))
					CMLib.coffeeTables().bump(target,CoffeeTableRow.STAT_DIVORCES);
				mob.location().send(mob,msg);
				String maleName=target.Name();
				String femaleName=target.getLiegeID();
				if(target.charStats().getStat(CharStats.STAT_GENDER)=='F')
				{
					femaleName=target.Name();
					maleName=target.getLiegeID();
				}
                Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.DIVORCES);
                for(int i=0;i<channels.size();i++)
                    CMLib.commands().postChannel((String)channels.elementAt(i),mob.getClanID(),maleName+" and "+femaleName+" are now divorced.",true);
				MOB M=CMLib.players().getPlayer(target.getLiegeID());
				if(M!=null) M.setLiegeID("");
				target.setLiegeID("");
				try
				{
					for(Enumeration e=CMLib.map().rooms();e.hasMoreElements();)
					{
						Room R=(Room)e.nextElement();
						LandTitle T=CMLib.law().getLandTitle(R);
						if((T!=null)&&(T.landOwner().equals(maleName)))
						{
							T.setLandOwner(femaleName);
							CMLib.database().DBUpdateRoom(R);
						}
						for(int i=0;i<R.numInhabitants();i++)
						{
							MOB M2=R.fetchInhabitant(i);
							if((M2!=null)&&(M2 instanceof Banker))
							{
								Banker B=(Banker)M2;
								Vector V=B.getDepositedItems(maleName);
								Item coins=B.findDepositInventory(femaleName,""+Integer.MAX_VALUE);
								for(int v=0;v<V.size();v++)
								{
									Item I=(Item)V.elementAt(v);
									if(I==null) break;
									B.delDepositInventory(maleName,I);
									if(I instanceof Coins)
									{
										if(coins!=null)
											B.delDepositInventory(femaleName,coins);
										else
										{
											coins=CMClass.getItem("StdCoins");
											((Coins)coins).setNumberOfCoins(0);
										}
										B.addDepositInventory(femaleName,coins);
									}
									else
										B.addDepositInventory(femaleName,I);
								}
							}
						}
					}
			    }catch(NoSuchElementException e){}
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> clear(s) <S-HIS-HER> throat.");

		return success;
	}
}
