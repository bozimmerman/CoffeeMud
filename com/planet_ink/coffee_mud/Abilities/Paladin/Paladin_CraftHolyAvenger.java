package com.planet_ink.coffee_mud.Abilities.Paladin;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Paladin_CraftHolyAvenger extends com.planet_ink.coffee_mud.Abilities.Common.CommonSkill
{
	public String ID() { return "Paladin_CraftHolyAvenger"; }
	public String name(){ return "Craft Holy Avenger";}
	private static final String[] triggerStrings = {"CRAFTHOLY","CRAFTHOLYAVENGER","CRAFTAVENGER"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Paladin_CraftHolyAvenger();}

	private Item building=null;
	private Item fire=null;
	private boolean messedUp=false;

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.TICK_MOB))
		{
			MOB mob=(MOB)affected;
			if((building==null)
			||(fire==null)
			||(!Sense.isOnFire(fire))
			||(!mob.location().isContent(fire))
			||(mob.isMine(fire)))
			{
				messedUp=true;
				unInvoke();
			}
		}
		return super.tick(ticking,tickID);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
						commonEmote(mob,"<S-NAME> mess(es) up crafting the Holy Avenger.");
					else
						mob.location().addItemRefuse(building,Item.REFUSE_PLAYER_DROP);
				}
				building=null;
			}
		}
		super.unInvoke();
	}


	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if((student.fetchAbility("Specialization_Sword")==null))
		{
			teacher.tell(student.name()+" has not yet specialized in swords.");
			student.tell("You need to specialize in swords to learn "+name()+".");
			return false;
		}
		if(student.fetchAbility("Weaponsmithing")==null)
		{
			teacher.tell(student.name()+" has not yet learned weaponsmithing.");
			student.tell("You need to learn weaponsmithing before you can learn "+name()+".");
			return false;
		}

		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		int completion=16;
		fire=null;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I2=mob.location().fetchItem(i);
			if((I2!=null)&&(I2.container()==null)&&(Sense.isOnFire(I2)))
			{
				fire=I2;
				break;
			}
		}
		if((fire==null)||(!mob.location().isContent(fire)))
		{
			commonTell(mob,"A fire will need to be built first.");
			return false;
		}
		building=null;
		messedUp=false;
		int woodRequired=50;
		Item firstWood=null;
		int foundWood=0;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I instanceof EnvResource)
			&&(!Sense.isOnFire(I))
			&&(I.container()==null))
			{
				if(((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
				   ||((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL))
				{
					if(firstWood==null)firstWood=I;
					if(firstWood.material()==I.material())
						foundWood++;
				}
			}
		}
		if(foundWood==0)
		{
			commonTell(mob,"There is no metal here to make anything from!  It might need to put it down first.");
			return false;
		}
		if(firstWood.material()==EnvResource.RESOURCE_MITHRIL)
			woodRequired=woodRequired/2;
		else
		if(firstWood.material()==EnvResource.RESOURCE_ADAMANTITE)
			woodRequired=woodRequired/3;
		if(woodRequired<1) woodRequired=1;
		if(foundWood<woodRequired)
		{
			commonTell(mob,"You need "+woodRequired+" pounds of "+EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)].toLowerCase()+" to craft the Holy Avenger.  There is not enough here.  Are you sure you set it all on the ground first?");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		destroyResources(mob.location(),woodRequired,firstWood.material(),null,null);
		building=CMClass.getWeapon("GenWeapon");
		completion=50-CMAble.qualifyingClassLevel(mob,this);
		String itemName="the Holy Avenger";
		building.setName(itemName);
		String startStr="<S-NAME> start(s) crafting "+building.name()+".";
		displayText="You are crafting "+building.name();
		verb="crafting "+building.name();
		int hardness=EnvResource.RESOURCE_DATA[firstWood.material()&EnvResource.RESOURCE_MASK][3]-5;
		building.setDisplayText(itemName+" is here");
		building.setDescription(itemName+". ");
		building.baseEnvStats().setWeight(woodRequired);
		building.setBaseValue(0);
		building.setMaterial(firstWood.material());
		building.baseEnvStats().setLevel(mob.envStats().level());
		building.baseEnvStats().setAbility(5);

		int highestAttack=(CMAble.qualifyingClassLevel(mob,this)/2);
		int highestDamage=CMAble.qualifyingClassLevel(mob,this);
		Weapon w=(Weapon)building;
		w.setWeaponClassification(Weapon.CLASS_SWORD);
		w.setWeaponType(Weapon.TYPE_SLASHING);
		w.setRanges(w.minRange(),1);
		building.setRawLogicalAnd(true);
		building.baseEnvStats().setAttackAdjustment(highestAttack+(hardness*5));
		building.baseEnvStats().setDamage(highestDamage+(hardness*2));
		Ability A=CMClass.getAbility("Prop_HaveZapper");
		A.setMiscText("-CLASS +Paladin -ALIGNMENT +Good");
		building.addNonUninvokableEffect(A);

		building.recoverEnvStats();
		building.text();
		building.recoverEnvStats();

		messedUp=!profficiencyCheck(0,auto);
		if(completion<6) completion=6;
		FullMsg msg=new FullMsg(mob,null,CMMsg.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
