package com.planet_ink.coffee_mud.Abilities.Common;
import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/* 
   Copyright 2000-2005 Bo Zimmerman

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

public class CraftingSkill extends CommonSkill
{
	public String ID() { return "CraftingSkill"; }
	public String name(){ return "Crafting Skill";}
	public long flags(){return FLAG_CRAFTING;}

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
	
	protected Vector addRecipes(MOB mob, Vector recipes)
	{
	    if(mob==null) return recipes;
	    Item I=null;
	    Vector V=null;
	    Vector V2=null;
	    boolean clonedYet=false;
	    for(int i=0;i<mob.inventorySize();i++)
	    {
	        I=mob.fetchInventory(i);
	        if((I instanceof Recipe)
	        &&(((Recipe)I).getCommonSkillID().equalsIgnoreCase(ID())))
	        {
	            if(!clonedYet){ recipes=(Vector)recipes.clone(); clonedYet=true;}
	            V=loadList(new StringBuffer(((Recipe)I).getRecipeCodeLine()));
	            for(int v=0;v<V.size();v++)
	            {
	                V2=(Vector)V.elementAt(v);
	                if((recipes.size()==0)||(((Vector)recipes.firstElement()).size()==V2.size()))
	                    recipes.addElement(V2);
	            }
	        }
	    }
	    return recipes;
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
			&&(!Sense.enchanted(I))
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
			&&(!Sense.enchanted(I))
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


	protected Item fetchFoundOtherEncoded(MOB mob, String otherRequired)
	{
		if((otherRequired==null)||(otherRequired.trim().length()==0))
			return null;
		Item firstOther=null;
		boolean resourceOther=otherRequired.startsWith("!");
		if(resourceOther) otherRequired=otherRequired.substring(1);
		if((otherRequired.length()>0)&&(!resourceOther))
			firstOther=findMostOfMaterial(mob.location(),otherRequired);
		if((firstOther==null)&&(otherRequired.length()>0))
			firstOther=findFirstResource(mob.location(),otherRequired);
		return firstOther;
	}

	protected static final int FOUND_CODE=0;
	protected static final int FOUND_AMT=1;
	protected int fixResourceRequirement(int resource, int amt)
	{
		if(amt<=0) return amt;
		switch(resource)
		{
		case EnvResource.RESOURCE_MITHRIL:
			amt=amt/2;
			break;
		case EnvResource.RESOURCE_ADAMANTITE:
			amt=amt/3;
			break;
		case EnvResource.RESOURCE_BALSA:
			amt=amt/2;
			break;
		case EnvResource.RESOURCE_IRONWOOD:
			amt=amt*2;
			break;
		}
		if(amt<0) amt=1;
		return amt;
	}
	
	public Vector fetchRecipes(){return loadRecipes();}
	protected Vector loadRecipes(){ return new Vector();}
	
	protected int[][] fetchFoundResourceData(MOB mob,
											 int req1Required,
											 String req1Desc, int[] req1,
											 int req2Required,
											 String req2Desc, int[] req2,
											 boolean bundle,
											 int autoGeneration)
	{
		int[][] data=new int[2][2];
		if((req1Desc!=null)&&(req1Desc.length()==0)) req1Desc=null;
		if((req2Desc!=null)&&(req2Desc.length()==0)) req2Desc=null;

		// the fake resource generation:
		if(autoGeneration>0)
		{
			data[0][FOUND_AMT]=req1Required;
			data[1][FOUND_AMT]=req2Required;
			data[1][FOUND_CODE]=autoGeneration;
			data[0][FOUND_CODE]=autoGeneration;
			return data;
		}

		Item firstWood=null;
		Item firstOther=null;
		if(req1!=null)
		{
			for(int i=0;i<req1.length;i++)
			{
				if((req1[i]&EnvResource.RESOURCE_MASK)==0)
					firstWood=findMostOfMaterial(mob.location(),req1[i]);
				else
					firstWood=findFirstResource(mob.location(),req1[i]);
				if(firstWood!=null) break;
			}
		}
		else
		if(req1Desc!=null)
			firstWood=fetchFoundOtherEncoded(mob,req1Desc);
		data[0][FOUND_AMT]=0;
		if(firstWood!=null)
		{
			data[0][FOUND_AMT]=findNumberOfResource(mob.location(),firstWood.material());
			data[0][FOUND_CODE]=firstWood.material();
		}

		if(req2!=null)
		{
			for(int i=0;i<req2.length;i++)
			{
				if((req2[i]&EnvResource.RESOURCE_MASK)==0)
					firstOther=findMostOfMaterial(mob.location(),req2[i]);
				else
					firstOther=findFirstResource(mob.location(),req2[i]);
				if(firstOther!=null) break;
			}
		}
		else
		if(req2Desc!=null)
			firstOther=fetchFoundOtherEncoded(mob,req2Desc);
		data[1][FOUND_AMT]=0;
		if(firstOther!=null)
		{
			data[1][FOUND_AMT]=findNumberOfResource(mob.location(),firstOther.material());
			data[1][FOUND_CODE]=firstOther.material();
		}
		if(req1Required>0)
		{
			if(data[0][FOUND_AMT]==0)
			{
				if(req1Desc!=null)
					commonTell(mob,"There is no "+req1Desc.toLowerCase()+" here to make anything from!  It might need to put it down first.");
				return null;
			}
			if(!bundle) req1Required=fixResourceRequirement(data[0][FOUND_CODE],req1Required);
		}
		if(req2Required>0)
		{
			if(((req2!=null)&&(data[1][FOUND_AMT]==0))
			||((req2==null)&&(req2Desc!=null)&&(req2Desc.length()>0)&&(data[1][FOUND_AMT]==0)))
			{
				if(req2Desc.equalsIgnoreCase("PRECIOUS"))
					commonTell(mob,"You need some sort of precious stones to make that.  There is not enough here.  Are you sure you set it all on the ground first?");
				else
					commonTell(mob,"You need some "+req2Desc.toLowerCase()+" to make that.  There is not enough here.  Are you sure you set it all on the ground first?");
				return null;
			}
			if(!bundle) req2Required=fixResourceRequirement(data[1][FOUND_CODE],req2Required);
		}

		if(req1Required>data[0][FOUND_AMT])
		{
			commonTell(mob,"You need "+req1Required+" pounds of "+EnvResource.RESOURCE_DESCS[(data[0][FOUND_CODE]&EnvResource.RESOURCE_MASK)].toLowerCase()+" to make that.  There is not enough here.  Are you sure you set it all on the ground first?");
			return null;
		}
		else
			data[0][FOUND_AMT]=req1Required;
		if((req2Required>0)&&(req2Required>data[1][FOUND_AMT]))
		{
			commonTell(mob,"You need "+req2Required+" pounds of "+EnvResource.RESOURCE_DESCS[(data[1][FOUND_CODE]&EnvResource.RESOURCE_MASK)].toLowerCase()+" to make that.  There is not enough here.  Are you sure you set it all on the ground first?");
			return null;
		}
		else
			data[1][FOUND_AMT]=req2Required;
		return data;
	}

	protected void randomRecipeFix(MOB mob, Vector recipes, Vector commands, int autoGeneration)
	{
		if(((mob.isMonster()&&(!Sense.isAnimalIntelligence(mob)))||(autoGeneration>0))
		&&(commands.size()==0)
		&&(recipes!=null)
		&&(recipes.size()>0))
		{
			Vector randomRecipe=(Vector)recipes.elementAt(Dice.roll(1,recipes.size(),-1));
			commands.addElement(randomRecipe.firstElement());
		}
	}

	protected Vector matchingRecipeNames(Vector recipes, String recipeName, boolean beLoose)
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
				if((replacePercent(item,"").toUpperCase().indexOf(recipeName.toUpperCase())>=0))
					matches.addElement(V);
			}
		}
		if(matches.size()>0) return matches;
		if(beLoose)
		{
			for(int r=0;r<recipes.size();r++)
			{
				Vector V=(Vector)recipes.elementAt(r);
				if(V.size()>0)
				{
					String item=(String)V.elementAt(0);
					if((recipeName.toUpperCase().indexOf(replacePercent(item,"").toUpperCase())>=0))
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
			&&(!Sense.enchanted(I))
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
		StringBuffer buf=new StringBuffer("");
		if(scanning==mob)
			buf.append("The following items could use some "+name()+":\n\r");
		else
			buf.append("The following items on "+scanning.name()+" could use some "+name()+":\n\r");
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

}
