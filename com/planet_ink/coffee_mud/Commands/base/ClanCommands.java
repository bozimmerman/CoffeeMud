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
				if(C==null)
				{
					mob.tell("There is no longer a clan called "+mob.getClanID()+".");
					return false;
				}
				if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
				{
					Vector apps=C.getMemberList(Clan.POS_APPLICANT);
					if(apps.size()<1)
					{
						mob.tell("There are no applicants to your "+C.typeName()+".");
						return false;
					}
					qual=Util.capitalize(qual);
					for(int q=0;q<apps.size();q++)
					{
						if(((String)apps.elementAt(q)).equalsIgnoreCase(qual))
						{
							found=true;
						}
					}
					if(found)
					{
						MOB M= Clans.getMOB(qual);
						if(M==null)
						{
							mob.tell(qual+" was not found.  Could not add to "+C.typeName()+".");
							return false;
						}
						else
						{
							clanAnnounce(mob,"New Member of "+C.name()+": "+M.Name());
							M.setClanID(mob.getClanID());
							M.setClanRole(Clan.POS_MEMBER);
							ExternalPlay.DBUpdateClanMembership(qual, mob.getClanID(), Clan.POS_MEMBER);
							mob.tell(M.Name()+" has been accepted into "+C.typeName()+" '"+C.ID()+"'.");
							M.tell(mob.Name()+" has accepted you as a member of "+C.typeName()+" '"+C.ID()+"'.");
							addClanHomeSpell(M);
							ExternalPlay.DBUpdateMOB(M);
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
					if((SaucerSupport.zapperCheck(C.getAcceptanceSettings(),mob))
					&&(SaucerSupport.zapperCheck("-<"+CommonStrings.getIntVar(CommonStrings.SYSTEMI_MINCLANLEVEL),mob)))
					{
						ExternalPlay.DBUpdateClanMembership(mob.Name(), C.ID(), Clan.POS_APPLICANT);
						mob.setClanID(C.ID());
						mob.setClanRole(Clan.POS_APPLICANT);
						Vector msgVector=new Vector();
						msgVector.addElement(new String("CLANTALK"));
						msgVector.addElement(new String("New Applicant: "+mob.Name()));
						Channels.channel(mob,msgVector,true);
					}
					else
					{
						msg.append("You are not of the right qualities to join "+C.ID());
					}
				}
				else
				{
					msg.append("There is no clan named '"+qual+"'.");
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
		if(commands.size()<3)
		{
			mob.tell("You must specify the members name, and a new role.");
			return false;
		}
		String qual=((String)commands.elementAt(1)).toUpperCase();
		String pos=Util.combine(commands,2).toUpperCase();
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
				if(C==null)
				{
					mob.tell("There is no longer a clan called "+mob.getClanID()+".");
					return false;
				}
				if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
				{
					Vector apps=C.getMemberList();
					if(apps.size()<1)
					{
						mob.tell("There are no members in your "+C.typeName()+"");
						return false;
					}
					int newPos=getRoleFromName(pos);
					if((newPos==0)&&(!pos.equalsIgnoreCase("Applicant")))
					{
						mob.tell("'"+pos+"' is not a valid role.");
						return false;
					}
					qual=Util.capitalize(qual);
					for(int q=0;q<apps.size();q++)
					{
						if(((String)apps.elementAt(q)).equalsIgnoreCase(qual))
						{
							found=true;
						}
					}
					if(found)
					{
						MOB M=Clans.getMOB(qual);
						if(M==null)
						{
							mob.tell(qual+" was not found.  Could not change "+C.typeName()+" role.");
							return false;
						}
						else
						{
							clanAnnounce(mob,M.Name()+" changed from "+Clans.getRoleName(M.getClanRole(),true,false)+" to "+Clans.getRoleName(newPos,true,false));
							M.setClanRole(newPos);
							ExternalPlay.DBUpdateClanMembership(M.Name(), C.ID(), newPos);
							mob.tell(M.Name()+" has been assigned to be "+Util.startWithAorAn(Clans.getRoleName(newPos,false,false))+" of "+C.typeName()+" '"+C.ID()+"'.");
							M.tell("You have been assigned to be "+Util.startWithAorAn(Clans.getRoleName(newPos,false,false))+" of "+C.typeName()+" '"+C.ID()+"'.");
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

	private static int getRoleFromName(String position)
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
			return 0;
		}
		return newPos;
	}

	public static boolean clandetails(MOB mob, Vector commands)
	{
		String qual=Util.combine(commands,1).toUpperCase();
		if(qual.length()==0) qual=mob.getClanID();
		StringBuffer msg=new StringBuffer("");
		if(qual.length()>0)
		{
			boolean found=false;
			for(int j=0;j<Clans.size();j++)
			{
				Clan C=Clans.elementAt(j);
				if(CoffeeUtensils.containsString(C.ID(), qual))
				{
					msg.append(C.getDetail(mob));
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
			msg.append("You need to specify which clan you would like details on. Try 'CLANLIST'.\n\r");
		}
		mob.tell(msg.toString());
		return false;
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
				if(C==null)
				{
					mob.tell("There is no longer a clan called "+mob.getClanID()+".");
					return false;
				}
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
						MOB M=Clans.getMOB(qual);
						if(M==null)
						{
							mob.tell(qual+" was not found.  Could not exile from "+C.typeName()+".");
							return false;
						}
						else
						{
							clanAnnounce(mob,"Member exiled from "+C.name()+": "+M.Name());
							ExternalPlay.DBUpdateClanMembership(qual, "", 0);
							M.setClanID("");
							M.setClanRole(0);
							mob.tell(M.Name()+" has been exiled from Clan '"+C.ID()+"'.");
							M.tell("You have been exiled from Clan '"+C.ID()+"'.");
							delClanHomeSpell(M);
							ExternalPlay.DBUpdateMOB(M);
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
			if(C==null)
			{
				mob.tell("There is no longer a clan called "+mob.getClanID()+".");
				return false;
			}
			if(C.getStatus()>Clan.CLANSTATUS_ACTIVE)
			{
				mob.tell("You cannot set a home.  Your clan does not have enough members to be considered active.");
				return false;
			}
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
						C.setRecall(R.roomID());
						C.update();
						mob.tell("Your clan home is now set to "+R.displayText()+".");
						clanAnnounce(mob, "Your clan home is now set to "+R.displayText()+".");
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

	public static boolean clandonateset(MOB mob, Vector commands)
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
			if(C==null)
			{
				mob.tell("There is no longer a clan called "+mob.getClanID()+".");
				return false;
			}
			if(C.getStatus()>Clan.CLANSTATUS_ACTIVE)
			{
				mob.tell("You cannot set a donation room.  Your clan does not have enough members to be considered active.");
				return false;
			}
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
						C.setDonation(R.roomID());
						C.update();
						mob.tell("Your clan donation is now set to "+R.displayText()+".");
						clanAnnounce(mob, "Your clan donation is now set to "+R.displayText()+".");
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
		head.append("^x[");
		head.append(Util.padRight("Clan Name",24)+" | ");
		head.append(Util.padRight("Status",8)+" | ");
		head.append(Util.padRight("Clan Members",12));
		head.append("]^.^? \n\r");
		StringBuffer msg=new StringBuffer("");
		for(int c=0;c<Clans.size();c++)
		{
			Clan thisClan=Clans.elementAt(c);
			msg.append(" ");
			msg.append(Util.padRight(thisClan.ID(),24)+"   ");
			String status="Active";
			switch(thisClan.getStatus())
			{
			case Clan.CLANSTATUS_FADING:
				status="Inactive";
				break;
			case Clan.CLANSTATUS_PENDING:
				status="Pending";
				break;
			}
			msg.append(Util.padRight(status,8)+"   ");
			msg.append(Util.padRight(new Integer(thisClan.getSize()).toString(),12));
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
				if(C==null)
				{
					mob.tell("There is no longer a clan called "+mob.getClanID()+".");
					return false;
				}
				if((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_LEADER))
				{
					Vector apps=C.getMemberList(Clan.POS_APPLICANT);
					if(apps.size()<1)
					{
					  mob.tell("There are no applicants to your "+C.typeName()+".");
					  return false;
					}
					qual=Util.capitalize(qual);
					for(int q=0;q<apps.size();q++)
					{
						if(((String)apps.elementAt(q)).equalsIgnoreCase(qual))
						{
							found=true;
						}
					}
					if(found)
					{
						MOB M=Clans.getMOB(qual);
						if(M==null)
						{
							mob.tell(qual+" was not found.  Could not reject from "+C.typeName()+".");
							return false;
						}
						else
						{
							M.setClanID("");
							M.setClanRole(0);
							ExternalPlay.DBUpdateClanMembership(M.Name(), "", 0);
							mob.tell(M.Name()+" has been denied acceptance to "+C.typeName()+" '"+C.ID()+"'.");
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
		if((mob.getClanID()==null)
		||(mob.getClanID().equalsIgnoreCase(""))
		||(Clans.getClan(mob.getClanID())==null))
		{
			msg.append("You aren't even a member of a clan.");
		}
		else
		if(!mob.isMonster())
		{
			Clan C=Clans.getClan(mob.getClanID());
			try
			{
				String check=mob.session().prompt("Are you absolutely SURE (y/N)?","N");
				if(check.equalsIgnoreCase("Y"))
				{
					clanAnnounce(mob,new String("Member resigned from "+C.name()+": "+mob.Name()));
					ExternalPlay.DBUpdateClanMembership(mob.Name(), "", 0);
					mob.setClanID("");
					mob.setClanRole(0);
					delClanHomeSpell(mob);
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

	public static boolean clanpremise(MOB mob, Vector commands)
	{
		StringBuffer msg=new StringBuffer("");
		if((mob.getClanID()==null)
		||(mob.getClanID().equalsIgnoreCase(""))
		||(Clans.getClan(mob.getClanID())==null))
		{
			msg.append("You aren't even a member of a clan.");
		}
		else
		if(mob.getClanRole()!=Clans.POS_BOSS)
		{
			msg.append("Only the boss can do that.");
		}
		else
		if(!mob.isMonster())
		{
			Clan C=Clans.getClan(mob.getClanID());
			try
			{
				String premise=mob.session().prompt("Describe your Clan's Premise\n\r: ","");
				if(premise.length()>0)
				{
					C.setPremise(premise);
					C.update();
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

	public static void clanCreate(MOB mob, Vector commands)
	{
		Room R=mob.location();
		String qual=Util.combine(commands,1).toUpperCase();
		StringBuffer msg=new StringBuffer("");
		if(qual.length()>0)
		{
			if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
			{
				Clan C=Clans.getClan(qual);
				if(C!=null)
				{
					msg.append("Clan "+C.ID()+" exists and is awaiting approval (type 'CLANLIST' and I'll show you what clans are pending).  You may 'CLANAPPLY' to join them.");

				}
				else
				if(!mob.isMonster())
				{
					int cost=CommonStrings.getIntVar(CommonStrings.SYSTEMI_CLANCOST);
					if(cost>0)
					{
						if(Money.totalMoney(mob)<cost)
						{
							mob.tell("It costs "+cost+" gold to create a clan.  You don't have it.");
							return;
						}
						Money.subtractMoney(null,mob,cost);
					}
					mob.tell("There is no Clan by that name.");
					try
					{
						String check=mob.session().prompt("Do you want to found a new clan (y/N)?","N");
						if(check.equalsIgnoreCase("Y"))
						{
							String doubleCheck=mob.session().prompt("Re-enter the name of your new Clan exactly how you want it:","");
							if(doubleCheck.length()<1)
								return;
							String lastCheck=mob.session().prompt("Is '"+doubleCheck+"' correct (y/N)?", "N");
							if(lastCheck.equalsIgnoreCase("Y"))
							{
								Clan newClan=Clans.getClanType(Clan.TYPE_CLAN);
								newClan.setName(doubleCheck);
								newClan.setStatus(Clan.CLANSTATUS_PENDING);
								Clans.createClan(newClan);
								ExternalPlay.DBUpdateClanMembership(mob.Name(),newClan.getName(),newClan.getTopRank());
								addClanHomeSpell(mob);
								clanAnnounce(mob, "Your new clan is online and can now accept applicants.");
							}
						}
					}
					catch(java.io.IOException e)
					{
					}
				}
			}
			else
			{
				msg.append("You are already a member of "+mob.getClanID()+". You need to resign from your before you can create one.");
			}
		}
		else
		{
			msg.append("You haven't specified the name for the Clan you are trying to create.");
		}
		mob.tell(msg.toString());
	}

	public static void clanAnnounce(MOB mob, String msg)
	{
		Vector msgVector=new Vector();
		msgVector.addElement(new String("CLANTALK"));
		msgVector.addElement(new String(msg));
		Channels.channel(mob,msgVector,true);
	}

	public static void addClanHomeSpell(MOB M)
	{
		M.addAbility(CMClass.findAbility("Spell_ClanHome"));
		(M.fetchAbility("Spell_ClanHome")).setProfficiency(50);
		M.addAbility(CMClass.findAbility("Spell_ClanDonate"));
		(M.fetchAbility("Spell_ClanDonate")).setProfficiency(100);
		ExternalPlay.DBUpdateMOB(M);
	}

	public static void delClanHomeSpell(MOB mob)
	{
		mob.delAbility(mob.fetchAbility("Spell_ClanHome"));
		mob.delAbility(mob.fetchAbility("Spell_ClanDonate"));
		ExternalPlay.DBUpdateMOB(mob);
	}
}
