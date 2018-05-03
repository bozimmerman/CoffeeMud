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

public class Prayer_Wave extends Prayer
{
	@Override
	public String ID()
	{
		return "Prayer_Wave";
	}

	private final static String	localizedName	= CMLib.lang().L("Wave");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_MALICIOUS;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_PRAYER | Ability.DOMAIN_CREATION;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_HOLY | Ability.FLAG_MOVING;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Waved)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return 0;
	}

	@Override
	protected int canTargetCode()
	{
		return Ability.CAN_EXITS;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		final Set<MOB> h=properTargets(mob,givenTarget,auto);
		if(h==null)
			return false;
		int dir=CMLib.directions().getGoodDirectionCode(CMParms.combine(commands,0));
		if(dir<0)
		{
			if(mob.isMonster())
			{
				for(int d=Directions.NUM_DIRECTIONS()-1;d>=0;d--)
				{
					final Room destRoom=mob.location().getRoomInDir(d);
					final Exit exitRoom=mob.location().getExitInDir(d);
					if((destRoom!=null)||(exitRoom!=null)||(d!=Directions.UP))
					{
						dir=d;
						break;
					}
				}
				if(dir<0)
					return false;
			}
			else
			{
				mob.tell(L("Wash your opponents which direction?"));
				return false;
			}
		}
		final Room destRoom=mob.location().getRoomInDir(dir);
		final Exit exitRoom=mob.location().getExitInDir(dir);
		if((destRoom==null)||(exitRoom==null)||(dir==Directions.UP))
		{
			mob.tell(L("You can't wash your opponents that way!"));
			return false;
		}

		if(!super.invoke(mob, commands, givenTarget, auto, asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		final int numEnemies=h.size();
		for (final Object element : h)
		{
			final MOB target=(MOB)element;
			if(target!=mob)
			{
				if(success)
				{
					final Room R=target.location();
					final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto)|CMMsg.MASK_MALICIOUS,
							auto?L("<T-NAME> <T-IS-ARE> swept away by a great wave!"):L("^S<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, @x1.^?",prayingWord(mob)));
					final CMMsg msg2=CMClass.getMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_WATER|(auto?CMMsg.MASK_ALWAYS:0),null);
					if((R.okMessage(mob,msg))&&((R.okMessage(mob,msg2))))
					{
						R.send(mob,msg);
						R.send(mob,msg2);
						if((msg.value()<=0)&&(msg2.value()<=0))
						{
							final int harming=CMLib.dice().roll(1,adjustedLevel(mob,asLevel)/numEnemies,numEnemies);
							CMLib.combat().postDamage(mob,target,this,harming,CMMsg.MASK_ALWAYS|CMMsg.TYP_WATER,Weapon.TYPE_BURSTING,L("A crashing wave <DAMAGE> <T-NAME>!"));
							final int chanceToStay=10+(target.charStats().getStat(CharStats.STAT_STRENGTH)-(mob.phyStats().level()+(2*getXLEVELLevel(mob)))*4);
							final int roll=CMLib.dice().rollPercentage();
							if((roll!=1)&&(roll>chanceToStay))
							{
								CMLib.tracking().walk(target,dir,true,false);
								if((!R.isInhabitant(target))&&(target.isMonster()))
									CMLib.tracking().markToWanderHomeLater(target);
							}
						}
					}
				}
				else
					maliciousFizzle(mob,target,L("<S-NAME> sweep(s) <S-HIS-HER> hands over <T-NAMESELF>, @x1, but @x2 does not heed.",prayingWord(mob),hisHerDiety(mob)));
			}
		}
		return success;
	}
}
