package org.approvej.approve;

import org.approvej.ApprovalResult;

/**
 * {@link ApprovalResult} for files.
 *
 * @param previouslyApproved the previously approved value
 * @param received the received value
 * @param pathProvider the {@link PathProvider} providing the paths to the received an approved
 *     files
 */
public record FileApprovalResult(
    String previouslyApproved, String received, PathProvider pathProvider)
    implements ApprovalResult {}
