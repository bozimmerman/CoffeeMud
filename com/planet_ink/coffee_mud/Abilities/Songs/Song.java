package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
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
public class Song extends StdAbility
{
	public String ID() { return "Song"; }
	public String name(){ return "a Song";}
	public String displayText(){ return "("+songOf()+")";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	private static final String[] triggerStrings = {"SING","SI"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SONG|Ability.DOMAIN_SINGING;}
	public int maxRange(){return 2;}

	protected boolean skipStandardSongInvoke(){return false;}
	protected boolean mindAttack(){return abstractQuality()==Ability.QUALITY_MALICIOUS;}
	protected boolean skipStandardSongTick(){return false;}
	protected String songOf(){return "Song of "+name();}

    private static final int EXPERTISE_STAGES=10;
    private static final String[] EXPERTISE={"SHARPSING","REJOICESING","RESOUNDSING"};
    private static final String[] EXPERTISE_NAME={"Sharp Singing","Rejoicing Singing","Resounding Singing"};
    private static final String[] EXPERTISE_QUAL={"MALICIOUS","BENEFICIAL",""};
    private static final String[][] EXPERTISE_STATS={{"CHA",""},
                                                     {"CHA",""},
                                                     {"CHA",""}
    };
    private static final int[] EXPERTISE_LEVELS={14,16,18};
    public void initializeClass()
    {
        super.initializeClass();
        if(!ID().equals("Song"))
        {
            if(CMLib.expertises().getDefinition(EXPERTISE[0]+EXPERTISE_STAGES)==null)
            for(int e=0;e<EXPERTISE.length;e++)
                for(int i=1;i<=EXPERTISE_STAGES;i++)
                    CMLib.expertises().addDefinition(EXPERTISE[e]+i,EXPERTISE_NAME[e]+" "+CMath.convertToRoman(i),
                            ((i==1)?"":"-EXPERTISE \"+"+EXPERTISE[e]+(i-1)+"\""),
                                " +"+EXPERTISE_STATS[e][0]+" "+(16+i)+" -SKILLFLAG \"+SINGING\" "
                               +((EXPERTISE_STATS[e][1].length()>0)?" +"+EXPERTISE_STATS[e][1]+" "+(16+i):"")
                               +" -LEVEL +>="+(EXPERTISE_LEVELS[e]+(5*i))
                               ,0,1,0,0,0);
        }
    }
    protected int getXLevel(MOB mob){
    	if(super.abstractQuality()==Ability.QUALITY_MALICIOUS)
	    	return getExpertiseLevel(mob,EXPERTISE[0]);
    	else
	    	return getExpertiseLevel(mob,EXPERTISE[1]);
    }
    
	public int singerQClassLevel()
	{
		if(invoker()==null) return CMLib.ableMapper().lowestQualifyingLevel(ID());
		int x=CMLib.ableMapper().qualifyingClassLevel(invoker(),this);
		if(x<=0) x=CMLib.ableMapper().lowestQualifyingLevel(ID());
		int charisma=(invoker().charStats().getStat(CharStats.STAT_CHARISMA)-10);
		if(charisma>10)
			return x+((charisma-10)/3)+(getXLevel(invoker())*2);
		return x+(getXLevel(invoker())*2);
	}

	public void executeMsg(Environmental host, CMMsg msg)
	{
		super.executeMsg(host,msg);
		if((affected==invoker)
		&&(msg.amISource(invoker))
		&&(!unInvoked))
		{
			if((msg.sourceMinor()==CMMsg.TYP_SPEAK)&&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_CHANNEL)))
				unInvoke();
			else
			if((msg.target() instanceof Armor)
			&&(msg.targetMinor()==CMMsg.TYP_WEAR))
				unInvoke();
		}
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID))
			return false;

		if(skipStandardSongTick())
			return true;

		MOB mob=(MOB)affected;
		if(mob==null)
			return false;

		if((invoker==null)
		||(invoker.fetchEffect(ID())==null)
		||(invoker.location()!=mob.location())
		||(!CMLib.flags().aliveAwakeMobile(invoker,true))
		||(!CMLib.flags().canBeHeardBy(invoker,mob)))
		{
			unsing(mob,null,false);
			return false;
		}
		return true;
	}

	protected void unsing(MOB mob, MOB invoker, boolean notMe)
	{
		if(mob==null) return;
		for(int a=mob.numEffects()-1;a>=0;a--)
		{
			Ability A=mob.fetchEffect(a);
			if((A!=null)
			&&(A instanceof Song)
			&&((!notMe)||(!A.ID().equals(ID())))
			&&((invoker==null)||(A.invoker()==null)||(A.invoker()==invoker)))
				A.unInvoke();
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if((!auto)
		&&(!mob.isMonster())
		&&(!disregardsArmorCheck(mob))
		&&(!CMLib.utensils().armorCheck(mob,CharClass.ARMOR_LEATHER))
		&&(mob.isMine(this))
		&&(mob.location()!=null)
		&&(CMLib.dice().rollPercentage()<50))
		{
			mob.location().show(mob,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> hit(s) a foul note on "+name()+" due to <S-HIS-HER> armor!");
			return false;
		}

		if(skipStandardSongInvoke())
			return true;

		if((!auto)&&(!CMLib.flags().canSpeak(mob)))
		{
			mob.tell("You can't sing!");
			return false;
		}

		boolean success=proficiencyCheck(mob,0,auto);
		unsing(mob,mob,true);
		if(success)
		{
			String str=auto?"^SThe "+songOf()+" begins to play!^?":"^S<S-NAME> begin(s) to sing the "+songOf()+".^?";
			if((!auto)&&(mob.fetchEffect(this.ID())!=null))
				str="^S<S-NAME> start(s) the "+songOf()+" over again.^?";

			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Song newOne=(Song)this.copyOf();

				HashSet h=properTargets(mob,givenTarget,auto);
				if(h==null) return false;
				if(!h.contains(mob)) h.add(mob);

				for(Iterator f=h.iterator();f.hasNext();)
				{
					MOB follower=(MOB)f.next();

					// malicious songs must not affect the invoker!
					int affectType=CMMsg.MSG_CAST_VERBAL_SPELL;
					if(auto) affectType=affectType|CMMsg.MASK_ALWAYS;
					if((castingQuality(mob,follower)==Ability.QUALITY_MALICIOUS)&&(follower!=mob))
						affectType=affectType|CMMsg.MASK_MALICIOUS;

					if((CMLib.flags().canBeHeardBy(invoker,follower)&&(follower.fetchEffect(this.ID())==null)))
					{
						CMMsg msg2=CMClass.getMsg(mob,follower,this,affectType,null);
						CMMsg msg3=msg2;
						if((mindAttack())&&(follower!=mob))
							msg2=CMClass.getMsg(mob,follower,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_ALWAYS:0),null);
						if((mob.location().okMessage(mob,msg2))&&(mob.location().okMessage(mob,msg3)))
						{
							follower.location().send(follower,msg2);
							if(msg2.value()<=0)
							{
								follower.location().send(follower,msg3);
								if((msg3.value()<=0)&&(follower.fetchEffect(newOne.ID())==null))
								{
									newOne.setSavable(false);
									if(follower!=mob)
										follower.addEffect((Ability)newOne.copyOf());
									else
										follower.addEffect(newOne);
								}
							}
						}
					}
				}
				mob.location().recoverRoomStats();
			}
		}
		else
			mob.location().show(mob,null,CMMsg.MSG_NOISE,"<S-NAME> hit(s) a foul note.");

		return success;
	}
}
