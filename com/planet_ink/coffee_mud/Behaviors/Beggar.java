package com.planet_ink.coffee_mud.Behaviors;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.Druid.Chant_TremorSense;
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
   Copyright 2002-2025 Bo Zimmerman

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
public class Beggar extends StdBehavior
{
	@Override
	public String ID()
	{
		return "Beggar";
	}
	Set<MOB> mobsHitUp=new LimitedTreeSet<MOB>(300000,1000,false);
	int tickTock=0;

	@Override
	public String accountForYourself()
	{
		return "vagrant-like begging";
	}

	@Override
	public CMObject copyOf()
	{
		final Beggar obj=(Beggar)super.copyOf();
		obj.mobsHitUp=new LimitedTreeSet<MOB>(300000,1000,false);
		obj.mobsHitUp.addAll(mobsHitUp);
		return obj;
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((host==null)||(!(host instanceof MOB)))
			return;
		final MOB mob=(MOB)host;
		if((msg.amITarget(mob))
		&&(msg.targetMinor()==CMMsg.TYP_GIVE))
		{
			if(msg.tool() instanceof Coins)
			{
				final Coins C = (Coins)msg.tool();
				final int namt = (int)Math.round(C.getTotalValue());
				if(msg.source().isPlayer() && (namt > 0))
					CMLib.achievements().possiblyBumpAchievement(msg.source(), AchievementLibrary.Event.CHARITY, namt, mob);
				msg.addTrailerRunnable(new Runnable() {
					final MOB M = mob;
					final double amt = C.getTotalValue();
					final String curr = C.getCurrency();
					@Override
					public void run()
					{
						CMLib.beanCounter().subtractMoney(M,curr,amt);
					}
				});
			}
			msg.addTrailerMsg(CMClass.getMsg(mob,msg.source(),CMMsg.MSG_SPEAK,L("^T<S-NAME> say(s) 'Thank you gov'ner!' to <T-NAME> ^?")));
		}
	}

	@Override
	public boolean tick(final Tickable ticking, final int tickID)
	{
		super.tick(ticking,tickID);

		if(tickID!=Tickable.TICKID_MOB)
			return true;
		if(!canFreelyBehaveNormal(ticking))
			return true;
		if(CMSecurity.isDisabled(CMSecurity.DisFlag.EMOTERS))
			return true;
		tickTock++;
		if(tickTock<5)
			return true;
		tickTock=0;
		final MOB mob=(MOB)ticking;
		for(int i=0;i<mob.location().numInhabitants();i++)
		{
			final MOB mob2=mob.location().fetchInhabitant(i);
			if((mob2!=null)
			   &&(CMLib.flags().canBeSeenBy(mob2,mob))
			   &&(mob2!=mob)
			   &&(!mobsHitUp.contains(mob2))
			   &&(!mob2.isMonster()))
			{
				String cl = CMLib.beanCounter().nameCurrencyLong(mob, 1.0);
				while((cl.length()>0)&&(Character.isDigit(cl.charAt(0))))
					cl=cl.substring(1);
				cl=cl.trim();
				switch(CMLib.dice().roll(1,10,0))
				{
				case 1:
					CMLib.commands().postSay(mob,mob2,L("A little something for a vet please?"),false,false);
					break;
				case 2:
					CMLib.commands().postSay(mob,mob2,L("Spare a @x1 @x2",cl,mob2.charStats().MisterMadam()+"?"),false,false);
					break;
				case 3:
					CMLib.commands().postSay(mob,mob2,L("Spare some change?"),false,false);
					break;
				case 4:
					CMLib.commands().postSay(mob,mob2,L("Please @x1, a little something for a(n) @x2 down on @x3 luck?",
							mob2.charStats().MisterMadam(),
							mob.charStats().manwoman(),
							mob.charStats().hisher()),false,false);
					break;
				case 5:
					CMLib.commands().postSay(mob,mob2,L("Hey, I lost my 'Will Work For Food' sign.  "
							+ "Can you spare me the money to buy one?"),false,false);
					break;
				case 6:
					CMLib.commands().postSay(mob,mob2,L("Spread a little joy to a poor @x1?",
							((mob.charStats().reproductiveCode()=='M')?"fella":"gal")),
							false,false);
					break;
				case 7:
					CMLib.commands().postSay(mob,mob2,L("Change?"),false,false);
					break;
				case 8:
					CMLib.commands().postSay(mob,mob2,L("Can you spare a little change?"),false,false);
					break;
				case 9:
					CMLib.commands().postSay(mob,mob2,L("Can you spare a @x1?",cl),false,false);
					break;
				case 10:
					CMLib.commands().postSay(mob,mob2,L("@x1 for a poor soul down on @x2 luck?",
														CMStrings.capitalizeAndLower(cl),
														mob.charStats().hisher()
														),false,false);
					break;
				}
				mobsHitUp.add(mob2);
				break;
			}
		}
		return true;
	}
}
