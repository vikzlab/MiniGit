// Name: Vikram Murali 

// The Repository class represents a version control repository. It manages a 
// collection of commit objects representing the version history of the repository. 
// This class provides functionalities to add, remove, and look up commits based on 
// commit IDs. It also includes methods to get a formatted string of the
// repository's commit history and to synchronize commits between two repositories.
import java.util.*;
import java.text.SimpleDateFormat;

public class Repository {
    private String name;
    private Commit head;
    private int size;
    
    // Constructs a Repository with a given name
    // Parameters:
    //  name: name of Repository
    // throws IllegalArgumentException if the name of Repository is empty or null
    public Repository(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Repository name cannot be null or empty");
        }
        this.name = name;
        this.head = null;
        this.size = 0;
    }

    // Returns the id of the current head of this repository
    // Returns: the id of the head commit or null if there is no commit
    public String getRepoHead(){
        if(this.head != null){
            return this.head.id;
        } else{
            return null;
        }
    }
    
    // Returns the number of commits in the Repository
    // Returns: the number of commits
    public int getRepoSize(){
        return this.size;
    }

    // Returns a string representation of this repository
    // Returns: a formatted string with repository details
    public String toString(){
        if(this.head == null){
            return this.name + " - No commits";
        } else{
            return this.name + " - Current head: " + head.toString();
        }
    }

    // Checks if a commit exists in a Repository
    // Parameters:
    //  targetId: the id of the commit to check
    // Returns: true if commit is found, false otherwise
    public boolean contains(String targetId){
        Commit current = head;
        while (current != null) {
            if (current.id.equals(targetId)) {
                return true;
            }
            current = current.past;
        }
        return false;
    }

    // Returns a string of the history of the number of most recent commits
    // This method gets the most recent 'n' commits from the repository history. If 
    // 'n' is larger than the total number of commits available all commits will be returned 
    // without throwing an error
    // Parameters:
    //  n: the number of commits to return
    // Returns: formatted string of commit history
    // throws IllegalArgumentException if 'n' is less than 1 
    // indicating that the request for number of commits is invalid
    public String getHistory(int n){
        if (n < 1) {
            throw new IllegalArgumentException("Number of commits to retrieve must be positive.");
        }
        String result = "";
        Commit current = head;
        int count = 0;
        while (current != null && count < n) {
            result += current + "\n";  
            current = current.past;
            count++;
        }
        if (!result.isEmpty()) {
            result = result.substring(0, result.length() - 1);  
        }
        return result;
    }

    // Adds a commit to the repository with the given message
    // Parameters:
    //  message: the commit message
    // Returns the ID of the newly created commit
    public String commit(String message){
        Commit newCommit = new Commit(message);
        newCommit.past = this.head;
        this.head = newCommit;
        size++;
        return newCommit.id;
    }

    // Removes a commit ID maintaining the rest of the history
    // Parameters: 
    //  targetId: the ID of the commit to remove
    // Returns true if the commit was successfully removed, false otherwise
    public boolean drop(String targetId) {
         if (this.head == null) {
            return false;
        }

        if (this.head.id.equals(targetId)) {
            head = head.past;
            size--;
            return true;
        }
        Commit current = head;
        while (current.past != null) {
            if (current.past.id.equals(targetId)) {
                current.past = current.past.past;
                size--;
                return true;
            }
            current = current.past;
        }
        return false;
    }

    // This method synchronizes the commits between this repository and another repository
    // If the other repository has no commits or if this repository has no commits it 
    // performs a single copy or transfer of commits
    // If both repositories have commits it merges them by comparing the timestamps of 
    // their commits and arranges them in chronological order
    // Finally it updates the head and size of this repository to reflect the synchronized 
    // commits and resets the other repository to an empty state
    // Parameters:
    //  other: the repository to synchronize with
    // Returns nothing
    public void synchronize(Repository other) {
        boolean shouldExit = false;
        if (other.head == null) {
            shouldExit = true;
        }
        if (!shouldExit && this.head == null) {
            this.head = other.head;
            this.size = other.size;
            other.head = null;
            other.size = 0;
            shouldExit = true;
        }
        if (!shouldExit) {
            Commit newHead = null;
            Commit lastAdded = null;
            Commit currentThis = this.head;
            Commit currentOther = other.head;
            while (currentThis != null || currentOther != null) {
                Commit candidate = null;
                if (currentThis != null && (currentOther == null || 
                    currentThis.timeStamp > currentOther.timeStamp)) {
                    candidate = currentThis;
                    currentThis = currentThis.past;
                } else if (currentOther != null) {
                    candidate = currentOther;
                    currentOther = currentOther.past;
                }
                if (newHead == null) {
                    newHead = candidate;
                } else {
                    lastAdded.past = candidate;
                }
                lastAdded = candidate;
            }
            if (lastAdded != null) {
                lastAdded.past = null;
            }
            this.head = newHead;
            this.size = 0; 
            Commit count = newHead;
            while (count != null) {
                this.size++;
                count = count.past;
            }
            other.head = null;
            other.size = 0;
        }
    }

    
    /**
     * A class that represents a single commit in the repository.
     * Commits are characterized by an identifier, a commit message,
     * and the time that the commit was made. A commit also stores
     * a reference to the immediately previous commit if it exists.
     */
    public static class Commit {

        private static int currentCommitID;

        /**
         * The time, in milliseconds, at which this commit was created.
         */
        public final long timeStamp;

        /**
         * A unique identifier for this commit.
         */
        public final String id;

        /**
         * A message describing the changes made in this commit.
         */
        public final String message;

        /**
         * A reference to the previous commit, if it exists. Otherwise, null.
         */
        public Commit past;

        /**
         * Constructs a commit object. The unique identifier and timestamp
         * are automatically generated.
         * @param message A message describing the changes made in this commit.
         * @param past A reference to the commit made immediately before this
         *             commit.
         */
        public Commit(String message, Commit past) {
            this.id = "" + currentCommitID++;
            this.message = message;
            this.timeStamp = System.currentTimeMillis();
            this.past = past;
        }

        /**
         * Constructs a commit object with no previous commit. The unique
         * identifier and timestamp are automatically generated.
         * @param message A message describing the changes made in this commit.
         */
        public Commit(String message) {
            this(message, null);
        }

        /**
         * Returns a string representation of this commit. The string
         * representation consists of this commit's unique identifier,
         * timestamp, and message, in the following form:
         *      "[identifier] at [timestamp]: [message]"
         * @return The string representation of this collection.
         */
        @Override
        public String toString() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(timeStamp);

            return id + " at " + formatter.format(date) + ": " + message;
        }

        /**
        * Resets the IDs of the commit nodes such that they reset to 0.
        * Primarily for testing purposes.
        */
        public static void resetIds() {
            Commit.currentCommitID = 0;
        }
    }
}