package com.planet_ink.coffee_mud.Abilities.Common;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;
import java.io.File;

public class Weaponsmithing extends CommonSkill
{
	public String ID() { return "Weaponsmithing"; }
	public String name(){ return "Weaponsmithing";}
	private static final String[] triggerStrings = {"WEAPONSMITH","WEAPONSMITHING"};
	public String[] triggerStrings(){return triggerStrings;}

	private static final int RCP_FINALNAME=0;
	private static final int RCP_LEVEL=1;
	private static final int RCP_TICKS=2;
	private static final int RCP_WOOD=3;
	private static final int RCP_VALUE=4;
	private static final int RCP_CLASSTYPE=5;
	private static final int RCP_WEAPONCLASS=6;
	private static final int RCP_WEAPONTYPE=7;
	private static final int RCP_ARMORDMG=8;
	private static final int RCP_ATTACK=9;
	private static final int RCP_HANDS=10;
	private static final int RCP_MAXRANGE=11;
	private static final int RCP_EXTRAREQ=12;

	private Item building=null;
	private Item fire=null;
	private boolean mending=false;
	private boolean messedUp=false;
	private static boolean mapped=false;
	public Weaponsmithing()
	{
		super();
		if(!mapped){mapped=true;
					CMAble.addCharAbilityMapping("All",1,ID(),false);}
	}
	public Environmental newInstance(){	return new Weaponsmithing();}

	public boolean tick(int tickID)
	{
		if((affected!=null)&&(affected instanceof MOB)&&(tickID==Host.MOB_TICK))
		{
			MOB mob=(MOB)affected;
			if((building==null)
			||(fire==null)
			||(!Sense.isOnFire(fire))
			||(!mob.location().isContent(fire))
			||(mob.isMine(fire)))
				unInvoke();
		}
		return super.tick(tickID);
	}

	private static synchronized Vector loadRecipes()
	{
		Vector V=(Vector)Resources.getResource("WEAPONSMITHING RECIPES");
		if(V==null)
		{
			StringBuffer str=Resources.getFile("resources"+File.separatorChar+"weaponsmith.txt");
			V=loadList(str);
			if(V.size()==0)
				Log.errOut("Weaponsmithing","Recipes not found!");
			Resources.submitResource("WEAPONSMITHING RECIPES",V);
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
						if(mending)
							commonEmote(mob,"<S-NAME> completely mess(es) up mending "+building.name()+".");
						else
							commonEmote(mob,"<S-NAME> completely mess(es) up smithing "+building.name()+".");
					}
					else
					{
						if(mending)
							building.setUsesRemaining(100);
						else
							mob.location().addItemRefuse(building,Item.REFUSE_PLAYER_DROP);
					}
				}
				building=null;
				mending=false;
			}
		}
		super.unInvoke();
	}


	private int specClass(String weaponClass)
	{
		for(int i=0;i<Weapon.classifictionDescription.length;i++)
		{
			if(Weapon.classifictionDescription[i].equalsIgnoreCase(weaponClass))
				return i;
		}
		return -1;
	}
	private int specType(String weaponType)
	{
		for(int i=0;i<Weapon.typeDescription.length;i++)
		{
			if(Weapon.typeDescription[i].equalsIgnoreCase(weaponType))
				return i;
		}
		return -1;
	}
	private boolean canDo(String weaponClass, MOB mob)
	{
		String specialization="";
		switch(specClass(weaponClass))
		{
		case Weapon.CLASS_AXE: specialization="Specialization_Axe"; break;
		case Weapon.CLASS_STAFF:
		case Weapon.CLASS_HAMMER:
		case Weapon.CLASS_BLUNT: specialization="Specialization_BluntWeapon"; break;
		case Weapon.CLASS_DAGGER:
		case Weapon.CLASS_EDGED: specialization="Specialization_EdgedWeapon"; break;
		case Weapon.CLASS_FLAILED: specialization="Specialization_FlailedWeapon"; break;
		case Weapon.CLASS_POLEARM: specialization="Specialization_Polearm"; break;
		case Weapon.CLASS_SWORD: specialization="Specialization_Sword"; break;
		case Weapon.CLASS_THROWN:
		case Weapon.CLASS_RANGED: specialization="Specialization_Ranged"; break;
		default: return false;
		}
		if(mob.fetchAbility(specialization)==null) return false;
		return true;
	}

	public boolean canBeLearnedBy(MOB teacher, MOB student)
	{
		if(!super.canBeLearnedBy(teacher,student))
			return false;
		if(student==null) return true;
		if((student.fetchAbility("Specialization_Axe")==null)
		&&(student.fetchAbility("Specialization_BluntWeapon")==null)
		&&(student.fetchAbility("Specialization_EdgedWeapon")==null)
		&&(student.fetchAbility("Specialization_FlailedWeapon")==null)
		&&(student.fetchAbility("Specialization_Polearm")==null)
		&&(student.fetchAbility("Specialization_Sword")==null)
		&&(student.fetchAbility("Specialization_Ranged")==null))
		{
			teacher.tell(student.name()+" has not yet specialized in any weapons.");
			student.tell("You need to specialize in a weapon type to learn "+name()+".");
			return false;
		}
		if(student.fetchAbility("Blacksmithing")==null)
		{
			teacher.tell(student.name()+" has not yet learned blacksmithing.");
			student.tell("You need to learn blacksmithing before you can learn "+name()+".");
			return false;
		}

		return true;
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(commands.size()==0)
		{
			commonTell(mob,"Make what? Enter \"weaponsmith list\" for a list, or \"weaponsmith mend <item>\".");
			return false;
		}
		Vector recipes=loadRecipes();
		String str=(String)commands.elementAt(0);
		String startStr=null;
		int completion=4;
		if(str.equalsIgnoreCase("list"))
		{
			StringBuffer buf=new StringBuffer("Weapons <S-NAME> <S-IS-ARE> skilled at making:\n\r");
			int toggler=1;
			int toggleTop=4;
			for(int r=0;r<toggleTop;r++)
				buf.append(Util.padRight("Item",14)+" "+Util.padRight("Amt",3)+" ");
			buf.append("\n\r");
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=replacePercent((String)V.elementAt(RCP_FINALNAME),"");
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					int wood=Util.s_int((String)V.elementAt(RCP_WOOD));
					if((level<=mob.envStats().level())
					&&(canDo((String)V.elementAt(this.RCP_WEAPONCLASS),mob)))
					{
						buf.append(Util.padRight(item,14)+" "+Util.padRight(""+wood,3)+((toggler!=toggleTop)?" ":"\n\r"));
						if(++toggler>toggleTop) toggler=1;
					}
				}
			}
			if(toggler!=1) buf.append("\n\r");
			commonEmote(mob,buf.toString());
			return true;
		}
		if(str.equalsIgnoreCase("mend"))
		{
			building=null;
			mending=false;
			messedUp=false;
			Vector newCommands=Util.parse(Util.combine(commands,1));
			building=getTarget(mob,mob.location(),givenTarget,newCommands,Item.WORN_REQ_UNWORNONLY);
			if(building==null) return false;

			if((!(building instanceof Weapon))
			||((((Weapon)building).material()&EnvResource.MATERIAL_MASK)!=EnvResource.MATERIAL_METAL))
			{
				commonTell(mob,"You don't know how to mend that sort of thing.");
				return false;
			}
			if(!building.subjectToWearAndTear())
			{
				commonTell(mob,"You can't mend "+building.name()+".");
				return false;
			}
			if(((Item)building).usesRemaining()>=100)
			{
				commonTell(mob,building.name()+" is in good condition already.");
				return false;
			}
			mending=true;
			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;
			startStr="<S-NAME> start(s) mending "+building.name()+".";
			displayText="You are mending "+building.name();
			verb="mending "+building.name();
		}
		else
		{
			mending=false;
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
			building=null;
			messedUp=false;
			String recipeName=Util.combine(commands,0);
			Vector foundRecipe=null;
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=(String)V.elementAt(RCP_FINALNAME);
					int level=Util.s_int((String)V.elementAt(RCP_LEVEL));
					if((level<=mob.envStats().level())
					&&(canDo((String)V.elementAt(this.RCP_WEAPONCLASS),mob))
					&&(replacePercent(item,"").equalsIgnoreCase(recipeName)))
					{
						foundRecipe=V;
						break;
					}
				}
			}
			if(foundRecipe==null)
			{
				commonTell(mob,"You don't know how to make a '"+recipeName+"'.  Try \"weaponsmith list\" for a list.");
				return false;
			}
			int woodRequired=Util.s_int((String)foundRecipe.elementAt(RCP_WOOD));
			String otherRequired=(String)foundRecipe.elementAt(RCP_EXTRAREQ);
			Item firstWood=null;
			Item firstOther=null;
			int foundWood=0;
			for(int i=0;i<mob.location().numItems();i++)
			{
				Item I=mob.location().fetchItem(i);
				if((I instanceof EnvResource)
				&&(!Sense.isOnFire(I))
				&&(I.container()==null))
				{
					if(((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_METAL)
					   ||((I.material()&EnvResource.MATERIAL_MASK)==EnvResource.MATERIAL_MITHRIL))
					{
						if(firstWood==null)firstWood=I;
						if(firstWood.material()==I.material())
							foundWood++;
					}
					else
					if((otherRequired.length()>0)
					&&(firstOther==null)
					&&(((EnvResource.MATERIAL_DESCS[(I.material()&EnvResource.MATERIAL_MASK)>>8].equalsIgnoreCase(otherRequired))
					   ||(EnvResource.RESOURCE_DESCS[(I.material()&EnvResource.RESOURCE_MASK)].equalsIgnoreCase(otherRequired)))))
						firstOther=I;
				}
			}
			if(foundWood==0)
			{
				commonTell(mob,"There is no metal here to make anything from!  It might need to put it down first.");
				return false;
			}
			if(firstWood.material()==EnvResource.RESOURCE_MITHRIL)
				woodRequired=woodRequired/2;
			else
			if(firstWood.material()==EnvResource.RESOURCE_ADAMANTITE)
				woodRequired=woodRequired/3;
			if(woodRequired<1) woodRequired=1;
			if(foundWood<woodRequired)
			{
				commonTell(mob,"You need "+woodRequired+" pounds of "+EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)].toLowerCase()+" to construct a "+recipeName.toLowerCase()+".  There is not enough here.  Are you sure you set it all on the ground first?");
				return false;
			}
			if((otherRequired.length()>0)&&(firstOther==null))
			{
				commonTell(mob,"You need a pound of "+otherRequired.toLowerCase()+" to construct a "+recipeName.toLowerCase()+".  There is not enough here.  Are you sure you set it all on the ground first?");
				return false;
			}
			if(!super.invoke(mob,commands,givenTarget,auto))
				return false;
			int woodDestroyed=woodRequired;
			for(int i=mob.location().numItems()-1;i>=0;i--)
			{
				Item I=mob.location().fetchItem(i);
				if((firstOther!=null)&&(I==firstOther))
					I.destroyThis();
				else
				if((I instanceof EnvResource)
				&&(I.container()==null)
				&&(!Sense.isOnFire(I))
				&&(I.material()==firstWood.material())
				&&((--woodDestroyed)>=0))
				  I.destroyThis();
			}
			building=CMClass.getItem((String)foundRecipe.elementAt(RCP_CLASSTYPE));
			if(building==null)
			{
				commonTell(mob,"There's no such thing as a "+foundRecipe.elementAt(RCP_CLASSTYPE)+"!!!");
				return false;
			}
			completion=Util.s_int((String)foundRecipe.elementAt(this.RCP_TICKS))-((mob.envStats().level()-Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL)))*2);
			String itemName=replacePercent((String)foundRecipe.elementAt(RCP_FINALNAME),EnvResource.RESOURCE_DESCS[(firstWood.material()&EnvResource.RESOURCE_MASK)]).toLowerCase();
			itemName=Util.startWithAorAn(itemName);
			building.setName(itemName);
			startStr="<S-NAME> start(s) smithing "+building.name()+".";
			displayText="You are smithing "+building.name();
			verb="smithing "+building.name();
			int hardness=EnvResource.RESOURCE_DATA[firstWood.material()&EnvResource.RESOURCE_MASK][3]-6;
			building.setDisplayText(itemName+" is here");
			building.setDescription(itemName+". ");
			building.baseEnvStats().setWeight(woodRequired);
			building.setBaseValue((Util.s_int((String)foundRecipe.elementAt(RCP_VALUE))/4)+(woodRequired*(firstWood.baseGoldValue())));
			building.setMaterial(firstWood.material());
			building.baseEnvStats().setLevel(Util.s_int((String)foundRecipe.elementAt(RCP_LEVEL))+(hardness*3));
			if(building instanceof Weapon)
			{
				Weapon w=(Weapon)building;
				w.setWeaponClassification(specClass((String)foundRecipe.elementAt(RCP_WEAPONCLASS)));
				w.setWeaponType(specType((String)foundRecipe.elementAt(RCP_WEAPONTYPE)));
				w.setRanges(w.minRange(),Util.s_int((String)foundRecipe.elementAt(RCP_MAXRANGE)));
			}
			if(Util.s_int((String)foundRecipe.elementAt(RCP_HANDS))==2)
				building.setRawLogicalAnd(true);
			building.baseEnvStats().setAttackAdjustment(Util.s_int((String)foundRecipe.elementAt(RCP_ATTACK))+(hardness*5));
			building.baseEnvStats().setDamage(Util.s_int((String)foundRecipe.elementAt(RCP_ARMORDMG))+hardness);

			building.recoverEnvStats();
			building.text();
			building.recoverEnvStats();
		}

		messedUp=!profficiencyCheck(0,auto);
		if(completion<6) completion=6;
		FullMsg msg=new FullMsg(mob,null,Affect.MSG_NOISYMOVEMENT,startStr);
		if(mob.location().okAffect(msg))
		{
			mob.location().send(mob,msg);
			beneficialAffect(mob,mob,completion);
		}
		return true;
	}
}
