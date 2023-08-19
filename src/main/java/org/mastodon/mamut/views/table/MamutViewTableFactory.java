package org.mastodon.mamut.views.table;

import static org.mastodon.mamut.views.MamutBranchView.BRANCH_GRAPH;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JViewport;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.mamut.views.MamutViewFactory;
import org.mastodon.ui.coloring.ColoringModel;
import org.mastodon.views.table.FeatureTagTablePanel;
import org.mastodon.views.table.TableViewFrameBuilder.MyTableViewFrame;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutViewFactory.class )
public class MamutViewTableFactory extends AbstractMamutViewFactory< MamutViewTable >
{

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
	 * is a <code>List<Map<String, Object>></code>.
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

	@Override
	public MamutViewTable create( final ProjectModel projectModel )
	{
		return new MamutViewTable( projectModel );
	}

	@Override
	public Map< String, Object > getGuiState( final MamutViewTable view )
	{
		final Map< String, Object > guiState = super.getGuiState( view );
		getGuiStateTable( view, guiState );
		return guiState;
	}

	static void getGuiStateTable( final MamutViewTable view, final Map< String, Object > guiState )
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
	public void restoreGuiState( final MamutViewTable view, final Map< String, Object > guiState )
	{
		super.restoreGuiState( view, guiState );
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
}
