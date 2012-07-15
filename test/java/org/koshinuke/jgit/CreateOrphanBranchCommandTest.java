package org.koshinuke.jgit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
		Files.write(name, file, Constants.CHARSET);
		git.add().addFilepattern(path).call();
		return git.commit().setMessage("add " + name).call();
	}

	@Test
	public void orphan() throws Exception {
		Ref ref = this.target.setName("ppp").call();
		assertNotNull(ref);
		assertEquals("refs/heads/ppp", ref.getTarget().getName());

		File HEAD = new File(this.repoDir, ".git/HEAD");
		String headRef = Files.readFirstLine(HEAD, Constants.CHARSET);
		assertEquals("ref: refs/heads/ppp", headRef);
		assertEquals(4, this.repoDir.list().length);

		File heads = new File(this.repoDir, ".git/refs/heads");
		assertEquals(1, heads.listFiles().length);

		this.noHead();
		this.assertStatus(3);
	}

	protected void noHead() throws GitAPIException {
		try {
			this.git.log().call();
			fail();
		} catch (NoHeadException e) {
			assertTrue(true);
		}
	}

	protected void assertStatus(int files) throws GitAPIException {
		Status status = this.git.status().call();
		assertFalse(status.isClean());
		assertEquals(files, status.getAdded().size());
	}

	@Test
	public void startCommit() throws Exception {
		this.target.setStartPoint(this.commits.get(1)).setName("qqq").call();
		assertEquals(3, this.repoDir.list().length);
		this.noHead();
		this.assertStatus(2);
	}

	@Test
	public void startPoint() throws Exception {
		this.target.setStartPoint("HEAD^^").setName("zzz").call();
		assertEquals(2, this.repoDir.list().length);
		this.noHead();
		this.assertStatus(1);
	}

	@Test
	public void linkFail() throws Exception {
		File HEAD = new File(this.repoDir, ".git/HEAD");
		try (FileInputStream in = new FileInputStream(HEAD)) {
			this.target.setName("aaa").call();
			fail();
		} catch (JGitInternalException e) {
			assertTrue(true);
		}
	}

	@Test
	public void invalidRefName() throws Exception {
		try {
			this.target.setName("../hoge").call();
			fail();
		} catch (InvalidRefNameException e) {
			assertTrue(true);
		}
	}

	@Test
	public void alreadyExists() throws Exception {
		this.git.checkout().setCreateBranch(true).setName("ppp").call();
		this.git.checkout().setName("master").call();

		try {
			this.target.setName("ppp").call();
			fail();
		} catch (RefAlreadyExistsException e) {
			assertTrue(true);
		}
	}

	@Test
	public void refNotFound() throws Exception {
		try {
			this.target.setStartPoint("1234567").setName("ppp").call();
			fail();
		} catch (RefNotFoundException e) {
			assertTrue(true);
		}
	}

	@Test
	public void toBeDeleted() throws Exception {
		File ccc = new File(this.repoDir, "ccc.txt");
		try (FileInputStream in = new FileInputStream(ccc)) {
			this.target.setName("zzz").setStartPoint(this.commits.get(0))
					.call();
			assertEquals(1, this.target.getToBeDeleted().size());
		}
	}

	@Test
	public void conflicts() throws Exception {
		this.git.checkout().setCreateBranch(true).setName("aaa").call();
		File bbb = new File(this.repoDir, "bbb.txt");
		Files.write("zzzz\nzzz", bbb, Constants.CHARSET);
		try {
			this.target.setName("zzz").setStartPoint(this.commits.get(0))
					.call();
			fail();
		} catch (CheckoutConflictException e) {
			assertEquals(1, this.target.conflicts.size());
			assertEquals(1, e.getConflictingPaths().size());
			assertTrue(true);
		}
	}
}
