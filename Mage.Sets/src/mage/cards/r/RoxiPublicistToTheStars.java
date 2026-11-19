package mage.cards.r;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.dynamicvalue.DynamicValue;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.continuous.SetBasePowerSourceEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.Zone;
import mage.counters.Counter;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetControlledPermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class RoxiPublicistToTheStars extends CardImpl {

    public RoxiPublicistToTheStars(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{2}{U}{R}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.EMPLOYEE);
        this.power = new MageInt(0);
        this.toughness = new MageInt(4);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // Roxi's power is equal to the number of permanents you control with an art sticker plus the number of cards in your graveyard with an art sticker.
        this.addAbility(new SimpleStaticAbility(Zone.ALL, new SetBasePowerSourceEffect(RoxiArtStickerValue.instance)
                .setText("{this}'s power is equal to the number of permanents you control with an art sticker "
                        + "plus the number of cards in your graveyard with an art sticker")));

        // When Roxi enters, distribute up to two art stickers among one or two nonland permanents you own.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new RoxiStickerEffect()));
    }

    private RoxiPublicistToTheStars(final RoxiPublicistToTheStars card) {
        super(card);
    }

    @Override
    public RoxiPublicistToTheStars copy() {
        return new RoxiPublicistToTheStars(this);
    }
}

enum RoxiArtStickerValue implements DynamicValue {
    instance;

    @Override
    public int calculate(Game game, Ability sourceAbility, Effect effect) {
        int count = 0;
        for (Permanent permanent : game.getBattlefield().getAllActivePermanents(
                StaticFilters.FILTER_PERMANENT, sourceAbility.getControllerId(), game
        )) {
            if (permanent.getCounters(game).getCount("art sticker") > 0) {
                count++;
            }
        }
        Player controller = game.getPlayer(sourceAbility.getControllerId());
        if (controller != null) {
            for (Card card : controller.getGraveyard().getCards(game)) {
                if (card.getCounters(game).getCount("art sticker") > 0) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public RoxiArtStickerValue copy() {
        return instance;
    }

    @Override
    public String getMessage() {
        return "permanents you control with an art sticker plus cards in your graveyard with an art sticker";
    }
}

class RoxiStickerEffect extends OneShotEffect {

    RoxiStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "distribute up to two art stickers among one or two nonland permanents you own";
    }

    private RoxiStickerEffect(final RoxiStickerEffect effect) {
        super(effect);
    }

    @Override
    public RoxiStickerEffect copy() {
        return new RoxiStickerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        for (int i = 0; i < 2; i++) {
            TargetControlledPermanent target = new TargetControlledPermanent(
                    0, 1, StaticFilters.FILTER_CONTROLLED_PERMANENT_NON_LAND, true
            );
            if (!controller.choose(Outcome.Benefit, target, source, game)) {
                break;
            }
            Permanent permanent = game.getPermanent(target.getFirstTarget());
            if (permanent == null) {
                break;
            }
            permanent.addCounters(new Counter("art sticker"), source.getControllerId(), source, game);
        }
        return true;
    }
}
