package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.io.*;
import java.util.*;


public class Conquerable extends Arrest
{
	public String ID(){return "Conquerable";}
	public Behavior newInstance(){ return new Conquerable();}
	protected boolean defaultModifiableNames(){return false;}
	public String getParms(){return "custom";}

	private String holdingClan="";
	private Vector clanItems=new Vector();
	private int clanControlPoints=-1;
	private int attitudePoints=-1;
	private int totalControlPoints=-1;


	private final static int ITEMTICKFREQ=10;
	private int itemtickdown=10;

	// here are the codes for interacting with this behavior
	// see Law.java for info
	public boolean modifyBehavior(Environmental hostObj,
								  MOB mob,
								  Object O)
	{
		if((mob!=null)
		&&(mob.location()!=null)
		&&(hostObj!=null)
		&&(hostObj instanceof Area))
		{
			Law laws=getLaws((Area)hostObj);
			Integer I=null;
			Vector V=null;
			if(O instanceof Integer)
				I=(Integer)O;
			else
			if(O instanceof Vector)
			{
				V=(Vector)O;
				if(V.size()==0)
					return false;
				I=(Integer)V.firstElement();
			}
			else
				return false;
			switch(I.intValue())
			{
			default:
				break;
			}
		}
		return super.modifyBehavior(hostObj,mob,O);
	}

	public boolean isAnyKindOfOfficer(Law laws, MOB M)
	{
		if((M!=null)
		&&(M.location()!=null)
		&&((!M.isMonster())||Sense.isMobile(M)))
			for(int i=0;i<M.inventorySize();i++)
			{
				Item I=M.fetchInventory(i);
				if((I!=null)
				&&(I instanceof ClanItem)
				&&(!I.amWearingAt(Item.INVENTORY))
				&&(((ClanItem)I).ciType()==ClanItem.CI_BANNER))
					return true;
			}
		return false;
	}

	public boolean isTheJudge(Law laws, MOB M)
	{
		if(M!=null)
			for(int i=0;i<M.inventorySize();i++)
			{
				Item I=M.fetchInventory(i);
				if((I!=null)
				&&(I instanceof ClanItem)
				&&(!I.amWearingAt(Item.INVENTORY))
				&&(((ClanItem)I).ciType()==ClanItem.CI_GAVEL))
					return true;
			}
		return false;
	}

	private void endClanRule()
	{
		holdingClan="";
		for(int v=0;v<clanItems.size();v++)
		{
			Item I=(Item)clanItems.elementAt(v);
			if(I.owner() instanceof MOB)
			{
				MOB M=(MOB)I.owner();
				if(M.location()!=null)
				{
					M.delInventory(I);
					M.location().addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
				}
			}
		}
		clanItems.clear();
		//**TODO clean up any clan rule
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!ExternalPlay.getSystemStarted())
			return true;
		
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID!=Host.TICK_AREA) return true;
		if(!(ticking instanceof Area)) return true;

		for(int i=clanItems.size()-1;i>=0;i--)
		{
			Item I=(Item)clanItems.elementAt(i);
			if(!I.tick(this,Host.TICK_CLANITEM))
				clanItems.remove(I);
		}

		return true;
	}

	protected boolean isAnUltimateAuthorityHere(MOB M, Law laws)
	{
		if((holdingClan.length()==0)
		||(!ExternalPlay.getSystemStarted()))
			return false;
		Clan C=Clans.getClan(holdingClan);
		if(C==null){ endClanRule(); return false;}
		return C.allowedToDoThis(M,Clan.FUNC_CLANCANORDERCONQUERED)==1;
	}

	protected boolean theLawIsEnabled(Law laws)
	{
		if((holdingClan.length()==0)
		||(!ExternalPlay.getSystemStarted()))
			return false;
		Clan C=Clans.getClan(holdingClan);
		if(C==null){ endClanRule(); return false;}
		return true;
	}
}
