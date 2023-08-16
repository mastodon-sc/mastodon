package org.mastodon;

public class StartMastodonDefaultProject
{

	public static void main( final String[] args )
	{
		final String projectPath = "samples/MaMuT_Parhyale_small.mastodon";
//		final String projectPath = "/Users/tinevez/Google Drive/Mastodon/Datasets/Remote/ParhyaleHawaiensis/MaMuT_Parhyale_demo-mamut.mastodon";
//		final String projectPath = "samples/drosophila_crop.mastodon";
		StartMastodonOnProject.launch( projectPath );
	}
}
