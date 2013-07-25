package com.planet_ink.coffee_mud.Abilities.Thief;
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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/*
   Copyright 2000-2013 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Thief_Embezzle extends ThiefSkill
{
	public String ID() { return "Thief_Embezzle"; }
	public String name(){ return "Embezzle";}
	public String displayText(){return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"EMBEZZLE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode() {   return Ability.ACODE_SKILL|Ability.DOMAIN_CRIMINAL; }
	protected boolean disregardsArmorCheck(MOB mob){return true;}
	public List<MOB> mobs=new Vector<MOB>();
	private LinkedList<Pair<MOB,Integer>> lastOnes=new LinkedList<Pair<MOB,Integer>>();

	protected int timesPicked(MOB target)
	{
		int times=0;
		for(Iterator<Pair<MOB,Integer>> p=lastOnes.iterator();p.hasNext();)
		{
			Pair<MOB,Integer> P=p.next();
			MOB M=P.first;
			Integer I=P.second;
			if(M==target)
			{
				times=I.intValue();
				p.remove();
				break;
			}
		}
		if(lastOnes.size()>=50)
			lastOnes.removeFirst();
		lastOnes.add(new Pair<MOB,Integer>(target,Integer.valueOf(times+1)));
		return times+1;
	}

	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((msg.amITarget(affected))
		   &&(mobs.contains(msg.source())))
		{
			if((msg.targetMinor()==CMMsg.TYP_BUY)
			   ||(msg.targetMinor()==CMMsg.TYP_BID)
			   ||(msg.targetMinor()==CMMsg.TYP_SELL)
			   ||(msg.targetMinor()==CMMsg.TYP_LIST)
			   ||(msg.targetMinor()==CMMsg.TYP_VALUE)
			   ||(msg.targetMinor()==CMMsg.TYP_VIEW))
			{
				msg.source().tell(affected.name()+" looks unwilling to do business with you.");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()<1)&&(givenTarget==null))
		{
			mob.tell("Embezzle money from whose accounts?");
			return false;
		}
		MOB target=null;
		if((givenTarget!=null)&&(givenTarget instanceof MOB)) 
			target=(MOB)givenTarget;
		else
			target=mob.location().fetchInhabitant(CMParms.combine(commands,0));
		if((target==null)||(target.amDead())||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+CMParms.combine(commands,1)+"' here.");
			return false;
		}
		if(!(target instanceof Banker))
		{
			mob.tell("You can't embezzle from "+target.name(mob)+"'s accounts.");
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell("You are too busy to embezzle.");
			return false;
		}
		Banker bank=(Banker)target;
		Ability A=target.fetchEffect(ID());
		if(A!=null)
		{
			mob.tell(target.name(mob)+" is watching "+target.charStats().hisher()+" books too closely.");
			return false;
		}
		int levelDiff=target.phyStats().level()-(mob.phyStats().level()+(2*super.getXLEVELLevel(mob)));

		if(!target.mayIFight(mob))
		{
			mob.tell("You cannot embezzle from "+target.charStats().himher()+".");
			return false;
		}

		Item myCoins=null;
		String myAcct=mob.Name();
		if(bank.isSold(ShopKeeper.DEAL_CLANBANKER))
		{
			Pair<Clan,Integer> clanPair=CMLib.clans().findPrivilegedClan(mob, Clan.Function.WITHDRAW);
			if(clanPair == null) clanPair=CMLib.clans().findPrivilegedClan(mob, Clan.Function.DEPOSIT_LIST);
			if(clanPair == null) clanPair=CMLib.clans().findPrivilegedClan(mob, Clan.Function.DEPOSIT);
			if(clanPair!=null)
				myAcct=clanPair.first.clanID();
		}
		myCoins=bank.findDepositInventory(myAcct,"1");
		if((myCoins==null)||(!(myCoins instanceof Coins)))
		{
			mob.tell("You don't have your own account with "+target.name(mob)+".");
			return false;
		}
		List<String> accounts=bank.getAccountNames();
		String victim="";
		int tries=0;
		Coins hisCoins=null;
		double hisAmount=0;
		while((hisCoins==null)&&((++tries)<10))
		{
			String possVic=accounts.get(CMLib.dice().roll(1,accounts.size(),-1));
			Item C=bank.findDepositInventory(possVic,"1");
			if((C!=null)
			&&(C instanceof Coins)
			&&((((Coins)C).getTotalValue()/50.0)>0.0)
			&&(!mob.Name().equals(possVic)))
			{
				hisCoins=(Coins)C;
				victim=possVic;
				hisAmount=hisCoins.getTotalValue()/50.0;
			}
		}
		int classLevel=CMLib.ableMapper().qualifyingClassLevel(mob,this)+(2*getXLEVELLevel(mob));
		if((classLevel>0)
		&&(Math.round(hisAmount)>(1000*(classLevel)+(2*getXLEVELLevel(mob)))))
		   hisAmount=1000l*(classLevel+(2l*getXLEVELLevel(mob)));

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,(-(levelDiff+(timesPicked(mob)*50))),auto);
		if((success)&&(hisAmount>0)&&(hisCoins!=null))
		{
			String str="<S-NAME> embezzle(s) "+CMLib.beanCounter().nameCurrencyShort(target,hisAmount)+" from the "+victim+" account maintained by <T-NAME>.";
			CMMsg msg=CMClass.getMsg(mob,target,this,(auto?CMMsg.MASK_ALWAYS:0)|CMMsg.MSG_THIEF_ACT,str,null,str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,(int)(((CMProps.getMillisPerMudHour()*mob.location().getArea().getTimeObj().getHoursInDay()*mob.location().getArea().getTimeObj().getDaysInMonth())/CMProps.getTickMillis())));
				bank.delDepositInventory(victim,hisCoins);
				hisCoins=CMLib.beanCounter().makeBestCurrency(target,hisCoins.getTotalValue()-(hisAmount/3.0));
				if(hisCoins.getNumberOfCoins()>0)
					bank.addDepositInventory(victim,hisCoins);
				bank.delDepositInventory(myAcct,myCoins);
				myCoins=CMLib.beanCounter().makeBestCurrency(mob,((Coins)myCoins).getTotalValue()+hisAmount);
				if(((Coins)myCoins).getNumberOfCoins()>0)
					bank.addDepositInventory(myAcct,myCoins);
			}
		}
		else
			maliciousFizzle(mob,target,"<T-NAME> catch(es) <S-NAME> trying to embezzle money!");
		return success;
	}

}
