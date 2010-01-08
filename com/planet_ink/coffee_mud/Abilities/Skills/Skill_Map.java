package com.planet_ink.coffee_mud.Abilities.Skills;
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
public class Skill_Map extends StdSkill
{
	public String ID() { return "Skill_Map"; }
	public String name(){ return "Make Maps";}
	public String displayText(){return "(Mapping)";}
	protected int canAffectCode(){return Ability.CAN_MOBS;}
	protected int canTargetCode(){return Ability.CAN_ITEMS;}
	public int abstractQuality(){return Ability.QUALITY_INDIFFERENT;}
	private static final String[] triggerStrings = {"MAP"};
	public String[] triggerStrings(){return triggerStrings;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_CALLIGRAPHY;}

	Vector roomsMappedAlready=new Vector();
	protected Item map=null;

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("You stop mapping.");
		map=null;
	}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;
		if((map.owner()==null)
		||(map.owner()!=mob))
			unInvoke();
		else
		if((msg.amISource(mob))
		&&(map!=null)
		&&(msg.targetMinor()==CMMsg.TYP_ENTER)
		&&(msg.target()!=null)
		&&(msg.target() instanceof Room)
		&&(CMLib.flags().canBeSeenBy(msg.target(),msg.source()))
		&&(!roomsMappedAlready.contains(msg.target()))
        &&(!CMath.bset(msg.target().envStats().sensesMask(),EnvStats.SENSE_ROOMUNMAPPABLE)))
		{
			roomsMappedAlready.addElement(msg.target());
			map.setReadableText(map.readableText()+";"+CMLib.map().getExtendedRoomID((Room)msg.target()));
			if(map instanceof com.planet_ink.coffee_mud.Items.interfaces.Map)
				((com.planet_ink.coffee_mud.Items.interfaces.Map)map).doMapArea();
		}

		super.executeMsg(myHost,msg);
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		Ability A=mob.fetchEffect(ID());
		if(A!=null)
		{
			A.unInvoke();
			return true;
		}
		if(mob.charStats().getStat(CharStats.STAT_INTELLIGENCE)<5)
		{
			mob.tell("You are too stupid to actually make a map.");
			return false;
		}
		Item target=getTarget(mob,null,givenTarget,commands,Wearable.FILTER_UNWORNONLY);
		if(target==null)return false;

		Item item=target;
		if(!CMLib.flags().isReadable(item))
		{
			mob.tell("You can't map on that.");
			return false;
		}

		if(item instanceof Scroll)
		{
			mob.tell("You can't map on a scroll.");
			return false;
		}

		if(item instanceof com.planet_ink.coffee_mud.Items.interfaces.Map)
		{
			if(!item.ID().equals("BardMap"))
			{
				mob.tell("There's no more room to add to that map.");
				return false;
			}
		}
		else
		if(item.readableText().length()>0)
		{
			mob.tell("There's no more room to map on that.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MSG_WRITE,"<S-NAME> start(s) mapping on <T-NAMESELF>.",CMMsg.MSG_WRITE,";",CMMsg.MSG_WRITE,"<S-NAME> start(s) mapping on <T-NAMESELF>.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(!item.ID().equals("BardMap"))
				{
					Item B=CMClass.getItem("BardMap");
					B.setContainer(item.container());
					B.setName(item.Name());
					B.setBaseEnvStats(item.baseEnvStats());
					B.setBaseValue(item.baseGoldValue()*2);
					B.setDescription(item.description());
					B.setDisplayText(item.displayText());
					B.setMaterial(item.material());
					B.setRawLogicalAnd(item.rawLogicalAnd());
					B.setRawProperLocationBitmap(item.rawProperLocationBitmap());
					B.setSecretIdentity(item.secretIdentity());
					CMLib.flags().setRemovable(B,CMLib.flags().isRemovable(item));
					B.setUsesRemaining(item.usesRemaining());
					item.destroy();
					mob.addInventory(B);
					item=B;
				}
				map=item;
				if(!roomsMappedAlready.contains(mob.location()))
				{
					roomsMappedAlready.addElement(mob.location());
					map.setReadableText(map.readableText()+";"+CMLib.map().getExtendedRoomID(mob.location()));
					if(map instanceof com.planet_ink.coffee_mud.Items.interfaces.Map)
						((com.planet_ink.coffee_mud.Items.interfaces.Map)map).doMapArea();
				}
				String rooms=item.readableText();
				int x=rooms.indexOf(";");
				while(x>=0)
				{
					String roomID=rooms.substring(0,x);
					Room room=CMLib.map().getRoom(roomID);
					if(room!=null)
						if(!roomsMappedAlready.contains(room))
							roomsMappedAlready.addElement(room);
					rooms=rooms.substring(x+1);
					x=rooms.indexOf(";");
				}
				beneficialAffect(mob,mob,asLevel,0);
			}
		}
		else
			mob.location().show(mob,target,CMMsg.MSG_OK_VISUAL,"<S-NAME> attempt(s) to start mapping on <T-NAMESELF>, but mess(es) up.");
		return success;
	}

}
