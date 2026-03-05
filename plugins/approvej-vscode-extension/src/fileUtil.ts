const APPROVED_INFIX = "-approved";
const RECEIVED_INFIX = "-received";

function findInfixIndex(filename: string, infix: string): number {
  const index = filename.lastIndexOf(infix);
  if (index <= 0) return -1;
  const suffix = filename.substring(index + infix.length);
  if (suffix === "" || suffix.startsWith(".")) return index;
  return -1;
}

export function isApprovedFileName(filename: string): boolean {
  return findInfixIndex(filename, APPROVED_INFIX) > 0;
}

export function isReceivedFileName(filename: string): boolean {
  return findInfixIndex(filename, RECEIVED_INFIX) > 0;
}

export function toApprovedFileName(
  receivedFilename: string,
): string | undefined {
  const index = findInfixIndex(receivedFilename, RECEIVED_INFIX);
  if (index < 0) return undefined;
  return (
    receivedFilename.substring(0, index) +
    APPROVED_INFIX +
    receivedFilename.substring(index + RECEIVED_INFIX.length)
  );
}

export function toReceivedFileName(
  approvedFilename: string,
): string | undefined {
  const index = findInfixIndex(approvedFilename, APPROVED_INFIX);
  if (index < 0) return undefined;
  return (
    approvedFilename.substring(0, index) +
    RECEIVED_INFIX +
    approvedFilename.substring(index + APPROVED_INFIX.length)
  );
}

export function toBaseFileName(
  receivedFilename: string,
): string | undefined {
  const index = findInfixIndex(receivedFilename, RECEIVED_INFIX);
  if (index < 0) return undefined;
  return (
    receivedFilename.substring(0, index) +
    receivedFilename.substring(index + RECEIVED_INFIX.length)
  );
}

export function approvedFileNameCandidates(
  receivedFilename: string,
): string[] {
  const approved = toApprovedFileName(receivedFilename);
  const base = toBaseFileName(receivedFilename);
  if (!approved || !base) return [];
  return [approved, base];
}
