package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Emoter extends ActiveTicker
{
	public String ID(){return "Emoter";}
	public Emoter()
	{
		minTicks=10;maxTicks=30;chance=50;
		tickReset();
	}
	public Behavior newInstance()
	{
		return new Emoter();
	}

	public void setParms(String newParms)
	{
		super.setParms(newParms);
		emotes=null;
	}

	protected Vector emotes=null;
	protected boolean broadcast=false;

	protected final static int EMOTE_VISUAL=0;
	protected final static int EMOTE_SOUND=1;
	protected final static int EMOTE_SMELL=2;
	protected int emoteType=0;

	private void setEmoteTypes(Vector V)
	{
		for(int v=V.size()-1;v>=0;v--)
		{
			String str=((String)V.elementAt(v)).toUpperCase();
			if(str.equals("BROADCAST"))
			{
				V.removeElementAt(v);
				broadcast=true;
			}
			else
			if(str.equals("NOBROADCAST"))
			{
				V.removeElementAt(v);
				broadcast=false;
			}
			else
			if(str.equals("VISUAL")||(str.equals("SIGHT")))
			{
				V.removeElementAt(v);
				emoteType=EMOTE_VISUAL;
			}
			else
			if(str.equals("AROMA")||(str.equals("SMELL")))
			{
				V.removeElementAt(v);
				emoteType=EMOTE_SMELL;
			}
			else
			if(str.equals("SOUND")||(str.equals("NOISE")))
			{
				V.removeElementAt(v);
				emoteType=EMOTE_SOUND;
			}
		}
	}
	
	private Vector parseEmotes()
	{
		if(emotes!=null) return emotes;
		broadcast=false;
		emoteType=EMOTE_VISUAL;
		emotes=new Vector();
		String newParms=getParms();
		char c=';';
		int x=newParms.indexOf(c);
		if(x<0){ c='/'; x=newParms.indexOf(c);}
		if(x>0)
		{
			String oldParms=newParms.substring(0,x);
			setEmoteTypes(Util.parse(oldParms));
			newParms=newParms.substring(x+1);
		}
		int defaultType=emoteType;
		boolean defaultBroadcast=broadcast;
		while(newParms.length()>0)
		{
			Vector thisEmoteV=new Vector();
			String thisEmote=newParms;
			x=newParms.indexOf(";");
			if(x<0)	
				newParms="";
			else
			{
				thisEmote=newParms.substring(0,x);
				newParms=newParms.substring(x+1);
			}
			if(thisEmote.trim().length()>0)
			{
				Vector V=Util.parse(thisEmote);
				emoteType=defaultType;
				broadcast=defaultBroadcast;
				setEmoteTypes(V);
				thisEmote=Util.combine(V,0);
				if(thisEmote.length()>0)
				{
					thisEmoteV.addElement(new Integer(emoteType));
					thisEmoteV.addElement(new Boolean(broadcast));
					thisEmoteV.addElement(thisEmote);
					emotes.addElement(thisEmoteV);
				}
			}
		}
		return emotes;
	}

	private void emoteHere(Room room, MOB emoter, Vector emote, boolean Wrapper)
	{
		if(room==null) return;
		FullMsg msg;
		Room oldLoc=emoter.location();
		if(emoter.location()!=room) emoter.setLocation(room);
		if(Wrapper)
		{
			msg=new FullMsg(emoter,null,CMMsg.MSG_EMOTE,"^E<S-NAME> "+(String)emote.elementAt(2)+"^?");
		}
		else
		{
			msg=new FullMsg(emoter,null,CMMsg.MSG_EMOTE,(String)emote.elementAt(2));
		}

		if(room.okMessage(emoter,msg))
		for(int i=0;i<room.numInhabitants();i++)
		{
			MOB M=room.fetchInhabitant(i);
			if((M!=null)&&(!M.isMonster()))
			switch(((Integer)emote.elementAt(0)).intValue())
			{
			case EMOTE_VISUAL:
				if(Sense.canBeSeenBy(emoter,M))	M.executeMsg(M,msg);
				break;
			case EMOTE_SOUND:
				if(Sense.canBeHeardBy(emoter,M)) M.executeMsg(M,msg);
				break;
			case EMOTE_SMELL:
				if(Sense.canSmell(M)) M.executeMsg(M,msg);
				break;
			}
		}
		if(oldLoc!=null) emoter.setLocation(oldLoc);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		parseEmotes();
		if((canAct(ticking,tickID))
		&&(emotes.size()>0)
		&&(!CommonStrings.isDisabled("EMOTERS")))
		{
			Vector emote=(Vector)emotes.elementAt(Dice.roll(1,emotes.size(),-1));
			MOB emoter=null;
			if(ticking instanceof Area)
			{
				emoter=CMClass.getMOB("StdMOB");
				for(Enumeration r=((Area)ticking).getMap();r.hasMoreElements();)
				{
					Room R=(Room)r.nextElement();
					emoteHere(R,emoter,emote,false);
				}
				return true;
			}
			if(ticking instanceof Room)
			{
				emoter=CMClass.getMOB("StdMOB");
				emoteHere((Room)ticking,emoter,emote,false);
				return true;
			}

			Room room=getBehaversRoom(ticking);
			if(room==null) return true;
			if(ticking instanceof MOB)
			{
				if(canFreelyBehaveNormal(ticking))
					emoter=(MOB)ticking;
			}
			else
			{
				if((ticking instanceof Item)&&(!Sense.isInTheGame((Item)ticking)))
					return true;
				
				emoter=CMClass.getMOB("StdMOB");
				MOB mob=getBehaversMOB(ticking);
				String name=ticking.name();
				if(ticking instanceof Environmental)
					name=((Environmental)ticking).name();
				if(mob!=null)
				{
					if(!Sense.isInTheGame(mob))
						emoter.setName(name+" carried by "+mob.name());
					else
						emoter=null;
				}
				else
					emoter.setName(name);
			}
			if(emoter==null) return true;
			
			emoteHere(room,emoter,emote,true);

			if(((Boolean)emote.elementAt(1)).booleanValue())
			{
				if(ticking instanceof MOB)
					emoter=CMClass.getMOB("StdMOB");
				for(int d=0;d<Directions.NUM_DIRECTIONS;d++)
				{
					Room R=room.getRoomInDir(d);
					Exit E=room.getExitInDir(d);
					if((R!=null)&&(E!=null)&&(E.isOpen()))
					{
						emoter.setName("something "+Directions.getInDirectionName(Directions.getOpDirectionCode(d)));
						emoteHere(R,emoter,emote,true);
					}
				}
			}
		}
		return true;
	}
}
