package org.mastodon.feature;

import static org.mastodon.graph.algorithm.AncestorFinder.ancestors;
import static org.mastodon.graph.algorithm.StronglyConnectedComponents.stronglyConnectedComponents;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.function.Function;
import java.util.stream.Collectors;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.RefStack;
import org.mastodon.graph.algorithm.LeafFinder;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.TopologicalSort;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.scijava.InstantiableException;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.util.ClassUtils;

public class FeatureComputerService extends AbstractService
{
	@Parameter
	private PluginService plugins;

	@Parameter
	private CommandService commands;

	@Parameter
	private FeatureSpecsService featureSpecs;

	private final FeatureDependencyGraph dependencies = new FeatureDependencyGraph();

	@Override
	public void initialize()
	{
		clear();

		// build the dependency graph
		// potentially with cycles and potentially with features whose computers are missing
		discover();

		// post-process dependency graph
		// 1.) remove cycles
		removeCycles( dependencies );
		// 2.) recursively remove vertices with missing inputs
		removeMissing( dependencies );

		System.out.println( dependencies );
	}

	private void clear()
	{
		// TODO: IMPLEMENT protected void AbstractObjectGraph.clear()
		// TODO: and public void FeatureDependencyGraph.clear() { super.clear(); }
//		dependencies.clear();
	}

	/**
	 * Add the inputs and outputs of all {@link FeatureComputer}s that are found by the {@code {@link CommandService} to the dependency graph.
	 * <p>
	 * Each vertex that is created has a {@link FeatureSpec}, and hopefully a {@link FeatureComputer} that computes the corresponding {@link Feature}.
	 * <p>
	 * Each edge that is created means a feature dependency ({@code A --> B} means A requires B, so B must be computed before A).
	 */
	private void discover()
	{
		final List< CommandInfo > infos = commands.getCommandsOfType( FeatureComputer.class );
		for ( final CommandInfo info : infos )
		{
			try
			{
				final FeatureComputer fc = ( FeatureComputer ) info.createInstance();

				FeatureSpec< ?, ? > featureSpec = null;
				for ( final ModuleItem< ? > item : info.outputs() )
				{
					if ( !Feature.class.isAssignableFrom( item.getType() ) )
						throw new IllegalArgumentException( "Ignoring FeatureComputer " + info.getClassName() + " because output " + item + " is not of type Feature." );

					if ( featureSpec != null )
						throw new IllegalArgumentException( "Ignoring FeatureComputer " + info.getClassName() + " because it defines more than one output." );

					featureSpec = featureSpecs.getSpec( ( Class< ? extends Feature< ? > > ) item.getType() );
				}

				FeatureDependencyGraph.Vertex v = dependencies.get( featureSpec );
				if ( v == null )
					v = dependencies.addVertex( featureSpec );
				if ( v.getFeatureComputer() != null )
					throw new IllegalArgumentException( "Ignoring FeatureComputer " + info.getClassName() + " because it computes " + featureSpec + " which is already computed by " + v.getFeatureComputer() );
				v.setFeatureComputer( fc, info );

				for ( final ModuleItem< ? > item : info.inputs() )
				{
					if ( Feature.class.isAssignableFrom( item.getType() ) )
					{
						final FeatureSpec dependencySpec = featureSpecs.getSpec( ( Class< ? extends Feature< ? > > ) item.getType() );
						FeatureDependencyGraph.Vertex dependency = dependencies.get( dependencySpec );
						if ( dependency == null )
							dependency = dependencies.addVertex( dependencySpec );
						dependencies.addEdge( v, dependency );
					}
				}
			}
			catch ( InstantiableException | IllegalArgumentException e )
			{
				// TODO: instead of printing the messages, they should be collected into one big message
				// that can then be obtained from the FeatureComputerService and presented to the user in some way
				// (the "presenting to the user" part we can decide on later...)
				System.out.println( e.getMessage() );
			}
		}
	}

	/**
	 * Removes vertices on cycles.
	 */
	public static void removeCycles( final FeatureDependencyGraph dependencies )
	{
		final Set< RefSet< FeatureDependencyGraph.Vertex > > sccs = stronglyConnectedComponents( dependencies );
		for ( final RefSet< FeatureDependencyGraph.Vertex > scc : sccs )
		{
			boolean prune = false;
			if ( scc.size() > 1 )
			{
				// component contains vertices that are on a cycle
				prune = true;
			}
			else if ( scc.size() == 1 )
			{
				// component contains exactly one vertex
				// still needs to be pruned, if the one vertex has an edge to itself
				final FeatureDependencyGraph.Vertex vertex = scc.iterator().next();
				for ( final FeatureDependencyGraph.Edge edge : vertex.outgoingEdges() )
					if ( edge.getTarget().equals( vertex ) )
						prune = true;
			}

			if ( prune )
				for ( final FeatureDependencyGraph.Vertex vertex : scc )
					dependencies.remove( vertex );
		}
	}

	/**
	 * Remove vertices whose feature cannot be computed.
	 * A Feature is incomputable if
	 * <ul>
	 *     <li>no FeatureComputer was found to compute it, or</li>
	 *     <li>it depends on an incomputable feature.</li>
	 * </ul>
	 */
	public static void removeMissing( final FeatureDependencyGraph dependencies )
	{
		final RefSet< FeatureDependencyGraph.Vertex > missing = RefCollections.createRefSet( dependencies.vertices() );
		for ( final FeatureDependencyGraph.Vertex vertex : dependencies.vertices() )
			if ( vertex.getFeatureComputer() == null )
				missing.add( vertex );
		for ( final FeatureDependencyGraph.Vertex vertex : ancestors( dependencies, missing) )
			dependencies.remove( vertex );
	}

	/*
	 *
	 * Partial dependency graphs for given feature keys or FeatureSpecs
	 *
	 */

	private FeatureDependencyGraph dependencyGraphFor( final String... keys )
	{
		return dependencyGraphFor(
				Arrays.stream( keys )
						.map( featureSpecs::getSpec )
						.collect( Collectors.toList() )
		);
	}

	private FeatureDependencyGraph dependencyGraphFor( final Collection< FeatureSpec< ?, ? > > specs )
	{
		final FeatureDependencyGraph graph = new FeatureDependencyGraph();
		new Runnable()
		{
			@Override
			public void run()
			{
				specs.forEach( this::vertex );
			}

			private FeatureDependencyGraph.Vertex vertex( FeatureSpec< ?, ? > spec )
			{
				FeatureDependencyGraph.Vertex vertex = graph.get( spec );
				if ( vertex == null )
				{
					FeatureDependencyGraph.Vertex dep = dependencies.get( spec );
					vertex = graph.addVertex( dep.getFeatureSpec() );
					vertex.setFeatureComputer( dep.getFeatureComputer(), dep.getFeatureComputerInfo() );
					for ( FeatureDependencyGraph.Edge edge : dep.outgoingEdges() )
						graph.addEdge( vertex, vertex( edge.getTarget().getFeatureSpec() ) );
				}
				return vertex;
			}
		}.run();
		return graph;
	}

	public void doStuff()
	{
		final FeatureDependencyGraph dependencyGraph = dependencyGraphFor( "F3", "F1" );
		final RefList< FeatureDependencyGraph.Vertex > sequence = new TopologicalSort<>( dependencyGraph ).get();
		sequence.forEach( System.out::println );


		final Map< FeatureSpec< ?, ? >, Feature< ? > > featureModel = new HashMap<>();
		for ( FeatureDependencyGraph.Vertex vertex : sequence )
		{
			final FeatureComputer featureComputer = vertex.getFeatureComputer();
			final CommandInfo info = vertex.getFeatureComputerInfo();
			final CommandModule module = new CommandModule( info, featureComputer );
			for ( ModuleItem item : info.inputs() )
			{
				final Class klass = item.getType();
				if ( Feature.class.isAssignableFrom( klass ) )
				{
					item.setValue( module, featureModel.get( featureSpecs.getSpec( klass ) ) );
				}
//				else if ( Model.class.isAssignableFrom( klass ) )
//				{
//					...
//				}
//				else if ( SharedBigDataViewerData.class.isAssignableFrom( klass ) )
//				{
//					...
//				}
				else
					System.err.println( "Unknown FeatureComputer input @Parameter " + item );
			}

			featureComputer.createOutput();

			Feature output = ( Feature ) info.outputs().iterator().next().getValue( module );
			featureModel.put( vertex.getFeatureSpec(), output );
		}

		System.out.println( featureModel );
	}
}
