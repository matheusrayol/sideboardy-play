package mage.cards.q;

import mage.abilities.Ability;
import mage.abilities.common.CastAsThoughItHadFlashIfConditionAbility;
import mage.abilities.common.SimpleStaticAbility;
import mage.abilities.condition.common.TeamworkCondition;
import mage.abilities.effects.common.AttachEffect;
import mage.abilities.effects.common.continuous.BoostEnchantedEffect;
import mage.abilities.effects.common.continuous.LoseAllAbilitiesAttachedEffect;
import mage.abilities.keyword.EnchantAbility;
import mage.abilities.keyword.TeamworkAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.AttachmentType;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.target.TargetPermanent;
import mage.target.common.TargetCreaturePermanent;

import java.util.UUID;

/**
 * @author Sideboardy
 */
public final class QuantumReduction extends CardImpl {

    public QuantumReduction(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{1}{U}");

        this.subtype.add(SubType.AURA);

        // Teamwork 2
        this.addAbility(new TeamworkAbility(2));

        // You may cast this spell as though it had flash if it's cast using teamwork.
        this.addAbility(new CastAsThoughItHadFlashIfConditionAbility(
                TeamworkCondition.instance,
                "you may cast this spell as though it had flash if it's cast using teamwork."
        ));

        // Enchant creature
        TargetPermanent auraTarget = new TargetCreaturePermanent();
        this.getSpellAbility().addTarget(auraTarget);
        this.getSpellAbility().addEffect(new AttachEffect(Outcome.UnboostCreature));
        this.addAbility(new EnchantAbility(auraTarget));

        // Enchanted creature gets -5/-0 and loses all abilities.
        Ability ability = new SimpleStaticAbility(new BoostEnchantedEffect(-5, 0));
        ability.addEffect(new LoseAllAbilitiesAttachedEffect(AttachmentType.AURA).setText("and loses all abilities"));
        this.addAbility(ability);
    }

    private QuantumReduction(final QuantumReduction card) {
        super(card);
    }

    @Override
    public QuantumReduction copy() {
        return new QuantumReduction(this);
    }
}
