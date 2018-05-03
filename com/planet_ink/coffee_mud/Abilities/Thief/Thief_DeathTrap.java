package com.planet_ink.coffee_mud.Abilities.Thief;
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
public class Thief_DeathTrap extends ThiefSkill implements Trap
{
	@Override
	public String ID()
	{
		return "Thief_DeathTrap";
	}

	private final static String	localizedName	= CMLib.lang().L("Death Trap");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	protected int canAffectCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_ROOMS;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	private static final String[]	triggerStrings	= I(new String[] { "DEATHTRAP" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT | USAGE_MANA;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_TRAPPING;
	}

	protected boolean	sprung	= false;
	protected int		attempts = 0;

	@Override
	public boolean disabled()
	{
		return false;
	}

	@Override
	public void disable()
	{
		unInvoke();
	}

	@Override
	public boolean sprung()
	{
		return false;
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
	public void setReset(int Reset)
	{
	}

	@Override
	public int getReset()
	{
		return CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY);
	}

	@Override
	public void resetTrap(MOB mob)
	{
	}

	@Override
	public boolean maySetTrap(MOB mob, int asLevel)
	{
		return false;
	}

	@Override
	public boolean canSetTrapOn(MOB mob, Physical P)
	{
		return false;
	}

	@Override
	public boolean canReSetTrap(MOB mob)
	{
		return false;
	}

	@Override
	public String requiresToSet()
	{
		return "";
	}

	@Override
	public List<Item> getTrapComponents()
	{
		final List<Item> V=new Vector<Item>();
		for(int i=0;i<100;i++)
		V.add(CMLib.materials().makeItemResource(RawMaterial.RESOURCE_IRON));
		return V;
	}

	@Override
	public Trap setTrap(MOB mob, Physical P, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(P==null)
			return null;
		final Trap T=(Trap)copyOf();
		T.setInvoker(mob);
		P.addEffect(T);
		final int duration = CMProps.getIntVar(CMProps.Int.TICKSPERMUDDAY)+(2*getXLEVELLevel(mob));
		CMLib.threads().startTickDown(T,Tickable.TICKID_TRAP_DESTRUCTION,duration);
		return T;
	}

	@Override
	public void spring(MOB M)
	{
		if((!sprung)
		&&(CMLib.dice().rollPercentage()+(2*getXLEVELLevel(invoker()))>M.charStats().getSave(CharStats.STAT_SAVE_TRAPS)))
		{
			CMLib.combat().postDeath(invoker(),M,null);
			if(M.amDead())
			{
				sprung=true;
				//tickDown=getReset();
				//CMLib.threads().startTickDown(this,Tickable.TICKID_TRAP_RESET,1);
			}
		}
		if((--attempts)<0)
		{
			sprung=true;
			if(affected != null)
				unInvoke();
		}
	}

	@Override
	public MOB invoker()
	{
		if(super.miscText.length()==0)
			return super.invoker();
		MOB M=super.invoker();
		if((M!=null)&&(M.Name().equals(miscText)))
			return M;
		M=CMLib.players().getLoadPlayer(miscText);
		if(M==null)
			miscText="";
		else
			invoker=M;
		return super.invoker();
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		if((msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target()==affected)
		&&(msg.source()!=invoker())
		&&(!msg.source().Name().equals(text()))
		&&(!sprung)
		&&(invoker()!=null)
		&&(invoker().mayIFight(msg.source()))
		&&((canBeUninvoked())||(!CMLib.law().doesHavePriviledgesHere(msg.source(),(Room)affected)))
		&&(!invoker().getGroupMembers(new HashSet<MOB>()).contains(msg.source())))
			spring(msg.source());
		super.executeMsg(myHost,msg);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if(tickID==Tickable.TICKID_TRAP_RESET)
		{
			sprung=false;
			return false;
		}
		else
		if(tickID==Tickable.TICKID_TRAP_DESTRUCTION)
		{
			unInvoke();
			return false;
		}
		return true;
	}

	@Override
	public int castingQuality(MOB mob, Physical target)
	{
		if(mob!=null)
		{
			if(mob.isInCombat())
				return Ability.QUALITY_INDIFFERENT;
		}
		return super.castingQuality(mob,target);
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Room trapThis=mob.location();

		Item resource=CMLib.materials().findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_METAL);
		if(resource == null)
			resource=CMLib.materials().findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_MITHRIL);
		int amount=0;
		if(resource!=null)
			amount=CMLib.materials().findNumberOfResource(mob.location(),resource.material());
		if((amount<100)||(resource==null))
		{
			mob.tell(L("You need 100 pounds of raw metal to build this trap."));
			return false;
		}
		if(mob.isInCombat())
		{
			mob.tell(L("You are too busy to get that done right now."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		CMLib.materials().destroyResourcesValue(mob.location(),100, resource.material(), -1, null);

		final CMMsg msg=CMClass.getMsg(mob,trapThis,this,auto?CMMsg.MSG_OK_ACTION:CMMsg.MSG_THIEF_ACT,CMMsg.MASK_ALWAYS|CMMsg.MSG_DELICATE_HANDS_ACT,CMMsg.MSG_OK_ACTION,(auto?L("@x1 begins to glow!",trapThis.name()):L("<S-NAME> attempt(s) to lay a trap here.")));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			attempts =  (RawMaterial.CODES.HARDNESS(resource.material())+super.getXLEVELLevel(mob))*2; 
			if(success)
			{
				mob.tell(L("You have set the trap."));
				setTrap(mob,trapThis,mob.charStats().getClassLevel(mob.charStats().getCurrentClass()),(CMLib.ableMapper().qualifyingClassLevel(mob,this)-CMLib.ableMapper().lowestQualifyingLevel(ID()))+1,false);
				final Thief_DeathTrap T=(Thief_DeathTrap)trapThis.fetchEffect(ID());
				if(T!=null)
				{
					T.setMiscText(mob.Name());
					T.attempts=attempts;
				}
			}
			else
			{
				if(CMLib.dice().rollPercentage()>50+(5*super.getXLEVELLevel(mob)))
				{
					final Trap T=setTrap(mob,trapThis,mob.charStats().getClassLevel(mob.charStats().getCurrentClass()),(CMLib.ableMapper().qualifyingClassLevel(mob,this)-CMLib.ableMapper().lowestQualifyingLevel(ID()))+1,false);
					mob.location().show(mob,null,CMMsg.MSG_OK_ACTION,L("<S-NAME> trigger(s) the trap on accident!"));
					T.spring(mob);
				}
				else
				{
					mob.tell(L("You fail in your attempt to set the death trap."));
				}
			}
		}
		return success;
	}
}
