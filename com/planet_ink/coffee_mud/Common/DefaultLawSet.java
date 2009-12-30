package com.planet_ink.coffee_mud.Common;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

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
public class DefaultLawSet implements Law
{
    public String ID(){return "DefaultLawSet";}
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultLawSet();}}
    public void initializeClass(){}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public CMObject copyOf()
    {
        try
        {
            return (DefaultLawSet)this.clone();
        }
        catch(CloneNotSupportedException e)
        {
            return newInstance();
        }
    }

    private boolean namesModifiable=false;
    private boolean lawsModifiable=false;
    private LegalBehavior legalDetails=null;

    private Vector otherCrimes=new Vector();
    private Vector otherBits=new Vector();
    private Vector bannedSubstances=new Vector();
    private Vector bannedBits=new Vector();
    private Hashtable abilityCrimes=new Hashtable();
    private Hashtable basicCrimes=new Hashtable();
    private Hashtable taxLaws=new Hashtable();

    private Vector chitChat=new Vector();
    private Vector chitChat2=new Vector();
    private Vector chitChat3=new Vector();
    private Vector jailRooms=new Vector();
    private Vector releaseRooms=new Vector();
    private Vector officerNames=new Vector();
    private Vector judgeNames=new Vector();
    private String[] messages=new String[Law.MSG_TOTAL];

    private boolean activated=true;

    private Vector oldWarrants=new Vector();
    private Vector warrants=new Vector();

    private boolean arrestMobs=false;

    private Properties theLaws=null;
    private int lastMonthChecked=-1;

    private String[] paroleMessages=new String[4];
    private Integer[] paroleTimes=new Integer[4];

    private String[] jailMessages=new String[4];
    private Integer[] jailTimes=new Integer[4];

    public void initialize(LegalBehavior details, Properties laws, boolean modifiableNames, boolean modifiableLaws)
    {
        legalDetails=details;
        namesModifiable=modifiableNames;
        lawsModifiable=modifiableLaws;
        resetLaw(laws);
    }

    public Vector otherCrimes() { return otherCrimes;}
    public Vector otherBits() { return otherBits;}
    public Vector bannedSubstances() { return bannedSubstances;}
    public Vector bannedBits() { return bannedBits;}
    public Hashtable abilityCrimes(){ return abilityCrimes;}
    public Hashtable basicCrimes(){ return basicCrimes;}
    public Hashtable taxLaws(){return taxLaws;}

    public boolean hasModifiableNames(){return namesModifiable;}
    public boolean hasModifiableLaws(){return lawsModifiable;}

    public Vector chitChat(){ return chitChat;}
    public Vector chitChat2(){ return chitChat2;}
    public Vector chitChat3(){ return chitChat3;}
    public Vector jailRooms(){ return jailRooms;}
    public Vector releaseRooms(){ return releaseRooms;}
    public Vector officerNames(){ return officerNames;}
    public Vector judgeNames(){ return judgeNames;}
    public String[] messages(){ return messages;}

    public Vector oldWarrants(){ return oldWarrants;}
    public Vector warrants(){ return warrants;}

    public boolean arrestMobs(){ return arrestMobs;}

    public String[] paroleMessages(){ return paroleMessages;}
    public Integer[] paroleTimes(){ return paroleTimes;}

    public String[] jailMessages(){ return jailMessages;}
    public Integer[] jailTimes(){ return jailTimes;}


    public void changeStates(LegalWarrant W, int state)
    {
        if((W==null)||(W.criminal()==null)) return;
        if(warrants.contains(W))
            for(int w=0;w<warrants.size();w++)
            {
                LegalWarrant W2=(LegalWarrant)warrants.elementAt(w);
                if(W2.criminal()==W.criminal())
                    W2.setState(state);
            }
    }

    public Environmental[] getTreasuryNSafe(Area A)
    {
        Room treasuryR=null;
        Item container=null;
        String tres=(String)taxLaws().get("TREASURY");
        if((tres!=null)&&(tres.length()>0))
        {
            Vector V=CMParms.parseSemicolons(tres,false);
            if(V.size()>0)
            {
                Room R=null;
                String room=(String)V.firstElement();
                String item="";
                if(V.size()>1) item=CMParms.combine(V,1);
                if(!room.equalsIgnoreCase("*"))
                {
                    treasuryR=CMLib.map().getRoom(room);
                    if(treasuryR!=null)
                        container=treasuryR.fetchAnyItem(item);
                }
                else
                if(item.length()>0)
                for(Enumeration e=A.getMetroMap();e.hasMoreElements();)
                {
                    R=(Room)e.nextElement();
                    if(R.fetchAnyItem(item) instanceof Container)
                    {
                        container=R.fetchAnyItem(item);
                        treasuryR=R;
                        break;
                    }
                }
                if((room.length()>0)&&(treasuryR==null))
                    treasuryR=A.getRandomMetroRoom();
            }
        }
        Environmental[] ES=new Environmental[2];
        ES[0]=treasuryR;
        ES[1]=container;
        return ES;
    }

    public void propertyTaxTick(Area A, boolean debugging)
    {
        if(lastMonthChecked!=A.getTimeObj().getMonth())
        {
            lastMonthChecked=A.getTimeObj().getMonth();
            double tax=CMath.s_double((String)taxLaws.get("PROPERTYTAX"));
            if(tax==0.0) return;
            tax=CMath.div(tax,100.0);
            Vector titles=CMLib.law().getAllUniqueTitles(A.getMetroMap(),"*",false);
            Hashtable owners=new Hashtable();
            for(Enumeration e=titles.elements();e.hasMoreElements();)
            {
                LandTitle T=(LandTitle)e.nextElement();
                Vector D=(Vector)owners.get(T.landOwner());
                if(D==null)
                {
                    D=new Vector();
                    owners.put(T.landOwner(),D);
                }
                D.addElement(T);
            }
            titles=null;
            Environmental[] Treas=getTreasuryNSafe(A);
            Room treasuryR=(Room)Treas[0];
            Item container=(Item)Treas[1];
            String[] evasionBits=(String[])taxLaws().get("TAXEVASION");

            for(Enumeration e=owners.keys();e.hasMoreElements();)
            {
                String owner=(String)e.nextElement();
                MOB responsibleMob=null;
                Clan C=CMLib.clans().getClan(owner);
                if(C!=null)
                    responsibleMob=C.getResponsibleMember();
                else
                    responsibleMob=CMLib.players().getLoadPlayer(owner);
                Vector particulars=(Vector)owners.get(owner);

                double totalValue=0;
                double paid=0;
                double owed=0;
                StringBuffer properties=new StringBuffer("");
                LandTitle T=null;
                Vector propertyRooms=null;

                for(int p=0;p<particulars.size();p++)
                {
                    if(p>0) properties.append(", ");
                    T=((LandTitle)particulars.elementAt(p));
                    propertyRooms=T.getPropertyRooms();
                    if((propertyRooms.size()<2)
                    ||(CMLib.map().getArea(T.landPropertyID())!=null))
                        properties.append(T.landPropertyID());
                    else
                        properties.append("around "+CMLib.map().getExtendedRoomID((Room)propertyRooms.firstElement()));
                    totalValue+=(double)T.landPrice();
                    if(T.backTaxes()>0)
                    {
                        totalValue+=(double)T.backTaxes();
                        owed+=(double)T.backTaxes();
                    }
                }
                owed+=CMath.mul(totalValue,tax);

                if(owed>0)
                for(int p=0;p<particulars.size();p++)
                {
                    T=((LandTitle)particulars.elementAt(p));
                    if(T.backTaxes()<0)
                    {
                        if((-T.backTaxes())>=owed)
                        {
                            paid+=owed;
                            T.setBackTaxes((int)Math.round(((double)T.backTaxes())+owed));
                            T.updateTitle();
                            break;
                        }
                        paid+=(double)(-T.backTaxes());
                        T.setBackTaxes(0);
                        T.updateTitle();
                    }
                }
                if(owed>0)
                {
                    owed-=paid;
                    if((owed>0)&&(!CMLib.beanCounter().modifyLocalBankGold(A,
                                    owner,
                                    CMLib.utensils().getFormattedDate(A)+": Withdrawal of "+owed+": Taxes on property: "+properties.toString(),
                                    CMLib.beanCounter().getCurrency(A),
                                   -owed)))
                    {
                        boolean owesButNotConfiscated=false;
                        for(int p=0;p<particulars.size();p++)
                        {
                            T=(LandTitle)particulars.elementAt(p);
                            double owedOnThisLand=CMath.mul(T.landPrice(),tax);
                            owedOnThisLand-=(paid/particulars.size());
                            if(owedOnThisLand>0)
                            {
                                T.setBackTaxes((int)Math.round(((double)T.backTaxes())+owedOnThisLand));
                                if((T.landPrice()/T.backTaxes())<4)
                                {
                                    if(CMLib.clans().getClan(T.landOwner())!=null)
                                    {
                                        Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
                                        for(int i=0;i<channels.size();i++)
                                            CMLib.commands().postChannel((String)channels.elementAt(i),T.landOwner(),T.landOwner()+" has lost the title to "+T.landPropertyID()+" due to failure to pay property taxes.",false);
                                    }
                                    else
                                    if(CMLib.players().getPlayer(T.landOwner())!=null)
                                        CMLib.players().getPlayer(T.landOwner()).tell("You have lost the title to "+T.landPropertyID()+" due to failure to pay property taxes.");
                                    T.setLandOwner("");
                                    T.updateTitle();
                                }
                                else
                                {
                                    owesButNotConfiscated=true;
                                    T.updateTitle();
                                }
                            }
                        }
                        if((owesButNotConfiscated)
                        &&(evasionBits!=null)
                        &&(evasionBits[Law.BIT_CRIMENAME].length()>0)
                        &&(responsibleMob!=null))
                            legalDetails.fillOutWarrant(responsibleMob,
                                                       this,
                                                       A,
                                                       null,
                                                       evasionBits[Law.BIT_CRIMELOCS],
                                                       evasionBits[Law.BIT_CRIMEFLAGS],
                                                       evasionBits[Law.BIT_CRIMENAME],
                                                       evasionBits[Law.BIT_SENTENCE],
                                                       evasionBits[Law.BIT_WARNMSG]);
                    }
                    else
                    {
                        for(int p=0;p<particulars.size();p++)
                        {
                            T=(LandTitle)particulars.elementAt(p);
                            if(T.backTaxes()>0)
                            {
                                T.setBackTaxes(0);
                                T.updateTitle();
                            }
                        }
                        if(owed<0) owed=0;
                        if((treasuryR!=null)&&((owed+paid)>0))
                        {
                            Vector V=CMLib.beanCounter().makeAllCurrency(CMLib.beanCounter().getCurrency(A),owed+paid);
                            for(int v=0;v<V.size();v++)
                            {
                                Coins COIN=(Coins)V.elementAt(v);
                                COIN.setContainer(container);
                                treasuryR.addItem(COIN);
                                COIN.putCoinsBack();
                            }
                        }
                        if((evasionBits!=null)
                        &&(evasionBits[Law.BIT_CRIMENAME].length()>0)
                        &&(responsibleMob!=null))
                            while(getWarrant(responsibleMob,evasionBits[Law.BIT_CRIMENAME],true,debugging)!=null);
                    }
                }
            }

        }
    }

    public String getMessage(int which)
    {
        if((which>=0)&&(which<messages.length)&&(messages[which]!=null))
           return messages[which];
        return "";
    }
    public String paroleMessages(int which)
    {
        if((which>=0)
        &&(which<paroleMessages.length)
        &&(paroleMessages[which]!=null))
           return paroleMessages[which];
        return "";
    }
    public int paroleTimes(int which)
    {
        if((which>=0)
        &&(which<paroleTimes.length)
        &&(paroleTimes[which]!=null))
           return paroleTimes[which].intValue();
        return 0;
    }
    public String jailMessages(int which)
    {
        if((which>=0)
        &&(which<jailMessages.length)
        &&(jailMessages[which]!=null))
           return jailMessages[which];
        return "";
    }
    public int jailTimes(int which)
    {
        if((which>=0)
        &&(which<jailTimes.length)
        &&(jailTimes[which]!=null))
           return jailTimes[which].intValue();
        return 0;
    }

    public String getInternalStr(String msg)
    {
        if((theLaws!=null)&&(theLaws.get(msg)!=null))
            return (String)theLaws.get(msg);
        return "";
    }
    public boolean isInternalStr(String msg)
    {
        if((theLaws!=null)&&(theLaws.get(msg)!=null)) return true;
        return false;
    }
    public void setInternalStr(String tag, String value)
    {
        if(theLaws!=null)
        {
            if(theLaws.get(tag)!=null)
                theLaws.remove(tag);
            theLaws.put(tag,value);
        }
    }
    public boolean lawIsActivated(){ return activated;}

    public void resetLaw()
    {
        if(theLaws!=null)
            resetLaw(theLaws);
    }
    private void resetLaw(Properties laws)
    {
        theLaws=laws;
        activated=(!getInternalStr("ACTIVATED").equalsIgnoreCase("FALSE"));
        officerNames=CMParms.parse(getInternalStr("OFFICERS"));
        chitChat=CMParms.parse(getInternalStr("CHITCHAT"));
        chitChat2=CMParms.parse(getInternalStr("CHITCHAT2"));
        chitChat3=CMParms.parse(getInternalStr("CHITCHAT3"));
        judgeNames=CMParms.parse(getInternalStr("JUDGE"));

        arrestMobs=getInternalStr("ARRESTMOBS").equalsIgnoreCase("true");

        messages=new String[Law.MSG_TOTAL];
        messages[Law.MSG_PREVOFF]=getInternalStr("PREVOFFMSG");
        messages[Law.MSG_WARNING]=getInternalStr("WARNINGMSG");
        messages[Law.MSG_THREAT]=getInternalStr("THREATMSG");
        messages[Law.MSG_EXECUTE]=getInternalStr("EXECUTEMSG");
        messages[Law.MSG_COPKILLER]=isInternalStr("COPKILLERMSG")?getInternalStr("COPKILLERMSG"):"COPKILLER!!!! ARGH!!!!!!";
        messages[Law.MSG_PROTECTEDMASK]=getInternalStr("PROTECTED");
        messages[Law.MSG_TRESPASSERMASK]=getInternalStr("TRESPASSERS");
        messages[Law.MSG_RESISTFIGHT]=getInternalStr("RESISTFIGHTMSG");
        messages[Law.MSG_NORESIST]=getInternalStr("NORESISTMSG");
        messages[Law.MSG_RESISTWARN]=getInternalStr("RESISTWARNMSG");
        messages[Law.MSG_PAROLEDISMISS]=getInternalStr("PAROLEDISMISS");
        messages[Law.MSG_RESIST]=getInternalStr("RESISTMSG");
        messages[Law.MSG_LAWFREE]=getInternalStr("LAWFREE");

        paroleMessages[0]=getInternalStr("PAROLE1MSG");
        paroleMessages[1]=getInternalStr("PAROLE2MSG");
        paroleMessages[2]=getInternalStr("PAROLE3MSG");
        paroleMessages[3]=getInternalStr("PAROLE4MSG");
        paroleTimes[0]=Integer.valueOf(CMath.s_int(getInternalStr("PAROLE1TIME")));
        paroleTimes[1]=Integer.valueOf(CMath.s_int(getInternalStr("PAROLE2TIME")));
        paroleTimes[2]=Integer.valueOf(CMath.s_int(getInternalStr("PAROLE3TIME")));
        paroleTimes[3]=Integer.valueOf(CMath.s_int(getInternalStr("PAROLE4TIME")));

        jailMessages[0]=getInternalStr("JAIL1MSG");
        jailMessages[1]=getInternalStr("JAIL2MSG");
        jailMessages[2]=getInternalStr("JAIL3MSG");
        jailMessages[3]=getInternalStr("JAIL4MSG");
        jailTimes[0]=Integer.valueOf(CMath.s_int(getInternalStr("JAIL1TIME")));
        jailTimes[1]=Integer.valueOf(CMath.s_int(getInternalStr("JAIL2TIME")));
        jailTimes[2]=Integer.valueOf(CMath.s_int(getInternalStr("JAIL3TIME")));
        jailTimes[3]=Integer.valueOf(CMath.s_int(getInternalStr("JAIL4TIME")));

        jailRooms=CMParms.parseSemicolons(getInternalStr("JAIL"),true);
        releaseRooms=CMParms.parseSemicolons(getInternalStr("RELEASEROOM"),true);

        taxLaws.clear();
        String taxLaw=getInternalStr("PROPERTYTAX");
        if(taxLaw.length()>0) taxLaws.put("PROPERTYTAX",taxLaw);
        taxLaw=getInternalStr("TAXEVASION");
        if(taxLaw.length()>0) taxLaws.put("TAXEVASION",getInternalBits(taxLaw));
        taxLaw=getInternalStr("TREASURY");
        if(taxLaw.length()>0) taxLaws.put("TREASURY",taxLaw);
        taxLaw=getInternalStr("SALESTAX");
        if(taxLaw.length()>0) taxLaws.put("SALESTAX",taxLaw);
        taxLaw=getInternalStr("CITTAX");
        if(taxLaw.length()>0) taxLaws.put("CITTAX",taxLaw);

        basicCrimes.clear();
        String basicLaw=getInternalStr("MURDER");
        if(basicLaw.length()>0) basicCrimes.put("MURDER",getInternalBits(basicLaw));
        basicLaw=getInternalStr("RESISTINGARREST");
        if(basicLaw.length()>0) basicCrimes.put("RESISTINGARREST",getInternalBits(basicLaw));
        basicLaw=getInternalStr("NUDITY");
        if(basicLaw.length()>0) basicCrimes.put("NUDITY",getInternalBits(basicLaw));
        basicLaw=getInternalStr("ASSAULT");
        if(basicLaw.length()>0) basicCrimes.put("ASSAULT",getInternalBits(basicLaw));
        basicLaw=getInternalStr("ARMED");
        if(basicLaw.length()>0) basicCrimes.put("ARMED",getInternalBits(basicLaw));
        basicLaw=getInternalStr("TRESPASSING");
        if(basicLaw.length()>0) basicCrimes.put("TRESPASSING",getInternalBits(basicLaw));
        basicLaw=getInternalStr("PROPERTYROB");
        if(basicLaw.length()>0) basicCrimes.put("PROPERTYROB",getInternalBits(basicLaw));

        abilityCrimes.clear();
        otherCrimes.clear();
        otherBits.clear();
        bannedSubstances.clear();
        bannedBits.clear();
        for(Enumeration e=laws.keys();e.hasMoreElements();)
        {
            String key=(String)e.nextElement();
            String words=(String)laws.get(key);
            int x=words.indexOf(";");
            if(x>=0)
            {
                if(key.startsWith("CRIME"))
                {
                    otherCrimes.addElement(CMParms.parse(words.substring(0,x)));
                    String[] bits=new String[Law.BIT_NUMBITS];
                    Vector parsed=CMParms.parseSemicolons(words.substring(x+1),false);
                    for(int i=0;i<Law.BIT_NUMBITS;i++)
                        if(i<parsed.size())
                            bits[i]=(String)parsed.elementAt(i);
                        else
                            bits[i]="";
                    otherBits.addElement(bits);
                }
                else
                if(key.startsWith("BANNED"))
                {
                    bannedSubstances.addElement(CMParms.parse(words.substring(0,x)));
                    String[] bits=new String[Law.BIT_NUMBITS];
                    Vector parsed=CMParms.parseSemicolons(words.substring(x+1),false);
                    for(int i=0;i<Law.BIT_NUMBITS;i++)
                        if(i<parsed.size())
                            bits[i]=(String)parsed.elementAt(i);
                        else
                            bits[i]="";
                    bannedBits.addElement(bits);
                }
                else
                if((key.startsWith("$")&&(CMClass.getAbility(key.substring(1))!=null))
                ||(CMClass.getAbility(key)!=null))
                    abilityCrimes.put(key.toUpperCase(),getInternalBits(words));
            }
        }
    }

    public String rawLawString()
    {
        if(theLaws!=null)
        {
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            try{ theLaws.store(out,"");}catch(IOException e){}
            String s=CMStrings.replaceAll(out.toString(),"\n\r","~");
            s=CMStrings.replaceAll(s,"\r\n","~");
            s=CMStrings.replaceAll(s,"\n","~");
            s=CMStrings.replaceAll(s,"\r","~");
            s=CMStrings.replaceAll(s,"'","`");
            return s;
        }
        return "";
    }

    private String[] getInternalBits(String bitStr)
    {
        String[] bits=new String[Law.BIT_NUMBITS];
        Vector parsed=CMParms.parseSemicolons(bitStr,false);
        for(int i=0;i<Law.BIT_NUMBITS;i++)
            if(i<parsed.size())
                bits[i]=(String)parsed.elementAt(i);
            else
                bits[i]="";
        return bits;
    }

    public LegalWarrant getWarrant(MOB criminal,
                                   String crime,
                                   boolean pull,
                                   boolean debugging)
    {
        LegalWarrant W=null;
        for(int i=0;i<warrants.size();i++)
        {
            LegalWarrant W2=(LegalWarrant)warrants.elementAt(i);
            if((W2.criminal()==criminal)
            &&(W2.crime().equals(crime))
            &&(legalDetails.isStillACrime(W2,debugging)))
            {
                W=W2;
                if(pull) warrants.removeElement(W2);
                break;
            }
        }
        return W;
    }

    public LegalWarrant getCopkiller(Area A, LegalBehavior behav, MOB mob)
    {
		String[] copKillerInfo=(String[])basicCrimes().get("MURDER");
        if(copKillerInfo!=null)
        for(int i=0;i<warrants.size();i++)
        {
            LegalWarrant W=(LegalWarrant)warrants.elementAt(i);
            if((W.criminal()==mob)
            &&(W.crime().equals(copKillerInfo[Law.BIT_CRIMENAME]))
            &&(W.victim()!=null)
            &&(behav!=null)
            &&(behav.isStillACrime(W,false))
            &&(behav.isAnyOfficer(A,W.victim())))
                return W;
        }
        return null;
    }

    public LegalWarrant getLawResister(Area A, LegalBehavior behav, MOB mob)
    {
        String[] lawResistInfo=(String[])basicCrimes().get("RESISTINGARREST");
        if(lawResistInfo!=null)
        for(int i=0;i<warrants.size();i++)
        {
            LegalWarrant W=(LegalWarrant)warrants.elementAt(i);
            if((W.criminal()==mob)
            &&(W.crime().equals(lawResistInfo[Law.BIT_CRIMENAME]))
            &&(W.victim()!=null)
            &&(behav.isStillACrime(W,false))
            &&(behav.isAnyOfficer(A,W.victim())))
                return W;
        }
        return null;
    }


    public LegalWarrant getWarrant(MOB mob, int which)
    {
        int one=0;
        for(int i=0;i<warrants.size();i++)
        {
            LegalWarrant W=(LegalWarrant)warrants.elementAt(i);
            if(W.criminal()==mob)
            {
                if(which==one)
                    return W;
                one++;
            }
        }
        return null;
    }

    public LegalWarrant getOldWarrant(MOB criminal, String crime, boolean pull)
    {
        LegalWarrant W=null;
        for(int i=0;i<oldWarrants.size();i++)
        {
            LegalWarrant W2=(LegalWarrant)oldWarrants.elementAt(i);
            if((W2.criminal()==criminal)&&(W2.crime().equals(crime)))
            {
                W=W2;
                if(pull) oldWarrants.removeElement(W2);
                break;
            }
        }
        return W;
    }

}
