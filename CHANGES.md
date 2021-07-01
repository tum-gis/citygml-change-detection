# Change Log

### 1.0.0 - Active

##### NEW
* This project is now using Gradle instead of Maven and can be built and published to JFrog Artifactory using the Gradle plugin, see [`c43173b`](https://github.com/tum-gis/citygml-change-detection/commit/c43173b44565ade5930d0016de4ba5ca0ce3c324).  
* Added categorization of detected changes to facilitate more user-friendly reading/interpretation, see [`6ba3c1e`](https://github.com/tum-gis/citygml-change-detection/commit/6ba3c1eb8cf462064ccdd32a0a784d8f6b1799bf). 
* Added example scripts for running the program using parallel execution (such as for tiles), see [`cbc57d1`](https://github.com/tum-gis/citygml-change-detection/commit/cbc57d1477b3903a55c3c4fc7682e357868cd567). 
* Added handling of empty city models while exporting RTree images, see [`5e0494e`](https://github.com/tum-gis/citygml-change-detection/commit/5e0494eb3b33716b22af34376066b0a5c2ae9bec).
* Added a statistics bot `StatBot`, see [`ea6695a`](https://github.com/tum-gis/citygml-change-detection/commit/ea6695a3896ee73284a3c30be819c1c3dfd08cc4).
* Added option for mapping only one city model (instead of two), see [`29069a6`](https://github.com/tum-gis/citygml-change-detection/commit/29069a6d069e8a3bb44dad31d42a4f4b00e4cc10).
* Added option for mapping only (without matching/editing), see [`29069a6`](https://github.com/tum-gis/citygml-change-detection/commit/29069a6d069e8a3bb44dad31d42a4f4b00e4cc10).

##### UPDATE
* Optimized database memory consumption of very large datasets, see [`29069a6`](https://github.com/tum-gis/citygml-change-detection/commit/29069a6d069e8a3bb44dad31d42a4f4b00e4cc10).

##### FIXES
* Fixed matching values of length elements with respect to error tolerance, see[`e474001`](https://github.com/tum-gis/citygml-change-detection/commit/e474001d21c06cf5eed02a770222734e66871f87).