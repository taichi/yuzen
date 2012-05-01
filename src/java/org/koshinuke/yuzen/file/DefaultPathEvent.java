package org.koshinuke.yuzen.file;

import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * @author taichi
 */
public class DefaultPathEvent implements PathEvent {

	final Kind<?> kind;

	final Path path;

	public DefaultPathEvent(@Nonnull Kind<?> kind, @Nonnull Path path) {
		Objects.requireNonNull(kind);
		Objects.requireNonNull(path);
		this.kind = kind;
		this.path = path;
	}

	@Override
	public Kind<?> getKind() {
		return this.kind;
	}

	@Override
	public Path getPath() {
		return this.path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (this.kind == null ? 0 : this.kind.hashCode());
		result = prime * result
				+ (this.path == null ? 0 : this.path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		PathEvent other = (PathEvent) obj;
		return this.kind.equals(other.getKind())
				&& this.path.equals(other.getPath());
	}

}
