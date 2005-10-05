package com.planet_ink.coffee_mud.Items.Basic;

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
public class GenPackagedItems extends GenItem implements PackagedItems
{
    public String ID(){ return "GenPackagedItems";}
    protected String    readableText="";
    public GenPackagedItems()
    {
        super();
        setName("item");
        baseEnvStats.setWeight(150);
        setDisplayText("");
        setDescription("");
        baseGoldValue=5;
        baseEnvStats().setLevel(1);
        setMaterial(EnvResource.RESOURCE_MEAT);
        recoverEnvStats();
    }
    public String name(){return "a package of "+numberOfItemsInPackage()+" "+Name()+"(s)";}
    public String displayText(){return "a package of "+numberOfItemsInPackage()+" "+Name()+"(s) sit here.";}
    public int numberOfItemsInPackage(){return baseEnvStats().ability();}
    public void setNumberOfItemsInPackage(int number){baseEnvStats().setAbility(number);envStats().setAbility(number);}
    public boolean packageMe(Item I, int number)
    {
        if(I==null) return false;
        name=EnglishParser.cleanArticles(I.Name());
        displayText="";
        setDescription("The contents of the package appears as follows:\n\r"+I.description());
        baseEnvStats().setLevel(I.baseEnvStats().level());
        baseEnvStats().setWeight(I.baseEnvStats().weight()*number);
        baseEnvStats().setHeight(I.baseEnvStats().height());
        setMaterial(I.material());
        setBaseValue(I.baseGoldValue()*number);
        StringBuffer itemstr=new StringBuffer("");
        itemstr.append("<PAKITEM>");
        itemstr.append(XMLManager.convertXMLtoTag("PICLASS",CMClass.className(I)));
        itemstr.append(XMLManager.convertXMLtoTag("PIDATA",CoffeeMaker.getPropertiesStr(I,true)));
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
        Vector buf=XMLManager.parseAllXML(packageText());
        if(buf==null)
        {
            Log.errOut("Packaged","Error parsing 'PAKITEM'.");
            return null;
        }
        XMLManager.XMLpiece iblk=XMLManager.getPieceFromPieces(buf,"PAKITEM");
        if((iblk==null)||(iblk.contents==null))
        {
            Log.errOut("Packaged","Error parsing 'PAKITEM'.");
            return null;
        }
        String itemi=XMLManager.getValFromPieces(iblk.contents,"PICLASS");
        Environmental newOne=CMClass.getItem(itemi);
        Vector idat=XMLManager.getRealContentsFromPieces(iblk.contents,"PIDATA");
        if((idat==null)||(newOne==null)||(!(newOne instanceof Item)))
        {
            Log.errOut("Packaged","Error parsing 'PAKITEM' data.");
            return null;
        }
        CoffeeMaker.setPropertiesStr(newOne,idat,true);
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
            destroy();
        else
        {
            baseEnvStats().setWeight(itemWeight*number);
            setBaseValue(itemValue*number);
        }
        recoverEnvStats();
        return V;
    }
    public String packageText(){ return CoffeeMaker.restoreAngleBrackets(readableText());}
    public void setPackageText(String text)
    {
        setReadableText(CoffeeMaker.parseOutAngleBrackets(text));
        Sense.setReadable(this,false);
    }
}