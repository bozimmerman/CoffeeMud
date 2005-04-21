package com.planet_ink.coffee_mud.Behaviors;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

/**
 * <p>Title: False Realities Presents FieryRoom</p>
 * <p>Description: False Realities - Discover your true destiny and change history...</p>
 * <p>Company: http://www.falserealities.com</p>
 * @author Tulath (a.k.a.) Jeremy Vyska
 */
public class FieryRoom
    extends ActiveTicker {
    public String ID() {
        return "FieryRoom"; }

    protected int canImproveCode() {
        return Behavior.CAN_ROOMS; }

    public Behavior newInstance() {
        return new FieryRoom(); }

    private String newDisplay = "";
    private String newDesc = "";
    private int directDamage = 10;
    private int eqChance = 0;
    private int burnTicks = 12;
    private boolean noStop = false;
    private boolean noNpc = false;
    private boolean noFireText = false;

    private String[] FireTexts = {"The fire here crackles and burns."};

    public FieryRoom() {
        minTicks = 5; maxTicks = 10; chance = 100;
        tickReset();
    }

    public void setParms(String newParms) {
        super.setParms(newParms);
        newDisplay = Util.getParmStr(newParms, "Title", "A Charred Ruin");
        newDesc = Util.getParmStr(newParms, "Description", "Whatever was once here is now nothing more than ash.");
        directDamage = Util.getParmInt(newParms, "damage", 10);
        if (newParms.toUpperCase().indexOf("NOSTOP") > 0) noStop = true;
        if (newParms.toUpperCase().indexOf("NONPC") > 0) noNpc = true;
        if (newParms.toUpperCase().indexOf("NOFIRETEXT") > 0) noFireText = true;
        eqChance = Util.getParmInt(newParms, "eqchance", 0);
        burnTicks = Util.getParmInt(newParms, "burnticks", 12);
        setFireTexts();
    }

    private void setFireTexts() {
        String[] newFireTexts = {"The fire here crackles and burns.",
                                  "The intense heat of the fire here is "+(directDamage>0?"very painful":"very unpleasant")+".",
                                  "The flames dance around you"+(eqChance>0?", licking at your clothes.":"."),
                                  "The fire is burning out of control. You fear for your safety"+(noStop?".":" as it looks like this place is being completely consumed."),
                                  "You hear popping and sizzling as something burns.",
                                  "The smoke here is very thick and you worry about whether you will be able to breathe."};
        FireTexts = newFireTexts;
    }

    public boolean tick(Tickable ticking, int tickID) {
        super.tick(ticking, tickID);
        // on every tick, we may do damage OR eq handling.
        Room room = (Room) ticking;
        if ( (directDamage > 0) || (eqChance > 0)) {
            // for each inhab, do directDamage to them.
            for (int i = 0; i < room.numInhabitants(); i++) {
                MOB inhab = room.fetchInhabitant(i);
                if (inhab.isMonster()) {
                    boolean reallyAffect = true;
                    if (noNpc) {
                        reallyAffect = false;
                        HashSet group = inhab.getGroupMembers(new HashSet());
                        for (Iterator e = group.iterator(); e.hasNext(); ) {
                            MOB follower = (MOB) e.next();
                            if (! (follower.isMonster())) {
                                reallyAffect = true;
                                break;
                            }
                        }
                    }
                    if (reallyAffect) {
                        dealDamage(inhab);
                        if (Dice.rollPercentage() > eqChance)
                            eqRoast(inhab);
                    }
                }
                else {
                    if((!CMSecurity.isAllowed(inhab,inhab.location(),"ORDER"))
        		&&(!CMSecurity.isAllowed(inhab,inhab.location(),"CMDROOMS"))) {
                        dealDamage(inhab);
                        if (Dice.rollPercentage() > eqChance)
                            eqRoast(inhab);
                    }
                }
            }
        }
        if (canAct(ticking, tickID)) {
            if (ticking instanceof Room) {
                // % chance of burning each item in the room.
                roastRoom(room);
                // The tick happened.  If NOT NoFireText, Do flame emotes
                if(!noFireText) {
                    Room R = (Room) ticking;
                    String pickedText=FireTexts[Dice.roll(1,FireTexts.length,0)-1];
                    R.showHappens(CMMsg.MSG_OK_ACTION,pickedText);
                }
                if (!noStop) {
                    if(burnTicks==0) {
                        // NOSTOP is false.  This means the room gets set
                        // to the torched text and the behavior goes away.
                        room.setDisplayText(newDisplay);
                        room.setDescription(newDesc);
                        room.delBehavior(this);
                    }
                    else
                        --burnTicks;
                }
            }
        }
        return true;
    }

    private void dealDamage(MOB mob) {
        MUDFight.postDamage(mob, mob, null, directDamage, CMMsg.MASK_GENERAL | CMMsg.TYP_FIRE, Weapon.TYPE_BURNING,
                            "The fire here <DAMAGE> <T-NAME>!");
    }

    private void eqRoast(MOB mob) {
        Item target = getSomething(mob);
        if (target != null) {
            switch (target.material() & EnvResource.MATERIAL_MASK) {
                case EnvResource.MATERIAL_GLASS:
                case EnvResource.MATERIAL_METAL:
                case EnvResource.MATERIAL_MITHRIL:
                case EnvResource.MATERIAL_PLASTIC:
                case EnvResource.MATERIAL_PRECIOUS:
                case EnvResource.MATERIAL_ROCK:
                case EnvResource.MATERIAL_UNKNOWN: {
                    // all these we'll make get hot and be dropped.
                    int damage = Dice.roll(1, 6, 1);
                    MUDFight.postDamage(mob, mob, null, damage, CMMsg.MASK_GENERAL | CMMsg.TYP_FIRE, Weapon.TYPE_BURNING, target.name() + " <DAMAGE> <T-NAME>!");
                    if (Dice.rollPercentage() < mob.charStats().getStat(CharStats.STRENGTH)) {
                        CommonMsgs.drop(mob, target, false, false);
                    }
                    break;
                }
                default: {
                    Ability burn = CMClass.getAbility("Burning");
                    if (burn != null) {
                        mob.location().showHappens(CMMsg.MSG_OK_ACTION, target.Name() + " begins to burn!");
                        target.addEffect(burn);
                        target.recoverEnvStats();
                    }
                }
            }
        }
    }

    private static void roastRoom(Room which) {
      for(int i=0;i<which.numItems();i++) {
        Item target=which.fetchItem(i);
        Ability burn = CMClass.getAbility("Burning");
                    if((burn != null)&&(Dice.rollPercentage()>60)) {
                        which.showHappens(CMMsg.MSG_OK_ACTION, target.Name() + " begins to burn!");
                        target.addEffect(burn);
                        target.recoverEnvStats();
                    }
      }
    }

    private static Item getSomething(MOB mob) {
        Vector good = new Vector();
        Vector great = new Vector();
        Item target = null;
        for (int i = 0; i < mob.inventorySize(); i++) {
            Item I = mob.fetchInventory(i);
            if (I.amWearingAt(Item.INVENTORY))
                good.addElement(I);
            else
                great.addElement(I);
        }
        if (great.size() > 0)
            target = (Item) great.elementAt(Dice.roll(1, great.size(), -1));
        else
        if (good.size() > 0)
            target = (Item) good.elementAt(Dice.roll(1, good.size(), -1));
        return target;
    }
}
