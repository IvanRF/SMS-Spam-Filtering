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

import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class IntegerTextField extends JFormattedTextField {

	private static final long serialVersionUID = 1L;
	
	public IntegerTextField() {
		this(0);
	}
	
	public IntegerTextField(int maximumIntegerDigits) {
		this(maximumIntegerDigits, true);
	}
	
	public IntegerTextField(boolean editable) {
		this(0, editable);
	}
	
	public IntegerTextField(int maximumIntegerDigits, boolean editable) {
		super();
		setFormatterFactory(maximumIntegerDigits);
		setEditable(editable);
		if(maximumIntegerDigits!=0)
        	setColumns(maximumIntegerDigits - (maximumIntegerDigits/4));
        else //Default size
        	setColumns(4);
	}

	private void setFormatterFactory(int maximumIntegerDigits){
		DefaultFormatter edit = new NumberFormatter(Utils.getIntegerFormat(maximumIntegerDigits));
        edit.setValueClass(Integer.class);
		
		setFormatterFactory(new DefaultFormatterFactory(edit, edit, edit));
	}
	
	@Override
	public Integer getValue() {
		if(super.getValue()!=null)
			return ((Number)super.getValue()).intValue();
		return null;
	}
}
