def get_args():
    from argparse import ArgumentParser
    parser = ArgumentParser()
    parser.add_argument('original', type=str)
    parser.add_argument('new', type=str)
    return parser.parse_args()


def read_maps(file: str):
    ret = []
    with open(file, encoding='utf-8') as f:
        for line in f:
            tokens = line.rstrip('\n').split()
            if len(tokens) > 0 and tokens[0] == 'map':
                ret.append(float(tokens[-1]))
    return ret


def main():
    args = get_args()

    orig_maps = read_maps(args.original)
    new_maps = read_maps(args.new)

    n = len(orig_maps)
    assert n == len(new_maps)

    n_win = 0
    n_loss = 0
    n_tie = 0
    for i in range(n):
        rel_map = (new_maps[i] - orig_maps[i]) / orig_maps[i]
        if rel_map <= -0.02:
            n_loss += 1
            print(f'{i + 1}-th qry: loss')
        elif rel_map >= 0.02:
            n_win += 1
            print(f'{i + 1}-th qry: win')
        else:
            n_tie += 1
            print(f'{i + 1}-th qry: tie')

    print(f"win:tie:loss: {n_win}:{n_tie}:{n_loss}")


if __name__ == '__main__':
    main()
