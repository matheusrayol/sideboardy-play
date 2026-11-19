package mage.cards.e;

import mage.MageInt;
import mage.MageObject;
import mage.abilities.Ability;
import mage.abilities.common.EntersBattlefieldAbility;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.CopyEffect;
import mage.abilities.keyword.FlashAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.filter.common.FilterCreatureCard;
import mage.filter.predicate.card.PutIntoGraveFromBattlefieldThisTurnPredicate;
import mage.game.Game;
import mage.players.Player;
import mage.target.Target;
import mage.target.common.TargetCardInYourGraveyard;
import mage.util.functions.CopyApplier;
import mage.watchers.common.CardsPutIntoGraveyardWatcher;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class EzriDaxNewlyJoined extends CardImpl {

    public EzriDaxNewlyJoined(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{1}{W}");

        this.supertype.add(SuperType.LEGENDARY);
        this.subtype.add(SubType.TRILL);
        this.subtype.add(SubType.ADVISOR);
        this.power = new MageInt(2);
        this.toughness = new MageInt(2);

        // Flash
        this.addAbility(FlashAbility.getInstance());

        // You may have Ezri Dax enter as a copy of any creature card in your graveyard that was put there from the battlefield this turn,
        // except her name is Ezri Dax, Newly Joined and she's a legendary Trill in addition to her other types.
        this.addAbility(new EntersBattlefieldAbility(new EzriDaxCopyEffect(), true), new CardsPutIntoGraveyardWatcher());
    }

    private EzriDaxNewlyJoined(final EzriDaxNewlyJoined card) {
        super(card);
    }

    @Override
    public EzriDaxNewlyJoined copy() {
        return new EzriDaxNewlyJoined(this);
    }
}

class EzriDaxCopyEffect extends OneShotEffect {

    private static final FilterCreatureCard filter = new FilterCreatureCard(
            "creature card in your graveyard that was put there from the battlefield this turn"
    );

    static {
        filter.add(PutIntoGraveFromBattlefieldThisTurnPredicate.instance);
    }

    EzriDaxCopyEffect() {
        super(Outcome.Copy);
        this.staticText = "as a copy of any creature card in your graveyard that was put there from the battlefield this turn, "
                + "except her name is Ezri Dax, Newly Joined and she's a legendary Trill in addition to her other types";
    }

    private EzriDaxCopyEffect(final EzriDaxCopyEffect effect) {
        super(effect);
    }

    @Override
    public EzriDaxCopyEffect copy() {
        return new EzriDaxCopyEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        if (controller == null) {
            return false;
        }
        Target target = new TargetCardInYourGraveyard(filter);
        target.withNotTarget(true);
        if (target.canChoose(source.getControllerId(), source, game)) {
            controller.choose(Outcome.Copy, target, source, game);
            Card copyFromCard = game.getCard(target.getFirstTarget());
            if (copyFromCard != null) {
                CopyEffect copyEffect = new CopyEffect(Duration.Custom, copyFromCard, source.getSourceId());
                copyEffect.setApplier(EzriDaxCopyApplier.instance);
                game.addEffect(copyEffect, source);
                return true;
            }
        }
        return false;
    }
}

class EzriDaxCopyApplier extends CopyApplier {

    static final EzriDaxCopyApplier instance = new EzriDaxCopyApplier();

    @Override
    public boolean apply(Game game, MageObject blueprint, Ability source, UUID copyToObjectId) {
        blueprint.setName("Ezri Dax, Newly Joined");
        blueprint.addSuperType(SuperType.LEGENDARY);
        blueprint.addSubType(SubType.TRILL);
        return true;
    }
}
