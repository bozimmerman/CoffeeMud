package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.Items.MiscMagic.StdWand;


public class GenStaff extends GenWeapon implements Wand
{
	public String ID(){	return "GenStaff";}
	private String secretWord=StdWand.words[Dice.roll(1,StdWand.words.length,0)-1];

	public GenStaff()
	{
		super();

		name="a wooden staff";
		displayText="a wooden staff lies in the corner of the room.";
		miscText="";
		description="";
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
		recoverEnvStats();
	}
	public Environmental newInstance()
	{
		return new GenStaff();
	}
	public boolean isGeneric(){return true;}

	public boolean useTheWand(Ability A, MOB mob)
	{
		return new StdWand().useTheWand(A,mob);
	}
	public int value()
	{
		if(usesRemaining()<=0)
			return 0;
		else
			return super.value();
	}
	public void setSpell(Ability theSpell)
	{
		readableText="";
		if(theSpell!=null)
			readableText=theSpell.ID();
		secretWord=StdWand.getWandWord(readableText);
	}
	public void setReadableText(String text)
	{
		super.setReadableText(text);
		secretWord=StdWand.getWandWord(readableText);
	}

	public String secretIdentity()
	{
		String id=super.secretIdentity();
		Ability A=getSpell();
		if(A!=null)
			id="'A staff of "+A.name()+"' Charges: "+usesRemaining()+"\n\r"+id;
		return id+"\n\rSay the magic word :`"+secretWord+"` to the target.";
	}

	public Ability getSpell()
	{
		return CMClass.getAbility(readableText());
	}

	public String magicWord()
	{
		return secretWord;
	}
	public void waveIfAble(MOB mob,
						   Environmental afftarget,
						   String message,
						   Wand me)
	{
		new StdWand().waveIfAble(mob,afftarget,message,me);
	}

	public void affect(Environmental myHost, Affect affect)
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
				affect.addTrailerMsg(new FullMsg(affect.source(),this,affect.target(),affect.NO_EFFECT,null,Affect.MASK_GENERAL|Affect.TYP_WAND_USE,affect.targetMessage(),affect.NO_EFFECT,null));
			break;
		default:
			break;
		}
		super.affect(myHost,affect);
	}
	// wand stats handled by genweapon, filled by readableText
}
