package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Lacquerring extends CommonSkill
{
	public String ID() { return "Lacquerring"; }
	public String name(){ return "Lacquerring";}
	private static final String[] triggerStrings = {"LACQUERRING","LACQUER"};
	public String[] triggerStrings(){return triggerStrings;}

	private Item found=null;
	private String writing="";
	private static boolean mapped=false;
	public Lacquerring()
	{
		super();
		displayText="You are lacquerring...";
		verb="lacquerring";
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Lacquerring();}

	private String fixColor(String name, String colorWord)
	{
		int end=name.indexOf("^?");
		if(end>0)
		{
			int start=name.substring(0,end).indexOf("^");
			if((start>=0)&&(start<(end-3))) name=name.substring(0,start)+name.substring(end+3);
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
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted))
			{
				MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonEmote(mob,"<S-NAME> mess(es) up the lacquerring.");
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
			commonTell(mob,"You must specify what you want to lacquer, and the color to lacquer it in.");
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

		if((((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_GLASS)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_METAL)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_MITHRIL)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_PRECIOUS)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_ROCK)
			&&((target.material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_WOODEN))
		||(!target.isGeneric()))
		{
			commonTell(mob,"You can't lacquer that material.");
			return false;
		}

		writing=Util.combine(commands,0).toLowerCase();
		if(" white green blue red yellow cyan purple ".indexOf(" "+writing.trim()+" ")<0)
		{
			commonTell(mob,"You can't lacquer anything '"+writing+"'.  Try white, green, blue, red, yellow, cyan, or purple.");
			return false;
		}
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;
		verb="lacquerring "+target.name()+" "+writing;
		displayText="You are "+verb;
		found=target;
		if(!profficiencyCheck(0,auto)) writing="";
		int duration=60-mob.envStats().level();
		if(duration<12) duration=12;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,"<S-NAME> start(s) lacquerring "+target.name());
		if(mob.location().okAffect(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,duration);
		}
		return true;
	}
}