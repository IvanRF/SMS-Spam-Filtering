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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;

import com.ivanrf.utils.CancelableButton;
import com.ivanrf.utils.Images;
import com.ivanrf.utils.IntegerTextField;
import com.ivanrf.utils.Utils;

public class SMSSpam extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private IntegerTextField wordsToKeepTextField;
	private JRadioButton tkCompleteRadioButton;
	private ButtonGroup tokenizerGroup;
	private JCheckBox attributeSelectionCheckBox;
	private JRadioButton cSMORadioButton;
	private ButtonGroup classifiersGroup;
	private JCheckBox boostingCheckBox;
	private JTextArea log;
	private CancelableButton trainButton;
	private CancelableButton evaluateButton;
	
	private JComboBox modelComboBox;
	private JTextArea sms;
	private CancelableButton classifyButton;
	private JLabel smsResult;
	private JTextArea logSMS;
	
	private Thread m_RunThread;
	
	private static SMSSpam instance;
	
	public static SMSSpam getInstance(){
		if(instance==null)
			instance = new SMSSpam();
		return instance;
	}
	
	private SMSSpam() {
		super();
		initGUI();
		setMinimumSize(new Dimension(800,600));
		setSize(800,600);
		Utils.locateOnScreenCenter(this);  
		setTitle("SMS Spam Filtering");
		setIconImage(Images.ICON);
		setVisible(true);
	}
	
	private void initGUI() {
		try {
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			setLayout(new BorderLayout());
			
			JTabbedPane jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("Classify", Images.CLASSIFY, getClassifyPanel());
			jTabbedPane.addTab("Train and Evaluate", Images.TRAIN_EVALUATE, getTrainPanel());
			jTabbedPane.addTab("About", Images.ABOUT, getAboutPanel());
			jTabbedPane.setFont(jTabbedPane.getFont().deriveFont(Font.BOLD, jTabbedPane.getFont().getSize()+4));
			this.add(jTabbedPane, BorderLayout.CENTER);
			
			setDefaultValues();
			setDefaultModel();
			
			pack();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JPanel getTrainPanel() {
		JPanel trainPanel = new JPanel(new BorderLayout());

		{
			JPanel selectionPanel = new JPanel(new GridBagLayout());
			trainPanel.add(selectionPanel, BorderLayout.NORTH);
			
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.weightx = 1.0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            {
				JPanel filterPanel = new JPanel(new BorderLayout());
				filterPanel.setBorder(BorderFactory.createTitledBorder("Filter"));
				constraints.gridwidth = GridBagConstraints.REMAINDER;
				selectionPanel.add(filterPanel, constraints);
				
				JPanel stwvPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
				filterPanel.add(stwvPanel, BorderLayout.NORTH);
				
				stwvPanel.add(new JLabel("<html><b>String To Word Vector</b></html>"));
				
				stwvPanel.add(new JLabel("   WordsToKeep"));
				
				wordsToKeepTextField = new IntegerTextField();
				wordsToKeepTextField.setColumns(6);
				stwvPanel.add(wordsToKeepTextField);
				
				stwvPanel.add(new JLabel("WordTokenizer: Delimiters"));

				tokenizerGroup = new ButtonGroup();
				
				addRadioButton(SpamClassifier.TOKENIZER_DEFAULT, " \\r\\n\\t.,;:'\"()?!", stwvPanel, tokenizerGroup);
				tkCompleteRadioButton = addRadioButton(SpamClassifier.TOKENIZER_COMPLETE, " \\r\\n\\t.,;:\'\"()?!-¿¡+*&#$%/=<>[]_`@\\^{}", stwvPanel, tokenizerGroup);
				addRadioButton(SpamClassifier.TOKENIZER_COMPLETE_NUMBERS, " \\r\\n\\t.,;:\'\"()?!-¿¡+*&#$%/=<>[]_`@\\^{}|“~0123456789", stwvPanel, tokenizerGroup);
    			
    			JPanel asPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				filterPanel.add(asPanel, BorderLayout.SOUTH);
				
    			attributeSelectionCheckBox = new JCheckBox("<html>Add <b>AttributeSelection</b> with Evaluator: InfoGainAttributeEval and Search: Ranker (threshold=0)</html>");
    			asPanel.add(attributeSelectionCheckBox);
            }
            {
				JPanel classifierPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				classifierPanel.setBorder(BorderFactory.createTitledBorder("Classifier"));
				constraints.gridy = 1;
				constraints.gridwidth = 1;
				constraints.fill = GridBagConstraints.BOTH;
				selectionPanel.add(classifierPanel, constraints);
				
				classifierPanel.add(new JLabel("Use"));
				
				classifiersGroup = new ButtonGroup();
				
				cSMORadioButton = addRadioButton(SpamClassifier.CLASSIFIER_SMO, "Implements John Platt's sequential minimal optimization algorithm for training a support vector classifier", classifierPanel, classifiersGroup);
				addRadioButton(SpamClassifier.CLASSIFIER_NB, "Class for a Naive Bayes classifier using estimator classes", classifierPanel, classifiersGroup);
				addRadioButton(SpamClassifier.CLASSIFIER_IB1, "K-nearest neighbours classifier. K=1", classifierPanel, classifiersGroup);
				addRadioButton(SpamClassifier.CLASSIFIER_IB3, "K-nearest neighbours classifier. K=3", classifierPanel, classifiersGroup);
				addRadioButton(SpamClassifier.CLASSIFIER_IB5, "K-nearest neighbours classifier. K=5", classifierPanel, classifiersGroup);
				addRadioButton(SpamClassifier.CLASSIFIER_PART, "Class for generating a PART decision list", classifierPanel, classifiersGroup);
				
				classifierPanel.add(Box.createRigidArea(new Dimension(20, 5)));
				
				boostingCheckBox = new JCheckBox("Apply Boosting (slow for NB)");
				classifierPanel.add(boostingCheckBox);
    			
				
				JPanel settingsPanel = new JPanel(new FlowLayout());
				settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
				constraints.gridx = 1;
				constraints.weightx = 0;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				selectionPanel.add(settingsPanel, constraints);
				
				JButton defaultsButton = new JButton("Set default values", Images.SETTINGS_16x16);
				settingsPanel.add(defaultsButton);
				defaultsButton.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent ae) {
		            	setDefaultValues();
					}
				});
            }
            constraints.weightx = 1.0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            {
            	JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				constraints.gridx = 0;
				constraints.gridy = 2;
				selectionPanel.add(buttonsPanel, constraints);
				
				trainButton = new CancelableButton("Train"){
					private static final long serialVersionUID = 1L;
					@Override
					protected boolean buttonActionPerformed() {
						return trainEvaluate(true);
					}
					@Override
					protected void cancelActionPerformed() {
						stopClassifier(log);
						setEnabled(true);
					}
				};
				buttonsPanel.add(trainButton);
				
				buttonsPanel.add(new JLabel("Trains the classifier on the SMSSpamCollection dataset and saves the trained model into a file."));
            }
            {
            	JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				constraints.gridy = 3;
				selectionPanel.add(buttonsPanel, constraints);
				
				evaluateButton = new CancelableButton("Evaluate"){
					private static final long serialVersionUID = 1L;
					@Override
					protected boolean buttonActionPerformed() {
						return trainEvaluate(false);
					}
					@Override
					protected void cancelActionPerformed() {
						stopClassifier(log);
						setEnabled(true);
					}
				};
				buttonsPanel.add(evaluateButton);
				
				buttonsPanel.add(new JLabel("Evaluates the classifier using a cross-validation (Folds=10, Random seed=1)."));
            }
		}
		{
			log = new JTextArea();
			DefaultCaret caret = (DefaultCaret) log.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			log.setEditable(false);
			log.setMargin(new Insets(2,2,2,2));
			
			trainPanel.add(new JScrollPane(log), BorderLayout.CENTER);
		}
		
		return trainPanel;
	}
	
	private JRadioButton addRadioButton(String text, String tooltip, JPanel panel, ButtonGroup buttonGroup){
		JRadioButton radioButton = new JRadioButton(text);
		radioButton.setActionCommand(text);
		radioButton.setToolTipText(tooltip);
		panel.add(radioButton);
		buttonGroup.add(radioButton);
		return radioButton;
	}
	
	private JPanel getClassifyPanel() {
		JPanel classifyPanel = new JPanel(new BorderLayout());

		{
			JPanel selectionPanel = new JPanel(new GridBagLayout());
			classifyPanel.add(selectionPanel, BorderLayout.NORTH);

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.weightx = 1.0;
	        constraints.fill = GridBagConstraints.HORIZONTAL;
	        int gridy=0;
			{
				JPanel modelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,5));
				constraints.gridy = gridy++;
				selectionPanel.add(modelPanel, constraints);
				
				modelPanel.add(new JLabel("Select the model to be used as classifier"));
				modelPanel.add(Box.createRigidArea(new Dimension(5, 5)));
				
				modelComboBox = new JComboBox(getModelFiles());
				modelPanel.add(modelComboBox);
				modelPanel.add(Box.createRigidArea(new Dimension(5, 5)));
				
				JButton updateButton = new JButton("Update list", Images.UPDATE_16x16);
				modelPanel.add(updateButton);
				updateButton.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent ae) {
		            	modelComboBox.setModel(new DefaultComboBoxModel(getModelFiles()));
		            	setDefaultModel();
					}
				});
				modelPanel.add(Box.createRigidArea(new Dimension(5, 5)));
				
				JButton defaultsButton = new JButton("Set default", Images.SETTINGS_16x16);
				modelPanel.add(defaultsButton);
				defaultsButton.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent ae) {
		            	setDefaultModel();
					}
				});
			}
			{
				JPanel smsPanel = new JPanel(new BorderLayout(5,5));
				constraints.gridy = gridy++;
				selectionPanel.add(smsPanel, constraints);
				
				JLabel label = new JLabel("SMS text message");
				label.setVerticalAlignment(SwingConstants.TOP);
				smsPanel.add(label, BorderLayout.WEST);
				
				sms = new JTextArea();
				sms.setMargin(new Insets(2,2,2,2));
				sms.setRows(6);
				sms.setLineWrap(true);
				sms.setWrapStyleWord(true);
				
				smsPanel.add(new JScrollPane(sms), BorderLayout.CENTER);
				
				JPanel buttonsPanel = new JPanel();
				buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
				smsPanel.add(buttonsPanel, BorderLayout.EAST);
						
				JButton smsSpamButton = new JButton("Load Spam example", Images.SETTINGS_16x16);
				buttonsPanel.add(smsSpamButton);
				smsSpamButton.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent ae) {
		            	sms.setText("U requested daily inspirational texts. Sub service is $9.99/mo + Msg&Data rates may apply. Reply STOP to cancel. Reply HELP for HELP. Enter on web: 6082");
					}
				});
				
				buttonsPanel.add(Box.createRigidArea(new Dimension(5, 5)));
				
				JButton smsHamButton = new JButton("Load Ham example", Images.SETTINGS_16x16);
				buttonsPanel.add(smsHamButton);
				smsHamButton.addActionListener(new ActionListener() {
		            public void actionPerformed(ActionEvent ae) {
		            	sms.setText("When you give importance to people they think that you are always free but they don't understand that you make yourself available for them every time");
					}
				});
			}
			{
				JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				constraints.gridy = gridy++;
				selectionPanel.add(buttonsPanel, constraints);
						
				classifyButton = new CancelableButton("Classify"){
					private static final long serialVersionUID = 1L;
					@Override
					protected boolean buttonActionPerformed() {
						return classify();
					}
					@Override
					protected void cancelActionPerformed() {
						stopClassifier(logSMS);
						setEnabled(true);
					}
				};
				buttonsPanel.add(classifyButton);
				
				buttonsPanel.add(new JLabel("Performs the classification of the SMS text message."));
			}
			{
				smsResult = new JLabel("", SwingConstants.CENTER);
				smsResult.setFont(smsResult.getFont().deriveFont(Font.BOLD, smsResult.getFont().getSize() + 10));
				smsResult.setBorder(BorderFactory.createEmptyBorder(0,0,5,0));
				constraints.gridy = gridy++;
				selectionPanel.add(smsResult, constraints);
			}
		}
		{
			logSMS = new JTextArea();
			DefaultCaret caret = (DefaultCaret) logSMS.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			logSMS.setEditable(false);
			logSMS.setMargin(new Insets(2,2,2,2));
			
	        classifyPanel.add(new JScrollPane(logSMS), BorderLayout.CENTER);
		}
		return classifyPanel;
	}
	
	private JPanel getAboutPanel() {
		JPanel aboutPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
		
		JLabel appName = new JLabel("SMS Spam Filtering", Images.IMAGE_ICON, SwingConstants.CENTER);
		appName.setFont(appName.getFont().deriveFont(Font.BOLD, appName.getFont().getSize()+14));
		aboutPanel.add(appName, constraints);
		
		String txt = "<html><center><br/>" +
					 "<b>Iván Ridao Freitas</b><br/>"+
					 "Web Data Mining<br/>"+
					 "Faculty of Exact Sciences<br/>"+
					 "Universidad Nacional del Centro de la Provincia de Buenos Aires"+
					 "</center></html>";
		JLabel txtLabel = new JLabel(txt, SwingConstants.CENTER);
		txtLabel.setFont(txtLabel.getFont().deriveFont((float)txtLabel.getFont().getSize()+4));
		constraints.gridy = 1;
		aboutPanel.add(txtLabel, constraints);
		
		return aboutPanel;
	}
	
	private void setSMSResult(String classPredicted){
		if(classPredicted==null){
			smsResult.setText("");
			smsResult.setIcon(null);
		} else if(classPredicted.equals("spam")){
			smsResult.setText("SMS is Spam");
			smsResult.setIcon(Images.SPAM);
			smsResult.setForeground(Color.RED);
		} else {
			smsResult.setText("SMS is Ham");
			smsResult.setIcon(Images.OK);
			smsResult.setForeground(null);
		}
	}

	private void setDefaultValues(){
		wordsToKeepTextField.setValue(1000000);
		tkCompleteRadioButton.setSelected(true);
		attributeSelectionCheckBox.setSelected(false);
		cSMORadioButton.setSelected(true);
		boostingCheckBox.setSelected(false);
	}
	
	private boolean trainEvaluate(final boolean train) {
		trainButton.setEnabled(train);
		evaluateButton.setEnabled(!train);
		classifyButton.setEnabled(false);
		log.setText("");
		
		final int wordsToKeep = wordsToKeepTextField.getValue();
		final String tokenizerOp = tokenizerGroup.getSelection().getActionCommand();
		final boolean useAttributeSelection = attributeSelectionCheckBox.isSelected();
		final String classifierOp = classifiersGroup.getSelection().getActionCommand();
		final boolean boosting = boostingCheckBox.isSelected();
		
		m_RunThread = new Thread(){
			@Override
			public void run() {
				try {
					if(train)
						SpamClassifier.train(wordsToKeep, tokenizerOp, useAttributeSelection, classifierOp, boosting, log);
					else
						SpamClassifier.evaluate(wordsToKeep, tokenizerOp, useAttributeSelection, classifierOp, boosting, log);
				} catch (Exception e) {
					log.append("Error");
				}
				enableButtons();
			}
		};
		m_RunThread.start();		
		return true;
	}
	
	private boolean classify(){
		trainButton.setEnabled(false);
		evaluateButton.setEnabled(false);
		logSMS.setText("");
		setSMSResult(null);
		
		final String model = modelComboBox.getSelectedItem().toString();
		final String text = sms.getText();
		
		m_RunThread = new Thread(){
			@Override
			public void run() {
				try {
					String classPredicted = SpamClassifier.classify(model, text, logSMS);
					setSMSResult(classPredicted);
				} catch (Exception e) {
					logSMS.append("Error");
				}
				enableButtons();
			}
		};
		m_RunThread.start();
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private void stopClassifier(JTextArea log) {
		if (m_RunThread != null) {
			m_RunThread.interrupt();

			// This is deprecated (and theoretically the interrupt should do).
			m_RunThread.stop();
	    }
		log.append("Cancelled by user");
		enableButtons();
	}
	
	private void enableButtons(){
		trainButton.setEnabled(true);
		evaluateButton.setEnabled(true);
		classifyButton.setEnabled(true);
	}
	
	private File[] getModelFiles(){
		File[] modelFiles = Utils.finder(".dat");
		if(modelFiles==null) //Si hay algun error pongo los mejores, para poder hacer pruebas
			modelFiles = new File[]{ new File("SMO_W1000000_TK-Complete_Boosting.dat"), new File("NaiveBayes_W1000000_TK-Numbers_Boosting.dat"), 
									 new File("PART_W1000_TK-Default.dat"), new File("IB1_W1000000_TK-Numbers.dat") };
		return modelFiles;
	}
	
	private void setDefaultModel(){
		modelComboBox.setSelectedItem(new File("SMO_W1000000_TK-Complete_Boosting.dat"));
	}
	
	public static void main(String[] args) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Utils.setSkin();
				SMSSpam.getInstance();
			}
		});
	}
}
