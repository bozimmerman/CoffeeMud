package com.planet_ink.coffee_mud.core;
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
public class CoffeeShops implements Cloneable
{
    private CoffeeShops(){super();}
    
    public static ShopKeeper getShopKeeper(Environmental E)
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

    public static Vector getAllShopkeepers(Room here, MOB notMOB)
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
                &&((notMOB==null)||(Sense.canBeSeenBy(thisMOB,notMOB))))
                    V.addElement(thisMOB);
            }
            for(int i=0;i<here.numItems();i++)
            {
                Item thisItem=here.fetchItem(i);
                if((thisItem!=null)
                &&(thisItem!=notMOB)
                &&(getShopKeeper(thisItem)!=null)
                &&(!Sense.isGettable(thisItem))
                &&(thisItem.container()==null)
                &&((notMOB==null)||(Sense.canBeSeenBy(thisItem,notMOB))))
                    V.addElement(thisItem);
            }
        }
        return V;
    }

    public static String getViewDescription(Environmental E)
    {
        StringBuffer str=new StringBuffer("");
        if(E==null) return str.toString();
        str.append("Interested in "+E.name()+"?");
        str.append(" Here is some information for you:");
        str.append("\n\rLevel      : "+E.envStats().level());
        if(E instanceof Item)
        {
            Item I=(Item)E;
            str.append("\n\rMaterial   : "+Util.capitalizeAndLower(EnvResource.RESOURCE_DESCS[I.material()&EnvResource.RESOURCE_MASK].toLowerCase()));
            str.append("\n\rWeight     : "+I.envStats().weight()+" pounds");
            if(I instanceof Weapon)
            {
                str.append("\n\rWeap. Type : "+Util.capitalizeAndLower(Weapon.typeDescription[((Weapon)I).weaponType()]));
                str.append("\n\rWeap. Class: "+Util.capitalizeAndLower(Weapon.classifictionDescription[((Weapon)I).weaponClassification()]));
            }
            else
            if(I instanceof Armor)
            {
                str.append("\n\rWear Info  : Worn on ");
                for(int l=0;l<Item.wornCodes.length;l++)
                {
                    int wornCode=1<<l;
                    if(Sense.wornLocation(wornCode).length()>0)
                    {
                        if(((I.rawProperLocationBitmap()&wornCode)==wornCode))
                        {
                            str.append(Util.capitalizeAndLower(Sense.wornLocation(wornCode))+" ");
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
    
    public static boolean shownInInventory(Environmental product, MOB buyer)
    {
        if(!(product instanceof Item)) return true;
        if(((Item)product).container()!=null) return false;
        if(CMSecurity.isAllowed(buyer,buyer.location(),"CMDMOBS")) return true;
        if(((Item)product).envStats().level()>buyer.envStats().level()) return false;
        if(!Sense.canBeSeenBy(product,buyer)) return false;
        return true;
    }
    
    private static double rawSpecificGoldPrice(Environmental product, 
                                               int whatISell, 
                                               double numberOfThem)
    {
        double price=0.0;
        if(product instanceof Item)
            price=((Item)product).value()*numberOfThem;
        else
        if(product instanceof Ability)
        {
            if(whatISell==ShopKeeper.DEAL_TRAINER)
                price=CMAble.lowestQualifyingLevel(product.ID())*100;
            else
                price=CMAble.lowestQualifyingLevel(product.ID())*75;
        }
        else
        if(product instanceof MOB)
        {
            Ability A=product.fetchEffect("Prop_Retainable");
            if(A!=null)
            {
                if(A.text().indexOf(";")<0)
                {
                    if(Util.isDouble(A.text()))
                        price=Util.s_double(A.text());
                    else
                        price=new Integer(Util.s_int(A.text())).doubleValue();
                }
                else
                {
                    String s2=A.text().substring(0,A.text().indexOf(";"));
                    if(Util.isDouble(s2))
                        price=Util.s_double(s2);
                    else
                        price=new Integer(Util.s_int(s2)).doubleValue();
                }
            }
            if(price==0.0)
                price=25.0*product.envStats().level();
        }
        else
            price=CMAble.lowestQualifyingLevel(product.ID())*25;
        return price;
    }
    
    private static double prejudiceValueFromPart(MOB customer, boolean sellTo, String part)
    {
        int x=part.indexOf("=");
        if(x<0) return 0.0;
        String sellorby=part.substring(0,x);
        part=part.substring(x+1);
        if(sellTo&&(!sellorby.trim().equalsIgnoreCase("SELL")))
           return 0.0;
        if((!sellTo)&&(!sellorby.trim().equalsIgnoreCase("BUY")))
           return 0.0;
        if(part.trim().indexOf(" ")<0)
            return Util.s_double(part.trim());
        Vector V=Util.parse(part.trim());
        double d=0.0;
        boolean yes=false;
        Vector VF=customer.fetchFactionRanges();
        String align=Sense.getAlignmentName(customer);
        String sex=customer.charStats().genderName();
        for(int v=0;v<V.size();v++)
        {
            String bit=(String)V.elementAt(v);
            if(Util.s_double(bit)!=0.0)
                d=Util.s_double(bit);
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
    
    private static double prejudiceFactor(MOB customer, String factors, boolean sellTo)
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
            if(Util.s_double(factors)!=0.0)
                return Util.s_double(factors);
            return 1.0;
        }
        int x=factors.indexOf(";");
        while(x>=0)
        {
            String part=factors.substring(0,x).trim();
            factors=factors.substring(x+1).trim();
            double d=prejudiceValueFromPart(customer,sellTo,part);
            if(d!=0.0) return d;
            x=factors.indexOf(";");
        }
        double d=prejudiceValueFromPart(customer,sellTo,factors.trim());
        if(d!=0.0) return d;
        return 1.0;
    }

    public static ShopKeeper.ShopPrice sellingPrice(MOB seller,
                                                    MOB buyer,
                                                    Environmental product,
                                                    ShopKeeper shop,
                                                    boolean includeSalesTax)
    {
        double number=1.0;
        ShopKeeper.ShopPrice val=new ShopKeeper.ShopPrice();
        if(product==null) return val;
        int stockPrice=shop.stockPrice(product);
        if(stockPrice<=-100)
        {
            if(stockPrice<=-1000)
                val.experiencePrice=(stockPrice*-1)-1000;
            else
                val.questPointPrice=(stockPrice*-1)-100;
            return val;
        }
        if(stockPrice>=0)
            val.absoluteGoldPrice=new Integer(stockPrice).doubleValue();
        else
            val.absoluteGoldPrice=CoffeeShops.rawSpecificGoldPrice(product,shop.whatIsSold(),number);

        if(buyer==null) 
        {
            if(val.absoluteGoldPrice>0.0)
                val.absoluteGoldPrice=CMLib.beanCounter().abbreviatedRePrice(seller,val.absoluteGoldPrice);
            return val;
        }

        double prejudiceFactor=CoffeeShops.prejudiceFactor(buyer,shop.prejudiceFactors(),true);
        val.absoluteGoldPrice=Util.mul(prejudiceFactor,val.absoluteGoldPrice);

        // the price is 200% at 0 charisma, and 100% at 30
        val.absoluteGoldPrice=val.absoluteGoldPrice
                             +val.absoluteGoldPrice
                             -Util.mul(val.absoluteGoldPrice,Util.div(buyer.charStats().getStat(CharStats.CHARISMA),30.0));
        if(includeSalesTax)
        {
            double salesTax=getSalesTax(seller.getStartRoom(),seller);
            val.absoluteGoldPrice+=((salesTax>0.0)?(Util.mul(val.absoluteGoldPrice,Util.div(salesTax,100.0))):0.0);
        }
        if(val.absoluteGoldPrice<=0.0) 
            val.absoluteGoldPrice=1.0;
        else
            val.absoluteGoldPrice=CMLib.beanCounter().abbreviatedRePrice(seller,val.absoluteGoldPrice);
        return val;
    }

    
    private static double devalue(ShopKeeper shop, Environmental product)
    {
        int num=shop.numberInStock(product);
        if(num<=0) return 0.0;
        double resourceRate=0.0;
        double itemRate=0.0;
        String s=shop.devalueRate();
        if(s.length()==0) s=CMProps.getVar(CMProps.SYSTEM_DEVALUERATE);
        Vector V=Util.parse(s.trim());
        if(V.size()<=0)
            return 0.0;
        else
        if(V.size()==1)
        {
            resourceRate=Util.div(Util.s_double((String)V.firstElement()),100.0);
            itemRate=resourceRate;
        }
        else
        {
            itemRate=Util.div(Util.s_double((String)V.firstElement()),100.0);
            resourceRate=Util.div(Util.s_double((String)V.lastElement()),100.0);
        }
        double rate=(product instanceof EnvResource)?resourceRate:itemRate;
        rate=rate*num;
        if(rate>1.0) rate=1.0;
        if(rate<0.0) rate=0.0;
        return rate;
    }
    
    public static ShopKeeper.ShopPrice pawningPrice(MOB buyer,
                                                    Environmental product,
                                                    ShopKeeper shop)
    {
        double number=1.0;
        if(product instanceof PackagedItems)
        {
            number=new Integer(((PackagedItems)product).numberOfItemsInPackage()).doubleValue();
            product=((PackagedItems)product).getItem();
        }
        ShopKeeper.ShopPrice val=new ShopKeeper.ShopPrice();
        if(product==null) 
            return val;
        int stockPrice=shop.stockPrice(product);
        if(stockPrice<=-100) return val;

        if(stockPrice>=0.0)
            val.absoluteGoldPrice=new Integer(stockPrice).doubleValue();
        else
            val.absoluteGoldPrice=CoffeeShops.rawSpecificGoldPrice(product,shop.whatIsSold(),number);

        if(buyer==null) return val;

        double prejudiceFactor=CoffeeShops.prejudiceFactor(buyer,shop.prejudiceFactors(),true);
        val.absoluteGoldPrice=Util.mul(prejudiceFactor,val.absoluteGoldPrice);

        //double halfPrice=Math.round(Util.div(val,2.0));
        // gets the shopkeeper a deal on junk.  Pays 5% at 3 charisma, and 50% at 30
        double buyPrice=Util.div(Util.mul(val.absoluteGoldPrice,buyer.charStats().getStat(CharStats.CHARISMA)),60.0);
        if(!(product instanceof Ability))
            buyPrice=Util.mul(buyPrice,1.0-devalue(shop,product));

        // the price is 200% at 0 charisma, and 100% at 30
        double sellPrice=val.absoluteGoldPrice
                        +val.absoluteGoldPrice
                        -Util.mul(val.absoluteGoldPrice,Util.div(buyer.charStats().getStat(CharStats.CHARISMA),30.0));

        if(buyPrice>sellPrice)
            val.absoluteGoldPrice=sellPrice;
        else
            val.absoluteGoldPrice=buyPrice;

        if(val.absoluteGoldPrice<=0.0) 
            val.absoluteGoldPrice=1.0;
        return val;
    }

    
    public static double getSalesTax(Room homeRoom, MOB seller)
    {
        if((seller==null)||(homeRoom==null)) return 0.0;
        Law theLaw=CoffeeUtensils.getTheLaw(homeRoom,seller);
        if(theLaw!=null)
        {
            String taxs=(String)theLaw.taxLaws().get("SALESTAX");
            if(taxs!=null)
                return Util.s_double(taxs);
        }
        return 0.0;
        
    }

    public static boolean standardSellEvaluation(MOB seller,
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
            if(seller.location()!=null)
            {
                int medianLevel=seller.location().getArea().getAreaIStats()[Area.AREASTAT_MEDLEVEL];
                if(medianLevel>0)
                {
                    String range=Util.getParmStr(shop.prejudiceFactors(),"RANGE","0");
                    int rangeI=0;
                    if((range.endsWith("%"))&&(Util.isInteger(range.substring(0,range.length()-1))))
                    {
                        rangeI=Util.s_int(range.substring(0,range.length()-1));
                        rangeI=(int)Math.round(Util.mul(medianLevel,Util.div(rangeI,100.0)));
                    }
                    else
                    if(Util.isInteger(range))
                        rangeI=Util.s_int(range);
                    if((rangeI>0)
                    &&((product.envStats().level()>(medianLevel+rangeI))
                        ||(product.envStats().level()<(medianLevel-rangeI))))
                    {
                        CommonMsgs.say(seller,buyer,"I'm sorry, that's out of my level range.",true,false);
                        return false;
                    }
                }
            }
            double yourValue=CoffeeShops.pawningPrice(buyer,product,shop).absoluteGoldPrice;
            if(yourValue<2)
            {
                CommonMsgs.say(seller,buyer,"I'm not interested.",true,false);
                return false;
            }
            if((sellNotValue)&&(yourValue>maxToPay))
            {
                if(yourValue>maxEverPaid)
                    CommonMsgs.say(seller,buyer,"That's way out of my price range! Try AUCTIONing it.",true,false);
                else
                    CommonMsgs.say(seller,buyer,"Sorry, I can't afford that right now.  Try back later.",true,false);
                return false;
            }
            if(product instanceof Ability)
            {
                CommonMsgs.say(seller,buyer,"I'm not interested.",true,false);
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
                    CommonMsgs.say(seller,buyer,"I won't buy that back unless you put the key in it.",true,false);
                    return false;
                }
            }
            if((product instanceof Item)&&(buyer.isMine(product)))
            {
                FullMsg msg2=new FullMsg(buyer,product,CMMsg.MSG_DROP,null);
                if(!buyer.location().okMessage(buyer,msg2))
                    return false;
            }
            return true;
        }
        CommonMsgs.say(seller,buyer,"I'm sorry, I'm not buying those.",true,false);
        return false;
    }
    public static boolean standardBuyEvaluation(MOB seller,
                                                MOB buyer,
                                                Environmental product,
                                                ShopKeeper shop,
                                                boolean buyNotView)
    {
        if((product!=null)
        &&(shop.doIHaveThisInStock("$"+product.Name()+"$",buyer)))
        {
            if(buyNotView)
            {
                ShopKeeper.ShopPrice price=CoffeeShops.sellingPrice(seller,buyer,product,shop,true);
                if((price.experiencePrice>0)&&(price.experiencePrice>buyer.getExperience()))
                {
                    CommonMsgs.say(seller,buyer,"You aren't experienced enough to buy "+product.name()+".",false,false);
                    return false;
                }
                if((price.questPointPrice>0)&&(price.questPointPrice>buyer.getQuestPoint()))
                {
                    CommonMsgs.say(seller,buyer,"You don't have enough quest points to buy "+product.name()+".",false,false);
                    return false;
                }
                if((price.absoluteGoldPrice>0.0)
                &&(price.absoluteGoldPrice>CMLib.beanCounter().getTotalAbsoluteShopKeepersValue(buyer,seller)))
                {
                    CommonMsgs.say(seller,buyer,"You can't afford to buy "+product.name()+".",false,false);
                    return false;
                }
            }
            if(product instanceof Item)
            {
                if(((Item)product).envStats().level()>buyer.envStats().level())
                {
                    CommonMsgs.say(seller,buyer,"That's too advanced for you, I'm afraid.",true,false);
                    return false;
                }
            }
            if((product instanceof LandTitle)
            &&((shop.whatIsSold()==ShopKeeper.DEAL_CLANDSELLER)||(shop.whatIsSold()==ShopKeeper.DEAL_CSHIPSELLER)))
            {
                Clan C=null;
                if(buyer.getClanID().length()>0)C=Clans.getClan(buyer.getClanID());
                if(C==null)
                {
                    CommonMsgs.say(seller,buyer,"I only sell to clans.",true,false);
                    return false;
                }
                if((C.allowedToDoThis(buyer,Clans.FUNC_CLANPROPERTYOWNER)<0)&&(!buyer.isMonster()))
                {
                    CommonMsgs.say(seller,buyer,"You are not authorized by your clan to handle property.",true,false);
                    return false;
                }
            }
            if(product instanceof MOB)
            {
                if(buyer.totalFollowers()>=buyer.maxFollowers())
                {
                    CommonMsgs.say(seller,buyer,"You can't accept any more followers.",true,false);
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
                if(shop.whatIsSold()==ShopKeeper.DEAL_TRAINER)
                {
                    MOB teacher=CMClass.getMOB("Teacher");
                    if(!((Ability)product).canBeLearnedBy(teacher,buyer))
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
                    if(A.canTarget(buyer)){}
                    else
                    if(A.canTarget(CMClass.sampleItem()))
                    {
                        Item I=buyer.fetchWieldedItem();
                        if(I==null) I=buyer.fetchFirstWornItem(Item.HELD);
                        if(I==null)
                        {
                            CommonMsgs.say(seller,buyer,"You need to be wielding or holding the item you want this cast on.",true,false);
                            return false;
                        }
                    }
                    else
                    {
                        CommonMsgs.say(seller,buyer,"I don't know how to sell that spell.",true,false);
                        return false;
                    }
                }
            }
            return true;
        }
        CommonMsgs.say(seller,buyer,"I don't have that in stock.  Ask for my LIST.",true,false);
        return false;
    }
    
    
    public static String getListInventory(MOB seller, 
                                          MOB buyer, 
                                          Vector inventory,
                                          int limit,
                                          ShopKeeper shop)
    {
        StringBuffer str=new StringBuffer("");
        int csize=0;
        if(inventory.size()>0)
        {
            int totalCols=((shop.whatIsSold()==ShopKeeper.DEAL_LANDSELLER)
                           ||(shop.whatIsSold()==ShopKeeper.DEAL_CLANDSELLER)
                           ||(shop.whatIsSold()==ShopKeeper.DEAL_SHIPSELLER)
                           ||(shop.whatIsSold()==ShopKeeper.DEAL_CSHIPSELLER))?1:2;
            int totalWidth=60/totalCols;
            String showPrice=null;
            ShopKeeper.ShopPrice price=null;
            Environmental E=null;
            for(int i=0;i<inventory.size();i++)
            {
                E=(Environmental)inventory.elementAt(i);
                if(shownInInventory(E,buyer))
                {
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
            }

            String c="^x["+Util.padRight("Cost",4+csize)+"] "+Util.padRight("Product",totalWidth-csize);
            str.append(c+((totalCols>1)?c:"")+"^.^N^<!ENTITY shopkeeper \""+seller.name()+"\"^>\n\r");
            int colNum=0;
            int rowNum=0;
            String col=null;
            for(int i=0;i<inventory.size();i++)
            {
                E=(Environmental)inventory.elementAt(i);

                if(shownInInventory(E,buyer))
                {
                    price=sellingPrice(seller,buyer,E,shop,true);
                    col=null;
                    if(price.questPointPrice>0)
                        col=Util.padRight("["+price.questPointPrice+"qp",5+csize)+"] ^<SHOP^>"+Util.padRight(E.name(),"^</SHOP^>",totalWidth-csize);
                    else
                    if(price.experiencePrice>0)
                        col=Util.padRight("["+price.experiencePrice+"xp",5+csize)+"] ^<SHOP^>"+Util.padRight(E.name(),"^</SHOP^>",totalWidth-csize);
                    else
                        col=Util.padRight("["+CMLib.beanCounter().abbreviatedPrice(seller,price.absoluteGoldPrice),5+csize)+"] ^<SHOP^>"+Util.padRight(E.name(),"^</SHOP^>",totalWidth-csize);
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
        }
        if(str.length()==0)
        {
            if((shop.whatIsSold()!=ShopKeeper.DEAL_BANKER)
            &&(shop.whatIsSold()!=ShopKeeper.DEAL_CLANBANKER)
            &&(shop.whatIsSold()!=ShopKeeper.DEAL_CLANPOSTMAN)
            &&(shop.whatIsSold()!=ShopKeeper.DEAL_POSTMAN))
                return seller.name()+" has nothing for sale.";
            return "";
        }
        double salesTax=getSalesTax(seller.getStartRoom(),seller);
        return "\n\r"+str
                +((salesTax<=0.0)?"":"\n\r\n\rPrices above include a "+salesTax+"% sales tax.")
                +"^T";
    }
    
    public static String findInnRoom(InnKey key, String addThis, Room R)
    {
        if(R==null) return null;
        String keyNum=key.getKey();
        for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
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
    
    public static MOB parseBuyingFor(MOB buyer, String message)
    {
        MOB mobFor=buyer;
        if((message!=null)&&(message.length()>0)&&(buyer.location()!=null))
        {
            Vector V=Util.parse(message);
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
    
    public static double transactPawn(MOB shopkeeper,
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
            double val=CoffeeShops.pawningPrice(pawner,rawSoldItem,shop).absoluteGoldPrice;
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
                shop.addStoreInventory(coreSoldItem,number,-1);
                if(V!=null)
                for(int v=0;v<V.size();v++)
                {
                    Item item2=(Item)V.elementAt(v);
                    if(!shop.doISellThis(item2)||(item2 instanceof Key))
                        item2.destroy();
                    else
                        shop.addStoreInventory(item2,1,-1);
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
                shop.addStoreInventory(newMOB);
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
    
    public static void transactMoneyOnly(MOB seller,
                                         MOB buyer,
                                         ShopKeeper shop,
                                         Environmental product)
    {
        if((seller==null)||(seller.location()==null)||(buyer==null)||(shop==null)||(product==null))
            return;
        Room room=seller.location();
        ShopKeeper.ShopPrice price=CoffeeShops.sellingPrice(seller,buyer,product,shop,true);
        if(price.absoluteGoldPrice>0.0) 
        {
            CMLib.beanCounter().subtractMoney(buyer,CMLib.beanCounter().getCurrency(seller),price.absoluteGoldPrice);
            double totalFunds=price.absoluteGoldPrice;
            if(CoffeeShops.getSalesTax(seller.getStartRoom(),seller)!=0.0)
            {
                Law theLaw=CoffeeUtensils.getTheLaw(room,seller);
                Area A2=CoffeeUtensils.getLegalObject(room);
                if((theLaw!=null)&&(A2!=null))
                {
                    Environmental[] Treas=theLaw.getTreasuryNSafe(A2);
                    Room treasuryR=(Room)Treas[0];
                    Item treasuryItem=(Item)Treas[1];
                    if(treasuryR!=null)
                    {
                        double taxAmount=totalFunds-CoffeeShops.sellingPrice(seller,buyer,product,shop,false).absoluteGoldPrice;
                        totalFunds-=taxAmount;
                        Coins COIN=CMLib.beanCounter().makeBestCurrency(CMLib.beanCounter().getCurrency(seller),taxAmount,treasuryR,treasuryItem);
                        if(COIN!=null) COIN.putCoinsBack();
                    }
                }
            }
            if(seller.isMonster())
            {
                LandTitle T=CoffeeUtensils.getLandTitle(seller.getStartRoom());
                if((T!=null)&&(T.landOwner().length()>0))
                {
                    CMLib.beanCounter().modifyLocalBankGold(seller.getStartRoom().getArea(),
                                                    T.landOwner(),
                                                    CoffeeUtensils.getFormattedDate(buyer)+": Deposit of "+CMLib.beanCounter().nameCurrencyShort(seller,totalFunds)+": Purchase: "+product.Name()+" from "+seller.Name(),
                                                    CMLib.beanCounter().getCurrency(seller),
                                                    totalFunds);
                }
            }
        }
        if(price.questPointPrice>0) buyer.setQuestPoint(buyer.getQuestPoint()-price.questPointPrice);
        if(price.experiencePrice>0) MUDFight.postExperience(buyer,null,null,-price.experiencePrice,false);
        buyer.recoverEnvStats();
    }
    
    public static boolean purchaseItems(Item baseProduct,
                                        Vector products,
                                        MOB seller,
                                        MOB mobFor)
    {
        if((seller==null)||(seller.location()==null)||(mobFor==null))
            return false;
        Room room=seller.location();
        for(int p=0;p<products.size();p++)
            room.addItemRefuse((Item)products.elementAt(p),Item.REFUSE_PLAYER_DROP);
        FullMsg msg2=new FullMsg(mobFor,baseProduct,seller,CMMsg.MSG_GET,null);
        if((baseProduct instanceof LandTitle)||(room.okMessage(mobFor,msg2)))
        {
            room.send(mobFor,msg2);
            if((baseProduct instanceof InnKey)&&(room!=null))
            {
                InnKey item =(InnKey)baseProduct;
                String buf=CoffeeShops.findInnRoom(item, "", room);
                if(buf==null) buf=CoffeeShops.findInnRoom(item, "upstairs", room.getRoomInDir(Directions.UP));
                if(buf==null) buf=CoffeeShops.findInnRoom(item, "downstairs", room.getRoomInDir(Directions.DOWN));
                if(buf!=null) CommonMsgs.say(seller,mobFor,"Your room is "+buf+".",true,false);
            }
            return true;
        }
        return false;
    }
    
    public static boolean purchaseMOB(MOB product,
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
        if(shop.whatIsSold()==ShopKeeper.DEAL_SLAVES)
        {
            slaveA=product.fetchEffect("Skill_Enslave");
            if(slaveA!=null) slaveA.setMiscText("");
            else
            if(!Sense.isAnimalIntelligence(product))
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
        CommonMsgs.follow(product,mobFor,false);
        if(product.amFollowing()==null)
        {
            mobFor.tell("You cannot accept seem to accept this follower!");
            return false;
        }
        return true;
    }
    
    public static void purchaseAbility(Ability A, 
                                       MOB seller,
                                       ShopKeeper shop,
                                       MOB mobFor)
    {
        if((seller==null)||(seller.location()==null)||(A==null)||(shop==null)||(mobFor==null))
            return ;
        Room room=seller.location();
        if(shop.whatIsSold()==ShopKeeper.DEAL_TRAINER)
        {
            MOB teacher=CMClass.getMOB("Teacher");
            A.teach(teacher,mobFor);
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
            if(A.canTarget(mobFor))
            {
                V.addElement("$"+mobFor.name()+"$");
                A.invoke(seller,V,mobFor,true,0);
            }
            else
            if(A.canTarget(CMClass.sampleItem()))
            {
                Item I=mobFor.fetchWieldedItem();
                if(I==null) I=mobFor.fetchFirstWornItem(Item.HELD);
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
    
    public static Vector addRealEstateTitles(Vector V, MOB buyer, int whatISell, Room myRoom)
    {
        if((myRoom==null)||(buyer==null)) return V;
        Area myArea=myRoom.getArea();
        if(((whatISell==ShopKeeper.DEAL_LANDSELLER)
            ||(whatISell==ShopKeeper.DEAL_SHIPSELLER)
            ||((whatISell==ShopKeeper.DEAL_CSHIPSELLER)&&(buyer.getClanID().length()>0))
            ||((whatISell==ShopKeeper.DEAL_CLANDSELLER)&&(buyer.getClanID().length()>0)))
        &&(myArea!=null))
        {
            String name=buyer.Name();
            if((whatISell==ShopKeeper.DEAL_CLANDSELLER)||(whatISell==ShopKeeper.DEAL_CSHIPSELLER))
                name=buyer.getClanID();
            HashSet roomsHandling=new HashSet();
            Hashtable titles=new Hashtable();
            if((whatISell==ShopKeeper.DEAL_CSHIPSELLER)||(whatISell==ShopKeeper.DEAL_SHIPSELLER))
            {
                for(Enumeration r=CMMap.areas();r.hasMoreElements();)
                {
                    Area A=(Area)r.nextElement();
                    if((A instanceof SpaceShip)
                    &&(Sense.isHidden(A)))
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
                            LandTitle LT=CoffeeUtensils.getLandTitle(A);
                            if(LT!=null) titles.put(A,LT);
                        }
                    }
                }
            }
            else
            for(Enumeration r=myArea.getProperMap();r.hasMoreElements();)
            {
                Room R=(Room)r.nextElement();
                LandTitle A=CoffeeUtensils.getLandTitle(R);
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
                        ((LandTitle)I).setLandPropertyID(CMMap.getExtendedRoomID((Room)R));
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
        return V;
    }
    
    public static boolean ignoreIfNecessary(MOB mob, String ignoreMask, MOB whoIgnores)
    {
        if(ignoreMask.length()==0) ignoreMask=CMProps.getVar(CMProps.SYSTEM_IGNOREMASK);
        if((ignoreMask.length()>0)&&(!MUDZapper.zapperCheck(ignoreMask,mob)))
        {
            mob.tell(whoIgnores,null,null,"<S-NAME> appear(s) to be ignoring you.");
            return false;
        }
        return true;
    }
    
    
    public static String storeKeeperString(int whatISell)
    {
        switch(whatISell)
        {
        case ShopKeeper.DEAL_ANYTHING:
            return "*Anything*";
        case ShopKeeper.DEAL_GENERAL:
            return "General items";
        case ShopKeeper.DEAL_ARMOR:
            return "Armor";
        case ShopKeeper.DEAL_MAGIC:
            return "Miscellaneous Magic Items";
        case ShopKeeper.DEAL_WEAPONS:
            return "Weapons";
        case ShopKeeper.DEAL_PETS:
            return "Pets and Animals";
        case ShopKeeper.DEAL_LEATHER:
            return "Leather";
        case ShopKeeper.DEAL_INVENTORYONLY:
            return "Only my Inventory";
        case ShopKeeper.DEAL_TRAINER:
            return "Training in skills/spells/prayers/songs";
        case ShopKeeper.DEAL_CASTER:
            return "Caster of spells/prayers";
        case ShopKeeper.DEAL_ALCHEMIST:
            return "Potions";
        case ShopKeeper.DEAL_INNKEEPER:
            return "My services as an Inn Keeper";
        case ShopKeeper.DEAL_JEWELLER:
            return "Precious stones and jewellery";
        case ShopKeeper.DEAL_BANKER:
            return "My services as a Banker";
        case ShopKeeper.DEAL_CLANBANKER:
            return "My services as a Banker to Clans";
        case ShopKeeper.DEAL_LANDSELLER:
            return "Real estate";
        case ShopKeeper.DEAL_CLANDSELLER:
            return "Clan estates";
        case ShopKeeper.DEAL_ANYTECHNOLOGY:
            return "Any technology";
        case ShopKeeper.DEAL_BUTCHER:
            return "Meats";
        case ShopKeeper.DEAL_FOODSELLER:
            return "Foodstuff";
        case ShopKeeper.DEAL_GROWER:
            return "Vegetables";
        case ShopKeeper.DEAL_HIDESELLER:
            return "Hides and Furs";
        case ShopKeeper.DEAL_LUMBERER:
            return "Lumber";
        case ShopKeeper.DEAL_METALSMITH:
            return "Metal ores";
        case ShopKeeper.DEAL_STONEYARDER:
            return "Stone and rock";
        case ShopKeeper.DEAL_SHIPSELLER:
            return "Ships";
        case ShopKeeper.DEAL_CSHIPSELLER:
            return "Clan Ships";
        case ShopKeeper.DEAL_SLAVES:
            return "Slaves";
        case ShopKeeper.DEAL_POSTMAN:
            return "My services as a Postman";
        case ShopKeeper.DEAL_CLANPOSTMAN:
            return "My services as a Postman for Clans";
        default:
            return "... I have no idea WHAT I sell";
        }
    }

    public static boolean doISellThis(Environmental thisThang, ShopKeeper shop)
    {
        if(thisThang instanceof PackagedItems)
            thisThang=((PackagedItems)thisThang).getItem();
        if(thisThang==null) return false;
        if(thisThang instanceof Coins) return false;
        switch(shop.whatIsSold())
        {
        case ShopKeeper.DEAL_ANYTHING:
            return true;
        case ShopKeeper.DEAL_ARMOR:
            return (thisThang instanceof Armor);
        case ShopKeeper.DEAL_MAGIC:
            return (thisThang instanceof MiscMagic);
        case ShopKeeper.DEAL_WEAPONS:
            return (thisThang instanceof Weapon)||(thisThang instanceof Ammunition);
        case ShopKeeper.DEAL_GENERAL:
            return ((thisThang instanceof Item)
                    &&(!(thisThang instanceof Armor))
                    &&(!(thisThang instanceof MiscMagic))
                    &&(!(thisThang instanceof ClanItem))
                    &&(!(thisThang instanceof Weapon))
                    &&(!(thisThang instanceof Ammunition))
                    &&(!(thisThang instanceof MOB))
                    &&(!(thisThang instanceof EnvResource))
                    &&(!(thisThang instanceof Ability)));
        case ShopKeeper.DEAL_LEATHER:
            return ((thisThang instanceof Item)
                    &&((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LEATHER)
                    &&(!(thisThang instanceof EnvResource)));
        case ShopKeeper.DEAL_PETS:
            return ((thisThang instanceof MOB)&&(Sense.isAnimalIntelligence((MOB)thisThang)));
        case ShopKeeper.DEAL_SLAVES:
            return ((thisThang instanceof MOB)&&(!Sense.isAnimalIntelligence((MOB)thisThang)));
        case ShopKeeper.DEAL_INVENTORYONLY:
            return (shop.inBaseInventory(thisThang));
        case ShopKeeper.DEAL_INNKEEPER:
            return thisThang instanceof InnKey;
        case ShopKeeper.DEAL_JEWELLER:
            return ((thisThang instanceof Item)
                    &&(!(thisThang instanceof Weapon))
                    &&(!(thisThang instanceof MiscMagic))
                    &&(!(thisThang instanceof ClanItem))
                    &&(((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_GLASS)
                    ||((((Item)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_PRECIOUS)
                    ||((Item)thisThang).fitsOn(Item.ON_EARS)
                    ||((Item)thisThang).fitsOn(Item.ON_NECK)
                    ||((Item)thisThang).fitsOn(Item.ON_RIGHT_FINGER)
                    ||((Item)thisThang).fitsOn(Item.ON_LEFT_FINGER)));
        case ShopKeeper.DEAL_ALCHEMIST:
            return (thisThang instanceof Potion);
        case ShopKeeper.DEAL_LANDSELLER:
        case ShopKeeper.DEAL_CLANDSELLER:
        case ShopKeeper.DEAL_SHIPSELLER:
        case ShopKeeper.DEAL_CSHIPSELLER:
            return (thisThang instanceof LandTitle);
        case ShopKeeper.DEAL_ANYTECHNOLOGY:
            return (thisThang instanceof Electronics);
        case ShopKeeper.DEAL_BUTCHER:
            return ((thisThang instanceof EnvResource)
                &&(((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_FLESH);
        case ShopKeeper.DEAL_FOODSELLER:
            return (((thisThang instanceof Food)||(thisThang instanceof Drink))
                    &&(!(thisThang instanceof EnvResource)));
        case ShopKeeper.DEAL_GROWER:
            return ((thisThang instanceof EnvResource)
                &&(((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_VEGETATION);
        case ShopKeeper.DEAL_HIDESELLER:
            return ((thisThang instanceof EnvResource)
                &&((((EnvResource)thisThang).material()==EnvResource.RESOURCE_HIDE)
                ||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_FEATHERS)
                ||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_LEATHER)
                ||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_SCALES)
                ||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_WOOL)
                ||(((EnvResource)thisThang).material()==EnvResource.RESOURCE_FUR)));
        case ShopKeeper.DEAL_LUMBERER:
            return ((thisThang instanceof EnvResource)
                &&((((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_WOODEN));
        case ShopKeeper.DEAL_METALSMITH:
            return ((thisThang instanceof EnvResource)
                &&(((((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
                ||(((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL));
        case ShopKeeper.DEAL_STONEYARDER:
            return ((thisThang instanceof EnvResource)
                &&((((EnvResource)thisThang).material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_ROCK));
        }

        return false;
    }

}
