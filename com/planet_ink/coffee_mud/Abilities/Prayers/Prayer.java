package com.planet_ink.coffee_mud.Abilities.Prayers;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prayer extends StdAbility
{
	public String ID() { return "Prayer"; }
	public String name(){ return "a Prayer";}
	public String displayText(){ return "";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){ return INDIFFERENT;}
	public int holyQuality(){ return HOLY_NEUTRAL;}
	private static final String[] triggerStrings = {"PRAY","PR"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.PRAYER;}
	
	public final static int HOLY_EVIL=0;
	public final static int HOLY_NEUTRAL=1;
	public final static int HOLY_GOOD=2;

	protected int affectType(boolean auto){
		int affectType=Affect.MSG_CAST_VERBAL_SPELL;
		if(quality()==Ability.MALICIOUS)
			affectType=Affect.MSG_CAST_ATTACK_VERBAL_SPELL;
		if(auto) affectType=affectType|Affect.MASK_GENERAL;
		return affectType;
	}
	public Environmental newInstance(){	return new Prayer();}

	public boolean appropriateToMyAlignment(int alignment)
	{
		switch(holyQuality())
		{
		case Prayer.HOLY_EVIL:
			if(alignment<350) return true;
			break;
		case Prayer.HOLY_GOOD:
			if(alignment>650) return true;
			break;
		case Prayer.HOLY_NEUTRAL:
			if((alignment>350)&&(alignment<650)) return true;
			break;
		}
		return false;
	}
	public void helpProfficiency(MOB mob)
	{

		Ability A=(Ability)mob.fetchAbility(this.ID());
		if(A==null) return;
		if(A.appropriateToMyAlignment(mob.getAlignment()))
		{
			super.helpProfficiency(mob);
			return;
		}
		return;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		if(auto) return true;
		
		int align=mob.getAlignment();
		
		if(appropriateToMyAlignment(align))	return true;
		
		int basis=0;
		if(holyQuality()==Prayer.HOLY_EVIL)
			basis=align/10;
		else
		if(holyQuality()==Prayer.HOLY_GOOD)
			basis=(1000-align)/10;
		else
		{
			basis=(500-align)/10;
			if(basis<0) basis=basis*-1;
			basis-=10;
		}
		
		if(Dice.rollPercentage()>basis)
			return true;

		if(holyQuality()==Prayer.HOLY_EVIL)
			mob.tell("The evil nature of "+name()+" disrupts your prayer.");
		else
		if(holyQuality()==Prayer.HOLY_GOOD)
			mob.tell("The goodness of "+name()+" disrupts your prayer.");
		else
		if(mob.getAlignment()>650)
			mob.tell("The anti-good nature of "+name()+" disrupts your thought.");
		else
		if(mob.getAlignment()<350)
			mob.tell("The anti-evil nature of "+name()+" disrupts your thought.");
					 
		return false;
	}
}
