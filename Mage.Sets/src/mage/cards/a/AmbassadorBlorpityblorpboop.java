package mage.cards.a;

import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.counter.GetTicketCountersControllerEffect;
import mage.abilities.triggers.BeginningOfCombatTriggeredAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
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
public final class AmbassadorBlorpityblorpboop extends CardImpl {

    public AmbassadorBlorpityblorpboop(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{G}{U}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.ALIEN);
        this.subtype.add(SubType.ADVISOR);
        this.subtype.add(SubType.GUEST);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // When Ambassador Blorpityblorpboop enters, you get {TK}{TK}{TK}, then you may put a sticker on a nonland permanent you own.
        Ability ability = new EntersBattlefieldTriggeredAbility(new GetTicketCountersControllerEffect(3));
        ability.addEffect(new AmbassadorStickerEffect());
        this.addAbility(ability);

        // At the beginning of each combat, you may have Ambassador Blorpityblorpboop's base power become equal to the total power of all stickers on permanents you control and its base toughness become equal to those stickers' total toughness.
        this.addAbility(new BeginningOfCombatTriggeredAbility(
                TargetController.ANY,
                new InfoEffect("you may have {this}'s base power become equal to the total power of all stickers on permanents you control and its base toughness become equal to those stickers' total toughness"),
                true
        ));
    }

    private AmbassadorBlorpityblorpboop(final AmbassadorBlorpityblorpboop card) {
        super(card);
    }

    @Override
    public AmbassadorBlorpityblorpboop copy() {
        return new AmbassadorBlorpityblorpboop(this);
    }
}

class AmbassadorStickerEffect extends OneShotEffect {

    AmbassadorStickerEffect() {
        super(Outcome.BoostCreature);
        staticText = "then you may put a sticker on a nonland permanent you own";
    }

    private AmbassadorStickerEffect(final AmbassadorStickerEffect effect) {
        super(effect);
    }

    @Override
    public AmbassadorStickerEffect copy() {
        return new AmbassadorStickerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        TargetControlledPermanent target = new TargetControlledPermanent(
                0, 1, StaticFilters.FILTER_CONTROLLED_PERMANENT_NON_LAND, true
        );
        if (controller.choose(Outcome.Benefit, target, source, game)) {
            Permanent permanent = game.getPermanent(target.getFirstTarget());
            if (permanent != null) {
                permanent.addCounters(new Counter("sticker"), source.getControllerId(), source, game);
            }
        }
        return true;
    }
}
