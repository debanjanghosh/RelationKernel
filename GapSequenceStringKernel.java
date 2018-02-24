package com.kernel;

import com.thomson.research.svm.EntityNode;
import com.thomson.research.svm.UrduEmotionNode;
import com.thomson.research.svm.UrduEntityNode;
import java.util.HashMap;
import java.util.Map;

/*
 * gap-sequence kernel implmentation (COLING 2012)
 * The kernel is implemented based on various research 
 * on sequence, string, tree kernels 
 */

public class GapSequenceStringKernel extends EntityStringKernel
{
  private Map<Integer, Double> entityCacheMap;
  private Map<Integer, Double> rankNodeCacheMap;
  private double lambdaDecay;
  private static final int SEQUENCE_LENGTH = 3;
  private static final int WORD = 1;
  private static final int ENTITY = 2;
  private static final int ENT_COMP = 1;
  private static final int ENT_COND = 2;
  private int ent_type = 1;

  public GapSequenceStringKernel(WordComparator wComp)
  {
    super(wComp);
    this.rankNodeCacheMap = new HashMap<Integer, Double>();

    this.entityCacheMap = new HashMap<Integer, Double>();

    this.lambdaDecay = 0.5D;
  }

  public GapSequenceStringKernel()
  {
    this.rankNodeCacheMap = new HashMap<Integer, Double>();

    this.entityCacheMap = new HashMap<Integer, Double>();
    this.lambdaDecay = 0.5D;
  }

  public void setWCObj(WordComparator wComp)
  {
  }

  /* 
   * simple cache mechanism to handle redundant kernel similarity calls
   */
  
  public double unnormalizedSelfSeparateEntityKernel(EntityNode[][] s, int i)
  {
    double ret = 0.0D;

    if (this.entityCacheMap.containsKey(Integer.valueOf(i))) {
      return ((Double)this.entityCacheMap.get(Integer.valueOf(i))).doubleValue();
    }

    ret = unnormalizedSeparateEntityKernel(s, s);
    this.entityCacheMap.put(Integer.valueOf(i), Double.valueOf(ret));
    return ret;
  }

  public double unnormalizedSelfSeparateEntityKernel(EntityNode[][] s)
  {
    double ret = 0.0D;
    ret = unnormalizedSeparateEntityKernel(s, s);
    return ret;
  }
  
  
  public double unnormalizedSelfEntityKernel(EntityNode[][] s, int i)
  {
    double ret = 0.0D;

    if (this.entityCacheMap.containsKey(Integer.valueOf(i))) {
      return ((Double)this.entityCacheMap.get(Integer.valueOf(i))).doubleValue();
    }

    ret = unnormalizedEntityKernel(s, s);
    this.entityCacheMap.put(Integer.valueOf(i), Double.valueOf(ret));
    return ret;
  }

  public double unnormalizedSelfEntityKernel(EntityNode[][] s)
  {
    return unnormalizedEntityKernel(s, s);
  }

  public double unnormalizedSelfStringKernel(EntityNode[][] s)
  {
    return unnormalizedStringKernel(s, s);
  }

  public double unnormalizedSelfStringKernel(UrduEntityNode[] s)
  {
    return unnormalizedStringKernel(s, s);
  }

  public double unnormalizedSelfStringKernel(EntityNode[][] s, int i)
  {
    double ret = 0.0D;
    
    Double val = rankNodeCacheMap.get(Integer.valueOf(i)) ;
    
    if ( null != val )
    {
    	return val.doubleValue() ;
    }
    
    ret = unnormalizedStringKernel(s, s);
    this.rankNodeCacheMap.put(Integer.valueOf(i), Double.valueOf(ret));
    return ret;
  }
  
  public double unnormalizedEntitySelfStringKernel(EntityNode[][] s, int i)
  {
    double ret = 0.0D;
    if (this.rankNodeCacheMap.containsKey(Integer.valueOf(i))) {
      return ((Double)this.rankNodeCacheMap.get(Integer.valueOf(i))).doubleValue();
    }

    ret = unnormalizedEntityStringKernel(s, s);
    this.rankNodeCacheMap.put(Integer.valueOf(i), Double.valueOf(ret));
    return ret;
  }
  
  
  public double unnormalizedEntitySelfStringKernel(EntityNode[][] s)
  {
   
    return unnormalizedEntityStringKernel(s, s);
   
  }
  /*
   * This functions were also used for the SRL kernel used in 
   * COLING 2010, CoNLL 2011 papers on SRL for Urdu language
   */
  
	public double unnormalizedLK(UrduEmotionNode[] dataPoint1, UrduEmotionNode[] dataPoint2)
	{
		
		double sum = 0;
		int xlen = dataPoint1.length;
		int ylen = dataPoint2.length;
		int i = 0;
		int j = 0;
		while(i < xlen && j < ylen)
		{
			if(dataPoint1[i].index == dataPoint2[j].index)
				sum += dataPoint1[i++].value * dataPoint2[j++].value;
			else
			{
				if(dataPoint1[i].index > dataPoint2[j].index)
					++j;
				else
					++i;
			}
		}
		return sum;
	
	}
   
  public double unnormalizedSeparateEntityKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    double entityKernelVal = 0.0D;

    if (this.ent_type == 1)
    {
      int nLenEnt1 = Math.min(dataPoint1[2].length, dataPoint2[2].length);
      double[] sk = entityKernel(dataPoint1[2], dataPoint2[2], nLenEnt1, 1.0D);
      entityKernelVal = sKernel(sk, sk.length);

    }
    else if (this.ent_type == 2)
    {
      if (dataPoint1[2][0].entityNodeFeatArray[6]
        .compareToIgnoreCase(dataPoint2[2][0].entityNodeFeatArray[6]) == 0)
      {
        entityKernelVal += 1.0D;
      }
      
    }
    return entityKernelVal;
  }
  /*
   * Summing up for entity kernels. The kernel uses word, dependency tree, POS, WN information
   */
  

  public double unnormalizedEntityKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    double entityKernelVal = 0.0D;

    if (this.ent_type == 1)
    {
      int nLenEnt1 = Math.min(dataPoint1[3].length, dataPoint2[3].length);
      double[] sk = entityKernel(dataPoint1[3], dataPoint2[3], nLenEnt1, 1.0D);
      entityKernelVal = sKernel(sk, sk.length);

      int nLenEnt2 = Math.min(dataPoint1[4].length, dataPoint2[4].length);
      sk = entityKernel(dataPoint1[4], dataPoint2[4], nLenEnt2, 1.0D);
      entityKernelVal += sKernel(sk, sk.length);
    }
    else if (this.ent_type == 2)
    {
      if (dataPoint1[3][0].entityNodeFeatArray[6]
        .compareToIgnoreCase(dataPoint2[3][0].entityNodeFeatArray[6]) == 0)
      {
        entityKernelVal += 1.0D;
      }
      if (dataPoint1[4][0].entityNodeFeatArray[6]
        .compareToIgnoreCase(dataPoint2[4][0].entityNodeFeatArray[6]) == 0)
      {
        entityKernelVal += 1.0D;
      }
    }
    return entityKernelVal;
  }

  public double unnormalizedStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    return unnormalizedSarcStringKernel(dataPoint1, dataPoint2);
 //   return unnormalizedBunescuStringKernel(dataPoint1, dataPoint2);
    
  }
  
  public double unnormalizedEntityStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    return unnormalizedEntityBunescuStringKernel(dataPoint1, dataPoint2);
  }
 
  public double unnormalizedEntityBunescuStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    double foreKernelVal = 0.0D;
    double afterKernelVal = 0.0D;

   
    if ((dataPoint1[0][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0) || 
      (dataPoint2[0][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0)) 
    {
      foreKernelVal = 0.0D;
    }
    else
    {
      double[] beforeK = beforeKernel(dataPoint1, dataPoint2);
      foreKernelVal = sKernel(beforeK, beforeK.length);
    }

    if ((dataPoint1[2][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0) || 
      (dataPoint2[2][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0))
    {
      afterKernelVal = 0.0D;
    }
    else
    {
      double[] afterK = afterKernel(dataPoint1, dataPoint2);
      afterKernelVal = sKernel(afterK, afterK.length);
    }

    return foreKernelVal  + afterKernelVal;
  }

  /*
   * The following function is based on Bunescu's work on string kernel papers.
   * We measure the before/intra/after kernels for relation extraction between
   * pair of entities/mentions
   */
  
  
  public double unnormalizedBunescuStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    double foreKernelVal = 0.0D;
    double intraKernelVal = 0.0D;
    double afterKernelVal = 0.0D;

  
    int nLenIntra = Math.min(dataPoint1[1].length, dataPoint2[1].length);
    if ((dataPoint1[1][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0) || 
      (dataPoint2[1][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0)) {
      intraKernelVal = 0.0D;
    }
    else {
      double[] intraK = wordKernel(dataPoint1[1], dataPoint2[1], 4, this.lambdaDecay);
      intraKernelVal = sKernel(intraK, intraK.length);
    }


    if ((dataPoint1[0][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0) || 
      (dataPoint2[0][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0)) {
      foreKernelVal = 0.0D;
    }
    else {
      double[] beforeK = beforeKernel(dataPoint1, dataPoint2);
      foreKernelVal = sKernel(beforeK, beforeK.length);
    }

    if ((dataPoint1[2][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0) || 
      (dataPoint2[2][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0)) {
      afterKernelVal = 0.0D;
    }
    else {
      double[] afterK = afterKernel(dataPoint1, dataPoint2);
      afterKernelVal = sKernel(afterK, afterK.length);
    }

    return foreKernelVal + intraKernelVal + afterKernelVal;
  }
  
  public double unnormalizedSarcStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    double intraKernelVal = 0.0D;
    double[] intraK = sarcKernel(dataPoint1[0], dataPoint2[0], SEQUENCE_LENGTH, this.lambdaDecay);
     intraKernelVal = sKernel(intraK, intraK.length);
     
     return intraKernelVal ;
  }

  protected double[] onlyBeforeKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    int len1 = dataPoint1[0].length;
    int len2 = dataPoint2[0].length;

    EntityNode[] beforeK1 = new EntityNode[len1];
    EntityNode[] beforeK2 = new EntityNode[len2];

    System.arraycopy(dataPoint1[0], 0, beforeK1, 0, len1);
    System.arraycopy(dataPoint2[0], 0, beforeK2, 0, len2);

    int minLength = Math.min(beforeK1.length, beforeK2.length);
    return stringKernel(beforeK1, beforeK2, minLength, this.lambdaDecay);
  }

  protected double[] beforeKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    int len1 = dataPoint1[0].length + dataPoint1[1].length;
    int len2 = dataPoint2[0].length + dataPoint2[1].length;

    EntityNode[] beforeK1 = new EntityNode[len1];
    EntityNode[] beforeK2 = new EntityNode[len2];

    System.arraycopy(dataPoint1[0], 0, beforeK1, 0, dataPoint1[0].length);
    System.arraycopy(dataPoint1[1], 0, beforeK1, dataPoint1[0].length, dataPoint1[1].length);

    System.arraycopy(dataPoint2[0], 0, beforeK2, 0, dataPoint2[0].length);
    System.arraycopy(dataPoint2[1], 0, beforeK2, dataPoint2[0].length, dataPoint2[1].length);

    int minLength = Math.min(beforeK1.length, beforeK2.length);
    return wordKernel(beforeK1, beforeK2, 4, this.lambdaDecay);
  }

  protected double[] onlyAfterKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    int len1 = dataPoint1[2].length;
    int len2 = dataPoint2[2].length;

    EntityNode[] afterK1 = new EntityNode[len1];
    EntityNode[] afterK2 = new EntityNode[len2];

    System.arraycopy(dataPoint1[2], 0, afterK1, 0, len1);
    System.arraycopy(dataPoint2[2], 0, afterK2, 0, len2);

    int minLength = Math.min(afterK1.length, afterK2.length);
    return stringKernel(afterK1, afterK2, minLength, this.lambdaDecay);
  }

  protected double[] afterKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    int len1 = dataPoint1[1].length + dataPoint1[2].length;
    int len2 = dataPoint2[1].length + dataPoint2[2].length;

    EntityNode[] afterK1 = new EntityNode[len1];
    EntityNode[] afterK2 = new EntityNode[len2];

    System.arraycopy(dataPoint1[1], 0, afterK1, 0, dataPoint1[1].length);
    System.arraycopy(dataPoint1[2], 0, afterK1, dataPoint1[1].length, dataPoint1[2].length);

    System.arraycopy(dataPoint2[1], 0, afterK2, 0, dataPoint2[1].length);
    System.arraycopy(dataPoint2[2], 0, afterK2, dataPoint2[1].length, dataPoint2[2].length);

    int minLength = Math.min(afterK1.length, afterK2.length);
    return wordKernel(afterK1, afterK2, 4, this.lambdaDecay);
  }

  /*
   * implementation of a string kernel similarity between two sets of nodes[]
   * lambda presents the decay factor 
   * the compare function measures similarity between any two nodes using the compare function
   */
  
  
  protected double[] stringKernel(EntityNode[] s, EntityNode[] t, int n, double lambda)
  {
    int slen = s.length;
    int tlen = t.length;

    double[][] K = new double[n + 1][slen * tlen];

    for (int j = 0; j < slen; ++j) {
      for (int k = 0; k < tlen; ++k)
        K[0][(k * slen + j)] = 1.0D;
    }
    for (int i = 0; i < n; ++i)
    {
      for (int j = 0; j < slen - 1; ++j)
      {
        double sum = 0.0D;
        EntityNode node1 = s[j];
        for (int k = 0; k < tlen - 1; ++k)
        {
          EntityNode node2 = t[k];
          sum = lambda * (sum + lambda * this.wordCompObj.compare(node1, node2) * K[i][(k * slen + j)]);
          K[(i + 1)][((k + 1) * slen + j + 1)] = (lambda * K[(i + 1)][((k + 1) * slen + j)] + sum);
        }
      }
    }

    double[] result = new double[n];
    for (int l = 0; l < result.length; ++l)
    {
      result[l] = 0.0D;
      for (int j = 0; j < slen; ++j)
      {
        EntityNode node1 = s[j];
        for (int k = 0; k < tlen; ++k)
        {
          EntityNode node2 = t[k];
          result[l] += lambda * lambda * this.wordCompObj.compare(node1, node2) * K[l][(k * slen + j)];
        }
      }
    }

    return result;
  }



  protected double[] sarcKernel(EntityNode[] s, EntityNode[] t, int n, double lambda)
  {
	  int slen = s.length;
	  int tlen = t.length;
	  double[][] K = new double[n + 1][slen * tlen];

	  for (int j = 0; j < slen; ++j)
	  {
		  for (int k = 0; k < tlen; ++k)
		  {
			  K[0][(k * slen + j)] = 1.0D;
		  }
	  }
	  for (int i = 0; i < n; ++i)
	  {
		  for (int j = 0; j < slen - 1; ++j)
		  {
			  double sum = 0.0D;
			  EntityNode node1 = s[j];
			  for (int k = 0; k < tlen - 1; ++k)
			  {
				  EntityNode node2 = t[k];
				  sum = lambda * (sum + lambda * wordCompObj.compareSarc(node1, node2) * K[i][(k * slen + j)]);
				  K[(i + 1)][((k + 1) * slen + j + 1)] = (lambda * K[(i + 1)][((k + 1) * slen + j)] + sum);
			  }
		  }
      }
       double[] result = new double[n];
       for (int l = 0; l < result.length; ++l)
       {
    	   result[l] = 0.0D;
    	   for (int j = 0; j < slen; ++j)
    	   {
    		   EntityNode node1 = s[j];
    		   for (int k = 0; k < tlen; ++k)
    		   {
    			   EntityNode node2 = t[k];
    			   result[l] += lambda * lambda * this.wordCompObj.compareSarc(node1, node2) * K[l][(k * slen + j)];
    		   }
    	   }
       }
       
       return result;
  }
  
  protected double[] wordKernel(EntityNode[] s, EntityNode[] t, int n, double lambda)
  {
	  int slen = s.length;
	  int tlen = t.length;
	  double[][] K = new double[n + 1][slen * tlen];

	  for (int j = 0; j < slen; ++j)
	  {
		  for (int k = 0; k < tlen; ++k)
		  {
			  K[0][(k * slen + j)] = 1.0D;
		  }
	  }
	  for (int i = 0; i < n; ++i)
	  {
		  for (int j = 0; j < slen - 1; ++j)
		  {
			  double sum = 0.0D;
			  EntityNode node1 = s[j];
			  for (int k = 0; k < tlen - 1; ++k)
			  {
				  EntityNode node2 = t[k];
				  sum = lambda * (sum + lambda * wordCompObj.compareWord(node1, node2) * K[i][(k * slen + j)]);
				  K[(i + 1)][((k + 1) * slen + j + 1)] = (lambda * K[(i + 1)][((k + 1) * slen + j)] + sum);
			  }
		  }
      }
       double[] result = new double[n];
       for (int l = 0; l < result.length; ++l)
       {
    	   result[l] = 0.0D;
    	   for (int j = 0; j < slen; ++j)
    	   {
    		   EntityNode node1 = s[j];
    		   for (int k = 0; k < tlen; ++k)
    		   {
    			   EntityNode node2 = t[k];
    			   result[l] += lambda * lambda * this.wordCompObj.compareWord(node1, node2) * K[l][(k * slen + j)];
    		   }
    	   }
       }
       
       return result;
  }

  protected double[] entityKernel(EntityNode[] s, EntityNode[] t, int n, double lambda_entity)
  {
    int slen = s.length;
    int tlen = t.length;

    double[][] K = new double[n + 1][slen * tlen];

    for (int j = 0; j < slen; ++j) {
      for (int k = 0; k < tlen; ++k)
        K[0][(k * slen + j)] = 1.0D;
    }
    for (int i = 0; i < n; ++i)
    {
      for (int j = 0; j < slen - 1; ++j)
      {
        double sum = 0.0D;
        EntityNode node1 = s[j];
        for (int k = 0; k < tlen - 1; ++k)
        {
          EntityNode node2 = t[k];
          sum = lambda_entity * (sum + lambda_entity * this.wordCompObj.compareEntity(node1, node2) * K[i][(k * slen + j)]);
          K[(i + 1)][((k + 1) * slen + j + 1)] = (lambda_entity * K[(i + 1)][((k + 1) * slen + j)] + sum);
        }
      }
    }

    double[] result = new double[n];
    for (int l = 0; l < result.length; ++l)
    {
      result[l] = 0.0D;
      for (int j = 0; j < slen; ++j)
      {
        EntityNode node1 = s[j];
        for (int k = 0; k < tlen; ++k)
        {
          EntityNode node2 = t[k];
          result[l] += lambda_entity * lambda_entity * this.wordCompObj.compareEntity(node1, node2) * K[l][(k * slen + j)];
        }
      }
    }

    return result;
  }

  public double unnormalizedStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2, int i, int j)
  {
    double ret1 = unnormalizedSelfStringKernel(dataPoint1, i);
    double ret2 = unnormalizedSelfStringKernel(dataPoint2, j);
    if ((ret1 != 0.0D) && (ret2 != 0.0D))
    {
      double norm = Math.sqrt(ret1) * Math.sqrt(ret2);
      double ret = unnormalizedBunescuStringKernel(dataPoint1, dataPoint2);
      
      return ret / norm;
    }

    return 0.0D;
  }
  
  public double unnormalizedSarcStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2, int i, int j)
  {
    double ret1 = unnormalizedSelfStringKernel(dataPoint1, i);
    double ret2 = unnormalizedSelfStringKernel(dataPoint2, j);
    if ((ret1 != 0.0D) && (ret2 != 0.0D))
    {
      double norm = Math.sqrt(ret1) * Math.sqrt(ret2);
      double ret = unnormalizedSarcStringKernel(dataPoint1, dataPoint2);
      
      return ret / norm;
    }

    return 0.0D;
  }

  public double unnormalizedStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2, int i)
  {
    double ret1 = unnormalizedSelfStringKernel(dataPoint1);
    double ret2 = unnormalizedSelfStringKernel(dataPoint2, i);
    if ((ret1 != 0.0D) && (ret2 != 0.0D))
    {
      double norm = Math.sqrt(ret1) * Math.sqrt(ret2);
      double ret = unnormalizedBunescuStringKernel(dataPoint1, dataPoint2);
      return ret / norm;
    }

    return 0.0D;
  }
  
  
  public double unnormalizedSarcStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2, int i)
  {
    double ret1 = unnormalizedSelfStringKernel(dataPoint1);
    double ret2 = unnormalizedSelfStringKernel(dataPoint2, i);
    if ((ret1 != 0.0D) && (ret2 != 0.0D))
    {
      double norm = Math.sqrt(ret1) * Math.sqrt(ret2);
      double ret = unnormalizedSarcStringKernel(dataPoint1, dataPoint2);
      
      return ret / norm;
    }

    return 0.0D;
  }

  
  public double unnormalizedEntityKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2, int i, int j)
  {
    double ret1 = unnormalizedSelfEntityKernel(dataPoint1, i);
    double ret2 = unnormalizedSelfEntityKernel(dataPoint2, j);
    if ((ret1 != 0.0D) && (ret2 != 0.0D))
    {
      double norm = Math.sqrt(ret1) * Math.sqrt(ret2);
      double ret = unnormalizedEntityKernel(dataPoint1, dataPoint2);
      return ret / norm;
    }

    return 0.0D;
  }

  public double unnormalizedEntityKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2, int i)
  {
    double ret1 = unnormalizedSelfEntityKernel(dataPoint1);
    double ret2 = unnormalizedSelfEntityKernel(dataPoint2, i);
    if ((ret1 != 0.0D) && (ret2 != 0.0D))
    {
      double norm = Math.sqrt(ret1) * Math.sqrt(ret2);
      double ret = unnormalizedEntityKernel(dataPoint1, dataPoint2);
      return ret / norm;
    }

    return 0.0D;
  }
  
  
  protected double sKernel(double[] intraK, int length)
  {
    double k = 0.0D;

    for (int i = 0; i < length; ++i)
    {
    	k += intraK[i];
    }
    return k;
  }

  public void setEntityKernelType(int i)
  {
    this.ent_type = i;
  }
}