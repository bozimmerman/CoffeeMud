package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class StdClanSpecialItem extends StdClanItem
{
	public String ID(){	return "StdClanSpecialItem";}
	private Behavior B=null;

	public StdClanSpecialItem()
	{
		super();

		setName("a clan item");
		baseEnvStats.setWeight(1);
		setDisplayText("an item belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		setCIType(ClanItem.CI_SPECIALOTHER);
		material=EnvResource.RESOURCE_PINE;
		recoverEnvStats();
	}

	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if(((ciType()==ClanItem.CI_SPECIALSCALES)||(ciType()==ClanItem.CI_SPECIALTAXER))
		&&(owner() instanceof MOB)
		&&(clanID().length()>0)
		&&(((MOB)owner()).isMonster())
		&&((((MOB)owner()).getClanID().equals(clanID()))
		&&(Sense.aliveAwakeMobile((MOB)owner(),true))
		&&(!Sense.isAnimalIntelligence((MOB)owner())))
		&&(B!=null))
			B.executeMsg(owner(),msg);
	}
	public boolean okMessage(Environmental affecting, CMMsg msg)
	{
		if(!super.okMessage(affecting,msg))
			return false;
		if((ciType()==ClanItem.CI_SPECIALTAXER)
		&&(B!=null)
		&&(owner() instanceof MOB))
			return B.okMessage((MOB)owner(),msg);
		else
			return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_CLANITEM)
		&&(owner() instanceof MOB)
		&&(clanID().length()>0)
		&&(((MOB)owner()).isMonster())
		&&((((MOB)owner()).getClanID().equals(clanID()))
		&&(Sense.aliveAwakeMobile((MOB)owner(),true))
		&&(!Sense.isAnimalIntelligence((MOB)owner()))))
		{
			switch(ciType())
			{
			case ClanItem.CI_SPECIALSCAVENGER:
				{
					MOB mob=(MOB)owner();
					Room R=((MOB)owner()).location();
					if(R!=null)
					{
						Item I=R.fetchItem(Dice.roll(1,R.numItems(),-1));
						if((I!=null)&&(I.container()==null))
							CommonMsgs.get(mob,null,I,false);
					}
					break;
				}
			case ClanItem.CI_SPECIALSCALES:
				{
					if((B==null)||(!B.ID().equals("GoodExecutioner")))
						B=CMClass.getBehavior("GoodExecutioner");
					break;
				}
			case ClanItem.CI_SPECIALTAXER:
				{
					if((B==null)||(!B.ID().equals("TaxCollector")))
						B=CMClass.getBehavior("TaxCollector");
					if(B!=null) B.tick((MOB)owner(),MudHost.TICK_MOB);
					break;
				}
			}
		}
		return true;
	}
}