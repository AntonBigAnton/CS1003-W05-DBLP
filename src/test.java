import java.util.ArrayList;

public class test {
    public static void main(String[] args) {
        ArrayList<String[]> tests = new ArrayList<String[]>();

        // Test 1: no values for any command line argument
        tests.add(new String[]{"--query", "--cache", "--search"});

        // Test 2: add an extra argument
        tests.add(new String[]{"--query", "jack cole", "--cache", "../cache", "--search", "author", "AN EXTRA ARGUMENT"});

        // Test 3: use an empty directory as cache
        tests.add(new String[]{"--query", "jack cole", "--cache", "emptyCache", "--search", "author"});

        // Test 4: use a file as cache
        tests.add(new String[]{"--query", "jack cole", "--cache", "file", "--search", "author"});

        // Test 5: search for author with no publication (me for example)
        tests.add(new String[]{"--query", "antoine megarbane", "--cache", "../cache", "--search", "author"});

        // Test 6: no --query argument
        tests.add(new String[]{"--cache", "../cache", "--search", "author"});

        System.out.println("TESTING");
        for (String[] t : tests) {
            System.out.println();
            CS1003P2.main(t);
        }
    }
}
