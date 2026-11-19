/**
 * Official Sideboardy symbol: three fanned cards (two cream side cards rotated ±20° around (32, 58)
 * and the front card in brand orange #F4A340).
 */
export function Mark({ size = 28, title }: { size?: number; title?: string }) {
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" role={title ? 'img' : undefined} aria-label={title} aria-hidden={title ? undefined : true}>
      <g transform="rotate(-20 32 58)">
        <rect x="21" y="12" width="22" height="34" rx="4" fill="var(--ink, #f7f5f0)" stroke="var(--mat-900, #141a2e)" strokeWidth="2.5" />
      </g>
      <g transform="rotate(20 32 58)">
        <rect x="21" y="12" width="22" height="34" rx="4" fill="var(--ink, #f7f5f0)" stroke="var(--mat-900, #141a2e)" strokeWidth="2.5" />
      </g>
      <rect x="21" y="10" width="22" height="34" rx="4" fill="var(--decision, #f4a340)" stroke="var(--mat-900, #141a2e)" strokeWidth="2.5" />
    </svg>
  );
}
