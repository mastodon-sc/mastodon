package org.mastodon.feature;

import static org.mastodon.graph.algorithm.AncestorFinder.ancestors;
import static org.mastodon.graph.algorithm.StronglyConnectedComponents.stronglyConnectedComponents;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.algorithm.TopologicalSort;
import org.scijava.Cancelable;
import org.scijava.InstantiableException;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.module.ModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;

@Plugin( type = FeatureComputerService.class )
public class DefaultFeatureComputerService extends AbstractService implements FeatureComputerService
{
	@Parameter
	private PluginService plugins;

	@Parameter
	private CommandService commands;

	@Parameter
	private FeatureSpecsService featureSpecs;

	private final FeatureDependencyGraph dependencies = new FeatureDependencyGraph();

	private final Class< ? extends FeatureComputer > klass;

	private PropertyChangeListener propertyChangeListener = new PropertyChangeListener()
	{
		// Does nothing.
		@Override
		public void propertyChange( final PropertyChangeEvent evt )
		{}
	};

	private String cancelReason;

	private FeatureComputer currentFeatureComputer;

	public DefaultFeatureComputerService()
	{
		this( FeatureComputer.class );
	}

	protected DefaultFeatureComputerService( final Class< ? extends FeatureComputer > klass )
	{
		this.klass = klass;
	}

	@Override
	public void initialize()
	{
		clear();

		/*
		 * Build the dependency graph. Potentially with cycles and potentially
		 * with features whose computers are missing.
		 */
		discover();

		// Post-process dependency graph.
		// 1.) remove cycles
		removeCycles( dependencies );
		// 2.) recursively remove vertices with missing inputs
		removeMissing( dependencies );
	}

	private void clear()
	{
		// TODO: IMPLEMENT protected void AbstractObjectGraph.clear()
		// TODO: and public void FeatureDependencyGraph.clear() { super.clear();
		// }
//		dependencies.clear();
	}

	/**
	 * Adds the inputs and outputs of all {@link FeatureComputer}s that are
	 * found by the {@link CommandService} to the dependency graph.
	 * <p>
	 * Each vertex that is created has a {@link FeatureSpec}, and hopefully a
	 * {@link FeatureComputer} that computes the corresponding {@link Feature}.
	 * <p>
	 * Each edge that is created means a feature dependency ({@code A --> B}
	 * means A requires B, so B must be computed before A).
	 */
	private void discover()
	{
		final List< CommandInfo > infos = commands.getCommandsOfType( klass );
		for ( final CommandInfo info : infos )
		{
			try
			{
				final FeatureComputer fc = ( FeatureComputer ) info.createInstance();

				FeatureSpec< ?, ? > featureSpec = null;
				for ( final ModuleItem< ? > item : info.outputs() )
				{
					if ( !Feature.class.isAssignableFrom( item.getType() ) )
						throw new IllegalArgumentException( "Ignoring FeatureComputer " + info.getClassName()
								+ " because output " + item + " is not of type Feature." );

					if ( featureSpec != null )
						throw new IllegalArgumentException( "Ignoring FeatureComputer " + info.getClassName()
								+ " because it defines more than one output." );

					@SuppressWarnings( "unchecked" )
					final Class< ? extends Feature< ? > > type = ( Class< ? extends Feature< ? > > ) item.getType();
					featureSpec = featureSpecs.getSpec( type );
				}
				if ( featureSpec == null )
					throw new IllegalArgumentException( "Ignoring FeatureComputer " + info.getClassName()
							+ " because it does not define an output." );

				FeatureDependencyGraph.Vertex v = dependencies.get( featureSpec );
				if ( v == null )
					v = dependencies.addVertex( featureSpec );
				if ( v.getFeatureComputer() != null )
					throw new IllegalArgumentException( "Ignoring FeatureComputer " + info.getClassName()
							+ " because it computes " + featureSpec + " which is already computed by " + v.getFeatureComputer() );
				v.setFeatureComputer( fc, info );

				for ( final ModuleItem< ? > item : info.inputs() )
				{
					if ( Feature.class.isAssignableFrom( item.getType() ) )
					{
						@SuppressWarnings( "unchecked" )
						final Class< ? extends Feature< ? > > type = ( Class< ? extends Feature< ? > > ) item.getType();
						@SuppressWarnings( "rawtypes" )
						final FeatureSpec dependencySpec = featureSpecs.getSpec( type );
						FeatureDependencyGraph.Vertex dependency = dependencies.get( dependencySpec );
						if ( dependency == null )
							dependency = dependencies.addVertex( dependencySpec );
						dependencies.addEdge( v, dependency );
					}
				}
			}
			catch ( InstantiableException | IllegalArgumentException e )
			{
				/*
				 * TODO: instead of printing the messages, they should be
				 * collected into one big message that can then be obtained from
				 * the FeatureComputerService and presented to the user in some
				 * way (the "presenting to the user" part we can decide on
				 * later...).
				 */
				System.out.println( e.getMessage() );
			}
		}
	}

	@Override
	public FeatureComputer getFeatureComputerFor( final FeatureSpec< ?, ? > spec )
	{
		return null == dependencies.get( spec )
				? null
				: dependencies.get( spec ).getFeatureComputer();
	}

	/**
	 * Removes vertices on cycles.
	 *
	 * @param dependencies
	 *                         the dependency graph.
	 */
	public static void removeCycles( final FeatureDependencyGraph dependencies )
	{
		final Set< RefSet< FeatureDependencyGraph.Vertex > > sccs = stronglyConnectedComponents( dependencies );
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
					dependencies.remove( vertex );
		}
	}

	/**
	 * Removes vertices whose feature cannot be computed. A Feature is incomputable
	 * if
	 * <ul>
	 * <li>no {@link FeatureComputer} was found to compute it, or</li>
	 * <li>it depends on an incomputable feature.</li>
	 * </ul>
	 *
	 * @param dependencies
	 *                         the dependency graph.
	 */
	public static void removeMissing( final FeatureDependencyGraph dependencies )
	{
		final RefSet< FeatureDependencyGraph.Vertex > missing = RefCollections.createRefSet( dependencies.vertices() );
		for ( final FeatureDependencyGraph.Vertex vertex : dependencies.vertices() )
			if ( vertex.getFeatureComputer() == null )
				missing.add( vertex );
		for ( final FeatureDependencyGraph.Vertex vertex : ancestors( dependencies, missing ) )
			dependencies.remove( vertex );
	}

	/*
	 *
	 * Partial dependency graphs for given feature keys or FeatureSpecs
	 *
	 */

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

			private FeatureDependencyGraph.Vertex vertex( final FeatureSpec< ?, ? > spec )
			{
				FeatureDependencyGraph.Vertex vertex = graph.get( spec );
				if ( vertex == null )
				{
					final FeatureDependencyGraph.Vertex dep = dependencies.get( spec );
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

	@Override
	public Map< FeatureSpec< ?, ? >, Feature< ? > > compute( final Collection< String > featureKeys )
	{
		cancelReason = null;
		final List< FeatureSpec< ?, ? > > specs = new ArrayList<>();
		for ( final String key : featureKeys )
		{
			// Do we have specs for this feature key?
			final FeatureSpec< ?, ? > spec = featureSpecs.getSpec( key );
			if ( null == spec )
			{
				System.err.println( "Unknown feature key: " + key + ". Skipping." );
				continue;
			}

			// Did we discover a feature computer for this feature spec?
			if ( null == dependencies.get( spec ) )
			{
				System.err.println( "No feature computer for feature key: " + key + ". Skipping." );
				continue;
			}

			specs.add( spec );
		}
		final FeatureDependencyGraph dependencyGraph = dependencyGraphFor( specs );
		final RefList< FeatureDependencyGraph.Vertex > sequence = new TopologicalSort<>( dependencyGraph ).get();

		final Map< FeatureSpec< ?, ? >, Feature< ? > > featureModel = new HashMap<>();
		for ( final FeatureDependencyGraph.Vertex vertex : sequence )
		{
			if ( isCanceled() )
				break;

			currentFeatureComputer = vertex.getFeatureComputer();
			final CommandInfo info = vertex.getFeatureComputerInfo();
			final CommandModule module = new CommandModule( info, currentFeatureComputer );
			for ( final ModuleItem< ? > item : info.inputs() )
			{
				final Class< ? > klass = item.getType();
				provideParameters( item, module, klass, featureModel );
			}

			propertyChangeListener.propertyChange(  new PropertyChangeEvent(
					this,
					"status",
					"",
					vertex.getFeatureSpec().getKey() ) );
			currentFeatureComputer.createOutput();
			currentFeatureComputer.run();

			final Feature< ? > output = ( Feature< ? > ) info.outputs().iterator().next().getValue( module );
			featureModel.put( vertex.getFeatureSpec(), output );
		}

		currentFeatureComputer = null;
		propertyChangeListener.propertyChange(  new PropertyChangeEvent(
				this,
				"clear",
				"",
				"" ) );
		return ( featureModel );
	}

	/**
	 * Try to set a value for the specified {@link ModuleItem} that is a parameter
	 * of the {@link FeatureComputer} described by the specified
	 * {@link CommandModule}.
	 * <p>
	 * An error is printed if this {@link DefaultFeatureComputerService} cannot assign a
	 * value for the specified class of input.
	 *
	 * @param item
	 *                           the parameter in the feature computer to set value
	 *                           of.
	 * @param module
	 *                           the command module representing the feature
	 *                           computer.
	 * @param parameterClass
	 *                           the class of the parameter to set.
	 * @param featureModel
	 *                           the map of already computed features to potentially
	 *                           serve as parameter.
	 */
	protected void provideParameters( final ModuleItem< ? > item, final CommandModule module, final Class< ? > parameterClass, final Map< FeatureSpec< ?, ? >, Feature< ? > > featureModel )
	{
		// Feature dependencies.
		if ( Feature.class.isAssignableFrom( parameterClass ) )
		{
			@SuppressWarnings( "unchecked" )
			final Class< ? extends Feature< ? > > featureClass = ( Class< ? extends Feature< ? > > ) parameterClass;
			@SuppressWarnings( "unchecked" )
			final ModuleItem< Feature< ? > > featureItem = ( ModuleItem< Feature< ? > > ) item;
			featureItem.setValue( module, featureModel.get( featureSpecs.getSpec( featureClass ) ) );
			return;
		}

		// PropertyChangeListener.
		if (PropertyChangeListener.class.isAssignableFrom( parameterClass ))
		{
			@SuppressWarnings( "unchecked" )
			final ModuleItem< PropertyChangeListener > propModule = ( ModuleItem< PropertyChangeListener > ) item;
			propModule.setValue( module, propertyChangeListener );
			return;
		}

		System.err.println( "Unknown FeatureComputer input @Parameter " + item + " in " + module.getCommand() );
	}

	/**
	 * Sets the {@link PropertyChangeListener} that will be notified of feature
	 * computation progress by feature computer that depend on it.
	 *
	 * @param propertyChangeListener
	 *            the {@link PropertyChangeListener}.
	 */
	public void setPropertyChangeListener( final PropertyChangeListener propertyChangeListener )
	{
		this.propertyChangeListener = propertyChangeListener;
	}

	@Override
	public boolean isCanceled()
	{
		return null != cancelReason;
	}

	@Override
	public void cancel( final String reason )
	{
		this.cancelReason = reason;
		if ( currentFeatureComputer instanceof Cancelable )
			( ( Cancelable ) currentFeatureComputer ).cancel( reason );
	}

	@Override
	public String getCancelReason()
	{
		return cancelReason;
	}

}
