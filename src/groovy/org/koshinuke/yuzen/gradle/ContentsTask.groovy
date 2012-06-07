package org.koshinuke.yuzen.gradle;

import java.io.File;
import org.gradle.api.file.FileTreeElement;


/**
 * @author taichi
 */
public interface ContentsTask {

	File contentsDir

	File destinationDir

	String templatePrefix

	String templateSuffix

	void processFile(FileTreeElement file)

	void deleteFile(FileTreeElement file)
}
