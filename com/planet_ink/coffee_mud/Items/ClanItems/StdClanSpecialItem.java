package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class StdClanSpecialItem extends StdClanItem
{
	public String ID(){	return "StdClanSpecialItem";}
	public Environmental newInstance(){ return new StdClanSpecialItem();}
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
	
	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if((ciType()==ClanItem.CI_SPECIALSCALES)
		&&(owner() instanceof MOB)
		&&(clanID().length()>0)
		&&(((MOB)owner()).isMonster())
		&&((((MOB)owner()).getClanID().equals(clanID()))
		&&(Sense.aliveAwakeMobile((MOB)owner(),true))
		&&(!Sense.isAnimalIntelligence((MOB)owner()))))
		{
			if(B==null) B=CMClass.getBehavior("GoodExecutioner");
			if(B!=null) B.executeMsg(owner(),msg);
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Host.TICK_CLANITEM)
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
							ExternalPlay.get(mob,null,I,false);
					}
					break;
				}
			case ClanItem.CI_SPECIALSCALES:
				{
					break;
				}
			}
		}
		return true;
	}
}