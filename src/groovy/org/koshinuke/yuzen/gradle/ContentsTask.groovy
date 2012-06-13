package org.koshinuke.yuzen.gradle;

import java.io.File;

import org.gradle.api.file.ConfigurableFileTree;


/**
 * @author taichi
 */
public interface ContentsTask {

	ConfigurableFileTree contents

	File destinationDir

	String templatePrefix

	String templateSuffix
}
