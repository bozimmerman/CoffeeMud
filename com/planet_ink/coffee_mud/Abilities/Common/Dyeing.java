package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Dyeing extends CommonSkill
{
	public String ID() { return "Dyeing"; }
	public String name(){ return "Dyeing";}
	private static final String[] triggerStrings = {"DYE","DYEING"};
	public String[] triggerStrings(){return triggerStrings;}

	private Item found=null;
	private String writing="";
	private static boolean mapped=false;
	public Dyeing()
	{
		super();
		displayText="You are dyeing...";
		verb="dyeing";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}

	private String fixColor(String name, String colorWord)
	{
		int end=name.indexOf("^?");
		if(end>0)
		{
			int start=name.substring(0,end).indexOf("^");
			if((start>=0)&&(start<(end-3)))
				name=name.substring(0,start)
					 +name.substring(end+3);
		}
		colorWord="^"+colorWord.charAt(0)+colorWord+"^?";
		Vector V=Util.parse(name);
		for(int v=0;v<V.size();v++)
		{
			String word=(String)V.elementAt(v);
			if((word.equalsIgnoreCase("of"))
			||(word.equalsIgnoreCase("some"))
			||(word.equalsIgnoreCase("an"))
			||(word.equalsIgnoreCase("a"))
			||(word.equalsIgnoreCase("the"))
			   )
			{
				V.insertElementAt(colorWord,v+1);
				return Util.combine(V,0);
			}
		}
		V.insertElementAt(colorWord,0);
		return Util.combine(V,0);
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping))
			{
				MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonEmote(mob,"<S-NAME> mess(es) up the dyeing.");
				else
				{
					StringBuffer desc=new StringBuffer(found.description());
					for(int x=0;x<(desc.length()-1);x++)
					{
						if((desc.charAt(x)=='^')
						&&(desc.charAt(x+1)!='?'))
							desc.setCharAt(x+1,writing.charAt(0));
					}
					String d=desc.toString();
					if(!d.endsWith("^?")) desc.append("^?");
					if(!d.startsWith("^"+writing.charAt(0))) desc.insert(0,"^"+writing.charAt(0));
					found.setDescription(desc.toString());
					found.setName(fixColor(found.Name(),writing));
					found.setDisplayText(fixColor(found.displayText(),writing));
					found.text();
				}
			}
		}
		super.unInvoke();
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<2)
		{
			commonTell(mob,"You must specify what you want to dye, and color to dye it.");
			return false;
		}
		Item target=mob.fetchCarried(null,(String)commands.firstElement());
		if((target==null)||(!Sense.canBeSeenBy(target,mob)))
		{
			commonTell(mob,"You don't seem to have a '"+((String)commands.firstElement())+"'.");
			return false;
		}
		else
			commands.remove(commands.firstElement());

		if((((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_CLOTH)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PAPER)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_LIQUID)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_VEGETATION)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_LEATHER))
		||(!target.isGeneric()))
		{
			commonTell(mob,"You can't dye that material.");
			return false;
		}
		writing=Util.combine(commands,0).toLowerCase();
		boolean darkFlag=false;
		if(writing.startsWith("dark "))
		{
			darkFlag=true;
			writing=writing.substring(5).trim();
		}
		if(" white green blue red yellow cyan purple ".indexOf(" "+writing.trim()+" ")<0)
		{
			commonTell(mob,"You can't dye anything '"+writing+"'.  Try white, green, blue, red, yellow, cyan, or purple. You can also prefix the colors with the word 'dark'.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		verb="dyeing "+target.name()+" "+writing;
		displayText="You are "+verb;
		found=target;
		if(darkFlag) writing=Util.capitalize(writing);
		if(!profficiencyCheck(mob,0,auto)) writing="";
		int duration=30-mob.envStats().level();
		if((target.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_LEATHER)
			duration*=2;
		if(duration<6) duration=6;
		FullMsg msg=new FullMsg(mob,null,this,CMMsg.MSG_NOISYMOVEMENT,"<S-NAME> start(s) dyeing "+target.name()+".");
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}