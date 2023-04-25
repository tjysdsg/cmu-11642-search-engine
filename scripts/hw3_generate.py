"""
Generate .param for HW3 experiments
"""

import os


def get_args():
    from argparse import ArgumentParser
    parser = ArgumentParser()
    parser.add_argument('--expid', type=str, required=True)
    parser.add_argument('--out-dir', type=str, required=True)
    parser.add_argument('--no-prf', action='store_true')
    parser.add_argument('--initial-ranking', type=str, default=None)
    parser.add_argument('--num-terms', type=str, default=10)
    parser.add_argument('--num-docs', type=str, default=10)
    parser.add_argument('--field', type=str, default='body')
    return parser.parse_args()


def main():
    args = get_args()

    params = f"indexPath=INPUT_DIR/index-cw09\n" \
             f"retrievalAlgorithm=Indri\n" \
             f"queryFilePath=TEST_DIR/HW3-Exp-{args.expid}.qry\n" \
             f"trecEvalOutputPath=OUTPUT_DIR/HW3-Exp-{args.expid}.teIn\n" \
             f"trecEvalOutputLength=1000\n" \
             f"Indri:mu=1500\n" \
             f"Indri:lambda=0.4\n"

    if not args.no_prf:
        params += f"prf=Indri\n" \
                  f"prf:numDocs={args.num_docs}\n" \
                  f"prf:numTerms={args.num_terms}\n" \
                  f"prf:Indri:mu=0\n" \
                  f"prf:Indri:origWeight=0.5\n" \
                  f"prf:expansionField={args.field}\n"

    if args.initial_ranking is not None:
        params += f"prf:initialRankingFile=TEST_DIR/{args.initial_ranking}\n"

    with open(os.path.join(args.out_dir, f'HW3-Exp-{args.expid}.param'), 'w', encoding='utf-8') as f:
        f.write(params)


if __name__ == '__main__':
    main()
