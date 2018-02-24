package com.kernel;

import java.util.HashMap;
import java.util.Map;
import com.thomson.research.svm.EntityNode;
import com.thomson.research.svm.UrduEmotionNode;

public class LinearKernel 
{
	private Map<Integer,Double> bowStringCacheMap ;
	double gamma = 0.09;
	double coef0 = 0.0 ;
	int degree = 3 ;
	public LinearKernel()
	{
		bowStringCacheMap = new HashMap<Integer,Double>() ;
	}

	public LinearKernel(WordComparator wcObj)
	{
		
	}
	
	public double unnormalizedLK(UrduEmotionNode[]  dataPoint1, 
			UrduEmotionNode[]  dataPoint2, int i, int j) throws Exception
	{
		
		double ret1 = unnormalizedLK(dataPoint1,i);
		double ret2 = unnormalizedLK(dataPoint2,j);
		double norm = (Math.sqrt(ret1) * Math.sqrt(ret2));
		double ret = unnormalizedLK(dataPoint1,dataPoint2) ;
		return (ret/norm);
	}
	
	public double unnormalizedLK(EntityNode[]  dataPoint1, 
			EntityNode[]  dataPoint2, int i, int j) 
	{
		
		double ret1 = unnormalizedLK(dataPoint1,i);
		double ret2 = unnormalizedLK(dataPoint2,j);
		double norm = (Math.sqrt(ret1) * Math.sqrt(ret2));
		double ret = unnormalizedLK(dataPoint1,dataPoint2) ;
		if (norm == 0.0 )
		{
			return 0.0 ;
		}
		return (ret/norm);
	}
	
	
	public double unnormalizedLK(UrduEmotionNode[]  dataPoint1, 
			UrduEmotionNode[]  dataPoint2, int j) 
	{
		
		double ret1 = unnormalizedLK(dataPoint1);
		double ret2 = unnormalizedLK(dataPoint2,j);
		double norm = (Math.sqrt(ret1) * Math.sqrt(ret2));
		double ret = unnormalizedLK(dataPoint1,dataPoint2) ;
		if (norm == 0.0 )
		{
			return 0.0 ;
		}
		return (ret/norm);
	}
	
	public double unnormalizedLK(EntityNode[]  dataPoint1, 
			EntityNode[]  dataPoint2, int j) 
	{
		
		double ret1 = unnormalizedLK(dataPoint1);
		double ret2 = unnormalizedLK(dataPoint2,j);
		double norm = (Math.sqrt(ret1) * Math.sqrt(ret2));
		if ( norm == 0.0 )
		{
			return 0.0 ;
		}
		double ret = unnormalizedLK(dataPoint1,dataPoint2) ;
		return (ret/norm);
	}
	
	
	public double unnormalizedLK(UrduEmotionNode[] s, int i)
	{
		double ret = 0.0 ;
		if(bowStringCacheMap.containsKey(i))
			return bowStringCacheMap.get(i) ;
		else
		{
			ret =  unnormalizedLK(s,s);
			bowStringCacheMap.put(i, ret);
			return ret;
		}
	}

	public double unnormalizedLK(EntityNode[] s, int i)
	{
		double ret = 0.0 ;
		if(bowStringCacheMap.containsKey(i))
			return bowStringCacheMap.get(i) ;
		else
		{
			ret =  unnormalizedLK(s,s);
			bowStringCacheMap.put(i, ret);
			return ret;
		}
	}

	
	public double unnormalizedLK(EntityNode[] s)
	{
		return unnormalizedLK(s,s); 
	}
	
	public double unnormalizedLK(EntityNode[] s, EntityNode[] t)
	{
		
		double sum = 0;
		int xlen = s.length;
		int ylen = t.length;
		int i = 0;
		int j = 0;
		while(i < xlen && j < ylen)
		{
			if(s[i].index == t[j].index)
				sum += s[i++].value * t[j++].value;
			else
			{
				if(s[i].index > t[j].index)
					++j;
				else
					++i;
			}
		}
		
		if (Double.isNaN(sum))
	    {
	       sum = 0.0 ;
	    }
	    if (Double.isInfinite(sum))
	    {
	       sum = 0.0 ;
	    }
	    
		return sum;
	
	}


	public double unnormalizedLK(UrduEmotionNode[] s, UrduEmotionNode[] t)
	{
		
		double sum = 0;
		int xlen = s.length;
		int ylen = t.length;
		int i = 0;
		int j = 0;
		while(i < xlen && j < ylen)
		{
			if(s[i].index == t[j].index)
				sum += s[i++].value * t[j++].value;
			else
			{
				if(s[i].index > t[j].index)
					++j;
				else
					++i;
			}
		}
		return sum;
	
	}

    public double unnormalizedLK( UrduEmotionNode[] dataPoint1 )
    {
        // TODO Auto-generated method stub
        return unnormalizedLK(dataPoint1,dataPoint1); 
    }
	
  
    public double dot (UrduEmotionNode[] s, UrduEmotionNode[] t)
    {
        double sum = 0;
        int xlen = s.length;
        int ylen = t.length;
        int i = 0;
        int j = 0;
        while(i < xlen && j < ylen)
        {
            if(s[i].index == t[j].index)
                sum += s[i++].value * t[j++].value;
            else
            {
                if(s[i].index > t[j].index)
                    ++j;
                else
                    ++i;
            }
        }
        return sum;
    
    }
    
    private double powi( double base, int times )
    {
        double tmp = base, ret = 1.0;

        for ( int t = times; t > 0; t /= 2 )
        {
            if ( t % 2 == 1 )
                ret *= tmp;
            tmp = tmp * tmp;
        }
        return ret;
    }



}
