package com.planet_ink.coffee_mud.Items.MiscMagic;
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


public class HerbChest extends BagOfHolding {
    public String ID(){	return "HerbChest";}
    public HerbChest() {
        super();
		setName("a small chest");
		setDisplayText("a small chest with many tiny drawers stands here.");
		setDescription("The most common magical item in the world, this carefully crafted chest is designed to help alchemists of the world carry their herbal supplies with them everywhere.");
		secretIdentity="An Alchemist's Herb Chest";
        setContainTypes(RawMaterial.RESOURCE_HERBS);
        capacity=500;
        baseGoldValue=0;
        material=RawMaterial.RESOURCE_REDWOOD;
        Ability A=CMClass.getAbility("Prop_HaveZapper");
        if(A!=null) {
            A.setMiscText("+SYSOP -MOB -anyclass +alchemist");
            addNonUninvokableEffect(A);
        }
    }
}
