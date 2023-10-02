package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.StdPlanarAbility;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.PlanarAbility.PlanarVar;
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
   Copyright 2020-2023 Bo Zimmerman

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
public class Spell_PlanarBubble extends Spell
{

	@Override
	public String ID()
	{
		return "Spell_PlanarBubble";
	}

	private final static String localizedName = CMLib.lang().L("Planar Bubble");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Planar Bubble)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_ROOMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL|Ability.DOMAIN_ALTERATION;
	}

	protected Room			newRoom		= null;
	protected String		planeName	= null;
	protected volatile long	contentHash	= 0;
	protected volatile int	oldAtmo		= 0;

	protected final List<Ability> aeffects = new Vector<Ability>();
	protected final List<Behavior> abehavs = new Vector<Behavior>();

	protected void totallyClearRoom(final Room R)
	{
		for(final Enumeration<Item> r=R.items();r.hasMoreElements();)
		{
			final Item I=r.nextElement();
			if(I!=null)
				I.destroy();
		}
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if(M!=null)
				M.destroy();
		}
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		final Room room=(Room)affected;
		if(canBeUninvoked())
			room.showHappens(CMMsg.MSG_OK_VISUAL, L("The nature of this place returns to normal."));
		final Room R = room();
		if(R!=null)
			totallyClearRoom(R);
		if(oldAtmo != 0)
			room.setAtmosphere(oldAtmo);
		aeffects.clear();
		abehavs.clear();
		super.unInvoke();
	}

	@Override
	public void setMiscText(final String newMiscText)
	{
		super.setMiscText(newMiscText);
		if(newMiscText.length()>0)
			this.planeName=newMiscText;
	}

	protected Room room()
	{
		if(newRoom==null)
		{
			aeffects.clear();
			abehavs.clear();
			if((affected instanceof Room)
			&&(planeName != null))
			{
				oldAtmo = ((Room)affected).getAtmosphereCode();
				final PlanarAbility planeA=(PlanarAbility)CMClass.getAbility("StdPlanarAbility");
				final Room baseRoom=(Room)affected;
				newRoom=CMClass.getLocale(baseRoom.ID());
				newRoom.setDisplayText(baseRoom.displayText());
				newRoom.setDescription(baseRoom.description());
				for(int d=0;d<Directions.NUM_DIRECTIONS();d++)
				{
					newRoom.rawDoors()[d]=baseRoom.rawDoors()[d];
					newRoom.setRawExit(d, baseRoom.getRawExit(d));
				}
				planeA.setPlanarName(planeName);
				planeA.doPlanarRoomColoring(newRoom);
				final String atmosphere = planeA.getPlaneVars().get(PlanarVar.ATMOSPHERE.toString());
				if((atmosphere!=null)&&(atmosphere.length()>0))
				{
					final int atmo=RawMaterial.CODES.FIND_IgnoreCase(atmosphere);
					baseRoom.setAtmosphere(atmo);
				}
				for(final CMObject O : planeA.getAreaEffectsBehavs())
				{
					if(O instanceof Ability)
					{
						((Ability)O).setAffectedOne(baseRoom.getArea());
						aeffects.add((Ability)O);
					}
					else
					if(O instanceof Behavior)
						abehavs.add((Behavior)O);
				}
				final String absorb = planeA.getPlaneVars().get(PlanarVar.ABSORB.toString());
				if(absorb != null)
				{
					final Ability A=CMClass.getAbility("Prop_AbsorbDamage");
					A.setMiscText(absorb);
					aeffects.add(A);
				}
			}
		}
		return newRoom;
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		if(!super.tick(ticking, tickID))
			return false;
		for(final Ability A : this.aeffects)
			A.tick(ticking,tickID);
		for(final Behavior B : this.abehavs)
			B.tick(ticking,tickID);
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		for(final Ability A : this.aeffects)
			A.executeMsg(this,msg);
		for(final Behavior B : this.abehavs)
			B.executeMsg(this,msg);
		super.executeMsg(myHost, msg);
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if((affected!=null)
		&&(affected instanceof Room)
		&&(room()!=null))
		{
			for(final Ability A : this.aeffects)
			{
				if(!A.okMessage(this,msg))
					return false;
			}
			for(final Behavior B : this.abehavs)
			{
				if(!B.okMessage(this,msg))
					return false;
			}
			if((msg.amITarget(affected))
			&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
			{
				try
				{
					final Room baseRoom=(Room)affected;
					long currentHash = msg.source().hashCode();
					final Room R=room();
					for(final Enumeration<Item> r=baseRoom.items();r.hasMoreElements();)
					{
						final Item I=r.nextElement();
						if(I!=null)
						{
							currentHash ^= I.name().hashCode();
							currentHash ^= I.displayText().hashCode();
							currentHash ^= I.phyStats().disposition();
						}
					}
					for(final Enumeration<MOB> m=baseRoom.inhabitants();m.hasMoreElements();)
					{
						final MOB M=m.nextElement();
						if(M!=null)
						{
							currentHash ^= M.name().hashCode();
							currentHash ^= M.displayText().hashCode();
							currentHash ^= M.phyStats().disposition();
						}
					}
					if(currentHash != this.contentHash)
					{
						totallyClearRoom(R);
						final Map<Integer,CMObject> map = new HashMap<Integer,CMObject>();
						for(final Enumeration<Item> r=baseRoom.items();r.hasMoreElements();)
						{
							final Item I=r.nextElement();
							final Item wrapI;
							if(I instanceof Exit)
								wrapI=CMClass.getBasicItem("StdPortalWrapper");
							else
							if(I instanceof Rideable)
								wrapI=CMClass.getBasicItem("StdRideableWrapper");
							else
							if(I instanceof Container)
								wrapI=CMClass.getBasicItem("StdContainerWrapper");
							else
								wrapI=CMClass.getBasicItem("StdItemWrapper");
							if(wrapI==null)
								continue;
							((CMObjectWrapper)wrapI).setWrappedObject(I);
							R.addItem(wrapI);
							map.put(Integer.valueOf(I.hashCode()), wrapI);
						}
						for(final Enumeration<MOB> m=baseRoom.inhabitants();m.hasMoreElements();)
						{
							final MOB M=m.nextElement();
							if(M == msg.source())
								continue;
							final MOB wrapM;
							if(M instanceof Rideable)
								wrapM=CMClass.getMOB("StdRideableWrapper");
							else
								wrapM=CMClass.getMOB("StdMobWrapper");
							((CMObjectWrapper)wrapM).setWrappedObject(M);
							R.addInhabitant(wrapM);
							map.put(Integer.valueOf(M.hashCode()), wrapM);
						}
						for(final Enumeration<Item> r=baseRoom.items();r.hasMoreElements();)
						{
							final Item I=r.nextElement();
							if(I.container()!=null)
							{
								final Item wrapI = (Item)map.get(Integer.valueOf(I.hashCode()));
								final CMObject container = map.get(Integer.valueOf(I.container().hashCode()));
								if(container instanceof Container)
									wrapI.setContainer((Container)container);
							}
							if(I.riding()!=null)
							{
								final Item wrapI = (Item)map.get(Integer.valueOf(I.hashCode()));
								final CMObject rideable = map.get(Integer.valueOf(I.riding().hashCode()));
								if(rideable instanceof Rideable)
									wrapI.setRiding((Rideable)rideable);
							}
						}
						for(final Enumeration<MOB> m=baseRoom.inhabitants();m.hasMoreElements();)
						{
							final MOB M=m.nextElement();
							if(M.riding()!=null)
							{
								final MOB wrapM = (MOB)map.get(Integer.valueOf(M.hashCode()));
								final CMObject rideable = map.get(Integer.valueOf(M.riding().hashCode()));
								if(rideable instanceof Rideable)
									wrapM.setRiding((Rideable)rideable);
							}
						}
						contentHash = currentHash;
					}
					final CMMsg msg2=CMClass.getMsg(msg.source(),room(),msg.tool(),
								  msg.sourceCode(),msg.sourceMessage(),
								  msg.targetCode(),msg.targetMessage(),
								  msg.othersCode(),msg.othersMessage());
					if(R.okMessage(msg.source(),msg2))
					{
						R.executeMsg(msg.source(),msg2);
						return false;
					}
				}
				finally
				{
				}
			}
		}
		return super.okMessage(myHost,msg);
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if((mob.isInCombat())&&(mob.isMonster()))
				return Ability.QUALITY_INDIFFERENT;
			if(target instanceof MOB)
			{
			}
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final PlanarAbility planeA=(PlanarAbility)CMClass.getAbility("StdPlanarAbility");

		if((commands.size()==0)
		&&(mob.isMonster()))
			commands.add(planeA.getAllPlaneKeys().get(CMLib.dice().roll(1, planeA.getAllPlaneKeys().size(), -1)));
		final String planeName = CMParms.combine(commands,0);
		if(commands.size()==0)
		{
			mob.tell(L("You need to specify which plane to create a bubble of."));
			mob.tell(L("Known planes: @x1",planeA.listOfPlanes()+L("Prime Material")));
			return false;
		}
		if(!planeA.getAllPlaneKeys().contains(planeName.toUpperCase()))
		{
			mob.tell(L("'@x1' is not a plane name.",planeName));
			mob.tell(L("Known planes: @x1",planeA.listOfPlanes()+L("Prime Material")));
			return false;
		}

		final Set<MOB> grpMembers = mob.getGroupMembers(new HashSet<MOB>());
		final Room R=mob.location();
		for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
		{
			final MOB M=m.nextElement();
			if((M!=null)
			&&(M!=mob)
			&&(!grpMembers.contains(M))
			&&(!mob.mayIFight(M)))
			{
				mob.tell(L("This powerful magic can not be invoked around @x1.",M.Name()));
				return false;
			}
		}


		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final Physical target = mob.location();
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg = CMClass.getMsg(mob, target, this, somaticCastCode(mob,target,auto), auto?"":L("^S<S-NAME> speak(s) and gesture(s) ominously!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				mob.location().showHappens(CMMsg.MSG_OK_VISUAL,L("The fundamental nature of this place changes..."));
				final Spell_PlanarBubble A;
				if(CMLib.law().doesOwnThisLand(mob,mob.location()))
					A=(Spell_PlanarBubble)maliciousAffect(mob, target, asLevel, 0, -1);
				else
					A=(Spell_PlanarBubble)beneficialAffect(mob, target, asLevel, 3);
				if(A!=null)
				{
					A.planeName = planeName;
					A.newRoom=null;
					A.room();
				}
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> speak(s) and gesture(s) ominously, but the spell fizzles."));

		// return whether it worked
		return success;
	}
}
