package com.planet_ink.coffee_mud.Items.MiscMagic;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;

public class HerbChest extends BagOfHolding {
    public String ID(){	return "HerbChest";}
    public HerbChest() {
        super();
		setName("a small chest");
		setDisplayText("a small chest with many tiny drawers stands here.");
		setDescription("The most common magical item in the world, this carefully crafted chest is designed to help alchemists of the world carry their herbal supplies with them everywhere.");
		secretIdentity="An Alchemist's Herb Chest";
        setContainTypes(EnvResource.RESOURCE_HERBS);
        capacity=500;
        baseGoldValue=0;
        material=EnvResource.RESOURCE_REDWOOD;
        Ability A=CMClass.getAbility("Prop_HaveZapper");
        if(A!=null) {
            A.setMiscText("+SYSOP -MOB -anyclass +alchemist");
            addNonUninvokableEffect(A);
        }
    }
}
