package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2004 Bo Zimmerman

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
public class Skill_Mimicry extends BardSkill
{
	public String ID() { return "Skill_Mimicry"; }
	public String name(){ return "Mimicry";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.OK_OTHERS;}
	private static final String[] triggerStrings = {"MIMICRY","MIMIC"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	private CMMsg lastMsg=null;
	private boolean disabled=false;

	/** this method defines how this thing responds
	 * to environmental changes.  It may handle any
	 * and every message listed in the CMMsg interface
	 * from the given Environmental source */
	public void executeMsg(Environmental affecting, CMMsg msg)
	{
		super.executeMsg(affecting,msg);
		if((affecting instanceof MOB)&&(!Sense.aliveAwakeMobile((MOB)affecting,true)))
			return;
		if(disabled) return;
		
		if(((!(affecting instanceof MOB))||(!msg.amISource((MOB)affecting)))
		&&((text().length()==0)||(text().equalsIgnoreCase(msg.source().Name())))
		&&((msg.sourceMinor()!=CMMsg.TYP_EMOTE)
			||((msg.tool()!=null)&&(msg.tool().ID().equals("Social")))))
			lastMsg=msg;
	}

	public void fixSNameTo(CMMsg msg, MOB sMOB, Environmental ticking)
	{
		//String src=msg.sourceMessage();
		String trg=msg.targetMessage();
		String oth=msg.othersMessage();
		//if(src!=null) src=Util.replaceAll(src,"<S-NAME>",ticking.name());
		//if(src!=null) src=Util.replaceAll(src,"You ",ticking.name()+" ");
		//if(src!=null) src=Util.replaceAll(src,"Your ",ticking.name()+"`s ");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-NAME>",ticking.name());
		if(oth!=null) oth=Util.replaceAll(oth,"<S-NAME>",ticking.name());
		//if(src!=null) src=Util.replaceAll(src,"<S-HIM-HERSELF>","itself");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-HIM-HERSELF>","itself");
		if(oth!=null) oth=Util.replaceAll(oth,"<S-HIM-HERSELF>","itself");
		//if(src!=null) src=Util.replaceAll(src,"<S-HIS-HERSELF>","itself");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-HIS-HERSELF>","itself");
		if(oth!=null) oth=Util.replaceAll(oth,"<S-HIS-HERSELF>","itself");
		//if(src!=null) src=Util.replaceAll(src,"<S-HIM-HER>","it");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-HIM-HER>","it");
		if(oth!=null) oth=Util.replaceAll(oth,"<S-HIM-HER>","it");
		//if(src!=null) src=Util.replaceAll(src,"<S-HE-SHE>","it");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-HE-SHE>","it");
		if(oth!=null) oth=Util.replaceAll(oth,"<S-HE-SHE>","it");
		//if(src!=null) src=Util.replaceAll(src,"<S-HIS-HER>","its");
		if(trg!=null) trg=Util.replaceAll(trg,"<S-HIS-HER>","its");
		if(oth!=null) oth=Util.replaceAll(oth,"<S-HIS-HER>","its");
		msg.modify(sMOB,sMOB,msg.tool(),
				   msg.sourceCode(),oth,
				   msg.targetCode(),trg,
				   msg.othersCode(),oth);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		super.tick(ticking,tickID);
		CMMsg msg=lastMsg;
		if(msg==null) return true;
		lastMsg=null;
		if(((affected instanceof MOB)&&(!Sense.aliveAwakeMobile((MOB)affected,true))))
			return true;
		msg=msg.copyOf();
		MOB sMOB=msg.source();
		if(msg.sourceMinor()==CMMsg.TYP_EMOTE)
		{
			if(affected instanceof MOB)
				msg.modify((MOB)affected,msg.target(),msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			else
			{
				MOB newSMOB=CMClass.getMOB("StdMOB");
				newSMOB.baseCharStats().setStat(CharStats.GENDER,'N');
				newSMOB.setName(affected.name());
				newSMOB.recoverCharStats();
				msg.modify(newSMOB,msg.source(),msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			}
		}
		else
		if((msg.tool()!=null)&&(msg.tool().ID().equals("Social")))
		{
			MOB target=null;
			if((msg.target()!=null)&&(msg.target() instanceof MOB))
				target=msg.source();
			if(affected instanceof MOB)
				msg.modify((MOB)affected,target,msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			else
			{
				MOB newSMOB=CMClass.getMOB("StdMOB");
				newSMOB.baseCharStats().setStat(CharStats.GENDER,'N');
				newSMOB.setName(affected.name());
				newSMOB.recoverCharStats();
				msg.modify(newSMOB,target,msg.tool(),
						   msg.sourceCode(),msg.sourceMessage(),
						   msg.targetCode(),msg.targetMessage(),
						   msg.othersCode(),msg.othersMessage());
			}
		}
		else
			return true;
		disabled=true;
		if((msg!=null)
		&&(sMOB.location()!=null)
		&&(sMOB.location().okMessage(sMOB,msg)))
		{
			if(msg.source().location()==null)
			{
				msg.source().setLocation(sMOB.location());
				sMOB.location().send(msg.source(),msg);
				msg.source().setLocation(null);
			}
			else
				sMOB.location().send(msg.source(),msg);
		}
		disabled=false;
		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_QUIETMOVEMENT|(auto?CMMsg.MASK_GENERAL:0),auto?"":"<S-NAME> begin(s) mimicing <T-NAMESELF>.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,asLevel,0);
				Ability A=mob.fetchEffect(ID());
				if((A!=null)&&(target!=mob)) A.setMiscText(target.Name());
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to mimic <T-NAMESELF>, but fail(s).");
		return success;
	}
}
