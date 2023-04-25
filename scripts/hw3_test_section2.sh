# Automatically test HW3 experiments

exp_dir=exp

echo "Remember to pass --user-id and --password"

for expid in 2a 2b 2c; do
  _opts="--hw-id HW3 --fileIn ${exp_dir}/HW3-Exp-${expid}.teIn"
  python scripts/test_service.py ${_opts} $@ > exp/HW3-Exp-${expid}.result || exit 1
  python scripts/test_service.py ${_opts} --detailed $@ > exp/HW3-Exp-${expid}.detail.result || exit 1
done

echo "2A vs 1A"
python scripts/calculate_win_tie_loss.py $exp_dir/HW3-Exp-2a.detail.result $exp_dir/HW3-Exp-1a.detail.result

echo "2B vs 1A"
python scripts/calculate_win_tie_loss.py $exp_dir/HW3-Exp-2b.detail.result $exp_dir/HW3-Exp-1a.detail.result

echo "2C vs 1A"
python scripts/calculate_win_tie_loss.py $exp_dir/HW3-Exp-2c.detail.result $exp_dir/HW3-Exp-1a.detail.result
