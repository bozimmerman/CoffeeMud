package com.planet_ink.coffee_mud.Abilities.Properties;
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
   Copyright 2018-2025 Bo Zimmerman

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
public class Prop_UseEmoter extends Property
{
	@Override
	public String ID()
	{
		return "Prop_UseEmoter";
	}

	@Override
	public String name()
	{
		return "Emoting when used";
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ITEMS;
	}

	protected volatile boolean	processing	= false;
	protected List<EmoteObj>	emotes		= null;
	protected List<EmoteObj>	smells		= null;
	protected List<EmoteObj>	puffs		= null;
	protected boolean			privateE	= false;
	protected boolean			broadcast	= false;
	protected int				chance		= 100;
	protected List<String>		inroomIDs	= new XVector<String>(0, true);
	protected CompiledZMask		mask		= null;

	protected static class EmoteObj
	{
		public EMOTE_TYPE type;
		public String msg;
		public boolean broadcast;
		public EmoteObj(final EMOTE_TYPE type, final String msg, final boolean broadcast)
		{
			this.type=type;
			this.msg=msg;
			this.broadcast=broadcast;
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

	protected EMOTE_TYPE emoteType=EMOTE_TYPE.EMOTE_VISUAL;

	@Override
	public String accountForYourself()
	{
		return "emoting when used";
	}

	protected boolean emoteNow(final CMMsg msg)
	{
		final Physical P = affected;
		if(P==null)
			return false;
		if(CMLib.dice().rollPercentage()>chance)
			return false;
		msg.addTrailerRunnable(new Runnable()
		{
			final Physical P = affected;
			final Room room=CMLib.map().roomLocation(affected);
			final MOB mob=(P instanceof MOB)?(MOB)P:((P instanceof Item)&&((Item)P).owner() instanceof MOB)?(MOB)((Item)P).owner():null;
			@Override
			public void run()
			{
				boolean killEmoter=false;
				MOB emoter=null;
				try
				{
					if(P instanceof MOB)
					{
						if(CMLib.flags().canFreelyBehaveNormal(P))
							emoter=(MOB)P;
					}
					else
					{
						emoter=CMClass.getFactoryMOB();
						killEmoter=true;
						final String name=P.name();
						if((mob!=null)&&(CMLib.flags().isInTheGame(P,false))&&(!privateE))
						{
							if(CMLib.flags().isInTheGame(mob,false))
								emoter.setName(L("@x1 carried by @x2",name,mob.name()));
							else
								emoter=null;
						}
						else
							emoter.setName(name);
					}
					parseEmotes();
					if((emoter==null)
					||(emotes==null)
					||(emotes.size()==0))
						return;
					final EmoteObj emote=emotes.get(CMLib.dice().roll(1, emotes.size(), -1));
					final MOB emoteTo=(privateE)?mob:null;

					emoteHere(room,emoter,emote,emoteTo,true);

					if(emote.broadcast)
					{
						if(P instanceof MOB)
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
								final String inDir=CMLib.directions().getInDirectionName(Directions.getOpDirectionCode(d),CMLib.flags().getDirType(R));
								emoter.setName(L("something @x1",inDir));
								emoteHere(R,emoter,emote,emoteTo,true);
							}
						}
					}
				}
				finally
				{
					if(killEmoter && (emoter!=null))
						emoter.destroy();
				}
			}
		});
		return true;
	}

	public void untargetedEmote(final List<EmoteObj> whats, final CMMsg msg)
	{
		final EmoteObj emote=whats.get(CMLib.dice().roll(1,whats.size(),-1));
		MOB emoter=null;
		if(affected instanceof Room)
		{
			try
			{
				emoter=CMClass.getFactoryMOB();
				emoteHere((Room)affected,emoter,emote,msg.source(),false);
			}
			finally
			{
				if(emoter != null)
					emoter.destroy();
			}
			return;
		}
		final Room room=CMLib.map().roomLocation(affected);
		if(room!=null)
		{
			if(affected instanceof MOB)
			{
				emoter=(MOB)affected;
				emoteHere(room,emoter,emote,null,true);
			}
			else
			{
				if((affected instanceof Item)
				&&(!CMLib.flags().isInTheGame((Item)affected,false)))
					return;
				try
				{
					emoter=CMClass.getFactoryMOB();
					emoter.setName(affected.name());
					emoteHere(room,emoter,emote,null,true);
				}
				finally
				{
					if(emoter != null)
						emoter.destroy();
				}
			}
		}
	}

	public boolean pufferCheck(final CMMsg msg)
	{
		if((msg.source() == affected)||(msg.target() == affected))
			return true;
		if ((affected instanceof Room) || (affected instanceof Area))
			return true;
		if(affected instanceof Item)
		{
			final Item I = (Item) affected;
			if((I.owner() == affected)&&(I.amBeingWornProperly()))
				return true;
			if((I instanceof RawMaterial) && (I.container() == affected))
				return true;
		}
		return false;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(processing)
			return;
		processing=true;
		try
		{
			if(affected==null)
				return;
			final Item myItem=(Item)affected;
			if(myItem.owner()==null)
				return;
			if(!(myItem.owner() instanceof MOB))
				return;

			parseEmotes();
			if((msg.targetMinor()==CMMsg.TYP_PUFF)
			&&(puffs != null)
			&&(msg.target() instanceof Light)
			&&(msg.tool() instanceof Light)
			&&(msg.target()==msg.tool())
			&&(((Light)msg.target()).amWearingAt(Wearable.WORN_MOUTH))
			&&(((Light)msg.target()).isLit())
			&&(pufferCheck(msg))
			&&((mask==null)||(CMLib.masking().maskCheck(mask, msg.source(), true))))
				untargetedEmote(puffs,msg);

			if((emotes==null)||(emotes.size()==0))
				return;

			if((msg.amITarget(affected))
			&&(msg.targetMinor()==CMMsg.TYP_SNIFF)
			&&(smells!=null)
			&&(CMLib.flags().canSmell(msg.source()))
			&&((mask==null)||(CMLib.masking().maskCheck(mask, msg.source(), true))))
				untargetedEmote(smells,msg);

			if((msg.amISource((MOB)myItem.owner()))
			&&((mask==null)||(CMLib.masking().maskCheck(mask, msg.source(), true))))
			{
				switch(msg.sourceMinor())
				{
				case CMMsg.TYP_FILL:
					if((myItem instanceof Drink)
					&&(msg.tool()!=myItem)
					&&(msg.amITarget(myItem)))
						this.emoteNow(msg);
					break;
				case CMMsg.TYP_WEAR:
					if((myItem instanceof Armor)
					  &&(msg.amITarget(myItem)))
						this.emoteNow(msg);
					break;
				case CMMsg.TYP_PUT:
				case CMMsg.TYP_INSTALL:
					if((myItem instanceof Container)
					  &&(msg.amITarget(myItem)))
						this.emoteNow(msg);
					break;
				case CMMsg.TYP_WIELD:
				case CMMsg.TYP_HOLD:
					if((!(myItem instanceof Drink))
					&&(!(myItem instanceof Armor))
					&&(!(myItem instanceof Container))
					&&(msg.amITarget(myItem)))
						this.emoteNow(msg);
					break;
				}
			}
		}
		finally
		{
			processing=false;
		}
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		inroomIDs=CMParms.parseCommas(CMParms.getParmStr(newMiscText,"inroom","").toUpperCase().trim(),true);
		chance=CMParms.getParmInt(newMiscText,"chance",100);
		emotes=null;
		smells=null;
		puffs=null;
		mask=null;
	}

	protected boolean setEmoteType(final String str)
	{
		switch(str.toUpperCase().trim())
		{
		case "BROADCAST":
			broadcast=true;
			break;
		case "PRIVATE":
			privateE=true;
			break;
		case "NOPRIVATE":
			privateE=false;
			break;
		case "NOBROADCAST":
			broadcast=false;
			break;
		case "VISUAL": case"SIGHT":
			emoteType=EMOTE_TYPE.EMOTE_VISUAL;
			break;
		case "AROMA": case"SMELL":
			emoteType=EMOTE_TYPE.EMOTE_SMELL;
			break;
		case "SOUND": case"NOISE":
			emoteType=EMOTE_TYPE.EMOTE_SOUND;
			break;
		case "SOCIAL":
			emoteType=EMOTE_TYPE.EMOTE_SOCIAL;
			break;
		case "PUFF":
			emoteType = EMOTE_TYPE.EMOTE_PUFF;
			break;
		default:
			return false;
		}
		return true;
	}

	protected void setEmoteTypes(final Vector<String> V, final boolean respectOnlyBeginningAndEnd)
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
		puffs=null;
		smells=null;
		mask=null;
		emoteType=EMOTE_TYPE.EMOTE_VISUAL;
		emotes=new Vector<EmoteObj>();
		String newParms=text();
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
			final String maskStr = CMParms.getParmStr(oldParms, "MASK", null);
			if(maskStr != null)
				this.mask=CMLib.masking().maskCompile(maskStr);
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
					if(emoteType==EMOTE_TYPE.EMOTE_PUFF)
					{
						if(puffs==null)
							puffs=new Vector<EmoteObj>();
						puffs.add(new EmoteObj(emoteType,thisEmote,broadcast));
					}
					else
						emotes.add(new EmoteObj(emoteType,thisEmote,broadcast));
				}
			}
		}
		return emotes;
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
							 final boolean Wrapper)
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
		if(emote.type==EMOTE_TYPE.EMOTE_SMELL)
			msg.setSourceCode(CMMsg.MASK_ALWAYS|CMMsg.TYP_AROMA);
		else
		if(emote.type==EMOTE_TYPE.EMOTE_PUFF)
			msg.setSourceCode(CMMsg.MASK_ALWAYS|CMMsg.TYP_AROMA|CMMsg.MASK_EYES);
		if(room.okMessage(emoter,msg))
		{
			for(int i=0;i<room.numInhabitants();i++)
			{
				final MOB M=room.fetchInhabitant(i);
				if((M!=null)&&(!M.isMonster()))
				{
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
						{
							M.executeMsg(M,msg);
						}
						break;
					case EMOTE_PUFF:
						if(CMLib.flags().canSmell(M)||CMLib.flags().canSee(M))
						{
							M.executeMsg(M,msg);
						}
						break;
					case EMOTE_SOCIAL:
						// handled above
						break;
					}
				}
			}
		}
		if(oldLoc!=null)
			emoter.setLocation(oldLoc);
	}
}
