package mage.cards.f;

import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldTriggeredAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.BoostEnchantedEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.counters.Counter;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetControlledCreaturePermanent;

import java.util.UUID;

/**
 *
 * @author sideboardy-play
 */
public final class FightTheFight extends CardImpl {

    public FightTheFight(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{G}");

        this.subtype.add(SubType.AURA);

        // Enchant creature you control
        TargetPermanent auraTarget = new TargetControlledCreaturePermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.BoostCreature));
        this.addAbility(new EnchantAbility(auraTarget));

        // When this Aura enters, you may put a name sticker on it. When you do, enchanted creature fights up to one target creature you don't control.
        this.addAbility(new EntersBattlefieldTriggeredAbility(new FightTheFightEnterEffect(), true));

        // Enchanted creature gets +0/+2 for each name sticker on this Aura with eight or more letters.
        this.addAbility(new SimpleStaticAbility(new BoostEnchantedEffect(0, 2)
                .setText("Enchanted creature gets +0/+2 for each name sticker on this Aura with eight or more letters")));
    }

    private FightTheFight(final FightTheFight card) {
        super(card);
    }

    @Override
    public FightTheFight copy() {
        return new FightTheFight(this);
    }
}

class FightTheFightEnterEffect extends OneShotEffect {

    FightTheFightEnterEffect() {
        super(Outcome.BoostCreature);
        staticText = "put a name sticker on {this}. When you do, enchanted creature fights up to one target creature you don't control";
    }

    private FightTheFightEnterEffect(final FightTheFightEnterEffect effect) {
        super(effect);
    }

    @Override
    public FightTheFightEnterEffect copy() {
        return new FightTheFightEnterEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent aura = source.getSourcePermanentIfItStillExists(game);
        if (controller == null || aura == null) {
            return false;
        }
        aura.addCounters(new Counter("name sticker"), source.getControllerId(), source, game);
        Permanent enchanted = game.getPermanent(aura.getAttachedTo());
        if (enchanted == null) {
            return true;
        }
        TargetPermanent target = new TargetPermanent(0, 1, StaticFilters.FILTER_CREATURE_YOU_DONT_CONTROL, true);
        if (controller.choose(Outcome.Damage, target, source, game)) {
            Permanent oppCreature = game.getPermanent(target.getFirstTarget());
            if (oppCreature != null) {
                enchanted.fight(oppCreature, source, game);
            }
        }
        return true;
    }
}
