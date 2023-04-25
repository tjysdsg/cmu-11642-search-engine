"""
Generate .param for HW5 experiments
"""

import os


def get_args():
    from argparse import ArgumentParser
    parser = ArgumentParser()
    parser.add_argument('--expid', type=str, required=True)
    parser.add_argument('--lambda_', type=float, required=True)
    parser.add_argument('--retrieval-algo', type=str, required=True)
    parser.add_argument('--diversity-algo', type=str, required=True)
    parser.add_argument('--max-input-length', type=int, default=100)
    parser.add_argument('--max-result-length', type=int, default=50)
    return parser.parse_args()


def main():
    args = get_args()

    bm25_params = """retrievalAlgorithm=BM25
BM25:b=0.75
BM25:k_1=1.2
BM25:k_3=0.0"""

    indri_params = """retrievalAlgorithm=Indri
Indri:mu=2500
Indri:lambda=0.4"""

    if args.retrieval_algo.lower() == 'bm25':
        model_params = bm25_params
    elif args.retrieval_algo.lower() == 'indri':
        model_params = indri_params
    else:
        assert False, f'"{args.retrieval_algo}"'

    params = f"""indexPath=INPUT_DIR/index-cw09
queryFilePath=TEST_DIR/HW5-Exp-all.qry
trecEvalOutputPath=OUTPUT_DIR/HW5-Exp-{args.expid}.teIn
trecEvalOutputLength=100
{model_params}
diversity=TRUE
diversity:maxInputRankingsLength={args.max_input_length}
diversity:maxResultRankingLength={args.max_result_length}
diversity:algorithm={args.diversity_algo}
diversity:lambda={args.lambda_}
diversity:intentsFile=TEST_DIR/HW5-Exp-all.intents
"""

    with open(os.path.join('experiments', 'hw5', f'HW5-Exp-{args.expid}.param'), 'w', encoding='utf-8') as f:
        f.write(params)


if __name__ == '__main__':
    main()
