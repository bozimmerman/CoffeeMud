package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
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


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.lang.ref.WeakReference;

/*
   Copyright 2000-2012 Bo Zimmerman

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
public class CommonMsgs extends StdLibrary implements CommonCommands
{
	public String ID(){return "CommonMsgs";}

	protected final static int LOOK_LONG=0;
	protected final static int LOOK_NORMAL=1;
	protected final static int LOOK_BRIEFOK=2;

	// this needs to be global to work right
	protected final List<WeakReference<MsgMonitor>>
			globalMonitors = new SLinkedList<WeakReference<MsgMonitor>>();
	
	protected String unknownCommand(){return "Huh?";}
	protected String unknownInvoke(){return "You don't know how to @x1 that.";}
	protected Ability awarenessA=null;
	
	public boolean handleUnknownCommand(MOB mob, List<String> command)
	{
		if(mob==null) return false;
		Room R=mob.location();
		String msgStr=unknownCommand();
		if(R==null){ mob.tell(msgStr); return false;}
		if(command.size()>0)
		{
			String word=CMLib.english().getAnEvokeWord(mob,(String)command.get(0));
			if(word!=null)
				msgStr=CMStrings.replaceAll(unknownInvoke(),"@x1",word.toLowerCase());
		}
		CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_HUH,msgStr,CMParms.combine(command,0),null);
		if(!R.okMessage(mob,msg)) return false;
		R.send(mob,msg);
		return true;
	}

	public Object unforcedInternalCommand(MOB mob, String command, Object... parms)
	{
		try
		{
			Command C=CMClass.getCommand(command);
			if(C!=null)
				return C.executeInternal(mob,0,parms);
		}
		catch(IOException e)
		{
			Log.errOut("CommonMsgs",e);
		}
		return Boolean.FALSE;
	}
	
	public Object forceInternalCommand(MOB mob, String command, Object... parms)
	{
		try
		{
			Command C=CMClass.getCommand(command);
			if(C!=null)
				return C.executeInternal(mob,Command.METAFLAG_FORCED,parms);
		}
		catch(IOException e)
		{
			Log.errOut("CommonMsgs",e);
		}
		return Boolean.FALSE;
	}

	public boolean forceStandardCommand(MOB mob, String command, Vector<Object> parms)
	{
		try
		{
			Command C=CMClass.getCommand(command);
			if(C!=null)
				return C.execute(mob,parms,Command.METAFLAG_FORCED);
		}
		catch(IOException e)
		{
			Log.errOut("CommonMsgs",e);
		}
		return false;
	}

	public void monitorGlobalMessage(Room room, CMMsg msg)
	{
		MsgMonitor M;
		for(final Iterator<WeakReference<MsgMonitor>> w=globalMonitors.iterator();w.hasNext();)
		{
			final WeakReference<MsgMonitor> W=w.next();
			M=W.get();
			if(M != null)
				M.monitorMsg(room, msg);
			else
				globalMonitors.remove(M);
		}
	}
	
	public void addGlobalMonitor(MsgMonitor M)
	{
		if(M==null) 
			return;
		for(final Iterator<WeakReference<MsgMonitor>> w=globalMonitors.iterator();w.hasNext();)
		{
			final WeakReference<MsgMonitor> W=w.next();
			if(W.get()==M)
				return;
		}
		globalMonitors.add(new WeakReference<MsgMonitor>(M));
	}
	
	public void delGlobalMonitor(MsgMonitor M)
	{
		if(M==null) 
			return;
		for(final Iterator<WeakReference<MsgMonitor>> w=globalMonitors.iterator();w.hasNext();)
		{
			final WeakReference<MsgMonitor> W=w.next();
			if(W.get()==M)
				globalMonitors.remove(W);
		}
	}
	
	public StringBuilder getScore(MOB mob)
	{
		Vector<Object> V=new Vector<Object>();
		forceStandardCommand(mob,"Score",V);
		if((V.size()==1)&&(V.firstElement() instanceof StringBuilder))
			return (StringBuilder)V.firstElement();
		return new StringBuilder("");
	}
	public StringBuilder getEquipment(MOB viewer, MOB mob)
	{
		return (StringBuilder)forceInternalCommand(mob,"Equipment",viewer);
	}

	public StringBuilder getInventory(MOB viewer, MOB mob)
	{
		Vector<Object> V=new Vector<Object>();
		V.addElement(viewer);
		forceStandardCommand(mob,"Inventory",V);
		if((V.size()>1)&&(V.elementAt(1) instanceof StringBuilder))
			return (StringBuilder)V.elementAt(1);
		return new StringBuilder("");
	}

	public void postChannel(MOB mob,
							String channelName,
							String message,
							boolean systemMsg)
	{
		forceStandardCommand(mob,"Channel",
						  new XVector<Object>(Boolean.valueOf(systemMsg),channelName,message));
	}

	public void postChannel(String channelName,
							String clanID,
							String message,
							boolean systemMsg)
	{
		MOB talker=CMClass.getFactoryMOB();
		talker.setName("^?");
		talker.setLocation(CMLib.map().getRandomRoom());
		talker.basePhyStats().setDisposition(PhyStats.IS_GOLEM);
		talker.phyStats().setDisposition(PhyStats.IS_GOLEM);
		talker.setClanID(clanID);
		postChannel(talker,channelName,message,systemMsg);
		talker.destroy();
	}

	public boolean postDrop(MOB mob, Environmental dropThis, boolean quiet, boolean optimized)
	{
		return ((Boolean)forceInternalCommand(mob,"Drop",dropThis,Boolean.valueOf(quiet),Boolean.valueOf(optimized))).booleanValue();
	}
	public boolean postOpen(MOB mob, Environmental openThis, boolean quiet)
	{
		return ((Boolean)forceInternalCommand(mob,"Open",openThis,Boolean.valueOf(quiet))).booleanValue();
	}
	public boolean postGet(MOB mob, Item container, Item getThis, boolean quiet)
	{
		if(container==null)
			return forceStandardCommand(mob,"Get",new XVector<Object>(getThis,Boolean.valueOf(quiet)));
		return forceStandardCommand(mob,"Get",new XVector<Object>(getThis,container,Boolean.valueOf(quiet)));
	}

	public boolean postRemove(MOB mob, Item item, boolean quiet)
	{
		if(quiet)
			return forceStandardCommand(mob,"Remove",new XVector<Object>("REMOVE",item,"QUIETLY"));
		return forceStandardCommand(mob,"Remove",new XVector<Object>("REMOVE",item));
	}

	public void postLook(MOB mob, boolean quiet)
	{
		if(quiet)
			forceStandardCommand(mob,"Look",new XVector<Object>("LOOK","UNOBTRUSIVELY"));
		else
			forceStandardCommand(mob,"Look",new XVector<Object>("LOOK"));
	}

	public void postFlee(MOB mob, String whereTo)
	{
		forceStandardCommand(mob,"Flee",new XVector<Object>("FLEE",whereTo));
	}

	public void postSheath(MOB mob, boolean ifPossible)
	{
		if(ifPossible)
			forceStandardCommand(mob,"Sheath",new XVector<Object>("SHEATH","IFPOSSIBLE"));
		else
			forceStandardCommand(mob,"Sheath",new XVector<Object>("SHEATH"));
	}

	public void postDraw(MOB mob, boolean doHold, boolean ifNecessary)
	{
		if(ifNecessary)
		{
			if(doHold)
				forceStandardCommand(mob,"Draw",new XVector<Object>("DRAW","HELD","IFNECESSARY"));
			else
				forceStandardCommand(mob,"Draw",new XVector<Object>("DRAW","IFNECESSARY"));
		}
		else
			forceStandardCommand(mob,"Draw",new XVector<Object>("DRAW"));
	}

	public void postStand(MOB mob, boolean ifNecessary)
	{
		if(ifNecessary)
			forceStandardCommand(mob,"Stand",new XVector<Object>("STAND","IFNECESSARY"));
		else
			forceStandardCommand(mob,"Stand",new XVector<Object>("STAND"));
	}

	public void postSleep(MOB mob)
	{
		forceStandardCommand(mob,"Sleep",new XVector<Object>("SLEEP"));
	}

	public void postFollow(MOB follower, MOB leader, boolean quiet)
	{
		if(leader!=null)
		{
			if(quiet)
				forceStandardCommand(follower,"Follow",new XVector<Object>("FOLLOW",leader,"UNOBTRUSIVELY"));
			else
				forceStandardCommand(follower,"Follow",new XVector<Object>("FOLLOW",leader));
		}
		else
		{
			if(quiet)
				forceStandardCommand(follower,"Follow",new XVector<Object>("FOLLOW","SELF","UNOBTRUSIVELY"));
			else
				forceStandardCommand(follower,"Follow",new XVector<Object>("FOLLOW","SELF"));
		}
	}

	public void postSay(MOB mob, MOB target,String text){ postSay(mob,target,text,false,false);}
	public void postSay(MOB mob, String text){ postSay(mob,null,text,false,false);}
	public void postSay(MOB mob,
						MOB target,
						String text,
						boolean isPrivate,
						boolean tellFlag)
	{
		Room location=mob.location();
		text=CMProps.applyINIFilter(text,CMProps.SYSTEM_SAYFILTER);
		if(target!=null)
			location=target.location();
		if(location==null) return;
		if((isPrivate)&&(target!=null))
		{
			if(tellFlag)
			{
				String targetName=target.name();
				if(targetName.indexOf('@')>=0)
				{
					String mudName=targetName.substring(targetName.indexOf('@')+1);
					targetName=targetName.substring(0,targetName.indexOf('@'));
					if((!(CMLib.intermud().i3online()))&&(!(CMLib.intermud().imc2online())))
						mob.tell("Intermud is unavailable.");
					else
						CMLib.intermud().i3tell(mob,targetName,mudName,text);
				}
				else
				{
					boolean ignore=((target.playerStats()!=null)&&(target.playerStats().getIgnored().contains(mob.Name())));
					CMMsg msg=null;
					if((!CMLib.flags().isSeen(mob))||(!CMLib.flags().isSeen(target)))
						msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_TELL,"^t^<TELL \""+CMStrings.removeColors(target.name())+"\"^>You tell <T-NAME> '"+text+"'^</TELL^>^?^.",CMMsg.MSG_TELL,"^t^<TELL \""+CMStrings.removeColors(mob.name())+"\"^><S-NAME> tell(s) you '"+text+"'^</TELL^>^?^.",CMMsg.NO_EFFECT,null);
					else
						msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_TELL,"^t^<TELL \""+CMStrings.removeColors(target.name())+"\"^>You tell "+target.name()+" '"+text+"'^</TELL^>^?^.",CMMsg.MSG_TELL,"^t^<TELL \""+CMStrings.removeColors(mob.name())+"\"^>"+mob.Name()+" tell(s) you '"+text+"'^</TELL^>^?^.",CMMsg.NO_EFFECT,null);
					if((mob.location().okMessage(mob,msg))
					&&((ignore)||(target.okMessage(target,msg))))
					{
						mob.executeMsg(mob,msg);
						if((mob!=target)&&(!ignore))
						{
							target.executeMsg(target,msg);
							if(msg.trailerMsgs()!=null)
							{
								for(CMMsg msg2 : msg.trailerMsgs())
									if((msg!=msg2)&&(target.okMessage(target,msg2)))
										target.executeMsg(target,msg2);
								msg.trailerMsgs().clear();
							}
							if((!mob.isMonster())&&(!target.isMonster()))
							{
								if(mob.playerStats()!=null)
								{
									mob.playerStats().setReplyTo(target,PlayerStats.REPLY_TELL);
									mob.playerStats().addTellStack(CMLib.coffeeFilter().fullOutFilter(mob.session(),mob,mob,target,null,CMStrings.removeColors(msg.sourceMessage()),false));
								}
								if(target.playerStats()!=null)
								{
									target.playerStats().setReplyTo(mob,PlayerStats.REPLY_TELL);
									String str=msg.targetMessage();
									if((msg.tool() instanceof Ability)
									&&((((Ability)msg.tool()).classificationCode() & Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE)
									&&(target.fetchEffect(msg.tool().ID()) != null))
										str=CMStrings.substituteSayInMessage(str,CMStrings.getSayFromMessage(msg.sourceMessage()));
									target.playerStats().addTellStack(CMLib.coffeeFilter().fullOutFilter(target.session(),target,mob,target,null,CMStrings.removeColors(str),false));
								}
							}
						}
					}
				}
			}
			else
			{
				CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_SPEAK,"^T^<SAY \""+CMStrings.removeColors(target.name())+"\"^><S-NAME> say(s) '"+text+"' to <T-NAMESELF>.^</SAY^>^?",CMMsg.MSG_SPEAK,"^T^<SAY \""+CMStrings.removeColors(mob.name())+"\"^><S-NAME> say(s) '"+text+"' to <T-NAMESELF>.^</SAY^>^?",CMMsg.NO_EFFECT,null);
				if(location.okMessage(mob,msg))
					location.send(mob,msg);
			}
		}
		else
		if(!isPrivate)
		{
			String str="<S-NAME> say(s) '"+text+"'"+((target==null)?"^</SAY^>":" to <T-NAMESELF>.^</SAY^>^?");
			CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_SPEAK,"^T^<SAY \""+CMStrings.removeColors((target==null)?mob.name():target.name())+"\"^>"+str,"^T^<SAY \""+CMStrings.removeColors(mob.name())+"\"^>"+str,"^T^<SAY \""+CMStrings.removeColors(mob.name())+"\"^>"+str);
			if(location.okMessage(mob,msg))
				location.send(mob,msg);
		}
	}

	public void handleBeingSniffed(CMMsg msg)
	{
		if(msg.target() instanceof Room)
			handleBeingRoomSniffed(msg);
		else
		if(msg.target() instanceof Item)
			handleBeingItemSniffed(msg);
		else
		if(msg.target() instanceof MOB)
			handleBeingMobSniffed(msg);
	}

	public void handleBeingMobSniffed(CMMsg msg)
	{
		if(!(msg.target() instanceof MOB)) return;
		MOB sniffingmob=msg.source();
		MOB sniffedmob=(MOB)msg.target();
		if((sniffedmob.playerStats()!=null)
		&&(sniffedmob.soulMate()==null)
		&&(sniffedmob.playerStats().getHygiene()>=PlayerStats.HYGIENE_DELIMIT))
		{
			int x=(int)(sniffedmob.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT);
			if(x<=1)
				sniffingmob.tell(sniffedmob.displayName(sniffingmob)+" has a slight aroma about "+sniffedmob.charStats().himher()+".");
			else
			if(x<=3)
				sniffingmob.tell(sniffedmob.displayName(sniffingmob)+" smells pretty sweaty.");
			else
			if(x<=7)
				sniffingmob.tell(sniffedmob.displayName(sniffingmob)+" stinks pretty bad.");
			else
			if(x<15)
				sniffingmob.tell(sniffedmob.displayName(sniffingmob)+" smells most foul.");
			else
				sniffingmob.tell(sniffedmob.displayName(sniffingmob)+" reeks of noxious odors.");
		}
	}
	
	public void handleObserveComesToLife(MOB observer, MOB lifer, CMMsg msg)
	{
		
	}

	public void handleComeToLife(MOB mob, CMMsg msg)
	{
		
	}

	public void handleSit(CMMsg msg)
	{
		MOB sittingmob=msg.source();
		int oldDisposition=sittingmob.basePhyStats().disposition();
		oldDisposition=oldDisposition&(~(PhyStats.IS_SLEEPING|PhyStats.IS_SNEAKING|PhyStats.IS_SITTING));
		sittingmob.basePhyStats().setDisposition(oldDisposition|PhyStats.IS_SITTING);
		sittingmob.recoverPhyStats();
		sittingmob.recoverCharStats();
		sittingmob.recoverMaxState();
		sittingmob.tell(sittingmob,msg.target(),msg.tool(),msg.sourceMessage());
	}
	public void handleSleep(CMMsg msg)
	{
		MOB sleepingmob=msg.source();
		int oldDisposition=sleepingmob.basePhyStats().disposition();
		oldDisposition=oldDisposition&(~(PhyStats.IS_SLEEPING|PhyStats.IS_SNEAKING|PhyStats.IS_SITTING));
		sleepingmob.basePhyStats().setDisposition(oldDisposition|PhyStats.IS_SLEEPING);
		sleepingmob.recoverPhyStats();
		sleepingmob.recoverCharStats();
		sleepingmob.recoverMaxState();
		sleepingmob.tell(sleepingmob,msg.target(),msg.tool(),msg.sourceMessage());
	}
	public void handleStand(CMMsg msg)
	{
		MOB standingmob=msg.source();
		int oldDisposition=standingmob.basePhyStats().disposition();
		oldDisposition=oldDisposition&(~(PhyStats.IS_SLEEPING|PhyStats.IS_SNEAKING|PhyStats.IS_SITTING));
		standingmob.basePhyStats().setDisposition(oldDisposition);
		standingmob.recoverPhyStats();
		standingmob.recoverCharStats();
		standingmob.recoverMaxState();
		standingmob.tell(standingmob,msg.target(),msg.tool(),msg.sourceMessage());
	}

	public void handleRecall(CMMsg msg)
	{
		MOB recallingmob=msg.source();
		if((msg.target()!=null)
		&&(msg.target() instanceof Room)
		&&(recallingmob.location() != msg.target()))
		{
			recallingmob.tell(msg.source(),null,msg.tool(),msg.targetMessage());
			
			recallingmob.location().delInhabitant(recallingmob);
			((Room)msg.target()).addInhabitant(recallingmob);
			((Room)msg.target()).showOthers(recallingmob,null,CMMsg.MSG_ENTER,"<S-NAME> appears out of the Java Plane.");

			recallingmob.setLocation(((Room)msg.target()));
			if((recallingmob.riding()!=null)
			&&(recallingmob.location()!=CMLib.map().roomLocation(recallingmob.riding())))
			{
				if(recallingmob.riding().mobileRideBasis())
				{
					if(recallingmob.riding() instanceof Item)
						recallingmob.location().moveItemTo((Item)recallingmob.riding(),ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
					else
					if(recallingmob.riding() instanceof MOB)
						recallingmob.location().bringMobHere((MOB)recallingmob.riding(),true);
				}
				else
					recallingmob.setRiding(null);
			}
			recallingmob.recoverPhyStats();
			recallingmob.recoverCharStats();
			recallingmob.recoverMaxState();
			postLook(recallingmob,true);
		}
	}

	public int tickManaConsumption(MOB mob, int manaConsumeCounter)
	{
		if((CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMETIME)>0)
		&&(CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMEAMT)>0)
		&&((--manaConsumeCounter)<=0))
		{
			Vector<Ability> expenseAffects=new Vector<Ability>();
			manaConsumeCounter=CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMETIME);
			for(final Enumeration<Ability> a=mob.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if(A!=null)
				{
					if((!A.isAutoInvoked())
					&&(A.canBeUninvoked())
					&&(A.displayText().length()>0)
					&&(((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SPELL)
						||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_CHANT)
						||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_SONG)
						||((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_PRAYER)))
					{
						final Ability myA=mob.fetchAbility(A.ID());
						if(((myA!=null)&&(myA.usageCost(mob,false)[0]>0))||(A.usageCost(mob, false)[0]>0))
							expenseAffects.addElement(A);
					}
				}
			}
			if(expenseAffects.size()>0)
			{
				int basePrice=1;
				switch(CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMEAMT))
				{
				case -100: basePrice=basePrice*mob.phyStats().level(); break;
				case -200:
					{
						int total=0;
						for(int a1=0;a1<expenseAffects.size();a1++)
						{
							int lql=CMLib.ableMapper().lowestQualifyingLevel(((Ability)expenseAffects.elementAt(a1)).ID());
							if(lql>0)
								total+=lql;
							else
								total+=1;
						}
						basePrice=basePrice*(total/expenseAffects.size());
					}
					break;
				default:
					basePrice=basePrice*CMProps.getIntVar(CMProps.SYSTEMI_MANACONSUMEAMT);
					break;
				}

				// 1 per tick per level per msg.  +1 to the affects so that way it's about
				// 3 cost = 1 regen... :)
				int reallyEat=basePrice*(expenseAffects.size()+1);
				while(mob.curState().getMana()<reallyEat)
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-YOUPOSS> strength of will begins to crumble.");
					//pick one and kill it
					Ability A=(Ability)expenseAffects.elementAt(CMLib.dice().roll(1,expenseAffects.size(),-1));
					A.unInvoke();
					expenseAffects.remove(A);
					reallyEat=basePrice*expenseAffects.size();
				}
				if(reallyEat>0)
					mob.curState().adjMana( -reallyEat, mob.maxState());
			}
		}
		return manaConsumeCounter;
	}

	public void tickAging(MOB mob, long millisSinceLast)
	{
		if(mob==null) return;
		mob.setAgeMinutes(mob.getAgeMinutes()+(millisSinceLast / 60000)); // this is really minutes
		final PlayerStats stats = mob.playerStats();
		if(stats==null) return;
		final int[] birthDay = stats.getBirthday();
		if((mob.baseCharStats().getStat(CharStats.STAT_AGE)>0)
		&&(birthDay!=null)
		&&((mob.getAgeMinutes()%20)==0))
		{
			final TimeClock clock = CMLib.time().globalClock();
			final int currYear=clock.getYear();
			final int month=clock.getMonth();
			final int day=clock.getDayOfMonth();
			final int bday=birthDay[0];
			final int bmonth=birthDay[1];
			while((currYear>birthDay[3])
			||((currYear==birthDay[3])&&((month>bmonth)||((month==bmonth)&&(day>=bday)))))
			{
				if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.IMMORT))
				{
					if((month==bmonth)&&(day==bday))
						mob.tell("Happy Birthday!");
					mob.baseCharStats().setStat(CharStats.STAT_AGE,mob.baseCharStats().getStat(CharStats.STAT_AGE)+1);
					mob.recoverCharStats();
					mob.recoverPhyStats();
					mob.recoverMaxState();
				}
				else
				{
					birthDay[2]++;
				}
				birthDay[3]++;
			}
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.IMMORT))
			{
				if((mob.baseCharStats().ageCategory()>=Race.AGE_VENERABLE)
				&&(CMLib.dice().rollPercentage()==1)
				&&(CMLib.dice().rollPercentage()==1)
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					Ability A=CMClass.getAbility("Disease_Cancer");
					if((A!=null)&&(mob.fetchEffect(A.ID())==null))
						A.invoke(mob,mob,true,0);
				}
				else
				if((mob.baseCharStats().ageCategory()>=Race.AGE_ANCIENT)
				&&(CMLib.dice().rollPercentage()==1)
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					Ability A=CMClass.getAbility("Disease_Arthritis");
					if((A!=null)&&(mob.fetchEffect(A.ID())==null))
						A.invoke(mob,mob,true,0);
				}
				else
				if((mob.baseCharStats().ageCategory()>=Race.AGE_ANCIENT)
				&&(CMLib.dice().rollPercentage()==1)
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					Ability A=CMClass.getAbility("Disease_Alzheimers");
					if((A!=null)&&(mob.fetchEffect(A.ID())==null))
						A.invoke(mob,mob,true,0);
				}
				else
				if(CMLib.dice().rollPercentage()<10)
				{
					int max=CMProps.getIntVar(CMProps.SYSTEMI_BASEMAXSTAT);
					for(int i: CharStats.CODES.MAX())
						if((max+mob.charStats().getStat(i))<=0)
						{
							mob.tell("Your max "+CharStats.CODES.DESC(CharStats.CODES.toMAXBASE(i)).toLowerCase()+" has fallen below 1!");
							CMLib.combat().postDeath(null,mob,null);
							break;
						}
				}
			}
		}
	}

	protected String relativeCharStatTest(CharStats C,
										  MOB mob,
										  String weakword,
										  String strongword,
										  int stat)
	{
		double d=CMath.div(C.getStat(stat),mob.charStats().getStat(stat));
		String prepend="";
		if((d<=0.5)||(d>=3.0)) prepend="much ";
		if(d>=1.6) return mob.charStats().HeShe()+" appears "+prepend+weakword+" than the average "+mob.charStats().raceName()+".\n\r";
		if(d<=0.67) return mob.charStats().HeShe()+" appears "+prepend+strongword+" than the average "+mob.charStats().raceName()+".\n\r";
		return "";
	}

	public void handleBeingLookedAt(CMMsg msg)
	{
		if(msg.target() instanceof Room)
			handleBeingRoomLookedAt(msg);
		else
		if(msg.target() instanceof Item)
			handleBeingItemLookedAt(msg);
		else
		if(msg.target() instanceof MOB)
			handleBeingMobLookedAt(msg);
		else
		if(msg.target() instanceof Exit)
			handleBeingExitLookedAt(msg);
	}

	public String examineItemString(MOB mob, Item item)
	{
		StringBuilder response=new StringBuilder("");
		String level=null;
		if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
		{
			int l=(int)Math.round(Math.floor(CMath.div(item.phyStats().level(),10.0)));
			level=(l*10)+"-"+((l*10)+9);
		}
		else
		if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<18))
		{
			int l=(int)Math.round(Math.floor(CMath.div(item.phyStats().level(),5.0)));
			level=(l*5)+"-"+((l*5)+4);
		}
		else
			level=""+item.phyStats().level();
		double divider=100.0;
		if(item.phyStats().weight()<10)
			divider=4.0;
		else
		if(item.phyStats().weight()<50)
			divider=10.0;
		else
		if(item.phyStats().weight()<150)
			divider=20.0;
		String weight=null;
		if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
		{
			double l=Math.floor(CMath.div(item.phyStats().level(),divider));
			weight=(int)Math.round(CMath.mul(l,divider))+"-"+(int)Math.round(CMath.mul(l,divider)+(divider-1.0));
		}
		else
		if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<18))
		{
			divider=divider/2.0;
			double l=Math.floor(CMath.div(item.phyStats().level(),divider));
			weight=(int)Math.round(CMath.mul(l,divider))+"-"+(int)Math.round(CMath.mul(l,divider)+(divider-1.0));
		}
		else
			weight=""+item.phyStats().weight();
		if(item instanceof CagedAnimal)
		{
			MOB M=((CagedAnimal)item).unCageMe();
			if(M==null)
				response.append("\n\rLooks like some sort of lifeless thing.\n\r");
			else
			{
				if(M.phyStats().height()>0)
					response.append("\n\r"+CMStrings.capitalizeFirstLetter(item.name())+" is "+M.phyStats().height()+" inches tall and weighs "+weight+" pounds.\n\r");
				if((mob==null)||(!mob.isMonster()))
					response.append(CMProps.mxpImage(M," ALIGN=RIGHT H=70 W=70"));
				response.append(M.healthText(mob)+"\n\r\n\r");
				if(!M.description().equalsIgnoreCase(item.description()))
					response.append(M.description()+"\n\r\n\r");
			}
		}
		else
		{
			response.append("\n\r"+CMStrings.capitalizeFirstLetter(item.name())+" is a level "+level+" item, and weighs "+weight+" pounds.  ");
			if((item instanceof RawMaterial)
			&&(!CMLib.flags().isABonusItems(item))
			&&(item.rawSecretIdentity().length()>0)
			&&(item.basePhyStats().weight()>1)
			&&((mob==null)||(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>3)))
				response.append("It appears to be a bundle of `"+item.rawSecretIdentity()+"`.  ");

			if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
				response.append("It is mostly made of a kind of "+RawMaterial.MATERIAL_NOUNDESCS[(item.material()&RawMaterial.MATERIAL_MASK)>>8].toLowerCase()+".  ");
			else
				response.append("It is mostly made of "+RawMaterial.CODES.NAME(item.material()).toLowerCase()+".  ");
			if((item instanceof Recipe)&&((Recipe)item).getTotalRecipePages()>1)
				response.append( "There are "+((Recipe)item).getTotalRecipePages()+" blank pages/entries remaining.  " );
			if(item instanceof Ammunition)
				response.append("It is "+((Ammunition)item).usesRemaining()+" ammunition of type '"+((Ammunition)item).ammunitionType()+"'.  ");
			else
			if(item instanceof Weapon) 
			{
				if((mob==null)||mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10)
				{
					response.append("It is a ");
					if((item.rawLogicalAnd())&&CMath.bset(item.rawProperLocationBitmap(),Wearable.WORN_WIELD|Wearable.WORN_HELD))
						response.append("two handed ");
					else
						response.append("one handed ");
					response.append(CMStrings.capitalizeAndLower(Weapon.CLASS_DESCS[((Weapon)item).weaponClassification()])+" class weapon that does "+CMStrings.capitalizeAndLower(Weapon.TYPE_DESCS[((Weapon)item).weaponType()])+" damage.  ");
				}
				if(((Weapon)item).requiresAmmunition())
					response.append("It requires ammunition of type '"+((Weapon)item).ammunitionType()+"'.  ");
			}
			else
			if((item instanceof Armor)&&((mob==null)||mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10))
			{
				if(item.phyStats().height()>0)
					response.append(" It is a size "+item.phyStats().height()+", and is ");
				else
					response.append(" It is your size, and is ");
				response.append(((item.rawProperLocationBitmap()==Wearable.WORN_HELD)||(item.rawProperLocationBitmap()==(Wearable.WORN_HELD|Wearable.WORN_WIELD)))
									 ?new StringBuilder("")
									 :new StringBuilder("worn on the "));
				Wearable.CODES codes = Wearable.CODES.instance();
				for(long wornCode : codes.all())
					if((wornCode != Wearable.IN_INVENTORY)
					&&(CMath.bset(item.rawProperLocationBitmap(),wornCode)))
					{
						String wornString=codes.name(wornCode);
						if(wornString.length()>0)
						 {
							response.append(CMStrings.capitalizeAndLower(wornString)+" ");
							if(item.rawLogicalAnd())
								response.append("and ");
							else
								response.append("or ");
						}
					}
				if(response.toString().endsWith(" and "))
					response.delete(response.length()-5,response.length());
				else
				if(response.toString().endsWith(" or "))
					response.delete(response.length()-4,response.length());
				response.append(".  ");
			}
		}
		return response.toString();
	}

	protected String dispossessionTimeLeftString(Item item)
	{
		if(item.expirationDate()==0)
			return "N/A";
		return ""+(item.expirationDate()-System.currentTimeMillis());
	}


	protected void handleBeingItemLookedAt(CMMsg msg)
	{
		MOB mob=msg.source();
		Item item=(Item)msg.target();
		if(!CMLib.flags().canBeSeenBy(item,mob))
		{
			mob.tell("You can't see that!");
			return;
		}

		StringBuilder buf=new StringBuilder("");
		if(CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
		{
			String dispTime=dispossessionTimeLeftString(item);
			if(CMath.isInteger(dispTime))
				dispTime=CMLib.time().date2EllapsedTime(CMath.s_long(dispTime), TimeUnit.MINUTES, false);
			buf.append(item.ID()+"\n\rRejuv : "+item.basePhyStats().rejuv()
							+"\n\rType  : "+item.ID()
							+"\n\rUses  : "+item.usesRemaining()
							+"\n\rHeight: "+item.basePhyStats().height()
							+"\n\rAbilty: "+item.basePhyStats().ability()
							+"\n\rLevel : "+item.basePhyStats().level()
							+"\n\rTime  : "+dispTime
							+((item instanceof Container)?("\n\rCapac.: "+((Container)item).capacity()):"")
							+"\n\rMisc  : "+item.text().length()+"\n\r"+item.text());
		}
		if(item.description().length()==0)
			buf.append("You don't see anything special about "+item.name());
		else
			buf.append(item.description());
		if((msg.targetMinor()==CMMsg.TYP_EXAMINE)&&(!item.ID().endsWith("Wallpaper")))
			buf.append(examineItemString(mob,item));
		if(item instanceof Container)
		{
			buf.append("\n\r");
			Container contitem=(Container)item;
			if((contitem.isOpen())
			&&((contitem.capacity()>0)
				||(contitem.getContents().size()>0)
				||((contitem instanceof Drink)&&(((Drink)contitem).liquidRemaining()>0))))
			{
				buf.append(item.name()+" contains:^<!ENTITY container \""+CMStrings.removeColors(item.name())+"\"^>"+(CMath.bset(mob.getBitmap(),MOB.ATT_COMPRESS)?" ":"\n\r"));
				Vector<Item> newItems=new Vector<Item>();
				if((item instanceof Drink)&&(((Drink)item).liquidRemaining()>0))
				{
					RawMaterial l=(RawMaterial)CMClass.getItem("GenLiquidResource");
					int myResource=((Drink)item).liquidType();
					l.setMaterial(myResource);
					((Drink)l).setLiquidType(myResource);
					l.setBaseValue(RawMaterial.CODES.VALUE(myResource));
					l.basePhyStats().setWeight(1);
					String name=RawMaterial.CODES.NAME(myResource).toLowerCase();
					l.setName("some "+name);
					l.setDisplayText("some "+name+" sits here.");
					l.setDescription("");
					CMLib.materials().addEffectsToResource(l);
					l.recoverPhyStats();
					newItems.addElement(l);
				}

				if(item.owner() instanceof MOB)
				{
					MOB M=(MOB)item.owner();
					for(int i=0;i<M.numItems();i++)
					{
						Item item2=M.getItem(i);
						if((item2!=null)&&(item2.container()==item))
							newItems.addElement(item2);
					}
					buf.append(CMLib.lister().lister(mob,newItems,true,"CMItem","",false,CMath.bset(mob.getBitmap(),MOB.ATT_COMPRESS)));
				}
				else
				if(item.owner() instanceof Room)
				{
					Room room=(Room)item.owner();
					if(room!=null)
					for(int i=0;i<room.numItems();i++)
					{
						Item item2=room.getItem(i);
						if((item2!=null)&&(item2.container()==item))
							newItems.addElement(item2);
					}
					buf.append(CMLib.lister().lister(mob,newItems,true,"CRItem","",false,CMath.bset(mob.getBitmap(),MOB.ATT_COMPRESS)));
				}
			}
			else
			if((contitem.hasALid())&&((contitem.capacity()>0)||(contitem.getContents().size()>0)))
				buf.append(item.name()+" is closed.");
		}
		if(!msg.source().isMonster())
			buf.append(CMProps.mxpImage(item," ALIGN=RIGHT H=70 W=70"));
		mob.tell(buf.toString());
	}

	protected void handleBeingItemSniffed(CMMsg msg)
	{
		String s=null;
		Item item=(Item)msg.target();
		if(CMLib.flags().canSmell(msg.source()))
			s=RawMaterial.CODES.SMELL(item.material()).toLowerCase();
		if((s!=null)&&(s.length()>0))
			msg.source().tell(msg.source(),item,null,"<T-NAME> has a "+s+" smell.");
	}

	public void handleIntroductions(MOB speaker, MOB me, String msg)
	{
		if((me.playerStats()!=null)
		&&(speaker!=me)
		&&(speaker.playerStats()!=null)
		&&(msg!=null)
		&&(!me.playerStats().isIntroducedTo(speaker.Name()))
		&&(CMLib.english().containsString(CMStrings.getSayFromMessage(msg),speaker.Name())))
			me.playerStats().introduceTo(speaker.Name());
	}

	protected void handleBeingRoomSniffed(CMMsg msg)
	{
		Room room=(Room)msg.target();
		StringBuilder smell=new StringBuilder("");
		switch(room.domainType())
		{
		case Room.DOMAIN_INDOORS_UNDERWATER:
		case Room.DOMAIN_INDOORS_WATERSURFACE:
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			smell.append("It smells very WET here. ");
			break;
		case Room.DOMAIN_INDOORS_CAVE:
			smell.append("It smells very dank and mildewy here. ");
			break;
		case Room.DOMAIN_OUTDOORS_HILLS:
		case Room.DOMAIN_OUTDOORS_PLAINS:
			switch(room.getArea().getTimeObj().getSeasonCode())
			{
			case TimeClock.SEASON_FALL:
			case TimeClock.SEASON_WINTER:
				smell.append("There is a faint grassy smell here. ");
				break;
			case TimeClock.SEASON_SPRING:
			case TimeClock.SEASON_SUMMER:
				smell.append("There is a floral grassy smell here. ");
				break;
			}
			break;
		case Room.DOMAIN_OUTDOORS_WOODS:
			switch(room.getArea().getTimeObj().getSeasonCode())
			{
			case TimeClock.SEASON_FALL:
			case TimeClock.SEASON_WINTER:
				smell.append("There is a faint woodsy smell here. ");
				break;
			case TimeClock.SEASON_SPRING:
			case TimeClock.SEASON_SUMMER:
				smell.append("There is a rich woodsy smell here. ");
				break;
			}
			break;
		case Room.DOMAIN_OUTDOORS_JUNGLE:
			smell.append("There is a rich floral and plant aroma here. ");
			break;
		case Room.DOMAIN_OUTDOORS_MOUNTAINS:
		case Room.DOMAIN_OUTDOORS_ROCKS:
			switch(room.getArea().getTimeObj().getSeasonCode())
			{
			case TimeClock.SEASON_FALL:
			case TimeClock.SEASON_WINTER:
			case TimeClock.SEASON_SUMMER:
				smell.append("It smells musty and rocky here. ");
				break;
			case TimeClock.SEASON_SPRING:
				smell.append("It smells musty, rocky, and a bit grassy here. ");
				break;
			}
			break;
		case Room.DOMAIN_OUTDOORS_SWAMP:
			smell.append("It smells stinky and gassy here. ");
			break;
		}
		if(smell.length()>0)
			msg.source().tell(smell.toString());
	}

	protected void handleBeingRoomLookedAt(CMMsg msg)
	{
		MOB mob=msg.source();
		final Session sess = mob.session();
		if(sess==null) 
			return; // no need for monsters to build all this data

		Room room=(Room)msg.target();
		int lookCode=LOOK_LONG;
		if(msg.targetMinor()!=CMMsg.TYP_EXAMINE)
			lookCode=(msg.sourceMessage()==null)?LOOK_BRIEFOK:LOOK_NORMAL;

		StringBuilder Say=new StringBuilder("");
		boolean sysmsgs=CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS);
		final boolean compress=CMath.bset(mob.getBitmap(),MOB.ATT_COMPRESS);
		if(sysmsgs && (!CMSecurity.isAllowed(mob,room,CMSecurity.SecFlag.SYSMSGS)))
		{
			mob.setBitmap(CMath.unsetb(mob.getBitmap(),MOB.ATT_SYSOPMSGS));
			sysmsgs=false;
		}
		if(sysmsgs)
		{
			if(room.getArea()!=null)
				Say.append("^!Area  :^N "+room.getArea().Name()+"\n\r");
			final String rscName=room.myResource()>=0?RawMaterial.CODES.NAME(room.myResource()):"";
			final String domType;
			final StringBuilder domCond=new StringBuilder("");
			if((room.domainType()&Room.INDOORS)==0)
				domType=Room.outdoorDomainDescs[room.domainType()];
			else
				domType=Room.indoorDomainDescs[CMath.unsetb(room.domainType(),Room.INDOORS)];
			switch(room.domainConditions())
			{
			case Room.CONDITION_COLD: domCond.append(" cold"); break;
			case Room.CONDITION_HOT: domCond.append(" hot"); break;
			case Room.CONDITION_WET: domCond.append(" wet"); break;
			}
			Say.append("^!RoomID:^N "+CMLib.map().getExtendedRoomID(room)+"\n\r^!"+room.ID()+"^N: "+domType+" "+domCond.toString()+" <"+rscName+"> "+room.basePhyStats().weight()+"mv \n\r");
		}
		if(CMLib.flags().canBeSeenBy(room,mob))
		{
			Say.append("^O^<RName^>" + room.roomTitle(mob)+"^</RName^>"+CMLib.flags().colorCodes(room,mob)+"^L\n\r");
			if((lookCode!=LOOK_BRIEFOK)||(!CMath.bset(mob.getBitmap(),MOB.ATT_BRIEF)))
			{
				String roomDesc=room.roomDescription(mob);
				if(lookCode==LOOK_LONG)
				{
					Vector<String> keyWords=null;
					String word=null;
					int x=0;
					for(int c=0;c<room.numItems();c++)
					{
						Item item=room.getItem(c);
						if(item==null) continue;
						if((item.container()==null)
						&&(item.displayText().length()==0)
						&&(CMLib.flags().canBeSeenBy(item,mob)))
						{
							keyWords=CMParms.parse(item.name().toUpperCase());
							for(int k=0;k<keyWords.size();k++)
							{
								word=(String)keyWords.elementAt(k);
								x=roomDesc.toUpperCase().indexOf(word);
								while(x>=0)
								{
									if(((x<=0)
										||((!Character.isLetterOrDigit(roomDesc.charAt(x-1)))&&(roomDesc.charAt(x-1)!='>')))
									&&(((x+word.length())>=(roomDesc.length()-1))
										||((!Character.isLetterOrDigit(roomDesc.charAt((x+word.length()))))&&(roomDesc.charAt(x+word.length())!='^'))))
									{
										int brackCheck=roomDesc.substring(x).indexOf("^>");
										int brackCheck2=roomDesc.substring(x).indexOf("^<");
										if((brackCheck<0)||(brackCheck2<brackCheck))
										{
											int start=x;
											while((start>=0)&&(!Character.isWhitespace(roomDesc.charAt(start))))
												start--;
											start++;
											int end=(x+word.length());
											while((end<roomDesc.length())&&(!Character.isWhitespace(roomDesc.charAt(end))))
												end++;
											int l=roomDesc.length();
											roomDesc=roomDesc.substring(0,start)+"^H^<WItem \""+CMStrings.removeColors(item.name())+"\"^>"+roomDesc.substring(start,end)+"^</WItem^>^?"+roomDesc.substring(end);
											x=x+(roomDesc.length()-l);
										}
									}
									x=roomDesc.toUpperCase().indexOf(word,x+1);
								}
							}
						}
					}
				}
				if((CMProps.getIntVar(CMProps.SYSTEMI_EXVIEW)==1)||(CMProps.getIntVar(CMProps.SYSTEMI_EXVIEW)==2))
					roomDesc += getRoomExitsParagraph(mob,room);
				Say.append("^L^<RDesc^>" + roomDesc+"^</RDesc^>");
				
				if((!mob.isMonster())&&(sess.clientTelnetMode(Session.TELNET_MXP)))
					Say.append(CMProps.mxpImage(room," ALIGN=RIGHT H=70 W=70"));
				if(compress)
					Say.append("^N  ");
				else
					Say.append("^N\n\r\n\r");
			}
		}

		Vector<Item> viewItems=new Vector<Item>();
		int itemsInTheDarkness=0;
		for(int c=0;c<room.numItems();c++)
		{
			Item item=room.getItem(c);
			if(item==null) continue;

			if(item.container()==null)
			{
				if(CMLib.flags().canBarelyBeSeenBy(item,mob))
					itemsInTheDarkness++;
				viewItems.addElement(item);
			}
		}

		StringBuilder itemStr=CMLib.lister().lister(mob,viewItems,false,"RItem"," \"*\"",lookCode==LOOK_LONG,compress);
		if(itemStr.length()>0)
			Say.append(itemStr);

		int mobsInTheDarkness=0;
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB mob2=room.fetchInhabitant(i);
			if((mob2!=null)&&(mob2!=mob))
			{
				final String displayText=mob2.displayText(mob);
				if((displayText.length()>0)
				||(sysmsgs))
				{
					if(CMLib.flags().canBeSeenBy(mob2,mob))
					{
						if((!compress)&&(!mob.isMonster())&&(sess.clientTelnetMode(Session.TELNET_MXP)))
							Say.append(CMProps.mxpImage(mob2," H=10 W=10",""," "));
						Say.append("^M^<RMob \""+CMStrings.removeColors(mob2.name())+"\"^>");
						if(compress) Say.append(CMLib.flags().colorCodes(mob2,mob)+"^M ");
						if(displayText.length()>0)
							Say.append(CMStrings.endWithAPeriod(CMStrings.capitalizeFirstLetter(displayText)));
						else
							Say.append(CMStrings.endWithAPeriod(CMStrings.capitalizeFirstLetter(mob2.name())));
						Say.append("^</RMob^>");
						if(sysmsgs)
							Say.append("^H("+CMClass.classID(mob2)+")^N ");
						if(!compress)
							Say.append(CMLib.flags().colorCodes(mob2,mob)+"^N\n\r");
						else
							Say.append("^N");
					}
					else
					if(CMLib.flags().canBarelyBeSeenBy(mob2,mob))
						mobsInTheDarkness++;
				}
			}
		}

		if(Say.length()==0)
			mob.tell("You can't see anything!");
		else
		{
			if(compress) Say.append("\n\r");
			mob.tell(Say.toString());
			if((CMProps.getIntVar(CMProps.SYSTEMI_AWARERANGE)>0)
			&&(!CMath.bset(mob.getBitmap(), MOB.ATT_AUTOMAP)))
			{
				if(awarenessA==null)
					awarenessA=CMClass.getAbility("Skill_RegionalAwareness");
				if(awarenessA!=null) 
				{
					sess.colorOnlyPrintln("", true);
					final Vector<Object> list=new Vector<Object>();
					awarenessA.invoke(mob, list, mob.location(), true, CMProps.getIntVar(CMProps.SYSTEMI_AWARERANGE));
					for(Object o : list)
						sess.colorOnlyPrintln((String)o, true);
					sess.colorOnlyPrintln("\n\r", true);
				}
			}
			if(itemsInTheDarkness>0)
				mob.tell("      ^IThere is something here, but it's too dark to make out.^?\n\r");
			if(mobsInTheDarkness>1)
				mob.tell("^MThe darkness conceals several others.^?\n\r");
			else
			if(mobsInTheDarkness>0)
				mob.tell("^MYou are not alone, but it's too dark to tell.^?\n\r");
		}
	}

	private static String getRoomExitsParagraph(MOB mob, Room room)
	{
		if(room == null || mob == null)
			return "";
		StringBuilder str = new StringBuilder("");
		Vector<Integer> exitDirs=new Vector<Integer>();
		Room R;
		Exit E;
		for(int dir : Directions.DIRECTIONS_BASE())
		{
			E = room.getExitInDir(dir);
			R = room.getRoomInDir(dir);
			if((R!=null)&&(E!=null)&&(CMLib.flags().canBeSeenBy(E,mob)))
				exitDirs.add(Integer.valueOf(dir));
		}
		String title = room.displayText();
		// do continues first
		Vector<Integer> continues = new Vector<Integer>();
		for(int i=exitDirs.size()-1;i>=0;i--)
		{
			R = room.getRoomInDir(exitDirs.elementAt(i).intValue());
			if((R!=null)&&(R.displayText().equalsIgnoreCase(title)))
				continues.addElement(exitDirs.remove(i));
		}
		
		if(continues.size()>0)
		{
			str.append("  ^L"+CMStrings.capitalizeFirstLetter(room.roomTitle(mob)).trim()+" continues ");
			if(continues.size()==1)
				str.append(Directions.getInDirectionName(continues.firstElement().intValue())+".");
			else
			{
				for(int i=0;i<continues.size()-1;i++)
					str.append(Directions.getDirectionName(continues.elementAt(i).intValue()).toLowerCase().trim()+", ");
				str.append("and "+Directions.getInDirectionName(continues.lastElement().intValue()).trim()+".");
			}
		}
		boolean style=CMLib.dice().rollPercentage()>50;
		if(exitDirs.size()>0)
		{
			str.append("  ^L"+CMStrings.capitalizeFirstLetter(getExitFragment(mob,room,exitDirs.firstElement().intValue(),style)).trim());
			if(exitDirs.size()>1)
			{
				str.append("^L, ");
				for(int i=1;i<exitDirs.size()-1;i++)
					str.append(getExitFragment(mob,room,exitDirs.elementAt(i).intValue(),style).trim()+"^L, ");
				str.append("and "+getExitFragment(mob,room,exitDirs.lastElement().intValue(),style).trim());
			}
			str.append("^L.");
		}
		return str.toString();
	}

	private static String getExitFragment(MOB mob, Room room, int dir, boolean style)
	{
		if(room==null) return "";
		Exit exit = room.getExitInDir(dir);
		if(exit == null) return "";
		if(style)
			return Directions.getInDirectionName(dir)
				   + " is " +exit.viewableText(mob, room.getRoomInDir(dir));
		else
			return exit.viewableText(mob, room.getRoomInDir(dir)).toString().trim()
				   +" is " + Directions.getInDirectionName(dir);
	}
		
	public boolean isHygenicMessage(final CMMsg msg, final int minHygeine, final long adjHygiene)
	{
		if((msg.sourceMajor(CMMsg.MASK_MOVE)
		||((msg.tool() instanceof Social)
			&&((msg.tool().Name().toUpperCase().startsWith("BATHE"))
			||(msg.tool().Name().toUpperCase().startsWith("WASH"))))))
				return (msg.source().playerStats()!=null)&&(msg.source().soulMate()==null);
		return false;
	}
	
	public void handleHygenicMessage(final CMMsg msg, final int minHygeine, final long adjHygiene)
	{
		if(isHygenicMessage(msg,minHygeine,adjHygiene))
		{
			final MOB mob=msg.source();
			if(mob.playerStats().getHygiene()>adjHygiene)
			{
				mob.playerStats().adjHygiene(PlayerStats.HYGIENE_WATERCLEAN);
				mob.tell("You feel a little cleaner.");
			}
			else
			if(adjHygiene==0)
				mob.tell("You are already perfectly clean.");
			else
				mob.tell("You can't get any cleaner here.");
		}
	}
	
	private static boolean isAClearExitView(MOB mob, Room room, Exit exit)
	{
		if((room!=null)
		&&(exit!=null)
		&&(exit.isOpen())
		&&(CMLib.flags().canBeSeenBy(room,mob))
		&&(CMLib.flags().canBeSeenBy(exit,mob)))
		{
			int domain=room.domainType();
			switch(domain)
			{
			case Room.DOMAIN_INDOORS_AIR:
			case Room.DOMAIN_INDOORS_UNDERWATER:
			case Room.DOMAIN_OUTDOORS_AIR:
			case Room.DOMAIN_OUTDOORS_UNDERWATER:
			{
				int weather=room.getArea().getClimateObj().weatherType(room);
				if((weather!=Climate.WEATHER_BLIZZARD)&&(weather!=Climate.WEATHER_DUSTSTORM))
					return true;
				break;
			}
			}
		}
		return false;
	}

	protected void handleBeingExitLookedAt(CMMsg msg)
	{
		Exit exit=(Exit)msg.target();
		MOB mob=msg.source();
		if(CMLib.flags().canBeSeenBy(exit,mob))
		{
			if(exit.description().trim().length()>0)
				mob.tell(exit.description());
			else
			if(mob.location()!=null)
			{
				Room room=null;
				int direction=-1;
				if(msg.tool() instanceof Room)
					room=(Room)msg.tool();
				else
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					if(mob.location().getExitInDir(d)==exit)
					{
						room=mob.location().getRoomInDir(d);
						break;
					}
				if(room!=null)
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
					if((mob.location().getRoomInDir(d)==room)
					&&((mob.location().getExitInDir(d)==exit)))
						direction=d;
				mob.tell(exit.viewableText(mob,room).toString());
				if(isAClearExitView(mob,room,exit)&&(direction>=0)&&(room!=null))
				{
					List<Room> view=null;
					Vector<Environmental> items=new Vector<Environmental>();
					if(room.getGridParent()!=null)
						view=room.getGridParent().getAllRooms();
					else
					{
						view=new Vector<Room>();
						view.add(room);
						for(int i=0;i<5;i++)
						{
							room=room.getRoomInDir(direction);
							if(room==null) break;
							Exit E=room.getExitInDir(direction);
							if((isAClearExitView(mob,room,E)))
								view.add(room);
						}
					}
					for(int r=0;r<view.size();r++)
					{
						room=(Room)view.get(r);
						for(int i=0;i<room.numItems();i++)
						{
							Item E=room.getItem(i);
							if(E!=null) items.add(E);
						}
						for(int i=0;i<room.numInhabitants();i++)
						{
							MOB E=room.fetchInhabitant(i);
							if(E!=null) items.add(E);
						}
					}
					StringBuilder seenThatWay=CMLib.lister().lister(msg.source(),items,true,"","",false,true);
					if(seenThatWay.length()>0)
						mob.tell("Yonder, you can also see: "+seenThatWay.toString());
				}
			}
			else
				mob.tell("You don't see anything special.");
			if(CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS))
			{
				mob.tell("Type  : "+exit.ID());
				mob.tell("Misc   : "+exit.text());
			}
			String image=CMProps.mxpImage(exit," ALIGN=RIGHT H=70 W=70");
			if((image!=null)&&(image.length()>0)) mob.tell(image);
		}
		else
			mob.tell("You can't see that way!");
	}

	protected void handleBeingMobLookedAt(CMMsg msg)
	{
		MOB viewermob=msg.source();
		MOB viewedmob=(MOB)msg.target();
		boolean longlook=msg.targetMinor()==CMMsg.TYP_EXAMINE;
		StringBuilder myDescription=new StringBuilder("");
		if(CMLib.flags().canBeSeenBy(viewedmob,viewermob))
		{
			if(CMath.bset(viewermob.getBitmap(),MOB.ATT_SYSOPMSGS))
				myDescription.append("\n\rType :"+viewedmob.ID()
									+"\n\rRejuv:"+viewedmob.basePhyStats().rejuv()
									+"\n\rAbile:"+viewedmob.basePhyStats().ability()
									+"\n\rLevel:"+viewedmob.basePhyStats().level()
									+"\n\rDesc : "+viewedmob.description()
									+"\n\rRoom :'"+((viewedmob.getStartRoom()==null)?"null":viewedmob.getStartRoom().roomID())
									+"\n\rMisc : "+viewedmob.text()
									+"\n\r");
			if(!viewedmob.isMonster())
			{
				String levelStr=null;
				if((!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
				&&(!viewedmob.charStats().getMyRace().classless())
				&&(!viewedmob.charStats().getCurrentClass().leveless())
				&&(!viewedmob.charStats().getMyRace().leveless())
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS)))
					levelStr=CMLib.english().startWithAorAn(viewedmob.charStats().displayClassLevel(viewedmob,false));
				else
				if((!CMSecurity.isDisabled(CMSecurity.DisFlag.LEVELS))
				&&(!viewedmob.charStats().getCurrentClass().leveless())
				&&(!viewedmob.charStats().getMyRace().leveless()))
					levelStr="level "+viewedmob.charStats().displayClassLevelOnly(viewedmob);
				else
				if((!CMSecurity.isDisabled(CMSecurity.DisFlag.CLASSES))
				&&(!viewedmob.charStats().getMyRace().classless()))
					levelStr=CMLib.english().startWithAorAn(viewedmob.charStats().displayClassName());
				if((!CMSecurity.isDisabled(CMSecurity.DisFlag.RACES))
				&&(!viewedmob.charStats().getCurrentClass().raceless()))
				{
					myDescription.append(viewedmob.displayName(viewermob)+" the ");
					if(viewedmob.charStats().getStat(CharStats.STAT_AGE)>0)
						myDescription.append(viewedmob.charStats().ageName().toLowerCase()+" ");
					myDescription.append(viewedmob.charStats().raceName());
				}
				else
					myDescription.append(viewedmob.displayName(viewermob)+" ");
				if(levelStr!=null)
					myDescription.append(" is "+levelStr+".\n\r");
				else
					myDescription.append("is here.\n\r");
			}
			if(viewedmob.phyStats().height()>0)
				myDescription.append(viewedmob.charStats().HeShe()+" is "+viewedmob.phyStats().height()+" inches tall and weighs "+viewedmob.basePhyStats().weight()+" pounds.\n\r");
			if((longlook)&&(viewermob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>12))
			{
				CharStats C=(CharStats)CMClass.getCommon("DefaultCharStats");
				MOB testMOB=CMClass.getFactoryMOB();
				viewedmob.charStats().getMyRace().affectCharStats(testMOB,C);
				myDescription.append(relativeCharStatTest(C,viewedmob,"weaker","stronger",CharStats.STAT_STRENGTH));
				myDescription.append(relativeCharStatTest(C,viewedmob,"clumsier","more nimble",CharStats.STAT_DEXTERITY));
				myDescription.append(relativeCharStatTest(C,viewedmob,"more sickly","healthier",CharStats.STAT_CONSTITUTION));
				myDescription.append(relativeCharStatTest(C,viewedmob,"more repulsive","more attractive",CharStats.STAT_CHARISMA));
				myDescription.append(relativeCharStatTest(C,viewedmob,"more naive","wiser",CharStats.STAT_WISDOM));
				myDescription.append(relativeCharStatTest(C,viewedmob,"dumber","smarter",CharStats.STAT_INTELLIGENCE));
				testMOB.destroy();
			}
			if(!viewermob.isMonster())
				myDescription.append(CMProps.mxpImage(viewedmob," ALIGN=RIGHT H=70 W=70"));
			myDescription.append(viewedmob.healthText(viewermob)+"\n\r\n\r");
			myDescription.append(viewedmob.description()+"\n\r\n\r");

			StringBuilder eq=CMLib.commands().getEquipment(viewermob,viewedmob);
			if(eq.length() > 0)
			{
				if((CMProps.getIntVar(CMProps.SYSTEMI_EQVIEW)>1)
				||((viewermob!=viewedmob)&&(CMProps.getIntVar(CMProps.SYSTEMI_EQVIEW)>0)))
					myDescription.append(viewedmob.charStats().HeShe()+" is wearing "+eq.toString());
				else
					myDescription.append(viewedmob.charStats().HeShe()+" is wearing:\n\r"+eq.toString());
			}
			viewermob.tell(myDescription.toString());
			if(longlook)
			{
				Command C=CMClass.getCommand("Consider");
				try{if(C!=null)C.executeInternal(viewermob,0,viewedmob);}catch(java.io.IOException e){}
			}
		}
	}

	public void handleBeingGivenTo(CMMsg msg)
	{
		if(!(msg.target() instanceof MOB)) return;
		MOB givermob=msg.source();
		MOB giveemob=(MOB)msg.target();
		if(giveemob.location()!=null)
		{
			CMMsg msg2=CMClass.getMsg(givermob,msg.tool(),null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null);
			giveemob.location().send(givermob,msg2);
			msg2=CMClass.getMsg((MOB)msg.target(),msg.tool(),null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null);
			giveemob.location().send(giveemob,msg2);
		}
	}

	public void handleBeingRead(CMMsg msg)
	{
		if((msg.targetMessage()==null)||(!msg.targetMessage().equals("CANCEL")))
		{
			MOB mob=msg.source();
			if(CMLib.flags().canBeSeenBy(msg.target(),mob))
			{
				String text=null;
				if((msg.target() instanceof Exit)&&(((Exit)msg.target()).isReadable()))
					text=((Exit)msg.target()).readableText();
				else
				if((msg.target() instanceof Item)&&(((Item)msg.target()).isReadable()))
				{
					text=((Item)msg.target()).readableText();
					if(((text==null)||(text.length()==0))
					&&(msg.target().description().length()>0)
					&&((msg.target().displayText().length()==0)
					   ||(!CMLib.flags().isGettable((Item)msg.target()))))
						text=msg.target().description();
				}
				if((text!=null)
				&&(text.length()>0))
				{
					if(text.toUpperCase().startsWith("FILE="))
					{
						StringBuffer buf=Resources.getFileResource(text.substring(5),true);
						if((buf!=null)&&(buf.length()>0))
							mob.tell("It says '"+buf.toString()+"'.");
						else
							mob.tell("There is nothing written on "+msg.target().name()+".");
					}
					else
						mob.tell("It says '"+text+"'.");
				}
				else
					mob.tell("There is nothing written on "+msg.target().name()+".");
			}
			else
				mob.tell("You can't see that!");
		}
	}

	public void handleBeingGetted(CMMsg msg)
	{
		if(!(msg.target() instanceof Item)) return;
		Item item=(Item)msg.target();
		MOB mob=msg.source();
		if(item instanceof Container)
		{
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Item))
			{
				Item newitem=(Item)msg.tool();
				if(newitem.container()==item)
				{
					newitem.setContainer(null);
					newitem.unWear();
				}
			}
			else
			if(!mob.isMine(item))
			{
				item.setContainer(null);
				mob.moveItemTo(item);
				if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					mob.location().recoverRoomStats();
				else
					mob.phyStats().setWeight(mob.phyStats().weight()+item.recursiveWeight());
			}
			else
			{
				item.setContainer(null);
				item.unWear();
				if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					mob.location().recoverRoomStats();
			}
		}
		else
		{
			item.setContainer(null);
			if(CMLib.flags().isHidden(item))
				item.basePhyStats().setDisposition(item.basePhyStats().disposition()&((int)PhyStats.ALLMASK-PhyStats.IS_HIDDEN));
			if(mob.location().isContent(item))
				mob.location().delItem(item);
			if(!mob.isMine(item))
			{
				mob.addItem(item);
				if(CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
					mob.phyStats().setWeight(mob.phyStats().weight()+item.phyStats().weight());
			}
			item.unWear();
			if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
				mob.location().recoverRoomStats();
			if(item instanceof Coins)
				((Coins)item).putCoinsBack();
			if(item instanceof RawMaterial)
				((RawMaterial)item).rebundle();
		}
		if(CMLib.flags().isCataloged(item))
			CMLib.catalog().bumpDeathPickup(item);
	}
	public void handleBeingDropped(CMMsg msg)
	{
		if(!(msg.target() instanceof Item)) return;
		Item item=(Item)msg.target();
		MOB mob=msg.source();
		if(mob.isMine(item)&&(item instanceof Container))
		{
			item.setContainer(null);
			CMLib.utensils().recursiveDropMOB(mob,mob.location(),item,item instanceof DeadBody);
			if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
				mob.location().recoverRoomStats();
		}
		if(mob.isMine(item))
		{
			mob.delItem(item);
			if(!mob.location().isContent(item))
				mob.location().addItem(item,ItemPossessor.Expire.Player_Drop);
			if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
				mob.location().recoverRoomStats();
		}
		item.unWear();
		item.setContainer(null);
		if(!msg.targetMajor(CMMsg.MASK_INTERMSG))
		{
			if(item instanceof Coins)
				((Coins)item).putCoinsBack();
			if(item instanceof RawMaterial)
				((RawMaterial)item).rebundle();
		}
	}
	public void handleBeingRemoved(CMMsg msg)
	{
		if(!(msg.target() instanceof Item)) return;
		Item item=(Item)msg.target();
		MOB mob=msg.source();
		if(item instanceof Container)
		{
			handleBeingGetted(msg);
			return;
		}
		item.unWear();
		if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
			mob.location().recoverRoomStats();
	}
	public void handleBeingWorn(CMMsg msg)
	{
		if(!(msg.target() instanceof Item)) return;
		Item item=(Item)msg.target();
		long wearLocation = (long)((msg.value()<=0)?0:((long)(1<<msg.value())/2));
		MOB mob=msg.source();
		if(item.canWear(mob,wearLocation))
		{
			if(wearLocation<=0)
				item.wearIfPossible(mob);
			else
				item.wearIfPossible(mob,wearLocation);
			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.recoverMaxState();
		}
	}
	public void handleBeingWielded(CMMsg msg)
	{
		if(!(msg.target() instanceof Item)) return;
		Item item=(Item)msg.target();
		MOB mob=msg.source();
		if(item.wearIfPossible(mob,Wearable.WORN_WIELD))
		{
			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.recoverMaxState();
		}
	}
	public void handleBeingHeld(CMMsg msg)
	{
		if(!(msg.target() instanceof Item)) return;
		Item item=(Item)msg.target();
		MOB mob=msg.source();
		if(item.wearIfPossible(mob,Wearable.WORN_HELD))
		{
			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.recoverMaxState();
		}
	}

	public void lookAtExits(Room room, MOB mob)
	{
		if((mob==null)
		||(room==null)
		||(mob.isMonster())) 
			return;
		
		if(!CMLib.flags().canSee(mob))
		{
			mob.tell("You can't see anything!");
			return;
		}

		StringBuilder buf=new StringBuilder("^DObvious exits:^.^N\n\r");
		String Dir=null;
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Exit exit=room.getExitInDir(d);
			Room room2=room.getRoomInDir(d);
			StringBuilder Say=new StringBuilder("");
			if(exit!=null)
				Say=exit.viewableText(mob, room2);
			else
			if((room2!=null)&&(CMath.bset(mob.getBitmap(),MOB.ATT_SYSOPMSGS)))
				Say.append(room2.roomID()+" via NULL");
			if(Say.length()>0)
			{
				if(exit!=null) Say.append(CMLib.flags().colorCodes(exit, mob));
				Dir=CMStrings.padRightPreserve(Directions.getDirectionName(d),5);
				if((mob.playerStats()!=null)
				&&(room2!=null)
				&&(!mob.playerStats().hasVisited(room2)))
					buf.append("^U^<EX^>" + Dir+"^</EX^>:^.^N ^u"+Say+"^.^N\n\r");
				else
					buf.append("^D^<EX^>" + Dir+"^</EX^>:^.^N ^d"+Say+"^.^N\n\r");
			}
		}
		Item I=null;
		for(int i=0;i<room.numItems();i++)
		{
			I=room.getItem(i);
			if((I instanceof Exit)&&(((Exit)I).doorName().length()>0)&&(I.container()==null))
			{
				StringBuilder Say=((Exit)I).viewableText(mob, room);
				if(Say.length()>0)
				{
					Say.append(CMLib.flags().colorCodes(I, mob));
					if(Say.length()>5)
						buf.append("^D^<MEX^>" + ((Exit)I).doorName()+"^</MEX^>:^.^N ^d"+Say+"^.^N\n\r");
					else
						buf.append("^D^<MEX^>" + CMStrings.padRight(((Exit)I).doorName(),5)+"^</MEX^>:^.^N ^d"+Say+"^.^N\n\r");
				}
			}
		}
		mob.tell(buf.toString());
	}

	public void lookAtExitsShort(Room room, MOB mob)
	{
		if((mob==null)||(room==null)||(mob.isMonster())) return;
		if(!CMLib.flags().canSee(mob)) return;

		StringBuilder buf=new StringBuilder("^D[Exits: ");
		for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
		{
			Exit exit=room.getExitInDir(d);
			if((exit!=null)&&(exit.viewableText(mob, room.getRoomInDir(d)).length()>0))
				buf.append("^<EX^>"+Directions.getDirectionName(d)+"^</EX^> ");
		}
		Item I=null;
		for(int i=0;i<room.numItems();i++)
		{
			I=room.getItem(i);
			if((I instanceof Exit)&&(I.container()==null)&&(((Exit)I).viewableText(mob, room).length()>0))
				buf.append("^<MEX^>"+((Exit)I).doorName()+"^</MEX^> ");
		}
		mob.tell(buf.toString().trim()+"]^.^N");
	}

}
