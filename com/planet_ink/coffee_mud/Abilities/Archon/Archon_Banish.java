package com.planet_ink.coffee_mud.Abilities.Archon;
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

public class Archon_Banish extends ArchonSkill
{
	boolean doneTicking=false;
	public String ID() { return "Archon_Banish"; }
	public String name(){ return "Banish";}
	public String displayText(){ return "(Banished)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int abstractQuality(){return Ability.QUALITY_MALICIOUS;}
	private static final String[] triggerStrings = {"BANISH"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.ACODE_SKILL;}
	public int maxRange(){return 1;}
	public int usageType(){return USAGE_MOVEMENT;}
	protected Room prison=null;

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if(affected instanceof Room)
		{
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(msg.source()!=null)
			&&(msg.source().location()!=null)
			&&(msg.sourceMinor()!=CMMsg.TYP_LEAVE))
			{
				boolean summon=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING);
				boolean teleport=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING);
				boolean shere=(msg.source().location()==affected)||(msg.source().location().getArea()==affected);
				if(((!shere)&&(!summon)&&(teleport))
				   ||((shere)&&(summon)))
				{
					msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
					return false;
				}
			}
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(msg.source()!=null)
			&&(msg.source().location()!=null)
			&&(msg.sourceMinor()!=CMMsg.TYP_ENTER))
			{
				boolean shere=(msg.source().location()==affected)||(msg.source().location().getArea()==affected);
				boolean summon=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING);
				boolean teleport=CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_TRANSPORTING);
				if(((shere)&&(!summon)&&(teleport))
				   ||((!shere)&&(summon)))
				{
					msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
					return false;
				}
			}
			if((msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(msg.source()!=null)
			&&(msg.source().location()!=null)
			&&((msg.source().location()==affected)
			   ||(msg.source().location().getArea()==affected))
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_SUMMONING)))
			{
				msg.source().location().showHappens(CMMsg.MSG_OK_VISUAL,"Magic energy fizzles and is absorbed into the air.");
				return false;
			}
			if(msg.sourceMinor()==CMMsg.TYP_RECALL)
			{
				if((msg.source()!=null)&&(msg.source().location()!=null))
					msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to recall, but the magic fizzles.");
				return false;
			}
		}
		return true;
	}

	public void unInvoke()
	{
		if((affected==null)||(!(affected instanceof MOB)))
		{
			super.unInvoke();
			return;
		}
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("You are released from banishment!");
		mob.getStartRoom().bringMobHere(mob,true);
		if(prison!=null){ prison.destroy(); prison=null;}
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
	        Room myPrison = CMLib.map().getRoom(CMParms.combine(commands,1));
		if(myPrison != null && !"".equals(myPrison.roomID()))
			while(commands.size() > 1)
			{
				commands.removeElementAt(1);
			}
		else
			myPrison = null;
		
		MOB target=getTargetAnywhere(mob,commands,givenTarget,false,true,false);
		if(target==null) return false;
		
		Archon_Banish A=(Archon_Banish)target.fetchEffect(ID());
		if(A!=null)
		{
			A.unInvoke();
			mob.tell(target.Name()+" is released from banishment.");
			return true;
		}

		if(!super.invoke(mob,commands,givenTarget,auto, asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,CMMsg.MASK_MOVE|CMMsg.TYP_JUSTICE|(auto?CMMsg.MASK_ALWAYS:0),auto?"<T-NAME> is banished!":"^F<S-NAME> banish(es) <T-NAMESELF>.^?");
            CMLib.color().fixSourceFightColor(msg);
			if(mob.location().okMessage(mob,msg))
			{
				beneficialAffect(mob,target,asLevel,Integer.MAX_VALUE/2);
				A=(Archon_Banish)target.fetchEffect(ID());
				if(A!=null)
				{
					if(myPrison != null)
					{
						A.prison = (Room) myPrison.copyOf();
					}
					else
					{
							
						A.prison=CMClass.getLocale("StoneRoom");
						A.prison.addNonUninvokableEffect((Ability)copyOf());
						A.prison.setArea(mob.location().getArea());
						A.prison.setDescription("You are standing on an immense, grey stone floor that stretches as far as you can see in all directions.  Rough winds plunging from the dark, starless sky tear savagely at your fragile body.");
						A.prison.setDisplayText("The Hall of Lost Souls");
						A.prison.setRoomID("");
						for(int d=0;d<Directions.DIRECTIONS_BASE.length;d++)
						{
							A.prison.rawExits()[Directions.DIRECTIONS_BASE[d]]=CMClass.getExit("Open");
							A.prison.rawDoors()[Directions.DIRECTIONS_BASE[d]]=A.prison;
						}
                        Ability A2=CMClass.getAbility("Prop_HereSpellCast");
                        if(A2!=null) A2.setMiscText("Spell_Hungerless;Spell_Thirstless");
                        if(A2!=null) A.addNonUninvokableEffect(A2);
					}
					CMLib.commands().postLook(target,true);
					for(int d=0;d<Directions.DIRECTIONS_BASE.length;d++)
					{
						A.prison.rawExits()[Directions.DIRECTIONS_BASE[d]]=CMClass.getExit("Open");
						A.prison.rawDoors()[Directions.DIRECTIONS_BASE[d]]=A.prison;
					}
					A.prison.bringMobHere(target,false);
					mob.location().send(mob,msg);
					mob.location().show(target,null,CMMsg.MSG_OK_VISUAL,"<S-NAME> <S-IS-ARE> banished to " + A.prison.displayText() + "!");
                    Log.sysOut("Banish",mob.name()+" banished "+target.name()+" to "+CMLib.map().getExtendedRoomID(A.prison)+".");
				}
				
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to banish <T-NAMESELF>, but fail(s).");
		return success;
	}
}
