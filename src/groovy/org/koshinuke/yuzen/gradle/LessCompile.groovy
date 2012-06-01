package org.koshinuke.yuzen.gradle

import java.net.URL;
import java.util.List;

import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.koshinuke.yuzen.util.FileUtil;
import org.lesscss.LessCompiler

/**
 * @author taichi
 */
class LessCompile extends SourceTask {

	@OutputDirectory
	def destinationDir

	LessCompiler compiler

	LessCompile() {
		this.group = BasePlugin.BUILD_GROUP
		this.compiler = new LessCompiler()
	}

	@TaskAction
	def compile() {
		getSource().visit([
					visitDir : {
					},
					visitFile : { FileTreeElement it ->
						def rel = it.relativePath
						def newrel = rel.parent.append(true, FileUtil.removeExtension(rel.lastName) + '.css')
						compiler.compile(it.file, newrel.getFile(destinationDir))
					}
				] as FileVisitor)
	}

	@Input
	public void setEnvJs(URL envJs) {
		this.compiler.setEnvJs(envJs);
	}

	@Input
	public void setLessJs(URL lessJs) {
		this.compiler.setLessJs(lessJs);
	}

	@Input
	public void setCustomJs(List<URL> customJs) {
		this.compiler.setCustomJs(customJs);
	}

	@Input
	public void setCompress(boolean compress) {
		this.compiler.setCompress(compress);
	}

	@Input
	public void setEncoding(String encoding) {
		this.compiler.setEncoding(encoding);
	}
}
