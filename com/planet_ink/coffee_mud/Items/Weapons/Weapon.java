package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.commands.*;
import com.planet_ink.coffee_mud.StdAffects.*;
import com.planet_ink.coffee_mud.Items.*;

public class Weapon extends StdItem
{
	// weapon types
	public final static int TYPE_NATURAL=0;
	public final static int TYPE_SLASHING=1;
	public final static int TYPE_PIERCING=2;
	public final static int TYPE_BASHING=3;
	public final static int TYPE_BURNING=4;
	public final static int TYPE_BURSTING=5;

	// weapon classifications
	public final static int CLASS_AXE=0;
	public final static int CLASS_BLUNT=1;
	public final static int CLASS_EDGED=2;
	public final static int CLASS_FLAILED=3;
	public final static int CLASS_HAMMER=4;
	public final static int CLASS_NATURAL=5;
	public final static int CLASS_POLEARM=6;
	public final static int CLASS_RANGED=7;
	public final static int CLASS_SWORD=8;

	public int weaponType=TYPE_NATURAL;
	public int weaponClassification=CLASS_NATURAL;

	public Weapon()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="weapon";
		displayText=" sits here.";
		description="This is a deadly looking weapon.";
		wornLogicalAnd=false;
		properWornBitmap=Item.HELD|Item.WIELD;
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(1);
		baseEnvStats().setAbility(0);
		baseGoldValue=15;
		material=Item.METAL;
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new Weapon();
	}

	public String secretIdentity()
	{
		if(envStats().ability()>0)
			return name()+" +"+envStats().ability();
		else
			return super.secretIdentity();
	}

	public void affectEnvStats(Environmental affected, Stats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(amWearingAt(Item.WIELD))
		{
			affectableStats.setAttackAdjustment(affectableStats.attackAdjustment()+(envStats().attackAdjustment()+(envStats().ability()*5)));
			affectableStats.setDamage(affectableStats.damage()+(envStats().damage()*(envStats().ability()+1)));
		}
	}

	public static String typeDescription(int weaponType)
	{
		switch(weaponType)
		{
		case TYPE_NATURAL:
			return "NATURAL";
		case TYPE_SLASHING:
			return "SLASHING";
		case TYPE_PIERCING:
			return "PIERCING";
		case TYPE_BASHING:
			return "BASHING";
		}
		return "";
	}

	public static String classifictionDescription(int weaponType)
	{
		switch(weaponType)
		{
		case CLASS_AXE:
			return "AXE";
		case CLASS_BLUNT:
			return "BLUNT";
		case CLASS_EDGED:
			return "EDGED";
		case CLASS_FLAILED:
			return "FLAILED";
		case CLASS_HAMMER:
			return "HAMMER";
		case CLASS_NATURAL:
			return "KARATE";
		case CLASS_POLEARM:
			return "POLEARM";
		case CLASS_RANGED:
			return "RANGED";
		case CLASS_SWORD:
			return "SWORD";
		}
		return "";
	}
	public void strike(MOB source, MOB target, boolean success)
	{
		if(success)
		{
            // calculate Base Damage (with Strength bonus)
            int damageAmount = Dice.roll(2, source.envStats().damage(), source.charStats().getStrength() / 4);

            // modify damage if target can not be seen
            if(!Sense.canBeSeenBy(target,source))
                damageAmount /= 2;

            // modify damage if target is sitting
            if(Sense.isSitting(target))
                damageAmount *=2;

            // modify damage if target is asleep
            if(Sense.isSleeping(target))
                damageAmount *=5;

            // modify damage if source is hungry
			if(source.curState().getHunger() < 1)
            {
                damageAmount *= 9;
                damageAmount /= 10;
            }

            //modify damage if source is thirtsy
			if(source.curState().getThirst() < 1)
            {
                damageAmount *= 9;
                damageAmount /= 10;
            }

			String youSee=null;
			FullMsg msg=new FullMsg(source,
									target,
									this,
									Affect.VISUAL_WNOISE,
									Affect.VISUAL_WNOISE,
									Affect.VISUAL_WNOISE,
									TheFight.hitString(weaponType,damageAmount,name()));
			source.location().send(source,msg);
			TheFight.doDamage(target,damageAmount);
		}
		else
		{
			FullMsg msg=new FullMsg(source,
									target,
									this,
									Affect.VISUAL_WNOISE,
									Affect.VISUAL_WNOISE,
									Affect.VISUAL_WNOISE,
									TheFight.missString(weaponType,name()));
			source.location().send(source,msg);
		}
	}
}
