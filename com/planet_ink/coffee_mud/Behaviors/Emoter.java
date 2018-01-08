package com.planet_ink.coffee_mud.Behaviors;
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

/*
   Copyright 2001-2018 Bo Zimmerman

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

public class Emoter extends ActiveTicker
{
	@Override
	public String ID()
	{
		return "Emoter";
	}

	protected int expires=0;
	public Emoter()
	{
		super();
		minTicks=10;maxTicks=30;chance=50;expires=0;
		tickReset();
	}

	@Override
	public void setParms(String newParms)
	{
		super.setParms(newParms);
		expires=CMParms.getParmInt(parms,"expires",0);
		inroomIDs=CMParms.parseCommas(CMParms.getParmStr(parms,"inroom","").toUpperCase().trim(),true);
		emotes=null;
		smells=null;
	}

	protected List<EmoteObj> emotes=null;
	protected List<EmoteObj> smells=null;
	protected boolean broadcast=false;
	protected List<String> inroomIDs=new XVector<String>(0,true);

	protected static class EmoteObj
	{
		public EMOTE_TYPE type;
		public String msg;
		public boolean broadcast;
		public EmoteObj(EMOTE_TYPE type, String msg, boolean broadcast)
		{
			this.type=type;
			this.msg=msg;
			this.broadcast=broadcast;
		}
	}

	protected static enum EMOTE_TYPE { EMOTE_VISUAL, EMOTE_SOUND, EMOTE_SMELL, EMOTE_SOCIAL }
	protected EMOTE_TYPE emoteType=EMOTE_TYPE.EMOTE_VISUAL;

	@Override
	public String accountForYourself()
	{
		return "emoting";
	}

	protected boolean setEmoteType(String str)
	{
		str=str.toUpperCase().trim();
		if(str.equals("BROADCAST"))
			broadcast=true;
		else
		if(str.equals("NOBROADCAST"))
			broadcast=false;
		else
		if(str.equals("VISUAL")||(str.equals("SIGHT")))
			emoteType=EMOTE_TYPE.EMOTE_VISUAL;
		else
		if(str.equals("AROMA")||(str.equals("SMELL")))
			emoteType=EMOTE_TYPE.EMOTE_SMELL;
		else
		if(str.equals("SOUND")||(str.equals("NOISE")))
			emoteType=EMOTE_TYPE.EMOTE_SOUND;
		else
		if(str.equals("SOCIAL"))
			emoteType=EMOTE_TYPE.EMOTE_SOCIAL;
		else
			return false;
		return true;
	}

	protected void setEmoteTypes(Vector<String> V, boolean respectOnlyBeginningAndEnd)
	{
		if(respectOnlyBeginningAndEnd)
		{
			if(setEmoteType(V.firstElement()))
				V.removeElementAt(0);
			else
			if(setEmoteType(V.lastElement()))
				V.removeElementAt(V.size()-1);
		}
		else
		for(int v=V.size()-1;v>=0;v--)
		{
			if(setEmoteType(V.elementAt(v)))
				V.removeElementAt(v);
		}
	}

	protected List<EmoteObj> parseEmotes()
	{
		if(emotes!=null)
			return emotes;
		broadcast=false;
		emoteType=EMOTE_TYPE.EMOTE_VISUAL;
		emotes=new Vector<EmoteObj>();
		String newParms=getParms();
		char c=';';
		int x=newParms.indexOf(c);
		if(x<0)
		{
			c='/';
			x=newParms.indexOf(c);
		}
		if(x>0)
		{
			final String oldParms=newParms.substring(0,x);
			setEmoteTypes(CMParms.parse(oldParms),false);
			newParms=newParms.substring(x+1);
		}
		final EMOTE_TYPE defaultType=emoteType;
		final boolean defaultBroadcast=broadcast;
		while(newParms.length()>0)
		{
			String thisEmote=newParms;
			x=newParms.indexOf(';');
			if(x<0)
				newParms="";
			else
			{
				thisEmote=newParms.substring(0,x);
				newParms=newParms.substring(x+1);
			}
			if(thisEmote.trim().length()>0)
			{
				final Vector<String> V=CMParms.parse(thisEmote);
				emoteType=defaultType;
				broadcast=defaultBroadcast;
				setEmoteTypes(V,true);
				thisEmote=CMParms.combine(V,0);
				if(thisEmote.length()>0)
				{
					if(emoteType==EMOTE_TYPE.EMOTE_SMELL)
					{
						if(smells==null)
							smells=new Vector<EmoteObj>();
						smells.add(new EmoteObj(emoteType,thisEmote,broadcast));
					}
					emotes.add(new EmoteObj(emoteType,thisEmote,broadcast));
				}
			}
		}
		return emotes;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.amITarget(myHost))
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(CMLib.flags().canSmell(msg.source()))
		&&(smells!=null))
		{
			final EmoteObj emote=smells.get(CMLib.dice().roll(1,smells.size(),-1));
			MOB emoter=null;
			if(myHost instanceof Room)
			{
				emoter=CMClass.getFactoryMOB();
				emoteHere((Room)myHost,emoter,emote,msg.source(),false);
				emoter.destroy();
				return;
			}
			final Room room=getBehaversRoom(myHost);
			if(room!=null)
			{
				if(myHost instanceof MOB)
				{
					emoter=(MOB)myHost;
					emoteHere(room,emoter,emote,null,true);
				}
				else
				{
					if((myHost instanceof Item)
					&&(!CMLib.flags().isInTheGame((Item)myHost,false)))
						return;
					emoter=CMClass.getFactoryMOB();
					emoter.setName(myHost.name());
					emoteHere(room,emoter,emote,null,true);
					emoter.destroy();
				}
			}
		}
	}

	protected boolean inRoom(final Room room)
	{
		if(inroomIDs.size()>0)
		{
			final String ID=CMLib.map().getExtendedRoomID(room).toUpperCase();
			if(ID.length()==0)
				return false;
			if(inroomIDs.contains(ID))
				return true;
			for(final String roomID : inroomIDs)
			{
				if(ID.endsWith(roomID))
					return true;
			}
			return false;
		}
		return true;
	}

	protected void emoteHere(Room room,
							 MOB emoter,
							 EmoteObj emote,
							 MOB emoteTo,
							 boolean Wrapper)
	{
		if(room==null)
			return;
		if(!inRoom(room))
			return;
		CMMsg msg;
		final Room oldLoc=emoter.location();
		String str=emote.msg;
		if(emoter.location()!=room)
			emoter.setLocation(room);
		if(emote.type==EMOTE_TYPE.EMOTE_SOCIAL)
		{
			Social S=CMLib.socials().fetchSocial(str,true);
			if(S==null)
				S=CMLib.socials().fetchSocial(str,false);
			if(S!=null)
			{
				S.invoke(emoter,CMParms.parse(str),emoteTo,false);
				return;
			}
		}
		if(Wrapper)
			str="^E<S-NAME> "+str+" ^?";
		if(emoteTo!=null)
		{
			emoteTo.tell(emoter,emoteTo,null,str);
			return;
		}
		msg=CMClass.getMsg(emoter,null,CMMsg.MSG_EMOTE,str);
		if(room.okMessage(emoter,msg))
		{
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB M=room.fetchInhabitant(i);
				if((M!=null)&&(!M.isMonster()))
					switch(emote.type)
					{
					case EMOTE_VISUAL:
						if(CMLib.flags().canBeSeenBy(emoter,M))
							M.executeMsg(M,msg);
						break;
					case EMOTE_SOUND:
						if(CMLib.flags().canBeHeardSpeakingBy(emoter,M))
							M.executeMsg(M,msg);
						break;
					case EMOTE_SMELL:
						if(CMLib.flags().canSmell(M))
							M.executeMsg(M,msg);
						break;
					case EMOTE_SOCIAL:
						// handled above
						break;
					}
			}
		}
		if(oldLoc!=null)
			emoter.setLocation(oldLoc);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		parseEmotes();
		if((canAct(ticking,tickID))
		&&(emotes.size()>0)
		&&(!CMSecurity.isDisabled(CMSecurity.DisFlag.EMOTERS)))
		{
			if((expires>0)&&((--expires)==0))
			{
				if(ticking instanceof PhysicalAgent)
					((PhysicalAgent)ticking).delBehavior(this);
				return false;
			}
			final EmoteObj emote=emotes.get(CMLib.dice().roll(1,emotes.size(),-1));
			MOB emoter=null;
			if(ticking instanceof Area)
			{
				emoter=CMClass.getFactoryMOB();
				for(final Enumeration<Room> r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					emoteHere(R,emoter,emote,null,false);
				}
				emoter.destroy();
				return true;
			}
			if(ticking instanceof Room)
			{
				emoter=CMClass.getFactoryMOB();
				emoteHere((Room)ticking,emoter,emote,null,false);
				emoter.destroy();
				return true;
			}

			final Room room=getBehaversRoom(ticking);
			if(room==null)
				return true;
			boolean killEmoter=false;
			if(ticking instanceof MOB)
			{
				if(canFreelyBehaveNormal(ticking))
					emoter=(MOB)ticking;
			}
			else
			{
				if((ticking instanceof Item)&&(!CMLib.flags().isInTheGame((Item)ticking,false)))
					return true;

				emoter=CMClass.getFactoryMOB();
				killEmoter=true;
				final MOB mob=getBehaversMOB(ticking);
				String name=ticking.name();
				if(ticking instanceof Environmental)
					name=((Environmental)ticking).name();
				if(mob!=null)
				{
					if(CMLib.flags().isInTheGame(mob,false))
						emoter.setName(L("@x1 carried by @x2",name,mob.name()));
					else
						emoter=null;
				}
				else
					emoter.setName(name);
			}
			if(emoter==null)
				return true;
			emoteHere(room,emoter,emote,null,true);

			if(emote.broadcast)
			{
				if(ticking instanceof MOB)
				{
					emoter=CMClass.getFactoryMOB();
					killEmoter=true;
				}
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room R=room.getRoomInDir(d);
					final Exit E=room.getExitInDir(d);
					if((R!=null)&&(E!=null)&&(E.isOpen()))
					{
						final String inDir=((R instanceof BoardableShip)||(R.getArea() instanceof BoardableShip))?
								CMLib.directions().getShipInDirectionName(Directions.getOpDirectionCode(d)):
									CMLib.directions().getInDirectionName(Directions.getOpDirectionCode(d));
						emoter.setName(L("something @x1",inDir));
						emoteHere(R,emoter,emote,null,true);
					}
				}
			}
			if(killEmoter)
				emoter.destroy();
		}
		return true;
	}
}

