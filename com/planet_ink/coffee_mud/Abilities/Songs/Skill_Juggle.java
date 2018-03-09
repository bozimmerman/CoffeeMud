package com.planet_ink.coffee_mud.Abilities.Songs;
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
   Copyright 2003-2018 Bo Zimmerman

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
@SuppressWarnings({"unchecked","rawtypes"})
public class Skill_Juggle extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_Juggle";
	}

	private final static String localizedName = CMLib.lang().L("Juggle");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return CAN_MOBS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[] triggerStrings =I(new String[] {"JUGGLE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_FOOLISHNESS;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	protected SVector<Item> juggles=new SVector<Item>();
	protected long lastJuggle=-1;
	protected boolean pause=false;

	@Override
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		juggles=new SVector();
	}

	public int maxJuggles()
	{
		if(affected instanceof MOB)
			return 5+(CMLib.ableMapper().qualifyingClassLevel((MOB)affected,this)+(2*getXLEVELLevel((MOB)affected)));
		return 5;
	}

	public int maxAttacks()
	{
		if(affected instanceof MOB)
			return (int)Math.round(affected.phyStats().speed())
				   +((CMLib.ableMapper().qualifyingClassLevel((MOB)affected,this)+(2*getXLEVELLevel((MOB)affected)))/5);
		return 1;
	}

	@Override
	public String displayText()
	{
		if(juggles.size()>0)
		{
			final StringBuffer str=new StringBuffer(L("(Juggling: "));
			final SVector<Item> V=juggles.copyOf();
			for(int i=0;i<V.size();i++)
			{
				final Item I=V.elementAt(i);
				boolean back=false;
				for(int ii=0;ii<i;ii++)
				{
					final Item I2=V.elementAt(ii);
					if(I2.name().equals(I.name()))
					{
						back=true;
						break;
					}
				}
				if(back)
					continue;
				boolean morethanone=false;
				for(int ii=i+1;ii<V.size();ii++)
				{
					final Item I2=V.elementAt(ii);
					if(I2.name().equals(I.name()))
					{
						morethanone=true;
						break;
					}
				}
				str.append(I.name()+(morethanone?"s":"")+" ");
			}
			return str.toString()+")";
		}
		return "(Juggling??)";
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_PUSH)||(msg.targetMinor()==CMMsg.TYP_PULL))
		&&(msg.target() instanceof Item)
		&&(juggles.contains(msg.target()))
		&&(affected instanceof MOB)
		&&(CMLib.dice().rollPercentage()<90)
		&&(msg.source()!=affected))
		{
			msg.source().tell(msg.source(),msg.target(),null,L("<T-NAME> is moving too fast for you to grab it."));
			return false;
		}
		return true;
	}

	protected void unJuggle(Item I)
	{
		if(I==null)
			return;
		final Ability A=I.fetchEffect("Spell_Fly");
		if(A!=null)
			A.unInvoke();
		juggles.removeElement(I);
	}

	public void juggleItem(Item I)
	{
		if(I==null)
			return;
		if(juggles.contains(I))
			return;
		if(I.fetchEffect("Spell_Fly")==null)
		{
			final Ability A=CMClass.getAbility("Spell_Fly");
			if(A!=null)
			{
				I.addEffect(A);
				A.makeLongLasting();
				A.setSavable(false);
				I.recoverPhyStats();
			}
		}
		juggles.addElement(I);
	}

	protected synchronized void juggle()
	{
		boolean anythingToDo=false;
		if(!(affected instanceof MOB))
			return;
		final MOB M=(MOB)affected;
		final Room R=M.location();
		if(R==null)
			return;
		for(int i=0;i<juggles.size();i++)
		{
			Item I=null;
			try
			{
				I=juggles.elementAt(i);
			}
			catch(final Exception e)
			{
			}
			if((I==null)
			||(I.owner()==null)
			||((I.owner() instanceof MOB)&&(I.owner()!=M))
			||((I.owner() instanceof Room)&&(I.owner()!=R)))
			{
				anythingToDo=true;
				break;
			}
		}
		if(anythingToDo)
		{
			final SVector<Item> copy=juggles.copyOf();
			for(int i=0;i<copy.size();i++)
			{
				final Item I=copy.elementAt(i);
				if((I.owner()==null)
				||((I.owner() instanceof MOB)&&(I.owner()!=M)))
					unJuggle(I);
				else
				if((I.owner() instanceof Room)&&(I.owner()!=R))
					R.moveItemTo(I,ItemPossessor.Expire.Player_Drop);
			}
		}
		pause=true;
		for(int i=0;i<M.numItems();i++)
		{
			final Item I=M.getItem(i);
			if((I!=null)
			&&((I.amWearingAt(Wearable.WORN_WIELD)||I.amWearingAt(Wearable.WORN_HELD)))
			&&(!juggles.contains(I))
			&&(juggles.size()<maxJuggles()))
			{
				if(M.location().show(M,I,CMMsg.MSG_DELICATE_HANDS_ACT,L("<S-NAME> start(s) juggling <T-NAMESELF>.")))
					juggleItem(I);
				else
				{
					unJuggle(I);
					CMLib.commands().postDrop(M,I,false,false,false);
					break;
				}
			}
		}
		pause=false;
		if(juggles.size()==0)
		{
			unInvoke();
			return;
		}
		if(lastJuggle>(System.currentTimeMillis()-500))
			return;
		lastJuggle=System.currentTimeMillis();
		final SVector<Item> copy=juggles.copyOf();
		int jug=-1;
		for(int i=0;i<copy.size();i++)
		{
			final Item I=copy.elementAt(i);
			if(I.amWearingAt(Wearable.WORN_WIELD)||I.amWearingAt(Wearable.WORN_HELD))
			{
				I.setRawWornCode(Wearable.IN_INVENTORY);
				jug=i;
			}
		}
		jug++;
		if((jug<0)||(jug>=copy.size()-2))
			jug=0;
		for(int i=0;i<copy.size();i++)
		{
			final Item I=copy.elementAt(i);
			if((i==jug)||(i==jug+1))
			{
				if(!M.isMine(I))
					M.moveItemTo(I);
				if(i==jug)
					I.setRawWornCode(Wearable.WORN_WIELD);
				else
					I.setRawWornCode(Wearable.WORN_HELD);
			}
			else
			{
				I.unWear();
				if(!M.location().isContent(I))
					M.location().moveItemTo(I,ItemPossessor.Expire.Player_Drop);
			}
		}
		M.recoverPhyStats();
		M.recoverCharStats();
		M.recoverMaxState();
		M.location().recoverRoomStats();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if(!pause)
		{
			juggle();
			if(((msg.targetMinor()==CMMsg.TYP_GET)||(msg.targetMinor()==CMMsg.TYP_REMOVE))
			&&(msg.target() instanceof Item)
			&&(juggles.contains(msg.target())))
			{
				unJuggle((Item)msg.target());
				if(juggles.size()==0)
					unInvoke();
			}
			else
			if((invoker()!=null)
			&&(!unInvoked)
			&&(affected==invoker())
			&&(msg.amISource(invoker()))
			&&(msg.target() instanceof Armor)
			&&(msg.targetMinor()==CMMsg.TYP_WEAR))
				unInvoke();
		}
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!pause)
		{
			juggle();
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(juggles.size()>0))
			{
				final MOB mob=(MOB)affected;
				if(mob.location()!=null)
				{
					if(!mob.location().show(mob,null,CMMsg.MSG_NOISYMOVEMENT,L("<S-NAME> juggle(s) @x1 items in the air.",""+juggles.size())))
						unInvoke();
					else
					if(mob.isInCombat())
					{
						final SVector<Item> copy=juggles.copyOf();
						final int maxAttacks=maxAttacks();
						for(int i=0;((i<maxAttacks)&&(copy.size()>0));i++)
						{
							final Item I=copy.elementAt(CMLib.dice().roll(1,copy.size(),-1));
							I.unWear();
							mob.moveItemTo(I);
							if((mob.isMine(I))&&(CMLib.commands().postDrop(mob,I,true,false,false)))
							{
								final Weapon w=CMClass.getWeapon("StdWeapon");
								w.setName(I.name());
								w.setDisplayText(I.displayText());
								w.setDescription(I.description());
								copy.removeElement(I);
								unJuggle(I);
								w.setWeaponClassification(Weapon.CLASS_THROWN);
								w.setRanges(0,10);
								if(I instanceof Weapon)
									w.setWeaponDamageType(((Weapon)I).weaponDamageType());
								else
									w.setWeaponDamageType(Weapon.TYPE_BASHING);
								w.basePhyStats().setDamage(CMLib.dice().roll(1,adjustedLevel(mob,0),0));
								w.basePhyStats().setWeight(I.basePhyStats().weight());
								w.recoverPhyStats();
								CMLib.combat().postAttack(mob,mob.getVictim(),w);
								w.destroy();
							}
							else
								break;
						}
					}
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof MOB)&&(super.canBeUninvoked()))
		{
			final MOB M=(MOB)affected;
			while(juggles.size()>0)
			{
				final Item I=juggles.elementAt(0);
				M.location().show(M,I,CMMsg.MSG_OK_ACTION,L("<S-NAME> stop(s) juggling <T-NAMESELF>."));
				unJuggle(I);
				I.unWear();
				if(!M.isMine(I))
					M.moveItemTo(I);
			}
		}
		super.unInvoke();
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat()&&(mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,"all")!=null))
				return super.castingQuality(mob, target,Ability.QUALITY_BENEFICIAL_SELF);
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		String whatToJuggle=(mob.isMonster()&&(givenTarget instanceof MOB))?"all":CMParms.combine(commands,0);
		Skill_Juggle A=(Skill_Juggle)mob.fetchEffect("Skill_Juggle");
		if(whatToJuggle.length()==0)
		{
			if(A==null)
			{
				mob.tell(L("Juggle what?"));
				return false;
			}
			mob.tell(L("You stop juggling."));
			A.unInvoke();
			return true;
		}

		if((A!=null)&&(A.juggles.size()>=A.maxJuggles()))
		{
			mob.tell(L("You are already juggling the most items you can."));
			return false;
		}

		int maxToJuggle=Integer.MAX_VALUE;
		if((commands.size()>1)
		&&(CMath.s_int(commands.get(0))>0))
		{
			maxToJuggle=CMath.s_int(commands.get(0));
			commands.set(0,"all");
		}

		final Vector<Item> V=new Vector<Item>();
		boolean allFlag=commands.get(0).equalsIgnoreCase("all");
		if(whatToJuggle.toUpperCase().startsWith("ALL."))
		{
			allFlag=true;
			whatToJuggle="ALL "+whatToJuggle.substring(4);
		}
		if(whatToJuggle.toUpperCase().endsWith(".ALL"))
		{
			allFlag=true;
			whatToJuggle="ALL "+whatToJuggle.substring(0,whatToJuggle.length()-4);
		}
		int addendum=1;
		String addendumStr="";
		boolean doBugFix = true;
		while(doBugFix || ((allFlag)&&(addendum<=maxToJuggle)))
		{
			doBugFix=false;
			final Item juggleThis=mob.findItem(null,whatToJuggle+addendumStr);
			if((juggleThis!=null)&&(!juggleThis.amWearingAt(Wearable.IN_INVENTORY)))
			{
				if((!juggleThis.amWearingAt(Wearable.WORN_HELD))&&(!juggleThis.amWearingAt(Wearable.WORN_WIELD)))
				{
					addendumStr="."+(++addendum);
					continue;
				}
				else
				if(!CMLib.commands().postRemove(mob,juggleThis,true))
					return false;
			}
			if(juggleThis==null)
				break;
			if((CMLib.flags().canBeSeenBy(juggleThis,mob))
			&&((A==null)||(!A.juggles.contains(juggleThis)))
			&&(!V.contains(juggleThis)))
				V.addElement(juggleThis);
			addendumStr="."+(++addendum);
		}

		if(V.size()==0)
		{
			mob.tell(L("You don't seem to be carrying that."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			if(A==null)
			{
				beneficialAffect(mob,mob,asLevel,0);
				A=(Skill_Juggle)mob.fetchEffect(ID());
				if(A==null)
					return false;
			}
			A.makeLongLasting();
			A.pause=true;
			for(int i=0;i<V.size();i++)
			{
				final Item I=V.elementAt(i);
				final CMMsg msg=CMClass.getMsg(mob,I,this,CMMsg.MSG_DELICATE_HANDS_ACT,L("<S-NAME> start(s) juggling <T-NAMESELF>."));
				if((A.juggles.size()<A.maxJuggles())
				&&(mob.location().okMessage(mob,msg)))
				{
					mob.location().send(mob,msg);
					A.juggleItem(I);
				}
				else
					break;
			}
			A.pause=false;
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> attempt(s) to juggle, but messes up."));

		// return whether it worked
		return success;
	}
}
