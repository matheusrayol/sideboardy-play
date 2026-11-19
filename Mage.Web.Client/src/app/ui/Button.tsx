import { forwardRef, type AnchorHTMLAttributes, type ButtonHTMLAttributes, type ReactNode } from 'react';
import { useTx } from '../i18n';
import styles from './Button.module.css';

export type ButtonVariant =
  /** the decision in front of the player; one per screen */
  | 'decision'
  /** printed outline on the mat */
  | 'print'
  /** text-only, for secondary actions */
  | 'quiet'
  | 'danger';

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  icon?: ReactNode;
  busy?: boolean;
}

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(function Button(
  { variant = 'print', size = 'md', icon, busy, children, className, disabled, type = 'button', title, 'aria-label': ariaLabel, ...rest },
  ref,
) {
  const tx = useTx();
  const shown = typeof children === 'string' ? tx(children) : children;
  return (
    <button
      ref={ref}
      type={type}
      className={[styles.button, styles[variant], styles[size], busy ? styles.busy : '', className ?? ''].join(' ')}
      disabled={disabled || busy}
      aria-busy={busy || undefined}
      title={title ? tx(title) : title}
      aria-label={ariaLabel ? tx(ariaLabel) : ariaLabel}
      {...rest}
    >
      {icon && <span className={styles.icon} aria-hidden="true">{icon}</span>}
      {shown && <span className={styles.label}>{shown}</span>}
    </button>
  );
});

export interface ButtonLinkProps extends AnchorHTMLAttributes<HTMLAnchorElement> {
  variant?: ButtonVariant;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  icon?: ReactNode;
}

/** A link that looks like a Button, for actions that leave Playmat (a deck's page on its site). */
export const ButtonLink = forwardRef<HTMLAnchorElement, ButtonLinkProps>(function ButtonLink(
  { variant = 'print', size = 'md', icon, children, className, title, 'aria-label': ariaLabel, ...rest },
  ref,
) {
  const tx = useTx();
  const shown = typeof children === 'string' ? tx(children) : children;
  return (
    <a
      ref={ref}
      className={[styles.button, styles[variant], styles[size], className ?? ''].join(' ')}
      title={title ? tx(title) : title}
      aria-label={ariaLabel ? tx(ariaLabel) : ariaLabel}
      {...rest}
    >
      {shown && <span className={styles.label}>{shown}</span>}
      {icon && <span className={styles.icon} aria-hidden="true">{icon}</span>}
    </a>
  );
});

export interface IconButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  label: string;
  icon: ReactNode;
  pressed?: boolean;
}

/** Square button with an icon; the label is its accessible name and tooltip. */
export const IconButton = forwardRef<HTMLButtonElement, IconButtonProps>(function IconButton(
  { label, icon, pressed, className, type = 'button', ...rest },
  ref,
) {
  const tx = useTx();
  const shownLabel = tx(label);
  return (
    <button
      ref={ref}
      type={type}
      aria-label={shownLabel}
      title={shownLabel}
      aria-pressed={pressed}
      className={[styles.iconButton, pressed ? styles.pressed : '', className ?? ''].join(' ')}
      {...rest}
    >
      <span aria-hidden="true">{icon}</span>
    </button>
  );
});
