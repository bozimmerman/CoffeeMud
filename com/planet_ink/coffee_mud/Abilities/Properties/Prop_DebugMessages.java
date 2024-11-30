package com.planet_ink.coffee_mud.Abilities.Properties;

import com.planet_ink.coffee_mud.Abilities.interfaces.Ability;
import com.planet_ink.coffee_mud.Common.interfaces.CMMsg;
import com.planet_ink.coffee_mud.core.interfaces.Environmental;

/*
   Copyright 2024 github.com/toasted323

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

public
class Prop_DebugMessages extends LoggableProperty {
	@Override
	public String ID() {
		return "Prop_DebugMessages";
	}

	@Override
	public String name() {
		return "Room Debug Messages";
	}

	@Override
	protected int canAffectCode() {
		return Ability.CAN_ROOMS;
	}

	@Override
	public String accountForYourself() {
		return "This is a property to debug messages passing through rooms.";
	}

	@Override
	protected void handleParsedConfiguration() {
		super.handleParsedConfiguration();
		logger.logInfo("Configuration parsed");
	}

	@Override
	public boolean okMessage(final Environmental myHost, final CMMsg msg) {
		logger.logDebug("Received okMessage: " + describeMessage(msg));
		return super.okMessage(myHost, msg);
	}

	@Override
	public void executeMsg(final Environmental myHost, final CMMsg msg) {
		logger.logDebug("Executing message: " + describeMessage(msg));
		super.executeMsg(myHost, msg);
	}

	private String describeMessage(CMMsg msg) {
		StringBuilder sb = new StringBuilder();
		sb.append("Debug: Message Details:\n");

		sb.append("Source: ").append(describeMsgComponent(msg.source())).append("\n");
		sb.append("Target: ").append(describeMsgComponent(msg.target())).append("\n");
		sb.append("Tool: ").append(describeMsgComponent(msg.tool())).append("\n");

		sb.append("Source Message: ").append(describeStringField(msg.sourceMessage())).append("\n");
		sb.append("Target Message: ").append(describeStringField(msg.targetMessage())).append("\n");
		sb.append("Others Message: ").append(describeStringField(msg.othersMessage())).append("\n");

		sb.append("Source Minor: ").append(describeMsgType(msg.sourceMinor())).append("\n");
		sb.append("Target Minor: ").append(describeMsgType(msg.targetMinor())).append("\n");
		sb.append("Others Minor: ").append(describeMsgType(msg.othersMinor())).append("\n");

		sb.append("Source Major: ").append(describeMsgType(msg.sourceMajor())).append("\n");
		sb.append("Target Major: ").append(describeMsgType(msg.targetMajor())).append("\n");
		sb.append("Others Major: ").append(describeMsgType(msg.othersMajor())).append("\n");

		sb.append("Source Code (flags): ").append(Integer.toBinaryString(msg.sourceCode())).append(" (Decimal: ").append(msg.sourceCode()).append(")\n");
		sb.append("Target Code (flags): ").append(Integer.toBinaryString(msg.targetCode())).append(" (Decimal: ").append(msg.targetCode()).append(")\n");
		sb.append("Others Code (flags): ").append(Integer.toBinaryString(msg.othersCode())).append(" (Decimal: ").append(msg.othersCode()).append(")\n");

		return sb.toString();
	}


	private String describeMsgComponent(Environmental component) {
		if (component == null) return "None";
		return component.name() + " (ID: " + component.ID() + ", Type: " + component.getClass().getSimpleName() + ")";
	}

	private String describeStringField(String field) {
		if (field == null) return "None";
		if (field.isEmpty()) return "Empty String";
		return "\"" + field + "\"";
	}

	private String describeMsgType(int type) {
		if (type >= 0 && type < CMMsg.TYPE_DESCS.length) {
			String typeName = CMMsg.TYPE_DESCS[type];
			return typeName + " (" + type + ")";
		} else {
			return "Unknown Type (" + type + ")";
		}
	}
}