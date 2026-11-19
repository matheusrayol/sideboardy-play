package mage.cards.t;

import mage.MageObjectReference;
import mage.ObjectColor;
import mage.abilities.Ability;
import mage.abilities.DelayedTriggeredAbility;
import mage.abilities.common.SagaAbility;
import mage.abilities.dynamicvalue.common.GreatestAmongPermanentsValue;
import mage.abilities.dynamicvalue.common.SavedDamageValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.ColorPredicate;
import mage.game.Game;
import mage.game.events.DamagedPlayerEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TreasureToken;
import mage.target.common.TargetCardInYourGraveyard;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class TheInnerLight extends CardImpl {

    private static final FilterCreatureCard filter = new FilterCreatureCard("green or white creature card from your graveyard");

    static {
        filter.add(Predicates.or(
                new ColorPredicate(ObjectColor.GREEN),
                new ColorPredicate(ObjectColor.WHITE)
        ));
    }

    public TheInnerLight(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{G}{W}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after III.)
        SagaAbility sagaAbility = new SagaAbility(this, SagaChapter.CHAPTER_III);

        // I — Return target green or white creature card from your graveyard to the battlefield.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_I, SagaChapter.CHAPTER_I,
                new ReturnFromGraveyardToBattlefieldTargetEffect(),
                new TargetCardInYourGraveyard(filter)
        );

        // II — Choose target creature. Whenever it deals combat damage to a player this turn, you draw that many cards.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_II, SagaChapter.CHAPTER_II,
                new TheInnerLightChapterIIEffect(),
                new TargetCreaturePermanent()
        );

        // III — You gain life equal to the greatest power among creatures you control. Create a Treasure token.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_III,
                new GainLifeEffect(GreatestAmongPermanentsValue.POWER_CONTROLLED_CREATURES)
                        .setText("you gain life equal to the greatest power among creatures you control"),
                new CreateTokenEffect(new TreasureToken())
        );

        this.addAbility(sagaAbility.addHint(GreatestAmongPermanentsValue.POWER_CONTROLLED_CREATURES.getHint()));
    }

    private TheInnerLight(final TheInnerLight card) {
        super(card);
    }

    @Override
    public TheInnerLight copy() {
        return new TheInnerLight(this);
    }
}

class TheInnerLightChapterIIEffect extends OneShotEffect {

    TheInnerLightChapterIIEffect() {
        super(Outcome.DrawCard);
        this.staticText = "Choose target creature. Whenever it deals combat damage to a player this turn, you draw that many cards";
    }

    private TheInnerLightChapterIIEffect(final TheInnerLightChapterIIEffect effect) {
        super(effect);
    }

    @Override
    public TheInnerLightChapterIIEffect copy() {
        return new TheInnerLightChapterIIEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null) {
            return false;
        }
        game.addDelayedTriggeredAbility(
                new TheInnerLightDelayedTriggeredAbility(new MageObjectReference(permanent, game)),
                source
        );
        return true;
    }
}

class TheInnerLightDelayedTriggeredAbility extends DelayedTriggeredAbility {

    private final MageObjectReference mor;

    TheInnerLightDelayedTriggeredAbility(MageObjectReference mor) {
        super(new DrawCardSourceControllerEffect(SavedDamageValue.MANY).setText("you draw that many cards"), Duration.EndOfTurn, false, false);
        this.mor = mor;
        setTriggerPhrase("Whenever that creature deals combat damage to a player this turn, ");
    }

    private TheInnerLightDelayedTriggeredAbility(final TheInnerLightDelayedTriggeredAbility ability) {
        super(ability);
        this.mor = ability.mor;
    }

    @Override
    public TheInnerLightDelayedTriggeredAbility copy() {
        return new TheInnerLightDelayedTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DAMAGED_PLAYER;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!((DamagedPlayerEvent) event).isCombatDamage() || event.getAmount() <= 0) {
            return false;
        }
        Permanent sourceCreature = game.getPermanentOrLKIBattlefield(event.getSourceId());
        if (sourceCreature == null || !mor.refersTo(sourceCreature, game)) {
            return false;
        }
        getEffects().setValue("damage", event.getAmount());
        return true;
    }
}
