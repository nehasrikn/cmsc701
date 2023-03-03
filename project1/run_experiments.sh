DATASET=mosquito
METHOD=naive

for K in 0 5 10 20
do
	for QUERY in 100 200 500 1000 2000 10000
	do

		#!/usr/bin/env bash
		echo K=$K QUERY_LENGTH=$QUERY METHOD=$METHOD
		javac -d out/production/project1 -cp lib/*.jar src/*.java
		java -cp out/production/project1 QuerySA results/${DATASET}/${DATASET}_sa_preftab_$K.bin data/${DATASET}/${DATASET}_queries.fa_$QUERY $METHOD results/${DATASET}/${METHOD}_k_${K}_q_${QUERY}.out
	done
done
