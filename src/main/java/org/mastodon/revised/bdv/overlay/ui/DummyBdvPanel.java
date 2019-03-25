package org.mastodon.revised.bdv.overlay.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mastodon.adapter.FocusModelAdapter;
import org.mastodon.adapter.HighlightModelAdapter;
import org.mastodon.adapter.RefBimap;
import org.mastodon.adapter.SelectionModelAdapter;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.model.DefaultFocusModel;
import org.mastodon.model.DefaultHighlightModel;
import org.mastodon.model.DefaultSelectionModel;
import org.mastodon.model.FocusModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProject.ProjectReader;
import org.mastodon.revised.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.revised.bdv.overlay.RenderSettings;
import org.mastodon.revised.bdv.overlay.wrap.OverlayEdgeWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayGraphWrapper;
import org.mastodon.revised.bdv.overlay.wrap.OverlayVertexWrapper;
import org.mastodon.revised.model.mamut.BoundingSphereRadiusStatistics;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.ModelOverlayProperties;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.ui.coloring.DefaultGraphColorGenerator;
import org.mastodon.revised.ui.coloring.GraphColorGenerator;
import org.mastodon.revised.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.spatial.SpatioTemporalIndex;

import bdv.BehaviourTransformEventHandler3D.BehaviourTransformEventHandler3DFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.InteractiveDisplayCanvasComponent;

/**
 * Panel that displays a dummy model, non-interactive, for the single purpose of
 * showing how a {@link RenderSettings} is displayed on real data.
 * 
 * @author Jean-Yves Tinevez
 */
public class DummyBdvPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 150;

	private static final int HEIGHT = 400;

	private final OverlayGraphRenderer< OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > renderer;

	public DummyBdvPanel()
	{
		super( new BorderLayout() );

		/*
		 * Load model.
		 */

		final File modelFile = new File( DummyBdvPanel.class.getResource( "dummy" ).getFile() );
		final Model model = new Model();
		try
		{
			final ProjectReader reader = new MamutProject( modelFile ).openForReading();
			model.loadRaw( reader );
		}
		catch ( final IOException e1 )
		{
			e1.printStackTrace();
		}
		final double dummyScale = 5.7;
		final double[] pos = new double[ 3 ];
		final double[][] cov = new double[ 3 ][ 3 ];
		for ( final Spot spot : model.getGraph().vertices() )
		{
			// Scale spot position so that they are in sync with the image.
			spot.localize( pos );
			spot.getCovariance( cov );
			for ( int d = 0; d < pos.length; d++ )
			{
				pos[ d ] *= dummyScale;
				for ( int d2 = 0; d2 < cov[ d ].length; d2++ )
					cov[ d ][ d2 ] *= dummyScale * dummyScale;

			}
			pos[ 0 ] -= 113 * dummyScale;
			pos[ 1 ] += 2 * dummyScale;
			spot.setPosition( pos );
			spot.setCovariance( cov );
		}

		/*
		 * Core model.
		 */

		final ModelGraph graph = model.getGraph();
		final GraphIdBimap< Spot, Link > idmap = model.getGraphIdBimap();
		final SpatioTemporalIndex< Spot > spatioTemporalIndex = model.getSpatioTemporalIndex();
		final SelectionModel< Spot, Link > selection = new DefaultSelectionModel<>( graph, idmap );
		final FocusModel< Spot, Link > focus = new DefaultFocusModel<>( idmap );
		final DefaultHighlightModel< Spot, Link > highlight = new DefaultHighlightModel<>( idmap );
		final GraphColorGenerator< Spot, Link > coloring = new DefaultGraphColorGenerator<>();
		final BoundingSphereRadiusStatistics radiusStats = new BoundingSphereRadiusStatistics( model );

		/*
		 * Set selection.
		 */

		// Find one particular spot.
		for ( final Spot spot : graph.vertices() )
		{
			if ( spot.getLabel().equals( "291" ) )
			{
				selection.setSelected( spot, true );
				break;
			}
		}

		// Select whole track.
		// Prepare the iterator.
		selection.pauseListeners();
		final DepthFirstSearch< Spot, Link > search = new DepthFirstSearch<>( graph, SearchDirection.UNDIRECTED );
		search.setTraversalListener( new SearchListener< Spot, Link, DepthFirstSearch< Spot, Link > >()
		{

			@Override
			public void processVertexLate( final Spot vertex, final DepthFirstSearch< Spot, Link > search )
			{}

			@Override
			public void processVertexEarly( final Spot vertex, final DepthFirstSearch< Spot, Link > search )
			{
				selection.setSelected( vertex, true );
				vertex.setLabel( "selected" );
			}

			@Override
			public void processEdge( final Link edge, final Spot from, final Spot to, final DepthFirstSearch< Spot, Link > search )
			{
				selection.setSelected( edge, true );
			}

			@Override
			public void crossComponent( final Spot from, final Spot to, final DepthFirstSearch< Spot, Link > search )
			{}
		} );

		// Iterate from all vertices that were in the selection.
		search.start( selection.getSelectedVertices().iterator().next() );
		selection.resumeListeners();

		/*
		 * Set highlight & focus.
		 */

		// Find one particular spot.
		for ( final Spot spot : graph.vertices() )
		{
			if ( spot.getLabel().equals( "408" ) )
			{
				highlight.highlightVertex( spot );
				spot.setLabel( "highlighted" );
				break;
			}
		}

		for ( final Spot spot : graph.vertices() )
		{
			if ( spot.getLabel().equals( "406" ) )
			{
				focus.focusVertex( spot );
				spot.setLabel( "focused" );
				break;
			}
		}

		/*
		 * Wrapped model.
		 */

		final OverlayGraphWrapper< Spot, Link > viewGraph = new OverlayGraphWrapper<>(
				graph,
				idmap,
				spatioTemporalIndex,
				graph.getLock(),
				new ModelOverlayProperties( graph, radiusStats ) );
		final RefBimap< Spot, OverlayVertexWrapper< Spot, Link > > vertexMap = viewGraph.getVertexMap();
		final RefBimap< Link, OverlayEdgeWrapper< Spot, Link > > edgeMap = viewGraph.getEdgeMap();

		final SelectionModelAdapter< Spot, Link, OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > viewSelection =
				new SelectionModelAdapter<>( selection, vertexMap, edgeMap );
		final FocusModelAdapter< Spot, Link, OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > viewFocus =
				new FocusModelAdapter<>( focus, vertexMap, edgeMap );
		final HighlightModelAdapter< Spot, Link, OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > viewHighlight =
				new HighlightModelAdapter<>( highlight, vertexMap, edgeMap );
		final GraphColorGeneratorAdapter< Spot, Link, OverlayVertexWrapper< Spot, Link >, OverlayEdgeWrapper< Spot, Link > > viewColoring =
				new GraphColorGeneratorAdapter<>( vertexMap, edgeMap );
		viewColoring.setColorGenerator( coloring );

		/*
		 * Canvas.
		 */

		final InteractiveDisplayCanvasComponent< AffineTransform3D > canvas = new InteractiveDisplayCanvasComponent<>( WIDTH, HEIGHT, new BehaviourTransformEventHandler3DFactory() );

		/*
		 * Picture.
		 */

		final BufferedImageFixAspectRatioOverlayRenderer imgRenderer = new BufferedImageFixAspectRatioOverlayRenderer();
		try
		{
			final File picFile = new File( DummyBdvPanel.class.getResource( "CaptureTP24.PNG" ).getFile() );
			final BufferedImage pic = ImageIO.read( picFile );
			imgRenderer.setBufferedImage( pic );
			final int lwidth = pic.getWidth();
			final int lheight = pic.getHeight();
			imgRenderer.setOriginalSize( new Dimension( lwidth, lheight ) );
		}
		catch ( final IOException e1 )
		{
			e1.printStackTrace();
		}
		final int width = imgRenderer.getOriginalSize().width;
		final int height = imgRenderer.getOriginalSize().height;


		/*
		 * Picture renderer.
		 */

		final int tp = 24;
		canvas.addOverlayRenderer( imgRenderer );

		/*
		 * Model renderer.
		 */

		this.renderer =
				new OverlayGraphRenderer<>( viewGraph, viewHighlight, viewFocus, viewSelection, viewColoring );
		canvas.addOverlayRenderer( renderer );
		renderer.timePointChanged( tp );

		/*
		 * Listeners. Listen to component being resized.
		 */

		canvas.addTransformListener( renderer );
		final AffineTransform3D t = new AffineTransform3D();
		// Set z to model import.
		t.set( -400., 2, 3 );
		canvas.addComponentListener( new ComponentAdapter()
		{

			@Override
			public void componentResized( final ComponentEvent e )
			{
				final int viewerWidth = canvas.getWidth();
				final int viewerHeight = canvas.getHeight();
				final double s1 = ( double ) viewerWidth / width;
				final double s2 = ( double ) viewerHeight / height;
				final double s = Math.max( s1, s2 );
				if ( s < 1e-3 )
					return;
				t.set( s, 0, 0 );
				t.set( s, 1, 1 );
				renderer.transformChanged( t );
			}
		} );
		this.add( canvas, BorderLayout.CENTER );

		/*
		 * Time-points.
		 */

		final JSlider slider = new JSlider( 0, 30, tp );
		this.add( slider, BorderLayout.SOUTH );
		slider.addChangeListener( new ChangeListener()
		{

			@Override
			public void stateChanged( final ChangeEvent e )
			{
				renderer.timePointChanged( slider.getValue() );
				repaint();
			}
		} );
	}

	public void setRenderSettings( final RenderSettings settings )
	{
		renderer.setRenderSettings( settings );
		repaint();
	}
}
