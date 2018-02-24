package com.kernel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityStringHelper
{
	public static String getRelationArg(String entSeqn, String marker)
	throws Exception
	{
		Pattern pattern = null;
		Matcher matcher = null;

		pattern = Pattern.compile(createArgPattern(marker), 2);
		matcher = pattern.matcher(entSeqn);
		if (matcher.find())
		{
			return matcher.group(1);
		}

		return "";
	}

	public static String getUrduEmotionArg(String entSeqn, int index)
	throws Exception
	{
		Pattern pattern = null;
		Matcher matcher = null;
		
		String marker = null ;
		if (index == 0)
			marker = "Candidate" ;
		if (index == 1)
			marker = "Emotion" ;
		if (index == 2)
			marker = "Predicate" ;
		if (index == 3)
			marker = "Sequence" ;
		if (index == 4)
			marker = "lk" ;
		
		if ( index == 3)
		{
			
			pattern = Pattern.compile(createUrduEmotionFullSequencePattern(marker));
		}
		
		if ( index == 4)
		{
			pattern = Pattern.compile(createUrduFeatureVectorPattern(marker));
		}
		else
		{
			pattern = Pattern.compile(createUrduEmotionPattern(marker));
		}
		
		matcher = pattern.matcher(entSeqn);
		if (matcher.find())
		{
			return matcher.group(1);
		}

		return "";
	}




	public static String[] getEntity(String eachLine, String marker, int type) {
		Pattern pattern = null;
		Matcher matcher = null;
		pattern = Pattern.compile(createArgPattern(marker, marker), 2);
		matcher = pattern.matcher(eachLine);

		String[] entArray = new String[2];
		if (matcher.find())
		{
			entArray[0] = matcher.group(2);
			entArray[1] = matcher.group(1);
		}
		else
		{
			entArray[0] = "BLANK";
			entArray[1] = "O";
		}
		return entArray;
	}

	public static String createArgPattern(String first)
	{
		return "<" + first + ">" + " (.*) " + "</" + first + ">";
	}
	
	public static String createUrduEmotionPattern(String first)
	{
		return "<" + first + ">\\[" + "(.*)" + "\\]</" + first + ">";
	}

	public static String createUrduEmotionFullSequencePattern(String first)
	{
		return "<" + first + ">\\[" + "(.*)" +  "\\]</" + first + ">";
	}
	public static String createUrduFeatureVectorPattern(String first)
	{
		return "<" + first + ">\\[" + "(.*)" + "\\]</" + first + ">";
	}
	
	public static String createArgPattern(String first, String second)
	{
		return "<" + first + ">" + 
		" (.*) " + 
		"</" + second + ">";
	}
}