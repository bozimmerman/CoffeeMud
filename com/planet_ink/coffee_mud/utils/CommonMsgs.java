package com.planet_ink.coffee_mud.common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;

public class CommonMsgs
{
	private CommonMsgs(){};
	
	public static void say(MOB mob, 
						   MOB target, 
						   String text, 
						   boolean isPrivate, 
						   boolean tellFlag)
	{
		Room location=mob.location();
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
					if(!(ExternalPlay.i3().i3online()))
						mob.tell("I3 is unavailable.");
					else
						ExternalPlay.i3().i3tell(mob,targetName,mudName,text);
				}
				else
				{
					boolean ignore=((target.playerStats()!=null)&&(target.playerStats().getIgnored().containsKey(mob.Name())));
					FullMsg msg=new FullMsg(mob,target,null,CMMsg.MSG_TELL,"^TYou tell "+target.name()+" '"+text+"'^?^.",CMMsg.MSG_TELL,"^T"+mob.name()+" tell(s) you '"+text+"'^?^.",CMMsg.NO_EFFECT,null);
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
								mob.playerStats().setReplyTo(target);
							if(target.playerStats()!=null)
								target.playerStats().setReplyTo(mob);
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
