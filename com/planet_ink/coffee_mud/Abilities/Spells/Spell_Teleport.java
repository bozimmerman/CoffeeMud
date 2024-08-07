package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdPlanarAbility;
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
import java.util.concurrent.atomic.AtomicInteger;

/*
   Copyright 2001-2024 Bo Zimmerman

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
public class Spell_Teleport extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_Teleport";
	}

	private final static String localizedName = CMLib.lang().L("Teleport");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private boolean isBadRoom(final Room room, final MOB mob, final Room newRoom)
	{
		return (room==null)
			|| (room==newRoom)
			|| (room.getArea()==newRoom.getArea())
			|| (room==mob.location())
			|| (!CMLib.flags().canAccess(mob,room))
			|| (CMLib.law().getLandTitle(room)!=null);
	}

	@Override
	public CMObject copyOf()
	{
		final Spell_Teleport st = (Spell_Teleport)super.copyOf();
		st.setMiscText(text()); // will overwrite old parentGenA;
		return st;
	}

	private final static AtomicInteger ctr = new AtomicInteger(0);

	private Area parentGenA = null;
	private Room returnToRoom = null;
	private String castMsgStr = "^S<S-NAME> invoke(s) a teleportation spell.^?";
	private String appearMsgStr = "<S-NAME> appears in a puff of smoke.@x1";
	private String leaveMsgStr = "<S-NAME> disappear(s) in a puff of smoke.";

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
		{
			castMsgStr = CMParms.getParmStr(newMiscText, "CASTMSG", castMsgStr);
			appearMsgStr = CMParms.getParmStr(newMiscText, "APPEARMSG", appearMsgStr);
			leaveMsgStr = CMParms.getParmStr(newMiscText, "LEAVEMSG", leaveMsgStr);
			final String theme = CMParms.getParmStr(newMiscText, "THEME", "");
			if(theme.length()>0)
			{
				parentGenA = CMClass.getAreaType("StdAutoGenInstance");
				((AutoGenArea)parentGenA).setAutoGenVariables(newMiscText);
				parentGenA.setName(ID()+ctr.addAndGet(1));
				final Room R = CMClass.getLocale("StdRoom");
				R.setName(L("In the void"));
				R.setDisplayText(L("You are trapped in a void between spaces."));
				R.setRoomID(parentGenA.Name()+"#0");
				R.setArea(parentGenA);
			}
		}
	}

	@Override
	public void unInvoke()
	{
		super.unInvoke();
		if(parentGenA != null)
			((AutoGenArea)parentGenA).resetInstance(returnToRoom);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if(affected instanceof Exit)
		{
			if(((msg.targetMinor()==CMMsg.TYP_ENTER)
				&&((msg.tool()==affected)||(msg.target()==affected)))
			||((msg.targetMinor()==CMMsg.TYP_SIT)&&(msg.target()==affected)))
			{
				final Spell_Teleport spellA = (Spell_Teleport)CMClass.getAbility(ID());
				final List<String> cmds;
				if(text().length()>0)
				{
					if(parentGenA != null)
						cmds = new XVector<String>(msg.source().Name());
					else
						cmds = new XVector<String>(text());
				}
				else
					cmds = new XVector<String>(msg.source().Name());
				if(spellA != null)
				{
					if(parentGenA != null)
					{
						// skip regeneration
						spellA.miscText = this.miscText;
						spellA.parentGenA = this.parentGenA;
						spellA.castMsgStr="";
						spellA.leaveMsgStr=L("<S-NAME> disappear(s) into @x1.",affected.name(msg.source()));
					}
					spellA.invoke(msg.source(), cmds, msg.source(), true, msg.source().phyStats().level());
					return false;
				}
			}
		}
		else
		if((parentGenA!=null) && (!parentGenA.okMessage(myHost, msg)))
			return false;
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
		if((parentGenA!=null)
		&&(!(affected instanceof Exit)))
			parentGenA.executeMsg(myHost, msg);
		return;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		if((parentGenA!=null)
		&&(!(affected instanceof Exit))
		&& (!parentGenA.tick(parentGenA, Area.TICKID_AREA)))
			return false;
		return true;
	}

	@Override
	public void affectPhyStats(final Physical affected, final PhyStats affectableStats)
	{
		super.affectPhyStats(affected, affectableStats);
		if((parentGenA!=null)&&(!(affected instanceof Exit)))
			parentGenA.affectPhyStats(affected, affectableStats);
	}

	@Override
	public void affectCharStats(final MOB affectedMob, final CharStats affectableStats)
	{
		super.affectCharStats(affectedMob, affectableStats);
		if(parentGenA!=null)
			parentGenA.affectCharStats(affectedMob, affectableStats);
	}

	@Override
	public void affectCharState(final MOB affectedMob, final CharState affectableMaxState)
	{
		super.affectCharState(affectedMob, affectableMaxState);
		if(parentGenA!=null)
			parentGenA.affectCharState(affectedMob, affectableMaxState);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		if(auto||mob.isMonster())
		{
			if((commands.size()<1)
			||(commands.get(0)).equals(mob.name()))
			{
				commands.clear();
				if((text().length()>0)
				&&(parentGenA == null)
				&&(CMLib.map().findArea(text())!=null))
					commands.add(text());
				else
					commands.add(CMLib.map().getRandomArea().Name());
			}
		}
		if((commands.size()<1)
		&&(parentGenA == null))
		{
			mob.tell(L("Teleport to what area?"));
			return false;
		}
		final String areaName=CMParms.combine(commands,0).trim().toUpperCase();
		final Area A;
		if(parentGenA != null)
			A = parentGenA;
		else
			A = CMLib.map().findArea(areaName);
		final Vector<Room> candidates=new Vector<Room>();
		if(A!=null)
			candidates.addAll(new XVector<Room>(A.getProperMap()));
		for(int c=candidates.size()-1;c>=0;c--)
		{
			if(!CMLib.flags().canAccess(mob,candidates.elementAt(c)))
				candidates.removeElementAt(c);
		}

		final Ability effA = mob.fetchEffect(ID());
		if(effA!=null)
			effA.unInvoke();

		if(candidates.size()==0)
		{
			mob.tell(L("You don't know of an area called '@x1'.",CMParms.combine(commands,0)));
			return false;
		}

		if((!auto)
		&&(CMLib.flags().isSitting(mob)||CMLib.flags().isSleeping(mob)))
		{
			mob.tell(L("You need to stand up!"));
			return false;
		}

		Room newRoom=null;
		int tries=0;
		while((tries<20)&&(newRoom==null))
		{
			newRoom=candidates.elementAt(CMLib.dice().roll(1,candidates.size(),-1));
			if(((newRoom.roomID().length()==0)&&(CMLib.dice().rollPercentage()>50))
			||((newRoom.domainType()==Room.DOMAIN_OUTDOORS_AIR)&&(CMLib.dice().rollPercentage()>10)))
			{
				newRoom=null;
				continue;
			}
			final CMMsg enterMsg=CMClass.getMsg(mob,newRoom,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null);
			final Session session=mob.session();
			if(A != parentGenA)
				mob.setSession(null);
			if(!newRoom.okMessage(mob,enterMsg))
				newRoom=null;
			else
				newRoom=(Room)enterMsg.target();
			mob.setSession(session);
			tries++;
		}

		if(newRoom==null)
		{
			mob.tell(L("Your magic seems unable to take you to that area."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(!success)
		{
			Room room=null;
			int x=0;
			while(isBadRoom(room,mob,newRoom) && ((++x)<1000))
				room=CMLib.map().getRandomRoom();
			if(isBadRoom(room,mob,newRoom))
				beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to invoke transportation, but fizzle(s) the spell."));
			newRoom=room;
		}

		final CMMsg msg=CMClass.getMsg(mob,null,this,CMMsg.MASK_MOVE|verbalCastCode(mob,newRoom,auto), L(castMsgStr));
		if(mob.location().okMessage(mob,msg)&&(newRoom!=null))
		{
			mob.location().send(mob,msg);
			if(A == parentGenA)
			{
				final Spell_Teleport tA = (Spell_Teleport)beneficialAffect(mob, mob, asLevel, 0);
				if(tA != null)
					tA.returnToRoom = mob.location();
			}
			final List<MOB> h=properTargetList(mob,givenTarget,false);
			if(h==null)
				return false;

			final Room thisRoom=mob.location();
			for (final MOB follower : h)
			{
				final CMMsg enterMsg=CMClass.getMsg(follower,newRoom,this,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,null,CMMsg.MSG_ENTER,
						L(appearMsgStr,CMLib.protocol().msp("appear.wav",10)));
				final CMMsg leaveMsg=CMClass.getMsg(follower,thisRoom,this,CMMsg.MSG_LEAVE|CMMsg.MASK_MAGIC, L(leaveMsgStr));
				if(thisRoom.okMessage(follower,leaveMsg)
				&&newRoom.okMessage(follower,enterMsg))
				{
					if(follower.isInCombat())
					{
						CMLib.commands().postFlee(follower,("NOWHERE"));
						follower.makePeace(false);
					}
					thisRoom.send(follower,leaveMsg);
					((Room)enterMsg.target()).bringMobHere(follower,false);
					((Room)enterMsg.target()).send(follower,enterMsg);
					follower.tell(L("\n\r\n\r"));
					CMLib.commands().postLook(follower,true);
				}
				else
				if(follower==mob)
					break;
			}
		}

		// return whether it worked
		return success;
	}
}
