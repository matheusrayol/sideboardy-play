package mage.cards.l;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.common.DifferentlyNamedPermanentCount;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.continuous.SetBasePowerToughnessSourceEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.cards.Cards;
import mage.cards.CardsImpl;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.filter.common.FilterLandCard;
import mage.game.Game;
import mage.players.Player;
import mage.target.common.TargetCardInHand;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class LtArexEverAdapting extends CardImpl {

    static final DifferentlyNamedPermanentCount landCount =
            new DifferentlyNamedPermanentCount(StaticFilters.FILTER_CONTROLLED_PERMANENT_LANDS);

    public LtArexEverAdapting(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{5}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.EDOSIAN);
        this.subtype.add(SubType.OFFICER);
        this.power = new MageInt(0);
        this.toughness = new MageInt(0);

        // Trample
        this.addAbility(TrampleAbility.getInstance());

        // Strange new worlds — Lt. Arex's power and toughness are each equal to the number of differently named lands you control.
        this.addAbility(new SimpleStaticAbility(
                Zone.ALL,
                new SetBasePowerToughnessSourceEffect(landCount)
                        .setText("{this}'s power and toughness are each equal to the number of differently named lands you control")
        ).setAbilityWord(AbilityWord.STRANGE_NEW_WORLDS).addHint(landCount.getHint()));

        // When Lt. Arex enters, draw three cards. You may put up to three land cards from your hand onto the battlefield tapped.
        Ability ability = new EntersBattlefieldTriggeredAbility(new DrawCardSourceControllerEffect(3));
        ability.addEffect(new LtArexPutLandsEffect());
        this.addAbility(ability);
    }

    private LtArexEverAdapting(final LtArexEverAdapting card) {
        super(card);
    }

    @Override
    public LtArexEverAdapting copy() {
        return new LtArexEverAdapting(this);
    }
}

class LtArexPutLandsEffect extends OneShotEffect {

    private static final FilterLandCard filter = new FilterLandCard("land cards");

    LtArexPutLandsEffect() {
        super(Outcome.PutLandInPlay);
        this.staticText = "You may put up to three land cards from your hand onto the battlefield tapped";
    }

    private LtArexPutLandsEffect(final LtArexPutLandsEffect effect) {
        super(effect);
    }

    @Override
    public LtArexPutLandsEffect copy() {
        return new LtArexPutLandsEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetCardInHand target = new TargetCardInHand(0, 3, filter);
        if (controller.choose(Outcome.PutLandInPlay, target, source, game)) {
            Cards cards = new CardsImpl(target.getTargets());
            if (!cards.isEmpty()) {
                controller.moveCards(cards.getCards(game), Zone.BATTLEFIELD, source, game, true, false, false, null);
            }
        }
        return true;
    }
}
