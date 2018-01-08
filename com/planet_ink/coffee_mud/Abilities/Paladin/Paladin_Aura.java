package com.planet_ink.coffee_mud.Abilities.Paladin;
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
   Copyright 2002-2018 Bo Zimmerman

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

public class Paladin_Aura extends PaladinSkill
{
	@Override
	public String ID()
	{
		return "Paladin_Aura";
	}

	private final static String localizedName = CMLib.lang().L("Paladin`s Aura");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_HOLYPROTECTION;
	}

	public Paladin_Aura()
	{
		super();
		paladinsGroup=new HashSet<MOB>();
	}

	protected boolean pass=false;

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		pass=(invoker==null)||(invoker.fetchAbility(ID())==null)||proficiencyCheck(null,0,false);
		final Room R=CMLib.map().roomLocation(invoker != null ? invoker : (affected != null ? affected : (ticking instanceof Physical ? (Physical)ticking : null)));
		if(pass && (R!=null))
		{
			for(final Enumeration<MOB> m=R.inhabitants();m.hasMoreElements();)
			{
				final MOB mob=m.nextElement();
				if(paladinsGroup.contains(mob) && CMLib.flags().isEvil(mob))
				{
					final int damage=(int)Math.round(CMath.div(mob.phyStats().level()+(2*getXLEVELLevel(invoker)),3.0));
					final MOB invoker=(invoker()!=null) ? invoker() : mob;
					CMLib.combat().postDamage(invoker,mob,this,damage,CMMsg.MASK_MALICIOUS|CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,L("^SThe aura around <S-NAME> <DAMAGES> <T-NAME>!^?"));
				}
			}
		}
		return true;
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((invoker==null)||(!(CMLib.flags().isGood(invoker))))
			return true;
		if(affected==null)
			return true;
		if(!(affected instanceof MOB))
			return true;

		if((msg.target() instanceof MOB)
		   &&(paladinsGroup.contains(msg.target()))
		   &&(!paladinsGroup.contains(msg.source()))
		   &&(pass)
		   &&(msg.source()!=invoker))
		{
			if((CMath.bset(msg.targetMajor(),CMMsg.MASK_MALICIOUS))
			&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
			&&(msg.tool() instanceof Ability)
			&&(!CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_HOLY))
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_UNHOLY)))
			{
				msg.source().location().show((MOB)msg.target(),null,CMMsg.MSG_OK_VISUAL,L("The holy field around <S-NAME> protect(s) <S-HIM-HER> from the evil magic attack of @x1.",msg.source().name()));
				return false;
			}
			if(((msg.targetMinor()==CMMsg.TYP_POISON)||(msg.targetMinor()==CMMsg.TYP_DISEASE))
			&&(pass))
				return false;
		}
		return true;
	}
}
