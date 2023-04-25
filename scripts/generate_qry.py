from argparse import ArgumentParser
from typing import List, Dict


def get_args():
    parser = ArgumentParser()
    parser.add_argument('--bow', type=str, required=True, help='From query ID to bag of words')
    parser.add_argument('--output', type=str, required=True, help='Output path')
    parser.add_argument('--w-body', type=float, default='0.25')
    parser.add_argument('--w-title', type=float, default='0.25')
    parser.add_argument('--w-keywords', type=float, default='0.25')
    parser.add_argument('--w-url', type=float, default='0.25')
    parser.add_argument('--sdm', action='store_true', help='Use sequential dependency model')
    parser.add_argument('--sdm-bow', type=float, default='0.33')
    parser.add_argument('--sdm-ngram', type=float, default='0.33')
    parser.add_argument('--sdm-window', type=float, default='0.33')
    return parser.parse_args()


def op_weighted(op_name: str, args: List[str], weights: List[float]) -> str:
    assert len(args) == len(weights)

    a = [
        f'{weights[i]} {args[i]}'
        for i in range(len(args))
    ]
    arg_str = ' '.join(a)

    return f'{op_name} ( {arg_str} )'


def op_generic(op_name: str, args: List[str]) -> str:
    arg_str = ' '.join(args)
    return f'{op_name} ( {arg_str} )'


def op_and(args: List[str]) -> str:
    return op_generic('#and', args)


def op_near(n: int, args: List[str]) -> str:
    return op_generic(f'#near/{n}', args)


def op_window(n: int, args: List[str]) -> str:
    return op_generic(f'#window/{n}', args)


def op_wsum(args: List[str], weights: List[float]) -> str:
    return op_weighted('#wsum', args, weights)


def op_wand(args: List[str], weights: List[float]) -> str:
    return op_weighted('#wand', args, weights)


def build_word_multi_repr(word: str, field2weights: Dict[str, float]) -> str:
    """
    Combine multiple fields of a word using #wsum
    """
    args = []
    weights = []

    for f, weight in field2weights.items():
        args.append(f'{word}.{f}')
        weights.append(weight)

    return op_wsum(args, weights)


def build_multi_repr_qry(words: List[str], field2weights: Dict[str, float]) -> str:
    """
    From BOW to multiple representation query
    """
    args = []

    for w in words:
        args.append(build_word_multi_repr(w, field2weights))

    return op_and(args)


def build_sdm_qry(words: List[str], sdm_weights: List[float]) -> str:
    """
    Build sequential dependency model query
    """

    if len(words) <= 1:
        return op_and(words)

    bow_qry = op_and(words)

    args = []
    for i in range(len(words) - 1):
        args.append(
            op_near(1, [words[i], words[i + 1]])
        )
    ngram_qry = op_and(args)

    args = []
    for i in range(len(words) - 1):
        args.append(
            op_window(8, [words[i], words[i + 1]])
        )
    window_qry = op_and(args)

    return op_wand([bow_qry, ngram_qry, window_qry], sdm_weights)


def main():
    args = get_args()

    q2words = {}
    with open(args.bow, encoding='utf-8') as f:
        for line in f:
            qid, bow = line.rstrip('\n').split(':')
            qid = qid.strip()
            words = bow.strip().split()

            q2words[qid] = words

    q2qry = {}
    field2weights = dict(
        body=args.w_body,
        title=args.w_title,
        keywords=args.w_keywords,
        url=args.w_url,
    )
    sdm_weights = [
        args.sdm_bow,
        args.sdm_ngram,
        args.sdm_window,
    ]

    for qid, words in q2words.items():
        if args.sdm:
            q2qry[qid] = build_sdm_qry(words, sdm_weights)
        else:
            q2qry[qid] = build_multi_repr_qry(words, field2weights)

    with open(args.output, 'w', encoding='utf-8') as f:
        for q, qry in q2qry.items():
            f.write(f'{q}: {qry}\n')


if __name__ == '__main__':
    main()
