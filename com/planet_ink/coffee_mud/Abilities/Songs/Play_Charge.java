package com.planet_ink.coffee_mud.Abilities.Songs;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;


/* 
   Copyright 2000-2005 Bo Zimmerman

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
public class Play_Charge extends Play
{
	public String ID() { return "Play_Charge"; }
	public String name(){ return "Charge!";}
	public int quality(){ return BENEFICIAL_OTHERS;}
	protected int canAffectCode(){return 0;}
	protected boolean persistantSong(){return false;}
	Vector chcommands=null;

	protected void inpersistantAffect(MOB mob)
	{
		Ability A=CMClass.getAbility("Fighter_Charge");
		if(A!=null) A.invoke(mob,chcommands,null,true,0);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto, int asLevel)
	{
		if((commands.size()==0)&&(!mob.isInCombat()))
		{
			mob.tell("Play charge at whom?");
			return false;
		}
		if(commands.size()==0)
			commands.addElement(mob.getVictim().name());
		chcommands=commands;
		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		return true;
	}
}
