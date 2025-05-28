package org.approvej.approve;

public record FileApprovalResult(
    String received, String previouslyApproved, PathProvider pathProvider)
    implements ApprovalResult {}
