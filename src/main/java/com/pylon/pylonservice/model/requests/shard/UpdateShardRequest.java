package com.pylon.pylonservice.model.requests.shard;

import com.pylon.pylonservice.model.domain.ShardAdditionalLink;
import com.pylon.pylonservice.model.domain.ShardRule;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collection;

/**
 * {
 *     "shardName": "fortnite",
 *     "shardFriendlyName": "Fortnite Battle Royale",
 *     "shardAvatarFilename": "f99269c2-9b0b-4dbf-b04a-385bc7ffa629.png",
 *     "shardBannerFilename": "f99269c2-9b0b-4dbf-b04a-385bc7ffa629.jpg",
 *     "shardDescription": "This is the best of all of the fortnite shards. JJOU(G*&^&F*D%^F&G*(F&GUOH)*OU!@#@$",
 *     "inheritedShardNames": [],
 *     "inheritedUsers": [
 *         "jason",
 *         "brett"
 *     ],
 *     "shardFeaturedImageFilename": "f99269c2-9b0b-4dbf-b04a-385bc7ffa629.png",
 *     "shardFeaturedImageLink": "https://google.com",
 *     "shardRules": [
 *         {
 *             "ruleName": "Rule #1",
 *             "ruleDescription": "Rule #1 description jasjoifjoihwoihfowh"
 *         },
 *         {
 *             "ruleName": "Rule #2",
 *             "ruleDescription": "Rule #2 description jasjoifjoihwoihfowh"
 *         }
 *     ],
 *     "shardAdditionalLinks": [
 *         {
 *             "title": "google",
 *             "url": "https://www.google.com/"
 *         },
 *         {
 *             "title": "facebook",
 *             "url": "https://facebook.com"
 *         }
 *     ]
 * }
 */
@Value
@EqualsAndHashCode(callSuper=true)
public class UpdateShardRequest extends ShardRequest {
    private static final long serialVersionUID = 0L;
    private static final int MAX_NUMBER_OF_RULES = 10;
    private static final int MAX_NUMBER_OF_ADDITIONAL_LINKS = 5;
    private static final int MAX_RULE_NAME_LENGTH = 40;
    private static final int MAX_RULE_DESCRIPTION_LENGTH = 140;
    private static final int MAX_ADDITIONAL_LINK_TITLE_LENGTH = 30;

    String shardFeaturedImageFilename;
    String shardFeaturedImageLink;
    Collection<ShardRule> shardRules;
    Collection<ShardAdditionalLink> shardAdditionalLinks;

    UpdateShardRequest(final String shardName,
                       final String shardFriendlyName,
                       final String shardAvatarFilename,
                       final String shardBannerFilename,
                       final String shardDescription,
                       final Collection<String> inheritedShardNames,
                       final Collection<String> inheritedUsers,
                       final String shardFeaturedImageFilename,
                       final String shardFeaturedImageLink,
                       final Collection<ShardRule> shardRules,
                       final Collection<ShardAdditionalLink> shardAdditionalLinks) {
        super(shardName, shardFriendlyName, shardAvatarFilename, shardBannerFilename, shardDescription,
            inheritedShardNames, inheritedUsers);
        this.shardFeaturedImageFilename = shardFeaturedImageFilename;
        this.shardFeaturedImageLink = shardFeaturedImageLink;
        this.shardRules = shardRules;
        this.shardAdditionalLinks = shardAdditionalLinks;
    }

    @Override
    public boolean isValid() {
        return isShardNameValid()
            && isShardFriendlyNameValid()
            && isShardAvatarFilenameValid()
            && isShardBannerFilenameValid()
            && isShardDescriptionValid()
            && isInheritedShardNamesValid()
            && isInheritedUsersValid()
            && isShardFeaturedImageFilenameValid()
            && isShardFeaturedImageLinkValid()
            && isShardRulesValid()
            && isShardAdditionalLinksValid();
    }

    private boolean isShardFeaturedImageFilenameValid() {
        return shardFeaturedImageFilename != null
            && (
                shardFeaturedImageFilename.isEmpty()
             || FILENAME_REGEX_PATTERN.matcher(shardFeaturedImageFilename).matches()
        );
    }

    private boolean isShardFeaturedImageLinkValid() {
        return shardFeaturedImageLink != null && shardFeaturedImageLink.length() < MAX_URL_LENGTH;
    }

    private boolean isShardRulesValid() {
        return shardRules != null && shardRules.size() <= MAX_NUMBER_OF_RULES &&
            shardRules.stream().allMatch(
                shardRule ->
                    isRuleNameValid(shardRule.getRuleName())
                 && isRuleDescriptionValid(shardRule.getRuleDescription())
            );
    }

    private boolean isRuleNameValid(final String ruleName) {
        return ruleName != null && ruleName.length() <= MAX_RULE_NAME_LENGTH;
    }

    private boolean isRuleDescriptionValid(final String ruleDescription) {
        return ruleDescription != null && ruleDescription.length() <= MAX_RULE_DESCRIPTION_LENGTH;
    }

    private boolean isShardAdditionalLinksValid() {
        return shardAdditionalLinks != null && shardAdditionalLinks.size() <= MAX_NUMBER_OF_ADDITIONAL_LINKS &&
            shardAdditionalLinks.stream().allMatch(
                shardAdditionalLink ->
                    isAdditionalLinkTitleValid(shardAdditionalLink.getTitle())
                 && isAdditionalLinkUrlValid(shardAdditionalLink.getUrl())
            );
    }

    private boolean isAdditionalLinkTitleValid(final String title) {
        return title != null && title.length() <= MAX_ADDITIONAL_LINK_TITLE_LENGTH;
    }

    private boolean isAdditionalLinkUrlValid(final String url) {
        return url != null && url.length() <= MAX_URL_LENGTH;
    }
}
