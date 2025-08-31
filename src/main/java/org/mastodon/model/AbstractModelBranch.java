package org.mastodon.model;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.branch.BranchGraphImp;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableVertex;
import org.mastodon.model.branch.BranchGraphFocusAdapter;
import org.mastodon.model.branch.BranchGraphHighlightAdapter;
import org.mastodon.model.branch.BranchGraphSelectionAdapter;
import org.mastodon.model.branch.BranchGraphTagSetAdapter;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.spatial.SpatioTemporalIndex;
import org.mastodon.spatial.SpatioTemporalIndexImp;

import net.imglib2.RealLocalizable;

/**
 * Base class for models that have a branch view of the core graph they manage.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <MG>
 *            the type of the core model graph.
 * @param <V>
 *            the type of vertices in the core model graph. Must be an
 *            AbstractSpot.
 * @param <E>
 *            the type of edges in the core model graph.
 * @param <BG>
 *            the type of the branch graph.
 * @param <BV>
 *            the type of vertices in the branch graph. Must have a timepoint
 *            and be a RealLocalizable.
 * @param <BE>
 *            the type of edges in the branch graph.
 */
public abstract class AbstractModelBranch<
		MG extends AbstractModelGraph< MG, ?, ?, V, E, ? >,
		V extends AbstractSpot< V, E, ?, ?, MG >,
		E extends AbstractListenableEdge< E, V, ?, ? >,
		BG extends BranchGraphImp< V, E, BV, BE, ?, ?, ? > ,
		BV extends AbstractListenableVertex< BV, BE, ?, ? > & HasTimepoint & RealLocalizable,
		BE extends AbstractListenableEdge< BE, BV, ?, ? >
> extends AbstractModel< MG, V, E > implements HasBranchModel< BG, BV, BE >
{

	private final BG branchGraph;

	private final BranchModel branchModel;

	protected AbstractModelBranch( final MG modelGraph, final BG branchGraph, final String spaceUnits, final String timeUnits )
	{
		super( modelGraph, spaceUnits, timeUnits );
		this.branchGraph = branchGraph;
		this.branchModel = new BranchModel();
	}

	@Override
	public BranchModel branchModel()
	{
		return branchModel;
	}

	public class BranchModel implements MastodonModel< BG, BV, BE >
	{

		private final TagSetModel< BV, BE > branchGraphTagSetModel;

		private final SpatioTemporalIndexImp< BV, BE > branchIndex;

		private final FocusModel< BV > branchFocusfocusModel;

		private final SelectionModel< BV, BE > branchSelectionModel;

		private final HighlightModel< BV, BE > branchHighlightModel;

		protected BranchModel()
		{
			this.branchIndex = new SpatioTemporalIndexImp< BV, BE >( branchGraph, branchGraph.getGraphIdBimap().vertexIdBimap() );
			final MG graph = AbstractModelBranch.super.getGraph();
			this.branchGraphTagSetModel = new BranchGraphTagSetAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), tagSetModel );
			this.branchFocusfocusModel = new BranchGraphFocusAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), focusModel );
			this.branchSelectionModel = new BranchGraphSelectionAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), selectionModel );
			this.branchHighlightModel = new BranchGraphHighlightAdapter<>( branchGraph, graph, graph.getGraphIdBimap(), highlightModel );
		}

		/**
		 * Exposes the tag set model of the branch graph of this model.
		 * <p>
		 * This tag set model is synchronized with the tag set model of the core
		 * model.
		 *
		 * @return the branch graph tag set model.
		 */
		@Override
		public TagSetModel< BV, BE > getTagSetModel()
		{
			return branchGraphTagSetModel;
		}

		/**
		 * Exposes the bidirectional map between branch vertices and their id,
		 * and between branch edges and their id.
		 *
		 * @return the bidirectional id map.
		 */
		@Override
		public GraphIdBimap< BV, BE > getGraphIdBimap()
		{
			return branchGraph.getGraphIdBimap();
		}

		@Override
		public BG getGraph()
		{
			return branchGraph;
		}

		/**
		 * Exposes the spatio-temporal index of branch graph of this model.
		 *
		 * @return the spatio-temporal index.
		 */
		@Override
		public SpatioTemporalIndex< BV > getSpatioTemporalIndex()
		{
			return branchIndex;
		}

		@Override
		public HighlightModel< BV, BE > getHighlightModel()
		{
			return branchHighlightModel;
		}

		@Override
		public FocusModel< BV > getFocusModel()
		{
			return branchFocusfocusModel;
		}

		@Override
		public SelectionModel< BV, BE > getSelectionModel()
		{
			return branchSelectionModel;
		}
	}
}
