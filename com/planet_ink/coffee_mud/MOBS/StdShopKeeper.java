package com.planet_ink.coffee_mud.MOBS;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class StdShopKeeper extends StdMOB implements ShopKeeper
{
	public String ID(){return "StdShopKeeper";}
    protected CoffeeShop shop=((CoffeeShop)CMClass.getCommon("DefaultCoffeeShop")).build(this);
	protected long whatIsSoldMask=0;
	protected int invResetRate=0;
	protected int invResetTickDown=0;
	protected String budget="";
	protected long budgetRemaining=Long.MAX_VALUE/2;
	protected long budgetMax=Long.MAX_VALUE/2;
	protected int budgetTickDown=2;
	protected String devalueRate="";
    protected String[] pricingAdjustments=new String[0];

	public StdShopKeeper()
	{
		super();
		Username="a shopkeeper";
		setDescription("He\\`s pleased to be of assistance.");
		setDisplayText("A shopkeeper is waiting to serve you.");
		CMLib.factions().setAlignment(this,Faction.ALIGN_GOOD);
		setMoney(0);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.STAT_INTELLIGENCE,16);
		baseCharStats().setStat(CharStats.STAT_CHARISMA,25);

		baseEnvStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

    public boolean isSold(int mask){
    	if(mask==0) return whatIsSoldMask==0;
    	if((whatIsSoldMask&255)==mask)
	    	return true;
		return CMath.bset(whatIsSoldMask>>8, CMath.pow(2,mask-1));
    }
    public void addSoldType(int mask)
    {
    	if(mask==0)
    		whatIsSoldMask=0;
    	else
    	{
	    	if((whatIsSoldMask>0)&&(whatIsSoldMask<256))
		    	whatIsSoldMask=(CMath.pow(2,whatIsSoldMask-1)<<8);
	    	
    		for(int c=0;c<ShopKeeper.DEAL_CONFLICTS.length;c++)
    			for(int c1=0;c1<ShopKeeper.DEAL_CONFLICTS[c].length;c1++)
    				if(ShopKeeper.DEAL_CONFLICTS[c][c1]==mask)
    				{
    	    			for(c1=0;c1<ShopKeeper.DEAL_CONFLICTS[c].length;c1++)
    	    				if((ShopKeeper.DEAL_CONFLICTS[c][c1]!=mask)
    	    				&&(isSold(ShopKeeper.DEAL_CONFLICTS[c][c1])))
    	    					addSoldType(-ShopKeeper.DEAL_CONFLICTS[c][c1]);
	    				break;
	    			}
    		
	    	if(mask<0)
		    	whatIsSoldMask=CMath.unsetb(whatIsSoldMask,(CMath.pow(2,(-mask)-1)<<8));
	    	else
		    	whatIsSoldMask|=(CMath.pow(2,mask-1)<<8);
    	}
    }
    public long getWhatIsSoldMask(){return whatIsSoldMask;}
	public void setWhatIsSoldMask(long newSellCode){whatIsSoldMask=newSellCode;}

	protected void cloneFix(MOB E)
	{
		super.cloneFix(E);
		if(E instanceof StdShopKeeper)
            shop=((CoffeeShop)((StdShopKeeper)E).shop.copyOf()).build(this);
	}
    
    public CoffeeShop getShop(){return shop;}
    
    public String storeKeeperString(){return CMLib.coffeeShops().storeKeeperString(getShop());}
	public boolean doISellThis(Environmental thisThang){return CMLib.coffeeShops().doISellThis(thisThang,this);}
    protected Area getStartArea(){
        Area A=CMLib.map().getStartArea(this);
        if(A==null) CMLib.map().areaLocation(this);
        if(A==null) A=(Area)CMLib.map().areas().nextElement();
        return A;
    }
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==Tickable.TICKID_MOB)&&(isGeneric()))
		{
			if((--invResetTickDown)<=0)
			{
				invResetTickDown=finalInvResetRate(); // we should now be at a positive number.
				if(invResetTickDown<=0)
					invResetTickDown=Integer.MAX_VALUE;
				else
				{
	                shop.emptyAllShelves();
					if(miscText!=null)
					{
						String shoptext;
						if(CMProps.getBoolVar(CMProps.SYSTEMB_MOBCOMPRESS))
							shoptext=CMLib.coffeeMaker().getGenMOBTextUnpacked(this,CMLib.encoder().decompressString(miscText));
						else
							shoptext=CMLib.coffeeMaker().getGenMOBTextUnpacked(this,CMStrings.bytesToStr(miscText));
						Vector xml=CMLib.xml().parseAllXML(shoptext);
						if(xml!=null)
						{
							CMLib.coffeeMaker().populateShops(this,xml);
							recoverEnvStats();
							recoverCharStats();
						}
					}
				}
			}
			if((--budgetTickDown)<=0)
			{
				budgetTickDown=100;
				budgetRemaining=Long.MAX_VALUE/2;
				String s=finalBudget();
				Vector V=CMParms.parse(s.trim().toUpperCase());
				if(V.size()>0)
				{
					if(((String)V.firstElement()).equals("0"))
						budgetRemaining=0;
					else
					{
						budgetRemaining=CMath.s_long((String)V.firstElement());
						if(budgetRemaining==0)
							budgetRemaining=Long.MAX_VALUE/2;
					}
					s="DAY";
					if(V.size()>1) s=((String)V.lastElement()).toUpperCase();
					if(s.startsWith("DAY"))
						budgetTickDown=CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY);
					else
					if(location()!=null)
					{
						if(s.startsWith("HOUR"))
							budgetTickDown=CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)/location().getArea().getTimeObj().getHoursInDay();
						else
						if(s.startsWith("WEEK"))
							budgetTickDown=location().getArea().getTimeObj().getDaysInWeek()*CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY);
						else
						if(s.startsWith("MONTH"))
							budgetTickDown=location().getArea().getTimeObj().getDaysInMonth()*CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY);
						else
						if(s.startsWith("YEAR"))
							budgetTickDown=location().getArea().getTimeObj().getDaysInMonth()*location().getArea().getTimeObj().getMonthsInYear()*CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY);
					}
				}
				budgetMax=budgetRemaining;
			}
		}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
			{
                if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this)) 
                    return false;
                if(CMLib.coffeeShops().standardSellEvaluation(this,msg.source(),msg.tool(),this,budgetRemaining,budgetMax,msg.targetMinor()==CMMsg.TYP_SELL))
                    return super.okMessage(myHost,msg);
                return false;
			}
			case CMMsg.TYP_BID:
			{
                CMLib.commands().postSay(this,msg.source(),"I'm afraid my prices are firm.",false,false);
                return false;
			}
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_VIEW:
			{
                if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this)) 
                    return false;
                if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
                    return false;
                if(CMLib.coffeeShops().standardBuyEvaluation(this,msg.source(),msg.tool(),this,msg.targetMinor()==CMMsg.TYP_BUY))
                    return super.okMessage(myHost,msg);
                return false;
			}
			case CMMsg.TYP_LIST:
            {
                if(!CMLib.coffeeShops().ignoreIfNecessary(msg.source(),finalIgnoreMask(),this)) 
                    return false;
				return super.okMessage(myHost,msg);
            }
			default:
				break;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if(msg.amITarget(this))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_GIVE:
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					if((msg.tool()!=null)
					&&((CMSecurity.isAllowed(msg.source(),location(),"ORDER")
	                    ||(CMLib.law().doesHavePriviledgesHere(msg.source(),getStartRoom()))
						||(CMSecurity.isAllowed(msg.source(),location(),"CMDMOBS")&&(isMonster()))
						||(CMSecurity.isAllowed(msg.source(),location(),"CMDROOMS")&&(isMonster()))))
					&&((doISellThis(msg.tool()))||(isSold(DEAL_INVENTORYONLY))))
	                {
	                    CMLib.commands().postSay(this,msg.source(),"Yes, I will now sell "+msg.tool().name()+".",false,false);
	                    getShop().addStoreInventory(msg.tool(),1,-1);
	                    if(isGeneric()) text();
						return;
					}
				}
				super.executeMsg(myHost,msg);
				break;
			case CMMsg.TYP_VALUE:
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
					CMLib.commands().postSay(this,mob,"I'll give you "+CMLib.beanCounter().nameCurrencyShort(this,CMLib.coffeeShops().pawningPrice(this,mob,msg.tool(),this).absoluteGoldPrice)+" for "+msg.tool().name()+".",true,false);
				break;
			}
			case CMMsg.TYP_SELL: // sell TO -- this is a shopkeeper purchasing from a player
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
	            {
	                double paid=CMLib.coffeeShops().transactPawn(this,msg.source(),this,msg.tool());
	                if(paid>Double.MIN_VALUE)
	                {
	                    budgetRemaining=budgetRemaining-Math.round(paid);
						if(mySession!=null)
							mySession.stdPrintln(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
						if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
							mob.location().recoverRoomStats();
	                    if(isGeneric()) text();
					}
	            }
				break;
			}
			case CMMsg.TYP_VIEW:
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					if((msg.tool()!=null)&&(getShop().doIHaveThisInStock("$"+msg.tool().Name()+"$",mob)))
						CMLib.commands().postSay(this,msg.source(),CMLib.coffeeShops().getViewDescription(msg.tool()),true,false);
				}
				break;
			}
			case CMMsg.TYP_BUY: // buy-from -- this is a player buying from a shopkeeper
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
	            {
	                MOB mobFor=CMLib.coffeeShops().parseBuyingFor(msg.source(),msg.targetMessage());
					if((msg.tool()!=null)
					&&(getShop().doIHaveThisInStock("$"+msg.tool().Name()+"$",mobFor))
					&&(location()!=null))
					{
				        Environmental item=getShop().getStock("$"+msg.tool().Name()+"$",mobFor);
				        if(item!=null) CMLib.coffeeShops().transactMoneyOnly(this,msg.source(),this,item,!isMonster());
				        
						Vector products=getShop().removeSellableProduct("$"+msg.tool().Name()+"$",mobFor);
						if(products.size()==0) break;
						Environmental product=(Environmental)products.firstElement();
	                    
						if(product instanceof Item)
						{
	                        if(!CMLib.coffeeShops().purchaseItems((Item)product,products,this,mobFor))
	                            return;
						}
						else
						if(product instanceof MOB)
						{
	                        if(CMLib.coffeeShops().purchaseMOB((MOB)product,this,this,mobFor))
	                        {
	                            msg.modify(msg.source(),msg.target(),product,msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),msg.targetMessage(),msg.othersCode(),msg.othersMessage());
	                            product.executeMsg(myHost,msg);
	                        }
						}
						else
						if(product instanceof Ability)
	                        CMLib.coffeeShops().purchaseAbility((Ability)product,this,this,mobFor);
	
						if(mySession!=null)
							mySession.stdPrintln(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
						if(!CMath.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
							mob.location().recoverRoomStats();
					}
	            }
				break;
			}
			case CMMsg.TYP_LIST:
			{
				super.executeMsg(myHost,msg);
				if(CMLib.flags().aliveAwakeMobileUnbound(mob,true))
				{
					String forMask=CMLib.coffeeShops().getListForMask(msg.targetMessage());
					Vector inventory=getShop().getStoreInventory();
					inventory=CMLib.coffeeShops().addRealEstateTitles(inventory,mob,getShop(),getStartRoom());
                    int limit=CMParms.getParmInt(finalPrejudiceFactors(),"LIMIT",0);
                    String s=CMLib.coffeeShops().getListInventory(this,mob,inventory,limit,this,forMask);
					if(s.length()>0)
						mob.tell(s);
				}
				break;
			}
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}

    public String finalPrejudiceFactors(){ 
        if(prejudiceFactors().length()>0) return prejudiceFactors();
        return getStartArea().finalPrejudiceFactors();
    }
	public String prejudiceFactors(){return CMLib.encoder().decompressString(miscText);}
	public void setPrejudiceFactors(String factors){miscText=CMLib.encoder().compressString(factors);}
    
    public String finalIgnoreMask(){ 
        if(ignoreMask().length()>0) return ignoreMask();
        return getStartArea().finalIgnoreMask();
    }
    public String ignoreMask(){return "";}
    public void setIgnoreMask(String factors){}

    public String[] finalItemPricingAdjustments(){ 
        if((itemPricingAdjustments()!=null)&&(itemPricingAdjustments().length>0))
            return itemPricingAdjustments();
        return getStartArea().finalItemPricingAdjustments();
    }
    public String[] itemPricingAdjustments(){ return pricingAdjustments;}
    public void setItemPricingAdjustments(String[] factors){pricingAdjustments=factors;}
    
    public String finalBudget(){ 
        if(budget().length()>0) return budget();
        return getStartArea().finalBudget();
    }
	public String budget(){return budget;}
	public void setBudget(String factors){budget=factors; budgetTickDown=0;}
	
    public String finalDevalueRate(){
        if(devalueRate().length()>0) return devalueRate();
        return getStartArea().finalDevalueRate();
    }
	public String devalueRate(){return devalueRate;}
	public void setDevalueRate(String factors){devalueRate=factors;}
	
    public int finalInvResetRate(){
        if(invResetRate()!=0) return invResetRate();
        return getStartArea().finalInvResetRate();
    }
	public int invResetRate(){return invResetRate;}
	public void setInvResetRate(int ticks){invResetRate=ticks; invResetTickDown=0;}
	
}
