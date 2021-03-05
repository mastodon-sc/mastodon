/*
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.bdv;

import bdv.ui.BdvDefaultCards;
import bdv.ui.CardPanel;
import bdv.ui.splitpanel.SplitPanel;
import bdv.viewer.ConverterSetups;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.GroupLocksPanel;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.grouping.GroupHandle;
import org.scijava.ui.behaviour.MouseAndKeyHandler;
import org.scijava.ui.behaviour.util.InputActionBindings;
import org.scijava.ui.behaviour.util.TriggerBehaviourBindings;

import bdv.cache.CacheControl;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerOptions;
import bdv.viewer.ViewerPanel;

/**
 * A {@link JFrame} containing a {@link ViewerPanel} and associated
 * {@link InputActionBindings}.
 *
 * @author Tobias Pietzsch
 */
public class ViewerFrameMamut extends ViewFrame
{
	private static final long serialVersionUID = 1L;

	private final ViewerPanel viewer;

	private final CardPanel cards;

	private final SplitPanel splitPanel;

	/**
	 * Creates a new {@link ViewerFrameMamut}.
	 *
	 * @param windowTitle
	 *            the window title to display.
	 * @param sources
	 *            the {@link SourceAndConverter sources} to display.
	 * @param setups
	 *            min/max and color settings of the sources.
	 * @param numTimepoints
	 *            number of available timepoints.
	 * @param cacheControl
	 *            handle to cache. This is used to control io timing.
	 * @param groupHandle
	 *            the group handle.
	 * @param optional
	 *            optional parameters. See {@link ViewerOptions#options()}.
	 */
	public ViewerFrameMamut(
			final String windowTitle,
			final List< SourceAndConverter< ? > > sources,
			final ConverterSetups setups,
			final int numTimepoints,
			final CacheControl cacheControl,
			final GroupHandle groupHandle,
			final ViewerOptions optional )
	{
		super( windowTitle );

		viewer = new ViewerPanel( sources, numTimepoints, cacheControl, optional );
		setups.listeners().add( s -> viewer.requestRepaint() );

		cards = new CardPanel();
		BdvDefaultCards.setup( cards, viewer, setups );
		splitPanel = new SplitPanel( viewer, cards );

		add( splitPanel, BorderLayout.CENTER );

		final GroupLocksPanel navigationLocksPanel = new GroupLocksPanel( groupHandle );
		settingsPanel.add( navigationLocksPanel );
		settingsPanel.add( Box.createHorizontalGlue() );

		pack();
		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				viewer.stop();
			}
		} );

		SwingUtilities.replaceUIActionMap( viewer, keybindings.getConcatenatedActionMap() );
		SwingUtilities.replaceUIInputMap( viewer, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keybindings.getConcatenatedInputMap() );

		final MouseAndKeyHandler mouseAndKeyHandler = new MouseAndKeyHandler();
		mouseAndKeyHandler.setInputMap( triggerbindings.getConcatenatedInputTriggerMap() );
		mouseAndKeyHandler.setBehaviourMap( triggerbindings.getConcatenatedBehaviourMap() );
		mouseAndKeyHandler.setKeypressManager( optional.values.getKeyPressedManager(), viewer.getDisplay() );
		viewer.getDisplay().addHandler( mouseAndKeyHandler );
	}

	public ViewerPanel getViewerPanel()
	{
		return viewer;
	}

	public CardPanel getCardPanel()
	{
		return cards;
	}

	public SplitPanel getSplitPanel()
	{
		return splitPanel;
	}

	public InputActionBindings getKeybindings()
	{
		return keybindings;
	}

	public TriggerBehaviourBindings getTriggerbindings()
	{
		return triggerbindings;
	}
}
