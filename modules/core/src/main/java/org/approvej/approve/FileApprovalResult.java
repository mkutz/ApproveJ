package org.approvej.approve;

import org.approvej.ApprovalResult;

/**
 * {@link ApprovalResult} for files.
 *
 * @param received the received value
 * @param previouslyApproved the previously approved value
 * @param pathProvider the {@link PathProvider} providing the paths to the received an approved
 *     files
 */
public record FileApprovalResult(
    String received, String previouslyApproved, PathProvider pathProvider)
    implements ApprovalResult {}
