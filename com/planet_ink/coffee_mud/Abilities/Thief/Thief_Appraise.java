package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Appraise extends ThiefSkill
{

	public Thief_Appraise()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Appraise";
		displayText="";
		miscText="";

		triggerStrings.addElement("APPRAISE");

		canTargetCode=Ability.CAN_ITEMS;
		canAffectCode=0;
		
		canBeUninvoked=true;
		isAutoinvoked=false;

		baseEnvStats().setLevel(1);

		recoverEnvStats();
	}

	public Environmental newInstance()
	{
		return new Thief_Appraise();
	}

	public int classificationCode()
	{
		return Ability.THIEF_SKILL;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			mob.tell("What would you like to appraise?");
			return false;
		}
		Item target=mob.fetchCarried(null,(String)commands.elementAt(0));
		if((target==null)||((target!=null)&&(!Sense.canBeSeenBy(target,mob))))
		{
			mob.tell("You don't see '"+((String)commands.elementAt(0))+"' here.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-mob.envStats().level();
		if(levelDiff>0) levelDiff=0;
		boolean success=profficiencyCheck(levelDiff,auto);

		FullMsg msg=new FullMsg(mob,target,null,Affect.MSG_DELICATE_HANDS_ACT,"<S-NAME> appraise(s) <T-NAMESELF>.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			int realValue=target.value();
			int materialCode=target.material();
			int weight=target.baseEnvStats().weight();
			int height=target.baseEnvStats().height();
			int allWeight=target.envStats().weight();
			if(!success)
			{
				double deviance=Util.div(Dice.roll(1,100,0)+50,100);
				realValue=(int)Math.round(Util.mul(realValue,deviance));
				materialCode=Dice.roll(1,EnvResource.RESOURCE_DESCS.length,-1);
				weight=(int)Math.round(Util.mul(weight,deviance));
				height=(int)Math.round(Util.mul(height,deviance));
				allWeight=(int)Math.round(Util.mul(allWeight,deviance));
			}
			StringBuffer str=new StringBuffer("");
			str.append(target.name()+" is made of "+EnvResource.RESOURCE_DESCS[materialCode&EnvResource.RESOURCE_MASK]+" is worth about "+realValue+" gold.");
			if(target instanceof Armor)
				str.append("\n\r"+target.name()+" is a size "+height+".");
			if(weight!=allWeight)
				str.append("\n\rIt weighs "+weight+" pounds empty and "+allWeight+" pounds right now.");
			else
				str.append("\n\rIt weighs "+weight+" pounds.");
			mob.tell(str.toString());
		}
		return success;
	}

}