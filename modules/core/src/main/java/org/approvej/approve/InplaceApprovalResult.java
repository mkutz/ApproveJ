package org.approvej.approve;

import org.approvej.ApprovalResult;

/**
 * A simple {@link ApprovalResult}, e.g. for an {@link InplaceApprover}.
 *
 * @param received the value that was received for approval
 * @param previouslyApproved the value that was previously approved
 */
public record InplaceApprovalResult(String received, String previouslyApproved)
    implements ApprovalResult {}
