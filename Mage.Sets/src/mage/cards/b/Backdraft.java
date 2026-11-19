package mage.cards.b;

import mage.MageObject;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.choices.Choice;
import mage.choices.ChoiceImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.WatcherScope;
import mage.constants.Zone;
import mage.filter.FilterPlayer;
import mage.filter.predicate.ObjectSourcePlayer;
import mage.filter.predicate.ObjectSourcePlayerPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.target.TargetPlayer;
import mage.watchers.Watcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class Backdraft extends CardImpl {

    public Backdraft(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{R}");

        // Choose a player who cast one or more sorcery spells this turn. Backdraft deals damage to that player equal to half the damage dealt by one of those sorcery spells this turn, rounded down.
        this.getSpellAbility().addEffect(new BackdraftEffect());
        this.getSpellAbility().addWatcher(new BackdraftWatcher());
    }

    private Backdraft(final Backdraft card) {
        super(card);
    }

    @Override
    public Backdraft copy() {
        return new Backdraft(this);
    }
}

class BackdraftEffect extends OneShotEffect {

    BackdraftEffect() {
        super(Outcome.Damage);
        staticText = "Choose a player who cast one or more sorcery spells this turn. "
                + "{this} deals damage to that player equal to half the damage dealt by one of those sorcery spells this turn, rounded down.";
    }

    private BackdraftEffect(final BackdraftEffect effect) {
        super(effect);
    }

    @Override
    public BackdraftEffect copy() {
        return new BackdraftEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        BackdraftWatcher watcher = game.getState().getWatcher(BackdraftWatcher.class);
        if (controller == null || watcher == null) {
            return false;
        }
        FilterPlayer filter = new FilterPlayer("player who cast one or more sorcery spells this turn");
        filter.add(BackdraftPlayerPredicate.instance);
        TargetPlayer target = new TargetPlayer(1, 1, true, filter);
        if (!target.canChoose(controller.getId(), source, game)) {
            return true;
        }
        if (!target.choose(Outcome.Damage, controller.getId(), source.getSourceId(), source, game)) {
            return false;
        }
        Player chosenPlayer = game.getPlayer(target.getFirstTarget());
        if (chosenPlayer == null) {
            return false;
        }
        List<BackdraftWatcher.SorceryRecord> records = watcher.getSorceriesByPlayer(chosenPlayer.getId());
        if (records.isEmpty()) {
            return false;
        }
        int selectedDamage;
        if (records.size() == 1) {
            selectedDamage = records.get(0).damageDone;
        } else {
            Choice choice = new ChoiceImpl(true);
            choice.setMessage("Choose one of " + chosenPlayer.getLogName() + "'s sorcery spells cast this turn");
            Map<String, Integer> damageByLabel = new LinkedHashMap<>();
            for (int i = 0; i < records.size(); i++) {
                BackdraftWatcher.SorceryRecord rec = records.get(i);
                String label = (i + 1) + ": " + rec.name + " (" + rec.damageDone + " damage dealt)";
                choice.getChoices().add(label);
                damageByLabel.put(label, rec.damageDone);
            }
            if (!controller.choose(Outcome.Damage, choice, game)) {
                return false;
            }
            selectedDamage = damageByLabel.getOrDefault(choice.getChoice(), 0);
        }
        int damage = selectedDamage / 2;
        if (damage > 0) {
            chosenPlayer.damage(damage, source.getSourceId(), source, game);
        }
        return true;
    }
}

enum BackdraftPlayerPredicate implements ObjectSourcePlayerPredicate<Player> {
    instance;

    @Override
    public boolean apply(ObjectSourcePlayer<Player> input, Game game) {
        BackdraftWatcher watcher = game.getState().getWatcher(BackdraftWatcher.class);
        return watcher != null && !watcher.getSorceriesByPlayer(input.getObject().getId()).isEmpty();
    }
}

class BackdraftWatcher extends Watcher {

    static final class SorceryRecord {
        final UUID spellId;
        final UUID sourceId;
        final MageObjectReference mor;
        final String name;
        int damageDone;

        SorceryRecord(UUID spellId, UUID sourceId, MageObjectReference mor, String name) {
            this.spellId = spellId;
            this.sourceId = sourceId;
            this.mor = mor;
            this.name = name;
            this.damageDone = 0;
        }

        SorceryRecord copy() {
            SorceryRecord clone = new SorceryRecord(spellId, sourceId, mor, name);
            clone.damageDone = damageDone;
            return clone;
        }
    }

    private final Map<UUID, List<SorceryRecord>> sorceriesByPlayer = new HashMap<>();

    BackdraftWatcher() {
        super(WatcherScope.GAME);
    }

    @Override
    public void watch(GameEvent event, Game game) {
        switch (event.getType()) {
            case SPELL_CAST: {
                Spell spell = game.getStack().getSpell(event.getTargetId());
                if (spell == null) {
                    MageObject obj = game.getLastKnownInformation(event.getTargetId(), Zone.STACK);
                    if (obj instanceof Spell) {
                        spell = (Spell) obj;
                    }
                }
                if (spell != null && spell.isSorcery(game)) {
                    MageObjectReference mor = new MageObjectReference(spell.getSourceId(), spell.getZoneChangeCounter(game), game);
                    sorceriesByPlayer
                            .computeIfAbsent(spell.getControllerId(), k -> new ArrayList<>())
                            .add(new SorceryRecord(spell.getId(), spell.getSourceId(), mor, spell.getName()));
                }
                break;
            }
            case DAMAGED_PERMANENT:
            case DAMAGED_PLAYER: {
                if (event.getAmount() <= 0 || sorceriesByPlayer.isEmpty()) {
                    break;
                }
                StackObject top = game.getStack().getFirstOrNull();
                if (top instanceof Spell) {
                    for (List<SorceryRecord> records : sorceriesByPlayer.values()) {
                        for (SorceryRecord rec : records) {
                            if (rec.spellId.equals(top.getId()) && rec.sourceId.equals(event.getSourceId())) {
                                rec.damageDone += event.getAmount();
                                return;
                            }
                        }
                    }
                }
                MageObjectReference sourceRef = new MageObjectReference(event.getSourceId(), game);
                for (List<SorceryRecord> records : sorceriesByPlayer.values()) {
                    for (int i = records.size() - 1; i >= 0; i--) {
                        SorceryRecord rec = records.get(i);
                        if (rec.mor.equals(sourceRef)) {
                            rec.damageDone += event.getAmount();
                            return;
                        }
                    }
                }
                break;
            }
        }
    }

    List<SorceryRecord> getSorceriesByPlayer(UUID playerId) {
        return sorceriesByPlayer.getOrDefault(playerId, Collections.emptyList());
    }

    @Override
    public void reset() {
        super.reset();
        sorceriesByPlayer.clear();
    }
}
