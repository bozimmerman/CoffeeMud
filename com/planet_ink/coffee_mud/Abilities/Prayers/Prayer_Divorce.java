package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


public class Prayer_Divorce extends Prayer
{
	public String ID() { return "Prayer_Divorce"; }
	public String name(){ return "Divorce";}
	public long flags(){return Ability.FLAG_UNHOLY;}
	public int quality(){return Ability.OK_OTHERS;}
	public Environmental newInstance(){	return new Prayer_Divorce();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
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

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> divorce(s) <T-NAMESELF> from "+target.getLiegeID()+".^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				String maleName=target.Name();
				String femaleName=target.getLiegeID();
				if(target.charStats().getStat(CharStats.GENDER)=='F')
				{
					femaleName=target.Name();
					maleName=target.getLiegeID();
				}
				MOB M=CMMap.getPlayer(target.getLiegeID());
				if(M!=null) M.setLiegeID("");
				target.setLiegeID("");
				for(Enumeration e=CMMap.rooms();e.hasMoreElements();)
				{
					Room R=(Room)e.nextElement();
					LandTitle T=CoffeeUtensils.getLandTitle(R);
					if((T!=null)&&(T.landOwner().equals(maleName)))
					{
						T.setLandOwner(femaleName);
						CMClass.DBEngine().DBUpdateRoom(R);
					}
					for(int i=0;i<R.numInhabitants();i++)
					{
						MOB M2=R.fetchInhabitant(i);
						if((M2!=null)&&(M2 instanceof Banker))
						{
							Banker B=(Banker)M2;
							Vector V=B.getDepositInventory(maleName);
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
			}
		}
		else
			beneficialWordsFizzle(mob,target,"<S-NAME> clear(s) <S-HIS-HER> throat.");

		return success;
	}
}
