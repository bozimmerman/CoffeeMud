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
import java.util.Map.Entry;

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

public class Thief_Shadowpass extends ThiefSkill
{
	@Override
	public String ID()
	{
		return "Thief_Shadowpass";
	}

	private final static String	localizedName	= CMLib.lang().L("Shadowpass");

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
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_INDIFFERENT;
	}

	private static final String[]	triggerStrings	= I(new String[] { "SHADOWPASS" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int usageType()
	{
		return USAGE_MOVEMENT;
	}

	@Override
	public long flags()
	{
		return Ability.FLAG_TRANSPORTING | super.flags();
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_THIEF_SKILL | Ability.DOMAIN_STEALTHY;
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room R=mob.location();
		if((!auto)&&(!CMLib.flags().isInDark(R))||(R==null))
		{
			mob.tell(L("You can only shadowpass from the shadows to the shadows."));
			return false;
		}
		final List<Integer> trail=new Vector<Integer>();
		int v=0;
		for(;v<commands.size();v++)
		{
			int num=1;
			String s=commands.get(v);
			if(CMath.s_int(s)>0)
			{
				num=CMath.s_int(s);
				v++;
				if(v<commands.size())
					s=commands.get(v);
			}
			else
			if(CMath.isNumberFollowedByString(s))
			{
				final Entry<Integer,String> pair=CMath.getNumberFollowedByString(s);
				num=pair.getKey().intValue();
				s=pair.getValue();
			}

			final int direction=CMLib.directions().getGoodDirectionCode(s);
			if(direction<0)
				break;
			if((R==null)||(R.getRoomInDir(direction)==null)||(R.getExitInDir(direction)==null))
				break;
			for(int i=0;i<num;i++)
			{
				R=R.getRoomInDir(direction);
				if((R==null)||(!CMLib.flags().canAccess(mob,R)))
					break;
				trail.add(Integer.valueOf(direction));
			}
		}
		final boolean kaplah=((v==commands.size())&&(R!=null)&&(mob.location()!=R)&&(CMLib.flags().isInDark(R)));

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);
		if(success && (mob.location()!=R))
		{
			final CMMsg msg=CMClass.getMsg(mob,R,this,auto?CMMsg.MSG_OK_VISUAL:CMMsg.MSG_DELICATE_HANDS_ACT,L("You begin the shadowpass ..."),CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if((mob.location().okMessage(mob,msg))&&(R!=null)&&(R.okMessage(mob,msg)))
			{
				mob.location().send(mob,msg);
				msg.setSourceMessage(null);
				R.send(mob,msg);
				R=mob.location();
				for(int i=0;i<trail.size();i++)
				{
					final int dir=trail.get(i).intValue();
					if(!kaplah)
					{
						if((!CMLib.tracking().walk(mob,dir,false,true,true))||(!CMLib.flags().isInDark(mob.location())))
						{
							CMLib.commands().postLook(mob,true);
							return beneficialVisualFizzle(mob,null,L("<S-NAME> do(es) not know <S-HIS-HER> way through shadowpass."));
						}
						CMLib.combat().expendEnergy(mob,true);
					}
					else
					{
						R=R.getRoomInDir(dir);
						R.bringMobHere(mob,false);
					}
					CMLib.combat().expendEnergy(mob,true);
				}
				CMLib.commands().postLook(mob,true);
			}
		}
		else
		for(int i=0;i<trail.size();i++)
		{
			final int dir=trail.get(i).intValue();
			if((!CMLib.tracking().walk(mob,dir,false,true,true))||(!CMLib.flags().isInDark(mob.location())))
			{
				CMLib.commands().postLook(mob,true);
				return beneficialVisualFizzle(mob,null,L("<S-NAME> lose(s) <S-HIS-HER> way through shadowpass."));
			}
			CMLib.combat().expendEnergy(mob,true);
			CMLib.combat().expendEnergy(mob,true);
		}
		return success;
	}

}
