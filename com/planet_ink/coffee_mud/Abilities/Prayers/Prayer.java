package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer extends StdAbility
{
	public final static int HOLY_EVIL=0;
	public final static int HOLY_NEUTRAL=1;
	public final static int HOLY_GOOD=2;

	protected int holyQuality=HOLY_NEUTRAL;

	protected int affectType=Affect.MSG_CAST_VERBAL_SPELL;

	public Prayer()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="a Prayer";
		displayText="(in the holy dominion of the gods)";
		miscText="";
		triggerStrings.addElement("PRAY");
		triggerStrings.addElement("PR");

		holyQuality=Prayer.HOLY_NEUTRAL;
		quality=Ability.INDIFFERENT;
		canBeUninvoked=true;
		isAutoinvoked=false;
		minRange=0;
		maxRange=0;
	}

	public int classificationCode()
	{
		return Ability.PRAYER;
	}

	public Environmental newInstance()
	{
		return new Prayer();
	}

	public int holyQuality()
	{
		return holyQuality;
	}

	public boolean appropriateToMyAlignment(MOB mob)
	{
		if(holyQuality==Prayer.HOLY_NEUTRAL) return true;
		if((holyQuality==Prayer.HOLY_EVIL)&&(mob.getAlignment()>650))
			return false;
		else
		if((holyQuality==Prayer.HOLY_GOOD)&&(mob.getAlignment()<350))
			return false;
		return true;
	}
	public void helpProfficiency(MOB mob)
	{

		Ability A=(Ability)mob.fetchAbility(this.ID());
		if(A==null) return;
		if(A.appropriateToMyAlignment(mob))
		{
			super.helpProfficiency(mob);
			return;
		}
		return;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		affectType=Affect.MSG_CAST_VERBAL_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=Affect.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto) affectType=affectType|Affect.ACT_GENERAL;

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if((!auto)&&(holyQuality!=Prayer.HOLY_NEUTRAL))
		{
			if(holyQuality==Prayer.HOLY_EVIL)
				mob.setAlignment(mob.getAlignment()-(envStats().level()*10));
			else
			if(holyQuality==Prayer.HOLY_GOOD)
				mob.setAlignment(mob.getAlignment()+(envStats().level()*10));
			if(mob.getAlignment()>1000)
				mob.setAlignment(1000);
			if(mob.getAlignment()<0)
				mob.setAlignment(0);
		}
		if((appropriateToMyAlignment(mob))||(auto))
			return true;
		if(Dice.rollPercentage()<25)
			return true;
		if(this.holyQuality()==Prayer.HOLY_EVIL)
			mob.tell("The evil nature of "+name()+" disrupts your prayer.");
		else
			mob.tell("The goodness of "+name()+" disrupts your prayer.");
		return false;
	}
}
