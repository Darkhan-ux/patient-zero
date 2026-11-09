import java.util.*;

/**
 * Mission 5. Dynamic programming on the tree of infections. Root the tree in person 0.
 * Input.Tree.neighbours() gives the neighbours of every person. The number of people is at most about 100, so a recursive DFS is fine here.
 */
public class Outbreak {

    /**
     * Choose people to test so that for every link (somebody infected somebody) at least one of the two is tested,
     * and the total cost of testing is as small as possible. cost[v] is the price of testing person v.
     * Returns tested[v] = true if person v is tested.
     *
     * State for every person v (looking only at v and everything below it): the smallest cost when v IS tested, and
     * the smallest cost when v is NOT tested. What must then be true about the children of v in each case?
     * Do not forget to rebuild the answer (who exactly is tested), not only the cost: the picture needs it.
     * Main calls this twice: with all costs equal to 1 (the line "Tests needed") and with the real costs ("Cheapest testing").
     */
    public static boolean[] cheapestTests(int n, int[][] nb, int[] cost) {
        boolean[] tested = new boolean[n];
        if (n == 0) return tested;
        int[] par = new int[n];
        int[] order = rootAtZero(n, nb, par);

        // take[v] = cheapest cost of the subtree of v when v IS tested,
        // skip[v] = cheapest cost of the subtree of v when v is NOT tested.
        long[] take = new long[n], skip = new long[n];
        for (int h = n - 1; h >= 0; h--) {          // backwards over the order: every child before its parent
            int v = order[h];
            take[v] = cost[v];
            skip[v] = 0;
            for (int c : nb[v]) {
                if (c == par[v]) continue;
                take[v] += Math.min(take[c], skip[c]);  // v covers the link v-c, so c may be either way
                skip[v] += take[c];                     // v is not tested, so c must be, or the link v-c is open
            }
        }

        // rebuild who is tested, from the root down, with the same rule (on a tie: test)
        tested[0] = take[0] <= skip[0];
        for (int h = 1; h < n; h++) {
            int v = order[h], p = par[v];
            tested[v] = !tested[p] || take[v] <= skip[v];
        }
        return tested;
    }

    /** Roots the tree in person 0: fills par[] (par[0] = -1) and returns the people in BFS order, parents before children.
     *  Keeping the parent is what stops the walk from going back along the link it came from. */
    static int[] rootAtZero(int n, int[][] nb, int[] par) {
        int[] order = new int[n];
        boolean[] seen = new boolean[n];
        int head = 0, tail = 0;
        order[tail++] = 0; seen[0] = true; par[0] = -1;
        while (head < tail) {
            int v = order[head++];
            for (int c : nb[v]) {
                if (seen[c]) continue;
                seen[c] = true; par[c] = v;
                order[tail++] = c;
            }
        }
        return order;
    }

    /**
     * The number of links on the longest chain of infections, that is the longest path in the tree (counted in links).
     * A path goes up from one person and down to another; for every person you need the two longest ways down.
     * Example: a line 0-1-2-3 has chain 3; a star with centre 0 and leaves 1, 2, 3 has chain 2.
     */
    public static int longestChain(int n, int[][] nb) {
        if (n <= 1) return 0;
        int[] par = new int[n];
        int[] order = rootAtZero(n, nb, par);
        int[] down = new int[n];                    // down[v] = links on the longest way from v down to a leaf
        int best = 0;
        for (int h = n - 1; h >= 0; h--) {
            int v = order[h];
            int first = 0, second = 0;              // the two longest ways down from v through different children
            for (int c : nb[v]) {
                if (c == par[v]) continue;
                int len = down[c] + 1;
                if (len > first) { second = first; first = len; }
                else if (len > second) second = len;
            }
            down[v] = first;
            best = Math.max(best, first + second);  // the longest chain that has v as its highest person
        }
        return best;
    }
}
