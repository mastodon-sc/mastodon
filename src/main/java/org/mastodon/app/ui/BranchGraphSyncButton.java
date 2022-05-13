package org.mastodon.app.ui;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.mastodon.app.MastodonIcons;
import org.mastodon.mamut.model.branch.BranchGraphSynchronizer;
import org.mastodon.mamut.model.branch.BranchGraphSynchronizer.UpdateListener;

public class BranchGraphSyncButton extends JPanel
{

	private static final long serialVersionUID = 1L;

	public BranchGraphSyncButton( final BranchGraphSynchronizer model )
	{
		final JButton btn = new JButton();
		final UpdateListener l = () -> btn.setIcon( model.isUptodate()
				? MastodonIcons.UP_TO_DATE_ICON
				: MastodonIcons.NOT_UP_TO_DATE_ICON );
		model.updateListeners().add( l );
		l.branchGraphSyncChanged();
		btn.addActionListener( e -> new Thread( () -> model.sync(), "Branch-graph synchronizer" ).start() );
		add( btn );
		btn.setText( "<html>Regen.<br>branch-graph</html>" );
		btn.setFont( new Font( "arial", Font.PLAIN, 8 ) );
	}
}
