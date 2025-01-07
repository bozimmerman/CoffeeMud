package com.planet_ink.coffee_mud.Abilities.Druid;
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
   Copyright 2024-2024 Bo Zimmerman

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
public class Chant_CurseFortune extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_CurseFortune";
	}

	private final static String localizedName = CMLib.lang().L("Curse Fortune");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Cursed Fortune)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT|Ability.DOMAIN_CURSING;
	}

	protected HashSet<Environmental> permProts=new HashSet<Environmental>();
	protected int prots=4;
	boolean notAgain=false;

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(L("Your cursed fortune fades."));
	}

	@Override
	public void affectCharStats(final MOB affected, final CharStats affectableStats)
	{
		final int amt=100+(10*getXLEVELLevel(invoker()));
		for(final int i : CharStats.CODES.SAVING_THROWS())
			affectableStats.setStat(i,-amt);
	}

	@Override
	public boolean okMessage(final Environmental host, final CMMsg msg)
	{

		if((msg.target()==affected)
		&&(affected instanceof MOB)
		&&((msg.tool()==null)||(!permProts.contains(msg.tool())))
		&&(prots>0)
		&&(msg.source().location()!=null))
		{
			boolean proceed=false;
			final int sm=msg.sourceMinor();
			final int tm=msg.targetMinor();
			final int[] CMMSGMAP=CharStats.CODES.CMMSGMAP();
			for(final int i : CharStats.CODES.SAVING_THROWS())
				if((CMMSGMAP[i]>=0)
				&&((sm==CMMSGMAP[i])||(tm==CMMSGMAP[i])))
					proceed=true;
			if((msg.tool() instanceof Trap)||(proceed))
			{
				if(msg.tool()!=null)
					permProts.add(msg.tool());
				prots--;
				msg.source().location().show((MOB)msg.target(),msg.source(),this,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> cursed fortune trips!"));
				if(prots==0)
					unInvoke();
			}
		}
		else
		if((msg.targetMinor()==CMMsg.TYP_WAND_USE)
		&&(msg.source()==affected)
		&&(msg.target() instanceof Wand)
		&&(msg.targetMessage()!=null)
		&&(!permProts.contains(msg.target()))
		&&(msg.source().location()!=null)
		&&(msg.tool() != null))
		{
			final Room R = msg.source().location();
			boolean tripped = false;
			if(msg.tool() instanceof MOB)
			{
				final List<MOB> choices = new ArrayList<MOB>(R.numInhabitants());
				for(final Enumeration<MOB> m = R.inhabitants();m.hasMoreElements();)
				{
					final MOB M = m.nextElement();
					if((M != msg.tool())&&(msg.source().mayIFight(M)))
						choices.add(M);
				}
				if(choices.size()>0)
				{
					msg.setTool(choices.get(CMLib.dice().roll(1, choices.size(), -1)));
					tripped = true;
				}
			}
			else
			if(msg.tool() instanceof Item)
			{
				final ItemPossessor poss = ((Item)msg.tool()).owner();
				final List<Item> choices = new ArrayList<Item>(poss.numItems());
				for(final Enumeration<Item> i = poss.items(); i.hasMoreElements();)
				{
					final Item I = i.nextElement();
					if(I != msg.tool())
						choices.add(I);
				}
				if(choices.size()>0)
				{
					msg.setTool(choices.get(CMLib.dice().roll(1, choices.size(), -1)));
					tripped = true;
				}
			}
			if(tripped)
			{
				permProts.add(msg.target());
				msg.source().location().show((MOB)msg.target(),msg.source(),this,CMMsg.MSG_OK_VISUAL,L("<S-YOUPOSS> cursed fortune trips!"));
			}
		}
		return super.okMessage(host,msg);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		final MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null)
			return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final int adjustment=target.phyStats().level()-(mob.phyStats().level()+super.getXLEVELLevel(mob));
		final boolean success=proficiencyCheck(mob,-adjustment,auto);
		final Room R = target.location();
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":L("^S<S-NAME> chant(s) a curse at <T-NAMESELF>.^?"));
			final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
			if((R.okMessage(mob,msg))&&((R.okMessage(mob,msg2))))
			{
				R.send(mob,msg);
				R.send(mob,msg2);
				if((msg.value()<=0) && (msg2.value()<=0))
					maliciousAffect(mob,target,asLevel,0, -1);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> chant(s) a curse at <T-NAMESELF>, but fail(s)."));

		return success;
	}
}
