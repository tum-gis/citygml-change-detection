# Change detection results

After the change detection process is complete, the results are stored by default in this directory, where:

+   [changes](changes) contains all raw information about the detected changes in multiple CSV files. 
    These are divided into 5 types of ``EditOperations``: node insertion and deletion, 
    and property insertion, deletion and update. 
    Please refer to the description in the directory for more information.
    
+   [logs](logs) stores the run logs for debugging purposes.

+   [rtrees](rtrees) stores R-tree footprints of input city models 
    (if spatial matching strategy R-tree is enabled, please refer to the [configuration file](../config)).
    These signature pictures visualized R-trees of respective city models 
    by representing each R-tree node as a rectangle, 
    while their colors indicate node levels in the R-tree (see examples [here](../resources/Berlin_M10.png)).
    
+   [statbot](statbot) summarizes raw data and produces results that are understandable und meaningful to humans.
    Please refer to the description in the directory for more information.