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

package com.ivanrf.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public abstract class CancelableButton extends JButton {

	private static final long serialVersionUID = 1L;
	
	protected static String CANCEL = "Cancel";
	
	private String buttonText;
	
	public CancelableButton(String text) {
		super(text, Images.PLAY_16x16);
		this.buttonText = text;
		addActionListener(new ActionListener() {				
            public void actionPerformed(ActionEvent ae) {
            	cancelableButtonActionPerformed(ae);
            }
        });
	}
	
	protected void cancelableButtonActionPerformed(ActionEvent ae){
		if(isEnabled()){
	    	if(getText().equals(buttonText)){
	    		if(buttonActionPerformed()){
		    		setText(CANCEL);
		    		setIcon(Images.STOP_16x16);
	    		}
	    	} else if(getText().equals(CANCEL)){
	    		setText(buttonText);
	    		setIcon(Images.PLAY_16x16);
	    		setEnabled(false);
	    		
	    		cancelActionPerformed();			
	    	}
		}
    }
	
	@Override
	public void setEnabled(boolean b) {		
		super.setEnabled(b);
		if(b){
			setText(buttonText);
    		setIcon(Images.PLAY_16x16);
		}
	}
	
	protected abstract boolean buttonActionPerformed();
	protected abstract void cancelActionPerformed();
}
