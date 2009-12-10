package com.planet_ink.coffee_mud.Abilities.Paladin;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
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
public class Paladin_Aura extends Paladin
{
	public String ID() { return "Paladin_Aura"; }
	public String name(){ return "Paladin`s Aura";}
    public int classificationCode(){return Ability.ACODE_SKILL|Ability.DOMAIN_HOLYPROTECTION;}
	public Paladin_Aura()
	{
		super();
		paladinsGroup=new Vector();
	}
    protected boolean pass=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if(!super.tick(ticking,tickID)) return false;
		pass=(invoker==null)||(invoker.fetchAbility(ID())==null)||proficiencyCheck(null,0,false);
		if(pass)
		for(int i=paladinsGroup.size()-1;i>=0;i--)
		{
			try
			{
				MOB mob=(MOB)paladinsGroup.elementAt(i);
				if(CMLib.flags().isEvil(mob))
				{
					int damage=(int)Math.round(CMath.div(mob.envStats().level()+(2*getXLEVELLevel(invoker)),3.0));
					CMLib.combat().postDamage(invoker,mob,this,damage,CMMsg.MASK_ALWAYS|CMMsg.TYP_CAST_SPELL,Weapon.TYPE_BURSTING,"^SThe aura around <S-NAME> <DAMAGES> <T-NAME>!^?");
				}
			}
			catch(java.lang.ArrayIndexOutOfBoundsException e)
			{
			}
		}
		return true;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if(!super.okMessage(myHost,msg))
			return false;
		if((invoker==null)||(!(CMLib.flags().isGood(invoker))))
			return true;
		if(affected==null) return true;
		if(!(affected instanceof MOB)) return true;

		if((msg.target()!=null)
		   &&(paladinsGroup.contains(msg.target()))
		   &&(!paladinsGroup.contains(msg.source()))
		   &&(pass)
		   &&(msg.target() instanceof MOB)
		   &&(msg.source()!=invoker))
		{
			if((CMath.bset(msg.targetCode(),CMMsg.MASK_MALICIOUS))
			&&(msg.targetMinor()==CMMsg.TYP_CAST_SPELL)
			&&(msg.tool()!=null)
			&&(msg.tool() instanceof Ability)
			&&(!CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_HOLY))
			&&(CMath.bset(((Ability)msg.tool()).flags(),Ability.FLAG_UNHOLY)))
			{
				msg.source().location().show((MOB)msg.target(),null,CMMsg.MSG_OK_VISUAL,"The holy field around <S-NAME> protect(s) <S-HIM-HER> from the evil magic attack of "+msg.source().name()+".");
				return false;
			}
			if(((msg.targetMinor()==CMMsg.TYP_POISON)||(msg.targetMinor()==CMMsg.TYP_DISEASE))
			&&(pass))
				return false;
		}
		return true;
	}
}
