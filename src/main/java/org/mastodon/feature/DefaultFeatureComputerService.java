package org.mastodon.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mastodon.collection.RefList;
import org.mastodon.feature.FeatureDependencyGraph.Edge;
import org.mastodon.feature.FeatureDependencyGraph.Vertex;
import org.mastodon.graph.algorithm.TopologicalSort;
import org.scijava.Cancelable;
import org.scijava.InstantiableException;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.listeners.Listeners;
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

	// FeatureComputer type discovered and managed by this service
	private final Class< ? extends FeatureComputer > klass;

	private final FeatureDependencyGraph dependencies = new FeatureDependencyGraph();

	private final FeatureComputationStatus status = new FeatureComputationStatus();

	private String cancelReason;

	private FeatureComputer currentFeatureComputer;

	public DefaultFeatureComputerService()
	{
		this( FeatureComputer.class );
	}

	/**
	 * @param klass
	 *            the type of {}@code FeatureComputer}s that should be
	 *            discovered and managed by this {@code FeatureComputerService}.
	 */
	protected DefaultFeatureComputerService( final Class< ? extends FeatureComputer > klass )
	{
		this.klass = klass;
	}

	@Override
	public void initialize()
	{
		dependencies.clear();

		/*
		 * Build the dependency graph. Potentially with cycles and potentially
		 * with features whose computers are missing.
		 */
		discover();

		// Post-process dependency graph.
		// 1.) remove cycles
		dependencies.removeCycles();
		// 2.) recursively remove vertices with missing inputs or featurecomputers
		dependencies.removeIncomputable();
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

	@Override
	public Set< FeatureSpec< ?, ? > > getFeatureSpecs()
	{
		return dependencies.getFeatureSpecs();
	}

	@Override
	public Collection< FeatureSpec< ?, ? > > getDependencies( final FeatureSpec< ?, ? > spec )
	{
		final Vertex vertex = dependencies.get( spec );
		if (null == vertex)
			return Collections.emptyList();

		final List<FeatureSpec< ?, ? >> deps = new ArrayList<>();
		for ( final Edge edge : vertex.outgoingEdges() )
		{
			final Vertex target = edge.getTarget();
			deps.add( target.getFeatureSpec() );
			deps.addAll( getDependencies( target.getFeatureSpec() ) );
		}
		return deps;
	}

	@Override
	public Map< FeatureSpec< ?, ? >, Feature< ? > > compute( final boolean forceComputeAll, final Collection< FeatureSpec< ?, ? > > featureKeys )
	{
		cancelReason = null;
		final List< FeatureSpec< ?, ? > > specs = new ArrayList<>();
		for ( final FeatureSpec< ?, ? > spec : featureKeys )
		{
			// Did we discover a feature computer for this feature spec?
			if ( !dependencies.contains( spec ) )
			{
				System.err.println( "No feature computer for feature: " + spec + ". Skipping." );
				continue;
			}

			specs.add( spec );
		}
		final FeatureDependencyGraph dependencyGraph = dependencies.subGraphFor( specs );
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

			status.notifyStatus( vertex.getFeatureSpec().getKey() );
			currentFeatureComputer.createOutput();
			currentFeatureComputer.run();

			final Feature< ? > output = ( Feature< ? > ) info.outputs().iterator().next().getValue( module );
			featureModel.put( vertex.getFeatureSpec(), output );
		}

		currentFeatureComputer = null;
		status.notifyClear();
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

		// FeatureComputationStatus.
		if (FeatureComputationStatus.class.isAssignableFrom( parameterClass ))
		{
			@SuppressWarnings( "unchecked" )
			final ModuleItem< FeatureComputationStatus > statusModule = ( ModuleItem< FeatureComputationStatus > ) item;
			statusModule.setValue( module, status );
			return;
		}

		System.err.println( "Unknown FeatureComputer input @Parameter " + item + " in " + module.getCommand() );
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

	/**
	 * {@code FeatureComputationStatusListener}s added here will be notified
	 * about progress of computation.
	 *
	 * @return the listeners.
	 */
	public Listeners< FeatureComputationStatusListener > computationStatusListeners()
	{
		return status.listeners;
	}

	/*
	 *
	 * Reporting computation status
	 *
	 */

	public interface FeatureComputationStatusListener
	{
		void status( final String status );

		void progress( final double progress );

		void clear();
	}

	public static class FeatureComputationStatus
	{
		private final Listeners.List< FeatureComputationStatusListener > listeners;

		FeatureComputationStatus()
		{
			listeners = new Listeners.SynchronizedList<>();
		}

		public void notifyStatus( final String status )
		{
			listeners.list.forEach( l -> l.status( status ) );
		}

		/**
		 * @param progress computation progress as a number between 0 and 1
		 */
		public void notifyProgress( final double progress )
		{
			listeners.list.forEach( l -> l.progress( progress ) );
		}

		public void notifyClear()
		{
			listeners.list.forEach( l -> l.clear() );
		}
	}
}
