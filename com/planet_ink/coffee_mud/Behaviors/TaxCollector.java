package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class TaxCollector extends StdBehavior
{
	public String ID(){return "TaxCollector";}
	protected int canImproveCode(){return Behavior.CAN_MOBS;}
	private DVector demanded=null;
	private DVector paid=null;
	private long waitTime=1000*60*2;
	private long graceTime=1000*60*60;
	private int lastMonthChecked=-1;
	private Vector taxableProperties=new Vector();
	private HashSet peopleWhoOwe=new HashSet();
	private Room treasuryR=null;
	private Item treasuryItem=null;

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
				int demanDex=demanded.indexOf(msg.source());
				int paidAmount=((Coins)msg.tool()).numberOfCoins();
				if(demanDex>=0)
				{
					demanded.removeElementAt(demanDex);
					paid.addElement(msg.source(),new Long(System.currentTimeMillis()));
				}
				
				int numProperties=0;
				for(int i=0;i<taxableProperties.size();i++)
				{
				    LandTitle T=(LandTitle)taxableProperties.elementAt(i);
				    if((T.landOwner().equals(msg.source().Name())))
				    {
				        numProperties++;
				        if((T.backTaxes()>0)&&(paidAmount>=0))
				        {
				            if(paidAmount>=T.backTaxes())
				            {
				                paidAmount-=T.backTaxes();
				                T.setBackTaxes(0);
				                T.updateTitle();
				            }
				            else
				            {
				                paidAmount=0;
				                T.setBackTaxes(T.backTaxes()-paidAmount);
				                T.updateTitle();
				                break;
				            }
				        }
				    }
				}
				
				if((paidAmount>0)&&(numProperties>0))
				for(int i=0;i<taxableProperties.size();i++)
				{
				    LandTitle T=(LandTitle)taxableProperties.elementAt(i);
				    if(((T.landOwner().equals(msg.source().Name())))
				    &&(paidAmount>0))
				    {
			            T.setBackTaxes(T.backTaxes()-(paidAmount/numProperties));
			            T.updateTitle();
			            paidAmount-=(paidAmount/numProperties);
				    }
				}
				
	            if((treasuryR!=null)&&(paidAmount>0))
	            {
    				Coins COIN=(Coins)CMClass.getStdItem("StdCoins");
    				COIN.setNumberOfCoins(paidAmount);
    				COIN.setContainer(treasuryItem);
    				treasuryR.addItem(COIN);
    				COIN.putCoinsBack();
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
	    if(!CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MUDSTARTED))
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
		if((R!=null)&&(lastMonthChecked!=R.getArea().getTimeObj().getMonth()))
		{
		    lastMonthChecked=R.getArea().getTimeObj().getMonth();
		    Behavior B=CoffeeUtensils.getLegalBehavior(R.getArea());
		    if(B!=null)
		    {
				Vector VB=new Vector();
				Area A2=CoffeeUtensils.getLegalObject(R.getArea());
				VB.addElement(new Integer(Law.MOD_LEGALINFO));
				B.modifyBehavior(A2,mob,VB);
				Law theLaw=(Law)VB.firstElement();
				
				String taxs=(String)theLaw.taxLaws().get("PROPERTYTAX");
				taxableProperties.clear();
				peopleWhoOwe.clear();
				Room R2=null;
				LandTitle T=null;
				if((taxs!=null)&&(taxs.length()>0)&&(Util.s_double(taxs)>0))
				for(Enumeration e=A2.getMetroMap();e.hasMoreElements();)
				{
				    R2=(Room)e.nextElement();
				    T=CoffeeUtensils.getLandTitle(R2);
				    if((T!=null)
				    &&(T.landOwner().length()>0)
				    &&(!taxableProperties.contains(T)))
				    {
				        taxableProperties.addElement(T);
				        if((!peopleWhoOwe.contains(T.landOwner()))
				        &&(T.backTaxes()>0))
				            peopleWhoOwe.add(T.landOwner());
				    }
				}
				
                String tres=(String)theLaw.taxLaws().get("TREASURY");
                if((tres!=null)&&(tres.length()>0))
                {
        			Vector V=Util.parseSemicolons(tres,false);
        			if(V.size()>0)
        			{
        				String room=(String)V.firstElement();
        				String item="";
        				if(V.size()>1) item=Util.combine(V,1);
        				if(!room.equalsIgnoreCase("*"))
        				{
        					treasuryR=CMMap.getRoom(room);
        					if(treasuryR!=null)
        					    treasuryItem=treasuryR.fetchAnyItem(item);
        				}
        				else
        				if(item.length()>0)
        				for(Enumeration e=A2.getMetroMap();e.hasMoreElements();)
        				{
        				    R2=(Room)e.nextElement();
        				    if(R2.fetchAnyItem(item) instanceof Container)
        				    { 
        				        treasuryItem=R2.fetchAnyItem(item);
        				        treasuryR=R2; 
        				        break;
    				        }
        				}
        				if((room.length()>0)&&(treasuryR==null))
        				    treasuryR=A2.getRandomMetroRoom();
        			}
                }
		    }
		}
		
		if((R!=null)
		&&(!mob.isInCombat())
		&&(Sense.aliveAwakeMobile(mob,true))
		&&(R.numInhabitants()>1))
		{
			MOB M=R.fetchInhabitant(Dice.roll(1,R.numInhabitants(),-1));
			if((M!=null)
			&&(M!=mob)
			&&((mob.getClanID().length()==0)
			   ||(M.getClanID().length()==0)
			   ||(!M.getClanID().equals(mob.getClanID())))
			&&(!Sense.isAnimalIntelligence(M))
			&&(Sense.canBeSeenBy(M,mob)))
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
					    String plus="";
					    if(peopleWhoOwe.contains(M.Name()))
					    {
					        int backTaxes=0;
					        for(int t=0;t<taxableProperties.size();t++)
					        {
					            LandTitle T=(LandTitle)taxableProperties.elementAt(t);
					            if((T.landOwner().equals(M.Name()))
					            &&(T.backTaxes()>0))
					                backTaxes+=T.backTaxes();
					        }
					        if(backTaxes>0)
					            plus=" plus "+backTaxes+" in back property taxes";
					    }
						int amount=money/10;
						if(amount<1) amount=1;
						CommonMsgs.say(mob,M,"You owe "+amount+" gold in citizen taxes"+plus+".  You must pay me immediately or face the consequences.",false,false);
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
