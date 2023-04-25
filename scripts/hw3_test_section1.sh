# Automatically test HW3 experiments

exp_dir=exp

for expid in 1a 1b 1c; do
  _opts="--hw-id HW3 --fileIn ${exp_dir}/HW3-Exp-${expid}.teIn"
  python scripts/test_service.py ${_opts} $@ > exp/HW3-Exp-${expid}.result || exit 1
  python scripts/test_service.py ${_opts} --detailed $@ > exp/HW3-Exp-${expid}.detail.result || exit 1
done

echo "1B vs 1A"
python scripts/calculate_win_tie_loss.py $exp_dir/HW3-Exp-1b.detail.result $exp_dir/HW3-Exp-1a.detail.result

echo "1c vs 1A"
python scripts/calculate_win_tie_loss.py $exp_dir/HW3-Exp-1c.detail.result $exp_dir/HW3-Exp-1a.detail.result
