package org.approvej.image.approve;

import java.awt.image.BufferedImage;
import java.util.function.Function;
import org.approvej.image.ImageApprovalResult;

public interface ImageApprover extends Function<BufferedImage, ImageApprovalResult> {}
