var Siplet =
{
	VERSION_MAJOR: 3.0,
	VERSION_MINOR: 0
};

function SipletWindow(windowName)
{
	this.siplet = Siplet;
	this.width = 80;
	this.height = 25;
	this.maxLines = 5000;
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
	this.window = document.getElementById(windowName);
	this.topWindow = this.window;
	this.wsocket = null;
	this.gauges=[];
	this.gaugeWindow = null;

	var me = this;
	
    this.window.style.height = 'calc(100vh - ' + inputAreaHeight + 'px)';
    this.window.style.overflowY = 'auto';
    this.window.style.fontFamily = 'monospace';
	
	this.connect = function(url)
	{
		this.wsocket = new WebSocket(url);
		this.wsocket.binaryType = "arraybuffer";
		this.wsocket.onmessage = this.onReceive;
		this.wsocket.onopen = function(event)  
		{
			me.wsopened=true; 
			me.window.style.backgroundColor="black";
			me.window.style.foregroundColor="white";
		};
		this.wsocket.onclose = function(event)  
		{ 
			me.wsopened=false; 
			me.window.style.background="red";
		};
	};
	
	this.onReceive = function(e)
	{
		var entries = me.bin.parse(e.data);
		while (me.window.childNodes.length > me.maxLines)
			me.window.removeChild(me.window.firstChild);
		var scroll=false;
		var html = '';
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
						html += newText;
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
					html += newText;
					if(newText.indexOf('<BR>')>=0)
					{
						var span = document.createElement('span');
						span.innerHTML = html;
						me.window.appendChild(span);
						html='';
						scroll=true;
					}
				}
			}
		}
		if(html.length > 0)
		{
			var span = document.createElement('span');
			span.innerHTML = html;
			me.window.appendChild(span);
			scroll=true;
		}
		if(scroll)
			me.window.scrollTop = me.window.scrollHeight - me.window.clientHeight;
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
		if(this.gaugeWindow == null)
		{
			var oldMainWindow = this.window;
		    oldMainWindow.style.overflowY = 'visible';
			this.gaugeWindow = document.createElement('div');
			this.gaugeWindow.innerHTML = '';
			this.gaugeWindow.classList = 'gauge';
			this.window = document.createElement('div');
		    this.window.style.height = 'calc(100vh - ' + (20+inputAreaHeight) + 'px)';
		    this.window.style.top = 20;
		    this.window.style.width = '100vw';
		    this.window.style.position='fixed';
		    this.window.style.overflowY = 'auto';
		    this.window.style.fontFamily = 'monospace';
			this.window.innerHTML = oldMainWindow.innerHTML;
			this.window.classList = 'subcontent';
			oldMainWindow.innerHTML='';
			oldMainWindow.appendChild(this.gaugeWindow);
			oldMainWindow.appendChild(this.window);
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
				//TODO: remove the damn thing.
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
		var span = document.createElement('span');
		span.innerHTML = value + '<BR>';
		this.window.appendChild(span);
		this.wsocket.send(value+'\n');
	};
}

var nextSipletSpanId = 0;

