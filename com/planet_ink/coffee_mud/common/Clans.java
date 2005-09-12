package com.planet_ink.coffee_mud.common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

import java.util.*;
/**
 * <p>Portions Copyright (c) 2003 Jeremy Vyska</p>
 * <p>Portions Copyright (c) 2004 Bo Zimmerman</p>
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
public class Clans implements Clan, Tickable
{
	private static Hashtable all=new Hashtable();

	private long tickStatus=Tickable.STATUS_NOT;
	public long getTickStatus(){return tickStatus;}
	protected String clanName="";
	protected String clanPremise="";
	protected String clanRecall="";
	protected String clanMorgue="";
	protected String clanDonationRoom="";
	protected int clanTrophies=0;
	protected String AcceptanceSettings="";
	protected int clanType=Clan.TYPE_CLAN;
	protected int ClanStatus=0;
	protected Vector voteList=null;
	protected long exp=0;
	protected double taxRate=0.0;
	
	//*****************
	public Hashtable relations=new Hashtable();
	public int government=Clan.GVT_DICTATORSHIP;
	//*****************

	public void updateVotes()
	{
		CMClass.DBEngine().DBDeleteData(ID(),"CLANVOTES",ID()+"/CLANVOTES");
		StringBuffer str=new StringBuffer("");
		for(Enumeration e=votes();e.hasMoreElements();)
		{
			ClanVote CV=(ClanVote)e.nextElement();
			str.append(XMLManager.convertXMLtoTag("BY",CV.voteStarter));
			str.append(XMLManager.convertXMLtoTag("FUNC",CV.function));
			str.append(XMLManager.convertXMLtoTag("ON",""+CV.voteStarted));
			str.append(XMLManager.convertXMLtoTag("STATUS",""+CV.voteStatus));
			str.append(XMLManager.convertXMLtoTag("CMD",CV.matter));
			if((CV.votes!=null)&&(CV.votes.size()>0))
			{
				str.append("<VOTES>");
				for(int v=0;v<CV.votes.size();v++)
				{
					str.append(XMLManager.convertXMLtoTag("BY",(String)CV.votes.elementAt(v,1)));
					str.append(XMLManager.convertXMLtoTag("YN",""+((Boolean)CV.votes.elementAt(v,2)).booleanValue()));
				}
				str.append("</VOTES>");
			}
		}
		if(str.length()>0)
			CMClass.DBEngine().DBCreateData(ID(),"CLANVOTES",ID()+"/CLANVOTES","<BALLOTS>"+str.toString()+"</BALLOTS>");
	}
	public void addVote(Object CV)
	{
		if(!(CV instanceof ClanVote))
			return;
		votes();
		voteList.addElement(CV);
	}
	public void delVote(Object CV)
	{
		votes();
		voteList.removeElement(CV);
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
			Behavior B=CoffeeUtensils.getLegalBehavior(A);
			if(B!=null)
			{
				Area A2=CoffeeUtensils.getLegalObject(A);
				Vector V=new Vector();
				V.addElement(new Integer(Law.MOD_CONTROLPOINTS));
				if((B.modifyBehavior(A2,CMClass.sampleMOB(),V))
				&&(V.size()==1)
				&&(V.firstElement() instanceof Integer))
					points+=((Integer)V.firstElement()).longValue();
			}
		}
		return points;
	}

	public Vector getControlledAreas()
	{
		Vector done=new Vector();
		for(Enumeration e=CMMap.areas();e.hasMoreElements();)
		{
			Area A=(Area)e.nextElement();
			Behavior B=CoffeeUtensils.getLegalBehavior(A);
			if(B!=null)
			{
				Vector V=new Vector();
				V.addElement(new Integer(Law.MOD_RULINGCLAN));
				Area A2=CoffeeUtensils.getLegalObject(A);
				boolean response=B.modifyBehavior(A2,CMClass.sampleMOB(),V);
				if(response
				&&(!done.contains(A2))
				&&(V.size()==1)
				&&(V.firstElement() instanceof String)
				&&(((String)V.firstElement()).equals(ID())))
				    done.addElement(A2);
			}
		}
		return done;
	}

	public Enumeration votes()
	{
		if(voteList==null)
		{
			Vector V=CMClass.DBEngine().DBReadData(ID(),"CLANVOTES",ID()+"/CLANVOTES");
			voteList=new Vector();
			for(int v=0;v<V.size();v++)
			{
				ClanVote CV=new ClanVote();
				String rawxml=(String)((Vector)V.elementAt(v)).elementAt(3);
				if(rawxml.trim().length()==0) return voteList.elements();
				Vector xml=XMLManager.parseAllXML(rawxml);
				if(xml==null)
				{
					Log.errOut("Clans","Unable to parse: "+rawxml);
					return voteList.elements();
				}
				Vector voteData=XMLManager.getRealContentsFromPieces(xml,"BALLOTS");
				if(voteData==null){	Log.errOut("Clans","Unable to get BALLOTS data."); return voteList.elements();}
				CV.voteStarter=XMLManager.getValFromPieces(voteData,"BY");
				CV.voteStarted=XMLManager.getLongFromPieces(voteData,"ON");
				CV.function=XMLManager.getIntFromPieces(voteData,"FUNC");
				CV.voteStatus=XMLManager.getIntFromPieces(voteData,"STATUS");
				CV.matter=XMLManager.getValFromPieces(voteData,"CMD");
				CV.votes=new DVector(2);
				Vector xV=XMLManager.getRealContentsFromPieces(voteData,"VOTES");
				if((xV!=null)&&(xV.size()>0))
				{
					for(int x=0;x<xV.size();x++)
					{
						XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)xV.elementAt(x);
						if((!iblk.tag.equalsIgnoreCase("VOTE"))||(iblk.contents==null))
							continue;
						String userID=XMLManager.getValFromPieces(iblk.contents,"BY");
						boolean yn=XMLManager.getBoolFromPieces(iblk.contents,"YN");
						CV.votes.addElement(userID,new Boolean(yn));
					}
				}
				voteList.addElement(CV);
			}
		}
		return voteList.elements();
	}

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

	public static void shutdownClans()
	{
		for(Enumeration e=all.elements();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			CMClass.ThreadEngine().deleteTick(C,MudHost.TICK_CLAN);
		}
		all.clear();
	}

    public static boolean isCommonClanRelations(String id1, String id2, int relation)
    {
        if((id1.length()==0)||(id2.length()==0)) return relation==Clan.REL_NEUTRAL;
        Clan C1=getClan(id1);
        Clan C2=getClan(id2);
        if((C1==null)||(C2==null)) return relation==Clan.REL_NEUTRAL;
        int i1=C1.getClanRelations(id2);
        int i2=C2.getClanRelations(id1);
        if((i1==i2)
        &&((i1==Clan.REL_WAR)
           ||(i1==Clan.REL_ALLY)))
           return relation==Clan.REL_ALLY;
        for(Enumeration e=clans();e.hasMoreElements();)
        {
            Clan C=(Clan)e.nextElement();
            if((C!=C1)&&(C!=C2))
            {
                if((i1!=Clan.REL_WAR)
                &&(C1.getClanRelations(C.ID())==Clan.REL_ALLY)
                &&(C.getClanRelations(C2.ID())==Clan.REL_WAR))
                    i1=Clan.REL_WAR;
                if((i2!=Clan.REL_WAR)
                &&(C2.getClanRelations(C.ID())==Clan.REL_ALLY)
                &&(C.getClanRelations(C1.ID())==Clan.REL_WAR))
                    i2=Clan.REL_WAR;
            }
        }
        if(i1==i2) return relation==i1;
        
        if(Clan.REL_NEUTRALITYGAUGE[i1]<Clan.REL_NEUTRALITYGAUGE[i2]) return relation==i1;
        return relation==i2;
    }
    
	public static int getClanRelations(String id1, String id2)
	{
		if((id1.length()==0)||(id2.length()==0)) return Clan.REL_NEUTRAL;
		Clan C1=getClan(id1);
		Clan C2=getClan(id2);
		if((C1==null)||(C2==null)) return Clan.REL_NEUTRAL;
		int i1=C1.getClanRelations(id2);
		int i2=C2.getClanRelations(id1);
		int rel=Clan.RELATIONSHIP_VECTOR[i1][i2];
		if(rel==Clan.REL_WAR) return Clan.REL_WAR;
		if(rel==Clan.REL_ALLY) return Clan.REL_ALLY;
		for(Enumeration e=clans();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			if((C!=C1)
			&&(C!=C2)
			&&(((C1.getClanRelations(C.ID())==Clan.REL_ALLY)&&(C.getClanRelations(C2.ID())==Clan.REL_WAR)))
				||((C2.getClanRelations(C.ID())==Clan.REL_ALLY)&&(C.getClanRelations(C1.ID())==Clan.REL_WAR)))
					return Clan.REL_WAR;
		}
		return rel;
	}

	public int getClanRelations(String id)
	{
		long i[]=(long[])relations.get(id.toUpperCase());
		if(i!=null) return (int)i[0];
		return	Clan.REL_NEUTRAL;
	}

	public static Clan getClan(String id)
	{
		if(id.length()==0) return null;
		Clan C=(Clan)all.get(id.toUpperCase());
        if(C!=null) return C;
        for(Enumeration e=all.elements();e.hasMoreElements();)
        {
            C=(Clan)e.nextElement();
            if(EnglishParser.containsString(Util.removeColors(C.name()),id))
                return C;
        }
        return null;
	}
    public static Clan findClan(String id)
    {
        Clan C=getClan(id);
        if(C!=null) return C;
        for(Enumeration e=all.elements();e.hasMoreElements();)
        {
            C=(Clan)e.nextElement();
            if(EnglishParser.containsString(Util.removeColors(C.name()),id))
                return C;
        }
        return null;
    }

	public static Clan getClanType(int type)
	{
		switch(type)
		{
		case Clan.TYPE_CLAN:
			return new Clans();
		default:
			return new Clans();
		}
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

	public static int getRoleOrder(int role)
	{
		for(int i=0;i<Clan.POSORDER.length;i++)
			if(Clan.POSORDER[i]==role) return i;
		return 1;
	}


	public static String getRoleName(int government, int role, boolean titleCase, boolean plural)
	{
		StringBuffer roleName=new StringBuffer();
		if((government<0)||(government>=Clan.ROL_DESCS.length))
			government=0;
		String[] roles=Clan.ROL_DESCS[government];
		roleName.append(roles[getRoleOrder(role)].toLowerCase());
		if(titleCase)
		{
			String titled=Util.capitalizeAndLower(roleName.toString());
			roleName.setLength(0);
			roleName.append(titled);
		}
		if(plural)
		{
			if(roleName.toString().equalsIgnoreCase("Staff"))
			{
				// do nothing
			}
			else
			if(new Character(roleName.charAt(roleName.length()-1)).equals(new Character((new String("y")).charAt(0))))
			{
				roleName.setCharAt(roleName.length()-1,'i');
				roleName.append("es");
			}
			else
			if(new Character(roleName.charAt(roleName.length()-1)).equals(new Character((new String("s")).charAt(0))))
			{
				roleName.append("es");
			}
			else
			{
				roleName.append("s");
			}
		}
		return roleName.toString();
	}

	public void create()
	{
		CMClass.DBEngine().DBCreateClan(this);
		addClan(this);
	}

	public void update()
	{
		CMClass.DBEngine().DBUpdateClan(this);
	}

	public boolean updateClanPrivileges(MOB M)
	{
		boolean did=false;
		if(M.getClanID().equals(ID())
		&&(M.getClanRole()!=Clan.POS_APPLICANT))
		{
			if(M.fetchAbility("Spell_ClanHome")==null)
			{
				M.addAbility(CMClass.findAbility("Spell_ClanHome"));
				(M.fetchAbility("Spell_ClanHome")).setProfficiency(50);
				did=true;
			}
			if(M.fetchAbility("Spell_ClanDonate")==null)
			{
				M.addAbility(CMClass.findAbility("Spell_ClanDonate"));
				(M.fetchAbility("Spell_ClanDonate")).setProfficiency(100);
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
		if(((M.getClanID().equals(ID())))
		&&(allowedToDoThis(M,Clan.FUNC_CLANCANORDERCONQUERED)>0))
		{
		    if(M.fetchAbility("Spell_Flagportation")==null)
		    {
				M.addAbility(CMClass.findAbility("Spell_Flagportation"));
				(M.fetchAbility("Spell_Flagportation")).setProfficiency(100);
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
		for(int i=0;i<Clans.POSORDER.length;i++)
		{
		    int pos=Clans.POSORDER[i];
			String title="*, "+getRoleName(getGovernment(),pos,true,false)+" of "+name();
			if((M.getClanRole()==pos)
			&&(M.getClanID().equals(ID()))
			&&(pos!=Clan.POS_APPLICANT))
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
                    R=CMMap.getRoom(getDonation());
                if((R==null)
                &&(getRecall()!=null)
                &&(getRecall().length()>0))
                    R=CMMap.getRoom(getRecall());
                if(I instanceof Container)
                {
                    Vector V=((Container)I).getContents();
                    for(int v=0;v<V.size();v++)
                        ((Item)V.elementAt(v)).setContainer(null);
                }
                I.setContainer(null);
                I.wearAt(Item.INVENTORY);
                if(R!=null)
                    R.bringItemHere(I,0);
                else
                if(M.isMine(I))
                    I.destroy();
                did=true;
            }
        }
		if((did)&&(!CMSecurity.isSaveFlag("NOPLAYERS")))
			CMClass.DBEngine().DBUpdatePlayer(M);
		return did;
	}

	public void destroyClan()
	{
		DVector members=getMemberList();
		for(int m=0;m<members.size();m++)
		{
			String member=(String)members.elementAt(m,1);
			MOB M=CMMap.getLoadPlayer(member);
			if(M!=null)
			{
				M.setClanID("");
				M.setClanRole(0);
				updateClanPrivileges(M);
				CMClass.DBEngine().DBUpdateClanMembership(M.Name(), "", 0);
			}
		}
		CMClass.DBEngine().DBDeleteClan(this);
		removeClan(this);
	}

	public static Enumeration clans()
	{
		return all.elements();
	}
	public static int size()
	{
		return all.size();
	}
	public static void addClan(Clan C)
	{
		if(!CMSecurity.isDisabled("CLANTICKS"))
			CMClass.ThreadEngine().startTickDown(C,MudHost.TICK_CLAN,CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY));
		all.put(C.ID().toUpperCase(),C);
	}
	public static void removeClan(Clan C)
	{
		CMClass.ThreadEngine().deleteTick(C,MudHost.TICK_CLAN);
		all.remove(C.ID().toUpperCase());
	}

	public String getDetail(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("^x"+typeName()+" Profile   :^.^N "+ID()+"\n\r"
		          +"-----------------------------------------------------------------\n\r"
		          +getPremise()+"\n\r"
		          +"-----------------------------------------------------------------\n\r"
				  +"^xType            :^.^N "+Util.capitalizeAndLower(Clan.GVT_DESCS[getGovernment()])+"\n\r"
				  +"^xQualifications  :^.^N "+((getAcceptanceSettings().length()==0)?"Anyone may apply":MUDZapper.zapperDesc(getAcceptanceSettings()))+"\n\r");
		if((mob.getClanID().equalsIgnoreCase(ID()))
		||(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)))
		{
			msg.append("^xExp. Tax Rate   :^.^N "+((int)Math.round(getTaxes()*100))+"%\n\r");
			msg.append("^xExperience Pts. :^.^N "+getExp()+"\n\r");
		}
		msg.append("^x"+Util.padRight(Clans.getRoleName(getGovernment(),Clan.POS_BOSS,true,true),16)+":^.^N "+crewList(Clan.POS_BOSS)+"\n\r"
		          +"^x"+Util.padRight(Clans.getRoleName(getGovernment(),Clan.POS_LEADER,true,true),16)+":^.^N "+crewList(Clan.POS_LEADER)+"\n\r"
		          +"^x"+Util.padRight(Clans.getRoleName(getGovernment(),Clan.POS_TREASURER,true,true),16)+":^.^N "+crewList(Clan.POS_TREASURER)+"\n\r"
		          +"^x"+Util.padRight(Clans.getRoleName(getGovernment(),Clan.POS_ENCHANTER,true,true),16)+":^.^N "+crewList(Clan.POS_ENCHANTER)+"\n\r"
		          +"^x"+Util.padRight(Clans.getRoleName(getGovernment(),Clan.POS_STAFF,true,true),16)+":^.^N "+crewList(Clan.POS_STAFF)+"\n\r"
		          +"^xTotal Members   :^.^N "+getSize()+"\n\r");
		if(all.size()>1)
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append("^x"+Util.padRight("Clan Relations",16)+":^.^N \n\r");
			for(Enumeration e=all.elements();e.hasMoreElements();)
			{
				Clan C=(Clan)e.nextElement();
				if(C!=this)
				{
					msg.append("^x"+Util.padRight(C.name(),16)+":^.^N ");
					msg.append(Util.capitalizeAndLower(Clan.REL_DESCS[getClanRelations(C.ID())]));
					int orel=C.getClanRelations(ID());
					if(orel!=Clan.REL_NEUTRAL)
						msg.append(" (<-"+Util.capitalizeAndLower(Clan.REL_DESCS[orel])+")");
					msg.append("\n\r");
				}
			}
		}
		if((mob.getClanID().equalsIgnoreCase(ID()))
		||(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)))
		{
			if(mob.getClanID().equalsIgnoreCase(ID()))
				updateClanPrivileges(mob);
			msg.append("-----------------------------------------------------------------\n\r"
			          +"^x"+Util.padRight(Clans.getRoleName(getGovernment(),Clan.POS_MEMBER,true,true),16)
					  +":^.^N "+crewList(Clan.POS_MEMBER)+"\n\r");
			if((allowedToDoThis(mob,Clan.FUNC_CLANACCEPT)>=0)
			||(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)))
			{
				msg.append("-----------------------------------------------------------------\n\r"
				        +"^x"+Util.padRight(Clans.getRoleName(getGovernment(),Clan.POS_APPLICANT,true,true),16)+":^.^N "+crewList(Clan.POS_APPLICANT)+"\n\r");
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
			msg.append("^xClan Controlled Areas:^.^N\n\r");
			Collections.sort(control);
			int col=0;
			for(int i=0;i<control.size();i++)
			{
				if((++col)>3)
				{
					msg.append("\n\r");
					col=1;
				}
				msg.append(Util.padRight((String)control.elementAt(i),23)+"^N");
			}
			msg.append("\n\r");
		}
		if((Clans.trophySystemActive())&&(getTrophies()!=0))
		{
			msg.append("-----------------------------------------------------------------\n\r");
			msg.append("^xTrophies awarded:^.^N\n\r");
			for(int i=0;i<Clan.TROPHY_DESCS.length;i++)
			    if((Clan.TROPHY_DESCS[i].length()>0)&&(Util.bset(getTrophies(),i)))
			    {
			        msg.append(Clan.TROPHY_DESCS[i]+" ");
			        switch(i){
				        case Clan.TROPHY_AREA: msg.append("("+control.size()+") "); break;
				        case Clan.TROPHY_CONTROL: msg.append("("+controlPoints+") "); break;
				        case Clan.TROPHY_EXP: msg.append("("+getExp()+") "); break;
			        }
			        msg.append(" Prize: "+Clans.translatePrize(i)+"\n\r");
			    }
		}
		return msg.toString();
	}

	private String crewList(int posType)
	{
		StringBuffer list=new StringBuffer("");
		DVector Members = getMemberList(posType);
		if(Members.size()>1)
		{
			for(int j=0;j<(Members.size() - 1);j++)
			{
				list.append(Members.elementAt(j,1)+", ");
			}
			list.append("and "+Members.elementAt(Members.size()-1,1));
		}
		else
		if(Members.size()>0)
		{
			list.append((String)Members.elementAt(0,1));
		}
		return list.toString();
	}

	public String typeName()
	{
		switch(clanType)
		{
		case Clan.TYPE_CLAN:
			if((getGovernment()>=0)&&(getGovernment()<Clan.GVT_DESCS.length))
				return Util.capitalizeAndLower(Clan.GVT_DESCS[getGovernment()].toLowerCase());
		}
		return "Clan";
	}

	public int allowedToDoThis(MOB mob, int function)
	{
		if(mob==null) return -1;
		int role=mob.getClanRole();
		if(role==Clan.POS_APPLICANT) return -1;
		if(government==Clan.GVT_OLIGARCHY)
		{
			switch(function)
			{
			case FUNC_CLANACCEPT:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_LEADER))?1:-1;
			case FUNC_CLANASSIGN:
				return 0;
			case FUNC_CLANEXILE:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_LEADER))?1:-1;
			case FUNC_CLANHOMESET:
				return 0;
			case FUNC_CLANTAX:
				return 0;
			case FUNC_CLANDONATESET:
				return 0;
			case FUNC_CLANREJECT:
				return (role==Clan.POS_BOSS)?1:-1;
			case FUNC_CLANPREMISE:
				return 0;
			case FUNC_CLANDECLARE:
				return 0;
			case FUNC_CLANPROPERTYOWNER:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_LEADER))?1:-1;
			case FUNC_CLANENCHANT:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_ENCHANTER))?1:-1;
			case FUNC_CLANWITHDRAW:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_TREASURER))?1:-1;
			case FUNC_CLANDEPOSITLIST:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_TREASURER))?1:-1;
			case FUNC_CLANCANORDERUNDERLINGS:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_LEADER)||(role==Clan.POS_TREASURER)||(role==Clan.POS_ENCHANTER)||(role==Clan.POS_STAFF))?1:-1;
			case FUNC_CLANCANORDERCONQUERED:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_LEADER)||(role==Clan.POS_TREASURER)||(role==Clan.POS_ENCHANTER)||(role==Clan.POS_STAFF)||(role==Clan.POS_MEMBER))?1:-1;
			case FUNC_CLANVOTEASSIGN:
				return (role==Clan.POS_BOSS)?1:-1;
			case FUNC_CLANVOTEOTHER:
				return (role==Clan.POS_BOSS)?1:-1;
			}
		}
		else
		if(government==Clan.GVT_REPUBLIC)
		{
			switch(function)
			{
			case FUNC_CLANACCEPT:
				return 0;
			case FUNC_CLANASSIGN:
				return 0;
			case FUNC_CLANTAX:
				return 0;
			case FUNC_CLANEXILE:
				return 0;
			case FUNC_CLANHOMESET:
				return 0;
			case FUNC_CLANDONATESET:
				return 0;
			case FUNC_CLANREJECT:
				return 0;
			case FUNC_CLANDECLARE:
				return 0;
			case FUNC_CLANPREMISE:
				return 0;
			case FUNC_CLANENCHANT:
				return (role==Clan.POS_ENCHANTER)?1:-1;
			case FUNC_CLANPROPERTYOWNER:
				return (role==Clan.POS_LEADER)?1:-1;
			case FUNC_CLANWITHDRAW:
				return (role==Clan.POS_TREASURER)?1:-1;
			case FUNC_CLANDEPOSITLIST:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_ENCHANTER)||(role==Clan.POS_LEADER)||(role==Clan.POS_TREASURER)||(role==Clan.POS_STAFF)||(role==Clan.POS_MEMBER))?1:-1;
			case FUNC_CLANCANORDERUNDERLINGS:
				return -1;
			case FUNC_CLANCANORDERCONQUERED:
				return (role==Clan.POS_STAFF)?1:-1;
			case FUNC_CLANVOTEASSIGN:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_ENCHANTER)||(role==Clan.POS_LEADER)||(role==Clan.POS_TREASURER)||(role==Clan.POS_STAFF)||(role==Clan.POS_MEMBER))?1:-1;
			case FUNC_CLANVOTEOTHER:
				return (role==Clan.POS_BOSS)?1:-1;
			}
		}
		else
		if(government==Clan.GVT_DEMOCRACY)
		{
			switch(function)
			{
			case FUNC_CLANACCEPT:
				return 0;
			case FUNC_CLANASSIGN:
				return 0;
			case FUNC_CLANEXILE:
				return 0;
			case FUNC_CLANDECLARE:
				return 0;
			case FUNC_CLANHOMESET:
				return 0;
			case FUNC_CLANTAX:
				return 0;
			case FUNC_CLANDONATESET:
				return 0;
			case FUNC_CLANREJECT:
				return 0;
			case FUNC_CLANPREMISE:
				return 0;
			case FUNC_CLANENCHANT:
				return (role==Clan.POS_ENCHANTER)?1:-1;
			case FUNC_CLANPROPERTYOWNER:
				return (role==Clan.POS_LEADER)?1:-1;
			case FUNC_CLANWITHDRAW:
				return (role==Clan.POS_TREASURER)?1:-1;
			case FUNC_CLANDEPOSITLIST:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_ENCHANTER)||(role==Clan.POS_LEADER)||(role==Clan.POS_TREASURER)||(role==Clan.POS_STAFF)||(role==Clan.POS_MEMBER))?1:-1;
			case FUNC_CLANCANORDERUNDERLINGS:
				return -1;
			case FUNC_CLANCANORDERCONQUERED:
				return (role==Clan.POS_STAFF)?1:-1;
			case FUNC_CLANVOTEASSIGN:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_ENCHANTER)||(role==Clan.POS_LEADER)||(role==Clan.POS_TREASURER)||(role==Clan.POS_STAFF)||(role==Clan.POS_MEMBER))?1:-1;
			case FUNC_CLANVOTEOTHER:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_ENCHANTER)||(role==Clan.POS_LEADER)||(role==Clan.POS_TREASURER)||(role==Clan.POS_STAFF)||(role==Clan.POS_MEMBER))?1:-1;
			}
		}
		else
		//if(government==Clan.GVT_DICTATORSHIP) or badly formed..
		{
			switch(function)
			{
			case FUNC_CLANACCEPT:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_LEADER))?1:-1;
			case FUNC_CLANASSIGN:
				return (role==Clan.POS_BOSS)?1:-1;
			case FUNC_CLANEXILE:
				return (role==Clan.POS_BOSS)?1:-1;
			case FUNC_CLANHOMESET:
				return (role==Clan.POS_BOSS)?1:-1;
			case FUNC_CLANDECLARE:
				return (role==Clan.POS_BOSS)?1:-1;
			case FUNC_CLANTAX:
				return (role==Clan.POS_BOSS)?1:-1;
			case FUNC_CLANDONATESET:
				return (role==Clan.POS_BOSS)?1:-1;
			case FUNC_CLANREJECT:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_LEADER))?1:-1;
			case FUNC_CLANPREMISE:
				return (role==Clan.POS_BOSS)?1:-1;
			case FUNC_CLANPROPERTYOWNER:
				return (role==Clan.POS_BOSS)?1:-1;
			case FUNC_CLANENCHANT:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_ENCHANTER))?1:-1;
			case FUNC_CLANWITHDRAW:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_TREASURER))?1:-1;
			case FUNC_CLANDEPOSITLIST:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_TREASURER))?1:-1;
			case FUNC_CLANCANORDERUNDERLINGS:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_LEADER))?1:-1;
			case FUNC_CLANCANORDERCONQUERED:
				return ((role==Clan.POS_BOSS)||(role==Clan.POS_ENCHANTER)||(role==Clan.POS_LEADER)||(role==Clan.POS_STAFF)||(role==Clan.POS_TREASURER))?1:-1;
			case FUNC_CLANVOTEASSIGN:
				return -1;
			case FUNC_CLANVOTEOTHER:
				return -1;
			}
		}
		return -1;
	}

	public DVector getRealMemberList(int PosFilter)
	{
		DVector members=getMemberList(PosFilter);
		if(members==null) return null;
		for(int i=members.size()-1;i>=0;i--)
		{
			String member=(String)members.elementAt(i,1);
			if(CMMap.getPlayer(member)!=null)
				continue;
			if(CMClass.DBEngine().DBUserSearch(null,member))
				continue;
			members.removeElementAt(i);
		}
		return members;
	}

	public int getSize()
	{
		Vector members=new Vector();
		return getSize(members);
	}

	public int getSize(Vector members)
	{
		CMClass.DBEngine().DBClanFill(this.ID(), members, new Vector(), new Vector());
		return members.size();
	}

	public String name() {return clanName;}
	public String getName() {return clanName;}
	public String ID() {return clanName;}
	public void setName(String newName) {clanName = newName; }
	public int getType() {return clanType;}

	public String getPremise() {return clanPremise;}
	public void setPremise(String newPremise){ clanPremise = newPremise;}

	public String getAcceptanceSettings() { return AcceptanceSettings; }
	public void setAcceptanceSettings(String newSettings) { AcceptanceSettings=newSettings; }

	public String getPolitics() {
		StringBuffer str=new StringBuffer("");
		str.append("<POLITICS>");
		str.append(XMLManager.convertXMLtoTag("GOVERNMENT",""+getGovernment()));
		str.append(XMLManager.convertXMLtoTag("TAXRATE",""+getTaxes()));
		str.append(XMLManager.convertXMLtoTag("EXP",""+getExp()));
		if(relations.size()==0)
			str.append("<RELATIONS/>");
		else
		{
			str.append("<RELATIONS>");
			for(Enumeration e=relations.keys();e.hasMoreElements();)
			{
				String key=(String)e.nextElement();
				str.append("<RELATION>");
				str.append(XMLManager.convertXMLtoTag("CLAN",key));
				long[] i=(long[])relations.get(key);
				str.append(XMLManager.convertXMLtoTag("STATUS",""+i[0]));
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
		government=Clan.GVT_DICTATORSHIP;
		if(politics.trim().length()==0) return;
		Vector xml=XMLManager.parseAllXML(politics);
		if(xml==null)
		{
			Log.errOut("Clans","Unable to parse: "+politics);
			return;
		}
		Vector poliData=XMLManager.getRealContentsFromPieces(xml,"POLITICS");
		if(poliData==null){	Log.errOut("Clans","Unable to get POLITICS data."); return;}
		government=XMLManager.getIntFromPieces(poliData,"GOVERNMENT");
		exp=XMLManager.getLongFromPieces(poliData,"EXP");
		taxRate=XMLManager.getDoubleFromPieces(poliData,"TAXRATE");
		// now RESOURCES!
		Vector xV=XMLManager.getRealContentsFromPieces(poliData,"RELATIONS");
		if((xV!=null)&&(xV.size()>0))
		{
			for(int x=0;x<xV.size();x++)
			{
				XMLManager.XMLpiece iblk=(XMLManager.XMLpiece)xV.elementAt(x);
				if((!iblk.tag.equalsIgnoreCase("RELATION"))||(iblk.contents==null))
					continue;
				String relClanID=XMLManager.getValFromPieces(iblk.contents,"CLAN");
				int rel=XMLManager.getIntFromPieces(iblk.contents,"STATUS");
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

	public DVector getMemberList()
	{
		return getMemberList(-1);
	}

	public DVector getMemberList(int PosFilter)
	{
		DVector filteredMembers=new DVector(3);
		Vector members=new Vector();
		Vector roles=new Vector();
		Vector lastDates=new Vector();
		CMClass.DBEngine().DBClanFill(this.ID(), members, roles, lastDates);
		for(int s=0;s<members.size();s++)
		{
			int posFilter=((Integer)roles.elementAt(s)).intValue();
			if((posFilter==PosFilter)||(PosFilter<0))
				filteredMembers.addElement(members.elementAt(s),roles.elementAt(s),lastDates.elementAt(s));
		}
		return filteredMembers;
	}

	public int getNumVoters(int function)
	{
		int realmembers=0;
		int bosses=0;
		DVector members=getMemberList();
		for(int m=0;m<members.size();m++)
		{
			if(((Integer)members.elementAt(m,2)).intValue()==Clan.POS_BOSS)
			{
				realmembers++;
				bosses++;
			}
			else
			if(((Integer)members.elementAt(m,2)).intValue()!=Clan.POS_APPLICANT)
				realmembers++;
		}
		int numVotes=bosses;
		if(getGovernment()==Clan.GVT_DEMOCRACY)
			numVotes=realmembers;
		else
		if((getGovernment()==Clan.GVT_REPUBLIC)&&(function==Clan.FUNC_CLANASSIGN))
			numVotes=realmembers;
		return numVotes;
	}


	public int getTopRank() {
		if((getGovernment()>=0)
		&&(getGovernment()<Clan.topRanks.length))
			return Clan.topRanks[getGovernment()];
		return POS_BOSS;
	}

	/** return a new instance of the object*/
	public Clan newInstance(){return new Clans();}
	public Clan copyOf()
	{
		try
		{
			return (Clan)this.clone();
		}
		catch(CloneNotSupportedException e)
		{
			return new Clans();
		}
	}

	public static void tickAllClans()
	{
		for(Enumeration e=clans();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			C.tick(C,MudHost.TICK_CLAN);
		}
	}
	
	public MOB getResponsibleMember()
	{
	    MOB mob=null;
        DVector DV=getMemberList();
        int newPos=-1;
		for(int i=0;i<DV.size();i++)
			if(((Integer)DV.elementAt(i,2)).intValue()>newPos)
			{    
			    mob=CMMap.getLoadPlayer((String)DV.elementAt(i,1));
			    if(mob!=null)
			        break;
			}
	    return mob;
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID!=MudHost.TICK_CLAN)
			return true;
		try{
			DVector members=getMemberList();
			int activeMembers=0;
			long deathMilis=CommonStrings.getIntVar(CommonStrings.SYSTEMI_DAYSCLANDEATH)*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)*MudHost.TICK_TIME;
			int[] numTypes=new int[Clan.POSORDER.length];
			for(int j=0;j<members.size();j++)
			{
				long lastLogin=((Long)members.elementAt(j,3)).longValue();
				if(((System.currentTimeMillis()-lastLogin)<deathMilis)||(deathMilis==0))
					activeMembers++;
				numTypes[getRoleOrder(((Integer)members.elementAt(j,2)).intValue())]++;
			}

			// handle any necessary promotions
			if((getGovernment()==Clan.GVT_DICTATORSHIP)
			||(getGovernment()==Clan.GVT_OLIGARCHY))
			{
				int highest=0;
				for(int i=numTypes.length-1;i>=0;i--)
					if(numTypes[i]>0){ highest=i; break;}
				int max=Clan.topRanks[getGovernment()];
				if(highest<getRoleOrder(max))
				{
					for(int i=0;i<members.size();i++)
					{
						if(getRoleOrder(((Integer)members.elementAt(i,2)).intValue())==highest)
						{
							String s=(String)members.elementAt(i,1);
							MOB M2=CMMap.getLoadPlayer(s);
							if(M2!=null) 
                            {
                                clanAnnounce(s+" is now a "+Clans.getRoleName(getGovernment(),max,true,false));
                                Log.sysOut("Clans",s+" of clan "+name()+" was autopromoted to "+getRoleName(getGovernment(),max,true,false)+".");
                                M2.setClanRole(max);
    							CMClass.DBEngine().DBUpdateClanMembership(s, ID(), max);
                            }
							break;
						}
					}
				}
			}


			if(activeMembers<CommonStrings.getIntVar(CommonStrings.SYSTEMI_MINCLANMEMBERS))
			{
				if(getStatus()==Clan.CLANSTATUS_FADING)
				{
					Log.sysOut("Clans","Clan '"+getName()+" deleted with only "+activeMembers+" having logged on lately.");
					destroyClan();
					StringBuffer buf=new StringBuffer("");
					for(int j=0;j<members.size();j++)
					{
						String s=(String)members.elementAt(j,1);
						long lastLogin=((Long)members.elementAt(j,3)).longValue();
						buf.append(s+" on "+new IQCalendar(lastLogin).d2String()+"  ");
					}
					Log.sysOut("Clans","Clan '"+getName()+" had the following membership: "+buf.toString());
					return true;
				}
				setStatus(Clan.CLANSTATUS_FADING);
				Log.sysOut("Clans","Clan '"+getName()+" fading with only "+activeMembers+" having logged on lately.");
				clanAnnounce(""+typeName()+" "+name()+" is in danger of being deleted if more members do not log on within 24 hours.");
			}
			else
			switch(getStatus())
			{
			case Clan.CLANSTATUS_FADING:
				setStatus(Clan.CLANSTATUS_ACTIVE);
				clanAnnounce(""+typeName()+" "+name()+" is no longer in danger of being deleted.  Be aware that there is required activity level.");
				break;
			case Clan.CLANSTATUS_PENDING:
				setStatus(Clan.CLANSTATUS_ACTIVE);
				Log.sysOut("Clans",""+typeName()+" '"+getName()+" now active with "+activeMembers+".");
				clanAnnounce(""+typeName()+" "+name()+" now has sufficient members.  The "+typeName()+" is now fully approved.");
				break;
			default:
				break;
			}


			// now do votes
			if((getGovernment()!=Clan.GVT_DICTATORSHIP)&&(votes()!=null))
			{
				boolean updateVotes=false;
				Vector votesToRemove=new Vector();
				Vector data=null;
				switch(getGovernment())
				{
				case Clan.GVT_DEMOCRACY:
					data=Util.parseCommas(CommonStrings.getVar(CommonStrings.SYSTEM_CLANVOTED),false);
					break;
				case Clan.GVT_OLIGARCHY:
					data=Util.parseCommas(CommonStrings.getVar(CommonStrings.SYSTEM_CLANVOTEO),false);
					break;
				case Clan.GVT_REPUBLIC:
					data=Util.parseCommas(CommonStrings.getVar(CommonStrings.SYSTEM_CLANVOTER),false);
					break;
				default:
					data=new Vector();
					break;
				}
				long duration=54;
				if(data.size()>0) duration=Util.s_long((String)data.firstElement());
				if(duration<=0) duration=54;
				duration=duration*CommonStrings.getIntVar(CommonStrings.SYSTEMI_TICKSPERMUDDAY)*MudHost.TICK_TIME;
				for(Enumeration e=votes();e.hasMoreElements();)
				{
					ClanVote CV=(ClanVote)e.nextElement();
					int numVotes=getNumVoters(CV.function);
					int quorum=50;
					if(data.size()>1) quorum=Util.s_int((String)data.lastElement());
					quorum=(int)Math.round(Util.mul(Util.div(quorum,100.0),numVotes));
					if(quorum<2) quorum=2;
					if(numVotes==1) quorum=1;
					long endsOn=CV.voteStarted+duration;
					if(CV.voteStatus==Clan.VSTAT_STARTED)
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
								CV.voteStatus=Clan.VSTAT_FAILED;
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
									CV.voteStatus=Clan.VSTAT_FAILED;
								else
								{
									CV.voteStatus=Clan.VSTAT_PASSED;
									MOB mob=CMClass.getMOB("StdMOB");
									mob.setName(ID());
									mob.setClanID(ID());
									mob.setClanRole(Clan.POS_BOSS);
									mob.baseEnvStats().setLevel(1000);
									if(mob.location()==null)
									{
										mob.setLocation(mob.getStartRoom());
										if(mob.location()==null)
											mob.setLocation(CMMap.getRandomRoom());
									}
									Vector V=Util.parse(CV.matter);
									mob.doCommand(V);
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
			
			if(Clans.trophySystemActive())
			{
			    // calculate winner of the exp contest
			    if(CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPEXP).length()>0)
			    {
			        Clan winner=null;
			        for(Enumeration e=Clans.clans();e.hasMoreElements();)
			        {
			            Clan C=(Clan)e.nextElement();
			            if((winner==null)||(C.getExp()>winner.getExp()))
			                winner=C;
			        }
			        if(winner==this)
			        {
			            if((!Util.bset(getTrophies(),Clan.TROPHY_EXP))&&(getExp()>0))
			            {
			                setTrophies(getTrophies()|Clan.TROPHY_EXP);
			                Clans.clanAnnounceAll("The "+typeName()+" "+name()+" has been awarded the trophy for "+Clans.TROPHY_DESCS[Clan.TROPHY_EXP]+".");
			            }
			        }
			        else
			        if(Util.bset(getTrophies(),Clan.TROPHY_EXP))
			        {
		                setTrophies(getTrophies()-Clan.TROPHY_EXP);
			            clanAnnounce("Your "+typeName()+" has lost control of the trophy for "+Clans.TROPHY_DESCS[Clan.TROPHY_EXP]+".");
			        }
			    }
			    
			    // calculate winner of the conquest contests
			    if((CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPAREA).length()>0)
			    ||(CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPCP).length()>0))
			    {
			        long mostClansControlled=-1;
			        Clan winnerMostClansControlled=null;
			        long mostControlPoints=-1;
			        Clan winnerMostControlPoints=null;
			        Vector tempControl=null;
			        long tempNumber=0;
			        for(Enumeration e=Clans.clans();e.hasMoreElements();)
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
			        &&(CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPAREA).length()>0)
			        &&(mostClansControlled>0))
			        {
			            if(!Util.bset(getTrophies(),Clan.TROPHY_AREA))
			            {
			                setTrophies(getTrophies()|Clan.TROPHY_AREA);
			                Clans.clanAnnounceAll("The "+typeName()+" "+name()+" has been awarded the trophy for "+Clans.TROPHY_DESCS[Clan.TROPHY_AREA]+".");
			            }
			        }
			        else
			        if(Util.bset(getTrophies(),Clan.TROPHY_AREA))
			        {
		                setTrophies(getTrophies()-Clan.TROPHY_AREA);
			            clanAnnounce("Your "+typeName()+" has lost control of the trophy for "+Clans.TROPHY_DESCS[Clan.TROPHY_AREA]+".");
			        }
			        if((winnerMostControlPoints==this)
			        &&(CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPCP).length()>0)
			        &&(mostControlPoints>0))
			        {
			            if(!Util.bset(getTrophies(),Clan.TROPHY_CONTROL))
			            {
			                setTrophies(getTrophies()|Clan.TROPHY_CONTROL);
			                Clans.clanAnnounceAll("The "+typeName()+" "+name()+" has been awarded the trophy for "+Clans.TROPHY_DESCS[Clan.TROPHY_CONTROL]+".");
			            }
			        }
			        else
			        if(Util.bset(getTrophies(),Clan.TROPHY_CONTROL))
			        {
		                setTrophies(getTrophies()-Clan.TROPHY_CONTROL);
			            clanAnnounce("Your "+typeName()+" has lost control of the trophy for "+Clans.TROPHY_DESCS[Clan.TROPHY_CONTROL]+".");
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
		CommonMsgs.channel("CLANTALK",ID(),msg,true);
	}

	public static void clanAnnounceAll(String msg)
	{
        for(Enumeration e=Clans.clans();e.hasMoreElements();)
        {
            Clan C=(Clan)e.nextElement();
            C.clanAnnounce(msg);
        }
	}

	public int applyExpMods(int exp)
	{
	    boolean changed=false;
	    if((getTaxes()>0.0)&&(exp>1))
	    {
			int clanshare=(int)Math.round(Util.mul(exp,getTaxes()));
			if(clanshare>0)
			{
			    exp-=clanshare;
				adjExp(clanshare);
				changed=true;
			}
	    }
	    for(int i=0;i<Clans.TROPHY_DESCS_SHORT.length;i++)
		    if((Clan.TROPHY_DESCS_SHORT[i].length()>0)
		    &&(Util.bset(getTrophies(),i)))
		    {
		        String awardStr=null;
		        switch(i)
		        {
		        case Clan.TROPHY_AREA: awardStr=CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPAREA); break;
		        case Clan.TROPHY_CONTROL: awardStr=CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPCP); break;
		        case Clan.TROPHY_EXP: awardStr=CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPEXP); break;
		        default: awardStr=null;
		        }
		        if(awardStr!=null)
		        {
		            int amount=0;
		            double pct=0.0;
		            Vector V=Util.parse(awardStr);
		            if(V.size()>=2)
		            {
		                String type=((String)V.lastElement()).toUpperCase();
		                String amt=(String)V.firstElement();
		                if(amt.endsWith("%"))
		                    pct=Util.div(Util.s_int(amt.substring(0,amt.length()-1)),100.0);
		                else
		                    amount=Util.s_int(amt);
		                if("EXPERIENCE".startsWith(type))
		                    exp+=((int)Math.round(Util.mul(exp,pct)))+amount;
		            }
		        }
		    }
	    if(changed) update();
	    return exp;
	}
	
	public static String translatePrize(int trophy)
	{
	    String prizeStr="";
	    switch(trophy)
	    {
	    	case Clan.TROPHY_AREA: prizeStr=CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPAREA); break;
	    	case Clan.TROPHY_CONTROL: prizeStr=CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPCP); break;
	    	case Clan.TROPHY_EXP: prizeStr=CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPEXP); break;
	    }
	    if(prizeStr.length()==0) return "None";
        if(prizeStr.length()>0)
        {
            Vector V=Util.parse(prizeStr);
            if(V.size()>=2)
            {
                String type=((String)V.lastElement()).toUpperCase();
                String amt=(String)V.firstElement();
                if("EXPERIENCE".startsWith(type))
                    return amt+" experience point bonus.";
            }
        }
	    return prizeStr;
	}
	public static boolean trophySystemActive()
	{
	    return (CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPAREA).length()>0)
	        || (CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPCP).length()>0)
	        || (CommonStrings.getVar(CommonStrings.SYSTEM_CLANTROPEXP).length()>0);
	    
	}
	public static class ClanVote
	{
		public String voteStarter="";
		public int voteStatus=0;
		public long voteStarted=0;
		public String matter="";
		public int function=0;
		public DVector votes=null;
	}


}