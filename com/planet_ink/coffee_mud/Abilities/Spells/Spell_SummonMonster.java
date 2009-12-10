package com.planet_ink.coffee_mud.Abilities.Spells;
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
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class Spell_SummonMonster extends Spell
{
	public String ID() { return "Spell_SummonMonster"; }
	public String name(){return "Monster Summoning";}
	public String displayText(){return "(Monster Summoning)";}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public int enchantQuality(){return Ability.QUALITY_INDIFFERENT;}
	public int classificationCode(){return Ability.ACODE_SPELL|Ability.DOMAIN_CONJURATION;}
	public long flags(){return Ability.FLAG_SUMMONING;}

	public void unInvoke()
	{
		MOB mob=(MOB)affected;
		super.unInvoke();
		if((canBeUninvoked())&&(mob!=null))
		{
			if(mob.amDead()) mob.setLocation(null);
			mob.destroy();
		}
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((affected!=null)
		&&(affected instanceof MOB)
		&&(msg.amISource((MOB)affected)||msg.amISource(((MOB)affected).amFollowing()))
		&&(msg.sourceMinor()==CMMsg.TYP_QUIT))
		{
			unInvoke();
			if(msg.source().playerStats()!=null) msg.source().playerStats().setLastUpdated(0);
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

        Room R=mob.location();
		if(success)
		{
			invoker=mob;
			CMMsg msg=CMClass.getMsg(mob,null,this,verbalCastCode(mob,null,auto),auto?"":"^S<S-NAME> summon(s) help from the Java Plain....^?");
			if(R.okMessage(mob,msg))
			{
				R.send(mob,msg);
		        MOB monster = determineMonster(mob, mob.envStats().level()+(getXLEVELLevel(mob)+(2*getX1Level(mob))));
		        if(monster!=null)
					beneficialAffect(mob,monster,asLevel,0);
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> call(s) for magical help, but choke(s) on the words.");

		// return whether it worked
		return success;
	}
	
	public void bringToLife(MOB M)
	{
		
	}
	
	public MOB determineMonster(MOB caster, int level)
	{
	    Room R=caster.location();
	    if(R==null) return null;
        MOB newMOB=null;
        Vector choices=new Vector();
        MOB M=null;
        int range=0;
        int diff=2;
        if(level>=100)
            diff=20;
        else
        if(level>=80)
            diff=17;
        else
        if(level>=60)
            diff=15;
        else
        if(level>=40)
            diff=10;
        else
        if(level>=20)
            diff=7;
        else
        if(level>=10)
            diff=5;
        while((choices.size()==0)&&(range<100))
        {
            range+=diff;
		    for(Enumeration e=CMClass.mobTypes();e.hasMoreElements();)
            {
                M=(MOB)((MOB)e.nextElement()).newInstance();
                if((M.baseEnvStats().level()<level-range)
                ||(M.baseEnvStats().level()>level+range)
                ||(M.isGeneric())
                ||(!CMLib.flags().isEvil(M))
                ||(!M.baseCharStats().getMyRace().fertile())
                ||CMLib.flags().isGolem(M)
                ){ M.destroy(); try{Thread.sleep(1);}catch(Exception e1){} continue;}
                choices.addElement(M);
            }
        }
        if(choices.size()>0)
        {
            MOB winM=(MOB)choices.firstElement();
            for(int i=1;i<choices.size();i++)
            {
                M=(MOB)choices.elementAt(i);
                if(CMath.pow(level-M.baseEnvStats().level(),2)<CMath.pow(level-winM.baseEnvStats().level(),2))
                    winM=M;
            }
            newMOB=winM;
        }
        else
        {
            newMOB=CMClass.getMOB("GenMOB");
            newMOB.baseEnvStats().setLevel(level);
            newMOB.charStats().setMyRace(CMClass.getRace("Unique"));
            newMOB.setName("a wierd extra-planar monster");
            newMOB.setDisplayText("a wierd extra-planar monster stands here");
            newMOB.setDescription("It's too difficult to describe what this thing looks like, but he/she/it is definitely angry!");
            CMLib.factions().setAlignment(newMOB,Faction.ALIGN_NEUTRAL);
            newMOB.baseCharStats().getMyRace().startRacing(newMOB,false);
            newMOB.baseState().setHitPoints(CMLib.dice().roll(baseEnvStats().level(),20,baseEnvStats().level()));
            newMOB.recoverMaxState();
            newMOB.resetToMaxState();
            newMOB.recoverEnvStats();
            newMOB.recoverCharStats();
            CMLib.leveler().fillOutMOB(newMOB,level);
            newMOB.recoverMaxState();
            newMOB.resetToMaxState();
            newMOB.recoverEnvStats();
            newMOB.recoverCharStats();
        }
        newMOB.setMoney(0);
		newMOB.setLocation(R);
		newMOB.baseEnvStats().setRejuv(Integer.MAX_VALUE);
		newMOB.addNonUninvokableEffect(CMClass.getAbility("Prop_ModExperience"));
		newMOB.recoverCharStats();
		newMOB.recoverEnvStats();
		newMOB.recoverMaxState();
		newMOB.resetToMaxState();
		newMOB.bringToLife(R,true);
		CMLib.beanCounter().clearZeroMoney(newMOB,null);
		R.showOthers(newMOB,null,CMMsg.MSG_OK_ACTION,"<S-NAME> appears!");
        MOB victim=caster.getVictim();
		newMOB.setStartRoom(null); // keep before postFollow for Conquest
        CMLib.commands().postFollow(newMOB,caster,true);
        if(newMOB.amFollowing()!=caster)
            caster.tell(newMOB.name()+" seems unwilling to follow you.");
        else
        if(victim!=null)
        {
            if(newMOB.getVictim()!=victim) newMOB.setVictim(victim);
            R.showOthers(newMOB,victim,CMMsg.MSG_OK_ACTION,"<S-NAME> start(s) attacking <T-NAMESELF>!");
        }
        if(newMOB.amDead()||newMOB.amDestroyed()) 
            return null;
		return(newMOB);
	}
}
