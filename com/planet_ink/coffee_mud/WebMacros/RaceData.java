package com.planet_ink.coffee_mud.WebMacros;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;
import java.util.*;



/* 
   Copyright 2000-2006 Bo Zimmerman

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
public class RaceData extends StdWebMacro
{
	public String name()	{return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}

	// valid parms include HELP, STATS, SENSES, TRAINS, PRACS, ABILITIES,
	// HEALTHTEXTS, NATURALWEAPON, PLAYABLE, DISPOSITIONS, STARTINGEQ,
	// CLASSES, LANGS, EFFECTS

	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		Hashtable parms=parseParms(parm);
		String last=httpReq.getRequestParameter("RACE");
		if(last==null) return " @break@";
		if(last.length()>0)
		{
			Race R=CMClass.getRace(last);
			if(R!=null)
			{
				StringBuffer str=new StringBuffer("");
				if(parms.containsKey("HELP"))
				{
					StringBuffer s=CMLib.help().getHelpText(R.ID(),null,false);
					if(s==null)
						s=CMLib.help().getHelpText(R.name(),null,false);
					if(s!=null)
						str.append(helpHelp(s));
				}
				if(parms.containsKey("PLAYABLE"))
					str.append(Area.THEME_DESCS_EXT[R.availabilityCode()]+", ");
				if(parms.containsKey("NATURALWEAPON"))
					str.append(R.myNaturalWeapon().name()+", ");
				MOB mob=CMClass.getMOB("StdMOB");
				MOB mob2=CMClass.getMOB("StdMOB");
				mob.baseCharStats().setMyRace(R);
				R.startRacing(mob,false);
				mob.recoverCharStats();
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.recoverMaxState();
				mob2.recoverCharStats();
				mob2.recoverEnvStats();
				mob2.recoverMaxState();
				if(parms.containsKey("STATS"))
				{
					for(int c=0;c<CharStats.NUM_STATS;c++)
					{
						int oldStat=mob2.charStats().getStat(c);
						int newStat=mob.charStats().getStat(c);
						if(oldStat>newStat)
							str.append(CharStats.STAT_DESCS[c].toLowerCase()+"-"+(oldStat-newStat)+", ");
						else
						if(newStat>oldStat)
							str.append(CharStats.STAT_DESCS[c].toLowerCase()+"+"+(newStat-oldStat)+", ");
					}
				}
				if(parms.containsKey("SENSES"))
				{
					if(!CMLib.flags().canHear(mob))
						str.append("deaf, ");
					if(!CMLib.flags().canSee(mob))
						str.append("blind, ");
					if(!CMLib.flags().canMove(mob))
						str.append("can't move, ");
					if(CMLib.flags().canSeeBonusItems(mob))
						str.append("detect magic, ");
					if(CMLib.flags().canSeeEvil(mob))
						str.append("detect evil, ");
					if(CMLib.flags().canSeeGood(mob))
						str.append("detect good, ");
					if(CMLib.flags().canSeeHidden(mob))
						str.append("see hidden, ");
					if(CMLib.flags().canSeeInDark(mob))
						str.append("darkvision, ");
					if(CMLib.flags().canSeeInfrared(mob))
						str.append("infravision, ");
					if(CMLib.flags().canSeeInvisible(mob))
						str.append("see invisible, ");
					if(CMLib.flags().canSeeMetal(mob))
						str.append("metalvision, ");
					if(CMLib.flags().canSeeSneakers(mob))
						str.append("see sneaking, ");
					if(!CMLib.flags().canSmell(mob))
						str.append("can't smell, ");
					if(!CMLib.flags().canSpeak(mob))
						str.append("can't speak, ");
					if(!CMLib.flags().canTaste(mob))
						str.append("can't eat, ");
				}
				if(parms.containsKey("DISPOSITIONS"))
				{
					if(CMLib.flags().isClimbing(mob))
						str.append("climbing, ");
					if((mob.envStats().disposition()&EnvStats.IS_EVIL)>0)
						str.append("evil, ");
					if(CMLib.flags().isFalling(mob))
						str.append("falling, ");
					if(CMLib.flags().isBound(mob))
						str.append("bound, ");
					if(CMLib.flags().isFlying(mob))
						str.append("flies, ");
					if((mob.envStats().disposition()&EnvStats.IS_GOOD)>0)
						str.append("good, ");
					if(CMLib.flags().isHidden(mob))
						str.append("hidden, ");
					if(CMLib.flags().isInDark(mob))
						str.append("darkness, ");
					if(CMLib.flags().isInvisible(mob))
						str.append("invisible, ");
					if(CMLib.flags().isGlowing(mob))
						str.append("glowing, ");
					if(CMLib.flags().isCloaked(mob))
						str.append("cloaked, ");
					if(!CMLib.flags().isSeen(mob))
						str.append("unseeable, ");
					if(CMLib.flags().isSitting(mob))
						str.append("crawls, ");
					if(CMLib.flags().isSleeping(mob))
						str.append("sleepy, ");
					if(CMLib.flags().isSneaking(mob))
						str.append("sneaks, ");
					if(CMLib.flags().isSwimming(mob))
						str.append("swims, ");
				}
				if(parms.containsKey("TRAINS"))
				{
					if(mob.getTrains()>0)
						str.append("trains"+"+"+mob.getTrains()+", ");
				}
				if(parms.containsKey("EXPECTANCY"))
					str.append(""+R.getAgingChart()[Race.AGE_ANCIENT]+", ");
				if(parms.containsKey("PRACS"))
				{
					if(mob.getPractices()>0)
						str.append("practices"+"+"+mob.getPractices()+", ");
				}
				if(parms.containsKey("ABILITIES"))
				{
					int num=CMath.s_int(R.getStat("NUMCABLE"));
					for(int i=0;i<num;i++)
					{
						Ability A=mob.fetchAbility(R.getStat("GETCABLE"+i));
						if(A!=null)
						{
							A.setProfficiency(CMath.s_int(R.getStat("GETCABLEPROF"+i)));
							if(A.profficiency()==0)
								str.append(A.Name()+", ");
							else
								str.append(A.Name()+"("+A.profficiency()+"%), ");
						}
					}

				}
				if(parms.containsKey("EFFECTS"))
				{
					int num=CMath.s_int(R.getStat("NUMREFF"));
					for(int i=0;i<num;i++)
					{
						Ability A=mob.fetchAbility(R.getStat("NUMREFF"+i));
						if(A!=null)
							str.append(A.Name()+", ");
					}
				}
				if(parms.containsKey("LANGS"))
				{
					for(int i=0;i<mob.numLearnedAbilities();i++)
					{
						Ability A=mob.fetchAbility(i);
						if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)==Ability.ACODE_LANGUAGE))
							if(A.profficiency()==0)
								str.append(A.Name()+", ");
							else
								str.append(A.Name()+"("+A.profficiency()+"%), ");
					}

				}
				if(parms.containsKey("STARTINGEQ"))
				{
					if(R.outfit()!=null)
					for(int i=0;i<R.outfit().size();i++)
					{
						Item I=(Item)R.outfit().elementAt(i);
						if(I!=null)
							str.append(I.Name()+", ");
					}
				}
				if(parms.containsKey("CLASSES"))
				{
					for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
						mob.baseCharStats().setStat(i,25);
					mob.recoverCharStats();
					for(Enumeration c=CMClass.charClasses();c.hasMoreElements();)
					{
						CharClass C=(CharClass)c.nextElement();
						if((C!=null)
						&&(CMProps.isTheme(C.availabilityCode()))
						&&(C.qualifiesForThisClass(mob,true)))
							str.append(C.name()+", ");
					}
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
                mob.destroy();
                mob2.destroy();
                return clearWebMacros(strstr);
			}
		}
		return "";
	}
}
