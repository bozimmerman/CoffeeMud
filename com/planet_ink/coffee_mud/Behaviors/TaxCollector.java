package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class TaxCollector extends StdBehavior
{
	public String ID(){return "TaxCollector";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	private DVector demanded=null;
	private DVector paid=null;
	private long waitTime=1000*60*2;
	private long graceTime=1000*60*60;

	public Behavior newInstance()
	{
		return new TaxCollector();
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		demanded=null;
		paid=null;
		waitTime=Util.getParmInt(newParms,"WAIT",1000*60*2);
		graceTime=Util.getParmInt(newParms,"GRACE",1000*60*60);
	}

	public void executeMsg(Environmental oking, CMMsg msg)
	{
		super.executeMsg(oking,msg);
		if((oking!=null)||(oking instanceof MOB))
		{
			MOB mob=(MOB)oking;
			if(msg.amITarget(mob)
			&&(msg.targetMinor()==CMMsg.TYP_GIVE)
			&&(msg.tool() instanceof Coins)
			&&((demanded!=null)&&(paid!=null))
			&&(demanded.contains(msg.source())))
			{
				int coins=((Coins)msg.tool()).numberOfCoins();
				int demanDex=demanded.indexOf(msg.source());
				if(demanDex>=0)
				{
					demanded.removeElementAt(demanDex);
					paid.addElement(msg.source(),new Long(System.currentTimeMillis()));
				}
			}
		}
	}

	public boolean okMessage(Environmental oking, CMMsg msg)
	{
		if((oking==null)||(!(oking instanceof MOB)))
		   return super.okMessage(oking,msg);
		MOB mob=(MOB)oking;
		if(msg.amITarget(mob)
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool() instanceof Coins)
		&&((demanded!=null)&&(paid!=null))
		&&(demanded.contains(msg.source())))
		{
			int money=MoneyUtils.totalMoney(msg.source());
			int coins=((Coins)msg.tool()).numberOfCoins();
			int owed=money/10;
			if(owed<1) owed=1;
			if(coins<owed)
			{
				CommonMsgs.say(mob,msg.source(),"That's not enough.  You owe "+owed+".  Try again.",false,false);
				return false;
			}
		}
		return true;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if((tickID!=MudHost.TICK_MOB)||(!(ticking instanceof MOB)))
			return true;

		MOB mob=(MOB)ticking;
		if(demanded==null) demanded=new DVector(2);
		if(paid==null) paid=new DVector(2);

		for(int i=paid.size()-1;i>=0;i--)
		{
			Long L=(Long)paid.elementAt(i,2);
			if((System.currentTimeMillis()-L.longValue())>graceTime)
				paid.removeElementAt(i);
		}

		Room R=mob.location();
		if((R!=null)
		&&(!mob.isInCombat())
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(R.numInhabitants()>1))
		{
			MOB M=R.fetchInhabitant(Dice.roll(1,R.numInhabitants(),-1));
			if((M!=null)
			&&((mob.getClanID().length()==0)
			   ||(M.getClanID().length()==0)
			   ||(!M.getClanID().equals(mob.getClanID()))
			&&(!Sense.isAnimalIntelligence(M))
			&&(Sense.canBeSeenBy(M,mob))))
			{
				int money=MoneyUtils.totalMoney(M);
				if(money>0)
				{
					int demandDex=demanded.indexOf(M);
					if((demandDex>=0)
					&&((System.currentTimeMillis()-((Long)demanded.elementAt(demandDex,2)).longValue())>waitTime))
					{
						CommonMsgs.say(mob,M,"You know what they say about death and taxes, so if you won't pay ... DIE!!!!",false,false);
						MUDFight.postAttack(mob,M,mob.fetchWieldedItem());
						demanded.removeElementAt(demandDex);
					}
					if((!paid.contains(M))&&(demandDex<0))
					{
						int amount=money/10;
						if(amount<1) amount=1;
						CommonMsgs.say(mob,M,"You owe "+amount+" gold in taxes.  You must pay me immediately or face the consequences.",false,false);
						demanded.addElement(M,new Long(System.currentTimeMillis()));
						if(M.isMonster())
						{
							Vector V=new Vector();
							V.addElement("GIVE");
							V.addElement(""+amount);
							V.addElement(mob.name());
							M.doCommand(V);
						}

					}
				}
			}

			Item I=R.fetchItem(Dice.roll(1,R.numItems(),-1));
			if((I!=null)&&(I instanceof Coins))
				CommonMsgs.get(mob,I.container(),I,false);
		}
		return true;
	}
}