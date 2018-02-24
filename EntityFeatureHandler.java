package com.kernel;

import com.thomson.research.svm.EntityNode;
import com.thomson.research.svm.NamedEntityNode;
import com.thomson.research.svm.UrduEmotionNode;
import com.thomson.research.svm.UrduEntityNode;
import com.thomson.research.svm.WordNode;
import com.thomson.research.util.Stemmer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.TagDictionary;

/*
 * This code handles entity nodes for SRL or Relation extraction kernels
 * Note, the main data structure is an EntityNode. This is designed in a way that
 * we can run the libSVM code with few modifications (from svm_node to entity node).
 * 
 * Similar structures are used in SRL for Urdu (UrduEmotionNode)
 */


public class EntityFeatureHandler
{
	private static final int MAX_NODES = 100;
	private EntityNode singleSVMNode;
	private EntityNode singleSVMWordNode;
	private EntityNode singleSVMEntityNode;
	private UrduEmotionNode singleSVMUrduEmotionNode ;
	private EntityNode[] entityArg;
	private EntityNode[] wordArg;
	private UrduEmotionNode[] urduArg;
	
	private EntityNode[] namedEntityArg;
	private Stemmer porterStem;
	private POSTagger openNLPTagger;
	private HashMap<String, String> posMap;

	public EntityFeatureHandler(String openNLPPath, String WordNetPath)
	{
		this.porterStem = new Stemmer();
		//   this.openNLPTagger = new POSTagger(openNLPPath,(TagDictionary)null);
		loadPOSMap();
	}

	public EntityFeatureHandler()
	{
		// loadPOSMap();
	}

	public void loadPOSMap()
	{
		this.posMap = new HashMap<String, String>();

		this.posMap.put("CC", "CONJ");
		this.posMap.put("CD", "NUM");
		this.posMap.put("DT", "DT");
		this.posMap.put("EX", "EX");
		this.posMap.put("FW", "FW");
		this.posMap.put("IN", "IN");
		this.posMap.put("JJR", "ADJ");
		this.posMap.put("JJ", "ADJ");
		this.posMap.put("JJS", "ADJ");
		this.posMap.put("LS", "LS");
		this.posMap.put("MD", "MD");
		this.posMap.put("NN", "NOUN");
		this.posMap.put("NNS", "NOUN");
		this.posMap.put("NNP", "NOUN");
		this.posMap.put("NNPS", "NOUN");
		this.posMap.put("POS", "POS");
		this.posMap.put("PRP", "PRP");
		this.posMap.put("RB", "RB");
		this.posMap.put("RBR", "RB");
		this.posMap.put("RBS", "RB");
		this.posMap.put("RP", "RP");
		this.posMap.put("SYM", "SYM");
		this.posMap.put("TO", "TO");
		this.posMap.put("UH", "UH");
		this.posMap.put("VB", "VERB");
		this.posMap.put("VBD", "VERB");
		this.posMap.put("VBG", "VERB");
		this.posMap.put("VBN", "VERB");
		this.posMap.put("VBP", "VERB");
		this.posMap.put("VBZ", "VERB");
		this.posMap.put("WDT", "WH");
		this.posMap.put("WP", "WH");
		this.posMap.put("WP$", "WH");
		this.posMap.put("WRB", "WH");
	}

	public EntityNode[] createArgAttributes(String arg, String[] argText, String[] argPOS, 
			String[] argChunk, String[] argNE, String[] argPosn)
	throws Exception
	{
		EntityNode[] entityTempArg = new EntityNode[100];

		String[] result = arg.split("\\s+");
		argText = new String[result.length];
		argPOS = new String[result.length];
		argChunk = new String[result.length];
		argNE = new String[result.length];
		argPosn = new String[result.length];

		int x ;
		for (x = 0; x < result.length; ++x)
		{
			StringTokenizer toks = new StringTokenizer(result[x], "|");
			try
			{
				argText[x] = toks.nextToken();
				argPOS[x] = toks.nextToken();
				argChunk[x] = toks.nextToken();
				argNE[x] = toks.nextToken();
				argPosn[x] = toks.nextToken();

				entityTempArg[x] = createSKWordFeatures(argText[x], argPOS[x], argChunk[x], argNE[x], argPosn[x]);
			}
			catch (Exception e)
			{
				argText[x] = "BLANK";
				argPOS[x] = "BLANK";
				argChunk[x] = "BLANK";
			}

		}

		this.entityArg = new EntityNode[x];
		System.arraycopy(entityTempArg, 0, this.entityArg, 0, x);
		return this.entityArg;
	}

	public UrduEmotionNode[] createUrduLKArgAttributes(String pattern) 
	{
		// TODO Auto-generated method stub
		String[] results = pattern.split("\\s+");
		UrduEmotionNode[] entityTempArg = new UrduEmotionNode[results.length];
		int x =0;
		UrduEmotionNode eachNode ;
		for ( String result : results )
		{
			String toks[] = result.split(":");
			int index = Integer.valueOf(toks[0]);
			double value = Double.valueOf(toks[1]) ;
			
			eachNode = new UrduEmotionNode();
			eachNode.setLinearFeature(index,value);
			
			entityTempArg[x] = eachNode;
			x++ ;
		}
		
		
		return entityTempArg;
	}


	public UrduEmotionNode[] createUrduEmotionArgAttributes(String arg)
	throws Exception
	{
		UrduEmotionNode[] entityTempArg = new UrduEmotionNode[100];
		
		if (arg.contains("["))
		{
			arg = arg.replace("[", " ");
		}
		if (arg.contains("]"))
		{
			arg = arg.replace("]", " ");
		}

		String[] results = arg.split("\\s+");
	

		int x =0;
		for ( String result : results )
		{
			String toks[] = result.split(";");
			if (toks.length ==1)
				continue ;
			
		//	argText[x] = toks[0];
		//	argPOS[x] = toks[4] ;
		//	argChunk[x] = toks[5];
			
			entityTempArg[x] = createUrduWordFeatures(toks);
			
			
			x++ ;
		}
		
		this.urduArg = new UrduEmotionNode[x];
		System.arraycopy(entityTempArg, 0, this.urduArg, 0, x);
		return this.urduArg;
	}


	public EntityNode[] createRelationArgAttributes(String arg, String[] argText, String[] argPOS,
			String[] argChunk, String[] argNE, String[] argPosn)
	throws Exception
	{
		EntityNode[] entityTempArg = new WordNode[100];

		String[] result = arg.split("\\s+");
		argText = new String[result.length];
		argPOS = new String[result.length];
		argChunk = new String[result.length];
		argNE = new String[result.length];
		argPosn = new String[result.length];

		int x ;
		for (x = 0; x < result.length; ++x)
		{
			StringTokenizer toks = new StringTokenizer(result[x], "|");
			try
			{
				argText[x] = toks.nextToken();
				argPOS[x] = toks.nextToken();
				argChunk[x] = toks.nextToken();
				argNE[x] = toks.nextToken();
				argPosn[x] = toks.nextToken();

				entityTempArg[x] = createWordFeatures(argText[x], argPOS[x], argChunk[x], argNE[x], argPosn[x]);
			}
			catch (Exception e)
			{
				argText[x] = "BLANK";
				argPOS[x] = "BLANK";
				argChunk[x] = "BLANK";
			}

		}

		this.wordArg = new WordNode[x];
		System.arraycopy(entityTempArg, 0, this.wordArg, 0, x);
		return this.wordArg;
	}
	
	public EntityNode[] createRelationArgAttributes(String arg, String[] argText, String[] argPOS,
			String[] argChunk, String[] argNE, String[] argDep, String[] argPosn)
	throws Exception
	{
		EntityNode[] entityTempArg = new WordNode[100];

		String[] result = arg.split("\\s+");
		argText = new String[result.length];
		argPOS = new String[result.length];
		argChunk = new String[result.length];
		argNE = new String[result.length];
		argDep = new String[result.length] ;
		argPosn = new String[result.length];
		

		int x ;
		for (x = 0; x < result.length; ++x)
		{
			StringTokenizer toks = new StringTokenizer(result[x], "|");
			try
			{
				argText[x] = toks.nextToken();
				argPOS[x] = toks.nextToken();
				argChunk[x] = toks.nextToken();
				argDep[x] = toks.nextToken();
				if ( argDep[x].contains("_"))
				{
					argDep[x] = argDep[x].substring(0,argDep[x].indexOf('_')) ;
				}
			}
			catch (Exception e)
			{
				argText[x] = "BLANK";
				argPOS[x] = "BLANK";
				argChunk[x] = "BLANK";
				argDep[x] = "BLANK";
			}
			entityTempArg[x] = createWordFeatures(argText[x], argPOS[x], argChunk[x], 
					argNE[x], argDep[x],argPosn[x]);

		}

		this.wordArg = new WordNode[x];
		System.arraycopy(entityTempArg, 0, this.wordArg, 0, x);
		return this.wordArg;
	}
	
	public EntityNode[] createSarcRelationArgAttributes(String arg, String[] argText, String[] argPOS,
			String[] argChunk, String[] argNE, String[] argDep, String[] argPosn)
	throws Exception
	{
		EntityNode[] entityTempArg = new WordNode[300];

		String[] result = arg.split("\\s+");
		argText = new String[result.length];
		argPOS = new String[result.length];
		argChunk = new String[result.length];
		argDep = new String[result.length] ;
		

		int x ;
		for (x = 0; x < result.length; ++x)
		{
			StringTokenizer toks = new StringTokenizer(result[x], "|");
			try
			{
				argText[x] = toks.nextToken();
				argPOS[x] = toks.nextToken();
				argChunk[x] = toks.nextToken();
				argDep[x] = toks.nextToken();
				if ( argDep[x].contains("_"))
				{
					argDep[x] = argDep[x].substring(0,argDep[x].indexOf('_')) ;
				}
			}
			catch (Exception e)
			{
				argText[x] = "BLANK";
				argPOS[x] = "BLANK";
				argChunk[x] = "BLANK";
				argDep[x] = "BLANK";
			}
			entityTempArg[x] = createSarcWordFeatures(argText[x], argPOS[x], argChunk[x], 
					argDep[x]);

		}

		this.wordArg = new WordNode[x];
		System.arraycopy(entityTempArg, 0, this.wordArg, 0, x);
		return this.wordArg;
	}


	public EntityNode[] createEntArgAttributes(String arg, String[] argText, String[] argPOS, String[] argChunk,
			String[] argNE, String[] argDep, String[] argPosn)
	{
		EntityNode[] entityTempArg = new NamedEntityNode[100];

		String[] result = arg.split("\\s+");
		argText = new String[result.length];
		argPOS = new String[result.length];
		argChunk = new String[result.length];
		argNE = new String[result.length];
		argDep = new String[result.length];
		argPosn = new String[result.length];

		int x ;
		for (x = 0; x < result.length; ++x)
		{
			StringTokenizer toks = new StringTokenizer(result[x], "|");
			try
			{
				argText[x] = toks.nextToken();
				argPOS[x] = toks.nextToken();
				argChunk[x] = toks.nextToken();
				argNE[x] = toks.nextToken();
				argDep[x] = toks.nextToken();
				if ( argDep[x].contains("_"))
				{
					argDep[x] = argDep[x].substring(0,argDep[x].indexOf('_')) ;
				}
				
				argPosn[x] = toks.nextToken();
				
			}
			catch (Exception e)
			{
				argText[x] = "BLANK";
				argPOS[x] = "BLANK";
				argChunk[x] = "BLANK";
				argNE[x] = "BLANK";
				argDep[x] = "BLANK";
				argPosn[x] = "BLANK";
			}
			entityTempArg[x] = createEntityFeatures(argText[x], argPOS[x], argChunk[x], 
					argNE[x], argDep[x],argPosn[x]);

		}

		this.namedEntityArg = new NamedEntityNode[x];
		System.arraycopy(entityTempArg, 0, this.namedEntityArg, 0, x);
		return this.namedEntityArg;
	}
	
	public EntityNode[] createEntArgAttributes(String arg, String[] argText, String[] argPOS, String[] argChunk,
			String[] argNE, String[] argDep, String[] argRole, String[] argPred, String[] argPosn)
	{
		EntityNode[] entityTempArg = new NamedEntityNode[100];

		String[] result = arg.split("\\s+");
		argText = new String[result.length];
		argPOS = new String[result.length];
		argChunk = new String[result.length];
		argNE = new String[result.length];
		argDep = new String[result.length];
		argRole = new String[result.length];
		argPred = new String[result.length];
		argPosn = new String[result.length];

		int x ;
		for (x = 0; x < result.length; ++x)
		{
			StringTokenizer toks = new StringTokenizer(result[x], "|");
			try
			{
				argText[x] = toks.nextToken();
				argPOS[x] = toks.nextToken();
				argChunk[x] = toks.nextToken();
				argNE[x] = toks.nextToken();
				argDep[x] = toks.nextToken();
				if ( argDep[x].contains("_"))
				{
					argDep[x] = argDep[x].substring(0,argDep[x].indexOf('_')) ;
				}
				
				argRole[x] = toks.nextToken();
				if ( argRole[x].contains("-"))
				{
					argRole[x] = argRole[x].substring(argRole[x].indexOf('-')+1, argRole[x].length()) ;
					argRole[x] = argRole[x].trim();
				}
				
				argPred[x] = toks.nextToken();
				argPosn[x] = toks.nextToken();
				
			}
			catch (Exception e)
			{
				argText[x] = "BLANK";
				argPOS[x] = "BLANK";
				argChunk[x] = "BLANK";
				argNE[x] = "BLANK";
				argDep[x] = "BLANK";
				argRole[x] = "BLANK";
				argPred[x] = "BLANK";
				argPosn[x] = "BLANK";
			}
			entityTempArg[x] = createEntityFeatures(argText[x], argPOS[x], argChunk[x], 
					argNE[x], argDep[x],argRole[x], argPred[x],argPosn[x]);

		}

		this.namedEntityArg = new NamedEntityNode[x];
		System.arraycopy(entityTempArg, 0, this.namedEntityArg, 0, x);
		return this.namedEntityArg;
	}
	
	public EntityNode[] createEntArgAttributes(String arg, String[] argText, String[] argPOS, String[] argChunk,
			String[] argNE, String[] argPosn)
	{
		EntityNode[] entityTempArg = new NamedEntityNode[100];

		String[] result = arg.split("\\s+");
		argText = new String[result.length];
		argPOS = new String[result.length];
		argChunk = new String[result.length];
		argNE = new String[result.length];
		argPosn = new String[result.length];

		int x ;
		for (x = 0; x < result.length; ++x)
		{
			StringTokenizer toks = new StringTokenizer(result[x], "|");
			try
			{
				argText[x] = toks.nextToken();
				argPOS[x] = toks.nextToken();
				argChunk[x] = toks.nextToken();
				argNE[x] = toks.nextToken();
				argPosn[x] = toks.nextToken();

				entityTempArg[x] = createEntityFeatures(argText[x], argPOS[x], argChunk[x], argNE[x], argPosn[x]);
			}
			catch (Exception e)
			{
				argText[x] = "BLANK";
				argPOS[x] = "BLANK";
				argChunk[x] = "BLANK";
			}

		}

		this.namedEntityArg = new NamedEntityNode[x];
		System.arraycopy(entityTempArg, 0, this.namedEntityArg, 0, x);
		return this.namedEntityArg;
	}

	public EntityNode createSKWordFeatures(String word, String pos, String chunk, String ne)
	{
		EntityNode node = createSKWordFeatures(word, pos, chunk);
		node.setNE(ne);
		return node;
	}

	public EntityNode createSKWordFeatures(String word, String pos, String chunk, String ne, String posn)
	{
		this.singleSVMNode = new EntityNode(word);

		if ((pos.compareToIgnoreCase("NNP") != 0) || (pos.compareToIgnoreCase("NNPS") == 0))
		{
			String root = this.porterStem.stemWord(word);
			this.singleSVMNode.setStemForm(root);
			String stemPos = this.openNLPTagger.tag(root);

			this.singleSVMNode.setPOS(pos);
			this.singleSVMNode.setMention(pos); 
		}
		else
		{
			String root = word;
			this.singleSVMNode.setStemForm(root);
			this.singleSVMNode.setPOS(pos);
			this.singleSVMNode.setMention(pos);
		}
		if (this.posMap.containsKey(pos))
		{
			this.singleSVMNode.setBasicPOS((String)this.posMap.get(pos));
		}
		else
		{
			this.singleSVMNode.setBasicPOS(pos);
		}

		this.singleSVMNode.setChunk(chunk);
		this.singleSVMNode.setPosn(posn);
		this.singleSVMNode.setNE(ne);

		return this.singleSVMNode;
	}

	public EntityNode createEntityFeatures(String word, String pos, String chunk, String ne, String posn)
	{
		this.singleSVMEntityNode = new NamedEntityNode(word);

		if ((pos.compareToIgnoreCase("NNP") != 0) || (pos.compareToIgnoreCase("NNPS") == 0))
		{
			String root = this.porterStem.stemWord(word);
		//	String stemPos = this.openNLPTagger.tag(root);
			this.singleSVMEntityNode.setPOS(pos);
		}
		else
		{
			String root = word;
			this.singleSVMEntityNode.setPOS(pos);
		}
		if (this.posMap.containsKey(pos))
		{
			this.singleSVMEntityNode.setBasicPOS((String)this.posMap.get(pos));
		}
		else
		{
			this.singleSVMEntityNode.setBasicPOS(pos);
		}
		this.singleSVMEntityNode.setChunk(chunk);

		this.singleSVMEntityNode.setNE(ne);

		return this.singleSVMEntityNode;
	}
	
	public EntityNode createEntityFeatures(String word, String pos, String chunk, String ne, String dep, String posn)
	{
		this.singleSVMEntityNode = new NamedEntityNode(word);

		if ((pos.compareToIgnoreCase("NNP") != 0) || (pos.compareToIgnoreCase("NNPS") == 0))
		{
			String root = this.porterStem.stemWord(word);
		//	String stemPos = this.openNLPTagger.tag(root);
			this.singleSVMEntityNode.setPOS(pos);
		}
		else
		{
			String root = word;
			this.singleSVMEntityNode.setPOS(pos);
		}
		if (this.posMap.containsKey(pos))
		{
			this.singleSVMEntityNode.setBasicPOS((String)this.posMap.get(pos));
		}
		else
		{
			this.singleSVMEntityNode.setBasicPOS(pos);
		}
		this.singleSVMEntityNode.setChunk(chunk);

		this.singleSVMEntityNode.setNE(ne);
		
		//can we generalize the deps?
	//	dep = checkMappingOfDep(dep);
		
		this.singleSVMEntityNode.setDep(dep);

		return this.singleSVMEntityNode;
	}

	public EntityNode createEntityFeatures(String word, String pos, String chunk, String ne, String dep, 
			String role, String pred, String posn)
	{
		this.singleSVMEntityNode = new NamedEntityNode(word);

		if ((pos.compareToIgnoreCase("NNP") != 0) || (pos.compareToIgnoreCase("NNPS") == 0))
		{
			String root = this.porterStem.stemWord(word);
		//	String stemPos = this.openNLPTagger.tag(root);
			this.singleSVMEntityNode.setPOS(pos);
		}
		else
		{
			String root = word;
			this.singleSVMEntityNode.setPOS(pos);
		}
		if (this.posMap.containsKey(pos))
		{
			this.singleSVMEntityNode.setBasicPOS((String)this.posMap.get(pos));
		}
		else
		{
			this.singleSVMEntityNode.setBasicPOS(pos);
		}
		this.singleSVMEntityNode.setChunk(chunk);

		this.singleSVMEntityNode.setNE(ne);
		
		//can we generalize the deps?
	//	dep = checkMappingOfDep(dep);
		
		this.singleSVMEntityNode.setDep(dep);
		this.singleSVMEntityNode.setSRLRole(role);
		this.singleSVMEntityNode.setSRLPredicate(pred);
		

		return this.singleSVMEntityNode;
	}

	
	
	private String checkMappingOfDep(String dep) 
	{
		// TODO Auto-generated method stub
		if(dep.equalsIgnoreCase("acomp"))
		{
			return "comp" ;
		}
		if(dep.equalsIgnoreCase("ccomp"))
		{
			return "comp" ;
		}
		if(dep.equalsIgnoreCase("xcomp"))
		{
			return "comp" ;
		}
		
		if(dep.equalsIgnoreCase("dobj"))
		{
			return "obj" ;
		}
		if(dep.equalsIgnoreCase("iobj"))
		{
			return "obj" ;
		}
		if(dep.equalsIgnoreCase("pobj"))
		{
			return "obj" ;
		}
		
		if(dep.equalsIgnoreCase("nsubj"))
		{
			return "subj" ;
		}
		
		if(dep.equalsIgnoreCase("nsubjpass"))
		{
			return "subj" ;
		}
		
		if(dep.equalsIgnoreCase("csubj"))
		{
			return "subj" ;
		}
		
		if(dep.equalsIgnoreCase("csubjpass"))
		{
			return "subj" ;
		}
		
		
		return null;
	}

	public UrduEmotionNode createUrduWordFeatures(String[] features)
	{
		String word = features[0];
		String stem = features[1];
		String pos = features[5];
		String chunk = features[6] ;
		String ne = features[7] ;
		String emot = features[8];
		String conceptTag = features[10];
		String nounFlex = features[12];
		String gender = features[14];
		String possess = features[20];
		String doer = features[21];
		String kahan = features[22];
		
		//feature set 1
		this.singleSVMUrduEmotionNode = new UrduEmotionNode(word);
		this.singleSVMUrduEmotionNode.setFeature(1,pos);
		
		
		//feature set 2 (1 + )
	   this.singleSVMUrduEmotionNode.setFeature(2,stem);
	   this.singleSVMUrduEmotionNode.setFeature(3,chunk);
		
		//feature set 3 ( 1 +2 +)
		
		this.singleSVMUrduEmotionNode.setFeature(4,ne);
		this.singleSVMUrduEmotionNode.setFeature(5,conceptTag);
	
	this.singleSVMUrduEmotionNode.setFeature(6,gender);
		this.singleSVMUrduEmotionNode.setFeature(7,emot);
	this.singleSVMUrduEmotionNode.setFeature(8,nounFlex);
		
		//feature set 4 (1+2+3 + urdu ones)
		
		this.singleSVMUrduEmotionNode.setFeature(9,kahan);
	
		return this.singleSVMUrduEmotionNode;
	}
	
	
	public UrduEmotionNode createUrduWordFeatures(String word, String pos, String chunk)
	{
		this.singleSVMUrduEmotionNode = new UrduEmotionNode(word);

		
		this.singleSVMUrduEmotionNode.setFeature(1,word);
		this.singleSVMUrduEmotionNode.setFeature(2,pos);
		this.singleSVMUrduEmotionNode.setFeature(3,chunk);
	
		return this.singleSVMUrduEmotionNode;
	}
	
	public EntityNode createWordFeatures(String word, String pos, String chunk, String ne, String posn)
	{
		this.singleSVMWordNode = new WordNode(word);

		if ((pos.compareToIgnoreCase("NNP") != 0) || (pos.compareToIgnoreCase("NNPS") == 0))
		{
			String root = this.porterStem.stemWord(word);
			this.singleSVMWordNode.setStemForm(root);
	//		String stemPos = this.openNLPTagger.tag(root);

			this.singleSVMWordNode.setPOS(pos);
			this.singleSVMWordNode.setMention(pos);
		}
		else
		{
			String root = word;
			this.singleSVMWordNode.setStemForm(root);
			this.singleSVMWordNode.setPOS(pos);
			this.singleSVMWordNode.setMention(pos);
		}
		if (this.posMap.containsKey(pos))
		{
			this.singleSVMWordNode.setBasicPOS((String)this.posMap.get(pos));
		}
		else
		{
			this.singleSVMWordNode.setBasicPOS(pos);
		}

		this.singleSVMWordNode.setChunk(chunk);
		this.singleSVMWordNode.setPosn(posn);

		return this.singleSVMWordNode;
	}
	
	public EntityNode createSarcWordFeatures(String word, String pos, String chunk, String dep)
	{
		this.singleSVMWordNode = new WordNode(word);

		if ((pos.compareToIgnoreCase("NNP") == 0) || (pos.compareToIgnoreCase("NNPS") == 0))
		{
			String root = this.porterStem.stemWord(word);
			this.singleSVMWordNode.setStemForm(root);
	//		String stemPos = this.openNLPTagger.tag(root);

			this.singleSVMWordNode.setPOS(pos);
			this.singleSVMWordNode.setMention(pos);
		}
		else
		{
			String root = word;
			this.singleSVMWordNode.setStemForm(root);
			this.singleSVMWordNode.setPOS(pos);
			this.singleSVMWordNode.setMention(pos);
		}
		if (this.posMap.containsKey(pos))
		{
			this.singleSVMWordNode.setBasicPOS((String)this.posMap.get(pos));
		}
		else
		{
			this.singleSVMWordNode.setBasicPOS(pos);
		}

		this.singleSVMWordNode.setChunk(chunk);
	//	this.singleSVMWordNode.setPosn(posn);

		this.singleSVMWordNode.setDep(dep);
		
		return this.singleSVMWordNode;
	}
	

	
	public EntityNode createWordFeatures(String word, String pos, String chunk, String ne, String dep, String posn)
	{
		this.singleSVMWordNode = new WordNode(word);

		if ((pos.compareToIgnoreCase("NNP") != 0) || (pos.compareToIgnoreCase("NNPS") == 0))
		{
			String root = this.porterStem.stemWord(word);
			this.singleSVMWordNode.setStemForm(root);
	//		String stemPos = this.openNLPTagger.tag(root);

			this.singleSVMWordNode.setPOS(pos);
			this.singleSVMWordNode.setMention(pos);
		}
		else
		{
			String root = word;
			this.singleSVMWordNode.setStemForm(root);
			this.singleSVMWordNode.setPOS(pos);
			this.singleSVMWordNode.setMention(pos);
		}
		if (this.posMap.containsKey(pos))
		{
			this.singleSVMWordNode.setBasicPOS((String)this.posMap.get(pos));
		}
		else
		{
			this.singleSVMWordNode.setBasicPOS(pos);
		}

		this.singleSVMWordNode.setChunk(chunk);
		
		//can we generalize the deps?
//		dep = checkMappingOfDep(dep);
		
		this.singleSVMWordNode.setDep(dep);
		this.singleSVMWordNode.setPosn(posn);

		return this.singleSVMWordNode;
	}


	public EntityNode createSKWordFeatures(String word, String pos, String chunk)
	{
		this.singleSVMNode = new EntityNode(word);

		if ((pos.compareToIgnoreCase("NNP") != 0) || (pos.compareToIgnoreCase("NNPS") == 0))
		{
			String root = this.porterStem.stemWord(word);
			this.singleSVMNode.setStemForm(root);
			String stemPos = this.openNLPTagger.tag(root);

			this.singleSVMNode.setPOS(pos);
			this.singleSVMNode.setMention(pos);
		}
		else
		{
			String root = word;
			this.singleSVMNode.setStemForm(root);
			this.singleSVMNode.setPOS(pos);
			this.singleSVMNode.setMention(pos);
		}
		this.singleSVMNode.setChunk(chunk);

		return this.singleSVMNode;
	}

	public EntityNode[] returnBlankNode()
	throws Exception
	{
		EntityNode[] entityTempArg = new EntityNode[1];
		this.singleSVMNode = new EntityNode("BLANK");
		entityTempArg[0] = this.singleSVMNode;
		return entityTempArg;
	}

	public EntityNode[] returnRelationBlankNode() throws Exception
	{
		EntityNode[] entityTempArg = new WordNode[1];
		this.singleSVMWordNode = new WordNode("BLANK");
		entityTempArg[0] = this.singleSVMWordNode;
		return entityTempArg;
	}

	public EntityNode[] returnEntityBlankNode() throws Exception {
		EntityNode[] entityTempArg = new NamedEntityNode[1];
		this.singleSVMEntityNode = new NamedEntityNode("BLANK");
		entityTempArg[0] = this.singleSVMEntityNode;
		return entityTempArg;
	}

	public EntityNode[] createBowFeature(String line) {
		StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
		int m = st.countTokens() / 2;
		this.entityArg = new EntityNode[m];
		for (int j = 0; j < m; ++j)
		{
			this.entityArg[j] = new EntityNode();
			this.entityArg[j].index = Integer.parseInt(st.nextToken());
			this.entityArg[j].value = Integer.parseInt(st.nextToken());
		}

		return this.entityArg;
	}

	public EntityNode setWordInfoLK(String idx, String val)
	{
		this.singleSVMNode = new EntityNode();
		this.singleSVMNode.index = Integer.parseInt(idx);
		this.singleSVMNode.value = Double.parseDouble(val);
		return this.singleSVMNode;
	}

	public boolean ifContainsHashSet(String word)
	{
		return true;
	}

	public HashSet<String> getHypernymSetForWord(String word)
	{
		return null;
	}

	
}