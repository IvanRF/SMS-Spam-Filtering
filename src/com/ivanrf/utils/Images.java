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

import java.awt.Image;

import javax.swing.ImageIcon;

public class Images {

	private static String IMG_FOLDER = "com/ivanrf/images/";
	
	public static Image ICON = getImage(IMG_FOLDER + "icon.png");
	
	public static ImageIcon CLASSIFY = getImageIcon(IMG_FOLDER + "classify.png");
	public static ImageIcon TRAIN_EVALUATE = getImageIcon(IMG_FOLDER + "train-evaluate.png");
	public static ImageIcon ABOUT = getImageIcon(IMG_FOLDER + "about.png");
	
	public static ImageIcon PLAY_16x16 = getImageIcon(IMG_FOLDER + "Play.png");
	public static ImageIcon STOP_16x16 = getImageIcon(IMG_FOLDER + "Stop.png");
	public static ImageIcon SETTINGS_16x16 = getImageIcon(IMG_FOLDER + "settings.png");
	public static ImageIcon UPDATE_16x16 = getImageIcon(IMG_FOLDER + "update.png");
	public static ImageIcon OK = getImageIcon(IMG_FOLDER + "ok.png");
	public static ImageIcon SPAM = getImageIcon(IMG_FOLDER + "spam.png");
	public static ImageIcon IMAGE_ICON = getImageIcon(IMG_FOLDER + "icon.png");
	
	private static Images instance; 
	
	private Images() {}
	
	public static Images getInstance() {
		if(instance==null)
			instance = new Images();
		return instance;
	}
	
	public static Image getImage(String image){
		return getImageIcon(image).getImage();
	}
	
	public static ImageIcon getImageIcon(String image){
		return new ImageIcon(getInstance().getClass().getClassLoader().getResource(image));
	}
}