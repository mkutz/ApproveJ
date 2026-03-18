package org.approvej.image.approve;

import java.awt.image.BufferedImage;
import java.util.function.Function;
import org.approvej.image.ImageApprovalResult;

/** A {@link Function} that evaluates a received image against an approved baseline. */
public interface ImageApprover extends Function<BufferedImage, ImageApprovalResult> {}
