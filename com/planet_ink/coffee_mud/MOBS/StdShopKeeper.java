package com.planet_ink.coffee_mud.MOBS;
import java.util.*;

import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
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
public class StdShopKeeper extends StdMOB implements ShopKeeper
{
	public String ID(){return "StdShopKeeper";}
    protected CoffeeShop shop=new CoffeeShop();
	protected int whatISell=0;
	protected int invResetRate=0;
	protected int invResetTickDown=0;
	protected String budget="";
	protected long budgetRemaining=Long.MAX_VALUE/2;
	protected long budgetMax=Long.MAX_VALUE/2;
	protected int budgetTickDown=2;
	protected String devalueRate="";

	public StdShopKeeper()
	{
		super();
		Username="a shopkeeper";
		setDescription("He\\`s pleased to be of assistance.");
		setDisplayText("A shopkeeper is waiting to serve you.");
		Factions.setAlignment(this,Faction.ALIGN_GOOD);
		setMoney(0);
		baseEnvStats.setWeight(150);
		setWimpHitPoint(0);

		baseCharStats().setStat(CharStats.INTELLIGENCE,16);
		baseCharStats().setStat(CharStats.CHARISMA,25);

		baseEnvStats().setArmor(0);

		baseState.setHitPoints(1000);

		recoverMaxState();
		resetToMaxState();
		recoverEnvStats();
		recoverCharStats();
	}

	public int whatIsSold(){return whatISell;}
	public void setWhatIsSold(int newSellCode){whatISell=newSellCode;}

	protected void cloneFix(MOB E)
	{
		super.cloneFix(E);
		if(E instanceof StdShopKeeper)
		{
            shop=new CoffeeShop();
            shop.cloneFix(((StdShopKeeper)E).shop);
		}
	}
    
    
    public boolean inBaseInventory(Environmental thisThang)
	{ return shop.inBaseInventory(thisThang);}
	public Environmental addStoreInventory(Environmental thisThang)
	{ return shop.addStoreInventory(thisThang,1,-1,whatIsSold(),this);}
	public int baseStockSize(){return shop.baseStockSize();}
	public int totalStockSize(){return shop.totalStockSize();}
	public void clearStoreInventory(){shop.clearStoreInventory();}
	public Vector getStoreInventory(){return shop.getStoreInventory();}
	public Vector getBaseInventory(){return shop.getBaseInventory();}
	public Environmental addStoreInventory(Environmental thisThang, int number, int price)
	{ return shop.addStoreInventory(thisThang,number,price,whatIsSold(),this);}
	public void delAllStoreInventory(Environmental thisThang)
	{ shop.delAllStoreInventory(thisThang,whatIsSold());}
    public boolean doIHaveThisInStock(String name, MOB mob)
    { return shop.doIHaveThisInStock(name,mob,whatIsSold(),getStartRoom());}
    public int stockPrice(Environmental likeThis)
    { return shop.stockPrice(likeThis);}
    public int numberInStock(Environmental likeThis)
    { return shop.numberInStock(likeThis);}
    public Environmental getStock(String name, MOB mob)
    { return shop.getStock(name,mob,whatIsSold(),getStartRoom());}
    public Environmental removeStock(String name, MOB mob)
    { return shop.removeStock(name,mob,whatIsSold(),getStartRoom());}
    public Vector removeSellableProduct(String named, MOB mob)
    { return shop.removeSellableProduct(named,mob,whatIsSold(),getStartRoom());}
    

    protected int processVariableEquipment()
    {
        int newLastTickedDateTime=super.processVariableEquipment();
        if(newLastTickedDateTime==0)
        {
            Vector rivals=new Vector();
            for(int v=0;v<shop.getBaseInventory().size();v++)
            {
                Environmental E=(Environmental)shop.getBaseInventory().elementAt(v);
                if((E.baseEnvStats().rejuv()>0)&&(E.baseEnvStats().rejuv()<Integer.MAX_VALUE))
                    rivals.addElement(E);
            }
            for(int r=0;r<rivals.size();r++)
            {
                Environmental E=(Environmental)rivals.elementAt(r);
                if(Dice.rollPercentage()>E.baseEnvStats().rejuv())
                    delAllStoreInventory(E);
                else
                {
                    E.baseEnvStats().setRejuv(0);
                    E.envStats().setRejuv(0);
                }
            }
        }
        return newLastTickedDateTime;
    }


    public String storeKeeperString(){return CoffeeShops.storeKeeperString(whatIsSold());}
	public boolean doISellThis(Environmental thisThang){return CoffeeShops.doISellThis(thisThang,this);}
    
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((tickID==MudHost.TICK_MOB)&&(isGeneric()))
		{
			if(invResetTickDown==0)
			{
				invResetTickDown=invResetRate();
				if(invResetTickDown==0) invResetTickDown=Util.s_int(CommonStrings.getVar(CommonStrings.SYSTEM_INVRESETRATE));
				if(invResetTickDown==0) invResetTickDown=Integer.MAX_VALUE;
			}
			else
			if((--invResetTickDown)<=0)
			{
                shop.emptyAllShelves();
				invResetTickDown=invResetRate();
				if(invResetTickDown==0) invResetTickDown=Util.s_int(CommonStrings.getVar(CommonStrings.SYSTEM_INVRESETRATE));
				if(invResetTickDown==0) invResetTickDown=Integer.MAX_VALUE;
				if(miscText!=null)
				{
					String shoptext;
					if(CommonStrings.getBoolVar(CommonStrings.SYSTEMB_MOBCOMPRESS))
						shoptext=CoffeeMaker.getGenMOBTextUnpacked(this,CMEncoder.decompressString(miscText));
					else
						shoptext=CoffeeMaker.getGenMOBTextUnpacked(this,new String(miscText));
					Vector xml=XMLManager.parseAllXML(shoptext);
					if(xml!=null)
					{
						CoffeeMaker.populateShops(this,xml);
						recoverEnvStats();
						recoverCharStats();
					}
				}
			}
			if((--budgetTickDown)<=0)
			{
				budgetTickDown=100;
				budgetRemaining=Long.MAX_VALUE/2;
				String s=budget();
				if(s.length()==0) s=CommonStrings.getVar(CommonStrings.SYSTEM_BUDGET);
				Vector V=Util.parse(s.trim().toUpperCase());
				if(V.size()>0)
				{
					if(((String)V.firstElement()).equals("0"))
						budgetRemaining=0;
					else
					{
						budgetRemaining=Util.s_long((String)V.firstElement());
						if(budgetRemaining==0)
							budgetRemaining=Long.MAX_VALUE/2;
					}
					s="DAY";
					if(V.size()>1) s=((String)V.lastElement()).toUpperCase();
					if(s.startsWith("DAY"))
						budgetTickDown=CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY);
					else
					if(location()!=null)
					{
						if(s.startsWith("HOUR"))
							budgetTickDown=CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)/location().getArea().getTimeObj().getHoursInDay();
						else
						if(s.startsWith("WEEK"))
							budgetTickDown=location().getArea().getTimeObj().getDaysInWeek()*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY);
						else
						if(s.startsWith("MONTH"))
							budgetTickDown=location().getArea().getTimeObj().getDaysInMonth()*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY);
						else
						if(s.startsWith("YEAR"))
							budgetTickDown=location().getArea().getTimeObj().getDaysInMonth()*location().getArea().getTimeObj().getMonthsInYear()*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY);
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
                if(!CoffeeShops.ignoreIfNecessary(msg.source(),ignoreMask(),this)) 
                    return false;
                if(CoffeeShops.standardSellEvaluation(this,msg.source(),msg.tool(),this,budgetRemaining,budgetMax,msg.targetMinor()==CMMsg.TYP_SELL))
                    return super.okMessage(myHost,msg);
                return false;
			}
			case CMMsg.TYP_BUY:
			case CMMsg.TYP_VIEW:
			{
                if(!CoffeeShops.ignoreIfNecessary(msg.source(),ignoreMask(),this)) 
                    return false;
                if((msg.targetMinor()==CMMsg.TYP_BUY)&&(msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
                    return false;
                if(CoffeeShops.standardBuyEvaluation(this,msg.source(),msg.tool(),this,msg.targetMinor()==CMMsg.TYP_BUY))
                    return super.okMessage(myHost,msg);
                return false;
			}
			case CMMsg.TYP_LIST:
            {
                if(!CoffeeShops.ignoreIfNecessary(msg.source(),ignoreMask(),this)) 
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
				if((msg.tool()!=null)
				&&((CMSecurity.isAllowed(msg.source(),location(),"ORDER")
                    ||(CoffeeUtensils.doesHavePriviledgesHere(msg.source(),getStartRoom()))
					||(CMSecurity.isAllowed(msg.source(),location(),"CMDMOBS")&&(isMonster()))
					||(CMSecurity.isAllowed(msg.source(),location(),"CMDROOMS")&&(isMonster()))))
				&&((doISellThis(msg.tool()))||(whatISell==DEAL_INVENTORYONLY)))
                {
                    CommonMsgs.say(this,msg.source(),"Yes, I will now sell "+msg.tool().name()+".",false,false);
                    addStoreInventory(msg.tool(),1,-1);
                    if(isGeneric()) text();
					return;
				}
				super.executeMsg(myHost,msg);
				break;
			case CMMsg.TYP_VALUE:
				super.executeMsg(myHost,msg);
				CommonMsgs.say(this,mob,"I'll give you "+BeanCounter.nameCurrencyShort(this,CoffeeShops.pawningPrice(mob,msg.tool(),this).absoluteGoldPrice)+" for "+msg.tool().name()+".",true,false);
				break;
			case CMMsg.TYP_SELL: // sell TO -- this is a shopkeeper purchasing from a player
            {
				super.executeMsg(myHost,msg);
                double paid=CoffeeShops.transactPawn(this,msg.source(),this,msg.tool());
                if(paid>Double.MIN_VALUE)
                {
                    budgetRemaining=budgetRemaining-Math.round(paid);
					if(mySession!=null)
						mySession.stdPrintln(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						mob.location().recoverRoomStats();
                    if(isGeneric()) text();
				}
				break;
            }
			case CMMsg.TYP_VIEW:
				super.executeMsg(myHost,msg);
				if((msg.tool()!=null)&&(doIHaveThisInStock("$"+msg.tool().Name()+"$",mob)))
					CommonMsgs.say(this,msg.source(),CoffeeShops.getViewDescription(msg.tool()),true,false);
				break;
			case CMMsg.TYP_BUY: // buy-from -- this is a player buying from a shopkeeper
            {
				super.executeMsg(myHost,msg);
                MOB mobFor=CoffeeShops.parseBuyingFor(msg.source(),msg.targetMessage());
				if((msg.tool()!=null)
				&&(doIHaveThisInStock("$"+msg.tool().Name()+"$",mobFor))
				&&(location()!=null))
				{
					Vector products=removeSellableProduct("$"+msg.tool().Name()+"$",mobFor);
					if(products.size()==0) break;
					Environmental product=(Environmental)products.firstElement();
                    
                    CoffeeShops.transactMoneyOnly(this,msg.source(),this,product);
                    
					if(product instanceof Item)
					{
                        if(!CoffeeShops.purchaseItems((Item)product,products,this,mobFor))
                            return;
					}
					else
					if(product instanceof MOB)
					{
                        if(CoffeeShops.purchaseMOB((MOB)product,this,this,mobFor))
                        {
                            msg.modify(msg.source(),msg.target(),product,msg.sourceCode(),msg.sourceMessage(),msg.targetCode(),msg.targetMessage(),msg.othersCode(),msg.othersMessage());
                            product.executeMsg(myHost,msg);
                        }
					}
					else
					if(product instanceof Ability)
                        CoffeeShops.purchaseAbility((Ability)product,this,this,mobFor);

					if(mySession!=null)
						mySession.stdPrintln(msg.source(),msg.target(),msg.tool(),msg.targetMessage());
					if(!Util.bset(msg.targetCode(),CMMsg.MASK_OPTIMIZE))
						mob.location().recoverRoomStats();
				}
				break;
            }
			case CMMsg.TYP_LIST:
				{
					super.executeMsg(myHost,msg);
					Vector inventory=getStoreInventory();
					inventory=CoffeeShops.addRealEstateTitles(inventory,mob,whatIsSold(),getStartRoom());
                    int limit=Util.getParmInt(prejudiceFactors(),"LIMIT",0);
                    String s=CoffeeShops.getListInventory(this,mob,inventory,limit,this);
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

	public String prejudiceFactors(){return CMEncoder.decompressString(miscText);}
	public void setPrejudiceFactors(String factors){miscText=CMEncoder.compressString(factors);}
    
    public String ignoreMask(){return "";}
    public void setIgnoreMask(String factors){}

	public String budget(){return budget;}
	public void setBudget(String factors){budget=factors; budgetTickDown=0;}
	
	public String devalueRate(){return devalueRate;}
	public void setDevalueRate(String factors){devalueRate=factors;}
	
	public int invResetRate(){return invResetRate;}
	public void setInvResetRate(int ticks){invResetRate=ticks; invResetTickDown=0;}
	
}
