package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Skill_Disguise extends BardSkill
{
	public String ID() { return "Skill_Disguise"; }
	public String name(){ return "Disguise";}
	public String description()
	{
		StringBuffer ret=new StringBuffer("");
		for(int i=0;i<whats.length;i++)
			if(whats[i]==null)
				ret.append(". ");
			else
				ret.append(whats[i]+" ");
		return ret.toString();
	}
	public String displayText(){ return "(In Disguise)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.OK_SELF;}
	private static final String[] triggerStrings = {"DISGUISE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}

	private final static String[] whats={
		//0!     1!      2!    3!     4!       5!     6!      7!
		"WEIGHT","LEVEL","SEX","RACE","HEIGHT","NAME","CLASS","ALIGNMENT"};
	private final static int[] levels={2,10,4,12,6,8,0,18};
	protected final static String[] values=new String[whats.length];

	public void affectEnvStats(Environmental myHost, EnvStats affectableStats)
	{
		if(values[5]!=null)
			affectableStats.setName(values[5]);
		if(values[7]!=null)
			if(values[7].equalsIgnoreCase("good"))
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_GOOD);
			else
			if(values[7].equalsIgnoreCase("evil"))
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_EVIL);
	}

	public void affectCharStats(MOB myHost, CharStats affectableStats)
	{
		if(values[3]!=null)
			affectableStats.setRaceName(values[3]);
		if(values[2]!=null)
			affectableStats.setGenderName(values[2]);
		if(values[1]!=null)
			affectableStats.setDisplayClassLevel(values[1]);
		if(values[6]!=null)
			affectableStats.setDisplayClassName(values[6]);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg)) return false;
		if((myHost==null)||(!(myHost instanceof MOB)))
		   return true;
		MOB mob=(MOB)myHost;
		if(msg.amITarget(mob)
		&&(Sense.canBeSeenBy(mob,msg.source()))
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING)
		&&((values[0]!=null)||(values[4]!=null)))
		{
			String omsg=null;
			if(msg.othersMessage()!=null)
			{
				omsg=Util.replaceAll(msg.othersMessage(),"<T-NAME>",mob.name());
				omsg=Util.replaceAll(omsg,"<T-NAMESELF>",mob.name());
			}
			msg.modify(msg.source(),this,msg.tool(),
					   msg.sourceCode(),msg.sourceMessage(),
					   msg.targetCode(),msg.targetMessage(),
					   msg.othersCode(),omsg);
		}
		return true;
	}
	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((myHost==null)||(!(myHost instanceof MOB)))
		   return;
		MOB mob=(MOB)myHost;
		if(msg.amITarget(this)
		&&(Sense.canBeSeenBy(mob,msg.source()))
		&&(msg.targetMinor()==CMMsg.TYP_EXAMINESOMETHING)
		&&((values[0]!=null)||(values[4]!=null)))
		{
			StringBuffer myDescription=new StringBuffer("");
			if(!mob.isMonster())
			{
				String levelStr=mob.charStats().displayClassLevel(mob,false);
				myDescription.append(mob.name()+" the "+mob.charStats().raceName()+" is a "+levelStr+".\n\r");
			}
			int height=mob.envStats().height();
			int weight=mob.baseEnvStats().weight();
			if(values[0]!=null) weight=Util.s_int(values[0]);
			if(values[4]!=null) height=Util.s_int(values[4]);
			if(height>0)
				myDescription.append(mob.charStats().HeShe()+" is "+height+" inches tall and weighs "+weight+" pounds.\n\r");
			myDescription.append(mob.healthText()+"\n\r\n\r");
			myDescription.append(mob.description()+"\n\r\n\r");
			myDescription.append(mob.charStats().HeShe()+" is wearing:\n\r"+CommonMsgs.getEquipment(msg.source(),mob));
			msg.source().tell(myDescription.toString());
		}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(!ID().equals("Skill_Disguise"))
			return super.invoke(mob,commands,givenTarget,auto,asLevel);

		Skill_Disguise A=(Skill_Disguise)mob.fetchEffect("Skill_Disguise");
		if(A==null) A=(Skill_Disguise)mob.fetchEffect("Skill_MarkDisguise");

		String validChoices="Weight, sex, race, height, name, level, class, or alignment";
		if(commands.size()==0)
		{
			if(A==null)
			{
				mob.tell("Disguise what? "+validChoices+".");
				return false;
			}
			else
			{
				A.unInvoke();
				mob.tell("You remove your disguise.");
				return true;
			}
		}
		String what=(String)commands.firstElement();
		int which=-1;
		for(int i=0;i<whats.length;i++)
			if(whats[i].startsWith(what.toUpperCase()))
				which=i;
		if(which<0)
		{
			mob.tell("Disguise what? '"+what+"' is not a valid choice.  Valid choices are: "+validChoices+".");
			return false;

		}
		if((CMAble.qualifyingLevel(mob,this)>0)
		   &&(CMAble.qualifyingClassLevel(mob,this)<levels[which]))
		{
			mob.tell("You must have "+levels[which]+" levels in this skill to use that disguise.");
			return false;
		}
		commands.removeElementAt(0);
		if(commands.size()==0)
		{
			mob.tell("Disguise "+whats[which].toLowerCase()+" in what way?  Be more specific.");
			return false;
		}
		String how=Util.combine(commands,0);

		int adjustment=0;
		switch(which)
		{
		case 0: //weight
		{
			if(Util.s_int(how)<=0)
			{
				mob.tell("You cannot disguise your weight as "+how+" pounds!");
				return false;
			}
			int x=mob.baseEnvStats().weight()-Util.s_int(how);
			if(x<0) x=x*-1;
			adjustment=-((int)Math.round(Util.div(x,mob.baseEnvStats().weight())*100.0));
			break;
		}
		case 1: // level
			if(Util.s_int(how)<=0)
			{
				mob.tell("You cannot disguise your level as "+how+"!");
				return false;
			}
			break;
		case 2: // sex
			if(how.toUpperCase().startsWith("M")) how="male";
			else
			if(how.toUpperCase().startsWith("F")) how="female";
			else
			if(how.toUpperCase().startsWith("N")) how="neuter";
			else
			if(how.toUpperCase().startsWith("B")) how="male";
			else
			if(how.toUpperCase().startsWith("G")) how="girl";
			else
			{
				mob.tell("'"+how+"' is a sex which cannot be guessed at!");
				return false;
			}
			break;
		case 3: // race
			{
				if(CMClass.getRace(how)==null)
				{
					mob.tell("'"+how+"' is an unknown race!");
					return false;
				}
				else
					how=CMClass.getRace(how).name();
				break;
			}
		case 4: // height
		{
			if(Util.s_int(how)<=0)
			{
				mob.tell("You cannot disguise your height as "+how+" inches!");
				return false;
			}
			int x=mob.envStats().height()-Util.s_int(how);
			if(x<0) x=x*-1;
			adjustment=-((int)Math.round(Util.div(x,mob.envStats().height())*100.0));
			break;
		}
		case 5: // name
		{
			if(how.indexOf(" ")>=0)
			{
				mob.tell("Your disguise name may not have a space in it.");
				return false;
			}
			else
			if(CMClass.DBEngine().DBUserSearch(null,how))
			{
				mob.tell("You cannot disguise yourself as an player except through Mark Disguise.");
				return false;
			}
			else
				how=Util.capitalize(how);
			break;
		}
		case 6: // class
			{
				if(how.equalsIgnoreCase("Archon"))
				{
					mob.tell("You cannot disguise yourself as an Archon.");
					return false;
				}
				if(CMClass.getCharClass(how)==null)
				{
					mob.tell("'"+how+"' is an unknown character class!");
					return false;
				}
				else
					how=CMClass.getCharClass(how).name();
				break;
			}
		case 7: // alignment
		{
			if((!how.equalsIgnoreCase("good"))&&(!how.equalsIgnoreCase("evil")))
			{
				mob.tell("You may only disguise your alignment as 'good' or 'evil'.");
				return false;
			}
			break;
		}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=profficiencyCheck(mob,adjustment,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,mob,null,CMMsg.MSG_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_GENERAL:0),"<S-NAME> turn(s) away for a second.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(A==null)	beneficialAffect(mob,mob,asLevel,0);
				if(A==null) A=(Skill_Disguise)mob.fetchEffect("Skill_Disguise");
				A.values[which]=how;
				mob.recoverCharStats();
				mob.recoverEnvStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialVisualFizzle(mob,null,"<S-NAME> turn(s) away and then back, but look(s) the same.");

		return success;
	}
}
