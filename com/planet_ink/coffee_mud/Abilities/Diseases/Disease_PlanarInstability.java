package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.PlanarAbility;
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

import java.util.List;

/*
   Copyright 2017-2018 Bo Zimmerman

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

public class Disease_PlanarInstability extends Disease
{
	@Override
	public String ID()
	{
		return "Disease_PlanarInstability";
	}

	private final static String localizedName = CMLib.lang().L("Planar Instability");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String displayText()
	{
		return "(Planar Instability)";
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

	@Override
	public boolean putInCommandlist()
	{
		return false;
	}

	@Override
	public int difficultyLevel()
	{
		return 10;
	}

	@Override
	protected int DISEASE_TICKS()
	{
		return (int)((10 * TimeManager.MILI_MINUTE) / CMProps.getTickMillis());
	}

	@Override
	protected int DISEASE_DELAY()
	{
		return 999999;
	}

	@Override
	protected String DISEASE_DONE()
	{
		return L("You feel more stable.");
	}

	@Override
	protected String DISEASE_START()
	{
		return L("^G<S-NAME> appear(s) to be metaphysically unstable.^?");
	}

	@Override
	protected String DISEASE_AFFECT()
	{
		return "";
	}

	@Override
	public int abilityCode()
	{
		return 0;
	}

	@Override
	public boolean canBeUninvoked()
	{
		return super.canBeUninvoked();
	}

	protected int level=1;

	@Override
	public void setMiscText(String newText)
	{
		level=1;
		if(CMath.isInteger(newText))
			level=CMath.s_int(newText);
		super.setMiscText(newText);
	}

	protected final static int[] MISC_SAVES=new int[] 
	{
		CharStats.STAT_SAVE_BLUNT,
		CharStats.STAT_SAVE_PIERCE,
		CharStats.STAT_SAVE_SLASH,
		CharStats.STAT_SAVE_SPELLS,
		CharStats.STAT_SAVE_PRAYERS,
		CharStats.STAT_SAVE_SONGS,
		CharStats.STAT_SAVE_CHANTS,
		CharStats.STAT_CRIT_CHANCE_PCT_WEAPON,
		CharStats.STAT_CRIT_CHANCE_PCT_MAGIC,
		CharStats.STAT_CRIT_DAMAGE_PCT_WEAPON,
		CharStats.STAT_CRIT_DAMAGE_PCT_MAGIC
	};
	
	@Override
	public void affectCharStats(MOB affectedMob, CharStats affectableStats)
	{
		super.affectCharStats(affectedMob, affectableStats);
		for(int code : CharStats.CODES.SAVING_THROWS())
		{
			final int oldStat=affectableStats.getStat(code);
			if(oldStat > 0)
			{
				affectableStats.setStat(code, oldStat - (10 * level));
				if(affectableStats.getStat(code) < 0)
					affectableStats.setStat(code, 0);
			}
		}
	}

	@Override
	public void affectPhyStats(Physical affected, PhyStats affectableStats)
	{
		super.affectPhyStats(affected,affectableStats);
	}

	@Override
	public void affectCharState(MOB affected, CharState affectableState)
	{
		super.affectCharState(affected,affectableState);
	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if((ticking==affected)
		&&(tickID==Tickable.TICKID_MOB)
		&&(affected instanceof MOB))
		{
		}
		return super.tick(ticking,tickID);
	}
	
	@Override
	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost, msg))
			return false;
		if((msg.source()==affected)
		&&(msg.tool() instanceof PlanarAbility)
		&&(msg.sourceMinor()==CMMsg.TYP_CAST_SPELL)
		&&(CMLib.dice().rollPercentage() > (10 * level)))
		{
			final Room room = msg.source().location();
			if(room != null)
			{
				room.show(msg.source(), null, CMMsg.MSG_OK_ACTION, L("<S-YOUPOSS> planar instability cause(s) <S-HIS-HER> magic to fizzle."));
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;
		final Ability A=target.fetchEffect(ID());
		if(A!=null)
		{
			if(CMath.s_int(A.text())<=0)
				A.setMiscText("1");
			else
			if(CMath.s_int(A.text())<10)
				A.setMiscText(""+(CMath.s_int(A.text())+1));
			return true;
		}

		if(super.invoke(mob,commands,givenTarget,auto,asLevel))
		{
			final Ability A2=target.fetchEffect(ID());
			if(A2!=null)
				A2.setMiscText("1");
			return true;
		}
		return false;
	}
}
