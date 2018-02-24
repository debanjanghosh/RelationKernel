package com.kernel;

import com.thomson.research.svm.EntityNode;
import com.thomson.research.svm.UrduEmotionNode;
import com.thomson.research.svm.UrduEntityNode;
import com.thomson.research.svm.WordNode;
import com.thomson.research.util.VerbNetWrapperUtil;

import rita.wordnet.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

public class WordComparator {
	private EntityFeatureHandler entFeatHndlrObj;
	private Vector<String> killVerbs;
	private RitaWordNetHandler wnHandlerObj;
	private VerbNetWrapperUtil verbNetWrapperObj;
	private String relation ;

	public WordComparator(EntityFeatureHandler efHndle) {
		this.entFeatHndlrObj = efHndle;

		// for urdu relations/opinions - now WordNet
		this.wnHandlerObj = new RitaWordNetHandler();
	//	verbNetWrapperObj = new VerbNetWrapperUtil();
		
	
	}

	public WordComparator() {
	}

	public double compareUrduEmotion(UrduEmotionNode node1,
			UrduEmotionNode node2) {
		double similarCount = 0.0D;
		assert (node1.entityNodeFeatArray.length == node1.entityNodeFeatArray.length);

		// for (int i = 0; i < node1.entityNodeFeatArray.length; ++i)
		for (int i = 0; i < 10; ++i) {
			if ((node1.entityNodeFeatArray[i] == null)
					|| (node2.entityNodeFeatArray[i] == null)
					|| (node1.entityNodeFeatArray[i].compareToIgnoreCase("0") == 0)
					|| (node2.entityNodeFeatArray[i].compareToIgnoreCase("0") == 0)
					|| (node1.entityNodeFeatArray[i]
							.compareToIgnoreCase(node2.entityNodeFeatArray[i]) != 0))
				continue;
			similarCount += 1.0D;
		}

		return similarCount;
	}

	public double compare(UrduEntityNode node1, UrduEntityNode node2) {
		double similarCount = 0.0D;
		assert (node1.entityNodeFeatArray.length == node1.entityNodeFeatArray.length);

		int checkPosn = node1.checkPosn;
		checkPosn = 2;
		if (checkPosn != 2) {
			System.out.println(" wrong check posn ");
		}

		for (int i = 0; i < node1.entityNodeFeatArray.length; ++i) {
			if ((node1.entityNodeFeatArray[i] == null)
					|| (node2.entityNodeFeatArray[i] == null)
					|| (node1.entityNodeFeatArray[i].compareToIgnoreCase("0") == 0)
					|| (node2.entityNodeFeatArray[i].compareToIgnoreCase("0") == 0)
					|| (node1.entityNodeFeatArray[i]
							.compareToIgnoreCase(node2.entityNodeFeatArray[i]) != 0))
				continue;
			similarCount += 1.0D;
		}

		return similarCount;
	}

	public double compare(EntityNode node1, EntityNode node2) {
		double similarCount = 0.0D;
		assert (node1.entityNodeFeatArray.length == node1.entityNodeFeatArray.length);

		if ((node1.entityNodeFeatArray[2] != null)
				&& (node2.entityNodeFeatArray[2] != null)
				&& (node1.entityNodeFeatArray[2]
						.compareToIgnoreCase(node2.entityNodeFeatArray[2]) == 0)) {
			for (int i = 0; i < 3; ++i) {
				if ((node1.entityNodeFeatArray[i] == null)
						|| (node2.entityNodeFeatArray[i] == null)
						|| (node1.entityNodeFeatArray[i]
								.compareToIgnoreCase(node2.entityNodeFeatArray[i]) != 0))
					continue;
				similarCount += 1.0D;
			}
/*
			if (((node1.entityNodeFeatArray[2].charAt(0) == 'V') && (node2.entityNodeFeatArray[2]
					.charAt(0) == 'V'))
					|| ((node1.entityNodeFeatArray[2].charAt(0) == 'N') && (node2.entityNodeFeatArray[2]
							.charAt(0) == 'N'))) {
				String text1 = node1.entityNodeFeatArray[0];
				String text2 = node2.entityNodeFeatArray[0];
				String pos1 = Character.toString(node1.entityNodeFeatArray[2]
						.charAt(0));
				String pos2 = Character.toString(node2.entityNodeFeatArray[2]
						.charAt(0));

				similarCount += this.wnHandlerObj.compHypernyms(text1, pos1,
						text2, pos2);
			}
*/
		}

		return similarCount;
	}

	public double compareWord(EntityNode node1, EntityNode node2) {
		double similarCount = 0.0D;
		assert (node1.entityNodeFeatArray.length == node2.entityNodeFeatArray.length);

	//	if((node1.entityNodeFeatArray[0].equalsIgnoreCase("Martinelli")) &&
	//	(node2.entityNodeFeatArray[0].equalsIgnoreCase("Martinelli") ))
	//	{
	//		System.out.println(node1.entityNodeFeatArray[0]+"\t" + node2.entityNodeFeatArray[0]);
	//	}
		
		if ((node1.entityNodeFeatArray[2] != null)
				&& (node2.entityNodeFeatArray[2] != null)
				&& (node1.entityNodeFeatArray[2]
						.compareToIgnoreCase(node2.entityNodeFeatArray[2]) == 0)) {
		//	for (int i = 0; i < node1.entityNodeFeatArray.length-1; ++i)
			for (int i = 0; i < 4; ++i)
			{
			//	 if(  i == 1 || i == 3 )
			//	 {
			//		 continue;
			//	 }

				if ((node1.entityNodeFeatArray[i] == null)
						|| (node2.entityNodeFeatArray[i] == null)
						|| (node1.entityNodeFeatArray[i]
								.compareToIgnoreCase(node2.entityNodeFeatArray[i]) != 0)) 
				{
					continue;
				}
				else
				{
					if ((node1.entityNodeFeatArray[i].equalsIgnoreCase("NO-DEP"))
							&& (node2.entityNodeFeatArray[i].equalsIgnoreCase("NO-DEP")))
							{
								
								continue ;
							}
					else if ((node1.entityNodeFeatArray[i].equalsIgnoreCase("BLANK"))
							&& (node2.entityNodeFeatArray[i].equalsIgnoreCase("BLANK")))
							{
								
								continue ;
							}
					else
					{
						similarCount += 1.0D;
					}
				}
			}

			if (((node1.entityNodeFeatArray[2].charAt(0) == 'V') && (node2.entityNodeFeatArray[2]
					.charAt(0) == 'V'))
					|| ((node1.entityNodeFeatArray[2].charAt(0) == 'N') && (node2.entityNodeFeatArray[2]
							.charAt(0) == 'N'))) {
				String text1 = node1.entityNodeFeatArray[0];
				String text2 = node2.entityNodeFeatArray[0];
				String pos1 = Character.toString(node1.entityNodeFeatArray[2]
						.charAt(0));
				String pos2 = Character.toString(node2.entityNodeFeatArray[2]
						.charAt(0));
				similarCount += wnHandlerObj.compHypernyms(text1, pos1, text2,
						pos2);
				
			}


/*
			
			 
			 if ((node1.entityNodeFeatArray[2].charAt(0) == 'V') &&
			 (node2.entityNodeFeatArray[2].charAt(0) == 'V')) 
			 { 
				 String text1 =
					 node1.entityNodeFeatArray[0]; String text2 =
						 node2.entityNodeFeatArray[0]; similarCount +=
							 verbNetWrapperObj.areINsameCLASS(text1, text2);
			 }
*/			 
			 
		}

		return similarCount;
	}
	
	public double compareSarc(EntityNode node1, EntityNode node2) {
		double similarCount = 0.0D;
		assert (node1.entityNodeFeatArray.length == node2.entityNodeFeatArray.length);

		
		for (int i = 0; i < 5; ++i)
		{
			if (
					 (node1.entityNodeFeatArray[i]
							.compareToIgnoreCase(node2.entityNodeFeatArray[i]) == 0)) 
					similarCount += 1.0D;
					
				
		}

		return similarCount;
	}


	public double compareEntity(EntityNode node1, EntityNode node2) {
		double similarCount = 0.0D;
		assert (node1.entityNodeFeatArray.length == node1.entityNodeFeatArray.length);

		if ((node1.entityNodeFeatArray[2] != null)
				&& (node2.entityNodeFeatArray[2] != null)
				&& (node1.entityNodeFeatArray[2]
						.compareToIgnoreCase(node2.entityNodeFeatArray[2]) == 0)) {
		//	 for (int i = 0; i < node1.entityNodeFeatArray.length; ++i)
			for (int i = 0; i < 1; ++i)
			{
				if(! ( i == 0 || i == 8   ))
				{
					continue ;
				}
				if ((node1.entityNodeFeatArray[i] == null)
						|| (node2.entityNodeFeatArray[i] == null)
						|| (node1.entityNodeFeatArray[i]
								.compareToIgnoreCase(node2.entityNodeFeatArray[i]) != 0))
				{
					continue ;
				}
				else if ((node1.entityNodeFeatArray[i].equalsIgnoreCase("BLANK"))
						&& (node2.entityNodeFeatArray[i].equalsIgnoreCase("BLANK")))
				{
							
							continue ;
				}
				else
				{
					similarCount += 1.0D;
				}
				                                                          				
			}
				
		}
/*		
		if (((node1.entityNodeFeatArray[2].charAt(0) == 'V') && (node2.entityNodeFeatArray[2].charAt(0) == 'V'))
				   || ((node1.entityNodeFeatArray[2].charAt(0) == 'N') && (node2.entityNodeFeatArray[2].charAt(0) == 'N'))) {
				    String text1 = node1.entityNodeFeatArray[0];
				    String text2 = node2.entityNodeFeatArray[0];
				    String pos1 = Character.toString(node1.entityNodeFeatArray[2].charAt(0));
				    String pos2 = Character.toString(node2.entityNodeFeatArray[2].charAt(0));
				    similarCount += wnHandlerObj.compHypernyms(text1, pos1, text2, pos2);
				     
		}
*/
		return similarCount;
	}

	public void writeHypernyms(String entityDir) throws IOException 
	{
		// TODO Auto-generated method stub
		wnHandlerObj.dumpHypernyms(entityDir);
	}
	
	public void readeHypernyms(String entityDir) throws IOException 
	{
		// TODO Auto-generated method stub
		wnHandlerObj.loadHypernyms(entityDir);
	}
}