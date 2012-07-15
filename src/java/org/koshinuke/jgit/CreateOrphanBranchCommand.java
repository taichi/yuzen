package org.koshinuke.jgit;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RefUpdate.Result;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.StringUtils;

/**
 * Create a new orphan branch.
 * 
 * @author taichi
 * @see <a
 *      href="http://www.kernel.org/pub/software/scm/git/docs/git-checkout.html"
 *      >Git documentation about Checkout</a>
 */
public class CreateOrphanBranchCommand extends GitCommand<Ref> {

	String name;

	String startPoint = null;

	RevCommit startCommit;

	List<String> conflicts = Collections.emptyList();

	List<String> toBeDeleted = Collections.emptyList();

	public CreateOrphanBranchCommand(@Nonnull Repository repository) {
		super(repository);
	}

	public CreateOrphanBranchCommand setName(@Nonnull String name) {
		this.checkCallable();
		this.name = name;
		return this;
	}

	public CreateOrphanBranchCommand setStartPoint(String startPoint) {
		this.checkCallable();
		this.startPoint = startPoint;
		this.startCommit = null;
		return this;
	}

	public CreateOrphanBranchCommand setStartPoint(RevCommit startPoint) {
		this.checkCallable();
		this.startPoint = null;
		this.startCommit = startPoint;
		return this;
	}

	@Override
	public Ref call() throws GitAPIException, RefNotFoundException,
			CheckoutConflictException, InvalidRefNameException,
			RefAlreadyExistsException {
		this.checkCallable();
		try {
			this.processOptions();
			this.checkoutStartPoint();
			RefUpdate update = this.getRepository().updateRef(Constants.HEAD);
			Result r = update.link(this.getBranchName());
			if (EnumSet.of(Result.NEW, Result.FORCED).contains(r) == false) {
				throw new JGitInternalException(MessageFormat.format(
						JGitText.get().checkoutUnexpectedResult, r.name()));
			}
			this.setCallable(false);
			return this.getRepository().getRef(Constants.HEAD);
		} catch (IOException e) {
			throw new JGitInternalException(e.getMessage(), e);
		}
	}

	protected void processOptions() throws InvalidRefNameException,
			RefAlreadyExistsException, IOException {
		String branchName = this.getBranchName();
		if (this.name == null || Repository.isValidRefName(branchName) == false) {
			throw new InvalidRefNameException(MessageFormat.format(JGitText
					.get().branchNameInvalid, this.name == null ? "<null>"
					: this.name));
		}
		Ref refToCheck = this.getRepository().getRef(branchName);
		if (refToCheck != null) {
			throw new RefAlreadyExistsException(MessageFormat.format(
					JGitText.get().refAlreadyExists, this.name));
		}
	}

	protected String getBranchName() {
		if (this.name.startsWith(Constants.R_REFS)) {
			return this.name;
		}
		return Constants.R_HEADS + this.name;
	}

	protected void checkoutStartPoint() throws GitAPIException,
			RefNotFoundException, CheckoutConflictException, IOException {
		ObjectId sp = this.getStartPoint();
		if (sp != null) {
			this.checkout(sp);
		}
	}

	protected ObjectId getStartPoint() throws RefNotFoundException, IOException {
		if (this.startCommit != null) {
			return this.startCommit.getId();
		}
		if (StringUtils.isEmptyOrNull(this.startPoint) == false) {
			ObjectId oid = this.getRepository().resolve(this.startPoint);
			if (oid == null) {
				throw new RefNotFoundException(MessageFormat.format(
						JGitText.get().refNotResolved, this.startPoint));
			}
			return oid;
		}
		return null;
	}

	protected void checkout(ObjectId fromId) throws GitAPIException,
			CheckoutConflictException, IOException {
		RevWalk rw = new RevWalk(this.getRepository());
		try {
			Ref headRef = this.repo.getRef(Constants.HEAD);
			AnyObjectId headId = headRef.getObjectId();
			RevCommit headCommit = headId == null ? null : rw
					.parseCommit(headId);
			RevTree headTree = headCommit == null ? null : headCommit.getTree();
			RevCommit from = rw.parseCommit(fromId);
			this.checkout(headTree, from.getTree());
		} finally {
			rw.release();
		}
	}

	protected void checkout(RevTree headTree, RevTree fromTree)
			throws GitAPIException, CheckoutConflictException, IOException {
		// DirCacheCheckout free lock of DirCache
		DirCacheCheckout dco = new DirCacheCheckout(this.getRepository(),
				headTree, this.repo.lockDirCache(), fromTree);
		dco.setFailOnConflict(true);
		try {
			dco.checkout();
			this.toBeDeleted = dco.getToBeDeleted();
		} catch (org.eclipse.jgit.errors.CheckoutConflictException e) {
			this.conflicts = dco.getConflicts();
			throw new CheckoutConflictException(dco.getConflicts(), e);
		}
	}

	public List<String> getConflicts() {
		return this.conflicts;
	}

	public List<String> getToBeDeleted() {
		return this.toBeDeleted;
	}
}
