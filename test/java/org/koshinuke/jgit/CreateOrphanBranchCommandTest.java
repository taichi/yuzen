package org.koshinuke.jgit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * @author taichi
 */
public class CreateOrphanBranchCommandTest {

	File repoDir;
	Git git;
	List<RevCommit> commits;

	CreateOrphanBranchCommand target;

	@Before
	public void setUp() throws Exception {
		this.repoDir = new File(Files.createTempDir(), "repo");
		this.git = Git.init().setDirectory(this.repoDir).call();
		this.commits = new ArrayList<>();
		this.commits.add(this.newFile(this.git, "aaa"));
		this.commits.add(this.newFile(this.git, "bbb"));
		this.commits.add(this.newFile(this.git, "ccc"));

		this.target = new CreateOrphanBranchCommand(this.git.getRepository());
	}

	@After
	public void tearDown() throws Exception {
		this.git.getRepository().close();
		FileUtils.delete(this.repoDir.getParentFile(), FileUtils.RECURSIVE);
	}

	protected RevCommit newFile(Git git, String name) throws Exception {
		String path = name + ".txt";
		File file = new File(this.repoDir, path);
		Files.write(name, file, Charsets.UTF_8);
		git.add().addFilepattern(path).call();
		return git.commit().setMessage("add " + name).call();
	}

	@Test
	public void orphan() throws Exception {
		this.target.setName("ppp").call();

		File HEAD = new File(this.repoDir, ".git/HEAD");
		String ref = Files.readFirstLine(HEAD, Charsets.UTF_8);
		assertEquals("ref: refs/heads/ppp", ref);
		assertEquals(4, this.repoDir.list().length);

		File heads = new File(this.repoDir, ".git/refs/heads");
		assertEquals(1, heads.listFiles().length);
	}

	@Test
	public void startCommit() throws Exception {
		this.target.setStartPoint(this.commits.get(1)).setName("qqq").call();
		assertEquals(3, this.repoDir.list().length);
	}

	@Test
	public void startPoint() throws Exception {
		this.target.setStartPoint("HEAD^^").setName("zzz").call();
		assertEquals(2, this.repoDir.list().length);
	}
}
