# STATBOT

This project employs a STATBOT tasked with mining and summarizing raw change detection data 
to produce results that are understandable and meaningful to humans.

By default, most information is stored in a log file (e.g. ``Stats.log``) in this directory
after the change detection process is complete.

The STATBOT also produces three CSV tables:

+   ``TopLevel_Changed.csv``: IDs of changed top-level features are stored here. 
    To enable tracking of objects that have their ID changed, 
    the table shall include both their old and new IDs.
    
+   ``TopLevel_Deleted.csv``: IDs of deleted top-level features are stored here.

+   ``TopLevel_Inserted.csv``: IDs of inserted top-level features are stored here.