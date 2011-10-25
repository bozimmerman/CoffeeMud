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
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;

/* 
   Copyright 2000-2011 Bo Zimmerman

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
public class Spell_KnowFate extends Spell
{
	public String ID() { return "Spell_KnowFate"; }
	public String name(){return "Know Fate";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return 0;}
	public int classificationCode(){ return Ability.ACODE_SPELL|Ability.DOMAIN_DIVINATION;}

	public boolean invoke(MOB mob, Vector commands, Physical givenTarget, boolean auto, int asLevel)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;

		boolean success=proficiencyCheck(mob,0,auto);

		if(success)
		{
			CMMsg msg=CMClass.getMsg(mob,target,this,somanticCastCode(mob,target,auto),auto?"":"^S<S-NAME> concentrate(s) on <T-NAMESELF>!^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				
        String[] aliasNames=new String[0];
        if(mob.playerStats()!=null)
        	aliasNames=mob.playerStats().getAliasNames();
        List<List<String>> combatV=new LinkedList<List<String>>();
        for(int i=0;i<aliasNames.length;i++)
        {
        	String alias=mob.playerStats().getAlias(aliasNames[i]);
          if(alias.length()>0)
          {
              Vector<String> all_stuff=CMParms.parseSquiggleDelimited(alias,true);
              for(String stuff : all_stuff)
              {
              	List THIS_CMDS=new XVector();
              	combatV.add(THIS_CMDS);
                Vector preCommands=CMParms.parse(stuff);
                for(int v=preCommands.size()-1;v>=0;v--)
                    THIS_CMDS.add(0,preCommands.elementAt(v));
              }
          }
        }
				
        int iwin=0;
        int hewin=0;
        long ihp=0;
        long hehp=0;
        
        for(int tries=0;tries<0;tries++)
        {
					MOB newMOB=(MOB)mob.copyOf();
					MOB newVictiM=(MOB)target.copyOf();
					Room arenaR=CMClass.getLocale("StdRoom");
					arenaR.setArea(mob.location().getArea());
					Session fakeS=(Session)CMClass.getCommon("FakeSession");
					newMOB.setSession(fakeS);
					arenaR.bringMobHere(newMOB,false);
					arenaR.bringMobHere(newVictiM,false);
					newMOB.setVictim(newVictiM);
					newVictiM.setVictim(newMOB);
					newMOB.setStartRoom(null);
					newVictiM.setStartRoom(null);
	        	
					while((!newMOB.amDead())&&(!newVictiM.amDead())
					&&(!newMOB.amDestroyed())&&(!newVictiM.amDestroyed()))
					{
						if(newMOB.commandQueSize()==0)
							for(List<String> cmd : combatV)
								newMOB.enqueCommand(cmd, 0, 0);
		        try 
		        {
		        	CMLib.commands().postStand(newMOB,true);
		        	CMLib.commands().postStand(newVictiM,true);
		        	newMOB.tick(newMOB,Tickable.TICKID_MOB);
		          newVictiM.tick(newVictiM,Tickable.TICKID_MOB);
		        } catch(Throwable t) {
		        	Log.errOut("Spell_KnowFate",t);
		        }
					}
					
					if((!newMOB.amDead())&&(!newMOB.amDestroyed()))
					{
						iwin++;
						ihp+=newMOB.curState().getHitPoints();
					}
					else
					{
						hewin++;
						hehp+=newMOB.curState().getHitPoints();
					}
					newMOB.destroy();
					newVictiM.destroy();
					arenaR.setArea(null);
					arenaR.destroy();
        }
        if(iwin>hewin)
        	mob.tell(iwin+"% of the time, you defeat "+target.charStats().himher()+" with "+(ihp/iwin)+" hit points left.");
        else
        	mob.tell(hewin+"% of the time you die, and "+target.charStats().himher()+" still has "+(hehp/hewin)+" hit points left.");
			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> concentrate(s) on <T-NAMESELF>, but look(s) frustrated.");

		// return whether it worked
		return success;
	}
}
