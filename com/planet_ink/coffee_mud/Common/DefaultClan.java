package com.planet_ink.coffee_mud.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.threads.ServiceEngine;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.Clan.MemberRecord;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004-2010 Bo Zimmerman</p>
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * <p>you may not use this file except in compliance with the License.
 * <p>You may obtain a copy of the License at
 *
 * <p>       http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * <p>distributed under the License is distributed on an "AS IS" BASIS,
 * <p>WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>See the License for the specific language governing permissions and
 * <p>limitations under the License.
 */
@SuppressWarnings("unchecked")
public class DefaultClan implements Clan
{
    public String ID(){return "DefaultClan";}
    private long tickStatus=Tickable.STATUS_NOT;
    public long getTickStatus(){return tickStatus;}
    protected String clanName="";
    protected String clanPremise="";
    protected String clanRecall="";
    protected String clanMorgue="";
    protected String clanClass="";
    protected String clanDonationRoom="";
    protected int clanTrophies=0;
    protected int autoPosition=Clan.POS_APPLICANT;
    protected String AcceptanceSettings="";
    protected int clanType=TYPE_CLAN;
    protected int ClanStatus=0;
    protected Vector voteList=null;
    protected long exp=0;
    protected Vector clanKills=new Vector();
    protected String lastClanKillRecord=null;
    protected double taxRate=0.0;

    //*****************
    public Hashtable relations=new Hashtable();
    public int government=GVT_DICTATORSHIP;
    //*****************

    /** return a new instance of the object*/
    public CMObject newInstance(){try{return (CMObject)getClass().newInstance();}catch(Exception e){return new DefaultClan();}}
    public void initializeClass(){}
    public int compareTo(CMObject o){ return CMClass.classID(this).compareToIgnoreCase(CMClass.classID(o));}
    public CMObject copyOf()
    {
        try
        {
            return (Clan)this.clone();
        }
        catch(CloneNotSupportedException e)
        {
            return new DefaultClan();
        }
    }

    private synchronized void clanKills()
    {
        if(lastClanKillRecord==null)
        {
            Vector V=CMLib.database().DBReadData(clanID(),"CLANKILLS",clanID()+"/CLANKILLS");
            clanKills.clear();
            if(V.size()==0)
                lastClanKillRecord="";
            else
            {
                lastClanKillRecord=((DatabaseEngine.PlayerData)V.firstElement()).xml;
                Vector V2=CMParms.parseSemicolons(lastClanKillRecord,true);
                for(int v=0;v<V2.size();v++)
                    clanKills.addElement(Long.valueOf(CMath.s_long((String)V2.elementAt(v))));
            }
        }
    }

    private void updateClanKills()
    {
        Long date=null;
        StringBuffer str=new StringBuffer("");
        for(int i=clanKills.size()-1;i>=0;i--)
        {
            date=(Long)clanKills.elementAt(i);
            if(date.longValue()<(System.currentTimeMillis()))
                clanKills.removeElementAt(i);
            else
                str.append(date.longValue()+";");
        }
        if((lastClanKillRecord==null)||(!lastClanKillRecord.equals(str.toString())))
        {
            lastClanKillRecord=str.toString();
            CMLib.database().DBReCreateData(clanID(),"CLANKILLS",clanID()+"/CLANKILLS",str.toString());
        }
    }

    public void updateVotes()
    {
        StringBuffer str=new StringBuffer("");
        for(Enumeration e=votes();e.hasMoreElements();)
        {
            ClanVote CV=(ClanVote)e.nextElement();
            str.append(CMLib.xml().convertXMLtoTag("BY",CV.voteStarter));
            str.append(CMLib.xml().convertXMLtoTag("FUNC",CV.function));
            str.append(CMLib.xml().convertXMLtoTag("ON",""+CV.voteStarted));
            str.append(CMLib.xml().convertXMLtoTag("STATUS",""+CV.voteStatus));
            str.append(CMLib.xml().convertXMLtoTag("CMD",CV.matter));
            if((CV.votes!=null)&&(CV.votes.size()>0))
            {
                str.append("<VOTES>");
                for(int v=0;v<CV.votes.size();v++)
                {
                    str.append("<VOTE>");
                    str.append(CMLib.xml().convertXMLtoTag("BY",(String)CV.votes.elementAt(v,1)));
                    str.append(CMLib.xml().convertXMLtoTag("YN",""+((Boolean)CV.votes.elementAt(v,2)).booleanValue()));
                    str.append("</VOTE>");
                }
                str.append("</VOTES>");
            }
        }
        if(str.length()>0)
            CMLib.database().DBReCreateData(clanID(),"CLANVOTES",clanID()+"/CLANVOTES","<BALLOTS>"+str.toString()+"</BALLOTS>");
        else
            CMLib.database().DBDeleteData(clanID(),"CLANVOTES",clanID()+"/CLANVOTES");
    }
    public void addVote(Object CV)
    {
        if(!(CV instanceof ClanVote))
            return;
        votes();
        voteList.addElement((ClanVote)CV);
    }
    public void delVote(Object CV)
    {
        votes();
        voteList.removeElement(CV);
    }

    public void recordClanKill()
    {
        clanKills();
        clanKills.addElement(Long.valueOf(System.currentTimeMillis()));
        updateClanKills();
    }
    public int getCurrentClanKills()
    {
        clanKills();
        return clanKills.size();
    }

    public long calculateMapPoints()
    {
        return calculateMapPoints(getControlledAreas());
    }
    public long calculateMapPoints(Vector controlledAreas)
    {
        long points=0;
        for(Enumeration e=controlledAreas.elements();e.hasMoreElements();)
        {
            Area A=(Area)e.nextElement();
            LegalBehavior B=CMLib.law().getLegalBehavior(A);
            if(B!=null)
                points+=B.controlPoints();
        }
        return points;
    }

    public Vector getControlledAreas()
    {
        Vector done=new Vector();
        for(Enumeration e=CMLib.map().sortedAreas();e.hasMoreElements();)
        {
            Area A=(Area)e.nextElement();
            LegalBehavior B=CMLib.law().getLegalBehavior(A);
            if(B!=null)
            {
                String controller=B.rulingOrganization();
                Area A2=CMLib.law().getLegalObject(A);
                if(controller.equals(clanID())&&(!done.contains(A2)))
                    done.addElement(A2);
            }
        }
        return done;
    }

    public Enumeration votes()
    {
        if(voteList==null)
        {
            Vector V=CMLib.database().DBReadData(clanID(),"CLANVOTES",clanID()+"/CLANVOTES");
            voteList=new Vector();
            for(int v=0;v<V.size();v++)
            {
                ClanVote CV=new ClanVote();
                String rawxml=((DatabaseEngine.PlayerData)V.elementAt(v)).xml;
                if(rawxml.trim().length()==0) return voteList.elements();
                Vector xml=CMLib.xml().parseAllXML(rawxml);
                if(xml==null)
                {
                    Log.errOut("Clans","Unable to parse: "+rawxml);
                    return voteList.elements();
                }
                Vector voteData=CMLib.xml().getRealContentsFromPieces(xml,"BALLOTS");
                if(voteData==null){ Log.errOut("Clans","Unable to get BALLOTS data."); return voteList.elements();}
                CV.voteStarter=CMLib.xml().getValFromPieces(voteData,"BY");
                CV.voteStarted=CMLib.xml().getLongFromPieces(voteData,"ON");
                CV.function=CMLib.xml().getIntFromPieces(voteData,"FUNC");
                CV.voteStatus=CMLib.xml().getIntFromPieces(voteData,"STATUS");
                CV.matter=CMLib.xml().getValFromPieces(voteData,"CMD");
                CV.votes=new DVector(2);
                Vector xV=CMLib.xml().getRealContentsFromPieces(voteData,"VOTES");
                if((xV!=null)&&(xV.size()>0))
                {
                    for(int x=0;x<xV.size();x++)
                    {
                        XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
                        if((!iblk.tag.equalsIgnoreCase("VOTE"))||(iblk.contents==null))
                            continue;
                        String userID=CMLib.xml().getValFromPieces(iblk.contents,"BY");
                        boolean yn=CMLib.xml().getBoolFromPieces(iblk.contents,"YN");
                        CV.votes.addElement(userID,Boolean.valueOf(yn));
                    }
                }
                voteList.addElement(CV);
            }
        }
        return voteList.elements();
    }

    public int getAutoPosition(){return autoPosition;}
    public void setAutoPosition(int pos){autoPosition=pos;}

    public long getExp(){return exp;}
    public void setExp(long newexp){exp=newexp;}
    public void adjExp(int howMuch)
    {
        exp=exp+howMuch;
        if(exp<0) exp=0;
    }

    public int getTrophies(){return clanTrophies;}
    public void setTrophies(int trophyFlag){clanTrophies=trophyFlag;}

    public void setTaxes(double rate)
    {
        taxRate=rate;
    }
    public double getTaxes(){return taxRate;}

    public int getClanRelations(String id)
    {
        long i[]=(long[])relations.get(id.toUpperCase());
        if(i!=null) return (int)i[0];
        return  REL_NEUTRAL;
    }

    public long getLastRelationChange(String id)
    {
        long i[]=(long[])relations.get(id.toUpperCase());
        if(i!=null) return i[1];
        return 0;
    }
    public void setClanRelations(String id, int rel, long time)
    {
        relations.remove(id.toUpperCase());
        long[] i=new long[2];
        i[0]=rel;
        i[1]=time;
        relations.put(id.toUpperCase(),i);
    }

    public int getGovernment(){return government;}
    public void setGovernment(int type){government=type;}

    public void create()
    {
        CMLib.database().DBCreateClan(this);
        CMLib.clans().addClan(this);
    }

    public void update()
    {
        CMLib.database().DBUpdateClan(this);
    }

    public void addMember(MOB M, int role)
    {
        M.setClanID(clanID());
        M.setClanRole(role);
        CMLib.database().DBUpdateClanMembership(M.Name(), M.getClanID(), role);
        updateClanPrivileges(M);
    }
    public void delMember(MOB M)
    {
        CMLib.database().DBUpdateClanMembership(M.Name(), "", 0);
        M.setClanID("");
        M.setClanRole(0);
        updateClanPrivileges(M);
    }

    public boolean updateClanPrivileges(MOB M)
    {
        boolean did=false;
        if(M==null) return false;
        if(M.getClanID().equals(clanID())
        &&(M.getClanRole()!=POS_APPLICANT))
        {
        	if(getClanClassC()!=null)
        	{
        		CharClass CC=getClanClassC();
            	if(M.baseCharStats().getCurrentClass()!=CC)
            	{
            		M.baseCharStats().setCurrentClass(CC);
            		did=true;
            		M.recoverCharStats();
            	}
        	}
            if(M.fetchAbility("Spell_ClanHome")==null)
            {
                M.addAbility(CMClass.findAbility("Spell_ClanHome"));
                (M.fetchAbility("Spell_ClanHome")).setProficiency(50);
                did=true;
            }
            if(M.fetchAbility("Spell_ClanDonate")==null)
            {
                M.addAbility(CMClass.findAbility("Spell_ClanDonate"));
                (M.fetchAbility("Spell_ClanDonate")).setProficiency(100);
                did=true;
            }
        }
        else
        {
            if(M.fetchAbility("Spell_ClanHome")!=null)
            {
                did=true;
                M.delAbility(M.fetchAbility("Spell_ClanHome"));
            }
            if(M.fetchAbility("Spell_ClanDonate")!=null)
            {
                did=true;
                M.delAbility(M.fetchAbility("Spell_ClanDonate"));
            }
        }
        if(((M.getClanID().equals(clanID())))
        &&(allowedToDoThis(M,FUNC_CLANCANORDERCONQUERED)>0))
        {
            if(M.fetchAbility("Spell_Flagportation")==null)
            {
                M.addAbility(CMClass.findAbility("Spell_Flagportation"));
                (M.fetchAbility("Spell_Flagportation")).setProficiency(100);
                did=true;
            }
        }
        else
        if(M.fetchAbility("Spell_Flagportation")!=null)
        {
            did=true;
            M.delAbility(M.fetchAbility("Spell_Flagportation"));
        }

        if(M.playerStats()!=null)
        for(int i=0;i<POSORDER.length;i++)
        {
            int pos=POSORDER[i];
            String title="*, "+CMLib.clans().getRoleName(getGovernment(),pos,true,false)+" of "+name();
            if((M.getClanRole()==pos)
            &&(M.getClanID().equals(clanID()))
            &&(pos!=POS_APPLICANT))
            {
                if(!M.playerStats().getTitles().contains(title))
                    M.playerStats().getTitles().addElement(title);
            }
            else
            if(M.playerStats().getTitles().contains(title))
                M.playerStats().getTitles().remove(title);
        }
        if(M.getClanID().length()==0)
        {
            Item I=null;
            Vector itemsToMove=new Vector();
            for(int i=0;i<M.inventorySize();i++)
            {
                I=M.fetchInventory(i);
                if(I instanceof ClanItem)
                    itemsToMove.addElement(I);
            }
            for(int i=0;i<itemsToMove.size();i++)
            {
                I=(Item)itemsToMove.elementAt(i);
                Room R=null;
                if((getDonation()!=null)
                &&(getDonation().length()>0))
                    R=CMLib.map().getRoom(getDonation());
                if((R==null)
                &&(getRecall()!=null)
                &&(getRecall().length()>0))
                    R=CMLib.map().getRoom(getRecall());
                if(I instanceof Container)
                {
                    Vector V=((Container)I).getContents();
                    for(int v=0;v<V.size();v++)
                        ((Item)V.elementAt(v)).setContainer(null);
                }
                I.setContainer(null);
                I.wearAt(Wearable.IN_INVENTORY);
                if(R!=null)
                    R.bringItemHere(I,0,false);
                else
                if(M.isMine(I))
                    I.destroy();
                did=true;
            }
        }
        if((did)&&(!CMSecurity.isSaveFlag("NOPLAYERS")))
            CMLib.database().DBUpdatePlayer(M);
        return did;
    }

    public void destroyClan()
    {
    	List<MemberRecord> members=getMemberList();
        for(MemberRecord member : members)
        {
            MOB M=CMLib.players().getLoadPlayer(member.name);
            if(M!=null)
            {
                M.setClanID("");
                M.setClanRole(0);
                updateClanPrivileges(M);
                CMLib.database().DBUpdateClanMembership(M.Name(), "", 0);
            }
        }
        CMLib.database().DBDeleteClan(this);
        CMLib.clans().removeClan(this);
    }

    protected CharClass getClanClassC()
    {
    	if(clanClass.length()==0) return null;
		CharClass C=CMClass.getCharClass(clanClass);
		if(C==null)C=CMClass.findCharClass(clanClass);
    	return C;
    }

    public String getDetail(MOB mob)
    {
        StringBuffer msg=new StringBuffer("");
        boolean member=(mob!=null)&&mob.getClanID().equalsIgnoreCase(clanID())&&(mob.getClanRole()>Clan.POS_APPLICANT);
        boolean sysmsgs=(mob!=null)&&CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS);
        msg.append("^x"+typeName()+" Profile   :^.^N "+clanID()+"\n\r"
                  +"-----------------------------------------------------------------\n\r"
                  +getPremise()+"\n\r"
                  +"-----------------------------------------------------------------\n\r"
                  +"^xType            :^.^N "+CMStrings.capitalizeAndLower(GVT_DESCS[getGovernment()])+"\n\r"
                  +"^xQualifications  :^.^N "+((getAcceptanceSettings().length()==0)?"Anyone may apply":CMLib.masking().maskDesc(getAcceptanceSettings()))+"\n\r");
        CharClass clanC=getClanClassC();
        if(clanC!=null) msg.append("^xClass           :^.^N "+clanC.name()+"\n\r");
        msg.append("^xExp. Tax Rate   :^.^N "+((int)Math.round(getTaxes()*100))+"%\n\r");
        if(member||sysmsgs)
        {
            msg.append("^xExperience Pts. :^.^N "+getExp()+"\n\r");
            if(getMorgue().length()>0)
            {
                Room R=CMLib.map().getRoom(getMorgue());
                if(R!=null)
                    msg.append("^xMorgue          :^.^N "+R.displayText()+"\n\r");
            }
            if(getDonation().length()>0)
            {
                Room R=CMLib.map().getRoom(getDonation());
                if(R!=null)
                    msg.append("^xDonations       :^.^N "+R.displayText()+"\n\r");
            }
            if(getRecall().length()>0)
            {
                Room R=CMLib.map().getRoom(getRecall());
                if(R!=null)
                    msg.append("^xRecall          :^.^N "+R.displayText()+"\n\r");
            }
        }
        msg.append("^x"+CMStrings.padRight(CMLib.clans().getRoleName(getGovernment(),POS_BOSS,true,true),16)+":^.^N "+crewList(POS_BOSS)+"\n\r"
                  +"^x"+CMStrings.padRight(CMLib.clans().getRoleName(getGovernment(),POS_LEADER,true,true),16)+":^.^N "+crewList(POS_LEADER)+"\n\r"
                  +"^x"+CMStrings.padRight(CMLib.clans().getRoleName(getGovernment(),POS_TREASURER,true,true),16)+":^.^N "+crewList(POS_TREASURER)+"\n\r"
                  +"^x"+CMStrings.padRight(CMLib.clans().getRoleName(getGovernment(),POS_ENCHANTER,true,true),16)+":^.^N "+crewList(POS_ENCHANTER)+"\n\r"
                  +"^x"+CMStrings.padRight(CMLib.clans().getRoleName(getGovernment(),POS_STAFF,true,true),16)+":^.^N "+crewList(POS_STAFF)+"\n\r"
                  +"^xTotal Members   :^.^N "+getSize()+"\n\r");
        if(CMLib.clans().numClans()>1)
        {
            msg.append("-----------------------------------------------------------------\n\r");
            msg.append("^x"+CMStrings.padRight("Clan Relations",16)+":^.^N \n\r");
            for(Enumeration e=CMLib.clans().allClans();e.hasMoreElements();)
            {
                Clan C=(Clan)e.nextElement();
                if(C!=this)
                {
                    msg.append("^x"+CMStrings.padRight(C.name(),16)+":^.^N ");
                    msg.append(CMStrings.capitalizeAndLower(REL_DESCS[getClanRelations(C.clanID())]));
                    int orel=C.getClanRelations(clanID());
                    if(orel!=REL_NEUTRAL)
                        msg.append(" (<-"+CMStrings.capitalizeAndLower(REL_DESCS[orel])+")");
                    msg.append("\n\r");
                }
            }
        }
        if(member||sysmsgs)
        {
            if(member) updateClanPrivileges(mob);
            msg.append("-----------------------------------------------------------------\n\r"
                      +"^x"+CMStrings.padRight(CMLib.clans().getRoleName(getGovernment(),POS_MEMBER,true,true),16)
                      +":^.^N "+crewList(POS_MEMBER)+"\n\r");
            if((allowedToDoThis(mob,FUNC_CLANACCEPT)>=0)||sysmsgs)
            {
                msg.append("-----------------------------------------------------------------\n\r"
                        +"^x"+CMStrings.padRight(CMLib.clans().getRoleName(getGovernment(),POS_APPLICANT,true,true),16)+":^.^N "+crewList(POS_APPLICANT)+"\n\r");
            }
        }
        Vector control=new Vector();
        Vector controlledAreas=getControlledAreas();
        long controlPoints=calculateMapPoints(controlledAreas);
        for(Enumeration e=controlledAreas.elements();e.hasMoreElements();)
            control.addElement(((Area)e.nextElement()).name());
        if(control.size()>0)
        {
            msg.append("-----------------------------------------------------------------\n\r");
            msg.append("^xClan Controlled Areas (% revolt):^.^N\n\r");
            Collections.sort(control);
            int col=0;
            for(int i=0;i<control.size();i++)
            {
                if((++col)>3)
                {
                    msg.append("\n\r");
                    col=1;
                }
                Area A=CMLib.map().getArea((String)control.elementAt(i));
                if(A!=null)
                {
                	LegalBehavior B=CMLib.law().getLegalBehavior(A);
                	Area legalA=CMLib.law().getLegalObject(A);
                	int pctRevolt=0;
                	if((B!=null)&&(legalA!=null)) pctRevolt=B.revoltChance();
	                msg.append("^c"+CMStrings.padRight(A.name()+"^N ("+pctRevolt+"%)",25)+"^N");
                }
            }
            msg.append("\n\r");
        }
        if((CMLib.clans().trophySystemActive())&&(getTrophies()!=0))
        {
            msg.append("-----------------------------------------------------------------\n\r");
            msg.append("^xTrophies awarded:^.^N\n\r");
            for(int i=0;i<TROPHY_DESCS.length;i++)
                if((TROPHY_DESCS[i].length()>0)&&(CMath.bset(getTrophies(),i)))
                {
                    msg.append(TROPHY_DESCS[i]+" ");
                    switch(i){
                        case TROPHY_AREA: msg.append("("+control.size()+") "); break;
                        case TROPHY_CONTROL: msg.append("("+controlPoints+") "); break;
                        case TROPHY_EXP: msg.append("("+getExp()+") "); break;
                    }
                    msg.append(" Prize: "+CMLib.clans().translatePrize(i)+"\n\r");
                }
        }
        return msg.toString();
    }

    private String crewList(int posType)
    {
        StringBuffer list=new StringBuffer("");
        List<MemberRecord> members = getMemberList(posType);
        if(members.size()>1)
        {
            for(int j=0;j<(members.size() - 1);j++)
                list.append(members.get(j).name+", ");
            list.append("and "+members.get(members.size()-1).name);
        }
        else
        if(members.size()>0)
            list.append(members.get(0).name);
        return list.toString();
    }

    public String typeName()
    {
        switch(clanType)
        {
        case TYPE_CLAN:
            if((getGovernment()>=0)&&(getGovernment()<GVT_DESCS.length))
                return CMStrings.capitalizeAndLower(GVT_DESCS[getGovernment()].toLowerCase());
        }
        return "Clan";
    }

	public boolean canBeAssigned(MOB mob, int role)
	{
        if(mob==null) return false;
        if(role<0) return false;
        switch(government)
        {
        case GVT_OLIGARCHY:
    		return true;
        case GVT_REPUBLIC:
    		return true;
        case GVT_DEMOCRACY:
    		return true;
        case GVT_THEOCRACY:
        	if(role>=POS_LEADER)
        	{
        		if((mob.baseCharStats().getCurrentClass().baseClass().equalsIgnoreCase("Cleric"))
        		||(mob.baseCharStats().getCurrentClass().baseClass().equalsIgnoreCase("Priest"))
        		||(mob.baseCharStats().getCurrentClass().baseClass().equalsIgnoreCase("Clergy")))
        			return true;
        		return false;
        	}
    		return true;
        case GVT_DICTATORSHIP:
    		return true;
        case GVT_FAMILY:
            if((role == POS_LEADER)||(role == POS_TREASURER)) {
                if(mob.baseCharStats().getStat(CharStats.STAT_GENDER)!='F')
                    return false;
            } 
            else 
            if((role == POS_BOSS)||(role == POS_ENCHANTER)) {
                if(mob.baseCharStats().getStat(CharStats.STAT_GENDER) == 'F')
                    return false;
            }
            return true; // technically requires some sort of familial check, but this will be enforced elsewhere.
        }
		return false;
	}

    public int allowedToDoThis(MOB mob, int function)
    {
        if(mob==null) return -1;
        int role=mob.getClanRole();
        if(role==POS_APPLICANT) return -1;
        String[] funcs=FUNC_PROCEDURE[government];
        char[] who=funcs[function].toCharArray();
        if(who.length==0) return -1;
        if(who[0]=='V') return 0;
        if(who[0]=='A') return 1;
        for(int c=0;c<who.length;c++)
            switch(who[c])
            {
            case 'B': if(role==POS_BOSS) return 1; break;
            case 'L': if(role==POS_LEADER) return 1; break;
            case 'E': if(role==POS_ENCHANTER) return 1; break;
            case 'T': if(role==POS_TREASURER) return 1; break;
            case 'S': if(role==POS_STAFF) return 1; break;
            case 'M': if(role==POS_MEMBER) return 1; break;
            }
        return -1;
    }

    public List<MemberRecord> getRealMemberList(int PosFilter)
    {
    	List<MemberRecord> members=getMemberList(PosFilter);
        if(members==null) return null;
        List<MemberRecord> realMembers=new Vector<MemberRecord>();
        for(MemberRecord member : members)
            if(CMLib.players().playerExists(member.name))
            	realMembers.add(member);
        return members;
    }

    public int getSize() { return getMemberList(-1).size();}

    public String name() {return clanName;}
    public String getName() {return clanName;}
    public String clanID() {return clanName;}
    public void setName(String newName) {clanName = newName; }
    public int getType() {return clanType;}

    public String getPremise() {return clanPremise;}
    public void setPremise(String newPremise){ clanPremise = newPremise;}

    public String getAcceptanceSettings() { return AcceptanceSettings; }
    public void setAcceptanceSettings(String newSettings) { AcceptanceSettings=newSettings; }

    public String getClanClass(){return clanClass;}
    public void setClanClass(String newClass){clanClass=newClass;}

    public String getPolitics() 
    {
        StringBuffer str=new StringBuffer("");
        str.append("<POLITICS>");
        str.append(CMLib.xml().convertXMLtoTag("GOVERNMENT",""+getGovernment()));
        str.append(CMLib.xml().convertXMLtoTag("TAXRATE",""+getTaxes()));
        str.append(CMLib.xml().convertXMLtoTag("EXP",""+getExp()));
        str.append(CMLib.xml().convertXMLtoTag("CCLASS",""+getClanClass()));
        str.append(CMLib.xml().convertXMLtoTag("AUTOPOS",""+getAutoPosition()));
        if(relations.size()==0)
            str.append("<RELATIONS/>");
        else
        {
            str.append("<RELATIONS>");
            for(Enumeration e=relations.keys();e.hasMoreElements();)
            {
                String key=(String)e.nextElement();
                str.append("<RELATION>");
                str.append(CMLib.xml().convertXMLtoTag("CLAN",key));
                long[] i=(long[])relations.get(key);
                str.append(CMLib.xml().convertXMLtoTag("STATUS",""+i[0]));
                str.append("</RELATION>");
            }
            str.append("</RELATIONS>");
        }
        str.append("</POLITICS>");
        return str.toString();
    }
    
    public void setPolitics(String politics)
    {
        relations.clear();
        government=GVT_DICTATORSHIP;
        if(politics.trim().length()==0) return;
        Vector xml=CMLib.xml().parseAllXML(politics);
        if(xml==null)
        {
            Log.errOut("Clans","Unable to parse: "+politics);
            return;
        }
        Vector poliData=CMLib.xml().getRealContentsFromPieces(xml,"POLITICS");
        if(poliData==null){ Log.errOut("Clans","Unable to get POLITICS data."); return;}
        government=CMLib.xml().getIntFromPieces(poliData,"GOVERNMENT");
        exp=CMLib.xml().getLongFromPieces(poliData,"EXP");
        taxRate=CMLib.xml().getDoubleFromPieces(poliData,"TAXRATE");
        clanClass=CMLib.xml().getValFromPieces(poliData,"CCLASS");
        autoPosition=CMLib.xml().getIntFromPieces(poliData,"AUTOPOS");

        // now RESOURCES!
        Vector xV=CMLib.xml().getRealContentsFromPieces(poliData,"RELATIONS");
        if((xV!=null)&&(xV.size()>0))
        {
            for(int x=0;x<xV.size();x++)
            {
                XMLLibrary.XMLpiece iblk=(XMLLibrary.XMLpiece)xV.elementAt(x);
                if((!iblk.tag.equalsIgnoreCase("RELATION"))||(iblk.contents==null))
                    continue;
                String relClanID=CMLib.xml().getValFromPieces(iblk.contents,"CLAN");
                int rel=CMLib.xml().getIntFromPieces(iblk.contents,"STATUS");
                setClanRelations(relClanID,rel,0);
            }
        }
    }

    public int getStatus() { return ClanStatus; }
    public void setStatus(int newStatus) { ClanStatus=newStatus; }

    public String getRecall() { return clanRecall; }
    public void setRecall(String newRecall) { clanRecall=newRecall; }

    public String getMorgue() { return clanMorgue; }
    public void setMorgue(String newMorgue) { clanMorgue=newMorgue; }

    public String getDonation() { return clanDonationRoom; }
    public void setDonation(String newDonation) { clanDonationRoom=newDonation; }

    public Vector<MemberRecord> getMemberList()
    {
        return getMemberList(-1);
    }

    public Vector<MemberRecord> getMemberList(int PosFilter)
    {
    	Vector<MemberRecord> members;
        members = CMLib.database().DBClanMembers(clanID());
        Vector<MemberRecord> filteredMembers=new Vector<MemberRecord>();
        for(MemberRecord member : members)
            if((member.role==PosFilter)||(PosFilter<0))
                filteredMembers.add(member);
        return filteredMembers;
    }

    public int getNumVoters(int function)
    {
        int realmembers=0;
        int bosses=0;
        List<MemberRecord> members=getMemberList();
        for(MemberRecord member : members)
        {
            if(member.role==POS_BOSS)
            {
                realmembers++;
                bosses++;
            }
            else
            if(member.role!=POS_APPLICANT)
                realmembers++;
        }
        int numVotes=bosses;
        if(getGovernment()==GVT_DEMOCRACY)
            numVotes=realmembers;
        else
        if((getGovernment()==GVT_REPUBLIC)&&(function==FUNC_CLANASSIGN))
            numVotes=realmembers;
        return numVotes;
    }


    public int getTopRank(MOB mob) {
        if((getGovernment()>=0)
        &&(getGovernment()<topRanks.length))
        {
            int topRank=topRanks[getGovernment()];
            while((topRank > 0)&&(!canBeAssigned(mob,topRank)))
                topRank--;
            return topRank;
        }
        return POS_BOSS;
    }

    public boolean tick(Tickable ticking, int tickID)
    {
        if(tickID!=Tickable.TICKID_CLAN)
            return true;
        try{
        	List<MemberRecord> members=getMemberList();
            int activeMembers=0;
            long deathMilis=CMProps.getIntVar(CMProps.SYSTEMI_DAYSCLANDEATH)*CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)*Tickable.TIME_TICK;
            int[] numTypes=new int[POSORDER.length];
            for(MemberRecord member : members)
            {
                long lastLogin=member.timestamp;
                if(((System.currentTimeMillis()-lastLogin)<deathMilis)||(deathMilis==0))
                    activeMembers++;
                numTypes[CMLib.clans().getRoleOrder(member.role)]++;
            }

            // handle any necessary promotions
            if(getGovernment()==GVT_THEOCRACY)
            {
                int highestCleric=0;
                MOB highestClericM=null;
                MOB currentHighCleric = getResponsibleMember();
                int max=topRanks[getGovernment()];
                for(MemberRecord member : members)
                {
                    long lastLogin=member.timestamp;
                    if(((System.currentTimeMillis()-lastLogin)<deathMilis)||(deathMilis==0))
                    {
                        String name = member.name;
                        MOB M2=CMLib.players().getLoadPlayer(name);
                        if((M2==null)||(M2.getClanRole()==Clan.POS_APPLICANT)) continue;
                        if(M2.charStats().getCurrentClass().baseClass().equals("Cleric")
                            ||CMSecurity.isASysOp(M2))
                        {
                            if((CMLib.clans().getRoleOrder(member.role)==max)
                            &&(M2.envStats().level()>=highestCleric))
                            {
                                highestCleric=M2.envStats().level();
                                highestClericM=M2;
                            }
                            else
                            if(M2.envStats().level()>highestCleric)
                            {
                                highestCleric=M2.envStats().level();
                                highestClericM=M2;
                            }
                            if((M2.getClanRole()<max) && (M2 != currentHighCleric))
                            {
                                clanAnnounce(M2.Name()+" is now a "+CMLib.clans().getRoleName(getGovernment(),max,true,false)+" of the "+typeName()+" "+name()+".");
                                Log.sysOut("Clans",M2.Name()+" of clan "+name()+" was autopromoted to "+CMLib.clans().getRoleName(getGovernment(),max,true,false)+".");
                                M2.setClanRole(max);
                                CMLib.database().DBUpdateClanMembership(M2.Name(), name(), max);
                                
                                int mid = max-1;
                                clanAnnounce(currentHighCleric.Name()+" is now a "+CMLib.clans().getRoleName(getGovernment(),mid,true,false)+" of the "+typeName()+" "+name()+".");
                                Log.sysOut("Clans",currentHighCleric.Name()+" of clan "+name()+" was autodemoted to "+CMLib.clans().getRoleName(getGovernment(),mid,true,false)+".");
                                currentHighCleric.setClanRole(mid);
                                CMLib.database().DBUpdateClanMembership(currentHighCleric.Name(), name(), mid);
                            }
                        }
                    }
                }
                if(highestClericM==null)
                {
                    Log.sysOut("Clans","Clan '"+getName()+" deleted for lack of clerics.");
                    destroyClan();
                    StringBuffer buf=new StringBuffer("");
                    for(MemberRecord member : members)
                        buf.append(member.name+" on "+CMLib.time().date2String(member.timestamp)+"  ");
                    Log.sysOut("Clans","Clan '"+getName()+" had the following membership: "+buf.toString());
                    return true;
                }
                else
                if(highestClericM.getClanRole()!=max)
                {
                    clanAnnounce(highestClericM.Name()+" is now a "+CMLib.clans().getRoleName(getGovernment(),max,true,false)+" of the "+typeName()+" "+name()+".");
                    Log.sysOut("Clans",highestClericM.Name()+" of clan "+name()+" was autopromoted to "+CMLib.clans().getRoleName(getGovernment(),max,true,false)+".");
                    highestClericM.setClanRole(max);
                    CMLib.database().DBUpdateClanMembership(highestClericM.Name(), name(), max);
                }
                for(MemberRecord member : members)
                {
                    long lastLogin=member.timestamp;
                    if(((System.currentTimeMillis()-lastLogin)<deathMilis)||(deathMilis==0))
                    {
                        String name = member.name;
                        MOB M2=CMLib.players().getLoadPlayer(name);
                        if((M2==null)||(M2.getClanRole()==Clan.POS_APPLICANT)) continue;
                        if(!M2.getWorshipCharID().equals(highestClericM.getWorshipCharID()))
                        {
                            M2.setClanID("");
                            M2.setClanRole(0);
                            CMLib.database().DBUpdateClanMembership(M2.Name(), "", 0);
                        }
                    }
                }
            }
            else
            if((getGovernment()==GVT_DICTATORSHIP)
            ||(getGovernment()==GVT_OLIGARCHY))
            {
                int highest=0;
                for(int i=numTypes.length-1;i>=0;i--)
                    if(numTypes[i]>0){ highest=i; break;}
                int maxRank=topRanks[getGovernment()];
                if(highest<CMLib.clans().getRoleOrder(maxRank))
                {
                    for(MemberRecord member : members)
                    {
                        if(CMLib.clans().getRoleOrder(member.role)==highest)
                        {
                            String name = member.name;
                            MOB M2=CMLib.players().getLoadPlayer(name);
                            if(M2!=null)
                            {
                                int newRank = getTopRank(M2);
                                clanAnnounce(name+" is now a "+CMLib.clans().getRoleName(getGovernment(),newRank,true,false)+" of the "+typeName()+" "+name()+".");
                                Log.sysOut("Clans",name+" of clan "+name()+" was autopromoted to "+CMLib.clans().getRoleName(getGovernment(),newRank,true,false)+".");
                                M2.setClanRole(newRank);
                                CMLib.database().DBUpdateClanMembership(name, name(), newRank);
                            }
                            break;
                        }
                    }
                }
            }
            else
            if(getGovernment()==GVT_FAMILY)
            {
                int highest=0;
                for(int i=numTypes.length-1;i>=0;i--)
                    if(numTypes[i]>0){ highest=i; break;}
                int maxRank=topRanks[getGovernment()];
                if(highest<CMLib.clans().getRoleOrder(maxRank)-1)
                {
                    for(MemberRecord member : members)
                    {
                        if(CMLib.clans().getRoleOrder(member.role)==highest)
                        {
                            String name = member.name;
                            MOB M2=CMLib.players().getLoadPlayer(name);
                            if(M2!=null)
                            {
                                int newRank = getTopRank(M2);
                                clanAnnounce(name+" is now a "+CMLib.clans().getRoleName(getGovernment(),newRank,true,false)+" of the "+typeName()+" "+name()+".");
                                Log.sysOut("Clans",name+" of clan "+name()+" was autopromoted to "+CMLib.clans().getRoleName(getGovernment(),newRank,true,false)+".");
                                M2.setClanRole(newRank);
                                CMLib.database().DBUpdateClanMembership(name, name(), newRank);
                            }
                            break;
                        }
                    }
                }
            }


            int minimumMembers = (getGovernment()==Clan.GVT_FAMILY)?1:CMProps.getIntVar(CMProps.SYSTEMI_MINCLANMEMBERS);
            if(activeMembers<minimumMembers)
            {
                if(getStatus()==CLANSTATUS_FADING)
                {
                    Log.sysOut("Clans","Clan '"+getName()+" deleted with only "+activeMembers+" having logged on lately.");
                    destroyClan();
                    StringBuffer buf=new StringBuffer("");
                    for(MemberRecord member : members)
                        buf.append(member.name+" on "+CMLib.time().date2String(member.timestamp)+"  ");
                    Log.sysOut("Clans","Clan '"+getName()+" had the following membership: "+buf.toString());
                    return true;
                }
                setStatus(CLANSTATUS_FADING);
                for(MemberRecord member : members)
                {
                    String name = member.name;
                    int role=member.role;
                    //long lastLogin=((Long)members.elementAt(j,3)).longValue();
					if(CMLib.clans().getRoleOrder(role)==Clan.POSORDER.length-1)
					{
						MOB player=CMLib.players().getLoadPlayer(name);
						if(player!=null)
							CMLib.smtp().emailIfPossible("AutoPurge",player,"AutoPurge: "+name(), 
									""+typeName()+" "+name()+" is in danger of being deleted if at least "+(minimumMembers-activeMembers)
									+" members do not log on within 24 hours.");
					}
                }
                
                Log.sysOut("Clans","Clan '"+getName()+" fading with only "+activeMembers+" having logged on lately.");
                clanAnnounce(""+typeName()+" "+name()+" is in danger of being deleted if more members do not log on within 24 hours.");
                update();
            }
            else
            switch(getStatus())
            {
            case CLANSTATUS_FADING:
                setStatus(CLANSTATUS_ACTIVE);
                clanAnnounce(""+typeName()+" "+name()+" is no longer in danger of being deleted.  Be aware that there is required activity level.");
                break;
            case CLANSTATUS_PENDING:
                setStatus(CLANSTATUS_ACTIVE);
                Log.sysOut("Clans",""+typeName()+" '"+getName()+" now active with "+activeMembers+".");
                clanAnnounce(""+typeName()+" "+name()+" now has sufficient members.  The "+typeName()+" is now fully approved.");
                break;
            default:
                break;
            }


            // now do votes
            if((getGovernment()!=GVT_DICTATORSHIP)
            &&(getGovernment()!=GVT_THEOCRACY)
            &&(getGovernment()!=GVT_FAMILY)
            &&(votes()!=null))
            {
                boolean updateVotes=false;
                Vector votesToRemove=new Vector();
                Vector data=null;
                switch(getGovernment())
                {
                case GVT_DEMOCRACY:
                    data=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_CLANVOTED),false);
                    break;
                case GVT_OLIGARCHY:
                    data=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_CLANVOTEO),false);
                    break;
                case GVT_REPUBLIC:
                    data=CMParms.parseCommas(CMProps.getVar(CMProps.SYSTEM_CLANVOTER),false);
                    break;
                default:
                    data=new Vector();
                    break;
                }
                long duration=54;
                if(data.size()>0) duration=CMath.s_long((String)data.firstElement());
                if(duration<=0) duration=54;
                duration=duration*CMProps.getIntVar(CMProps.SYSTEMI_TICKSPERMUDDAY)*Tickable.TIME_TICK;
                for(Enumeration e=votes();e.hasMoreElements();)
                {
                    ClanVote CV=(ClanVote)e.nextElement();
                    int numVotes=getNumVoters(CV.function);
                    int quorum=50;
                    if(data.size()>1) quorum=CMath.s_int((String)data.lastElement());
                    quorum=(int)Math.round(CMath.mul(CMath.div(quorum,100.0),numVotes));
                    if(quorum<2) quorum=2;
                    if(numVotes==1) quorum=1;
                    long endsOn=CV.voteStarted+duration;
                    if(CV.voteStatus==VSTAT_STARTED)
                    {
                        if(CV.votes==null) CV.votes=new DVector(2);
                        boolean voteIsOver=false;
                        if(System.currentTimeMillis()>endsOn)
                            voteIsOver=true;
                        else
                        if(CV.votes.size()==numVotes)
                            voteIsOver=true;
                        if(voteIsOver)
                        {
                            CV.voteStarted=System.currentTimeMillis();
                            updateVotes=true;
                            if(CV.votes.size()<quorum)
                                CV.voteStatus=VSTAT_FAILED;
                            else
                            {
                                int yeas=0;
                                int nays=0;
                                for(int i=0;i<CV.votes.size();i++)
                                    if(((Boolean)CV.votes.elementAt(i,2)).booleanValue())
                                        yeas++;
                                    else
                                        nays++;
                                if(yeas<=nays)
                                    CV.voteStatus=VSTAT_FAILED;
                                else
                                {
                                    CV.voteStatus=VSTAT_PASSED;
                                    MOB mob=CMClass.getMOB("StdMOB");
                                    mob.setName(clanID());
                                    mob.setClanID(clanID());
                                    mob.setClanRole(POS_BOSS);
                                    mob.baseEnvStats().setLevel(1000);
                                    if(mob.location()==null)
                                    {
                                        mob.setLocation(mob.getStartRoom());
                                        if(mob.location()==null)
                                            mob.setLocation(CMLib.map().getRandomRoom());
                                    }
                                    Vector V=CMParms.parse(CV.matter);
                                    mob.doCommand(V,Command.METAFLAG_FORCED);
                                    mob.destroy();
                                }
                            }
                        }
                    }
                    else
                    if(System.currentTimeMillis()>endsOn)
                    {
                        updateVotes=true;
                        votesToRemove.addElement(CV);
                    }
                }
                for(int v=0;v<votesToRemove.size();v++)
                    delVote(votesToRemove.elementAt(v));
                if(updateVotes)
                    updateVotes();
            }

            if(CMLib.clans().trophySystemActive())
            {
                // calculate winner of the exp contest
                if(CMProps.getVar(CMProps.SYSTEM_CLANTROPEXP).length()>0)
                {
                    Clan winner=null;
                    for(Enumeration e=CMLib.clans().clans();e.hasMoreElements();)
                    {
                        Clan C=(Clan)e.nextElement();
                        if((winner==null)||(C.getExp()>winner.getExp()))
                            winner=C;
                    }
                    if(winner==this)
                    {
                        if((!CMath.bset(getTrophies(),TROPHY_EXP))&&(getExp()>0))
                        {
                            setTrophies(getTrophies()|TROPHY_EXP);
                            CMLib.clans().clanAnnounceAll("The "+typeName()+" "+name()+" has been awarded the trophy for "+TROPHY_DESCS[TROPHY_EXP]+".");
                        }
                    }
                    else
                    if(CMath.bset(getTrophies(),TROPHY_EXP))
                    {
                        setTrophies(getTrophies()-TROPHY_EXP);
                        clanAnnounce("The "+typeName()+" "+name()+" has lost control of the trophy for "+TROPHY_DESCS[TROPHY_EXP]+".");
                    }
                }

                // calculate winner of the pk contest
                if(CMProps.getVar(CMProps.SYSTEM_CLANTROPPK).length()>0)
                {
                    Clan winner=null;
                    for(Enumeration e=CMLib.clans().clans();e.hasMoreElements();)
                    {
                        Clan C=(Clan)e.nextElement();
                        if((winner==null)||(C.getCurrentClanKills()>winner.getCurrentClanKills()))
                            winner=C;
                    }
                    if(winner==this)
                    {
                        if((!CMath.bset(getTrophies(),TROPHY_PK))
                        &&(getCurrentClanKills()>0))
                        {
                            setTrophies(getTrophies()|TROPHY_PK);
                            CMLib.clans().clanAnnounceAll("The "+typeName()+" "+name()+" has been awarded the trophy for "+TROPHY_DESCS[TROPHY_PK]+".");
                        }
                    }
                    else
                    if(CMath.bset(getTrophies(),TROPHY_PK))
                    {
                        setTrophies(getTrophies()-TROPHY_PK);
                        clanAnnounce("The "+typeName()+" "+name()+" has lost control of the trophy for "+TROPHY_DESCS[TROPHY_PK]+".");
                    }
                }

                // calculate winner of the conquest contests
                if((CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA).length()>0)
                ||(CMProps.getVar(CMProps.SYSTEM_CLANTROPCP).length()>0))
                {
                    long mostClansControlled=-1;
                    Clan winnerMostClansControlled=null;
                    long mostControlPoints=-1;
                    Clan winnerMostControlPoints=null;
                    Vector tempControl=null;
                    long tempNumber=0;
                    for(Enumeration e=CMLib.clans().clans();e.hasMoreElements();)
                    {
                        Clan C=(Clan)e.nextElement();
                        tempControl=C.getControlledAreas();
                        tempNumber=C.calculateMapPoints(tempControl);
                        if((winnerMostClansControlled==null)||(tempControl.size()>mostClansControlled))
                        {
                            winnerMostClansControlled=C;
                            mostClansControlled=tempControl.size();
                        }
                        if((winnerMostControlPoints==null)||(tempNumber>mostControlPoints))
                        {
                            winnerMostControlPoints=C;
                            mostControlPoints=tempNumber;
                        }
                    }
                    if((winnerMostClansControlled==this)
                    &&(CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA).length()>0)
                    &&(mostClansControlled>0))
                    {
                        if(!CMath.bset(getTrophies(),TROPHY_AREA))
                        {
                            setTrophies(getTrophies()|TROPHY_AREA);
                            CMLib.clans().clanAnnounceAll("The "+typeName()+" "+name()+" has been awarded the trophy for "+TROPHY_DESCS[TROPHY_AREA]+".");
                        }
                    }
                    else
                    if(CMath.bset(getTrophies(),TROPHY_AREA))
                    {
                        setTrophies(getTrophies()-TROPHY_AREA);
                        clanAnnounce("The "+typeName()+" "+name()+" has lost control of the trophy for "+TROPHY_DESCS[TROPHY_AREA]+".");
                    }
                    if((winnerMostControlPoints==this)
                    &&(CMProps.getVar(CMProps.SYSTEM_CLANTROPCP).length()>0)
                    &&(mostControlPoints>0))
                    {
                        if(!CMath.bset(getTrophies(),TROPHY_CONTROL))
                        {
                            setTrophies(getTrophies()|TROPHY_CONTROL);
                            CMLib.clans().clanAnnounceAll("The "+typeName()+" "+name()+" has been awarded the trophy for "+TROPHY_DESCS[TROPHY_CONTROL]+".");
                        }
                    }
                    else
                    if(CMath.bset(getTrophies(),TROPHY_CONTROL))
                    {
                        setTrophies(getTrophies()-TROPHY_CONTROL);
                        clanAnnounce("The "+typeName()+" "+name()+" has lost control of the trophy for "+TROPHY_DESCS[TROPHY_CONTROL]+".");
                    }
                }
            }
            update(); // also saves exp, and trophies
        }
        catch(Exception x2)
        {
            Log.errOut("Clans",x2);
        }
        return true;
    }

    public void clanAnnounce(String msg)
    {
        Vector channels=CMLib.channels().getFlaggedChannelNames(ChannelsLibrary.ChannelFlag.CLANINFO);
        for(int i=0;i<channels.size();i++)
            CMLib.commands().postChannel((String)channels.elementAt(i),clanID(),msg,true);
    }

    public int applyExpMods(int exp)
    {
        boolean changed=false;
        if((getTaxes()>0.0)&&(exp>1))
        {
            int clanshare=(int)Math.round(CMath.mul(exp,getTaxes()));
            if(clanshare>0)
            {
                exp-=clanshare;
                adjExp(clanshare);
                changed=true;
            }
        }
        for(int i=0;i<TROPHY_DESCS_SHORT.length;i++)
            if((TROPHY_DESCS_SHORT[i].length()>0)
            &&(CMath.bset(getTrophies(),i)))
            {
                String awardStr=null;
                switch(i)
                {
                case TROPHY_AREA: awardStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPAREA); break;
                case TROPHY_CONTROL: awardStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPCP); break;
                case TROPHY_EXP: awardStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPEXP); break;
                case TROPHY_PK: awardStr=CMProps.getVar(CMProps.SYSTEM_CLANTROPPK); break;
                }
                if(awardStr!=null)
                {
                    int amount=0;
                    double pct=0.0;
                    Vector V=CMParms.parse(awardStr);
                    if(V.size()>=2)
                    {
                        String type=((String)V.lastElement()).toUpperCase();
                        String amt=(String)V.firstElement();
                        if(amt.endsWith("%"))
                            pct=CMath.div(CMath.s_int(amt.substring(0,amt.length()-1)),100.0);
                        else
                            amount=CMath.s_int(amt);
                        if("EXPERIENCE".startsWith(type))
                            exp+=((int)Math.round(CMath.mul(exp,pct)))+amount;
                    }
                }
            }
        if(changed) update();
        return exp;
    }
    public MOB getResponsibleMember()
    {
        MOB mob=null;
        List<MemberRecord> members=getMemberList();
        int newPos=-1;
        String memberName = null;
        for(MemberRecord member : members)
            if(member.role>newPos)
            {
            	newPos = member.role;
            	memberName = member.name;
            }
        if(memberName != null)
        	mob=CMLib.players().getLoadPlayer(memberName);
        return mob;
    }

    public String[] getStatCodes(){ return CLAN_STATS;}
    public int getSaveStatIndex(){ return CLAN_STATS.length;}
    public boolean isStat(String code){ return CMParms.indexOf(getStatCodes(),code.toUpperCase().trim())>=0;}
    public String getStat(String code) {
        int dex=CMParms.indexOf(getStatCodes(),code.toUpperCase().trim());
        if(dex<0) return "";
        switch(dex)
        {
        case 0: return getAcceptanceSettings();
        case 1: return getDetail(null);
        case 2: return getDonation();
        case 3: return ""+getExp();
        case 4: return Clan.GVT_DESCS[getGovernment()];
        case 5: return getMorgue();
        case 6: return getPolitics();
        case 7: return getPremise();
        case 8: return getRecall();
        case 9: return ""+getSize();
        case 10: return Clan.CLANSTATUS_DESC[getStatus()];
        case 11: return ""+getTaxes();
        case 12: return ""+getTrophies();
        case 13: return ""+getType();
        case 14: {
             Vector areas=getControlledAreas();
             StringBuffer list=new StringBuffer("");
             for(int i=0;i<areas.size();i++)
                 list.append("\""+((Environmental)areas.elementAt(i)).name()+"\" ");
             return list.toString().trim();
        }
        case 15: 
        {
        	List<MemberRecord> members=getMemberList();
                 StringBuffer list=new StringBuffer("");
                 for(MemberRecord member : members)
                     list.append("\""+member.name+"\" ");
                 return list.toString().trim();
        }
        case 16: {
            MOB M=getResponsibleMember();
            if(M!=null) return M.Name();
            return "";
        }
        }
        return "";
    }
    
    public void setStat(String code, String val) {
        int dex=CMParms.indexOf(getStatCodes(),code.toUpperCase().trim());
        if(dex<0) return;
        switch(dex) {
        case 0: setAcceptanceSettings(val); break;
        case 1: break; // detail
        case 2: setDonation(val); break;
        case 3: setExp(CMath.s_long(val.trim())); break;
        case 4: setGovernment(CMath.s_int(val.trim())); break;
        case 5: setMorgue(val); break;
        case 6: setPolitics(val); break;
        case 7: setPremise(val); break;
        case 8: setRecall(val); break;
        case 9: break; // size
        case 10: setStatus(CMath.s_int(val.trim())); break;
        case 11: setTaxes(CMath.s_double(val.trim())); break;
        case 12: setTrophies(CMath.s_int(val.trim())); break;
        case 13: break; // type
        case 14: break; // areas
        case 15: break; // memberlist
        case 16: break; // topmember
        }
    }
    
}
