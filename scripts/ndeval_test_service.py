# !/usr/bin/python

import requests
import re


def get_args():
    from argparse import ArgumentParser
    parser = ArgumentParser()
    parser.add_argument('--user-id', type=str, required=True)
    parser.add_argument('--password', type=str, required=True)
    parser.add_argument('--fileIn', type=str, required=True)
    parser.add_argument('--hw-id', type=str, required=True)
    parser.add_argument('--detailed', action='store_true')
    return parser.parse_args()


def main():
    args = get_args()
    qrels = 'cw09a.diversity.1-200.qrel.indexed'

    url = 'https://boston.lti.cs.cmu.edu/classes/11-642/HW/HTS/nes.cgi'
    values = {
        'hwid': args.hw_id,  # cgi parameter
        'qrel': qrels,  # cgi parameter
        'logtype': 'Detailed' if args.detailed else 'Summary',  # cgi parameter
        'leaderboard': 'No'  # cgi parameter
    }

    # Make the request
    files = {'infile': (args.fileIn, open(args.fileIn, 'rb'))}  # cgi parameter
    result = requests.post(url, data=values, files=files, auth=(args.user_id, args.password))

    res_text = result.text
    vals = re.search(f"<pre>([^<]+)amean,([^<]+)</pre>", res_text)
    nums = vals.group(2).split(",")
    val_idxs = [16, 17, 11]
    metrics = ['P_IA@10', 'P_IA@20', 'alpha_NDCG@20']
    for m, idx in zip(metrics, val_idxs):
        print(f'{m}: {nums[idx]}')


if __name__ == '__main__':
    main()
