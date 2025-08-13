package org.approvej.approve;

import org.approvej.ApprovalResult;

/**
 * A simple {@link ApprovalResult}, e.g. for an {@link InplaceApprover}.
 *
 * @param previouslyApproved the value that was previously approved
 * @param received the value that was received for approval
 */
public record InplaceApprovalResult(String previouslyApproved, String received)
    implements ApprovalResult {}
