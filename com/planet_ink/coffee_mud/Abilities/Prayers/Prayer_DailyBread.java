package com.planet_ink.coffee_mud.Abilities.Prayers;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer_DailyBread extends Prayer
{
	public String ID() { return "Prayer_DailyBread"; }
	public String name(){ return "Daily Bread";}
	public int quality(){ return OK_OTHERS;}
	public long flags(){return Ability.FLAG_UNHOLY;}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public Environmental newInstance(){	return new Prayer_DailyBread();}
	protected int overrideMana(){return 100;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff<0) levelDiff=0;
		boolean success=profficiencyCheck(mob,-(levelDiff*25),auto);
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
			CommonMsgs.get(target,BreadContainer,Bread,false);
		if(Bread==null)
		{
			ShopKeeper SK=CoffeeUtensils.getShopKeeper(target);
			if(SK!=null)
			{
				Vector inv=SK.getUniqueStoreInventory();
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
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),"^S<S-NAME> "+prayWord(mob)+" for <T-NAMESELF> to provide <S-HIS-HER> daily bread!^?");
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					msg=new FullMsg(target,mob,Bread,CMMsg.MSG_GIVE,"<S-NAME> gladly donate(s) <O-NAME> to <T-NAMESELF>.");
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