package com.planet_ink.coffee_mud.Abilities.Spells;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_FoolsGold extends Spell
{
	boolean destroyOnNextTick=false;
	public Spell_FoolsGold()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Fools Gold";

		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(13);
		
		canAffectCode=Ability.CAN_ITEMS;
		canTargetCode=0;
		

		baseEnvStats().setAbility(0);
		uses=Integer.MAX_VALUE;
		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Spell_FoolsGold();
	}
	public int classificationCode()
	{
		return Ability.SPELL|Ability.DOMAIN_ILLUSION;
	}

	public boolean tick(int tickID)
	{
		if(!destroyOnNextTick) return super.tick(tickID);
		((Item)affected).destroyThis();
		destroyOnNextTick=false;
		return false;
	}
	
	public void affect(Affect affect)
	{
		super.affect(affect);
		if((affected!=null)&&(affected instanceof Item))
		{
			if((affect.amITarget(affected))&&(affect.targetMinor()==Affect.TYP_GET)&&(affect.source()!=invoker))
				destroyOnNextTick=true;
			else
			if((affect.tool()!=null)&&(affect.tool()==affected)&&(affect.targetMinor()==Affect.TYP_GIVE))
				destroyOnNextTick=true;
		}
	}
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if((commands.size()==0)||(Util.s_int(Util.combine(commands,0))==0))
		{
			mob.tell("You must specify how big of a pile of gold to create.");
			return false;
		}
		int amount=Util.s_int(Util.combine(commands,0));
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(0,auto);

		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType,auto?"":"^S<S-NAME> wave(s) <S-HIS-HER> arms around dramatically.^?");
			if(mob.location().okAffect(msg))
			{
				mob.location().send(mob,msg);
				Item gold=(Item)CMClass.getItem("GenItem");
				switch(amount)
				{
				case 1:
					gold.setName("a gold coin"); 
					gold.setDisplayText("a gold coin sits here"); 
					break;
				case 2:
					gold.setName("two gold coins"); 
					gold.setDisplayText("two gold coins sit here"); 
					break;
				default:
					gold.setName("a pile of "+amount+" gold coins"); 
					gold.setDisplayText(gold.name()+" sit here"); 
					break;
				}
				gold.baseEnvStats().setWeight(0);
				gold.recoverEnvStats();
				mob.addInventory(gold);
				mob.location().show(mob,null,Affect.MSG_OK_ACTION,"Suddenly, <S-NAME> hold(s) "+gold.name());
				destroyOnNextTick=false;
				beneficialAffect(mob,gold,0);
			}
		}
		else
			beneficialVisualFizzle(mob,null,"<S-NAME> wave(s) <S-HIS-HER> arms around dramatically, but fizzle(s) the spell.");


		// return whether it worked
		return success;
	}
}