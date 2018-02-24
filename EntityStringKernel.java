package com.kernel;

import com.thomson.research.svm.EntityNode;
import com.thomson.research.svm.UrduEmotionNode;
import com.thomson.research.svm.UrduEntityNode;

/*
 * Represent an abstract string kernel class for 
 * Entity and Relation 
 */

public abstract class EntityStringKernel 
{
	protected WordComparator wordCompObj;

	public EntityStringKernel() {
	}

	public EntityStringKernel(WordComparator WCObj) {
		this.wordCompObj = WCObj;
	}

	public void setWCObj(WordComparator WCObj) {
		this.wordCompObj = WCObj;
	}

	public double unnormalizedSelfStringKernel(EntityNode[][] dataPoint) {
		return 0.0D;
	}

	public double unnormalizedSelfStringKernel(EntityNode[][] dataPoint, int i) {
		return 0.0D;
	}

	public double unnormalizedSelfStringKernel1(EntityNode[][] dataPoint) {
		return 0.0D;
	}

	public double unnormalizedSelfStringKernel(UrduEmotionNode[][] dataPoint,
			int i) {
		return 0.0D;
	}

	public double unnormalizedStringKernel(UrduEmotionNode[][] dataPoint1,
			UrduEmotionNode[][] dataPoint2) {
		return 0.0D;
	}

	public double unnormalizedSelfEntityKernel(EntityNode[][] dataPoint1) {
		return 0.0D;
	}

	public double unnormalizedEntityKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i, int j) {
		return 0.0D;
	}
	
	public double unnormalizedEntityKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int  j) {
		return 0.0D;
	}

	public double unnormalizedEntityKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2) {
		return 0.0D;
	}

	public double unnormalizedRankingStringKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2) {
		return 0.0D;
	}

	public double unnormalizedRankingStringKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i, int j) {
		return 0.0D;
	}

	public double unnormalizedRankingSeparateEntityKernel(
			EntityNode[][] dataPoint1, EntityNode[][] dataPoint2, int i, int j) {
		return 0.0D;
	}

	public double unnormalizedEntityRankingStringKernel(
			EntityNode[][] dataPoint1, EntityNode[][] dataPoint2, int i, int j) {
		return 0.0D;
	}

	public double unnormalizedRankingStringKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int j) {
		return 0.0D;
	}

	public double unnormalizedRankingEntityKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i, int j) {
		return 0.0D;
	}

	public double unnormalizedRankingEntityKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int j) {
		return 0.0D;
	}

	public double unnormalizedSelfStringKernel(UrduEntityNode[] dataPoint1) {
		return 0.0D;
	}

	public double unnormalizedStringKernel(UrduEntityNode[] dataPoint1,
			UrduEntityNode[] dataPoint2) {
		return 0.0D;
	}

	public abstract void setEntityKernelType(int paramInt);

	public double unnormalizedUrduStringKernel(UrduEmotionNode[][] dataPoint1,
			UrduEmotionNode[][] dataPoint2, int i, int j) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double unnormalizedUrduStringKernel(UrduEmotionNode[][] dataPoint1,
			UrduEmotionNode[][] dataPoint2, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double unnormalizedEntityRankingStringKernel(
			EntityNode[][] dataPoint1, EntityNode[][] dataPoint2, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double unnormalizedRankingSeparateEntityKernel(
			EntityNode[][] dataPoint1, EntityNode[][] dataPoint2, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double unnormalizedStringKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i, int j) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double unnormalizedSeparateEntityKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i, int j) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double unnormalizedSeparateEntityKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double unnormalizedStringKernel(EntityNode[][] dataPoint1,
			EntityNode[][] dataPoint2, int j) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double unnormalizedSarcStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2, int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double unnormalizedSarcStringKernel(EntityNode[][] dataPoint1, EntityNode[][] dataPoint2, int i, int j) {
		// TODO Auto-generated method stub
		return 0;
	}

}