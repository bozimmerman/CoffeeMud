package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Play_Instrument extends Play
{
	public String ID() { return "Play_Instrument"; }
	public String name(){ return "Instruments";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_WOODS;}
	public String mimicSpell(){return "";}

	protected void inpersistantAffect(MOB mob)
	{
		if((getSpell()!=null)
		&&((mob!=invoker())||(getSpell().quality()!=MALICIOUS)))
		{
			Vector chcommands=new Vector();
			chcommands.addElement(mob.name());
			((Ability)getSpell().copyOf()).invoke(invoker(),chcommands,null,true,0);
		}
	}


	protected String songOf()
	{
		if(instrument!=null)
			return instrument.name();
		else
			return name();
	}
	protected Ability getSpell()
	{
		return null;
	}
	public int quality()
	{
		if(getSpell()!=null) return getSpell().quality();
		return BENEFICIAL_OTHERS;
	}
	protected boolean persistantSong(){return false;}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
}
