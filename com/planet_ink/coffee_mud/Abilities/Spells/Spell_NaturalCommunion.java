package com.planet_ink.coffee_mud.Abilities.Spells;
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
import com.planet_ink.coffee_mud.Libraries.interfaces.TrackingLibrary;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;



import java.util.*;

/* 
   Copyright 2000-2014 Bo Zimmerman

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
@SuppressWarnings("rawtypes")
public class Spell_NaturalCommunion extends Spell
{
	public String ID() { return "Spell_NaturalCommunion"; }
	public String name(){return "Natural Communion";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canTargetCode(){return 0;}
	protected int canAffectCode(){return 0;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}

	public void communeWithThisRoom(MOB mob, Room room, List<String> stuff)
	{
		if(!CMLib.flags().canAccess(mob, room))
			return;
		if((room.domainType()&Room.INDOORS)==0)
		{
			try
			{
				String desc=Room.outdoorDomainDescs[room.domainType()].toLowerCase();
				if(!stuff.contains(desc))
					stuff.add(desc);
			}
			catch(Exception t) { }
		}
		int resource=room.myResource()&RawMaterial.RESOURCE_MASK;
		if(RawMaterial.CODES.IS_VALID(resource))
		{
			Physical found=CMLib.materials().makeResource(room.myResource(),Integer.toString(room.domainType()),false,null);
			if(found!=null)
			{
				final String name;
				if(found instanceof RawMaterial)
					name=RawMaterial.CODES.NAME(((RawMaterial) found).material()).toLowerCase();
				else
					name=found.name();
				if(!stuff.contains(name))
					stuff.add(name);
				found.destroy();
			}
		}
		for(final Enumeration<MOB> m = room.inhabitants(); m.hasMoreElements(); )
		{
			final MOB M=m.nextElement();
			if((CMLib.flags().isVegetable(M))&&(!stuff.contains(M.name())))
				stuff.add(M.name());
			else
			if((CMLib.flags().isAnimalIntelligence(M))&&(!stuff.contains(M.name())))
				stuff.add(M.name());
		}
	}
	
	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		Room targetR=mob.location();
		if(targetR==null)
			return false;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		
		int chance=0;
		if((mob.location().domainType()&Room.INDOORS)>0)
			chance-=25;
		boolean success=proficiencyCheck(mob,chance,auto);
		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,targetR,this,somanticCastCode(mob,targetR,auto),auto?"":"^S<S-NAME> commune(s) with <S-HIS-HER> natural surroundings.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				int radius=3 + super.getXLEVELLevel(mob);
				List<Room> rooms=CMLib.tracking().getRadiantRooms(mob.location(), new TrackingLibrary.TrackingFlags(), radius);
				List<String> stuff=new Vector<String>();
				communeWithThisRoom(mob,mob.location(),stuff);
				for(Room R : rooms)
					communeWithThisRoom(mob,R,stuff);
				mob.tell("Your surroundings show the following natural signs: "+CMLib.english().toEnglishStringList(stuff.toArray(new String[0]))+".");
			}
		}
		else
			beneficialVisualFizzle(mob,targetR,"<S-NAME> attempt(s) to commune with nature, and fail(s).");


		// return whether it worked
		return success;
	}
}
