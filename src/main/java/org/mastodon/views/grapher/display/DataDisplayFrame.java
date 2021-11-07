/*-
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
package org.mastodon.views.grapher.display;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.GroupLocksPanel;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.TimepointModel;
import org.mastodon.ui.context.ContextChooserPanel;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.views.context.ContextChooser;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.scijava.ui.behaviour.MouseAndKeyHandler;

public class DataDisplayFrame extends ViewFrame
{
	private static final long serialVersionUID = 1L;

	private final DataDisplayPanel dataDisplayPanel;

	public DataDisplayFrame(
			final DataGraph< ?, ? > graph,
			final HighlightModel< DataVertex, DataEdge > highlight,
			final FocusModel< DataVertex, DataEdge > focus,
			final TimepointModel timepoint,
			final SelectionModel< DataVertex, DataEdge > selection,
			final NavigationHandler< DataVertex, DataEdge > navigation,
			final UndoPointMarker undoPointMarker,
			final GroupHandle groupHandle,
			final ContextChooser< ? > contextChooser,
			final DataDisplayOptions optional )
	{
		super( "Data" );

		dataDisplayPanel = new DataDisplayPanel(
				graph,
				highlight,
				focus,
				timepoint,
				selection,
				navigation,
				optional );
		add( dataDisplayPanel, BorderLayout.CENTER );

		final GroupLocksPanel navigationLocksPanel = new GroupLocksPanel( groupHandle );
		settingsPanel.add( navigationLocksPanel );
		settingsPanel.add( Box.createHorizontalGlue() );

		final ContextChooserPanel< ? > contextChooserPanel = new ContextChooserPanel<>( contextChooser );
		settingsPanel.add( contextChooserPanel );

		pack();
		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				dataDisplayPanel.stop();
			}
		} );

		SwingUtilities.replaceUIActionMap( dataDisplayPanel, keybindings.getConcatenatedActionMap() );
		SwingUtilities.replaceUIInputMap( dataDisplayPanel, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, keybindings.getConcatenatedInputMap() );

		final MouseAndKeyHandler mouseAndKeyHandler = new MouseAndKeyHandler();
		mouseAndKeyHandler.setInputMap( triggerbindings.getConcatenatedInputTriggerMap() );
		mouseAndKeyHandler.setBehaviourMap( triggerbindings.getConcatenatedBehaviourMap() );
		mouseAndKeyHandler.setKeypressManager( optional.values.getKeyPressedManager(), dataDisplayPanel.getDisplay() );
		dataDisplayPanel.getDisplay().addHandler( mouseAndKeyHandler );
		setLocation( optional.values.getX(), optional.values.getY() );
	}

	public DataDisplayPanel getDataDisplayPanel()
	{
		return dataDisplayPanel;
	}
}
