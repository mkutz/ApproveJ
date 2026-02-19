package org.approvej.approve;

import org.jspecify.annotations.NullMarked;

/** An entry in the approved file inventory, mapping a file path to its originating test method. */
@NullMarked
record InventoryEntry(String relativePath, String testReference) {}
