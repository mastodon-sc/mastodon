#@ Context context

from java.io import File

from org.mastodon.mamut import WindowManager
from org.mastodon.mamut.project import MamutProject
from org.mastodon.tracking.mamut.trackmate import TrackMate
from org.mastodon.tracking.mamut.trackmate import Settings
from org.mastodon.tracking.mamut.detection import DoGDetectorMamut
from org.mastodon.tracking.mamut.linking import SimpleSparseLAPLinkerMamut
from org.mastodon.tracking.linking import LinkingUtils

bdv_file = "/Users/tinevez/Development/Mastodon/mastodon/samples/datasethdf5.xml"
project = MamutProject( None, File( bdv_file ) );

# Open the project.
wm = WindowManager( context )
wm.getProjectManager().open( project )
app_model = wm.getAppModel()

# Get Mastodon model.
model = app_model.getModel()
selection_model = app_model.getSelectionModel()

# Get image data.
image_data = app_model.getSharedBdvData()

# Configure TrackMate.

# Detector settings.
detector_settings = {
	"MIN_TIMEPOINT" : 0,
	"MAX_TIMEPOINT" : 1000,
	"SETUP" : 0, 				# The channel or source in the BDV data.
	"RADIUS" : 7., 			# The cell expected radius.
	"THRESHOLD" : 200.	 		# Threshold on quality.
}

# Linker settings. There are too many, so we take the default and edit it.
linker_settings = LinkingUtils.getDefaultLAPSettingsMap()
linker_settings[ "MIN_TIMEPOINT" ] = 0
linker_settings[ "MAX_TIMEPOINT" ] = 1000
linker_settings[ "LINKING_MAX_DISTANCE" ] = 10.
linker_settings[ "ALLOW_GAP_CLOSING" ] = True
linker_settings[ "MAX_FRAME_GAP" ] = 2 

# Create the settings objects.
settings = Settings() \
		.sources( image_data.getSources() ) \
		.detector( DoGDetectorMamut ) \
		.detectorSettings( detector_settings ) \
		.linker( SimpleSparseLAPLinkerMamut ) \
		.linkerSettings( linker_settings )

trackmate = TrackMate( settings, model, selection_model )

# We need to give a context to TrackMate.
trackmate.setContext( context )

# Run TrackMate.
trackmate.run()
if trackmate.isCanceled():
	print( "Calculation was canceled. Reason: " + trackmate.getCancelReason() )
elif not trackmate.isSuccessful():
	print( "Calculation failed with error message:\n" + trackmate.getErrorMessage() )
else:
	print( "Calculation complete." );

wm.createTrackScheme()
wm.createBigDataViewer()

# Compute features.




wm.createTable( False )
