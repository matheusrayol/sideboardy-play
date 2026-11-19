package mage.cards.q;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.common.CreateTokenEffect;
import mage.abilities.triggers.BeginningOfUpkeepTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterControlledPermanent;
import mage.filter.predicate.permanent.TokenPredicate;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.game.permanent.token.TreasureToken;

import java.util.UUID;

/**
 * @author sideboardy-play
 */
public final class QuarkHumbleProprietor extends CardImpl {

    public QuarkHumbleProprietor(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.FERENGI);
        this.subtype.add(SubType.ROGUE);
        this.power = new MageInt(1);
        this.toughness = new MageInt(2);

        // You can't sacrifice Treasure tokens.
        this.addAbility(new SimpleStaticAbility(new QuarkHumbleProprietorEffect()));

        // At the beginning of each upkeep, create a Treasure token.
        this.addAbility(new BeginningOfUpkeepTriggeredAbility(
                TargetController.ANY,
                new CreateTokenEffect(new TreasureToken()),
                false
        ));
    }

    private QuarkHumbleProprietor(final QuarkHumbleProprietor card) {
        super(card);
    }

    @Override
    public QuarkHumbleProprietor copy() {
        return new QuarkHumbleProprietor(this);
    }
}

class QuarkHumbleProprietorEffect extends ContinuousEffectImpl {

    private static final FilterControlledPermanent filter = new FilterControlledPermanent(SubType.TREASURE);

    static {
        filter.add(TokenPredicate.TRUE);
    }

    QuarkHumbleProprietorEffect() {
        super(Duration.WhileOnBattlefield, Layer.RulesEffects, SubLayer.NA, Outcome.Detriment);
        this.staticText = "You can't sacrifice Treasure tokens";
    }

    private QuarkHumbleProprietorEffect(final QuarkHumbleProprietorEffect effect) {
        super(effect);
    }

    @Override
    public QuarkHumbleProprietorEffect copy() {
        return new QuarkHumbleProprietorEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        for (Permanent permanent : game.getBattlefield().getActivePermanents(filter, source.getControllerId(), source, game)) {
            permanent.setCanBeSacrificed(false);
        }
        return true;
    }
}
