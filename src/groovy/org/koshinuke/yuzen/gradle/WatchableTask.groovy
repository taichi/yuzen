package org.koshinuke.yuzen.gradle;

import org.gradle.api.file.FileTreeElement;

/**
 * @author taichi
 */
public interface WatchableTask extends ContentsTask {

	void processFile(FileTreeElement file)

	void deleteFile(FileTreeElement file)
}
