package com.kernel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.didion.jwnl.JWNLException;

import processing.core.PApplet;
import rita.wordnet.*;

public class RitaWordNetHandler
{
  private RiWordnet wordnet;
  private Map<String, List<String>> wordHypernymMap;
private PApplet pApplet;

  public RitaWordNetHandler()
  {
	wordnet = new RiWordnet();
//	pApplet = new PApplet();
//	wordnet = new RiWordnet(pApplet,"/Users/nabaparnaghosh/debanjan/workspace/WordNet3/dict/");
    wordHypernymMap = new HashMap<String, List<String>>();
  //  testRita();
  }

  public double compHypernyms(String word1, String pos1, String word2, String pos2)
  {
	 List<String> result1 = null ;
	 List<String> result2 = null ;
	 
	 result1 = wordHypernymMap.get(word1)  ;
	 if ( null == result1)
	 {
		  result1 = new ArrayList<String>() ;
		  String[] hyps1 = null ;
		  try
		  {
		   hyps1 = wordnet.getAllHypernyms(word1, pos1) ;
		  }
		  catch (Exception e )
		  {
			  //we may get exceptions from rita object
			  System.out.println("caught an exception here " + e.getMessage());
		  }
		  
		  if ((hyps1 == null) || (hyps1.length == 0))
		  {
			  wordHypernymMap.put(word1, result1);
			  return 0.0D;
		  }
		  result1 = Arrays.asList(hyps1);
		  wordHypernymMap.put(word1, result1);
	  }

	  result2 = wordHypernymMap.get(word2);
	  if (null == result2)
	  {
		  result2 = new ArrayList<String>() ;
		  String[] hyps2 = null ;
		  try
		  {
			  hyps2 = wordnet.getAllHypernyms(word2, pos2) ;
		  }
		  catch ( Exception e )
		  {
			  //issue here too
			  System.out.println("caught an exception here " + e.getMessage());
		  }
	 	  if ((hyps2 == null) || (hyps2.length == 0)) 
		  {
	 		  wordHypernymMap.put(word2, result2);
			  return 0.0D;
		  }
		  result2 = Arrays.asList(hyps2);
		  wordHypernymMap.put(word2, result2);
	  }
	 
	  double sim = commonHypeynyms(result1, result2);
/*	  
	  if(sim>0 )
	  {
		  if(!word1.equalsIgnoreCase(word2))
		  {
			  System.out.println("wordnet" +"\t" + word1+"\t"+word2 +"\t" + sim);
		  }
	  }
*/	  
	  return sim ;
  }

  private double commonHypeynyms(List<String> result1, List<String> result2)
  {
    Set<String> intersection1 = new HashSet<String>(result1);
    Set<String> intersection2 = new HashSet<String>(result2);

    intersection1.retainAll(intersection2);

    int size = intersection1.size() ;

    double norm = normalize(size);
    
    
    return norm ;
  }

  private double normalize(int size)
  {
    if (size == 0)
      return 0.0D;
    if (size < 10)
      return 1.0D;
    if (size < 20)
      return 2.0D;
    if (size < 30)
      return 3.0D;
    if (size < 40)
      return 4.0D;
    if (size < 50)
      return 5.0D;
    if (size < 60)
      return 6.0D;
    if (size < 70)
      return 7.0D;
    if (size < 80)
      return 8.0D;
    if (size < 90)
      return 9.0D;
    if (size < 100)
    {
      return 10.0D;
    }
    return 11.0D;
  }

  private void testRita()
  {
    String word = "Miami";

    String pos = this.wordnet.getBestPos(word);

    String[] result = this.wordnet.getAllHypernyms(word, pos);
    result = this.wordnet.getAllGlosses(word, pos) ;
    for (int i = 0; i < result.length; ++i)
    {
    	System.out.println(result[i]);
    }
  }
  
  public void dumpHypernyms( String path) throws IOException
  {
	  BufferedWriter writer = new BufferedWriter ( new FileWriter ( path + "wordnet_hypernyms.txt" ));
	  
	  for ( String word : wordHypernymMap.keySet())
	  {
		  writer.write(word +"\t" + wordHypernymMap.get(word));
		  writer.newLine();
	  }
	  writer.close();
  }
  
  public void loadHypernyms( String path) throws IOException
  {
	 
	  if ( new File(path + "wordnet_hypernyms.txt").exists())
	  {
		  BufferedReader reader = new BufferedReader ( new FileReader ( path + "wordnet_hypernyms.txt" ));
		  
		  while ( true )
		  {
			  String line = reader.readLine();
			  if ( null == line )
			  {
				  break;
			  }
			  
			  String features[] = line.split("\t");
			  String word = features[0];
			  String hyper = features[1].substring(1,features[1].length()-1);
			  List<String> hypernyms = new ArrayList<String> () ;
			  String hypers[] = hyper.split(",");
			  for ( String h : hypers )
			  {
				  String hypernym = h.trim();
				  hypernyms.add(hypernym);
				  
			  }
			  
			  wordHypernymMap.put(word, hypernyms);
		  }
		  reader.close();
		  
	  }
	  
	  
  
  }
  
  
  
  public static void main ( String[] args)
  {
	  RitaWordNetHandler handler = new RitaWordNetHandler() ;
	  handler.testRita();
  }
  
}