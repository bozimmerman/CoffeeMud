package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Sounder extends StdBehavior
{
	public String ID(){return "Sounder";}
	private int minTicks=23;
	private int maxTicks=23;
	protected int tickDown=(int)Math.round(Math.random()*(maxTicks-minTicks))+minTicks;
	protected int canImproveCode(){return Behavior.CAN_ITEMS|Behavior.CAN_MOBS|Behavior.CAN_ROOMS|Behavior.CAN_EXITS|Behavior.CAN_AREAS;}
	protected int[] triggers=null;
	protected String[] strings=null;
	protected static int UNDER_MASK=1023;
	protected static int TICK_MASK=65536;
	protected static int ROOM_MASK=32768;
	private CMMsg lastMsg=null;

	public Sounder()
	{
		minTicks=23;
		maxTicks=23;
		tickReset();
	}
	public Behavior newInstance()
	{
		return new Sounder();
	}

	protected void tickReset()
	{
		tickDown=(int)Math.round(Math.random()*(maxTicks-minTicks))+minTicks;
	}
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		Vector emote=Util.parseSemicolons(newParms,true);
		triggers=new int[emote.size()];
		strings=new String[emote.size()];

		if(emote.size()>0)
		{
			String s=(String)emote.firstElement();
			minTicks=23;
			minTicks=Util.getParmInt(newParms,"min",minTicks);
			maxTicks=23;
			maxTicks=Util.getParmInt(newParms,"max",maxTicks);
			if((minTicks!=23)||(maxTicks!=23))
				emote.removeElementAt(0);
			for(int v=0;v<emote.size();v++)
			{
				s=((String)emote.elementAt(v)).trim();
				s=Util.replaceAll(s,"$n","<S-NAME>");
				s=Util.replaceAll(s,"$e","<S-HE-SHE>");
				s=Util.replaceAll(s,"$s","<S-HIS-HER>");
				if(s.toUpperCase().startsWith("SOUND "))
				{
					s=s.substring(6).trim();
					int x=s.indexOf(" ");
					if(x<0) continue;
					String y=s.substring(0,x);
					if(!Util.isNumber(y)) continue;
					triggers[v]=TICK_MASK+Util.s_int(y);
					s="^E"+s.substring(x+1).trim()+"^?";
					strings[v]=s;
				}
				else
				if((s.toUpperCase().startsWith("GET ")))
				{
					triggers[v]=CMMsg.TYP_GET;
					strings[v]=s.substring(4).trim();
				}
				else
				if((s.toUpperCase().startsWith("GET_ROOM ")))
				{
					triggers[v]=CMMsg.TYP_GET|ROOM_MASK;
					strings[v]=s.substring(9).trim();
				}
				else
				if((s.toUpperCase().startsWith("EAT_ROOM ")))
				{
					triggers[v]=CMMsg.TYP_EAT|ROOM_MASK;
					strings[v]=s.substring(9).trim();
				}
				else
				if((s.toUpperCase().startsWith("EAT ")))
				{
					triggers[v]=CMMsg.TYP_EAT;
					strings[v]=s.substring(4).trim();
				}
				else
				if((s.toUpperCase().startsWith("SIT ")))
				{
					triggers[v]=CMMsg.TYP_SIT;
					strings[v]=s.substring(4).trim();
				}
				else
				if((s.toUpperCase().startsWith("SIT_ROOM ")))
				{
					triggers[v]=CMMsg.TYP_SIT|ROOM_MASK;
					strings[v]=s.substring(9).trim();
				}
				else
				if((s.toUpperCase().startsWith("DROP ")))
				{
					triggers[v]=CMMsg.TYP_DROP;
					strings[v]=s.substring(5).trim();
				}
				else
				if((s.toUpperCase().startsWith("DROP_ROOM ")))
				{
					triggers[v]=CMMsg.TYP_DROP|ROOM_MASK;
					strings[v]=s.substring(10).trim();
				}
				else
				if((s.toUpperCase().startsWith("WEAR ")))
				{
					triggers[v]=CMMsg.TYP_WEAR;
					strings[v]=s.substring(5).trim();
				}
				else
				if((s.toUpperCase().startsWith("WEAR_ROOM ")))
				{
					triggers[v]=CMMsg.TYP_WEAR|ROOM_MASK;
					strings[v]=s.substring(10).trim();
				}
				else
				if((s.toUpperCase().startsWith("DRINK ")))
				{
					triggers[v]=CMMsg.TYP_DRINK;
					strings[v]=s.substring(6).trim();
				}
				else
				if((s.toUpperCase().startsWith("DRINK_ROOM ")))
				{
					triggers[v]=CMMsg.TYP_DRINK|ROOM_MASK;
					strings[v]=s.substring(11).trim();
				}
				else
				if((s.toUpperCase().startsWith("MOUNT ")))
				{
					triggers[v]=CMMsg.TYP_MOUNT;
					strings[v]=s.substring(6).trim();
				}
				else
				if((s.toUpperCase().startsWith("MOUNT_ROOM ")))
				{
					triggers[v]=CMMsg.TYP_MOUNT|ROOM_MASK;
					strings[v]=s.substring(11).trim();
				}
				else
				if((s.toUpperCase().startsWith("REMOVE ")))
				{
					triggers[v]=CMMsg.TYP_REMOVE;
					strings[v]=s.substring(7).trim();
				}
				else
				if((s.toUpperCase().startsWith("REMOVE_ROOM ")))
				{
					triggers[v]=CMMsg.TYP_REMOVE|ROOM_MASK;
					strings[v]=s.substring(12).trim();
				}
				else
				if((s.toUpperCase().startsWith("PORTAL_ENTER ")))
				{
					triggers[v]=CMMsg.TYP_ENTER;
					strings[v]=s.substring(13).trim();
				}
				else
				if((s.toUpperCase().startsWith("PORTAL_ENTER_ROOM ")))
				{
					triggers[v]=CMMsg.TYP_ENTER|ROOM_MASK;
					strings[v]=s.substring(18).trim();
				}
				else
				if((s.toUpperCase().startsWith("PORTAL_EXIT ")))
				{
					triggers[v]=CMMsg.TYP_LEAVE;
					strings[v]=s.substring(12).trim();
				}
				else
				if((s.toUpperCase().startsWith("PORTAL_EXIT_ROOM ")))
				{
					triggers[v]=CMMsg.TYP_LEAVE|ROOM_MASK;
					strings[v]=s.substring(17).trim();
				}
			}
		}
		tickReset();
	}

	private void emoteHere(Room room, MOB emoter, String emote)
	{
		if(room==null) return;
		Room oldLoc=emoter.location();
		if(emoter.location()!=room) emoter.setLocation(room);
		FullMsg msg=new FullMsg(emoter,null,CMMsg.MSG_EMOTE,emote);
		if(room.okMessage(emoter,msg))
		{
			for(int i=0;i<room.numInhabitants();i++)
			{
				MOB M=room.fetchInhabitant(i);
				if((M!=null)
				&&(!M.isMonster())
				&&(Sense.canSenseMoving(emoter,M)))
					M.executeMsg(M,msg);
			}
		}
		if(oldLoc!=null)
			emoter.setLocation(oldLoc);
	}

	public void doEmote(Tickable ticking, String emote)
	{
		MOB emoter=null;
		emote=Util.replaceAll(emote,"$p",ticking.name());
		if(ticking instanceof Area)
		{
			emoter=CMClass.getMOB("StdMOB");
			emoter.setName(ticking.name());
			emoter.charStats().setStat(CharStats.GENDER,(int)'N');
			for(Enumeration r=((Area)ticking).getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				emoteHere(R,emoter,emote);
			}
		}
		else
		if(ticking instanceof Room)
		{
			emoter=CMClass.getMOB("StdMOB");
			emoter.setName(ticking.name());
			emoter.charStats().setStat(CharStats.GENDER,(int)'N');
			emoteHere((Room)ticking,emoter,emote);
		}
		else
		if(ticking instanceof MOB)
		{
			emoter=(MOB)ticking;
			emoteHere(((MOB)ticking).location(),emoter,emote);
		}
		else
		{
			Room R=getBehaversRoom(ticking);
			if(R!=null)
			{
				emoter=CMClass.getMOB("StdMOB");
				emoter.setName(ticking.name());
				emoter.charStats().setStat(CharStats.GENDER,(int)'N');
				emoteHere(R,emoter,emote);
			}
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(((--tickDown)<=0)
		&&((!(ticking instanceof MOB))||(canFreelyBehaveNormal(ticking))))
		{
			tickReset();
			for(int v=0;v<triggers.length;v++)
			if((Util.bset(triggers[v],TICK_MASK))
			&&(Dice.rollPercentage()<(triggers[v]&UNDER_MASK)))
			{
				doEmote(ticking,strings[v]);
				break;
			}
		}
		return true;
	}

	public void executeMsg(Environmental E, CMMsg msg)
	{
		// this will work because, for items, behaviors
		// get the first tick.
		int lookFor=-1;
		if(msg!=lastMsg)
		switch(msg.targetMinor())
		{
		case CMMsg.TYP_GET:
		case CMMsg.TYP_REMOVE:
		case CMMsg.TYP_WEAR:
		case CMMsg.TYP_HOLD:
		case CMMsg.TYP_WIELD:
		case CMMsg.TYP_EAT:
		case CMMsg.TYP_DRINK:
		case CMMsg.TYP_SIT:
		case CMMsg.TYP_SLEEP:
		case CMMsg.TYP_MOUNT:
			if((msg.target()==E)||(!(E instanceof Item)))
				lookFor=msg.targetMinor();
			break;
		case CMMsg.TYP_DROP:
			if(((!(E instanceof Item))||(msg.target()==E))
			&&(msg.target() instanceof Item))
				lookFor=CMMsg.TYP_DROP;
			break;
		case CMMsg.TYP_ENTER:
			if((msg.target()!=null)
			&&(msg.target()==getBehaversRoom(E)))
				lookFor=CMMsg.TYP_ENTER;
			break;
		case CMMsg.TYP_LEAVE:
			if((msg.target()!=null)
			&&(msg.target()==getBehaversRoom(E)))
				lookFor=CMMsg.TYP_LEAVE;
			break;
		}
		lastMsg=msg;
		Room room=msg.source().location();
		if((lookFor>=0)
		&&(room!=null)
		&&((!(E instanceof MOB))||(canFreelyBehaveNormal((MOB)E))))
		for(int v=0;v<triggers.length;v++)
			if(((triggers[v]&UNDER_MASK)==lookFor)
			&&(!Util.bset(triggers[v],TICK_MASK)))
			{
				if(Util.bset(triggers[v],ROOM_MASK))
				{
					FullMsg msg2=new FullMsg(msg.source(),null,null,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,CMMsg.MSG_EMOTE,Util.replaceAll(strings[v],"$p",E.name()));
					msg.addTrailerMsg(msg2);
				}
				else
				{
					FullMsg msg2=new FullMsg(msg.source(),null,null,CMMsg.MSG_EMOTE,CMMsg.NO_EFFECT,CMMsg.NO_EFFECT,Util.replaceAll(strings[v],"$p",E.name()));
					msg.addTrailerMsg(msg2);
				}
			}
		super.executeMsg(E,msg);
	}
}
