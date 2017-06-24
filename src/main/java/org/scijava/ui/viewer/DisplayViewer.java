/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
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

package org.scijava.ui.viewer;

import org.scijava.Disposable;
import org.scijava.display.Display;
import org.scijava.display.event.DisplayActivatedEvent;
import org.scijava.display.event.DisplayDeletedEvent;
import org.scijava.display.event.DisplayUpdatedEvent;
import org.scijava.display.event.DisplayUpdatedEvent.DisplayUpdateLevel;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.RichPlugin;
import org.scijava.ui.UserInterface;

/**
 * A display viewer is a UI widget that shows a display to a user.
 * <p>
 * Display viewers discoverable at runtime must implement this interface and be
 * annotated with @{@link Plugin} with attribute {@link Plugin#type()} =
 * {@link DisplayViewer}.class. While it possible to create a display viewer
 * merely by implementing this interface, it is encouraged to instead extend
 * {@link AbstractDisplayViewer}, for convenience.
 * </p>
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 * @see Plugin
 */
public interface DisplayViewer<T> extends RichPlugin, Disposable {

	/** Returns true if this display viewer can be used with the given UI. */
	boolean isCompatible(final UserInterface ui);

	/**
	 * Returns true if an instance of this display viewer can view the given
	 * display.
	 */
	boolean canView(Display<?> d);

	/**
	 * Begins viewing the given display.
	 * 
	 * @param w The frame / window that will contain the GUI elements
	 * @param d the model for the display to show.
	 */
	void view(DisplayWindow w, Display<?> d);

	/** Gets the display being viewed. */
	Display<T> getDisplay();

	/** Gets the window in which the view is displayed. */
	DisplayWindow getWindow();

	/**
	 * Installs the display panel.
	 * 
	 * @param panel the panel used to host the gui
	 */
	void setPanel(DisplayPanel panel);

	/** Gets the display panel that hosts the gui elements. */
	DisplayPanel getPanel();

	/**
	 * Creates a {@link DisplayWindow} to house the given {@link Display}. Viewers
	 * wishing to customize the actual UI window used may override this method;
	 * otherwise, the framework will fall back the default {@link DisplayWindow}
	 * for the relevant UI.
	 * 
	 * @param d The display for which a window is desired.
	 * @return The newly created window.
	 */
	default DisplayWindow createWindow(final Display<?> d) {
		return null;
	}

	/** Synchronizes the user interface appearance with the display model. */
	default void onDisplayUpdatedEvent(final DisplayUpdatedEvent e) {
		if (e.getLevel() == DisplayUpdateLevel.REBUILD) {
			getPanel().redoLayout();
		}
		getPanel().redraw();
	}

	/** Removes the user interface when the display is deleted. */
	@SuppressWarnings("unused")
	default void onDisplayDeletedEvent(final DisplayDeletedEvent e) {
		getPanel().getWindow().close();
	}

	/**
	 * Handles a display activated event directed at this viewer's display. Note
	 * that the event's display may not be the viewer's display, but the active
	 * display will always be the viewer's display.
	 */
	@SuppressWarnings("unused")
	default void onDisplayActivatedEvent(final DisplayActivatedEvent e) {
		getPanel().getWindow().requestFocus();
	}

	// -- Disposable methods --

	@Override
	default void dispose() {
		final DisplayWindow w = getWindow();
		if (w != null) w.close();
	}
}
