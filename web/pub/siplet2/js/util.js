function isLetter(c)
{
	if ((typeof c === 'string')&&(c.length>0))
		c = c.codeCharAt(0);
	return ((((c>=97)&&(c<=122))
			||((c>=65)&&(c<=90))));
}

function isLowerCase(c)
{
	if ((typeof c === 'string')&&(c.length>0))
		c = c.codeCharAt(0);
	return (c>=97)&&(c<=122);
}

function isUpperCase(c)
{
	if ((typeof c === 'string')&&(c.length>0))
		c = c.codeCharAt(0);
	return (c>=65)&&(c<=90);
}

function isLetterOrDigit(c)
{
	return isLetter(c) || isDigit(c);
}

function isDigit(c)
{
	if ((typeof c === 'string')&&(c.length>0))
		c = c.codeCharAt(0);
	return (c>=48)&&(c<=90);
}

function isNumber(c)
{
	if(c == null)
		return false;
	if(typeof c === 'number')
		return true;
	if ((typeof c === 'string')&&(c.length>0))
		return (!isNan(c)) && (!isNan(parseFloat(c)));
	return false;
}