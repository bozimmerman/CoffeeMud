package com.planet_ink.coffee_mud.Abilities.Misc;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
   Copyright 2006-2018 Bo Zimmerman

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

public class Bleeding extends StdAbility implements HealthCondition
{
	@Override
	public String ID()
	{
		return "Bleeding";
	}

	private final static String	localizedName	= CMLib.lang().L("Bleeding");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Bleeding)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	private static final String[]	triggerStrings	= I(new String[] { "MAKEBLEED" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_ITEMS | Ability.CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	protected int	hpToKeep	= -1;
	protected int	lastDir		= -1;
	protected Room	lastRoom	= null;

	public double healthPct(MOB mob)
	{
		return CMath.div(mob.curState().getHitPoints(), mob.maxState().getHitPoints());
	}

	@Override
	public String getHealthConditionDesc()
	{
		return "Bleeding externally and excessively.";
	}

	@Override
	public void unInvoke()
	{
		if((affected instanceof MOB)
		&&(canBeUninvoked())
		&&(!((MOB)affected).amDead())
		&&(CMLib.flags().isInTheGame((MOB)affected,true)))
			((MOB)affected).location().show((MOB)affected,null,null,CMMsg.MSG_OK_VISUAL,L("<S-NAME> stop(s) bleeding."));
		super.unInvoke();
	}

	@Override
	public void  executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)&&(msg.amITarget(affected))&&(affected instanceof MOB))
		{
			if(msg.targetMinor()==CMMsg.TYP_HEALING)
			{
				hpToKeep=-1;
				if(healthPct((MOB)affected)>0.50)
					unInvoke();
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_LOOK)
			||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
				msg.source().tell((MOB)msg.target(),null,null,L("^R<S-NAME> <S-IS-ARE> still bleeding..."));
		}
		else
		if((msg.source()==affected)
		&&(msg.target() instanceof Room)
		&&(msg.tool() instanceof Exit)
		&&(msg.targetMinor()==CMMsg.TYP_LEAVE))
		{
			final Room R=(Room)msg.target();
			int dir=-1;
			for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
			{
				if(msg.tool()==R.getReverseExit(d))
					dir=d;
			}
			if((dir>=0)&&(R.findItem(null,"a trail of blood")==null))
			{
				final Item I=CMClass.getItem("GenFatWallpaper");
				I.setName(L("A trail of blood"));
				if(lastDir>=0)
					I.setDisplayText(L("A faint trail of blood leads from @x1 to @x2.",CMLib.directions().getDirectionName(lastDir),CMLib.directions().getDirectionName(dir)));
				else
					I.setDisplayText(L("A faint trail of blood leads @x1.",CMLib.directions().getDirectionName(dir)));
				I.phyStats().setDisposition(I.phyStats().disposition()|PhyStats.IS_HIDDEN|PhyStats.IS_UNSAVABLE);
				I.setSecretIdentity(msg.source().Name()+"`s blood.");
				R.addItem(I,ItemPossessor.Expire.Monster_EQ);
			}
			lastDir=Directions.getOpDirectionCode(dir);
			lastRoom=R;
		}
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((ticking instanceof MOB)&&(tickID==Tickable.TICKID_MOB))
		{
			final MOB mob=(MOB)ticking;
			if(hpToKeep<=0)
			{
				hpToKeep=mob.curState().getHitPoints();
				mob.recoverMaxState();
			}
			else
			{
				if(mob.curState().getHitPoints()>hpToKeep)
					mob.curState().setHitPoints(hpToKeep);
				final int maxMana=(int)Math.round(CMath.mul(mob.maxState().getMana(),healthPct(mob)));
				if(mob.curState().getMana()>maxMana)
					mob.curState().setMana(maxMana);
				final int maxMovement=(int)Math.round(CMath.mul(mob.maxState().getMovement(),healthPct(mob)));
				if(mob.curState().getMovement()>maxMovement)
					mob.curState().setMovement(maxMovement);
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical target, boolean auto, int asLevel)
	{
		if(target==null)
			target=mob;
		if(!(target instanceof MOB))
			return false;
		if(CMLib.flags().isGolem(target))
			return false;
		if(((MOB)target).phyStats().level()<CMProps.getIntVar(CMProps.Int.INJBLEEDMINLEVEL))
			return false;
		if(((MOB)target).fetchEffect(ID())!=null)
			return false;
		if(((MOB)target).location()==null)
			return false;
		if(((MOB)target).amDead())
			return false;
		if(((MOB)target).location().show((MOB)target,null,this,CMMsg.MSG_OK_VISUAL,L("^R<S-NAME> start(s) BLEEDING!^?")))
			beneficialAffect(mob,target,asLevel,0);
		return true;
	}
}
