package com.planet_ink.coffee_mud.Commands.base;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class ClanCommands
{
	private ClanCommands(){}
	public static boolean clanaccept(MOB mob, Vector commands)
	{
		String qual=Util.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		Clan C=null;
		boolean found=false;
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				msg.append("You aren't even a member of a clan.");
			}
			else
			{
				C=Clans.getClan(mob.getClanID());
				if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
				{
					Vector apps=C.getMemberList(Clan.POS_APPLICANT);
					if(apps.size()<1)
					{
						mob.tell("There are no applicants to your "+C.typeName()+".");
						return false;
					}
					for(int q=0;q<apps.size();q++)
					{
						if(((String)apps.elementAt(q)).equalsIgnoreCase(qual))
						{
							found=true;
						}
					}
					if(found)
					{
						MOB M= CMMap.getPlayer(Util.capitalize(qual));
						if(M==null)
						{
							M=CMClass.getMOB("StdMOB");
							M.setName(qual);
							if(ExternalPlay.DBReadUserOnly(M))
							{
								Vector msgVector=new Vector();
								msgVector.addElement(new String("CLANTALK"));
								msgVector.addElement(new String("New Member: "+mob.ID()));
								Channels.channel(mob,msgVector,true);
								ExternalPlay.DBUpdateClan(qual, mob.getClanID(), Clan.POS_MEMBER);
								mob.tell(M.ID()+" has been accepted into to "+C.typeName()+" '"+C.ID()+"'.");
								return false;
							}
							else
							{
								mob.tell(qual+" was not found.  Could not add to "+C.typeName()+".");
								return false;
							}
						}
						else
						{
							Vector msgVector=new Vector();
							msgVector.addElement(new String("CLANTALK"));
							msgVector.addElement(new String("New Member: "+mob.ID()));
							Channels.channel(mob,msgVector,true);
							ExternalPlay.DBUpdateClan(qual, mob.getClanID(), Clan.POS_MEMBER);
							mob.tell(M.ID()+" has been accepted into "+C.typeName()+" '"+C.ID()+"'.");
							M.tell(mob.ID()+" has accepted you as a member of "+C.typeName()+" '"+C.ID()+"'.");
							return false;
						}
					}
					else
					{
						msg.append(qual+" isn't an applicant of your "+C.typeName()+".");
					}
				}
				else
				{
					msg.append("You aren't in the right position to accept members into your "+C.typeName()+".");
				}
			}
		}
		else
		{
			msg.append("You haven't specified which applicant you are accepting.");
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public static boolean clanapply(MOB mob, Vector commands)
	{
		String qual=Util.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				Clan C=Clans.getClan(qual);
				if(C!=null)
				{
					if(AbilityHelper.zapperCheck(C.getAcceptanceSettings(),mob))
					{
						ExternalPlay.DBUpdateClan(mob.ID(), C.ID(), Clan.POS_APPLICANT);
						Vector msgVector=new Vector();
						msgVector.addElement(new String("CLANTALK"));
						msgVector.addElement(new String("New Applicant: "+mob.ID()));
						Channels.channel(mob,msgVector,true);
					}
					else
					{
						msg.append("You are not of the right qualities to join "+C.ID());
					}
				}
				else
				{
					msg.append("There is no "+C.typeName()+" named '"+qual+"'.");
				}
			}
			else
			{
				msg.append("You are already a member of "+mob.getClanID()+". You need to resign from your before you can apply to another.");
			}
		}
		else
		{
			msg.append("You haven't specified which clan you are applying to.");
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public static boolean clanassign(MOB mob, Vector commands)
	{
		String qual=((String)commands.elementAt(1)).toUpperCase();
		String pos=((String)commands.elementAt(2)).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		Clan C=null;
		boolean found=false;
		int newPos;
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				msg.append("You aren't even a member of a clan.");
			}
			else
			{
				C=Clans.getClan(mob.getClanID());
				if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
				{
					Vector apps=C.getMemberList();
					if(apps.size()<1)
					{
						mob.tell("There are no members in your "+C.typeName()+"");
						return false;
					}
					for(int q=0;q<apps.size();q++)
					{
						if(((String)apps.elementAt(q)).equalsIgnoreCase(qual))
						{
							found=true;
						}
					}
					if(found)
					{
						MOB M=CMMap.getPlayer(Util.capitalize(qual));
						if(M==null)
						{
							M=CMClass.getMOB("StdMOB");
							M.setName(qual);
							if(ExternalPlay.DBReadUserOnly(M))
							{
								newPos=changeRole(M,C,pos);
								mob.tell(M.ID()+" has been assigned to be a "+Clans.getRoleName(newPos,false,false)+" of "+C.typeName()+" '"+C.ID()+"'.");
								return false;
							}
							else
							{
								mob.tell(qual+" was not found.  Could not change "+C.typeName()+" role.");
								return false;
							}
						}
						else
						{
							newPos=changeRole(M,C,pos);
							mob.tell(M.ID()+" has been assigned to be a "+Clans.getRoleName(newPos,false,false)+" of "+C.typeName()+" '"+C.ID()+"'.");
							M.tell("You have been assigned to be a"+Clans.getRoleName(newPos,false,false)+" of "+C.typeName()+" '"+C.ID()+"'.");
							return false;
						}
					}
					else
					{
						msg.append(qual+" isn't a member of your "+C.typeName()+".");
					}
				}
				else
				{
					msg.append("You aren't in the right position assign anyone in your "+C.typeName()+".");
				}
			}
		}
		else
		{
			msg.append("You haven't specified which member you are assigning a new role to.");
		}
		mob.tell(msg.toString());
		return false;
	}

	private static int changeRole(MOB mob, Clan clan, String position)
	{
		int newPos=0;
		if(position.equalsIgnoreCase("BOSS"))
		{
			newPos=Clan.POS_BOSS;
		}
		else
		if(position.equalsIgnoreCase("LEADER"))
		{
			newPos=Clan.POS_LEADER;
		}
		else
		if(position.equalsIgnoreCase("TREASURER"))
		{
			newPos=Clan.POS_TREASURER;
		}
		else
		if(position.equalsIgnoreCase("MEMBER"))
		{
			newPos=Clan.POS_MEMBER;
		}
		else
		{
			mob.tell("Unknown role '"+position+"'");
			return 0;
		}
		Vector msgVector=new Vector();
		msgVector.addElement(new String("CLANTALK"));
		msgVector.addElement(new String(mob.ID()+" changed from "+Clans.getRoleName(mob.getClanRole(),true,false)+" to "+Clans.getRoleName(newPos,true,false)));
		Channels.channel(mob,msgVector,true);
		ExternalPlay.DBUpdateClan(mob.ID(), clan.ID(), newPos);
		return newPos;
	}
	
	public static boolean clandetails(MOB mob, Vector commands)
	{
		String qual=Util.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		if(qual.length()>0)
		{
			boolean found=false;
			for(int j=0;j<Clans.size();j++)
			{
				Clan C=Clans.elementAt(j);
				if(CoffeeUtensils.containsString(C.ID(), qual))
				{
					msg.append(createDetailString(C,mob));
					found=true;
				}
			}
			if(!found)
			{
				msg.append("No clan was found by the name of '"+qual+"'.\n\r");
			}
		}
		else
		{
			msg.append("You need to specify which clan you would like details on.\n\r");
		}
		mob.tell(msg.toString());
		return false;
	}

	private static String createDetailString(Clan C, MOB mob)
	{
		StringBuffer msg=new StringBuffer("");
		msg.append(""+C.typeName()+" Profile: "+C.ID()+"\n\r"
		          +"-----------------------------------------------------------------\n\r"
		          +C.getPremise()+"\n\r"
		          +"-----------------------------------------------------------------\n\r"
		          +Util.padRight(Clans.getRoleName(Clan.POS_BOSS,true,true),16)+": "+crewList(C,Clan.POS_BOSS)+"\n\r"
		          +Util.padRight(Clans.getRoleName(Clan.POS_LEADER,true,true),16)+": "+crewList(C,Clan.POS_LEADER)+"\n\r"
		          +Util.padRight(Clans.getRoleName(Clan.POS_TREASURER,true,true),16)+": "+crewList(C,Clan.POS_TREASURER)+"\n\r"
		          +"Total Members   : "+C.getSize()+"\n\r"
		          +"Clan Alignment  : "+CommonStrings.alignmentStr(C.getAlign())+"\n\r");
		if(mob.getClanID().equalsIgnoreCase(C.ID()))
		{
			msg.append("-----------------------------------------------------------------\n\r"
			          +Util.padRight(Clans.getRoleName(Clan.POS_MEMBER,true,true),16)+": "+crewList(C,Clan.POS_MEMBER)+"\n\r");
			if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
			{
				msg.append("-----------------------------------------------------------------\n\r"
				        +Util.padRight(Clans.getRoleName(Clan.POS_APPLICANT,true,true),16)+": "+crewList(C,Clan.POS_APPLICANT)+"\n\r");
			}
		}

		if((mob.getBitmap()&MOB.ATT_SYSOPMSGS)>0)
		{
			msg.append("-----------------------------------------------------------------\n\r"
			          +Util.padRight(Clans.getRoleName(Clan.POS_MEMBER,true,true),16)+": "+crewList(C,Clan.POS_MEMBER)+"\n\r");
			msg.append("-----------------------------------------------------------------\n\r"
			          +Util.padRight(Clans.getRoleName(Clan.POS_APPLICANT,true,true),16)+": "+crewList(C,Clan.POS_APPLICANT)+"\n\r");
		}
		return msg.toString();
	}

	private static String crewList(Clan C, int posType)
	{
		StringBuffer list=new StringBuffer("");
		Vector Members=new Vector();
		MOB m;

		Members = C.getMemberList(posType);
		Members.trimToSize();
		if(Members.size()>1)
		{
			for(int j=0;j<(Members.size() - 1);j++)
			{
				list.append(Members.elementAt(j)+", ");
			}
			list.append("and "+Members.lastElement());
		}
		else
		if(Members.size()>0)
		{
			list.append((String)Members.firstElement());
		}
		return list.toString();
	}
	
	
	public static boolean clanexile(MOB mob, Vector commands)
	{
		String qual=Util.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		Clan C=null;
		boolean found=false;
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				msg.append("You aren't even a member of a clan.");
			}
			else
			{
				C=Clans.getClan(mob.getClanID());
				if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
				{
					Vector apps=C.getMemberList();
					if(apps.size()<1)
					{
						mob.tell("There are no members in your "+C.typeName()+".");
						return false;
					}
					for(int q=0;q<apps.size();q++)
					{
						if(((String)apps.elementAt(q)).equalsIgnoreCase(qual))
						{
							found=true;
						}
					}
					if(found)
					{
						MOB M=CMMap.getPlayer(Util.capitalize(qual));
						if(M==null)
						{
							M=CMClass.getMOB("StdMOB");
							M.setName(qual);
							if(ExternalPlay.DBReadUserOnly(M))
							{
								Vector msgVector=new Vector();
								msgVector.addElement(new String("CLANTALK"));
								msgVector.addElement(new String("Member exiled: "+mob.ID()));
								Channels.channel(mob,msgVector,true);
								ExternalPlay.DBUpdateClan(qual, "", 0);
								mob.tell(M.ID()+" has been exiled from "+C.typeName()+" '"+C.ID()+"'.");
								return false;
							}
							else
							{
								mob.tell(qual+" was not found.  Could not exile from "+C.typeName()+".");
								return false;
							}
						}
						else
						{
							Vector msgVector=new Vector();
							msgVector.addElement(new String("CLANTALK"));
							msgVector.addElement(new String("Member exiled: "+mob.ID()));
							Channels.channel(mob,msgVector,true);
							ExternalPlay.DBUpdateClan(qual, "", 0);
							mob.tell(M.ID()+" has been exiled from Clan '"+C.ID()+"'.");
							M.tell("You have been exiled from Clan '"+C.ID()+"'.");
							return false;
						}
					}
					else
					{
						msg.append(qual+" isn't a member of your "+C.typeName()+".");
					}
				}
				else
				{
					msg.append("You aren't in the right position to exile anyone from your "+C.typeName()+".");
				}
			}
		}
		else
		{
			msg.append("You haven't specified which member you are exiling.");
		}
		mob.tell(msg.toString());
		return false;
	}
	
	public static boolean clanhomeset(MOB mob, Vector commands)
	{
		LandTitle l=null;
		Room R=mob.location();

		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		else
		{
			Clan C=Clans.getClan(mob.getClanID());
			if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
			{
				for(int a=0;a<R.numAffects();a++)
				{
					if(R.fetchAffect(a) instanceof LandTitle)
						l=(LandTitle)R.fetchAffect(a);
				}
				if(l==null)
				{
					mob.tell("Your "+C.typeName()+" does not own this room.");
					return false;
				}
				else
				{
					if(l.landOwner().equalsIgnoreCase(mob.getClanID()))
					{
						C.setRecall(R.ID());
						C.update();
					}
					else
					{
						mob.tell("Your "+C.typeName()+" does not own this room.");
						return false;
					}
				}
			}
			else
			{
				mob.tell("You aren't in the right position to set your "+C.typeName()+"'s home.");
				return false;
			}
		}
		return false;
	}
	
	public static boolean clanlist(MOB mob,Vector commands)
	{
		StringBuffer head=new StringBuffer("");
		head.append("[");
		head.append(Util.padRight("Clan Name",24)+" | ");
		head.append(Util.padRight("Clan Members",12)+" | ");
		head.append(Util.padRight("Alignment",16));
		head.append("] \n\r");
		StringBuffer msg=new StringBuffer("");
		for(int c=0;c<Clans.size();c++)
		{
			Clan thisClan=Clans.elementAt(c);
			msg.append(" ");
			msg.append(Util.padRight(thisClan.ID(),24)+"   ");
			msg.append(Util.padRight(new Integer(thisClan.getSize()).toString(),12)+"   ");
			msg.append(Util.padRight(CommonStrings.alignmentStr(thisClan.getAlign()),16));
			msg.append("\n\r");
		}
		mob.tell(head.toString()+msg.toString());
		return false;
	}
	
	
	public static boolean clanreject(MOB mob, Vector commands)
	{
		String qual=Util.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		Clan C=null;
		boolean found=false;
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				msg.append("You aren't even a member of a clan.");
			}
			else
			{
				C=Clans.getClan(mob.getClanID());
				if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
				{
					Vector apps=C.getMemberList(Clan.POS_APPLICANT);
					if(apps.size()<1)
					{
					  mob.tell("There are no applicants to your "+C.typeName()+".");
					  return false;
					}
					for(int q=0;q<apps.size();q++)
					{
						if(((String)apps.elementAt(q)).equalsIgnoreCase(qual))
						{
							found=true;
						}
					}
					if(found)
					{
						MOB M=CMMap.getPlayer(Util.capitalize(qual));
						if(M==null)
						{
							M=CMClass.getMOB("StdMOB");
							M.setName(qual);
							if(ExternalPlay.DBReadUserOnly(M))
							{
								ExternalPlay.DBUpdateClan(qual, "", 0);
								mob.tell(M.ID()+" has been denied acceptance to "+C.typeName()+" '"+C.ID()+"'.");
								return false;
							}
							else
							{
								mob.tell(qual+" was not found.  Could not reject from "+C.typeName()+".");
								return false;
							}
						}
						else
						{
							ExternalPlay.DBUpdateClan(qual, "", 0);
							mob.tell(M.ID()+" has been denied acceptance to "+C.typeName()+" '"+C.ID()+"'.");
							M.tell("You have been rejected as a member of "+C.typeName()+" '"+C.ID()+"'.");
							return false;
						}
					}
					else
					{
						msg.append(qual+" isn't a member of your "+C.typeName()+".");
					}
				}
				else
				{
				  msg.append("You aren't in the right position to reject applicants to your "+C.typeName()+".");
				}
			}
		}
		else
		{
			msg.append("You haven't specified which applicant you are rejecting.");
		}
		mob.tell(msg.toString());
		return false;
	}
	
	
	public static boolean clanresign(MOB mob, Vector commands)
	{
		StringBuffer msg=new StringBuffer("");
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			msg.append("You aren't even a member of a clan.");
		}
		else
		{
			try
			{
				String pwd=mob.session().prompt("Are you absolutely SURE (y/N)?","N");
				if(pwd.equalsIgnoreCase("Y"))
				{
					Vector msgVector=new Vector();
					msgVector.addElement(new String("CLANTALK"));
					msgVector.addElement(new String("Member resigned: "+mob.ID()));
					Channels.channel(mob,msgVector,true);
					ExternalPlay.DBUpdateClan(mob.ID(), "", 0);
				}
				else
				{
					return false;
				}
			}
			catch(java.io.IOException e)
			{
			}
		}
		mob.tell(msg.toString());
		return false;
	}
	
}
