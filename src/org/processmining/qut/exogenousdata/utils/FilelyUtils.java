package org.processmining.qut.exogenousdata.utils;

import java.awt.Component;
import java.io.File;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FilelyUtils {
	
	private FilelyUtils () {}
	private static final JFileChooser fc = new JFileChooser();
	
	/**
	 * User friendly way to ask for a file location to store data.
	 * @param dummyName 
	 * @param extension
	 * @param extensionName
	 * @param owner
	 * @return A File or null.
	 */
	static public File getFileFromUser(
			String dummyName, String extension, String extensionName,
			Component owner) {
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setSelectedFile(new File(dummyName+"."+extension));
		fc.setFileFilter(new FileNameExtensionFilter(extensionName, extension));
		int ret = fc.showSaveDialog(owner);
		if (ret == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
		}
		return null;
	}
	
	static public Path getDirectoryPathFromUser(
			Component owner) {
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int ret = fc.showSaveDialog(owner);
		if (ret == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile().toPath();
		}
		return null;
	}

}
