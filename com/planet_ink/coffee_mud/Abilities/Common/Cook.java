package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Cook extends CommonSkill
{
	private Item cooking=null;
	private int recipe=-1;
	private boolean burnt=false;
	public Cook()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		name="Cooking";

		displayText="You are cooking...";
		verb="cooking";
		miscText="";
		triggerStrings.addElement("COOK");
		quality=Ability.INDIFFERENT;

		recoverEnvStats();
		CMAble.addCharAbilityMapping("All",1,ID(),false);
	}

	public Environmental newInstance()
	{
		return new Cook();
	}
	public boolean tick(int tickID)
	{
		MOB mob=(MOB)affected;
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
		}
		return super.tick(tickID);
	}

	private static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("COOKING RECIPES");
		if(V==null)
		{
			V=new Vector();
			StringBuffer str=Resources.getFile("recipes.txt");
			if(str!=null)
			{
				Vector V2=new Vector();
				boolean oneComma=false;
				int start=0;
				for(int i=0;i<str.length();i++)
				{
					if(str.charAt(i)==',')
					{
						V2.addElement(str.substring(start,i));
						start=i+1;
						oneComma=true;
					}
					else
					if((str.charAt(i)=='\n')||(str.charAt(i)=='\r'))
					{
						if(oneComma)
						{
							V2.addElement(str.substring(start,i));
							V.addElement(V2);
							V2=new Vector();
						}
						start=i+1;
						oneComma=false;
					}
				}
				if(V2.size()>1)
				{
					if(oneComma)
						V2.addElement(str.substring(start,str.length()));
					V.addElement(V2);
				}
			}
			else
				Log.errOut("Cook","Recipes not found!");
			Resources.submitResource("COOKING RECIPES",V);
		}
		return V;
	}
	
	public void unInvoke()
	{
		if((affected!=null)&&(affected instanceof MOB))
		{
			MOB mob=(MOB)affected;
			if((cooking!=null)&&(!aborted)&&(recipe>=0))
			{
			}
		}
		super.unInvoke();
	}
	
	
	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		verb="cooking";
		cooking=null;
		Item I=getTarget(mob,null,givenTarget,commands);
		if(I==null) return false;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		if(!(I instanceof Container))
		{
			mob.tell("There's nothing in "+I.name()+" to cook!");
			return false;
		}
		Container C=(Container)I;
		switch(I.material()&EnvResource.MATERIAL_MASK)
		{
		case EnvResource.MATERIAL_GLASS:
		case EnvResource.MATERIAL_METAL:
		case EnvResource.MATERIAL_MITHRIL:
		case EnvResource.MATERIAL_ROCK:
			break;
		default:
			mob.tell(I.name()+" is not suitable to cook in.");
			return false;
		}
		
		burnt=!profficiencyCheck(0,auto);
		int duration=40-mob.envStats().level();
		if(duration<15) duration=15;
		FullMsg msg=new FullMsg(mob,I,null,Affect.MSG_NOISYMOVEMENT,Affect.MSG_OK_ACTION,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) cooking something in a <T-NAME>.");
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}
