package mage.cards.o;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.common.SagaAbility;
import mage.abilities.condition.common.SourceRemainsInZoneCondition;
import mage.abilities.decorator.ConditionalContinuousEffect;
import mage.abilities.effects.common.DrawCardSourceControllerEffect;
import mage.abilities.effects.common.PreventDamageByTargetEffect;
import mage.abilities.effects.common.continuous.GainAbilityTargetEffect;
import mage.abilities.keyword.HexproofAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.SagaChapter;
import mage.constants.SubType;
import mage.constants.Zone;
import mage.game.Game;
import mage.target.common.TargetControlledCreaturePermanent;
import mage.target.common.TargetCreaturePermanent;

/**
 *
 * @author muz
 */
public final class OldFatSpiderCantSeeMe extends CardImpl {

    public OldFatSpiderCantSeeMe(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{2}{U}");

        this.subtype.add(SubType.SAGA);

        // (As this Saga enters and after your draw step, add a lore counter. Sacrifice after IV.)
        SagaAbility sagaAbility = new SagaAbility(this, SagaChapter.CHAPTER_IV);

        // I — Target creature you control gains hexproof for as long as this Saga remains on the battlefield.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_I, SagaChapter.CHAPTER_I,
                new ConditionalContinuousEffect(
                        new GainAbilityTargetEffect(HexproofAbility.getInstance(), Duration.Custom),
                        new SourceRemainsInZoneCondition(Zone.BATTLEFIELD),
                        "target creature you control gains hexproof for as long as {this} remains on the battlefield"
                ),
                new TargetControlledCreaturePermanent()
        );

        // II — Prevent all damage that would be dealt by up to one target creature for as long as this Saga remains on the battlefield.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_II, SagaChapter.CHAPTER_II,
                new OldFatSpiderPreventDamageEffect(),
                new TargetCreaturePermanent(0, 1)
        );

        // III, IV — Draw a card.
        sagaAbility.addChapterEffect(
                this, SagaChapter.CHAPTER_III, SagaChapter.CHAPTER_IV,
                new DrawCardSourceControllerEffect(1)
        );

        this.addAbility(sagaAbility);
    }

    private OldFatSpiderCantSeeMe(final OldFatSpiderCantSeeMe card) {
        super(card);
    }

    @Override
    public OldFatSpiderCantSeeMe copy() {
        return new OldFatSpiderCantSeeMe(this);
    }
}

class OldFatSpiderPreventDamageEffect extends PreventDamageByTargetEffect {

    OldFatSpiderPreventDamageEffect() {
        super(Duration.Custom, false);
        staticText = "prevent all damage that would be dealt by up to one target creature for as long as {this} remains on the battlefield";
    }

    private OldFatSpiderPreventDamageEffect(final OldFatSpiderPreventDamageEffect effect) {
        super(effect);
    }

    @Override
    public OldFatSpiderPreventDamageEffect copy() {
        return new OldFatSpiderPreventDamageEffect(this);
    }

    @Override
    public boolean isInactive(Ability source, Game game) {
        if (source.getSourcePermanentIfItStillExists(game) == null) {
            return true;
        }
        return super.isInactive(source, game);
    }
}
