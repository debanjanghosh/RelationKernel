package com.kernel;

import com.thomson.research.svm.EntityNode;
import java.util.HashMap;

/*
 * Simple spectrum kernel implementation 
 */

public class SpectrumKernel extends EntityStringKernel
{
  private WordComparator wordCompObj;
  private HashMap<EntityNode[][], Double> chunkNodeCacheMap;
  private HashMap<EntityNode[][], Double> entityCacheMap;
  private int maxLength;

  public SpectrumKernel()
  {
    this.chunkNodeCacheMap = new HashMap<EntityNode[][], Double>();
    this.entityCacheMap = new HashMap<EntityNode[][], Double>();

    this.maxLength = 3;
  }

  public SpectrumKernel(WordComparator wComp)
  {
    this.chunkNodeCacheMap = new HashMap<EntityNode[][], Double>();
    this.entityCacheMap = new HashMap<EntityNode[][], Double>();
    this.wordCompObj = wComp;
    this.maxLength = 3;
  }

  public double unnormalizedSelfEntityKernel(EntityNode[][] s)
  {
    double ret = 0.0D;

    if (this.entityCacheMap.containsKey(s)) {
      return ((Double)this.entityCacheMap.get(s)).doubleValue();
    }

    ret = unnormalizedEntityKernel(s, s);
    this.entityCacheMap.put(s, Double.valueOf(ret));
    return ret;
  }

  public double unnormalizedSelfStringKernel(EntityNode[][] s)
  {
    double ret = 0.0D;
    if (this.chunkNodeCacheMap.containsKey(s)) {
      return ((Double)this.chunkNodeCacheMap.get(s)).doubleValue();
    }

    ret = unnormalizedStringKernel(s, s);
    this.chunkNodeCacheMap.put(s, Double.valueOf(ret));
    return ret;
  }

  public double unnormalizedEntityKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    int nLenEnt1 = Math.min(dataPoint1[3].length, dataPoint2[3].length);
    double entityKernelVal = stringKernel(dataPoint1[3], dataPoint2[3], nLenEnt1);

    int nLenEnt2 = Math.min(dataPoint1[4].length, dataPoint2[4].length);
    entityKernelVal += stringKernel(dataPoint1[4], dataPoint2[4], nLenEnt2);

    return entityKernelVal;
  }

  public double unnormalizedStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    return unnormalizedBunescuStringKernel(dataPoint1, dataPoint2);
  }

  public double unnormalizedBunescuStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
  {
    double foreKernelVal = 0.0D;
    double intraKernelVal = 0.0D;
    double afterKernelVal = 0.0D;

    int nLenIntra = Math.min(dataPoint1[1].length, dataPoint2[1].length);
    intraKernelVal = stringKernel(dataPoint1[1], dataPoint2[1], this.maxLength);

    if ((dataPoint1[0][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0) || 
      (dataPoint2[0][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0)) {
      foreKernelVal = 0.0D;
    }
    else {
      foreKernelVal = beforeKernel(dataPoint1, dataPoint2);
    }

    if ((dataPoint1[2][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0) || 
      (dataPoint2[2][0].entityNodeFeatArray[0].compareToIgnoreCase("BLANK") == 0)) {
      afterKernelVal = 0.0D;
    }
    else {
      afterKernelVal = afterKernel(dataPoint1, dataPoint2);
    }

    return foreKernelVal + intraKernelVal + afterKernelVal;
  }

  protected double beforeKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
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
    return stringKernel(beforeK1, beforeK2, this.maxLength);
  }

  protected double afterKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2)
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
    return stringKernel(afterK1, afterK2, this.maxLength);
  }

  protected double stringKernel(EntityNode[] s, EntityNode[] t, int n)
  {
    return spectrumKernel(s, t, n);
  }

  protected double spectrumKernel(EntityNode[] s, EntityNode[] t, int n)
  {
    double sum = 0.0D;

    for (int k = 1; k <= n; ++k) {
      for (int i = 0; i < s.length - k + 1; ++i)
        for (int j = 0; j < t.length - k + 1; ++j)
        {
          EntityNode[] array1 = new EntityNode[s.length];
          EntityNode[] array2 = new EntityNode[t.length];
          System.arraycopy(s, i, array1, 0, i + k);
          System.arraycopy(t, j, array2, 0, j + k);
          sum += spectrumKernelCompute(array1, array2);
        }
    }
    return sum;
  }

  double spectrumKernelCompute(EntityNode[] first, EntityNode[] second) {
    assert (first.length == second.length);

    double k = 0.0D;
    for (int i = 0; i < first.length; ++i)
    {
      k += this.wordCompObj.compare(first[i], second[i]);
    }
    return k;
  }

  public void setEntityKernelType(int i)
  {
  }
}