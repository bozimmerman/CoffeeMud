package com.planet_ink.coffee_mud.MOBS;
import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class StdClanMaster extends StdMOB implements ClanMaster
{
	public String ID(){return "StdClanMaster";}
	protected static final Integer realLifeDay=new Integer(new Long(Area.A_FULL_DAY*Host.TIME_TICK_DELAY*9).intValue());
	protected static Long nextHousekeeping=null;
	protected static Integer DaysClanDeath=new Integer(0);
	protected static Integer MinClanMembers=new Integer(1);
	private int typeIHandle=1;
	protected static final Integer CLAN_KILL=new Integer(1);
	protected static final Integer CLAN_FADE=new Integer(2);
	protected static final Integer CLAN_ACTIVATE=new Integer(3);

	public StdClanMaster()
	{
		super();
		Username="a clan master";
		setDescription("He\\`s pleased to be of assistance.");
		setDisplayText("A clan master is waiting to serve you.");
		setTypeIHandle(Clan.TYPE_CLAN);
		setDaysClanDeath(new Integer(CommonStrings.getIntVar(CommonStrings.SYSTEMI_DAYSCLANDEATH)));
		setMinClanMembers(new Integer(CommonStrings.getIntVar(CommonStrings.SYSTEMI_MINCLANMEMBERS)));
		setAlignment(1000);
		setMoney(0);
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

	public void setTypeIHandle(int ClanType) { typeIHandle=ClanType; }
	public int getTypeIHandle() { return typeIHandle; }

	public void setDaysClanDeath(Integer daysClanDeath) { DaysClanDeath=daysClanDeath; }
	public int getDaysClanDeath() { return DaysClanDeath.intValue(); }

	public void setMinClanMembers(Integer minMembers) { MinClanMembers=minMembers; }
	public int getMinClanMembers() { return MinClanMembers.intValue(); }

	public Environmental newInstance()
	{
		return new StdClanMaster();
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		/*try{
			if(tickID==Host.MOB_TICK)
			{
				boolean proceed=false;
				// handle interest by watching the days go by...
				// put stuff up for sale if the account runs out
				synchronized(bankChain())
				{
					if((nextHousekeeping==null)||(nextHousekeeping.longValue()<System.currentTimeMillis()))
					{
						nextHousekeeping=new Long(System.currentTimeMillis()+realLifeDay.intValue());
						proceed=true;
					}
					if(proceed)
					{
						Vector clansChange=new Vector("");
						for(int i=0;i<Clans.size();i++)
						{
							Clan C=(Clan)Clans.elementAt(i);
							Vector members=new Vector();
							Vector lastDates=new Vector();
							Vector V;
							int activeMembers=0;
							ExternalPlay.DBClanFill(C.getName(),members,new Vector(),lastDates);
							for(int j=0;j<members.size(),j++)
							{
								if(System.currentTimeMillis()-(((Long)lastDates.elementAt(j)).longvalue())<(getDaysClanDeath()*realLifeDay))
								{
									activeMembers+=1;
								}
							}
							if(activeMembers<getMinClanMembers())
							{
								if(C.getStatus()==Clan.STATUS_FADING)
								{
									V=new Vector();
									V.addElement(C);
									V.addElement(CLAN_KILL)
									clansChange.addElement(V);
								}
								else
								{
									V=new Vector();
									V.addElement(C);
									V.addElement(CLAN_FADE)
									clansChange.addElement(V);
								}
							}	
							else
							{
								switch(C.getStatus)
								{
									case Clan.STATUS_FADING:
									case Clan.STATUS_PENDING:
									{
										V=new Vector();
										V.addElement(C);
										V.addElement(CLAN_ACTIVATE)
										clansChange.addElement(V);
										break;
									}
									default:
										continue;
								}
							}
						}
						updateClans(clansChange,this);
					}
				}
			}
		}catch(Exception e){Log.errOut("StdBanker",e);}*/
		return true;
	}

	public boolean okAffect(Environmental myHost, Affect affect)
	{
		MOB mob=affect.source();
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
				case Affect.TYP_LIST:
				{
					StringBuffer head=new StringBuffer("I have the following Clans waiting to be formed:\n\r");
					head.append("[");
					head.append(Util.padRight("Clan Name",24)+" | ");
					head.append(Util.padRight("Clan Members",12)+" | ");
					head.append(Util.padRight("Alignment",16));
					head.append("] \n\r");
					StringBuffer msg=new StringBuffer("");
					for(int c=0;c<Clans.size();c++)
					{
						Clan thisClan=Clans.elementAt(c);
						if(thisClan.getStatus()!=Clan.STATUS_PENDING)
							continue;
						msg.append(" ");
						msg.append(Util.padRight(thisClan.ID(),24)+"   ");
						msg.append(Util.padRight(new Integer(thisClan.getSize()).toString(),12)+"   ");
						msg.append(Util.padRight(CommonStrings.alignmentStr(thisClan.getAlign()),16));
						msg.append("\n\r");
					}
					ExternalPlay.quickSay(this,mob,msg.toString()+"^T",true,false);
					return false;
				}
				case Affect.TYP_GENERAL:
					return true;
				default:
					break;
			}
		}
		return super.okAffect(myHost,affect);
	}

	public void affect(Environmental myHost, Affect affect)
	{
		MOB mob=affect.source();
		if(affect.amITarget(this))
		{
			switch(affect.targetMinor())
			{
				case Affect.TYP_GENERAL:
				{
					ExternalPlay.quickSay(this,mob,"There is no Clan by that name.",true,false);
					try
					{
						String check=mob.session().prompt("Do you want to found a new clan (y/N)?","N");
						if(check.equalsIgnoreCase("Y"))
						{
							String doubleCheck=mob.session().prompt("Enter the name of your new Clan exactly how you want it:","");
							if(doubleCheck.length()<1)
								return;
							String lastCheck=mob.session().prompt("Is '"+doubleCheck+"' correct (y/N)?", "N");
							if(lastCheck.equalsIgnoreCase("Y"))
							{
								Clan newClan=Clans.getClanType(getTypeIHandle());
								newClan.setName(doubleCheck);
								newClan.setStatus(Clan.STATUS_PENDING);
								Clans.createClan(newClan);
								ExternalPlay.DBUpdateClan(mob.name(),newClan.getName(),newClan.getTopRank());
								com.planet_ink.coffee_mud.Commands.base.ClanCommands.addClanHomeSpell(mob);
								com.planet_ink.coffee_mud.Commands.base.ClanCommands.clanAnnounce(mob, "Your new clan is online and can now accept applicants.");
								
							}
							else
								return;
						}
						else
						{
							ExternalPlay.quickSay(this,mob,"Then quit wasting my time!",true,false);
							return;
						}
					}
					catch(java.io.IOException e)
					{
					}
				}
			}
		}
	}

	private static void updateClans(Vector clanList, ClanMaster master)
	{
		for(int i=0;i<clanList.size();i++)
		{
			Vector V=(Vector)clanList.elementAt(i);
			Clan C=(Clan)V.elementAt(0);
			int changeType=((Integer)V.elementAt(1)).intValue();
			switch(changeType)
			{
				case 1:
				{
					Clans.destroyClan(C);
					break;
				}
				case 2:
				{
					C.setStatus(Clan.STATUS_FADING);
					com.planet_ink.coffee_mud.Commands.base.ClanCommands.clanAnnounce(master, "Your Clan is in danger of being deleted if more members do not log on within 24 hours.");
					break;
				}
				case 3:
				{
					if(C.getStatus()==Clan.STATUS_PENDING)
					{
						com.planet_ink.coffee_mud.Commands.base.ClanCommands.clanAnnounce(master, "Your Clan now has sufficient members.  Your Clan is now fully approved.");
					}
					else
					{
						com.planet_ink.coffee_mud.Commands.base.ClanCommands.clanAnnounce(master, "Your Clan is no longer in danger of being deleted.  Be aware that there is required activity level.");
					}
					C.setStatus(Clan.STATUS_ACTIVE);
					break;
				}
			}
		}
	}

}