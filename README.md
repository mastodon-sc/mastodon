[![](https://travis-ci.org/fiji/TrackMate3.svg?branch=master)](https://travis-ci.org/fiji/TrackMate3)

![Mastodon logo](doc/Mastodon-logo-512x512.png)

# Mastodon – a large-scale tracking and track-editing framework for large, multi-view images.

Modern microscopy technologies such as light sheet microscopy allows live 3D imaging of entire developing embryos with high spatial and temporal resolution. Computational analysis of these recordings promises new insights in developmental biology. However, a single dataset often comprises many terabytes, which makes storage, processing, and visualization of the data a challenging problem. 

Large-scale automated tracking in biological datasets is a very active field of research. To support machine learning methods, editing tools are needed to facilitate curation, proof-reading, and the manual generation of ground truth data. To make such tools accessible to biologist researchers, they should be easy to obtain, learn, and use. Additionally they must be intuitively usable and remain responsive in the face of millions of tracked objects and terabytes of image data. To make them useful for researchers in automated tracking, they need to be open source, adaptable, and extensible. 

Mastodon is our effort to provide such a tool. 

Mastodon is a track-editing framework for cell tracking and lineage tracing tool. 


## How to use.

The keyboard shortcuts listed below are valid for the _default_ keymap.

### BigDataViewer (BDV) windows.

#### Moving around and display in BDV windows.

| Action                           | Key                                                          |
| -------------------------------- | ------------------------------------------------------------ |
| _View._                          |                                                              |
| Move in X & Y.                   | Right-click and drag.                                        |
| Move in Z.                       | Mouse-wheel. Press and hold shift to move faster, control to move slower. |
| Rotate.                          | Click and drag. The view will rotate around the location you clicked. |
| Align view with X /  Y / Z axes. | - Align with XY plane: `Shift-Z`<br> - Align with YZ plane: `Shift-X` <br>-  Align with XZ plane: `Shift-C` or `Shift-Y`   <br> The view will rotate around the location you clicked. |
| Zoom / Unzoom.                   | `Control-shift mouse-wheel` or `Command-mouse-wheel`.<br>The view will zoom and unzoom around the mouse location.     |
| _Time-points._                     |                                                              |
| Next time-point.                 | `]` or `M`                                                   |
| Previous time-point.             | `[` or `N`                                                   |
| _Bookmarks._                       |                                                              |
| Store a bookmark.                | Shift-B then press any key to store a bookmark with this key as name. A bookmark stores the position, zoom and orientation in the view but not the time-point. Bookmarks are saved in display settings file. |
| Recall a bookmark.               | Press B then the key of the bookmark.                        |
| Recall a bookmark orientation.   | Press O then the key of the bookmark. Only the orientation of the bookmark will be restored. |
| _Image display._                   |                                                              |
| Select source 1, 2, ...          | Press 1, 2, ...                                              |
| Brightness and color dialog.     | Press S. In this dialog you can adjust the min & max for each source, select to what sources these min & max apply and pick a color for each source. |
| Toggle fused mode.               | Press F. In fused mode, several sources are overlaid. Press shift-1, shift-2, … to add / remove the source to the view. In single-source mode, only one source is shown. |
| Visibility and grouping dialog.  | Press F6. In this dialog you can define what sources are visible in fused mode, and define groups of sources for use in the grouping mode. |
| Save / load display settings.    | F11 / F12. This will create a XYZ_settings.xml file in which the display settings will be saved. |


#### Manual editing and navigation in BDV windows.

| Action                                    | Key                      |
|-------------------------------------------|--------------------------|
| _Editing spots._                          |                          |
| Add a new spot.                           | Press `A` with the mouse over the desired location. |
| Remove a spot.                            | Press `D` with the mouse inside the spot to remove.   |
| Increase / Decrease the radius of a spot. | Press `E` / `Q` with the mouse inside the spot.<br>`Shift-E` / `Q` increase / decrease the spot radius by larger steps.<br>`Control-E` / `Q` enlarges the spot radius by finer steps.|
| Move a spot.                              | Press and hold `space` with mouse inside the spot to move, and move it around.                |
| _Creating links between spots._           |                          |
| Create a link between two spots.          | Press and hold `L` with the mouse inside the source spot. The BDV moves to the next frame. Release `L` when inside the target spot.<br>Press and hold `shift-L` to do the same, but linking to the previous frame.                        |
| Remove a link.                            | Press `D` with the mouse on the link to remove.   |
| Create a spot linked to a spot.           | Press and hold `A` with the mouse inside the source spot. The BDV moves to the next frame. Release `A` at the desired position. A new spot is created, linked to the source spot.<br>Press and hold `shift-A` to do the same, but linking to the previous frame.                      |
| _Selection editing._                      |                         |
| Add a spot / link to the selection.       | `Shift-click` on a spot or a link to add / remove it to / from the selection.                    |
| Clearing the selection.                   | Click on an empty place of the image.                                                            |
| Remove selection content.                 | `Shift-delete`.         |
| _Undo / redo_.                            |                         |
| Undo.                                     | `Control-Z`.            |
| Redo.                                     | `Control-shift-Z.`      |


### TrackScheme windows.

#### Navigation through lineages in BDV and TrackScheme windows.

| Action                                     | Key                                                          |
| ------------------------------------------ | ------------------------------------------------------------ |
| _Navigation._                              |                                                              |
| Navigate to parent / child in time.        | `↑` / `↓`. Select and move to the spot linked to this one in the previous / next time-point.<br>Press `shift` to also add it to the current selection. |
| Navigate to sibling.                       | `←` / `→`. Select and move to the sibling of this spot. A sibling is another spot from the same lineage in the same time-point.<br>Press `shift` to also add it to the current selection. |
| Navigate to branch parent / child in time. | `Alt + ↑` / `↓`. Select and move to the parent / child branch. A branch starts and ends with a division or fork in the lineage. Press `shift` to also add it to the current selection. |
| Navigate to spot / link.                   | `Double-click` on the spot / link.                           |
| _Selection._                               |                                                              |
| Select all parents.                        | `Shift + ⇞`. Select all the parents of this spots, that is all the spots in its lineage backward in time. |
| Select all children.                       | `Shift + ⇟`. Select all the children of this spots, that is all the spots in its lineage forward in time. |
| Select all lineage.                        | `Shift + space`. Select all the spots of this spot lineage.  |

#### Moving around in TrackScheme windows.

| Action                  | Key                                        |
|-------------------------|--------------------------------------------|
| _View._                 |                                            |
| Move around.            | `Right-click` and `drag` or `mouse-wheel`. |
| Zoom / unzoom in X.     | `Shift mouse-wheel`.                       |
| Zoom / unzoom in Y.     | `Control-mouse-wheel`.                     |
| Zoom / unzoom in X & Y. | `Control-shift-mouse-wheel`.               |
| Full zoom, full unzoom. | Press `Z`. The view zoom at max level to the mouse location. Pressing `Z` again to unzoom fully.                    |
| Zoom in a box.          | Press and hold Z, then drag a box. The view will zoom to the box.                            |

_TrackScheme box zoom:_
Drag a rectangle with the `Z` key pressed. TrackScheme will zoom to this rectangle. A press of `Z` fully zoom to designated location.
If TrackScheme is fully zoomed, a tap of `Z` unzoom fully.
![TrackScheme_ZoomBox](https://user-images.githubusercontent.com/3583203/32853983-6bfdd534-ca3d-11e7-8437-ec76eb04ae61.gif)

#### Manual track editing in TrackScheme windows.

| Action                              | Key                        |
|-------------------------------------|----------------------------|
| _Editing spots._                    |                            |
| Remove a spot.                      | Press `D` with the mouse inside the spot to remove.                                                |
| Edit the label of a spot.           | Press `Enter` when a spot is focused, then enter its label and press `Enter` to validate.       |
| _Creating links between spots._     |                            |
| Create a link between two spots.    | Press and hold `L` with the mouse inside the source spot. Release `L` when inside the target spot. |
| Remove a link.                      | Press `D` with the mouse on the link to remove.                                                    |
| _Selection editing._                  |                                                                                                |
| Add a spot / link to the selection. | `Shift-click` on a spot or a link to add / remove it to / from the selection.                   |
| Select all in a box.                | `Click and drag` a box.<br>`Shift-click and drag` to add the content of the box to the current selection.                   |
| Clearing the selection.             | `Click` on an empty place of the image.                                                          |
| Remove selection content.           | `Shift-delete.`         |
| _Undo / redo._                      |                         |
| Undo.                               | `Control-Z`.            |
| Redo.                               | `Control-shift-Z`.      |
