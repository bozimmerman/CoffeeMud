package com.planet_ink.coffee_mud.utils;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import java.util.*;
import java.io.IOException;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class CommonMsgs
{
	private CommonMsgs(){};

	public static boolean doStandardCommand(MOB mob, String command, Vector parms)
	{
		try
		{
			Command C=CMClass.getCommand(command);
			if(C!=null)
				return C.execute(mob,parms);
		}
		catch(IOException e)
		{
			Log.errOut("CommonMsgs",e);
		}
		return false;
	}

	public static StringBuffer getScore(MOB mob)
	{
		Vector V=new Vector();
		doStandardCommand(mob,"Score",V);
		if((V.size()==1)&&(V.firstElement() instanceof StringBuffer))
			return (StringBuffer)V.firstElement();
		return new StringBuffer("");
	}
	public static StringBuffer getEquipment(MOB viewer, MOB mob)
	{
		Vector V=new Vector();
		V.addElement(viewer);
		doStandardCommand(mob,"Equipment",V);
		if((V.size()>1)&&(V.elementAt(1) instanceof StringBuffer))
			return (StringBuffer)V.elementAt(1);
		return new StringBuffer("");
	}
	public static StringBuffer getInventory(MOB viewer, MOB mob)
	{
		Vector V=new Vector();
		V.addElement(viewer);
		doStandardCommand(mob,"Inventory",V);
		if((V.size()>1)&&(V.elementAt(1) instanceof StringBuffer))
			return (StringBuffer)V.elementAt(1);
		return new StringBuffer("");
	}
	
	public static void channel(MOB mob, 
							   String channelName, 
							   String message, 
							   boolean systemMsg)
	{
		doStandardCommand(mob,"Channel",
						  Util.makeVector(new Boolean(systemMsg),channelName,message));
	}
	
	private static MOB talker=null;
	public static void channel(String channelName, 
							   String clanID, 
							   String message, 
							   boolean systemMsg)
	{
		
		if(talker==null)
		{
			talker=CMClass.getMOB("StdMOB");
			talker.setName("^?");
			talker.setLocation(CMMap.getRandomRoom());
			talker.baseEnvStats().setDisposition(EnvStats.IS_GOLEM);
			if(talker==null) return;
		}
		talker.setClanID(clanID);
		channel(talker,channelName,message,systemMsg);
	}

	public static boolean drop(MOB mob, Environmental dropThis, boolean quiet, boolean optimized)
	{
		return doStandardCommand(mob,"Drop",Util.makeVector(dropThis,new Boolean(quiet),new Boolean(optimized)));
	}
	public static boolean get(MOB mob, Item container, Item getThis, boolean quiet)
	{
		if(container==null)
			return doStandardCommand(mob,"Get",Util.makeVector(getThis,new Boolean(quiet)));
		else
			return doStandardCommand(mob,"Get",Util.makeVector(getThis,container,new Boolean(quiet)));
	}
	
	public static boolean remove(MOB mob, Item item, boolean quiet)
	{
		if(quiet)
			return doStandardCommand(mob,"Remove",Util.makeVector("REMOVE",item,"QUIETLY"));
		else
			return doStandardCommand(mob,"Remove",Util.makeVector("REMOVE",item));
	}
	
	public static void look(MOB mob, boolean quiet)
	{
		if(quiet)
			doStandardCommand(mob,"Look",Util.makeVector("LOOK","UNOBTRUSIVELY"));
		else
			doStandardCommand(mob,"Look",Util.makeVector("LOOK"));
	}

	public static void flee(MOB mob, String whereTo)
	{
		doStandardCommand(mob,"Flee",Util.makeVector("FLEE",whereTo));
	}

	public static void sheath(MOB mob, boolean ifPossible)
	{
		if(ifPossible)
			doStandardCommand(mob,"Sheath",Util.makeVector("SHEATH","IFPOSSIBLE"));
		else
			doStandardCommand(mob,"Sheath",Util.makeVector("SHEATH"));
	}
	
	public static void draw(MOB mob, boolean doHold, boolean ifNecessary)
	{
		if(ifNecessary)
		{
			if(doHold)
				doStandardCommand(mob,"Draw",Util.makeVector("DRAW","HELD","IFNECESSARY"));
			else
				doStandardCommand(mob,"Draw",Util.makeVector("DRAW","IFNECESSARY"));
		}
		else
			doStandardCommand(mob,"Draw",Util.makeVector("DRAW"));
	}
	
	public static void stand(MOB mob, boolean ifNecessary)
	{
		if(ifNecessary)
			doStandardCommand(mob,"Stand",Util.makeVector("STAND","IFNECESSARY"));
		else
			doStandardCommand(mob,"Stand",Util.makeVector("STAND"));
	}

	public static void follow(MOB follower, MOB leader, boolean quiet)
	{
		if(leader!=null)
		{
			if(quiet)
				doStandardCommand(follower,"Follow",Util.makeVector("FOLLOW",leader,"UNOBTRUSIVELY"));
			else
				doStandardCommand(follower,"Follow",Util.makeVector("FOLLOW",leader));
		}
		else
		{
			if(quiet)
				doStandardCommand(follower,"Follow",Util.makeVector("FOLLOW","SELF","UNOBTRUSIVELY"));
			else
				doStandardCommand(follower,"Follow",Util.makeVector("FOLLOW","SELF"));
		}
	}

	public static void say(MOB mob,
						   MOB target,
						   String text,
						   boolean isPrivate,
						   boolean tellFlag)
	{
		Room location=mob.location();
		text=CommonStrings.applyFilter(text,CommonStrings.SYSTEM_SAYFILTER);
		if(target!=null)
			location=target.location();
		if(location==null) return;
		if((isPrivate)&&(target!=null))
		{
			if(tellFlag)
			{
				String targetName=target.name();
				if(targetName.indexOf("@")>=0)
				{
					String mudName=targetName.substring(targetName.indexOf("@")+1);
					targetName=targetName.substring(0,targetName.indexOf("@"));
					if((!(CMClass.I3Interface().i3online()))&&(!(CMClass.I3Interface().imc2online())))
						mob.tell("Intermud is unavailable.");
					else
						CMClass.I3Interface().i3tell(mob,targetName,mudName,text);
				}
				else
				{
					boolean ignore=((target.playerStats()!=null)&&(target.playerStats().getIgnored().contains(mob.Name())));
					FullMsg msg=new FullMsg(mob,target,null,CMMsg.MSG_TELL,"^tYou tell "+target.name()+" '"+text+"'^?^.",CMMsg.MSG_TELL,"^t"+mob.Name()+" tell(s) you '"+text+"'^?^.",CMMsg.NO_EFFECT,null);
					if((mob.location().okMessage(mob,msg))
					&&((ignore)||(target.okMessage(target,msg))))
					{
						mob.executeMsg(mob,msg);
						if((mob!=target)&&(!ignore))
						{
							target.executeMsg(target,msg);
							if(msg.trailerMsgs()!=null)
							{
								for(int i=0;i<msg.trailerMsgs().size();i++)
								{
									CMMsg msg2=(CMMsg)msg.trailerMsgs().elementAt(i);
									if((msg!=msg2)&&(target.okMessage(target,msg2)))
										target.executeMsg(target,msg2);
								}
								msg.trailerMsgs().clear();
							}
							if(mob.playerStats()!=null)
							{
								mob.playerStats().setReplyTo(target);
								mob.playerStats().addTellStack(msg.sourceMessage());
							}
							if(target.playerStats()!=null)
							{
								target.playerStats().setReplyTo(mob);
								target.playerStats().addTellStack(msg.targetMessage());
							}
						}
					}
				}
			}
			else
			{
				FullMsg msg=new FullMsg(mob,target,null,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) '"+text+"'"+((target==null)?"":" to <T-NAMESELF>.^?"),CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) '"+text+"'"+((target==null)?"":" to <T-NAMESELF>.^?"),CMMsg.NO_EFFECT,null);
				if(location.okMessage(mob,msg))
					location.send(mob,msg);
			}
		}
		else
		if(!isPrivate)
		{
			FullMsg msg=new FullMsg(mob,target,null,CMMsg.MSG_SPEAK,"^T<S-NAME> say(s) '"+text+"'"+((target==null)?"":" to <T-NAMESELF>.^?"));
			if(location.okMessage(mob,msg))
				location.send(mob,msg);
		}
	}
}
