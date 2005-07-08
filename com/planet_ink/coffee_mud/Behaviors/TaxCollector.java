package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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
	
	public double[] totalMoneyOwed(MOB collector,MOB M)
	{
	    double[] owed=new double[3];
	    if((peopleWhoOwe.contains(M.Name()))
        ||((M.getClanID().length()>0)&&(peopleWhoOwe.contains(M.getClanID()))))
	    {
	        for(int t=0;t<taxableProperties.size();t++)
	        {
	            LandTitle T=(LandTitle)taxableProperties.elementAt(t);
			    if((T.landOwner().equals(M.Name())
			            ||((M.getClanID().length()>0)&&T.landOwner().equals(M.getClanID())))
	            &&(T.backTaxes()>0))
	                owed[2]+=new Integer(T.backTaxes()).doubleValue();
	        }
	    }
        Behavior B=CoffeeUtensils.getLegalBehavior(M.location());
        if(B!=null)
        {
            Area A2=CoffeeUtensils.getLegalObject(M.location());
            if((A2!=null)
            &&(!B.modifyBehavior(A2,M,new Integer(Law.MOD_ISOFFICER)))
            &&(!B.modifyBehavior(A2,M,new Integer(Law.MOD_ISJUDGE))))
            {
                Vector VB=new Vector();
                VB.addElement(new Integer(Law.MOD_LEGALINFO));
                B.modifyBehavior(A2,M,VB);
                Law theLaw=(Law)VB.firstElement();
                if(theLaw!=null)
                {
                    double cittax=Util.s_double((String)theLaw.taxLaws().get("CITTAX"));
                    if(cittax>0.0)
                        owed[1]=Util.mul(BeanCounter.getTotalAbsoluteShopKeepersValue(M,collector),Util.div(cittax,100.0));
                }
            }
        }
		else
			owed[1]=Util.div(BeanCounter.getTotalAbsoluteShopKeepersValue(M,collector),10.0);
		owed[0]=owed[1]+owed[2];
		return owed;
	}

	public void executeMsg(Environmental oking, CMMsg msg)
	{
		super.executeMsg(oking,msg);
		if((oking!=null)||(oking instanceof MOB))
		{
			MOB mob=(MOB)oking;
			if(msg.amITarget(mob)
			&&(msg.targetMinor()==CMMsg.TYP_GIVE)
			&&(msg.tool() instanceof Coins))
			{
				double paidAmount=((Coins)msg.tool()).getTotalValue();
				if(paidAmount<=0.0) return;
			    double[] owed=totalMoneyOwed(mob,msg.source());
	            if(treasuryR!=null)
	            {
	                Coins COIN=BeanCounter.makeBestCurrency(BeanCounter.getCurrency(mob),paidAmount,treasuryR,treasuryItem);
    				if(COIN!=null) COIN.putCoinsBack();
	            }
	            
				if((demanded!=null)&&(demanded.contains(msg.source())))
				{
					int demanDex=demanded.indexOf(msg.source());
					if(demanDex>=0)
					{
					    paidAmount-=owed[1];
						demanded.removeElementAt(demanDex);
					}
				}
				if(paid.contains(msg.source())) paid.removeElement(msg.source());
				paid.addElement(msg.source(),new Long(System.currentTimeMillis()));
				
				int numProperties=0;
				int numBackTaxesUnpaid=0;
				boolean paidBackTaxes=false;
				for(int i=0;i<taxableProperties.size();i++)
				{
				    LandTitle T=(LandTitle)taxableProperties.elementAt(i);
				    if(T.landOwner().equals(msg.source().Name())
			            ||((msg.source().getClanID().length()>0)&&T.landOwner().equals(msg.source().getClanID())))
				    {
				        numProperties++;
				        if(T.backTaxes()>0)
				        {
				            numBackTaxesUnpaid++;
					        if(paidAmount>=0)
					        {
					            if(paidAmount>=T.backTaxes())
					            {
					                paidAmount-=T.backTaxes();
					                T.setBackTaxes(0);
					                T.updateTitle();
						            numBackTaxesUnpaid--;
						            paidBackTaxes=true;
					            }
					            else
					            {
					                paidAmount=0;
					                T.setBackTaxes(T.backTaxes()-(int)Math.round(paidAmount));
					                T.updateTitle();
					                break;
					            }
					        }
				        }
				    }
				}
				if((paidBackTaxes)&&(numBackTaxesUnpaid==0)&&(mob.location()!=null))
				{
				    Behavior B=CoffeeUtensils.getLegalBehavior(mob.location().getArea());
				    if((B!=null)&&(!msg.source().isMonster()))
				    {
						Vector VB=new Vector();
						Area A2=CoffeeUtensils.getLegalObject(mob.location().getArea());
						VB.addElement(new Integer(Law.MOD_CRIMEAQUIT));
						VB.addElement("TAXEVASION");
						B.modifyBehavior(A2,msg.source(),VB);
				    }
				}
				
				if((paidAmount>0)&&(numProperties>0))
				for(int i=0;i<taxableProperties.size();i++)
				{
				    LandTitle T=(LandTitle)taxableProperties.elementAt(i);
				    if(((T.landOwner().equals(msg.source().Name())))
				    &&(paidAmount>0))
				    {
			            T.setBackTaxes(T.backTaxes()-(int)Math.round(Util.div(paidAmount,numProperties)));
			            T.updateTitle();
			            paidAmount-=Util.div(paidAmount,numProperties);
				    }
				}
				msg.addTrailerMsg(new FullMsg(mob,msg.source(),null,CMMsg.MSG_SPEAK,"<S-NAME> says 'Very good.  Your taxes are paid in full.' to <T-NAMESELF>."));
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
		&&(msg.tool() instanceof Coins))
		{
		    String currency=BeanCounter.getCurrency(mob);
		    double[] owe=totalMoneyOwed(mob,msg.source());
			double coins=((Coins)msg.tool()).getTotalValue();
			if((paid!=null)&&(paid.contains(msg.source())))
		        owe[0]-=owe[1];
			String owed=BeanCounter.nameCurrencyShort(currency,owe[0]);
            if((!((Coins)msg.tool()).getCurrency().equals(BeanCounter.getCurrency(mob))))
            {
                msg.source().tell(mob.name()+" refuses your money.");
                CommonMsgs.say(mob,msg.source(),"I don't accept that kind of currency.",false,false);
                return false;
            }
			if(coins<owe[0])
			{
			    msg.source().tell(mob.name()+" refuses your money.");
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
			Law theLaw=CoffeeUtensils.getTheLaw(R,mob);
			if(theLaw!=null)
			{
			    Area A2=CoffeeUtensils.getLegalObject(R);
				String taxs=(String)theLaw.taxLaws().get("PROPERTYTAX");
				LandTitle T=null;
				peopleWhoOwe.clear();
				if((taxs!=null)&&(taxs.length()>0)&&(Util.s_double(taxs)>0))
				{
				    taxableProperties=CoffeeUtensils.getAllUniqueTitles(A2.getMetroMap(),"*",false);
				    for(int v=0;v<taxableProperties.size();v++)
				    {
				        T=(LandTitle)taxableProperties.elementAt(v);
				        if((!peopleWhoOwe.contains(T.landOwner()))
				        &&(T.backTaxes()>0))
				            peopleWhoOwe.add(T.landOwner());
				    }
				}
				else
					taxableProperties.clear();
				
				Environmental[] Treas=theLaw.getTreasuryNSafe(A2);
				treasuryR=(Room)Treas[0];
				treasuryItem=(Item)Treas[1];
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
				int demandDex=demanded.indexOf(M);
				if((demandDex>=0)
				&&((System.currentTimeMillis()-((Long)demanded.elementAt(demandDex,2)).longValue())>waitTime))
				{
				    Behavior B=CoffeeUtensils.getLegalBehavior(R.getArea());
				    if(B!=null)
				    {
						Vector VB=new Vector();
						Area A2=CoffeeUtensils.getLegalObject(R.getArea());
						VB.addElement(new Integer(Law.MOD_CRIMEACCUSE));
						VB.addElement(mob);
						VB.addElement("TAXEVASION");
						B.modifyBehavior(A2,M,VB);
						CommonMsgs.say(mob,M,"Can't pay huh?  Well, you'll be hearing from the law -- THAT's for sure!",false,false);
				    }
				    else
				    {
						CommonMsgs.say(mob,M,"You know what they say about death and taxes, so if you won't pay ... DIE!!!!",false,false);
						MUDFight.postAttack(mob,M,mob.fetchWieldedItem());
						demanded.removeElementAt(demandDex);
				    }
				}
				if((!paid.contains(M))&&(demandDex<0))
				{
				    double[] owe=totalMoneyOwed(mob,M);
				    StringBuffer say=new StringBuffer("");
				    String currency=BeanCounter.getCurrency(mob);
				    double denomination=BeanCounter.getLowestDenomination(currency);
				    if(owe[1]>0)
				    	say.append("You owe "+BeanCounter.getDenominationName(currency,denomination,Math.round(Util.div(owe[1],denomination)))+" in local taxes. ");
				    if(owe[2]>0)
				    	say.append("You owe "+BeanCounter.getDenominationName(currency,denomination,Math.round(Util.div(owe[2],denomination)))+" in back property taxes");
				    if(say.length()>0)
				    {
						CommonMsgs.say(mob,M,say.toString()+".  You must pay me immediately or face the consequences.",false,false);
						demanded.addElement(M,new Long(System.currentTimeMillis()));
						if(M.isMonster())
						{
							Vector V=new Vector();
							V.addElement("GIVE");
							V.addElement(""+owe[0]);
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
