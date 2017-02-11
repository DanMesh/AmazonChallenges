/*
    Amazon Challenge: Pool
    Daniel Mesham
    6 October 2016
    Challenge: http://www.adccpt.com/index.html%3Fpage_id=394.html
*/


import java.util.Scanner;
import java.util.Date;

public class Pool {

    // Variable probabilities
    private double probA = 0.25;
    private double probB = 0.3;
    private double probC;

    // The string making up the order
    private String order;
    private static String initialOrder = "AAAAAAAAAAAAAAA";

    // The mean score
    private double expectedScore = 0.0;

    // The sum of all the probabilities
    private double totalProb = 0.0;



    public Pool() {
        System.out.println("===========================================");
        System.out.println("             Amazon Challenges ");
        System.out.println("                   POOL ");
        System.out.println("             ~ Daniel Mesham ~ ");
        System.out.println("===========================================\n");

        // Get the score for Cathy (C) (assumed to be valid...)
        System.out.print("Enter the desired score: ");
        Scanner scan = new Scanner(System.in);
        Double score = scan.nextDouble();
        System.out.println();

        /* HARDCODED (for submitted code):
        Double score = 60.0;
        */

        // Set the intial probability bounds
        // i.e. the known max & min probability of Cathy potting if she scores 'score'
        // Assume 0 and 1 to account for all possible score inputs
        Double upperLimit = 1.0;
        Double lowerLimit = 0.0;

        // Keep trying probabilities in a binary search manner until the probability is accurate to 9 d.p.
        while (!String.format("%.9f", upperLimit).equals(String.format("%.9f", lowerLimit))) {
            // Get the score for a probablility halfway between the 2 limits
            Double trialProb = 0.5*(upperLimit + lowerLimit);
            Double nextScore = getScore(trialProb);

            // Display current input and result
            System.out.println(String.format("%.9f", trialProb) + " >> " + nextScore);

            // If score is higher than desired, trialProb is a new upper limit etc.
            if (nextScore > score) upperLimit = trialProb;
            else if (nextScore < score) lowerLimit = trialProb;
            // If it is equal (unlikely), you've got the answer
            else {upperLimit = trialProb; lowerLimit = upperLimit;}
        }

        System.out.println("\nANSWER: " + String.format("%.9f", upperLimit) + "\n");

        // Testing the result:
        System.out.println("The score when probC = " + upperLimit + " is:\n" + getScore(upperLimit) + "\n");

    }



    /*
        Returns Cathy's score when her chances of potting = probIn
    */
    private Double getScore(Double probIn) {
        // Initialise order, score & total probability
        order = initialOrder;
        expectedScore = 0.0;
        totalProb = 0.0;

        // Use probIn for Cathy's probability
        probC = probIn;

        // Go through every iteration and add the weighted scores
        boolean done = false;
        while (!done) {
            addWeightedScore();
            done = !incrementOrder();   // Finished if all orders have been considered
        }

        return expectedScore;
    }

    /*
        Changes the order to the next one (A->B->C)
        Returns true if not back at the start.
        Returns false if all orders have been analysed.
    */
    private boolean incrementOrder() {
        /*
            This method takes an order 'prefix', removes the last character and increments it.
            The char then gets placed at the front of a new 'suffix' for the new order.
            This happens continuously until the removed character is incremented to B or C (i.e. didn't overflow).
            The prefix + suffix is therefore the new order.
        */

        String suffix = "";         // The new suffix for the order string
        String prefix = order;      // The new prefix (starts as the old order)
        boolean change = true;      // Whether the next char has to be changed

        while (change && prefix.length()>0) {
            int endPos = prefix.length()-1;             // Position of the last character in the prefix
            char lastChar = prefix.charAt(endPos);      // The last character in the prefix
            prefix = prefix.substring(0, endPos);       // Shorten the prefix

            lastChar++;                                 // Increment the last character to the next char

            if (lastChar > 'C') {                       // If it goes beyond C, cycle back & increment the previous char too
                lastChar = 'A';
                change = true;
            }
            else {                                      // If still within the bounds, done
                change = false;
            }

            suffix = lastChar + suffix;                 // Add the new last char to the suffix
        }

        order = prefix + suffix;                        // Combine the prefix & suffix into the new order

        if (order.equals(initialOrder)) return false;   // If the order is back to all A, no more increments
        return true;                                    // Otherwise, success
    }

    /*
        Determines the score for the current order and adds it to the total score.
    */
    private void addWeightedScore() {
        int score = 0;
        double prob = 1.0;

        // Go through each letter (player) in the order
        for (int i = 1; i <= 15; i++) {
            char c = order.charAt(i-1);
            char last = 'A';                        // Default: last char assumed to be A
            if (i > 1) {
                last = order.charAt(i-2);           // Get previous char
            }
            // Get probability of this particular char/player being next and scale it
            double thisProb = fundamentalProb(last, c)*recursiveFailureProb();
            // Multiply the total probability of the current order by this probability
            prob *= thisProb;

            // If the player who sank this ball was Cathy, add her score
            if (c == 'C') score += i;

        }

        expectedScore += prob * score;              // Add the score * its probability
        totalProb += prob;
    }

    /*
        Returns the probability of b being the next to pot after a, without missing
    */
    private double fundamentalProb(char a, char b) {
        double prob = 1.0;
        if (a == 'A') {
            if      (b == 'A') prob *= probA;
            else if (b == 'B') prob *= (1-probA)*probB;
            else if (b == 'C') prob *= (1-probA)*(1-probB)*probC;
        }
        else if (a == 'B') {
            if      (b == 'B') prob *= probB;
            else if (b == 'C') prob *= (1-probB)*probC;
            else if (b == 'A') prob *= (1-probB)*(1-probC)*probA;
        }
        else if (a == 'C') {
            if      (b == 'C') prob *= probC;
            else if (b == 'A') prob *= (1-probC)*probA;
            else if (b == 'B') prob *= (1-probC)*(1-probA)*probB;
        }
        return prob;
    }


    /*
        Returns the scaling factor for the probability of a particular
        order of pottings (e.g. 'A then B') to account for the possibility
        that the players could all fail multiple times
        e.g. it could go AB, ABCAB, ABCABCAB... etc.
        This is the sum of a geometric series where the common ratio is the
        product of the probabilities of all 3 players failing.
    */
    private double recursiveFailureProb() {
        return 1/(1-(1-probA)*(1-probB)*(1-probC));
    }


    public static void main(String[] args) {
        new Pool();
    }

}
