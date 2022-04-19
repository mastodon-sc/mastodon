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
package org.mastodon.mamut;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.NavigationHandlerAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.adapter.TimepointModelAdapter;
import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.ViewFrame;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.branch.BranchGraphFocusAdapter;
import org.mastodon.model.branch.BranchGraphHighlightAdapter;
import org.mastodon.model.branch.BranchGraphNavigationHandlerAdapter;
import org.mastodon.model.branch.BranchGraphSelectionAdapter;
import org.mastodon.ui.keymap.Keymap;
import org.mastodon.ui.keymap.Keymap.UpdateListener;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.WrappedActionMap;
import org.scijava.ui.behaviour.util.WrappedInputMap;

public class MamutBranchView< 
	VG extends ViewGraph< BranchSpot, BranchLink, V, E >, 
	V extends Vertex< E >, 
	E extends Edge< V > >
{

	protected final MamutAppModel appModel;

	protected ViewFrame frame;

	protected final String[] keyConfigContexts;

	protected Actions viewActions;

	protected Behaviours viewBehaviours;

	protected final VG viewGraph;

	protected final GroupHandle groupHandle;

	protected final TimepointModelAdapter timepointModel;

	protected final HighlightModelAdapter< BranchSpot, BranchLink, V, E > highlightModel;

	protected final FocusModelAdapter< BranchSpot, BranchLink, V, E > focusModel;

	protected final SelectionModelAdapter< BranchSpot, BranchLink, V, E > selectionModel;

	protected final NavigationHandlerAdapter< BranchSpot, BranchLink, V, E > navigationHandler;

	protected final ArrayList< Runnable > runOnClose;

	protected final RefBimap< BranchSpot, V > vertexMap;

	protected final RefBimap< BranchLink, E > edgeMap;


	public MamutBranchView( final MamutAppModel appModel, final VG viewGraph, final String[] keyConfigContexts )
	{
		this.appModel = appModel;
		this.viewGraph = viewGraph;
		this.keyConfigContexts = keyConfigContexts;

		// Maps.
		this.vertexMap = viewGraph.getVertexMap();
		this.edgeMap = viewGraph.getEdgeMap();

		// Granp handle.
		this.groupHandle = appModel.getGroupManager().createGroupHandle();

		// Graphs.
		final ModelBranchGraph branchGraph = appModel.getModel().getBranchGraph();
		final ModelGraph graph = appModel.getModel().getGraph();

		// Highlight.
		final HighlightModel< Spot, Link > graphHighlightModel = appModel.getHighlightModel();
		final HighlightModel< BranchSpot, BranchLink > branchHighlightModel =
				new BranchGraphHighlightAdapter<>( branchGraph, graph, graphHighlightModel );
		this.highlightModel = new HighlightModelAdapter<>( branchHighlightModel, vertexMap, edgeMap );

		// Focus
		final FocusModel< Spot, Link > graphFocusModel = appModel.getFocusModel();
		final FocusModel< BranchSpot, BranchLink > branchFocusfocusModel =
				new BranchGraphFocusAdapter<>( branchGraph, graph, graphFocusModel );
		this.focusModel = new FocusModelAdapter<>( branchFocusfocusModel, vertexMap, edgeMap );

		// Selection
		final SelectionModel< Spot, Link > graphSelectionModel = appModel.getSelectionModel();
		final SelectionModel< BranchSpot, BranchLink > branchSelectionModel =
				new BranchGraphSelectionAdapter<>( branchGraph, graph, graphSelectionModel );
		selectionModel = new SelectionModelAdapter<>( branchSelectionModel, vertexMap, edgeMap );

		// Navigation.
		final NavigationHandler< Spot, Link > graphNavigationHandler = groupHandle.getModel( appModel.NAVIGATION );
		final NavigationHandler< BranchSpot, BranchLink > branchGraphNavigation =
				new BranchGraphNavigationHandlerAdapter<>( branchGraph, graph, graphNavigationHandler );
		this.navigationHandler = new NavigationHandlerAdapter<>( branchGraphNavigation, vertexMap, edgeMap );

		// Time-point.
		this.timepointModel = new TimepointModelAdapter( groupHandle.getModel( appModel.TIMEPOINT ) );

		// Closing runnables.
		this.runOnClose = new ArrayList<>();
		runOnClose.add( () -> {
			timepointModel.listeners().removeAll();
			highlightModel.listeners().removeAll();
			focusModel.listeners().removeAll();
			selectionModel.listeners().removeAll();
			navigationHandler.listeners().removeAll();
		} );
	}

	protected void setFrame( final ViewFrame frame )
	{
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				close();
			}
		} );
		this.frame = frame;

		final Actions globalActions = appModel.getGlobalActions();
		if ( globalActions != null )
		{
			frame.getKeybindings().addActionMap( "global", new WrappedActionMap( globalActions.getActionMap() ) );
			frame.getKeybindings().addInputMap( "global", new WrappedInputMap( globalActions.getInputMap() ) );
		}

		final Actions pluginActions = appModel.getPlugins().getPluginActions();
		if ( pluginActions != null )
		{
			frame.getKeybindings().addActionMap( "plugin", new WrappedActionMap( pluginActions.getActionMap() ) );
			frame.getKeybindings().addInputMap( "plugin", new WrappedInputMap( pluginActions.getInputMap() ) );
		}

		final Actions appActions = appModel.getAppActions();
		frame.getKeybindings().addActionMap( "app", new WrappedActionMap( appActions.getActionMap() ) );
		frame.getKeybindings().addInputMap( "app", new WrappedInputMap( appActions.getInputMap() ) );

		final Keymap keymap = appModel.getKeymap();

		viewActions = new Actions( keymap.getConfig(), keyConfigContexts );
		viewActions.install( frame.getKeybindings(), "view" );

		viewBehaviours = new Behaviours( keymap.getConfig(), keyConfigContexts );
		viewBehaviours.install( frame.getTriggerbindings(), "view" );

		final UpdateListener updateListener = () -> {
			viewBehaviours.updateKeyConfig( keymap.getConfig() );
			viewActions.updateKeyConfig( keymap.getConfig() );
		};
		keymap.updateListeners().add( updateListener );
		onClose( () -> keymap.updateListeners().remove( updateListener ) );
	}

	/**
	 * Adds the specified {@link Runnable} to the list of runnables to execute
	 * when this view is closed.
	 *
	 * @param runnable
	 *            the {@link Runnable} to add.
	 */
	public synchronized void onClose( final Runnable runnable )
	{
		runOnClose.add( runnable );
	}

	protected synchronized void close()
	{
		runOnClose.forEach( Runnable::run );
		runOnClose.clear();
	}
}
