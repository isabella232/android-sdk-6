package com.configcat;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class RolloutRule {
    /**
     * Value served when the rule is selected during evaluation.
     */
    @SerializedName(value = "v")
    public JsonElement value;

    /**
     * The user attribute used in the comparison during evaluation.
     */
    @SerializedName(value = "a")
    public String comparisonAttribute;

    /**
     * The operator used in the comparison.
     *
     * 0:  'IS ONE OF',
     * 1:  'IS NOT ONE OF',
     * 2:  'CONTAINS',
     * 3:  'DOES NOT CONTAIN',
     * 4:  'IS ONE OF (SemVer)',
     * 5:  'IS NOT ONE OF (SemVer)',
     * 6:  'lt (SemVer)',
     * 7:  'lte (SemVer)',
     * 8:  'gt (SemVer)',
     * 9:  'gte (SemVer)',
     * 10: 'equal (Number)',
     * 11: 'not equal (Number)',
     * 12: 'lt (Number)',
     * 13: 'lte (Number)',
     * 14: 'gt (Number)',
     * 15: 'gte (Number)',
     * 16: 'IS ONE OF (Sensitive)',
     * 17: 'IS NOT ONE OF (Sensitive)'
     */
    @SerializedName(value = "t")
    public int comparator;

    /**
     * The comparison value compared to the given user attribute.
     */
    @SerializedName(value = "c")
    public String comparisonValue;

    /**
     * The rule's variation ID (for analytical purposes).
     */
    @SerializedName(value = "i")
    public String variationId;
}
