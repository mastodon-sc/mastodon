package org.mastodon.revised.model.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mastodon.graph.algorithm.TopologicalSort;
import org.mastodon.graph.object.ObjectEdge;
import org.mastodon.graph.object.ObjectGraph;
import org.mastodon.graph.object.ObjectVertex;
import org.mastodon.revised.mamut.MastodonLogger;
import org.mastodon.revised.model.AbstractModel;
import org.scijava.InstantiableException;
import org.scijava.log.LogSource;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;

public abstract class AbstractFeatureComputerService< AM extends AbstractModel< ?, ?, ? > > extends AbstractService implements FeatureComputerService< AM >
{

	@Parameter
	private PluginService pluginService;

	@Parameter
	private MastodonLogger logger;

	/**
	 * Feature computers of any type, mapped by their key, for dependency
	 * management.
	 */
	private final Map< String, FeatureComputer< AM > > featureComputers = new HashMap<>();

	private LogSource logSource;

	@Override
	public void initialize()
	{
		super.initialize();
		logSource = logger.getLogSourceRoot().subSource( "Feature computer" );
	}

	@Override
	public boolean compute( final AM model, final FeatureModel featureModel, final Set< FeatureComputer< AM > > computers )
	{
		final ObjectGraph< FeatureComputer< AM > > dependencyGraph = getDependencyGraph( computers );
		final TopologicalSort< ObjectVertex< FeatureComputer< AM > >, ObjectEdge< FeatureComputer< AM > > > sorter = new TopologicalSort<>( dependencyGraph );

		if ( sorter.hasFailed() )
		{
			logger.error( "Could not compute features using  " + computers +
					" as they have a circular dependency.", logSource );
			logger.setStatus( "Circular dependency!", logSource );
			return false;
		}

		final long start = System.currentTimeMillis();

		featureModel.clear();
		int progress = 1;
		for ( final ObjectVertex< FeatureComputer< AM > > v : sorter.get() )
		{
			final FeatureComputer< AM > computer = v.getContent();
			logger.setStatus( computer.getKey(), logSource );
			final Feature< ?, ? > feature = computer.compute( model );
			featureModel.declareFeature( feature );

			logger.setProgress( progress++ / ( double ) computers.size(), logSource );
		}

		final long end = System.currentTimeMillis();
		logger.setProgress( 1., logSource );
		logger.info( String.format( "Done in %.1f s.", ( end - start ) / 1000. ), logSource );

		return true;
	}

	/*
	 * DEPENDENCY GRAPH.
	 */

	private ObjectGraph< FeatureComputer< AM > > getDependencyGraph( final Set< FeatureComputer< AM > > computers )
	{
		final ObjectGraph< FeatureComputer< AM > > computerGraph = new ObjectGraph<>();
		final ObjectVertex< FeatureComputer< AM > > ref = computerGraph.vertexRef();

		final Set< FeatureComputer< AM > > requestedFeatureComputers = new HashSet<>();
		for ( final FeatureComputer< AM > computer : computers )
		{
			// Build a list of feature computers.
			requestedFeatureComputers.add( computer );

			// Add them in the dependency graph.
			addDepVertex( computer, computerGraph, ref );
		}

		computerGraph.releaseRef( ref );
		prune( computerGraph, requestedFeatureComputers );
		return computerGraph;
	}

	/**
	 * Removes uncalled for dependencies.
	 * <p>
	 * When a computer has missing dependencies, it is removed from the
	 * dependency graph. But its dependencies that are available are still
	 * present in the graph after its removal. This method removes them if their
	 * calculation were not requested by the user.
	 *
	 * @param computerGraph
	 * @param requestedFeatureComputers
	 */
	private void prune( final ObjectGraph< FeatureComputer< AM > > computerGraph, final Set< FeatureComputer< AM > > requestedFeatureComputers )
	{
		for ( final ObjectVertex< FeatureComputer< AM > > v : new ArrayList<>( computerGraph.vertices() ) )
			if ( v.incomingEdges().isEmpty() && !requestedFeatureComputers.contains( v.getContent() ) )
				computerGraph.remove( v );
	}

	/**
	 * Called recursively.
	 *
	 * @param computer
	 * @param computerGraph
	 * @param ref
	 * @return
	 */
	private final ObjectVertex< FeatureComputer< AM > > addDepVertex(
			final FeatureComputer< AM > computer,
			final ObjectGraph< FeatureComputer< AM > > computerGraph,
			final ObjectVertex< FeatureComputer< AM > > ref )
	{
		for ( final ObjectVertex< FeatureComputer< AM > > v : computerGraph.vertices() )
		{
			if ( v.getContent().equals( computer ) )
				return v;
		}

		final ObjectVertex< FeatureComputer< AM > > source = computerGraph.addVertex( ref ).init( computer );
		final Set< String > deps = computer.getDependencies();

		final ObjectVertex< FeatureComputer< AM > > vref2 = computerGraph.vertexRef();
		final ObjectEdge< FeatureComputer< AM > > eref = computerGraph.edgeRef();

		for ( final String dep : deps )
		{
			final FeatureComputer< AM > computerDep = featureComputers.get( dep );
			if ( null == computerDep )
			{
				logger.error( "Cannot add feature computer named " + dep + " as it is not registered.", logSource );
				return null;
			}

			final ObjectVertex< FeatureComputer< AM > > target = addDepVertex( computerDep, computerGraph, vref2 );
			if ( null == target )
			{
				logger.error( "Removing feature computer named " + computer + " as some of its dependencies could not be resolved.", logSource );
				computerGraph.remove( source );
				break;
			}
			computerGraph.addEdge( source, target, eref );
		}

		computerGraph.releaseRef( vref2 );
		computerGraph.releaseRef( eref );

		return source;
	}

	/*
	 * PRIVATE METHODS.
	 */

	protected < K extends FeatureComputer< AM > > void initializeFeatureComputers( final Class< K > cl )
	{
		final List< PluginInfo< K > > infos = pluginService.getPluginsOfType( cl );
		for ( final PluginInfo< K > info : infos )
		{
			final String name = info.getName();
			if ( featureComputers.keySet().contains( name ) )
			{
				logger.error( "Cannot register feature computer with name " + name + " of class " + cl +
						". There is already a feature computer registered with this name.", logSource );
				continue;
			}

			try
			{
				final K computer = info.createInstance();
				featureComputers.put( name, computer );
			}
			catch ( final InstantiableException e )
			{
				logger.error( "Could not instantiate computer  with name " + name + " of class " + cl +
						":\n" + e.getMessage(), logSource );
				e.printStackTrace();
			}
		}
	}

	@Override
	public Collection< FeatureComputer< AM > > getFeatureComputers()
	{
		return Collections.unmodifiableCollection( featureComputers.values() );
	}
}
