package com.planet_ink.coffee_mud.web.macros;
import java.util.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;


public class TimsItemTable extends StdWebMacro
{
	public String name(){return this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);}
	public boolean isAdminMacro()	{return true;}

	
	public String runMacro(ExternalHTTPRequests httpReq, String parm)
	{
		long endTime=System.currentTimeMillis()+(1000*60*5);
		int min=Util.s_int((httpReq.getRequestParameter("MIN")));
		if(min>0)
			endTime=System.currentTimeMillis()+(1000*60*((long)min));
		
		StringBuffer str=new StringBuffer("<TABLE WIDTH=100% BORDER=1>");
		str.append("<TR><TD>Name</TD><TD>LVL</TD><TD>TVLV</TD><TD>ARM</TD><TD>ATT</TD><TD>DAM</TD><TD>ADJ</TD><TD>CAST</TD><TD>RESIST</TD></TR>");
		Vector onesDone=new Vector();
		for(Enumeration e=CMMap.areas();e.hasMoreElements();)
		{
			Area A=(Area)e.nextElement();
			for(Enumeration r=A.getMap();r.hasMoreElements();)
			{
				Room R=(Room)r.nextElement();
				if((endTime>0)&&(System.currentTimeMillis()>endTime))
					break;
				for(int i=0;i<R.numItems();i++)
				{
					Item I=R.fetchItem(i);
					if((endTime>0)&&(System.currentTimeMillis()>endTime))
						break;
					if(!doneBefore(onesDone,I)) str.append(addRow(I));
				}
				if((endTime>0)&&(System.currentTimeMillis()>endTime))
					break;
				for(int m=0;m<R.numInhabitants();m++)
				{
					if((endTime>0)&&(System.currentTimeMillis()>endTime))
						break;
					MOB M=R.fetchInhabitant(m);
					if(M==null) continue;
					for(int i=0;i<M.inventorySize();i++)
					{
						Item I=M.fetchInventory(i);
						if((endTime>0)&&(System.currentTimeMillis()>endTime))
							break;
						if(!doneBefore(onesDone,I)) str.append(addRow(I));
					}
					if((endTime>0)&&(System.currentTimeMillis()>endTime))
						break;
					if(!(M instanceof ShopKeeper)) continue;
					ShopKeeper S=(ShopKeeper)M;
					Vector V2=S.getUniqueStoreInventory();
					for(int v=0;v<V2.size();v++)
					{
						if((endTime>0)&&(System.currentTimeMillis()>endTime))
							break;
						if((V2.elementAt(v) instanceof Item)
						&&(!doneBefore(onesDone,(Item)V2.elementAt(v))))
							str.append(addRow((Item)V2.elementAt(v)));
					}
				}
			}
		}
		return str.toString()+"</TABLE>";
	}
	
	public boolean doneBefore(Vector V, Item I)
	{
		if(I==null) return true;
		if((!(I instanceof Armor))&&(!(I instanceof Weapon)))
			return true;
		if(I.displayText().length()==0)
			return true;
		for(int i=0;i<V.size();i++)
			if(I.sameAs((Environmental)V.elementAt(i)))
				return true;
		V.addElement(I);
		return false;
	}
	
	public String addRow(Item I)
	{
		StringBuffer row=new StringBuffer("");
		Ability ADJ=I.fetchEffect("Prop_WearAdjuster");
		if(ADJ==null) ADJ=I.fetchEffect("Prop_HaveAdjuster");
		if(ADJ==null) ADJ=I.fetchEffect("Prop_RideAdjuster");
		Ability RES=I.fetchEffect("Prop_WearResister");
		if(RES==null) RES=I.fetchEffect("Prop_HaveResister");
		Ability CAST=I.fetchEffect("Prop_WearSpellCast");
		int castMul=1;
		if(CAST==null) CAST=I.fetchEffect("Prop_UseSpellCast");
		if(CAST==null) CAST=I.fetchEffect("Prop_UseSpellCast2");
		if(CAST==null) CAST=I.fetchEffect("Prop_HaveSpellCast");
		if(CAST==null){ CAST=I.fetchEffect("Prop_FightSpellCast"); castMul=-1;}
		row.append("<TR>");
		row.append("<TD>"+I.name()+"</TD>");
		row.append("<TD>"+I.baseEnvStats().level()+"</TD>");
		row.append("<TD>"+timsLevelCalculator(I,ADJ,RES,CAST,castMul)+"</TD>");
		if(!(I instanceof Weapon))
			row.append("<TD>"+I.baseEnvStats().armor()+"</TD><TD>&nbsp;</TD><TD>&nbsp;</TD>");
		else
		{
			row.append("<TD>&nbsp;</TD><TD>"+I.baseEnvStats().attackAdjustment()+"</TD>");
			row.append("<TD>"+I.baseEnvStats().damage()+"</TD>");
		}
		if(ADJ!=null) row.append("<TD>"+ADJ.text()+"</TD>");
		else row.append("<TD>&nbsp;</TD>");
		if(CAST!=null) row.append("<TD>"+CAST.text()+"</TD>");
		else row.append("<TD>&nbsp;</TD>");
		if(RES!=null) row.append("<TD>"+RES.text()+"</TD>");
		else row.append("<TD>&nbsp;</TD>");
		row.append("</TR>");
		return row.toString();
	}
	
	public static int timsLevelCalculator(Item I,
										  Ability ADJ,
										  Ability RES,
										  Ability CAST,
										  int castMul)
	{
		Hashtable vals=null;
		int level=0;
		Item savedI=(Item)I.copyOf();
		savedI.recoverEnvStats();
		int otherDam=0;
		int otherAtt=0;
		int otherArm=0;
		if(ADJ!=null)
		{
			otherArm=Util.getParmPlus(ADJ.text(),"arm")*-1;
			otherAtt=Util.getParmPlus(ADJ.text(),"att");
			otherDam=Util.getParmPlus(ADJ.text(),"dam");
		}
		int curArmor=savedI.baseEnvStats().armor()+otherArm;
		int curFight=(savedI.baseEnvStats().damage()+savedI.baseEnvStats().attackAdjustment()+otherDam+otherAtt)/2;
		if(I instanceof Weapon)
		{
			int lastFight=Integer.MAX_VALUE;
			int newFight=Integer.MIN_VALUE;
			while(level<500)
			{
				vals=CoffeeMaker.timsItemAdjustments(I,
													level,
													I.material(),
													I.baseEnvStats().weight(),
													I.rawLogicalAnd()?2:1,
													((Weapon)I).weaponClassification(),
													I.maxRange(),
													0);
				int newDam=Util.s_int((String)vals.get("DAMAGE"));
				int newAtt=Util.s_int((String)vals.get("ATTACK"));
				lastFight=newFight;
				newFight=((newDam+newAtt)/2);
				if(curFight==((newDam+newAtt)/2))
					break;
				else
				if((newFight>curFight)
				&&(lastFight<curFight))
				{
					level--;
					break;
				}
				level++;
			}
		}
		else
		{
			int lastArm=Integer.MAX_VALUE;
			int newArm=Integer.MIN_VALUE;
			while(level<500)
			{
				vals=CoffeeMaker.timsItemAdjustments(I,
													level,
													I.material(),
													I.baseEnvStats().weight(),
													I.rawLogicalAnd()?2:1,
													0,
													0,
													I.rawProperLocationBitmap());
				lastArm=newArm;
				newArm=Util.s_int((String)vals.get("ARMOR"));
				if(newArm==curArmor)
					break;
				else
				if((newArm>curArmor)
				&&(lastArm<curArmor))
				{
					level--;
					break;
				}
				level++;
			}
		}
		level+=I.baseEnvStats().ability()*5;
		if(CAST!=null)
		{
			String ID=CAST.ID().toUpperCase();
			Vector theSpells=new Vector();
			String names=CAST.text();
			int del=names.indexOf(";");
			while(del>=0)
			{
				String thisOne=names.substring(0,del);
				Ability A=(Ability)CMClass.getAbility(thisOne);
				if(A!=null)	theSpells.addElement(A);
				names=names.substring(del+1);
				del=names.indexOf(";");
			}
			Ability A=(Ability)CMClass.getAbility(names);
			if(A!=null) theSpells.addElement(A);
			for(int v=0;v<theSpells.size();v++)
			{
				A=(Ability)theSpells.elementAt(v);
				int mul=1;
				if(A.quality()==Ability.MALICIOUS) mul=-1;
				if(ID.indexOf("HAVE")>=0)
					level+=(mul*CMAble.lowestQualifyingLevel(A.ID()));
				else
					level+=(mul*CMAble.lowestQualifyingLevel(A.ID())/2);
			}
		}
		if(ADJ!=null)
		{
			String newText=ADJ.text();
			int ab=Util.getParmPlus(newText,"abi");
			int arm=Util.getParmPlus(newText,"arm")*-1;
			int att=Util.getParmPlus(newText,"att");
			int dam=Util.getParmPlus(newText,"dam");
			if(I instanceof Weapon)
			{
				att=0; dam=0;
			}
			else
				arm=0;
			level+=(arm*3);
			level+=(att/2);
			level+=(dam*3);
			level+=ab*5;
			
			
			int dis=Util.getParmPlus(newText,"dis");
			if(dis!=0) level+=5;
			int sen=Util.getParmPlus(newText,"sen");
			if(sen!=0) level+=5;
			level+=(int)Math.round(5.0*Util.getParmDoublePlus(newText,"spe"));
			for(int i=0;i<CharStats.NUM_BASE_STATS;i++)
			{
				int stat=Util.getParmPlus(newText,CharStats.TRAITS[i].substring(0,3).toLowerCase());
				int max=Util.getParmPlus(newText,("max"+(CharStats.TRAITS[i].substring(0,3).toLowerCase())));
				level+=(stat*5);
				level+=(max*5);
			}

			int hit=Util.getParmPlus(newText,"hit");
			int man=Util.getParmPlus(newText,"man");
			int mv=Util.getParmPlus(newText,"mov");
			level+=(hit/5);
			level+=(man/5);
			level+=(mv/5);
		}
		
		return level;
	}
	
}
