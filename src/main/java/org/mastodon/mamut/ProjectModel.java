/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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

import java.util.function.Consumer;

import org.mastodon.app.MastodonAppModel;
import org.mastodon.app.plugin.MastodonAppPluginModel;
import org.mastodon.app.plugin.PluginUtils;
import org.mastodon.mamut.io.ProjectActions;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.BoundingSphereRadiusStatistics;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchGraphSynchronizer;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.plugin.MamutPlugins;
import org.mastodon.ui.SelectionActions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.Keymap;
import org.mastodon.ui.keymap.KeymapManager;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.scijava.listeners.Listeners;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.util.Actions;

import bdv.viewer.animate.MessageOverlayAnimator;

/**
 * Data class that stores the data model and the application model of the Mamut
 * application.
 *
 * @author Jean-Yves Tinevez
 */
public class ProjectModel extends MastodonAppModel< Model, Spot, Link > implements MastodonAppPluginModel
{
	private static final int NUM_GROUPS = 3;

	private final BoundingSphereRadiusStatistics radiusStats;

	private final SharedBigDataViewerData sharedBdvData;

	private final int minTimepoint;

	private final int maxTimepoint;

	private final BranchGraphSynchronizer branchGraphSync;

	private final Listeners.List< CloseListener > closeListeners = new Listeners.List<>();

	private final WindowManager windowManager;

	private final Context context;

	private final MamutProject project;

	public ProjectModel(
			final Context context,
			final Model model,
			final SharedBigDataViewerData sharedBdvData,
			final KeyPressedManager keyPressedManager,
			final KeymapManager keymapManager,
			final MamutProject project )
	{
		super(
				NUM_GROUPS,
				model,
				keyPressedManager,
				keymapManager,
				new MamutPlugins( keymapManager.getForwardDefaultKeymap() ),
				new Actions( keymapManager.getForwardDefaultKeymap().getConfig(), KeyConfigContexts.MASTODON ),
				new String[] { KeyConfigContexts.MASTODON } );

		this.context = context;
		this.project = project;
		this.radiusStats = new BoundingSphereRadiusStatistics( model );
		this.sharedBdvData = sharedBdvData;
		this.minTimepoint = 0;
		this.maxTimepoint = sharedBdvData.getNumTimepoints() - 1;

		final Keymap keymap = keymapManager.getForwardDefaultKeymap();
		keymap.updateListeners().add( () -> {
			getProjectActions().updateKeyConfig( keymap.getConfig() );
			getModelActions().updateKeyConfig( keymap.getConfig() );
		} );

		// Register save / export actions
		ProjectActions.installAppActions( getModelActions(), this, null );

		this.branchGraphSync = new BranchGraphSynchronizer( model.getBranchGraph(), model.getGraph().getLock().readLock() );
		model.getGraph().addGraphChangeListener( branchGraphSync );
		/*
		 * TODO: (?) For now, we use timepoint indices in MaMuT model, instead
		 * of IDs/names. This is because BDV also displays timepoint index, and
		 * it would be confusing to have different labels in TrackScheme. If
		 * this is changed in the future, then probably only in the model files.
		 */

		// WindowManager.
		this.windowManager = new WindowManager( this );

		// Update sharedBdvData
		sharedBdvData.getOptions()
				.shareKeyPressedEvents( keyPressedManager )
				.msgOverlay( new MessageOverlayAnimator( 1500, 0.005, 0.02 ) );

		// Plugins.
		discoverPlugins();

		// Install common actions.
		UndoActions.install( getModelActions(), model );
		SelectionActions.install( getModelActions(), model.getGraph(), model.getGraph().getLock(), model.getGraph(), getSelectionModel(), model );
		MamutActions.install( getModelActions(), this );
	}

	public WindowManager getWindowManager()
	{
		return windowManager;
	}

	public Context getContext()
	{
		return context;
	}

	public BoundingSphereRadiusStatistics getRadiusStats()
	{
		return radiusStats;
	}

	public SharedBigDataViewerData getSharedBdvData()
	{
		return sharedBdvData;
	}

	public int getMinTimepoint()
	{
		return minTimepoint;
	}

	public int getMaxTimepoint()
	{
		return maxTimepoint;
	}

	public BranchGraphSynchronizer getBranchGraphSync()
	{
		return branchGraphSync;
	}

	public void close()
	{
		closeListeners.list.forEach( CloseListener::close );
		windowManager.closeAllWindows();
	}

	/**
	 * Listeners that are notified when the Mastodon project is closed.
	 */
	public Listeners< CloseListener > projectClosedListeners()
	{
		return closeListeners;
	}

	private void discoverPlugins()
	{
		if ( context == null )
			return;

		final MamutPlugins plugins = ( MamutPlugins ) getPlugins();
		final Consumer< MamutPlugin > registerAction = ( mp ) -> {
			mp.setAppPluginModel( this );
			plugins.register( mp );
		};
		PluginUtils.forEachDiscoveredPlugin( MamutPlugin.class, registerAction, context );
	}

	public MamutProject getProject()
	{
		return project;
	}
}
