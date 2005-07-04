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
public class StdPostman extends StdShopKeeper implements PostOffice
{
    public String ID(){return "StdPostman";}

    protected double minimumPostage=1.0;
    protected double postagePerPound=1.0;
    protected double holdFeePerPound=0.10;
    protected double feeForNewBox=50.0;
    protected int maxMudMonthsHeld=12;
    protected static Hashtable postalTimes=new Hashtable();

    public StdPostman()
    {
        super();
        Username="a postman";
        setDescription("He\\`s making a speedy delivery!");
        setDisplayText("The local postman is waiting to serve you.");
        Factions.setAlignment(this,Faction.ALIGN_GOOD);
        setMoney(0);
        whatISell=ShopKeeper.DEAL_POSTMAN;
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



    public double minimumPostage(){return minimumPostage;}
    public void setMinimumPostage(double d){minimumPostage=d;}
    public double postagePerPound(){return postagePerPound;}
    public void setPostagePerPound(double d){postagePerPound=d;}
    public double holdFeePerPound(){return holdFeePerPound;}
    public void setHoldFeePerPound(double d){holdFeePerPound=d;}
    public double feeForNewBox(){return feeForNewBox;}
    public void setFeeForNewBox(double d){feeForNewBox=d;}
    public int maxMudMonthsHeld(){return maxMudMonthsHeld;}
    public void setMaxMudMonthsHeld(int months){maxMudMonthsHeld=months;}
    
    public void destroy()
    {
        super.destroy();
        CMMap.delPostOffice(this);
    }
    public void bringToLife(Room newLocation, boolean resetStats)
    {
        super.bringToLife(newLocation,resetStats);
        CMMap.addPostOffice(this);
    }
    
    public int whatIsSold(){return whatISell;}
    public void setWhatIsSold(int newSellCode){
        if(newSellCode!=ShopKeeper.DEAL_CLANPOSTMAN)
            whatISell=ShopKeeper.DEAL_POSTMAN;
        else
            whatISell=ShopKeeper.DEAL_CLANPOSTMAN;
    }

    public String postalChain(){return text();}
    public void setPostalChain(String name){setMiscText(name);}
    public String postalBranch(){return CMMap.getExtendedRoomID(getStartRoom());}

    public void addToBox(String mob, Item thisThang, String from, String to, long holdTime, double COD)
    {
        String name=thisThang.ID();
        CMClass.DBEngine().DBCreateData(mob,
                postalChain(),
                postalBranch()+";"+thisThang+Math.random(),
                from+";"
                +to+";"
                +holdTime+";"
                +COD+";"
                +name+";"
                +CoffeeMaker.getPropertiesStr(thisThang,true));
    }

    public void addToBox(MOB mob, Item thisThang, String from, String to, long holdTime, double COD)
    {
        if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
        {
            if(mob.getClanID().length()==0)
                return;
            addToBox(mob.getClanID(),thisThang,from,to,holdTime,COD);
        }
        else
            addToBox(mob.Name(),thisThang,from,to,holdTime,COD);
    }

    public boolean delFromBox(MOB mob, Item thisThang)
    {
        if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
        {
            if(mob.getClanID().length()>0)
                return delFromBox(mob.getClanID(),thisThang);
        }
        else
            return delFromBox(mob.Name(),thisThang);
        return false;
    }

    public boolean delFromBox(String mob, Item thisThang)
    {
        Vector V=getBoxRowData(mob);
        boolean found=false;
        for(int v=V.size()-1;v>=0;v--)
        {
            Vector V2=(Vector)V.elementAt(v);
            if((V2.size()>3)
            &&(((String)V2.elementAt(DATA_KEY)).startsWith(postalBranch()+";")))
            {
                Item I=makeItem(parsePieceData((String)V2.elementAt(DATA_DATA)));
                if(I==null) continue;
                if(thisThang.sameAs(I))
                {
                    found=true;
                    CMClass.DBEngine().DBDeleteData(
                            ((String)V2.elementAt(DATA_USERID)),
                            ((String)V2.elementAt(DATA_CHAIN)),
                            ((String)V2.elementAt(DATA_KEY)));
                    break;
                }
            }
        }
        return found;
    };
    public void emptyBox(String mob)
    {
        CMClass.DBEngine().DBDeleteData(mob,postalChain());
    };
    public Hashtable getOurOpenBoxes(String mob)
    {
        Hashtable branches=new Hashtable();
        Vector V=CMClass.DBEngine().DBReadData(mob,postalChain());
        if(V==null) return branches;
        for(int v=0;v<V.size();v++)
        {
            Vector V2=(Vector)V.elementAt(v);
            if(V2.size()>3)
            {
                String key=(String)V2.elementAt(DATA_KEY);
                int x=key.indexOf("/");
                if(x>0)
                    branches.put(key.substring(0,x),key.substring(x+1));
            }
        }
        return branches;
    }
    public void createBoxHere(String mob, String forward)
    {
        if(!getOurOpenBoxes(mob).containsKey(postalBranch()))
        {
            CMClass.DBEngine().DBCreateData(mob,
                    postalChain(),
                    postalBranch()+"/"+forward,
                    "50");
        }
    }
    public void deleteBoxHere(String mob)
    {
        Vector V=getBoxRowData(mob);
        if(V==null) return;
        for(int v=0;v<V.size();v++)
        {
            Vector V2=(Vector)V.elementAt(v);
            if((V2.size()>3)
            &&(((String)V2.elementAt(DATA_KEY)).startsWith(postalBranch()+"/")))
            {
                CMClass.DBEngine().DBDeleteData(
                ((String)V2.elementAt(DATA_USERID)),
                ((String)V2.elementAt(DATA_CHAIN)),
                ((String)V2.elementAt(DATA_KEY)));
            }
        }
    }
    public Vector getAllBoxItemVectors(String mob)
    {
        Vector V=getBoxRowData(mob);
        Vector mine=new Vector();
        for(int v=0;v<V.size();v++)
        {
            Vector V2=(Vector)V.elementAt(v);
            if((V2.size()>3)
            &&(((String)V2.elementAt(DATA_KEY)).startsWith(postalBranch()+";")))
            {
                Vector V3=parsePieceData((String)V2.elementAt(DATA_DATA));
                Item I=makeItem(V3);
                if(I!=null) 
                {
                    Vector V4=new Vector();
                    V4.addElement(V3.elementAt(PIECE_FROM));
                    V4.addElement(V3.elementAt(PIECE_TO));
                    V4.addElement(V3.elementAt(PIECE_TIME));
                    V4.addElement(V3.elementAt(PIECE_COD));
                    V4.addElement(V3.elementAt(PIECE_CLASSID));
                    V4.addElement(I);
                    mine.addElement(V4);
                }
            }
        }
        return mine;
    }
    public Vector getBoxRowData(String mob)
    {
        return CMClass.DBEngine().DBReadData(mob,postalChain());
    };
    public Item findBoxContents(MOB mob, String likeThis)
    {
        if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
        {
            if(mob.getClanID().length()==0) return null;
            return findBoxContents(mob.getClanID(),likeThis);
        }
        else
            return findBoxContents(mob.Name(),likeThis);
    }

    public Item findBoxContents(String mob, String likeThis)
    {
        Vector V=getBoxRowData(mob);
        for(int v=0;v<V.size();v++)
        {
            Vector V2=(Vector)V.elementAt(v);
            if((V2.size()>3)
            &&(((String)V2.elementAt(DATA_KEY)).startsWith(postalBranch()+";")))
            {
                Item I=makeItem(parsePieceData((String)V2.elementAt(DATA_DATA)));
                if(I==null) continue;
                if(EnglishParser.containsString(I.Name(),likeThis))
                    return I;
            }
        }
        return null;
    };

    public Vector findExactBoxData(String mob, Item likeThis)
    {
        Vector V=getBoxRowData(mob);
        for(int v=0;v<V.size();v++)
        {
            Vector V2=(Vector)V.elementAt(v);
            if((V2.size()>3)
            &&(((String)V2.elementAt(DATA_KEY)).startsWith(postalBranch()+";")))
            {
                Item I=makeItem(parsePieceData((String)V2.elementAt(DATA_DATA)));
                if(I==null) continue;
                if(I.sameAs(likeThis))
                    return parsePieceData((String)V2.elementAt(DATA_DATA));
            }
        }
        return null;
    };

    protected Vector parsePieceData(String data)
    {
        Vector V=new Vector();
        for(int i=0;i<5;i++)
        {
            int x=data.indexOf(";");
            if(x<0) return new Vector();
            V.addElement(data.substring(0,x));
            data=data.substring(x+1);
        }
        V.addElement(data);
        return V;
    }
    
    protected Item makeItem(Vector data)
    {
        if(data.size()<NUM_PIECES)
            return null;
        Item I=CMClass.getItem((String)data.elementAt(PIECE_CLASSID));
        if(I!=null)
        {
            CoffeeMaker.setPropertiesStr(I,(String)data.elementAt(PIECE_MISCDATA),true);
            I.recoverEnvStats();
            I.text();
            return I;
        }
        return null;
    }
    
    protected double getTotalChargeForPiece(Vector data)
    {
        if(data.size()<NUM_PIECES)
            return 0.0;
        if(getStartRoom()==null) return 0.0;
        double COD=Util.s_double((String)data.elementAt(PIECE_COD));
        if(COD==0.0) return 0.0;
        Item I=makeItem(data);
        if(I==null) return 0.0;
        TimeClock TC=getStartRoom().getArea().getTimeObj();
        double amt=minimumPostage()+COD;
        int chargeableWeight=0;
        if(I.envStats().weight()>0)
            chargeableWeight=(I.envStats().weight()-1);
        amt+=Util.mul(postagePerPound,chargeableWeight);
        if(chargeableWeight<=0) return amt;
        long time=System.currentTimeMillis()-Util.s_long((String)data.elementAt(PIECE_TIME));
        long millisPerMudMonth=TC.getDaysInMonth()*MudHost.TIME_UTILTHREAD_SLEEP*TC.getHoursInDay();
        if(time<0) return amt;
        amt+=Util.mul(Util.mul(Math.floor(Util.div(time,millisPerMudMonth)),holdFeePerPound()),chargeableWeight);
        return amt;
    }
    
    protected String getBranchPostableTo(String toWhom, String branch, Hashtable allBranchBoxes)
    {
        String forward=(String)allBranchBoxes.get(branch);
        if(forward==null) return null;
        if(forward.equalsIgnoreCase(toWhom))
            return branch;
        PostOffice P=CMMap.getPostOffice(postalChain(),forward);
        if(P!=null)
        {
            forward=(String)allBranchBoxes.get(P.postalBranch());
            if((forward!=null)&&forward.equalsIgnoreCase(toWhom))
                return P.postalBranch();
        }
        return null;
    }
    
    public String findProperBranch(String toWhom)
    {
        if(CMMap.getLoadPlayer(toWhom)!=null)
        {
            MOB M=CMMap.getLoadPlayer(toWhom);
            if(M.getStartRoom()!=null)
            {
                Hashtable allBranchBoxes=getOurOpenBoxes(toWhom);
                PostOffice P=CMMap.getPostOffice(postalChain(),M.getStartRoom().getArea().Name());
                String branch=null;
                if(P!=null)
                {
                    branch=getBranchPostableTo(toWhom,P.postalBranch(),allBranchBoxes);
                    if(branch!=null) return branch;
                    if((branch==null)&&(allBranchBoxes.size()==0))
                    {
                        P.createBoxHere(toWhom,toWhom);
                        return P.postalBranch();
                    }
                }
                branch=getBranchPostableTo(toWhom,postalBranch(),allBranchBoxes);
                if(branch!=null) return branch;
                for(Enumeration e=allBranchBoxes.keys();e.hasMoreElements();)
                {
                    String tryBranch=(String)e.nextElement();
                    branch=getBranchPostableTo(toWhom,tryBranch,allBranchBoxes);
                    if(branch!=null) return branch;
                }
                if(P!=null)
                {
                    P.deleteBoxHere(toWhom);
                    P.createBoxHere(toWhom,toWhom);
                    return P.postalBranch();
                }
            }
        }
        else
        if(Clans.getClan(toWhom)!=null)
        {
            Hashtable allBranchBoxes=getOurOpenBoxes(toWhom);
            String branch=getBranchPostableTo(toWhom,postalBranch(),allBranchBoxes);
            if(branch!=null) return branch;
            for(Enumeration e=allBranchBoxes.keys();e.hasMoreElements();)
            {
                String tryBranch=(String)e.nextElement();
                branch=getBranchPostableTo(toWhom,tryBranch,allBranchBoxes);
                if(branch!=null) return branch;
            }
        }
        return null;
    }

    public boolean tick(Tickable ticking, int tickID)
    {
        if(!super.tick(ticking,tickID))
            return false;
        if((tickID==MudHost.TICK_MOB)&&(location()!=null))
        {
            boolean proceed=false;
            // handle interest by watching the days go by...
            // each chain is handled by one branch, 
            // since pending mail all looks the same.
            Long L=(Long)postalTimes.get(postalChain()+"/"+postalBranch());
            if((L==null)||(L.longValue()<System.currentTimeMillis()))
            {
                L=new Long(System.currentTimeMillis()+new Long((location().getArea().getTimeObj().getHoursInDay())*MudHost.TIME_MILIS_PER_MUDHOUR*5).longValue());
                proceed=true;
                postalTimes.remove(postalChain()+"/"+postalBranch());
                postalTimes.put(postalChain()+"/"+postalBranch(),L);
            }
            if(proceed)
            {
                Vector V=getBoxRowData(postalChain());
                // first parse all the pending mail,
                // and remove it from the sorter
                Vector parsed=new Vector();
                if(V==null) V=new Vector();
                for(int v=0;v<V.size();v++)
                {
                    Vector V2=(Vector)V.elementAt(v);
                    parsed.addElement(parsePieceData((String)V2.elementAt(DATA_DATA)));
                    CMClass.DBEngine().DBDeleteData(
                    ((String)V2.elementAt(DATA_USERID)),
                    ((String)V2.elementAt(DATA_CHAIN)),
                    ((String)V2.elementAt(DATA_KEY)));
                }
                PostOffice P=null;
                for(int v=0;v<parsed.size();v++)
                {
                    Vector V2=(Vector)parsed.elementAt(v);
                    String toWhom=(String)V2.elementAt(PIECE_TO);
                    String deliveryBranch=findProperBranch(toWhom);
                    if(deliveryBranch!=null)
                    {
                        P=CMMap.getPostOffice(postalChain(),deliveryBranch);
                        if(P!=null)
                        {
                            Item I=makeItem((Vector)V.elementAt(v));
                            if(I!=null)
                            {
                                P.addToBox(toWhom,I,(String)V2.elementAt(PIECE_FROM),(String)V2.elementAt(PIECE_TO),Util.s_long((String)V2.elementAt(PIECE_TIME)),Util.s_double((String)V2.elementAt(PIECE_COD)));
                                continue;
                            }
                        }
                    }
                    String fromWhom=(String)V2.elementAt(PIECE_FROM);
                    deliveryBranch=findProperBranch(fromWhom);
                    if(deliveryBranch!=null)
                    {
                        P=CMMap.getPostOffice(postalChain(),deliveryBranch);
                        if(P!=null)
                        {
                            Item I=makeItem((Vector)V.elementAt(v));
                            if(I!=null)
                                P.addToBox(fromWhom,I,(String)V2.elementAt(PIECE_TO),"POSTMASTER",System.currentTimeMillis(),0.0);
                        }
                    }
                    
                }
                V=CMClass.DBEngine().DBReadData(postalChain());
                TimeClock TC=null;
                if(getStartRoom()!=null) TC=getStartRoom().getArea().getTimeObj();
                if((TC!=null)&&(maxMudMonthsHeld()>0))
                for(int v=0;v<V.size();v++)
                {
                    Vector V2=(Vector)V.elementAt(v);
                    if((V2.size()>3)
                    &&(((String)V2.elementAt(DATA_KEY)).startsWith(postalBranch()+";")))
                    {
                        Vector data=parsePieceData(((String)V2.elementAt(DATA_DATA)));
                        if((data!=null)&&(data.size()>1)&&(getStartRoom()!=null))
                        {
                            long time=System.currentTimeMillis()-Util.s_long((String)data.elementAt(PIECE_TIME));
                            long millisPerMudMonth=TC.getDaysInMonth()*MudHost.TIME_UTILTHREAD_SLEEP*TC.getHoursInDay();
                            if(time>0)
                            {
                                int months=(int)Math.round(Math.floor(Util.div(time,millisPerMudMonth)));
                                if(months>maxMudMonthsHeld())
                                {
                                    Item I=makeItem(V2);
                                    CMClass.DBEngine().DBDeleteData(
                                            ((String)V2.elementAt(DATA_USERID)),
                                            ((String)V2.elementAt(DATA_CHAIN)),
                                            ((String)V2.elementAt(DATA_KEY)));
                                    if(I!=null)
                                        addStoreInventory(I);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public void autoGive(MOB src, MOB tgt, Item I)
    {
        FullMsg msg2=new FullMsg(src,I,null,CMMsg.MSG_DROP,null,CMMsg.MSG_DROP,"GIVE",CMMsg.MSG_DROP,null);
        location().send(this,msg2);
        msg2=new FullMsg(tgt,I,null,CMMsg.MSG_GET,null,CMMsg.MSG_GET,"GIVE",CMMsg.MSG_GET,null);
        location().send(this,msg2);
    }
    
    protected String getMsgFromAffect(String msg)
    {
        if(msg==null) return null;
        int start=msg.indexOf("'");
        int end=msg.lastIndexOf("'");
        if((start>0)&&(end>start))
            return msg.substring(start+1,end);
        return "";
    }
    
    public void executeMsg(Environmental myHost, CMMsg msg)
    {
        MOB mob=msg.source();
        if(msg.amITarget(this))
        {
            switch(msg.targetMinor())
            {
            case CMMsg.TYP_GIVE:
            case CMMsg.TYP_DEPOSIT:
                {
                    if(msg.tool() instanceof Container)
                        ((Container)msg.tool()).emptyPlease();
                    Session S=msg.source().session();
                    if((!msg.source().isMonster())&&(S!=null)&&(msg.tool() instanceof Item))
                    {
                        autoGive(msg.source(),this,(Item)msg.tool());
                        if(isMine(msg.tool()))
                        {
                            try
                            {
                                String toWhom=S.prompt("Address this to whom? ","");
                                if((toWhom!=null)&&(toWhom.length()>0)
                                &&((CMMap.getLoadPlayer(toWhom)!=null)||(Clans.findClan(toWhom)!=null)))
                                {
                                    String fromWhom=msg.source().Name();
                                    if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                                        fromWhom=msg.source().getClanID();
                                    if(CMMap.getLoadPlayer(toWhom)!=null)
                                        toWhom=CMMap.getLoadPlayer(toWhom).Name();
                                    else
                                        toWhom=Clans.findClan(toWhom).name();
                                    double amt=minimumPostage();
                                    if(msg.tool().envStats().weight()>0)
                                        amt+=Util.mul(postagePerPound,(msg.tool().envStats().weight()-1));
                                    double COD=0.0;
                                    boolean deliver=true;
                                    String choice=S.choose("Postage on this will be "+BeanCounter.nameCurrencyShort(msg.source(),amt)+".  Would you like to P)ay this now, or be C)harged on delivery (c/P)?","CP\n","P").trim().toUpperCase();
                                    if(choice.startsWith("C"))
                                    {
                                        String CODstr=S.prompt("Enter COD amount ("+BeanCounter.getDenominationName(BeanCounter.getCurrency(this),BeanCounter.getLowestDenomination(BeanCounter.getCurrency(this)))+"): ");
                                        if((CODstr.length()==0)||(!Util.isNumber(CODstr))||(Util.s_double(CODstr)<=0.0))
                                        {
                                            CommonMsgs.say(this,mob,"That is not a valid amount.",true,false);
                                            autoGive(this,msg.source(),(Item)msg.tool());
                                            deliver=false;
                                        }
                                        else
                                        {
                                            COD=Util.s_double(CODstr);
                                            amt=0.0;
                                        }
                                    }
                                    else
                                    if((amt>0.0)&&(BeanCounter.getTotalAbsoluteShopKeepersValue(msg.source(),this)<amt))
                                    {
                                        CommonMsgs.say(this,mob,"You can't afford postage.",true,false);
                                        autoGive(this,msg.source(),(Item)msg.tool());
                                        deliver=false;
                                    }
                                    else
                                        BeanCounter.subtractMoney(mob,BeanCounter.getCurrency(this),amt);
                                    if(deliver)
                                    {
                                        addToBox(postalChain(),(Item)msg.tool(),fromWhom,toWhom,System.currentTimeMillis(),COD);
                                        CommonMsgs.say(this,mob,"I'll deliver that for ya right away!",true,false);
                                        ((Item)msg.tool()).destroy();
                                    }
                                }
                                else
                                {
                                    CommonMsgs.say(this,mob,"That is not a valid player or clan name.",true,false);
                                    autoGive(this,msg.source(),(Item)msg.tool());
                                }
                            }
                            catch(Exception e)
                            {
                                CommonMsgs.drop(mob,msg.tool(),false,false);
                            }
                        }
                        else
                            CommonMsgs.say(this,mob,"Ugh, I can't seem to deliver "+msg.tool().name()+".",true,false);
                    }
                }
                return;
            case CMMsg.TYP_WITHDRAW:
                {
                    String thename=msg.source().Name();
                    if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                        thename=msg.source().getClanID();
                    Item old=(Item)msg.tool();
                    Vector data=findExactBoxData(thename,(Item)msg.tool());
                    if((data==null)
                    &&(whatISell!=ShopKeeper.DEAL_CLANPOSTMAN)
                    &&(msg.source().isMarriedToLiege()))
                        data=findExactBoxData(msg.source().getLiegeID(),(Item)msg.tool());
                    if((data==null)||(data.size()<NUM_PIECES))
                        CommonMsgs.say(this,mob,"You want WHAT? Try LIST.",true,false);
                    else
                    {
                        if((!delFromBox(msg.source(),old))
                        &&(whatISell!=ShopKeeper.DEAL_CLANPOSTMAN)
                        &&(msg.source().isMarriedToLiege()))
                            delFromBox(msg.source().getLiegeID(),old);
                        double totalCharge=getTotalChargeForPiece(data);
                        if((totalCharge>0.0)&&(BeanCounter.getTotalAbsoluteShopKeepersValue(msg.source(),this)>=totalCharge))
                        {
                            Item returnMoney=BeanCounter.makeBestCurrency(this,Util.s_double((String)data.elementAt(PIECE_COD)));
                            if(returnMoney!=null)
                            {
                                BeanCounter.subtractMoney(msg.source(),totalCharge);
                                addToBox(postalChain(),returnMoney,(String)data.elementAt(PIECE_TO),(String)data.elementAt(PIECE_FROM),System.currentTimeMillis(),0.0);
                                CommonMsgs.say(this,mob,"The COD amount of "+returnMoney.Name()+" has been sent back to "+((String)data.elementAt(PIECE_FROM))+".",true,false);
                            }
                        }
                        CommonMsgs.say(this,mob,"There ya go!",true,false);
                        if(location()!=null)
                            location().addItemRefuse(old,Item.REFUSE_PLAYER_DROP);
                        FullMsg msg2=new FullMsg(mob,old,this,CMMsg.MSG_GET,null);
                        if(location().okMessage(mob,msg2))
                            location().send(mob,msg2);
                    }
                }
                return;
            case CMMsg.TYP_VALUE:
            case CMMsg.TYP_SELL:
            case CMMsg.TYP_VIEW:
                super.executeMsg(myHost,msg);
                return;
            case CMMsg.TYP_BUY:
                super.executeMsg(myHost,msg);
                return;
            case CMMsg.TYP_SPEAK:
            {
                super.executeMsg(myHost,msg);
                String str=getMsgFromAffect(msg.targetMessage());
                String theName=msg.source().Name();
                if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                    theName=msg.source().getClanID();
                if((str!=null)&&(str.trim().equalsIgnoreCase("open")))
                {
                    if((whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                    &&((theName.length()==0)
                      ||(Clans.getClan(theName)==null)))
                        CommonMsgs.say(this,mob,"I'm sorry, I only do business with Clans, and you aren't part of one.",true,false);
                    else
                    if(getOurOpenBoxes(theName).containsKey(postalBranch()))
                    {
                        if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                            CommonMsgs.say(this,mob,"Your clan already has a box open here!",true,false);
                        else
                            CommonMsgs.say(this,mob,"You already have a box open here!",true,false);
                    }
                    else
                    if(feeForNewBox()>0.0)
                    {
                        if(BeanCounter.getTotalAbsoluteShopKeepersValue(msg.source(),this)<feeForNewBox())
                        {
                            CommonMsgs.say(this,mob,"Too bad you can't afford it.",true,false);
                            return;
                        }
                        else
                            BeanCounter.subtractMoney(msg.source(),BeanCounter.getCurrency(this),feeForNewBox());
                        createBoxHere(theName,theName);
                        if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                            CommonMsgs.say(this,mob,"A box has been opened for your clan.",true,false);
                        else
                            CommonMsgs.say(this,mob,"A box has been opened for you.",true,false);
                    }
                    else
                    {
                        createBoxHere(theName,theName);
                        if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                            CommonMsgs.say(this,mob,"A box has been opened for your clan.",true,false);
                        else
                            CommonMsgs.say(this,mob,"A box has been opened for you.",true,false);
                    }
                }
                else
                if((str!=null)&&(str.trim().equalsIgnoreCase("close")))
                {
                    if(!getOurOpenBoxes(theName).containsKey(postalBranch()))
                    {
                        if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                            CommonMsgs.say(this,mob,"Your clan does not have a box here!",true,false);
                        else
                            CommonMsgs.say(this,mob,"You don't have a box open here!",true,false);
                    }
                    else
                    if(getAllBoxItemVectors(theName).size()>0)
                        CommonMsgs.say(this,mob,"That box has pending items which must be removed first.",true,false);
                    else
                    {
                        deleteBoxHere(theName);
                        CommonMsgs.say(this,mob,"That box is now closed.",true,false);
                    }
                }
                else
                if((str!=null)&&(str.toUpperCase().trim().startsWith("FORWARD")))
                {
                    str=str.trim().substring(7).trim();
                    if(!getOurOpenBoxes(theName).containsKey(postalBranch()))
                    {
                        if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                            CommonMsgs.say(this,mob,"Your clan does not have a box here!",true,false);
                        else
                            CommonMsgs.say(this,mob,"You don't have a box open here!",true,false);
                    }
                    else
                    {
                        Area A=CMMap.findArea(str);
                        if(A==null)
                            CommonMsgs.say(this,mob,"I don't know of an area called '"+str+"'.",true,false);
                        else
                        {
                            PostOffice P=CMMap.getPostOffice(postalBranch(),A.Name());
                            if(P==null)
                                CommonMsgs.say(this,mob,"I'm sorry, we don't have a branch in "+A.name()+".",true,false);
                            else
                            if(!P.getOurOpenBoxes(theName).containsKey(P.postalBranch()))
                            {
                                if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                                    CommonMsgs.say(this,mob,"I'm sorry, your clan does not have a box at our branch in "+A.name()+".",true,false);
                                else
                                    CommonMsgs.say(this,mob,"I'm sorry, you don't have a box at our branch in "+A.name()+".",true,false);
                            }
                            else
                            {
                                deleteBoxHere(theName);
                                createBoxHere(theName,P.postalBranch());
                                CommonMsgs.say(this,mob,"Ok, mail will now be forwarded to our branch in "+A.name()+".",true,false);
                            }
                        }
                    }
                }
                return;
            }
            case CMMsg.TYP_LIST:
            {
                super.executeMsg(myHost,msg);
                Vector V=null;
                if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                    V=getAllBoxItemVectors(mob.getClanID());
                else
                {
                    V=getAllBoxItemVectors(mob.Name());
                    if(mob.isMarriedToLiege())
                    {
                        Vector V2=getAllBoxItemVectors(mob.getLiegeID());
                        if((V2!=null)&&(V2.size()>0))
                            Util.addToVector(V2,V);
                    }
                }

                StringBuffer str=new StringBuffer("");
                str.append("\n\rItems in your postal box here:\n\r");
                str.append("^x[COD   ][From           ][Sent              ][Item                     ]^.^N\n\r");
                TimeClock C=DefaultTimeClock.globalClock;
                if(getStartRoom()!=null) C=getStartRoom().getArea().getTimeObj();
                boolean codCharge=false;
                for(int i=0;i<V.size();i++)
                {
                    Vector V2=(Vector)V.elementAt(i);
                    if(Util.s_double((String)V2.elementAt(PIECE_COD))>0.0)
                    {
                        codCharge=true;
                        str.append("["+Util.padRight(""+BeanCounter.abbreviatedPrice(this,Util.s_double((String)V2.elementAt(PIECE_COD))),6)+"]");
                    }
                    else
                        str.append("[      ]");
                    str.append("["+Util.padRight((String)V2.elementAt(PIECE_FROM),15)+"]");
                    TimeClock C2=C.deriveClock(Util.s_long((String)V2.elementAt(PIECE_TIME)));
                    str.append("["+Util.padRight(C2.getShortTimeDescription(),18)+"]");
                    str.append("["+Util.padRight(((Item)V2.elementAt(PIECE_MISCDATA)).Name(),25)+"]");
                    mob.tell(str.toString()+"^T");
                    str=new StringBuffer("\n\r^N");
                }
                str.append("\n\r");
                if(codCharge)
                    str.append("* COD charges above include all shipping costs.\n\r");
                str.append("* This branch charges minimum "+BeanCounter.nameCurrencyShort(this,minimumPostage())+" postage for first pound.\n\r");
                str.append("* An additional "+BeanCounter.nameCurrencyShort(this,postagePerPound())+" per pound is charged for packages.\n\r");
                str.append("* A charge of "+BeanCounter.nameCurrencyShort(this,holdFeePerPound())+" per pound per month is charged for holding.\n\r");
                str.append("To forward your mail, 'say \""+name()+"\" \"forward <areaname>\"'.  Use 'say \""+name()+"\" close' to close your box.\n\r");
                mob.tell(str.toString());
                return;
            }
            default:
                break;
            }
        }
        else
        if(msg.sourceMinor()==CMMsg.TYP_RETIRE)
            emptyBox(msg.source().Name());
        super.executeMsg(myHost,msg);
    }

    public boolean okMessage(Environmental myHost, CMMsg msg)
    {
        MOB mob=msg.source();
        if(msg.amITarget(this))
        {
            switch(msg.targetMinor())
            {
            case CMMsg.TYP_GIVE:
            case CMMsg.TYP_DEPOSIT:
                {
                    if(!ignoreIfNecessary(msg.source())) 
                        return false;
                    if(msg.tool()==null) return false;
                    if((whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                    &&((msg.source().getClanID().length()==0)
                      ||(Clans.getClan(msg.source().getClanID())==null)))
                    {
                        CommonMsgs.say(this,mob,"I'm sorry, I only do business with Clans, and you aren't part of one.",true,false);
                        return false;
                    }
                    if(!(msg.tool() instanceof Item))
                    {
                        mob.tell(mob.charStats().HeShe()+" doesn't look interested.");
                        return false;
                    }
                }
                return true;
            case CMMsg.TYP_WITHDRAW:
                {
                    if(!ignoreIfNecessary(msg.source())) 
                        return false;
                    String thename=msg.source().Name();
                    if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                    {
                        thename=msg.source().getClanID();
                        Clan C=Clans.getClan(msg.source().getClanID());
                        if((msg.source().getClanID().length()==0)
                          ||(C==null))
                        {
                            CommonMsgs.say(this,mob,"I'm sorry, I only do business with Clans, and you aren't part of one.",true,false);
                            return false;
                        }

                        if(C.allowedToDoThis(msg.source(),Clan.FUNC_CLANWITHDRAW)<0)
                        {
                            CommonMsgs.say(this,mob,"I'm sorry, you aren't authorized by your clan to do that.",true,false);
                            return false;
                        }
                    }
                    if((msg.tool()==null)||(!(msg.tool() instanceof Item)))
                    {
                        CommonMsgs.say(this,mob,"What do you want? I'm busy!",true,false);
                        return false;
                    }
                    if((msg.tool()!=null)&&(!msg.tool().okMessage(myHost,msg)))
                        return false;
                    Vector data=findExactBoxData(thename,(Item)msg.tool());
                    if((data==null)
                    &&(whatISell!=ShopKeeper.DEAL_CLANPOSTMAN)
                    &&(msg.source().isMarriedToLiege()))
                        data=findExactBoxData(msg.source().getLiegeID(),(Item)msg.tool());
                    if((data==null)||(data.size()<NUM_PIECES))
                    {
                        CommonMsgs.say(this,mob,"You want WHAT? Try LIST.",true,false);
                        return false;
                    }
                    double totalCharge=getTotalChargeForPiece(data);
                    if(BeanCounter.getTotalAbsoluteShopKeepersValue(msg.source(),this)<totalCharge)
                    {
                        CommonMsgs.say(this,mob,"The total charge to receive that item is "+BeanCounter.nameCurrencyShort(this,totalCharge)+". You don't have enough." ,true,false);
                        return false;
                    }
                }
                return true;
            case CMMsg.TYP_VALUE:
            case CMMsg.TYP_SELL:
            case CMMsg.TYP_VIEW:
                return super.okMessage(myHost,msg);
            case CMMsg.TYP_BUY:
                return super.okMessage(myHost,msg);
            case CMMsg.TYP_LIST:
            {
                if(!ignoreIfNecessary(msg.source())) 
                    return false;
                String thename=msg.source().Name();
                if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                {
                    thename=msg.source().getClanID();
                    Clan C=Clans.getClan(msg.source().getClanID());
                    if((msg.source().getClanID().length()==0)
                      ||(C==null))
                    {
                        CommonMsgs.say(this,mob,"I'm sorry, I only do business with Clans, and you aren't part of one.",true,false);
                        return false;
                    }
                    if(C.allowedToDoThis(msg.source(),Clan.FUNC_CLANDEPOSITLIST)<0)
                    {
                        CommonMsgs.say(this,mob,"I'm sorry, you aren't authorized by your clan to do that.",true,false);
                        return false;
                    }
                }
                else
                if((!getOurOpenBoxes(thename).containsKey(postalBranch()))
                &&((whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                   ||(!msg.source().isMarriedToLiege())
                   ||(!getOurOpenBoxes(msg.source().getLiegeID()).containsKey(postalBranch()))))
                {
                    if((whatISell!=ShopKeeper.DEAL_CLANPOSTMAN)
                    &&(msg.source().getStartRoom().getArea()==getStartRoom().getArea()))
                    {
                        createBoxHere(msg.source().Name(),msg.source().Name());
                        return true;
                    }
                    StringBuffer str=new StringBuffer("");
                    if(whatISell==ShopKeeper.DEAL_CLANPOSTMAN)
                        str.append("The Clan "+thename+" does not have a postal box at this branch, I'm afraid.");
                    else
                        str.append("You don't have a postal box at this branch, I'm afraid.");
                    if(postalChain().length()>0)
                        str.append("\n\rThis branch is part of the "+postalChain()+" postal chain.");
                    CommonMsgs.say(this,mob,str.toString()+"^T",true,false);
                    mob.tell("Use 'say \""+name()+"\" open' to open a box here"+((feeForNewBox()<=0.0)?".":(" for "+BeanCounter.nameCurrencyShort(this,feeForNewBox())+".")));
                    return false;
                }
                else
                    return true;
            }
            default:
                break;
            }
        }
        return super.okMessage(myHost,msg);
    }
}
