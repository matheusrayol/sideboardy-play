import { registerMessages, type Translate } from '../i18n';
import messages from '../i18n/en/match';

// the match stage's English, registered when the first of its parts loads (with the game or the build screen)
registerMessages(messages);

const INTERACTION_LABELS: Record<string, Parameters<Translate>[0]> = {
  Pass: 'match.action.pass',
  Resolve: 'match.action.resolve',
  Next: 'match.action.next',
  'To Combat': 'match.action.toCombat',
  'End Turn': 'match.action.endTurn',
  'No Attacks': 'match.action.noAttacks',
  'All attack': 'match.action.allAttack',
  'No Blocks': 'match.action.noBlocks',
  Done: 'match.action.done',
  Cancel: 'match.ability.cancel',
  'Assign damage': 'match.action.assignDamage',
  'Undo tap': 'match.action.undoTap',
  'Special action': 'match.action.special',
};

/** Traduz rótulos de botão gerados pela camada pura de interação sem alterar a detecção de atalhos. */
export function translateInteractionLabel(label: string, t: Translate): string {
  const exact = INTERACTION_LABELS[label];
  if (exact) return t(exact);
  const attack = /^Attack \((\d+)\)$/.exec(label);
  if (attack) return t('match.action.attackCount', { count: Number(attack[1]) });
  const block = /^Block \((\d+)\)$/.exec(label);
  if (block) return t('match.action.blockCount', { count: Number(block[1]) });
  return label;
}

/** Traduz manchetes padrão geradas por deriveInteraction mantendo prompts do servidor intactos. */
export function translateInteractionHeadline(headline: string, t: Translate): string {
  switch (headline) {
    case 'Choose attackers':
      return t('match.action.chooseAttackers');
    case 'Choose blockers':
      return t('match.action.chooseBlockers');
    case 'Assign combat damage':
      return t('match.action.assignCombatDamage');
    case 'Make a choice':
      return t('match.action.makeChoice');
    case 'Waiting':
      return t('match.action.waiting');
    default: {
      const assign = /^Assign (.+)'s combat damage$/.exec(headline);
      if (assign) return t('match.action.assignSourceDamage', { name: assign[1] });
      const waitingFor = /^Waiting for (.+)$/.exec(headline);
      if (waitingFor) return t('match.action.waitingFor', { name: waitingFor[1] });
      return headline;
    }
  }
}
