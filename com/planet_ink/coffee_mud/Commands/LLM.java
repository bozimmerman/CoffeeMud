package com.planet_ink.coffee_mud.Commands;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.core.collections.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Libraries.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/*
   Copyright 2025-2025 Bo Zimmerman

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
public class LLM extends StdCommand
{
	public LLM()
	{
	}

	private final String[] access=I(new String[]{"LLM"});
	@Override
	public String[] getAccessWords()
	{
		return access;
	}

    private static final String OLLAMA_URL = "http://192.168.1.10:11434"; // Fedora server IP
    private static final String MODEL_NAME = "llama3"; // Matches your Ollama model
    private static final Double TEMPERATURE = Double.valueOf(0.8);
    private static final Long TIMEOUT_SECONDS = Long.valueOf(20);
    private static final Integer MAX_MSGS = Integer.valueOf(10);

    public interface Assistant {
        String chat(String message);
    }

    @Override
	public boolean execute(final MOB mob, final List<String> commands, final int metaFlags)
		throws java.io.IOException
	{
		try {
            // Load LangChain4j classes dynamically
            final Class<?> ollamaChatModelBuilderClass = Class.forName("dev.langchain4j.model.ollama.OllamaChatModel$OllamaChatModelBuilder");
            final Class<?> ollamaChatModelClass = Class.forName("dev.langchain4j.model.ollama.OllamaChatModel");
            final Class<?> durationClass = Class.forName("java.time.Duration");
            final Method durationOfSecondsMethod = durationClass.getMethod("ofSeconds", long.class);
            final Object timeout = durationOfSecondsMethod.invoke(null, TIMEOUT_SECONDS);
            final Method builderMethod = ollamaChatModelClass.getMethod("builder");
            final Object builder = builderMethod.invoke(null);
            final Method baseUrlMethod = ollamaChatModelBuilderClass.getMethod("baseUrl", String.class);
            final Method modelNameMethod = ollamaChatModelBuilderClass.getMethod("modelName", String.class);
            final Method temperatureMethod = ollamaChatModelBuilderClass.getMethod("temperature", Double.class);
            final Method timeoutMethod = ollamaChatModelBuilderClass.getMethod("timeout", durationClass);
            final Method buildMethod = ollamaChatModelBuilderClass.getMethod("build");
            baseUrlMethod.invoke(builder, OLLAMA_URL);
            modelNameMethod.invoke(builder, MODEL_NAME);
            temperatureMethod.invoke(builder, TEMPERATURE);
            timeoutMethod.invoke(builder, timeout);
            final Object chatModel = buildMethod.invoke(builder);

            // Set up chat memory
            final Class<?> messageWindowChatMemoryClass = Class.forName("dev.langchain4j.memory.chat.MessageWindowChatMemory");
            final Method chatMemBuilderMethod = messageWindowChatMemoryClass.getMethod("builder");
            final Object memBuilder = chatMemBuilderMethod.invoke(null);
            final Method maxMessagesMethod = memBuilder.getClass().getMethod("maxMessages", Integer.class);
            final Method chatMemBuildMethod = memBuilder.getClass().getMethod("build");
            maxMessagesMethod.invoke(memBuilder, MAX_MSGS);
            final Object memory = chatMemBuildMethod.invoke(memBuilder);

            // Set up AiServices
            final Class<?> aiServicesClass = Class.forName("dev.langchain4j.service.AiServices");
            final Method aiBuilderMethod = aiServicesClass.getMethod("builder", Class.class);
            final Object aiBuilder = aiBuilderMethod.invoke(null, Assistant.class);
            final Class<?> aiBuilderClass = aiBuilder.getClass();
            final Class<?> chatModelClass = Class.forName("dev.langchain4j.model.chat.ChatModel");
            final Class<?> chatMemoryClass = Class.forName("dev.langchain4j.memory.ChatMemory");
            final Method chatModelMethod = aiBuilderClass.getMethod("chatModel", chatModelClass);
            chatModelMethod.setAccessible(true);
            final Method chatMemoryMethod = aiBuilderClass.getMethod("chatMemory", chatMemoryClass);
            chatMemoryMethod.setAccessible(true);
            final Method aiBuildMethod = aiBuilderClass.getMethod("build");
            aiBuildMethod.setAccessible(true);
            chatModelMethod.invoke(aiBuilder, chatModel);
            chatMemoryMethod.invoke(aiBuilder, memory);
            final Object assistant = aiBuildMethod.invoke(aiBuilder);

            // Create user message
            final String userText = CMParms.combine(commands, 1);

            // Generate response using assistant
            final Method chatMethod = Assistant.class.getMethod("chat", String.class);
            final String responseText = (String) chatMethod.invoke(assistant, userText);
            mob.tell(responseText);
        } catch (final ClassNotFoundException e) {
        	mob.tell("LangChain4j classes not found: " + e.getMessage());
        } catch (final NoSuchMethodException e) {
        	mob.tell("Method not found: " + e.getMessage());
        } catch (final Exception e) {
        	Log.errOut(e);
            mob.tell("Error testing Ollama connection: " + e.getMessage());
        }
		return false;
	}

	@Override
	public boolean canBeOrdered()
	{
		return true;
	}

	@Override
	public boolean securityCheck(final MOB mob)
	{
		return (mob.playerStats()!=null) && (CMSecurity.isAllowed(mob,mob.location(),CMSecurity.SecFlag.LISTADMIN));
	}

}
