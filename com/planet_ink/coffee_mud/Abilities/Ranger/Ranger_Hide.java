package com.planet_ink.coffee_mud.Abilities.Ranger;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.Thief.Thief_Hide;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.ExpertiseLibrary;
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
public class Ranger_Hide extends StdAbility
{
	public String ID() { return "Ranger_Hide"; }
	public String name(){ return "Woodland Hide";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_OK_SELF;}
	private static final String[] triggerStrings = {"WHIDE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_STEALTHY;}
	public int usageType(){return USAGE_MOVEMENT|USAGE_MANA;}
	protected int bonus=0;
	
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;

		MOB mob=(MOB)affected;

		if(msg.amISource(mob))
		{

			if(((CMath.bset(msg.sourceMajor(),CMMsg.MASK_SOUND)
				 ||(msg.sourceMinor()==CMMsg.TYP_SPEAK)
				 ||(msg.sourceMinor()==CMMsg.TYP_ENTER)
				 ||(msg.sourceMinor()==CMMsg.TYP_LEAVE)
				 ||(msg.sourceMinor()==CMMsg.TYP_RECALL)))
			 &&(!CMath.bset(msg.sourceMajor(),CMMsg.MASK_ALWAYS))
			 &&(msg.sourceMinor()!=CMMsg.TYP_LOOK)
             &&(msg.sourceMinor()!=CMMsg.TYP_EXAMINE)
			 &&(msg.sourceMajor()>0))
			 {
				unInvoke();
				mob.recoverEnvStats();
			 }
		}
		return;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_HIDDEN);
		if(CMLib.flags().isSneaking(affected))
			affectableStats.setDisposition(affectableStats.disposition()-EnvStats.IS_SNEAKING);
	}
    public void affectCharStats(MOB affected, CharStats affectableStats)
    {
        super.affectCharStats(affected,affectableStats);
        affectableStats.setStat(CharStats.STAT_SAVE_DETECTION,proficiency()+bonus+affectableStats.getStat(CharStats.STAT_SAVE_DETECTION));
    }

	public int getMOBLevel(MOB meMOB)
	{
		if(meMOB==null) return 0;
		return meMOB.envStats().level();
	}
	public MOB getHighestLevelMOB(MOB meMOB, Vector not)
	{
		if(meMOB==null) return null;
		Room R=meMOB.location();
		if(R==null) return null;
		int highestLevel=0;
		MOB highestMOB=null;
		HashSet H=meMOB.getGroupMembers(new HashSet());
		if(not!=null) H.addAll(not);
		for(int i=0;i<R.numInhabitants();i++)
		{
			MOB M=R.fetchInhabitant(i);
			if((M!=null)
			&&(M!=meMOB)
			&&(!CMLib.flags().isSleeping(M))
			&&(!H.contains(M))
			&&(highestLevel<M.envStats().level()))
			{
				highestLevel=M.envStats().level();
				highestMOB=M;
			}
		}
		return highestMOB;
	}
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		if(mob.fetchEffect(this.ID())!=null)
		{
			mob.tell("You are already hiding.");
			return false;
		}

		if(mob.isInCombat())
		{
			mob.tell("Not while in combat!");
			return false;
		}

		if((((mob.location().domainType()&Room.INDOORS)>0))&&(!auto))
		{
			mob.tell("You only know how to hide outdoors.");
			return false;
		}
		if(((mob.location().domainType()==Room.DOMAIN_OUTDOORS_CITY)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_SPACEPORT))
		&&(!auto))
		{
			mob.tell("You don't know how to hide in a place like this.");
			return false;
		}

		MOB highestMOB=this.getHighestLevelMOB(mob,null);
		int levelDiff=(mob.envStats().level()+(2*getXLEVELLevel(mob)))-getMOBLevel(highestMOB);

		String str="You creep into some foliage and remain completely still.";
		if((mob.location().domainType()==Room.DOMAIN_OUTDOORS_ROCKS)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_MOUNTAINS)
		   ||(mob.location().domainType()==Room.DOMAIN_OUTDOORS_DESERT))
			str="You creep behind some rocks and remain completely still.";


		boolean success=(highestMOB==null)||proficiencyCheck(mob,levelDiff*10,auto);

		if(!success)
		{
			if(highestMOB!=null)
				beneficialVisualFizzle(mob,highestMOB,"<S-NAME> attempt(s) to hide from <T-NAMESELF> and fail(s).");
			else
				beneficialVisualFizzle(mob,null,"<S-NAME> attempt(s) to hide and fail(s).");
		}
		else
		{
			CMMsg msg=CMClass.getMsg(mob,null,this,auto?CMMsg.MSG_OK_ACTION:(CMMsg.MSG_DELICATE_HANDS_ACT|CMMsg.MASK_MOVE),str,CMMsg.NO_EFFECT,null,CMMsg.NO_EFFECT,null);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				invoker=mob;
				Ability newOne=(Ability)this.copyOf();
				((Ranger_Hide)newOne).bonus=getXLEVELLevel(mob)*2;
				if(mob.fetchEffect(newOne.ID())==null)
					mob.addEffect(newOne);
				mob.recoverEnvStats();
			}
			else
				success=false;
		}
		return success;
	}
}
