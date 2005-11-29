package com.planet_ink.coffee_mud.Abilities.Common;
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

public class Merchant extends CommonSkill implements ShopKeeper
{
	public String ID() { return "Merchant"; }
	public String name(){ return "Marketeering";}
	private static final String[] triggerStrings = {"MARKET"};
	public String[] triggerStrings(){return triggerStrings;}
	public int overrideMana(){return 5;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
	protected int trainsRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKILLTRAINCOST);}
	protected int practicesRequired(){return CommonStrings.getIntVar(CommonStrings.SYSTEMI_SKILLPRACCOST);}

    protected CoffeeShop shop=new CoffeeShop();
	public Merchant()
	{
		super();
		displayText="";

		isAutoInvoked();
	}
	public String text()
	{
		return shop.makeXML();
	}
	public String budget(){return "";}
	public void setBudget(String factors){}
	public String devalueRate(){return "";}
	public void setDevalueRate(String factors){}
	public int invResetRate(){return 0;}
	public void setInvResetRate(int ticks){}
	public void setMiscText(String text)
	{
		synchronized(this)
		{
            shop.buildShopFromXML(text,whatIsSold(),this);
		}
	}

	public void affectEnvStats(Environmental E, EnvStats affectableStats)
	{
		if(E instanceof MOB)
			affectableStats.setWeight(affectableStats.weight()+shop.totalStockWeight());
	}

	public int whatIsSold(){return ShopKeeper.DEAL_ANYTHING;}
	public void setWhatIsSold(int newSellCode){}
    protected boolean inBaseInventory(Environmental thisThang)
    { return shop.inBaseInventory(thisThang);}
    public Environmental addStoreInventory(Environmental thisThang)
    { return shop.addStoreInventory(thisThang,1,-1,whatIsSold(),this);}
    public int baseStockSize(){return shop.baseStockSize();}
    public int totalStockSize(){return shop.totalStockSize();}
    public void clearStoreInventory(){shop.clearStoreInventory(); setMiscText("");}
    public Vector getStoreInventory(){return shop.getStoreInventory();}
    public Vector getBaseInventory(){return shop.getBaseInventory();}
    public Environmental addStoreInventory(Environmental thisThang, int number, int price)
    { return shop.addStoreInventory(thisThang,number,price,whatIsSold(),this);}
    public void delAllStoreInventory(Environmental thisThang)
    { shop.delAllStoreInventory(thisThang,whatIsSold());}
    public boolean doIHaveThisInStock(String name, MOB mob)
    { return shop.doIHaveThisInStock(name,mob,whatIsSold(),null);}
    public int stockPrice(Environmental likeThis)
    { return shop.stockPrice(likeThis);}
    public int numberInStock(Environmental likeThis)
    { return shop.numberInStock(likeThis);}
    public Environmental getStock(String name, MOB mob)
    { return shop.getStock(name,mob,whatIsSold(),null);}
    public Environmental removeStock(String name, MOB mob)
    { return shop.removeStock(name,mob,whatIsSold(),null);}
    public Vector removeSellableProduct(String named, MOB mob)
    { return shop.removeSellableProduct(named,mob,whatIsSold(),null);}
	public boolean doISellThis(Environmental thisThang)
	{
		return (thisThang instanceof Item)&&(numberInStock(thisThang)>0);
	}

	public String storeKeeperString(){return "Only my Inventory";}
	public String prejudiceFactors(){return "";}
	public void setPrejudiceFactors(String factors){}
    public String ignoreMask(){return "";}
    public void setIgnoreMask(String factors){}
    private MOB staticMOB=null;
    
    public MOB deriveMerchant()
    {
        if(affected ==null) return null;
        if(affected instanceof MOB)
            return (MOB)affected;
        if(affected instanceof Item)
        {
            if(((Item)affected).owner() instanceof MOB)
                return (MOB)((Item)affected).owner();
            if(Sense.isGettable((Item)affected))
                return null;
        }
        Room room=CoffeeUtensils.roomLocation(affected);
        if(room==null) return null;
        if(staticMOB==null)
        {
            staticMOB=CMClass.getMOB("StdMOB");
            if((affected instanceof Room)||(affected instanceof Exit))
                staticMOB.setName("the shopkeeper");
            else
                staticMOB.setName(affected.Name());
        }
        staticMOB.setLocation(room);
        if(BeanCounter.getTotalAbsoluteNativeValue(staticMOB)<100000.0)
            staticMOB.setMoney(100000);
        return staticMOB;
    }

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
        MOB merchantM=deriveMerchant();
		if(merchantM==null)
			return super.okMessage(myHost,msg);

		MOB shopperM=msg.source();
        if((msg.source()==merchantM)
        &&(msg.targetMinor()==CMMsg.TYP_GET)
        &&(msg.target() instanceof Item))
        {
            Item newitem=(Item)msg.target();
            if((newitem.numberOfItems()>(merchantM.maxItems()-(merchantM.inventorySize()+shop.totalStockSizeIncludingDuplicates())))
            &&(!merchantM.isMine(this)))
            {
                merchantM.tell("You can't carry that many items.");
                return false;
            }
        }
        
		if(msg.amITarget(merchantM))
		{
			switch(msg.targetMinor())
			{
			case CMMsg.TYP_VALUE:
			case CMMsg.TYP_SELL:
            {
                if(!merchantM.isMonster())
                {
                    shopperM.tell("You'll have to talk to "+merchantM.name()+" about that.");
    				return false;
                }
                double budgetRemaining=BeanCounter.getTotalAbsoluteValue(merchantM,BeanCounter.getCurrency(merchantM));
                double budgetMax=budgetRemaining*100;
                if(CoffeeShops.standardSellEvaluation(merchantM,msg.source(),msg.tool(),this,budgetRemaining,budgetMax,msg.targetMinor()==CMMsg.TYP_SELL))
                    return super.okMessage(myHost,msg);
                return false;
            }
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_VIEW:
			{
                if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
                    return false;
                if(CoffeeShops.standardBuyEvaluation(merchantM,msg.source(),msg.tool(),this,msg.targetMinor()==CMMsg.TYP_BUY))
					return super.okMessage(myHost,msg);
				return false;
			}
			case CMMsg.TYP_LIST:
				return super.okMessage(myHost,msg);
			default:
				break;
			}
		}
		else
		if(msg.amISource(merchantM)&&(msg.sourceMinor()==CMMsg.TYP_DEATH))
		{
			Item I=(Item)removeStock("all",merchantM);
			while(I!=null)
			{
                merchantM.addInventory(I);
				I=(Item)removeStock("all",merchantM);
			}
            merchantM.recoverEnvStats();
		}
		return super.okMessage(myHost,msg);
	}

    public boolean putUpForSale(MOB source, MOB merchantM, Environmental tool)
    {
        if((tool!=null)
        &&(merchantM.isMonster())
        &&((CMSecurity.isAllowed(source,merchantM.location(),"ORDER")
            ||(CoffeeUtensils.doesHavePriviledgesHere(source,merchantM.getStartRoom()))
            ||(CMSecurity.isAllowed(source,merchantM.location(),"CMDMOBS")&&(merchantM.isMonster()))
            ||(CMSecurity.isAllowed(source,merchantM.location(),"CMDROOMS")&&(merchantM.isMonster())))))
        {
            CommonMsgs.say(merchantM,source,"OK, I will now sell "+tool.name()+".",false,false);
            addStoreInventory(tool,1,-1);
            return true;
        }
        return false;
    }
    
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
        MOB merchantM=deriveMerchant();
		if(merchantM==null)
		{
			super.executeMsg(myHost,msg);
			return;
		}

		if(msg.amITarget(merchantM))
		{
			MOB mob=msg.source();
			switch(msg.targetMinor())
			{
            case CMMsg.TYP_GIVE:
                if(!putUpForSale(msg.source(),merchantM,msg.tool()))
                    super.executeMsg(myHost,msg);
                break;
            case CMMsg.TYP_DROP:
                if(((affected instanceof Room)
                    ||(affected instanceof Exit)
                    ||((affected instanceof Item)&&(!Sense.isGettable((Item)affected))))
                &&(putUpForSale(msg.source(),merchantM,msg.tool())))
                    return;
                super.executeMsg(myHost,msg);
                break;
            case CMMsg.TYP_VALUE:
                super.executeMsg(myHost,msg);
                if(merchantM.isMonster())
                    CommonMsgs.say(merchantM,mob,"I'll give you "+BeanCounter.nameCurrencyShort(merchantM,CoffeeShops.pawningPrice(mob,msg.tool(),this).absoluteGoldPrice)+" for "+msg.tool().name()+".",true,false);
                break;
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost,msg);
				if((msg.tool()!=null)&&(doIHaveThisInStock(msg.tool().Name(),mob)))
					CommonMsgs.say(merchantM,msg.source(),"Interested in "+msg.tool().name()+"? Here is some information for you:\n\rLevel "+msg.tool().envStats().level()+"\n\rDescription: "+msg.tool().description(),true,false);
				break;
            case CMMsg.TYP_SELL: // sell TO -- this is a shopkeeper purchasing from a player
            {
                super.executeMsg(myHost,msg);
                CoffeeShops.transactPawn(merchantM,msg.source(),this,msg.tool());
                break;
            }
			case CMMsg.TYP_BUY:
            {
				super.executeMsg(myHost,msg);
                MOB mobFor=CoffeeShops.parseBuyingFor(msg.source(),msg.targetMessage());
                if((msg.tool()!=null)
                &&(doIHaveThisInStock("$"+msg.tool().Name()+"$",mobFor))
                &&(merchantM.location()!=null))
                {
                    Vector products=removeSellableProduct("$"+msg.tool().Name()+"$",mobFor);
                    if(products.size()==0) break;
                    Environmental product=(Environmental)products.firstElement();
                    
                    CoffeeShops.transactMoneyOnly(merchantM,msg.source(),this,product);
                    
                    if(product instanceof Item)
                    {
                        if(!CoffeeShops.purchaseItems((Item)product,products,merchantM,mobFor))
                            return;
                    }
                    else
                    if(product instanceof MOB)
                    {
                        if(CoffeeShops.purchaseMOB((MOB)product,merchantM,this,mobFor))
                        {
                            msg.modify(msg.source(),msg.target(),product,msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),msg.targetMessage(),msg.othersCode(),msg.othersMessage());
                            product.executeMsg(myHost,msg);
                        }
                    }
                    else
                    if(product instanceof Ability)
                        CoffeeShops.purchaseAbility((Ability)product,merchantM,this,mobFor);
                }
				break;
            }
			case CMMsg.TYP_LIST:
				{
					super.executeMsg(myHost,msg);
                    Vector inventory=getStoreInventory();
                    String s=CoffeeShops.getListInventory(merchantM,mob,inventory,0,this);
                    if(s.length()>0)
                        mob.tell(s);
				}
				break;
			default:
				super.executeMsg(myHost,msg);
				break;
			}
		}
		else
			super.executeMsg(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Market what? Enter \"market list\" for a list or \"market item value\" to sell something.");
			return false;
		}
		if(Util.combine(commands,0).equalsIgnoreCase("list"))
		{
			FullMsg msg=new FullMsg(mob,mob,CMMsg.MSG_LIST,"<S-NAME> review(s) <S-HIS-HER> inventory.");
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return true;
		}
		if(((String)commands.firstElement()).equalsIgnoreCase("remove")
		||((String)commands.firstElement()).equalsIgnoreCase("delete"))
		{
			if(commands.size()==1)
			{
				commonTell(mob,"Remove what item from the marketing list?");
				return false;
			}
			String itemName=Util.combine(commands,1);
			Item I=(Item)removeStock(itemName,mob);
			if(I==null)
			{
				commonTell(mob,"'"+itemName+"' is not on the list.");
				return false;
			}
			String iname=I.name();
			while(I!=null)
			{
				mob.addInventory(I);
				I=(Item)removeStock(itemName,mob);
			}
			delAllStoreInventory(I);
			mob.recoverCharStats();
			mob.recoverEnvStats();
			mob.recoverMaxState();
			mob.tell(iname+" has been removed from your inventory list.");
			return true;
		}

		Environmental target=null;
		double val=-1;
		if(commands.size()>1)
		{
			String s=(String)commands.lastElement();
            if(Util.isInteger(s))
            {
                val=new Integer(Util.s_int(s)).doubleValue();
                if(val>0) commands.removeElement(s);
            }
            else
            {
    			long numberCoins=EnglishParser.numPossibleGold(mob,s);
    		    if(numberCoins>0)
    		    {
    			    String currency=EnglishParser.numPossibleGoldCurrency(mob,s);
    			    double denom=EnglishParser.numPossibleGoldDenomination(mob,currency,s);
    			    if(denom>0.0)
    			    {
    					val=Util.mul(numberCoins,denom);
    					if(val>0) commands.removeElement(s);
    			    }
    		    }
            }
		}

		String itemName=Util.combine(commands,0);
		Vector V=new Vector();
		boolean allFlag=((String)commands.elementAt(0)).equalsIgnoreCase("all");
		if(itemName.toUpperCase().startsWith("ALL.")){ allFlag=true; itemName="ALL "+itemName.substring(4);}
		if(itemName.toUpperCase().endsWith(".ALL")){ allFlag=true; itemName="ALL "+itemName.substring(0,itemName.length()-4);}
		int addendum=1;
		String addendumStr="";
		do
		{
			Item I=mob.fetchCarried(null,itemName+addendumStr);
			if(I==null) break;
			if(target==null)
				target=I;
			else
			if(!target.sameAs(I))
				break;
			if(Sense.canBeSeenBy(I,mob))
				V.addElement(I);
			addendumStr="."+(++addendum);
		}
		while(allFlag);

		if(V.size()==0)
		{
			commonTell(mob,"You don't seem to be carrying '"+itemName+"'.");
			return false;
		}

		if((numberInStock(target)<=0)&&(val<=0))
		{
			commonTell(mob,"You failed to specify a price for '"+itemName+"'.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(!profficiencyCheck(mob,0,auto))
		{
			commonTell(mob,target,null,"You fail to put <T-NAME> up for sale.");
			return false;
		}

		FullMsg msg=new FullMsg(mob,target,CMMsg.MSG_SELL,"<S-NAME> put(s) <T-NAME> up for sale.");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			for(int i=0;i<V.size();i++)
			{
				Item I=(Item)V.elementAt(i);
				if(val<=0)
					addStoreInventory(I);
				else
					addStoreInventory(I,1,(int)Math.round(val));
				mob.delInventory(I);
			}
		}
		mob.location().recoverRoomStats();
		mob.recoverEnvStats();
		return true;
	}
}
