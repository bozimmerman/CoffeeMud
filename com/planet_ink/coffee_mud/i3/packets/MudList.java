package com.planet_ink.coffee_mud.i3.packets;
import com.planet_ink.coffee_mud.i3.persist.Persistent;

import java.util.Hashtable;
import java.io.Serializable;

/**
 * Copyright (c) 1996 George Reese
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
public class MudList implements Serializable 
{
	public static final long serialVersionUID=0;
	
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

