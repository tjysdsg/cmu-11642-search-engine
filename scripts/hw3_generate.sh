# Automatically generate HW3 files

source_dir=src/main/java/search_engine

# 1
python scripts/hw3_generate.py --expid 1a --no-prf                               --out-dir ${source_dir}  || exit 1
python scripts/hw3_generate.py --expid 1b --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir}  || exit 1
python scripts/hw3_generate.py --expid 1c                                        --out-dir ${source_dir}  || exit 1

# 2
python scripts/hw3_generate.py --expid 2a --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 10 || exit 1
python scripts/hw3_generate.py --expid 2b --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 25 || exit 1
python scripts/hw3_generate.py --expid 2c --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 50 || exit 1

# 3
python scripts/hw3_generate.py --expid 3a --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 50 --num-terms  5 || exit 1
python scripts/hw3_generate.py --expid 3b --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 50 --num-terms 10 || exit 1
python scripts/hw3_generate.py --expid 3c --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 50 --num-terms 25 || exit 1
python scripts/hw3_generate.py --expid 3d --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 50 --num-terms 50 || exit 1

# 4
# title
python scripts/hw3_generate.py --expid 4a --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 50 --num-terms 50 --field title || exit 1
python scripts/hw3_generate.py --expid 4b --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 50 --num-terms  5 --field title || exit 1
python scripts/hw3_generate.py --expid 4c --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 10 --num-terms  5 --field title || exit 1
# url
python scripts/hw3_generate.py --expid 4d --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 50 --num-terms 50 --field url || exit 1
python scripts/hw3_generate.py --expid 4e --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 50 --num-terms  5 --field url || exit 1
python scripts/hw3_generate.py --expid 4f --initial-ranking HW3-Indri-Bow.inRank --out-dir ${source_dir} --num-docs 10 --num-terms  5 --field url || exit 1
