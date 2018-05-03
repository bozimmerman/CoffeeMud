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
public class Titling extends CommonSkill
{
	@Override
	public String ID()
	{
		return "Titling";
	}

	private final static String	localizedName	= CMLib.lang().L("Titling");

	@Override
	public String name()
	{
		return localizedName;
	}

	private static final String[]	triggerStrings	= I(new String[] { "ENTITLE", "TITLING" });

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

	public static final String CATALOG_PREFIX = " This is a catalog of type `";
	
	protected Item		found	= null;
	protected String	writing	= "";
	protected String	catalog = "";
	
	public static enum CATALOGS {
		Armor,
		Jewelry,
		Weapons,
		Furniture,
		Books,
		Potions,
		Pills,
		Wands,
		Foods,
		Drinks,
		Alcohol,
		Races,
		Survey,
		Misc,
		None
		;
		private static String[] names=null;
		public static String[] getNames()
		{
			if(names == null)
			{
				names=new String[CATALOGS.values().length];
				for(int i=0;i<CATALOGS.values().length;i++)
					names[i]=CATALOGS.values()[i].name();
			}
			return names;
		}
	}

	@Override
	protected boolean canBeDoneSittingDown()
	{
		return true;
	}

	public Titling()
	{
		super();
		displayText=L("You are titling...");
		verb=L("titling");
	}

	public String doTitle(String old, String newTitle)
	{
		final String titlePrefix=L(" entitled `");
		final int x=old.indexOf(titlePrefix);
		if(x > 0)
		{
			final int y=old.lastIndexOf('`');
			if(y>x+11)
				old=old.substring(0,x)+old.substring(y+1);
		}
		if((newTitle != null)&&(newTitle.length()>0))
			return old + titlePrefix+newTitle.replace('\'', '-').replace('`', '-')+"`";
		return old;
	}
	
	public String doCatalog(String old, String newCatalog)
	{
		final String catalogPrefix=L(CATALOG_PREFIX);
		final int x=old.indexOf(catalogPrefix);
		if(x > 0)
		{
			final int y=old.indexOf("`.",x+catalogPrefix.length());
			if(y>x+catalogPrefix.length())
				old=old.substring(0,x).trim() + old.substring(y+2);
		}
		if((newCatalog != null)&&(newCatalog.length()>0))
			return old + catalogPrefix+newCatalog+"`.";
		return old;
	}

	public static String getCatalogType(Item I)
	{
		final String desc=I.description();
		final int x=desc.indexOf(CATALOG_PREFIX);
		if(x > 0)
		{
			final int y=desc.indexOf("`.",x+CATALOG_PREFIX.length());
			if(y>x+CATALOG_PREFIX.length())
				return desc.substring(x+CATALOG_PREFIX.length(),y);
		}
		return "";
	}
	
	public static String getCatalogEntryType(Environmental E)
	{
		if(E instanceof Room)
			return CATALOGS.Survey.name();
		if(E instanceof Area)
			return CATALOGS.Survey.name();
		if(E instanceof Item)
		{
			if(Taxidermy.getStatueRace((Item)E).length()>0)
				return CATALOGS.Races.name();
		}
		if(E instanceof Armor)
			return CATALOGS.Armor.name(); // armor
		if(E instanceof Armor)
			return CATALOGS.Jewelry.name(); //jewelry
		if(E instanceof Weapon)
			return CATALOGS.Weapons.name();
		if((E instanceof Rideable)&&(E instanceof Item))
			return CATALOGS.Furniture.name();
		if((E instanceof Item)
		&&(CMLib.flags().isReadable((Item)E))
		&&((((Item)E).material()&RawMaterial.MATERIAL_MASK)==RawMaterial.MATERIAL_PAPER))
			return CATALOGS.Books.name();
		if((E instanceof Drink)
		&&(E instanceof Item)
		&& CMLib.flags().isAlcoholic((Item)E))
			return CATALOGS.Alcohol.name();
		if((E instanceof Item)
		&& (E instanceof Potion))
			return CATALOGS.Potions.name();
		if((E instanceof Item)
		&&(E instanceof Pill))
			return CATALOGS.Pills.name();
		if(E instanceof Wand)
			return CATALOGS.Wands.name();
		if(E instanceof Food)
			return CATALOGS.Foods.name();
		if(E instanceof Drink)
			return CATALOGS.Drinks.name();
		if(E instanceof MOB)
			return CATALOGS.Races.name();
		if(E instanceof Item)
			return CATALOGS.Misc.name();
		return "";
	}

	@Override
	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB)&&(!aborted)&&(!helping))
			{
				final MOB mob=(MOB)affected;
				if(writing.length()==0)
					commonTell(mob,L("You mess up your titling."));
				else
				{
					if(writing.equalsIgnoreCase("remove"))
						writing="";
					found.setName(doTitle(found.Name(),writing));
					found.setDisplayText(doTitle(found.displayText(),writing));
					if(catalog.length()>0)
					{
						if(catalog.equalsIgnoreCase("None"))
							catalog="";
						found.setDescription(doCatalog(found.description(),catalog));
					}
				}
			}
		}
		super.unInvoke();
	}

	@Override
	public boolean invoke(MOB mob, List<String> commands, Physical givenTarget, boolean auto, int asLevel)
	{
		if(super.checkStop(mob, commands))
			return true;
		if(commands.size()<2)
		{
			commonTell(mob,L("You must specify what you want to entitle, and what you want the title to be.  Use a title of `remove` to remove "
					+ "a previous title.  You can also designate a category for the word by making the last word of the title one of "
					+ "these: "+CMParms.toListString(CATALOGS.getNames()))+".");
			return false;
		}
		Item target=mob.fetchItem(null,Wearable.FILTER_UNWORNONLY,commands.get(0));
		if((target==null)||(!CMLib.flags().canBeSeenBy(target,mob)))
			target=mob.location().findItem(null, commands.get(0));
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
				commonTell(mob,L("You aren't allowed to work on '@x1'.",(commands.get(0))));
				return false;
			}
			*/
		}
		else
			target=null;
		if(target==null)
		{
			commonTell(mob,L("You don't seem to have a '@x1'.",(commands.get(0))));
			return false;
		}
		commands.remove(commands.get(0));
		
		String newCatalog = "";
		if(commands.size()>1)
		{
			String potentialCatalog = CMStrings.capitalizeAndLower(commands.get(commands.size()-1));
			if(CMParms.contains(CATALOGS.getNames(), potentialCatalog))
			{
				newCatalog=potentialCatalog;
				commands.remove(commands.size()-1);
			}
		}

		final Ability write=mob.fetchAbility("Skill_Write");
		if(write==null)
		{
			commonTell(mob,L("You must know how to write to entitle."));
			return false;
		}
		
		if((target.material()&RawMaterial.MATERIAL_MASK)!=RawMaterial.MATERIAL_PAPER)
		{
			commonTell(mob,L("You can't give a title to something like that."));
			return false;
		}
		if(!CMLib.flags().isReadable(target))
		{
			commonTell(mob,L("That's not even readable!"));
			return false;
		}
		
		/*
		 *String brand = getBrand(target);
		if((brand==null)||(brand.length()==0))
		{
			commonTell(mob,L("You aren't permitted to entitle that."));
			return false;
		}
		 */
		if(!target.isGeneric())
		{
			commonTell(mob,L("You aren't able to give that a title."));
			return false;
		}
		
		if(BookNaming.isAlreadyNamed(target.Name()))
		{
			commonTell(mob,L("That already has a name."));
			return false;
		}
		if(target.fetchEffect("Copyright")!=null)
		{
			commonTell(mob,L("This book is copyrighted, and can't be renamed."));
			return false;
		}
		

		if(!super.invoke(mob,commands,givenTarget,auto,asLevel))
			return false;
		writing=CMParms.combine(commands,0);
		catalog=newCatalog;
		verb=L("titling @x1",target.name());
		displayText=L("You are @x1",verb);
		found=target;
		if((!proficiencyCheck(mob,0,auto))||(!write.proficiencyCheck(mob,0,auto)))
			writing="";
		final int duration=getDuration(30,mob,1,3);
		final CMMsg msg=CMClass.getMsg(mob,target,this,getActivityMessageType(),L("<S-NAME> start(s) titling <T-NAME>."));
		if(mob.location().okMessage(mob,msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,asLevel,duration);
		}
		return true;
	}
}
