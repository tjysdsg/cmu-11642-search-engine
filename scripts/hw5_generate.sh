mkdir -p experiments/hw5 || exit 1

# ============= Section 2 =============
# Indri + PM2
python scripts/hw5_generate.py \
  --expid 2.1a \
  --retrieval-algo Indri \
  --diversity-algo PM2 \
  --lambda_ 0.0 || exit 1

python scripts/hw5_generate.py \
  --expid 2.1b \
  --retrieval-algo Indri \
  --diversity-algo PM2 \
  --lambda_ 0.33 || exit 1

python scripts/hw5_generate.py \
  --expid 2.1c \
  --retrieval-algo Indri \
  --diversity-algo PM2 \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 2.1d \
  --retrieval-algo Indri \
  --diversity-algo PM2 \
  --lambda_ 1.0 || exit 1

# Indri + xQuAD
python scripts/hw5_generate.py \
  --expid 2.2a \
  --retrieval-algo Indri \
  --diversity-algo xQuAD \
  --lambda_ 0.0 || exit 1

python scripts/hw5_generate.py \
  --expid 2.2b \
  --retrieval-algo Indri \
  --diversity-algo xQuAD \
  --lambda_ 0.33 || exit 1

python scripts/hw5_generate.py \
  --expid 2.2c \
  --retrieval-algo Indri \
  --diversity-algo xQuAD \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 2.2d \
  --retrieval-algo Indri \
  --diversity-algo xQuAD \
  --lambda_ 1.0 || exit 1

# BM25 + PM2
python scripts/hw5_generate.py \
  --expid 2.3a \
  --retrieval-algo BM25 \
  --diversity-algo PM2 \
  --lambda_ 0.0 || exit 1

python scripts/hw5_generate.py \
  --expid 2.3b \
  --retrieval-algo BM25 \
  --diversity-algo PM2 \
  --lambda_ 0.33 || exit 1

python scripts/hw5_generate.py \
  --expid 2.3c \
  --retrieval-algo BM25 \
  --diversity-algo PM2 \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 2.3d \
  --retrieval-algo BM25 \
  --diversity-algo PM2 \
  --lambda_ 1.0 || exit 1

# BM25 + xQuAD
python scripts/hw5_generate.py \
  --expid 2.4a \
  --retrieval-algo BM25 \
  --diversity-algo xQuAD \
  --lambda_ 0.0 || exit 1

python scripts/hw5_generate.py \
  --expid 2.4b \
  --retrieval-algo BM25 \
  --diversity-algo xQuAD \
  --lambda_ 0.33 || exit 1

python scripts/hw5_generate.py \
  --expid 2.4c \
  --retrieval-algo BM25 \
  --diversity-algo xQuAD \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 2.4d \
  --retrieval-algo BM25 \
  --diversity-algo xQuAD \
  --lambda_ 1.0 || exit 1

# ============= Section 3 =============
# Indri + PM2
python scripts/hw5_generate.py \
  --expid 3.1a \
  --retrieval-algo Indri \
  --diversity-algo PM2 \
  --max-input-length 25 \
  --max-result-length 25 \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 3.1b \
  --retrieval-algo Indri \
  --diversity-algo PM2 \
  --max-input-length 50 \
  --max-result-length 25 \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 3.1c \
  --retrieval-algo Indri \
  --diversity-algo PM2 \
  --max-input-length 100 \
  --max-result-length 50 \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 3.1d \
  --retrieval-algo Indri \
  --diversity-algo PM2 \
  --max-input-length 200 \
  --max-result-length 100 \
  --lambda_ 0.67 || exit 1

# BM25 + PM2
python scripts/hw5_generate.py \
  --expid 3.2a \
  --retrieval-algo BM25 \
  --diversity-algo PM2 \
  --max-input-length 25 \
  --max-result-length 25 \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 3.2b \
  --retrieval-algo BM25 \
  --diversity-algo PM2 \
  --max-input-length 50 \
  --max-result-length 25 \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 3.2c \
  --retrieval-algo BM25 \
  --diversity-algo PM2 \
  --max-input-length 100 \
  --max-result-length 50 \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 3.2d \
  --retrieval-algo BM25 \
  --diversity-algo PM2 \
  --max-input-length 200 \
  --max-result-length 100 \
  --lambda_ 0.67 || exit 1

# BM25 + xQuAD
python scripts/hw5_generate.py \
  --expid 3.3a \
  --retrieval-algo BM25 \
  --diversity-algo xQuAD \
  --max-input-length 25 \
  --max-result-length 25 \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 3.3b \
  --retrieval-algo BM25 \
  --diversity-algo xQuAD \
  --max-input-length 50 \
  --max-result-length 25 \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 3.3c \
  --retrieval-algo BM25 \
  --diversity-algo xQuAD \
  --max-input-length 100 \
  --max-result-length 50 \
  --lambda_ 0.67 || exit 1

python scripts/hw5_generate.py \
  --expid 3.3d \
  --retrieval-algo BM25 \
  --diversity-algo xQuAD \
  --max-input-length 200 \
  --max-result-length 100 \
  --lambda_ 0.67 || exit 1
