package com.kernel;

import com.thomson.research.svm.EntityNode;
import com.thomson.research.svm.UrduEmotionNode;
import com.thomson.research.svm.UrduEntityNode;
import com.thomson.research.svm.WordNode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * This code manages the different types of string/relation kernels code
 * Note - this is unusued (for most of the part) since the output is Kgram matrix (directly)
 * which is not the best way to handle such data. We also have code that directly use SVM (libsvm) library
 *  
 */


public class SarcStringKernelHandler
{
	private static final boolean REMOVE_OTHER_ENTITY = false;
	private static final int ENTITY_COMP = 1;
	private static final int ENTITY_COND = 2;
	private EntityStringKernel BCSKObject;
	private List<EntityNode[][]> TrainChunkVec;
	private List<String> TrainCategoryVec;
	private List<EntityNode[][]> TestChunkVec;
	private List<String> TestCategoryVec;
	private String kernelTrainingFile;
	private String kernelTestingFile;
	private final int TRAIN = 1;
	private final int TEST = 2;
	private EntitySVMParameters SVMParamObj;
	private EntityFeatureHandler entFeatHndlrObj;
	private WordComparator wordCompObj;
	protected double relWt = 0.0D;
	protected double entWt = 0.0D;
	protected double bowWt = 0.0D;
	private LinearKernel LKObj;
	private Log logger;
	private ArrayList TrainUrduEmotionVec;
	private ArrayList TestUrduEmotionVec;
	private List<String> allWordList ;

	public SarcStringKernelHandler(EntityFeatureHandler efHandle,
			EntitySVMParameters param) 
	{
		this.entFeatHndlrObj = efHandle;
		this.SVMParamObj = param;
		
		
		this.wordCompObj = new WordComparator(this.entFeatHndlrObj);
		
		
		if (this.SVMParamObj.STRING_KERNEL_TYPE == 0)
			this.BCSKObject = new GapSequenceStringKernel(this.wordCompObj);
		else if (this.SVMParamObj.STRING_KERNEL_TYPE == 1)
			this.BCSKObject = new SpectrumKernel(this.wordCompObj);
		this.LKObj = new LinearKernel();

		this.logger = LogFactory.getLog(super.getClass());

		//either comp or cond
		this.BCSKObject.setEntityKernelType(ENTITY_COMP);
		
		allWordList = new ArrayList<String>() ;
	}

	public SarcStringKernelHandler(EntityFeatureHandler efHandle,
			double relWt, double entWt) {
		this.entFeatHndlrObj = efHandle;
		this.wordCompObj = new WordComparator(this.entFeatHndlrObj);
		this.BCSKObject = new GapSequenceStringKernel(this.wordCompObj);
		this.relWt = relWt;
		this.entWt = entWt;
		this.bowWt = entWt;
		this.LKObj = new LinearKernel();
		this.logger = LogFactory.getLog(super.getClass());
	}


	public void kernelInterface(String entityDir, String folder,
			String trainingFile, String testingFile, double relWt, double neWt, double bowWt)
			throws IOException, Exception
	{
		//entityDir = entityDir + folder + "/";

		this.logger.info(" training file reading started..."+"\t"+trainingFile);

		kernelReadInputFile(entityDir, folder, trainingFile, 1);

		this.logger.info("  training file reading completed...");
		
		this.logger.info(" testing file reading started..."+"\t"+testingFile);

		kernelReadInputFile(entityDir, folder, testingFile, 2);

		this.logger.info(" test file reading completed... ");

		System.out.println("the wt are  rel = " + relWt + " bow = "
				+ bowWt + " neWt = " + neWt);

//		createTrainingKGramMatrix(entityDir, folder, trainingFile, relWt, neWt, bowWt);

		this.logger.info(" training gram-matrix completed ");

		createTestingKGramMatrix(entityDir, folder, testingFile, relWt, neWt, bowWt);

		this.logger.info(" testing gram-matrix completed ");
	}

	public void kernelReadInputFile(String entityDir, String folder,
			String entityFileNm, int type) throws IOException, Exception
	{
		BufferedReader reader = null;
		int linenum = 0;
		if (type == 1)
		{
			TrainChunkVec = new ArrayList<EntityNode[][]>();
			TrainCategoryVec = new ArrayList<String>();
			
			reader = new BufferedReader(new InputStreamReader( new FileInputStream (entityDir + "/" + folder + "/"  
					+ entityFileNm  +".train" + folder),"UTF8") );
		} 
		else if (type == 2) 
		{
			TestChunkVec = new ArrayList<EntityNode[][]>();
			TestCategoryVec = new ArrayList<String>();
			reader = new BufferedReader(new InputStreamReader (new  FileInputStream (entityDir + "/" + folder + "/" 
					+ entityFileNm +".test" + folder),"UTF8") );
		}
		
		String eachLine;
		
		int context_present = 0 ;
		int sarc_present = 0 ;
		int notsarc_present = 0 ;
		
		while (true)
		{
			eachLine = reader.readLine();
			
		//	
			
			if ( eachLine == null )
			{
				break;
			}
			++linenum;

			EntityNode[][] RelationPatternNodes = new EntityNode[1][];

			String[] results = eachLine.split("\t");
			
			if ( results.length !=4)
			{
				continue ;
			}
			
			String category = results[1].trim() ; //result[1] because we are adding the fold position in the data
			String context = results[2] ;
			String message = results[3] ;
			//just checking for the sake of correctness
			if ( category.equalsIgnoreCase("sarc"))
			{
				category = "1" ;
				sarc_present++ ;
			}
			else
			{
				category = "0" ;
				notsarc_present++;
			}
			
			if (type == 1)
			{
				TrainCategoryVec.add(category + " ");
			}
			else if (type == 2) 
			{
				TestCategoryVec.add(category + " ");
			}
	//		if (linenum == 689)
	//		{
	//			System.out.println("here");
	//		}

			createSarcPattern(RelationPatternNodes, message) ;
	//		createRelationPatterns(RelationPatternNodes, eachLine, "Before", 0);
	//		createRelationPatterns(RelationPatternNodes, eachLine, "Intra", 1);
	//		createRelationPatterns(RelationPatternNodes, eachLine, "After", 2);/

	//		createEntityPatterns(RelationPatternNodes, eachLine, "Entity1", 3);
	//		createEntityPatterns(RelationPatternNodes, eachLine, "Entity2", 4);
			
	//		createBoWPatterns(RelationPatternNodes,eachLine);

			if (type == 1)
			{
				TrainChunkVec.add(RelationPatternNodes);
			} else
			{
				if (type != 2)
					continue;
				
				TestChunkVec.add(RelationPatternNodes);
			}
		}

		reader.close();
		
		System.out.println("sarc size " + sarc_present) ;
		System.out.println("notsarc size " + notsarc_present) ;
		
	}

	public void kernelReadEntityInputFile(String entityDir, String folder,
			String entityFileNm, int type) throws IOException, Exception
	{
		BufferedReader reader = null;
		int linenum = 0;
		if (type == 1)
		{
			TrainChunkVec = new ArrayList<EntityNode[][]>();
			TrainCategoryVec = new ArrayList<String>();
			
			reader = new BufferedReader(new InputStreamReader( new FileInputStream (entityDir + "/"
					+ entityFileNm  +".train" + folder),"UTF8") );
		} 
		else if (type == 2) 
		{
			TestChunkVec = new ArrayList<EntityNode[][]>();
			TestCategoryVec = new ArrayList<String>();
			reader = new BufferedReader(new InputStreamReader (new  FileInputStream (entityDir + "/"
					+ entityFileNm +".test" + folder),"UTF8") );
		}
		
		String eachLine;
		while (true)
		{
			eachLine = reader.readLine();
			if ( eachLine == null )
			{
				break;
			}
			++linenum;

			EntityNode[][] RelationPatternNodes = new EntityNode[3][];

			String[] result = eachLine.split("\\s+");
			String category = result[1].trim() ;
			
			//not doing both
	//		if ( category.equalsIgnoreCase("2"))
	//		{
	//			category = "1" ;
	//		}
			int val = Integer.valueOf(category);
			if ( val > 2 )
			{
				System.out.println("here");
			}
			
			
			if (type == 1)
			{
				TrainCategoryVec.add(category + " ");
			}
			else if (type == 2) 
			{
				TestCategoryVec.add(category + " ");
			}
			
			
			if (type == 1) //training
			{
				createEntityRelationPatterns(RelationPatternNodes, eachLine, "Before", 0);
				createEntityPatterns(RelationPatternNodes, eachLine, "Entity", 2);
				createEntityRelationPatterns(RelationPatternNodes, eachLine, "After", 1);
			}
			else if ( type == 2 ) //testing - for entity 1
			{
				createEntityRelationPatterns(RelationPatternNodes, eachLine, "Before", 0);
				createEntityPatterns(RelationPatternNodes, eachLine, "Entity1", 2);
				createEntityRelationPatterns(RelationPatternNodes, eachLine, "Intra", 1,4);
			}
			
			else if ( type == 3 ) //testing - for entity 2
			{
				createEntityRelationPatterns(RelationPatternNodes, eachLine, "Intra", 0,-4);
				createEntityPatterns(RelationPatternNodes, eachLine, "Entity2", 2);
				createEntityRelationPatterns(RelationPatternNodes, eachLine, "After", 1);
			}
			
	//		createBoWPatterns(RelationPatternNodes,eachLine);

			if (type == 1)
			{
				TrainChunkVec.add(RelationPatternNodes);
			} else
			{
				if (type != 2)
					continue;
				
				TestChunkVec.add(RelationPatternNodes);
			}
		}

		reader.close();
	}
	
	
	public void createEntityTrainingKGramMatrix (String entityDir, String fileNm,
			double relWt, double neWt, double bowWt) throws IOException, Exception 
			{
		this.kernelTrainingFile = (entityDir + fileNm + ".train" + ".kGramMatrix");

		BufferedWriter writer = new BufferedWriter(new FileWriter(
				kernelTrainingFile));
		List<EntityNode[][]> rowVector = TrainChunkVec;
		List<EntityNode[][]> columnVector = TrainChunkVec;

		int rowLen = rowVector.size();
		int colLen = columnVector.size();
		double[][] kernelScores = new double[rowLen][colLen];

		for (int i = 0; i < rowLen; ++i)
		{
			for (int j = 0; j < rowLen; ++j)
			{
				kernelScores[i][j] = -1.0D;
			}

		}

		this.logger.info(" number of data instances: " + rowLen);

		for (int i = 0; i < rowLen; ++i) 
		{
			EntityNode[][] dataPoint1 = rowVector.get(i);

			
			for (int j = i; j < colLen; ++j)
			{
				EntityNode[][] dataPoint2 = columnVector.get(j);

				int running = 0 ;
				double finalRet = 0.0D;
				double relRet = 0.0D;
				double bowRet = 0.0D;
				double neRet = 0.0D;
				if (relWt > 0.0D)
				{
					
					relRet = runEntityJointKernel(dataPoint1, dataPoint2, i, j);
					running = running+1;
				}

				if (neWt > 0.0D)
				{
				//	neRet = runEntityKernel(dataPoint1, dataPoint2, i, j);
					neRet = runEntitySeparateKernel(dataPoint1, dataPoint2, i, j);
					running = running+1 ;
				}
				
				if (bowWt > 0.0D)
				{
			//		EntityNode[] firstPoly = getLKNodes(dataPoint1);
			//		EntityNode[] secondPoly = getLKNodes(dataPoint2);
					bowRet = runBoWKernel(dataPoint1[5], dataPoint2[5], i, j);
					running = running + 1;
				}

			//	finalRet = relWt * relRet + neWt * neRet +  bowWt * bowRet;
				finalRet = ( relRet + neRet +   bowRet)/(double)running;
				kernelScores[i][j] = finalRet;
			}
			if (( i % 20 ) == 0)
			{
					logger
							.info(" line number " + (i + 1) + " : train gram matrix ready ");
			}
		}

		for (int i = 0; i < rowLen; ++i)
		{
			for (int j = 0; j < rowLen; ++j)
			{
				if (kernelScores[i][j] == -1.0D)
				{
					kernelScores[i][j] = kernelScores[j][i];
				}
			}
		}
		writeTrainingKernelMatrix(kernelScores, rowLen, writer);

			}
	
	
	public void createTrainingKGramMatrix(String entityDir, String folder, String fileNm,
			double relWt, double neWt, double bowWt)
			throws IOException, Exception 
	{
		
		//do we have the hyperynyms?
	//	wordCompObj.readeHypernyms(entityDir+"/"+folder+"/");
		
		this.kernelTrainingFile = (entityDir + "/" + folder + "/" + fileNm + ".train" + ".kGramMatrix");

		BufferedWriter writer = new BufferedWriter(new FileWriter(
				kernelTrainingFile));
		List<EntityNode[][]> rowVector = TrainChunkVec;
		List<EntityNode[][]> columnVector = TrainChunkVec;

		int rowLen = rowVector.size();
		int colLen = columnVector.size();
		double[][] kernelScores = new double[rowLen][colLen];

		for (int i = 0; i < rowLen; ++i)
		{
			for (int j = 0; j < rowLen; ++j)
			{
				kernelScores[i][j] = -1.0D;
			}

		}

		this.logger.info(" number of data instances: " + rowLen);

		for (int i = 0; i < rowLen; ++i) 
		{
			EntityNode[][] dataPoint1 = rowVector.get(i);

			
			for (int j = i; j < colLen; ++j)
			{
				EntityNode[][] dataPoint2 = columnVector.get(j);

				int running = 0 ;
				double finalRet = 0.0D;
				double relRet = 0.0D;
				double bowRet = 0.0D;
				double neRet = 0.0D;

			//	if ( j == 689 )
			//	{
			//		System.out.println("i = "+i + " j = " + j);
					
			//	}
				if (relWt > 0.0D)
				{
					relRet = runRelationKernel(dataPoint1, dataPoint2, i, j);
					running = running+1;
				}
			/*	

				if (neWt > 0.0D)
				{
				
					neRet = runEntityKernel(dataPoint1, dataPoint2, i, j);
					running = running+1 ;
				}
				
				if (bowWt > 0.0D)
				{
			//		EntityNode[] firstPoly = getLKNodes(dataPoint1);
			//		EntityNode[] secondPoly = getLKNodes(dataPoint2);
					bowRet = runBoWKernel(dataPoint1[5], dataPoint2[5], i, j);
					
					if (Double.isNaN(bowRet))
				    {
				        System.out.println("NaN result!");
				    }
				    if (Double.isInfinite(bowRet))
				    {
				    	System.out.println("Result is Infinite");
				    }
					
					running = running + 1;
				}
				
		//		relWt = 0.35 ;
		//		neWt = 0.35 ; 
		//		bowWt = 0.3 ;
*/
		//		finalRet = relWt * relRet + neWt * neRet +  bowWt * bowRet;
				finalRet = ( relRet + neRet +   bowRet)/(double)running;
				kernelScores[i][j] = finalRet;
			}
			if (( i % 20 ) == 0)
			{
					logger
							.info(" line number " + (i + 1) + " : train gram matrix ready ");
			}
		}

		for (int i = 0; i < rowLen; ++i)
		{
			for (int j = 0; j < rowLen; ++j)
			{
				if (kernelScores[i][j] == -1.0D)
				{
					kernelScores[i][j] = kernelScores[j][i];
				}
			}
		}
		writeTrainingKernelMatrix(kernelScores, rowLen, writer);
	}

	private double runBoWKernel(EntityNode[] dataPoint1,
			EntityNode[] dataPoint2, int i, int j) 
	{
		// TODO Auto-generated method stub
		return LKObj.unnormalizedLK(dataPoint1, dataPoint2, i, j);
	}

	public void createEntityTestingKGramMatrix(String entityDir, String fileNm,
			double relWt,  double neWt, double bowWt)
			throws IOException, Exception {
		this.kernelTestingFile = (entityDir + fileNm + ".test" + ".kGramMatrix");

		BufferedWriter writer = new BufferedWriter(new FileWriter(
				this.kernelTestingFile));
		List<EntityNode[][]> rowVector = this.TestChunkVec;
		List<EntityNode[][]> columnVector = this.TrainChunkVec;
		List<String> categVector = this.TestCategoryVec;

		int rowLen = rowVector.size();
		int colLen = columnVector.size();

		this.logger.info(" number of test data instances: " + rowLen);

		for (int i = 0; i < rowLen; ++i) 
		{
			EntityNode[][] dataPoint1 = (EntityNode[][]) rowVector.get(i);
			String label1 = (String) categVector.get(i);

			String result = label1 + " " + "0:" + (i + 1) + " ";

			for (int j = 0; j < colLen; ++j)
			{
				EntityNode[][] dataPoint2 = (EntityNode[][]) columnVector
						.get(j);

				double finalRet = 0.0D;
				double relRet = 0.0D;
				double bowRet = 0.0D;
				double neRet = 0.0D;
				int running = 0;
				if (relWt > 0.0D)
				{
					relRet = runEntityJointKernel(dataPoint1, dataPoint2, j);
					running = running + 1 ;
				}

				if (neWt > 0.0D)
				{
					neRet = runEntitySeparateKernel(dataPoint1, dataPoint2, j);
					running = running + 1 ;
				}

				if (bowWt > 0.0D)
				{
					bowRet = runBoWKernel(dataPoint1[5], dataPoint2[5], j);
					running = running + 1;
				}

				
			//	finalRet = relWt * relRet + neWt * neRet + bowWt * bowRet ;
				finalRet = (relRet + neRet +  bowRet)/(double)running ;


				result = result + (j + 1) + ":" + (float) finalRet + " ";
			}

			if (( i % 100 ) == 0)
			{
					logger
							.info(" line number " + (i + 1) + " : test gram matrix ready ");
			}
			
			writer.write(result);
			writer.newLine();
		}
		writer.close();
		
		
	}

	
	public void createTestingKGramMatrix(String entityDir, String folder, String fileNm,
			double relWt,  double neWt, double bowWt)
			throws IOException, Exception 
			{
		this.kernelTestingFile = (entityDir + "/" + folder + "/" + fileNm + ".test" + ".kGramMatrix");

		BufferedWriter writer = new BufferedWriter(new FileWriter(
				this.kernelTestingFile));
		List<EntityNode[][]> rowVector = this.TestChunkVec;
		List<EntityNode[][]> columnVector = this.TrainChunkVec;
		List<String> categVector = this.TestCategoryVec;

		int rowLen = rowVector.size();
		int colLen = columnVector.size();

		this.logger.info(" number of test data instances: " + rowLen);

		for (int i = 0; i < rowLen; ++i) 
		{
			EntityNode[][] dataPoint1 = (EntityNode[][]) rowVector.get(i);
			String label1 = (String) categVector.get(i);

			String result = label1 + " " + "0:" + (i + 1) + " ";
			
	//		if(i==307)
	//		{
	//			System.out.println("here");
	//		}

			for (int j = 0; j < colLen; ++j)
			{
				EntityNode[][] dataPoint2 = (EntityNode[][]) columnVector
						.get(j);
				double finalRet = 0.0D;
				double relRet = 0.0D;
				double bowRet = 0.0D;
				double neRet = 0.0D;
				int running = 0;
				if (relWt > 0.0D)
				{
					relRet = runRelationKernel(dataPoint1, dataPoint2, j);
					running = running + 1 ;
				}

				if (neWt > 0.0D)
				{
					neRet = runEntityKernel(dataPoint1, dataPoint2, j);
					running = running + 1 ;
				}

				if (bowWt > 0.0D)
				{
					bowRet = runBoWKernel(dataPoint1[5], dataPoint2[5], j);
					running = running + 1;
				}

				if (Double.isNaN(bowRet))
			    {
			        System.out.println("NaN result!");
			    }
			    if (Double.isInfinite(bowRet))
			    {
			    	System.out.println("Result is Infinite");
			    }
				
		//	    relWt = 0.35 ;
		//		neWt = 0.35 ;
		//		bowWt = 0.3 ;
			    
			//	finalRet = relWt * relRet + neWt * neRet + bowWt * bowRet ;
				finalRet = (relRet + neRet +  bowRet)/(double)running ;


				result = result + (j + 1) + ":" + (float) finalRet + " ";
			}

			if (( i % 100 ) == 0)
			{
					logger
							.info(" line number " + (i + 1) + " : test gram matrix ready ");
			}
			
			writer.write(result);
			writer.newLine();
		}
		writer.close();
		
		//write the hypernyms
		wordCompObj.writeHypernyms(entityDir+"/"+folder+"/");
		
	}

	private double runBoWKernel(EntityNode[] dataPoint1,
			EntityNode[] dataPoint2, int j) 
	{
		// TODO Auto-generated method stub
		return LKObj.unnormalizedLK(dataPoint1, dataPoint2, j);
	}

	public void writeTrainingKernelMatrix(double[][] kernelScores, int rowLen,
			BufferedWriter writer) throws IOException {
		for (int i = 0; i < rowLen; ++i) {
			String result = (String) this.TrainCategoryVec.get(i) + " " + "0:"
					+ (i + 1) + " ";
			for (int j = 0; j < rowLen; ++j) {
				result = result + (j + 1) + ":" + (float) kernelScores[i][j]
						+ " ";
			}
			writer.write(result);
			writer.newLine();
		}

		writer.close();
	}
	
	
	public void createSarcPattern(EntityNode[][] RelationPatternNodes,
			String eachLine) throws Exception
	{
		String[] argText = new String[0];
		String[] argPOS = new String[0];
		String[] argChunk = new String[0];
		String[] argNE = new String[0];
		String[] argPosn = new String[0];
		String[] argDep = new String[0];

		RelationPatternNodes[0] = entFeatHndlrObj
					.createSarcRelationArgAttributes(eachLine,argText, argPOS,
							argChunk, argNE, argDep,argPosn);
	}

	public void createRelationPatterns(EntityNode[][] RelationPatternNodes,
			String eachLine, String marker, int idx) throws Exception
	{
		String[] argText = new String[0];
		String[] argPOS = new String[0];
		String[] argChunk = new String[0];
		String[] argNE = new String[0];
		String[] argPosn = new String[0];
		String[] argDep = new String[0];

		String pattern = EntityStringHelper.getRelationArg(eachLine, marker);
		pattern = pattern.trim();
		
		if(REMOVE_OTHER_ENTITY)
		{
			pattern = removeOtherEntity(pattern);
		}
		
		
		if (pattern.contains("|"))
			RelationPatternNodes[idx] = entFeatHndlrObj
					.createRelationArgAttributes(pattern, argText, argPOS,
							argChunk, argNE, argDep,argPosn);
		else
			RelationPatternNodes[idx] = entFeatHndlrObj
					.returnRelationBlankNode();
	}
	
	private String removeOtherEntity(String pattern)
	{
		// TODO Auto-generated method stub
		String tokens[] = pattern.split("\\s++");
		String ret = " ";
		for ( String token : tokens )
		{
			String features[] = token.split("\\|");
			if ( features.length > 1 )
			{
				String ne = features[3];
				if ( ne.equalsIgnoreCase("Peop") || ne.equalsIgnoreCase("Org") || ne.equalsIgnoreCase("Loc"))
				{
					continue ;
				}
			}
			
			ret += token + " ";
		}
		
		ret = ret.trim();
		return ret ;
	}

	public void createEntityRelationPatterns(EntityNode[][] RelationPatternNodes,
			String eachLine, String marker, int idx, int num) throws Exception
	{
		String[] argText = new String[0];
		String[] argPOS = new String[0];
		String[] argChunk = new String[0];
		String[] argNE = new String[0];
		String[] argPosn = new String[0];
		

		String pattern = EntityStringHelper.getRelationArg(eachLine, marker);
		pattern = getPatternForSeparateEntity(pattern,num);
		if (pattern.contains("|"))
			RelationPatternNodes[idx] = entFeatHndlrObj
					.createRelationArgAttributes(pattern, argText, argPOS,
							argChunk, argNE,argPosn);
		else
			RelationPatternNodes[idx] = entFeatHndlrObj
					.returnRelationBlankNode();
	}

	
	private String getPatternForSeparateEntity(String pattern, int num) 
	{
		// TODO Auto-generated method stub
		String ret = " ";
		String features[] = pattern.split("\\s++");
		if ( num == 4 )
		{
			int min = Math.min(features.length, 4);
			for ( int i = 0 ;i < min ;i++)
			{
				ret += features[i]+" ";
			}
			
		}
		if ( num == -4 )
		{
			int min = Math.min(features.length, 4);
			for ( int i = features.length-min ;i < features.length ;i++)
			{
				ret += features[i]+" ";
			}
			
		}
		
		
		ret = ret.trim();
		return ret ;
	}

	public void createEntityRelationPatterns(EntityNode[][] RelationPatternNodes,
			String eachLine, String marker, int idx) throws Exception
	{
		String[] argText = new String[0];
		String[] argPOS = new String[0];
		String[] argChunk = new String[0];
		String[] argNE = new String[0];
		String[] argPosn = new String[0];
		

		String pattern = EntityStringHelper.getRelationArg(eachLine, marker);
		if (pattern.contains("|"))
			RelationPatternNodes[idx] = entFeatHndlrObj
					.createRelationArgAttributes(pattern, argText, argPOS,
							argChunk, argNE,argPosn);
		else
			RelationPatternNodes[idx] = entFeatHndlrObj
					.returnRelationBlankNode();
	}

	public void createEntityPatterns(EntityNode[][] RelationPatternNodes,
			String eachLine, String marker, int idx) throws Exception
	{
		String[] argText = new String[0];
		String[] argPOS = new String[0];
		String[] argChunk = new String[0];
		String[] argNE = new String[0];
		String[] argPosn = new String[0];
		String[] argDep = new String[0];
		String[] argSRLRole = new String[0];
		String[] argPred = new String[0] ;

		String pattern = EntityStringHelper.getRelationArg(eachLine, marker);

		if (pattern.contains("|")) {
			RelationPatternNodes[idx] = entFeatHndlrObj
					.createEntArgAttributes(pattern, argText, argPOS, argChunk,
							argNE, argDep,argSRLRole,argPred,argPosn);
		} else
			RelationPatternNodes[idx] = entFeatHndlrObj.returnBlankNode();
	}

	public void createBoWPatterns(EntityNode[][] RelationPatternNodes,
			String eachLine, String marker, int idx) throws Exception
	{
		String bow = EntityStringHelper.getRelationArg(eachLine, marker);

		if (bow.indexOf(':') != -1)
			RelationPatternNodes[5] = this.entFeatHndlrObj
					.createBowFeature(bow);
		else
			RelationPatternNodes[5] = this.entFeatHndlrObj.returnBlankNode();
	}
/*
	public void createBoWPatterns(EntityNode[][] RelationPatternNodes,
			String eachLine) throws Exception
	{
		//first we need to parse the "only text" part
		String onlyText = getText(eachLine);
		String features[] = onlyText.split("\\s++");
		
		EntityNode node ;
		int index = 0 ;
		Set<Integer> posnSet = new HashSet<Integer>();
		for ( String feature : features )
		{
			int posn = allWordList.indexOf(feature.toLowerCase());
			if ( -1 == posn)
			{
				allWordList.add(feature.toLowerCase());
				posn = allWordList.size()-1 ;
			}
			
			posnSet.add(posn);		
		}
		
		
		List<Integer> posnList = new ArrayList<Integer>(posnSet);
		java.util.Collections.sort(posnList);
		EntityNode[] tempNodes = new EntityNode[posnList.size()] ;
		for ( Integer posn : posnList)
		{
			node = new EntityNode();
			node.index = posn ;
			node.value = 1.0 ;
			tempNodes[index] = node ;
			index++ ;
		}
		//RelationPatternNodes[5][] 
		//System.arraycopy(tempNodes, 0, RelationPatternNodes[5], 0, tempNodes.length);

		RelationPatternNodes[5] = tempNodes ;
		
		
	}
*/
	/*
	public void createBoWPatterns(EntityNode[][] RelationPatternNodes,
			String eachLine) throws Exception
	{
		//first we need to parse the "only text" part
		String features[] = eachLine.split("\\s++");
		int entityDistance = entityDistance(features);
		
		EntityNode node ;
		int index = 0 ;
		
		EntityNode[] tempNodes = new EntityNode[10] ;
		for ( int i = 1 ;i <= 10; i++)
		{
			node = new EntityNode();
			node.index = i ;
			node.value = getValue(entityDistance,i) ;
			tempNodes[index] = node ;
			index++ ;
		}
		//RelationPatternNodes[5][] 
		//System.arraycopy(tempNodes, 0, RelationPatternNodes[5], 0, tempNodes.length);

		RelationPatternNodes[5] = tempNodes ;
		
		
	}
	*/
	public void createBoWPatterns(EntityNode[][] RelationPatternNodes,
			String eachLine) throws Exception
	{
		//first we need to parse the "only text" part
		int fvPosn = eachLine.indexOf("|FV|") ;
		eachLine = eachLine.substring(fvPosn+4, eachLine.length()) ;
		eachLine = eachLine.trim();
		
		String features[] = eachLine.split("\\s++");
		EntityNode node ;
		int index = 0 ;
		
	//	EntityNode[] tempNodes = new EntityNode[features.length-1] ;
		EntityNode[] tempNodes = new EntityNode[features.length] ;
		for ( int i = 0 ; i < features.length ; i++)
		{
			String bow = features[i] ;
			int idx = Integer.valueOf(bow.split(":")[0]) ;
			double val = Double.valueOf(bow.split(":")[1]) ;
			
			node = new EntityNode(idx,val);
			tempNodes[index] = node ;
			index++ ;
		}
		//RelationPatternNodes[5][] 
		//System.arraycopy(tempNodes, 0, RelationPatternNodes[5], 0, tempNodes.length);

		RelationPatternNodes[5] = tempNodes ;
		
		
	}
	

	private int getValue(int entityDistance, int i)
	{
	// TODO Auto-generated method stub
		
		switch(i)
		{
			case 1:
			if(0<= entityDistance && entityDistance <=5)
				return 1 ;
			else
				return 0 ;
			case 2:
			if(6<= entityDistance && entityDistance <=10)
				return 1 ;
			else
				return 0 ;
			case 3:
			if(11<= entityDistance && entityDistance <=15)
				return 1 ;
			else
				return 0 ;
			case 4:
			if(16<= entityDistance && entityDistance <=20)
				return 1 ;
			else
				return 0 ;
			case 5:
			if(21<= entityDistance && entityDistance <=25)
				return 1 ;
			else
				return 0 ;
			case 6:
			if(26<= entityDistance && entityDistance <=30)
				return 1 ;
			else
				return 0 ;
			case 7:
			if(31<= entityDistance && entityDistance <=35)
				return 1 ;
			else
				return 0 ;
			case 8:
			if(36<= entityDistance && entityDistance <=40)
				return 1 ;
			else
				return 0 ;
			case 9:
			if(41<= entityDistance && entityDistance <=45)
				return 1 ;
			else
				return 0 ;
			case 10:
				if(46<= entityDistance )
					return 1 ;
				else
					return 0 ;
			
				
		}
		
	return 0;
	}

	private int entityDistance(String[] features) 
	{
		// TODO Auto-generated method stub
		int dist = 0;
		int index = 0 ;
		int ent1Posn = 0 ;
		int ent2Posn = 0;
		for ( String feature : features )
		{
			String tokens[] = feature.split("\\|");
			if ( tokens[0].contains("<Intra>"))
			{
				ent1Posn = index+1 ; //relation starts from the next token
			}
			
			if ( tokens[0].contains("</Intra>"))
			{
				ent2Posn = index ; //relation ends in the previous token
			}
			
			index++ ;
		}
		
		
		return Math.abs(ent1Posn-ent2Posn);
	}
	
	
	private String getText(String eachLine)
	{
		// TODO Auto-generated method stub
		String ret = " ";
		String features[] = eachLine.split("\\s+");
		
		//i == 1 because features[0] is the category
		for ( int i = 1 ; i < features.length ;i ++)
		{
			String feature = features[i];
			if (Markers.contains(feature))
			{
				continue ;
			}
			
			String text = feature.split("\\|")[0];
			ret = ret + text + " " ;
			
		}
		ret = ret.trim() ;
		return ret ;
	}
	
	

	
	public double kernelFunction(EntityNode[] first, EntityNode[] second,
			int i, int j) {
		EntityNode[][] dataPoint1 = getEntityPattern(first);
		EntityNode[][] dataPoint2 = getEntityPattern(second);

		double relRet = 0.0D;
		double bowRet = 0.0D;
		double neRet = 0.0D;

		relRet = runRelationKernel(dataPoint1, dataPoint2, i, j);

		double finalRet = this.relWt * relRet + bowRet + this.entWt * neRet;

		return finalRet;
	}

	private EntityNode[][] getEntityPattern(EntityNode[] first) {
		EntityNode[][] dataPoint1 = new EntityNode[5][];

		dataPoint1[0] = createRelationPatterns(first, "Before");
		dataPoint1[1] = createRelationPatterns(first, "Intra");
		dataPoint1[2] = createRelationPatterns(first, "After");
		dataPoint1[3] = createEntityPatterns(first, "Entity1");
		dataPoint1[4] = createEntityPatterns(first, "Entity2");

		return dataPoint1;
	}

	public double kernelFunction(EntityNode[] first, EntityNode[] second) {
		double finalRet = 0.0D;
		try {
			EntityNode[][] dataPoint1 = new EntityNode[5][];
			EntityNode[][] dataPoint2 = new EntityNode[5][];

			dataPoint1[0] = createRelationPatterns(first, "Before");
			dataPoint1[1] = createRelationPatterns(first, "Intra");
			dataPoint1[2] = createRelationPatterns(first, "After");
			dataPoint1[3] = createEntityPatterns(first, "Entity1");
			dataPoint1[4] = createEntityPatterns(first, "Entity2");

			dataPoint2[0] = createRelationPatterns(second, "Before");
			dataPoint2[1] = createRelationPatterns(second, "Intra");
			dataPoint2[2] = createRelationPatterns(second, "After");
			dataPoint2[3] = createEntityPatterns(second, "Entity1");
			dataPoint2[4] = createEntityPatterns(second, "Entity2");

			double relRet = 0.0D;
			double bowRet = 0.0D;
			double neRet = 0.0D;

		//	relRet = runRelationKernel(dataPoint1, dataPoint2);

			finalRet = this.relWt * relRet + bowRet + this.entWt * neRet;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return finalRet;
	}

	public EntityNode[] createRelationPatterns(EntityNode[] nodeList,
			String marker) {
		int j = 0;
		int begin = 0;
		int end = 0;

		for (int i = 0; i < nodeList.length; ++i) {
			if (nodeList[i].getOrigWord().indexOf("<" + marker + ">") > -1) {
				begin = i;
			}
			if (nodeList[i].getOrigWord().indexOf("</" + marker + ">") <= -1)
				continue;
			end = i;
			break;
		}

		EntityNode[] pattern = (EntityNode[]) null;
		if (end == begin + 1) {
			pattern = new EntityNode[1];
			pattern[0] = new EntityNode("BLANK");
		} else {
			pattern = new EntityNode[end - (begin + 1)];
			System.arraycopy(nodeList, begin + 1, pattern, 0, end - (begin + 1));
		}
		return pattern;
	}

	public EntityNode[] createEntityPatterns(EntityNode[] nodeList,
			String marker) {
		int j = 0;
		int begin = 0;
		int end = 0;

		for (int i = 0; i < nodeList.length; ++i) {
			if (nodeList[i].getOrigWord().indexOf("<" + marker) > -1) {
				begin = i;
			}
			if (nodeList[i].getOrigWord().indexOf("</" + marker) <= -1)
				continue;
			end = i;
			break;
		}

		EntityNode[] pattern = (EntityNode[]) null;
		if (end == begin + 1) {
			pattern = new EntityNode[1];
			pattern[0] = new EntityNode("BLANK");
		} else {
			pattern = new EntityNode[end - (begin + 1)];
			System.arraycopy(nodeList, begin + 1, pattern, 0, end - (begin + 1));
		}
		return pattern;
	}

	protected double runRelationKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int j) throws Exception {
	//	return this.BCSKObject.unnormalizedStringKernel(dataPoint1,
		//		dataPoint2, j);
		
		return this.BCSKObject.unnormalizedSarcStringKernel(dataPoint1,
				dataPoint2, j);
	}

	protected double runEntityKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i, int j)
	{
		return this.BCSKObject.unnormalizedEntityKernel(dataPoint1,
				dataPoint2, i, j);
	}
	
	protected double runEntitySeparateKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i, int j)
	{

		
		return this.BCSKObject.unnormalizedSeparateEntityKernel(dataPoint1,
				dataPoint2, i, j);
	}
	
	protected double runEntitySeparateKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i)
	{

		return this.BCSKObject.unnormalizedSeparateEntityKernel(dataPoint1,
				dataPoint2, i);
	}
	

	protected double runEntityKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int j) throws Exception {
		return this.BCSKObject.unnormalizedEntityKernel(dataPoint1,
				dataPoint2, j);
	}

	protected double runBoWKernel(UrduEmotionNode[] dataPoint1,
			UrduEmotionNode[] dataPoint2, int i, int j) throws Exception 
			{
		return this.LKObj.unnormalizedLK(dataPoint1, dataPoint2, i, j);
	}

	protected double runBoWKernel(UrduEmotionNode[] dataPoint1,
			UrduEmotionNode[] dataPoint2, int j) throws Exception {
		return this.LKObj.unnormalizedLK(dataPoint1, dataPoint2, j);
	}
	
	
	protected double runRelationKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i, int j) 
	{
	//	return this.BCSKObject.unnormalizedStringKernel(dataPoint1,
		//		dataPoint2, i, j);
		
		return this.BCSKObject.unnormalizedSarcStringKernel(dataPoint1,
				dataPoint2, i, j);
		
	}
	
	protected double runEntityJointKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i, int j) 
	{
	//	double ret = BCSKObject.unnormalizedRankingStringKernel(dataPoint1,
	//			dataPoint2, i, j);
		
		return BCSKObject.unnormalizedEntityRankingStringKernel(dataPoint1,
				dataPoint2, i, j);
	}
	
	protected double runEntityJointKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i) 
	{
		return BCSKObject.unnormalizedEntityRankingStringKernel(dataPoint1,
				dataPoint2, i);
	}
	
	/*

	protected double runRelationKernel(UrduEmotionNode[][] dataPoint1,
			UrduEmotionNode[][] dataPoint2) {
		double ret1 = this.BCSKObject.unnormalizedSelfStringKernel(dataPoint1);

		double ret2 = this.BCSKObject.unnormalizedSelfStringKernel(dataPoint2);

		double ret3 = this.BCSKObject.unnormalizedStringKernel(dataPoint1,
				dataPoint2);

		if ((ret1 == 0.0D) || (ret2 == 0.0D)) {
			return 0.0D;
		}
		return ret3 / (Math.sqrt(ret1) * Math.sqrt(ret2));
	}
*/
	protected double runBoWKernel(UrduEmotionNode[] dataPoint1,
			UrduEmotionNode[] dataPoint2) {
		double ret1 = this.LKObj.unnormalizedLK(dataPoint1);

		double ret2 = this.LKObj.unnormalizedLK(dataPoint2);

		double ret3 = this.LKObj.unnormalizedLK(dataPoint1, dataPoint2);

		if ((ret1 == 0.0D) || (ret2 == 0.0D)) {
			return 0.0D;
		}
		return ret3 / (Math.sqrt(ret1) * Math.sqrt(ret2));
	}

	protected double runRelationKernel(UrduEntityNode[] dataPoint1,
			UrduEntityNode[] dataPoint2) {
		double ret1 = this.BCSKObject.unnormalizedSelfStringKernel(dataPoint1);

		double ret2 = this.BCSKObject.unnormalizedSelfStringKernel(dataPoint2);

		double ret3 = this.BCSKObject.unnormalizedStringKernel(dataPoint1,
				dataPoint2);

		if ((ret1 == 0.0D) || (ret2 == 0.0D)) {
			return 0.0D;
		}

		return ret3 / (Math.sqrt(ret1) * Math.sqrt(ret2));
	}

	protected double runEntityKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2) {
		double ret1 = this.BCSKObject.unnormalizedSelfEntityKernel(dataPoint1);

		double ret2 = this.BCSKObject.unnormalizedSelfEntityKernel(dataPoint2);

		double ret3 = this.BCSKObject.unnormalizedEntityKernel(dataPoint1,
				dataPoint2);

		if ((ret1 == 0.0D) || (ret2 == 0.0D)) {
			return 0.0D;
		}
		return ret3 / (Math.sqrt(ret1) * Math.sqrt(ret2));
	}
/*
	protected double runBoWKernel(EntityNode[] BoW1, EntityNode[] BoW2) {
		double ret1 = this.LKObj.unnormalizedLK(BoW1, 0);

		double ret2 = this.LKObj.unnormalizedLK(BoW2, 0);

		double ret3 = this.LKObj.unnormalizedLK(BoW1, BoW2);

		return ret3 / (Math.sqrt(ret1) * Math.sqrt(ret2));
	}
*/	
}