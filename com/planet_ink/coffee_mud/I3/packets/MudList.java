package com.planet_ink.coffee_mud.system.I3.packets;
import com.planet_ink.coffee_mud.system.I3.persist.Persistent;

import java.util.Hashtable;
import java.io.Serializable;

public class MudList implements Serializable {
    private int id;
    private Hashtable list;
    private int modified;

    public MudList() {
        super();
        id = -1;
        modified = Persistent.MODIFIED;
        list = new Hashtable();
    }

    public MudList(int i) {
        this();
        id = i;
    }

    public int getModified() {
        return modified;
    }

    public void setModified(int x) {
        modified = x;
    }

    public void addMud(Mud mud) {
        if( mud.mud_name == null ) {
            return;
        }
        { // temp hack
            char c = mud.mud_name.charAt(0);

            if( !(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z') && c != '(' ) {
                return;
            }
        }
        if( list.containsKey(mud.mud_name) ) {
            mud.modified = Persistent.MODIFIED;
        }
        else {
            mud.modified = Persistent.NEW;
        }
        list.put(mud.mud_name, mud);
        modified = Persistent.MODIFIED;
    }

    public Mud getMud(String mud) {
        if( !list.containsKey(mud) ) {
            return null;
        }
        else {
            Mud tmp = (Mud)list.get(mud);

            if( tmp.modified == Persistent.DELETED ) {
                return null;
            }
            else {
                return tmp;
            }
        }
    }

    public void removeMud(Mud mud) {
        if( mud.mud_name == null ) {
            return;
        }
        mud.modified = Persistent.DELETED;
        modified = Persistent.MODIFIED;
    }

    public int getMudListId() {
        return id;
    }

    public void setMudListId(int x) {
        id = x;
    }

    public Hashtable getMuds() {
        return list;
    }
}

