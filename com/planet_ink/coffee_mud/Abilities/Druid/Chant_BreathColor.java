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
   Copyright 20024-2025 Bo Zimmerman

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
public class Chant_BreathColor extends Chant
{
	@Override
	public String ID()
	{
		return "Chant_BreathColor";
	}

	private final static String	localizedName	= CMLib.lang().L("Breath Color");

	@Override
	public String name()
	{
		return localizedName;
	}

	private final static String	localizedStaticDisplay	= CMLib.lang().L("(Breath Color)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_CHANT | Ability.DOMAIN_WEATHER_MASTERY;
	}

	@Override
	public long flags()
	{
		return super.flags() | Ability.FLAG_DIVINING;
	}

	@Override
	public void unInvoke()
	{
		if(!(affected instanceof MOB))
			return;
		final MOB mob=(MOB)affected;
		super.unInvoke();
		if(canBeUninvoked())
			mob.tell(L("Your senses are no longer sensitive to breath colors."));
	}

	public String getBreathColor(final MOB mob, final MOB M)
	{
		if((M==null)||(mob==null))
			return "";
		int sourceCode=-1;
		int levelCode=-1;
		int[] colors=null;
		if(M!=mob)
		{
			if(colors==null)
				colors=new int[11];
			if(M.phyStats().level()>=(mob.phyStats().level()+25))
				levelCode=4;
			else
			if(M.phyStats().level()>=(mob.phyStats().level()+15))
			{
				if (levelCode < 3)
					levelCode = 3;
			}
			else
			if(M.phyStats().level()>=(mob.phyStats().level()+5))
			{
				if (levelCode < 2)
					levelCode = 2;
			}
			else
			if(M.phyStats().level()>(mob.phyStats().level()-5))
			{
				if (levelCode < 1)
					levelCode = 1;
			}
			else
			if(M.phyStats().level()>(mob.phyStats().level()-15))
			{
				if (levelCode < 0)
					levelCode = 0;
			}

			int done=0;
			if(CMLib.flags().isUndead(M))
			{
				done++;
				colors[0]++;
			}
			if((M.charStats().getMyRace().ID().equals("StoneGolem"))
			||(M.charStats().getMyRace().ID().equals("MetalGolem")))
			{
				done++;
				colors[4]++;
			}
			if(CMLib.flags().isHidden(M))
			{
				done++;
				colors[5]++;
			}
			if(CMLib.flags().isInvisible(M))
			{
				done++;
				colors[6]++;
			}
			if(M.charStats().getMyRace().ID().equals("WoodGolem"))
			{
				done++;
				colors[7]++;
			}
			if(done>0)
			{
				if(sourceCode>=0)
					sourceCode=1;
				else sourceCode=0;
			}
			if(CMLib.flags().isLawful(M))
				colors[9]++;
			else
			if(CMLib.flags().isChaotic(M))
				colors[10]++;
			if(CMLib.flags().isGood(M))
				colors[2]++;
			else
			if(CMLib.flags().isEvil(M))
				colors[1]++;
			else
			if((!CMLib.flags().isGood(M))&&(!CMLib.flags().isEvil(M)))
				colors[3]++;
		}
		if(colors==null)
			return "";
		boolean foundOne=false;
		for (final int color : colors)
			if(color>0)
			{
				foundOne=true;
				break;
			}
		if(!foundOne)
			return "";

		final StringBuffer str=new StringBuffer("");
		switch(sourceCode)
		{
		case 0:
			switch(levelCode)
			{
			case -1:
				str.append(L("dull stripes of "));
				break;
			case 0:
				str.append(L("faded stripes of "));
				break;
			case 1:
				str.append(L("striped "));
				break;
			case 2:
				str.append(L("brightly striped "));
				break;
			case 3:
				str.append(L("brilliant stripes of "));
				break;
			case 4:
				str.append(L("dazzling stripes of "));
				break;
			}
			break;
		case 1:
			switch(levelCode)
			{
			case -1:
				str.append(L("a swirl of dull "));
				break;
			case 0:
				str.append(L("a swirl of faded "));
				break;
			case 1:
				str.append(L("a swirl of "));
				break;
			case 2:
				str.append(L("a bright swirl of "));
				break;
			case 3:
				str.append(L("a swirl of brilliant "));
				break;
			case 4:
				str.append(L("a swirl of dazzling "));
				break;
			}
			break;
		default:
			switch(levelCode)
			{
			case -1:
				str.append(L("faded "));
				break;
			case 0:
				str.append(L("faded "));
				break;
			case 1:
				break;
			case 2:
				str.append(L("bright "));
				break;
			case 3:
				str.append(L("brilliant "));
				break;
			case 4:
				str.append(L("dazzling "));
				break;
			}
			break;
		}
		final Vector<Integer> V=new Vector<Integer>();
		for(int i=0;i<colors.length;i++)
		{
			if(colors[i]>0)
				V.addElement(Integer.valueOf(i));
		}
		if(V.size()==1)
			return str.toString()+Chant_WindColor.getColor(V.firstElement().intValue());
		for(int i=0;i<V.size();i++)
		{
			final int x=V.elementAt(i).intValue();
			if(i==V.size()-1)
				str.append(L("and @x1 ",Chant_WindColor.getColor(x)));
			else
			if(i>0)
				str.append(", "+Chant_WindColor.getColor(x)+" ");
			else str.append(Chant_WindColor.getColor(x)+" ");
		}
		return str.toString().trim();
	}

	@Override
	public void executeMsg(final Environmental host, final CMMsg msg)
	{
		if((msg.source()==affected)
		&&(msg.target() instanceof MOB)
		&&(msg.tool()==null)
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE)))
			msg.addTrailerMsg(CMClass.getMsg(msg.source(),msg.target(),null,CMMsg.MSG_OK_VISUAL,
					L("<T-YOUPOSS> breath is @x1.",getBreathColor(msg.source(),(MOB)msg.target())),
					CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null));
		super.executeMsg(host,msg);
	}

	@Override
	public boolean invoke(final MOB mob, final List<String> commands, final Physical givenTarget, final boolean auto, final int asLevel)
	{
		MOB target=mob;
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof MOB))
			target=(MOB)givenTarget;

		if(target.fetchEffect(this.ID())!=null)
		{
			failureTell(mob,target,auto,L("<S-NAME> <S-IS-ARE> already watching breath colors."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?L("<T-NAME> gain(s) visions of breath!"):L("^S<S-NAME> chant(s) for visions of breath on the wind!^?"));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,target,asLevel,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,L("<S-NAME> chant(s) into the air, but the magic fizzles."));

		return success;
	}
}
