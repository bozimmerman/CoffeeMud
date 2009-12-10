package com.planet_ink.coffee_mud.Items.Basic;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;


import java.util.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;

/* 
   Copyright 2000-2010 Bo Zimmerman

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
@SuppressWarnings("unchecked")
public class GenPackagedItems extends GenItem implements PackagedItems
{
    public String ID(){ return "GenPackagedItems";}
    public GenPackagedItems()
    {
        super();
        setName("item");
        baseEnvStats.setWeight(150);
        setDisplayText("");
        setDescription("");
        baseGoldValue=5;
        baseEnvStats().setLevel(1);
        setMaterial(RawMaterial.RESOURCE_MEAT);
        recoverEnvStats();
    }
    protected boolean abilityImbuesMagic(){return false;}
    public String name(){return "a package of "+numberOfItemsInPackage()+" "+Name().trim()+"(s)";}
    public String displayText(){return "a package of "+numberOfItemsInPackage()+" "+Name().trim()+"(s) sit here.";}
    public int numberOfItemsInPackage(){return baseEnvStats().ability();}
    public void setNumberOfItemsInPackage(int number){baseEnvStats().setAbility(number);envStats().setAbility(number);}
    protected byte[]    readableText=null;
    public String readableText(){return readableText==null?"":CMLib.encoder().decompressString(readableText);}
    public void setReadableText(String text){readableText=(text.trim().length()==0)?null:CMLib.encoder().compressString(text);}
    public boolean packageMe(Item I, int number)
    {
        if(I==null) return false;
        name=CMLib.english().cleanArticles(I.Name());
        displayText="";
        setDescription("The contents of the package appears as follows:\n\r"+I.description());
        baseEnvStats().setLevel(I.baseEnvStats().level());
        baseEnvStats().setWeight(I.baseEnvStats().weight()*number);
        baseEnvStats().setHeight(I.baseEnvStats().height());
        setMaterial(I.material());
        setBaseValue(I.baseGoldValue()*number);
        StringBuffer itemstr=new StringBuffer("");
        itemstr.append("<PAKITEM>");
        itemstr.append(CMLib.xml().convertXMLtoTag("PICLASS",CMClass.classID(I)));
        itemstr.append(CMLib.xml().convertXMLtoTag("PIDATA",CMLib.coffeeMaker().getPropertiesStr(I,true)));
        itemstr.append("</PAKITEM>");
        setPackageText(itemstr.toString());
        setNumberOfItemsInPackage(number);
        recoverEnvStats();
        return true;
    }
    
    public boolean isPackagable(Vector V)
    {
        if(V==null) return false;
        if(V.size()==0) return false;
        for(int v1=0;v1<V.size();v1++)
        {
            Item I=(Item)V.elementAt(v1);
            for(int v2=v1+1;v2<V.size();v2++)
                if(!((Item)V.elementAt(v2)).sameAs(I))
                    return false;
        }
        return true;
    }
    
    public Item getItem()
    {
        if(packageText().length()==0) return null;
        Vector buf=CMLib.xml().parseAllXML(packageText());
        if(buf==null)
        {
            Log.errOut("Packaged","Error parsing 'PAKITEM'.");
            return null;
        }
        XMLLibrary.XMLpiece iblk=CMLib.xml().getPieceFromPieces(buf,"PAKITEM");
        if((iblk==null)||(iblk.contents==null))
        {
            Log.errOut("Packaged","Error parsing 'PAKITEM'.");
            return null;
        }
        String itemi=CMLib.xml().getValFromPieces(iblk.contents,"PICLASS");
        Environmental newOne=CMClass.getItem(itemi);
        Vector idat=CMLib.xml().getRealContentsFromPieces(iblk.contents,"PIDATA");
        if((idat==null)||(newOne==null)||(!(newOne instanceof Item)))
        {
            Log.errOut("Packaged","Error parsing 'PAKITEM' data.");
            return null;
        }
        CMLib.coffeeMaker().setPropertiesStr(newOne,idat,true);
        return (Item)newOne;
    }

    public Vector unPackage(int number)
    {
        Vector V=new Vector();
        if(number>=numberOfItemsInPackage())
            number=numberOfItemsInPackage();
        if(number<=0)
            return V;
        int itemWeight=baseEnvStats().weight()/numberOfItemsInPackage();
        int itemValue=baseGoldValue()/numberOfItemsInPackage();
        Item I=getItem();
        if(I==null) return V;
        I.recoverEnvStats();
        for(int i=0;i<number;i++)
            V.addElement(I.copyOf());
        setNumberOfItemsInPackage(numberOfItemsInPackage()-number);
        if(numberOfItemsInPackage()<=0)
        {
            destroy();
           return V;
        }
        baseEnvStats().setWeight(itemWeight*number);
        setBaseValue(itemValue*number);
        recoverEnvStats();
        return V;
    }
    public String packageText()
    { 
        return CMLib.xml().restoreAngleBrackets(readableText());
    }
    public void setPackageText(String text)
    {
        setReadableText(CMLib.xml().parseOutAngleBrackets(text));
        CMLib.flags().setReadable(this,false);
    }
}