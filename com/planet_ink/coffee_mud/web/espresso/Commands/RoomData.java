package com.planet_ink.coffee_mud.web.espresso.Commands;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.web.espresso.*;
import java.util.*;

/**
 * <p>Title: False Realities Flavored CoffeeMUD</p>
 * <p>Description: The False Realities Version of CoffeeMUD</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: http://falserealities.game-host.org</p>
 * @author FR - Jeremy Vyska; CM - Bo Zimmerman
 * @version 1.0.0.0
 */

public class RoomData extends StdEspressoCommand {
  public String Name = "RoomData";
  public String ID() { return name(); }
  public String name() { return Name; }

  public RoomData() {
  }

  public static Vector mobs=new Vector();
public static Vector items=new Vector();

public static String getItemCode(Room R, Item I)
{
        if(I==null) return "";
        for(int i=0;i<R.numItems();i++)
                if(R.fetchItem(i)==I)
                        return new Long(new String(I.ID()+"/"+I.Name()+"/"+I.displayText()).hashCode()<<5).toString()+i;
        return "";
}

public static String getItemCode(Vector allitems, Item I)
{
        if(I==null) return "";
        for(int i=0;i<allitems.size();i++)
                if(allitems.elementAt(i)==I)
                        return new Long(new String(I.ID()+"/"+I.Name()+"/"+I.displayText()).hashCode()<<5).toString()+i;
        return "";
}

public static String getItemCode(MOB M, Item I)
{
        if(I==null) return "";
        for(int i=0;i<M.inventorySize();i++)
                if(M.fetchInventory(i)==I)
                        return new Long(new String(I.ID()+"/"+I.Name()+"/"+I.displayText()).hashCode()<<5).toString()+i;
        return "";
}

public static String getMOBCode(Room R, MOB M)
{
        if(M==null) return "";
        int code=0;
        for(int i=0;i<R.numInhabitants();i++)
        {
                MOB M2=R.fetchInhabitant(i);
                if(M==M2)
                        return new Long(new String(M.ID()+"/"+M.Name()+"/"+M.displayText()).hashCode()<<5).toString()+code;
                else
                if((M2!=null)&&(M2.isEligibleMonster()))
                        code++;
        }
        return "";
}

public static String getMOBCode(Vector mobs, MOB M)
{
        if(M==null) return "";
        for(int i=0;i<mobs.size();i++)
                if(mobs.elementAt(i)==M)
                        return new Long(new String(M.ID()+"/"+M.Name()+"/"+M.displayText()).hashCode()<<5).toString()+i;
        return "";
}

public static Item getItemFromCode(MOB M, String code)
{
        for(int i=0;i<M.inventorySize();i++)
                if(getItemCode(M,M.fetchInventory(i)).equals(code))
                        return M.fetchInventory(i);
        if(code.length()>2) code=code.substring(0,code.length()-2);
        for(int i=0;i<M.inventorySize();i++)
                if(getItemCode(M,M.fetchInventory(i)).startsWith(code))
                        return M.fetchInventory(i);
        return null;
}

public static Item getItemFromCode(Room R, String code)
{
        for(int i=0;i<R.numItems();i++)
                if(getItemCode(R,R.fetchItem(i)).equals(code))
                        return R.fetchItem(i);
        if(code.length()>2) code=code.substring(0,code.length()-2);
        for(int i=0;i<R.numItems();i++)
                if(getItemCode(R,R.fetchItem(i)).startsWith(code))
                        return R.fetchItem(i);
        return null;
}

public static Item getItemFromCode(Vector allitems, String code)
{
        for(int i=0;i<allitems.size();i++)
                if(getItemCode(allitems,(Item)allitems.elementAt(i)).equals(code))
                        return (Item)allitems.elementAt(i);
        if(code.length()>2) code=code.substring(0,code.length()-2);
        for(int i=0;i<allitems.size();i++)
                if(getItemCode(allitems,(Item)allitems.elementAt(i)).startsWith(code))
                        return (Item)allitems.elementAt(i);
        return null;
}

public static MOB getMOBFromCode(Room R, String code)
{
        for(int i=0;i<R.numInhabitants();i++)
                if(getMOBCode(R,R.fetchInhabitant(i)).equals(code))
                        return R.fetchInhabitant(i);
        if(code.length()>2) code=code.substring(0,code.length()-2);
        for(int i=0;i<R.numInhabitants();i++)
                if(getMOBCode(R,R.fetchInhabitant(i)).startsWith(code))
                        return R.fetchInhabitant(i);
        return null;
}

public static MOB getMOBFromCode(Vector allmobs, String code)
{
        for(int i=0;i<allmobs.size();i++)
                if(getMOBCode(allmobs,((MOB)allmobs.elementAt(i))).equals(code))
                        return ((MOB)allmobs.elementAt(i));
        if(code.length()>2) code=code.substring(0,code.length()-2);
        for(int i=0;i<allmobs.size();i++)
                if(getMOBCode(allmobs,((MOB)allmobs.elementAt(i))).startsWith(code))
                        return ((MOB)allmobs.elementAt(i));
        return null;
}

public static Item getItemFromAnywhere(Object allitems, String MATCHING)
{
        if(isAllNum(MATCHING))
        {
                if(allitems instanceof Room)
                        return getItemFromCode((Room)allitems,MATCHING);
                else
                if(allitems instanceof MOB)
                        return getItemFromCode((MOB)allitems,MATCHING);
                else
                if(allitems instanceof Vector)
                        return getItemFromCode((Vector)allitems,MATCHING);
        }
        else
        if(MATCHING.indexOf("@")>0)
        {
                for(int m=0;m<items.size();m++)
                {
                        Item I2=(Item)items.elementAt(m);
                        if(MATCHING.equals(""+I2))
                                return I2;
                }
        }
        else
        {
                for(Enumeration i=CMClass.items();i.hasMoreElements();)
                {
                        Item I=(Item)i.nextElement();
                        if(CMClass.className(I).equals(MATCHING))
                                return I;
                }
                for(Enumeration i=CMClass.armor();i.hasMoreElements();)
                {
                        Item I=(Item)i.nextElement();
                        if(CMClass.className(I).equals(MATCHING))
                                return I;
                }
                for(Enumeration i=CMClass.weapons();i.hasMoreElements();)
                {
                        Item I=(Item)i.nextElement();
                        if(CMClass.className(I).equals(MATCHING))
                                return I;
                }
                for(Enumeration i=CMClass.miscMagic();i.hasMoreElements();)
                {
                        Item I=(Item)i.nextElement();
                        if(CMClass.className(I).equals(MATCHING))
                                return I;
                }
        }
        return null;
}

public static Vector contributeMOBs(Vector inhabs)
{
        for(int i=0;i<inhabs.size();i++)
        {
                MOB M=(MOB)inhabs.elementAt(i);
                if(M.isGeneric())
                {
                        boolean found=false;
                        for(int m=0;m<mobs.size();m++)
                        {
                                MOB M2=(MOB)mobs.elementAt(m);
                                if(M.sameAs(M2))
                                {	found=true;	break;	}
                        }
                        if((!found)&&(M.isEligibleMonster()))
                        {
                                MOB M3=(MOB)M.copyOf();
                                mobs.addElement(M3);
                                for(int i3=0;i3<M3.inventorySize();i3++)
                                {
                                        Item I3=M3.fetchInventory(i3);
                                        if(I3!=null) I3.stopTicking();
                                }
                        }
                }
        }
        return mobs;
}

public static boolean isAllNum(String str)
{
        if(str.length()==0) return false;
        for(int c=0;c<str.length();c++)
                if((!Character.isDigit(str.charAt(c)))
                &&(str.charAt(c)!='-'))
                        return false;
        return true;
}

public static Vector contributeItems(Vector inhabs)
{
        for(int i=0;i<inhabs.size();i++)
        {
                Item I=(Item)inhabs.elementAt(i);
                if(I.isGeneric())
                {
                        boolean found=false;
                        for(int i2=0;i2<items.size();i2++)
                        {
                                Item I2=(Item)items.elementAt(i2);
                                if(I.sameAs(I2))
                                {	found=true;	break;	}
                        }
                        if(!found)
                        {
                                I.setContainer(null);
                                I.wearAt(Item.INVENTORY);
                                Item I2=(Item)I.copyOf();
                                items.addElement(I2);
                                I2.stopTicking();
                        }
                }
        }
        return items;
}


public Object run(Vector param, EspressoServer server)
{
        // Element 1 MUST always be the RoomID
        String last=safelyGetStr(param,1);
        if(last==null) return null;
        if(last.length()==0) return null;

        Room R=(Room)CMMap.getRoom(last);
        if(R==null)
        {
          return null;
        }
        CoffeeUtensils.resetRoom(R);
        if(safelyGetStr(param,2).equalsIgnoreCase("NAME"))
        {
                return R.displayText();
        }
        if(safelyGetStr(param,2).equalsIgnoreCase("DESCRIPTION"))
        {
                return R.description();
        }
        if(safelyGetStr(param,2).equalsIgnoreCase("LOCALE"))
        {
          return R.ID();
        }
        if((safelyGetStr(param,2).equalsIgnoreCase("XGRID"))&&(R instanceof GridLocale))
        {
          return new Integer(((GridLocale)R).xSize());
        }
        if((safelyGetStr(param,2).equalsIgnoreCase("YGRID"))&&(R instanceof GridLocale))
        {
          return new Integer(((GridLocale)R).ySize());
        }
        if(safelyGetStr(param,2).equalsIgnoreCase("ISGRID"))
        {
                return new Boolean(R instanceof GridLocale);
        }
        if(safelyGetStr(param,2).equalsIgnoreCase("MOBLIST"))
        {
                Vector classes=new Vector();
                Vector moblist=null;
                if(param.size()>3)
                {
                        moblist=mobs;
                        for(int i=3;i<param.size();i++)
                        {
                                String MATCHING=(String)param.elementAt(i);
                                if(MATCHING==null)
                                        break;
                                else
                                if(isAllNum(MATCHING))
                                {
                                        MOB M2=getMOBFromCode(R,MATCHING);
                                        if(M2!=null)
                                                classes.addElement(M2);
                                }
                                else
                                if(MATCHING.indexOf("@")>0)
                                {
                                        for(int m=0;m<moblist.size();m++)
                                        {
                                                MOB M2=(MOB)moblist.elementAt(m);
                                                if(MATCHING.equals(""+M2))
                                                {	classes.addElement(M2);	break;	}
                                        }
                                }
                                else
                                for(Enumeration m=CMClass.mobTypes();m.hasMoreElements();)
                                {
                                        MOB M2=(MOB)m.nextElement();
                                        if(CMClass.className(M2).equals(MATCHING)
                                           &&(!M2.isGeneric()))
                                        {	classes.addElement(M2.copyOf()); break;	}
                                }
                        }
                }
                else
                {
                        for(int m=0;m<R.numInhabitants();m++)
                        {
                                MOB M=R.fetchInhabitant(m);
                                if(M.isEligibleMonster())
                                        classes.addElement(M);
                        }
                        //moblist=contributeMOBs(classes);
                        moblist=classes;
                }
                Vector response=new Vector();
                for(int i=0;i<moblist.size();i++)
                {
                  MOB M=(MOB)moblist.elementAt(i);
                  // The client needs the mobs in vector trios:  [Name,Code,Class]
                  response.add(M.Name());
                  response.add(getMOBCode(R,M));
                  response.add(M.ID());
                }
                return response;
        }

        if(safelyGetStr(param,2).equalsIgnoreCase("ITEMLIST"))
        {
                Vector classes=new Vector();
                Vector itemlist=null;
                if(param.size()>3)
                {
                        itemlist=items;
                        for(int i=3;i<param.size();i++)
                        {
                                String MATCHING=(String)param.elementAt(i);
                                if(MATCHING==null)
                                        break;
                                else
                                {
                                        Item I2=getItemFromAnywhere(R,MATCHING);
                                        if(I2!=null)
                                                classes.addElement(I2);
                                }
                        }
                }
                else
                {
                        for(int m=0;m<R.numItems();m++)
                        {
                                Item I2=R.fetchItem(m);
                                classes.addElement(I2);
                        }
                        //itemlist=contributeItems(classes);
                }
                Vector response=new Vector();
                for(int i=0;i<classes.size();i++)
                {
                  Item I=(Item)classes.elementAt(i);
                  // The client needs the items in vector trios:  [Name,Code,Class]
                  response.add(I.Name());
                  response.add(getItemCode(R,I));
                  response.add(I.ID());
                }
                return response;
        }
        if(safelyGetStr(param,2).equalsIgnoreCase("BEHAVIORS"))
        {
          Vector theclasses=new Vector();
          for (int b = 0; b < R.numBehaviors(); b++) {
            Behavior B = R.fetchBehavior(b);
            if (B != null) {
              theclasses.addElement(CMClass.className(B));
            }
          }
          return theclasses;
        }
        if(safelyGetStr(param,2).equalsIgnoreCase("AFFECTS"))
        {
          Vector theclasses = new Vector();
          for (int a = 0; a < R.numEffects(); a++) {
            Ability Able = R.fetchEffect(a);
            if ( (Able != null) && (!Able.isBorrowed(R))) {
              theclasses.addElement(CMClass.className(Able));
            }
          }
          return theclasses;
        }
        return null;
}


}
