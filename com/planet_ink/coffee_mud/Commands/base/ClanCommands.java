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
								clanAnnounce(mob,"New Member: "+mob.Name());
								ExternalPlay.DBUpdateClan(qual, mob.getClanID(), Clan.POS_MEMBER);
								mob.tell(M.Name()+" has been accepted into to "+C.typeName()+" '"+C.ID()+"'.");
								ExternalPlay.DBReadMOB(M);
								addClanHomeSpell(M);
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
							clanAnnounce(mob,"New Member: "+mob.Name());
							ExternalPlay.DBUpdateClan(qual, mob.getClanID(), Clan.POS_MEMBER);
							mob.tell(M.Name()+" has been accepted into "+C.typeName()+" '"+C.ID()+"'.");
							M.tell(mob.Name()+" has accepted you as a member of "+C.typeName()+" '"+C.ID()+"'.");
							addClanHomeSpell(M);
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
					if((AbilityHelper.zapperCheck(C.getAcceptanceSettings(),mob))
					&&(AbilityHelper.zapperCheck("-<"+CommonStrings.getIntVar(CommonStrings.SYSTEMI_MINCLANLEVEL),mob)))
					{
						ExternalPlay.DBUpdateClan(mob.Name(), C.ID(), Clan.POS_APPLICANT);
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
								mob.tell(M.Name()+" has been assigned to be a "+Clans.getRoleName(newPos,false,false)+" of "+C.typeName()+" '"+C.ID()+"'.");
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
							mob.tell(M.Name()+" has been assigned to be a "+Clans.getRoleName(newPos,false,false)+" of "+C.typeName()+" '"+C.ID()+"'.");
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
		clanAnnounce(mob,mob.Name()+" changed from "+Clans.getRoleName(mob.getClanRole(),true,false)+" to "+Clans.getRoleName(newPos,true,false));
		ExternalPlay.DBUpdateClan(mob.Name(), clan.ID(), newPos);
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
								clanAnnounce(mob,"Member exiled: "+mob.Name());
								ExternalPlay.DBUpdateClan(qual, "", 0);
								mob.tell(M.Name()+" has been exiled from "+C.typeName()+" '"+C.ID()+"'.");
								ExternalPlay.DBReadMOB(M);
								delClanHomeSpell(M);
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
							clanAnnounce(mob,"Member exiled: "+mob.Name());
							ExternalPlay.DBUpdateClan(qual, "", 0);
							mob.tell(M.Name()+" has been exiled from Clan '"+C.ID()+"'.");
							M.tell("You have been exiled from Clan '"+C.ID()+"'.");
							delClanHomeSpell(M);
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
			if(C.getStatus()>Clan.STATUS_ACTIVE)
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
						mob.tell("Your clan home is now set to "+R.name()+".");
						clanAnnounce(mob, "Your clan donation is now set to "+R.name()+".");
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
			if(C.getStatus()>Clan.STATUS_ACTIVE)
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
						mob.tell("Your clan donation is now set to "+R.name()+".");
						clanAnnounce(mob, "Your clan donation is now set to "+R.name()+".");
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
		ClanMaster master=null;
		Room R=mob.location();
		for(int i=0;i<R.numInhabitants();i++)
		{
			if(R.fetchInhabitant(i) instanceof ClanMaster)
			{
				master=(ClanMaster)R.fetchInhabitant(i);
				break;
			}
		}
		if(master==null)
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
				if(thisClan.getStatus()>Clan.STATUS_ACTIVE)
					continue;
				msg.append(" ");
				msg.append(Util.padRight(thisClan.ID(),24)+"   ");
				msg.append(Util.padRight(new Integer(thisClan.getSize()).toString(),12)+"   ");
				msg.append(Util.padRight(CommonStrings.alignmentStr(thisClan.getAlign()),16));
				msg.append("\n\r");
			}
			mob.tell(head.toString()+msg.toString());
			return false;
		}
		FullMsg newMsg=new FullMsg(mob,master,null,Affect.MSG_LIST,null);
		if(!mob.location().okAffect(mob,newMsg))
			return false;
		mob.location().send(mob,newMsg);
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
								mob.tell(M.Name()+" has been denied acceptance to "+C.typeName()+" '"+C.ID()+"'.");
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
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			msg.append("You aren't even a member of a clan.");
		}
		else
		{
			try
			{
				String check=mob.session().prompt("Are you absolutely SURE (y/N)?","N");
				if(check.equalsIgnoreCase("Y"))
				{
					clanAnnounce(mob,new String("Member resigned: "+mob.Name()));
					ExternalPlay.DBUpdateClan(mob.Name(), "", 0);
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

	public static boolean clanDeposit(MOB mob, Vector commands)
	{
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			mob.tell("You aren't a member of a clan.");
			return false;
		}
		Clan C=Clans.getClan(mob.getClanID());
		if(C.getStatus()>Clan.STATUS_ACTIVE)
		{
			mob.tell("You cannot access your account.  Your clan does not have enough members to be considered active.");
			return false;
		}
		Banker banker=null;
		Room R=mob.location();
		for(int i=0;i<R.numInhabitants();i++)
		{
			if(R.fetchInhabitant(i) instanceof Banker)
			{
				banker=(Banker)R.fetchInhabitant(i);
				break;
			}
		}
		if(banker==null)
		{
			mob.tell("There is no banker here to help you.");
			return false;
		}
		if(commands.size()<2)
		{
			ExternalPlay.quickSay((MOB)banker,mob,"Pardon, but clandeposit how much?",true,false);
			return false;
		}

		String likeThis=Util.combine(commands,1);
		if(!(Util.s_int(likeThis)>0))
		{
			ExternalPlay.quickSay((MOB)banker,mob,"I am sorry, but bank policy does not allow us to store items for clans, only funds.",true,false);
			return false;
		}
		Item thisThang=SocialProcessor.possibleGold(mob,likeThis);
		if((thisThang==null)||(!Sense.canBeSeenBy(thisThang,mob)))
		{
			mob.tell("You don't seem to be carrying that.");
			return false;
		}

		Coins older=(Coins)thisThang;
		Coins item=(Coins)CMClass.getItem("StdCoins");
		int newNum=older.numberOfCoins();
		Item old=banker.findDepositInventory(mob.getClanID(),""+Integer.MAX_VALUE);
		if((old!=null)&&(old instanceof Coins))
			newNum+=((Coins)old).numberOfCoins();
		item.setNumberOfCoins(newNum);
		if(old!=null)
			banker.delDepositInventory(mob.getClanID(),old);
		banker.addDepositInventory(mob.getClanID(),item);
		ExternalPlay.quickSay((MOB)banker,mob,"Thank you for your business!",true,false);
		return false;
	}

	public static boolean clanWithdraw(MOB mob, Vector commands)
	{
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		if(!((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_TREASURER)))
		{
			mob.tell("\n\rHuh?\n\r");
			return false;
		}
		Banker banker=null;
		Room R=mob.location();
		for(int i=0;i<R.numInhabitants();i++)
		{
			if(R.fetchInhabitant(i) instanceof Banker)
			{
				banker=(Banker)R.fetchInhabitant(i);
				break;
			}
		}
		if(banker==null)
		{
			mob.tell("There is no banker here to help you.");
			return false;
		}
		String str=(String)commands.firstElement();
		if(((String)commands.lastElement()).equalsIgnoreCase("coins"))
		{
			if((!str.equalsIgnoreCase("all"))
			&&(Util.s_int((String)commands.firstElement())<=0))
			{
				mob.tell("Withdraw how much?");
				return false;
			}

			commands.removeElement(commands.lastElement());
		}
		if(((String)commands.lastElement()).equalsIgnoreCase("gold"))
		{
			if((!str.equalsIgnoreCase("all"))
			&&(Util.s_int((String)commands.firstElement())<=0))
			{
				mob.tell("Withdraw how much?");
				return false;
			}
			commands.removeElement(commands.lastElement());
		}

		String likeThis=Util.combine(commands,1);
		if(!(Util.s_int(likeThis)>0))
		{
			ExternalPlay.quickSay((MOB)banker,mob,"I am sorry, but bank policy does not allow us to store items for clans, only funds.",true,false);
			return false;
		}
		Item thisThang=((Banker)banker).findDepositInventory(mob.getClanID(),likeThis);
		if((thisThang==null)||(!Sense.canBeSeenBy(thisThang,mob)))
		{
			ExternalPlay.quickSay((MOB)banker,mob,"We don't seem to be carrying that.",true,false);
			return false;
		}

		Item old=thisThang;
		if(old instanceof Coins)
		{
			Item item=((Banker)banker).findDepositInventory(mob.getClanID(),""+Integer.MAX_VALUE);
			if((item!=null)&&(item instanceof Coins))
			{
				Coins coins=(Coins)item;
				coins.setNumberOfCoins(coins.numberOfCoins()-((Coins)old).numberOfCoins());
				coins.recoverEnvStats();
				((Banker)banker).delDepositInventory(mob.getClanID(),item);
				com.planet_ink.coffee_mud.utils.Money.giveMoney(mob,((MOB)banker),((Coins)old).numberOfCoins());
				if(coins.numberOfCoins()<=0)
				{
					ExternalPlay.quickSay((MOB)banker,mob,"I have closed your Clan's account. Thanks for your business.",true,false);
					return false;
				}
				else
				{
					((Banker)banker).addDepositInventory(mob.getClanID(),item);
					ExternalPlay.quickSay((MOB)banker,mob,"Ok, your Clan's new balance is "+getBalance((MOB)banker,mob.getClanID())+" gold coins.",true,false);
				}
			}
			else
			    ExternalPlay.quickSay((MOB)banker,mob,"But, your balance is "+((Coins)item).numberOfCoins()+" gold coins.",true,false);
		}
		return false;
	}

	public static boolean clanBalance(MOB mob, Vector commands)
	{
		if((mob.getClanID()==null)||(mob.getClanID().equalsIgnoreCase("")))
		{
			mob.tell("You aren't even a member of a clan.");
			return false;
		}
		if(!((mob.getClanRole()==Clan.POS_BOSS)||(mob.getClanRole()==Clan.POS_TREASURER)))
		{
			mob.tell("\n\rHuh?\n\r");
			return false;
		}
		Banker banker=null;
		Room R=mob.location();
		for(int i=0;i<R.numInhabitants();i++)
		{
			if(R.fetchInhabitant(i) instanceof Banker)
			{
				banker=(Banker)R.fetchInhabitant(i);
				break;
			}
		}
		if(banker==null)
		{
			mob.tell("There is no banker here to help you.");
			return false;
		}
		ExternalPlay.quickSay((MOB)banker,mob,"Your Clan's balance is "+getBalance((MOB)banker,mob.getClanID())+" gold coins.",true,false);
		return false;
	}

	protected static int getBalance(MOB banker, String clanName)
	{
		Banker reallyBanker=(Banker)banker;
		Item old=reallyBanker.findDepositInventory(clanName,""+Integer.MAX_VALUE);
		if((old!=null)&&(old instanceof Coins))
			return ((Coins)old).numberOfCoins();
		return 0;
	}

	public static boolean clanDonate(MOB mob, Vector commands)
	{
		String likeThis=Util.combine(commands,1);
		Item I=null;
		Room donateRoom=null;
		I=mob.fetchInventory(likeThis);
		if(I==null)
		{
			mob.tell("You don't seem to have "+likeThis+" to donate.");
			return false;
		}
		Clan C=null;
		C=Clans.getClan(mob.getClanID());
		if(C.getStatus()>Clan.STATUS_ACTIVE)
		{
			mob.tell("You cannot donate items.  Your clan does not have enough members to be considered active.");
			return false;
		}
		donateRoom=CMMap.getRoom(C.getDonation());
		if(donateRoom==null)
		{
			mob.tell("You clan does not have a donation room.  Ask your clan boss for more information.");
			return false;
		}
		mob.delInventory(I);
		donateRoom.addItemRefuse(I,Item.REFUSE_PLAYER_DROP);
		if(donateRoom!=null) donateRoom.recoverRoomStats();
		if(mob!=null)
		{
			mob.recoverCharStats();
			mob.recoverEnvStats();
			mob.recoverMaxState();
		}
		return false;
	}

	public static void clanCreate(MOB mob, Vector commands)
	{
		ClanMaster master=null;
		Room R=mob.location();
		for(int i=0;i<R.numInhabitants();i++)
		{
			if(R.fetchInhabitant(i) instanceof ClanMaster)
			{
				master=(ClanMaster)R.fetchInhabitant(i);
				break;
			}
		}
		if(master==null)
		{
			mob.tell("There is no clan master here to help you.");
			return;
		}
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
				{
					FullMsg newMsg=new FullMsg(mob,master,null,(Affect.MSK_HAGGLE|Affect.TYP_GENERAL),null);
					if(!mob.location().okAffect(mob,newMsg))
						return;
					mob.location().send(mob,newMsg);
					return;
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
		ExternalPlay.DBUpdateMOB(M);
	}

	public static void delClanHomeSpell(MOB mob)
	{
		mob.delAbility(mob.fetchAbility("Spell_ClanHome"));
		ExternalPlay.DBUpdateMOB(mob);
	}
}
