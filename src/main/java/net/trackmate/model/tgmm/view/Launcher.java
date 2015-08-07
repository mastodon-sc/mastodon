package net.trackmate.model.tgmm.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;
import net.imglib2.ui.TransformListener;
import net.trackmate.bdv.wrapper.OverlayGraphWrapper;
import net.trackmate.bdv.wrapper.VertexLocalizer;
import net.trackmate.model.Link;
import net.trackmate.model.ModelGraph;
import net.trackmate.model.tgmm.RawIO;
import net.trackmate.model.tgmm.SpotCovariance;
import net.trackmate.model.tgmm.TgmmModel;
import net.trackmate.model.tgmm.view.DisplaySettingsPanel.SpotOverlayStyle;
import net.trackmate.trackscheme.SelectionListener;
import net.trackmate.trackscheme.SelectionModel;
import net.trackmate.trackscheme.ShowTrackScheme;
import net.trackmate.trackscheme.TrackSchemeEdge;
import net.trackmate.trackscheme.TrackSchemeGraph;
import net.trackmate.trackscheme.TrackSchemeUtil;
import net.trackmate.trackscheme.TrackSchemeVertex;
import net.trackmate.trackscheme.laf.TrackSchemeStyle;
import bdv.BigDataViewer;
import bdv.export.ProgressWriterConsole;
import bdv.viewer.InputActionBindings;
import bdv.viewer.ViewerPanel;
import bdv.viewer.animate.TranslationAnimator;
import bdv.viewer.state.ViewerState;

public class Launcher
{
	public static final boolean DEFAULT_USE_TRACKSCHEME_CONTEXT = false;

	protected static final double CLICK_TOLERANCE = 10d;

	public static void main( final String[] args ) throws IOException, SpimDataException
	{
		/*
		 * Settings.
		 */

		final String bdvFile = "/Volumes/Data/BDV_MVD_5v_final.xml";
//		final String modelFile = "/Volumes/Data/model.raw";
		final int timepointIndex = 10;
//		final String bdvFile = "D:/Users/Jean-Yves/Development/Data/drosophila.xml";
		final String modelFile = "";
//		final int timepointIndex = 1;

		/*
		 * Load BDV.
		 */

		System.out.println( "Launching viewer." );
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		final BigDataViewer bdv = BigDataViewer.open( bdvFile, new File( bdvFile ).getName(), new ProgressWriterConsole() );
		final ViewerPanel viewer = bdv.getViewer();
		viewer.setTimepoint( timepointIndex );
		System.out.println( "Done." );

		/*
		 * Load model.
		 */

		System.out.println( "Loading/Instantiating TGMM model." );
		final TgmmModel model;
		if ( null != modelFile && modelFile.length() > 0 )
		{
			model = RawIO.read( new File( modelFile ) );
		}
		else
		{
			model = new TgmmModel();
		}
		System.out.println( "Done." );

		/*
		 * Build TrackScheme.
		 */

		final ModelGraph< SpotCovariance > graph = model.getGraph();
		final TrackSchemeGraph tsg = TrackSchemeUtil.buildTrackSchemeGraph( graph, graph.getIdBimap() );
		final ShowTrackScheme trackscheme = new ShowTrackScheme( tsg );
		final OverlayGraphWrapper< SpotCovariance, Link< SpotCovariance > > overlayGraph =
				new OverlayGraphWrapper< SpotCovariance, Link< SpotCovariance > >(
						tsg,
						graph,
						graph.getIdBimap(),
						new VertexLocalizer.DefaultVertexLocalizer< SpotCovariance >() );
		overlayGraph.HACK_updateTimepointSets();

		final TracksOverlaySpotCovariance tracksOverlay = new TracksOverlaySpotCovariance(
				overlayGraph,
				bdv.getViewer(),
				model.timepoints().size() );

		viewer.getDisplay().addOverlayRenderer( tracksOverlay );
		viewer.addRenderTransformListener( tracksOverlay );
		final ContextTransformListener tl = setupContextTrackscheme( bdv, overlayGraph, trackscheme );
		tl.setEnabled( DEFAULT_USE_TRACKSCHEME_CONTEXT );

		/*
		 * Center views on single selected vertex.
		 */
		
		trackscheme.getSelectionHandler().addSelectionListener( new SelectionListener()
		{
			private final SelectionModel< TrackSchemeVertex, TrackSchemeEdge > selectionModel = trackscheme.getSelectionHandler().getSelectionModel();

			final SpotCovariance spot = graph.vertexRef();

			@Override
			public void selectionChanged()
			{
				if ( selectionModel.getSelectedVertices().size() == 1 )
				{
					final TrackSchemeVertex v = selectionModel.getSelectedVertices().iterator().next();
					graph.getIdBimap().getVertex( v.getModelVertexId(), spot );
					centerViewOn( spot, bdv.getViewer() );

					if ( !tl.isEnabled() )
					{
						/*
						 * TrackScheme context is not one, so we center
						 * TrackScheme on vertex selection.
						 */
						trackscheme.centerOn( v );
					}
				}
				viewer.repaint();
			}
		} );
		
		/*
		 * Catch mouse events on BDV.
		 */

		final ModelEditHandler meh = new ModelEditHandler( model, overlayGraph, viewer, trackscheme );
		viewer.getDisplay().addHandler( meh );
		bdv.getViewerFrame().getKeybindings().addActionMap( "editModel", meh.getActionMap() );
		bdv.getViewerFrame().getKeybindings().addInputMap( "editModel", meh.getDefaultInputMap() );

		/*
		 * Display config panel.
		 */

		final JFrame configFrame = new JFrame( "Display settings" );
		configFrame.setSize( 600, 400 );
		final DisplaySettingsPanel configPanel = new DisplaySettingsPanel();
		configPanel.addActionListener( new DisplaySettingsListener( configPanel, tracksOverlay, bdv, trackscheme, tl ) );
		configFrame.getContentPane().add( configPanel );
		configFrame.setVisible( true );
	}

	private static final void centerViewOn( final SpotCovariance spot, final ViewerPanel viewer )
	{
		final ViewerState state = viewer.getState();
		final InteractiveDisplayCanvasComponent< AffineTransform3D > display = viewer.getDisplay();

		final int tp = spot.getTimepointId();
		viewer.setTimepoint( tp );

		final AffineTransform3D t = new AffineTransform3D();
		state.getViewerTransform( t );

		final double[] spotCoords = new double[ 3 ];
		spot.localize( spotCoords );

		// Translate view so that the target spot is in the middle of the
		// display
		final double dx = display.getWidth() / 2 - ( t.get( 0, 0 ) * spotCoords[ 0 ] + t.get( 0, 1 ) * spotCoords[ 1 ] + t.get( 0, 2 ) * spotCoords[ 2 ] );
		final double dy = display.getHeight() / 2 - ( t.get( 1, 0 ) * spotCoords[ 0 ] + t.get( 1, 1 ) * spotCoords[ 1 ] + t.get( 1, 2 ) * spotCoords[ 2 ] );
		final double dz = -( t.get( 2, 0 ) * spotCoords[ 0 ] + t.get( 2, 1 ) * spotCoords[ 1 ] + t.get( 2, 2 ) * spotCoords[ 2 ] );

		// But use an animator to do this smoothly.
		final double[] target = new double[] { dx, dy, dz };
		viewer.setTransformAnimator( new TranslationAnimator( t, target, 300 ) );
	}

	private static ContextTransformListener setupContextTrackscheme(
			final BigDataViewer bdv,
			final OverlayGraphWrapper< SpotCovariance, Link< SpotCovariance > > overlayGraph,
			final ShowTrackScheme trackscheme )
	{
		final ContextTrackScheme< ?, ? > context = ContextTrackScheme.create( overlayGraph, trackscheme );

		final String REFRESH_CONTEXT_TRACKSCHEME = "refresh context trackscheme";
		final InputMap inputMap = new InputMap();
		inputMap.put( KeyStroke.getKeyStroke( "R" ), REFRESH_CONTEXT_TRACKSCHEME );
		final ActionMap actionMap = new ActionMap();
		actionMap.put( REFRESH_CONTEXT_TRACKSCHEME, new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				System.out.println( REFRESH_CONTEXT_TRACKSCHEME + ": Do nothing." );
			}
		} );
		final InputActionBindings bindings = bdv.getViewerFrame().getKeybindings();
		bindings.addActionMap( "trackscheme", actionMap );
		bindings.addInputMap( "trackscheme", inputMap );

		final ContextTransformListener tl = new ContextTransformListener( context, bdv );
		bdv.getViewer().addRenderTransformListener( tl );
		return tl;
	}

	/**
	 * Listen to changed that affect TrackScheme, interpret it, and forward it
	 * to TrackScheme.
	 */
	private static final class ContextTransformListener implements TransformListener< AffineTransform3D >
	{
		private final ContextTrackScheme< ?, ? > context;

		private final BigDataViewer bdv;

		private boolean enabled;

		public ContextTransformListener( final ContextTrackScheme< ?, ? > context, final BigDataViewer bdv )
		{
			this.context = context;
			this.bdv = bdv;
		}

		public boolean isEnabled()
		{
			return this.enabled;
		}

		public void setEnabled( final boolean enabled )
		{
			this.enabled = enabled;
		}

		public void setContextWindow( final int contextWindow )
		{
			context.setContextWindow( contextWindow );
		}

		public void setFocusRange( final double focusRange )
		{
			context.setFocusRange( focusRange );
		}

		public void setUseCrop( final boolean useCrop )
		{
			context.setUseCrop( useCrop );
		}

		public void setTrackSchemeStyle( final TrackSchemeStyle selectedTrackSchemeStyle )
		{
			context.setTrackSchemeStyle( selectedTrackSchemeStyle );
		}

		public void setAutoscale( final boolean autoscale )
		{
			context.setAutoscale( autoscale );
		}

		@Override
		public void transformChanged( final AffineTransform3D transform )
		{
			if ( !enabled )
				return;

			final ViewerState state = bdv.getViewer().getState();
			final int timepoint = state.getCurrentTimepoint();
			final AffineTransform3D viewerTransform = new AffineTransform3D();
			state.getViewerTransform( viewerTransform );
			final int width = bdv.getViewer().getWidth();
			final int height = bdv.getViewer().getHeight();
			context.buildContext( timepoint, viewerTransform, width, height );
		}
	}

	/**
	 * Listen to display settings changes and forward it to adequate target.
	 */
	private static final class DisplaySettingsListener implements ActionListener
	{
		private final DisplaySettingsPanel configPanel;

		private final TracksOverlaySpotCovariance tracksOverlay;

		private final BigDataViewer bdv;

		private final ContextTransformListener tl;

		private final ShowTrackScheme trackscheme;

		public DisplaySettingsListener( final DisplaySettingsPanel configPanel, final TracksOverlaySpotCovariance tracksOverlay, final BigDataViewer bdv, final ShowTrackScheme trackscheme, final ContextTransformListener tl )
		{
			this.configPanel = configPanel;
			this.tracksOverlay = tracksOverlay;
			this.bdv = bdv;
			this.trackscheme = trackscheme;
			this.tl = tl;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			new Thread( "Display settings updater thread." )
			{
				@Override
				public void run()
				{
					if ( e == configPanel.antialiasingOn )
					{
						tracksOverlay.setAntialising( true );
					}
					else if ( e == configPanel.antialiasingOff )
					{
						tracksOverlay.setAntialising( false );
					}
					else if ( e == configPanel.gradientOn )
					{
						tracksOverlay.setUseGradient( true );
					}
					else if ( e == configPanel.gradientOff )
					{
						tracksOverlay.setUseGradient( false );
					}
					else if ( e == configPanel.limitFocusRangeOn || e == configPanel.focusRangeChanged )
					{
						tracksOverlay.setFocusRange( configPanel.getFocusRange() );
						tl.setUseCrop( true );
						tl.setFocusRange( configPanel.getFocusRange() );
						tl.transformChanged( null );
					}
					else if ( e == configPanel.limitFocusRangeOff )
					{
						tracksOverlay.setFocusRange( Double.POSITIVE_INFINITY );
						tl.setUseCrop( false );
						tl.transformChanged( null );
					}
					else if ( e == configPanel.limitTimeRangeOn || e == configPanel.timeRangeChanged )
					{
						tracksOverlay.setTimeRange( configPanel.getTimeRange() );
					}
					else if ( e == configPanel.limitTimeRangeOff )
					{
						tracksOverlay.setTimeRange( Double.POSITIVE_INFINITY );
					}
					else if ( e == configPanel.drawSpotsOn )
					{
						tracksOverlay.setDrawSpots( true );
					}
					else if ( e == configPanel.drawSpotsOff )
					{
						tracksOverlay.setDrawSpots( false );
					}
					else if ( e == configPanel.drawLinksOn )
					{
						tracksOverlay.setDrawLinks( true );
					}
					else if ( e == configPanel.drawLinksOff )
					{
						tracksOverlay.setDrawLinks( false );
					}
					else if ( e == configPanel.spotStyleChanged )
					{
						tracksOverlay.setDrawSpotEllipse( configPanel.getSelectedSpotOverlayStyle() == SpotOverlayStyle.ELLIPSE );
					}
					else if ( e == configPanel.trackschemeContextOn )
					{
						tl.setEnabled( true );
						tl.transformChanged( null );
					}
					else if ( e == configPanel.trackschemeContextOff )
					{
						tl.setEnabled( false );
						trackscheme.relayout();
					}
					else if ( e == configPanel.trackschemeAutoscaleContextOn )
					{
						tl.setAutoscale( true );
						tl.transformChanged( null );
					}
					else if ( e == configPanel.trackschemeAutoscaleContextOff )
					{
						tl.setAutoscale( false );
					}
					else if ( e == configPanel.contextWindowChanged )
					{
						tl.setContextWindow( configPanel.getContextWindow() );
						tl.transformChanged( null );
					}
					else if ( e == configPanel.trackschemeStyleChanged )
					{
						tl.setTrackSchemeStyle( configPanel.getSelectedTrackSchemeStyle() );
						trackscheme.repaint();
					}

					bdv.getViewer().getDisplay().repaint();
				};
			}.start();
		}
	}
}
