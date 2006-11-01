package com.planet_ink.coffee_mud.Abilities.Specializations;
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
public class Specialization_Weapon extends StdAbility
{
	public String ID() { return "Specialization_Weapon"; }
	public String name(){ return "Weapon Specialization";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return 0;}
	public int abstractQuality(){return Ability.QUALITY_BENEFICIAL_SELF;}
	public boolean isAutoInvoked(){return true;}
	public boolean canBeUninvoked(){return false;}

	protected boolean activated=false;
	protected int weaponType=-1;
	protected int secondWeaponType=-1;
	protected String[] EMPTYSTR=new String[0];
	protected int[] EMPTYINT=new int[0];
	protected String[] EXPERTISES(){return EMPTYSTR;}
	protected String[] EXPERTISES_NAMES(){return EMPTYSTR;}
	protected String[] EXPERTISE_STATS(){return EMPTYSTR;}
	protected int[] EXPERTISE_LEVELS(){return EMPTYINT;}
	protected int[] EXPERTISE_DAMAGE_TYPE(){return EMPTYINT;}

	public int classificationCode(){return Ability.ACODE_SKILL;}
	
    private static final int EXPERTISE_STAGES=10;
    public void initializeClass()
    {
        super.initializeClass();
        if(EXPERTISES().length==0) return;
        
        if(CMLib.expertises().getDefinition(EXPERTISES()[0]+EXPERTISE_STAGES)==null)
        	for(int e=0;e<EXPERTISES().length;e++)
        	{
	            for(int i=1;i<=EXPERTISE_STAGES;i++)
	                CMLib.expertises().addDefinition(EXPERTISES()[e]+i,EXPERTISES_NAMES()[e]+" "+CMath.convertToRoman(i),
	                        "","+"+EXPERTISE_STATS()[e]+" "+(15+i)+" -LEVEL +>="+(EXPERTISE_LEVELS()[e]+(5*i)),0,1,0,0,0);
        	}
        registerExpertiseUsage(EXPERTISES(),EXPERTISE_STAGES,false,null);
    }
    protected int getXLevel(MOB mob){ return EXPERTISES().length==0?0:getExpertiseLevel(mob,EXPERTISES()[0]);}

	public void executeMsg(Environmental myHost, CMMsg msg)
	{
		if((activated)
		&&(msg.source()==affected)
		&&(msg.tool() instanceof Weapon)
		&&(msg.target() instanceof MOB)
		&&((((Weapon)msg.tool()).weaponClassification()==weaponType)
 		 ||(weaponType<0)
		 ||(((Weapon)msg.tool()).weaponClassification()==secondWeaponType)))
		{
			if((msg.targetMinor()==CMMsg.TYP_WEAPONATTACK)&&(CMLib.dice().rollPercentage()<25))
				helpProficiency((MOB)affected);
			else
			if((msg.targetMinor()==CMMsg.TYP_DAMAGE)
			&&(msg.tool() instanceof Weapon)
			&&(!((Weapon)msg.tool()).amWearingAt(Item.IN_INVENTORY))
			&&(msg.value()>0)
			&&(EXPERTISE_DAMAGE_TYPE().length>0))
			{
				for(int i=1;i<EXPERTISE_DAMAGE_TYPE().length;i++)
					if((EXPERTISE_DAMAGE_TYPE()[i]==((Weapon)msg.tool()).weaponType()))
					{
						msg.setValue(msg.value()+(2*super.getExpertiseLevel(msg.source(),EXPERTISES()[i])));
						break;
					}
			}
		}
	}


	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		activated=false;
		if(affected instanceof MOB)
		{
			Item myWeapon=((MOB)affected).fetchWieldedItem();
			if((myWeapon!=null)
			&&(myWeapon instanceof Weapon)
			&&((((Weapon)myWeapon).weaponClassification()==weaponType)
 			 ||(weaponType<0)
			 ||(((Weapon)myWeapon).weaponClassification()==secondWeaponType)))
			{
				activated=true;
				int plusMore=0;
				if(EXPERTISES().length>0)
					plusMore=super.getExpertiseLevel((MOB)affected,EXPERTISES()[0])*2;
				affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()
						+(int)Math.round(15.0*(CMath.div(proficiency(),100.0)))
						+plusMore);
					
			}
		}
	}
}
