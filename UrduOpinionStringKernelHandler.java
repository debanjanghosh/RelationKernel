package com.kernel;

import com.thomson.research.svm.EntityNode;
import com.thomson.research.svm.UrduEmotionNode;
import com.thomson.research.svm.UrduEntityNode;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UrduOpinionStringKernelHandler {
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

	public UrduOpinionStringKernelHandler(EntityFeatureHandler efHandle,
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

		this.BCSKObject.setEntityKernelType(2);
	}

	public UrduOpinionStringKernelHandler(EntityFeatureHandler efHandle,
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


	public void urduKernelTogetherCueInterface(String entityDir, String folder,
			String trainingFile, String testingFile, double relWt, double neWt)
			throws IOException, Exception 
	{
		entityDir = entityDir + folder + "/";
		this.logger.info(" training file reading started...");
		readUrduTogetherCueInputFile(entityDir, folder, trainingFile, 1);
		this.logger.info("  training file reading completed...");
		readUrduTogetherCueInputFile(entityDir, folder, testingFile, 2);
		this.logger.info(" test file reading completed... ");

		int relation = 1;
		int bow = 0;
		int entity = 0;

		System.out.println("the wt are  rel = " + relWt + " bow = "
				+ this.bowWt + " neWt = " + neWt);

		createUrduTogetherCueTrainingKGramMatrix(entityDir, trainingFile, relation, bow,
				entity, relWt, this.bowWt, neWt);

		this.logger.info(" training gram-matrix completed ");

		createUrduTogetherCueTestingKGramMatrix(entityDir, testingFile, relation, bow, entity,
				relWt, this.bowWt, neWt);

		this.logger.info(" testing gram-matrix completed ");
	}

	
	public void urduKernelSeparateCueInterface(String entityDir, String folder,
			String trainingFile, String testingFile, double relWt, double neWt)
			throws IOException, Exception 
	{
		entityDir = entityDir + "/"  + folder + "/";

		this.logger.info(" training file reading started in separate cue...");
		readUrduSeparateCueInputFile(entityDir, folder, trainingFile, 1);
		this.logger.info("  training file reading completed in separate cue...");
		readUrduSeparateCueInputFile(entityDir, folder, testingFile, 2);

		this.logger.info(" test file reading completed... ");

		int relation = 1;
		int bow = 0;
		int entity = 0;

		System.out.println("the wt are  rel = " + relWt + " bow = "
				+ this.bowWt + " neWt = " + neWt);

		createUrduSeparateCueTrainingKGramMatrix(entityDir, trainingFile, relation, bow,
				entity, relWt, this.bowWt, neWt);

		this.logger.info(" training gram-matrix completed ");

		createUrduSeparateCueTestingKGramMatrix(entityDir, testingFile, relation, bow, entity,
				relWt, this.bowWt, neWt);

		this.logger.info(" testing gram-matrix completed ");
	}
	



	@SuppressWarnings("unchecked")
	public void readUrduSeparateCueInputFile(String entityDir, String folder,
			String entityFileNm, int type) throws IOException, Exception 
	{
		BufferedReader reader = null;
		int linenum = 0;
		
		if (type == 1) 
		{
			this.TrainUrduEmotionVec = new ArrayList<UrduEmotionNode[][]>();
			this.TrainCategoryVec = new ArrayList <String>();
			
			logger.info(" training file is :" +entityDir + "/"
					+ entityFileNm + ".train" + folder ) ;
			 reader = 
				new BufferedReader(new InputStreamReader(new FileInputStream(
						entityDir + "/"	+ entityFileNm + ".train" + folder), "UTF-8"));
		
			
		} 
		else if (type == 2) 
		{
			this.TestUrduEmotionVec = new ArrayList<UrduEmotionNode[][]>();
			this.TestCategoryVec = new ArrayList<String>();
			
			
			logger.info(" testing file is :" +entityDir + "/"
					+ entityFileNm + ".test" + folder ) ;
			 reader = 
					new BufferedReader(new InputStreamReader(new FileInputStream(
							entityDir + "/"+ entityFileNm + ".test" + folder), "UTF-8"));
		
		}
		
		while ( true )
		{
			String eachLine = reader.readLine();
			if ( eachLine == null)
				break;
			
			UrduEmotionNode[][] RelationPatternNodes = new UrduEmotionNode[3][];

			String[] result = eachLine.split("\\s+");
			
			//we are taking everything together (as in 1 and 2 together
			//vs 0)
			
			if (result[0].equalsIgnoreCase("2"))
			{
				result[0] = "1" ;
			}
			
			if (type == 1)
			{
				this.TrainCategoryVec.add(result[0] + " ");
			}
			else if (type == 2)
			{
				this.TestCategoryVec.add(result[0] + " ");
			}
			
			//candidate
			createUrduEmotionPatterns(RelationPatternNodes, eachLine,0);
			//emotion
			createUrduEmotionPatterns(RelationPatternNodes, eachLine,1);
			//predicate
			createUrduEmotionPatterns(RelationPatternNodes, eachLine,2);

			if (type == 1) 
			{
				this.TrainUrduEmotionVec.add(RelationPatternNodes);
			}
			else
			{
				if (type != 2)
					continue;
				
				this.TestUrduEmotionVec.add(RelationPatternNodes);
			}
			linenum++ ;
		}
		System.out.println(" number of lines "+linenum);
		reader.close();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public void readUrduTogetherCueInputFile(String entityDir, String folder,
			String entityFileNm, int type) throws IOException, Exception 
	{
		BufferedReader reader = null;
		int linenum = 0;
		if (type == 1) 
		{
			this.TrainUrduEmotionVec = new ArrayList<UrduEmotionNode[][]>();
			this.TrainCategoryVec = new ArrayList();
			
			//reader = new BufferedReader(new FileReader(entityDir + "/"
				//	+ entityFileNm + ".train" + folder));
			 reader = 
				new BufferedReader(new InputStreamReader(new FileInputStream(entityDir + "/"
							+ entityFileNm + ".train" + folder), "UTF-8"));
		
			
		} 
		else if (type == 2) 
		{
			this.TestUrduEmotionVec = new ArrayList<UrduEmotionNode[][]>();
			this.TestCategoryVec = new ArrayList();
			
		//	reader = new BufferedReader(new FileReader(entityDir + "/"
			//		+ entityFileNm + ".test" + folder));
			 reader = 
					new BufferedReader(new InputStreamReader(new FileInputStream(entityDir + "/"
							+ entityFileNm + ".test" + folder), "UTF-8"));
			
		}
		
		while ( true )
		{
			String eachLine = reader.readLine();
			if ( eachLine == null)
				break;
			
			UrduEmotionNode[][] RelationPatternNodes ;
			

			String[] result = eachLine.split("\\s+");
			
			if (result[0].equalsIgnoreCase("0"))
			{
				continue ;
			}
			
	/*		
			if (result[0].equalsIgnoreCase("2"))
			{
				result[0] = "1" ;
			}
	*/		
			if (type == 1)
			{
				this.TrainCategoryVec.add(result[0] + " ");
			}
			else if (type == 2) 
			{
				this.TestCategoryVec.add(result[0] + " ");
			}
			
			if ( eachLine.contains("<Sequence>") && eachLine.contains("<lk>"))
			{
				RelationPatternNodes =  new UrduEmotionNode[2][];
				createUrduComboPatterns(RelationPatternNodes, eachLine,0,3);
				createUrduComboPatterns(RelationPatternNodes, eachLine,1,4);
			}
			
			else
			{
				RelationPatternNodes =  new UrduEmotionNode[1][];
				createUrduRelationPatterns(RelationPatternNodes, eachLine);
			}

			if (type == 1) 
			{
				this.TrainUrduEmotionVec.add(RelationPatternNodes);
			} 
			else 
			{
				if (type != 2)
					continue;
				
				this.TestUrduEmotionVec.add(RelationPatternNodes);
			}
		}

		reader.close();
	}
	
	
	
	
	private void createUrduSeparateCueTrainingKGramMatrix(String entityDir,String trainingFile, int relation, int bow, int entity,
			double relWt2, double bowWt2, double neWt) throws IOException 
	{
		// TODO Auto-generated method stub
		this.kernelTrainingFile = (entityDir + trainingFile + ".train" + ".kGramMatrix");
	//	this.kernelTrainingFile = ("Data/sep-cue-urdu-train" + ".train" + ".kGramMatrix");

		BufferedWriter writer = new BufferedWriter(new FileWriter(this.kernelTrainingFile));
		
		List rowVector = this.TrainUrduEmotionVec;
		List columnVector = this.TrainUrduEmotionVec;

		int rowLen = rowVector.size();
		int colLen = columnVector.size();
		
		System.out.println("rowvector size = "+rowLen);
		System.out.println("colvector size = "+colLen);
		
		double[][] kernelScores = new double[rowLen][colLen];

		for (int i = 0; i < rowLen; ++i) 
		{
			for (int j = 0; j < rowLen; ++j) 
			{
				kernelScores[i][j] = -1.0D;
			}

		}
		
		this.logger.info(" number of row vectors: " + rowLen);
		this.logger.info(" number of column vectors: " + colLen);

		for (int i = 0; i < rowLen; ++i) 
		{
			UrduEmotionNode[][] dataPoint1 = (UrduEmotionNode[][]) rowVector.get(i);

			for (int j = i; j < colLen; ++j) 
			{
				UrduEmotionNode[][] dataPoint2 = (UrduEmotionNode[][]) columnVector
						.get(j);

				double ret1 = 0.0D;
				double ret2 = 0.0D;
				double ret3 = 0.0D;
				double finalRet = 0.0D;
				double relRet = 0.0D;
				double bowRet = 1.0D;
				double neRet = 0.0D;
				
					relRet = runUrduEmotionKernel(dataPoint1, dataPoint2, i, j);
				
				finalRet = relRet ;
				
				kernelScores[i][j] = finalRet;
			}
			this.logger
					.info(" line number " + (i + 1) + " :gram matrix ready ");
		}

		for (int i = 0; i < rowLen; ++i) 
		{
			for (int j = 0; j < rowLen; ++j)
				if (kernelScores[i][j] == -1.0D)
					kernelScores[i][j] = kernelScores[j][i];
		}
		writeTrainingKernelMatrix(kernelScores, rowLen, writer);
	}
	
	
	private void createUrduTogetherCueTrainingKGramMatrix(String entityDir,
			String trainingFile, int relation, int bow, int entity,
			double relWt2, double bowWt2, double neWt) throws Exception 
	{
		// TODO Auto-generated method stub
		this.kernelTrainingFile = (entityDir + trainingFile + ".train.3" + ".kGramMatrix");
	//	this.kernelTrainingFile = ("Data/urdu-train" + ".train" + ".kGramMatrix");

		BufferedWriter writer = new BufferedWriter(new FileWriter(
				this.kernelTrainingFile));
		
		List rowVector = this.TrainUrduEmotionVec;
		List columnVector = this.TrainUrduEmotionVec;

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
			UrduEmotionNode[][] dataPoint1 = (UrduEmotionNode[][]) rowVector.get(i);

			for (int j = i; j < colLen; ++j) 
			{
				UrduEmotionNode[][] dataPoint2 = (UrduEmotionNode[][]) columnVector
						.get(j);

				double ret1 = 0.0D;
				double ret2 = 0.0D;
				double ret3 = 0.0D;
				double finalRet = 0.0D;
				double relRet = 0.0D;
				double bowRet = 1.0D;
				double neRet = 0.0D;
				
				relRet = runUrduEmotionKernel(dataPoint1, dataPoint2, i, j);
				bowRet = runBoWKernel(dataPoint1, dataPoint2, i, j);
				
				if (dataPoint1.length == 2 )
				{
					finalRet = (0.5)* relRet + (0.5)* bowRet;
			//		finalRet = (1.0)* relRet + (0.0)* bowRet;
					
				}
				else
					finalRet = relRet ;
				
				kernelScores[i][j] = finalRet;
			}
			this.logger
					.info(" line number " + (i + 1) + " :gram matrix ready ");
		}

		for (int i = 0; i < rowLen; ++i) 
		{
			for (int j = 0; j < rowLen; ++j)
				if (kernelScores[i][j] == -1.0D)
					kernelScores[i][j] = kernelScores[j][i];
		}
		writeTrainingKernelMatrix(kernelScores, rowLen, writer);
	}
	
	


	private void createUrduTogetherCueTestingKGramMatrix(String entityDir,
			String testingFile, int relation, int bow, int entity,
			double relWt2, double bowWt2, double neWt) throws Exception
	{
		// TODO Auto-generated method stub
		this.kernelTestingFile = (entityDir + testingFile + ".test.3" + ".kGramMatrix");
		
	//	this.kernelTestingFile = ("data/urdu-test" + ".test" + ".kGramMatrix");


		BufferedWriter writer = new BufferedWriter(new FileWriter(
				this.kernelTestingFile));
		
		List rowVector = this.TestUrduEmotionVec;
		List columnVector = this.TrainUrduEmotionVec;
		List categVector = this.TestCategoryVec;

		int p = 3 ;
		int rowLen = rowVector.size();
		int colLen = columnVector.size();

		this.logger.info(" number of test data instances: " + rowLen);

		for (int i = 0; i < rowLen; ++i) 
		{
			UrduEmotionNode[][] dataPoint1 = (UrduEmotionNode[][]) rowVector.get(i);
			String label1 = (String) categVector.get(i);

		//	String result = label1 + " " + "0:" + (i + 1) + " ";
			StringBuffer result = new StringBuffer();
			result.append(label1);
			result.append(" ");
			result.append("0:");
			result.append(i+1);
			result.append(" ");



			for (int j = 0; j < colLen; ++j) 
			{
				UrduEmotionNode[][] dataPoint2 = (UrduEmotionNode[][]) columnVector
						.get(j);

				double relRet = runUrduEmotionKernel(dataPoint1, dataPoint2, j);
				double bowRet = runBoWKernel(dataPoint1, dataPoint2, j);
				
				double finalRet = 0 ;
				if ( dataPoint1.length == 2 && dataPoint2.length == 2 )
				{
					finalRet = 0.5 * relRet + 0.5 * bowRet ;
				//	finalRet = 1.0 * relRet + 0.0 * bowRet ;
					
				}
				else
					finalRet = relRet ;

			//	result = result + (j + 1) + ":" + (float) finalRet + " ";
				float x1 = (float)finalRet ;
				float x2 = (float) ((int)(x1*Math.pow(10,p))/Math.pow(10,p))  ;
			
				
				result.append(j+1);
				result.append(":");
				result.append((float) x2);
				result.append(" ");
			}

			writer.write(result.toString());
			writer.newLine();
		}
		writer.close();
	}

	private void createUrduSeparateCueTestingKGramMatrix(String entityDir,
			String testingFile, int relation, int bow, int entity,
			double relWt2, double bowWt2, double neWt) throws IOException
	{
		// TODO Auto-generated method stub
		this.kernelTestingFile = (entityDir + testingFile + ".test" + ".kGramMatrix");
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.kernelTestingFile));
	
		List rowVector = this.TestUrduEmotionVec;
		List columnVector = this.TrainUrduEmotionVec;
		List categVector = this.TestCategoryVec;

		int rowLen = rowVector.size();
		int colLen = columnVector.size();
		
		int p = 3 ;

		this.logger.info(" number of test data instances: " + rowLen);

		for (int i = 0; i < rowLen; ++i) 
		{
			UrduEmotionNode[][] dataPoint1 = (UrduEmotionNode[][]) rowVector.get(i);
			String label1 = (String) categVector.get(i);

			StringBuffer result = new StringBuffer();
			result.append(label1);
			result.append(" ");
			result.append("0:");
			result.append(i+1);
			result.append(" ");
		//		label1 + " " + "0:" + (i + 1) + " ";

			for (int j = 0; j < colLen; ++j) 
			{
				UrduEmotionNode[][] dataPoint2 = (UrduEmotionNode[][]) columnVector
						.get(j);

				double relRet = runUrduEmotionKernel(dataPoint1, dataPoint2, j);
				double finalRet = relRet ;
				
				float x1 = (float)finalRet ;
				float x2 = (float) ((int)(x1*Math.pow(10,p))/Math.pow(10,p))  ;
			
				
				result.append(j+1);
				result.append(":");
				result.append((float) x2);
				result.append(" ");

			//	result = result + (j + 1) + ":" + (float) finalRet + " ";
			}

			writer.write(result.toString());
			writer.newLine();
		}
		writer.close();
	}

	

	


	public void writeTrainingKernelMatrix(double[][] kernelScores, int rowLen,
			BufferedWriter writer) throws IOException 
	{
		
		// test.append("static string ");
		// test.append(dynamicCall());
		// test.append(" another static string ");
		// test.append(someVariable);
		int p = 3 ;// decimal places

		for (int i = 0; i < rowLen; ++i) 
		{
			StringBuffer result = new StringBuffer();
			result.append(this.TrainCategoryVec.get(i) );
			result.append(" ");
			result.append("0:");
			result.append(i+1);
			result.append(" ");
			
			for (int j = 0; j < rowLen; ++j)
			{
				result.append(j + 1);
				result.append( ":");
				
				float x1 = (float)kernelScores[i][j] ;
				float x2 = (float) ((int)(x1*Math.pow(10,p))/Math.pow(10,p))  ;
				result.append(x2);
				
				result.append(" ");
						
			}
			writer.write(result.toString());
			writer.newLine();
		}

		writer.close();
	}

	
	
	public void createUrduEmotionPatterns(UrduEmotionNode[][] RelationPatternNodes,
			String eachLine, int index ) throws Exception 
	{
		String pattern = EntityStringHelper.getUrduEmotionArg(eachLine, index);
		
		RelationPatternNodes[index] = this.entFeatHndlrObj.createUrduEmotionArgAttributes(pattern);
		
	}
	public void createUrduComboPatterns(UrduEmotionNode[][] RelationPatternNodes,
			String eachLine, int index, int flag ) throws Exception 
	{
		String pattern = EntityStringHelper.getUrduEmotionArg(eachLine, flag);
		
		if ( flag == 3)
		{
			RelationPatternNodes[index] = this.entFeatHndlrObj.createUrduEmotionArgAttributes(pattern);
		}
		else if ( flag == 4 )
		{
			RelationPatternNodes[index] = this.entFeatHndlrObj.createUrduLKArgAttributes(pattern);
		}
	}
	
	
	
	public void createUrduRelationPatterns(UrduEmotionNode[][] RelationPatternNodes,
			String eachLine) throws Exception 
	{
		
		RelationPatternNodes[0] = this.entFeatHndlrObj
		.createUrduEmotionArgAttributes(eachLine);
		
	}
	
	
	public double urduSRLkernelFunction(UrduEntityNode[] first,
			UrduEntityNode[] second) {
		double finalRet = 0.0D;
		double relRet = 0.0D;
		double polyRet = 0.0D;

		UrduEntityNode[] firstRel = getSKNodes(first);
		UrduEntityNode[] secondRel = getSKNodes(second);

		relRet = runRelationKernel(firstRel, secondRel);

		UrduEntityNode[] firstPoly = getLKNodes(first);
		UrduEntityNode[] secondPoly = getLKNodes(second);
	///	polyRet = runBoWKernel(firstPoly, secondPoly);

		finalRet = this.relWt * relRet + this.bowWt * polyRet;

		return finalRet;
	}

	private UrduEntityNode[] getLKNodes(UrduEntityNode[] nodeList) {
		int index = 0;
		for (UrduEntityNode eachNode : nodeList) {
			if (eachNode.index != -1) {
				break;
			}
			++index;
		}
		UrduEntityNode[] lkNodes = new UrduEntityNode[nodeList.length - index];
		System.arraycopy(nodeList, index, lkNodes, 0, nodeList.length - index);

		return lkNodes;
	}

	private UrduEntityNode[] getSKNodes(UrduEntityNode[] nodeList) {
		int index = 0;
		for (UrduEntityNode eachNode : nodeList) {
			if (eachNode.index != -1)
				break;
			++index;
		}

		UrduEntityNode[] skNodes = new UrduEntityNode[index];
		System.arraycopy(nodeList, 0, skNodes, 0, index);
		return skNodes;
	}

		protected double runRelationKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int j) throws Exception {
		return this.BCSKObject.unnormalizedRankingStringKernel(dataPoint1,
				dataPoint2, j);
	}

	protected double runEntityKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i, int j) {
		return this.BCSKObject.unnormalizedRankingEntityKernel(dataPoint1,
				dataPoint2, i, j);
	}

	protected double runEntityKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int j) throws Exception {
		return this.BCSKObject.unnormalizedRankingEntityKernel(dataPoint1,
				dataPoint2, j);
	}

	protected double runBoWKernel(UrduEmotionNode[] dataPoint1,
			UrduEmotionNode[] dataPoint2, int i, int j) throws Exception {
		return this.LKObj.unnormalizedLK(dataPoint1, dataPoint2, i, j);
	}

	protected double runBoWKernel(UrduEmotionNode[] dataPoint1,
			UrduEmotionNode[] dataPoint2, int j) throws Exception {
		return this.LKObj.unnormalizedLK(dataPoint1, dataPoint2, j);
	}
	
	protected double runBoWKernel(UrduEmotionNode[][] dataPoint1,
			UrduEmotionNode[][] dataPoint2, int j) throws Exception 
	{
		if ( dataPoint1.length == 2 && dataPoint2.length == 2 )
			return this.LKObj.unnormalizedLK(dataPoint1[1], dataPoint2[1], j);
		else
			return 0D ;
	}
	
	protected double runBoWKernel(UrduEmotionNode[][] dataPoint1,
			UrduEmotionNode[][] dataPoint2, int i, int j) throws Exception 
	{
		if ( dataPoint1.length == 2 && dataPoint2.length == 2 )
			return this.LKObj.unnormalizedLK(dataPoint1[1], dataPoint2[1], i, j);
		else 
			return 0D ;
	}
	
	
	protected double runUrduEmotionKernel(UrduEmotionNode[][] dataPoint1,
			UrduEmotionNode[][] dataPoint2, int i, int j) 
	{
		return this.BCSKObject.unnormalizedUrduStringKernel(dataPoint1,
				dataPoint2, i, j);
	}
	
	protected double runUrduCueEmotionKernel(UrduEmotionNode[][] dataPoint1,
			UrduEmotionNode[][] dataPoint2, int i, int j) 
	{
		return this.BCSKObject.unnormalizedUrduStringKernel(dataPoint1,
				dataPoint2, i, j);
	}
	
	
	protected double runUrduEmotionKernel(UrduEmotionNode[][] dataPoint1,
			UrduEmotionNode[][] dataPoint2, int i) 
	{
		return this.BCSKObject.unnormalizedUrduStringKernel(dataPoint1,
				dataPoint2, i);
	}

	protected double runRelationKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i, int j) 
	{
		return this.BCSKObject.unnormalizedRankingStringKernel(dataPoint1,
				dataPoint2, i, j);
	}
/*
	protected double runRelationKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2) {
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
	/*
	 * protected double runBoWKernel(UrduEntityNode[] dataPoint1,
			UrduEntityNode[] dataPoint2) {
		double ret1 = this.LKObj.unnormalizedLK(dataPoint1);

		double ret2 = this.LKObj.unnormalizedLK(dataPoint2);

		double ret3 = this.LKObj.unnormalizedLK(dataPoint1, dataPoint2);

		if ((ret1 == 0.0D) || (ret2 == 0.0D)) {
			return 0.0D;
		}
		return ret3 / (Math.sqrt(ret1) * Math.sqrt(ret2));
	}
*/
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