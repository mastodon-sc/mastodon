/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.feature;

import static org.mastodon.graph.algorithm.AncestorFinder.ancestors;
import static org.mastodon.graph.algorithm.StronglyConnectedComponents.stronglyConnectedComponents;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.object.AbstractObjectEdge;
import org.mastodon.graph.object.AbstractObjectGraph;
import org.mastodon.graph.object.AbstractObjectVertex;
import org.scijava.command.CommandInfo;

/**
 * Each vertex represents a Feature and the FeatureComputer that computes it.
 * Each edge represents a dependency, {@code A --> B} means "feature A depends
 * on feature B".
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

	@Override
	public void remove( final Vertex vertex )
	{
		super.remove( vertex );
		features.remove( vertex.getFeatureSpec() );
	}

	@Override
	public void clear()
	{
		super.clear();
		features.clear();
	}

	/**
	 * Get {@code FeatureSpec}s for all vertices in this graph.
	 *
	 * @return set of {@code FeatureSpec}s of all vertices in this graph.
	 */
	public Set< FeatureSpec< ?, ? > > getFeatureSpecs()
	{
		return Collections.unmodifiableSet( features.keySet() );
	}

	/**
	 * Removes vertices on cycles.
	 */
	public void removeCycles()
	{
		final Set< RefSet< Vertex > > sccs = stronglyConnectedComponents( this );
		for ( final RefSet< FeatureDependencyGraph.Vertex > scc : sccs )
		{
			boolean prune = false;
			if ( scc.size() > 1 )
			{
				// Component contains vertices that are on a cycle.
				prune = true;
			}
			else if ( scc.size() == 1 )
			{
				// Component contains exactly one vertex.
				// Still needs to be pruned, if the one vertex has an edge to
				// itself.
				final FeatureDependencyGraph.Vertex vertex = scc.iterator().next();
				for ( final FeatureDependencyGraph.Edge edge : vertex.outgoingEdges() )
					if ( edge.getTarget().equals( vertex ) )
						prune = true;
			}

			if ( prune )
				for ( final FeatureDependencyGraph.Vertex vertex : scc )
					remove( vertex );
		}
	}

	/**
	 * Removes vertices whose feature cannot be computed. A Feature is
	 * incomputable if
	 * <ul>
	 * <li>no {@link FeatureComputer} was found to compute it, or</li>
	 * <li>it depends on an incomputable feature.</li>
	 * </ul>
	 */
	public void removeIncomputable()
	{
		final RefSet< FeatureDependencyGraph.Vertex > missing = RefCollections.createRefSet( vertices() );
		for ( final FeatureDependencyGraph.Vertex vertex : vertices() )
			if ( vertex.getFeatureComputer() == null )
				missing.add( vertex );
		for ( final FeatureDependencyGraph.Vertex vertex : ancestors( this, missing ) )
			remove( vertex );
	}

	/**
	 * Get the subgraph of (vertices for) the given {@code FeatureSpec}s and
	 * recursive dependencies.
	 * <p>
	 * <em>Note that this assumes that there are no cycles in the graph!</em>
	 * 
	 * @param specs
	 *            the collection of feature specs.
	 * @return a new feature dependency graph.
	 */
	public FeatureDependencyGraph subGraphFor( final Collection< FeatureSpec< ?, ? > > specs )
	{
		final FeatureDependencyGraph graph = new FeatureDependencyGraph();
		new Runnable()
		{
			@Override
			public void run()
			{
				specs.forEach( this::vertex );
			}

			private FeatureDependencyGraph.Vertex vertex( final FeatureSpec< ?, ? > spec )
			{
				FeatureDependencyGraph.Vertex vertex = graph.get( spec );
				if ( vertex == null )
				{
					final FeatureDependencyGraph.Vertex dep = FeatureDependencyGraph.this.get( spec );
					vertex = graph.addVertex( dep.getFeatureSpec() );
					vertex.setFeatureComputer( dep.getFeatureComputer(), dep.getFeatureComputerInfo() );
					for ( final FeatureDependencyGraph.Edge edge : dep.outgoingEdges() )
						graph.addEdge( vertex, vertex( edge.getTarget().getFeatureSpec() ) );
				}
				return vertex;
			}
		}.run();
		return graph;
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
		final StringBuilder sb = new StringBuilder();

		sb.append( "FeatureDependencyGraph{\n" );
		sb.append( "  {\n" );
		for ( final Vertex vertex : vertices() )
			sb.append( "    " + vertex + "\n" );
		sb.append( "  }, {\n" );
		for ( final Edge edge : edges() )
			sb.append( "    " + edge + "\n" );
		sb.append( "  }\n" );
		sb.append( "}" );
		return sb.toString();
	}
}
