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
import com.planet_ink.coffee_mud.Libraries.interfaces.MaskingLibrary.CompiledZMask;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/*
   Copyright 2001-2025 Bo Zimmerman

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

	protected int				expires		= 0;
	protected List<EmoteObj>	emotes		= null;
	protected List<EmoteObj>	smells		= null;
	protected List<EmoteObj>	puffs		= null;
	protected List<String>		inroomIDs	= new XVector<String>(0, true);
	protected CompiledZMask		mask		= null;

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
		emotes=null;
		mask=null;
		inroomIDs.clear();
		smells=null;
		puffs=null;
		final EmoteObj flags = new EmoteObj();
		emotes=new Vector<EmoteObj>();
		char c=';';
		String firstParms = newParms;
		int x=newParms.indexOf(c);
		if(x<0)
		{
			c='/';
			x=newParms.indexOf(c); // try another char?
		}

		if(x>0)
		{
			firstParms=newParms.substring(0,x);
			setEmoteTypes(CMParms.parse(firstParms),flags,false);
			newParms=newParms.substring(x+1);
		}
		expires=CMParms.getParmInt(firstParms,"expires",0);
		inroomIDs=CMParms.parseCommas(CMParms.getParmStr(firstParms,"inroom","").toUpperCase().trim(),true);
		mask = null;
		final String maskStr = CMParms.getParmStr(firstParms, "MASK", null);
		if(maskStr != null)
			this.mask=CMLib.masking().maskCompile(maskStr);

		for(String thisEmote : CMParms.parseAny(newParms,c,true))
		{
			if(thisEmote.trim().length()==0)
				continue;
			final Vector<String> V=CMParms.parse(thisEmote);
			final int oldSize = V.size();
			final EmoteObj localFlags = flags.clone(null);
			setEmoteTypes(V,localFlags,true);
			if(V.size() != oldSize)
				thisEmote=CMParms.combine(V,0);
			if(thisEmote.length()>0)
			{
				if(localFlags.type==EMOTE_TYPE.EMOTE_SMELL)
				{
					if(smells==null)
						smells=new Vector<EmoteObj>();
					final EmoteObj newObj = localFlags.clone(thisEmote);
					smells.add(newObj);
				}
				if(localFlags.type==EMOTE_TYPE.EMOTE_PUFF)
				{
					if(puffs==null)
						puffs=new Vector<EmoteObj>();
					final EmoteObj newObj = localFlags.clone(thisEmote);
					puffs.add(newObj);
				}
				else
				{
					final EmoteObj newObj = localFlags.clone(thisEmote);
					emotes.add(newObj);
				}
			}
		}
	}

	protected static class EmoteObj implements Cloneable
	{
		public boolean broadcast=false;
		public boolean emoteOnly=false;
		public boolean privateE=false;
		public String mask = null;
		public MaskingLibrary.CompiledZMask zmask = null;
		public EMOTE_TYPE type=EMOTE_TYPE.EMOTE_VISUAL;
		public String msg = "";
		public EmoteObj() {}
		public EmoteObj clone(final String msg)
		{
			try
			{
				final EmoteObj obj=(EmoteObj)super.clone();
				obj.msg=msg;
				if(obj.mask != null)
					obj.zmask=CMLib.masking().maskCompile(mask);
				else
					obj.zmask=null;
				return obj;
			}
			catch (final CloneNotSupportedException e)
			{
				return this;
			}
		}
	}

	protected static enum EMOTE_TYPE
	{
		EMOTE_VISUAL,
		EMOTE_SOUND,
		EMOTE_SMELL,
		EMOTE_SOCIAL,
		EMOTE_PUFF
	}

	@Override
	public String accountForYourself()
	{
		return "emoting";
	}

	protected boolean setEmoteType(final String str,final EmoteObj flags)
	{
		switch(str.toUpperCase().trim())
		{
		case "BROADCAST":
			flags.broadcast=true;
			break;
		case "NOBROADCAST":
			flags.broadcast=false;
			break;
		case "PUFF":
			flags.type = EMOTE_TYPE.EMOTE_PUFF;
			break;
		case "PRIVATE":
			flags.privateE=true;
			break;
		case "NOPRIVATE":
			flags.privateE=false;
			break;
		case "EMOTEONLY":
			flags.emoteOnly=true;
			break;
		case "VISUAL":
		case "SIGHT":
			flags.type=EMOTE_TYPE.EMOTE_VISUAL;
			break;
		case "AROMA":
		case "SMELL":
			flags.type=EMOTE_TYPE.EMOTE_SMELL;
			break;
		case "SOUND":
		case "NOISE":
			flags.type=EMOTE_TYPE.EMOTE_SOUND;
			break;
		case "SOCIAL":
			flags.type=EMOTE_TYPE.EMOTE_SOCIAL;
			break;
		default:
			return false;
		}
		return true;
	}

	protected void setEmoteTypes(final Vector<String> V, final EmoteObj flags, final boolean respectOnlyBeginningAndEnd)
	{
		if(respectOnlyBeginningAndEnd)
		{
			if(setEmoteType(V.firstElement(),flags))
				V.removeElementAt(0);
			else
			if(setEmoteType(V.lastElement(),flags))
				V.removeElementAt(V.size()-1);
		}
		else
		for(int v=V.size()-1;v>=0;v--)
		{
			if(setEmoteType(V.elementAt(v),flags))
				V.removeElementAt(v);
		}
	}

	public boolean pufferCheck(final Environmental myHost, final CMMsg msg)
	{
		if((msg.source() == myHost)||(msg.target() == myHost))
			return true;
		if ((myHost instanceof Room) || (myHost instanceof Area))
			return true;
		if(myHost instanceof Item)
		{
			final Item I = (Item) myHost;
			if((I.owner() == myHost)&&(I.amBeingWornProperly()))
				return true;
			if((I instanceof RawMaterial) && (I.container() == myHost))
				return true;
		}
		return false;
	}

	public void pickedEmote(final List<EmoteObj> obj, final Environmental myHost, final CMMsg msg)
	{
		final EmoteObj emote=obj.get(CMLib.dice().roll(1,obj.size(),-1));
		MOB emoter=null;
		if(myHost instanceof Room)
		{
			try
			{
				emoter=CMClass.getFactoryMOB();
				emoter.setName(L("something here"));
				emoteHere((Room)myHost,emoter,emote,msg.source(),emote.broadcast);
			}
			finally
			{
				emoter.destroy();
			}
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
				try
				{
					emoter=CMClass.getFactoryMOB();
					emoter.setName(myHost.name());
					emoteHere(room,emoter,emote,null,true);
				}
				finally
				{
					emoter.destroy();
				}
			}
		}
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((msg.amITarget(myHost)||(myHost instanceof Area)||(myHost instanceof Room))
		&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
		&&(smells!=null)
		&&(CMLib.flags().canSmell(msg.source()))
		&&((mask==null)||(CMLib.masking().maskCheck(mask, msg.source(), true))))
			pickedEmote(smells,myHost,msg);
		else
		if((msg.targetMinor()==CMMsg.TYP_PUFF)
		&&(msg.target() instanceof Light)
		&&(msg.tool() instanceof Light)
		&&(puffs!=null)
		&&(pufferCheck(myHost,msg))
		&&(msg.target()==msg.tool())
		&&(((Light)msg.target()).amWearingAt(Wearable.WORN_MOUTH))
		&&(((Light)msg.target()).isLit()))
			pickedEmote(puffs,myHost,msg);
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

	protected void emoteHere(final Room room,
							 final MOB emoter,
							 final EmoteObj emote,
							 final MOB emoteTo,
							 final boolean wrapper)
	{
		if(room==null)
			return;
		if(!inRoom(room))
			return;
		if((mask!=null)&&(!CMLib.masking().maskCheck(mask, emoter, true)))
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
		if(wrapper)
			str="^E<S-NAME> "+str+" ^?";
		if(emoteTo!=null)
		{
			emoteTo.tell(emoter,emoteTo,null,str);
			return;
		}
		if(emote.emoteOnly)
			msg=CMClass.getMsg(emoter,null,CMMsg.MSG_EMOTE,str);
		else
		switch(emote.type)
		{
		case EMOTE_VISUAL:
			msg=CMClass.getMsg(emoter,null,CMMsg.MSG_EMOTE,str);
			break;
		case EMOTE_SOUND:
			msg=CMClass.getMsg(emoter,null,CMMsg.MSG_NOISE,str);
			break;
		case EMOTE_SMELL:
		case EMOTE_PUFF:
			msg=CMClass.getMsg(emoter,null,CMMsg.TYP_AROMA|CMMsg.MASK_EYES,str);
			break;
		default:
		case EMOTE_SOCIAL:
			msg=null;
			break;
		}
		if((msg != null) && (room.okMessage(emoter,msg)))
			room.send(emoter, msg);
		if(oldLoc!=null)
			emoter.setLocation(oldLoc);
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
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
			if((emote.zmask != null)
			&&(ticking instanceof Environmental)
			&&(!CMLib.masking().maskCheck(emote.zmask,(Environmental)ticking,true)))
				return true;
			MOB emoter=null;
			if(ticking instanceof Area)
			{
				emoter=CMClass.getFactoryMOB();
				emoter.setName(ticking.name());
				for(final Enumeration<Room> r=((Area)ticking).getMetroMap();r.hasMoreElements();)
				{
					final Room R=r.nextElement();
					// if a tree falls in a forest...
					if((R!=null)
					&&(R.numInhabitants() > 0)
					&&(R.numPCInhabitants() > 0))
						emoteHere(R,emoter,emote,null,false);
				}
				emoter.destroy();
				return true;
			}
			else
			{
				final Room room=getBehaversRoom(ticking);
				if(room==null)
					return true;
				boolean killEmoter=false;
				// if a tree falls in a forest...
				if((room.numInhabitants()==0)||(room.numPCInhabitants()==0))
				{
					if(!emote.broadcast)
						return true;
				}
				else
				{
					if(ticking instanceof Room)
					{
						emoter=CMClass.getFactoryMOB();
						emoter.setName(L("something here"));
						killEmoter=true;
						emoteHere((Room)ticking,emoter,emote,null,emote.broadcast);
					}
					else
					{
						if(ticking instanceof MOB)
						{
							if(canFreelyBehaveNormal(ticking))
								emoter=(MOB)ticking;
						}
						else
						{
							if(ticking instanceof Item)
							{
								if(!CMLib.flags().isInTheGame((Item)ticking,false))
									return true;
							}

							emoter=CMClass.getFactoryMOB();
							killEmoter=true;
							final MOB mob=getBehaversMOB(ticking);
							String name=ticking.name();
							if(ticking instanceof Environmental)
								name=((Environmental)ticking).name();
							if(mob!=null)
							{
								if(CMLib.flags().isInTheGame(mob,false) && (!emote.privateE))
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
					}
				}

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
						if((R!=null)
						&&(E!=null)
						&&(E.isOpen())
						&&(R.numInhabitants()>0)
						&&(R.numPCInhabitants()>0))
						{
							final int opDir=room.getReverseDir(d);
							final String inDir=CMLib.directions().getInDirectionName(opDir, CMLib.flags().getDirType(R));
							if(emoter == null)
							{
								emoter=CMClass.getFactoryMOB();
								killEmoter=true;
							}
							emoter.setName(L("something @x1",inDir));
							emoteHere(R,emoter,emote,null,true);
						}
					}
				}

				if(killEmoter && (emoter != null))
					emoter.destroy();
			}
		}
		return true;
	}
}

