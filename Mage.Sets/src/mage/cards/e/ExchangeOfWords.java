package mage.cards.e;

import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class ExchangeOfWords extends CardImpl {

    public ExchangeOfWords(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{U}{U}");

        // When this enchantment enters, choose two target creatures. For as long as this enchantment remains on the battlefield, exchange the text boxes of those creatures.
        Ability ability = new EntersBattlefieldTriggeredAbility(new ExchangeOfWordsEffect());
        ability.addTarget(new TargetCreaturePermanent(2, 2));
        this.addAbility(ability);
    }

    private ExchangeOfWords(final ExchangeOfWords card) {
        super(card);
    }

    @Override
    public ExchangeOfWords copy() {
        return new ExchangeOfWords(this);
    }
}

class ExchangeOfWordsEffect extends OneShotEffect {

    ExchangeOfWordsEffect() {
        super(Outcome.Neutral);
        staticText = "choose two target creatures. For as long as {this} remains on the battlefield, "
                + "exchange the text boxes of those creatures";
    }

    private ExchangeOfWordsEffect(final ExchangeOfWordsEffect effect) {
        super(effect);
    }

    @Override
    public ExchangeOfWordsEffect copy() {
        return new ExchangeOfWordsEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        List<UUID> targets = source.getTargets().isEmpty() ? Collections.emptyList() : source.getTargets().get(0).getTargets();
        if (targets.size() < 2) {
            return false;
        }
        Permanent p1 = game.getPermanent(targets.get(0));
        Permanent p2 = game.getPermanent(targets.get(1));
        if (p1 == null || p2 == null) {
            return false;
        }
        List<Ability> abilities1 = new ArrayList<>();
        for (Ability ab : p1.getAbilities(game)) {
            abilities1.add(ab.copy());
        }
        List<Ability> abilities2 = new ArrayList<>();
        for (Ability ab : p2.getAbilities(game)) {
            abilities2.add(ab.copy());
        }
        game.addEffect(new ExchangeOfWordsContinuousEffect(
                new MageObjectReference(p1, game),
                new MageObjectReference(p2, game),
                abilities1,
                abilities2
        ), source);
        return true;
    }
}

class ExchangeOfWordsContinuousEffect extends ContinuousEffectImpl {

    private final MageObjectReference mor1;
    private final MageObjectReference mor2;
    private final List<Ability> abilities1;
    private final List<Ability> abilities2;

    ExchangeOfWordsContinuousEffect(
            MageObjectReference mor1,
            MageObjectReference mor2,
            List<Ability> abilities1,
            List<Ability> abilities2
    ) {
        super(Duration.Custom, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.Neutral);
        this.mor1 = mor1;
        this.mor2 = mor2;
        this.abilities1 = new ArrayList<>(abilities1);
        this.abilities2 = new ArrayList<>(abilities2);
    }

    private ExchangeOfWordsContinuousEffect(final ExchangeOfWordsContinuousEffect effect) {
        super(effect);
        this.mor1 = effect.mor1;
        this.mor2 = effect.mor2;
        this.abilities1 = new ArrayList<>(effect.abilities1.size());
        for (Ability ab : effect.abilities1) {
            this.abilities1.add(ab.copy());
        }
        this.abilities2 = new ArrayList<>(effect.abilities2.size());
        for (Ability ab : effect.abilities2) {
            this.abilities2.add(ab.copy());
        }
    }

    @Override
    public ExchangeOfWordsContinuousEffect copy() {
        return new ExchangeOfWordsContinuousEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (source.getSourcePermanentIfItStillExists(game) == null) {
            discard();
            return false;
        }
        Permanent p1 = mor1.getPermanent(game);
        if (p1 != null) {
            p1.removeAllAbilities(source.getSourceId(), game);
            for (Ability ab : abilities2) {
                p1.addAbility(ab.copy(), source.getSourceId(), game);
            }
        }
        Permanent p2 = mor2.getPermanent(game);
        if (p2 != null) {
            p2.removeAllAbilities(source.getSourceId(), game);
            for (Ability ab : abilities1) {
                p2.addAbility(ab.copy(), source.getSourceId(), game);
            }
        }
        return true;
    }
}
