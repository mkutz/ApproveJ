package org.approvej.approve;

public record InplaceApprovalResult(String received, String previouslyApproved)
    implements ApprovalResult {}
