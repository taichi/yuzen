package org.koshinuke.yuzen.util;

import java.io.IOException;

/**
 * @author taichi
 */
public class IORuntimeException extends RuntimeException {

	private static final long serialVersionUID = 7577213703964552893L;

	public IORuntimeException(IOException cause) {
		super(cause);
	}
}
