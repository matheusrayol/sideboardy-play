package mage.cards.d;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.effects.common.GainLifeEffect;
import mage.abilities.effects.common.LoseLifeOpponentsEffect;
import mage.abilities.effects.common.MayTapOrUntapTargetEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.RollDieType;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.events.DieRolledEvent;
import mage.game.events.GameEvent;
import mage.game.permanent.token.TokenImpl;
import mage.target.TargetPermanent;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class DeeKayFinderOfTheLost extends CardImpl {

    public DeeKayFinderOfTheLost(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{U}{B}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ZOMBIE);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(1);
        this.toughness = new MageInt(4);

        // When Dee Kay enters, open an Attraction.
        this.addAbility(new EntersBattlefieldTriggeredAbility(
                new CreateTokenEffect(new DeeKayAttractionToken())
                        .setText("open an Attraction. (Put the top card of your Attraction deck onto the battlefield.)")
        ));

        // Whenever you roll a 2, each opponent loses 1 life and you gain 1 life.
        Ability r2 = new DeeKayDieTriggeredAbility(
                2, new LoseLifeOpponentsEffect(1),
                "Whenever you roll a 2, each opponent loses 1 life and you gain 1 life."
        );
        r2.addEffect(new GainLifeEffect(1).concatBy("and"));
        this.addAbility(r2);

        // Whenever you roll a 4, you may tap or untap target artifact or creature.
        Ability r4 = new DeeKayDieTriggeredAbility(
                4, new MayTapOrUntapTargetEffect(),
                "Whenever you roll a 4, you may tap or untap target artifact or creature."
        );
        r4.addTarget(new TargetPermanent(StaticFilters.FILTER_PERMANENT_ARTIFACT_OR_CREATURE));
        this.addAbility(r4);

        // Whenever you roll a 6, return target creature card from your graveyard to your hand.
        Ability r6 = new DeeKayDieTriggeredAbility(
                6, new ReturnFromGraveyardToHandTargetEffect(),
                "Whenever you roll a 6, return target creature card from your graveyard to your hand."
        );
        r6.addTarget(new TargetCardInYourGraveyard(StaticFilters.FILTER_CARD_CREATURE_YOUR_GRAVEYARD));
        this.addAbility(r6);
    }

    private DeeKayFinderOfTheLost(final DeeKayFinderOfTheLost card) {
        super(card);
    }

    @Override
    public DeeKayFinderOfTheLost copy() {
        return new DeeKayFinderOfTheLost(this);
    }
}

class DeeKayDieTriggeredAbility extends TriggeredAbilityImpl {

    private final int targetRoll;
    private final String ruleText;

    DeeKayDieTriggeredAbility(int targetRoll, Effect effect, String ruleText) {
        super(Zone.BATTLEFIELD, effect, false);
        this.targetRoll = targetRoll;
        this.ruleText = ruleText;
    }

    private DeeKayDieTriggeredAbility(final DeeKayDieTriggeredAbility ability) {
        super(ability);
        this.targetRoll = ability.targetRoll;
        this.ruleText = ability.ruleText;
    }

    @Override
    public DeeKayDieTriggeredAbility copy() {
        return new DeeKayDieTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.DIE_ROLLED;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getTargetId())) {
            return false;
        }
        DieRolledEvent drEvent = (DieRolledEvent) event;
        return drEvent.getRollDieType() == RollDieType.NUMERICAL && drEvent.getResult() == targetRoll;
    }

    @Override
    public String getRule() {
        return ruleText;
    }
}

class DeeKayAttractionToken extends TokenImpl {

    DeeKayAttractionToken() {
        super("Attraction", "Attraction artifact token");
        cardType.add(CardType.ARTIFACT);
        subtype.add(SubType.ATTRACTION);
    }

    private DeeKayAttractionToken(final DeeKayAttractionToken token) {
        super(token);
    }

    @Override
    public DeeKayAttractionToken copy() {
        return new DeeKayAttractionToken(this);
    }
}
