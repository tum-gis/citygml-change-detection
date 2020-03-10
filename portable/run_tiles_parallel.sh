#!/bin/bash

# First argument: number of parallel processes to be executed

# We assume both directories have identical number of tiles, as well as tile filenames
# We assume every corresponding tile pair in the old and new datasets have the same name
# If not, execute ./run_rename_all_NW.sh

A_test_data_location="test_data/2017/2017_LoD2_NRW/"
B_test_data_location="test_data/2019/LoD2_NW_2019_UTM32/"

timestamp() {
  date +"%T"
}

################################################################################
# MATCHED TILES
echo $(timestamp) MATCHING TILES IN BOTH OLD AND NEW DATASETS

: <<'END'
# Version 1: Divide the workflow for N workers, each worker takes 1/N of the total work
# Split main file arrays into smaller arrays for parallel execution
AB_matched=($A_test_data_location*.gml) # Both A and B test data location have the same filenames
AB_matched_size=${#AB_matched[@]} # A_matched and B_matched have the same length
AB_matched_sub_size=$(($AB_matched_size/$1)) # The result is a (rounded) integer
for ((AB_i=0; AB_i < AB_matched_size; AB_i+=AB_matched_sub_size)); do
        AB_matched_sub=( "${AB_matched[@]:AB_i:AB_matched_sub_size}" )

        # Execute in parallel
        # Argument "$AB_matched_sub" only gives the first element -> use "${AB_matched_sub[*]}"
        AB_arg=$( IFS=$','; echo "${AB_matched_sub[*]}" )
        ./run_tiles_matched.sh "$AB_arg" &
done
wait
END

: <<'END'
# Version 2: Divide the workflow to N chunks, each chunk has a fixed size
# Split main file arrays into smaller arrays for parallel execution
AB_matched=($A_test_data_location*.gml) # Both A and B test data location have the same filenames
AB_matched_size=${#AB_matched[@]} # A_matched and B_matched have the same length
AB_matched_sub_size=$1
for ((AB_i=0; AB_i < AB_matched_size; AB_i+=AB_matched_sub_size)); do
        AB_matched_sub=( "${AB_matched[@]:AB_i:AB_matched_sub_size}" )

        # Execute in parallel
        # Argument "$AB_matched_sub" only gives the first element -> use "${AB_matched_sub[*]}"
        AB_arg=$( IFS=$','; echo "${AB_matched_sub[*]}" )
        ./run_tiles_matched.sh "$AB_arg" &
done
wait
END

# Version 3: Use xargs
cpus=$( ls -d /sys/devices/system/cpu/cpu[[:digit:]]* | wc -w )
# A unique union of all (base) names of files in both directories
# The program considers all 3 cases: both tiles are matched, only old tile, or only new tile is found
find $A_test_data_location $B_test_data_location -name '*.gml' -type f | xargs -n 1 basename | sort -u | xargs -l -P $cpus ./run_tiles_matched_for_xargs.sh
#find $A_test_data_location -name \*.gml | xargs -l -P $1 ./run_tiles_matched_for_xargs.sh

echo $(timestamp) DONE.
read -p "Press enter to continue"