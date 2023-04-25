rm QryEval.zip
rm -rf QryEval

mkdir -p QryEval

cp src/main/java/search_engine/*.java QryEval/
cp experiments/hw5/* QryEval/

sed -i '/package search_engine/d' QryEval/* || exit 1

cp jiyangta-HW5-Report.pdf QryEval/

zip -r QryEval.zip QryEval || exit 1