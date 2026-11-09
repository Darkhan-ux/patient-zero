import java.util.*;

/**
 * Missions 3 and 4. Find a word in a long text.
 */
public class Marker {

    /**
     * Mission 3a. The prefix function of p. pi[i] = the length of the longest proper prefix of p[0..i] that is
     * also a suffix of p[0..i] ("proper" means shorter than p[0..i] itself).
     * Example: p = "ACGAC" gives [0, 0, 0, 1, 2]. The last number is the "Border length" line.
     */
    public static int[] prefixFunction(String p) {
        int m = p.length();
        int[] pi = new int[m];          // pi[0] = 0: a single letter has no proper border
        int k = 0;                      // the length of the current border of p[0..i-1]
        for (int i = 1; i < m; i++) {
            // the border cannot grow with p[i]: fall back to the next shorter border
            while (k > 0 && p.charAt(i) != p.charAt(k)) k = pi[k - 1];
            if (p.charAt(i) == p.charAt(k)) k++;
            pi[i] = k;
        }
        return pi;
    }

    /**
     * Mission 3b. KMP. All start positions (counting from 0, in increasing order) where p occurs in text.
     * Overlapping occurrences count: "ACG" occurs in "ACGACG" at 0 and 3, and "AA" in "AAA" at 0 and 1.
     * Use the prefix function: the text pointer never goes back. The number of positions is "Marker hits",
     * the first one is "First hit" (-1 when there is none).
     */
    public static int[] findAll(String text, String p) {
        int n = text.length(), m = p.length();
        if (m == 0 || m > n) return new int[0];
        int[] pi = prefixFunction(p);
        int[] found = new int[n];       // there are at most n - m + 1 hits
        int count = 0;
        int k = 0;                      // how many letters of p are matched right now
        for (int i = 0; i < n; i++) {   // i only moves forward: the text pointer never goes back
            char c = text.charAt(i);
            while (k > 0 && c != p.charAt(k)) k = pi[k - 1];   // mismatch: keep the longest border that still fits
            if (c == p.charAt(k)) k++;
            if (k == m) {               // full match ending at i
                found[count++] = i - m + 1;
                k = pi[m - 1];          // do not restart from 0: the next hit may overlap this one
            }
        }
        return Arrays.copyOf(found, count);
    }

    /**
     * Mission 4. Rabin-Karp. The number of positions i in a such that the word of k letters starting at i
     * (a[i..i+k-1]) also occurs somewhere in b. Count positions, not different words. This is the "Shared words" line.
     * The idea: give every window of k letters a number (a rolling hash, updated in O(1) when the window moves by one),
     * look the number up among the windows of b, and when it is found COMPARE THE LETTERS, because two different words
     * can have the same hash.
     * Example: a = "ACGTAC", b = "TTACGT", k = 4: the words of a are ACGT, CGTA, GTAC; only ACGT is in b, so the answer is 1.
     */
    public static int sharedWords(String a, String b, int k) {
        return sharedWords(a, b, k, MOD);
    }

    static final long BASE = 131;
    static final long MOD = 1_000_000_007L;

    /** The same, with a chosen modulus. With a tiny modulus (for example 101) many different words get the same hash;
     *  the answer must not change, because every hash hit is checked letter by letter. */
    static int sharedWords(String a, String b, int k, long mod) {
        if (k <= 0 || a.length() < k || b.length() < k) return 0;
        long top = 1;                                   // BASE^(k-1) mod mod: the weight of the letter that leaves the window
        for (int i = 1; i < k; i++) top = top * BASE % mod;

        // the hash of every window of b, and where in b each hash occurs
        Map<Long, List<Integer>> inB = new HashMap<>();
        long h = 0;
        for (int i = 0; i < k; i++) h = (h * BASE + b.charAt(i)) % mod;
        inB.computeIfAbsent(h, x -> new ArrayList<>()).add(0);
        for (int i = 1; i + k <= b.length(); i++) {
            h = roll(h, b.charAt(i - 1), b.charAt(i + k - 1), top, mod);
            inB.computeIfAbsent(h, x -> new ArrayList<>()).add(i);
        }

        // slide over a; on a hash hit compare the letters, because equal hashes do not prove equal words
        int count = 0;
        h = 0;
        for (int i = 0; i < k; i++) h = (h * BASE + a.charAt(i)) % mod;
        for (int i = 0; i + k <= a.length(); i++) {
            if (i > 0) h = roll(h, a.charAt(i - 1), a.charAt(i + k - 1), top, mod);
            List<Integer> candidates = inB.get(h);
            if (candidates == null) continue;
            for (int j : candidates) {
                if (sameWord(a, i, b, j, k)) { count++; break; }   // count the position i once
            }
        }
        return count;
    }

    /** The hash of the next window in O(1): remove the old first letter, shift by one place, add the new last letter. */
    static long roll(long h, char out, char in, long top, long mod) {
        h = (h - out * top % mod + mod) % mod;          // + mod keeps the number from going negative
        return (h * BASE + in) % mod;
    }

    /** Letter-by-letter check that a[i..i+k-1] equals b[j..j+k-1]. */
    static boolean sameWord(String a, int i, String b, int j, int k) {
        for (int t = 0; t < k; t++) if (a.charAt(i + t) != b.charAt(j + t)) return false;
        return true;
    }
}
