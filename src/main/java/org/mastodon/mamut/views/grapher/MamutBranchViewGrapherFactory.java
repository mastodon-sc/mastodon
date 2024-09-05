package org.mastodon.mamut.views.grapher;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.views.AbstractMamutViewFactory;
import org.mastodon.mamut.views.MamutViewFactory;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

@Plugin( type = MamutViewFactory.class, priority = Priority.NORMAL - 8 )
public class MamutBranchViewGrapherFactory extends AbstractMamutViewFactory< MamutBranchViewGrapher >
{
	public static final String NEW_GRAPHER_BRANCH_VIEW = "new grapher branch view";

	@Override
	public MamutBranchViewGrapher create( final ProjectModel projectModel )
	{
		return new MamutBranchViewGrapher( projectModel );
	}

	@Override
	public String getCommandName()
	{
		return NEW_GRAPHER_BRANCH_VIEW;
	}

	@Override
	public String getCommandDescription()
	{
		return "Open a new Grapher Branch view.";
	}

	@Override
	public String getCommandMenuText()
	{
		return "New Grapher Branch View";
	}

	@Override
	public Class< MamutBranchViewGrapher > getViewClass()
	{
		return MamutBranchViewGrapher.class;
	}
}
