window.currentSiplet = null;
window.nextSipletSpanId = 0;
window.siplets = [];
window.windowArea = null;

var Siplet =
{
	VERSION_MAJOR: 3.0,
	VERSION_MINOR: 0
};

function SipletWindow(windowName)
{
	this.siplet = Siplet;
	this.width = getConfig('window/width',80);
	this.height = getConfig('window/height',25);;
	this.maxLines = getConfig('window/lines',5000);;
	this.MSPsupport = false;
	this.MSDPsupport = false;
	this.GMCPsupport = false;
	this.MXPsupport = false;
	this.MCCPsupport = false;
	this.wsopened = false;
	this.windowName = windowName;
	this.bin = new BPPARSE(false);
	this.ansi = new ANSISTACK();
	this.telnet = new TELNET(this);
	this.msp = new MSP(this);
	this.mxp = new MXP(this);
	this.text = new TEXT([this.mxp,this.msp]);
	this.topWindow = document.getElementById(windowName);
	this.topWindow.onclick = function() { delayhidemenu(); boxFocus(); };
	this.wsocket = null;
	this.gauges=[];
	this.gaugeWindow = null;
	this.textBuffer = '';
	this.textBufferPruneIndex = 0;
	this.globalTriggers = GetGlobalTriggers();
	this.triggers = null;
	this.globalAliases = GetGlobalAliases();
	this.aliases = null;
	this.globalTimers = JSON.parse(JSON.stringify(GetGlobalTimers()));
	this.timers = null;
	this.activeTimers = [];
	this.vars = {};
	this.lastStyle = '';

	var me = this;
	
    this.topWindow.style.fontFamily = getConfig('window/fontface', 'monospace');
    this.topWindow.style.fontSize = getConfig('window/fontsize', '16px');
	this.topWindow.style.whiteSpace = 'pre'; // Preserve spaces as-is
    this.topContainer = this.topWindow.firstChild;
	this.window = this.topContainer.firstChild;
	[this.topWindow, this.topContainer, this.window].forEach(function(o)
	{
		o.style.position='absolute';
		o.style.top = '0%';
		o.style.height = '100%';
		o.style.width = '100%';
		o.style.left = '0%';
	});
    this.window.style.overflowY = 'auto';
    this.window.style.overflowX = 'hidden';
	
	this.connect = function(url)
	{
		this.wsocket = new WebSocket(url);
		this.wsocket.binaryType = "arraybuffer";
		this.wsocket.onmessage = this.onReceive;
		this.wsocket.onopen = function(event)  
		{
			me.wsopened=true; 
			me.tab.style.backgroundColor="green";
			me.tab.style.color="white";
			me.tab.innerHTML=(me.tabTitle)?(me.tabTitle):'';
			me.window.style.backgroundColor="black";
			me.window.style.color="white";
			me.startTimers();
		};
		this.wsocket.onclose = function(event)  
		{ 
			me.wsopened=false; 
			me.tab.style.backgroundColor="#FF555B";
			me.tab.style.color="white";
			me.clearTimers();
		};
	};

	this.closeSocket = function()
	{
	    if (this.wsocket && this.wsocket.readyState === WebSocket.OPEN) {
	    	this.wsocket.close();
	    }
	}
	
	this.close = function()
	{
		this.closeSocket();
		this.reset();
	}
	
	this.reset = function()
	{
		while (this.topWindow.children.length > 1) {
			this.topWindow.removeChild(this.topWindow.lastChild);
		}
		this.topContainer = this.topWindow.firstChild;
		this.topContainer.style.cssText = 'position:absolute;top:0%;left:0%;width:100%;height:100%;'
		while (this.topContainer.children.length > 1) {
			this.topContainer.removeChild(this.topContainer.lastChild);
		}
		this.window = this.topContainer.firstChild;
		this.window.style.cssText = 'position:absolute;top:0%;left:0%;width:100%;height:100%;'
		this.window.style.overflowY = 'auto';
		this.window.style.overflowX = 'hidden';
  		this.MSPsupport = false;
		this.MSDPsupport = false;
		this.GMCPsupport = false;
		this.MXPsupport = false;
		this.MCCPsupport = false;
		this.wsopened = false;
		this.bin.reset();
		this.ansi.reset();
		this.telnet.reset();
		this.msp.reset();
		this.mxp.reset();
		this.text.reset();
		this.gauges=[];
		this.gaugeWindow = null;
		this.textBuffer = '';
		this.textBufferPruneIndex = 0;
		this.globalTriggers = GetGlobalTriggers();
		this.triggers = null;
		this.globalAliases = GetGlobalAliases();
		this.aliases = null;
		this.vars = {};
		this.resetTimers();
	}
	
	this.htmlBuffer = '';
	this.flushWindow = function() {
		if(this.htmlBuffer.length > 0)
		{
			var span = document.createElement('span');
			span.style.cssText = this.lastStyle;
			this.lastStyle = this.ansi.styleSheet();
			span.innerHTML = this.htmlBuffer;
			this.window.appendChild(span);
			this.htmlBuffer='';
			if(window.currentSiplet != me)
			{
				this.tab.style.backgroundColor = "lightgreen";
				this.tab.style.color = "black";
			}
			this.window.scrollTop = this.window.scrollHeight - this.window.clientHeight;
		}
	};

	this.onReceive = function(e)
	{
		var entries = me.bin.parse(e.data);
		while (me.window.childNodes.length > me.maxLines)
			me.window.removeChild(me.window.firstChild);
		me.htmlBuffer = '';
		while(entries.length > 0)
		{
			if((entries.length == 1)
			&&(!entries[0].done))
				break;
			var blk = entries.shift();
			if(blk.data.length==0)
				continue;
			if(blk.type == BPTYPE.TELNET)
			{
				var buffer = me.telnet.process(blk.data);
				if ((buffer.byteLength > 0) && (me.wsopened))
					me.wsocket.send(buffer);
			}
			else
			{
				var newText = '';
				if(blk.type == BPTYPE.ANSI)
				{
					newText += me.ansi.process(blk.data);
					if(!me.mxp.active())
					{
						me.htmlBuffer += newText;
						continue;
					}
					var s = me.mxp.process(blk.data);
					if (s != null)
						newText += s;
					blk.data = newText; // convert to text-like
				}
				else
				{
					//TODO: me is not going to support utf-16 well
					for (var i=0; i < blk.data.length; i++) {
						newText += String.fromCharCode( blk.data[i]);
					}
				}
				newText = me.text.process(newText);
				if(newText.length>0)
				{
					me.htmlBuffer += newText;
					me.textBuffer += stripHtmlTags(newText);
					if(newText.indexOf('<BR>')>=0)
					{
						me.flushWindow();
						me.triggerCheck();
						while(me.textBuffer.length > 1024)
						{
							var x = me.textBuffer.indexOf('\n');
							if(x<0)
							{
								me.textBufferPruneIndex+=me.textBuffer.length;
								me.textBuffer = '';
							}
							else
							{
								me.textBuffer = me.textBuffer.substr(0,x+1);
								me.textBufferPruneIndex+=x;
							}
						}
					}
				}
			}
		}
		me.flushWindow();
		me.triggerCheck();
	};

	this.evalTriggerGroup = function(triggers)
	{
		var win = this;
		for(var i=0;i<triggers.length;i++)
		{
			var trig = triggers[i];
			if((!trig.once || trig.prev==0) 
			&& eval(trig.allowed) && trig.pattern
			&& (!trig.disabled))
			{
				var prev = trig.prev - this.textBufferPruneIndex;
				if(prev < 0)
					prev = 0;
				if(trig.regex && trig.pattern && trig.pattern.exec)
				{
					trig.pattern.lastIndex = prev;
					var match = trig.pattern.exec(this.textBuffer);
					while(match !== null)
					{
						trig.prev = this.textBufferPruneIndex + match.index + 1;
						var action = trig.action.replaceAll('\\n','\n');
						try { eval(action); } catch(e) {};
						match = trig.pattern.exec(this.textBuffer);
					}
				}
				else
				{
					var x = this.textBuffer.indexOf(trig.pattern,prev);
					while(x>=0)
					{
						prev += x + trig.pattern.length + 1;
						trig.prev = this.textBufferPruneIndex + x + trig.pattern.length + 1;
						var action = trig.action.replaceAll('\\n','\n');
						try { eval(action); } catch(e) {};
						x = this.textBuffer.indexOf(trig.pattern,prev);
					}
				}
			}
		}
	};
	
	this.localTriggers = function()
	{
		if(this.triggers == null)
		{
			if((!this.pbentry)
			||(!this.pbentry.triggers)
			||(!Array.isArray(this.pbentry.triggers)))
			{
				this.triggers=[];
				return [];
			}
			this.triggers = ParseTriggers(this.pbentry.triggers);
		}
		return this.triggers;
	}
	
	this.triggerCheck = function()
	{
		this.evalTriggerGroup(this.globalTriggers);
		this.evalTriggerGroup(this.localTriggers());
	};
	
	this.evalAliasGroup = function(aliases, txt)
	{
		var win = this;
		for(var i=0;i<aliases.length;i++)
		{
			var alias = aliases[i];
			if(!alias.disabled)
			{
				if(alias.regex && alias.pattern && alias.pattern.test)
				{
					if(alias.pattern.test(txt))
					{
						txt = txt.replace(alias.pattern,alias.replace);
						var action = alias.action.replaceAll('\\n','\n');
						try { eval(action); } catch(e) {};
						return txt
					}
				}
				else
				if(txt.startsWith(alias.pattern))
				{
					var action = alias.action.replaceAll('\\n','\n');
					try { eval(action); } catch(e) {};
					return alias.replace + txt.substr(alias.pattern.length);
				}
			}
		}
		return txt;
	};
	
	this.aliasProcess = function(x)
	{
		var oldx = x;
		x = this.evalAliasGroup(this.globalAliases, x);
		if(x == oldx)
			x = this.evalAliasGroup(this.localAliases(), x);
		return x;
	};
	
	this.localAliases = function()
	{
		if(this.aliases == null)
		{
			if((!this.pbentry)
			||(!this.pbentry.aliases)
			||(!Array.isArray(this.pbentry.aliases)))
			{
				this.aliases=[];
				return [];
			}
			this.aliases = ParseAliases(this.pbentry.aliases);
		}
		return this.aliases;
	};
	
	this.localScripts = function()
	{
		if(this.scripts == null)
		{
			if((!this.pbentry)
			||(!this.pbentry.scripts)
			||(!Array.isArray(this.pbentry.scripts)))
			{
				this.scripts=[];
				return [];
			}
			this.scripts = this.pbentry.scripts;
		}
		return this.scripts;
	};
	
	this.localTimers = function()
	{
		if(this.timers == null)
		{
			if((!this.pbentry)
			||(!this.pbentry.timers)
			||(!Array.isArray(this.pbentry.timers)))
			{
				this.timers=[];
				return [];
			}
			this.timers = this.pbentry.timers;
		}
		return this.timers;
	};

	this.findTimerByName = function(name, ci)
	{
		var i;
		for(i=0;i<this.localTimers().length;i++)
			if((this.localTimers()[i].name == name)
			||(ci && (this.localTimers()[i].name.toLowerCase() == name.toLowerCase())))
				return this.localTimers()[i];
		for(i=0;i<this.globalTimers.length;i++)
			if((this.globalTimers[i].name == name)
			||(ci && (this.globalTimers[i].name.toLowerCase() == name.toLowerCase())))
				return this.globalTimers[i];
		return null;
	};

	this.startTimer = function(timer)
	{
		if(timer == null)
			return;
		if(typeof timer === "string")
		{
			this.startTimer(findTimerByName(timer));
			return;
		}
		if(!timer.multiple)
		{
			for(var i=0;i < this.activeTimers.length; i++)
			{
				var atimer = this.activeTimers[i];
				if(atimer.timerId == timer.timerId)
					return;
				if(timer.name == atimer.name)
				{
					timer.timerId = atimer.timerId;
					return;
				}
			}
		}
		var timerCopy = JSON.parse(JSON.stringify(timer));
		var me = this;
		var execute = function() {
			var action = timer.action.replaceAll('\\n','\n');
			var win = me;
			try { eval(action); } catch(e) {};
			if(timer.repeat)
			{
				timer.timerId = setTimeout(execute, timer.delay);
				timerCopy.timerId = timer.timerId;
			}
			else
			{
				var x = me.activeTimers.indexOf(timerCopy);
				if(x >= 0 )
				{
					me.activeTimers.splice(x,1);
					if(timer.timerid)
						delete timer.timerId;
				}
			}
		}
		timer.timerId = setTimeout(execute, timer.delay);
		timerCopy.timerId = timer.timerId;
		this.activeTimers.push(timerCopy);
	};

	this.startTimers = function(timers)
	{
		if(!this.wsopened)
			return;
		if((timers == null)||(timers==undefined)||(!Array.isArray(timers)))
		{
			this.startTimers(this.globalTimers);
			this.startTimers(this.localTimers());
		}
		else
		{
			for(var i=0;i < timers.length; i++)
			{
				var timer = timers[i];
				if((timer.timerId == undefined)
				&&(timer.trigger))
					this.startTimer(timer);
			}
		}
	};
	
	this.clearTimer = function(name)
	{
		var timer = findTimerByName(name);
		if(timer !== null)
		{
			for(var i=this.activeTimers.length-1;i>=0;i--)
			{
				if((this.activeTimers[i].timerId == timer.timerId)
				||(this.activeTimers[i].name == timer.name))
				{
					clearTimeout(this.activeTimers[i].timerId);
					if(timer.timerId !== undefined)
						delete timer.timerId;
					this.activeTimers.splice(i,1);
				}
			}
		}
	};
	
	this.clearTimers = function(timers)
	{
		if((timers == null)||(timers==undefined)||(!Array.isArray(timers)))
		{
			if(this.activeTimers && Array.isArray(this.activeTimers))
				this.clearTimers(this.activeTimers);
			this.activeTimers = [];
			if(this.globalTimers && Array.isArray(this.globalTimers))
				this.clearTimers(this.globalTimers);
			if(this.timers && Array.isArray(this.timers))
				this.clearTimers(this.timers);
		}
		else
		{
			for(var i=0;i < timers.length; i++)
			{
				var timer = timers[i];
				if(timer.timerId !== undefined)
					clearTimeout(timer.timerId);
				delete timer.timerId;
			}
		}
	};
	
	this.resetTimers = function()
	{
		this.clearTimers();
		this.startTimers();
	};
	
	this.createGauge = function(entity,caption,color,value,max)
	{
		var gaugedata=new Array(5);
		gaugedata[0]=entity;
		gaugedata[1]=caption;
		gaugedata[2]=color;
		gaugedata[3]=value;
		gaugedata[4]=max;
		this.gauges[this.gauges.length]=gaugedata;
		var gaugeHeight = 20;
		if(this.gaugeWindow == null)
		{
			this.gaugeWindow = document.createElement('div');
			this.gaugeWindow.innerHTML = '';
			this.gaugeWindow.style.position = 'absolute';
			this.gaugeWindow.style.top = '0%';
			this.gaugeWindow.style.width = '100%';
			this.gaugeWindow.style.height = gaugeHeight+'px';
			this.gaugeWindow.style.background = 'black';
			this.topWindow.appendChild(this.gaugeWindow);
			var parent = this.window.parentNode;
			parent.style.top = 'calc(' +parent.style.top + ' + '+gaugeHeight + 'px)';
			parent.style.height = 'calc(' +parent.style.height + ' - '+gaugeHeight + 'px)';
		}
		this.modifyGauge(entity,value,max);
	};
	
	this.removeGauge = function(entity)
	{
		var index = this.gauges.indexOf(entity);
		if(index >=0)
		{
			this.gauges.splice(index);
			this.modifyGauge(entity,-1,-1);
			if((this.gauges.length == 0) && (this.topWindow != null))
			{
				
				var mainWindow = this.gaugeWindow.parentNode.firstChild;
				mainWindow.style.top = this.gaugeWindow.top;
				this.gaugeWindow.outerHTML = '';
				this.gaugeWindow = null;
			}
		}
	}
	
	this.modifyGauge = function(entity,value,max)
	{
		var div=this.gaugeWindow;
		if(div == null)
			return;
		if(this.gauges.length==0)
			div.innerHTML='';
		else
		{
			var s='<TABLE WIDTH=100% CELLPADDING=0 CELLSPACING=0 BORDER=1><TR>';
			var i=0;
			var cellwidth=100/this.gauges.length;
			for(i=0;i<this.gauges.length;i++)
			{
				var gaugedata=this.gauges[i];
				if(gaugedata[0]==entity)
				{
					gaugedata[3]=value;
					gaugedata[4]=max;
				}
			}
			for(i=0;i<this.gauges.length;i++)
			{
				var gaugedata=this.gauges[i];
				s+='<TD WIDTH='+cellwidth+'%>';
				s+='<FONT STYLE="color: '+gaugedata[2]+'" SIZE=-2>'+gaugedata[1]+'</FONT><BR>';
				var gaugedata=this.gauges[i];
				var fullwidth=100-gaugedata[3];
				var lesswidth=gaugedata[3];
				s+='<TABLE WIDTH=100% CELLPADDING=0 CELLSPACING=0 BORDER=0 HEIGHT=5><TR HEIGHT=5>';
				s+='<TD STYLE="background-color: '+gaugedata[2]+'" WIDTH='+lesswidth+'%></TD>';
				s+='<TD STYLE="background-color: black" WIDTH='+fullwidth+'%></TD>';
				s+='</TR></TABLE>';
				s+='</TD>';
			}
			s+='</TR></TABLE>'
			div.innerHTML=s;
		}
	};
	
	this.submitInput = function(value)
	{
		this.displayText(value);
		this.wsocket.send(value+'\n');
	};
	
	this.displayText = function(value)
	{
		var span = document.createElement('span');
		span.innerHTML = value.replaceAll('\n','<BR>') + '<BR>';
		this.window.appendChild(span);
		this.window.scrollTop = this.window.scrollHeight - this.window.clientHeight;
	};
	
	this.playSound = function(file)
	{
		if(!file)
			return;
		var x = file.lastIndexOf('/');
		var url;
		var key;
		if(x>0)
		{
			key=file.substr(x+1);
			url=file.substr(0,x+1);
		}
		else
		{
			url = window.location.protocol + '//' + window.location.host + '/images/';
			key = file;
		}
		this.msp.PlaySound(key,url,0,50,50);
	};

	this.setVariable = function(key, value)
	{
		this.vars[key] = value;
	};
	
	this.getVariable = function(key)
	{
		if(key in this.vars)
			return this.vars[key];
		return '';
	};

	this.findLocalScript = function(value)
	{
		var run = FindAScript(this.localScripts(),value,false);
		if(run == null) 
			run = FindAScript(this.globalScripts,value,false);
		if(run == null) 
			run = FindAScript(this.localScripts(),value,true);
		if(run == null) 
			run = FindAScript(this.globalScripts,value,true);
		return run;
	}
	
	this.runScript = function(value)
	{
		var run = findLocalScript(value);
		if(run != null)
		{
			var win = this;
			eval(value);
		}
		else
			console.log("Unable to find script '"+value+"'");
	};
	
	this.enableTrigger = function(value)
	{
		var trigs = this.localTriggers();
		for(var i=0;i<trigs.length;i++)
			if(value == trigs[i].name)
				trigs[i].disabled = false;
		trigs = this.globalTriggers;
		for(var i=0;i<trigs.length;i++)
			if(value == trigs[i].name)
				trigs[i].disabled = false;
	};
	
	this.disableTrigger = function(value)
	{
		var trigs = this.localTriggers();
		for(var i=0;i<trigs.length;i++)
			if(value == trigs[i].name)
				trigs[i].disabled = true;
		trigs = this.globalTriggers;
		for(var i=0;i<trigs.length;i++)
			if(value == trigs[i].name)
				trigs[i].disabled = true;
	};
	
	this.setColor = function(value)
	{
		this.DisplayText('</FONT><FONT COLOR="'+value+'">');
	};
	
	this.isConnected = function() 
	{
		return this.wsopened;
	}
}

function AddNewSipletTabByPort(port)
{
	var protocol = (window.location.protocol === 'https:') ? 'wss:' : 'ws:';
	var defaultUrl = protocol + '//' + window.location.host + '/WebSock';
	if(port === 'default')
		return AddNewSipletTab(defaultUrl);
	else
		return AddNewSipletTab(defaultUrl+'?port='+port);
}
	
function AddNewSipletTabByPB(which)
{
	if(which=='')
		return;
	var ogwhich = ''+which;
	var global = ogwhich.startsWith('g');
	var pb;
	if(global)
	{
		which = Number(ogwhich.substr(1));
		if((which<0) || (which > window.phonebook.length))
			return;
		pb = window.phonebook[which];
	}
	else
	{
		var phonebook = getConfig('/phonebook/dial',[]);
		which = Number(ogwhich);
		if((which<0) || (which > phonebook.length))
			return;
		pb = phonebook[which];
	}
	var port = pb.port;
	var protocol = (window.location.protocol === 'https:') ? 'wss:' : 'ws:';
	var defaultUrl = protocol + '//' + window.location.host + '/WebSock';
	var siplet;
	if(port === 'default')
		siplet = AddNewSipletTab(defaultUrl);
	else
		siplet = AddNewSipletTab(defaultUrl+'?port='+port);
	if(global)
		siplet.tabTitle = pb.name + '('+pb.port+')';
	else
		siplet.tabTitle = pb.user + '@' + pb.name + ' ('+pb.port+')';
	siplet.pbentry = pb;
	siplet.pbwhich = ogwhich;
}

function AddNewSipletTab(url)
{
	var windowName = 'W'+window.siplets.length;
	var newTopElement=document.createElement('DIV');
	var newWinContainer=document.createElement('DIV');
	var newWindow=document.createElement('DIV');
	newTopElement.id = windowName;
	window.windowArea.appendChild(newTopElement);
	newTopElement.appendChild(newWinContainer);
	newWinContainer.appendChild(newWindow);
	newWinContainer.style.cssText = 'position:absolute;top:0%;left:0%;width:100%;height:100%;'
	var siplet = new SipletWindow(windowName); // makes a deep copy
	siplet.tab = AddNewTab(); 
	siplet.url = url;
	window.siplets.push(siplet);
	siplet.connect(url);
	SetCurrentTab(window.siplets.length-1);
	return siplet;
}

function SetCurrentTab(which)
{
	window.currentSiplet=window.siplets[which];
	for(var i=0;i<window.siplets.length;i++)
	{
		var s = window.siplets[i];
		if(i == which)
		{
			s.topWindow.style.visibility = "visible";
			if(s.wsopened)
			{
				s.tab.style.backgroundColor = "green";
				s.tab.style.color = "white";
			}
		}
		else
		{
			s.topWindow.style.visibility = "hidden";
			if(s.wsopened)
			{
				s.tab.style.backgroundColor = "lightgray";
				s.tab.style.color = "black";
			}
		}
	}
}

function CloseAllSiplets()
{
	for(var i=0;i<window.siplets.length;i++)
	{
		var siplet = window.siplets[i];
		siplet.closeSocket();
	}
}

function AutoConnect()
{
	AddNewSipletTabByPB(getConfig('/phonebook/auto',''));
}

function PBSameAs(pb1, pb2)
{
	if(pb1 === pb2)
		return true;
	if(!pb1.user || !pb2.name || !pb1.user || !pb1.name)
		return false;
	return (pb1.account == pb2.account)
		&&(pb1.name == pb2.name)
		&&(pb1.port == pb2.port)
		&&(pb1.accountName == pb2.accountName)
		&&(pb1.user == pb2.user)
		&&(pb1.password == pb2.password);
}

function FindSipletsByPB(pb)
{
	var found = [];
	for(var i=0;i<window.siplets.length;i++)
	{
		var siplet = window.siplets[i];
		var p = siplet.pbentry;
		if(PBSameAs(pb,p))
			found.push(siplet);
	}
	return found;
}

function UpdateSipetTabsByPBIndex(which)
{
	if((which+'').startsWith('g'))
		return;
	var phonebook = getConfig('/phonebook/dial',[]);
	which = Number(which);
	if((which<0) || (which > phonebook.length))
		return;
	var pb = phonebook[which];
	for(var i=0;i<window.siplets.length;i++)
	{
		var siplet = window.siplets[i];
		if(Number(siplet.pbwhich) == which)
		{
			siplet.tabTitle = pb.user + '@' + pb.name + ' ('+pb.port+')';
			if(siplet.tab)
				siplet.tab.innerHTML = siplet.tabTitle;
			siplet.pbentry = pb;
			if(siplet.triggers != null)
				siplet.triggers = null;
		}
	}
}

setTimeout(function() {
	var updateSipletConfigs = function() {
		for(var i=0;i<window.siplets.length;i++)
		{
			var siplet = window.siplets[i];
			siplet.width = getConfig('window/width',siplet.width);
			siplet.height = getConfig('window/height',siplet.height);
			siplet.maxLines = getConfig('window/lines',siplet.maxLines);
		}
	};
	addConfigListener('window/width', updateSipletConfigs);
	addConfigListener('window/height', updateSipletConfigs);
	addConfigListener('window/lines', updateSipletConfigs);
	var updateSipletWindows = function() {
		for(var i=0;i<window.siplets.length;i++)
		{
			var siplet = window.siplets[i];
		    siplet.topWindow.style.fontFamily = getConfig('window/fontface', siplet.topWindow.style.fontFamily);
		    siplet.topWindow.style.fontSize = getConfig('window/fontsize', siplet.topWindow.style.fontSize);
	    }
	}
	addConfigListener('window/fontface', updateSipletWindows);
	addConfigListener('window/fontsize', updateSipletWindows);
},0);
