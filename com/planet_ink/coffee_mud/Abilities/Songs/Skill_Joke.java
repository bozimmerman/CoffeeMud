package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.*;

public class Skill_Joke extends BardSkill
{
	public String ID() { return "Skill_Joke"; }
	public String name(){ return "Joke";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"JOKE"};
	public String[] triggerStrings(){return triggerStrings;}
	public int classificationCode(){return Ability.SKILL;}
	public Environmental newInstance(){	return new Skill_Joke();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// if they can't hear the sleep spell, it
		// won't happen
		if((!auto)&&(!Sense.canBeHeardBy(mob,target)))
		{
			mob.tell(target.charStats().HeShe()+" can't hear your words.");
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		String str=null;
		if(success)
		{
			if(auto) str="<T-NAME> remember(s) a joke!";
			else
			{
				Vector insultd=Resources.getFileLineVector(Resources.getFileResource("skills"+File.separatorChar+"insultd.txt"));
				Vector insulto=Resources.getFileLineVector(Resources.getFileResource("skills"+File.separatorChar+"insulto.txt"));
				String[] ob=new String[5];
				String[] de=new String[5];
				for (int cnt=1; cnt<4; cnt++)
				{
					ob[cnt] = (String)insulto.elementAt(Dice.roll(1,insulto.size(),-1));
					de[cnt] = (String)insultd.elementAt(Dice.roll(1,insultd.size(),-1));
				}
				String joke=null;
				switch(Dice.roll(1,7,0))
				{
				case 1:
					joke=  "Q: What do you get if you cross a "+ob[1]+" with a "+ob[2]+"?\n\r"
						  +"A: "+de[1]+" "+ob[3]+"!";
					break;
				case 2:
					joke=  "What did the "+ob[1]+" say to the "+ob[2]+"?\n\r"
					      +"'You are "+de[1]+" "+ob[3]+"!'";
					break;
				case 3:
					joke= "Person 1: 'Knock, knock!'\n\r"
					     +"Person 2: 'Who's there?'\n\r"
					     +"Person 1: 'A "+ob[1]+".'\n\r"
					     +"Person 2: 'A "+ob[1]+" who?'\n\r"
					     +"Person 1: '"+de[1]+" "+ob[2]+"!'";
					break;
				case 4:
					joke= "Q: What's the difference between a "+ob[1]+" and a "+ob[2]+"?\n\r"
					     +"A: A "+ob[1]+" is "+de[1]+" "+ob[3]+"!";
					break;
				case 5:
					joke= "Q: What did the big "+ob[1]+" say to the little "+ob[1]+"?\n\r"
					     +"A: 'You are "+de[1]+" "+ob[2]+"!'";
					break;
				case 6:
					joke= "Q: What do you call "+de[1]+" "+ob[1]+" without "+de[2]+" "+ob[2]+"?\n\r"
					     +"A: "+Util.capitalize(de[3])+" "+ob[3]+"!";
					break;
				case 7:
					joke= "Q: When is "+de[1]+" "+ob[1]+" not "+de[1]+" "+ob[1]+"?\n\r"
					     +"A: When it's "+de[2]+" "+ob[2]+"!'";
					break;
				}
				str="<S-NAME> joke(s) to <T-NAMESELF>:\n\r"+joke;
			}
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_SPEAK|(auto?CMMsg.MASK_GENERAL:0),str);
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				if(Dice.rollPercentage()<25)
				{
					Ability A=CMClass.getAbility("Spell_Laughter");
					A.invoke(mob,target,true);
				}
				else
				{
					Ability A=CMClass.getAbility("Disease_Giggles");
					A.invoke(mob,target,true);
				}

			}
		}
		else
			return beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to think up a joke for <T-NAMESELF>, but fail(s).");

		return success;
	}

}
