import * as DropdownMenu from '@radix-ui/react-dropdown-menu';
import { Check, ChevronDown } from 'lucide-react';
import {
  Children,
  Fragment,
  isValidElement,
  useMemo,
  type CSSProperties,
  type OptgroupHTMLAttributes,
  type OptionHTMLAttributes,
  type ReactNode,
} from 'react';
import { useTx, type PhraseTranslator } from '../i18n';
import styles from './Select.module.css';

export interface SelectChangeEvent {
  target: { value: string };
}

export interface SelectProps {
  value: string | number;
  onChange?: (event: SelectChangeEvent) => void;
  onValueChange?: (value: string) => void;
  disabled?: boolean;
  className?: string;
  style?: CSSProperties;
  id?: string;
  'aria-label'?: string;
  title?: string;
  placeholder?: string;
  side?: 'top' | 'bottom';
  align?: 'start' | 'center' | 'end';
  children?: ReactNode;
}

interface ParsedOption {
  kind: 'option';
  value: string;
  label: ReactNode;
  text: string;
  disabled?: boolean;
  lang?: string;
}

interface ParsedGroup {
  kind: 'group';
  label: string;
  options: ParsedOption[];
}

type ParsedEntry = ParsedOption | ParsedGroup;

function textOf(node: ReactNode): string {
  if (typeof node === 'string' || typeof node === 'number') return String(node);
  if (Array.isArray(node)) return node.map(textOf).join('');
  if (isValidElement<{ children?: ReactNode }>(node)) return textOf(node.props.children);
  return '';
}

function collectEntries(children: ReactNode): ParsedEntry[] {
  const entries: ParsedEntry[] = [];
  Children.forEach(children, (child) => {
    if (!isValidElement(child)) return;
    if (child.type === Fragment) {
      entries.push(...collectEntries((child.props as { children?: ReactNode }).children));
      return;
    }
    if (child.type === 'option') {
      const props = child.props as OptionHTMLAttributes<HTMLOptionElement>;
      const text = textOf(props.children);
      const value = props.value !== undefined ? String(props.value) : text;
      entries.push({
        kind: 'option',
        value,
        label: props.children,
        text,
        disabled: props.disabled,
        lang: props.lang,
      });
      return;
    }
    if (child.type === 'optgroup') {
      const props = child.props as OptgroupHTMLAttributes<HTMLOptGroupElement>;
      const options = collectEntries(props.children).filter((entry): entry is ParsedOption => entry.kind === 'option');
      entries.push({
        kind: 'group',
        label: props.label ?? '',
        options,
      });
    }
  });
  return entries;
}

function renderOptionItem(option: ParsedOption, tx: PhraseTranslator) {
  const shown = typeof option.label === 'string' ? tx(option.label) : option.label;
  return (
    <DropdownMenu.RadioItem
      key={option.value}
      value={option.value}
      disabled={option.disabled}
      lang={option.lang}
      className={styles.item}
    >
      <span>{shown}</span>
      <DropdownMenu.ItemIndicator className={styles.check}>
        <Check size={16} aria-hidden="true" />
      </DropdownMenu.ItemIndicator>
    </DropdownMenu.RadioItem>
  );
}

/** Accessible custom dropdown built on Radix DropdownMenu with a drop-in <option>/<optgroup> API. */
export function Select({
  value,
  onChange,
  onValueChange,
  disabled,
  className,
  style,
  id,
  'aria-label': ariaLabel,
  title,
  placeholder = '',
  side = 'bottom',
  align = 'start',
  children,
}: SelectProps) {
  const tx = useTx();
  const current = String(value);
  const entries = useMemo(() => collectEntries(children), [children]);
  const selected = useMemo(() => {
    for (const entry of entries) {
      if (entry.kind === 'option' && entry.value === current) return entry;
      if (entry.kind === 'group') {
        const hit = entry.options.find((option) => option.value === current);
        if (hit) return hit;
      }
    }
    return undefined;
  }, [entries, current]);

  function handleChange(next: string) {
    onValueChange?.(next);
    onChange?.({ target: { value: next } });
  }

  return (
    <DropdownMenu.Root>
      <DropdownMenu.Trigger asChild disabled={disabled}>
        <button
          id={id}
          type="button"
          disabled={disabled}
          aria-label={ariaLabel ? tx(ariaLabel) : ariaLabel}
          title={title ? tx(title) : title}
          data-a11y-dynamic-name=""
          className={[styles.trigger, className ?? ''].join(' ').trim()}
          style={style}
        >
          <span className={styles.label} lang={selected?.lang}>
            {selected ? tx(selected.text) : placeholder ? tx(placeholder) : placeholder}
          </span>
          <ChevronDown className={styles.chevron} size={16} aria-hidden="true" />
        </button>
      </DropdownMenu.Trigger>
      <DropdownMenu.Portal>
        <DropdownMenu.Content className={styles.menu} side={side} align={align} sideOffset={6} collisionPadding={12}>
          <DropdownMenu.RadioGroup value={current} onValueChange={handleChange}>
            {entries.map((entry, index) => {
              if (entry.kind === 'option') return renderOptionItem(entry, tx);
              return (
                <Fragment key={`${entry.label}-${index}`}>
                  {index > 0 && <DropdownMenu.Separator className={styles.rule} />}
                  {entry.label && <DropdownMenu.Label className={styles.groupLabel}>{tx(entry.label)}</DropdownMenu.Label>}
                  {entry.options.map((option) => renderOptionItem(option, tx))}
                </Fragment>
              );
            })}
          </DropdownMenu.RadioGroup>
        </DropdownMenu.Content>
      </DropdownMenu.Portal>
    </DropdownMenu.Root>
  );
}
