package com.planet_ink.coffee_mud.Abilities.Songs;
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
public class Skill_Stability extends BardSkill
{
	public String ID() { return "Skill_Stability"; }
	public String name(){return "Stability";}
	public String displayText(){return "";}
	public int abstractQuality(){ return Ability.QUALITY_INDIFFERENT;}
	protected int canAffectCode(){return CAN_MOBS;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_ACROBATIC;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;

		if((affected==null)||(!(affected instanceof MOB)))
			return true;

		MOB mob=(MOB)affected;
		if((msg.tool()!=null)
		&&(msg.tool() instanceof Ability)
		&&(msg.amITarget(affected))
		&&(((Ability)msg.tool()).abstractQuality()==Ability.QUALITY_MALICIOUS)
		&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_MOVING))
		&&((mob.fetchAbility(ID())==null)||proficiencyCheck(null,-40+(2*getXLEVELLevel(mob)),false)))
		{
			Room roomS=null;
			Room roomD=null;
			if((msg.target()!=null)&&(msg.target() instanceof MOB))
				roomD=((MOB)msg.target()).location();
			if((msg.source()!=null)&&(msg.source().location()!=null))
				roomS=msg.source().location();
			if((msg.target()!=null)&&(msg.target() instanceof Room))
				roomD=(Room)msg.target();

			if((roomS!=null)&&(roomD!=null)&&(roomS==roomD))
				roomD=null;

			if(roomS!=null)
				roomS.show((MOB)affected,null,msg.tool(),CMMsg.MSG_OK_VISUAL,"<S-NAME> remain(s) stable despite the <O-NAME>.");
			if(roomD!=null)
				roomD.show((MOB)affected,null,msg.tool(),CMMsg.MSG_OK_VISUAL,"<S-NAME> remain(s) stable despite the <O-NAME>.");
			helpProficiency((MOB)affected);
			return false;
		}
		return true;
	}


}
