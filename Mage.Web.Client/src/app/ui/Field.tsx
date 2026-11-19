import { forwardRef, useId, type InputHTMLAttributes, type ReactNode } from 'react';
import { useTx } from '../i18n';
import styles from './Field.module.css';

export interface FieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  hint?: ReactNode;
  error?: string | null;
}

/** Labeled text input printed on the mat. */
export const Field = forwardRef<HTMLInputElement, FieldProps>(function Field(
  { label, hint, error, placeholder, className, id, ...rest },
  ref,
) {
  const tx = useTx();
  const generatedId = useId();
  const inputId = id ?? generatedId;
  const hintId = `${inputId}-hint`;
  const shownHint = typeof hint === 'string' ? tx(hint) : hint;
  const shownError = typeof error === 'string' ? tx(error) : error;
  return (
    <div className={[styles.field, className ?? ''].join(' ')}>
      <label htmlFor={inputId} className={styles.label}>{tx(label)}</label>
      <input
        ref={ref}
        id={inputId}
        className={styles.input}
        placeholder={placeholder ? tx(placeholder) : placeholder}
        aria-invalid={shownError ? true : undefined}
        aria-describedby={shownHint || shownError ? hintId : undefined}
        {...rest}
      />
      {(shownError || shownHint) && (
        <p id={hintId} className={shownError ? styles.error : styles.hint}>{shownError ?? shownHint}</p>
      )}
    </div>
  );
});
