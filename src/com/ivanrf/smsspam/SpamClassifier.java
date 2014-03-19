/*
 * Copyright (C) 2013 Ivan Ridao Freitas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivanrf.smsspam;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import javax.swing.JTextArea;

import com.ivanrf.utils.Utils;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.rules.PART;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.core.tokenizers.WordTokenizer;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class SpamClassifier {
	
	public static final String TOKENIZER_DEFAULT = "Default";
	public static final String TOKENIZER_COMPLETE = "Complete";
	public static final String TOKENIZER_COMPLETE_NUMBERS = "Complete with numbers";
	
	public static final String CLASSIFIER_SMO = "SMO";
	public static final String CLASSIFIER_NB = "NaiveBayes";
	public static final String CLASSIFIER_IB1 = "IB1";
	public static final String CLASSIFIER_IB3 = "IB3";
	public static final String CLASSIFIER_IB5 = "IB5";
	public static final String CLASSIFIER_PART = "PART";

	private static FilteredClassifier initFilterClassifier(int wordsToKeep, String tokenizerOp, boolean useAttributeSelection, String classifierOp, boolean boosting) throws Exception {
		StringToWordVector filter = new StringToWordVector();
		filter.setDoNotOperateOnPerClassBasis(true);
		filter.setLowerCaseTokens(true);
		filter.setWordsToKeep(wordsToKeep);
		
		if(!tokenizerOp.equals(TOKENIZER_DEFAULT)){
			//Make a tokenizer
			WordTokenizer wt = new WordTokenizer();
			if(tokenizerOp.equals(TOKENIZER_COMPLETE))
				wt.setDelimiters(" \r\n\t.,;:\'\"()?!-¿¡+*&#$%/=<>[]_`@\\^{}");
			else //TOKENIZER_COMPLETE_NUMBERS)
				wt.setDelimiters(" \r\n\t.,;:\'\"()?!-¿¡+*&#$%/=<>[]_`@\\^{}|“~0123456789");
			filter.setTokenizer(wt);
		}
		
		FilteredClassifier classifier = new FilteredClassifier();
		classifier.setFilter(filter);
		
		if(useAttributeSelection){
			AttributeSelection as = new AttributeSelection();
			as.setEvaluator(new InfoGainAttributeEval());
			Ranker r = new Ranker();
			r.setThreshold(0);
			as.setSearch(r);
			
			MultiFilter mf = new MultiFilter();
			mf.setFilters(new Filter[]{ filter, as });
			
			classifier.setFilter(mf);
		}
		
		if(classifierOp.equals(CLASSIFIER_SMO))
			classifier.setClassifier(new SMO());
		else if(classifierOp.equals(CLASSIFIER_NB))
			classifier.setClassifier(new NaiveBayes());
		else if(classifierOp.equals(CLASSIFIER_IB1))
			classifier.setClassifier(new IBk(1));
		else if(classifierOp.equals(CLASSIFIER_IB3))
			classifier.setClassifier(new IBk(3));
		else if(classifierOp.equals(CLASSIFIER_IB5))
			classifier.setClassifier(new IBk(5));
		else if(classifierOp.equals(CLASSIFIER_PART))
			classifier.setClassifier(new PART()); //Tarda mucho
		
		if(boosting){
			AdaBoostM1 boost = new AdaBoostM1();
			boost.setClassifier(classifier.getClassifier());
			classifier.setClassifier(boost); //Con NB tarda mucho
		}
		
		return classifier;
	}
	
	public static void train(int wordsToKeep, String tokenizerOp, boolean useAttributeSelection, String classifierOp, boolean boosting, JTextArea log){
		try {
			long start = System.currentTimeMillis();
			
			String modelName = getModelName(wordsToKeep, tokenizerOp, useAttributeSelection, classifierOp, boosting);
			showEstimatedTime(true, modelName, log);
			
			Instances trainData = loadDataset("SMSSpamCollection.arff", log);
			trainData.setClassIndex(0);
			
			FilteredClassifier classifier = initFilterClassifier(wordsToKeep, tokenizerOp, useAttributeSelection, classifierOp, boosting);
			
			publishEstado("=== Building the classifier on the filtered data ===", log);
			classifier.buildClassifier(trainData);

			publishEstado(classifier.toString(), log);
			publishEstado("=== Training done ===", log);
			
			saveModel(classifier, modelName, log);
			
			publishEstado("Elapsed time: " + Utils.getDateHsMinSegString(System.currentTimeMillis() - start), log);
		} catch (Exception e) {
			e.printStackTrace();
			publishEstado("Error found when training", log);
		}
	}
	
	public static void evaluate(int wordsToKeep, String tokenizerOp, boolean useAttributeSelection, String classifierOp, boolean boosting, JTextArea log){
		try {
			long start = System.currentTimeMillis();
			
			String modelName = getModelName(wordsToKeep, tokenizerOp, useAttributeSelection, classifierOp, boosting);
			showEstimatedTime(false, modelName, log);
			
			Instances trainData = loadDataset("SMSSpamCollection.arff", log);
			trainData.setClassIndex(0);
			FilteredClassifier classifier = initFilterClassifier(wordsToKeep, tokenizerOp, useAttributeSelection, classifierOp, boosting);
			
			publishEstado("=== Performing cross-validation ===", log);
			Evaluation eval = new Evaluation(trainData);
//			eval.evaluateModel(classifier, trainData);
			eval.crossValidateModel(classifier, trainData, 10, new Random(1));
			
			publishEstado(eval.toSummaryString(), log);
			publishEstado(eval.toClassDetailsString(), log);
			publishEstado(eval.toMatrixString(), log);
			publishEstado("=== Evaluation finished ===", log);
			
			publishEstado("Elapsed time: " + Utils.getDateHsMinSegString(System.currentTimeMillis() - start), log);
		} catch (Exception e) {
			e.printStackTrace();
			publishEstado("Error found when evaluating", log);
		}
	}
	
	public static String classify(String model, String text, JTextArea log){
		FilteredClassifier classifier = loadModel(model, log);
		
		//Create the instance
		ArrayList<String> fvNominalVal = new ArrayList<String>();
		fvNominalVal.add("ham");
		fvNominalVal.add("spam");
		
		Attribute attribute1 = new Attribute("spam_class", fvNominalVal);
		Attribute attribute2 = new Attribute("text", (List<String>) null);
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(attribute1);
		fvWekaAttributes.add(attribute2);
		
		Instances instances = new Instances("Test relation", fvWekaAttributes, 1);
		instances.setClassIndex(0);

		DenseInstance instance = new DenseInstance(2);
		instance.setValue(attribute2, text);
		instances.add(instance);
		
		publishEstado("=== Instance created ===", log);
		publishEstado(instances.toString(), log);
				
		//Classify the instance
		try {
			publishEstado("=== Classifying instance ===", log);
			
			double pred = classifier.classifyInstance(instances.instance(0));
			
			publishEstado("=== Instance classified  ===", log);
			
			String classPredicted = instances.classAttribute().value((int) pred);
			publishEstado("Class predicted: " + classPredicted, log);
			
			return classPredicted;
		} catch (Exception e) {
			publishEstado("Error found when classifying the text", log);
			return null;
		}
	}
	
	private static Instances loadDataset(String fileName, JTextArea log) {
		try {
			publishEstado("=== Loading dataset: " + fileName + " ===", log);
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			ArffReader arff = new ArffReader(reader);
			Instances trainData = arff.getData();
			reader.close();
			publishEstado("=== Dataset loaded ===", log);
			return trainData;
		} catch (IOException e) {
			publishEstado("Error found when reading: " + fileName, log);
			e.printStackTrace();
			return null;
		}
	}
	
	private static String getModelName(int wordsToKeep, String tokenizerOp, boolean useAttributeSelection, String classifierOp, boolean boosting) {
		String tk = "TK-";
		if(tokenizerOp.equals(TOKENIZER_DEFAULT))
			tk += "Default";
		else if(tokenizerOp.equals(TOKENIZER_COMPLETE))
			tk += "Complete";
		else //TOKENIZER_COMPLETE_NUMBERS
			tk += "Numbers";
		
		String modelName = classifierOp + "_W" + wordsToKeep + "_" + tk;
		
		if(useAttributeSelection)
			modelName += "_AttSelection";
		if(boosting)
			modelName += "_Boosting";
		
		return modelName + ".dat";
	}
	
	private static void saveModel(FilteredClassifier classifier, String fileName, JTextArea log) {
		try {
			publishEstado("=== Saving model: " + fileName + " ===", log);
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(classifier);
            out.close();
            publishEstado("=== Model saved ===", log);
        } catch (IOException e) {
        	publishEstado("Error found when writing: " + fileName, log);
		}
	}
	
	private static FilteredClassifier loadModel(String fileName, JTextArea log) {
		try {
			publishEstado("=== Loading model: " + fileName + " ===", log);
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
            FilteredClassifier classifier = (FilteredClassifier) in.readObject();
            in.close();
            publishEstado("=== Model loaded ===", log);
            return classifier;
		} catch (Exception e) {
			publishEstado("Error found when reading: " + fileName, log);
			return null;
		}
	}
	
	private static void publishEstado(String estado, JTextArea log){
		log.append(estado + "\n");
	}
	
	//************************************* TIEMPOS ESTIMADOS ****************************************//
	
	private static void showEstimatedTime(boolean train, String modelName, JTextArea log){
		modelName = modelName.replace(".dat", "");
		String estimatedTime = (train) ? trainingTime.get(modelName) : evaluationTime.get(modelName);
		if(estimatedTime!=null)
			publishEstado("Estimated time: " + estimatedTime, log);
	}
	
	private static Hashtable<String, String> trainingTime = initTrainingTime();
	private static Hashtable<String, String> evaluationTime = initEvaluationTime();

	private static Hashtable<String, String> initTrainingTime() {
		Hashtable<String, String> tt = new Hashtable<String, String>();
		tt.put("SMO_W1000000_TK-Numbers", "23s");
		tt.put("SMO_W1000000_TK-Numbers_Boosting", "3m25s");
		tt.put("SMO_W1000000_TK-Complete_Boosting", "29s");
		tt.put("SMO_W1000000_TK-Complete", "33s");
		tt.put("SMO_W1000000_TK-Default", "27s");
		tt.put("SMO_W1000000_TK-Complete_AttSelection", "1m9s");
		tt.put("NaiveBayes_W1000000_TK-Numbers_Boosting", "39m47s (YOU CAN CANCEL)");
		tt.put("NaiveBayes_W1000000_TK-Complete_Boosting", "44m42s (YOU CAN CANCEL)");
		tt.put("SMO_W1000_TK-Complete", "11s");
		tt.put("SMO_W1000_TK-Default", "12s");
		tt.put("NaiveBayes_W1000000_TK-Numbers", "37s");
		tt.put("NaiveBayes_W1000000_TK-Complete", "41s");
		tt.put("NaiveBayes_W1000000_TK-Complete_AttSelection", "1m18s");
		tt.put("NaiveBayes_W1000_TK-Complete", "5s");
		tt.put("NaiveBayes_W1000000_TK-Default", "42s");
		tt.put("NaiveBayes_W1000_TK-Default", "5s");
		tt.put("PART_W1000000_TK-Default", "31m42s (YOU CAN CANCEL)");
		tt.put("PART_W1000_TK-Default", "3m39s");
		tt.put("PART_W1000_TK-Complete", "3m47s");
		tt.put("PART_W1000000_TK-Complete", "26m36s (YOU CAN CANCEL)");
		tt.put("IB1_W1000000_TK-Numbers", "4s");
		tt.put("IB1_W1000000_TK-Numbers_Boosting", "45s");
		tt.put("IB1_W1000_TK-Complete", "1s");
		tt.put("IB1_W1000_TK-Default", "1s");
		tt.put("IB1_W1000000_TK-Complete", "4s");
		tt.put("IB1_W1000000_TK-Complete_Boosting", "47s");
		tt.put("IB1_W1000000_TK-Complete_AttSelection", "1m10s");
		tt.put("IB1_W1000000_TK-Default", "4s");
		tt.put("IB3_W1000_TK-Complete", "1s");
		tt.put("IB3_W1000_TK-Default", "1s");
		tt.put("IB3_W1000000_TK-Complete", "4s");
		tt.put("IB3_W1000000_TK-Default", "5s");
		tt.put("IB5_W1000_TK-Complete", "2s");
		tt.put("IB5_W1000_TK-Default", "1s");
		tt.put("IB5_W1000000_TK-Complete", "4s");
		tt.put("IB5_W1000000_TK-Default", "5s");
		return tt;
	}

	private static Hashtable<String, String> initEvaluationTime() {
		Hashtable<String, String> et = new Hashtable<String, String>();
		et.put("SMO_W1000000_TK-Numbers", "2m16s");
		et.put("SMO_W1000000_TK-Numbers_Boosting", "18m24s");
		et.put("SMO_W1000000_TK-Complete_Boosting", "5m55s");
		et.put("SMO_W1000000_TK-Complete", "2m27s");
		et.put("SMO_W1000000_TK-Default", "2m38s");
		et.put("SMO_W1000000_TK-Complete_AttSelection", "8m53s");
		et.put("NaiveBayes_W1000000_TK-Numbers_Boosting", "5h42m6s (YOU CAN CANCEL)");
		et.put("NaiveBayes_W1000000_TK-Complete_Boosting", "8h1m (YOU CAN CANCEL)");
		et.put("SMO_W1000_TK-Complete", "1m34s");
		et.put("SMO_W1000_TK-Default", "1m31s");
		et.put("NaiveBayes_W1000000_TK-Numbers", "6m22s");
		et.put("NaiveBayes_W1000000_TK-Complete", "7m8s");
		et.put("NaiveBayes_W1000000_TK-Complete_AttSelection", "10m24s");
		et.put("NaiveBayes_W1000_TK-Complete", "58s");
		et.put("NaiveBayes_W1000000_TK-Default", "7m31s");
		et.put("NaiveBayes_W1000_TK-Default", "59s");
		et.put("PART_W1000000_TK-Default", "3h52m48s (YOU CAN CANCEL)");
		et.put("PART_W1000_TK-Default", "28m1s (YOU CAN CANCEL)");
		et.put("PART_W1000_TK-Complete", "33m22s (YOU CAN CANCEL)");
		et.put("PART_W1000000_TK-Complete", "3h35m51s (YOU CAN CANCEL)");
		et.put("IB1_W1000000_TK-Numbers", "1m38s");
		et.put("IB1_W1000000_TK-Numbers_Boosting", "6m29s");
		et.put("IB1_W1000_TK-Complete", "55s");
		et.put("IB1_W1000_TK-Default", "44s");
		et.put("IB1_W1000000_TK-Complete", "1m38s");
		et.put("IB1_W1000000_TK-Complete_Boosting", "6m52s");
		et.put("IB1_W1000000_TK-Complete_AttSelection", "9m8s");
		et.put("IB1_W1000000_TK-Default", "1m36s");
		et.put("IB3_W1000_TK-Complete", "57s");
		et.put("IB3_W1000_TK-Default", "50s");
		et.put("IB3_W1000000_TK-Complete", "1m44s");
		et.put("IB3_W1000000_TK-Default", "1m46s");
		et.put("IB5_W1000_TK-Complete", "59s");
		et.put("IB5_W1000_TK-Default", "51s");
		et.put("IB5_W1000000_TK-Complete", "1m46s");
		et.put("IB5_W1000000_TK-Default", "1m55s");
		return et;
	}
}
