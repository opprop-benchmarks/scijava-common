/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.thread;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for managing active threads.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public final class DefaultThreadService extends AbstractService implements
	ThreadService
{

	@Parameter
	private LogService log;

	private ExecutorService executor;

	private int nextThread = 0;

	private boolean disposed;

	private WeakHashMap<Thread, Thread> parents = new WeakHashMap<Thread, Thread>();

	// -- ThreadService methods --

	@Override
	public <V> Future<V> run(final Callable<V> code) {
		if (disposed) return null;
		return executor().submit(wrap(code));
	}

	@Override
	public Future<?> run(final Runnable code) {
		if (disposed) return null;
		return executor().submit(wrap(code));
	}

	@Override
	public boolean isDispatchThread() {
		return EventQueue.isDispatchThread();
	}

	@Override
	public void invoke(final Runnable code) throws InterruptedException,
		InvocationTargetException
	{
		if (isDispatchThread()) {
			// just call the code
			code.run();
		}
		else {
			// invoke on the EDT
			EventQueue.invokeAndWait(wrap(code));
		}
	}

	@Override
	public void queue(final Runnable code) {
		EventQueue.invokeLater(wrap(code));
	}

	@Override
	public Thread getParent(final Thread thread) {
		return parents.get(thread != null ? thread : Thread.currentThread());
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		disposed = true;
		if (executor != null) executor.shutdown();
	}

	// -- ThreadFactory methods --

	@Override
	public Thread newThread(final Runnable r) {
		final String contextHash = Integer.toHexString(context().hashCode());
		final String threadName =
			"SciJava-" + contextHash + "-Thread-" + nextThread++;
		return new Thread(r, threadName);
	}

	// -- Helper methods --

	private ExecutorService executor() {
		if (executor == null) {
			executor = Executors.newCachedThreadPool(this);
		}
		return executor;
	}

	private Runnable wrap(final Runnable r) {
		final Thread parent = Thread.currentThread();
		return new Runnable() {
			@Override
			public void run() {
				final Thread thread = Thread.currentThread();
				try {
					if (parent != thread) parents.put(thread, parent);
					r.run();
				}
				finally {
					if (parent != thread) parents.remove(thread);
				}
			}
		};
	}

	private <V> Callable<V> wrap(final Callable<V> c) {
		final Thread parent = Thread.currentThread();
		return new Callable<V>() {
			@Override
			public V call() throws Exception {
				final Thread thread = Thread.currentThread();
				try {
					if (parent != thread) parents.put(thread, parent);
					return c.call();
				}
				finally {
					if (parent != thread) parents.remove(thread);
				}
			}
		};
	}
}
