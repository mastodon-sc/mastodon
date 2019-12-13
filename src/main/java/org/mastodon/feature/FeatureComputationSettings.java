package org.mastodon.feature;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import net.imglib2.util.ValuePair;

/**
 * A model that manages a collection of computation settings for feature
 * computers identified with their {@link FeatureSpec}s.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class FeatureComputationSettings
{

	private final Map< FeatureSpec< ?, ? >, Object > settingsMap = new HashMap< FeatureSpec< ?, ? >, Object >();

	/**
	 * The description of the image data. Required to create a proper source
	 * settings.
	 */
	private final AbstractSequenceDescription< ?, ?, ? > sequenceDescription;

	public FeatureComputationSettings( final AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
	{
		this.sequenceDescription = sequenceDescription;
	}

	public SourceSelection getSourceSelection( final FeatureSpec< ?, ? > spec )
	{
		return ( SourceSelection ) settingsMap.computeIfAbsent( spec, k -> new SourceSelection( sequenceDescription ) );
	}

	public SourcePairSelection getSourcePairSelection( final FeatureSpec< ?, ? > spec )
	{
		return ( SourcePairSelection ) settingsMap.computeIfAbsent( spec, k -> new SourcePairSelection( sequenceDescription ) );
	}

	public static class SourceSelection
	{

		private final BitSet selectedSources;

		private final AbstractSequenceDescription< ?, ?, ? > sequenceDescription;

		public SourceSelection( final AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
		{
			this.sequenceDescription = sequenceDescription;
			this.selectedSources = new BitSet( sequenceDescription.getViewSetupsOrdered().size() );
			selectedSources.set( 0, sequenceDescription.getViewSetupsOrdered().size(), true );
		}

		public AbstractSequenceDescription< ?, ?, ? > getSequenceDescription()
		{
			return sequenceDescription;
		}

		public boolean isSourceSelected( final int source )
		{
			return selectedSources.get( source );
		}

		public void setSourceSelected( final int source, final boolean selected )
		{
			selectedSources.set( source, selected );
		}

		@Override
		public String toString()
		{
			final StringBuilder str = new StringBuilder();
			str.append( super.toString() + "\n" );
			for ( int i = 0; i < sequenceDescription.getViewSetupsOrdered().size(); i++ )
				str.append( " - " + i + ": " + sequenceDescription.getViewSetupsOrdered().get( i ).getName() + " -> " + isSourceSelected( i ) + "\n" );

			return str.toString();
		}
	}

	public static class SourcePairSelection
	{

		private final Set< ValuePair< Integer, Integer > > sourcePairs = new HashSet<>();

		private final AbstractSequenceDescription< ?, ?, ? > sequenceDescription;

		public SourcePairSelection( final AbstractSequenceDescription< ?, ?, ? > sequenceDescription )
		{
			this.sequenceDescription = sequenceDescription;
		}

		public AbstractSequenceDescription< ?, ?, ? > getSequenceDescription()
		{
			return sequenceDescription;
		}

		public boolean isSourcePairSelected( final int source1, final int source2 )
		{
			return sourcePairs.contains( new ValuePair<>( Integer.valueOf( source1 ), Integer.valueOf( source2 ) ) );
		}

		public void setSourcePairSelected( final int source1, final int source2, final boolean selected )
		{
			final ValuePair< Integer, Integer > pair = new ValuePair<>( Integer.valueOf( source1 ), Integer.valueOf( source2 ) );
			if ( selected )
				sourcePairs.add( pair );
			else
				sourcePairs.remove( pair );
		}
	}
}
