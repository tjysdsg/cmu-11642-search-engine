#!/usr/bin/python

import requests


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
    qrels = 'cw09a.adhoc.1-200.qrel.indexed'

    # Form parameters - these must match form parameters in the web page

    url = 'https://boston.lti.cs.cmu.edu/classes/11-642/HW/HTS/tes.cgi'
    values = {
        'hwid': args.hw_id,  # cgi parameter
        'qrel': qrels,  # cgi parameter
        'logtype': 'Detailed' if args.detailed else 'Summary',  # cgi parameter
        'leaderboard': 'No'  # cgi parameter
    }

    # Make the request
    files = {'infile': (args.fileIn, open(args.fileIn, 'rb'))}  # cgi parameter
    result = requests.post(url, data=values, files=files, auth=(args.user_id, args.password))

    # Replace the <br /> with \n for clarity
    print(result.text.replace('<br />', '\n'))


if __name__ == '__main__':
    main()
