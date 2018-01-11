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
import com.planet_ink.coffee_mud.Libraries.interfaces.AchievementLibrary.Event;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.lang.ref.WeakReference;

/*
   Copyright 2004-2018 Bo Zimmerman

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
	@Override
	public String ID()
	{
		return "CommonMsgs";
	}

	protected final static int LOOK_LONG=0;
	protected final static int LOOK_NORMAL=1;
	protected final static int LOOK_BRIEFOK=2;

	// this needs to be global to work right
	protected final List<WeakReference<MsgMonitor>>
			globalMonitors = new SLinkedList<WeakReference<MsgMonitor>>();

	protected Ability awarenessA=null;

	@Override
	public boolean handleUnknownCommand(MOB mob, List<String> command)
	{
		if(mob==null)
			return false;
		final Room R=mob.location();
		String msgStr;
		if(R==null)
		{ 
			mob.tell(L("Huh?")); 
			return false;
		}
		if(command.size()>0)
		{
			final String word=CMLib.english().getAnEvokeWord(mob,command.get(0));
			if(word!=null)
				msgStr=L("You don't know how to @x1 that.",word.toLowerCase());
			else
				msgStr=L("Huh?");
		}
		else
			msgStr=L("Huh?");
		final CMMsg msg=CMClass.getMsg(mob,null,null,CMMsg.MSG_HUH,msgStr,CMParms.combineQuoted(command,0),null);
		if(!R.okMessage(mob,msg))
			return false;
		R.send(mob,msg);
		return true;
	}

	public boolean handleCommandFail(final MOB mob, final List<String> commands, final String msgStr)
	{
		return handleCommandFail(mob, null, null, commands, msgStr);
	}
	
	public boolean handleCommandFail(final MOB mob, Environmental target, Environmental tools, final List<String> command, final String msgStr)
	{
		if(mob==null)
			return false;
		final Room R=mob.location();
		final CMMsg msg=CMClass.getMsg(mob,target,tools,CMMsg.MSG_COMMANDFAIL,msgStr,CMMsg.NO_EFFECT,CMParms.combineQuoted(command,0),CMMsg.NO_EFFECT,null);
		if(!R.okMessage(mob,msg))
			return false;
		R.send(mob,msg);
		return true;
	}
	
	@Override
	public boolean postCommandFail(final MOB mob, final List<String> commands, final String msgStr)
	{
		return postCommandFail(mob, null, null, commands, msgStr);
	}
	
	@Override
	public boolean postCommandFail(final MOB mob, Environmental target, Environmental tools, final List<String> command, final String msgStr)
	{
		if(mob==null)
			return false;
		final Room R=mob.location();
		if(R!=null)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,tools,CMMsg.MSG_COMMANDFAIL,msgStr,CMMsg.NO_EFFECT,CMParms.combineQuoted(command,0),CMMsg.NO_EFFECT,null);
			if(!R.okMessage(mob,msg))
				return false;
			R.send(mob,msg);
		}
		return true;
	}
	
	@Override
	public Object unforcedInternalCommand(MOB mob, String command, Object... parms)
	{
		try
		{
			final Command C=CMClass.getCommand(command);
			if(C!=null)
				return C.executeInternal(mob,0,parms);
		}
		catch(final IOException e)
		{
			Log.errOut("CommonMsgs",e);
		}
		return Boolean.FALSE;
	}

	@Override
	public Object forceInternalCommand(MOB mob, String command, Object... parms)
	{
		try
		{
			final Command C=CMClass.getCommand(command);
			if(C!=null)
				return C.executeInternal(mob,MUDCmdProcessor.METAFLAG_FORCED,parms);
		}
		catch(final IOException e)
		{
			Log.errOut("CommonMsgs",e);
		}
		return Boolean.FALSE;
	}

	@Override
	public boolean forceStandardCommand(MOB mob, String command, List<String> parms)
	{
		try
		{
			final Command C=CMClass.getCommand(command);
			if(C!=null)
				return C.execute(mob,parms,MUDCmdProcessor.METAFLAG_FORCED);
		}
		catch(final IOException e)
		{
			Log.errOut("CommonMsgs",e);
		}
		return false;
	}

	@Override
	public void monitorGlobalMessage(Room room, CMMsg msg)
	{
		MsgMonitor M;
		for (WeakReference<MsgMonitor> W : globalMonitors)
		{
			M=W.get();
			if(M != null)
				M.monitorMsg(room, msg);
			else
				globalMonitors.remove(M);
		}
	}

	@Override
	public void addGlobalMonitor(MsgMonitor M)
	{
		if(M==null)
			return;
		for (WeakReference<MsgMonitor> W : globalMonitors)
		{
			if(W.get()==M)
				return;
		}
		globalMonitors.add(new WeakReference<MsgMonitor>(M));
	}

	@Override
	public void delGlobalMonitor(MsgMonitor M)
	{
		if(M==null)
			return;
		for (WeakReference<MsgMonitor> W : globalMonitors)
		{
			if(W.get()==M)
				globalMonitors.remove(W);
		}
	}

	@Override
	public StringBuilder getScore(MOB mob)
	{
		final Vector<String> V=new Vector<String>();
		forceStandardCommand(mob,"Score",V);
		if(V.size()==1)
			return new StringBuilder(V.firstElement());
		return new StringBuilder("");
	}

	@Override
	public StringBuilder getEquipment(MOB viewer, MOB mob)
	{
		return (StringBuilder)forceInternalCommand(mob,"Equipment",viewer);
	}

	@Override
	public StringBuilder getInventory(MOB viewer, MOB mob)
	{
		return new StringBuilder(forceInternalCommand(mob,"Inventory",viewer).toString());
	}

	@Override
	public void postChannel(MOB mob, String channelName, String message, boolean systemMsg)
	{
		forceInternalCommand(mob,"Channel",Boolean.valueOf(systemMsg),channelName,message);
	}

	public MOB nonClanTalker = null;

	@Override
	public void postChannel(String channelName, Iterable<Pair<Clan,Integer>> clanList, String message, boolean systemMsg)
	{
		MOB talker;
		if(clanList != null)
		{
			talker=CMClass.getFactoryMOB();
			talker.setName("^</B^>");
			talker.setLocation(CMLib.map().getRandomRoom());
			talker.basePhyStats().setDisposition(PhyStats.IS_GOLEM);
			talker.phyStats().setDisposition(PhyStats.IS_GOLEM);
			for(final Pair<Clan,Integer> c : clanList)
				talker.setClan(c.first.clanID(),c.second.intValue());
		}
		else
		if(nonClanTalker!=null)
		{
			talker=nonClanTalker;
		}
		else
		{
			talker=CMClass.getFactoryMOB();
			talker.setName("^</B^>");
			talker.setLocation(CMLib.map().getRandomRoom());
			talker.basePhyStats().setDisposition(PhyStats.IS_GOLEM);
			talker.phyStats().setDisposition(PhyStats.IS_GOLEM);
			nonClanTalker=talker;
		}
		postChannel(talker,channelName,message,systemMsg);
	}

	@Override
	public boolean postDrop(MOB mob, Environmental dropThis, boolean quiet, boolean optimized, boolean intermediate)
	{
		return ((Boolean)forceInternalCommand(mob,"Drop",dropThis,Boolean.valueOf(quiet),Boolean.valueOf(optimized),Boolean.valueOf(intermediate))).booleanValue();
	}

	@Override
	public boolean postGive(MOB mob, MOB targetM, Item giveThis, boolean quiet)
	{
		return ((Boolean)forceInternalCommand(mob,"Give",giveThis,targetM,Boolean.valueOf(quiet))).booleanValue();
	}

	@Override
	public boolean postOpen(MOB mob, Environmental openThis, boolean quiet)
	{
		return ((Boolean)forceInternalCommand(mob,"Open",openThis,Boolean.valueOf(quiet))).booleanValue();
	}

	@Override
	public boolean postGet(MOB mob, Item container, Item getThis, boolean quiet)
	{
		if(container==null)
			return ((Boolean)forceInternalCommand(mob,"Get",getThis,Boolean.valueOf(quiet))).booleanValue();
		return ((Boolean)forceInternalCommand(mob,"Get",getThis,container,Boolean.valueOf(quiet))).booleanValue();
	}

	@Override
	public boolean postPut(MOB mob, Item container, Item getThis, boolean quiet)
	{
		if(container==null)
			return ((Boolean)forceInternalCommand(mob,"Put",getThis,Boolean.valueOf(quiet))).booleanValue();
		return ((Boolean)forceInternalCommand(mob,"Put",getThis,container,Boolean.valueOf(quiet))).booleanValue();
	}

	@Override
	public boolean postRemove(MOB mob, Item item, boolean quiet)
	{
		return ((Boolean)forceInternalCommand(mob,"Remove",item,Boolean.valueOf(quiet))).booleanValue();
	}

	@Override
	public boolean postWear(MOB mob, Item item, boolean quiet)
	{
		return ((Boolean)forceInternalCommand(mob,"Wear",item,Boolean.valueOf(quiet))).booleanValue();
	}

	@Override
	public void postLook(MOB mob, boolean quiet)
	{
		if(quiet)
			forceStandardCommand(mob,"Look",new XVector<String>("LOOK","UNOBTRUSIVELY"));
		else
			forceStandardCommand(mob,"Look",new XVector<String>("LOOK"));
	}

	@Override
	public void postRead(MOB mob, Physical target, String readOff, boolean quiet)
	{
		forceInternalCommand(mob,"Read",target,readOff,Boolean.valueOf(quiet));
	}

	@Override
	public void postFlee(MOB mob, String whereTo)
	{
		forceStandardCommand(mob,"Flee",new XVector<String>("FLEE",whereTo));
	}

	@Override
	public void postSheath(MOB mob, boolean ifPossible)
	{
		if(ifPossible)
			forceStandardCommand(mob,"Sheath",new XVector<String>("SHEATH","IFPOSSIBLE"));
		else
			forceStandardCommand(mob,"Sheath",new XVector<String>("SHEATH"));
	}

	@Override
	public void postDraw(MOB mob, boolean doHold, boolean ifNecessary)
	{
		if(ifNecessary)
		{
			if(doHold)
				forceStandardCommand(mob,"Draw",new XVector<String>("DRAW","HELD","IFNECESSARY"));
			else
				forceStandardCommand(mob,"Draw",new XVector<String>("DRAW","IFNECESSARY"));
		}
		else
			forceStandardCommand(mob,"Draw",new XVector<String>("DRAW"));
	}

	@Override
	public void postStand(MOB mob, boolean ifNecessary)
	{
		if(ifNecessary)
			forceStandardCommand(mob,"Stand",new XVector<String>("STAND","IFNECESSARY"));
		else
			forceStandardCommand(mob,"Stand",new XVector<String>("STAND"));
	}

	@Override
	public void postSleep(MOB mob)
	{
		forceStandardCommand(mob,"Sleep",new XVector<String>("SLEEP"));
	}

	@Override
	public void postFollow(MOB follower, MOB leader, boolean quiet)
	{
		if(leader!=null)
		{
			forceInternalCommand(follower,"Follow",leader,Boolean.valueOf(quiet));
		}
		else
		{
			if(quiet)
				forceStandardCommand(follower,"Follow",new XVector<String>("FOLLOW","SELF","UNOBTRUSIVELY"));
			else
				forceStandardCommand(follower,"Follow",new XVector<String>("FOLLOW","SELF"));
		}
	}

	@Override
	public void postSay(MOB mob, MOB target,String text)
	{
		postSay(mob,target,text,false,false);
	}

	@Override
	public void postSay(MOB mob, String text)
	{
		postSay(mob,null,text,false,false);
	}

	@Override
	public void postSay(MOB mob, MOB target, String text, boolean isPrivate, boolean tellFlag)
	{
		Room location=mob.location();
		text=CMProps.applyINIFilter(text,CMProps.Str.SAYFILTER);
		if(target!=null)
			location=target.location();
		if(location==null)
			return;
		if((isPrivate)&&(target!=null))
		{
			if(tellFlag)
			{
				String targetName=target.name();
				if(targetName.indexOf('@')>=0)
				{
					final String mudName=targetName.substring(targetName.indexOf('@')+1);
					targetName=targetName.substring(0,targetName.indexOf('@'));
					if((!(CMLib.intermud().i3online()))&&(!(CMLib.intermud().imc2online())))
						mob.tell(L("Intermud is unavailable."));
					else
						CMLib.intermud().i3tell(mob,targetName,mudName,text);
				}
				else
				{
					final boolean ignore=((target.playerStats()!=null)&&(target.playerStats().getIgnored().contains(mob.Name())));
					CMMsg msg=null;
					if((!CMLib.flags().isSeeable(mob))||(!CMLib.flags().isSeeable(target)))
						msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_TELL,L("^t^<TELL \"@x1\"^>You tell <T-NAME> '@x2'^</TELL^>^?^.",CMStrings.removeColors(target.name(mob)),text),CMMsg.MSG_TELL,L("^t^<TELL \"@x1\"^><S-NAME> tell(s) you '@x2'^</TELL^>^?^.",CMStrings.removeColors(mob.name(target)),text),CMMsg.NO_EFFECT,null);
					else
						msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_TELL,L("^t^<TELL \"@x1\"^>You tell @x2 '@x3'^</TELL^>^?^.",CMStrings.removeColors(target.name(mob)),target.name(mob),text),CMMsg.MSG_TELL,L("^t^<TELL \"@x1\"^>@x2 tell(s) you '@x3'^</TELL^>^?^.",CMStrings.removeColors(mob.name(target)),mob.Name(),text),CMMsg.NO_EFFECT,null);
					if((mob.location().okMessage(mob,msg))
					&&((ignore)||((target.location()!=null)&&(target.location().okMessage(target,msg)))))
					{
						if((mob.session()!=null)&&(mob.session().getClientTelnetMode(Session.TELNET_GMCP)))
						{
							mob.session().sendGMCPEvent("comm.channel", "{\"chan\":\"tell\",\"msg\":\""+
									MiniJSON.toJSONString(CMLib.coffeeFilter().fullOutFilter(null, mob, mob, target, null, CMStrings.removeColors(msg.sourceMessage()), false))
									+"\",\"player\":\""+mob.name(target)+"\"}");
						}
						mob.executeMsg(mob,msg);
						if((mob!=target)&&(!ignore))
						{
							if((target.session()!=null)&&(target.session().getClientTelnetMode(Session.TELNET_GMCP)))
							{
								target.session().sendGMCPEvent("comm.channel", "{\"chan\":\"tell\",\"msg\":\""+
										MiniJSON.toJSONString(CMLib.coffeeFilter().fullOutFilter(null, target, mob, target, null, CMStrings.removeColors(msg.targetMessage()), false))
										+"\",\"player\":\""+mob.name(target)+"\"}");
							}
							target.executeMsg(target,msg);
							if(msg.trailerMsgs()!=null)
							{
								for(final CMMsg msg2 : msg.trailerMsgs())
								{
									if((msg!=msg2)&&(target.okMessage(target,msg2)))
										target.executeMsg(target,msg2);
								}
								msg.trailerMsgs().clear();
								if(msg.trailerRunnables()!=null)
								{
									for(final Runnable r : msg.trailerRunnables())
										CMLib.threads().executeRunnable(r);
									msg.trailerRunnables().clear();
								}
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
				final CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_SPEAK,L("^T^<SAY \"@x1\"^><S-NAME> say(s) '@x2' to <T-NAMESELF>.^</SAY^>^?",CMStrings.removeColors(target.name(mob)),text),CMMsg.MSG_SPEAK,L("^T^<SAY \"@x1\"^><S-NAME> say(s) '@x2' to <T-NAMESELF>.^</SAY^>^?",CMStrings.removeColors(mob.name(target)),text),CMMsg.NO_EFFECT,null);
				gmcpSaySend("say",mob, target, msg);
				if(location.okMessage(mob,msg))
					location.send(mob,msg);
			}
		}
		else
		if(!isPrivate)
		{
			final String str=L("<S-NAME> say(s) '@x1'"+((target==null)?"^</SAY^>":" to <T-NAMESELF>.^</SAY^>^?"),text);
			final CMMsg msg=CMClass.getMsg(mob,target,null,CMMsg.MSG_SPEAK,"^T^<SAY \""+CMStrings.removeColors((target==null)?mob.name(target):target.name(mob))+"\"^>"+str,"^T^<SAY \""
					+CMStrings.removeColors(mob.name(target))+"\"^>"+str,"^T^<SAY \""+CMStrings.removeColors(mob.name(target))+"\"^>"+str);
			gmcpSaySend("say",mob, target, msg);
			if(location.okMessage(mob,msg))
				location.send(mob,msg);
		}
	}

	protected void gmcpSaySend(String sayName, MOB mob, MOB target, CMMsg msg)
	{
		if((mob.session()!=null)&&(mob.session().getClientTelnetMode(Session.TELNET_GMCP)))
		{
			mob.session().sendGMCPEvent("comm.channel", "{\"chan\":\""+sayName+"\",\"msg\":\""+
					MiniJSON.toJSONString(CMLib.coffeeFilter().fullOutFilter(null, mob, mob, target, null, CMStrings.removeColors(msg.sourceMessage()), false))
					+"\",\"player\":\""+mob.name(target)+"\"}");
		}
		final Room R=mob.location();
		if(R!=null)
		for(int i=0;i<R.numInhabitants();i++)
		{
			final MOB M=R.fetchInhabitant(i);
			if((M!=null)&&(M!=msg.source())&&(M.session()!=null)&&(M.session().getClientTelnetMode(Session.TELNET_GMCP)))
			{
				M.session().sendGMCPEvent("comm.channel", "{\"chan\":\""+sayName+"\",\"msg\":\""+
						MiniJSON.toJSONString(CMLib.coffeeFilter().fullOutFilter(null, M, mob, target, null, CMStrings.removeColors(msg.othersMessage()), false))
						+"\",\"player\":\""+mob.name(target)+"\"}");
			}
		}
	}

	@Override
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
		if(!(msg.target() instanceof MOB))
			return;
		final MOB sniffingmob=msg.source();
		final MOB sniffedmob=(MOB)msg.target();
		if((sniffedmob.playerStats()!=null)
		&&(sniffedmob.soulMate()==null)
		&&(sniffedmob.playerStats().getHygiene()>=PlayerStats.HYGIENE_DELIMIT))
		{
			final int x=(int)(sniffedmob.playerStats().getHygiene()/PlayerStats.HYGIENE_DELIMIT);
			if(x<=1)
				sniffingmob.tell(L("@x1 has a slight aroma about @x2.",sniffedmob.name(sniffingmob),sniffedmob.charStats().himher()));
			else
			if(x<=3)
				sniffingmob.tell(L("@x1 smells pretty sweaty.",sniffedmob.name(sniffingmob)));
			else
			if(x<=7)
				sniffingmob.tell(L("@x1 stinks pretty bad.",sniffedmob.name(sniffingmob)));
			else
			if(x<15)
				sniffingmob.tell(L("@x1 smells most foul.",sniffedmob.name(sniffingmob)));
			else
				sniffingmob.tell(L("@x1 reeks of noxious odors.",sniffedmob.name(sniffingmob)));
		}
	}

	@Override
	public void handleObserveComesToLife(MOB observer, MOB lifer, CMMsg msg)
	{

	}

	@Override
	public void handleComeToLife(MOB mob, CMMsg msg)
	{

	}

	@Override
	public void handleSit(CMMsg msg)
	{
		final MOB sittingmob=msg.source();
		int oldDisposition=sittingmob.basePhyStats().disposition();
		oldDisposition=oldDisposition&(~(PhyStats.IS_SLEEPING|PhyStats.IS_SNEAKING|PhyStats.IS_SITTING|PhyStats.IS_CUSTOM));
		sittingmob.basePhyStats().setDisposition(oldDisposition|PhyStats.IS_SITTING);
		sittingmob.recoverPhyStats();
		sittingmob.recoverCharStats();
		sittingmob.recoverMaxState();
		sittingmob.tell(sittingmob,msg.target(),msg.tool(),msg.sourceMessage());
	}
	
	@Override
	public void handleSleep(CMMsg msg)
	{
		final MOB sleepingmob=msg.source();
		int oldDisposition=sleepingmob.basePhyStats().disposition();
		oldDisposition=oldDisposition&(~(PhyStats.IS_SLEEPING|PhyStats.IS_SNEAKING|PhyStats.IS_SITTING|PhyStats.IS_CUSTOM));
		sleepingmob.basePhyStats().setDisposition(oldDisposition|PhyStats.IS_SLEEPING);
		sleepingmob.recoverPhyStats();
		sleepingmob.recoverCharStats();
		sleepingmob.recoverMaxState();
		sleepingmob.tell(sleepingmob,msg.target(),msg.tool(),msg.sourceMessage());
	}
	
	@Override
	public void handleStand(CMMsg msg)
	{
		final MOB standingmob=msg.source();
		int oldDisposition=standingmob.basePhyStats().disposition();
		oldDisposition=oldDisposition&(~(PhyStats.IS_SLEEPING|PhyStats.IS_SNEAKING|PhyStats.IS_SITTING|PhyStats.IS_CUSTOM));
		standingmob.basePhyStats().setDisposition(oldDisposition);
		standingmob.recoverPhyStats();
		standingmob.recoverCharStats();
		standingmob.recoverMaxState();
		standingmob.tell(standingmob,msg.target(),msg.tool(),msg.sourceMessage());
	}

	@Override
	public void handleRecall(CMMsg msg)
	{
		final MOB recallingMob=msg.source();
		final Room recallingRoom=recallingMob.location();
		if((msg.target() instanceof Room)
		&&(recallingRoom!=null)
		&&(recallingRoom != msg.target()))
		{
			final Room recallToRoom=(Room)msg.target();
			recallingMob.tell(msg.source(),null,msg.tool(),msg.targetMessage());

			recallingRoom.delInhabitant(recallingMob);
			recallToRoom.addInhabitant(recallingMob);
			recallToRoom.showOthers(recallingMob,null,CMMsg.MSG_ENTER,L("<S-NAME> appears out of the Java Plane."));

			recallingMob.setLocation(recallToRoom);
			if((recallingMob.riding()!=null)
			&&(recallToRoom!=CMLib.map().roomLocation(recallingMob.riding())))
			{
				if(recallingMob.riding().mobileRideBasis())
				{
					if(recallingMob.riding() instanceof Item)
						recallToRoom.moveItemTo((Item)recallingMob.riding(),ItemPossessor.Expire.Never,ItemPossessor.Move.Followers);
					else
					if(recallingMob.riding() instanceof MOB)
						recallToRoom.bringMobHere((MOB)recallingMob.riding(),true);
				}
				else
					recallingMob.setRiding(null);
			}
			recallingMob.recoverPhyStats();
			recallingMob.recoverCharStats();
			recallingMob.recoverMaxState();
			postLook(recallingMob,true);
		}
	}

	@Override
	public int tickManaConsumption(MOB mob, int manaConsumeCounter)
	{
		if((CMProps.getIntVar(CMProps.Int.MANACONSUMETIME)>0)
		&&(CMProps.getIntVar(CMProps.Int.MANACONSUMEAMT)>0)
		&&((--manaConsumeCounter)<=0))
		{
			final Vector<Ability> expenseAffects=new Vector<Ability>();
			manaConsumeCounter=CMProps.getIntVar(CMProps.Int.MANACONSUMETIME);
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
				switch(CMProps.getIntVar(CMProps.Int.MANACONSUMEAMT))
				{
				case -100:
					basePrice = basePrice * mob.phyStats().level();
					break;
				case -200:
					{
						int total=0;
						for(int a1=0;a1<expenseAffects.size();a1++)
						{
							final int lql=CMLib.ableMapper().lowestQualifyingLevel(expenseAffects.elementAt(a1).ID());
							if(lql>0)
								total+=lql;
							else
								total+=1;
						}
						basePrice=basePrice*(total/expenseAffects.size());
					}
					break;
				default:
					basePrice=basePrice*CMProps.getIntVar(CMProps.Int.MANACONSUMEAMT);
					break;
				}

				// 1 per tick per level per msg.  +1 to the affects so that way it's about
				// 3 cost = 1 regen... :)
				int reallyEat=basePrice*(expenseAffects.size()+1);
				while(mob.curState().getMana()<reallyEat)
				{
					mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> strength of will begins to crumble."));
					//pick one and kill it
					final Ability A=expenseAffects.elementAt(CMLib.dice().roll(1,expenseAffects.size(),-1));
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

	@Override
	public void tickAging(final MOB mob, final long millisSinceLast)
	{
		if((mob==null)||(CMSecurity.isDisabled(CMSecurity.DisFlag.ALL_AGEING)))
			return;
		final long minutesEllapsed=(millisSinceLast / 60000);
		mob.setAgeMinutes(mob.getAgeMinutes()+minutesEllapsed); // this is really minutes
		if((minutesEllapsed>0)
		&&((!CMLib.flags().isCloaked(mob))
		  ||(!CMSecurity.isAllowed(mob, mob.location(), CMSecurity.SecFlag.CMDROOMS))))
			CMLib.players().bumpPrideStat(mob,AccountStats.PrideStat.MINUTES_ON, (int)minutesEllapsed);

		final PlayerStats stats = mob.playerStats();
		if(stats==null)
			return;
		final int[] birthDay = stats.getBirthday();
		if((mob.baseCharStats().getStat(CharStats.STAT_AGE)>0)
		&&(birthDay!=null))
		{
			final TimeClock clock=CMLib.time().localClock(mob.getStartRoom());
			final int currYear=clock.getYear();
			final int month=clock.getMonth();
			final int day=clock.getDayOfMonth();
			final int bday=birthDay[PlayerStats.BIRTHDEX_DAY];
			final int bmonth=birthDay[PlayerStats.BIRTHDEX_MONTH];
			while((currYear>birthDay[PlayerStats.BIRTHDEX_LASTYEARCELEBRATED])
			||((currYear==birthDay[PlayerStats.BIRTHDEX_LASTYEARCELEBRATED])&&((month>bmonth)||((month==bmonth)&&(day>=bday)))))
			{
				if(CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.IMMORT))
				{
					birthDay[PlayerStats.BIRTHDEX_YEAR]++;
				}
				else
				{
					if((month==bmonth)&&(day==bday))
						mob.tell(L("Happy Birthday!"));
					mob.baseCharStats().setStat(CharStats.STAT_AGE,mob.baseCharStats().getStat(CharStats.STAT_AGE)+1);
					mob.recoverCharStats();
					mob.recoverPhyStats();
					mob.recoverMaxState();
				}
				if(CMSecurity.isDisabled(CMSecurity.DisFlag.SLOW_AGEING) || (birthDay[PlayerStats.BIRTHDEX_LASTYEARCELEBRATED]==currYear))
					birthDay[PlayerStats.BIRTHDEX_LASTYEARCELEBRATED]++;
				else
					birthDay[PlayerStats.BIRTHDEX_LASTYEARCELEBRATED]=currYear;
			}
			if(!CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.IMMORT))
			{
				if((mob.baseCharStats().ageCategory()>=Race.AGE_VENERABLE)
				&&(CMLib.dice().rollPercentage()==1)
				&&(CMLib.dice().rollPercentage()==1)
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					final Ability A=CMClass.getAbility("Disease_Cancer");
					if((A!=null)&&(mob.fetchEffect(A.ID())==null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
						A.invoke(mob,mob,true,0);
				}
				else
				if((mob.baseCharStats().ageCategory()>=Race.AGE_ANCIENT)
				&&(CMLib.dice().rollPercentage()==1)
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					final Ability A=CMClass.getAbility("Disease_Arthritis");
					if((A!=null)&&(mob.fetchEffect(A.ID())==null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
						A.invoke(mob,mob,true,0);
				}
				else
				if((mob.baseCharStats().ageCategory()>=Race.AGE_ANCIENT)
				&&(CMLib.dice().rollPercentage()==1)
				&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.AUTODISEASE)))
				{
					final Ability A=CMClass.getAbility("Disease_Alzheimers");
					if((A!=null)&&(mob.fetchEffect(A.ID())==null)&&(!CMSecurity.isAbilityDisabled(A.ID())))
						A.invoke(mob,mob,true,0);
				}
				else
				if(CMLib.dice().rollPercentage()<10)
				{
					final int max=CMProps.getIntVar(CMProps.Int.BASEMAXSTAT);
					for(final int i: CharStats.CODES.MAXCODES())
					{
						if((max+mob.charStats().getStat(i))<=0)
						{
							mob.tell(L("Your max @x1 has fallen below 1!",CharStats.CODES.DESC(CharStats.CODES.toMAXBASE(i)).toLowerCase()));
							CMLib.combat().postDeath(null,mob,null);
							break;
						}
					}
				}
			}
		}
	}

	protected String relativeCharStatTest(CharStats C, MOB mob, String weakword, String strongword, int stat)
	{
		final double d=CMath.div(C.getStat(stat),mob.charStats().getStat(stat));
		String prepend="";
		if((d<=0.5)||(d>=3.0))
			prepend="much ";
		if(d>=1.6)
			return mob.charStats().HeShe()+" appears "+prepend+weakword+" than the average "+mob.charStats().raceName()+".\n\r";
		if(d<=0.67)
			return mob.charStats().HeShe()+" appears "+prepend+strongword+" than the average "+mob.charStats().raceName()+".\n\r";
		return "";
	}

	@Override
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

	public String makeContainerTypes(Container E)
	{
		if(E.containTypes()>0)
		{
			ArrayList<String> list=new ArrayList<String>();
			for(int i=0;i<Container.CONTAIN_DESCS.length-1;i++)
			{
				if(CMath.isSet((int)E.containTypes(),i))
					list.add(CMStrings.capitalizeAndLower(Container.CONTAIN_DESCS[i+1]));
			}
			return CMLib.english().toEnglishStringList(list);

		}
		return "";
	}

	@Override
	public String getExamineItemString(MOB mob, Item item)
	{
		final StringBuilder response=new StringBuilder("");
		String level=null;
		if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
		{
			final int l=(int)Math.round(Math.floor(CMath.div(item.phyStats().level(),10.0)));
			level=(l*10)+"-"+((l*10)+9);
		}
		else
		if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<18))
		{
			final int l=(int)Math.round(Math.floor(CMath.div(item.phyStats().level(),5.0)));
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
			final double l=Math.floor(CMath.div(item.phyStats().level(),divider));
			weight=(int)Math.round(CMath.mul(l,divider))+"-"+(int)Math.round(CMath.mul(l,divider)+(divider-1.0));
		}
		else
		if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<18))
		{
			divider=divider/2.0;
			final double l=Math.floor(CMath.div(item.phyStats().level(),divider));
			weight=(int)Math.round(CMath.mul(l,divider))+"-"+(int)Math.round(CMath.mul(l,divider)+(divider-1.0));
		}
		else
			weight=""+item.phyStats().weight();
		if(item instanceof CagedAnimal)
		{
			final MOB M=((CagedAnimal)item).unCageMe();
			if(M==null)
				response.append(L("\n\rLooks like some sort of lifeless thing.\n\r"));
			else
			{
				if(M.phyStats().height()>0)
					response.append(L("\n\r@x1 is @x2 inches tall and weighs @x3 pounds.\n\r",CMStrings.capitalizeFirstLetter(item.name()),""+M.phyStats().height(),weight));
				if((mob==null)||(!mob.isMonster()))
					response.append(CMLib.protocol().mxpImage(M," ALIGN=RIGHT H=70 W=70"));
				response.append(M.healthText(mob)+"\n\r\n\r");
				if(!M.description().equalsIgnoreCase(item.description()))
					response.append(M.description(mob)+"\n\r\n\r");
			}
		}
		else
		{
			response.append(L("\n\r@x1 is a level @x2 item, and weighs @x3 pounds.  ",CMStrings.capitalizeFirstLetter(item.name(mob)),level,weight));
			if((item instanceof RawMaterial)
			&&(!CMLib.flags().isABonusItems(item))
			&&(item.rawSecretIdentity().length()>0)
			&&(item.basePhyStats().weight()>1)
			&&((mob==null)||(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>3)))
				response.append(L("It appears to be a bundle of `@x1`.  ",item.rawSecretIdentity()));

			if((mob!=null)&&(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<10))
				response.append(L("It is mostly made of a kind of @x1.  ",RawMaterial.Material.findByMask(item.material()&RawMaterial.MATERIAL_MASK).noun()));
			else
				response.append(L("It is mostly made of @x1.  ",RawMaterial.CODES.NAME(item.material()).toLowerCase()));
			if(item instanceof Recipe)
			{
				final String[] recipeCodeLines = ((Recipe)item).getRecipeCodeLines();
				final int usedPages = (recipeCodeLines == null) ? 0 : recipeCodeLines.length;
				final int totalRecipePages = ((Recipe)item).getTotalRecipePages();
				final int remainingRecipePages = totalRecipePages - usedPages;
				if(((Recipe)item).getTotalRecipePages()>1)
					response.append( L("There are @x1 blank pages remaining out of @x2 total.  ",""+remainingRecipePages,""+totalRecipePages));
			}
			if((item instanceof Container)
			&&(((Container)item).capacity()>=item.phyStats().weight())
			&&((mob==null)||(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>7)))
			{
				final Container C=(Container)item;
				String suffix="";
				if(C.hasADoor() && C.hasALock())
					suffix = L(" with a lid and lock");
				else
				if(C.hasADoor())
					suffix = L(" with a lid");
				if(C.containTypes()==Container.CONTAIN_ANYTHING)
					response.append(L("It is a container@x1.  ",suffix));
				else
					response.append(L("It is a container@x1 that can hold @x2.  ",suffix,this.makeContainerTypes(C)));
				if((mob==null)||(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10))
				{
					
					double error = 5.0*(18.0 - ((mob==null)?18:mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)));
					int finalCap = C.capacity() - C.basePhyStats().weight();
					if((error > 0) && (finalCap > 0))
					{
						finalCap += CMLib.dice().plusOrMinus((int)Math.round(error * finalCap));
						response.append(L("You believe it will hold about @x1 pounds.  ",""+finalCap));
					}
				}
			}
					
			if(item instanceof Ammunition)
				response.append(L("It is @x1 ammunition of type '@x2'.  ",""+((Ammunition)item).ammunitionRemaining(),((Ammunition)item).ammunitionType()));
			else
			if(item instanceof Weapon)
			{
				if((mob==null)||mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10)
				{
					response.append(L("It is a "));
					if((item.rawLogicalAnd())&&CMath.bset(item.rawProperLocationBitmap(),Wearable.WORN_WIELD|Wearable.WORN_HELD))
						response.append(L("two handed "));
					else
						response.append(L("one handed "));
					response.append(L("@x1 class weapon that does @x2 damage.  ",CMStrings.capitalizeAndLower(Weapon.CLASS_DESCS[((Weapon)item).weaponClassification()]),CMStrings.capitalizeAndLower(Weapon.TYPE_DESCS[((Weapon)item).weaponDamageType()])));
				}
				if((item instanceof AmmunitionWeapon) && ((AmmunitionWeapon)item).requiresAmmunition())
					response.append(L("It requires ammunition of type '@x1'.  ",((AmmunitionWeapon)item).ammunitionType()));
			}
			else
			if((item instanceof Armor)
			&&((mob==null)||mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>10))
			{
				if(item.phyStats().height()>0)
					response.append(L(" It is a size @x1, and is ",""+item.phyStats().height()));
				else
					response.append(L(" It is your size, and is "));
				response.append(((item.rawProperLocationBitmap()==Wearable.WORN_HELD)||(item.rawProperLocationBitmap()==(Wearable.WORN_HELD|Wearable.WORN_WIELD)))
									 ?new StringBuilder("")
									 :new StringBuilder("worn on the "));
				final Wearable.CODES codes = Wearable.CODES.instance();
				for(final long wornCode : codes.all())
				{
					if((wornCode != Wearable.IN_INVENTORY)
					&&(CMath.bset(item.rawProperLocationBitmap(),wornCode)))
					{
						final String wornString=codes.name(wornCode);
						if(wornString.length()>0)
						 {
							response.append(CMStrings.capitalizeAndLower(wornString)+" ");
							if(item.rawLogicalAnd())
								response.append(L("and "));
							else
								response.append(L("or "));
						}
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
		if(item.expirationDate() < System.currentTimeMillis())
			return "*IMMINENT*";
		return ""+CMLib.time().date2EllapsedTime((item.expirationDate()-System.currentTimeMillis()), TimeUnit.MINUTES, false);
	}

	protected void handleBeingItemLookedAt(CMMsg msg)
	{
		final MOB mob=msg.source();
		final Item item=(Item)msg.target();
		if(!CMLib.flags().canBeSeenBy(item,mob))
		{
			mob.tell(L("You can't see that!"));
			return;
		}

		final StringBuilder buf=new StringBuilder("");
		if(mob.isAttributeSet(MOB.Attrib.SYSOPMSGS))
		{
			String decayTime="";
			if(item instanceof Decayable)
			{
				if(((Decayable)item).decayTime()==Long.MAX_VALUE)
					decayTime=L("/  Never Decays");
				else
					decayTime=L("/  Decay on @x1",CMLib.time().date2String(((Decayable)item).decayTime()));
			}
			StringBuilder spells=new StringBuilder("");
			for(Enumeration<Ability> a=item.effects();a.hasMoreElements();)
			{
				final Ability A=a.nextElement();
				if((!A.isSavable())||(A.canBeUninvoked()))
					spells.append(A.ID()).append(" ");
			}
			buf.append(L("\n\r"
			+ "Type  : @x1\n\r"
			+ "Rejuv : @x2\n\r"
			+ "Uses  : @x3\n\r"
			+ "Height: @x4\n\r"
			+ "Weight: @x5\n\r"
			+ "Abilty: @x6\n\r"
			+ "Level : @x7\n\r"
			+ "Expire: @x8\n\r"
			+ "Capaci: @x9\n\r"
			+ "Affect: @x10\n\r"
			+ "Misc  : @x11\n\r"
			+ "@x12",
			item.ID(),
			""+item.basePhyStats().rejuv(),
			""+item.usesRemaining(),
			""+item.basePhyStats().height(),
			""+item.basePhyStats().weight(),
			""+item.basePhyStats().ability(),
			""+item.basePhyStats().level(),
			dispossessionTimeLeftString(item)+decayTime,
			((item instanceof Container)?(L("\n\rCapac.: ")+((Container)item).capacity()):""),
			spells.toString(),
			""+item.text().length(),
			item.text()
			));
		}
		if(item.description(mob).length()==0)
			buf.append(L("You don't see anything special about @x1.",item.name()));
		else
			buf.append(item.description(mob));
		if((msg.targetMinor()==CMMsg.TYP_EXAMINE)&&(!item.ID().endsWith("Wallpaper")))
			buf.append(getExamineItemString(mob,item));
		if(item instanceof Container)
		{
			buf.append("\n\r");
			final Container contitem=(Container)item;
			if((contitem.isOpen())
			&&((contitem.capacity()>0)
				||(contitem.hasContent())
				||((contitem instanceof Drink)&&(((Drink)contitem).liquidRemaining()>0))))
			{
				buf.append(item.name()+" contains:^<!ENTITY container \""+CMStrings.removeColors(item.name())+"\"^>"+(mob.isAttributeSet(MOB.Attrib.COMPRESS)?" ":"\n\r"));
				final Vector<Item> newItems=new Vector<Item>();
				if((item instanceof Drink)&&(((Drink)item).liquidRemaining()>0))
				{
					final RawMaterial l=(RawMaterial)CMClass.getItem("GenLiquidResource");
					final int myResource=((Drink)item).liquidType();
					l.setMaterial(myResource);
					((Drink)l).setLiquidType(myResource);
					l.setBaseValue(RawMaterial.CODES.VALUE(myResource));
					l.basePhyStats().setWeight(1);
					final String name=RawMaterial.CODES.NAME(myResource).toLowerCase();
					l.setName(L("some @x1",name));
					l.setDisplayText(L("some @x1 sits here.",name));
					l.setDescription("");
					CMLib.materials().addEffectsToResource(l);
					l.recoverPhyStats();
					newItems.addElement(l);
				}

				if(item.owner() instanceof MOB)
				{
					final MOB M=(MOB)item.owner();
					for(int i=0;i<M.numItems();i++)
					{
						final Item item2=M.getItem(i);
						if((item2!=null)&&(item2.container()==item))
							newItems.addElement(item2);
					}
					buf.append(CMLib.lister().lister(mob,newItems,true,"CMItem","",false,mob.isAttributeSet(MOB.Attrib.COMPRESS)));
				}
				else
				if(item.owner() instanceof Room)
				{
					final Room room=(Room)item.owner();
					if(room!=null)
					for(int i=0;i<room.numItems();i++)
					{
						final Item item2=room.getItem(i);
						if((item2!=null)&&(item2.container()==item))
							newItems.addElement(item2);
					}
					buf.append(CMLib.lister().lister(mob,newItems,true,"CRItem","",false,mob.isAttributeSet(MOB.Attrib.COMPRESS)));
				}
			}
			else
			if((contitem.hasADoor())&&((contitem.capacity()>0)||(contitem.hasContent())))
				buf.append(L("@x1 is closed.",item.name()));
		}
		if(!msg.source().isMonster())
			buf.append(CMLib.protocol().mxpImage(item," ALIGN=RIGHT H=70 W=70"));
		mob.tell(buf.toString());
	}

	protected void handleBeingItemSniffed(CMMsg msg)
	{
		String s=null;
		final Item item=(Item)msg.target();
		if(CMLib.flags().canSmell(msg.source()))
			s=RawMaterial.CODES.SMELL(item.material()).toLowerCase();
		if((s!=null)&&(s.length()>0))
			msg.source().tell(msg.source(),item,null,L("<T-NAME> has a @x1 smell.",s));
	}

	@Override
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
		final Room room=(Room)msg.target();
		final StringBuilder smell=new StringBuilder("");
		switch(room.domainType())
		{
		case Room.DOMAIN_INDOORS_UNDERWATER:
		case Room.DOMAIN_INDOORS_WATERSURFACE:
		case Room.DOMAIN_OUTDOORS_UNDERWATER:
		case Room.DOMAIN_OUTDOORS_WATERSURFACE:
			smell.append(L("It smells very WET here. "));
			break;
		case Room.DOMAIN_OUTDOORS_SEAPORT:
			smell.append(L("It smells clean, wet, and breezy here. "));
			break;
		case Room.DOMAIN_INDOORS_CAVE:
			smell.append(L("It smells very dank and mildewy here. "));
			break;
		case Room.DOMAIN_OUTDOORS_HILLS:
		case Room.DOMAIN_OUTDOORS_PLAINS:
			switch(room.getArea().getTimeObj().getSeasonCode())
			{
			case FALL:
			case WINTER:
				smell.append(L("There is a faint grassy smell here. "));
				break;
			case SPRING:
			case SUMMER:
				smell.append(L("There is a floral grassy smell here. "));
				break;
			}
			break;
		case Room.DOMAIN_OUTDOORS_WOODS:
			switch(room.getArea().getTimeObj().getSeasonCode())
			{
			case FALL:
			case WINTER:
				smell.append(L("There is a faint woodsy smell here. "));
				break;
			case SPRING:
			case SUMMER:
				smell.append(L("There is a rich woodsy smell here. "));
				break;
			}
			break;
		case Room.DOMAIN_OUTDOORS_JUNGLE:
			smell.append(L("There is a rich floral and plant aroma here. "));
			break;
		case Room.DOMAIN_OUTDOORS_MOUNTAINS:
		case Room.DOMAIN_OUTDOORS_ROCKS:
			switch(room.getArea().getTimeObj().getSeasonCode())
			{
			case FALL:
			case WINTER:
			case SUMMER:
				smell.append(L("It smells musty and rocky here. "));
				break;
			case SPRING:
				smell.append(L("It smells musty, rocky, and a bit grassy here. "));
				break;
			}
			break;
		case Room.DOMAIN_OUTDOORS_SWAMP:
			smell.append(L("It smells stinky and gassy here. "));
			break;
		}
		if(smell.length()>0)
			msg.source().tell(smell.toString());
	}

	protected void handleBeingRoomLookedAt(CMMsg msg)
	{
		final MOB mob=msg.source();
		final Session sess = mob.session();
		if(sess==null)
			return; // no need for monsters to build all this data

		final Room room=(Room)msg.target();
		int lookCode=LOOK_LONG;
		if(msg.targetMinor()!=CMMsg.TYP_EXAMINE)
			lookCode=(msg.sourceMessage()==null)?LOOK_BRIEFOK:LOOK_NORMAL;
		
		sess.setStat("ROOMLOOK", ""+room.hashCode()); // for gmcp/protocol notifications

		final StringBuilder finalLookStr=new StringBuilder("");
		boolean sysmsgs=mob.isAttributeSet(MOB.Attrib.SYSOPMSGS);
		final boolean compress=mob.isAttributeSet(MOB.Attrib.COMPRESS) || (CMath.bset(room.phyStats().sensesMask(), PhyStats.SENSE_ALWAYSCOMPRESSED));
		if(sysmsgs && (!CMSecurity.isAllowed(mob,room,CMSecurity.SecFlag.SYSMSGS)))
		{
			mob.setAttribute(MOB.Attrib.SYSOPMSGS,false);
			sysmsgs=false;
		}
		if(sysmsgs)
		{
			if(room.getArea()!=null)
				finalLookStr.append("^!Area  :^N "+room.getArea().Name()+"\n\r");
			final String rscName=room.myResource()>=0?RawMaterial.CODES.NAME(room.myResource()):"";
			final String domType;
			final StringBuilder domCond=new StringBuilder("");
			if((room.domainType()&Room.INDOORS)==0)
				domType=Room.DOMAIN_OUTDOOR_DESCS[room.domainType()];
			else
				domType=Room.DOMAIN_INDOORS_DESCS[CMath.unsetb(room.domainType(),Room.INDOORS)];
			final int climateType=room.getClimateType();
			if(CMath.bset(climateType, Places.CLIMASK_COLD))
				domCond.append(" cold");
			if(CMath.bset(climateType, Places.CLIMASK_WET))
				domCond.append(" wet");
			if(CMath.bset(climateType, Places.CLIMASK_HOT))
				domCond.append(" hot");
			if(CMath.bset(climateType, Places.CLIMASK_DRY))
				domCond.append(" dry");
			if(CMath.bset(climateType, Places.CLIMASK_WINDY))
				domCond.append(" windy");
			finalLookStr.append("^!RoomID:^N "+CMLib.map().getExtendedRoomID(room)+"\n\r"
								+ "^!"+room.ID()+"^N: "+domType+" "+domCond.toString()+" <"+rscName+"> "+room.basePhyStats().weight()+"mv \n\r");
		}
		if(CMLib.flags().canBeSeenBy(room,mob))
		{
			finalLookStr.append("^O^<RName^>" + room.displayText(mob)+"^</RName^>"+CMLib.flags().getDispositionBlurbs(room,mob)+"^L\n\r");
			if((lookCode!=LOOK_BRIEFOK)||(!mob.isAttributeSet(MOB.Attrib.BRIEF)))
			{
				String roomDesc=room.description(mob);
				if(lookCode==LOOK_LONG)
				{
					Vector<String> keyWords=null;
					String word=null;
					int x=0;
					for(int c=0;c<room.numItems();c++)
					{
						final Item item=room.getItem(c);
						if(item==null)
							continue;
						if((item.container()==null)
						&&(item.displayText(mob).length()==0)
						&&(CMLib.flags().canBeSeenBy(item,mob)))
						{
							keyWords=CMParms.parse(item.name().toUpperCase());
							for(int k=0;k<keyWords.size();k++)
							{
								word=keyWords.elementAt(k);
								x=roomDesc.toUpperCase().indexOf(word);
								while(x>=0)
								{
									if(((x<=0)
										||((!Character.isLetterOrDigit(roomDesc.charAt(x-1)))&&(roomDesc.charAt(x-1)!='>')))
									&&(((x+word.length())>=(roomDesc.length()-1))
										||((!Character.isLetterOrDigit(roomDesc.charAt((x+word.length()))))&&(roomDesc.charAt(x+word.length())!='^'))))
									{
										final int brackCheck=roomDesc.substring(x).indexOf("^>");
										final int brackCheck2=roomDesc.substring(x).indexOf("^<");
										if((brackCheck<0)||(brackCheck2<brackCheck))
										{
											int start=x;
											while((start>=0)&&(!Character.isWhitespace(roomDesc.charAt(start))))
												start--;
											start++;
											int end=(x+word.length());
											while((end<roomDesc.length())&&(!Character.isWhitespace(roomDesc.charAt(end))))
												end++;
											final int l=roomDesc.length();
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
				if((CMProps.getIntVar(CMProps.Int.EXVIEW)==CMProps.Int.EXVIEW_PARAGRAPH)
				||(CMProps.getIntVar(CMProps.Int.EXVIEW)==CMProps.Int.EXVIEW_MIXED))
					roomDesc += getRoomExitsParagraph(mob,room);
				finalLookStr.append("^L^<RDesc^>" + roomDesc+"^</RDesc^>");

				if((!mob.isMonster())&&(sess.getClientTelnetMode(Session.TELNET_MXP)))
					finalLookStr.append(CMLib.protocol().mxpImage(room," ALIGN=RIGHT H=70 W=70"));
				if(compress)
					finalLookStr.append("^N  ");
				else
					finalLookStr.append("^N\n\r\n\r");
			}
		}

		final Item notItem;
		final Room mobLocR=mob.location();
		if((mobLocR!=room)
		&&(mobLocR!=null)
		&&(mobLocR.getArea() instanceof BoardableShip))
			notItem=((BoardableShip)mobLocR.getArea()).getShipItem();
		else
			notItem=null;
		
		final List<Item> viewItems=new ArrayList<Item>(room.numItems());
		final List<Item> compressedItems=((compress) || (lookCode==LOOK_LONG)) ? null :  new ArrayList<Item>(1);
		int itemsInTheDarkness=0;
		for(int c=0;c<room.numItems();c++)
		{
			final Item item=room.getItem(c);
			if((item==null)||(item==notItem))
				continue;

			if(item.container()==null)
			{
				if(CMLib.flags().canBarelyBeSeenBy(item,mob))
					itemsInTheDarkness++;
				if((compressedItems!=null)&&(CMath.bset(item.phyStats().sensesMask(), PhyStats.SENSE_ALWAYSCOMPRESSED)))
					compressedItems.add(item);
				else
					viewItems.add(item);
			}
		}

		if((compressedItems != null) && (compressedItems.size()>0))
		{
			final StringBuilder itemStr=CMLib.lister().lister(mob,compressedItems,false,"RItem"," \"*\"",false,true);
			if(itemStr.length()>0)
				finalLookStr.append(itemStr).append("\n\r\n\r");
		}
		final StringBuilder itemStr=CMLib.lister().lister(mob,viewItems,false,"RItem"," \"*\"",lookCode==LOOK_LONG,compress);
		if(itemStr.length()>0)
			finalLookStr.append(itemStr);

		int mobsInTheDarkness=0;
		for(int i=0;i<room.numInhabitants();i++)
		{
			final MOB mob2=room.fetchInhabitant(i);
			if((mob2!=null)&&(mob2!=mob))
			{
				final String displayText=mob2.displayText(mob);
				if((displayText.length()>0)
				||(sysmsgs))
				{
					if(CMLib.flags().canBeSeenBy(mob2,mob))
					{
						if((!compress)&&(!mob.isMonster())&&(sess.getClientTelnetMode(Session.TELNET_MXP)))
							finalLookStr.append(CMLib.protocol().mxpImage(mob2," H=10 W=10",""," "));
						finalLookStr.append("^M^<RMob \""+CMStrings.removeColors(mob2.name())+"\"^>");
						if(compress)
							finalLookStr.append(CMLib.flags().getDispositionBlurbs(mob2,mob)+"^M ");
						if(displayText.length()>0)
							finalLookStr.append(CMStrings.endWithAPeriod(CMStrings.capitalizeFirstLetter(displayText)));
						else
							finalLookStr.append(CMStrings.endWithAPeriod(CMStrings.capitalizeFirstLetter(mob2.name())));
						finalLookStr.append("^</RMob^>");
						if(sysmsgs)
							finalLookStr.append("^H("+CMClass.classID(mob2)+")^N ");
						if(!compress)
							finalLookStr.append(CMLib.flags().getDispositionBlurbs(mob2,mob)+"^N\n\r");
						else
							finalLookStr.append("^N");
					}
					else
					if(CMLib.flags().canBarelyBeSeenBy(mob2,mob))
						mobsInTheDarkness++;
				}
			}
		}

		if(finalLookStr.length()==0)
			mob.tell(L("You can't see anything!"));
		else
		{
			if(compress)
				finalLookStr.append("\n\r");
			mob.tell(finalLookStr.toString());
			if((CMProps.getIntVar(CMProps.Int.AWARERANGE)>0)
			&&(!mob.isAttributeSet(MOB.Attrib.AUTOMAP)))
			{
				if(awarenessA==null)
					awarenessA=CMClass.getAbility("Skill_RegionalAwareness");
				if(awarenessA!=null)
				{
					sess.colorOnlyPrintln("", true);
					final Vector<String> list=new Vector<String>();
					awarenessA.invoke(mob, list, mobLocR, true, CMProps.getIntVar(CMProps.Int.AWARERANGE));
					for(final String o : list)
					{
						sess.setIdleTimers();
						sess.colorOnlyPrintln(o, true); // the zero turns off stack
					}
					sess.colorOnlyPrintln("\n\r", true);
				}
			}
			if(itemsInTheDarkness>0)
				mob.tell(L("      ^IThere is something here, but it's too dark to make out.^?\n\r"));
			if(mobsInTheDarkness>1)
				mob.tell(L("^MThe darkness conceals several others.^?\n\r"));
			else
			if(mobsInTheDarkness>0)
				mob.tell(L("^MYou are not alone, but it's too dark to tell.^?\n\r"));
		}
	}

	private static String getRoomExitsParagraph(MOB mob, Room room)
	{
		if(room == null || mob == null)
			return "";
		final StringBuilder str = new StringBuilder("");
		final Vector<Integer> exitDirs=new Vector<Integer>();
		Room R;
		Exit E;
		final boolean useShipNames=((room instanceof BoardableShip)||(room.getArea() instanceof BoardableShip));
		for(final int dir : Directions.CODES())
		{
			E = room.getExitInDir(dir);
			R = room.getRoomInDir(dir);
			if((R!=null)&&(E!=null)&&(CMLib.flags().canBeSeenBy(E,mob)))
				exitDirs.add(Integer.valueOf(dir));
		}
		final String title = room.displayText(mob);
		// do continues first
		final Vector<Integer> continues = new Vector<Integer>();
		for(int i=exitDirs.size()-1;i>=0;i--)
		{
			R = room.getRoomInDir(exitDirs.elementAt(i).intValue());
			if((R!=null)&&(R.displayText(mob).equalsIgnoreCase(title)))
				continues.addElement(exitDirs.remove(i));
		}

		if(continues.size()>0)
		{
			str.append("  ^L"+CMStrings.capitalizeFirstLetter(room.displayText(mob)).trim()+" continues ");
			if(continues.size()==1)
			{
				if(useShipNames)
					str.append(CMLib.directions().getShipInDirectionName(continues.firstElement().intValue())+".");
				else
					str.append(CMLib.directions().getInDirectionName(continues.firstElement().intValue())+".");
			}
			else
			{
				for(int i=0;i<continues.size()-1;i++)
				{
					if(useShipNames)
						str.append(CMLib.directions().getShipDirectionName(continues.elementAt(i).intValue()).toLowerCase().trim()+", ");
					else
						str.append(CMLib.directions().getDirectionName(continues.elementAt(i).intValue()).toLowerCase().trim()+", ");
				}
				if(useShipNames)
					str.append("and "+CMLib.directions().getShipInDirectionName(continues.lastElement().intValue()).trim()+".");
				else
					str.append("and "+CMLib.directions().getInDirectionName(continues.lastElement().intValue()).trim()+".");
			}
		}
		final boolean style=CMLib.dice().rollPercentage()>50;
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
		if(room==null)
			return "";
		final Exit exit = room.getExitInDir(dir);
		if(exit == null)
			return "";
		final boolean useShipDirs=(room instanceof BoardableShip)||(room.getArea() instanceof BoardableShip);
		final String inDirName=useShipDirs?CMLib.directions().getShipInDirectionName(dir):CMLib.directions().getInDirectionName(dir);
		if(style)
			return inDirName   + " is " +exit.viewableText(mob, room.getRoomInDir(dir));
		else
			return exit.viewableText(mob, room.getRoomInDir(dir)).toString().trim()   +" is " + inDirName;
	}

	@Override
	public boolean isHygienicMessage(final CMMsg msg, final int minHygiene, final long adjHygiene)
	{
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.HYGIENE))
			return false;
		if((msg.sourceMajor(CMMsg.MASK_MOVE)
		&&((msg.tool()==null)
			||(!(msg.tool() instanceof Ability))
			||((((Ability)msg.tool()).classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_COMMON_SKILL)))
		||((msg.tool() instanceof Social)
			&&((msg.tool().Name().toUpperCase().startsWith("BATHE"))
				||(msg.tool().Name().toUpperCase().startsWith("WASH")))))
				return (msg.source().playerStats()!=null)&&(msg.source().soulMate()==null);
		return false;
	}

	@Override
	public void handleHygienicMessage(final CMMsg msg, final int minHygiene, final long adjHygiene)
	{
		if(isHygienicMessage(msg,minHygiene,adjHygiene))
		{
			final MOB mob=msg.source();
			if(mob.playerStats().getHygiene()>(-adjHygiene))
			{
				mob.playerStats().adjHygiene(adjHygiene);
				if(mob.playerStats().getHygiene()>(PlayerStats.HYGIENE_DELIMIT/2))
					mob.tell(L("You feel a little cleaner, but are still very dirty."));
				else
				if(mob.playerStats().getHygiene()<1500)
					mob.tell(L("You feel a little cleaner; almost perfect."));
				else
					mob.tell(L("You feel a little cleaner."));
			}
			else
			if(adjHygiene==0)
				mob.tell(L("You are already perfectly clean."));
			else
			if((msg.sourceMinor()!=CMMsg.TYP_NOISYMOVEMENT)
			||(mob.riding()!=null)
			||(CMLib.flags().isWateryRoom(mob.location())))
			{
				final long h=mob.playerStats().getHygiene();
				if((h>4)
				&&(h<minHygiene)
				&&(msg.sourceMinor()!=CMMsg.TYP_LEAVE)
				&&((!CMLib.flags().isFlying(mob))
					||(mob.location().domainType()==Room.DOMAIN_INDOORS_UNDERWATER)
					||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_UNDERWATER)))
				{
					mob.playerStats().adjHygiene(-h);
					mob.tell(L("You can't get any cleaner here."));
				}
				else
					mob.playerStats().adjHygiene(-h);
			}
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
			final int domain=room.domainType();
			switch(domain)
			{
			case Room.DOMAIN_INDOORS_AIR:
			case Room.DOMAIN_INDOORS_UNDERWATER:
			case Room.DOMAIN_OUTDOORS_AIR:
			case Room.DOMAIN_OUTDOORS_UNDERWATER:
			{
				final int weather=room.getArea().getClimateObj().weatherType(room);
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
		final Exit exit=(Exit)msg.target();
		final MOB mob=msg.source();
		if(CMLib.flags().canBeSeenBy(exit,mob))
		{
			if(exit.description(mob).trim().length()>0)
				mob.tell(exit.description(mob));
			else
			if(mob.location()!=null)
			{
				Room room=null;
				int direction=-1;
				if(msg.tool() instanceof Room)
					room=(Room)msg.tool();
				else
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					if(mob.location().getExitInDir(d)==exit)
					{
						room=mob.location().getRoomInDir(d);
						break;
					}
				}
				if(room!=null)
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					if((mob.location().getRoomInDir(d)==room)
					&&((mob.location().getExitInDir(d)==exit)))
						direction=d;
				}
				mob.tell(exit.viewableText(mob,room).toString());
				if(isAClearExitView(mob,room,exit)&&(direction>=0)&&(room!=null))
				{
					List<Room> view=null;
					final Vector<Environmental> items=new Vector<Environmental>();
					if(room.getGridParent()!=null)
						view=room.getGridParent().getAllRooms();
					else
					{
						view=new Vector<Room>();
						view.add(room);
						for(int i=0;i<5;i++)
						{
							room=room.getRoomInDir(direction);
							if(room==null)
								break;
							final Exit E=room.getExitInDir(direction);
							if((isAClearExitView(mob,room,E)))
								view.add(room);
						}
					}
					for(int r=0;r<view.size();r++)
					{
						room=view.get(r);
						for(int i=0;i<room.numItems();i++)
						{
							final Item E=room.getItem(i);
							if(E!=null)
								items.add(E);
						}
						for(int i=0;i<room.numInhabitants();i++)
						{
							final MOB E=room.fetchInhabitant(i);
							if(E!=null)
								items.add(E);
						}
					}
					final StringBuilder seenThatWay=CMLib.lister().lister(msg.source(),items,true,"","",false,true);
					if(seenThatWay.length()>0)
						mob.tell(L("Yonder, you can also see: @x1",seenThatWay.toString()));
				}
			}
			else
				mob.tell(L("You don't see anything special."));
			if(mob.isAttributeSet(MOB.Attrib.SYSOPMSGS))
			{
				mob.tell(L("Type  : @x1",exit.ID()));
				mob.tell(L("Misc   : @x1",exit.text()));
			}
			final String image=CMLib.protocol().mxpImage(exit," ALIGN=RIGHT H=70 W=70");
			if((image!=null)&&(image.length()>0))
				mob.tell(image);
		}
		else
			mob.tell(L("You can't see that way!"));
	}

	protected void handleBeingMobLookedAt(CMMsg msg)
	{
		final MOB viewermob=msg.source();
		final MOB viewedmob=(MOB)msg.target();
		final boolean longlook=msg.targetMinor()==CMMsg.TYP_EXAMINE;
		final StringBuilder myDescription=new StringBuilder("");
		if(CMLib.flags().canBeSeenBy(viewedmob,viewermob))
		{
			if(viewermob.isAttributeSet(MOB.Attrib.SYSOPMSGS))
			{
				myDescription.append("\n\rType :"+viewedmob.ID()
									+"\n\rRejuv:"+viewedmob.basePhyStats().rejuv()
									+((!viewedmob.isMonster())?", Hunger="+viewedmob.curState().getHunger():"")
									+"\n\rAbile:"+viewedmob.basePhyStats().ability()
									+((!viewedmob.isMonster())?", Thirst="+viewedmob.curState().getThirst():"")
									+"\n\rLevel:"+viewedmob.basePhyStats().level()
									+"\n\rDesc : "+viewedmob.description()
									+"\n\rRoom :'"+((viewedmob.getStartRoom()==null)?"null":viewedmob.getStartRoom().roomID())
									+"\n\rMisc : "+viewedmob.text()
									+"\n\r");
			}
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
					myDescription.append(viewedmob.name(viewermob)+" the ");
					if((viewedmob.charStats().getStat(CharStats.STAT_AGE)>0)&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.ALL_AGEING)))
						myDescription.append(viewedmob.charStats().ageName().toLowerCase()+" ");
					myDescription.append(viewedmob.charStats().raceName());
				}
				else
					myDescription.append(viewedmob.name(viewermob)+" ");
				if(levelStr!=null)
					myDescription.append(" is "+levelStr+".\n\r");
				else
					myDescription.append("is here.\n\r");
			}
			if(viewedmob.phyStats().height()>0)
				myDescription.append(viewedmob.charStats().HeShe()+" is "+viewedmob.phyStats().height()+" inches tall and weighs "+viewedmob.basePhyStats().weight()+" pounds.\n\r");
			if((longlook)&&(viewermob.charStats().getStat(CharStats.STAT_INTELLIGENCE)>12))
			{
				final CharStats C=(CharStats)CMClass.getCommon("DefaultCharStats");
				final MOB testMOB=CMClass.getFactoryMOB();
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
				myDescription.append(CMLib.protocol().mxpImage(viewedmob," ALIGN=RIGHT H=70 W=70"));
			myDescription.append(viewedmob.healthText(viewermob)+"\n\r\n\r");
			myDescription.append(viewedmob.description(viewermob)+"\n\r\n\r");

			final StringBuilder eq=CMLib.commands().getEquipment(viewermob,viewedmob);
			if(eq.length() > 0)
			{
				if((CMProps.getIntVar(CMProps.Int.EQVIEW)>CMProps.Int.EQVIEW_MIXED)
				||((viewermob!=viewedmob)&&(CMProps.getIntVar(CMProps.Int.EQVIEW)>CMProps.Int.EQVIEW_DEFAULT)))
					myDescription.append(viewedmob.charStats().HeShe()+" is wearing "+eq.toString());
				else
					myDescription.append(viewedmob.charStats().HeShe()+" is wearing:\n\r"+eq.toString());
			}
			viewermob.tell(myDescription.toString());
			if(longlook)
			{
				final Command C=CMClass.getCommand("Consider");
				try
				{
					if (C != null)
						C.executeInternal(viewermob, 0, viewedmob);
				}
				catch (final java.io.IOException e)
				{
				}
			}
		}
	}

	@Override
	public void handleBeingGivenTo(CMMsg msg)
	{
		if(!(msg.target() instanceof MOB))
			return;
		final MOB givermob=msg.source();
		final MOB giveemob=(MOB)msg.target();
		if(giveemob.location()!=null)
		{
			CMMsg msg2=CMClass.getMsg(givermob,msg.tool(),null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_DROP|CMMsg.MASK_INTERMSG,null);
			giveemob.location().send(givermob,msg2);
			msg2=CMClass.getMsg((MOB)msg.target(),msg.tool(),null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null,CMMsg.MSG_GET|CMMsg.MASK_INTERMSG,null);
			giveemob.location().send(giveemob,msg2);
		}
	}

	@Override
	public void handleBeingRead(CMMsg msg)
	{
		if((msg.targetMessage()==null)||(!msg.targetMessage().equals("CANCEL")))
		{
			final MOB mob=msg.source();
			if(CMLib.flags().canBeSeenBy(msg.target(),mob))
			{
				String text=null;
				if((msg.target() instanceof Exit)&&(((Exit)msg.target()).isReadable()))
					text=((Exit)msg.target()).readableText();
				else
				if((msg.target() instanceof Item)&&(((Item)msg.target()).isReadable()))
				{
					final Item targetI=(Item)msg.target();
					text=targetI.readableText();
					if(((text==null)||(text.length()==0))
					&&(targetI.description(mob).length()>0)
					&&((targetI.displayText(mob).length()==0)||(!CMLib.flags().isGettable(targetI))))
						text=targetI.description(mob);
				}
				if((text!=null)
				&&(text.length()>0))
				{
					if(text.toUpperCase().startsWith("FILE="))
					{
						final StringBuffer buf=Resources.getFileResource(text.substring(5),true);
						if((buf!=null)&&(buf.length()>0))
							text=buf.toString();
						else
						if(msg.target() instanceof Electronics)
						{
							text=null;
							mob.tell(L("There is nothing on @x1.",((Electronics)msg.target()).name(mob)));
						}
						else
						if(msg.target() instanceof Physical)
						{
							text=null;
							mob.tell(L("There is nothing written on @x1.",((Physical)msg.target()).name(mob)));
						}
						else
						{
							text=null;
							mob.tell(L("There is nothing written on @x1.",msg.target().name()));
						}
					}
					if((text!=null)&&(text.length()>0))
					{
						CMMsg readMsg=CMClass.getMsg(msg.source(), msg.target(), msg.tool(), 
								 CMMsg.MSG_WASREAD|CMMsg.MASK_ALWAYS, L("It says '@x1'.",text),
								 CMMsg.MSG_WASREAD|CMMsg.MASK_ALWAYS, text, 
								 CMMsg.NO_EFFECT, null);
						msg.addTrailerMsg(readMsg);
					}
				}
				else
				if(msg.target() instanceof Electronics)
					mob.tell(L("There is nothing on @x1.",((Electronics)msg.target()).name(mob)));
				else
				if(msg.target() instanceof Physical)
					mob.tell(L("There is nothing written on @x1.",((Physical)msg.target()).name(mob)));
				else
					mob.tell(L("There is nothing written on @x1.",msg.target().name()));
			}
			else
				mob.tell(L("You can't see that!"));
		}
	}

	@Override
	public void handleBeingGetted(CMMsg msg)
	{
		if(!(msg.target() instanceof Item))
			return;
		final Item item=(Item)msg.target();
		final MOB mob=msg.source();
		final boolean isMine = mob.isMine(item);
		if(item instanceof Container)
		{
			if(msg.tool() instanceof Item)
			{
				final Item newitem=(Item)msg.tool();
				if(newitem.container()==item)
				{
					newitem.setContainer(null);
					newitem.unWear();
				}
			}
			else
			if(!isMine)
			{
				item.setContainer(null);
				mob.moveItemTo(item);
				if(!isMine)
					CMLib.achievements().possiblyBumpAchievement(mob, Event.GOTITEM, 1, item);
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
			if(!isMine)
			{
				mob.addItem(item);
				if(!isMine)
					CMLib.achievements().possiblyBumpAchievement(mob, Event.GOTITEM, 1, item);
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

	@Override
	public void handleBeingDropped(CMMsg msg)
	{
		if(!(msg.target() instanceof Item))
			return;
		final Item item=(Item)msg.target();
		final MOB mob=msg.source();
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
			CMLib.achievements().possiblyBumpAchievement(mob, Event.GOTITEM, -1, item);
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

	@Override
	public void handleBeingRemoved(CMMsg msg)
	{
		if(!(msg.target() instanceof Item))
			return;
		final Item item=(Item)msg.target();
		final MOB mob=msg.source();
		if(item instanceof Container)
		{
			handleBeingGetted(msg);
			return;
		}
		item.unWear();
		if(!CMath.bset(msg.targetMajor(),CMMsg.MASK_OPTIMIZE))
			mob.location().recoverRoomStats();
	}

	@Override
	public void handleBeingWorn(CMMsg msg)
	{
		if(!(msg.target() instanceof Item))
			return;
		final Item item=(Item)msg.target();
		final long wearLocation = (msg.value()<=0)?0:((long)(1<<msg.value())/2);
		final MOB mob=msg.source();
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

	@Override
	public void handleBeingWielded(CMMsg msg)
	{
		if(!(msg.target() instanceof Item))
			return;
		final Item item=(Item)msg.target();
		final MOB mob=msg.source();
		if(item.wearIfPossible(mob,Wearable.WORN_WIELD))
		{
			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.recoverMaxState();
		}
	}

	@Override
	public void handleBeingHeld(CMMsg msg)
	{
		if(!(msg.target() instanceof Item))
			return;
		final Item item=(Item)msg.target();
		final MOB mob=msg.source();
		if(item.wearIfPossible(mob,Wearable.WORN_HELD))
		{
			mob.recoverCharStats();
			mob.recoverPhyStats();
			mob.recoverMaxState();
		}
	}

	@Override
	public void lookAtExits(Room room, MOB mob)
	{
		if((mob==null)
		||(room==null)
		||(mob.isMonster()))
			return;

		if(!CMLib.flags().canSee(mob))
		{
			mob.tell(L("You can't see anything!"));
			return;
		}

		final boolean useShipNames=((room instanceof BoardableShip)||(room.getArea() instanceof BoardableShip));
		final StringBuilder buf=new StringBuilder("^DObvious exits:^.^N\n\r");
		String Dir=null;
		for(int d : Directions.DISPLAY_CODES())
		{
			final Exit exit=room.getExitInDir(d);
			final Room room2=room.getRoomInDir(d);
			StringBuilder Say=new StringBuilder("");
			if(exit!=null)
				Say=exit.viewableText(mob, room2);
			else
			if((room2!=null)&&(mob.isAttributeSet(MOB.Attrib.SYSOPMSGS)))
				Say.append(room2.roomID()+" via NULL");
			if(Say.length()>0)
			{
				Dir=CMStrings.padRightPreserve(useShipNames?CMLib.directions().getShipDirectionName(d):CMLib.directions().getDirectionName(d),5);
				if((mob.playerStats()!=null)
				&&(room2!=null)
				&&(mob.playerStats().hasVisited(room2)))
					buf.append("^D^<EX^>" + Dir+"^</EX^>:^.^N ^d"+Say+"^.^N\n\r");
				else
					buf.append("^U^<EX^>" + Dir+"^</EX^>:^.^N ^u"+Say+"^.^N\n\r");
			}
		}
		boolean noBoardableShips = false;
		if((mob.location() != room)
		&&(mob.location() != null)
		&&(mob.location().getArea()!=room.getArea())
		&&(mob.location().getArea() instanceof BoardableShip))
			noBoardableShips = true;
		
		Item I=null;
		for(int i=0;i<room.numItems();i++)
		{
			I=room.getItem(i);
			if((I instanceof Exit)
			&&(((Exit)I).doorName().length()>0)
			&&(I.container()==null)
			&&((!(I instanceof BoardableShip))||(!noBoardableShips)))
			{
				final StringBuilder Say=((Exit)I).viewableText(mob, room);
				if(Say.length()>0)
				{
					Say.append(CMLib.flags().getDispositionBlurbs(I, mob));
					if(Say.length()>5)
						buf.append("^D^<MEX^>" + ((Exit)I).doorName()+"^</MEX^>:^.^N ^d"+Say+"^.^N\n\r");
					else
						buf.append("^D^<MEX^>" + CMStrings.padRight(((Exit)I).doorName(),5)+"^</MEX^>:^.^N ^d"+Say+"^.^N\n\r");
				}
			}
		}
		mob.tell(buf.toString());
	}

	@Override
	public void lookAtExitsShort(Room room, MOB mob)
	{
		if((mob==null)||(room==null)||(mob.isMonster()))
			return;
		if(!CMLib.flags().canSee(mob))
			return;

		final boolean useShipNames=((room instanceof BoardableShip)||(room.getArea() instanceof BoardableShip));
		final StringBuilder buf=new StringBuilder(L("^D[Exits: "));
		for(int d : Directions.DISPLAY_CODES())
		{
			final Exit exit=room.getExitInDir(d);
			if((exit!=null)&&(exit.viewableText(mob, room.getRoomInDir(d)).length()>0))
				buf.append("^<EX^>"+(useShipNames?CMLib.directions().getShipDirectionName(d):CMLib.directions().getDirectionName(d))+"^</EX^> ");
		}
		boolean noBoardableShips = false;
		if((mob.location() != room)
		&&(mob.location() != null)
		&&(mob.location().getArea()!=room.getArea())
		&&(mob.location().getArea() instanceof BoardableShip))
			noBoardableShips = true;
		Item I=null;
		for(int i=0;i<room.numItems();i++)
		{
			I=room.getItem(i);
			if((I instanceof Exit)
			&&(I.container()==null)
			&&(((Exit)I).viewableText(mob, room).length()>0)
			&&((!(I instanceof BoardableShip))||(!noBoardableShips)))
				buf.append("^<MEX^>"+((Exit)I).doorName()+"^</MEX^> ");
		}
		mob.tell(buf.toString().trim()+"]^.^N");
	}
}
