package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


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
					StringBuffer s=MUDHelp.getHelpText(R.ID(),null);
					if(s==null)
						s=MUDHelp.getHelpText(R.name(),null);
					if(s!=null)
						str.append(helpHelp(s));
				}
				if(parms.containsKey("PLAYABLE"))
					str.append(Race.AVAILABLE_DESC[R.availability()]+", ");
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
							str.append(CharStats.TRAITS[c].toLowerCase()+"-"+(oldStat-newStat)+", ");
						else
						if(newStat>oldStat)
							str.append(CharStats.TRAITS[c].toLowerCase()+"+"+(newStat-oldStat)+", ");
					}
				}
				if(parms.containsKey("SENSES"))
				{
					if(!Sense.canHear(mob))
						str.append("deaf, ");
					if(!Sense.canSee(mob))
						str.append("blind, ");
					if(!Sense.canMove(mob))
						str.append("can't move, ");
					if(Sense.canSeeBonusItems(mob))
						str.append("detect magic, ");
					if(Sense.canSeeEvil(mob))
						str.append("detect evil, ");
					if(Sense.canSeeGood(mob))
						str.append("detect good, ");
					if(Sense.canSeeHidden(mob))
						str.append("see hidden, ");
					if(Sense.canSeeInDark(mob))
						str.append("darkvision, ");
					if(Sense.canSeeInfrared(mob))
						str.append("infravision, ");
					if(Sense.canSeeInvisible(mob))
						str.append("see invisible, ");
					if(Sense.canSeeMetal(mob))
						str.append("metalvision, ");
					if(Sense.canSeeSneakers(mob))
						str.append("see sneaking, ");
					if(!Sense.canSmell(mob))
						str.append("can't smell, ");
					if(!Sense.canSpeak(mob))
						str.append("can't speak, ");
					if(!Sense.canTaste(mob))
						str.append("can't eat, ");
				}
				if(parms.containsKey("DISPOSITIONS"))
				{
					if(Sense.isClimbing(mob))
						str.append("climbing, ");
					if((mob.envStats().disposition()&EnvStats.IS_EVIL)>0)
						str.append("evil, ");
					if(Sense.isFalling(mob))
						str.append("falling, ");
					if(Sense.isBound(mob))
						str.append("bound, ");
					if(Sense.isFlying(mob))
						str.append("flies, ");
					if((mob.envStats().disposition()&EnvStats.IS_GOOD)>0)
						str.append("good, ");
					if(Sense.isHidden(mob))
						str.append("hidden, ");
					if(Sense.isInDark(mob))
						str.append("darkness, ");
					if(Sense.isInvisible(mob))
						str.append("invisible, ");
					if(Sense.isGlowing(mob))
						str.append("glowing, ");
					if(!Sense.isSeen(mob))
						str.append("unseeable, ");
					if(Sense.isSitting(mob))
						str.append("crawls, ");
					if(Sense.isSleeping(mob))
						str.append("sleepy, ");
					if(Sense.isSneaking(mob))
						str.append("sneaks, ");
					if(Sense.isSwimming(mob))
						str.append("swims, ");
				}
				if(parms.containsKey("TRAINS"))
				{
					if(mob.getTrains()>0)
						str.append("trains"+"+"+mob.getTrains()+", ");
				}
				if(parms.containsKey("PRACS"))
				{
					if(mob.getPractices()>0)
						str.append("practices"+"+"+mob.getPractices()+", ");
				}
				if(parms.containsKey("ABILITIES"))
				{
					int num=Util.s_int(R.getStat("NUMCABLE"));
					for(int i=0;i<num;i++)
					{
						Ability A=mob.fetchAbility(R.getStat("GETCABLE"+i));
						if(A!=null)
						{
							A.setProfficiency(Util.s_int(R.getStat("GETCABLEPROF"+i)));
							if(A.profficiency()==0)
								str.append(A.Name()+", ");
							else
								str.append(A.Name()+"("+A.profficiency()+"%), ");
						}
					}

				}
				if(parms.containsKey("EFFECTS"))
				{
					int num=Util.s_int(R.getStat("NUMREFF"));
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
						if((A!=null)&&((A.classificationCode()&Ability.ALL_CODES)==Ability.LANGUAGE))
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
						if((C!=null)&&(C.playerSelectable())&&(C.qualifiesForThisClass(mob,true)))
							str.append(C.name()+", ");
					}
				}
				String strstr=str.toString();
				if(strstr.endsWith(", "))
					strstr=strstr.substring(0,strstr.length()-2);
				return strstr;
			}
		}
		return "";
	}
}
