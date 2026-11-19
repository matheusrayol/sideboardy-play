package mage.cards.l;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.ContinuousEffectImpl;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.InfoEffect;
import mage.abilities.effects.common.continuous.BoostEnchantedEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Layer;
import mage.constants.Outcome;
import mage.constants.SubLayer;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.counters.Counter;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInYourGraveyard;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class LastVoyageOfThe extends CardImpl {

    public LastVoyageOfThe(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{3}{B}");

        // When this enchantment enters, you may put a name sticker on it, then it becomes an Aura with enchant creature. Return a creature card from your graveyard to the battlefield and attach this Aura to it.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new LastVoyageEffect()));

        // Enchanted creature gets +2/+0 for each name sticker on this Aura with seven or fewer letters.
        this.addAbility(new SimpleStaticAbility(new BoostEnchantedEffect(2, 0)
                .setText("Enchanted creature gets +2/+0 for each name sticker on this Aura with seven or fewer letters")));

        // When this Aura leaves the battlefield, sacrifice enchanted creature.
        this.addAbility(new SimpleStaticAbility(new InfoEffect(
                "When this Aura leaves the battlefield, sacrifice enchanted creature"
        )));
    }

    private LastVoyageOfThe(final LastVoyageOfThe card) {
        super(card);
    }

    @Override
    public LastVoyageOfThe copy() {
        return new LastVoyageOfThe(this);
    }
}

class LastVoyageEffect extends OneShotEffect {

    LastVoyageEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "you may put a name sticker on {this}, then it becomes an Aura with enchant creature. "
                + "Return a creature card from your graveyard to the battlefield and attach this Aura to it";
    }

    private LastVoyageEffect(final LastVoyageEffect effect) {
        super(effect);
    }

    @Override
    public LastVoyageEffect copy() {
        return new LastVoyageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Permanent auraPerm = source.getSourcePermanentIfItStillExists(game);
        if (auraPerm != null && controller.chooseUse(Outcome.BoostCreature, "Put a name sticker on " + auraPerm.getLogName() + "?", source, game)) {
            auraPerm.addCounters(new Counter("name sticker"), source.getControllerId(), source, game);
        }
        if (auraPerm != null) {
            game.addEffect(new LastVoyageAuraTypeEffect(), source);
        }
        TargetCardInYourGraveyard target = new TargetCardInYourGraveyard(
                0, 1, StaticFilters.FILTER_CARD_CREATURE_YOUR_GRAVEYARD, true
        );
        if (controller.choose(Outcome.PutCreatureInPlay, target, source, game)) {
            Card card = game.getCard(target.getFirstTarget());
            if (card != null && controller.moveCards(card, Zone.BATTLEFIELD, source, game)) {
                Permanent creaturePerm = game.getPermanent(card.getId());
                if (creaturePerm != null && auraPerm != null) {
                    creaturePerm.addAttachment(auraPerm.getId(), source, game);
                }
            }
        }
        return true;
    }
}

class LastVoyageAuraTypeEffect extends ContinuousEffectImpl {

    LastVoyageAuraTypeEffect() {
        super(Duration.WhileOnBattlefield, Layer.TypeChangingEffects_4, SubLayer.NA, Outcome.Neutral);
    }

    private LastVoyageAuraTypeEffect(final LastVoyageAuraTypeEffect effect) {
        super(effect);
    }

    @Override
    public LastVoyageAuraTypeEffect copy() {
        return new LastVoyageAuraTypeEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent permanent = source.getSourcePermanentIfItStillExists(game);
        if (permanent == null) {
            discard();
            return false;
        }
        permanent.addSubType(game, SubType.AURA);
        return true;
    }
}
