package com.planet_ink.coffee_mud.Items.Weapons;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.Dice;
import com.planet_ink.coffee_mud.Items.MiscMagic.StdWand;

public class Staff extends StdWeapon implements Wand
{
	private String secretWord=StdWand.words[Dice.roll(1,StdWand.words.length,0)-1];

	public Staff()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
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
		material=Item.WOODEN;
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
	}

	public Ability getSpell()
	{
		return CMClass.getAbility(text());
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
		case Affect.TYP_SPEAK:
			if(affect.amITarget(this))
			{
				if(affect.sourceCode()==Affect.NO_EFFECT)
					waveIfAble(mob,affect.tool(),affect.targetMessage(),this);
			}
			else
			if(affect.sourceMinor()==Affect.TYP_SPEAK)
				affect.addTrailerMsg(new FullMsg(affect.source(),this,affect.target(),affect.NO_EFFECT,null,affect.targetCode(),affect.targetMessage(),affect.NO_EFFECT,null));
			break;
		default:
			break;
		}
		super.affect(affect);
	}
}
