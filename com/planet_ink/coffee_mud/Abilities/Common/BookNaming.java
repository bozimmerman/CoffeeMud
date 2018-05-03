package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
   Copyright 2017-2018 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

	   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class BookNaming extends CommonSkill
{
	@Override
	public String ID()
	{
		return "BookNaming";
	}

	private final static String	localizedName	= CMLib.lang().L("Book Naming");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "BOOKNAMING", "BOOKNAME", "BNAME" });

	@Override
	public String[] triggerStrings()
	{
		return triggerStrings;
	}

	@Override
	public int classificationCode()
	{
		return Ability.ACODE_COMMON_SKILL | Ability.DOMAIN_CALLIGRAPHY;
	}

	protected Item		found	= null;
	protected String	writing	= "";
	
	protected static String[] PREFIXES = new String[] {
		"The Book of @x1",
		"The Tales of @x1",
		"The Tale of @x1",
		"The Chronicles of @x1",
		"The Story of @x1",
		"The Tome of @x1",
		"The Booklet of @x1",
		"@x1: A Novel",
		"The `@x1` Book",
		"The `@x1` Journal",
		"The `@x1` Magazine",
		"`@x1`: A Book by @x2",
		"`@x1`: A Novel by @x2",
		"`@x1`: A Tome by @x2",
		"`@x1`: A Story by @x2",
		"`@x1`: A Volume by @x2",
		"`@x1`: A Treatise by @x2",
		"None"
	};
	protected static Pattern[] PATTERNS = new Pattern[0]; 
	protected static String prefixList = null;
	
	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public BookNaming()
	{
		super();
		displayText=L("You are naming a book...");
		verb=L("naming");
	}

	public static boolean isAlreadyNamed(final String name)
	{
		if(PATTERNS.length==0)
		{
			ArrayList<Pattern> Ps=new ArrayList<Pattern>();
			for(String prefix : PREFIXES)
			{
				prefix = CMStrings.replaceAll(prefix, "@x1",".+");
				prefix = CMStrings.replaceAll(prefix, "@x2",".+");
				Pattern P=Pattern.compile(prefix);
				Ps.add(P);
			}
			PATTERNS=Ps.toArray(PATTERNS);
		}
		CharSequence S = name.subSequence(0, name.length());
		for(Pattern P : PATTERNS)
		{
			if(P.matcher(S).matches())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)
			&&(affected instanceof MOB)
			&&(!aborted)
			&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonTell(mob,L("You mess up your book naming."));
				else
				{
					found.setName(writing);
					commonTell(mob,L("Your work has a new name."));
				}
			}
		}
		super.unInvoke();
	}

	public boolean error(final MOB mob)
	{
		commonTell(mob,L("You must specify what book to name, the name form-number, and the simple name to insert. Use BNAME LIST to see the list of name-forms."));
		return false;
	}
	
	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()<1)
			return error(mob);
		if(commands.get(0).equalsIgnoreCase("list"))
		{
			if(BookNaming.prefixList==null)
			{
				StringBuilder prefixes = new StringBuilder("^HName Forms:^N\n\r");
				int index=1;
				for(String P : PREFIXES)
				{
					P=CMStrings.replaceAll(P, "@x1", "NAME");
					P=CMStrings.replaceAll(P, "@x2", "AUTHOR");
					
					prefixes.append(CMStrings.padRight(""+index,2)).append(") "+P+"\n\r");
					index++;
				}
				BookNaming.prefixList=prefixes.toString();
			}
			commonTell(mob,BookNaming.prefixList);
			return true;
		}
		if(commands.size()<2)
			return error(mob);
		String itemName = commands.get(0);
		String nameType = commands.get(1);
		Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,itemName);
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
			target=mob.location().findItem(null, itemName);
		if((target!=null)&&(CMLib.flags().canBeSeenBy(target,mob)))
		{
			/*
			final Set<MOB> followers=mob.getGroupMembers(new TreeSet<MOB>());
			boolean ok=false;
			for(final MOB M : followers)
			{
				if(target.secretIdentity().indexOf(getBrand(M))>=0)
					ok=true;
			}
			if(!ok)
			{
				commonTell(mob,L("You aren't allowed to work on '@x1'.",itemName));
				return false;
			}
			*/
		}
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
		{
			commonTell(mob,L("You don't seem to have a '@x1'.",itemName));
			return false;
		}
		if(target.fetchEffect("Copyright")!=null)
		{
			commonTell(mob,L("This book is copyrighted, and can't be renamed."));
			return false;
		}
		
		final Ability write=mob.fetchAbility("Skill_Write");
		if(write==null)
		{
			commonTell(mob,L("You must know how to write to name a book."));
			return false;
		}
		
		if((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER)
		{
			commonTell(mob,L("You can't give a name to something like that."));
			return false;
		}
		
		if(!CMLib.flags().isReadable(target))
		{
			commonTell(mob,L("That's not even readable!"));
			return false;
		}
		
		if((!CMath.isInteger(nameType))
		||(CMath.s_int(nameType)<1)
		||(CMath.s_int(nameType)>PREFIXES.length))
		{
			commonTell(mob,L("'@x1' is not a valid name form number.  Try BNAME LIST.",nameType));
			return false;
		}
		nameType = PREFIXES[CMath.s_int(nameType)-1];
		String nameWord = CMParms.combine(commands,2).trim();
		if(nameWord.length()>20)
		{
			commonTell(mob,L("The name must be under 20 characters."));
			return false;
		}
		
		if(!target.isGeneric())
		{
			commonTell(mob,L("You aren't able to give that a name."));
			return false;
		}

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		writing=CMStrings.replaceAll(nameType, "@x1", nameWord);
		writing=CMStrings.replaceAll(writing, "@x2", mob.Name());
		verb=L("naming @x1",target.name());
		displayText=L("You are @x1",verb);
		found=target;
		if((!proficiencyCheck(mob,0,auto))||(!write.proficiencyCheck(mob,0,auto)))
			writing="";
		final int duration=getDuration(30,mob,1,3);
		final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),L("<S-NAME> start(s) naming <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
