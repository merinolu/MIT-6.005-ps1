/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

    /*
     * TODO: your testing strategies for these methods should go here.
     * See the ic03-testing exercise for examples of what a testing strategy comment looks like.
     * Make sure you have partitions.
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant beforeEpoch = Instant.parse("1901-02-17T11:00:00Z");
    private static final Instant afterEpoch = Instant.parse("2001-12-31T11:00:00Z");
    private static final Instant future = Instant.now().plusSeconds(86400); //86400 is the seconds in a day
        
    
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    /*
     * Testing strategy getTimespan
     * 
     * Partition the inputs as follows:
     * 
     * tweets.size(): 0, 1, >1
     * 
     * for tweet in tweets, tweet.getTimeStamp() satisfies: Instant.MIN, <Instant.Epoch, 
     * Instant.EPOCH, >Instant.Epoch, > Instant.now(), Instant.MAX
     * 
     * Cover all cases
     */
    
    
    @Test
    public void testGetTimespanTwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }
    
    // covers tweets.size() == 0
    @Test
    public void testGetTimeNoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList());
        
        assertTrue("expected a timespan", timespan instanceof Timespan);
    }
    //Covers tweets.size() == 1 and tweet.getTimestamp() = Instant.EPOCH
    @Test
    public void testGetTimespanOneTweet() {
        final Tweet tweet = new Tweet(3, "tweet epoch", "this tweet posted at epoch", Instant.EPOCH);
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet));
        assertEquals("expected epoch start", Instant.EPOCH, timespan.getStart());
        assertEquals("expected epoch end", Instant.EPOCH, timespan.getEnd());
        
    }
    
    //covers tweets.size() > 1 and tweet.getTimestamp() = Instant.MIN, Instant.MAX, < Instant.EPOCH
    @Test
    public void testGetTimespanMinMax() {
        final Tweet tweetMin = new Tweet(3, "tweet min", "this tweet posted at min", Instant.MIN);
        final Tweet tweetMax = new Tweet(4, "tweet max", "this tweet posted at max", Instant.MAX);
        final Tweet tweetBeforeEpoch = new Tweet(5, "tweet before Epoch", "this tweet posted at before Epoch", beforeEpoch);
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweetBeforeEpoch, tweetMax, tweetMin));
        assertEquals("expected min start", Instant.MIN, timespan.getStart());
        assertEquals("expected max end", Instant.MAX, timespan.getEnd());
    }
    
  //covers tweets.size() > 1 and tweet.getTimestamp() = > Instant.EPOCH, > Instant.now(),
    @Test
    public void testGetTimespanFuture() {
        final Tweet tweetafterEpoch = new Tweet(3, "tweet after epoch", "this tweet posted after epoch", afterEpoch);
        final Tweet tweetFuture = new Tweet(4, "tweet future", "this tweet posted at future", future);
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweetafterEpoch, tweetFuture));
        assertEquals("expected after epoch start", afterEpoch, timespan.getStart());
        assertEquals("expected future end", future, timespan.getEnd());
    }
    
    
    /*
     * Test strategy getMentionedUsers
     * 
     * Partition the inputs as follows:
     * tweets.size(): 0, 1, >1
     * For tweet in tweets, tweet.getText(): has no references, has one reference, 
     * has more than one reference, has an email address, has a reference at the beginning, has a reference at the end 
     * has an @ that is not part of a valid reference, has a repeated reference
     * 
     * Cover all cases
     */
    
    //covers tweets.size() = 1 and tweet.getText() has no references
    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1));
        
        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }
    
    //covers tweets.size() = 0
    @Test
    public void testGetMentionedUsersNoUsers() {
        
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList());
        assertTrue("expected empty set from no users", mentionedUsers.isEmpty());
    }
    //covers tweets.size() = 1 and tweet.getText() has one reference
    public void testGetMentionedUsersOneUserOneMention() {
        final Tweet tweetOneMention = new Tweet(3, "tweet one mention", "this tweet posted with @OneMention", afterEpoch);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweetOneMention));
        Set<String> expected = new HashSet<String>();
        expected.add("onemention");
        assertEquals("expected same size sets", expected.size(),mentionedUsers.size());
        for (String mention: mentionedUsers) {
            assertTrue("unexpected set element " + mention, expected.contains(mention.toLowerCase()));
        }
        
    }
    
    //covers tweets.size() > 1 and tweet.getText() has more than one reference,
    //has a reference at the beginning, has an email
    @Test
    public void testGetMentionedUsersTwoUsersMultipleMentions() {
        final Tweet tweetTwoMentions = new Tweet(3, "tweet two mentions", "@OneOfTwo this tweet posted @TwoOfTwo with two mentions", beforeEpoch);
        final Tweet tweetEmails = new Tweet(4, "tweet emails", "tweet@email.com and tweet@2ndemail.com", future);
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweetTwoMentions,tweetEmails));
        Set<String> expected = new HashSet<String>();
        expected.add("oneoftwo");
        expected.add("twooftwo");
        assertEquals("expected same size sets", expected.size(),mentionedUsers.size());
        for (String mention: mentionedUsers) {
            assertTrue("unexpected set element " + mention, expected.contains(mention.toLowerCase()));
        }
    }
    
    //covers tweet.getText() has an @ not part of a valid reference, has a repeated reference
    @Test
    public void testGetMentionedUsersInvalidRepeatedReference() {
     
            final Tweet tweetInvalid = new Tweet(3, "tweet invalid and one ref", "Then message has a ty@po and a @OneMention", afterEpoch);
            final Tweet tweetRepeated = new Tweet(4, "tweet repeated", "message with a repeated @oNEmENTION", Instant.MAX);
            Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweetInvalid,tweetRepeated));
            Set<String> expected = new HashSet<String>();
            expected.add("onemention");
            assertEquals("expected same size sets", expected.size(),mentionedUsers.size());
            for (String mention: mentionedUsers) {
                assertTrue("unexpected set element " + mention, expected.contains(mention.toLowerCase()));
            }
    }

    /*
     * Warning: all the tests you write here must be runnable against any
     * Extract class that follows the spec. It will be run against several staff
     * implementations of Extract, which will be done by overwriting
     * (temporarily) your version of Extract with the staff's version.
     * DO NOT strengthen the spec of Extract or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Extract, because that means you're testing a
     * stronger spec than Extract says. If you need such helper methods, define
     * them in a different class. If you only need them in this test class, then
     * keep them in this test class.
     */

}
