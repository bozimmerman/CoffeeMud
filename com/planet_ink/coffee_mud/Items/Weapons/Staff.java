package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.StdWand;

public class Staff extends StdWeapon implements Wand
{
	public String ID(){	return "Staff";}
	private String secretWord=StdWand.words[Dice.roll(1,StdWand.words.length,0)-1];

	public Staff()
	{
		super();

		name="a wooden staff";
		displayText="a wooden staff lies in the corner of the room.";
		miscText="";
		description="It`s long and wooden, just like a staff ought to be.";
		secretIdentity="";
		baseEnvStats().setAbility(0);
		baseEnvStats().setLevel(0);
		baseEnvStats.setWeight(4);
		baseEnvStats().setAttackAdjustment(0);
		baseEnvStats().setDamage(4);
		baseGoldValue=1;
		recoverEnvStats();
		wornLogicalAnd=true;
		material=EnvResource.RESOURCE_OAK;
		properWornBitmap=Item.HELD|Item.WIELD;
		weaponType=TYPE_BASHING;
		weaponClassification=Weapon.CLASS_STAFF;
	}

	public String magicWord()
	{
		return secretWord;
	}
	public Environmental newInstance()
	{
		return new Staff();
	}

	public boolean useTheWand(Ability A, MOB mob)
	{
		return new StdWand().useTheWand(A,mob);
	}
	public void setSpell(Ability theSpell)
	{
		miscText="";
		if(theSpell!=null)
			miscText=theSpell.ID();
		secretWord=StdWand.getWandWord(miscText);
	}
	public void setMiscText(String newText)
	{
		super.setMiscText(newText);
		secretWord=StdWand.getWandWord(newText);
	}

	public Ability getSpell()
	{
		return CMClass.getAbility(text());
	}
	public int value()
	{
		if(usesRemaining()<=0) 
			return 0;
		else 
			return super.value();
	}
	public String secretIdentity()
	{
		String id=super.secretIdentity();
		Ability A=getSpell();
		if(A!=null)
			id="'A staff of "+A.name()+"' Charges: "+usesRemaining()+"\n\r"+id;
		return id+"\n\rSay the magic word :`"+secretWord+"` to the target.";
	}

	public void waveIfAble(MOB mob,
						   Environmental afftarget,
						   String message,
						   Wand me)
	{
		new StdWand().waveIfAble(mob,afftarget,message,me);
	}

	public void affect(Affect affect)
	{
		MOB mob=affect.source();
		switch(affect.targetMinor())
		{
		case Affect.TYP_WAND_USE:
			if(affect.amITarget(this))
				waveIfAble(mob,affect.tool(),affect.targetMessage(),this);
			break;
		case Affect.TYP_SPEAK:
			if(affect.sourceMinor()==Affect.TYP_SPEAK)
				affect.addTrailerMsg(new FullMsg(affect.source(),this,affect.target(),affect.NO_EFFECT,null,Affect.ACT_GENERAL|Affect.TYP_WAND_USE,affect.targetMessage(),affect.NO_EFFECT,null));
			break;
		default:
			break;
		}
		super.affect(affect);
	}
}
