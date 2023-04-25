# Experiment 3
python scripts/generate_qry.py \
  --bow HW2-bow.txt \
  --output src/main/java/search_engine/HW2-Exp-3.1b.qry \
  --w-body 0.25 \
  --w-title 0.25 \
  --w-keywords 0.25 \
  --w-url 0.25 \
  || exit 1

python scripts/generate_qry.py \
  --bow HW2-bow.txt \
  --output src/main/java/search_engine/HW2-Exp-3.1c.qry \
  --w-body 0.9 \
  --w-title 0.1 \
  --w-keywords 0.01 \
  --w-url 0.01 \
  || exit 1

python scripts/generate_qry.py \
  --bow HW2-bow.txt \
  --output src/main/java/search_engine/HW2-Exp-3.1d.qry \
  --w-body 0.8 \
  --w-title 0.1 \
  --w-keywords 0.1 \
  --w-url 0.01 \
  || exit 1

python scripts/generate_qry.py \
  --bow HW2-bow.txt \
  --output src/main/java/search_engine/HW2-Exp-3.1e.qry \
  --w-body 0.7 \
  --w-title 0.1 \
  --w-keywords 0.1 \
  --w-url 0.1 \
  || exit 1

# Experiment 4

python scripts/generate_qry.py \
  --bow HW2-bow.txt \
  --output src/main/java/search_engine/HW2-Exp-4.1b.qry \
  --sdm \
  --sdm-bow 0.01 \
  --sdm-ngram 1 \
  --sdm-window 0.01 \
  || exit 1

python scripts/generate_qry.py \
  --bow HW2-bow.txt \
  --output src/main/java/search_engine/HW2-Exp-4.1c.qry \
  --sdm \
  --sdm-bow 0.01 \
  --sdm-ngram 0.01 \
  --sdm-window 1 \
  || exit 1

python scripts/generate_qry.py \
  --bow HW2-bow.txt \
  --output src/main/java/search_engine/HW2-Exp-4.1d.qry \
  --sdm \
  --sdm-bow 0.01 \
  --sdm-ngram 0.5 \
  --sdm-window 0.5 \
  || exit 1

python scripts/generate_qry.py \
  --bow HW2-bow.txt \
  --output src/main/java/search_engine/HW2-Exp-4.1e.qry \
  --sdm \
  --sdm-bow 0.7 \
  --sdm-ngram 0.2 \
  --sdm-window 0.1 \
  || exit 1
