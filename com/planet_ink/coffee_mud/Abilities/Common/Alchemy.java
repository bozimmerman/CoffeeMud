package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Alchemy extends CommonSkill
{
	public String ID() { return "Alchemy"; }
	public String name(){ return "Alchemy";}
	private static final String[] triggerStrings = {"BREW","ALCHEMY"};
	public String[] triggerStrings(){return triggerStrings;}

	private boolean requiresFire=false;
	private Item building=null;
	private Item fire=null;
	String oldName="";
	private Ability theSpell=null;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public Alchemy()
	{
		super();

		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("Mage",1,ID(),false);
					CMAble.addCharAbilityMapping("Bard",10,ID(),false);
					CMAble.addCharAbilityMapping("Cleric",1,ID(),false);}
	}
	public Environmental newInstance(){ return new Alchemy();	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if((building==null)
			||((requiresFire)&&((fire==null)
								||(!Sense.isOnFire(fire))
								||(!mob.location().isContent(fire))
								||(mob.isMine(fire))))
			||(theSpell==null))
			{
				aborted=true;
				unInvoke();
			}
			else
			if(tickUp==0)
			{
				if((theSpell.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
				{
					commonEmote(mob,"<S-NAME> start(s) praying for "+building.name()+".");
					displayText="You are praying for "+building.name();
					verb="praying for "+building.name();
				}
				else
				{
					commonEmote(mob,"<S-NAME> start(s) brewing "+building.name()+".");
					displayText="You are brewing "+building.name();
					verb="brewing "+building.name();
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	private static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("ALCHEMY RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"skills"+File.separatorChar+"alchemy.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Alchemy","Recipes not found!");
			Resources.submitResource("ALCHEMY RECIPES",V);
		}
		return V;
	}

	public void unInvoke()
	{
		if(canBeUninvoked())
		{
			if((affected!=null)&&(affected instanceof MOB))
			{
				MOB mob=(MOB)affected;
				if((building!=null)&&(!aborted))
				{
					if(messedUp)
					{
						if(oldName.length()>0)
							commonTell(mob,"Something went wrong! "+(Character.toUpperCase(oldName.charAt(0))+oldName.substring(1))+" explodes!");
					}
					else
						mob.addInventory(building);
				}
				building=null;
			}
		}
		super.unInvoke();
	}

	private int spellLevel(MOB mob, String spell)
	{
		int lvl=CMAble.getQualifyingLevel(mob.charStats().getCurrentClass().ID(),spell);
		if(lvl<0) return lvl;
		switch(lvl)
		{
		case 0: return lvl;
		case 1: return lvl;
		case 2: return lvl+1;
		case 3: return lvl+1;
		case 4: return lvl+2;
		case 5: return lvl+2;
		case 6: return lvl+3;
		case 7: return lvl+3;
		case 8: return lvl+4;
		case 9: return lvl+4;
		default: return lvl+5;
		}
	}


	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()<1)
		{
			commonTell(mob,"Brew what? Enter \"brew list\" for a list.");
			return false;
		}
		Vector recipes=loadRecipes();
		String pos=(String)commands.lastElement();
		if(pos.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer("Potions you know how to brew:\n\r");
			buf.append(Util.padRight("Spell",25)+" "+Util.padRight("Spell",25)+" "+Util.padRight("Spell",25));
			int toggler=1;
			int toggleTop=3;
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String spell=(String)V.elementAt(0);
					Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(spellLevel(mob,spell)>=0)
					&&(mob.envStats().level()>=spellLevel(mob,spell)))
					{
						buf.append(Util.padRight(A.name(),25)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			if(toggler!=1) buf.append("\n\r");
			commonTell(mob,buf.toString());
			return true;
		}
		else
		if(commands.size()<2)
		{
			commonEmote(mob,"You must specify what magic you wish to brew, and the container to brew it in.");
			return false;
		}
		else
		{
			building=getTarget(mob,null,givenTarget,Util.parse(pos),Item.WORN_REQ_UNWORNONLY);
			commands.remove(pos);
			if(building==null) return false;
			if(!mob.isMine(building))
			{
				commonTell(mob,"You'll need to pick that up first.");
				return false;
			}
			if(!(building instanceof Container))
			{
				commonTell(mob,"There's nothing in "+building.name()+" to cook!");
				return false;
			}
			if(!(building instanceof Drink))
			{
				commonTell(mob,"You can't drink out of a "+building.name()+".");
				return false;
			}
			if(((Drink)building).liquidRemaining()==0)
			{
				commonTell(mob,"The "+building.name()+" contains no liquid base.  Water is probably fine.");
				return false;
			}
			if(building.material()!=EnvResource.RESOURCE_GLASS)
			{
				commonTell(mob,"You can only brew into glass containers.");
				return false;
			}
			String recipeName=Util.combine(commands,0);
			theSpell=null;
			String ingredient="";
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String spell=(String)V.elementAt(0);
					Ability A=mob.fetchAbility(spell);
					if((A!=null)
					&&(mob.envStats().level()>=spellLevel(mob,spell))
					&&(A.name().equalsIgnoreCase(recipeName)))
					{
						theSpell=A;
						ingredient=(String)V.elementAt(1);
					}
				}
			}
			if(theSpell==null)
			{
				commonTell(mob,"You don't know how to brew '"+recipeName+"'.  Try \"brew list\" for a list.");
				return false;
			}
			int experienceToLose=10;
			if((theSpell.classificationCode()&Ability.ALL_CODES)==Ability.PRAYER)
			{
				requiresFire=false;
				fire=null;
				experienceToLose+=CMAble.qualifyingLevel(mob,theSpell)*10;
				experienceToLose-=CMAble.qualifyingClassLevel(mob,theSpell)*5;
			}
			else
			{
				requiresFire=true;
				fire=null;
				for(int i=0;i<mob.location().numItems();i++)
				{
					Item I2=mob.location().fetchItem(i);
					if((I2!=null)&&(I2.container()==null)&&(Sense.isOnFire(I2)))
					{
						fire=I2;
						break;
					}
				}
				if((fire==null)||(!mob.location().isContent(fire)))
				{
					commonTell(mob,"A fire will need to be built first.");
					return false;
				}
				experienceToLose+=CMAble.qualifyingLevel(mob,theSpell)*10;
				experienceToLose-=CMAble.qualifyingClassLevel(mob,theSpell)*5;
			}
			int resourceType=-1;
			for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
				if(EnvResource.RESOURCE_DESCS[i].equalsIgnoreCase(ingredient))
				{ resourceType=EnvResource.RESOURCE_DATA[i][0]; break;}

			boolean found=false;
			Vector V=((Container)building).getContents();
			if(resourceType>0)
			{
				if(((Drink)building).liquidType()==resourceType)
				{
					found=true;
					if(V.size()>0)
					{
						commonTell(mob,"The extraneous stuff from the "+building.name()+" must be removed before starting.");
						return false;
					}
				}
				else
				for(int i=0;i<V.size();i++)
				{
					Item I=(Item)V.elementAt(i);
					if(I.material()==resourceType)
						found=true;
					else
					{
						commonTell(mob,"The "+I.name()+" must be removed from the "+building.name()+" before starting.");
						return false;
					}
				}
				if(!found)
				{
					commonTell(mob,"This potion requires "+ingredient+".  Please place some inside the "+building.name()+" and try again.");
					return false;
				}
			}
			if(experienceToLose<10) experienceToLose=10;

			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;

			mob.charStats().getCurrentClass().loseExperience(mob,experienceToLose);
			commonTell(mob,"You lose "+experienceToLose+" experience points for the effort.");
			oldName=building.name();
			building.destroy();
			building=CMClass.getItem("GenPotion");
			((Potion)building).setSpellList(theSpell.ID());
			building.setName("a potion of "+theSpell.name().toLowerCase());
			building.setDisplayText("a potion of "+theSpell.name().toLowerCase()+" sits here.");
			building.setDescription("");
			building.recoverEnvStats();
			building.text();

			int completion=CMAble.qualifyingLevel(mob,theSpell)*5;
			if(completion<10) completion=10;
			messedUp=!profficiencyCheck(0,auto);
			FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,null);
			if(mob.location().okAffect(mob,msg))
			{
				mob.location().send(mob,msg);
				beneficialAffect(mob,mob,completion);
			}
		}
		return true;
	}
}
