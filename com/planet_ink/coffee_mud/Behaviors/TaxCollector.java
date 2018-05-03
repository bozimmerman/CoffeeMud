package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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
   Copyright 2004-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "TaxCollector";
	}

	@Override
	protected int canImproveCode()
	{
		return Behavior.CAN_MOBS;
	}

	protected DVector			demanded			= null;
	protected DVector			paid				= null;
	protected long				waitTime			= 1000 * 60 * 2;
	protected long				graceTime			= 1000 * 60 * 60;
	protected int				lastMonthChecked	= -1;
	protected List<LandTitle>	taxableProperties	= new Vector<LandTitle>();
	protected Set<String>		peopleWhoOwe		= new HashSet<String>();
	protected String			treasuryRoomID		= null;
	protected Container			treasuryContainer	= null;

	public final static int		OWE_TOTAL			= 0;
	public final static int		OWE_CITIZENTAX		= 1;
	public final static int		OWE_BACKTAXES		= 2;
	public final static int		OWE_FINES			= 3;

	@Override
	public String accountForYourself()
	{
		return "tax collecting";
	}

	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		demanded=null;
		paid=null;
		waitTime=CMParms.getParmInt(newParms,"WAIT",1000*60*2);
		graceTime=CMParms.getParmInt(newParms,"GRACE",1000*60*60);
	}

	protected boolean belongsToAnOweingClan(MOB M)
	{
		for(final String clanID : peopleWhoOwe)
		{
			if(M.getClanRole(clanID)!=null)
				return true;
		}
		return false;
	}

	public double[] totalMoneyOwed(MOB collector,MOB M)
	{
		final double[] owed=new double[4];
		if((peopleWhoOwe.contains(M.Name()))
		||(belongsToAnOweingClan(M)))
		{
			for(int t=0;t<taxableProperties.size();t++)
			{
				final LandTitle T=taxableProperties.get(t);
				if((T.getOwnerName().equals(M.Name())
					||(M.getClanRole(T.getOwnerName())!=null))
				&&(T.backTaxes()>0))
					owed[OWE_BACKTAXES]+=T.backTaxes();
			}
		}
		final LegalBehavior B=CMLib.law().getLegalBehavior(M.location());
		if(B!=null)
		{
			final Area A2=CMLib.law().getLegalObject(M.location());
			if(A2!=null)
				owed[OWE_FINES]=B.finesOwed(M);
			if((A2!=null)
			&&(!B.isAnyOfficer(A2,M))
			&&(!B.isJudge(A2,M)))
			{
				final Law theLaw=B.legalInfo(A2);
				if(theLaw!=null)
				{
					final double cittax=CMath.s_double((String)theLaw.taxLaws().get("CITTAX"));
					if(cittax>0.0)
						owed[OWE_CITIZENTAX]=CMath.mul(CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(M,collector),CMath.div(cittax,100.0));
				}
			}
		}
		else
			owed[OWE_CITIZENTAX]=CMath.div(CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(M,collector),10.0);
		owed[OWE_TOTAL]=owed[OWE_CITIZENTAX]+owed[OWE_BACKTAXES]+owed[OWE_FINES];
		owed[OWE_TOTAL]=Math.round(owed[OWE_TOTAL]);
		return owed;
	}

	@Override
	public void executeMsg(Environmental oking, CMMsg msg)
	{
		super.executeMsg(oking,msg);
		if((oking!=null)&&(oking instanceof MOB))
		{
			final MOB mob=(MOB)oking;
			if(msg.amITarget(mob)
			&&(msg.targetMinor()==CMMsg.TYP_GIVE)
			&&(msg.tool() instanceof Coins))
			{
				double paidAmount=((Coins)msg.tool()).getTotalValue();
				if(paidAmount<=0.0)
					return;
				final double[] owed=totalMoneyOwed(mob,msg.source());
				if(treasuryRoomID!=null)
				{
					final Room treasuryR=CMLib.map().getRoom(treasuryRoomID);
					if(treasuryR!=null)
					{
						final Coins COIN=CMLib.beanCounter().makeBestCurrency(CMLib.beanCounter().getCurrency(mob),paidAmount,treasuryR,treasuryContainer);
						if(COIN!=null)
							COIN.putCoinsBack();
					}
				}

				if((demanded!=null)&&(demanded.contains(msg.source())))
				{
					final int demanDex=demanded.indexOf(msg.source());
					if(demanDex>=0)
					{
						paidAmount-=owed[OWE_CITIZENTAX];
						demanded.removeElementAt(demanDex);
					}
				}
				if(paid.contains(msg.source()))
					paid.removeElement(msg.source());
				paid.addElement(msg.source(),Long.valueOf(System.currentTimeMillis()));

				if(owed[OWE_FINES]>0)
				{
					final LegalBehavior B=CMLib.law().getLegalBehavior(msg.source().location());
					final Area A2=CMLib.law().getLegalObject(msg.source().location());
					if((B!=null)&&(A2!=null))
					{
						if(paidAmount>=owed[OWE_FINES])
						{
							paidAmount-=owed[OWE_FINES];
							B.modifyAssessedFines(0.0,msg.source());
						}
						else
						{
							owed[OWE_FINES]-=paidAmount;
							paidAmount=0;
							B.modifyAssessedFines(owed[OWE_FINES],msg.source());
						}
					}
				}

				int numProperties=0;
				int numBackTaxesUnpaid=0;
				boolean paidBackTaxes=false;
				for(int i=0;i<taxableProperties.size();i++)
				{
					final LandTitle T=taxableProperties.get(i);
					if(T.getOwnerName().equals(msg.source().Name())
						||(msg.source().getClanRole(T.getOwnerName())!=null))
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
					final LegalBehavior B=CMLib.law().getLegalBehavior(mob.location().getArea());
					if((B!=null)&&(!msg.source().isMonster()))
					{
						final Area A2=CMLib.law().getLegalObject(mob.location().getArea());
						B.aquit(A2,msg.source(),new String[]{"TAXEVASION"});
					}
				}

				if((paidAmount>0)&&(numProperties>0))
				for(int i=0;i<taxableProperties.size();i++)
				{
					final LandTitle T=taxableProperties.get(i);
					if(((T.getOwnerName().equals(msg.source().Name())))
					&&(paidAmount>0))
					{
						T.setBackTaxes(T.backTaxes()-(int)Math.round(CMath.div(paidAmount,numProperties)));
						T.updateTitle();
						paidAmount-=CMath.div(paidAmount,numProperties);
					}
				}
				msg.addTrailerMsg(CMClass.getMsg(mob,msg.source(),null,CMMsg.MSG_SPEAK,L("<S-NAME> says 'Very good.  Your taxes are paid in full.' to <T-NAMESELF>.")));
			}
		}
	}

	@Override
	public boolean okMessage(Environmental oking, CMMsg msg)
	{
		if((oking==null)||(!(oking instanceof MOB)))
			return super.okMessage(oking,msg);
		final MOB mob=(MOB)oking;
		if(msg.amITarget(mob)
		&&(msg.targetMinor()==CMMsg.TYP_GIVE)
		&&(msg.tool() instanceof Coins))
		{
			final String currency=CMLib.beanCounter().getCurrency(mob);
			final double[] owe=totalMoneyOwed(mob,msg.source());
			final double coins=((Coins)msg.tool()).getTotalValue();
			if((paid!=null)&&(paid.contains(msg.source())))
				owe[OWE_TOTAL]-=owe[OWE_CITIZENTAX];
			final String owed=CMLib.beanCounter().nameCurrencyShort(currency,owe[OWE_TOTAL]);
			if((!((Coins)msg.tool()).getCurrency().equals(CMLib.beanCounter().getCurrency(mob))))
			{
				msg.source().tell(L("@x1 refuses your money.",mob.name(msg.source())));
				CMLib.commands().postSay(mob,msg.source(),L("I don't accept that kind of currency."),false,false);
				return false;
			}
			if(coins<owe[OWE_TOTAL])
			{
				msg.source().tell(L("@x1 refuses your money.",mob.name(msg.source())));
				CMLib.commands().postSay(mob,msg.source(),L("That's not enough.  You owe @x1.  Try again.",owed),false,false);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);

		if((tickID!=Tickable.TICKID_MOB)||(!(ticking instanceof MOB)))
			return true;
		if(!CMProps.getBoolVar(CMProps.Bool.MUDSTARTED))
			return true;

		final MOB mob=(MOB)ticking;
		if(demanded==null)
			demanded=new DVector(2);
		if(paid==null)
			paid=new DVector(2);

		for(int i=paid.size()-1;i>=0;i--)
		{
			final Long L=(Long)paid.elementAt(i,2);
			if((System.currentTimeMillis()-L.longValue())>graceTime)
				paid.removeElementAt(i);
		}

		final Room R=mob.location();
		if((R!=null)&&(lastMonthChecked!=R.getArea().getTimeObj().getMonth()))
		{
			lastMonthChecked=R.getArea().getTimeObj().getMonth();
			final Law theLaw=CMLib.law().getTheLaw(R,mob);
			if(theLaw!=null)
			{
				final Area A2=CMLib.law().getLegalObject(R);
				final String taxs=(String)theLaw.taxLaws().get("PROPERTYTAX");
				LandTitle T=null;
				peopleWhoOwe.clear();
				if((taxs!=null)&&(taxs.length()>0)&&(CMath.s_double(taxs)>0))
				{
					taxableProperties=CMLib.law().getAllUniqueLandTitles(A2.getMetroMap(),"*",false);
					for(int v=0;v<taxableProperties.size();v++)
					{
						T=taxableProperties.get(v);
						if((!peopleWhoOwe.contains(T.getOwnerName()))
						&&(T.backTaxes()>0))
							peopleWhoOwe.add(T.getOwnerName());
					}
				}
				else
					taxableProperties.clear();

				final Law.TreasurySet treas=theLaw.getTreasuryNSafe(A2);
				treasuryRoomID=CMLib.map().getExtendedRoomID(treas.room);
				treasuryContainer=treas.container;
			}
		}

		if((R!=null)
		&&(!mob.isInCombat())
		&&(CMLib.flags().isAliveAwakeMobileUnbound(mob,true))
		&&(R.numInhabitants()>1))
		{
			final MOB M=R.fetchRandomInhabitant();
			if((M!=null)
			&&(M!=mob)
			&&(!CMLib.flags().isAnimalIntelligence(M))
			&&(!CMSecurity.isAllowed(M,R,CMSecurity.SecFlag.ORDER))
			&&(!CMSecurity.isAllowed(M,R,CMSecurity.SecFlag.CMDROOMS))
			&&(CMLib.clans().findCommonRivalrousClans(mob, M).size()==0)
			&&(CMLib.flags().canBeSeenBy(M,mob)))
			{
				final int demandDex=demanded.indexOf(M);
				if((demandDex>=0)
				&&((System.currentTimeMillis()-((Long)demanded.elementAt(demandDex,2)).longValue())>waitTime))
				{
					final LegalBehavior B=CMLib.law().getLegalBehavior(R.getArea());
					if(M.isMonster()
					&&(M.getStartRoom()!=null)
					&&(CMLib.law().getLandTitle(M.getStartRoom())==null))
						demanded.removeElementAt(demandDex);
					else
					if(B!=null)
					{
						B.accuse(CMLib.law().getLegalObject(R),M,mob,new String[]{"TAXEVASION"});
						CMLib.commands().postSay(mob,M,L("Can't pay huh?  Well, you'll be hearing from the law -- THAT's for sure!"),false,false);
					}
					else
					{
						CMLib.commands().postSay(mob,M,L("You know what they say about death and taxes, so if you won't pay ... DIE!!!!"),false,false);
						CMLib.combat().postAttack(mob,M,mob.fetchWieldedItem());
						demanded.removeElementAt(demandDex);
					}
				}
				if((!paid.contains(M))&&(demandDex<0))
				{
					final double[] owe=totalMoneyOwed(mob,M);
					final StringBuffer say=new StringBuffer("");
					final String currency=CMLib.beanCounter().getCurrency(mob);
					final double denomination=CMLib.beanCounter().getLowestDenomination(currency);
					if(owe[OWE_CITIZENTAX]>1.0)
						say.append("You owe "+CMLib.beanCounter().getDenominationName(currency,denomination,Math.round(CMath.div(owe[OWE_CITIZENTAX],denomination)))+" in local taxes. ");
					if(owe[OWE_BACKTAXES]>1.0)
						say.append("You owe "+CMLib.beanCounter().getDenominationName(currency,denomination,Math.round(CMath.div(owe[OWE_BACKTAXES],denomination)))+" in back property taxes");
					if(owe[OWE_FINES]>1.0)
						say.append("You owe "+CMLib.beanCounter().getDenominationName(currency,denomination,Math.round(CMath.div(owe[OWE_FINES],denomination)))+" in fines");
					if(say.length()>0)
					{
						CMLib.commands().postSay(mob,M,L("@x1.  You must pay me immediately or face the consequences.",say.toString()),false,false);
						demanded.addElement(M,Long.valueOf(System.currentTimeMillis()));
						if(M.isMonster())
						{
							final Vector<String> V=new Vector<String>();
							V.addElement("GIVE");
							V.addElement(""+Math.round(owe[OWE_TOTAL]));
							V.addElement(mob.name());
							M.doCommand(V,MUDCmdProcessor.METAFLAG_FORCED);
						}
					}
				}
			}

			final Item I=R.getRandomItem();
			if((I!=null)&&(I instanceof Coins))
				CMLib.commands().postGet(mob,I.container(),I,false);
		}
		return true;
	}
}
