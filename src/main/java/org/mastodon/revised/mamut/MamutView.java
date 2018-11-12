package org.mastodon.revised.mamut;

import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.MastodonFrameView;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;
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
				appModel.getModel().getFeatureModel(), Spot.class, Link.class );

		tagSetModel.listeners().add( coloringMenu );
		onClose( () -> tagSetModel.listeners().remove( coloringMenu ) );

		featureModel.listeners().add( coloringMenu );
		onClose( () -> featureModel.listeners().remove( coloringMenu ) );

		featureColorModeManager.getForwardDefaultMode().updateListeners().add( coloringMenu );
		onClose( () -> featureColorModeManager.getForwardDefaultMode().updateListeners().remove( coloringMenu ) );

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
				final String[] vertexKeys = fcm.getVertexFeatureProjection();
				final FeatureSpec< ?, ? > vertexFeatureSpec = getFeatureSpec( featureModel, vertexKeys[0] );
				final Feature< ? > vertexFeature = featureModel.getFeature( vertexFeatureSpec );
				if ( null == vertexFeature || null == vertexFeature.project( vertexKeys[ 1 ] ) )
					vertexColorGenerator = new DefaultColorGenerator< Spot >();
				else
				{
					final FeatureProjection< ? > vertexProjection = vertexFeature.project( vertexKeys[ 1 ] );
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
				final String[] edgeKeys = fcm.getEdgeFeatureProjection();
				final FeatureSpec< ?, ? > edgeFeatureSpec = getFeatureSpec( featureModel, edgeKeys[0] );
				final Feature< ? > edgeFeature = featureModel.getFeature( edgeFeatureSpec );
				if ( null == edgeFeature || null == edgeFeature.project( edgeKeys[ 1 ] ) )
					edgeColorGenerator = new DefaultColorGenerator< Link >();
				else
				{
					final FeatureProjection< ? > edgeProjection = edgeFeature.project( edgeKeys[ 1 ] );
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

	private static final FeatureSpec< ?, ? > getFeatureSpec( final FeatureModel featureModel, final String featureKey )
	{
		return featureModel.getFeatureSpecs().stream()
				.filter( ( fs ) -> featureKey.equals( fs.getKey() ) )
				.findFirst()
				.get();
	}
}
