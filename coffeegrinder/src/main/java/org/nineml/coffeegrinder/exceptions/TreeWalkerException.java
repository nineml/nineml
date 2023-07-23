package org.nineml.coffeegrinder.exceptions;

import org.nineml.coffeegrinder.trees.Axe;

/**
 * Tree walking exceptions.
 * <p>Tree walking exceptions are generally errors in the implementation of the tree walker.</p>
 */
public class TreeWalkerException extends CoffeeGrinderException {
    /**
     * Grammar exception with a message.
     * @param code the code
     * @param message the message
     */
    public TreeWalkerException(String code, String message) {
        super(code, message);
    }

    /**
     * Grammar exception with an underlying cause.
     * @param code the code
     * @param message the message
     * @param cause the cause
     */
    public TreeWalkerException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    private static TreeWalkerException getException(String code) {
        return new TreeWalkerException(code, MessageGenerator.getMessage(code));
    }

    /**
     * Raised if a {@link Axe#select} function returns an
     * empty list.
     * @return a TreeWalkerException
     */
    public static TreeWalkerException noChoiceSelected() { return getException("T001"); }

    /**
     * Raised if an attempt is made to walk a tree when there are no more trees.
     * @return a TreeWalkerException
     */
    public static TreeWalkerException noMoreTrees() { return getException("T002"); }

    /**
     * Raised if an internal error occurs.
     * @return a TreeWalkerException
     */
    public static TreeWalkerException internalError() { return getException("T003"); }

    /**
     * Raised if a {@link Axe#select} function returns an
     * any elements not taken from the list of choices offered or any duplicates in the list of
     * potential future choices.
     * @return a TreeWalkerException
     */
    public static TreeWalkerException invalidChoiceSelected() { return getException("T004"); }

    /**
     * Raised if an attempt is made to walk a tree when there are no trees. (The parse failed.)
     * @return a TreeWalkerException
     */
    public static TreeWalkerException noTrees() { return getException("T005"); }

}
