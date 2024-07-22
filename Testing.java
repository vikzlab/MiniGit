// Vikram Murali

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class Testing {
    private Repository repo1;
    private Repository repo2;

    // Occurs before each of the individual test cases
    // (creates new repos and resets commit ids)
    @BeforeEach
    public void setUp() {
        repo1 = new Repository("repo1");
        repo2 = new Repository("repo2");
        Repository.Commit.resetIds();
    }

    @Test
    @DisplayName("Synchronize with Front Commit")
    public void testSynchronizeFrontCommit() throws InterruptedException{
        commitAll(repo2, new String[]{"One"});
        commitAll(repo1, new String[]{"Zero"});
        assertEquals(1, repo1.getRepoSize());
        assertEquals(1, repo2.getRepoSize());
        repo1.synchronize(repo2);
        assertEquals(2, repo1.getRepoSize());
        assertEquals(0, repo2.getRepoSize());
        testHistory(repo1, 2, new String[]{"One", "Zero"});
    }

    @Test
    @DisplayName("Synchronize with Middle Commit")
    public void testSynchronizeMiddleCommit() throws InterruptedException {
        commitAll(repo1, new String[]{"First", "Second", "Third"});  
        commitAll(repo2, new String[]{"Fourth", "Fifth", "Sixth"}); 
        assertEquals(3, repo1.getRepoSize());
        assertEquals(3, repo2.getRepoSize());
        repo1.synchronize(repo2);
        assertEquals(6, repo1.getRepoSize());
        assertEquals(0, repo2.getRepoSize());
        testHistory(repo1, 6, new String[]{"First", "Second", "Third","Fourth", "Fifth", "Sixth"});
    }

    @Test
    @DisplayName("Synchronize with Last Commit")
    public void testSynchronizeLastCommit() throws InterruptedException {
        commitAll(repo1, new String[]{"First", "Second"}); 
        commitAll(repo2, new String[]{"Third"});  
        assertEquals(2, repo1.getRepoSize());
        assertEquals(1, repo2.getRepoSize());
        repo1.synchronize(repo2);
        assertEquals(3, repo1.getRepoSize());
        assertEquals(0, repo2.getRepoSize());
        testHistory(repo1, 3, new String[]{"First", "Second", "Third"});
    }



    // Commits all of the provided messages into the provided repo, making sure timestamps
    // are correctly sequential (no ties). If used, make sure to include
    //      'throws InterruptedException'
    // much like we do with 'throws FileNotFoundException'. Example useage:
    //
    // repo1:
    //      head -> null
    // To commit the messages "one", "two", "three", "four"
    //      commitAll(repo1, new String[]{"one", "two", "three", "four"})
    // This results in the following after picture
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    public void commitAll(Repository repo, String[] messages) throws InterruptedException {
        // Commit all of the provided messages
        for (String message : messages) {
            int size = repo.getRepoSize();
            repo.commit(message);
            
            // Make sure exactly one commit was added to the repo
            assertEquals(size + 1, repo.getRepoSize(),
                         String.format("Size not correctly updated after commiting message [%s]",
                                       message));

            // Sleep to guarantee that all commits have different time stamps
            Thread.sleep(2);
        }
    }

    // Makes sure the given repositories history is correct up to 'n' commits, checking against
    // all commits made in order. Example useage:
    //
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //      (Commits made in the order ["one", "two", "three", "four"])
    // To test the getHistory() method up to n=3 commits this can be done with:
    //      testHistory(repo1, 3, new String[]{"one", "two", "three", "four"})
    // Similarly, to test getHistory() up to n=4 commits you'd use:
    //      testHistory(repo1, 4, new String[]{"one", "two", "three", "four"})
    public void testHistory(Repository repo, int n, String[] allCommits) {
        int totalCommits = repo.getRepoSize();
        assertTrue(n <= totalCommits,
                   String.format("Provided n [%d] too big. Only [%d] commits",
                                 n, totalCommits));
        
        String[] nCommits = repo.getHistory(n).split("\n");
        
        assertTrue(nCommits.length <= n,
                   String.format("getHistory(n) returned more than n [%d] commits", n));
        assertTrue(nCommits.length <= allCommits.length,
                   String.format("Not enough expected commits to check against. " +
                                 "Expected at least [%d]. Actual [%d]",
                                 n, allCommits.length));
        
        for (int i = 0; i < n; i++) {
            String commit = nCommits[i];

            // Old commit messages/ids are on the left and the more recent commit messages/ids are
            // on the right so need to traverse from right to left
            int backwardsIndex = totalCommits - 1 - i;
            String commitMessage = allCommits[backwardsIndex];

            assertTrue(commit.contains(commitMessage),
                       String.format("Commit [%s] doesn't contain expected message [%s]",
                                     commit, commitMessage));
            assertTrue(commit.contains("" + backwardsIndex),
                       String.format("Commit [%s] doesn't contain expected id [%d]",
                                     commit, backwardsIndex));
        }
    }
}