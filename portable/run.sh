cpus=$( ls -d /sys/devices/system/cpu/cpu[[:digit:]]* | wc -w )
./run_tiles_parallel.sh -1000 | xargs --max-args=1 --max-procs=$cpus 
