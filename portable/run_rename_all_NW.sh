for file in test_data/2018/LOD1-Nordrhein-Westfalen/*.gml; do
	mv "$file" "${file/_NW/}" 
done

