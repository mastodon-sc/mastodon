package org.mastodon.revised.ui.coloring;

import java.util.Optional;

import java.util.stream.Stream;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.ui.coloring.feature.FeatureColorMode;
import org.mastodon.revised.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.revised.ui.coloring.feature.Projections;
import org.mastodon.revised.ui.coloring.feature.ProjectionsFromFeatureModel;
import org.mastodon.util.Listeners;

/**
 * ColoringModel knows which coloring scheme is currently active.
 * Possible options are: none, by a tag set, by a feature.
 * <p>
 * Notifies listeners when coloring is changed.
 * <p>
 * Listens for disappearing tag sets or features.
 *
 * @author Tobias Pietzsch
 */
public class ColoringModel implements TagSetModel.TagSetModelListener, FeatureColorModeManager.FeatureColorModesListener
{
	public interface ColoringChangedListener
	{
		void coloringChanged();
	}

	private final TagSetModel< ?, ? > tagSetModel;

	private TagSetStructure.TagSet tagSet;

	private FeatureColorMode featureColorMode;

	private final FeatureColorModeManager featureColorModeManager;

	private final Projections projections;

	private final Listeners.List< ColoringChangedListener > listeners;

	public ColoringModel(
			final TagSetModel< ?, ? > tagSetModel,
			final FeatureColorModeManager featureColorModeManager,
			final FeatureModel featureModel )
	{
		this.tagSetModel = tagSetModel;
		this.featureColorModeManager = featureColorModeManager;
		this.projections = new ProjectionsFromFeatureModel( featureModel );
		this.listeners = new Listeners.SynchronizedList<>();
	}

	public Listeners< ColoringChangedListener > listeners()
	{
		return listeners;
	}

	public void colorByNone()
	{
		tagSet = null;
		featureColorMode = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	public void colorByTagSet( final TagSetStructure.TagSet tagSet )
	{
		this.tagSet = tagSet;
		this.featureColorMode = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	public TagSetStructure.TagSet getTagSet()
	{
		return tagSet;
	}

	public void colorByFeature( final FeatureColorMode featureColorMode )
	{
		this.featureColorMode = featureColorMode;
		this.tagSet = null;
		listeners.list.forEach( ColoringChangedListener::coloringChanged );
	}

	public FeatureColorMode getFeatureColorMode()
	{
		return featureColorMode;
	}

	public boolean noColoring()
	{
		return tagSet == null && featureColorMode == null;
	}

	@Override
	public void tagSetStructureChanged()
	{
		if ( tagSet != null )
		{
			final int id = tagSet.id();
			final TagSetStructure tss = tagSetModel.getTagSetStructure();
			final Optional< TagSetStructure.TagSet > ts = tss.getTagSets().stream().filter( t -> t.id() == id ).findFirst();
			if ( ts.isPresent() )
				colorByTagSet( ts.get() );
			else
				colorByNone();
		}
	}

	@Override
	public void featureColorModesChanged()
	{
		if ( featureColorMode != null )
		{
			final String name = featureColorMode.getName();
			Optional< FeatureColorMode > mode = Stream.concat(
					featureColorModeManager.getBuiltinStyles().stream(),
					featureColorModeManager.getUserStyles().stream() )
					.filter( m -> m.getName().equals( name ) && isValid( m ) )
					.findFirst();
			if ( mode.isPresent() )
				colorByFeature( mode.get() );
			else
				colorByNone();
		}
	}

	public TagSetStructure getTagSetStructure()
	{
		return tagSetModel.getTagSetStructure();
	}

	public FeatureColorModeManager getFeatureColorModeManager()
	{
		return featureColorModeManager;
	}

	/**
	 * Returns {@code true} if the specified color mode is valid against the
	 * {@link FeatureModel}. That is: the feature projections that the color
	 * mode rely on are declared in the feature model, and of the right class.
	 *
	 * @param mode
	 *            the color mode
	 * @return {@code true} if the color mode is valid.
	 */
	public boolean isValid( final FeatureColorMode mode )
	{
		if ( mode.getVertexColorMode() != FeatureColorMode.VertexColorMode.NONE
				&& null == projections.getFeatureProjection( mode.getVertexFeatureProjection() ) )
			return false;

		if ( mode.getEdgeColorMode() != FeatureColorMode.EdgeColorMode.NONE
				&& null == projections.getFeatureProjection( mode.getEdgeFeatureProjection() ) )
			return false;

		return true;
	}

	public < V extends Vertex< E >, E extends Edge< V > > GraphColorGenerator< V, E > getFeatureGraphColorGenerator()
	{
		final FeatureColorMode fcm = featureColorMode;
		if ( fcm == null )
			return new DefaultGraphColorGenerator<>();

		// Vertex.
		final ColorGenerator< V > vertexColorGenerator;
		final FeatureProjection< ? > vertexProjection = projections.getFeatureProjection( fcm.getVertexFeatureProjection() );
		if ( null == vertexProjection )
			vertexColorGenerator = new DefaultColorGenerator<>();
		else
		{
			final String vertexColorMap = fcm.getVertexColorMap();
			final double vertexRangeMin = fcm.getVertexRangeMin();
			final double vertexRangeMax = fcm.getVertexRangeMax();
			switch ( fcm.getVertexColorMode() )
			{
			case INCOMING_EDGE:
				vertexColorGenerator = new FeatureColorGeneratorIncomingEdge<>(
						( FeatureProjection< E > ) vertexProjection,
						ColorMap.getColorMap( vertexColorMap ),
						vertexRangeMin, vertexRangeMax );
				break;
			case OUTGOING_EDGE:
				vertexColorGenerator = new FeatureColorGeneratorOutgoingEdge<>(
						( FeatureProjection< E > ) vertexProjection,
						ColorMap.getColorMap( vertexColorMap ),
						vertexRangeMin, vertexRangeMax );
				break;
			case VERTEX:
				vertexColorGenerator = new FeatureColorGenerator<>(
						( FeatureProjection< V > ) vertexProjection,
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
		final EdgeColorGenerator< V, E > edgeColorGenerator;
		final FeatureProjection< ? > edgeProjection = projections.getFeatureProjection( fcm.getEdgeFeatureProjection() );
		if ( null == edgeProjection )
			edgeColorGenerator = new DefaultEdgeColorGenerator<>();
		else
		{
			final String edgeColorMap = fcm.getEdgeColorMap();
			final double edgeRangeMin = fcm.getEdgeRangeMin();
			final double edgeRangeMax = fcm.getEdgeRangeMax();
			switch ( fcm.getEdgeColorMode() )
			{
			case SOURCE_VERTEX:
				edgeColorGenerator = new FeatureColorGeneratorSourceVertex<>(
						( FeatureProjection< V > ) edgeProjection,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin, edgeRangeMax );
				break;
			case TARGET_VERTEX:
				edgeColorGenerator = new FeatureColorGeneratorTargetVertex<>(
						( FeatureProjection< V > ) edgeProjection,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin, edgeRangeMax );
				break;
			case EDGE:
				edgeColorGenerator = new FeatureEdgeColorGenerator<>(
						( FeatureProjection< E > ) edgeProjection,
						ColorMap.getColorMap( edgeColorMap ),
						edgeRangeMin, edgeRangeMax );
				break;
			case NONE:
			default:
				edgeColorGenerator = new DefaultEdgeColorGenerator<>();
				break;
			}
		}

		return new CompositeGraphColorGenerator<>( vertexColorGenerator, edgeColorGenerator );
	}
}
