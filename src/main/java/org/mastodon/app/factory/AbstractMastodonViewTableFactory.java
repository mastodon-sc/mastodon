/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.app.factory;

import static org.mastodon.mamut.views.MamutBranchView.BRANCH_GRAPH;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JViewport;

import org.mastodon.app.AppModel;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.mamut.views.MamutViewFactory;
import org.mastodon.mamut.views.table.MastodonViewTable2;
import org.mastodon.mamut.views.table.TableModelGraphProperties;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.views.table.FeatureTagTablePanel;
import org.mastodon.views.table.TableViewFrameBuilder.MyTableViewFrame;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

/**
 * Factory to create and display Tables.
 * <p>
 * The GUI state is specified as a map of strings to objects. The accepted key
 * and value types are:
 * <ul>
 * <li><code>'FramePosition'</code> &rarr; an <code>int[]</code> array of 4
 * elements: x, y, width and height.
 * <li><code>'LockGroupId'</code> &rarr; an integer that specifies the lock
 * group id.
 * <li><code>'SettingsPanelVisible'</code> &rarr; a boolean that specifies
 * whether the settings panel is visible on this view.
 * <li><code>'NoColoring'</code> &rarr; a boolean; if <code>true</code>, the
 * feature or tag coloring will be ignored.
 * <li><code>'TagSet'</code> &rarr; a string specifying the name of the tag-set
 * to use for coloring. If not <code>null</code>, the coloring will be done
 * using the tag-set.
 * <li><code>'FeatureColorMode'</code> &rarr; a @link String specifying the name
 * of the feature color mode to use for coloring. If not <code>null</code>, the
 * coloring will be done using the feature color mode.
 * <li><code>'ColorbarVisible'</code> &rarr; a boolean specifying whether the
 * colorbar is visible for tag-set and feature-based coloring.
 * <li><code>'ColorbarPosition'</code> &rarr; a {@link Position} specifying the
 * position of the colorbar.
 * </ul>
 */
@Plugin( type = MamutViewFactory.class, priority = Priority.NORMAL - 2 )
public abstract class AbstractMastodonViewTableFactory<
			T extends MastodonViewTable2< ?, ?, ?, ? >,
			G extends ListenableReadOnlyGraph< ?, ? >,
			AM extends AppModel< AM, ?, G, ?, ? > >
		extends AbstractMastodonViewFactory< T, AM >
		implements MastodonViewFactory< T, AM >
{

	public static final String NEW_TABLE_VIEW = "new full table view";

	/**
	 * Key that specifies whether a table is currently showing the vertex table.
	 * If <code>false</code>, then the edge table is displayed.
	 */
	public static final String TABLE_DISPLAYING_VERTEX_TABLE = "TableVertexTableDisplayed";

	/**
	 * Key that specifies what table is currently showing in the table view.
	 * Values are <code>String</code> that points to a tab name in the tabbed
	 * pane.
	 */
	public static final String TABLE_DISPLAYED = "TableDisplayed";

	/**
	 * Key to the parameter that stores the vertex table displayed rectangle.
	 * Value is and <code>int[]</code> array of 4 elements: x, y, width and
	 * height.
	 */
	public static final String TABLE_VERTEX_TABLE_VISIBLE_POS = "TableVertexTableVisibleRect";

	/**
	 * Key to the parameter that stores the table displayed position. Value is
	 * and <code>int[]</code> array of 2 elements: x, y.
	 */
	public static final String TABLE_VISIBLE_POS = "TableVisibleRect";

	/**
	 * Key to the parameter that stores the GUI states of multiple tables. Value
	 * is a <code>List&lt;Map&lt;String, Object&gt;&gt;</code>.
	 */
	public static final String TABLE_ELEMENT = "Tables";

	/**
	 * Key to the parameter that stores the table name in a table GUI state.
	 * Value is a <code>String</code>.
	 */
	public static final String TABLE_NAME = "TableName";

	/**
	 * Key to the parameter that stores the edge table displayed rectangle.
	 * Value is and <code>int[]</code> array of 4 elements: x, y, width and
	 * height.
	 */
	public static final String TABLE_EDGE_TABLE_VISIBLE_POS = "TableEdgeTableVisibleRect";

	protected abstract TableModelGraphProperties< ? > getModelGraphProperties( G graph );

	@Override
	public Map< String, Object > getGuiState( final T view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		getGuiStateTable( view, guiState );
		return guiState;
	}

	static < T extends MastodonViewTable2< ?, ?, ?, ? > > void getGuiStateTable( final T view, final Map< String, Object > guiState )
	{
		// Currently displayed table.
		final FeatureTagTablePanel< ? > currentlyDisplayedTable = view.getFrame().getCurrentlyDisplayedTable();
		String displayedTableName = "";

		// Table visible rectangles.
		final List< FeatureTagTablePanel< ? > > tables = view.getFrame().getTables();
		final List< String > names = view.getFrame().getTableNames();
		final List< Map< String, Object > > tableGuiStates = new ArrayList<>( names.size() );
		for ( int i = 0; i < names.size(); i++ )
		{
			final String name = names.get( i );
			final FeatureTagTablePanel< ? > table = tables.get( i );

			if ( table == currentlyDisplayedTable )
				displayedTableName = name;

			final JViewport viewportVertex = table.getScrollPane().getViewport();
			final Point tableRect = viewportVertex.getViewPosition();

			final LinkedHashMap< String, Object > tableGuiState = new LinkedHashMap<>();
			tableGuiState.put( TABLE_NAME, name );
			tableGuiState.put( TABLE_VISIBLE_POS, new int[] {
					tableRect.x,
					tableRect.y } );

			tableGuiStates.add( tableGuiState );
		}
		guiState.put( TABLE_ELEMENT, tableGuiStates );
		guiState.put( TABLE_DISPLAYED, displayedTableName );

		// Coloring for core graph.
		final ColoringModel coloringModel = view.getColoringModel();
		getColoringState( coloringModel, guiState );

		// Coloring for branch-graph.
		final ColoringModel branchColoringModel = view.getBranchColoringModel();
		final Map< String, Object > branchGraphMap = new HashMap<>();
		getColoringState( branchColoringModel, branchGraphMap );
		guiState.put( BRANCH_GRAPH, branchGraphMap );
	}

	@Override
	public void restoreGuiState( final T view, final Map< String, Object > guiState )
	{
		super.restoreGuiState( view, guiState );
		restoreGuiStateTable( view, guiState );
	}

	static final < T extends MastodonViewTable2< ?, ?, ?, ? > > void restoreGuiStateTable( final T view, final Map< String, Object > guiState )
	{
		// Restore branch-graph coloring.
		@SuppressWarnings( "unchecked" )
		final Map< String, Object > branchGraphGuiState = ( Map< String, Object > ) guiState.getOrDefault( BRANCH_GRAPH, Collections.EMPTY_MAP );
		restoreColoringModel( view.getBranchColoringModel(), branchGraphGuiState );

		// Restore table visible rectangle and displayed table.
		final MyTableViewFrame frame = view.getFrame();
		final String displayedTableName = ( String ) guiState.getOrDefault( TABLE_DISPLAYED, "TableSpot" );
		final List< FeatureTagTablePanel< ? > > tables = frame.getTables();
		final List< String > names = frame.getTableNames();
		@SuppressWarnings( "unchecked" )
		final List< Map< String, Object > > list =
				( List< Map< String, Object > > ) guiState.getOrDefault( TABLE_ELEMENT, Collections.emptyList() );
		for ( int i = 0; i < list.size(); i++ )
		{
			final String name = names.get( i );
			if ( name.equals( displayedTableName ) )
				frame.displayTable( name );

			final Map< String, Object > tableGuiState = list.get( i );
			final int[] viewPos = ( int[] ) tableGuiState.get( TABLE_VISIBLE_POS );
			if ( viewPos != null )
			{
				final FeatureTagTablePanel< ? > table = tables.get( i );
				table.getScrollPane().getViewport().setViewPosition( new Point(
						viewPos[ 0 ],
						viewPos[ 1 ] ) );
			}
		}
	}

	@Override
	public String getCommandName()
	{
		return NEW_TABLE_VIEW;
	}

	@Override
	public String getCommandDescription()
	{
		return "Open a new table view.";
	}

	@Override
	public String getCommandMenuText()
	{
		return "New Data table";
	}
}
