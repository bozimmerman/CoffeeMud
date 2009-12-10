package com.planet_ink.coffee_mud.Abilities.Thief;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary;
import com.planet_ink.coffee_mud.Libraries.interfaces.MoneyLibrary.MoneyDenomination;
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
public class Thief_Forgery extends ThiefSkill
{
	public String ID() { return "Thief_Forgery"; }
	public String name(){ return "Forgery";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return Ability.CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"FORGERY"};
    public int classificationCode(){return Ability.ACODE_THIEF_SKILL|Ability.DOMAIN_CALLIGRAPHY;}
	public String[] triggerStrings(){return triggerStrings;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if(commands.size()<2)
		{
			mob.tell("What would you like to forge, and onto what?");
			return false;
		}
		Item target=mob.fetchInventory(null,(String)commands.lastElement());
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			mob.tell("You don't see '"+((String)commands.lastElement())+"' here.");
			return false;
		}
		commands.removeElementAt(commands.size()-1);

		if((!target.isGeneric())
		   ||((!(target instanceof Scroll))&&(!CMLib.flags().isReadable(target))))
		{
			mob.tell("You can't forge anything on that.");
			return false;
		}

		String forgeWhat=CMParms.combine(commands,0);
		if(forgeWhat.length()==0)
		{
			mob.tell("Forge what onto '"+target.name()+"'?  Try a spell name, a room ID, or a bank note name.");
			return false;
		}

		String newName="";
		String newDisplay="";
		String newDescription="";
		String newSecretIdentity="";
		Room room=CMLib.map().getRoom(forgeWhat);
		if(room!=null)
		{
			Item I=CMClass.getItem("StdTitle");
			((LandTitle)I).setLandPropertyID(CMLib.map().getExtendedRoomID(room));
			newName=I.name();
			newDescription=I.description();
			newDisplay=I.displayText();
			newSecretIdentity=I.secretIdentity();
		}
		if(newName.length()==0)
		{
			Ability A=CMClass.findAbility(forgeWhat);
			if((A!=null)&&((A.classificationCode()&Ability.ALL_ACODES)!=Ability.ACODE_SPELL))
			{
				mob.tell("You can't forge '"+A.name()+"'.");
				return false;
			}
			else
			if(A!=null)
			{
				if(!(target instanceof Scroll))
				{
					mob.tell("You can only forge a spell onto real scrollpaper.");
					return false;
				}
				else
				if(((Scroll)target).getSpells().size()>0)
				{
					mob.tell("That already has real spells on it!");
					return false;
				}
				else
				{
					newName=target.name();
					newDisplay=target.displayText();
					newDescription=target.description();
					newSecretIdentity="a scroll of "+A.name()+" Charges: 10\n";
				}
			}
		}
		if(newName.length()==0)
		{
			MoneyLibrary.MoneyDenomination[] DV=CMLib.beanCounter().getCurrencySet(CMLib.beanCounter().getCurrency(mob));
			for(int i=0;i<DV.length;i++)
			{
				Item note=CMLib.beanCounter().makeBestCurrency(CMLib.beanCounter().getCurrency(mob), DV[i].value);
				if((note!=null)&&(CMLib.english().containsString(note.name(),forgeWhat)))
				{
					newName=note.name();
					newDisplay=note.displayText();
					newDescription=note.description();
					newSecretIdentity=note.rawSecretIdentity();
					break;
				}
			}
		}
		if(newName.length()==0)
		{
			mob.tell("You don't know how to forge a '"+forgeWhat+"'.  Try a spell name, a room ID, or a bank note name.");
			return false;
		}
		forgeWhat=newName;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

        int levelDiff=(mob.envStats().level()+(2*getXLEVELLevel(mob)))-target.envStats().level();
        if(levelDiff>0) levelDiff=0;
        levelDiff*=5;
		boolean success=proficiencyCheck(mob,levelDiff,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_THIEF_ACT,"<S-NAME> forge(s) "+forgeWhat+" on <T-NAMESELF>.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				target.setName(newName);
				target.setDescription(newDescription);
				target.setDisplayText(newDisplay);
				target.setSecretIdentity(newSecretIdentity);
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to forge "+forgeWhat+", but fail(s).");
		return success;
	}
}
