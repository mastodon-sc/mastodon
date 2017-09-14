package org.mastodon.revised.mamut.feature;

import java.util.ArrayList;
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
import org.mastodon.revised.mamut.ProgressListener;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.feature.FeatureComputerService;
import org.mastodon.revised.model.mamut.Model;
import org.scijava.InstantiableException;
import org.scijava.app.StatusService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;

@Plugin( type = FeatureComputerService.class )
public class DefaultMamutFeatureComputerService extends AbstractService implements FeatureComputerService< Model >
{

	@Parameter
	private PluginService pluginService;

	@Parameter
	private LogService logService;

	@Parameter
	private StatusService status;

	/**
	 * Feature computers of any type.
	 */
	private final Map< String, FeatureComputer< Model > > availableFeatureComputers = new HashMap<>();

	/**
	 * Available spot feature computers.
	 */
	private Set< String > availableSpotFeatureComputers;

	/**
	 * Available link feature computers.
	 */
	private Set< String > availableLinkFeatureComputers;

	public DefaultMamutFeatureComputerService()
	{}

	@Override
	public void initialize()
	{
		super.initialize();
		initializeAvailableSpotFeatureComputers();
		initializeAvailableLinkFeatureComputers();
	}

	@Override
	public final Set< String > getAvailableVertexFeatureComputers()
	{
		return availableSpotFeatureComputers;
	}

	@Override
	public Set< String > getAvailableEdgeFeatureComputers()
	{
		return availableLinkFeatureComputers;
	}

	@Override
	public boolean compute( final Model model, final Set< String > computerNames, final ProgressListener progressListener )
	{
		final ObjectGraph< FeatureComputer< Model > > dependencyGraph = getDependencyGraph( computerNames );
		final TopologicalSort< ObjectVertex< FeatureComputer< Model > >, ObjectEdge< FeatureComputer< Model > > > sorter
			= new TopologicalSort<>( dependencyGraph );

		if (sorter.hasFailed())
		{
			logService.error( "Could not compute features using  " + computerNames +
					" as they have a circular dependency." );
			progressListener.showStatus( "Circular dependency!" );
			return false;
		}

		final long start = System.currentTimeMillis();

		model.getFeatureModel().clear();
		int progress = 1;
		for ( final ObjectVertex< FeatureComputer< Model > > v : sorter.get() )
		{
			final FeatureComputer< Model > computer = v.getContent();
			progressListener.showStatus( computer.getKey() );
			final Feature< ?, ?, ? > feature = computer.compute( model );
			model.getFeatureModel().declareFeature( feature );

			progressListener.showProgress( progress++, computerNames.size() );
		}
		
		final long end = System.currentTimeMillis();
		progressListener.clearStatus();
		progressListener.showStatus( String.format( "Done in %.1f s.", ( end - start ) / 1000. ) );

		return true;
	}

	/*
	 * DEPENDENCY GRAPH.
	 */

	private ObjectGraph< FeatureComputer< Model > > getDependencyGraph( final Set< String > computerNames )
	{
		final ObjectGraph< FeatureComputer< Model > > computerGraph = new ObjectGraph<>();
		final ObjectVertex< FeatureComputer< Model > > ref = computerGraph.vertexRef();

		final Set< FeatureComputer< Model > > requestedFeatureComputers = new HashSet<>();
		for ( final String cName : computerNames )
		{
			// Build a list of feature computers.
			requestedFeatureComputers.add( availableFeatureComputers.get( cName ) );

			// Add them in the dependency graph.
			addDepVertex( cName, computerGraph, ref );
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
	private void prune( final ObjectGraph< FeatureComputer< Model > > computerGraph, final Set< FeatureComputer< Model > > requestedFeatureComputers )
	{
		for ( final ObjectVertex< FeatureComputer< Model > > v : new ArrayList<>( computerGraph.vertices() ) )
			if ( v.incomingEdges().isEmpty() && !requestedFeatureComputers.contains( v.getContent() ) )
				computerGraph.remove( v );
	}

	/**
	 * Called recursively.
	 *
	 * @param depName
	 * @param computerGraph
	 * @param ref
	 * @return
	 */
	private final ObjectVertex< FeatureComputer< Model > > addDepVertex(
			final String depName,
			final ObjectGraph< FeatureComputer< Model > > computerGraph,
			final ObjectVertex< FeatureComputer< Model > > ref )
	{
		final FeatureComputer< Model > fc = availableFeatureComputers.get( depName );
		if ( null == fc )
		{
			logService.error( "Cannot add feature computer named " + depName + " as it is not registered." );
			return null;
		}

		for ( final ObjectVertex< FeatureComputer< Model > > v : computerGraph.vertices() )
		{
			if ( v.getContent().equals( fc ) )
				return v;
		}

		final ObjectVertex< FeatureComputer< Model > > source = computerGraph.addVertex( ref ).init( fc );
		final Set< String > deps = fc.getDependencies();

		final ObjectVertex< FeatureComputer< Model > > vref2 = computerGraph.vertexRef();
		final ObjectEdge< FeatureComputer< Model > > eref = computerGraph.edgeRef();

		for ( final String dep : deps )
		{
			final ObjectVertex< FeatureComputer< Model > > target = addDepVertex( dep, computerGraph, vref2 );
			if ( null == target )
			{
				logService.error( "Removing feature computer named " + depName + " as some of its dependencies could not be resolved." );
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

	private void initializeAvailableSpotFeatureComputers()
	{
		this.availableSpotFeatureComputers =
				initializeFeatureComputers( SpotFeatureComputer.class );
	}

	private void initializeAvailableLinkFeatureComputers()
	{
		this.availableLinkFeatureComputers =
				initializeFeatureComputers( LinkFeatureComputer.class );
	}

	private < K extends FeatureComputer< Model > > Set< String > initializeFeatureComputers( final Class< K > cl )
	{
		final List< PluginInfo< K > > infos = pluginService.getPluginsOfType( cl );
		final Set< String > names = new HashSet<>( infos.size() );
		for ( final PluginInfo< K > info : infos )
		{
			final String name = info.getName();
			if ( availableFeatureComputers.keySet().contains( name ) )
			{
				logService.error( "Cannot register feature computer with name " + name + " of class " + cl +
						". There is already a feature computer registered with this name." );
				continue;
			}

			try
			{
				final K computer = info.createInstance();
				availableFeatureComputers.put( name, computer );
				names.add( name );
			}
			catch ( final InstantiableException e )
			{
				logService.error( "Could not instantiate computer  with name " + name + " of class " + cl +
						":\n" + e.getMessage() );
				e.printStackTrace();
			}
		}
		return Collections.unmodifiableSet( names );
	}

}