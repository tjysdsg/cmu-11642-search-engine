# Automatically test HW3 experiments

exp_dir=exp

echo "Remember to pass --user-id and --password"

for expid in 4a 4b 4c 4d 4e 4f; do
  _opts="--hw-id HW3 --fileIn ${exp_dir}/HW3-Exp-${expid}.teIn"
  python scripts/test_service.py ${_opts} $@ > exp/HW3-Exp-${expid}.result || exit 1
  python scripts/test_service.py ${_opts} --detailed $@ > exp/HW3-Exp-${expid}.detail.result || exit 1

  echo "${expid} vs 1A"
  python scripts/calculate_win_tie_loss.py $exp_dir/HW3-Exp-${expid}.detail.result $exp_dir/HW3-Exp-1a.detail.result
done
