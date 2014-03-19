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

import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.text.DecimalFormat;

import javax.swing.UIManager;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.ModerateSkin;

public class Utils {
	
	public static void setSkin() {
		SubstanceLookAndFeel.setSkin(new ModerateSkin());
		UIManager.put(SubstanceLookAndFeel.SHOW_EXTRA_WIDGETS, Boolean.TRUE);
	}
	
	public static void locateOnScreenCenter(Component component) {
		Dimension paneSize = component.getSize();
	    Dimension screenSize = component.getToolkit().getScreenSize();
	    component.setLocation((screenSize.width - paneSize.width) / 2, (screenSize.height - paneSize.height) / 2);
	}

	public static void saveFile(File file, String fileContent){
		if(file!=null){
			try {
			    FileWriter fstream = new FileWriter(file);
			    BufferedWriter out = new BufferedWriter(fstream);
			    out.write(fileContent);
			    out.close();
		    } catch (Exception e){
		     	e.printStackTrace();
		    }
		}
	}
	
	public static File[] finder(final String fileExtension){
    	try {
    		File dir = new File(".");
    		File[] files = dir.listFiles(new FilenameFilter() { 
	    		public boolean accept(File dir, String filename){
	    			return filename.endsWith(fileExtension);
	    		}
	    	});
    		File[] ret = new File[files.length];
    		for (int i = 0; i < files.length; i++) 
				ret[i] = new File(files[i].getName());
    		return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
	
	public static String getDateHsMinSegString(long timeInMillis){
		int timeInSeconds = (int) (timeInMillis/1000);
		int hours, minutes, seconds;
		hours = timeInSeconds / 3600;
		timeInSeconds -= hours * 3600;
		minutes = timeInSeconds / 60;
		timeInSeconds -= minutes * 60;
		seconds = timeInSeconds;
		
		String ret="";
		if(hours!=0)
			ret += hours + "h";
		if(minutes!=0)
			ret += minutes + "m";
		ret += seconds + "s";
		return ret;
	}
	
	public static DecimalFormat getIntegerFormat(int maximumIntegerDigits){
		DecimalFormat decimalEditFormat = new DecimalFormat();
		decimalEditFormat.setMaximumFractionDigits(0);
		decimalEditFormat.setGroupingUsed(false);
		decimalEditFormat.setParseIntegerOnly(true);
		if(maximumIntegerDigits>0){
			decimalEditFormat.setMaximumIntegerDigits(maximumIntegerDigits);
			decimalEditFormat.setMinimumIntegerDigits(maximumIntegerDigits);
		}
		return decimalEditFormat;
	}
}
