package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class CraftingSkill extends CommonSkill
{
	public String ID() { return "CraftingSkill"; }
	public String name(){ return "Crafting Skill";}
	public long flags(){return FLAG_CRAFTING;}
	public Environmental newInstance()	{	return new CraftingSkill();	}
	
	protected String replacePercent(String thisStr, String withThis)
	{
		if(withThis.length()==0)
		{
			int x=thisStr.indexOf("% ");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+2,withThis).toString();
			x=thisStr.indexOf(" %");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+2,withThis).toString();
			x=thisStr.indexOf("%");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+1,withThis).toString();
		}
		else
		{
			int x=thisStr.indexOf("%");
			if(x>=0) return new StringBuffer(thisStr).replace(x,x+1,withThis).toString();
		}
		return thisStr;
	}

	protected Vector loadList(StringBuffer str)
	{
		Vector V=new Vector();
		if(str==null) return V;
		Vector V2=new Vector();
		boolean oneComma=false;
		int start=0;
		int longestList=0;
		for(int i=0;i<str.length();i++)
		{
			if(str.charAt(i)=='\t')
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
					if(V2.size()>longestList) longestList=V2.size();
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
			if(V2.size()>longestList) longestList=V2.size();
			V.addElement(V2);
		}
		for(int v=0;v<V.size();v++)
		{
			V2=(Vector)V.elementAt(v);
			while(V2.size()<longestList)
				V2.addElement("");
		}
		return V;
	}

	protected Item findFirstResource(Room room, String other)
	{
		if((other==null)||(other.length()==0))
			return null;
		for(int i=0;i<EnvResource.RESOURCE_DESCS.length;i++)
			if(EnvResource.RESOURCE_DESCS[i].equalsIgnoreCase(other))
				return findFirstResource(room,EnvResource.RESOURCE_DATA[i][0]);
		return null;
	}
	protected Item findFirstResource(Room room, int resource)
	{
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I instanceof EnvResource)
			&&(I.material()==resource)
			&&(!Sense.isOnFire(I))
			&&(I.container()==null))
				return I;
		}
		return null;
	}
	protected Item findMostOfMaterial(Room room, String other)
	{
		if((other==null)||(other.length()==0))
			return null;
		for(int i=0;i<EnvResource.MATERIAL_DESCS.length;i++)
			if(EnvResource.MATERIAL_DESCS[i].equalsIgnoreCase(other))
				return findMostOfMaterial(room,(i<<8));
		return null;
	}

	protected Item findMostOfMaterial(Room room, int material)
	{
		int most=0;
		int mostMaterial=-1;
		Item mostItem=null;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I instanceof EnvResource)
			&&((I.material()&EnvResource.MATERIAL_MASK)==material)
			&&(I.material()!=mostMaterial)
			&&(!Sense.isOnFire(I))
			&&(I.container()==null))
			{
				int num=findNumberOfResource(room,I.material());
				if(num>most)
				{
					mostItem=I;
					most=num;
					mostMaterial=I.material();
				}
			}
		}
		return mostItem;
	}

	protected void randomRecipeFix(MOB mob, Vector recipes, Vector commands)
	{
		if((mob.isMonster()
		&&(!Sense.isAnimalIntelligence(mob)))
		&&(commands.size()==0)
		&&(recipes!=null)
		&&(recipes.size()>0))
		{
			Vector randomRecipe=(Vector)recipes.elementAt(Dice.roll(1,recipes.size(),-1));
			commands.addElement((String)randomRecipe.firstElement());
		}
	}

	protected Vector matchingRecipeNames(Vector recipes, String recipeName)
	{
		Vector matches=new Vector();
		if(recipeName.length()==0) return matches;
		for(int r=0;r<recipes.size();r++)
		{
			Vector V=(Vector)recipes.elementAt(r);
			if(V.size()>0)
			{
				String item=(String)V.elementAt(0);
				if(replacePercent(item,"").equalsIgnoreCase(recipeName))
					matches.addElement(V);
			}
		}
		if(matches.size()>0) return matches;
		for(int r=0;r<recipes.size();r++)
		{
			Vector V=(Vector)recipes.elementAt(r);
			if(V.size()>0)
			{
				String item=(String)V.elementAt(0);
				if((replacePercent(item,"").toUpperCase().indexOf(recipeName.toUpperCase())>=0)
				||(recipeName.toUpperCase().indexOf(replacePercent(item,"").toUpperCase())>=0))
					matches.addElement(V);
			}
		}
		if(matches.size()>0) return matches;
		String lastWord=(String)Util.parse(recipeName).lastElement();
		for(int r=0;r<recipes.size();r++)
		{
			Vector V=(Vector)recipes.elementAt(r);
			if(V.size()>0)
			{
				String item=(String)V.elementAt(0);
				if((replacePercent(item,"").toUpperCase().indexOf(lastWord.toUpperCase())>=0)
				||(lastWord.toUpperCase().indexOf(replacePercent(item,"").toUpperCase())>=0))
					matches.addElement(V);
			}
		}
		return matches;
	}

	protected int findNumberOfResource(Room room, int resource)
	{
		int foundWood=0;
		for(int i=0;i<room.numItems();i++)
		{
			Item I=room.fetchItem(i);
			if((I instanceof EnvResource)
			&&(I.material()==resource)
			&&(!Sense.isOnFire(I))
			&&(I.container()==null))
				foundWood+=I.envStats().weight();
		}
		return foundWood;
	}

	protected Vector getAllMendable(MOB mob, Environmental from, Item contained)
	{
		Vector V=new Vector();
		if(from==null) return V;
		if(from instanceof Room)
		{
			Room R=(Room)from;
			for(int i=0;i<R.numItems();i++)
			{
				Item I=R.fetchItem(i);
				if((I!=null)
				&&(I.container()==contained)
				&&(canMend(mob,I,true))
				&&(Sense.canBeSeenBy(I,mob)))
					V.addElement(I);
			}
		}
		else
		if(from instanceof MOB)
		{
			MOB M=(MOB)from;
			for(int i=0;i<M.inventorySize();i++)
			{
				Item I=M.fetchInventory(i);
				if((I!=null)
				&&(I.container()==contained)
				&&(canMend(mob,I,true))
				&&(Sense.canBeSeenBy(I,mob))
				&&((mob==from)||(!I.amWearingAt(Item.INVENTORY))))
					V.addElement(I);
			}
		}
		else
		if(from instanceof Item)
		{
			if(from instanceof Container)
				V=getAllMendable(mob,((Item)from).owner(),(Item)from);
			if(canMend(mob,from,true))
				V.addElement(from);
		}
		return V;
	}

	public boolean publicScan(MOB mob, Vector commands)
	{
		String rest=Util.combine(commands,1);
		Environmental scanning=null;
		if(rest.length()==0)
			scanning=mob;
		else
		if(rest.equalsIgnoreCase("room"))
			scanning=mob.location();
		else
		{
			scanning=mob.location().fetchInhabitant(rest);
			if((scanning==null)||(!Sense.canBeSeenBy(scanning,mob)))
			{
				commonTell(mob,"You don't see anyone called '"+rest+"' here.");
				return false;
			}
		}
		Vector allStuff=getAllMendable(mob,scanning,null);
		if(allStuff.size()==0)
		{
			if(mob==scanning)
				commonTell(mob,"You don't seem to have anything that needs mending with "+name()+".");
			else
				commonTell(mob,"You don't see anything on "+scanning.name()+" that needs mending with "+name()+".");
			return false;
		}
		StringBuffer buf=new StringBuffer("The following items could use some "+name()+":\n\r");
		for(int i=0;i<allStuff.size();i++)
		{
			Item I=(Item)allStuff.elementAt(i);
			buf.append(Util.padRight(I.usesRemaining()+"%",5)+I.name());
			if(!I.amWearingAt(Item.INVENTORY))
				buf.append(" ("+Sense.wornLocation(I.rawWornCode())+")");
			if(i<(allStuff.size()-1))
				buf.append("\n\r");
		}
		commonTell(mob,buf.toString());
		return true;
	}


	protected boolean canMend(MOB mob, Environmental E, boolean quiet)
	{
		if(E==null) return false;
		if(!(E instanceof Item))
		{
			if(!quiet)
				commonTell(mob,"You can't mend "+E.name()+".");
			return false;
		}
		Item IE=(Item)E;
		if(!IE.subjectToWearAndTear())
		{
			if(!quiet)
				commonTell(mob,"You can't mend "+IE.name()+".");
			return false;
		}
		if(IE.usesRemaining()>=100)
		{
			if(!quiet)
				commonTell(mob,IE.name()+" is in good condition already.");
			return false;
		}
		return true;
	}

	protected int destroyResources(Room room,
									int howMuch,
									int finalMaterial,
									Item firstOther,
									Item never)
	{
		int lostValue=0;
		if(room==null) return 0;

		if(howMuch>0)
		for(int i=room.numItems()-1;i>=0;i--)
		{
			Item I=room.fetchItem(i);
			if(I==null) break;
			if(I==never) continue;

			if((firstOther!=null)&&(I==firstOther))
			{
				lostValue+=I.value();
				I.destroy();
			}
			else
			if((I instanceof EnvResource)
			&&(I.container()==null)
			&&(!Sense.isOnFire(I))
			&&(I.material()==finalMaterial))
			{
				if(I.baseEnvStats().weight()>howMuch)
				{
					I.baseEnvStats().setWeight(I.baseEnvStats().weight()-howMuch);
					I.destroy();
					lostValue+=I.value();
					for(int x=0;x<I.baseEnvStats().weight();x++)
					{
						Environmental E=makeResource(finalMaterial,true);
						if(E instanceof Item)
							room.addItemRefuse((Item)E,Item.REFUSE_PLAYER_DROP);
					}
					break;
				}
				else
				{
					howMuch-=I.baseEnvStats().weight();
					I.destroy();
					lostValue+=I.value();
					if(howMuch<=0)
						break;
				}
			}
		}
		return lostValue;
	}


}
