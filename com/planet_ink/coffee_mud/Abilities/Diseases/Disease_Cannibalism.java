package com.planet_ink.coffee_mud.Abilities.Diseases;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.Items.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Disease_Cannibalism extends Disease
{
	public String ID() { return "Disease_Cannibalism"; }
	public String name(){ return "Cannibalism";}
	public String displayText(){ return "(Cannibalism)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public int quality(){return Ability.MALICIOUS;}
	public boolean putInCommandlist(){return false;}
	public Environmental newInstance(){	return new Disease_Cannibalism();}

	protected int DISEASE_TICKS(){return 999999;}
	protected int DISEASE_DELAY(){return 100;}
	protected String DISEASE_DONE()
   {
      String desiredMeat = "";
      if(affected instanceof MOB)
      {
         MOB mob = (MOB) affected;
         desiredMeat = mob.charStats().raceName();
      }
      else
      {
         desiredMeat = "your race's";
      }
      return "<S-NAME> no longer hunger for "+ desiredMeat +" meat.";
   }
	protected String DISEASE_START()
   {
      String desiredMeat = "";
      if(affected instanceof MOB)
      {
         MOB mob = (MOB) affected;
         desiredMeat = mob.charStats().raceName();
      }
      else
      {
         desiredMeat = "your race's";
      }
      return "^G<S-NAME> hunger(s) for "+ desiredMeat +" meat.^?";
   }
	protected String DISEASE_AFFECT(){return "";}
	public int abilityCode(){return DiseaseAffect.SPREAD_CONSUMPTION;}
	
	public void unInvoke()
	{
		if(affected==null)
			return;
		if(affected instanceof MOB)
		{
			MOB mob=(MOB)affected;

			super.unInvoke();
			if(canBeUninvoked())
				mob.tell(mob,null,this,DISEASE_DONE());
		}
		else
			super.unInvoke();
	}
   
	public boolean okAffect(Environmental myHost, Affect msg)
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
         MOB source=(MOB)msg.source();
         if(source == null)
            return false;
			MOB mob=(MOB)affected;
         if(msg.targetMinor() == Affect.TYP_EAT)
         {
            Environmental food = msg.target();
            if((food!=null)
			&&(food.name().toLowerCase().indexOf(mob.charStats().raceName()) < 0))
            {
				FullMsg newMessage=new FullMsg(mob,null,this,Affect.MSG_OK_VISUAL,"^S<S-NAME> attempt(s) to eat "+ food.Name() +", but can't stomach it....^?");
				if(mob.location().okAffect(mob,newMessage))
					mob.location().send(mob,newMessage);
				return false;
            }
         }
      }
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if(msg.amITarget(mob)
			&&(msg.tool()!=null)
			&&(msg.tool().ID().equals("Spell_Hungerless")))
			{
				mob.tell("You don't feel any less hungry.");
				return false;
			}
		}

      return super.okAffect(myHost,msg);
	}
}