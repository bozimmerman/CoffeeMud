package com.planet_ink.coffee_mud.common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
/**
  * Global utility Vector for holding and creating Clans
  * @author=Jeremy Vyska
  */
public class Clans implements Clan, Tickable
{
	private static Hashtable all=new Hashtable();
		
	private long tickStatus=Tickable.STATUS_NOT;
	public long getTickStatus(){return tickStatus;}
	protected String clanName="";
	protected String clanPremise="";
	protected String clanRecall="";
	protected String clanDonationRoom="";
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
		ExternalPlay.DBDeleteData(ID(),"CLANVOTES",ID()+"/CLANVOTES");
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
			ExternalPlay.DBCreateData(ID(),"CLANVOTES",ID()+"/CLANVOTES","<BALLOTS>"+str.toString()+"</BALLOTS>");
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

	public Enumeration votes()
	{
		if(voteList==null)
		{
			Vector V=ExternalPlay.DBReadData(ID(),"CLANVOTES",ID()+"/CLANVOTES");
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
		if(howMuch<0) exp=0;
	}
	
	public void setTaxes(double rate){
		taxRate=rate;
	}
	public double getTaxes(){return taxRate;}
	
	public static void shutdownClans()
	{
		for(Enumeration e=all.elements();e.hasMoreElements();)
		{
			Clan C=(Clan)e.nextElement();
			ExternalPlay.deleteTick(C,Host.CLAN_TICK);
		}
		all.clear();
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
			&&(((C1.getClanRelations(C.ID())==Clan.REL_ALLY)&&(C2.getClanRelations(C.ID())==Clan.REL_WAR)))
				||((C2.getClanRelations(C.ID())==Clan.REL_ALLY)&&(C1.getClanRelations(C.ID())==Clan.REL_WAR)))
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
		return (Clan)all.get(id.toUpperCase());
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
		if(i!=null) return (long)i[1];
		return 0;
	}
	public void setClanRelations(String id, int rel)
	{
		relations.remove(id.toUpperCase());
		long[] i=new long[2];
		i[0]=(long)rel;
		i[1]=System.currentTimeMillis();
		relations.put(id.toUpperCase(),i);
	}
	
	public int getGovernment(){return government;}
	public void setGovernment(int type){government=type;}
	
	public static String getRoleName(int government, int role, boolean titleCase, boolean plural)
	{
		StringBuffer roleName=new StringBuffer();
		if((government<0)||(government>=Clan.ROL_DESCS.length))
			government=0;
		String[] roles=Clan.ROL_DESCS[government];
		Character c;
		switch(role)
		{
		case Clan.POS_APPLICANT:
			roleName.append(roles[0].toLowerCase());
			break;
		case Clan.POS_STAFF:
			roleName.append(roles[2].toLowerCase());
			break;
		case Clan.POS_MEMBER:
			roleName.append(roles[1].toLowerCase());
			break;
		case Clan.POS_ENCHANTER:
			roleName.append(roles[3].toLowerCase());
			break;
		case Clan.POS_TREASURER:
			roleName.append(roles[4].toLowerCase());
			break;
		case Clan.POS_LEADER:
			roleName.append(roles[5].toLowerCase());
			break;
		case Clan.POS_BOSS:
			roleName.append(roles[6].toLowerCase());
			break;
		default:
			roleName.append("member");
			break;
		}
		if(titleCase)
		{
			String titled=Util.capitalize(roleName.toString());
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
		ExternalPlay.DBCreateClan(this);
		addClan(this);
	}

	public void update()
	{
		ExternalPlay.DBUpdateClan(this);
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
				ExternalPlay.DBUpdateClanMembership(M.Name(), "", 0);
			}
		}
		ExternalPlay.DBDeleteClan(this);
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
		ExternalPlay.startTickDown(C,Host.CLAN_TICK,(int)Host.TICKS_PER_MUDDAY);
		all.put(C.ID().toUpperCase(),C);
	}
	public static void removeClan(Clan C)
	{
		ExternalPlay.deleteTick(C,Host.CLAN_TICK);
		all.remove(C.ID().toUpperCase());
	}
	
	public String getDetail(MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append("^x"+typeName()+" Profile   :^.^N "+ID()+"\n\r"
		          +"-----------------------------------------------------------------\n\r"
		          +getPremise()+"\n\r"
		          +"-----------------------------------------------------------------\n\r"
				  +"^xType            :^.^N "+Util.capitalize(Clan.GVT_DESCS[getGovernment()])+"\n\r"
				  +"^xQualifications  :^.^N "+((getAcceptanceSettings().length()==0)?"Anyone may apply":SaucerSupport.zapperDesc(getAcceptanceSettings()))+"\n\r");
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
					msg.append(Util.capitalize(Clan.REL_DESCS[getClanRelations(C.ID())]));
					int orel=C.getClanRelations(ID());
					if(orel!=Clan.REL_NEUTRAL)
						msg.append(" (<-"+Util.capitalize(Clan.REL_DESCS[orel])+")");
					msg.append("\n\r");
				}
			}
		}
		if((mob.getClanID().equalsIgnoreCase(ID()))
		||(Util.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)))
		{
			msg.append("-----------------------------------------------------------------\n\r"
			          +"^x"+Util.padRight(Clans.getRoleName(getGovernment(),Clan.POS_MEMBER,true,true),16)
					  +":^.^N "+crewList(Clan.POS_MEMBER)+"\n\r");
			if(allowedToDoThis(mob,Clan.FUNC_CLANACCEPT)>=0)
			{
				msg.append("-----------------------------------------------------------------\n\r"
				        +"^x"+Util.padRight(Clans.getRoleName(getGovernment(),Clan.POS_APPLICANT,true,true),16)+":^.^N "+crewList(Clan.POS_APPLICANT)+"\n\r");
			}
		}
		return msg.toString();
	}

	private String crewList(int posType)
	{
		StringBuffer list=new StringBuffer("");
		MOB m;
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
				return Util.capitalize(Clan.GVT_DESCS[getGovernment()].toLowerCase());
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
				return (role==Clan.POS_BOSS)?1:-1;
			case FUNC_CLANASSIGN:
				return 0;
			case FUNC_CLANEXILE:
				return (role==Clan.POS_BOSS)?1:-1;
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
				return (role==Clan.POS_BOSS)?1:-1;
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
			if(ExternalPlay.DBUserSearch(null,member))
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
		ExternalPlay.DBClanFill(this.ID(), members, new Vector(), new Vector());
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
				setClanRelations(relClanID,rel);
			}
		}
	}

	public int getStatus() { return ClanStatus; }
	public void setStatus(int newStatus) { ClanStatus=newStatus; }

	public String getRecall() { return clanRecall; }
	public void setRecall(String newRecall) { clanRecall=newRecall; }

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
		ExternalPlay.DBClanFill(this.ID(), members, roles, lastDates);
		for(int s=0;s<members.size();s++)
		{
			int posFilter=((Integer)roles.elementAt(s)).intValue();
			if((posFilter==PosFilter)||(PosFilter<0))
				filteredMembers.addElement((String)members.elementAt(s),roles.elementAt(s),lastDates.elementAt(s));
		}
		return filteredMembers;
	}

	public int getNumVoters(int function)
	{
		Vector realmembers=new Vector();
		Vector bosses=new Vector();
		DVector members=getMemberList();
		for(int m=0;m<members.size();m++)
		{
			if(((Integer)members.elementAt(m,2)).intValue()==Clan.POS_BOSS)
			{
				realmembers.addElement(members.elementAt(m,1));
				bosses.addElement(members.elementAt(m,1));
			}
			else
			if(((Integer)members.elementAt(m,2)).intValue()!=Clan.POS_APPLICANT)
				realmembers.addElement(members.elementAt(m,1));
		}
		int numVotes=bosses.size();
		if(getGovernment()==Clan.GVT_DEMOCRACY)
			numVotes=realmembers.size();
		else
		if((getGovernment()==Clan.GVT_REPUBLIC)&&(function==Clan.FUNC_CLANASSIGN))
			numVotes=realmembers.size();
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
			C.tick(C,Host.CLAN_TICK);
		}
	}
	
	public boolean tick(Tickable ticking, int tickID)
	{
		if(tickID!=Host.CLAN_TICK)
			return true;
		try{
			DVector members=getMemberList();
			int activeMembers=0;
			long deathMilis=CommonStrings.getIntVar(CommonStrings.SYSTEMI_DAYSCLANDEATH)*Host.TICKS_PER_MUDDAY*Host.TICK_TIME;
			for(int j=0;j<members.size();j++)
			{
				long lastLogin=((Long)members.elementAt(j,3)).longValue();
				if((System.currentTimeMillis()-lastLogin)<deathMilis)
					activeMembers++;
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
				else
				{
					setStatus(Clan.CLANSTATUS_FADING);
					Log.sysOut("Clans","Clan '"+getName()+" fading with only "+activeMembers+" having logged on lately.");
					clanAnnounce(""+typeName()+" "+name()+" is in danger of being deleted if more members do not log on within 24 hours.");
				}
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
					data=Util.parseCommas(CommonStrings.getVar(CommonStrings.SYSTEM_CLANVOTED));
					break;
				case Clan.GVT_OLIGARCHY:
					data=Util.parseCommas(CommonStrings.getVar(CommonStrings.SYSTEM_CLANVOTEO));
					break;
				case Clan.GVT_REPUBLIC:
					data=Util.parseCommas(CommonStrings.getVar(CommonStrings.SYSTEM_CLANVOTER));
					break;
				default:
					data=new Vector();
					break;
				}
				long duration=54;
				if(data.size()>0) duration=Util.s_long((String)data.firstElement());
				if(duration<=0) duration=54;
				duration=duration*Host.TICKS_PER_MUDDAY*Host.TICK_TIME;
				for(Enumeration e=votes();e.hasMoreElements();)
				{
					ClanVote CV=(ClanVote)e.nextElement();
					int numVotes=getNumVoters(CV.function);
					int quorum=50;
					if(data.size()>1) quorum=Util.s_int((String)data.lastElement());
					quorum=(int)Math.round(Util.mul(Util.div((int)quorum,100.0),numVotes));
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
									try{
										Vector V=Util.parse(CV.matter);
										ExternalPlay.doCommand(mob,V);
									}
									catch(Exception x){}
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
			update(); // also saves exp
		}
		catch(Exception x2)
		{
			Log.errOut("Clans",x2);
		}
		return true;
	}
	
	public void clanAnnounce(String msg)
	{
		ExternalPlay.channel("CLANTALK",ID(),msg,true);
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