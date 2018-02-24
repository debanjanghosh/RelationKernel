package com.kernel;

public class EntitySVMParameters 
{
	public String JTPModelPath ; //JTextPro - the CRF chunker
	public String WNPath ;	//WordNet 2.0 installation 
	public String EntityDataPath ; //the data path for the whole project
	public String OpenNLPPath ; //POS tagger path
	public int TASK_TYPE ; //what to do with the project?
	public boolean PREPROC_ENTITY  ; //creating entity sequences
	public boolean TRAINING_KERNEL  ;//creating training/testing k-gram matrix
	public boolean TRAINING_SVM  ;//training with libSVM
	public int DROP_INTRA_ENTITY ;
	public int RELATION_KERNEL ;
	public int ENTITY_KERNEL ;
	public int BoW_KERNEL ;
	public int STRING_KERNEL_TYPE ;
	
	public EntitySVMParameters()
	{
		//default values
		STRING_KERNEL_TYPE = 0; //sequence kernel
		RELATION_KERNEL = 1 ; //run the pattern kernel
		ENTITY_KERNEL = 0 ;//dont run the entity kernel
		BoW_KERNEL = 0 ; //dont run the linear kernel
		DROP_INTRA_ENTITY = 0 ;// dont use the drop intra entity feature
	}
	
	public void setMainOption()
	{
		if(TASK_TYPE == 1)
			PREPROC_ENTITY = true ;
		if(TASK_TYPE == 2)
			TRAINING_KERNEL = true ;
		if(TASK_TYPE == 3)
			TRAINING_SVM = true ;
		
	}
}
