/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.views.bdv;

import java.util.Map;

import org.jdom2.Element;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.mamut.views.MamutViewFactory;
import org.mastodon.ui.coloring.ColorBarOverlay.Position;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import bdv.tools.InitializeViewerState;
import bdv.viewer.AbstractViewerPanel;
import bdv.viewer.SynchronizedViewerState;
import bdv.viewer.ViewerPanel;
import bdv.viewer.ViewerState;
import bdv.viewer.state.XmlIoViewerState;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * Factory to create and display a BDV views.
 * <p>
 * The GUI state is specified as a map of strings to objects. The accepted key
 * and value types are:
 * <ul>
 * <li><code>'FramePosition'</code> &rarr; an <code>int[]</code> array of 4
 * elements: x, y, width and height.
 * <li><code>'LockGroupId'</code> &rarr; an integer that specifies the lock
 * group id.
 * <li><code>'SettingsPanelVisible'</code> &rarr; a boolean that specifies
 * whether the settings panel is visible on this view.
 * <li><code>'BdvState'</code> &rarr; a XML Element that specifies the BDV
 * window state. See {@link ViewerPanel#stateToXml()} and
 * {@link ViewerPanel#stateFromXml(org.jdom2.Element)} for more information.
 * <li><code>'BdvTransform'</code> &rarr; an {@link AffineTransform3D} that
 * specifies the view point.
 * <li><code>'NoColoring'</code> &rarr; a boolean; if <code>true</code>, the
 * feature or tag coloring will be ignored.
 * <li><code>'TagSet'</code> &rarr; a string specifying the name of the tag-set
 * to use for coloring. If not <code>null</code>, the coloring will be done
 * using the tag-set.
 * <li><code>'FeatureColorMode'</code> &rarr; a String specifying the name of
 * the feature color mode to use for coloring. If not <code>null</code>, the
 * coloring will be done using the feature color mode.
 * <li><code>'ColorbarVisible'</code> &rarr; a boolean specifying whether the
 * colorbar is visible for tag-set and feature-based coloring.
 * <li><code>'ColorbarPosition'</code> &rarr; a {@link Position} specifying the
 * position of the colorbar.
 * </ul>
 */
@Plugin( type = MamutViewFactory.class, priority = Priority.NORMAL )
public class MamutViewBdvFactory extends AbstractMamutViewFactory< MamutViewBdv >
{

	public static final String NEW_BDV_VIEW = "new bdv view";

	static final String[] NEW_BDV_VIEW_KEYS = new String[] { "not mapped" };

	/**
	 * Key for the {@link ViewerState} in a BDV view. Value is a XML
	 * {@link Element} serialized from the state.
	 */
	public static final String BDV_STATE_KEY = "BdvState";

	/**
	 * Key for the transform in a BDV view. Value is an
	 * {@link AffineTransform3D} instance.
	 */
	public static final String BDV_TRANSFORM_KEY = "BdvTransform";

	@Override
	public MamutViewBdv create( final ProjectModel projectModel )
	{
		return new MamutViewBdv( projectModel );
	}

	@Override
	public Map< String, Object > getGuiState( final MamutViewBdv view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		getBdvGuiState( view.getViewerPanelMamut(), guiState );
		return guiState;
	}

	@Override
	public void restoreGuiState( final MamutViewBdv view, final Map< String, Object > guiState )
	{
		super.restoreGuiState( view, guiState );
		restoreBdvGuiState( view.getViewerPanelMamut(), guiState );
	}

	public static void getBdvGuiState( final AbstractViewerPanel viewerPanel, final Map< String, Object > guiState )
	{
		// Viewer state.
		@SuppressWarnings( "deprecation" )
		final bdv.viewer.state.ViewerState deprecatedState = new bdv.viewer.state.ViewerState( ( SynchronizedViewerState ) viewerPanel.state() );
		final Element stateEl = new XmlIoViewerState().toXml( deprecatedState );
		guiState.put( BDV_STATE_KEY, stateEl );
		// Transform.
		final AffineTransform3D t = new AffineTransform3D();
		viewerPanel.state().getViewerTransform( t );
		guiState.put( BDV_TRANSFORM_KEY, t );
	}

	public static void restoreBdvGuiState( final AbstractViewerPanel viewerPanel, final Map< String, Object > guiState )
	{

		// Restore BDV state.
		final Element stateEl = ( Element ) guiState.get( BDV_STATE_KEY );
		if ( null != stateEl )
		{
			final XmlIoViewerState io = new XmlIoViewerState();
			@SuppressWarnings( "deprecation" )
			final bdv.viewer.state.ViewerState deprecatedState = new bdv.viewer.state.ViewerState( ( SynchronizedViewerState ) viewerPanel.state() );
			io.restoreFromXml( stateEl.getChild( io.getTagName() ), deprecatedState );
		}
		// Restore transform.
		final AffineTransform3D tLoaded = ( AffineTransform3D ) guiState.get( BDV_TRANSFORM_KEY );
		if ( null == tLoaded )
			InitializeViewerState.initTransform( viewerPanel );
		else
			new Thread( () -> {
				try
				{
					/*
					 * If we don't wait a little bit, the BDV state, notably the
					 * transform, is not restored properly. We put this in a
					 * separate thread not to block the loading if we have many
					 * BDV views.
					 */
					Thread.sleep( 100 );
					viewerPanel.state().setViewerTransform( tLoaded );
				}
				catch ( final InterruptedException e )
				{
					e.printStackTrace();
				}
			} ).start();
	}

	@Override
	public String getCommandName()
	{
		return NEW_BDV_VIEW;
	}

	@Override
	public String[] getCommandKeys()
	{
		return NEW_BDV_VIEW_KEYS;
	}

	@Override
	public String getCommandDescription()
	{
		return "Open a new BigDataViewer view.";
	}

	@Override
	public String getCommandMenuText()
	{
		return "New Bdv";
	}

	@Override
	public Class< MamutViewBdv > getViewClass()
	{
		return MamutViewBdv.class;
	}
}
