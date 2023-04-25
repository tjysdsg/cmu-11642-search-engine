# Automatically test HW5 experiments

exp_dir=exp

for expid in 3.1a 3.1b 3.1c 3.1d 3.2a 3.2b 3.2c 3.2d 3.3a 3.3b 3.3c 3.3d; do
  _opts="--hw-id HW5 --fileIn ${exp_dir}/HW5-Exp-${expid}.teIn"
  python scripts/test_service.py ${_opts} $@ > exp/HW5-Exp-${expid}.result || exit 1
  python scripts/ndeval_test_service.py ${_opts} $@ > exp/HW5-Exp-${expid}_diversity.result || exit 1
done
