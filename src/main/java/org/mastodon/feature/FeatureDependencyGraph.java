package org.mastodon.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.mastodon.graph.object.AbstractObjectEdge;
import org.mastodon.graph.object.AbstractObjectGraph;
import org.mastodon.graph.object.AbstractObjectVertex;
import org.scijava.command.CommandInfo;

/**
 * Each vertex represents a Feature and tha FeatureComputer that computes it.
 * Each edge represents a dependency, {@code A --> B} means "feature A depends on feature B".
 */
public class FeatureDependencyGraph extends AbstractObjectGraph< FeatureDependencyGraph.Vertex, FeatureDependencyGraph.Edge >
{
	private final Map< FeatureSpec< ?, ? >, Vertex > features;

	public FeatureDependencyGraph()
	{
		super( new Factory(), new HashSet<>(), new HashSet<>() );
		features = new HashMap<>();
	}

	public boolean contains( final FeatureSpec< ?, ? > featureSpec )
	{
		return features.containsKey( featureSpec );
	}

	public Vertex get( final FeatureSpec< ?, ? > featureSpec )
	{
		return features.get( featureSpec );
	}

	public Vertex addVertex( final FeatureSpec< ?, ? > featureSpec )
	{
		final Vertex v = super.addVertex().init( featureSpec );
		features.put( featureSpec, v );
		return v;
	}

	@Override
	public Vertex addVertex()
	{
		throw new UnsupportedOperationException( "Use addVertex(FeatureSpec) instead" );
	}

	private static class Factory implements AbstractObjectGraph.Factory< Vertex, Edge >
	{
		@Override
		public Vertex createVertex()
		{
			return new Vertex();
		}

		@Override
		public Edge createEdge( final Vertex source, final Vertex target )
		{
			return new Edge( source, target );
		}
	}

	public static class Edge extends AbstractObjectEdge< Edge, Vertex >
	{
		Edge( final Vertex source, final Vertex target )
		{
			super( source, target );
		}

		@Override
		public String toString()
		{
			return "Edge{ " + getSource().featureSpec.getKey() + " --> " + getTarget().featureSpec.getKey() + "}";
		}
	}

	public static class Vertex extends AbstractObjectVertex< Vertex, Edge >
	{
		private FeatureSpec< ?, ? > featureSpec;

		private FeatureComputer featureComputer;

		private CommandInfo featureComputerInfo;

		private Vertex()
		{}

		private Vertex init( final FeatureSpec< ?, ? > featureSpec )
		{
			this.featureSpec = featureSpec;
			return this;
		}

		public FeatureSpec< ?, ? > getFeatureSpec()
		{
			return featureSpec;
		}

		public void setFeatureComputer(
				final FeatureComputer featureComputer,
				final CommandInfo featureComputerInfo )
		{
			this.featureComputer = featureComputer;
			this.featureComputerInfo = featureComputerInfo;
		}

		public FeatureComputer getFeatureComputer()
		{
			return featureComputer;
		}

		public CommandInfo getFeatureComputerInfo()
		{
			return featureComputerInfo;
		}

		@Override
		public String toString()
		{
			return "Vertex{" +
					"featureSpec=" + featureSpec.getKey() +
					", featureComputer=" + ( featureComputer == null ? "null" : featureComputer.getClass().getSimpleName() ) +
					'}';
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append( "FeatureDependencyGraph{\n" );
		sb.append( "  {\n" );
		for ( Vertex vertex : vertices() )
			sb.append( "    " + vertex + "\n" );
		sb.append( "  }, {\n" );
		for ( Edge edge : edges() )
			sb.append( "    " + edge + "\n" );
		sb.append( "  }\n" );
		sb.append( "}" );
		return sb.toString();
	}
}
