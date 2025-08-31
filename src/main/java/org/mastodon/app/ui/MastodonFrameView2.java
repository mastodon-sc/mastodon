package org.mastodon.app.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.ViewMenuBuilder.JMenuHandle;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.model.HasBranchModel;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.ui.TagSetMenu;
import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.ui.coloring.ColorBarOverlayMenu;
import org.mastodon.ui.coloring.ColoringMenu;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.ui.coloring.ColoringModel.ColoringChangedListener;
import org.mastodon.ui.coloring.ColoringModelMain;
import org.mastodon.ui.coloring.DefaultColoringModel;
import org.mastodon.ui.coloring.GraphColorGenerator;
import org.mastodon.ui.coloring.GraphColorGeneratorAdapter;
import org.mastodon.ui.coloring.TagSetGraphColorGenerator;
import org.mastodon.ui.coloring.TrackGraphColorGenerator;
import org.mastodon.ui.coloring.feature.FeatureColorModeManager;
import org.mastodon.undo.UndoPointMarker;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.Behaviours;
import org.scijava.ui.behaviour.util.WrappedActionMap;
import org.scijava.ui.behaviour.util.WrappedInputMap;

import bdv.ui.keymap.Keymap;
import bdv.ui.keymap.Keymap.UpdateListener;

/**
 * Base class for views of a {@link MastodonModel} that have a frame.
 * 
 * @param <M>
 *            the type of the mastodon model.
 * @param <VG>
 *            the type of the view-graph.
 * @param <MV>
 *            the type of vertices in the mastodon model.
 * @param <ME>
 *            the type of edges in the mastodon model.
 * @param <V>
 *            the type of vertices in the view-graph.
 * @param <E>
 *            the type of edges in the view-graph.
 */
public class MastodonFrameView2<
			M extends MastodonModel< ?, MV, ME >,
			VG extends ViewGraph< MV, ME, V, E >,
			MV extends Vertex< ME >, 
			ME extends Edge< MV >,
			V extends Vertex< E >,
			E extends Edge< V > >
		extends MastodonView2< M, VG, MV, ME, V, E >
		implements HasFrame
{

	protected ViewFrame frame;

	protected final String[] keyConfigContexts;

	protected Actions viewActions;

	protected Behaviours viewBehaviours;

	public MastodonFrameView2(
			final M dataModel,
			final UIModel uiModel,
			final VG viewGraph,
			final String[] keyConfigContexts )
	{
		super( dataModel, uiModel, viewGraph );

		final Set< String > c = new LinkedHashSet<>( Arrays.asList( uiModel.getKeyConfigContexts() ) );
		c.addAll( Arrays.asList( keyConfigContexts ) );
		this.keyConfigContexts = c.toArray( new String[] {} );
	}

	@Override
	public ViewFrame getFrame()
	{
		return frame;
	}

	protected void setFrame( final ViewFrame frame )
	{
		frame.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				close();
			}
		} );
		this.frame = frame;

		final Actions projectActions = uiModel.getProjectActions();
		if ( projectActions != null )
		{
			frame.keybindings.addActionMap( "project", new WrappedActionMap( projectActions.getActionMap() ) );
			frame.keybindings.addInputMap( "project", new WrappedInputMap( projectActions.getInputMap() ) );
		}

		final Actions pluginActions = uiModel.getPlugins().getPluginActions();
		if ( pluginActions != null )
		{
			frame.keybindings.addActionMap( "plugin", new WrappedActionMap( pluginActions.getActionMap() ) );
			frame.keybindings.addInputMap( "plugin", new WrappedInputMap( pluginActions.getInputMap() ) );
		}

		final Actions modelActions = uiModel.getModelActions();
		frame.keybindings.addActionMap( "model", new WrappedActionMap( modelActions.getActionMap() ) );
		frame.keybindings.addInputMap( "model", new WrappedInputMap( modelActions.getInputMap() ) );

		final Keymap keymap = uiModel.getKeymap();

		viewActions = new Actions( keymap.getConfig(), keyConfigContexts );
		viewActions.install( frame.keybindings, "view" );

		viewBehaviours = new Behaviours( keymap.getConfig(), keyConfigContexts );
		viewBehaviours.install( frame.triggerbindings, "view" );

		final UpdateListener updateListener = () -> {
			viewBehaviours.updateKeyConfig( keymap.getConfig() );
			viewActions.updateKeyConfig( keymap.getConfig() );
		};
		keymap.updateListeners().add( updateListener );
		onClose( () -> keymap.updateListeners().remove( updateListener ) );
	}

	/*
	 * Coloring methods.
	 *
	 * Since they require a JMenuHandle, their place is here.
	 */

	/**
	 * Registers a {@link ColorBarOverlayMenu} in the specified menu handle, and
	 * links it to the specified {@link ColorBarOverlay}.
	 *
	 * @param colorBarOverlay
	 *            the {@link ColorBarOverlay} to link to the menu.
	 * @param menuHandle
	 *            the menu handle where to create the menu.
	 * @param refresh
	 *            a {@link Runnable} that refreshes the view when the color bar
	 *            visibility is modified.
	 */
	protected void registerColorbarOverlay(
			final ColorBarOverlay colorBarOverlay,
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		final ColorBarOverlayMenu menu = new ColorBarOverlayMenu( menuHandle.getMenu(), colorBarOverlay, refresh );
		colorBarOverlay.listeners().add( menu );
	}

	/**
	 * Registers a {@link TagSetMenu} in the specified menu handle, if the data
	 * model supports it. Otherwise does nothing.
	 *
	 * @param menuHandle
	 *            the
	 * @param refresh
	 *            a {@link Runnable} that refreshes the view when a tag is set.
	 */
	protected void registerTagSetMenu(
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		if ( dataModel instanceof UndoPointMarker )
		{
			final UndoPointMarker undo = ( UndoPointMarker ) dataModel;
			final SelectionModel< MV, ME > selectionModel = dataModel.getSelectionModel();
			final TagSetModel< MV, ME > tagSetModel = dataModel.getTagSetModel();
			final TagSetMenu< MV, ME > tagSetMenu = new TagSetMenu< MV, ME >( menuHandle.getMenu(), tagSetModel, selectionModel,
					dataModel.getLock(), undo, refresh );
			tagSetModel.listeners().add( tagSetMenu );
			onClose( () -> tagSetModel.listeners().remove( tagSetMenu ) );
		}
	}

	/**
	 * Sets up and registers the coloring menu item and related actions and
	 * listeners. A new instance of the {@code ColoringModel} is created here
	 * and a reference on it is returned. This instance is bound to all relevant
	 * actions and is therefore knowledgeable of the currently used coloring
	 * style.
	 * <p>
	 * A different implementation of {@code ColoringModel} is created depending
	 * on whether the data model has a branch graph or not.
	 *
	 * @param colorGeneratorAdapter
	 *            adapts a (modifiable) model coloring to view vertices/edges.
	 * @param menuHandle
	 *            handle to the JMenu corresponding to the coloring submenu.
	 *            Coloring options will be installed here.
	 * @param refresh
	 *            triggers repaint of the graph (called when coloring changes)
	 *
	 * @return reference on the underlying {@code ColoringModel}
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	protected ColoringModel registerColoring(
			final GraphColorGeneratorAdapter< MV, ME, V, E > colorGeneratorAdapter,
			final JMenuHandle menuHandle,
			final Runnable refresh )
	{
		final TagSetModel< MV, ME > tagSetModel = dataModel.getTagSetModel();
		final FeatureColorModeManager featureColorModeManager = uiModel.getInstance( FeatureColorModeManager.class );
		final FeatureModel featureModel = dataModel.getFeatureModel();

		final ColoringModel coloringModel;
		if ( dataModel instanceof HasBranchModel )
		{
			final HasBranchModel bm = ( HasBranchModel ) dataModel;
			final MastodonModel branchModel = bm.branchModel();
			final BranchGraph branchGraph = ( BranchGraph ) branchModel.getGraph();
			coloringModel = new ColoringModelMain( tagSetModel, featureColorModeManager, featureModel, branchGraph );
		}
		else
		{
			coloringModel = new DefaultColoringModel<>( tagSetModel, featureColorModeManager, featureModel );
		}

		final ColoringMenu coloringMenu = new ColoringMenu( menuHandle.getMenu(), coloringModel );
		tagSetModel.listeners().add( coloringModel );
		onClose( () -> tagSetModel.listeners().remove( coloringModel ) );
		tagSetModel.listeners().add( coloringMenu );
		onClose( () -> tagSetModel.listeners().remove( coloringMenu ) );

		featureColorModeManager.listeners().add( coloringModel );
		onClose( () -> featureColorModeManager.listeners().remove( coloringModel ) );
		featureColorModeManager.listeners().add( coloringMenu );
		onClose( () -> featureColorModeManager.listeners().remove( coloringMenu ) );

		featureModel.listeners().add( coloringMenu );
		onClose( () -> featureModel.listeners().remove( coloringMenu ) );

		final ColoringChangedListener coloringChangedListener = () -> {
			final GraphColorGenerator< MV, ME > colorGenerator;
			switch ( coloringModel.getColoringStyle() )
			{
			case BY_FEATURE:
				colorGenerator = ( GraphColorGenerator< MV, ME > ) coloringModel.getFeatureGraphColorGenerator();
				break;
			case BY_TAGSET:
				colorGenerator = new TagSetGraphColorGenerator<>( tagSetModel, coloringModel.getTagSet() );
				break;
			case BY_TRACK:
				final TrackGraphColorGenerator< MV, ME > tgcg = uiModel.getInstance( TrackGraphColorGenerator.class );
				colorGenerator = tgcg;
				break;
			case NONE:
				colorGenerator = null;
				break;
			default:
				throw new IllegalArgumentException( "Unknown coloring style: " + coloringModel.getColoringStyle() );
			}
			colorGeneratorAdapter.setColorGenerator( colorGenerator );
			refresh.run();
		};
		coloringModel.listeners().add( coloringChangedListener );

		return coloringModel;
	}
}
