# Indri with SDM for test query
python scripts/generate_qry.py \
  --bow HW4-test.qry \
  --output src/main/java/search_engine/HW4-Exp-1.1b.qry \
  --sdm \
  --sdm-bow 0.7 \
  --sdm-ngram 0.2 \
  --sdm-window 0.1 \
  || exit 1
