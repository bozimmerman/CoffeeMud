package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2014-2018 Bo Zimmerman

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

public class Spell_HelpingHand extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_HelpingHand";
	}

	private final static String localizedName = CMLib.lang().L("Helping Hand");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Helping Hand)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_EVOCATION;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	protected MOB targetM = null;
	protected int frustration = 0;
	protected String targetName = "your target";
	protected List<Room> trail = null;
	
	@Override
	public void setMiscText(String text)
	{
		super.setMiscText(text);
		targetM=null;
		targetName = "your target";
		frustration = 0;
		if(text.length()>0)
		{
			targetName = text;
			MOB M=CMLib.players().getPlayer(text);
			if((M!=null)&&(CMLib.flags().isInTheGame(M, true)))
				targetM=M;
		}
	}
	
	@Override
	public void setAffectedOne(Physical P)
	{
		if(!(P instanceof MOB))
			super.setAffectedOne(P);
		else
		{
			frustration = 0;
			if(CMLib.flags().isInTheGame(P, true))
				targetM=(MOB)P;
		}
	}
	
	@Override
	public void unInvoke()
	{
		final Physical affected=super.affected;
		// undo the affects of this spell
		if(affected==null)
			return;
		if(canBeUninvoked())
		{
			if((invoker()!=null)
			&&((targetM == null)||(!CMLib.flags().isInTheGame(targetM, true))||(targetM.location()!=invoker().location())))
				invoker().tell(L("The helping hand for @x1 has given up.",targetName));
			final Room R=CMLib.map().roomLocation(affected);
			if(R.isHere(affected))
				R.showHappens(CMMsg.MSG_OK_VISUAL,L("The helping hand vanishes."));
		}
		super.unInvoke();
		if(canBeUninvoked() && (!affected.amDestroyed()))
			affected.destroy();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		final MOB targetM = this.targetM;
		final MOB invokerM = this.invoker();
		if((targetM == null)
		||(!CMLib.flags().isInTheGame(targetM, true))
		||(invokerM==null)
		||(!CMLib.flags().isInTheGame(invokerM, true))
		||(targetM.location()==invokerM.location()))
		{
			unInvoke();
			return false;
		}
		if(affected instanceof Item)
		{
			final Item handI=(Item)affected;
			Room handIR=CMLib.map().roomLocation(handI);
			if(targetM.location() != handIR)
			{
				targetM.location().moveItemTo(handI);
				if(frustration < 3)
					targetM.location().show(CMLib.map().deity(), targetM, handI, CMMsg.MSG_OK_VISUAL, L("<O-NAME> floats into the room and gestures for <T-NAME> to follow it."));
				else
				if(frustration < 10)
					targetM.location().show(CMLib.map().deity(), targetM, handI, CMMsg.MSG_OK_VISUAL, L("<O-NAME> floats into the room and urgently gestures for <T-NAME> to follow it."));
				else
				if(frustration < 20)
					targetM.location().show(CMLib.map().deity(), targetM, handI, CMMsg.MSG_OK_VISUAL, L("<O-NAME> floats into the room and wildly gestures for <T-NAME> to follow it."));
				else
				if(frustration < 50)
					targetM.location().show(CMLib.map().deity(), targetM, handI, CMMsg.MSG_OK_VISUAL, L("<O-NAME> floats into the room and INSISTS that <T-NAME> follow it."));
				else
					targetM.location().show(CMLib.map().deity(), targetM, handI, CMMsg.MSG_OK_VISUAL, L("<O-NAME> floats into the room, grabs <T-NAME> by the collar and DEMANDS that <T-NAME> follow it."));
				frustration++;
				handIR=targetM.location();
				// give the target a second to read
				return true;
			}
			if((trail == null) || (trail.size()==0) || (trail.get(0) != invokerM.location()))
				trail = CMLib.tracking().findTrailToRoom(handIR, invokerM.location(), null, 1000);
			int nextDirection = CMLib.tracking().trackNextDirectionFromHere(trail, handIR, false);
			if(nextDirection < 0)
			{
				trail = CMLib.tracking().findTrailToRoom(handIR, invokerM.location(), null, 1000);
				nextDirection = CMLib.tracking().trackNextDirectionFromHere(trail, handIR, false);
			}
			final Room nextRoom=handIR.getRoomInDir(nextDirection);
			final Exit nextExit=handIR.getExitInDir(nextDirection);
			if((nextDirection < 0)||(nextRoom==null)||(nextExit==null))
			{
				handIR.show(CMLib.map().deity(), targetM, handI, CMMsg.MSG_OK_VISUAL, L("<O-NAME> gives a thumbs-down."));
				unInvoke();
				return false;
			}
			if(!nextExit.isOpen())
			{
				final MOB doorOpenerM=CMClass.getFactoryMOB();
				doorOpenerM.setName(handI.Name());
				doorOpenerM.basePhyStats().setLevel(handI.basePhyStats().level());
				doorOpenerM.phyStats().setLevel(handI.basePhyStats().level());
				doorOpenerM.setLocation(handIR);
				if((nextExit.hasALock())&&(nextExit.isLocked()))
				{
					CMMsg msg=CMClass.getMsg(doorOpenerM,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_OK_VISUAL,null);
					if(handIR.okMessage(doorOpenerM,msg))
					{
						msg=CMClass.getMsg(doorOpenerM,nextExit,null,CMMsg.MSG_OK_VISUAL,CMMsg.MSG_UNLOCK,CMMsg.MSG_OK_VISUAL,L("<S-NAME> pick(s) the lock on <T-NAMESELF>."));
						if(handIR.okMessage(doorOpenerM,msg))
							CMLib.utensils().roomAffectFully(msg,handIR,nextDirection);
					}
				}
				if(!nextExit.isOpen())
				{
					final Vector<String> openCommandV=new ReadOnlyVector<String>(CMParms.parse("OPEN "+CMLib.directions().getDirectionName(nextDirection)));
					doorOpenerM.doCommand(openCommandV,MUDCmdProcessor.METAFLAG_FORCED);
				}
				doorOpenerM.destroy();
			}
			handIR.show(CMLib.map().deity(), targetM, handI, CMMsg.MSG_OK_VISUAL, L("<O-NAME> floats @x1.",CMLib.directions().getDirectionName(nextDirection)));
			nextRoom.moveItemTo(handI);
			if((targetM.location() == handIR) && (targetM.isMonster()) && (invoker()!=null) && (invoker().getGroupMembers(new HashSet<MOB>()).contains(targetM)))
				CMLib.tracking().walk(targetM, nextDirection, targetM.isInCombat(), true);
		}
		return true;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<1)
		{
			if(mob.isMonster() && (mob.getGroupMembers(new HashSet<MOB>()).size()>1))
			{
				for(MOB M : mob.getGroupMembers(new HashSet<MOB>()))
				{
					if((M!=mob)&&(M.isPlayer()))
					{
						commands.add(M.Name());
						break;
					}
				}
				if(commands.size()<1)
				{
					for(MOB M : mob.getGroupMembers(new HashSet<MOB>()))
					{
						if(M!=mob)
						{
							commands.add(M.Name());
							break;
						}
					}
				}
			}
			if(commands.size()<1)
			{
				mob.tell(L("Provide a helping hand for whom?"));
				return false;
			}
		}
		
		MOB target=null;
		final String whomName=CMParms.combine(commands);
		MOB M=CMLib.players().getPlayer(whomName);
		if((M!=null)&&(CMLib.flags().isInTheGame(M,true)))
			target=M;
		else
		for(MOB M2 : mob.getGroupMembers(new HashSet<MOB>()))
		{
			if((M2!=null)
			&&(CMLib.flags().isInTheGame(M2,true))
			&&(CMLib.english().containsString(M2.name(), whomName)||CMLib.english().containsString(M2.displayText(), whomName)))
			{
				target=M2;
				break;
			}
		}
		if(target==null)
		{
			mob.tell(L("You can't seem to focus on @x1.",whomName));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":L("^S<S-NAME> wave(s) <S-HIS-HER> arms around, incanting.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item handI=CMClass.getItem("GenItem");
				handI.setName(L("a helping hand"));
				handI.setDisplayText(L("a helping hand is here to show @x1 the way.",target.name()));
				handI.setDescription(L("It seems to be here for @x1, and wants you to follow where it leads you.",target.name()));
				handI.basePhyStats().setLevel(adjustedLevel(mob,asLevel));
				handI.phyStats().setLevel(adjustedLevel(mob,asLevel));
				CMLib.flags().setGettable(handI, false);
				handI.basePhyStats().setDisposition(handI.basePhyStats().disposition()|PhyStats.IS_FLYING);
				mob.location().addItem(handI);
				final Ability A=beneficialAffect(mob,handI,asLevel,0);
				if(A!=null)
				{
					A.setMiscText(target.Name());
					A.setAffectedOne(target);
				}
				else
					handI.destroy();
			}
		}
		else
			beneficialVisualFizzle(mob,target,L("<S-NAME> wave(s) <S-HIS-HER> arms around, incanting, but nothing happens."));

		// return whether it worked
		return success;
	}
}
