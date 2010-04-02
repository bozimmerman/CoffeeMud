package com.planet_ink.coffee_mud.Libraries;
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
import com.planet_ink.coffee_mud.MOBS.interfaces.Auctioneer.AuctionData;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;


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
public class CoffeeShops extends StdLibrary implements ShoppingLibrary
{
    public String ID(){return "CoffeeShops";}
    public ShopKeeper getShopKeeper(Environmental E)
    {
        if(E==null) return null;
        if(E instanceof ShopKeeper) return (ShopKeeper)E;
        Ability A=null;
        for(int a=0;a<E.numEffects();a++)
        {
            A=E.fetchEffect(a);
            if(A instanceof ShopKeeper)
                return (ShopKeeper)A;
        }
        if(E instanceof MOB)
        {
            Item I=null;
            MOB mob=(MOB)E;
            for(int i=0;i<mob.inventorySize();i++)
            {
                I=mob.fetchInventory(i);
                if(I instanceof ShopKeeper)
                    return (ShopKeeper)I;
                if(I!=null)
                for(int a=0;a<I.numEffects();a++)
                {
                    A=I.fetchEffect(a);
                    if(A instanceof ShopKeeper)
                        return (ShopKeeper)A;
                }
            }
        }
        return null;
    }

    public Vector getAllShopkeepers(Room here, MOB notMOB)
    {
        Vector V=new Vector();
        if(here!=null)
        {
            if(getShopKeeper(here)!=null)
                V.addElement(here);
            Area A=here.getArea();
            if(getShopKeeper(A)!=null)
                V.addElement(A);
            Vector V2=A.getParentsRecurse();
            for(int v2=0;v2<V2.size();v2++)
                if(getShopKeeper((Area)V2.elementAt(v2))!=null)
                    V.addElement(V2.elementAt(v2));

            for(int i=0;i<here.numInhabitants();i++)
            {
                MOB thisMOB=here.fetchInhabitant(i);
                if((thisMOB!=null)
                &&(thisMOB!=notMOB)
                &&(getShopKeeper(thisMOB)!=null)
                &&((notMOB==null)||(CMLib.flags().canBeSeenBy(thisMOB,notMOB))))
                    V.addElement(thisMOB);
            }
            for(int i=0;i<here.numItems();i++)
            {
                Item thisItem=here.fetchItem(i);
                if((thisItem!=null)
                &&(thisItem!=notMOB)
                &&(getShopKeeper(thisItem)!=null)
                &&(!CMLib.flags().isGettable(thisItem))
                &&(thisItem.container()==null)
                &&((notMOB==null)||(CMLib.flags().canBeSeenBy(thisItem,notMOB))))
                    V.addElement(thisItem);
            }
        }
        return V;
    }

    public String getViewDescription(Environmental E)
    {
        StringBuffer str=new StringBuffer("");
        if(E==null) return str.toString();
        str.append("Interested in "+E.name()+"?");
        str.append(" Here is some information for you:");
        str.append("\n\rLevel      : "+E.envStats().level());
        if(E instanceof Item)
        {
            Item I=(Item)E;
            str.append("\n\rMaterial   : "+CMStrings.capitalizeAndLower(RawMaterial.CODES.NAME(I.material()).toLowerCase()));
            str.append("\n\rWeight     : "+I.envStats().weight()+" pounds");
            if(I instanceof Weapon)
            {
                str.append("\n\rWeap. Type : "+CMStrings.capitalizeAndLower(Weapon.TYPE_DESCS[((Weapon)I).weaponType()]));
                str.append("\n\rWeap. Class: "+CMStrings.capitalizeAndLower(Weapon.CLASS_DESCS[((Weapon)I).weaponClassification()]));
            }
            else
            if(I instanceof Armor)
            {
                str.append("\n\rWear Info  : Worn on ");
                Wearable.CODES codes = Wearable.CODES.instance();
                for(long wornCode : codes.all())
	                if(wornCode != Wearable.IN_INVENTORY)
	                {
	                    if(codes.name(wornCode).length()>0)
	                    {
	                        if(((I.rawProperLocationBitmap()&wornCode)==wornCode))
	                        {
	                            str.append(CMStrings.capitalizeAndLower(codes.name(wornCode))+" ");
	                            if(I.rawLogicalAnd())
	                                str.append("and ");
	                            else
	                                str.append("or ");
	                        }
	                    }
	                }
                if(str.toString().endsWith(" and "))
                    str.delete(str.length()-5,str.length());
                else
                if(str.toString().endsWith(" or "))
                    str.delete(str.length()-4,str.length());
            }
        }
        str.append("\n\rDescription: "+E.description());
        return str.toString();
    }

    protected Ability getTrainableAbility(MOB teacher, Ability A)
    {
    	if((teacher==null)||(A==null)) return A;
    	Ability teachableA=teacher.fetchAbility(A.ID());
    	if(teachableA==null)
    	{
    		teachableA=(Ability)A.copyOf();
    		teacher.addAbility(teachableA);
    	}
    	teachableA.setProficiency(100);
    	return teachableA;
    }
    
    protected boolean shownInInventory(MOB seller, MOB buyer, Environmental product, ShopKeeper shopKeeper)
    {
        if(CMSecurity.isAllowed(buyer,buyer.location(),"CMDMOBS")) 
        	return true;
        if(seller == buyer) return true;
        if(product instanceof Item) 
        {
            if(((Item)product).container()!=null) 
            	return false;
            if(((Item)product).envStats().level()>buyer.envStats().level()) 
            	return false;
            if(!CMLib.flags().canBeSeenBy(product,buyer)) 
            	return false;
        }
        if(product instanceof MOB)
        {
            if(((MOB)product).envStats().level()>buyer.envStats().level()) 
            	return false;
        }
        if(product instanceof Ability)
        {
        	if(shopKeeper.isSold(ShopKeeper.DEAL_TRAINER))
        	{
        		if(!CMLib.ableMapper().qualifiesByLevel(buyer, (Ability)product))
    				return false;
        	}
        }
        return true;
    }

    public double rawSpecificGoldPrice(Environmental product,
                                       CoffeeShop shop,
                                       double numberOfThem)
    {
        double price=0.0;
        if(product instanceof Item)
            price=((Item)product).value()*numberOfThem;
        else
        if(product instanceof Ability)
        {
            if(shop.isSold(ShopKeeper.DEAL_TRAINER))
                price=CMLib.ableMapper().lowestQualifyingLevel(product.ID())*100;
            else
                price=CMLib.ableMapper().lowestQualifyingLevel(product.ID())*75;
        }
        else
        if(product instanceof MOB)
        {
            Ability A=product.fetchEffect("Prop_Retainable");
            if(A!=null)
            {
                if(A.text().indexOf(";")<0)
                {
                    if(CMath.isDouble(A.text()))
                        price=CMath.s_double(A.text());
                    else
                        price=(double)CMath.s_int(A.text());
                }
                else
                {
                    String s2=A.text().substring(0,A.text().indexOf(";"));
                    if(CMath.isDouble(s2))
                        price=CMath.s_double(s2);
                    else
                        price=(double)CMath.s_int(s2);
                }
            }
            if(price==0.0)
                price=(25.0+product.envStats().level())*product.envStats().level();
        }
        else
            price=CMLib.ableMapper().lowestQualifyingLevel(product.ID())*25;
        return price;
    }

    public double prejudiceValueFromPart(MOB customer, boolean pawnTo, String part)
    {
        int x=part.indexOf("=");
        if(x<0) return 0.0;
        String sellorby=part.substring(0,x);
        part=part.substring(x+1);
        if(pawnTo&&(!sellorby.trim().equalsIgnoreCase("SELL")))
           return 0.0;
        if((!pawnTo)&&(!sellorby.trim().equalsIgnoreCase("BUY")))
           return 0.0;
        if(part.trim().indexOf(" ")<0)
            return CMath.s_double(part.trim());
        Vector V=CMParms.parse(part.trim());
        double d=0.0;
        boolean yes=false;
        Vector VF=customer.fetchFactionRanges();
        String align=CMLib.flags().getAlignmentName(customer);
        String sex=customer.charStats().genderName();
        for(int v=0;v<V.size();v++)
        {
            String bit=(String)V.elementAt(v);
            if(CMath.s_double(bit)!=0.0)
                d=CMath.s_double(bit);
            if(bit.equalsIgnoreCase(customer.charStats().getCurrentClass().name() ))
            { yes=true; break;}
            if(bit.equalsIgnoreCase(customer.charStats().getCurrentClass().name(customer.charStats().getCurrentClassLevel()) ))
            { yes=true; break;}
            if(bit.equalsIgnoreCase(sex ))
            { yes=true; break;}
            if(bit.equalsIgnoreCase(customer.charStats().getMyRace().racialCategory()))
            {   yes=true; break;}
            if(bit.equalsIgnoreCase(align))
            { yes=true; break;}
            for(int vf=0;vf<VF.size();vf++)
                if(bit.equalsIgnoreCase((String)V.elementAt(v)))
                { yes=true; break;}
        }
        if(yes) return d;
        return 0.0;

    }

    public double prejudiceFactor(MOB customer, String factors, boolean pawnTo)
    {
        factors=factors.toUpperCase();
        if(factors.length()==0)
        {
            factors=CMProps.getVar(CMProps.SYSTEM_PREJUDICE).trim();
            if(factors.length()==0)
                return 1.0;
        }
        if(factors.indexOf("=")<0)
        {
            if(CMath.s_double(factors)!=0.0)
                return CMath.s_double(factors);
            return 1.0;
        }
        int x=factors.indexOf(";");
        while(x>=0)
        {
            String part=factors.substring(0,x).trim();
            factors=factors.substring(x+1).trim();
            double d=prejudiceValueFromPart(customer,pawnTo,part);
            if(d!=0.0) return d;
            x=factors.indexOf(";");
        }
        double d=prejudiceValueFromPart(customer,pawnTo,factors.trim());
        if(d!=0.0) return d;
        return 1.0;
    }

    public double itemPriceFactor(Environmental E, Room R, String[] priceFactors, boolean pawnTo)
    {
        if(priceFactors.length==0) return 1.0;
        double factor=1.0;
        int x=0;
        String factorMask=null;
        Environmental oldOwner=null;
        if(E instanceof Item)
        {
            oldOwner=((Item)E).owner();
            if(R!=null) ((Item)E).setOwner(R);
        }
        for(int p=0;p<priceFactors.length;p++)
        {
            factorMask=priceFactors[p].trim();
            x=factorMask.indexOf(' ');
            if(x<0) continue;
            if(CMLib.masking().maskCheck(factorMask.substring(x+1).trim(),E,false))
                factor*=CMath.s_double(factorMask.substring(0,x).trim());
        }
        if(E instanceof Item)
            ((Item)E).setOwner(oldOwner);
        if(factor!=0.0) return factor;
        return 1.0;
    }

    public ShopKeeper.ShopPrice sellingPrice(MOB seller,
                                             MOB buyer,
                                             Environmental product,
                                             ShopKeeper shop,
                                             boolean includeSalesTax)
    {
        double number=1.0;
        ShopKeeper.ShopPrice val=new ShopKeeper.ShopPrice();
        if(product==null) return val;
        int stockPrice=shop.getShop().stockPrice(product);
        if(stockPrice<=-100)
        {
            if(stockPrice<=-1000)
                val.experiencePrice=(stockPrice*-1)-1000;
            else
                val.questPointPrice=(stockPrice*-1)-100;
            return val;
        }
        if(stockPrice>=0)
            val.absoluteGoldPrice=(double)stockPrice;
        else
            val.absoluteGoldPrice=rawSpecificGoldPrice(product,shop.getShop(),number);

        if(buyer==null)
        {
            if(val.absoluteGoldPrice>0.0)
                val.absoluteGoldPrice=CMLib.beanCounter().abbreviatedRePrice(seller,val.absoluteGoldPrice);
            return val;
        }

        double prejudiceFactor=prejudiceFactor(buyer,shop.finalPrejudiceFactors(),false);
        Room loc=CMLib.map().roomLocation(shop);
        prejudiceFactor*=itemPriceFactor(product,loc,shop.finalItemPricingAdjustments(),false);
        val.absoluteGoldPrice=CMath.mul(prejudiceFactor,val.absoluteGoldPrice);

        // the price is 200% at 0 charisma, and 100% at 30
        val.absoluteGoldPrice=val.absoluteGoldPrice
                             +val.absoluteGoldPrice
                             -CMath.mul(val.absoluteGoldPrice,CMath.div(buyer.charStats().getStat(CharStats.STAT_CHARISMA),30.0));
        if(includeSalesTax)
        {
            double salesTax=getSalesTax(seller.getStartRoom(),seller);
            val.absoluteGoldPrice+=((salesTax>0.0)?(CMath.mul(val.absoluteGoldPrice,CMath.div(salesTax,100.0))):0.0);
        }
        if(val.absoluteGoldPrice<=0.0)
            val.absoluteGoldPrice=1.0;
        else
            val.absoluteGoldPrice=CMLib.beanCounter().abbreviatedRePrice(seller,val.absoluteGoldPrice);

        // the magical aura discount for miscmagic (potions, anything else.. MUST be baseEnvStats tho!
        if((CMath.bset(buyer.baseEnvStats().disposition(),EnvStats.IS_BONUS))
        &&(product instanceof MiscMagic)
        &&(val.absoluteGoldPrice>2.0))
        	val.absoluteGoldPrice/=2;

        return val;
    }


    public double devalue(ShopKeeper shop, Environmental product)
    {
        int num=shop.getShop().numberInStock(product);
        if(num<=0) return 0.0;
        double resourceRate=0.0;
        double itemRate=0.0;
        String s=shop.finalDevalueRate();
        Vector V=CMParms.parse(s.trim());
        if(V.size()<=0)
            return 0.0;
        else
        if(V.size()==1)
        {
            resourceRate=CMath.div(CMath.s_double((String)V.firstElement()),100.0);
            itemRate=resourceRate;
        }
        else
        {
            itemRate=CMath.div(CMath.s_double((String)V.firstElement()),100.0);
            resourceRate=CMath.div(CMath.s_double((String)V.lastElement()),100.0);
        }
        double rate=(product instanceof RawMaterial)?resourceRate:itemRate;
        rate=rate*num;
        if(rate>1.0) rate=1.0;
        if(rate<0.0) rate=0.0;
        return rate;
    }

    public ShopKeeper.ShopPrice pawningPrice(MOB seller,
                                             MOB buyer,
                                             Environmental product,
                                             ShopKeeper shop)
    {
        double number=1.0;
        if(product instanceof PackagedItems)
        {
            number=(double)((PackagedItems)product).numberOfItemsInPackage();
            product=((PackagedItems)product).getItem();
        }
        ShopKeeper.ShopPrice val=new ShopKeeper.ShopPrice();
        if(product==null)
            return val;
        int stockPrice=shop.getShop().stockPrice(product);
        if(stockPrice<=-100) return val;

        if(stockPrice>=0.0)
            val.absoluteGoldPrice=(double)stockPrice;
        else
            val.absoluteGoldPrice=rawSpecificGoldPrice(product,shop.getShop(),number);

        if(buyer==null) return val;

        double prejudiceFactor=prejudiceFactor(buyer,shop.finalPrejudiceFactors(),true);
        Room loc=CMLib.map().roomLocation(shop);
        prejudiceFactor*=itemPriceFactor(product,loc,shop.finalItemPricingAdjustments(),true);
        val.absoluteGoldPrice=CMath.mul(prejudiceFactor,val.absoluteGoldPrice);

        // gets the shopkeeper a deal on junk.  Pays 5% at 3 charisma, and 50% at 30
        double buyPrice=CMath.div(CMath.mul(val.absoluteGoldPrice,buyer.charStats().getStat(CharStats.STAT_CHARISMA)),60.0);
        if(!(product instanceof Ability))
            buyPrice=CMath.mul(buyPrice,1.0-devalue(shop,product));


        // the price is 200% at 0 charisma, and 100% at 30
        double sellPrice=sellingPrice(seller,buyer,product,shop,false).absoluteGoldPrice;

        if(buyPrice>sellPrice)
            val.absoluteGoldPrice=sellPrice;
        else
            val.absoluteGoldPrice=buyPrice;

        if(val.absoluteGoldPrice<=0.0)
            val.absoluteGoldPrice=1.0;
        return val;
    }


    public double getSalesTax(Room homeRoom, MOB seller)
    {
        if((seller==null)||(homeRoom==null)) return 0.0;
        Law theLaw=CMLib.law().getTheLaw(homeRoom,seller);
        if(theLaw!=null)
        {
            String taxs=(String)theLaw.taxLaws().get("SALESTAX");
            if(taxs!=null)
                return CMath.s_double(taxs);
        }
        return 0.0;

    }

    public boolean standardSellEvaluation(MOB seller,
                                          MOB buyer,
                                          Environmental product,
                                          ShopKeeper shop,
                                          double maxToPay,
                                          double maxEverPaid,
                                          boolean sellNotValue)
    {
        if((product!=null)
        &&(shop.doISellThis(product))
        &&(!(product instanceof Coins)))
        {
        	Room sellerR=seller.location();
            if(sellerR!=null)
            {
                int medianLevel=sellerR.getArea().getAreaIStats()[Area.AREASTAT_MEDLEVEL];
                if(medianLevel>0)
                {
                    String range=CMParms.getParmStr(shop.finalPrejudiceFactors(),"RANGE","0");
                    int rangeI=0;
                    if((range.endsWith("%"))&&(CMath.isInteger(range.substring(0,range.length()-1))))
                    {
                        rangeI=CMath.s_int(range.substring(0,range.length()-1));
                        rangeI=(int)Math.round(CMath.mul(medianLevel,CMath.div(rangeI,100.0)));
                    }
                    else
                    if(CMath.isInteger(range))
                        rangeI=CMath.s_int(range);
                    if((rangeI>0)
                    &&((product.envStats().level()>(medianLevel+rangeI))
                        ||(product.envStats().level()<(medianLevel-rangeI))))
                    {
                        CMLib.commands().postSay(seller,buyer,"I'm sorry, that's out of my level range.",true,false);
                        return false;
                    }
                }
            }
            double yourValue=pawningPrice(seller,buyer,product,shop).absoluteGoldPrice;
            if(yourValue<2)
            {
                CMLib.commands().postSay(seller,buyer,"I'm not interested.",true,false);
                return false;
            }
            if((sellNotValue)&&(yourValue>maxToPay))
            {
                if(yourValue>maxEverPaid)
                    CMLib.commands().postSay(seller,buyer,"That's way out of my price range! Try AUCTIONing it.",true,false);
                else
                    CMLib.commands().postSay(seller,buyer,"Sorry, I can't afford that right now.  Try back later.",true,false);
                return false;
            }
            if(product instanceof Ability)
            {
                CMLib.commands().postSay(seller,buyer,"I'm not interested.",true,false);
                return false;
            }
            if((product instanceof Container)&&(((Container)product).hasALock()))
            {
                boolean found=false;
                Vector V=((Container)product).getContents();
                for(int i=0;i<V.size();i++)
                {
                    Item I=(Item)V.elementAt(i);
                    if((I instanceof Key)
                    &&(((Key)I).getKey().equals(((Container)product).keyName())))
                        found=true;
                }
                if(!found)
                {
                    CMLib.commands().postSay(seller,buyer,"I won't buy that back unless you put the key in it.",true,false);
                    return false;
                }
            }
            if((product instanceof Item)&&(buyer.isMine(product)))
            {
                CMMsg msg2=CMClass.getMsg(buyer,product,CMMsg.MSG_DROP,null);
                if(!buyer.location().okMessage(buyer,msg2))
                    return false;
            }
            return true;
        }
        CMLib.commands().postSay(seller,buyer,"I'm sorry, I'm not buying those.",true,false);
        return false;
    }
    
    
    public boolean standardBuyEvaluation(MOB seller,
                                         MOB buyer,
                                         Environmental product,
                                         ShopKeeper shop,
                                         boolean buyNotView)
    {
        if((product!=null)
        &&(shop.getShop().doIHaveThisInStock("$"+product.Name()+"$",buyer)))
        {
            if(buyNotView)
            {
                ShopKeeper.ShopPrice price=sellingPrice(seller,buyer,product,shop,true);
                if((price.experiencePrice>0)&&(price.experiencePrice>buyer.getExperience()))
                {
                    CMLib.commands().postSay(seller,buyer,"You aren't experienced enough to buy "+product.name()+".",false,false);
                    return false;
                }
                if((price.questPointPrice>0)&&(price.questPointPrice>buyer.getQuestPoint()))
                {
                    CMLib.commands().postSay(seller,buyer,"You don't have enough quest points to buy "+product.name()+".",false,false);
                    return false;
                }
                if((price.absoluteGoldPrice>0.0)
                &&(price.absoluteGoldPrice>CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(buyer,seller)))
                {
                    CMLib.commands().postSay(seller,buyer,"You can't afford to buy "+product.name()+".",false,false);
                    return false;
                }
            }
            if(product instanceof Item)
            {
                if(((Item)product).envStats().level()>buyer.envStats().level())
                {
                    CMLib.commands().postSay(seller,buyer,"That's too advanced for you, I'm afraid.",true,false);
                    return false;
                }
            }
            if((product instanceof LandTitle)
            &&((shop.isSold(ShopKeeper.DEAL_CLANDSELLER))||(shop.isSold(ShopKeeper.DEAL_CSHIPSELLER))))
            {
                Clan C=null;
                if(buyer.getClanID().length()>0)C=CMLib.clans().getClan(buyer.getClanID());
                if(C==null)
                {
                    CMLib.commands().postSay(seller,buyer,"I only sell to clans.",true,false);
                    return false;
                }
                if((C.allowedToDoThis(buyer,Clan.FUNC_CLANPROPERTYOWNER)<0)&&(!buyer.isMonster()))
                {
                    CMLib.commands().postSay(seller,buyer,"You are not authorized by your clan to handle property.",true,false);
                    return false;
                }
            }
            if(product instanceof MOB)
            {
                if(buyer.totalFollowers()>=buyer.maxFollowers())
                {
                    CMLib.commands().postSay(seller,buyer,"You can't accept any more followers.",true,false);
                    return false;
                }
                if((CMProps.getIntVar(CMProps.SYSTEMI_FOLLOWLEVELDIFF)>0)
                &&(!CMSecurity.isAllowed(seller,seller.location(),"ORDER"))
                &&(!CMSecurity.isAllowed(buyer,buyer.location(),"ORDER")))
                {
                    if(seller.envStats().level() > (buyer.envStats().level() + CMProps.getIntVar(CMProps.SYSTEMI_FOLLOWLEVELDIFF)))
                    {
                        buyer.tell(product.name() + " is too advanced for you.");
                        return false;
                    }
                    if(seller.envStats().level() < (buyer.envStats().level() - CMProps.getIntVar(CMProps.SYSTEMI_FOLLOWLEVELDIFF)))
                    {
                        buyer.tell(product.name() + " is too inexperienced for you.");
                        return false;
                    }
                }
            }
            if(product instanceof Ability)
            {
                if(shop.isSold(ShopKeeper.DEAL_TRAINER))
                {
                    MOB teacher=CMClass.getMOB("Teacher");
                    Ability teachableA=getTrainableAbility(teacher, (Ability)product);
                    if((teachableA==null)||(!teachableA.canBeLearnedBy(teacher,buyer)))
                    {
                        teacher.destroy();
                        return false;
                    }
                    teacher.destroy();
                }
                else
                if(buyNotView)
                {
                    Ability A=(Ability)product;
                    if(A.canTarget(Ability.CAN_MOBS)){}
                    else
                    if(A.canTarget(Ability.CAN_ITEMS))
                    {
                        Item I=buyer.fetchWieldedItem();
                        if(I==null) I=buyer.fetchFirstWornItem(Wearable.WORN_HELD);
                        if(I==null)
                        {
                            CMLib.commands().postSay(seller,buyer,"You need to be wielding or holding the item you want this cast on.",true,false);
                            return false;
                        }
                    }
                    else
                    {
                        CMLib.commands().postSay(seller,buyer,"I don't know how to sell that spell.",true,false);
                        return false;
                    }
                }
            }
            return true;
        }
        CMLib.commands().postSay(seller,buyer,"I don't have that in stock.  Ask for my LIST.",true,false);
        return false;
    }

    public String getListInventory(MOB seller,
                                   MOB buyer,
                                   Vector rawInventory,
                                   int limit,
                                   ShopKeeper shop,
                                   String mask)
    {
        StringBuffer str=new StringBuffer("");
        int csize=0;
        Vector inventory=new Vector();
        Environmental E=null;
        for(int i=0;i<rawInventory.size();i++)
        {
            E=(Environmental)rawInventory.elementAt(i);
            if(shownInInventory(seller,buyer,E,shop)
            &&((mask==null)||(mask.length()==0)||(CMLib.english().containsString(E.name(),mask))))
            	inventory.addElement(E);
        }

        if(inventory.size()>0)
        {
            int totalCols=((shop.isSold(ShopKeeper.DEAL_LANDSELLER))
                           ||(shop.isSold(ShopKeeper.DEAL_CLANDSELLER))
                           ||(shop.isSold(ShopKeeper.DEAL_SHIPSELLER))
                           ||(shop.isSold(ShopKeeper.DEAL_CSHIPSELLER)))?1:2;
            int totalWidth=60/totalCols;
            String showPrice=null;
            ShopKeeper.ShopPrice price=null;
            for(int i=0;i<inventory.size();i++)
            {
                E=(Environmental)inventory.elementAt(i);
                price=sellingPrice(seller,buyer,E,shop,true);
                if((price.experiencePrice>0)&&(((""+price.experiencePrice).length()+2)>(4+csize)))
                    csize=(""+price.experiencePrice).length()-2;
                else
                if((price.questPointPrice>0)&&(((""+price.questPointPrice).length()+2)>(4+csize)))
                    csize=(""+price.questPointPrice).length()-2;
                else
                {
                    showPrice=CMLib.beanCounter().abbreviatedPrice(seller,price.absoluteGoldPrice);
                    if(showPrice.length()>(4+csize))
                        csize=showPrice.length()-4;
                }
            }

            String c="^x["+CMStrings.padRight("Cost",4+csize)+"] "+CMStrings.padRight("Product",totalWidth-csize);
            str.append(c+((totalCols>1)?c:"")+"^.^N^<!ENTITY shopkeeper \""+seller.name()+"\"^>\n\r");
            int colNum=0;
            int rowNum=0;
            String col=null;
            for(int i=0;i<inventory.size();i++)
            {
                E=(Environmental)inventory.elementAt(i);
                price=sellingPrice(seller,buyer,E,shop,true);
                col=null;
                if(price.questPointPrice>0)
                    col=CMStrings.padRight("["+price.questPointPrice+"qp",5+csize)+"] ^<SHOP^>"+CMStrings.padRight(E.name(),"^</SHOP^>",totalWidth-csize);
                else
                if(price.experiencePrice>0)
                    col=CMStrings.padRight("["+price.experiencePrice+"xp",5+csize)+"] ^<SHOP^>"+CMStrings.padRight(E.name(),"^</SHOP^>",totalWidth-csize);
                else
                    col=CMStrings.padRight("["+CMLib.beanCounter().abbreviatedPrice(seller,price.absoluteGoldPrice),5+csize)+"] ^<SHOP^>"+CMStrings.padRight(E.name(),"^</SHOP^>",totalWidth-csize);
                if((++colNum)>totalCols)
                {
                    str.append("\n\r");
                    rowNum++;
                    if((limit>0)&&(rowNum>limit))
                        break;
                    colNum=1;
                }
                str.append(col);
            }
        }
        if(str.length()==0)
        {
            if((!shop.isSold(ShopKeeper.DEAL_BANKER))
            &&(!shop.isSold(ShopKeeper.DEAL_CLANBANKER))
            &&(!shop.isSold(ShopKeeper.DEAL_CLANPOSTMAN))
            &&(!shop.isSold(ShopKeeper.DEAL_AUCTIONEER))
            &&(!shop.isSold(ShopKeeper.DEAL_POSTMAN)))
                return seller.name()+" has nothing for sale.";
            return "";
        }
        double salesTax=getSalesTax(seller.getStartRoom(),seller);
        return "\n\r"+str
                +((salesTax<=0.0)?"":"\n\r\n\rPrices above include a "+salesTax+"% sales tax.")
                +"^T";
    }

    public String findInnRoom(InnKey key, String addThis, Room R)
    {
        if(R==null) return null;
        String keyNum=key.getKey();
        for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
        {
            if((R.getExitInDir(d)!=null)&&(R.getExitInDir(d).keyName().equals(keyNum)))
            {
                if(addThis.length()>0)
                    return addThis+" and to the "+(Directions.getDirectionName(d).toLowerCase());
                return "to the "+(Directions.getDirectionName(d).toLowerCase());
            }
        }
        return null;
    }

    public MOB parseBuyingFor(MOB buyer, String message)
    {
        MOB mobFor=buyer;
        if((message!=null)&&(message.length()>0)&&(buyer.location()!=null))
        {
            Vector V=CMParms.parse(message);
            if(((String)V.elementAt(V.size()-2)).equalsIgnoreCase("for"))
            {
                String s=(String)V.lastElement();
                if(s.endsWith(".")) s=s.substring(0,s.length()-1);
                MOB M=buyer.location().fetchInhabitant("$"+s+"$");
                if(M!=null)
                    mobFor=M;
            }
        }
        return mobFor;
    }

    public double transactPawn(MOB shopkeeper,
                               MOB pawner,
                               ShopKeeper shop,
                               Environmental product)
    {
        Environmental coreSoldItem=product;
        Environmental rawSoldItem=product;
        int number=1;
        if(coreSoldItem instanceof PackagedItems)
        {
            coreSoldItem=((PackagedItems)rawSoldItem).getItem();
            number=((PackagedItems)rawSoldItem).numberOfItemsInPackage();
        }
        if((coreSoldItem!=null)&&(shop.doISellThis(coreSoldItem)))
        {
            double val=pawningPrice(shopkeeper,pawner,rawSoldItem,shop).absoluteGoldPrice;
            String currency=CMLib.beanCounter().getCurrency(shopkeeper);
            if(!(shopkeeper instanceof ShopKeeper))
                CMLib.beanCounter().subtractMoney(shopkeeper,currency,val);
            CMLib.beanCounter().giveSomeoneMoney(shopkeeper,pawner,currency,val);
            pawner.recoverEnvStats();
            pawner.tell(shopkeeper.name()+" pays you "+CMLib.beanCounter().nameCurrencyShort(shopkeeper,val)+" for "+rawSoldItem.name()+".");
            if(rawSoldItem instanceof Item)
            {
                Vector V=null;
                if(rawSoldItem instanceof Container)
                    V=((Container)rawSoldItem).getContents();
                ((Item)rawSoldItem).unWear();
                ((Item)rawSoldItem).removeFromOwnerContainer();
                if(V!=null)
                for(int v=0;v<V.size();v++)
                    ((Item)V.elementAt(v)).removeFromOwnerContainer();
                shop.getShop().addStoreInventory(coreSoldItem,number,-1);
                if(V!=null)
                for(int v=0;v<V.size();v++)
                {
                    Item item2=(Item)V.elementAt(v);
                    if(!shop.doISellThis(item2)||(item2 instanceof Key))
                        item2.destroy();
                    else
                        shop.getShop().addStoreInventory(item2,1,-1);
                }
            }
            else
            if(product instanceof MOB)
            {
                MOB newMOB=(MOB)product.copyOf();
                newMOB.setStartRoom(null);
                Ability A=newMOB.fetchEffect("Skill_Enslave");
                if(A!=null) A.setMiscText("");
                newMOB.setLiegeID("");
                newMOB.setClanID("");
                shop.getShop().addStoreInventory(newMOB);
                ((MOB)product).setFollowing(null);
                if((((MOB)product).baseEnvStats().rejuv()>0)
                &&(((MOB)product).baseEnvStats().rejuv()<Integer.MAX_VALUE)
                &&(((MOB)product).getStartRoom()!=null))
                    ((MOB)product).killMeDead(false);
                else
                    ((MOB)product).destroy();
            }
            else
            if(product instanceof Ability)
            {

            }
            return val;
        }
        return Double.MIN_VALUE;
    }

    public void transactMoneyOnly(MOB seller,
                                  MOB buyer,
                                  ShopKeeper shop,
                                  Environmental product,
                                  boolean sellerGetsPaid)
    {
        if((seller==null)||(seller.location()==null)||(buyer==null)||(shop==null)||(product==null))
            return;
        Room room=seller.location();
        ShopKeeper.ShopPrice price=sellingPrice(seller,buyer,product,shop,true);
        if(price.absoluteGoldPrice>0.0)
        {
            CMLib.beanCounter().subtractMoney(buyer,CMLib.beanCounter().getCurrency(seller),price.absoluteGoldPrice);
            double totalFunds=price.absoluteGoldPrice;
            if(getSalesTax(seller.getStartRoom(),seller)!=0.0)
            {
                Law theLaw=CMLib.law().getTheLaw(room,seller);
                Area A2=CMLib.law().getLegalObject(room);
                if((theLaw!=null)&&(A2!=null))
                {
                    Environmental[] Treas=theLaw.getTreasuryNSafe(A2);
                    Room treasuryR=(Room)Treas[0];
                    Item treasuryItem=(Item)Treas[1];
                    if(treasuryR!=null)
                    {
                        double taxAmount=totalFunds-sellingPrice(seller,buyer,product,shop,false).absoluteGoldPrice;
                        totalFunds-=taxAmount;
                        Coins COIN=CMLib.beanCounter().makeBestCurrency(CMLib.beanCounter().getCurrency(seller),taxAmount,treasuryR,treasuryItem);
                        if(COIN!=null) COIN.putCoinsBack();
                    }
                }
            }
            if(seller.isMonster())
            {
                LandTitle T=CMLib.law().getLandTitle(seller.getStartRoom());
                if((T!=null)&&(T.landOwner().length()>0))
                {
                    CMLib.beanCounter().modifyLocalBankGold(seller.getStartRoom().getArea(),
                                                    T.landOwner(),
                                                    CMLib.utensils().getFormattedDate(buyer)+": Deposit of "+CMLib.beanCounter().nameCurrencyShort(seller,totalFunds)+": Purchase: "+product.Name()+" from "+seller.Name(),
                                                    CMLib.beanCounter().getCurrency(seller),
                                                    totalFunds);
                }
            }
            if(sellerGetsPaid)
                CMLib.beanCounter().giveSomeoneMoney(seller,seller,CMLib.beanCounter().getCurrency(seller),totalFunds);
        }
        if(price.questPointPrice>0) buyer.setQuestPoint(buyer.getQuestPoint()-price.questPointPrice);
        if(price.experiencePrice>0) CMLib.leveler().postExperience(buyer,null,null,-price.experiencePrice,false);
        buyer.recoverEnvStats();
    }

    public boolean purchaseItems(Item baseProduct,
                                 Vector products,
                                 MOB seller,
                                 MOB mobFor)
    {
        if((seller==null)||(seller.location()==null)||(mobFor==null))
            return false;
        Room room=seller.location();
        for(int p=0;p<products.size();p++)
            room.addItemRefuse((Item)products.elementAt(p),CMProps.getIntVar(CMProps.SYSTEMI_EXPIRE_PLAYER_DROP));
        CMMsg msg2=CMClass.getMsg(mobFor,baseProduct,seller,CMMsg.MSG_GET,null);
        if((baseProduct instanceof LandTitle)||(room.okMessage(mobFor,msg2)))
        {
            room.send(mobFor,msg2);
            if(baseProduct instanceof InnKey)
            {
                InnKey item =(InnKey)baseProduct;
                String buf=findInnRoom(item, "", room);
                if(buf==null) buf=findInnRoom(item, "upstairs", room.getRoomInDir(Directions.UP));
                if(buf==null) buf=findInnRoom(item, "downstairs", room.getRoomInDir(Directions.DOWN));
                if(buf!=null) CMLib.commands().postSay(seller,mobFor,"Your room is "+buf+".",true,false);
            }
            return true;
        }
        return false;
    }

    public boolean purchaseMOB(MOB product,
                               MOB seller,
                               ShopKeeper shop,
                               MOB mobFor)
    {
        if((seller==null)||(seller.location()==null)||(product==null)||(shop==null)||(mobFor==null))
            return false;
        product.baseEnvStats().setRejuv(Integer.MAX_VALUE);
        product.recoverEnvStats();
        product.setMiscText(product.text());
        Ability slaveA=null;
        if(shop.isSold(ShopKeeper.DEAL_SLAVES))
        {
            slaveA=product.fetchEffect("Skill_Enslave");
            if(slaveA!=null) slaveA.setMiscText("");
            else
            if(!CMLib.flags().isAnimalIntelligence(product))
            {
                slaveA=CMClass.getAbility("Skill_Enslave");
                if(slaveA!=null)
                    product.addNonUninvokableEffect(slaveA);
            }
        }
        product.bringToLife(seller.location(),true);
        if(slaveA!=null)
        {
            product.setLiegeID("");
            product.setClanID("");
            product.setStartRoom(null);
            slaveA.setMiscText(mobFor.Name());
            product.text();
        }
        CMLib.commands().postFollow(product,mobFor,false);
        if(product.amFollowing()==null)
        {
            mobFor.tell("You cannot accept seem to accept this follower!");
            return false;
        }
        return true;
    }

    public void purchaseAbility(Ability A,
                                MOB seller,
                                ShopKeeper shop,
                                MOB mobFor)
    {
        if((seller==null)||(seller.location()==null)||(A==null)||(shop==null)||(mobFor==null))
            return ;
        Room room=seller.location();
        if(shop.isSold(ShopKeeper.DEAL_TRAINER))
        {
            MOB teacher=CMClass.getMOB("Teacher");
            Ability teachableA=getTrainableAbility(teacher,A);
            if(teachableA!=null)
            	teachableA.teach(teacher,mobFor);
            teacher.destroy();
        }
        else
        {
            if(seller.isMonster())
            {
                seller.curState().setMana(seller.maxState().getMana());
                seller.curState().setMovement(seller.maxState().getMovement());
            }
            Object[][] victims=new Object[room.numInhabitants()][2];
            for(int x=0;x>victims.length;x++)
            { // save victim status
                MOB M=room.fetchInhabitant(x);
                if(M!=null){ victims[x][0]=M;victims[x][1]=M.getVictim();}
            }
            Vector V=new Vector();
            if(A.canTarget(Ability.CAN_MOBS))
            {
                V.addElement("$"+mobFor.name()+"$");
                A.invoke(seller,V,mobFor,true,0);
            }
            else
            if(A.canTarget(Ability.CAN_ITEMS))
            {
                Item I=mobFor.fetchWieldedItem();
                if(I==null) I=mobFor.fetchFirstWornItem(Wearable.WORN_HELD);
                if(I==null) I=mobFor.fetchWornItem("all");
                if(I==null) I=mobFor.fetchCarried(null,"all");
                if(I!=null)
                {
                    V.addElement("$"+I.name()+"$");
                    seller.addInventory(I);
                    A.invoke(seller,V,I,true,0);
                    seller.delInventory(I);
                    if(!mobFor.isMine(I)) mobFor.addInventory(I);
                }
            }
            if(seller.isMonster())
            {
                seller.curState().setMana(seller.maxState().getMana());
                seller.curState().setMovement(seller.maxState().getMovement());
            }
            for(int x=0;x>victims.length;x++)
                ((MOB)victims[x][0]).setVictim((MOB)victims[x][1]);
        }
    }

    public Vector addRealEstateTitles(Vector V, MOB buyer, CoffeeShop shop, Room myRoom)
    {
        if((myRoom==null)||(buyer==null)) return V;
        Area myArea=myRoom.getArea();
        if(((shop.isSold(ShopKeeper.DEAL_LANDSELLER))
            ||(shop.isSold(ShopKeeper.DEAL_SHIPSELLER))
            ||((shop.isSold(ShopKeeper.DEAL_CSHIPSELLER))&&(buyer.getClanID().length()>0))
            ||((shop.isSold(ShopKeeper.DEAL_CLANDSELLER))&&(buyer.getClanID().length()>0)))
        &&(myArea!=null))
        {
            String name=buyer.Name();
            if((shop.isSold(ShopKeeper.DEAL_CLANDSELLER))||(shop.isSold(ShopKeeper.DEAL_CSHIPSELLER)))
                name=buyer.getClanID();
            HashSet roomsHandling=new HashSet();
            Hashtable titles=new Hashtable();
            if((shop.isSold(ShopKeeper.DEAL_CSHIPSELLER))||(shop.isSold(ShopKeeper.DEAL_SHIPSELLER)))
            {
                for(Enumeration a=CMLib.map().areas();a.hasMoreElements();)
                {
                    Area A=(Area)a.nextElement();
                    if((A instanceof SpaceShip)
                    &&(CMLib.flags().isHidden(A)))
                    {
                        boolean related=myArea.isChild(A)||A.isParent(myArea);
                        if(!related)
                            for(int p=0;p<myArea.getNumParents();p++)
                            {
                                Area P=myArea.getParent(p);
                                if((P!=null)&&(P!=myArea)&&((P==A)||(A.isParent(P))||(P.isChild(A))))
                                { related=true; break;}
                            }
                        if(related)
                        {
                            LandTitle LT=CMLib.law().getLandTitle(A);
                            if(LT!=null) titles.put(A,LT);
                        }
                    }
                }
            }
            else
            for(Enumeration r=myArea.getProperMap();r.hasMoreElements();)
            {
                Room R=(Room)r.nextElement();
                LandTitle A=CMLib.law().getLandTitle(R);
                if((A!=null)&&(R.roomID().length()>0))
                    titles.put(R,A);
            }

            for(Enumeration r=titles.keys();r.hasMoreElements();)
            {
                Environmental R=(Environmental)r.nextElement();
                LandTitle A=(LandTitle)titles.get(R);
                if(!roomsHandling.contains(R))
                {
                    if(R instanceof Area)
                        roomsHandling.add(R);
                    else
                    {
                        Vector V2=A.getPropertyRooms();
                        for(int v=0;v<V2.size();v++)
                            roomsHandling.add(V2.elementAt(v));
                    }
                    if((A.landOwner().length()>0)
                    &&(!A.landOwner().equals(name))
                    &&((!A.landOwner().equals(buyer.getLiegeID()))||(!buyer.isMarriedToLiege())))
                        continue;
                    boolean skipThisOne=false;
                    if(R instanceof Room)
                        for(int d=0;d<4;d++)
                        {
                            Room R2=((Room)R).getRoomInDir(d);
                            LandTitle L2=null;
                            if(R2!=null)
                            {
                                L2=(LandTitle)titles.get(R2);
                                if(L2==null)
                                { skipThisOne=false; break;}
                            }
                            else
                                continue;
                            if((L2.landOwner().equals(name))
                            ||(L2.landOwner().equals(buyer.getLiegeID())&&(buyer.isMarriedToLiege())))
                            { skipThisOne=false; break;}
                            if(L2.landOwner().length()>0)
                                skipThisOne=true;
                        }
                    if(skipThisOne)
                        continue;
                    Item I=CMClass.getItem("GenTitle");
                    if(R instanceof Room)
                        ((LandTitle)I).setLandPropertyID(CMLib.map().getExtendedRoomID((Room)R));
                    else
                        ((LandTitle)I).setLandPropertyID(R.Name());
                    if((((LandTitle)I).landOwner().length()>0)
                    &&(!I.Name().endsWith(" (Copy)")))
                        I.setName(I.Name()+" (Copy)");
                    I.text();
                    I.recoverEnvStats();
                    if((A.landOwner().length()==0)
                    &&(I.Name().endsWith(" (Copy)")))
                        I.setName(I.Name().substring(0,I.Name().length()-7));
                    V.addElement(I);
                }
            }
        }
        if(V.size()<2) return V;
        Vector V2=new Vector(V.size());
        LandTitle L=null;
        LandTitle L2=null;
        int x=-1;
        int x2=-1;
        while(V.size()>0)
        {
            if(((!(V.elementAt(0) instanceof LandTitle)))
            ||((x=(L=(LandTitle)V.elementAt(0)).landPropertyID().lastIndexOf('#'))<0))
            {
                if(V2.size()==0)
                    V2.addElement(V.remove(0));
                else
                    V2.insertElementAt(V.remove(0),0);
            }
            else
            {

                int lowest=CMath.s_int(L.landPropertyID().substring(x+1).trim());
                for(int v=1;v<V.size();v++)
                    if(V.elementAt(v) instanceof LandTitle)
                    {
                        L2=(LandTitle)V.elementAt(v);
                        x2=L2.landPropertyID().lastIndexOf('#');
                        if((x2>0)&&(CMath.s_int(L2.landPropertyID().substring(x+1).trim())<lowest))
                        {
                            lowest=CMath.s_int(L2.landPropertyID().substring(x+1).trim());
                            L=L2;
                        }
                    }
                V.removeElement(L);
                V2.addElement(L);
            }
        }
        return V2;
    }

    public boolean ignoreIfNecessary(MOB mob, String ignoreMask, MOB whoIgnores)
    {
        if((ignoreMask.length()>0)&&(!CMLib.masking().maskCheck(ignoreMask,mob,false)))
        {
            mob.tell(whoIgnores,null,null,"<S-NAME> appear(s) to be ignoring you.");
            return false;
        }
        return true;
    }


    public String storeKeeperString(CoffeeShop shop)
    {
    	if(shop==null) return "";
    	if(shop.isSold(ShopKeeper.DEAL_ANYTHING))
    		return "*Anything*";
    	
    	Vector V=new Vector();
    	for(int d=1;d<ShopKeeper.DEAL_DESCS.length;d++)
		if(shop.isSold(d))
        switch(d)
        {
        case ShopKeeper.DEAL_GENERAL:
            V.addElement("General items"); break;
        case ShopKeeper.DEAL_ARMOR:
            V.addElement("Armor"); break;
        case ShopKeeper.DEAL_MAGIC:
            V.addElement("Miscellaneous Magic Items"); break;
        case ShopKeeper.DEAL_WEAPONS:
            V.addElement("Weapons"); break;
        case ShopKeeper.DEAL_PETS:
            V.addElement("Pets and Animals"); break;
        case ShopKeeper.DEAL_LEATHER:
            V.addElement("Leather"); break;
        case ShopKeeper.DEAL_INVENTORYONLY:
            V.addElement("Only my Inventory"); break;
        case ShopKeeper.DEAL_TRAINER:
            V.addElement("Training in skills/spells/prayers/songs"); break;
        case ShopKeeper.DEAL_CASTER:
            V.addElement("Caster of spells/prayers"); break;
        case ShopKeeper.DEAL_ALCHEMIST:
            V.addElement("Potions"); break;
        case ShopKeeper.DEAL_INNKEEPER:
            V.addElement("My services as an Inn Keeper"); break;
        case ShopKeeper.DEAL_JEWELLER:
            V.addElement("Precious stones and jewellery"); break;
        case ShopKeeper.DEAL_BANKER:
            V.addElement("My services as a Banker"); break;
        case ShopKeeper.DEAL_CLANBANKER:
            V.addElement("My services as a Banker to Clans"); break;
        case ShopKeeper.DEAL_LANDSELLER:
            V.addElement("Real estate"); break;
        case ShopKeeper.DEAL_CLANDSELLER:
            V.addElement("Clan estates"); break;
        case ShopKeeper.DEAL_ANYTECHNOLOGY:
            V.addElement("Any technology"); break;
        case ShopKeeper.DEAL_BUTCHER:
            V.addElement("Meats"); break;
        case ShopKeeper.DEAL_FOODSELLER:
            V.addElement("Foodstuff"); break;
        case ShopKeeper.DEAL_GROWER:
            V.addElement("Vegetables"); break;
        case ShopKeeper.DEAL_HIDESELLER:
            V.addElement("Hides and Furs"); break;
        case ShopKeeper.DEAL_LUMBERER:
            V.addElement("Lumber"); break;
        case ShopKeeper.DEAL_METALSMITH:
            V.addElement("Metal ores"); break;
        case ShopKeeper.DEAL_STONEYARDER:
            V.addElement("Stone and rock"); break;
        case ShopKeeper.DEAL_SHIPSELLER:
            V.addElement("Ships"); break;
        case ShopKeeper.DEAL_CSHIPSELLER:
            V.addElement("Clan Ships"); break;
        case ShopKeeper.DEAL_SLAVES:
            V.addElement("Slaves"); break;
        case ShopKeeper.DEAL_POSTMAN:
            V.addElement("My services as a Postman"); break;
        case ShopKeeper.DEAL_CLANPOSTMAN:
            V.addElement("My services as a Postman for Clans"); break;
        case ShopKeeper.DEAL_AUCTIONEER:
            V.addElement("My services as an Auctioneer"); break;
        default:
            V.addElement("... I have no idea WHAT I sell"); break;
        }
    	return CMParms.toStringList(V);
    }

    protected boolean shopKeeperItemTypeCheck(Environmental E, int dealCode, ShopKeeper shopKeeper)
    {
        switch(dealCode)
        {
        case ShopKeeper.DEAL_ANYTHING:
            return !(E instanceof LandTitle);
        case ShopKeeper.DEAL_ARMOR:
            return (E instanceof Armor);
        case ShopKeeper.DEAL_MAGIC:
            return (E instanceof MiscMagic);
        case ShopKeeper.DEAL_WEAPONS:
            return (E instanceof Weapon)||(E instanceof Ammunition);
        case ShopKeeper.DEAL_GENERAL:
            return ((E instanceof Item)
                    &&(!(E instanceof Armor))
                    &&(!(E instanceof MiscMagic))
                    &&(!(E instanceof ClanItem))
                    &&(!(E instanceof Weapon))
                    &&(!(E instanceof Ammunition))
                    &&(!(E instanceof MOB))
                    &&(!(E instanceof LandTitle))
                    &&(!(E instanceof RawMaterial))
                    &&(!(E instanceof Ability)));
        case ShopKeeper.DEAL_LEATHER:
            return ((E instanceof Item)
                    &&((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_LEATHER)
                    &&(!(E instanceof RawMaterial)));
        case ShopKeeper.DEAL_PETS:
            return ((E instanceof MOB)&&(CMLib.flags().isAnimalIntelligence((MOB)E)));
        case ShopKeeper.DEAL_SLAVES:
            return ((E instanceof MOB)&&(!CMLib.flags().isAnimalIntelligence((MOB)E)));
        case ShopKeeper.DEAL_INVENTORYONLY:
            return (shopKeeper.getShop().inBaseInventory(E));
        case ShopKeeper.DEAL_INNKEEPER:
            return E instanceof InnKey;
        case ShopKeeper.DEAL_JEWELLER:
            return ((E instanceof Item)
                    &&(!(E instanceof Weapon))
                    &&(!(E instanceof MiscMagic))
                    &&(!(E instanceof ClanItem))
                    &&(((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_GLASS)
                    ||((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PRECIOUS)
                    ||((Item)E).fitsOn(Wearable.WORN_EARS)
                    ||((Item)E).fitsOn(Wearable.WORN_NECK)
                    ||((Item)E).fitsOn(Wearable.WORN_RIGHT_FINGER)
                    ||((Item)E).fitsOn(Wearable.WORN_LEFT_FINGER)));
        case ShopKeeper.DEAL_ALCHEMIST:
            return (E instanceof Potion);
        case ShopKeeper.DEAL_LANDSELLER:
        case ShopKeeper.DEAL_CLANDSELLER:
        case ShopKeeper.DEAL_SHIPSELLER:
        case ShopKeeper.DEAL_CSHIPSELLER:
            return (E instanceof LandTitle);
        case ShopKeeper.DEAL_ANYTECHNOLOGY:
            return (E instanceof Electronics);
        case ShopKeeper.DEAL_BUTCHER:
            return ((E instanceof RawMaterial)
                &&(((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_FLESH);
        case ShopKeeper.DEAL_FOODSELLER:
            return (((E instanceof Food)||(E instanceof Drink))
                    &&(!(E instanceof RawMaterial)));
        case ShopKeeper.DEAL_GROWER:
            return ((E instanceof RawMaterial)
                &&(((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_VEGETATION);
        case ShopKeeper.DEAL_HIDESELLER:
            return ((E instanceof RawMaterial)
                &&((((RawMaterial)E).material()==RawMaterial.RESOURCE_HIDE)
                ||(((RawMaterial)E).material()==RawMaterial.RESOURCE_FEATHERS)
                ||(((RawMaterial)E).material()==RawMaterial.RESOURCE_LEATHER)
                ||(((RawMaterial)E).material()==RawMaterial.RESOURCE_SCALES)
                ||(((RawMaterial)E).material()==RawMaterial.RESOURCE_WOOL)
                ||(((RawMaterial)E).material()==RawMaterial.RESOURCE_FUR)));
        case ShopKeeper.DEAL_LUMBERER:
            return ((E instanceof RawMaterial)
                &&((((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_WOODEN));
        case ShopKeeper.DEAL_METALSMITH:
            return ((E instanceof RawMaterial)
                &&(((((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_METAL)
                ||(((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_MITHRIL));
        case ShopKeeper.DEAL_STONEYARDER:
            return ((E instanceof RawMaterial)
                &&((((RawMaterial)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_ROCK));
        }
        return false;
    }
    
    public boolean doISellThis(Environmental thisThang, ShopKeeper shop)
    {
        if(thisThang instanceof PackagedItems)
            thisThang=((PackagedItems)thisThang).getItem();
        if(thisThang==null) return false;
        if((thisThang instanceof Coins)
        ||(thisThang instanceof DeadBody)
        ||(CMLib.flags().isChild(thisThang)))
            return false;
        if(shop.isSold(ShopKeeper.DEAL_ANYTHING))
            return !(thisThang instanceof LandTitle);
        else
        for(int d=1;d<ShopKeeper.DEAL_DESCS.length;d++)
        	if(shop.isSold(d) && shopKeeperItemTypeCheck(thisThang,d,shop))
        		return true;
        return false;
    }

	public void returnMoney(MOB to, String currency, double amt)
	{
		if(amt>0)
			CMLib.beanCounter().giveSomeoneMoney(to, currency, amt);
		else
			CMLib.beanCounter().subtractMoney(to, currency,-amt);
		if(amt!=0)
			if(!CMLib.flags().isInTheGame(to,true))
				CMLib.database().DBUpdatePlayerItems(to);
	}

	public String[] bid(MOB mob, double bid, String bidCurrency, Auctioneer.AuctionData auctionData, Item I, Vector auctionAnnounces)
	{
		String bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.currency,auctionData.bid);
		String currencyName=CMLib.beanCounter().getDenominationName(auctionData.currency);
		if(bid==0.0)
			return new String[]{"Up for auction: "+I.name()+".  The current bid is "+bidWords+".",null};

		if(!bidCurrency.equals(auctionData.currency))
		    return new String[]{"This auction is being bid in "+currencyName+" only.",null};

		if(bid>CMLib.beanCounter().getTotalAbsoluteValue(mob,auctionData.currency))
			return new String[]{"You don't have enough "+currencyName+" on hand to cover that bid.",null};

        if((bid<auctionData.bid)||(bid==0))
        {
    		String bwords=CMLib.beanCounter().nameCurrencyShort(bidCurrency, bid);
            return new String[]{"Your bid of "+bwords+" is insufficient."+((auctionData.bid>0)?" The current high bid is "+bidWords+".":""),null};
        }
        else
		if((bid>auctionData.highBid)||((bid>auctionData.bid)&&(auctionData.highBid==0)))
		{
            MOB oldHighBider=auctionData.highBidderM;
            if(auctionData.highBidderM!=null)
                returnMoney(auctionData.highBidderM,auctionData.currency,auctionData.highBid);
			auctionData.highBidderM=mob;
			if(auctionData.highBid<=0.0)
            {
                if(auctionData.bid>0)
                    auctionData.highBid=auctionData.bid;
                else
                    auctionData.highBid=0.0;
            }
			auctionData.bid=auctionData.highBid+1.0;
			auctionData.highBid=bid;
            returnMoney(auctionData.highBidderM,auctionData.currency,-bid);
			bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.currency,auctionData.bid);
			String yourBidWords = CMLib.beanCounter().abbreviatedPrice(currencyName, auctionData.highBid);
			auctionAnnounces.addElement("A new bid has been entered for "+I.name()+". The current high bid is "+bidWords+".");
			if((oldHighBider!=null)&&(oldHighBider==mob))
				return new String[]{"You have submitted a new high bid of "+yourBidWords+" for "+I.name()+".",null};
			else
			if((oldHighBider!=null)&&(oldHighBider!=mob))
				return new String[]{"You have the new high reserve bid of "+yourBidWords+" for "+I.name()+". The current nominal high bid is "+bidWords+".","You have been outbid for "+I.name()+"."};
			else
				return new String[]{"You have submitted a bid of "+yourBidWords+" for "+I.name()+".",null};
		}
		else
		if((bid==auctionData.bid)&&(auctionData.highBidderM!=null))
		{
			return new String[]{"You must bid higher than "+bidWords+" to have your bid accepted.",null};
		}
		else
		if((bid==auctionData.highBid)&&(auctionData.highBidderM!=null))
		{
			if((auctionData.highBidderM!=null)&&(auctionData.highBidderM!=mob))
			{
				auctionData.bid=bid;
				bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.currency,auctionData.bid);
				auctionAnnounces.addElement("A new bid has been entered for "+I.name()+". The current bid is "+bidWords+".");
				return new String[]{"You have been outbid by proxy for "+I.name()+".","Your high bid for "+I.name()+" has been reached."};
			}
		}
		else
		{
			auctionData.bid=bid;
			bidWords=CMLib.beanCounter().nameCurrencyShort(auctionData.currency,auctionData.bid);
			auctionAnnounces.addElement("A new bid has been entered for "+I.name()+". The current bid is "+bidWords+".");
			return new String[]{"You have been outbid by proxy for "+I.name()+".",null};
		}
		return null;
	}

	public Auctioneer.AuctionData getEnumeratedAuction(String named, String auctionHouse)
	{
		Vector V=getAuctions(null,auctionHouse);
		Vector V2=new Vector();
		for(int v=0;v<V.size();v++)
			V2.addElement(((Auctioneer.AuctionData)V.elementAt(v)).auctioningI);
		Environmental E=CMLib.english().fetchEnvironmental(V2,named,true);
		if(!(E instanceof Item)) E=CMLib.english().fetchEnvironmental(V2,named,false);
		if(E!=null)
		for(int v=0;v<V.size();v++)
			if(((Auctioneer.AuctionData)V.elementAt(v)).auctioningI==E)
				return (Auctioneer.AuctionData)V.elementAt(v);
		return null;
	}

	public void saveAuction(Auctioneer.AuctionData data, String auctionHouse, boolean updateOnly)
	{
		if(data.auctioningI instanceof Container) ((Container)data.auctioningI).emptyPlease();
        StringBuffer xml=new StringBuffer("<AUCTION>");
        xml.append("<PRICE>"+data.bid+"</PRICE>");
        xml.append("<BUYOUT>"+data.buyOutPrice+"</BUYOUT>");
        if(data.highBidderM!=null)
        	xml.append("<BIDDER>"+data.highBidderM.Name()+"</BIDDER>");
        else
        	xml.append("<BIDDER />");
        xml.append("<MAXBID>"+data.highBid+"</MAXBID>");
        xml.append("<AUCTIONITEM>");
        xml.append(CMLib.coffeeMaker().getItemXML(data.auctioningI).toString());
        xml.append("</AUCTIONITEM>");
        xml.append("</AUCTION>");
        if(!updateOnly)
			CMLib.database().DBWriteJournal("SYSTEM_AUCTIONS_"+auctionHouse.toUpperCase().trim(),
											data.auctioningM.Name(),
											""+data.tickDown,
											CMStrings.limit(data.auctioningI.name(),38),
											xml.toString());
        else
        	CMLib.database().DBUpdateJournal(data.auctionDBKey, data.auctioningI.Name(),xml.toString(), 0);
	}

    public Vector getAuctions(Object ofLike, String auctionHouse)
    {
    	Vector auctions=new Vector();
    	String house="SYSTEM_AUCTIONS_"+auctionHouse.toUpperCase().trim();
	    Vector otherAuctions=CMLib.database().DBReadJournalMsgs(house);
	    for(int o=0;o<otherAuctions.size();o++)
	    {
	    	JournalsLibrary.JournalEntry auctionData=(JournalsLibrary.JournalEntry)otherAuctions.elementAt(o);
            String from=(String)auctionData.from;
	        String to=(String)auctionData.to;
            String key=(String)auctionData.key;
	        if((ofLike instanceof MOB)&&(!((MOB)ofLike).Name().equals(to)))
	        	continue;
	        if((ofLike instanceof String)&&(!((String)ofLike).equals(key)))
	        	continue;
            AuctionData data=new AuctionData();
            data.start=auctionData.date;
            data.tickDown=CMath.s_long(to);
            String xml=(String)auctionData.msg;
            Vector xmlV=CMLib.xml().parseAllXML(xml);
            xmlV=CMLib.xml().getContentsFromPieces(xmlV,"AUCTION");
            String bid=CMLib.xml().getValFromPieces(xmlV,"PRICE");
            double oldBid=CMath.s_double(bid);
            data.bid=oldBid;
            String highBidder=CMLib.xml().getValFromPieces(xmlV,"BIDDER");
            if(highBidder.length()>0)
                data.highBidderM=CMLib.players().getLoadPlayer(highBidder);
            String maxBid=CMLib.xml().getValFromPieces(xmlV,"MAXBID");
            double oldMaxBid=CMath.s_double(maxBid);
            data.highBid=oldMaxBid;
            data.auctionDBKey=key;
            String buyOutPrice=CMLib.xml().getValFromPieces(xmlV,"BUYOUT");
            data.buyOutPrice=CMath.s_double(buyOutPrice);
            data.auctioningM=CMLib.players().getLoadPlayer(from);
            data.currency=CMLib.beanCounter().getCurrency(data.auctioningM);
            for(int v=0;v<xmlV.size();v++)
            {
                XMLLibrary.XMLpiece X=(XMLLibrary.XMLpiece)xmlV.elementAt(v);
                if(X.tag.equalsIgnoreCase("AUCTIONITEM"))
                {
                    data.auctioningI=CMLib.coffeeMaker().getItemFromXML(X.value);
                    break;
                }
            }
            if((ofLike instanceof Item)&&(!((Item)ofLike).sameAs(data.auctioningI)))
                continue;
            auctions.addElement(data);
	    }
	    return auctions;
    }

    public String getListForMask(String targetMessage)
    {
    	if(targetMessage==null) return null;
		int x=targetMessage.toUpperCase().lastIndexOf("FOR '");
		if(x>0)
		{
			int y=targetMessage.lastIndexOf("'");
			if(y>x)
				return targetMessage.substring(x+5,y);
		}
		return null;
    }

    public String getAuctionInventory(MOB seller,
    								  MOB buyer,
    								  Auctioneer auction,
    								  String mask)
    {
        StringBuffer str=new StringBuffer("");
        str.append("^x"+CMStrings.padRight("Lvl",3)+" "+CMStrings.padRight("Item",50)+" "+CMStrings.padRight("Days",4)+" ["+CMStrings.padRight("Bid",6)+"] Buy^.^N\n\r");
        Vector auctions=getAuctions(null,auction.auctionHouse());
        for(int v=0;v<auctions.size();v++)
        {
        	Auctioneer.AuctionData data=(Auctioneer.AuctionData)auctions.elementAt(v);
	        if(shownInInventory(seller,buyer,data.auctioningI,auction))
	        {
	        	if(((mask==null)||(mask.length()==0)||(CMLib.english().containsString(data.auctioningI.name(),mask)))
	        	&&((data.tickDown>System.currentTimeMillis())||(data.auctioningM==buyer)||(data.highBidderM==buyer)))
	        	{
		            Area area=CMLib.map().getStartArea(seller);
		            if(area==null) area=CMLib.map().getStartArea(buyer);
		        	str.append(CMStrings.padRight(""+data.auctioningI.envStats().level(),3)+" ");
		        	str.append(CMStrings.padRight(data.auctioningI.name(),50)+" ");
		        	if(data.tickDown>System.currentTimeMillis())
		        	{
			        	long days=data.daysRemaining(buyer,seller);
			        	str.append(CMStrings.padRight(""+days,4)+" ");
		        	}
		        	else
		        	if(data.auctioningM==buyer)
		        		str.append("DONE ");
		        	else
		        		str.append("WON! ");
		        	str.append("["+CMStrings.padRight(CMLib.beanCounter().abbreviatedPrice(seller,data.bid),6)+"] ");
		        	if(data.buyOutPrice<=0.0)
			        	str.append(CMStrings.padRight("-",6));
		        	else
			        	str.append(CMStrings.padRight(CMLib.beanCounter().abbreviatedPrice(seller,data.buyOutPrice),6));
		        	str.append("\n\r");
	        	}
	        }
        }
        return "\n\r"+str.toString();
    }

	public void auctionNotify(MOB M, String resp, String regardingItem)
	{
		try{
	    	if(CMLib.flags().isInTheGame(M,true))
	    		M.tell(resp);
	    	else
	    	if(M.playerStats()!=null)
	    	{
	            CMLib.smtp().emailIfPossible(CMProps.getVar(CMProps.SYSTEM_SMTPSERVERNAME),
					                            "auction@"+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase(),
					                            "noreply@"+CMProps.getVar(CMProps.SYSTEM_MUDDOMAIN).toLowerCase(),
					                            M.playerStats().getEmail(),
					                            "Auction Update for item: "+regardingItem,
					                            resp);
	    	}
		}catch(Exception e){}
	}

    public void cancelAuction(String auctionHouse, Auctioneer.AuctionData data)
    {
    	data.auctioningM.giveItem(data.auctioningI);
    	if(data.highBidderM!=null)
    	{
    		MOB M=data.highBidderM;
	    	auctionNotify(M,"The auction for "+data.auctioningI.Name()+" was closed early.  You have been refunded your max bid.",data.auctioningI.Name());
	    	CMLib.coffeeShops().returnMoney(M,data.currency,data.highBid);
        }
    	CMLib.database().DBDeleteJournal(auctionHouse, data.auctionDBKey);
        data.auctioningM.tell("Auction ended.");
    }
}
