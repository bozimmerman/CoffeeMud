package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Druid extends StdCharClass
{
	private static boolean abilitiesLoaded=false;
	private static long wearMask=Item.ON_TORSO|Item.ON_LEGS|Item.ON_ARMS|Item.ON_WAIST|Item.ON_HEAD;
	
	public Druid()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=25;
		maxStat[CharStats.CONSTITUTION]=25;
		bonusPracLevel=2;
		manaMultiplier=15;
		attackAttribute=CharStats.CONSTITUTION;
		bonusAttackLevel=1;
		damageBonusPerLevel=0;
		name=myID;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",0,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",50,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Swim",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Climb",100,true);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			
			CMAble.addCharAbilityMapping(ID(),1,"Druid_ShapeShift",true);
		}
	}

	public boolean playerSelectable()
	{
		return false;
	}

	public String statQualifications(){return "Constitution 9+";}
	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStat(CharStats.CONSTITUTION)<=8)
			return false;
		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&& !(mob.charStats().getMyRace().ID().equals("Elf"))
		&& !(mob.charStats().getMyRace().ID().equals("HalfElf")))
			return(false);
		return true;
	}

	public String weaponLimitations(){return "To avoid fumbling, must be Natural, Wooden, or Vegetation-based weapons.";}
	public String armorLimitations(){return "Must wear cloth, paper, or vegetation armor to avoid chant failure.";}
	public String otherLimitations(){return "Must remain Neutral to avoid skill and chant failure chances.";}

	public boolean okAffect(MOB myChar, Affect affect)
	{
		if(!super.okAffect(myChar, affect))
			return false;

		if(affect.amISource(myChar)&&(!myChar.isMonster()))
		{
			if(affect.sourceMinor()==Affect.TYP_CAST_SPELL)
			{
				for(int i=0;i<myChar.inventorySize();i++)
				{
					Item I=myChar.fetchInventory(i);
					if(I==null) break;
					if((((I.rawWornCode()&wearMask)>0)&&(I instanceof Armor))
					 ||(I.amWearingAt(Item.HELD)&&(I instanceof Shield)))
					{
						switch(I.material()&EnvResource.MATERIAL_MASK)
						{
						case EnvResource.MATERIAL_CLOTH:
						case EnvResource.MATERIAL_VEGETATION:
						case EnvResource.MATERIAL_PAPER:
							break;
						default:
							if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.INTELLIGENCE)*2)
							{
								myChar.location().show(myChar,null,Affect.MSG_OK_VISUAL,"<S-NAME> watch(es) <S-HIS-HER> armor absorb <S-HIS-HER> magical energy!");
								return false;
							}
							break;
						}
					}
				}
			}
			else
			if((affect.sourceMinor()==Affect.TYP_WEAPONATTACK)
			&&(affect.tool()!=null)
			&&(affect.tool() instanceof Weapon)
			&&((((Weapon)affect.tool()).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN)
			&&((((Weapon)affect.tool()).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_VEGETATION))
			{
				if(Dice.rollPercentage()>myChar.charStats().getStat(CharStats.CONSTITUTION)*2)
				{
					myChar.location().show(myChar,null,Affect.MSG_OK_ACTION,"<S-NAME> fumble(s) horribly with "+affect.tool().name()+".");
					return false;
				}
			}
		}
		return true;
	}

	public void outfit(MOB mob)
	{
	}
}
