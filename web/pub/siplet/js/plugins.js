var PLUGINS = function(sipwin)
{
	this.plugins = [];
	this.framesMap = {};
	this.mxpText = null;
	this.aliasList = null;
	this.triggerList = null;
	this.timerList = null;
	this.supportPluginCode = 'window.onmessage=function(event){ if(window.onevent)window.onevent(event.data.payload);};'
							+'\nwindow.send=function(event){ parent.postMessage(event,"*");};'
							+'\n';
	this.defaultPluginCode = 'window.onevent=function(event){ console.log("plugin");console.log(event);window.send(event);};';
	this.addPluginFrame = function(pluginName, pluginCode)
	{
		var iframe = document.createElement("iframe");
		iframe.setAttribute('id', "PLUGIN_" + pluginName.toUpperCase().replaceAll(' ','_'));
		iframe.setAttribute("sandbox", "allow-scripts");
		iframe.style.display = "none";
		sipwin.topWindow.appendChild(iframe);
		iframe.srcdoc = '<SCRIPT>' + this.supportPluginCode + pluginCode + '</SCRIPT>';
		this.framesMap[pluginName] =  iframe;
		var me = this;
		var listener = function(event) {
			if((!(pluginName in me.framesMap))
			||(me.framesMap[pluginName] !== iframe)
			||(sipwin.plugins !== me)
			||(window.siplets.indexOf(sipwin)<0))
				window.removeEventListener('message', listener);
			else
			if(event.source == iframe.contentWindow)
				me.processMessage(sipwin, pluginName, event.data);
		};
		window.addEventListener('message', listener);
		this.aliasList = null;
		this.triggerList = null;
		this.timerList = null;
	};
	
	this.reset = function()
	{
		this.plugins = [];
		this.framesMap = {};
		if(!sipwin.topContainer)
			return;
		var global = GetGlobalPlugins();
		if(global && Array.isArray(global)) {
			for(var i=0;i<global.length;i++)
			{
				if(global[i].code)
					this.addPluginFrame(global[i].name,global[i].code);
				this.plugins.push(JSON.parse(JSON.stringify(global[i])));
			}
		}
		if(sipwin.pb && sipwin.pb.plugins && Array.isArray(sipwin.pb.plugins)) {
			for(var i=0;i<sipwin.pb.plugins.length;i++)
			{
				if(sipwin.pb.plugins[i].code)
					this.addPluginFrame(sipwin.pb.plugins[i].name,sipwin.pb.plugins[i].code);
				this.plugins.push(JSON.parse(JSON.stringify(sipwin.pb.plugins[i])));
			}
		}
	};
	
	this.postEvent = function(event) {
		for(var k in this.framesMap)
			if(this.framesMap[k].contentWindow)
				this.framesMap[k].contentWindow.postMessage({ type: 'message', payload: event}, '*');
	};
	
	this.postEventToPlugin = function(plugin, event) {
		if(plugin in this.framesMap)
			if(this.framesMap[plugin].contentWindow)
				this.framesMap[plugin].contentWindow.postMessage({ type: 'message', payload: event}, '*');
	}
	
	this.processMessage = function(sipwin, pluginName, data) {
		// what do we let them do, exactly?
		if(data.command)
		{
			switch(data.command)
			{
			case 'submitInput':
				if(data.data)
					sipwin.submitInput(data.data);
				break;
			case 'submitHidden':
				if(data.data)
					sipwin.submitHidden(data.data);
				break;
			case 'displayText':
				if(data.data)
					sipwin.displayText(data.data);
				break;
			case 'playSound':
				if(data.data)
					sipwin.playSound(data.data);
				break;
			case 'setVariable':
				if(data.key && data.data)
					sipwin.setVariable(data.key, data.data);
				break;
			case 'runScript':
				if(data.data)
					sipwin.runScript(data.data);
				break;
			case 'enableTrigger':
				if(data.data)
					sipwin.enableTrigger(data.data);
				break;
			case 'sendGMCP':
				if(data.command && data.data)
					sipwin.sendGMCP(data.command, data.data);
				break;
			case 'sendMSDP':
				if(data.data)
					sipwin.sendMSDP(data.data);
				break;
			case 'disableTrigger':
				if(data.data)
					sipwin.disableTrigger(data.data);
				break;
			case 'startTimer':
				if(data.data)
					sipwin.startTimer(data.data);
				break;
			case 'clearTimer':
				if(data.data)
					sipwin.clearTimer(data.data);
				break;
			case 'displayAt':
				if(data.data && data.frame)
					sipwin.displayAt(data.data, data.frame);
				break;
			case 'fetchVariable':
				if(data.data)
					sipwin.fetchVariable(pluginName, data.data);
				break;
			}
		}
	};
	
	this.mxp = function()
	{
		if(this.mxpText == null)
		{
			var s = '';
			for(var i=0;i<this.plugins.length;i++)
				if('mxp' in this.plugins[i])
					s += '\n' + this.plugins[i].mxp;
			s=s.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
			this.mxpText = s;
		}
		return this.mxpText;
	};
	
	this.aliases = function()
	{
		if(this.aliasList == null)
		{
			this.aliasList = [];
			for(var i=0;i<this.plugins.length;i++)
				if(('aliases' in this.plugins[i]) && Array.isArray(this.plugins[i].aliases))
				{
					var paliases = this.plugins[i].aliases;
					for(var p=0;p<paliases.length;p++)
					{
						var palias=paliases[p];
						var req = ['name','regex','pattern','replace','action','argument'];
						var r=0;
						for(r=0;r<req.length;r++)
							if(!(req[r] in palias))
							{
								console.log("Missing "+req[r]+" from plugin "+this.plugins[i].name+" alias");
								break;
							}
						if(r<req.length)
							continue;
						if((palias.action in sipwin)
						&&(typeof sipwin[palias.action] == "function"))
						{
							if(isValidExpression(palias.argument))
							{
								var newOne = {
									name: palias.name,
									regex: palias.regex,
									replace: palias.replace,
									pattern: palias.pattern,
									action: 'win.' + palias.action + '(' + palias.argument + ')'
								};
								this.aliasList.push(newOne);
							}
							else
								console.log("Bad argument in plugin "+this.plugins[i].name+" alias");
						}
						else
							console.log("Bad action in plugin "+this.plugins[i].name+" alias");
					}
				}
			this.aliasList = ParseAliases(this.aliasList);
		}
		return this.aliasList;
	};
	
	this.triggers = function()
	{
		if(this.triggerList == null)
		{
			this.triggerList = [];
			for(var i=0;i<this.plugins.length;i++)
				if(('triggers' in this.plugins[i]) && Array.isArray(this.plugins[i].triggers))
				{
					var ptriggers = this.plugins[i].triggers;
					for(var p=0;p<ptriggers.length;p++)
					{
						var ptrigger=ptriggers[p];
						var req = ['name','regex','pattern','once','action','argument'];
						var r=0;
						for(r=0;r<req.length;r++)
							if(!(req[r] in ptrigger))
							{
								console.log("Missing "+req[r]+" from plugin "+this.plugins[i].name+" trigger");
								break;
							}
						if(r<req.length)
							continue;
						if((ptrigger.action in sipwin)
						&&(typeof sipwin[ptrigger.action] == "function"))
						{
							if(isValidExpression(ptrigger.argument))
							{
								var newOne = {
									name: ptrigger.name,
									regex: ptrigger.regex,
									once: ptrigger.replace,
									pattern: ptrigger.pattern,
									action: 'win.' + ptrigger.action + '(' + ptrigger.argument + ')',
									allowed: true
								};
								this.triggerList.push(newOne);
							}
							else
								console.log("Bad argument in plugin "+this.plugins[i].name+" trigger");
						}
						else
							console.log("Bad action in plugin "+this.plugins[i].name+" trigger");
					}
				}
			this.triggerList = ParseTriggers(this.triggerList);
		}
		return this.triggerList;
	};
	
	this.timers = function()
	{
		if(this.timerList == null)
		{
			this.timerList = [];
			for(var i=0;i<this.plugins.length;i++)
				if(('timers' in this.plugins[i]) && Array.isArray(this.plugins[i].timers))
				{
					var ptimers = this.plugins[i].timers;
					for(var p=0;p<ptimers.length;p++)
					{
						var ptimer=ptimers[p];
						var req = ['name','delay','option','auto','action','argument'];
						var r=0;
						for(r=0;r<req.length;r++)
							if(!(req[r] in ptimer))
							{
								console.log("Missing "+req[r]+" from plugin "+this.plugins[i].name+" timer");
								break;
							}
						if(r<req.length)
							continue;
						if((ptimer.action in sipwin)
						&&(typeof sipwin[ptimer.action] == "function"))
						{
							if(ptimer.option)
								ptimer.option = ptimer.option.toLowerCase();
							if(!isValidExpression(ptimer.argument))
								console.log("Bad argument in plugin "+this.plugins[i].name+" timer");
							else
							if(!isNumber(ptimer.delay))
								console.log("Bad delay in plugin "+this.plugins[i].name+" timer");
							else
							if(['repeat','multiple','once'].indexOf(ptimer.option)<0)
								console.log("Bad option in plugin "+this.plugins[i].name+" timer (repeat, multiple, once)");
							{
								var newOne = {
									name: ptimer.name,
									delay: Number(ptimer.delay),
									repeat: ptimer.option=='repeat',
									multiple: ptimer.option=='multiple',
									trigger: (''+ptimer.auto).toLowerCase()=='true',
									action: 'win.' + ptimer.action + '(' + ptimer.argument + ')',
									allowed: true
								};
								this.timerList.push(newOne);
							}
						}
						else
							console.log("Bad action in plugin "+this.plugins[i].name+" timer");
					}
				}
		}
		return this.timerList;
	};
}
