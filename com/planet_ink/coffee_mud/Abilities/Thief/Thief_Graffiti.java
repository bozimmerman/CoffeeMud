package com.planet_ink.coffee_mud.Abilities.Thief;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Thief_Graffiti extends ThiefSkill
{
	public String ID() { return "Thief_Graffiti"; }
	public String name(){ return "Graffiti";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	public int quality(){return Ability.INDIFFERENT;}
	private static final String[] triggerStrings = {"GRAFFITI"};
	public String[] triggerStrings(){return triggerStrings;}
	public Environmental newInstance(){	return new Thief_Graffiti();}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		String str=Util.combine(commands,0);
		if(str.length()==0)
		{
			mob.tell("What would you like to write here?");
			return false;
		}
		Room target=mob.location();
		if((auto)&&(givenTarget!=null)&&(givenTarget instanceof Room))
			target=(Room)givenTarget;
		
		if((mob.location().domainType()!=Room.DOMAIN_OUTDOORS_CITY)
		   &&(mob.location().domainType()!=Room.DOMAIN_INDOORS_WOOD)
		   &&(mob.location().domainType()!=Room.DOMAIN_INDOORS_STONE))
		{
			mob.tell("You can't put graffiti here.");
			return false;
		}
		
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		int levelDiff=target.envStats().level()-(mob.envStats().level()+abilityCode());
		if(levelDiff>0) levelDiff=0;
		boolean success=profficiencyCheck(mob,-levelDiff,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,target,this,CMMsg.MSG_DELICATE_SMALL_HANDS_ACT,"<S-NAME> write(s) graffiti here.");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);
				Item I=CMClass.getItem("GenWallpaper");
				I.setName("Graffiti");
				Sense.setReadable(I,true);
				I.setReadableText(str);
				switch(Dice.roll(1,6,0))
				{
				case 1:
					I.setDescription("Someone has scribbed some graffiti here.  Try reading it.");
					break;
				case 2:
					I.setDescription("A cryptic message has been written on the walls.  Try reading it.");
					break;
				case 3:
					I.setDescription("Someone wrote a message here to read.");
					break;
				case 4:
					I.setDescription("A strange message is written here.  Read it.");
					break;
				case 5:
					I.setDescription("This graffiti looks like it is in "+mob.name()+" handwriting.  Read it!");
					break;
				case 6:
					I.setDescription("The wall is covered in graffiti.  You might want to read it.");
					break;
				}
				mob.location().addItem(I);
			}
		}
		else
			beneficialVisualFizzle(mob,target,"<S-NAME> attempt(s) to write graffiti here, but fails.");
		return success;
	}
}