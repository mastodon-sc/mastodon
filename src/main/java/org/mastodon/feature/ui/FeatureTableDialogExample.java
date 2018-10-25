package org.mastodon.feature.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class FeatureTableDialogExample
{
	static class MyElement
	{
		private final String name;

		private boolean selected;

		private final boolean uptodate;

		public MyElement( final String name, final boolean selected, final boolean uptodate )
		{
			this.name = name;
			this.selected = selected;
			this.uptodate = uptodate;
		}

		public String getName()
		{
			return name;
		}

		public boolean isSelected()
		{
			return selected;
		}

		public void setSelected( final boolean selected )
		{
			this.selected = selected;
		}

		public boolean isUptodate()
		{
			return uptodate;
		}

//		public void setUptodate( final boolean uptodate )
//		{
//			this.uptodate = uptodate;
//		}
	}

	static class FeatureTableDialog extends JDialog
	{
		private static final long serialVersionUID = 1L;

		public FeatureTableDialog( final Frame owner, final ArrayList< MyElement> elements1, final ArrayList< MyElement> elements2 )
		{
			super( owner, "tag sets configuration", false );

			final FeatureTable< ArrayList< MyElement >, MyElement > featureTable = new FeatureTable<>(
					null,
					ArrayList::size,
					ArrayList::get,
					MyElement::getName,
					MyElement::isSelected,
					MyElement::setSelected,
					MyElement::isUptodate );
			final JPanel featureTablePanel = new JPanel( new BorderLayout( 0, 0 ) );
			featureTablePanel.add( featureTable.getComponent(), BorderLayout.CENTER );
			featureTable.selectionListeners().add( ( t ) -> System.out.println( "Element " + t + " changed." ) );

			final JPanel buttons = new JPanel();
			featureTablePanel.add( buttons, BorderLayout.SOUTH );

			final JButton b1 = new JButton( "1" );
			final JButton b2 = new JButton( "2" );
			final JButton b4 = new JButton( "null" );
			b1.addActionListener( e -> featureTable.setElements( elements1 ) );
			b2.addActionListener( e -> featureTable.setElements( elements2 ) );
			b4.addActionListener( e -> featureTable.setElements( null ) );
			buttons.add( b1 );
			buttons.add( b2 );
			buttons.add( b4 );

			featureTablePanel.setPreferredSize( new Dimension( 400, 500 ) );
			getContentPane().add( featureTablePanel );
			pack();
		}
	}

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, InterruptedException
	{
		final ArrayList< MyElement> elements = new ArrayList<>();
		elements.add( new MyElement( "Spot N links", true, true ) );
		elements.add( new MyElement( "Spot frame", true, true ) );
		elements.add( new MyElement( "Spot gaussian-filtered intensity", false, false ) );
		elements.add( new MyElement( "Spot track ID", true, true ) );
		elements.add( new MyElement( "Track N spots", true, false ) );

		final ArrayList< MyElement> elements2 = new ArrayList<>();
		elements2.add( new MyElement( "Link displacement", true, true ) );
		elements2.add( new MyElement( "Link velocity", true, true ) );

		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final FeatureTableDialog frame = new FeatureTableDialog( null, elements, elements2 );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		frame.setLocationByPlatform( true );
		frame.setVisible( true );
	}
}
