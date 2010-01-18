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
public class Spell_AnalyzeDweomer extends Spell
{
	public String ID() { return "Spell_AnalyzeDweomer"; }
	public String name(){return "Analyze Item";}
    public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canTargetCode(){return CAN_ITEMS;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Item target=getTarget(mob,mob.location(),givenTarget,commands,Wearable.FILTER_ANY);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,verbalCastCode(mob,target,auto),auto?"":"^S<S-NAME> analyze(s) the nature of <T-NAMESELF> carefully.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				StringBuffer str=new StringBuffer("");
				if(target instanceof Armor)
                {
					str.append("It is a kind of armor.  ");
                    if(!target.rawLogicalAnd())
                        str.append("It is worn on any one of the following: ");
                    else
                        str.append("It is worn on all of the following: ");
                    Wearable.CODES codes = Wearable.CODES.instance();
                    for(long wornCode : codes.all())
                    	if(wornCode!=Wearable.IN_INVENTORY)
	                    {
	                        if((codes.name(wornCode).length()>0)
	                        &&(((target.rawProperLocationBitmap()&wornCode)==wornCode)))
	                            str.append(codes.name(wornCode).toLowerCase()+" ");
	                    }
                    str.append(".  ");
                }
				if((target instanceof Container)&&(((Container)target).capacity()>0))
					str.append("It is a container.  ");
				if(target instanceof Coins)
					str.append("It is currency. ");
				if(target instanceof Drink)
					str.append("You can drink it. ");
				if(target instanceof Food)
					str.append("You can eat it.  ");
				if(target instanceof Pill)
					str.append("It is a magic pill.  ");
				if(target instanceof Potion)
					str.append("It is a magic potion.  ");
				if(target instanceof Light)
					str.append("It is a light source.  ");
				if(target instanceof com.planet_ink.coffee_mud.Items.interfaces.Map)
					str.append("It is a map.  ");
				if(target instanceof MiscMagic)
					str.append("It has a magical aura.  ");
				if(target instanceof Scroll)
					str.append("It is a magic scroll.  ");
				if(target instanceof Wand)
					str.append("It is a magic wand.  ");
				if(target instanceof Electronics)
					str.append("It is some sort of high technology.  ");
				if(target instanceof InnKey)
					str.append("It is an Inn key.  ");
				else
				if(target instanceof Key)
					str.append("It is a key.  ");
				if(target instanceof LandTitle)
					str.append("It is a property title.  ");
				if(CMLib.flags().isReadable(target))
					str.append("It is readable.  ");
				if(target instanceof DeadBody)
					str.append("It is a corpse of a "+((DeadBody)target).charStats().getMyRace().name()+".  ");
				if(target instanceof Weapon)
				{
					Weapon w=(Weapon)target;
					str.append("It is a "+Weapon.CLASS_DESCS[w.weaponClassification()].toLowerCase()+" weapon.  ");
					str.append("It does "+Weapon.TYPE_DESCS[w.weaponType()].toLowerCase()+" damage.  ");
					if(w.minRange()>0)
						str.append("It has a minimum range of "+w.minRange()+".  ");
					if(w.maxRange()>w.minRange())
						str.append("It has a maximum range of "+w.maxRange()+".  ");
				}
				str.append("It is made of "+RawMaterial.CODES.NAME(target.material()).toLowerCase()+".  ");
				if(mob.isMonster())
					CMLib.commands().postSay(mob,null,str.toString().trim(),false,false);
				else
					mob.tell(str.toString().trim());
			}

		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> analyze(s) the nature of <T-NAMESELF>, looking more frustrated every second.");


		// return whether it worked
		return success;
	}
}
