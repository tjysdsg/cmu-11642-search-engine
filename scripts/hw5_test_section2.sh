# Automatically test HW5 experiments

exp_dir=exp

for expid in 2.1a 2.1b 2.1c 2.1d 2.2a 2.2b 2.2c 2.2d 2.3a 2.3b 2.3c 2.3d 2.4a 2.4b 2.4c 2.4d; do
  _opts="--hw-id HW5 --fileIn ${exp_dir}/HW5-Exp-${expid}.teIn"
  python scripts/test_service.py ${_opts} $@ > exp/HW5-Exp-${expid}.result || exit 1
  python scripts/ndeval_test_service.py ${_opts} $@ > exp/HW5-Exp-${expid}_diversity.result || exit 1
done
