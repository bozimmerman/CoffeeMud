package com.planet_ink.coffee_mud.Abilities.Prayers;
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
   Copyright 2004-2018 Bo Zimmerman

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

public class Prayer_Revival extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Revival";
	}

	private final static String localizedName = CMLib.lang().L("Revival");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(Revival)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER|Ability.DOMAIN_EVANGELISM;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_NEUTRAL;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public void unInvoke()
	{
		// undo the affects of this spell
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		if(canBeUninvoked())
			mob.tell(L("Your part in the revival is over."));
		super.unInvoke();

	}

	@Override
	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(((MOB)affected).location()!=null))
		{
			final MOB mob=(MOB)affected;
			final Room R=mob.location();
			int levels=0;
			final Vector<MOB> inhabs=new Vector<MOB>();
			final Vector<MOB> clerics=new Vector<MOB>();
			final int bonus=(2*getXLEVELLevel(invoker()));
			for(int i=0;i<R.numInhabitants();i++)
			{
				final MOB M=R.fetchInhabitant(i);
				if(M!=null)
				{
					if(mob.getWorshipCharID().equals(M.getWorshipCharID()))
					{
						if(M.fetchEffect(ID())!=null)
						{
							levels+=(M.phyStats().level()+bonus);
							clerics.addElement(M);
						}
					}
					else
					if(CMLib.dice().rollPercentage()<10)
						inhabs.addElement(M);
				}
			}
			final Deity D=CMLib.map().getDeity(mob.getWorshipCharID());
			if((D!=null)&&(CMLib.dice().rollPercentage()<50))
			switch(CMLib.dice().roll(1,13,0))
			{
				case 1:
					CMLib.commands().postSay(mob, null, L("@x1 is great! Shout @x2 praises!", D.name(), CMStrings.capitalizeAndLower(D.charStats().hisher())), false, false);
					break;
				case 2:
					CMLib.commands().postSay(mob, null, L("Can I hear an AMEN?!"), false, false);
					break;
				case 3:
					CMLib.commands().postSay(mob, null, L("Praise @x1!", D.name()), false, false);
					break;
				case 4:
					CMLib.commands().postSay(mob, null, L("Halleluyah! @x1 is great!", D.name()), false, false);
					break;
				case 5:
					CMLib.commands().postSay(mob, null, L("Let's hear it for @x1!", D.name()), false, false);
					break;
				case 6:
					CMLib.commands().postSay(mob, null, L("Exalt the name of @x1!", D.name()), false, false);
					break;
				case 7:
					if (clerics.size() > 1)
					{
						final MOB M = clerics.elementAt(CMLib.dice().roll(1, clerics.size(), -1));
						if (M != mob)
							CMLib.commands().postSay(mob, null, L("Preach it @x1!", M.name(mob)), false, false);
						else
							CMLib.commands().postSay(mob, null, L("I LOVE @x1!", D.name()), false, false);
					}
					else
						CMLib.commands().postSay(mob, null, L("I LOVE @x1!", D.name()), false, false);
					break;
				case 8:
					CMLib.commands().postSay(mob, null, L("Holy is the name of @x1!", D.name()), false, false);
					break;
				case 9:
					CMLib.commands().postSay(mob, null, L("Do you BELIEVE?!? I BELIEVE!!!"), false, false);
					break;
				case 10:
					CMLib.commands().postSay(mob, null, L("Halleluyah!"), false, false);
					break;
				case 11:
					mob.enqueCommand(CMParms.parse("EMOTE do(es) a spirit-filled dance!"), MUDCmdProcessor.METAFLAG_FORCED, 0);
					break;
				case 12:
					mob.enqueCommand(CMParms.parse("EMOTE wave(s) <S-HIS-HER> hands in the air!"), MUDCmdProcessor.METAFLAG_FORCED, 0);
					break;
				case 13:
					mob.enqueCommand(CMParms.parse("EMOTE catch(es) the spirit of " + D.name() + "!"), MUDCmdProcessor.METAFLAG_FORCED, 0);
					break;
			}
			if((clerics.size()>2)&&(inhabs.size()>0))
			{
				levels=levels/clerics.size();
				levels=levels+((clerics.size()-3)*5);
				final MOB M=inhabs.elementAt(CMLib.dice().roll(1,inhabs.size(),-1));
				if((M!=null)&&(levels>=(M.phyStats().level()+bonus)))
				{
					final MOB vic1=mob.getVictim();
					final MOB vic2=M.getVictim();
					if(M.getWorshipCharID().length()>0)
					{
						final Ability A=CMClass.getAbility("Prayer_Faithless");
						if(A!=null)
							A.invoke(mob,M,true,0);
					}
					if(M.getWorshipCharID().length()==0)
					{
						final Ability A=CMClass.getAbility("Prayer_UndeniableFaith");
						if(A!=null)
						{
							if(A.invoke(mob,M,true,0))
							{
								if(M.getWorshipCharID().equals(mob.getWorshipCharID()))
								{
									for(int c=0;c<clerics.size();c++)
									{
										final MOB M2=clerics.elementAt(c);
										if(M2!=mob)
											CMLib.leveler().postExperience(M2,M,null,25,false);
									}
								}
							}
						}
					}
					mob.setVictim(vic1);
					M.setVictim(vic2);
				}
			}
		}
		return true;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;
		if((target.getWorshipCharID().length()==0)
		||(CMLib.map().getDeity(target.getWorshipCharID())==null))
		{
			target.tell(L("You must worship a god to use this prayer."));
			return false;
		}
		if(target.fetchEffect(this.ID())!=null)
		{
			mob.tell(target,null,null,L("<S-NAME> <S-IS-ARE> already participating in a revival."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> start(s) a revival!"):L("^S<S-NAME> @x1 for successful revival, and then start(s) MOVING!^?",prayWord(mob)));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,10);
			}
		}
		else
			beneficialWordsFizzle(mob,target,L("<S-NAME> @x1 for a successful revival, but fail(s).",prayWord(mob)));

		return success;
	}
}
