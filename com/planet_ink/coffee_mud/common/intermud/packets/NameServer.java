package com.planet_ink.coffee_mud.i3.packets;
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
public class NameServer implements Serializable 
{
	public static final long serialVersionUID=0;
	
    protected String ip;
    protected String name;
    protected int    port;

    public NameServer(String addr, int p, String nom) {
        super();
        ip = addr;
        port = p;
        name = nom;
    }
}
