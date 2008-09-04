package com.planet_ink.coffee_mud.Libraries;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityMapper.AbilityMapping;
import com.planet_ink.coffee_mud.Libraries.interfaces.AbilityParameters.AbilityParmEditor;
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

/*
   Copyright 2000-2008 Bo Zimmerman

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
public class CMAbleParms extends StdLibrary implements AbilityParameters
{
    public String ID(){return "CMAbleParms";}
    private Vector defaultFields = new Vector();
    
    public CMAbleParms()
    {
        super();
        defaultFields = CMParms.makeVector(new Object[] {
            new AbilityParmEditorImpl("SPELL_ID","Spell",PARMTYPE_CHOICES) {
                public void createChoices() { createChoices(CMClass.abilities());}
            },
            new AbilityParmEditorImpl("RESOURCE_NAME","Resource",PARMTYPE_CHOICES) {
                public void createChoices() { createChoices(RawMaterial.RESOURCE_DESCS);}
            },
            new AbilityParmEditorImpl("ITEM_NAME","Item Final Name",PARMTYPE_STRING){public void createChoices() {}}, 
            new AbilityParmEditorImpl("ITEM_LEVEL","Lvl",PARMTYPE_NUMBER){public void createChoices() {}},
            new AbilityParmEditorImpl("BUILD_TIME_TICKS","Time",PARMTYPE_NUMBER){public void createChoices() {}},
            new AbilityParmEditorImpl("AMOUNT_MATERIAL_REQUIRED","Amt",PARMTYPE_NUMBER){public void createChoices() {}},
            new AbilityParmEditorImpl("ITEM_BASE_VALUE","Value",PARMTYPE_NUMBER){public void createChoices() {}},
            new AbilityParmEditorImpl("ITEM_CLASS_ID","Class ID",PARMTYPE_CHOICES) {
                public void createChoices() { 
                    Vector V  = new Vector();
                    V.addAll(CMParms.makeVector(CMClass.clanItems()));
                    V.addAll(CMParms.makeVector(CMClass.armor()));
                    V.addAll(CMParms.makeVector(CMClass.basicItems()));
                    V.addAll(CMParms.makeVector(CMClass.miscMagic()));
                    V.addAll(CMParms.makeVector(CMClass.miscTech()));
                    V.addAll(CMParms.makeVector(CMClass.weapons()));
                    Vector V2=new Vector();
                    Item I;
                    for(Enumeration e=V.elements();e.hasMoreElements();)
                    {
                        I=(Item)e.nextElement();
                        if(I.isGeneric())
                            V2.addElement(I);
                    }
                    createChoices(V2);
                }
            },
            new AbilityParmEditorImpl("CODED_WEAR_LOCATION","Wear Locs",PARMTYPE_SPECIAL) {
                public boolean appliesToClass(Object o) { return o instanceof Armor;}
                public void createChoices() {}
//TODO: finish
            },
            new AbilityParmEditorImpl("CONTAINER_CAPACITY","Cap.",PARMTYPE_NUMBER) {
                public boolean appliesToClass(Object o) { return o instanceof Container;}
                public void createChoices() {}
            },
            new AbilityParmEditorImpl("BASE_ARMOR_AMOUNT","Arm.",PARMTYPE_NUMBER) {
                public boolean appliesToClass(Object o) { return o instanceof Armor;}
                public void createChoices() {}
            },
            new AbilityParmEditorImpl("CONTAINER_TYPE","Typ.",PARMTYPE_MULTICHOICES) {
                public void createChoices() { createBinaryChoices(Container.CONTAIN_DESCS);}
                public boolean appliesToClass(Object o) { return o instanceof Container;}
            },
            new AbilityParmEditorImpl("CODED_SPELL_LIST","Spells",PARMTYPE_SPECIAL) {
                public void createChoices() {}
//TODO: finish
            },
            new AbilityParmEditorImpl("BASE_DAMAGE","Dmg.",PARMTYPE_NUMBER) {
                public boolean appliesToClass(Object o) { return o instanceof Weapon;}
                public void createChoices() {}
            },
            new AbilityParmEditorImpl("LID_LOCK","Lid.",PARMTYPE_CHOICES) {
                public boolean appliesToClass(Object o) { return o instanceof Container;}
                public void createChoices() { createChoices(new String[]{"","LID","LOCK"});}
            },
            new AbilityParmEditorImpl("STATUE","Statue",PARMTYPE_CHOICES) {
                public void createChoices() { createChoices(new String[]{"","STATUE"});}
            },
            new AbilityParmEditorImpl("RIDE_BASIS","Ride",PARMTYPE_CHOICES) {
                public boolean appliesToClass(Object o) { return o instanceof Rideable;}
                public void createChoices() { createChoices(new String[]{"","CHAIR","TABLE","LADDER","ENTER","BED"});}
            },
            new AbilityParmEditorImpl("LIQUID_CAPACITY","Liq.",PARMTYPE_NUMBER) {
                public boolean appliesToClass(Object o) { return o instanceof Drink;}
                public void createChoices() {}
            },
            new AbilityParmEditorImpl("WEAPON_CLASS","WClas",PARMTYPE_CHOICES) {
                public boolean appliesToClass(Object o) { return o instanceof Weapon;}
                public void createChoices() { createChoices(Weapon.CLASS_DESCS);}
            },
            new AbilityParmEditorImpl("SMOKE_FLAG","Smoke",PARMTYPE_CHOICES) {
                public boolean appliesToClass(Object o) { return o instanceof Light;}
                public void createChoices() { createChoices(new String[]{"","SMOKE"});}
            },
            new AbilityParmEditorImpl("WEAPON_HANDS_REQUIRED","Hand",PARMTYPE_NUMBER) {
                public boolean appliesToClass(Object o) { return o instanceof Weapon;}
                public void createChoices() {}
            },
            new AbilityParmEditorImpl("LIGHT_DURATION","Dur.",PARMTYPE_NUMBER) {
                public boolean appliesToClass(Object o) { return o instanceof Light;}
                public void createChoices() {}
            },
            new AbilityParmEditorImpl("CLAN_ITEM_CODENUMBER","Typ.",PARMTYPE_CHOICES) {
                public boolean appliesToClass(Object o) { return o instanceof ClanItem;}
                public void createChoices() { createNumberedChoices(ClanItem.CI_DESC);}
            },
            new AbilityParmEditorImpl("CLAN_EXPERIENCE_COST","Exp",PARMTYPE_NUMBER) {public void createChoices() {}},
            new AbilityParmEditorImpl("CLAN_AREA_FLAG","Area",PARMTYPE_CHOICES) {
                public void createChoices() { createChoices(new String[]{"","AREA"});}
            },
            new AbilityParmEditorImpl("READABLE_TEXT","Read",PARMTYPE_STRINGORNULL) {public void createChoices() {}},
            new AbilityParmEditorImpl("REQUIRED_COMMON_SKILL_ID","Common Skill",PARMTYPE_CHOICES) {
                public void createChoices() {
                    Vector V  = new Vector();
                    Ability A = null;
                    for(Enumeration e=V.elements();e.hasMoreElements();)
                    {
                        A=(Ability)e.nextElement();
                        if((A.classificationCode() & Ability.ALL_ACODES) == Ability.ACODE_COMMON_SKILL)
                            V.addElement(A);
                    }
                    createChoices(V);
                }
            },
            new AbilityParmEditorImpl("FOOD_DRINK","Type",PARMTYPE_CHOICES) {
                public void createChoices() { createChoices(new String[]{"","FOOD","DRINK"});}
            },
            new AbilityParmEditorImpl("SMELL_LIST","Smells",PARMTYPE_SPECIAL) {
//TODO:
                public void createChoices() {}
                public boolean appliesToClass(Object o) { return o instanceof Perfume;}
            },
            new AbilityParmEditorImpl("RESOURCE_OR_KEYWORD","Resource",PARMTYPE_SPECIAL) {
//TODO:
                public void createChoices() {}
            },
            new AbilityParmEditorImpl("AMMO_TYPE","Ammo",PARMTYPE_STRING) {
                public void createChoices() {}
                public boolean appliesToClass(Object o) { return o instanceof Weapon;}
            },
            new AbilityParmEditorImpl("AMMO_CAPACITY","Ammo#",PARMTYPE_NUMBER) {
                public void createChoices() {}
                public boolean appliesToClass(Object o) { return o instanceof Weapon;}
            },
            new AbilityParmEditorImpl("MAXIMUM_RANGE","Max",PARMTYPE_NUMBER) { public void createChoices() {} },
            new AbilityParmEditorImpl("RESOURCE_OR_MATERIAL","Rsc/Mat",PARMTYPE_CHOICES) {
                public void createChoices() {
                    Vector V=CMParms.makeVector(RawMaterial.RESOURCE_DESCS);
                    V.addAll(CMParms.makeVector(RawMaterial.MATERIAL_DESCS));
                    createChoices(V);
                }
            },
            new AbilityParmEditorImpl("HERB_NAME$","Herb Final Name",PARMTYPE_STRING) {public void createChoices() {}},
            new AbilityParmEditorImpl("RIDE_CAPACITY","Ridrs",PARMTYPE_NUMBER) {
                public void createChoices() {}
                public boolean appliesToClass(Object o) { return o instanceof Rideable;}
            },
            new AbilityParmEditorImpl("METAL_OR_WOOD","Metal",PARMTYPE_CHOICES) {
                public void createChoices() { createChoices(new String[]{"METAL","WOOD"});}
            },
            new AbilityParmEditorImpl("OPTIONAL_RACE_ID","Race",PARMTYPE_CHOICES) {
                public void createChoices() { 
                    createChoices(CMClass.races());
                    choices().addElement("","");
                }
            },
            new AbilityParmEditorImpl("INSTRUMENT_TYPE","Instrmnt",PARMTYPE_CHOICES) {
                public void createChoices() { createChoices(MusicalInstrument.TYPE_DESC); }
                public boolean appliesToClass(Object o) { return o instanceof MusicalInstrument;}
            },
            new AbilityParmEditorImpl("STONE_FLAG","Stone",PARMTYPE_CHOICES) {
                public void createChoices() { createChoices(new String[]{"","STONE"});}
            },
            new AbilityParmEditorImpl("POSE_NAME","Pose Word",PARMTYPE_ONEWORD) {public void createChoices() {}},
            new AbilityParmEditorImpl("POSE_DESCRIPTION","Pose Description",PARMTYPE_STRING) {public void createChoices() {}},
            new AbilityParmEditorImpl("WOOD_METAL_CLOTH","",PARMTYPE_CHOICES) {
                public void createChoices() { createChoices(new String[]{"WOOD","METAL","CLOTH"});}
            },
            new AbilityParmEditorImpl("WEAPON_TYPE","W.Type",PARMTYPE_CHOICES) {
                public boolean appliesToClass(Object o) { return o instanceof Weapon;}
                public void createChoices() { createChoices(Weapon.TYPE_DESCS);}
            },
            new AbilityParmEditorImpl("ATTACK_MODIFICATION","Att.",PARMTYPE_NUMBER) {
                public void createChoices() {}
                public boolean appliesToClass(Object o) { return o instanceof Weapon;}
            }
        });
    };
    
    public abstract class AbilityParmEditorImpl implements AbilityParmEditor 
    {
        private String ID;
        private DVector choices = null;
        private int fieldType;
        private String prompt = null;
        private String header = null;
        
        public AbilityParmEditorImpl(String fieldName, String shortHeader, int type) {
            ID=fieldName; 
            fieldType = type;
            header = shortHeader;
            prompt = CMStrings.capitalizeAndLower(CMStrings.replaceAll(ID,"_"," "));
            createChoices();
        }
        public String ID(){ return ID;}
        public int parmType(){ return fieldType;}
        public String prompt() { return prompt; }
        public String colHeader() { return header;}
        
        public String commandLinePrompt(MOB mob, String oldVal, int[] showNumber, int showFlag)
            throws java.io.IOException
        {
            String str = null;
            boolean emptyOK = false;
            boolean spaceOK = fieldType != PARMTYPE_ONEWORD;
            switch(fieldType) {
                case PARMTYPE_STRINGORNULL:
                    emptyOK = true;
                case PARMTYPE_ONEWORD:
                case PARMTYPE_STRING:
                {
                    ++showNumber[0];
                    boolean proceed = true;
                    while(proceed) {
                        str = CMLib.genEd().prompt(mob,oldVal,showNumber[0],showFlag,prompt(),emptyOK).trim();
                        if((!spaceOK) && (str.indexOf(' ') >= 0))
                            mob.tell("Spaces are not allowed here.");
                        else
                            proceed=false;
                    }
                    break;
                }
                case PARMTYPE_NUMBER:
                    str = Integer.toString(CMLib.genEd().prompt(mob,Integer.parseInt(oldVal),++showNumber[0],showFlag,prompt()));
                    break;
                case PARMTYPE_CHOICES:
                    str = CMLib.genEd().prompt(mob,oldVal,++showNumber[0],showFlag,prompt(),choices);
                    break;
                case PARMTYPE_MULTICHOICES:
                    str = Integer.toString(CMLib.genEd().promptMulti(mob,Integer.parseInt(oldVal),++showNumber[0],showFlag,prompt(),choices));
                    break;
            }
            return str;
        }
        
        public abstract void createChoices(); 
        public DVector createChoices(Enumeration e) {
            if(choices != null) return choices;
            choices = new DVector(2);
            Object o = null;
            for(;e.hasMoreElements();) {
                o = e.nextElement();
                if(o instanceof String)
                    choices.addElement(o,CMStrings.capitalizeAndLower((String)o));
                else
                if(o instanceof Ability)
                    choices.addElement(((Ability)o).ID(),((Ability)o).name());
                else
                if(o instanceof Race)
                    choices.addElement(((Race)o).ID(),((Race)o).name());
                else
                if(o instanceof Environmental)
                    choices.addElement(((Environmental)o).ID(),((Environmental)o).ID());
            }
            return choices;
        }
        public DVector createChoices(Vector V) { return createChoices(V.elements());}
        public DVector createChoices(String[] S) { return createChoices(CMParms.makeVector(S).elements());}
        public DVector createBinaryChoices(String[] S) { 
            if(choices != null) return choices;
            choices = createChoices(CMParms.makeVector(S).elements());
            for(int i=0;i<choices.size();i++)
                if(i==0)
                    choices.setElementAt(i,1,Integer.toString(0));
                else
                    choices.setElementAt(i,1,Integer.toString(1<<(i-1)));
            return choices;
        }
        public DVector createNumberedChoices(String[] S) { 
            if(choices != null) return choices;
            choices = createChoices(CMParms.makeVector(S).elements());
            for(int i=0;i<choices.size();i++)
                choices.setElementAt(i,1,Integer.toString(i));
            return choices;
        }
        public DVector choices() { return choices; } 
        public boolean appliesToClass(Object o) { return o instanceof Item;}
    }
}
