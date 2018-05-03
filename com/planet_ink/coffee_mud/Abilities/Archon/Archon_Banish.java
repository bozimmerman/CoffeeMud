package com.planet_ink.coffee_mud.Abilities.Archon;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

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

public class Archon_Banish extends ArchonSkill
{
	@Override
	public String ID()
	{
		return "Archon_Banish";
	}

	private final static String localizedName = CMLib.lang().L("Banish");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return L("(Banished " + timeRemaining() + ")");
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[] triggerStrings = I(new String[] { "BANISH" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL | Ability.DOMAIN_ARCHON;
	}

	@Override
	public int maxRange()
	{
		return adjustedMaxInvokerRange(1);
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected Room	prisonRoom	= null;
	protected long	releaseTime	= 0;

	protected String timeRemaining()
	{
		if(releaseTime<=0)
			return "indefinitely";
		if(releaseTime<System.currentTimeMillis())
			return "until any second now.";
		return "for another "+CMLib.english().returnTime(releaseTime-System.currentTimeMillis(),0);
	}

	public Room prison()
	{
		if((prisonRoom!=null)&&(!prisonRoom.amDestroyed()))
			return prisonRoom;

		Room myPrison=null;
		int x=0;
		if((text().length()>0)&&((x=text().indexOf("<P>"))>0))
			myPrison=CMLib.map().getRoom(text().substring(0,x));
		if(myPrison != null)
		{
			prisonRoom = (Room) myPrison.copyOf();
		}
		else
		{
			prisonRoom=CMClass.getLocale("StoneRoom");
			prisonRoom.addNonUninvokableEffect((Ability)copyOf());
			prisonRoom.setArea(CMLib.map().getFirstArea());
			prisonRoom.setDescription(L("You are standing on an immense, grey stone floor that stretches as far as you can see in all directions.  Rough winds plunging from the dark, starless sky tear savagely at your fragile body."));
			prisonRoom.setDisplayText(L("The Hall of Lost Souls"));
			prisonRoom.setRoomID("");
			final Ability A2=CMClass.getAbility("Prop_HereSpellCast");
			if(A2!=null)
				A2.setMiscText("Spell_Hungerless;Spell_Thirstless");
			if(A2!=null)
				prisonRoom.addNonUninvokableEffect(A2);
		}
		for(final int dir : Directions.CODES())
		{
			prisonRoom.setRawExit(dir,CMClass.getExit("Open"));
			prisonRoom.rawDoors()[dir]=prisonRoom;
		}
		return prisonRoom;
	}

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		final int x=newText.indexOf("<P>");
		if(x>=0)
			releaseTime=CMath.s_long(newText.substring(x+3));
		prisonRoom=null;
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		final Room room=prison();
		if((ticking instanceof MOB)&&(!room.isInhabitant((MOB)ticking)))
			room.bringMobHere((MOB)ticking,false);
		if(releaseTime<=0)
			return true;
		if(releaseTime>System.currentTimeMillis())
			return true;
		unInvoke();
		return false;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected instanceof Room)
		{
			if((msg.tool() instanceof Ability)
			&&(msg.source().location()!=null)
			&&(msg.sourceMinor()!=CMMsg.TYP_LEAVE))
			{
				final boolean summon=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING);
				final boolean teleport=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING);
				final boolean shere=(msg.source().location()==affected)||(msg.source().location().getArea()==affected);
				if(((!shere)&&(!summon)&&(teleport))
				   ||((shere)&&(summon)))
				{
					msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,L("Magic energy fizzles and is absorbed into the air."));
					return false;
				}
			}
			if((msg.tool() instanceof Ability)
			&&(msg.source().location()!=null)
			&&(msg.sourceMinor()!=CMMsg.TYP_ENTER))
			{
				final boolean shere=(msg.source().location()==affected)||(msg.source().location().getArea()==affected);
				final boolean summon=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING);
				final boolean teleport=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING);
				if(((shere)&&(!summon)&&(teleport))
				   ||((!shere)&&(summon)))
				{
					msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,L("Magic energy fizzles and is absorbed into the air."));
					return false;
				}
			}
			if((msg.tool() instanceof Ability)
			&&(msg.source().location()!=null)
			&&((msg.source().location()==affected)
			   ||(msg.source().location().getArea()==affected))
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING)))
			{
				msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,L("Magic energy fizzles and is absorbed into the air."));
				return false;
			}
			if(msg.sourceMinor()==CMMsg.TYP_RECALL)
			{
				if(msg.source().location()!=null)
					msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,L("<S-NAME> attempt(s) to recall, but the magic fizzles."));
				return false;
			}
		}
		return true;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
		{
			super.unInvoke();
			return;
		}
		final MOB mob=(MOB)affected;

		super.unInvoke();

		mob.tell(L("You are released from banishment!"));
		mob.getStartRoom().bringMobHere(mob,true);
		if(prisonRoom!=null)
		{
			CMLib.map().emptyRoom(prisonRoom, mob.getStartRoom(), true);
			prisonRoom.destroy();
			prisonRoom=null;
		}
		mob.delEffect(this);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		long time=0;
		if(commands.size()>2)
		{
			final String last=(commands.get(commands.size()-1)).toUpperCase();
			final String num=commands.get(commands.size()-2);
			if((CMath.isInteger(num))&&(CMath.s_int(num)>0))
			{
				if("DAYS".startsWith(last))
					time=System.currentTimeMillis()+(TimeManager.MILI_DAY*CMath.s_int(num));
				else
				if("MONTHS".startsWith(last))
					time=System.currentTimeMillis()+(TimeManager.MILI_MONTH*CMath.s_int(num));
				else
				if("HOURS".startsWith(last))
					time=System.currentTimeMillis()+(TimeManager.MILI_HOUR*CMath.s_int(num));
				else
				if("MINUTES".startsWith(last))
					time=System.currentTimeMillis()+(TimeManager.MILI_MINUTE*CMath.s_int(num));
				else
				if("SECONDS".startsWith(last))
					time=System.currentTimeMillis()+(TimeManager.MILI_SECOND*CMath.s_int(num));
				else
				if("TICKS".startsWith(last))
					time=System.currentTimeMillis()+(CMProps.getTickMillis()*CMath.s_int(num));
				if(time>System.currentTimeMillis())
				{
					commands.remove(commands.size()-1);
					commands.remove(commands.size()-1);
				}
			}
		}
		Room myPrison = CMLib.map().getRoom(CMParms.combine(commands,1));
		if(myPrison != null && CMLib.map().getExtendedRoomID(myPrison).length()>0)
		{
			while(commands.size() > 1)
				commands.remove(1);
		}
		else
			myPrison = null;

		final MOB target=getTargetAnywhere(mob,commands,givenTarget,false,true,false);
		if(target==null)
			return false;

		Archon_Banish A=(Archon_Banish)target.fetchEffect(ID());
		if(A!=null)
		{
			A.unInvoke();
			mob.tell(L("@x1 is released from banishment.",target.Name()));
			return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto, asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?L("<T-NAME> is banished!"):L("^F<S-NAME> banish(es) <T-NAMESELF>.^?"));
			CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				A=(Archon_Banish)copyOf();
				String prisonID="";
				if(myPrison!=null)
					prisonID=CMLib.map().getExtendedRoomID(myPrison);
				A.setMiscText(prisonID+"<P>"+time);
				target.addNonUninvokableEffect(A);
				A=(Archon_Banish)target.fetchEffect(ID());
				if((A!=null)&&(A.prison()!=null)&&(!A.prison().isInhabitant(target)))
				{
					A.prison().bringMobHere(target,false);
					mob.location().send(mob,msg);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> <S-IS-ARE> banished to @x1!",A.prison().displayText()));
					Log.sysOut("Banish",mob.Name()+" banished "+target.name()+" to "+CMLib.map().getDescriptiveExtendedRoomID(A.prison())+".");
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,target,L("<S-NAME> attempt(s) to banish <T-NAMESELF>, but fail(s)."));
		return success;
	}
}
