package com.planet_ink.coffee_mud.Items.ClanItems;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class StdClanPamphlet extends StdClanItem
{
	public String ID(){	return "StdClanPamphlet";}
	private int tradeTime=-1;

	public StdClanPamphlet()
	{
		super();

		setName("a clan pamphlet");
		baseEnvStats.setWeight(1);
		setDisplayText("a pamphlet belonging to a clan is here.");
		setDescription("");
		secretIdentity="";
		baseGoldValue=1;
		setCIType(ClanItem.CI_PROPAGANDA);
		material=EnvResource.RESOURCE_PAPER;
		recoverEnvStats();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_CLANITEM)
		&&(owner() instanceof MOB)
		&&(clanID().length()>0)
		&&(((MOB)owner()).isMonster())
		&&(!Sense.isAnimalIntelligence((MOB)owner()))
		&&(((MOB)owner()).getStartRoom()!=null)
		&&(((MOB)owner()).location()!=null)
		&&(((MOB)owner()).getStartRoom().getArea()==((MOB)owner()).location().getArea()))
		{
			String rulingClan=null;
			Room R=((MOB)owner()).location();
			if((!((MOB)owner()).getClanID().equals(clanID()))
			||(((--tradeTime)<=0)))
			{
				Area A=null;
				Behavior B=null;
				Vector V=null;
				if(R!=null)	A=R.getArea();
				if(A!=null) V=Sense.flaggedBehaviors(A,Behavior.FLAG_LEGALBEHAVIOR);
				if((V!=null)&&(V.size()>0)) B=(Behavior)V.firstElement();
				if(B!=null)
				{
					V.clear();
					V.addElement(new Integer(Law.MOD_RULINGCLAN));
					rulingClan="";
					if((B.modifyBehavior(A,(MOB)owner(),V))
					&&(V.size()>0)
					&&(V.firstElement() instanceof String))
						rulingClan=(String)V.firstElement();
				}
			}
			if(((!((MOB)owner()).getClanID().equals(clanID()))
			&&(rulingClan!=null))
			&&(rulingClan.length()>0))
				((MOB)owner()).setClanID(clanID());
			if(tradeTime<=0)
			{
				MOB mob=(MOB)owner();
				tradeTime=CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY);
				if((mob.getClanID().equals(clanID()))
				&&(rulingClan!=null)
				&&(rulingClan.length()>0)
				&&(Sense.canSpeak(mob))
				&&(Sense.aliveAwakeMobile(mob,true))
				&&(R!=null))
				{
					MOB M=R.fetchInhabitant(Dice.roll(1,R.numInhabitants(),-1));
					if((M!=null)
					&&(M!=mob)
					&&(M.isMonster())
					&&(M.getClanID().equals(rulingClan))
					&&(!Sense.isAnimalIntelligence(M))
					&&(Sense.canBeSeenBy(M,mob))
					&&(Sense.canBeHeardBy(M,mob)))
					{
						CommonMsgs.say(mob,M,"Hey, take a look at this.",false,false);
						ClanItem I=(ClanItem)copyOf();
						mob.addInventory((Item)I);
						FullMsg newMsg=new FullMsg(mob,M,I,CMMsg.MSG_GIVE,"<S-NAME> give(s) <O-NAME> to <T-NAMESELF>.");
						if(mob.location().okMessage(mob,newMsg))
							mob.location().send(mob,newMsg);
						if(!M.isMine(I)) ((Item)I).destroy();
					}
				}
			}
		}
		return true;
	}
}