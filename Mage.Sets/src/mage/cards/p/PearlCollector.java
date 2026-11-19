package mage.cards.p;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleActivatedAbility;
import mage.abilities.condition.common.YouGainedLifeCondition;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ConjureCardEffect;
import mage.abilities.keyword.DeathtouchAbility;
import mage.abilities.keyword.LifelinkAbility;
import mage.abilities.triggers.BeginningOfSecondMainTriggeredAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.TargetPermanent;
import mage.watchers.common.PlayerGainedLifeWatcher;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class PearlCollector extends CardImpl {

    public PearlCollector(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{B}");

        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.WARLOCK);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Deathtouch
        this.addAbility(DeathtouchAbility.getInstance());

        // Lifelink
        this.addAbility(LifelinkAbility.getInstance());

        // At the beginning of your second main phase, if you gained 4 or more life this turn, conjure a card named Mox Pearl into your hand. This ability triggers only once.
        BeginningOfSecondMainTriggeredAbility mainAbility = new BeginningOfSecondMainTriggeredAbility(
                new ConjureCardEffect("Mox Pearl"), false
        );
        mainAbility.withInterveningIf(new YouGainedLifeCondition(ComparisonType.MORE_THAN, 3));
        mainAbility.addWatcher(new PlayerGainedLifeWatcher());
        mainAbility.setTriggersLimitEachGame(1);
        this.addAbility(mainAbility);

        // {2}{W}: Another target creature perpetually gains lifelink.
        Ability activated = new SimpleActivatedAbility(
                new PearlCollectorPerpetualLifelinkEffect(),
                new ManaCostsImpl<>("{2}{W}")
        );
        activated.addTarget(new TargetPermanent(StaticFilters.FILTER_ANOTHER_TARGET_CREATURE));
        this.addAbility(activated);
    }

    private PearlCollector(final PearlCollector card) {
        super(card);
    }

    @Override
    public PearlCollector copy() {
        return new PearlCollector(this);
    }
}

class PearlCollectorPerpetualLifelinkEffect extends OneShotEffect {

    private static final String KEY = "perpetualLifelink|";

    PearlCollectorPerpetualLifelinkEffect() {
        super(Outcome.AddAbility);
        this.staticText = "another target creature perpetually gains lifelink";
    }

    private PearlCollectorPerpetualLifelinkEffect(final PearlCollectorPerpetualLifelinkEffect effect) {
        super(effect);
    }

    @Override
    public PearlCollectorPerpetualLifelinkEffect copy() {
        return new PearlCollectorPerpetualLifelinkEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent target = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (target == null) {
            return false;
        }
        Card mainCard = game.getCard(target.getMainCard().getId());
        UUID cardId = mainCard != null ? mainCard.getId() : target.getId();
        String key = KEY + cardId;
        if (!Boolean.TRUE.equals(game.getState().getValue(key))) {
            game.getState().setValue(key, Boolean.TRUE);
            game.addEffect(new PearlCollectorPerpetualContinuousEffect(cardId), source);
        }
        return true;
    }

    static boolean hasPerpetualLifelink(UUID cardId, Game game) {
        return Boolean.TRUE.equals(game.getState().getValue(KEY + cardId));
    }
}

class PearlCollectorPerpetualContinuousEffect extends ContinuousEffectImpl {

    private final UUID cardId;

    PearlCollectorPerpetualContinuousEffect(UUID cardId) {
        super(Duration.Custom, Layer.AbilityAddingRemovingEffects_6, SubLayer.NA, Outcome.AddAbility);
        this.cardId = cardId;
    }

    private PearlCollectorPerpetualContinuousEffect(final PearlCollectorPerpetualContinuousEffect effect) {
        super(effect);
        this.cardId = effect.cardId;
    }

    @Override
    public PearlCollectorPerpetualContinuousEffect copy() {
        return new PearlCollectorPerpetualContinuousEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        if (!PearlCollectorPerpetualLifelinkEffect.hasPerpetualLifelink(cardId, game)) {
            return false;
        }
        Card card = game.getCard(cardId);
        if (card != null) {
            game.getState().addOtherAbility(card, LifelinkAbility.getInstance());
        }
        Permanent permanent = game.getPermanent(cardId);
        if (permanent != null) {
            permanent.addAbility(LifelinkAbility.getInstance(), source.getSourceId(), game);
        }
        return true;
    }
}
