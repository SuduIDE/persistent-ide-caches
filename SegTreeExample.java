public class SegTreeExample {
    int[] tree;
    int pw;
    SegTreeExample(int[] a) {
        int n = a.length;
        pw = 1;
        while (pw < n) {
            pw *= 2;
        }
        tree = new int[2 * pw];
        for (int i = 0; i < n; i++) {
            tree[pw + i] = a[i];
        }
        for (int i = pw - 1; i > 0; i--) {
            tree[i] = tree[2 * i] + tree[2 * i + 1];
        }
    }

    //  sum on interval [from; to)
    int querySum(int from, int to) {
        int result = 0;
        from += pw;
        to += pw;
        while (from < to) {
            if (from % 2 == 1) {
                result += tree[from++];
            }
            if (to % 2 == 1) {
                result += tree[--to];
            }
            from /= 2;
            to /= 2;
        }
        return result;
    }

    void setValue(int at, int value) {
        at += pw;
        tree[at] = value;
        at /= 2;
        while (at > 0) {
            tree[at] = tree[2 * at] + tree[2 * at + 1];
            at /= 2;
        }
    }
}
