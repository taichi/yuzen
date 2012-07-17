package org.koshinuke.jgit;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

/**
 * @author taichi
 */
public class PassphraseProvider extends CredentialsProvider {

	@Override
	public boolean isInteractive() {
		return false;
	}

	@Override
	public boolean supports(CredentialItem... items) {
		return true;
	}

	@Override
	public boolean get(URIish uri, CredentialItem... items)
			throws UnsupportedCredentialItem {
		for (CredentialItem c : items) {
			if (c instanceof CredentialItem.StringType) {
				char[] passPhrase = System.console().readPassword(
						c.getPromptText());
				CredentialItem.StringType st = (CredentialItem.StringType) c;
				st.setValue(new String(passPhrase));
				return true;
			} else {
				throw new UnsupportedCredentialItem(uri, c.getPromptText());
			}
		}
		return false;
	}

}
