export function formatScore(value: number | undefined | null): string {
  if (value === undefined || value === null) {
    return '0.00';
  }
  return Number(value).toFixed(2);
}
