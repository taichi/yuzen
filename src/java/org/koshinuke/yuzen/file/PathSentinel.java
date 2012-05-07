package org.koshinuke.yuzen.file;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.koshinuke._;
import org.koshinuke.yuzen.util.WatchServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author taichi
 */
public class PathSentinel {

	static Logger LOG = LoggerFactory.getLogger(PathSentinel.class);

	protected Deque<PathEvent> events = new LinkedList<>();

	protected CopyOnWriteArrayList<PathEventListener> listeners = new CopyOnWriteArrayList<>();

	protected Map<WatchEvent.Kind<?>, PathEventDispatcher> dispatchers = new HashMap<>();

	protected WatchService watchService = WatchServiceUtil.newWatchService();

	protected ExecutorService watcherExecutor;

	protected ScheduledExecutorService workerExecutor;

	public PathSentinel() {
		this(Executors.newFixedThreadPool(1), Executors
				.newScheduledThreadPool(1));
	}

	public PathSentinel(@Nonnull ExecutorService watcherExecutor,
			@Nonnull ScheduledExecutorService workerExecutor) {
		Objects.requireNonNull(watcherExecutor);
		Objects.requireNonNull(workerExecutor);
		this.watcherExecutor = watcherExecutor;
		this.workerExecutor = workerExecutor;
		this.setUpDispatchers();
	}

	public PathSentinel watch(@Nonnull String path) {
		Objects.requireNonNull(path);
		this.watch(Paths.get(path));
		return this;
	}

	public PathSentinel watch(@Nonnull Path path) {
		Objects.requireNonNull(path);
		WatchServiceUtil.watch(this.watchService, path);
		return this;
	}

	public PathSentinel watchTree(@Nonnull Path path) {
		Objects.requireNonNull(path);
		WatchServiceUtil.watchTree(this.watchService, path);
		return this;
	}

	public PathSentinel register(@Nonnull PathEventListener _) {
		Objects.requireNonNull(_);
		this.listeners.add(_);
		return this;
	}

	public PathSentinel unregister(@Nonnull PathEventListener _) {
		Objects.requireNonNull(_);
		this.listeners.remove(_);
		return this;
	}

	public void startUp() {
		this.startWatcher();
		this.startWorker();
	}

	protected void startWatcher() {
		this.watcherExecutor.submit(new Callable<_>() {
			@Override
			public _ call() throws Exception {
				try {
					LOG.debug("take watchkey");
					WatchKey key = PathSentinel.this.watchService.take();
					Path path = Path.class.cast(key.watchable());
					for (WatchEvent<?> i : key.pollEvents()) {
						WatchEvent.Kind<?> kind = i.kind();
						Path resolved = path.resolve(Path.class.cast(i
								.context()));
						DefaultPathEvent event = new DefaultPathEvent(kind,
								resolved);
						PathSentinel.this.add(event);
					}
					key.reset();
					PathSentinel.this.watcherExecutor.submit(this);
					return _._;
				} catch (ClosedWatchServiceException
						| RejectedExecutionException e) {
					LOG.debug("any time no problem.", e);
					throw e;
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					throw e;
				}
			}
		});
	}

	protected boolean add(PathEvent event) {
		synchronized (this.events) {
			if (this.events.contains(event)) {
				LOG.debug("duplicated {}", event);
				return false;
			} else {
				LOG.debug("queued {}", event);
				this.events.add(event);
				return true;
			}
		}
	}

	protected void clearEvents() {
		synchronized (this.events) {
			this.events.clear();
		}
	}

	protected boolean hasEvents() {
		synchronized (this.events) {
			return this.events.isEmpty() == false;
		}
	}

	protected PathEvent take() {
		synchronized (this.events) {
			return this.events.removeFirst();
		}
	}

	protected void startWorker() {
		// TODO use more worker tasks ...
		this.workerExecutor.submit(new Callable<_>() {
			@Override
			public _ call() throws Exception {
				try {
					while (PathSentinel.this.hasEvents()) {
						PathEvent event = PathSentinel.this.take();
						PathSentinel.this.dispatch(event);
					}
					PathSentinel.this.workerExecutor.schedule(this, 10,
							TimeUnit.MILLISECONDS);
					return _._;
				} catch (RejectedExecutionException e) {
					LOG.debug("any time no problem.", e);
					throw e;
				} catch (Exception e) {
					LOG.error(e.getMessage(), e);
					throw e;
				}
			}
		});
	}

	protected void setUpDispatchers() {
		this.dispatchers.put(StandardWatchEventKinds.OVERFLOW,
				new PathEventDispatcher() {
					@Override
					public void dispatch(PathEventListener listener,
							PathEvent event) throws IOException {
						listener.overflowed();
					}
				});
		this.dispatchers.put(StandardWatchEventKinds.ENTRY_CREATE,
				new PathEventDispatcher() {
					@Override
					public void dispatch(PathEventListener listener,
							PathEvent event) throws IOException {
						listener.created(event);
					}
				});
		this.dispatchers.put(StandardWatchEventKinds.ENTRY_DELETE,
				new PathEventDispatcher() {
					@Override
					public void dispatch(PathEventListener listener,
							PathEvent event) throws IOException {
						listener.deleted(event);
					}
				});
		this.dispatchers.put(StandardWatchEventKinds.ENTRY_MODIFY,
				new PathEventDispatcher() {
					@Override
					public void dispatch(PathEventListener listener,
							PathEvent event) throws IOException {
						listener.modified(event);
					}
				});
	}

	protected void dispatch(PathEvent event) throws IOException {
		LOG.debug("dispatch {}", event);
		PathEventDispatcher dispatcher = this.dispatchers.get(event.getKind());
		if (dispatcher != null) {
			for (PathEventListener listener : this.listeners) {
				dispatcher.dispatch(listener, event);
			}
		}
	}

	public void shutdown() {
		this.listeners.clear();
		this.clearEvents();
		WatchServiceUtil.close(this.watchService);
		this.watcherExecutor.shutdownNow();
		this.workerExecutor.shutdownNow();
	}
}
