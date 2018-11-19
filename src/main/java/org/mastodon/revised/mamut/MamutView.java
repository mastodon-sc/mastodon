package org.mastodon.revised.mamut;

import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.MastodonFrameView;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.ui.coloring.ColorGenerator;
import org.mastodon.revised.ui.coloring.ColorMap;
import org.mastodon.revised.ui.coloring.ColoringMenu;
import org.mastodon.revised.ui.coloring.ColoringModel;
import org.mastodon.revised.ui.coloring.ComposedGraphColorGenerator;
import org.mastodon.revised.ui.coloring.DefaultColorGenerator;
import org.mastodon.revised.ui.coloring.FeatureColorGenerator;
import org.mastodon.revised.ui.coloring.FeatureColorGeneratorIncomingEdge;
import org.mastodon.revised.ui.coloring.FeatureColorGeneratorOutgoingEdge;
import org.mastodon.revised.ui.coloring.FeatureColorGeneratorSourceVertex;
import org.mastodon.revised.ui.coloring.FeatureColorGeneratorTargetVertex;
import org.mastodon.revised.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.revised.ui.coloring.TagSetGraphColorGenerator;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.revised.ui.coloring.feature.revised.Playground.ProjectionsFromFeatureModel;

public class MamutView< VG extends ViewGraph< Spot, Link, V, E >, V extends Vertex< E >, E extends Edge< V > >
		extends MastodonFrameView< MamutAppModel, VG, Spot, Link, V, E >
{
	public MamutView( final MamutAppModel appModel, final VG viewGraph, final String[] keyConfigContexts )
	{
		super( appModel, viewGraph, keyConfigContexts );
	}

	/**
	 * Sets up and register the coloring menu item and related actions and
	 * listeners.
	 *
	 * @param coloring
	 * @param menuHandle
	 * @param refresh
	 */
	protected void registerColoring( final GraphColorGeneratorAdapter< Spot, Link, V, E > coloring, final JMenuHandle menuHandle, final Runnable refresh )
	{
		final TagSetModel< Spot, Link > tagSetModel = appModel.getModel().getTagSetModel();
		final FeatureModel featureModel = appModel.getModel().getFeatureModel();
		final FeatureColorModeManager featureColorModeManager = appModel.getFeatureColorModeManager();
		final ColoringModel coloringModel = new ColoringModel( tagSetModel, featureColorModeManager );

		tagSetModel.listeners().add( coloringModel );
		onClose( () -> tagSetModel.listeners().remove( coloringModel ) );

		featureColorModeManager.getForwardDefaultMode().updateListeners().add( coloringModel );
		onClose( () -> featureColorModeManager.getForwardDefaultMode().updateListeners().remove( coloringModel ) );

		final ColoringMenu coloringMenu = new ColoringMenu( menuHandle.getMenu(), coloringModel,
				appModel.getModel().getFeatureModel() );

		tagSetModel.listeners().add( coloringMenu );
		onClose( () -> tagSetModel.listeners().remove( coloringMenu ) );

		featureModel.listeners().add( coloringMenu );
		onClose( () -> featureModel.listeners().remove( coloringMenu ) );

		featureColorModeManager.getForwardDefaultMode().updateListeners().add( coloringMenu );
		onClose( () -> featureColorModeManager.getForwardDefaultMode().updateListeners().remove( coloringMenu ) );

		final ProjectionsFromFeatureModel projections = new ProjectionsFromFeatureModel( featureModel );

		@SuppressWarnings( "unchecked" )
		final ColoringModel.ColoringChangedListener coloringChangedListener = () -> {
			if ( coloringModel.noColoring() )
				coloring.setColorGenerator( null );
			else if ( coloringModel.getTagSet() != null )
				coloring.setColorGenerator( new TagSetGraphColorGenerator<>( tagSetModel, coloringModel.getTagSet() ) );
			else if ( coloringModel.getFeatureColorMode() != null )
			{
				final FeatureColorMode fcm = coloringModel.getFeatureColorMode();

				// Vertex.
				final ColorGenerator< Spot > vertexColorGenerator;
				final FeatureProjection< ? > vertexProjection = projections.getFeatureProjection( fcm.getVertexFeatureProjection() );
				if ( null == vertexProjection )
					vertexColorGenerator = new DefaultColorGenerator< Spot >();
				else
				{
					final String vertexColorMap = fcm.getVertexColorMap();
					final double vertexRangeMin = fcm.getVertexRangeMin();
					final double vertexRangeMax = fcm.getVertexRangeMax();
					switch ( fcm.getVertexColorMode() )
					{
					case INCOMING_EDGE:
						vertexColorGenerator = new FeatureColorGeneratorIncomingEdge< Spot, Link >(
								( FeatureProjection< Link > ) vertexProjection,
								ColorMap.getColorMap( vertexColorMap ),
								vertexRangeMin, vertexRangeMax,
								appModel.getModel().getGraph().edgeRef() );
						break;
					case OUTGOING_EDGE:
						vertexColorGenerator = new FeatureColorGeneratorOutgoingEdge< Spot, Link >(
								( FeatureProjection< Link > ) vertexProjection,
								ColorMap.getColorMap( vertexColorMap ),
								vertexRangeMin, vertexRangeMax,
								appModel.getModel().getGraph().edgeRef() );
						break;
					case VERTEX:
						vertexColorGenerator = new FeatureColorGenerator< Spot >(
								( FeatureProjection< Spot > ) vertexProjection,
								ColorMap.getColorMap( vertexColorMap ),
								vertexRangeMin, vertexRangeMax );
						break;
					case NONE:
					default:
						vertexColorGenerator = new DefaultColorGenerator<>();
						break;
					}
				}

				// Edge.
				final ColorGenerator< Link > edgeColorGenerator;
				final FeatureProjection< ? > edgeProjection = projections.getFeatureProjection( fcm.getEdgeFeatureProjection() );
				if ( null == edgeProjection )
					edgeColorGenerator = new DefaultColorGenerator< Link >();
				else
				{
					final String edgeColorMap = fcm.getEdgeColorMap();
					final double edgeRangeMin = fcm.getEdgeRangeMin();
					final double edgeRangeMax = fcm.getEdgeRangeMax();
					switch ( fcm.getEdgeColorMode() )
					{
					case SOURCE_VERTEX:
						edgeColorGenerator = new FeatureColorGeneratorSourceVertex< Spot, Link >(
								( FeatureProjection< Spot > ) edgeProjection,
								ColorMap.getColorMap( edgeColorMap ),
								edgeRangeMin, edgeRangeMax,
								appModel.getModel().getGraph().vertexRef() );
						break;
					case TARGET_VERTEX:
						edgeColorGenerator = new FeatureColorGeneratorTargetVertex< Spot, Link >(
								( FeatureProjection< Spot > ) edgeProjection,
								ColorMap.getColorMap( edgeColorMap ),
								edgeRangeMin, edgeRangeMax,
								appModel.getModel().getGraph().vertexRef() );
						break;
					case EDGE:
						edgeColorGenerator = new FeatureColorGenerator< Link >(
								( FeatureProjection< Link > ) edgeProjection,
								ColorMap.getColorMap( edgeColorMap ),
								edgeRangeMin, edgeRangeMax );
						break;
					case NONE:
					default:
						edgeColorGenerator = new DefaultColorGenerator<>();
						break;
					}
				}

				coloring.setColorGenerator( new ComposedGraphColorGenerator<>( vertexColorGenerator, edgeColorGenerator ) );
			}
			refresh.run();
		};
		coloringModel.listeners().add( coloringChangedListener );
	}
}
