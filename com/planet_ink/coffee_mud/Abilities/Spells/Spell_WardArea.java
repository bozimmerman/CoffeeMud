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
   Copyright 2003-2025 Bo Zimmerman

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
public class Spell_WardArea extends Spell implements Trap
{

	@Override
	public String ID()
	{
		return "Spell_WardArea";
	}

	private final static String	localizedName	= CMLib.lang().L("Ward Area");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Ward Area spell)");

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

	private Ability			shooterA		= null;
	protected List<String>	parameters	= null;

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SPELL | Ability.DOMAIN_EVOCATION;
	}

	protected boolean	sprung	= false;

	public MOB theInvoker()
	{
		if(invoker()!=null)
			return invoker();
		if(text().length()>0)
			invoker=CMLib.players().getPlayerAllHosts(text());
		return invoker();
	}

	@Override
	public boolean isABomb()
	{
		return false;
	}

	@Override
	public void activateBomb()
	{
	}

	@Override
	public boolean disabled()
	{
		return sprung;
	}

	@Override
	public void disable()
	{
		unInvoke();
	}

	@Override
	public void setReset(final int Reset)
	{
	}

	@Override
	public int getReset()
	{
		return 0;
	}

	@Override
	public boolean maySetTrap(final MOB mob, final int asLevel)
	{
		return false;
	}

	@Override
	public boolean canSetTrapOn(final MOB mob, final Physical P)
	{
		return false;
	}

	@Override
	public boolean canReSetTrap(final MOB mob)
	{
		return false;
	}

	@Override
	public List<Item> getTrapComponents()
	{
		return new Vector<Item>(1);
	}

	@Override
	public String requiresToSet()
	{
		return "";
	}

	@Override
	public void resetTrap(final MOB mob)
	{
	}

	@Override
	public Trap setTrap(final MOB mob, final Physical P, final int trapBonus, final int qualifyingClassLevel, final boolean perm)
	{
		beneficialAffect(mob, P, qualifyingClassLevel + trapBonus, 0);
		return (Trap) P.fetchEffect(ID());
	}

	@Override
	public boolean sprung()
	{
		return sprung;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(sprung)
			return super.okMessage(myHost,msg);
		if(!super.okMessage(myHost,msg))
			return false;

		if((msg.amITarget(affected))
		&&(!msg.amISource(invoker())))
		{
			if((msg.targetMinor()==CMMsg.TYP_ENTER)
			||(msg.targetMinor()==CMMsg.TYP_LEAVE)
			||(msg.targetMinor()==CMMsg.TYP_FLEE))
			{
				if(msg.targetMinor()==CMMsg.TYP_LEAVE)
					return true;
				spring(msg.source());
				if(sprung)
					return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean isLocalExempt(final MOB target)
	{
		if(target==null)
			return false;
		final Room R=target.location();
		if((!canBeUninvoked())
		&&(!isABomb())
		&&(R!=null))
		{
			if((CMLib.law().getLandTitle(R)!=null)
			&&(CMLib.law().doesHavePriviledgesHere(target,R)))
				return true;

			if((target.isMonster())
			&&(target.getStartRoom()!=null)
			&&(target.getStartRoom().getArea()==R.getArea()))
				return true;
		}
		return false;
	}

	protected boolean canInvokeTrapOn(final MOB invoker, final MOB target)
	{
		if((invoker==null)
		||(invoker.mayIFight(target)
			&&(!invoker.getGroupMembers(new HashSet<MOB>()).contains(target))))
		{
			if(!isLocalExempt(target))
				return true;
		}
		return false;
	}

	@Override
	public void spring(final MOB mob)
	{
		if(affected==null)
			return;
		if(!(affected instanceof Room))
			return;
		if((shooterA==null)||(parameters==null)||(mob==null))
			return;
		if((invoker()!=null)&&(!invoker().mayIFight(mob)))
			return;
		if((!canInvokeTrapOn(invoker(),mob))
		||(CMLib.dice().rollPercentage()<mob.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
			mob.location().show(mob,affected,this,CMMsg.MSG_OK_ACTION,L("<S-NAME> avoid(s) a magical ward trap."));
		else
		{
			final MOB newCaster=CMClass.getMOB("StdFactoryMOB");
			newCaster.setName(L("the thin air"));
			newCaster.setDescription(" ");
			newCaster.setDisplayText(" ");
			if(invoker()!=null)
				newCaster.basePhyStats().setLevel(invoker.phyStats().level()+super.getXLEVELLevel(invoker()));
			else
				newCaster.basePhyStats().setLevel(10);
			newCaster.recoverPhyStats();
			newCaster.recoverCharStats();
			if(invoker()!=null)
				newCaster.setLiegeID(invoker().Name());
			newCaster.setLocation((Room)affected);
			try
			{
				shooterA.invoke(newCaster,parameters,mob,true,0);
			}
			catch (final Exception e)
			{
				Log.errOut("WARD/" + CMParms.combine(parameters, 0), e);
			}
			newCaster.setLocation(null);
			newCaster.destroy();
		}
		unInvoke();
		sprung=true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if(sprung)
			return;

		if((msg.amITarget(affected))
		&&(!msg.amISource(invoker())))
		{
			if(msg.targetMinor()==CMMsg.TYP_LEAVE)
				spring(msg.source());
		}
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		super.unInvoke();
		if(canBeUninvoked())
		{
			shooterA=null;
			parameters=null;
		}
	}

	@Override
	public int castingQuality(final MOB mob, final Physical target)
	{
		if(mob!=null)
		{
			if(mob.isMonster())
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
		if(commands.size()<1)
		{
			mob.tell(L("You must specify what arcane spell to set, and any necessary parameters."));
			return false;
		}
		commands.add(0,"CAST");
		shooterA=CMLib.english().getSkillToInvoke(mob,commands);
		parameters=commands;
		if((shooterA==null)||((shooterA.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_SPELL))
		{
			parameters=null;
			shooterA=null;
			mob.tell(L("You don't know any arcane spell by that name."));
			return false;
		}
		shooterA = (Ability)shooterA.copyOf();

		if(shooterA.enchantQuality()==Ability.QUALITY_MALICIOUS)
		{
			for(int m=0;m<mob.location().numInhabitants();m++)
			{
				final MOB M=mob.location().fetchInhabitant(m);
				if((M!=null)&&(M!=mob)&&(!M.mayIFight(mob)))
				{
					mob.tell(L("You cannot set that spell here -- there are other players present!"));
					return false;
				}
			}
		}
		final Physical target = mob.location();
		if((target.fetchEffect(this.ID())!=null)||(givenTarget!=null))
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("A ward trap has already been set here!"));
			if(mob.location().okMessage(mob,msg))
				mob.location().send(mob,msg);
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{

			setMiscText(shooterA.ID());
			final CMMsg msg = CMClass.getMsg(mob, target, this, verbalCastCode(mob,target,auto), auto?"":L("^S<S-NAME> set(s) a magical trap.^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				setMiscText(mob.Name());
				if(CMLib.law().doesOwnThisLand(mob,mob.location()))
				{
					mob.location().addNonUninvokableEffect((Ability)copyOf());
					CMLib.database().DBUpdateRoom(mob.location());
				}
				else
					beneficialAffect(mob,mob.location(),asLevel,9999);
				shooterA=null;
				parameters=null;
			}
		}
		else
			return beneficialWordsFizzle(mob,null,L("<S-NAME> attempt(s) to set a magic trap, but fail(s)."));

		// return whether it worked
		return success;
	}
}
