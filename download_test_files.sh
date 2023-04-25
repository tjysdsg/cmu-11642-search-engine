# for ext in qry qryOut param teIn inRank trainQry trainQrels LtrTrain Model LtrTest DocScore; do
for ext in qry param teIn intents inRank; do
  for i in $(seq 1 24); do
    wget --user xxx --password xxx https://boston.lti.cs.cmu.edu/classes/11-642/HW/HTS/tests/HW5-Train-${i}.${ext} -P src/test/resources/
  done
done