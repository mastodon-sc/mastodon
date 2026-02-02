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
package org.mastodon.views.bdv.overlay.shapes.ellipsoid.actions;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.model.FocusModel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.keymap.KeyConfigScopes;
import org.mastodon.undo.UndoPointMarker;
import org.mastodon.views.bdv.overlay.OverlayGraph;
import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.EllipsoidOverlayEdge;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.EllipsoidOverlayVertex;
import org.mastodon.views.bdv.overlay.util.JamaEigenvalueDecomposition;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;

import net.imglib2.util.LinAlgHelpers;

/**
 * Behaviours specific to ellipsoid shapes in the BigDataViewer overlay.
 *
 * @param <V>
 *            vertex type.
 * @param <E>
 *            edge type.
 *
 * @author Jean-Yves Tinevez
 * @author Tobias Pietzsch
 */
public class ResizeEllipsoidBehaviours< V extends EllipsoidOverlayVertex< V, E >, E extends EllipsoidOverlayEdge< E, V > >
{

	public static final String INCREASE_SPOT_RADIUS = "increase spot radius";

	public static final String INCREASE_SPOT_RADIUS_ALOT = "increase spot radius a lot";

	public static final String INCREASE_SPOT_RADIUS_ABIT = "increase spot radius a bit";

	public static final String DECREASE_SPOT_RADIUS = "decrease spot radius";

	public static final String DECREASE_SPOT_RADIUS_ALOT = "decrease spot radius a lot";

	public static final String DECREASE_SPOT_RADIUS_ABIT = "decrease spot radius a bit";

	static final String[] INCREASE_SPOT_RADIUS_KEYS = new String[] { "E" };

	static final String[] INCREASE_SPOT_RADIUS_ALOT_KEYS = new String[] { "shift E" };

	static final String[] INCREASE_SPOT_RADIUS_ABIT_KEYS = new String[] { "control E" };

	static final String[] DECREASE_SPOT_RADIUS_KEYS = new String[] { "Q" };

	static final String[] DECREASE_SPOT_RADIUS_ALOT_KEYS = new String[] { "shift Q" };

	static final String[] DECREASE_SPOT_RADIUS_ABIT_KEYS = new String[] { "control Q" };

	static double lastRadius = 5.;

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigScopes.MASTODON, KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( INCREASE_SPOT_RADIUS, INCREASE_SPOT_RADIUS_KEYS,
					"Increase radius of spot at mouse position." );
			descriptions.add( INCREASE_SPOT_RADIUS_ALOT, INCREASE_SPOT_RADIUS_ALOT_KEYS,
					"Increase radius of spot at mouse position (a lot)." );
			descriptions.add( INCREASE_SPOT_RADIUS_ABIT, INCREASE_SPOT_RADIUS_ABIT_KEYS,
					"Increase radius of spot at mouse position (a little)." );
			descriptions.add( DECREASE_SPOT_RADIUS, DECREASE_SPOT_RADIUS_KEYS,
					"Decrease radius of spot at mouse position." );
			descriptions.add( DECREASE_SPOT_RADIUS_ALOT, DECREASE_SPOT_RADIUS_ALOT_KEYS,
					"Decrease radius of spot at mouse position (a lot)." );
			descriptions.add( DECREASE_SPOT_RADIUS_ABIT, DECREASE_SPOT_RADIUS_ABIT_KEYS,
					"Decrease radius of spot at mouse position (a little)." );
		}
	}

	public static final boolean FOCUS_EDITED_SPOT = true;

	public static final boolean SELECT_ADDED_SPOT = true;

	public static final double POINT_SELECT_DISTANCE_TOLERANCE = 5.0;

	/** Ratio by which we change the radius upon change radius action. */
	private static final double NORMAL_RADIUS_CHANGE = 0.1;

	/** Ratio by which we change the radius upon change radius a bit action. */
	private static final double ABIT_RADIUS_CHANGE = 0.01;

	/** Ratio by which we change the radius upon change radius a lot action. */
	private static final double ALOT_RADIUS_CHANGE = 1.;

	private final ResizeSpotBehaviour increaseSpotRadiusBehaviour;

	private final ResizeSpotBehaviour increaseSpotRadiusBehaviourALot;

	private final ResizeSpotBehaviour increaseSpotRadiusBehaviourABit;

	private final ResizeSpotBehaviour decreaseSpotRadiusBehaviour;

	private final ResizeSpotBehaviour decreaseSpotRadiusBehaviourALot;

	private final ResizeSpotBehaviour decreaseSpotRadiusBehaviourABit;

	public static < V extends EllipsoidOverlayVertex< V, E >, E extends EllipsoidOverlayEdge< E, V > > void install(
			final Behaviours behaviours,
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E, ? > renderer,
			final FocusModel< V > focus,
			final UndoPointMarker undo,
			final double minRadius )
	{
		final ResizeEllipsoidBehaviours< V, E > eb = new ResizeEllipsoidBehaviours<>( overlayGraph, renderer, focus,
				undo, NORMAL_RADIUS_CHANGE, ABIT_RADIUS_CHANGE, ALOT_RADIUS_CHANGE, minRadius );

		behaviours.namedBehaviour( eb.increaseSpotRadiusBehaviour, INCREASE_SPOT_RADIUS_KEYS );
		behaviours.namedBehaviour( eb.increaseSpotRadiusBehaviourABit, INCREASE_SPOT_RADIUS_ABIT_KEYS );
		behaviours.namedBehaviour( eb.increaseSpotRadiusBehaviourALot, INCREASE_SPOT_RADIUS_ALOT_KEYS );
		behaviours.namedBehaviour( eb.decreaseSpotRadiusBehaviour, DECREASE_SPOT_RADIUS_KEYS );
		behaviours.namedBehaviour( eb.decreaseSpotRadiusBehaviourABit, DECREASE_SPOT_RADIUS_ABIT_KEYS );
		behaviours.namedBehaviour( eb.decreaseSpotRadiusBehaviourALot, DECREASE_SPOT_RADIUS_ALOT_KEYS );
	}

	private final OverlayGraph< V, E > overlayGraph;

	private final ReentrantReadWriteLock lock;

	private final OverlayGraphRenderer< V, E, ? > renderer;

	private final FocusModel< V > focus;

	private final UndoPointMarker undo;

	private ResizeEllipsoidBehaviours(
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E, ? > renderer,
			final FocusModel< V > focus,
			final UndoPointMarker undo,
			final double normalRadiusChange,
			final double aBitRadiusChange,
			final double aLotRadiusChange,
			final double minRadius )
	{
		this.overlayGraph = overlayGraph;
		this.lock = overlayGraph.getLock();
		this.renderer = renderer;
		this.focus = focus;
		this.undo = undo;

		increaseSpotRadiusBehaviour = new ResizeSpotBehaviour( INCREASE_SPOT_RADIUS, normalRadiusChange, minRadius );
		increaseSpotRadiusBehaviourALot = new ResizeSpotBehaviour( INCREASE_SPOT_RADIUS_ALOT, aLotRadiusChange, minRadius );
		increaseSpotRadiusBehaviourABit = new ResizeSpotBehaviour( INCREASE_SPOT_RADIUS_ABIT, aBitRadiusChange, minRadius );
		decreaseSpotRadiusBehaviour = new ResizeSpotBehaviour( DECREASE_SPOT_RADIUS, -normalRadiusChange / ( 1 + normalRadiusChange ), minRadius );
		decreaseSpotRadiusBehaviourALot = new ResizeSpotBehaviour( DECREASE_SPOT_RADIUS_ALOT, -aLotRadiusChange / ( 1 + aLotRadiusChange ), minRadius );
		decreaseSpotRadiusBehaviourABit = new ResizeSpotBehaviour( DECREASE_SPOT_RADIUS_ABIT, -aBitRadiusChange / ( 1 + aBitRadiusChange ), minRadius );
	}

	private class ResizeSpotBehaviour extends AbstractNamedBehaviour implements ClickBehaviour
	{

		private final double[][] mat;

		private final double factor;

		private final JamaEigenvalueDecomposition eig;

		private final double minRadius;

		public ResizeSpotBehaviour( final String name, final double factor, final double minRadius )
		{
			super( name );
			this.factor = factor;
			this.minRadius = minRadius;
			mat = new double[ 3 ][ 3 ];
			eig = new JamaEigenvalueDecomposition( 3 );
		}

		@Override
		public void click( final int x, final int y )
		{
			final V ref = overlayGraph.vertexRef();
			lock.readLock().lock();
			try
			{
				final V vertex = renderer.getVertexAt( x, y, POINT_SELECT_DISTANCE_TOLERANCE, ref );
				if ( vertex != null )
				{
					// Scale the covariance matrix.
					vertex.getCovariance( mat );
					LinAlgHelpers.scale( mat, 1 + factor, mat );

					// Check if the min radius is not too small.
					eig.decomposeSymmetric( mat );
					final double[] eigVals = eig.getRealEigenvalues();
					for ( final double eigVal : eigVals )
					{
						final double r = Math.sqrt( eigVal );
						if ( r < minRadius )
							return;
					}

					vertex.setCovariance( mat );
					lastRadius = Math.max( minRadius, Math.sqrt( vertex.getBoundingSphereRadiusSquared() ) );
					overlayGraph.notifyGraphChanged();
					undo.setUndoPoint();

					if ( FOCUS_EDITED_SPOT )
						focus.focusVertex( vertex );
				}
			}
			finally
			{
				lock.readLock().unlock();
				overlayGraph.releaseRef( ref );
			}
		}
	}
}
