package com.planet_ink.coffee_mud.Abilities.Songs;
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

public class Skill_Disguise extends BardSkill
{
	@Override
	public String ID()
	{
		return "Skill_Disguise";
	}

	private final static String localizedName = CMLib.lang().L("Disguise");

	@Override
	public String name()
	{
		return localizedName;
	}

	@Override
	public String description()
	{
		final StringBuffer ret=new StringBuffer("");
		for (final String what : whats)
			if(what==null)
				ret.append(". ");
			else
				ret.append(what+" ");
		return ret.toString();
	}

	private final static String localizedStaticDisplay = CMLib.lang().L("(In Disguise)");

	@Override
	public String displayText()
	{
		return localizedStaticDisplay;
	}

	@Override
	protected int canAffectCode()
	{
		return CAN_MOBS;
	}

	@Override
	protected int canTargetCode()
	{
		return 0;
	}

	@Override
	public int abstractQuality()
	{
		return Ability.QUALITY_OK_SELF;
	}

	private static final String[] triggerStrings =I(new String[] {"DISGUISE"});
	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_SKILL|Ability.DOMAIN_DECEPTIVE;
	}

	protected final static String[] whats={
		//0!	 1! 	 2!    3!     4!	   5!     6!	  7!		  8!
		"WEIGHT","LEVEL","SEX","RACE","HEIGHT","NAME","CLASS","ALIGNMENT","AGE"};
	protected final static int[] levels={2,10,4,14,6,8,0,18,12};
	protected String[] values=new String[whats.length];

	@Override
	protected void cloneFix(Ability E)
	{
		values=new String[whats.length];
		for(int i=0;i<values.length;i++)
			values[i]=null;
	}

	@Override
	public String text()
	{
		final StringBuilder str=new StringBuilder("");
		for(int i=0;i<whats.length;i++)
		{
			if(values[i]!=null)
				str.append(" ").append(whats[i]).append("=\"").append(values[i]).append("\"");
		}
		return str.toString();
	}
	
	@Override
	public void setMiscText(final String txt)
	{
		values=new String[whats.length];
		for(int i=0;i<values.length;i++)
			values[i] = CMParms.getParmStr(txt, whats[i], null);
	}
	
	@Override
	public void affectPhyStats(Physical myHost, PhyStats affectableStats)
	{
		if(values[5]!=null)
			affectableStats.setName(values[5]);
		if(values[7]!=null)
			if(values[7].equalsIgnoreCase("good"))
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_GOOD);
			else
			if(values[7].equalsIgnoreCase("evil"))
				affectableStats.setDisposition(affectableStats.disposition()|PhyStats.IS_EVIL);
	}

	@Override
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
		if(values[8]!=null)
			affectableStats.setStat(CharStats.STAT_AGE,CMath.s_int(values[8]));
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((myHost==null)||(!(myHost instanceof MOB)))
			return true;
		final MOB mob=(MOB)myHost;
		if(msg.amITarget(mob)
		&&(CMLib.flags().canBeSeenBy(mob,msg.source()))
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&((values[0]!=null)||(values[4]!=null)))
		{
			String omsg=null;
			if(msg.othersMessage()!=null)
			{
				omsg=CMStrings.replaceAll(msg.othersMessage(),"<T-NAME>",mob.name());
				omsg=CMStrings.replaceAll(omsg,"<T-NAMESELF>",mob.name());
			}
			msg.modify(msg.source(),this,msg.tool(),
					   msg.sourceCode(),msg.sourceMessage(),
					   msg.targetCode(),msg.targetMessage(),
					   msg.othersCode(),omsg);
		}
		return true;
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg)
	{
		super.executeMsg(myHost,msg);
		if((myHost==null)||(!(myHost instanceof MOB)))
			return;
		final MOB mob=(MOB)myHost;
		if(msg.amITarget(this)
		&&(CMLib.flags().canBeSeenBy(mob,msg.source()))
		&&((msg.targetMinor()==CMMsg.TYP_LOOK)||(msg.targetMinor()==CMMsg.TYP_EXAMINE))
		&&((values[0]!=null)||(values[4]!=null)))
		{
			final StringBuffer myDescription=new StringBuffer("");
			if(!mob.isMonster())
			{
				final String levelStr=mob.charStats().displayClassLevel(mob,false);
				myDescription.append(mob.name(msg.source())+" the "+mob.charStats().raceName()+" is a "+levelStr+".\n\r");
			}
			int height=mob.phyStats().height();
			int weight=mob.basePhyStats().weight();
			if(values[0]!=null)
				weight=CMath.s_int(values[0]);
			if(values[4]!=null)
				height=CMath.s_int(values[4]);
			if(height>0)
				myDescription.append(mob.charStats().HeShe()+" is "+height+" inches tall and weighs "+weight+" pounds.\n\r");
			myDescription.append(mob.healthText(msg.source())+"\n\r\n\r");
			myDescription.append(mob.description()+"\n\r\n\r");
			myDescription.append(mob.charStats().HeShe()+" is wearing:\n\r"+CMLib.commands().getEquipment(msg.source(),mob));
			msg.source().tell(myDescription.toString());
		}
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(!ID().equals("Skill_Disguise"))
			return super.invoke(mob,commands,givenTarget,auto,asLevel);

		Skill_Disguise A=(Skill_Disguise)mob.fetchEffect("Skill_Disguise");
		if(A==null)
			A=(Skill_Disguise)mob.fetchEffect("Skill_MarkDisguise");

		final String validChoices="Weight, sex, race, height, age, name, level, class, or alignment";
		if(commands.size()==0)
		{
			if(A==null)
			{
				mob.tell(L("Disguise what? @x1.",validChoices));
				return false;
			}
			A.unInvoke();
			mob.tell(L("You remove your disguise."));
			return true;
		}
		final String what=commands.get(0);
		int which=-1;
		for(int i=0;i<whats.length;i++)
		{
			if(whats[i].startsWith(what.toUpperCase()))
				which=i;
		}
		if(which<0)
		{
			mob.tell(L("Disguise what? '@x1' is not a valid choice.  Valid choices are: @x2.",what,validChoices));
			return false;

		}
		if((CMLib.ableMapper().qualifyingLevel(mob,this)>0)
		   &&((CMLib.ableMapper().qualifyingClassLevel(mob,this)+getXLEVELLevel(mob))<levels[which]))
		{
			mob.tell(L("You must have @x1 levels in this skill to use that disguise.",""+levels[which]));
			return false;
		}
		commands.remove(0);
		if(commands.size()==0)
		{
			mob.tell(L("Disguise @x1 in what way?  Be more specific.",whats[which].toLowerCase()));
			return false;
		}
		String how=CMStrings.removeColors(CMParms.combine(commands,0));

		int adjustment=0;
		switch(which)
		{
		case 0: //weight
		{
			if(CMath.s_int(how)<=0)
			{
				mob.tell(L("You cannot disguise your weight as @x1 pounds!",how));
				return false;
			}
			int x=mob.basePhyStats().weight()-CMath.s_int(how);
			if(x<0)
				x=x*-1;
			adjustment=-((int)Math.round(CMath.div(x,mob.basePhyStats().weight())*100.0));
			break;
		}
		case 1: // level
			if((CMath.s_int(how)<=0)||CMath.s_int(how)>100000)
			{
				mob.tell(L("You cannot disguise your level as @x1!",how));
				return false;
			}
			how=Integer.toString(CMath.s_int(how));
			break;
		case 2: // sex
			if(how.toUpperCase().startsWith("M"))
				how="male";
			else
			if(how.toUpperCase().startsWith("F"))
				how="female";
			else
			if(how.toUpperCase().startsWith("N"))
				how="neuter";
			else
			if(how.toUpperCase().startsWith("B"))
				how="male";
			else
			if(how.toUpperCase().startsWith("G"))
				how="girl";
			else
			{
				mob.tell(L("'@x1' is a sex which cannot be guessed at!",how));
				return false;
			}
			break;
		case 3: // race
			{
				if(CMClass.getRace(how)==null)
				{
					mob.tell(L("'@x1' is an unknown race!",how));
					return false;
				}
				how=CMClass.getRace(how).name();
				break;
			}
		case 4: // height
		{
			if(CMath.s_int(how)<=0)
			{
				mob.tell(L("You cannot disguise your height as @x1 inches!",how));
				return false;
			}
			int x=mob.phyStats().height()-CMath.s_int(how);
			if(x<0)
				x=x*-1;
			adjustment=-((int)Math.round(CMath.div(x,mob.phyStats().height())*100.0));
			break;
		}
		case 5: // name
		{
			if((how.indexOf(' ')>=0)||(how.indexOf('<')>=0))
			{
				mob.tell(L("Your disguise name may not have a space in it, or illegal characters."));
				return false;
			}
			else
			if(CMLib.players().playerExists(how))
			{
				mob.tell(L("You cannot disguise yourself as a player except through Mark Disguise."));
				return false;
			}
			else
			if(CMLib.login().isBadName(how))
			{
				mob.tell(L("You cannot disguise yourself as that."));
				return false;
			}
			else
				how=CMStrings.capitalizeAndLower(how);
			break;
		}
		case 6: // class
			{
				if(how.equalsIgnoreCase("Archon"))
				{
					mob.tell(L("You cannot disguise yourself as an Archon."));
					return false;
				}
				if(CMClass.findCharClass(how)==null)
				{
					mob.tell(L("'@x1' is an unknown character class!",how));
					return false;
				}
				how=CMStrings.capitalizeAndLower(how);
				break;
			}
		case 7: // alignment
		{
			if((!how.equalsIgnoreCase("good"))&&(!how.equalsIgnoreCase("evil")))
			{
				mob.tell(L("You may only disguise your alignment as 'good' or 'evil'."));
				return false;
			}
			break;
		}
		case 8: // age
		{
			if((CMath.s_int(how)<=0)||(CMath.s_int(how)>100000))
			{
				mob.tell(L("You cannot disguise your age as @x1 years!",how));
				return false;
			}
			int x=mob.baseCharStats().getStat(CharStats.STAT_AGE)-CMath.s_int(how);
			if(x<0)
				x=x*-1;
			adjustment=-((int)Math.round(CMath.div(x,mob.baseCharStats().getStat(CharStats.STAT_AGE))*100.0));
			break;
		}
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		final boolean success=proficiencyCheck(mob,adjustment,auto);

		if(success)
		{
			final CMMsg msg=CMClass.getMsg(mob,mob,null,CMMsg.MSG_DELICATE_HANDS_ACT|(auto?CMMsg.MASK_ALWAYS:0),L("<S-NAME> turn(s) away for a second."));
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(A==null)
					beneficialAffect(mob,mob,asLevel,0);
				if(A==null)
					A=(Skill_Disguise)mob.fetchEffect("Skill_Disguise");
				if(A!=null)
				{
					A.values[which]=how;
					A.makeLongLasting();
				}
				mob.recoverCharStats();
				mob.recoverPhyStats();
				mob.location().recoverRoomStats();
			}
		}
		else
			return beneficialVisualFizzle(mob,null,L("<S-NAME> turn(s) away and then back, but look(s) the same."));

		return success;
	}
}
