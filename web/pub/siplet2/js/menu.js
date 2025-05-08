var menuAreaHeight = 20;
var menuArea = null;
var menuWindow = null;
function configureMenu(obj)
{
	menuArea=obj;
	menuArea.style.position='fixed';
	menuArea.style.top=0;
	menuArea.style.width='100%';
	menuArea.style.height=menuAreaHeight+'px';
	menuArea.style.background='#404040';
	
	var menuData = [
		{"Window": [
			{"n":"New"},
			{"n":"Disconnect"},
			{"n":"Reconnect"}
		]},
		{"Options": [
			{"n":"Global"}
		]},
		{"Help": [
			{"n":"About","a":"javascript:menuAbout()"}
		]}
	];
	var html = '';
	html += '<TABLE style="border: 1px solid white; border-collapse: collapse; height: 20px; table-layout: fixed; width: 100%;">';
	html +='<TR style="height: 20px;" >';
	for(var to=0;to<menuData.length;to++)
	{
		var topO = menuData[to];
		var topN = Object.keys(topO)[0];
		var subList = topO[topN];
		html += '<TD style="border: 1px solid white; padding: 0;"';
		var href='';
		var hint='';
		for(var h=0;h<subList.length;h++)
		{
			var sub=subList[h];
			href+=sub['a']+'|';
			hint+=sub['n']+'|';
		}
		//dropdownmenu(obj, e, href, hint, prompt, menuwidth)
		html += ' ONCLICK="menumenu(this,event,\''+href+'\',\''+hint+'\',\'\')" ';
		html += '><FONT COLOR=YELLOW><B>&nbsp;&nbsp;';
		html += topN + '</B></FONT></TD>';
	}
	html += '</TR></TABLE>';
	menuArea.innerHTML = html;
}

function hideOptionWindow()
{
	if(window.menuWindow != null)
	{
		window.menuWindow.style.visibility = 'hidden';
		window.menuWindow.onclick = function() {};
		var contentWindow = window.menuWindow.getElementsByTagName('div')[1];
		contentWindow.innerHTML = '';
	}
}

function getOptionWindow(heading, w, h)
{
	if(window.menuWindow == null)
	{
		window.menuWindow = document.createElement('div');
		window.menuWindow.style.cssText = "position:absolute;top:20%;left:10%;height:60%;width:80%;z-index:99;";
		window.menuWindow.style.cssText += "border-style:solid;border-width:5px;border-color:white;";
		window.menuWindow.style.backgroundColor = 'darkgray';
		window.menuWindow.style.visibility = 'visible';
		window.menuWindow.style.foregroundColor = 'black';
		document.body.appendChild(window.menuWindow);
		var titleBar = document.createElement('div');
		titleBar.style.cssText = "position:absolute;top:0%;left:0%;height:20px;width:100%;";
		titleBar.style.backgroundColor = 'white';
		titleBar.style.foregroundColor = 'black';
		var contentWindow = document.createElement('div');
		contentWindow.style.cssText = "position:absolute;top:20px;left:0%;height:calc(100% - 20px);width:100%;";
		contentWindow.style.backgroundColor = 'lightgray';
		contentWindow.style.foregroundColor = 'black';
		window.menuWindow.appendChild(titleBar);
		window.menuWindow.appendChild(contentWindow);
	}
	window.menuWindow.onclick = function() {};
	var titleBar = window.menuWindow.getElementsByTagName('div')[0];
	var contentWindow = window.menuWindow.getElementsByTagName('div')[1];
	contentWindow.innerHTML = '';
	window.menuWindow.style.top = 'calc(' + (((100-h)/2)+'%') + ' - '+inputAreaHeight+'px);';
	window.menuWindow.style.left = ((100-w)/2)+'%';
	window.menuWindow.style.height = h+'%';
	window.menuWindow.style.width = w+'%';
	window.menuWindow.style.visibility = 'visible';
	titleBar.innerHTML = '<FONT COLOR=BLACK>'+heading+'</FONT>'+
		'<IMG style="float: right; width: 16px; height: 16px;" '
		+'ONCLICK="hideOptionWindow();" '
		+'SRC="images/close.gif">';
	return contentWindow;
}

function menuAbout()
{
	var content = getOptionWindow("About",60,40);
	var aboutHtml = '<div style="position:absolute;top:50%;left:50%;white-space:nowrap;';
	aboutHtml += 'transform:translate(-50%,-50%);text-align:center;line-height: 1.5;">';
	aboutHtml += '<div style="display:block;margin:0 auto;"><h1>Siplet v'+Siplet.VERSION_MAJOR;
	if(Siplet.VERSION_MINOR != 0)
		aboutHtml += '.'+Siplet.VERSION_MINOR;
	aboutHtml += '</h1></div><BR><BR><div style="display:block;margin:0 auto;">';
	aboutHtml += '(C)2025-2025 Bo Zimmerman</div>';
	aboutHtml += '</div>';
	content.innerHTML=aboutHtml;
	this.menuWindow.onclick = hideOptionWindow;
}
